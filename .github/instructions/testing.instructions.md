---
applyTo: "**/*Test.java,**/*Tester.java"
---
# 测试规范指南

## 重要前提：本工程的技术栈约束
- **本工程使用 Spring Framework 5.3.x，不使用 Spring Boot**。
- pom.xml 中没有 `spring-boot-*` 任何依赖，**禁止引入** `spring-boot-starter-test` 或 `spring-boot-test`。
- **本工程不包含 `spring-boot-test` 依赖，`@SpringBootTest` 不可用**，不得使用。
- 需要 Spring 容器的集成测试，在 `console-admin/pom.xml` 的 `test` scope 添加 `org.springframework:spring-test`（版本跟随 `${spring-framework.version}`），然后使用 `@ExtendWith(SpringExtension.class)` + `@ContextConfiguration`。
- 新增**不写版本号**的依赖前，必须先确认该依赖已被父 `pom.xml` 的 `dependencyManagement`（或其 `import` 的 BOM）管理；若未管理，需先补齐版本管理再添加依赖，避免 `dependencies.dependency.version is missing`。

## 何时使用本文件
- 新增或修改测试用例时。
- 修复测试中注入相关问题时。
- 涉及 console-admin/sql/*.sql 变更时，需补充数据库冒烟验证。

## 测试分层约定

| 层级 | 位置 | 使用工具 | 是否在 CI 中自动运行 |
|---|---|---|---|
| 单元测试（纯函数/工具类） | `src/test/java/**/utils/` 或 `**/domain/` | JUnit 5，无需 Spring | ✅ 是 |
| Spring 集成测试 | `src/test/java/**/service/` | `@ExtendWith(SpringExtension.class)` + `@ContextConfiguration` | ✅ 是（需添加 spring-test 依赖）|
| MySQL 数据库冒烟测试 | `src/test/java/**/tools/database/` | JUnit 5（纯 JDBC，连接 CI 提供的 MySQL 服务）| ✅ 是（SQL 变更时必须触发）|
| 外部依赖集成测试 | `src/test/java/**/integration/` | 使用 `@Tag("integration")`，依赖外部环境 | ❌ CI 中可按需跳过 |
| 前端 API 契约测试 | `console-ui/src/api/**/__tests__/` | Vitest + vi.mock | ✅ 是 |
| 前端 API 集成测试 | `console-ui/src/api/__integration_tests__/` | Vitest + 真实 axios（无 mock）+ `/interLogin` | ✅ CI `frontend-integration-tests` job |

## Spring 集成测试注入方式（非 Spring Boot）

当测试需要 Spring Bean 注入时，**不要用 `@SpringBootTest`**，而是：

**第一步**：在 `console-admin/pom.xml` 中添加（若尚未存在）：
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <scope>test</scope>
</dependency>
```
（版本由父 pom 的 `${spring-framework.version}` 管理，无需写版本号；若父 pom 未管理 `spring-test`，必须先在父 pom 的 `dependencyManagement` 中补齐再使用该写法）

**第二步**：测试类写法：
```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MyTestConfig.class })
public class MyServiceTest {

    @Autowired
    private MyService myService;

    // 在内部静态类中定义测试专用的 Spring Bean
    @Configuration
    static class MyTestConfig {
        @Bean
        public MyService myService() { return new MyService(); }
    }
}
```

**涉及完整 Service 层集成测试时**，复用公共 `TestAppConfig`：
```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestAppConfig.class)
class MyServiceTest {
    // TestAppConfig 已提供 DataSource、SqlSessionFactory、Service 组件扫描等
}
```
- `TestAppConfig` 位于 `src/test/java/com/uh/system/service/config/TestAppConfig.java`
- 支持带数据库的 Service 层测试；若目标 MySQL 库中无 `sys_user` 表，会在测试上下文启动前自动执行运行时数据库初始化
- 包含 `EarlySpringUtilsInitializer`，可确保 `SpringUtils.getBean()` 在 `@PostConstruct` 中可用

## 数据库冒烟测试规范

### 触发条件
**每次 `console-admin/sql/*.sql` 文件变更时**，必须通过以下两步验证 SQL 脚本有效性：

