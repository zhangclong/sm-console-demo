---
applyTo: "**/*"
---
# PR 代码审查清单（AI 辅助检查指南）

## 何时使用本文件

- 提交 Pull Request 前进行自查时。
- AI 代码审查工具对 PR 进行自动检查时。
- Code Review 时作为人工审查的参考清单。

## 审查目标

确保代码变更：
1. **符合项目规范** - 遵守已定义的工程规范和约定
2. **不引入安全漏洞** - 避免常见安全问题
3. **保持架构一致** - 不破坏现有架构和模块边界
4. **可测试可维护** - 代码清晰，易于理解和维护

## 当前租户状态说明

- 当前系统为单租户运行模式。
- 本文中多租户相关检查项用于未来多租户改造的对齐与预留。
- 当前改动应按单租户方式落地和验证，但需避免引入阻碍后续多租户升级的实现方式。

## 自动检查清单（AI 审查重点）

### 1. 基础规范检查

#### 1.1 代码分层与职责
- [ ] Controller 只做参数接收、权限校验、调用 Service、返回结果
- [ ] Service 包含业务逻辑、事务边界，不直接操作 Mapper
- [ ] Mapper 接口定义方法，SQL 在对应的 XML 文件中
- [ ] 没有跨层直连（如 Controller 直接调 Mapper）
- [ ] Domain/VO 类只做数据承载，不包含业务逻辑

#### 1.2 命名与风格
- [ ] 类名、方法名、变量名符合 Java 驼峰命名规范
- [ ] 包名全小写，类名首字母大写
- [ ] 常量使用 `UPPER_SNAKE_CASE`
- [ ] 布尔类型方法以 `is/has/can` 开头
- [ ] 方法名清晰表达意图（动词+名词）

#### 1.3 注释与文档
- [ ] 公开 API 有清晰的 JavaDoc 注释
- [ ] 复杂逻辑有必要的行内注释说明
- [ ] 不包含注释掉的大段废弃代码
- [ ] TODO/FIXME 注释有明确的负责人和计划

### 2. 权限与安全检查

#### 2.1 权限控制
- [ ] 所有非公开 Controller 接口加 `@PrePermission` 注解
- [ ] 前端按钮权限 `v-hasPermi` 与后端 `@PrePermission` 对齐
- [ ] `menu.yml` 中 `menuCode` 与 `@PrePermission`、`v-hasPermi` 三处一致
- [ ] 写操作上的 `@Log(title=...)` 与对应功能的 `menu.yml` `menuCode` 一致
- [ ] 权限标识格式正确：`模块:功能:操作`（如 `console:rdsservice:list`）
- [ ] 写操作（增删改）添加 `@Log` 注解用于审计
- [ ] 超管判断使用 `SecurityUtils.isAdmin(userId)`，没有硬编码 `userId == 1L`
- [ ] 跨租户操作在 Service 层有 `TenantContext.isPlatformAdmin()` 二次校验
- [ ] 角色菜单授权使用 `menuCodes` / `sys_role_menu.menu_code`，没有残留旧的 `menuIds` / `menu_id` 运行时依赖

#### 2.2 SQL 注入防护
- [ ] MyBatis XML 使用参数化查询（`#{param}` 而非 `${param}`）
- [ ] 动态表名/列名场景有白名单校验
- [ ] 没有在 Java 代码中拼接 SQL 字符串

#### 2.3 敏感信息保护
- [ ] 密码使用 `SecurityUtils.encryptPassword()` 加密
- [ ] API 返回不包含密码、token 等敏感字段
- [ ] 敏感配置（数据库密码、秘钥）不硬编码在代码中
- [ ] 日志输出不包含明文密码、身份证号等敏感信息

#### 2.4 输入校验
- [ ] Controller 接收参数使用 `@Validated` 进行校验
- [ ] 文件上传限制类型、大小
- [ ] 批量操作限制数量上限
- [ ] ID/枚举值等关键参数有合法性校验
- [ ] 使用 `params.begin*/end*` 的查询在 Service 层调用 `checkDateFormat(...)` 校验，且字段名与 UI/Mapper 一致

### 3. 多租户隔离检查

