# sm-console MySQL 迁移实施方案

> 版本：v1.0  
> 适用范围：将 `console-admin` 模块的底层数据库由 H2（内嵌文件模式）整体迁移到 MySQL Community Server 8.4。

---

## 1. 目标与原则

### 1.1 迁移目标
1. 将生产与开发环境的数据库由 H2 替换为 **MySQL Community Server 8.4 (LTS)**。
2. 数据库 schema 统一使用 **utf8mb4** 字符集 + **utf8mb4_0900_ai_ci** 排序规则。
3. 清理代码中所有 H2 强耦合（Web 控制台、`.mv.db` 文件判断、H2 方言 SQL、H2 备份/恢复工具）。
4. 为后续业务扩展预留稳定的数据库底座。

### 1.2 约束与原则
- **不引入 Spring Boot**，继续使用 Spring 原生组件（`@Configuration` / `@Bean` / `spring-tx` / `mybatis-spring`）。
- **保留现有 c3p0 连接池**，不替换为 HikariCP 或 Druid，降低侵入性。
- **本地开发使用 Homebrew 安装的 MySQL 8.4**，不再提供 `docker-compose.yml`。
- 严格遵循项目 Copilot 指南中的"最小且安全改动"原则，不做无关重构。
- 迁移过程保留完整回滚路径，分支合并前旧 H2 版本随时可回退。

---

## 2. 版本与依赖

### 2.1 MySQL 服务端
- **版本**：MySQL Community Server **8.4.x** (LTS)。
- **默认认证插件**：`caching_sha2_password`（8.4 默认）。
- **安装方式（macOS 本地）**：
  ```bash
  brew install mysql            # 若尚未安装
  brew services start mysql     # 后台启动
  mysql_secure_installation     # 初始化 root 密码等
  ```
- **端口**：默认 3306。

### 2.2 JDBC 驱动
- 移除 `com.h2database:h2` 依赖（`compile` 与 `test` scope 均移除）。
- 引入：

  ```xml
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
  </dependency>
  ```
  > 注意：坐标已从老的 `mysql:mysql-connector-java` 迁移到 `com.mysql:mysql-connector-j`。

### 2.3 连接池
- 保留 **c3p0**，继续使用 `application-datasource.yml` 中已有的 c3p0 语义字段
  （`initialPoolSize / minPoolSize / maxPoolSize / acquireIncrement /
    acquireRetryAttempts / maxIdleTime / maxStatements / idleConnectionTestPeriod`）。
- 增加 MySQL 连接有效性检测（详见 §4.2）。

---

## 3. Schema 迁移规范

### 3.1 字符集与排序规则
- **库级**：
  ```sql
  CREATE DATABASE consoledb
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
  ```
- **表级**：`ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci`
- **需严格区分大小写的字段**（如密码哈希、license key、token）：单独指定 `COLLATE utf8mb4_bin`。
- **连接级**：JDBC URL 必须带 `characterEncoding=utf8&useUnicode=true`，防止驱动端二次转码。

### 3.2 VARCHAR → TEXT 转换规则

| 原长度 M | 迁移后类型 | 备注 |
|---|---|---|
| `M ≤ 1000` | 保持 `VARCHAR(M)` | 对索引、排序临时表友好 |
| `1000 < M ≤ 16000` | 转为 `TEXT` | 不占用行长度预算 |
| `M > 16000` | 转为 `MEDIUMTEXT` | 规避 65535 字节单行硬限制 |

> 说明：最初拟定阈值为 utf8mb4 理论上限 16383，但 MySQL 单行 65535 字节是**所有字段共享**的预算，实践中超过 1000 的 VARCHAR 已不再适合做索引和排序，故下调为 1000。

**当前仓库需转换的字段清单**（位于 `console-admin/sql/1.commons.sql`）：

| 表 | 字段 | 原类型 | 迁移后类型 |
|---|---|---|---|
| `cnsl_template` | `temp_content` | `varchar(9000)` | `TEXT` |
| `cnsl_command_history` | `cmd_msg` | `varchar(10000)` | `TEXT` |
| `cnsl_command_history` | `res_msg` | `varchar(5000)` | `TEXT` |
| `cnsl_command_history` | `cmd_file` | `varchar(1000)` | 保持 `VARCHAR(1000)` |

### 3.3 DDL 语法调整清单

