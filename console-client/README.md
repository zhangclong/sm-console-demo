# console-client

`console-client` 是一个用于 `sm-console` 独立 E2E 集成测试的 Java HTTP 客户端模块。
它仿照参考仓库中的同名模块，但只实现了当前仓库后端**真实存在**的能力：

- `UserActions`：`/web-api/system/user/**`
- `RoleActions`：`/web-api/system/role/**`
- `MenuActions`：`/web-api/system/menu/**`
- `DictActions`：`/web-api/system/dict/data/**`
- `ConsoleWebClient`：通过 `/web-api/interLogin` 登录，管理 token 并发起真实请求

> 当前仓库没有参考仓库中的 License、RDS 节点/服务、中心节点等后端接口，因此这些 Actions 没有迁移进来。

## 目录结构

- `src/main/java/com/uh/console/client/`：客户端实现
- `src/test/java/com/uh/console/tests/`：E2E 集成测试
- `src/test/resources/console-config.yml`：默认测试配置
- `docs/console-client-upgrade-design.md`：本仓库适配设计说明

## 配置

测试配置文件默认从类路径读取 `console-config.yml`：

```yaml
console:
  baseUrl: "http://localhost:8083"
  username: "admin"
  password: "admin123"
```

也可以用 Maven 参数覆盖：

```bash
mvn test -DskipTests=false -Dconsole.config.file=console-config.yml
```

## 本地运行

### 方式一：一键脚本

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh
```

### 方式二：手动启动后端后单独跑客户端测试

```bash
cd console-admin
mvn clean package -DskipTests=true -Ptest

cd apphome
java -jar ../target/console-admin.jar
```

后端就绪后执行：

```bash
cd console-client
mvn test -DskipTests=false
```

只跑写操作或只读操作：

```bash
cd console-client
mvn test -DskipTests=false -Dgroups=writeOps
mvn test -DskipTests=false -Dgroups=readOps
```

## 已实现测试

- `UserActionsTest`：用户列表、详情、创建、修改、停用、删除
- `RoleActionsTest`：角色列表、详情、创建、菜单授权、停用、删除
- `MenuActionsTest`：菜单树、角色菜单树
- `DictActionsTest`：枚举字典查询
- `UserRestPasswordTest`：管理员重置测试用户密码并验证新密码可登录

## 与参考仓库的差异

当前 `sm-console` 的后端约束与参考仓库不同，主要适配点如下：

1. `server.context-path` 默认为 `/`，所以测试基地址使用 `http://localhost:8083`。
2. 菜单权限改为 `menuCode` 驱动，角色更新使用 `menuCodes` 字段。
3. 菜单控制器仅保留 `treeselect` 与 `roleMenuTreeselect` 两个只读接口，没有旧版的菜单 CRUD。
4. 字典接口是枚举驱动的只读接口 `GET /system/dict/data/type/{dictType}`。
5. 所有 E2E 测试依赖 `-Ptest` 启动后端，以开启 `/interLogin`。