- [ ] 新增业务表包含 `tenant_id` 列并建索引
- [ ] 受隔离表在 `TenantSqlInterceptor.TENANT_TABLES` 中注册
- [ ] Domain 类包含 `tenantId` 字段（含 getter/setter）
- [ ] Mapper XML 的 `<resultMap>` 包含 `tenant_id` 映射
- [ ] 没有在 Service/Controller 中手动拼接 `tenant_id` 过滤条件
- [ ] 拦截器 `TenantInterceptor` 正确调用 `TenantContext.clear()`

### 4. 数据库变更检查

#### 4.1 表结构变更
- [ ] 修改在当前仓库初始化脚本（`console-admin/sql/*.sql`）中
- [ ] 若存在版本化变更脚本，对应脚本已创建
- [ ] 变更脚本保持幂等（可重复执行不报错）
- [ ] 字段类型、长度、默认值合理
- [ ] 索引设计合理（高频查询字段、外键、唯一约束）

#### 4.2 MyBatis 映射同步
- [ ] Domain/VO 字段与数据库表字段对齐
- [ ] Mapper XML 的 `resultMap` 包含新增字段
- [ ] 所有 SELECT 语句显式列出字段（避免 `SELECT *`）
- [ ] INSERT/UPDATE 语句覆盖新增字段

#### 4.3 菜单初始化数据同步
- [ ] 运行时菜单结构变更已同步更新 `console-admin/src/main/resources/menu.yml`
- [ ] 删除目录/菜单/按钮权限时，角色授权树回显、前端按钮权限和兼容初始化数据已一起清理，不存在脏权限

#### 4.4 枚举字典同步
- [ ] 新增/修改字典优先落在枚举 + `@DictType` + `DictRegistry`，没有回退到 `sys_dict_type` / `sys_dict_data` 运行时依赖
- [ ] 前端现有 `getDicts(dictType)` 调用与 `/system/dict/data/type/{dictType}` 接口契约保持兼容

### 5. 事务与并发检查

- [ ] 多表写入、级联删除、状态联动加 `@Transactional`
- [ ] 事务边界在 Service 层，不在 Controller 层
- [ ] 事务方法中没有长时间 IO 操作（如 HTTP 调用）
- [ ] 乐观锁（版本号）或悲观锁正确使用
- [ ] 没有明显的死锁风险（锁顺序一致）

### 6. 异常处理检查

- [ ] 不吞异常（catch 后无处理/无抛出）
- [ ] 异常信息清晰，便于定位问题
- [ ] 业务异常使用 `ServiceException`，系统异常使用运行时异常
- [ ] 资源（文件、连接）使用 try-with-resources 或 finally 释放
- [ ] 全局异常处理器 `GlobalExceptionHandler` 统一处理

### 7. 依赖与构建检查

- [ ] 新增依赖在父 `pom.xml` 的 `dependencyManagement` 中有版本管理
- [ ] 子模块 `pom.xml` 的依赖不写版本号（由父 pom 管理）
- [ ] 没有引入 `spring-boot-*` 依赖（本工程使用 Spring Framework）
- [ ] 测试依赖 scope 为 `test`
- [ ] Maven 构建命令与当前仓库构建规范一致

### 8. 测试覆盖检查

- [ ] 新增业务逻辑有对应单元测试
- [ ] 测试类命名 `{被测类名}Test.java`
- [ ] 测试方法命名清晰（如 `testSelectUserById_success`）
- [ ] 使用 `@ExtendWith(SpringExtension.class)` + `@ContextConfiguration`（非 `@SpringBootTest`）
- [ ] 租户相关测试在 `@BeforeEach` 设置 `TenantContext.setTenantId()`
- [ ] 外部依赖测试标记（如 `@Tag("integration")`）使用正确

### 9. 前后端联调检查