| 原写法（H2/老 MySQL） | MySQL 8.4 推荐写法 | 原因 |
|---|---|---|
| `bigint(20)`、`int(4)` | `bigint`、`int` | 8.x 起显示宽度被废弃并 warning |
| `DEFAULT CHARSET=utf8` | `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci` | 统一字符集 |
| `create_time datetime` (无默认值) | `create_time datetime DEFAULT CURRENT_TIMESTAMP` | 严格模式下 insert 不传值会报错 |
| `update_time datetime` (无默认值) | `update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 自动维护更新时间 |
| H2 字符串函数如 `DATEADD('DAY', ?, CURRENT_TIMESTAMP())` | `DATE_ADD(NOW(), INTERVAL ? DAY)` | MySQL 原生函数 |

### 3.4 SQL 脚本重组

现有脚本位于 `console-admin/sql/`：
- `1.commons.sql`

迁移后保持文件分拆，但需要：
1. 按 §3.1–§3.3 统一调整 DDL。
2. 在每个文件头部加入：
   ```sql
   SET NAMES utf8mb4;
   SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION';
   ```
3. 保证脚本**幂等性**：所有 `CREATE TABLE` 前保留 `DROP TABLE IF EXISTS`。

---

## 4. 配置变更

### 4.1 数据源配置
按项目约定：**生产可变项**放 `console-admin/src/main/run-resources/config/application.yml`，**默认/不变项**放 `console-admin/src/main/resources/application-datasource.yml`。

`application-datasource.yml`（编译期打包，不建议运维修改）：
```yaml
datasource:
  driverClassName: com.mysql.cj.jdbc.Driver
  # 具体 url/username/password 由 run-resources/config/application.yml 覆盖
  initialPoolSize: 10
  minPoolSize: 10
  maxPoolSize: 50
  acquireIncrement: 3
  acquireRetryAttempts: 3
  maxIdleTime: 300
  maxStatements: 50
  idleConnectionTestPeriod: 60
```

`run-resources/config/application.yml`（发布后可调整）：
```yaml
datasource:
  url: jdbc:mysql://127.0.0.1:3306/consoledb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
  username: 'console'
  password: 'change-me'
```

移除 `application-datasource.yml` 中 H2 专属项：
- `dataDevDefaultPort`
- `dataDevDefaultHosts`

### 4.2 c3p0 连接池补充配置
在 c3p0 DataSource 的 Java 装配代码中追加：

```java
dataSource.setPreferredTestQuery("SELECT 1");
dataSource.setTestConnectionOnCheckin(false);
dataSource.setTestConnectionOnCheckout(false);   // 仅依赖 idleConnectionTestPeriod 周期性测试
dataSource.setCheckoutTimeout(30_000);           // 30s 获取连接超时
dataSource.setUnreturnedConnectionTimeout(0);    // 保持默认
```

### 4.3 MyBatis 配置
`mybatis-config.xml`：确认 `<setting name="jdbcTypeForNull" value="NULL"/>`，避免 MyBatis 在 MySQL 下对 null 参数的类型推断问题。

---

## 5. 代码层改造清单

### 5.1 初始化器：`H2DatabaseInitializer` → `MySqlDatabaseInitializer`
- **路径**：`console-admin/src/main/java/com/uh/system/service/`
- **判断条件变更**：由原来的"`./data/db/consoledb.mv.db` 文件不存在即初始化"，改为：
  > 连接数据库后查询 `information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_user'`；若不存在则执行 `sql/1.commons.sql`。
- `updateAdminPasswordExpired` 方法中的 SQL：
  ```java
  // 原: UPDATE sys_user SET password_expired = DATEADD('DAY', ?, CURRENT_TIMESTAMP()) WHERE user_name = ?
  // 新:
  "UPDATE sys_user SET password_expired = DATE_ADD(NOW(), INTERVAL ? DAY) WHERE user_name = ?"
  ```
- 保留旧类一段时间并加 `@Deprecated`，方便灰度；正式删除时机见 §8.3。

### 5.2 备份/恢复：`DatabaseServiceImpl`
- 移除对 `org.h2.tools.RunScript` 的依赖。
- 改用**外部命令**方式调用 `mysqldump` / `mysql` 实现备份与恢复：
    - 备份：`mysqldump --single-transaction --default-character-set=utf8mb4 -h HOST -P PORT -u USER -pPWD DB > export-YYYYMMDDHHMMSS.sql`
    - 恢复：`mysql --default-character-set=utf8mb4 -h HOST -P PORT -u USER -pPWD DB < export-xxx.sql`
    - 通过 `ProcessBuilder` 调用，并将输出重定向到 `apphome/logs/`。
