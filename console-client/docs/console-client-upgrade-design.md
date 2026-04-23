# console-client 适配设计

## 1. 目标

在 `sm-console` 中新增一个可独立运行的 `console-client` Maven 模块，用真实 HTTP 请求驱动 `console-admin`，用于单实例 E2E 集成测试与 CI 手动回归。

## 2. 适配原则

- 结构上仿照参考仓库的 `console-client`。
- 仅迁移**当前仓库后端真实存在**的接口能力，不为缺失接口引入伪实现。
- 保持单实例、单租户的本地闭环：`console-admin -Ptest` + `console-client`。
- 测试通过 `/interLogin` 获取 token，避免验证码依赖。

## 3. 当前仓库可实现的 Actions

### 3.1 UserActions
对应 `console-admin/src/main/java/com/uh/system/web/SysUserController.java`：

- `listUsers()` → `GET /web-api/system/user/list`
- `getUserInfo()` → `GET /web-api/system/user/{userId}`
- `createUser()` → `POST /web-api/system/user`
- `updateUser()` → `POST /web-api/system/user/edit`
- `deleteUser()` → `GET /web-api/system/user/delete/{userIds}`
- `resetUserPwd()` → `POST /web-api/system/user/resetPwd`
- `changeUserStatus()` → `POST /web-api/system/user/changeStatus`

### 3.2 RoleActions
对应 `console-admin/src/main/java/com/uh/system/web/SysRoleController.java`：

- `listRoles()` → `GET /web-api/system/role/list`
- `getRoleInfo()` → `GET /web-api/system/role/{roleId}`
- `createRole()` → `POST /web-api/system/role`
- `updateRole()` → `POST /web-api/system/role/edit`
- `deleteRole()` → `GET /web-api/system/role/delete/{roleIds}`
- `changeRoleStatus()` → `POST /web-api/system/role/changeStatus`
- `optionselect()` → `GET /web-api/system/role/optionselect`

### 3.3 MenuActions
当前仓库菜单改为 `menu.yml + menuCode` 驱动，只保留只读接口：

- `treeselect()` → `GET /web-api/system/menu/treeselect`
- `roleMenuTreeselect()` → `GET /web-api/system/menu/roleMenuTreeselect/{roleId}`

### 3.4 DictActions
对应 `console-admin/src/main/java/com/uh/system/web/SysDictDataController.java`：

- `dictType()` → `GET /web-api/system/dict/data/type/{dictType}`

## 4. 不迁移的参考能力

以下参考仓库中的能力在当前仓库没有对应后端接口，因此不纳入本次实现：

- License 管理
- RDS 服务/节点/节点管理器/中心节点 Actions
- 依赖外部集群环境的高可用测试

## 5. 测试设计

### 5.1 标签约定

- `@Tag("defaultEnv")`：默认本地闭环环境可运行
- `@Tag("writeOps")`：写操作测试
- `@Tag("readOps")`：只读测试
- `@Tag("externalEnv")`：预留给外部环境测试，当前默认不使用

### 5.2 测试类

- `UserActionsTest`
- `RoleActionsTest`
- `MenuActionsTest`
- `DictActionsTest`
- `UserRestPasswordTest`

### 5.3 环境前提

- 后端需执行 `mvn clean package -DskipTests=true -Ptest`
- 后端需运行在 `http://localhost:8083`
- `console-client` 默认读取 `src/test/resources/console-config.yml`

## 6. CI 与脚本

### 6.1 工作流
新增 `.github/workflows/standalone-integration-tests.yml`，支持手动触发：

- `all`
- `writeOps`
- `readOps`

### 6.2 脚本
新增 `console-admin/src/test/shell/standalone-integration-test.sh`，支持：

- `--build`
- `--start`
- `--test`
- `--stop`
- `--scope all|writeOps|readOps`

默认行为为：构建 → 启动 → 测试 → 停止。

## 7. 验收口径

- `console-client` 已加入根 `pom.xml` 模块列表。
- 单独执行 `cd console-client && mvn test -DskipTests=false` 可运行 E2E 测试。
- 脚本 `console-admin/src/test/shell/standalone-integration-test.sh` 可本地闭环执行。
- `standalone-integration-tests.yml` 的命令序列与本地脚本一致，可用于手动 CI 回归。