- [ ] 前端 API 层（`console-ui/src/api/*.js`）新增对应函数
- [ ] 后端 Controller 路径与前端 API `url` 一致
- [ ] 返回结构符合约定（列表页 `rows/total`，普通接口 `data`）
- [ ] 错误处理依赖 `request.js` 拦截器统一处理
- [ ] 接口变更同步修改前端调用代码
- [ ] 菜单授权接口 `roleMenuTreeselect` 的 `checkedKeys` 与树节点 `id` 为字符串 `menuCode`，前后端保持一致
- [ ] 字典接口继续通过 `/system/dict/data/type/{dictType}` 返回兼容 `SysDictData` 结构
- [ ] 日期范围查询使用 `addDateRange(queryParams, dateRange)` 透传到 `params.begin*/end*`，并保证 UI/Service/Mapper 字段名一致
- [ ] 前端构建流程与仓库约定一致（`console-ui` 通过 Maven 或 `npm run build:prod` 输出 `dist/`）
- [ ] 前端产物已同步到 `console-admin/src/main/resources/public/`（无旧文件残留导致的页面错配）

### 10. 代码质量检查

- [ ] 没有重复代码（相似逻辑抽取为公共方法）
- [ ] 方法长度合理（建议不超过 50 行）
- [ ] 圈复杂度合理（避免嵌套过深）
- [ ] 魔法数字/字符串提取为常量
- [ ] 空指针检查（使用 `StringUtils.isNull/isNotNull`）
- [ ] 集合操作前检查是否为空
- [ ] 避免性能问题（循环中查数据库、N+1 查询等）

### 11. 部署与配置检查

- [ ] 配置文件（`application*.yml` 及运行环境 `config/application.yml`）不包含生产环境敏感信息
- [ ] 环境差异配置有明确且可维护的区分方式
- [ ] 配置放置遵循约定：`src/main/resources/application*.yml` 放相对稳定配置，`src/main/run-resources/config/application.yml` 放部署时可调整配置
- [ ] 运行环境 `config/application.yml` 对 `resources/application*.yml` 的覆盖关系清晰且符合预期
- [ ] 前端 `VUE_APP_CONTEXT_PATH` 以 `/` 开头和结尾，且与后端 `server.context-path` 对齐
- [ ] 前端 `VUE_APP_BASE_API` 与 `VUE_APP_CONTEXT_PATH` 保持一致（`${VUE_APP_CONTEXT_PATH}web-api`）
- [ ] MySQL 连接串（`jdbc:mysql://host:port/db`）及账号密码已在 `run-resources/config/application.yml` 中按环境配置
- [ ] 日志级别合理（开发 DEBUG，生产 INFO/WARN）
- [ ] 部署相关配置与当前单一运行模式保持一致

### 12. 向后兼容性检查

- [ ] 接口签名变更保持向后兼容
- [ ] 数据库变更脚本不破坏现有数据
- [ ] 配置项变更有默认值或迁移逻辑
- [ ] 不删除公开 API 方法（或标记为 `@Deprecated`）

## 高风险变更重点审查

以下类型的变更需要额外仔细审查：

### 🔴 高风险变更
- [ ] 权限校验逻辑修改
- [ ] 数据库表结构变更
- [ ] 租户隔离相关代码
- [ ] 密码加密/认证逻辑
- [ ] 敏感操作（批量删除、数据导出）
- [ ] 核心业务流程（服务创建、配置变更）
- [ ] 版本化变更脚本（必须幂等且测试充分）

### 🟡 中风险变更
- [ ] Service 层事务边界调整
- [ ] 公共工具类/方法修改
- [ ] 定时任务、异步任务
- [ ] 缓存策略变更
- [ ] 日志输出变更

## AI 审查建议输出格式

AI 进行代码审查时，建议按以下格式输出：

```markdown
## PR 审查结果

### ✅ 通过检查项
- 代码分层职责清晰，Controller/Service/Mapper 分离正确
- 权限注解完整，前后端权限标识对齐
- SQL 使用参数化查询，无注入风险

### ⚠️ 需关注问题
1. **[中风险] Service 层缺少事务注解**
   - 文件：`RdsServiceServiceImpl.java:123`
   - 问题：`updateServiceConfig` 方法涉及多表写入，但未加 `@Transactional`
   - 建议：添加 `@Transactional` 确保事务一致性

2. **[代码质量] 重复代码**
   - 文件：`RdsServiceController.java:88` 和 `RdsServiceController.java:156`
   - 问题：节点状态判断逻辑重复
   - 建议：抽取为 `checkNodeStatus(node)` 公共方法

### ❌ 必须修复问题
1. **[高风险] 权限注解缺失**
   - 文件：`ExampleController.java:45`
   - 问题：`deleteByIds` 接口未加 `@PrePermission` 注解
   - 修复：添加 `@PrePermission("console:example:delete")`

### 📋 测试建议
- 补充 `RdsServiceServiceImpl.updateServiceConfig` 的单元测试
- 测试删除接口的权限拦截是否生效

### 📊 审查摘要
- 检查文件数：8 个
- 通过检查项：35 个
- 需关注问题：2 个
- 必须修复问题：1 个
```

