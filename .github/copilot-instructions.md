# uh-console 的 Copilot 指南

本文件定义任务的全局规则。高频工作流以 `.github/instructions/**/*.instructions.md` 为准。

## 指南文件映射

- `.github/instructions/build-test.instructions.md`：Maven build/test 工作流规则（含前端集成测试运行方式）。
- `.github/instructions/backend-java.instructions.md`：Java/MyBatis/backend 变更规则。
- `.github/instructions/dict-enum.instructions.md`：枚举字典（`DictRegistry` / `@DictType`）开发规则。
- `.github/instructions/frontend-ui.instructions.md`：UI/client-side 变更规则。
- `.github/instructions/db-development.instructions.md`：初始化数据库脚本变更规则。
- `.github/instructions/standalone-integration-tests.instructions.md`：`console-client` 单实例 HTTP 集成测试脚本规范。
- `.github/instructions/testing.instructions.md`：测试分层与注入约定（非 Spring Boot），含前端 API 集成测试规范。
- `.github/instructions/date-range-query.instructions.md`：日期范围查询（beginTime/endTime）从 Vue 到 Service 到 Mapper 的链路规范。

## 全局工程规则

- 仅做最小且安全的改动，不重构无关代码。
- 优先复用现有 utility/mapper/service，避免重复逻辑。
- 代码变更后执行最小相关验证命令。
- 保持行为显式，不添加掩盖错误的静默 fallback。
- 控制器写操作上的 `@Log(title=...)`，`title` 统一使用对应功能在 `console-admin/src/main/resources/menu.yml` 中的 `menuCode`。
- 本工程使用 Spring Framework（非 Spring Boot），禁止引入 `spring-boot-*` 依赖。

## Build、Test 与 Run（快速参考）

本项目使用 Maven，多模块根 `pom.xml` 当前仅保留：

- `console-ui`
- `console-client`
- `console-admin`

### Build

1. 前端编译（输出静态资源）：

```bash
cd console-ui
mvn clean compile
```

说明：`console-ui` 在 Maven 流程中会自动执行前端依赖安装、`npm run build:prod`，并将 `dist/*` 复制到 `console-admin/src/main/resources/public/`。

2. 后端编译（生成 apphome 目录与运行环境骨架）：

```bash
cd console-admin
mvn clean compile
```

3. 打包：

```bash
cd console-admin
mvn clean package -DskipTests=true
```

### Test

```bash
cd console-admin
mvn test -DskipTests=false
```

测试运行目录需为 `console-admin/apphome`（由项目配置处理）。

### 前端 API 集成测试

```bash
# 先以 -Ptest profile 打包后端（开启 interLogin 接口，admin 密码非过期）
cd console-admin && mvn clean package -DskipTests=true -Ptest
# 进入 apphome/bin，用 console.sh 在后台启动后端
cd console-admin/apphome/bin && bash ./console.sh start
# 等待后端就绪后运行集成测试
cd console-ui && npm run test:integration
```

集成测试文件位于 `console-ui/src/api/__integration_tests__/`，使用真实 axios，通过 `/interLogin` 获取 token 后调用真实接口。`console-admin` 在 `-Ptest` 下会通过 Maven 资源过滤将 `console.adminPasswordExpireDays` 替换为 `30`，CI 中由 `frontend-integration-tests` job 自动运行。

停止后台服务时，进入 `console-admin/apphome/bin` 执行 `bash ./console.sh stop`；如需在当前 shell 前台运行，则使用 `bash ./console.sh run`。

### 本地 Run

主入口：`console-admin` 的 `com.uh.ConsoleApplication`。

运行时工作目录：`console-admin/apphome`。

说明：数据库不再由 Maven `compile` 阶段预创建；应用在首次启动、连接配置的 MySQL 后，若检测到 `sys_user` 表不存在，会自动执行类路径下的 SQL 初始化脚本创建表并导入初始化数据。

日常启动方式统一使用 `console-admin/apphome/bin/console.sh`：`start` 后台启动、`stop` 停止、`run` 前台运行。

## 前端环境变量约定（console-ui）

- `VUE_APP_CONTEXT_PATH` 必须以 `/` 开头和结尾（如 `/`、`/uhrds/`）。
- `VUE_APP_BASE_API` 约定为 `${VUE_APP_CONTEXT_PATH}web-api`。
- 本地调试建议 `VUE_APP_CONTEXT_PATH='/'`，发布时按后端 `server.context-path` 对齐。 比如：后端 `server.context-path=/uhrds`，则前端 `VUE_APP_CONTEXT_PATH='/uhrds/'`。

## 架构概览

- `console-admin`：主 Web 应用与编排层。
- `console-admin` 运行时菜单树与权限事实源为 `src/main/resources/menu.yml`，由 `com.uh.system.manage.MenuRegistry` 在启动时加载；角色授权提交/存储使用 `menuCodes` / `sys_role_menu.menu_code`。
- `console-admin` 运行时字典由 `com.uh.system.manage.DictRegistry` 扫描 `com.uh.system.dict`、`com.uh.console.dict` 下枚举并直接输出给 `/system/dict/data/type/{dictType}`。
- `console-ui`：前端 UI 组件与页面（由 `console-admin` 使用其构建产物）。
- `console-client`：独立的真实 HTTP 客户端测试模块，用于单实例 E2E / CI 验证。

## 项目约定

- `console-admin/lib` 用于本地调试附加 jar，编译时不包含。目前该目录为空（H2 已下线，改用外部 MySQL 8.4）。
- `console-admin/lib/h2-*.jar` 已废弃（H2 已从项目中移除，数据库类型为 MySQL 8.4）。
- 运行依赖构建插件生成的 `apphome` 目录结构（config/log/db）。
- initialization mode 支持 `-i` 参数用于 bootstrap/reset 流程。

## 配置文件放置约定（console-admin）

- `console-admin/src/main/resources/application*.yml`：打包后不可直接修改。适合放置部署后不需要被看到、且通常不变更的配置。
- `console-admin/src/main/run-resources/config/application.yml`：会拷贝到发布后的运行环境。适合放置部署时需要调整的配置项。
- 运行环境中的 `run-resources/config/application.yml` 可以覆盖 `resources/application*.yml` 中同名配置项。
