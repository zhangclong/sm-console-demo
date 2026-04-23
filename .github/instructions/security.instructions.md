---
applyTo: "**/*.java,**/*.vue"
---
# 权限改造指南（Spring MVC + Vue.js）

## 何时使用本文件

- 任务涉及新增接口、修改权限控制、新增页面功能按钮时。
- 调整或重构权限校验逻辑时。
- 修复权限相关安全漏洞时。

## 权限框架概览

本项目采用基于注解的权限控制，通过 Spring AOP 在方法执行前进行权限校验。

### 核心组件

- **后端权限注解**：`@PrePermission("xxx:yyy:zzz")`
  - 定义在 `console-admin/src/main/java/com/uh/common/annotation/PrePermission.java`
  - 通过 `PermissionAspect` 切面拦截，在方法执行前校验权限
  - 权限字符串格式：`模块:功能:操作`（如 `system:user:list`、`console:rdsservice:create`）
- **前端权限指令**：`v-hasPermi="['xxx:yyy:zzz']"`
  - 用于控制页面按钮/操作的显示/隐藏
  - 权限标识必须与后端 `@PrePermission` 保持一致
- **权限校验**：
  - `SecurityUtils.getLoginUser()` 获取当前登录用户
  - `LoginUser.getPermissions()` 返回用户权限集合
  - `PermissionAspect.hasPermi(permission)` 判断是否具备指定权限
- **超级管理员**：
  - 使用 `SecurityUtils.isAdmin(userId)` 判断是否为平台超管
  - 超管拥有所有权限（`*:*:*`），跳过常规权限检查

## 权限改造规范

### 1. 新增 Controller 接口

当新增 Controller 接口时，必须按照以下步骤完成权限配置：

#### 后端 Controller
```java
@RestController
@RequestMapping("/console/example")
public class ExampleController extends BaseController {

    // ✅ 正确：所有非公开接口必须加 @PrePermission
    @PrePermission("console:example:list")
    @GetMapping("/list")
    public TableDataInfo list(ExampleVo queryVo) {
        startPage();
        List<Example> list = exampleService.selectList(queryVo);
        return getDataTable(list);
    }

    // ✅ 正确：写操作必须同时加 @Log 用于审计
    @Log(title = "ExampleManagement", businessType = BusinessType.INSERT)
    @PrePermission("console:example:create")
    @PostMapping
    public AjaxResult add(@RequestBody Example example) {
        return toAjax(exampleService.insert(example));
    }

    // ❌ 错误：缺少权限注解
    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody Long[] ids) {
        return toAjax(exampleService.deleteByIds(ids));
    }
}
```

#### 前端页面按钮
```vue
<template>
  <el-button
    type="primary"
    icon="el-icon-plus"
    size="mini"
    @click="handleAdd"
    v-hasPermi="['console:example:create']"
  >
    新增
  </el-button>

  <el-button
    type="danger"
    icon="el-icon-delete"
    size="mini"
    @click="handleDelete"
    v-hasPermi="['console:example:delete']"
  >
    删除
  </el-button>
</template>
```

**权限标识对应关系 Checklist：**
1. 后端 Controller 方法加 `@PrePermission("console:example:create")`
2. 前端按钮加 `v-hasPermi="['console:example:create']"`
3. 权限字符串必须完全一致（包括大小写、冒号分隔）
4. 在 `console-admin/src/main/resources/menu.yml` 中补充对应 `menuCode`

### 2. 权限粒度划分

按照以下约定划分权限标识：

| 操作类型 | 权限后缀 | 说明 | 示例 |
|---------|---------|------|------|
| 查询列表 | `:list` | 查询列表（含分页） | `console:rdsservice:list` |
| 查询详情 | `:query` | 查询单条记录详情 | `system:user:query` |
| 新增 | `:create` 或 `:add` | 新增记录 | `console:example:create` |
| 修改 | `:edit` 或 `:update` | 修改记录 | `system:menu:edit` |
| 删除 | `:delete` 或 `:remove` | 删除记录 | `console:rdsservice:delete` |
| 导出 | `:export` | 导出数据（Excel 等） | `system:user:export` |
| 导入 | `:import` | 导入数据 | `system:user:import` |
| 重置密码 | `:resetPwd` | 用户密码重置 | `system:user:resetPwd` |
| 状态变更 | `:changeStatus` | 启用/禁用等状态操作 | `system:user:changeStatus` |
| 特殊操作 | 自定义 | 业务特定操作（如启动、停止） | `console:rdsservice:start` |

**命名规范：**
- 前缀使用模块名：`system`（系统管理）、`console`（控制台业务）
- 中间使用功能名：`user`、`menu`、`rdsservice`、`version`
- 后缀使用操作名：参考上表

### 3. 跨租户权限控制

当涉及多租户隔离时，除了权限校验外，还需结合租户上下文：