## 自查使用指南

提交 PR 前，开发者可按以下步骤自查：

1. **快速扫描**：过一遍"自动检查清单"，勾选已满足的项
2. **重点审查**：关注"高风险变更重点审查"中涉及的部分
3. **运行检查**：执行 linter、单元测试、build 确保通过
4. **功能验证**：本地测试变更功能，确认符合预期
5. **提交前确认**：所有必须修复问题已解决

## 相关规范文件

- `.github/instructions/security.instructions.md` - 权限改造规范
- `.github/instructions/backend-java.instructions.md` - 后端 Java 开发规范
- `.github/instructions/dict-enum.instructions.md` - 枚举字典开发规范
- `.github/instructions/frontend-ui.instructions.md` - 前端 UI 开发规范
- `docs/multitenancy/tenant.instructions.md` - 多租户开发规范
- `.github/instructions/db-development.instructions.md` - 数据库开发规范
- `.github/instructions/testing.instructions.md` - 测试规范
- `.github/instructions/build-test.instructions.md` - 构建测试规范
- `.github/instructions/date-range-query.instructions.md` - 日期范围查询链路规范

## 附录：常见反模式

### ❌ 反模式 1：Controller 直接操作 Mapper
```java
// 错误
@RestController
public class BadController {
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public List<User> list() {
        return userMapper.selectAll(); // ❌ 跨层调用
    }
}

// 正确
@RestController
public class GoodController {
    @Autowired
    private UserService userService;

    @PrePermission("system:user:list")
    @GetMapping("/users")
    public TableDataInfo list() {
        startPage();
        return getDataTable(userService.selectAll()); // ✅ 通过 Service 调用
    }
}
```

### ❌ 反模式 2：吞异常
```java
// 错误
try {
    rdsService.start(serviceId);
} catch (Exception e) {
    // ❌ 吞异常，不处理也不抛出
}

// 正确
try {
    rdsService.start(serviceId);
} catch (ServiceException e) {
    log.error("启动服务失败: serviceId={}", serviceId, e);
    throw e; // ✅ 重新抛出或转换为业务异常
}
```

### ❌ 反模式 3：SQL 拼接
```java
// 错误：使用 ${} 直接拼接，存在 SQL 注入风险
<select id="selectByName" resultType="User">
    SELECT * FROM sys_user WHERE user_name = '${userName}'
</select>

// 正确：使用 #{} 参数化查询
<select id="selectByName" resultType="User">
    SELECT * FROM sys_user WHERE user_name = #{userName}
</select>
```

### ❌ 反模式 4：硬编码超管判断
```java
// 错误
if (userId == 1L) { // ❌ 硬编码
    // 超管逻辑
}

// 正确
if (SecurityUtils.isAdmin(userId)) { // ✅ 使用工具类
    // 超管逻辑
}
```

### ❌ 反模式 5：租户过滤手动拼接
```java
// 错误
@Service
public class BadServiceImpl {
    public List<RdsService> selectList(RdsServiceVo vo) {
        vo.setTenantId(TenantContext.getTenantId()); // ❌ 手动设置
        return mapper.selectList(vo);
    }
}

// 正确
@Service
public class GoodServiceImpl {
    public List<RdsService> selectList(RdsServiceVo vo) {
        // ✅ 依赖 TenantSqlInterceptor 自动追加 tenant_id 条件
        return mapper.selectList(vo);
    }
}
```

---

**注意**：本清单作为 AI 审查和人工审查的参考，不能完全替代深入的代码审查。对于复杂业务逻辑、架构设计决策，仍需人工审查和团队讨论。