1. **运行时初始化验证**：应用启动或测试启动时，若连接 MySQL 后检测到 `sys_user` 表不存在，会自动执行类路径下的 `1.commons.sql` 创建数据库。若 SQL 有语法错误，初始化会直接失败。

2. **JUnit 冒烟测试**：`MySqlDatabaseSmokeTest` 连接由 CI 提供的 MySQL 8.x service 容器，验证：
   - 核心用户/角色/RBAC 表（如 `sys_user`、`sys_role`、`sys_user_role`、`sys_role_menu`）存在，且已删除的历史菜单表不会重新出现
   - 初始数据完整（admin 用户、admin 角色、admin-role 关联，以及 `sys_role_menu.menu_code` 绑定数据等）

### 新增 SQL 变更时的要求
- 若新增表，需在 `MySqlDatabaseSmokeTest` 中添加对应的 `assertTableExists()` 断言
- 若新增关键初始数据，需添加对应的数据验证断言
- 当前首次初始化脚本为 `1.commons.sql`；若后续新增初始化脚本，需在文档、测试与初始化器实现中同步更新。
- 若改动了菜单/权限初始化数据，除 SQL 冒烟外还应确认运行时 `menu.yml` / `MenuRegistry` 与兼容 SQL 保持一致。

### MySqlDatabaseSmokeTest 位置
```
console-admin/src/test/java/com/uh/tools/database/MySqlDatabaseSmokeTest.java
```

## 关键功能测试用例要求

### 用户登录/退出 (`SysLoginServiceTest`)
涉及登录相关变更时，必须覆盖：
- `interfaceLogin(username, password)` 成功路径（有效凭证返回 token）
- `interfaceLogin` 失败路径（无效密码、不存在的用户名抛异常）
- `getPasswordSuggestion(userId)` 返回非空结果
- `checkUserPassword` 正确/错误密码行为

**注意**：`interfaceLogin()` 直接调用 Service，不经过 Controller 层，不受 `interface.enable` 开关限制。

### 用户管理 (`SysUserServiceTest`)
涉及用户相关变更时，必须覆盖：
- `selectUserList()` 返回非空列表
- `selectUserByUserName("admin")` 找到 admin 用户
- `selectUserById(1L)` 找到用户
- `checkUserNameUnique` 已存在 vs 新用户名的结果
- `insertUser(user)` 插入成功并回填 userId

### 角色管理 (`SysRoleSvcTest`)
涉及角色相关变更时，必须覆盖：
- `selectRoleAll()` 返回非空列表
- `selectRoleById(1L)` 找到 admin 角色
- `checkRoleNameUnique` 已存在 vs 新角色名的结果
- `checkRoleAllowed` 对 admin 角色抛异常、对普通角色不抛异常
- `insertRole(role)` 插入成功并可查到
- 若涉及角色授权树/菜单授权变更，需额外验证 `menuCodes` 提交与 `roleMenuTreeselect` 的字符串 `checkedKeys` 回显

## 前端 API 层测试规范（Vitest）

### 框架与工具
- 使用 Vitest（`npm run test`）
- 测试文件位于 `src/api/**/__tests__/*.test.js`
- 通过 `vi.mock('@/utils/request', ...)` 模拟 HTTP 请求，不发真实 HTTP 调用

### 涉及功能变更时的测试要求

| 变更的 API 文件 | 对应测试文件 | 必须验证的关键点 |
|---|---|---|
| `src/api/login.js` | `src/api/__tests__/login.test.js` | URL、method、密码加密、isToken 头 |
| `src/api/system/user.js` | `src/api/system/__tests__/user.test.js` | URL、method、参数传递、密码加密 |
| `src/api/system/role.js` | `src/api/system/__tests__/role.test.js` | URL、method、参数传递 |

### 测试模式
```js
// 必须 mock request 和加密工具
vi.mock('@/utils/request', () => ({ default: vi.fn() }));
vi.mock('@/utils/aesutils.js', () => ({ default: { encrypt: vi.fn((s) => `encrypted_${s}`) } }));

// 验证 API 函数调用
import request from '@/utils/request';
import { login } from '@/api/login';

describe('login()', () => {
    it('should call /login with POST and encrypted password', () => {
        login('admin', 'pass123', '1234', 'uuid');
        expect(request).toHaveBeenCalledWith(
            expect.objectContaining({ url: '/login', method: 'post' })
        );
    });
});
```