```java
@PrePermission("console:rdsservice:list")
@GetMapping("/list")
public TableDataInfo list(RdsServiceVo queryVo) {
    // ✅ 正确：Service 层会自动通过 TenantSqlInterceptor 追加租户过滤
    // 平台超管（tenant_id=1）可查看所有租户数据
    startPage();
    List<RdsService> list = rdsServiceService.selectList(queryVo);
    return getDataTable(list);
}

// ❌ 错误：不要在 Service 或 Controller 中手动拼接 tenant_id 条件
// 应依赖 TenantSqlInterceptor 自动处理
```

**跨租户操作接口规范：**
- 平台超管跨租户操作接口（如查看所有租户数据）必须在 Service 层用 `TenantContext.isPlatformAdmin()` 做二次校验
- 不能只依赖前端权限按钮，必须在后端强制校验

### 4. 公开接口处理

极少数无需登录的公开接口（如登录、验证码、健康检查）不加 `@PrePermission`：

```java
@RestController
@RequestMapping("/system")
public class SysLoginController {

    // ✅ 正确：登录接口无需权限校验
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        // ...
    }

    // ✅ 正确：验证码接口无需权限校验
    @GetMapping("/captcha")
    public AjaxResult getCaptcha() {
        // ...
    }
}
```

**公开接口清单（需明确记录并定期审查）：**
- `/system/login` - 用户登录
- `/system/captcha` - 验证码获取
- `/actuator/health` - 健康检查（如有）

### 5. 菜单树与权限标识同步规则

当菜单或权限发生变更时，必须同时维护以下三处并保持一致：

1. `console-admin/src/main/resources/menu.yml` 中的 `menuCode`；
2. 后端 Controller 的 `@PrePermission("xxx:yyy:zzz")`；
3. 前端页面按钮的 `v-hasPermi="['xxx:yyy:zzz']"`。

**强制要求：**
- 菜单裁剪必须按 `docs/菜单项整理.csv` 执行。
- 删除目录（`menu_type='M'`）或菜单（`menu_type='C'`）时，必须同步删除所有后代按钮（`menu_type='F'`）。
- 不允许保留“孤儿按钮权限”（`parent_id` 指向已删除菜单）。
- 不允许前后端继续使用已从运行时菜单配置中删除的权限标识。

## 权限变更 Checklist

当新增或修改权限时，必须完成以下步骤：

- [ ] 后端 Controller 方法加 `@PrePermission("xxx:yyy:zzz")`
- [ ] 前端按钮/操作加 `v-hasPermi="['xxx:yyy:zzz']"`
- [ ] 确保前后端权限标识完全一致
- [ ] 写操作添加 `@Log` 注解用于审计
- [ ] 如涉及敏感操作，加强业务层二次校验
- [ ] 在 `console-admin/src/main/resources/menu.yml` 中添加/调整对应 `menuCode`
- [ ] 菜单裁剪/删除时已同步清理前端按钮、角色授权回显和兼容初始化数据
- [ ] `menu.yml.menuCode`、`@PrePermission`、`v-hasPermi` 三处权限标识完全一致
- [ ] 如涉及跨租户数据，确认租户隔离逻辑正确
- [ ] 测试：普通用户无权限时返回 403 错误，有权限时正常访问
- [ ] 测试：前端按钮在无权限时正确隐藏

## 禁止事项

- **禁止**跳过权限校验直接开放接口。
- **禁止**前后端权限标识不一致（导致前端可见但后端拒绝，或前端隐藏但后端允许）。
- **禁止**在权限注解中使用动态权限字符串（必须是编译时常量）。
- **禁止**在 Service 层做权限判断（权限校验统一在 Controller 层通过 `@PrePermission`）。
- **禁止**将权限校验逻辑散落在多处，必须依赖框架统一管理。

## 常见问题

### Q1: 新增接口报 403 错误？
**A:** 检查：
1. Controller 方法是否加 `@PrePermission`
2. 权限标识是否已在 `menu.yml` 中配置并被当前角色授权
3. 当前登录用户角色是否关联该权限

### Q2: 前端按钮显示但点击报权限错误？
**A:** 前后端权限标识不一致，检查 `v-hasPermi` 与 `@PrePermission` 的值是否完全相同。

### Q4: 如何批量修改权限标识？
**A:** 谨慎操作，需同步修改：
1. 后端 Controller 所有 `@PrePermission` 注解
2. 前端所有 `v-hasPermi` 指令
3. `console-admin/src/main/resources/menu.yml` 中的 `menuCode`

## 参考文件

- `console-admin/src/main/java/com/uh/framework/aspectj/PermissionAspect.java` - 权限切面实现
- `console-admin/src/main/java/com/uh/common/annotation/PrePermission.java` - 权限注解定义
- `console-admin/src/main/java/com/uh/common/utils/SecurityUtils.java` - 安全工具类
- `docs/multitenancy/tenant.instructions.md` - 多租户开发指南
- `.github/instructions/backend-java.instructions.md` - 后端 Java 开发指南
- `.github/instructions/frontend-ui.instructions.md` - 前端 UI 开发指南