- 输出文件后缀由 `.zip` 改为 `.sql.gz`，配合 `gzip` 做压缩。
- 清理 `cleanExpiredBackup` 中的后缀判断逻辑以匹配新扩展名。
- 更新 README 的"数据库备份 / 恢复"章节，说明：**操作所在机器需安装 `mysqldump` 与 `mysql` 客户端**。

### 5.3 移除 H2 Web 控制台
- 删除 `DatabaseServiceImpl#startH2Console()` / `stopH2Console()` 方法及其调用点。
- 删除"开发者模式"中对 H2 控制台的启动分支（检索 `switch on development mode` 相关逻辑）。
- 移除 `application-datasource.yml` 中 `dataDevDefaultPort`、`dataDevDefaultHosts` 字段与对应 `DataSourceConf` 属性。
- 从 README 中删除"H2数据库维护控制台"章节，替换为 MySQL 本地访问提示（`mysql -uroot -p consoledb` 或 DBeaver/Sequel Ace 等客户端）。
- 清理 `console-admin/lib/h2-*.jar` 本地调试 jar 及相关文档。

### 5.4 全仓库 H2 方言排查（参考 grep 列表）
建议逐项搜索并替换：

| 关键词 | 处理方式 |
|---|---|
| `org.h2.` | 全部移除或替换 |
| `jdbc:h2:` | 替换为 `jdbc:mysql:` |
| `DATEADD(` | 改为 `DATE_ADD(`；注意第二个参数语义 |
| `CURRENT_TIMESTAMP()` | 改为 `NOW()` 或 `CURRENT_TIMESTAMP`（注意 MyBatis XML 中的转义） |
| `MODE=MySQL` | 移除 |
| `.mv.db` | 移除（连初始化判定一起重构） |
| `RunScript` | 替换为 mysqldump / mysql 调用 |

### 5.5 `ConsoleApplication` 启动流程
- 启动时调用 `MySqlDatabaseInitializer.initializeIfAbsent()` 替代原 `H2DatabaseInitializer`。
- 启动日志打印实际连接的 JDBC URL（屏蔽密码），便于运维确认目标库。

---

## 6. Maven 构建与目录结构

- `console-admin/pom.xml`：
    - 移除 H2 依赖。
    - 新增 `mysql-connector-j:8.4.0`。
    - 保留 c3p0 依赖不动。
- `console-admin/lib/h2-*.jar`：删除。
- `apphome/data/db/`：保留目录结构（存放 `mysqldump` 导出文件）；文件内容由 H2 `*.mv.db` 变为导出的 `*.sql.gz`，必要时在 `console.sh` 中加 `data/db` 初始化逻辑。
- `apphome/bin/console.sh`、`console.bat`：`CMD_OPTS` 行为保持不变，`-i` 参数仍用于触发 §5.1 新的初始化器。

---

## 7. 本地开发环境搭建（macOS / Homebrew）

### 7.1 MySQL 配置

在 `~/.my.cnf`（或 `/opt/homebrew/etc/my.cnf`）追加：
```ini
[mysqld]
character-set-server = utf8mb4
collation-server     = utf8mb4_0900_ai_ci
default-time-zone    = '+08:00'

[client]
default-character-set = utf8mb4

[mysql]
default-character-set = utf8mb4
```
重启：`brew services restart mysql`。

### 7.2 创建开发库和账号

```sql
CREATE DATABASE consoledb
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'console'@'localhost' IDENTIFIED BY 'console123';
GRANT ALL PRIVILEGES ON consoledb.* TO 'console'@'localhost';
FLUSH PRIVILEGES;
```

### 7.3 本地运行项目
1. 打包：`cd console-admin && mvn clean package -DskipTests=true`
2. 启动：`cd console-admin/apphome/bin && bash ./console.sh start`
3. 初始化启动（首次）：在 `console.sh` 的 `CMD_OPTS` 设置 `-i` 启动一次，成功后去掉。
4. 验证：访问 PC 控制台，使用 `admin` 登录（初始化密码见 §5.1 流程输出）。

### 7.4 数据库维护工具推荐
- CLI：`mysql -uconsole -p consoledb`
- GUI：DBeaver / Sequel Ace / TablePlus，连接信息 `127.0.0.1:3306 / console / console123`。

