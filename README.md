# RDS管理控制台-后端系统工程 #

## 环境准备和前置工作 ##

1. 从 **MySQL 8.4（LTS）** 开始本项目默认连接的数据库类型：

   - 默认 JDBC URL（可在 `console-admin/apphome/config/application.yml` 调整）：
     ```
     jdbc:mysql://127.0.0.1:3306/consoledb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
     ```
   - 默认数据库账号：`console` / `console123`（仅用于本地开发调试，生产环境请替换）。
   - 字符集：`utf8mb4` + `utf8mb4_0900_ai_ci`。
   - 应用首次启动会自动检测 `sys_user` 表是否存在：不存在则执行 `console-admin/sql/1.commons.sql` 完成建表与初始化数据。

   本地快速启动一个 MySQL 实例（示例）：
   ```shell
   docker run -d --name sm-console-mysql \
     -p 3306:3306 \
     -e MYSQL_ROOT_PASSWORD=root \
     -e MYSQL_DATABASE=consoledb \
     -e MYSQL_USER=console \
     -e MYSQL_PASSWORD=console123 \
     mysql:8.4
   ```

2. 如果要编译运行 console-admin 工程，Maven 会自动编译 console-ui 工程，生成前端的静态资源文件。
   无需手动构建前端，只需在根目录执行 `mvn install` 即可。

## 各子工程说明 ##
1. console-ui: 前端Vue工程，生成静态资源
2. console-admin: 管理控制台主程序，运行程序。


## 编译和打包 ## 
### 全部编译并打包 ###
主要在项目根目录下执行mvn clean package 命令

### 手工编译单个子工程 ###
为开发和调试方便，可以单独进入某个子工程目录进行编译打包。
#### 编译父工程(POM工程) ####
独立编译打包的子工程前需要在项目根目录执行：mvn install -N 保证依赖的父POM被安装到本地仓库中。


##  开发调试和运行 ##
### 通过IDEA打开工程 ###
- 用IDEA导入打开整个工程，加载完成后，项目应该就可以正常编译通过。
- 运行console-admin工程中的 com.uh.ConsoleApplication 类即可启动系统，运行时要指定工作路径（Working directory)为 console-admin/apphome 目录。

### 开发用配置和参数 ###
1. 启动时初始化操作
   - 调试状态是可在启动 com.uh.ConsoleApplication 时，加入命令行参数 -i
   - 在正式运行环境中可以在 console.sh(console.bat) 中找到 CMD_OPTS 变量把值设置为 "-i" 
   - 初始化操作主要是对可用菜单进行进行初始化，对定时任务进行初始化，对用户进行初始化(内嵌模式会删除数据库中的用户), 重新设置admin用户的初始化密码
   - 注意在初始化启动完成后，要把CMD_OPTS 变量中的"-i"参数去掉，以防止下次启动时再次进行初始化操作。
2. 开启和关闭开发者模式
   - 开启开发者模式: 在控制台登录后，在页面右上处的搜索框中录入 "switch on development mode" （注意不带引号）
     配置更改后，会开启开发所用的菜单。
   - 关闭开发者模式: 在控制台登录后，在页面右上处的搜索框中录入 "switch off development mode" （注意不带引号）
   - 注意：浏览器重新刷新页面后才可以看到菜单的变化。
3. 数据库管理
   - 开发调试期间可使用任意 MySQL 客户端（如 `mysql`、DBeaver、Navicat、IDEA Database 工具）连接 `127.0.0.1:3306` 查看数据。
   - 应用不再启动内置数据库控制台（H2 Web Console 已移除）。
4. 审计日志标题
   - 控制器写操作上的 `@Log(title=...)`，`title` 统一使用对应功能在 `console-admin/src/main/resources/menu.yml` 中的 `menuCode`。
   - 例如 `RdsVersionController` 中的 `@Log(title = "console:rdsversion", ...)`，其 `title` 就对应 `menuCode: console:rdsversion`。
5. 数据库备份
   注意：备份通过外部 `mysqldump` 命令执行，需预先安装 MySQL 客户端工具。
   在apphome/目录下执行 java -jar lib/console-admin.jar --backup
   进行一次数据库数据导出，导出文件（`.sql.gz`）到 data/dbbak 目录下，备份完成后程序会退出。
6. 恢复备份数据
   恢复通过外部 `mysql` 命令执行，需预先安装 MySQL 客户端工具。
   在apphome/目录下执行 java -jar lib/console-admin.jar --restore export-20250428165907.sql.gz
   进行数据库数据恢复, 把 data/dbbak/export-20250428165907.sql.gz 中的数据恢复到当前数据库中。
   恢复后会，继续启动控制台，控制台程序会进入正常的启动和运行状态。
   注意：恢复前控制台程序不能运行，否则数据文件会被占用，无法进行恢复。

