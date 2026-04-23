# Build/Test 指南（uh-console）

## 何时使用本文件

- 任务涉及编译、测试、打包或运行模块时。

## 必做步骤

1. 编译输出前端资源，进入 `console-ui`目录，执行：
   ```bash
    mvn clean compile
   ```
   该命令在 Maven 生命周期内会自动执行以下动作：
   - 下载并安装隔离 Node.js（要求：`18+`，实际版本以 `console-ui/pom.xml` 前端插件配置为准）到 `console-ui/node/`。
   - 执行 `npm install --registry=https://registry.npmmirror.com --legacy-peer-deps`。
   - 执行 `npm run build:prod`。
   - 将 `console-ui/dist/*` 复制到 `console-admin/src/main/resources/public/`。
2. 编译输出后端类与运行环境目录骨架，进入目录 `console-admin`，执行：
    ```bash
     mvn clean compile
    ```
   说明：数据库不再由 Maven `compile` 阶段预创建；应用或测试在首次启动、检测到 MySQL 目标库中无 `sys_user` 表时，会自动执行类路径下的 SQL 初始化脚本。
3. 如果要运行测试，进入目录 `console-admin` 执行：
   ```bash
    mvn test -DskipTests=false
   ```
   运行时要指定工作目录为 `console-admin/apphome` 
4. 编译输出项目整体运行部署包（前提是在第一步完成的情况下），进入目录 `console-admin`，执行：
    ```bash
     mvn clean package -DskipTests=true
    ```
5. 优先做最小范围验证（模块或测试类），必要时再扩大范围。

## 前端开发调试（console-ui）

- 本地热更新调试（非 Maven 生命周期）使用系统 Node 环境，建议与 README 保持一致。
- 系统 Node.js 版本要求：`18+`。
- 在 `console-ui` 目录执行：

```bash
npm install --registry=https://registry.npmmirror.com --legacy-peer-deps
npm run dev
```

## 前端测试（console-ui）

```bash
cd console-ui
npm install --registry=https://registry.npmmirror.com --legacy-peer-deps
npm run test
```

- 使用 Vitest，测试文件位于 `src/api/**/__tests__/*.test.js`
- 通过 `vi.mock` 模拟 HTTP 请求，不依赖后端
- Vitest 配置文件：`console-ui/vitest.config.js`

## 前端 API 集成测试（console-ui）

```bash
cd console-ui
npm install --registry=https://registry.npmmirror.com --legacy-peer-deps
npm run test:integration
```

- 集成测试文件位于 `src/api/__integration_tests__/**/*.integration.test.js`
- 使用真实 `axios` 发送 HTTP 请求，不使用 `vi.mock`，需要后端服务运行
- 需要后端服务在 `http://localhost:8083` 运行，且已使用 `-Ptest` 启动（开启 `/interLogin` 接口）
- `console-admin` 在 `-Ptest` 下会通过 Maven 资源过滤将 `console.adminPasswordExpireDays` 替换为 `30`，确保首次初始化时 admin 账号不过期
- 使用 `admin / admin123` 通过 `/interLogin` 获取 token 后调用真实接口
- Vitest 配置文件：`console-ui/vitest.integration.config.js`

启动后端（供本地集成测试使用）：
```bash
cd console-admin
mvn clean package -DskipTests=true -Ptest
cd apphome/bin
bash ./console.sh run
```

- 需要后台运行时，使用 `bash ./console.sh start`
- 停止后台实例时，使用 `bash ./console.sh stop`

## 单实例 HTTP 集成测试（console-client）

- 当前仓库根 `pom.xml` 还包含 `console-client` 模块，用于真实 HTTP E2E / CI 验证。
- 优先使用统一脚本执行完整闭环：

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh
```

- 该脚本会按顺序完成：`console-admin -Ptest` 打包 → `apphome/bin/console.sh start` 启动后端 → 运行 `console-client` 测试 → `console.sh stop` 停止服务。
- 只读/写操作分组与更多参数，参考 `.github/instructions/standalone-integration-tests.instructions.md`。

## interface.enable 配置替换机制

`run-resources/config/application.yml` 中的 `interface.enable` 使用占位符 `@INTERFACE_ENABLE@`：

- **默认**（无 profile）：`false`（`pom.xml` 中 `<INTERFACE_ENABLE>false</INTERFACE_ENABLE>`）
- **test profile**（`-Ptest`）：`true`（允许 `/interLogin` 绕过验证码）
- 占位符在 Ant `copy` + `filterset` 阶段被替换（`process-resources` 阶段，早于 `compile`）

运行时启用接口登录：
```bash
mvn test -DskipTests=false -Ptest
```

> **注意**：Service 层测试（`SysLoginServiceTest`）直接调用 `loginService.interfaceLogin()`，
> 不经过 Controller 层，不受 `interface.enable` 开关限制，无需 `-Ptest`。

## 前端发布（console-ui）

在 `console-ui` 目录执行：

```bash
npm run build:prod
cp -r dist/* ../console-admin/src/main/resources/public/
```

注意：发布前需确认后端 `server.context-path` 与前端环境变量 `VUE_APP_CONTEXT_PATH` 对齐。

## 配置文件放置约定（console-admin）

- `console-admin/src/main/resources/application*.yml`：打包后不可直接修改，适合放置部署后通常不变更、也不需要暴露给运维直接编辑的配置。
- `console-admin/src/main/run-resources/config/application.yml`：会拷贝到发布运行环境，适合放置部署时需要按环境调整的配置项。
- `console-admin/src/main/run-resources/config/application.yml` 中同名配置可覆盖 `console-admin/src/main/resources/application*.yml` 的配置。



## 禁止事项

- 当 Maven commands 已能覆盖任务时，不要引入新的 build 工具。