---

## 8. 测试与 CI

### 8.1 单元测试层
- 删除 `console-admin/src/test/java/com/uh/tools/database/H2DatabaseSmokeTest.java`。
- 新增 `MySqlDatabaseSmokeTest`：
    - 使用 **Testcontainers for MySQL (1.19.x)**，**纯 JUnit 5，不依赖 Spring Boot**。
    - 在容器内启动 MySQL 8.4，执行 `sql/1.commons.sql`，断言 `sys_user / sys_role / sys_role_menu` 等核心表与 admin 数据存在，且已删除的历史菜单表不会重新出现。
- 若 CI 运行环境无 Docker，可用 profile 切换为"连接 CI 提供的外部 MySQL 服务"模式。

### 8.2 前端 API 集成测试
- 保持现有 `frontend-integration-tests` job 逻辑不变。
- CI 中额外启动一个 **MySQL 8.4 服务容器**（GitHub Actions `services:` 原生支持），并将 `consoledb` 预创建好。
- `-Ptest` profile 中的 `console.adminPasswordExpireDays` 替换保持不变。

### 8.3 灰度与下线时间点
1. 迁移 PR 合并后��� 1 个迭代内，保留 `H2DatabaseInitializer`（标记 `@Deprecated`）、H2 相关 lib 的 backup 分支。
2. 线上稳定运行 1 个完整发布周期后，彻底清理所有 H2 残留代码。

---

## 9. 风险与应对

| 风险项 | 应对策略 |
|---|---|
| MyBatis XML 中隐藏的 H2 方言未清理干净 | 迁移完成后，跑一次全量接口冒烟 + SQL 日志审计 |
| 现网已有 H2 数据需要迁移到 MySQL | 单独编写一次性导出工具：H2 的 `SCRIPT TO` 导出 → 脚本转换（字符集、语法）→ MySQL `source` 导入；此阶段仅走运维流程，不纳入应用代码 |
| c3p0 对 MySQL 长连接断开不敏感 | 显式配置 `idleConnectionTestPeriod=60`、`preferredTestQuery=SELECT 1`（§4.2） |
| 严格 `sql_mode` 下 insert 失败 | DDL 层统一加 `DEFAULT CURRENT_TIMESTAMP`；代码层审查所有手写 insert SQL |
| Emoji / 特殊字符乱码 | 库、表、列、连接串、`my.cnf` 四处统一 utf8mb4 |
| 生产环境 `caching_sha2_password` 认证失败 | 确认驱动为 `mysql-connector-j:8.4.0+`；连接串带 `allowPublicKeyRetrieval=true`（无 SSL 时）或启用 SSL |

---

## 10. 交付清单与分阶段计划

### 10.1 里程碑

| 阶段 | 交付物 | 预计工期 |
|---|---|---|
| M1 · SQL 脚本改造 | `sql/1.commons.sql` 按 §3 完成重写 | 0.5 人日 |
| M2 · 初始化器与配置 | `MySqlDatabaseInitializer`、数据源配置、pom 依赖 | 1 人日 |
| M3 · H2 相关代码下线 | 移除 H2 Web 控制台、`DatabaseServiceImpl` 备份恢复重构 | 1 人日 |
| M4 · 测试迁移 | `MySqlDatabaseSmokeTest` + CI 服务容器 | 1 人日 |
| M5 · 文档与 README | README、`.github/instructions/db-development.instructions.md` 更新 | 0.5 人日 |
| M6 · 联调验证 | 本地 + 集成测试通过，准备合并 | 0.5 人日 |
| **合计** | | **4.5 人日** |

### 10.2 合并前 Checklist

- [ ] `mvn clean package -DskipTests=true` 通过。
- [ ] `cd console-admin && mvn test -DskipTests=false` 通过，含 `MySqlDatabaseSmokeTest`。
- [ ] `cd console-ui && npm run test:integration` 通过。
- [ ] 首次启动 `console.sh start -i` 能成功建库建表；第二次启动不再执行初始化。
- [ ] 全仓库检索 `org.h2`、`jdbc:h2`、`.mv.db`、`DATEADD(` 均无剩余。
- [ ] README 的"开发用配置和参数"章节已同步更新。
- [ ] `.github/instructions/db-development.instructions.md` 更新为 MySQL 规范。