## 前端 API 集成测试规范

### 框架与工具
- 使用 `vitest.integration.config.js` 配置（`npm run test:integration`）
- 测试文件位于 `console-ui/src/api/__integration_tests__/**/*.integration.test.js`
- `environment: 'node'`，直接使用原生 `axios` 发真实 HTTP 请求，**不使用 `vi.mock`**

### 前提条件
- 后端服务已使用 `-Ptest` profile 启动（开启 `/interLogin` 接口）
- `console-admin` 在 `-Ptest` 下会通过 Maven 资源过滤将 `console.adminPasswordExpireDays` 替换为 `30`，确保首次初始化时 admin 密码不过期
- 后端默认监听 `http://localhost:8083`
- baseURL 由 `BACKEND_BASE_URL` 环境变量控制，默认 `http://localhost:8083/web-api`

### 登录方式
- 通过 `/interLogin` 以 `admin / admin123` 登录（**明文密码，不加密**，后端 `/interLogin` 不走 AES 解密流程）
- 辅助函数封装在 `src/api/__integration_tests__/helpers/apiClient.js`

### 测试范围约定
- 集成测试仅做**只读操作**（GET），不对数据库做破坏性变更
- 测试文件命名：`*.integration.test.js`，位于 `__integration_tests__` 目录
- 若变更字典或菜单接口，优先补充对 `/system/dict/data/type/{dictType}`、`/system/menu/treeselect`、`/system/menu/roleMenuTreeselect/{roleId}` 的真实接口校验

### 本地运行集成测试
```bash
# 1. 先启动后端（-Ptest 激活 interLogin，并将 adminPasswordExpireDays 过滤为 30）
cd console-admin
mvn clean package -DskipTests=true -Ptest
cd apphome/bin
bash ./console.sh start

# 2. 等待后端就绪后运行集成测试
cd console-ui
npm run test:integration

# 3. 测试结束后停止后台服务
cd ../console-admin/apphome/bin
bash ./console.sh stop
```

## 命名约定
- **自动运行测试类**：`{被测类名}Test.java` —— Maven Surefire 自动发现。
- **手动运行工具类**：`{名称}Tester.java` —— 不加 `@Test` 或手动禁用，供开发本地调试使用。

## 租户测试辅助规范
- 凡是测试涉及数据库查询或 Service 层调用的，必须在 `@BeforeEach` 中设置 `TenantContext.setTenantId(TEST_TENANT_ID)`，`@AfterEach` 中调用 `TenantContext.clear()`。
- 测试常量：`TEST_TENANT_ID = 1L`（平台租户），`TEST_TENANT_ID_2 = 2L`（普通租户，用于隔离验证）。

## 外部依赖测试隔离
- 所有依赖外部环境（集群、中间件、远端服务）的测试类/方法必须加 `@Tag("integration")`。
- CI 运行命令按流水线策略过滤该标签，例如：`mvn test -DskipTests=false -Dgroups="!integration"`。

## CI 运行验证要求与通过标准

| 测试类型 | CI Job | 通过标准 |
|---|---|---|
| 后端 Service 层 + DB 冒烟 | `backend-tests` | 所有 JUnit 测试通过，无 FAILURE/ERROR |
| SQL 脚本变更冒烟 | `sql-smoke` | 运行时初始化成功，`MySqlDatabaseSmokeTest` 通过 |
| 前端 API 契约测试 | `frontend-tests` | 所有 Vitest 测试通过 |
| 前端 API 集成测试 | `frontend-integration-tests` | 所有集成测试通过（需真实后端） |

CI 工作流文件：`.github/workflows/ci.yml`。

## 禁止事项
- **禁止使用 `@SpringBootTest`**（本工程不包含 `spring-boot-test` 依赖，不可用）。
- 禁止将测试依赖的绝对路径（如 `E:\\...`）提交到代码仓库（参见 `PoiExcelTester`，此为反例）。
- 禁止注释掉测试代码而不给出替代方案，应重构为可运行的轻量测试或标记为 `@Disabled("原因")` 并附 TODO。
