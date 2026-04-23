# Backend Java 指南（Spring MVC + MyBatis）

## 何时使用本文件

- 任务涉及 `console-admin` 后端接口、业务逻辑、MyBatis 映射、数据库字段变更时。

## 后端分层与目录（本仓库约定）

- Controller（Spring MVC）：
  - `console-admin/src/main/java/com/uh/console/web/*Controller.java`
  - `console-admin/src/main/java/com/uh/system/web/*Controller.java`
- Service 接口：
  - `console-admin/src/main/java/com/uh/*/service/*Service.java`
- Service 实现：
  - `console-admin/src/main/java/com/uh/*/service/impl/*ServiceImpl.java`
- DAO（MyBatis Mapper 接口）：
  - `console-admin/src/main/java/com/uh/*/mapper/*Mapper.java`
- MyBatis XML：
  - `console-admin/src/main/resources/mapper/console/*Mapper.xml`
  - `console-admin/src/main/resources/mapper/system/*Mapper.xml`
- Domain/VO：
  - 主要位于 `console-admin/src/main/java/com/uh/**/domain/**`、`.../domain/vo/**`
  - 菜单运行时定义见 `console-admin/src/main/java/com/uh/system/domain/MenuDefinition.java`

## 代码流转规则

1. Controller 只负责参数接收、权限校验、调用 Service、返回 `AjaxResult`/`TableDataInfo`。
2. Service 负责业务编排、事务边界、跨表逻辑，不把业务堆到 Controller。
3. Mapper 接口定义方法，SQL 写在同名 `*Mapper.xml`；接口方法名与 XML `id` 保持一致。
4. 字段/实体变化必须同步更新：Domain/VO、Mapper 接口、Mapper XML（`resultMap`/SQL）。
5. 当前菜单树/授权树是配置驱动：菜单查询优先走 `MenuRegistry` + `menu.yml`，不要把运行时菜单树重新做回 `SysMenuMapper` 数据库查询。
6. 当前字典是枚举驱动：字典查询/标签转换优先走 `DictRegistry` / `DictUtils`，不要新增运行时数据库字典依赖。

## 书写规范

- Controller：
  - 只允许使用GET和POST两种HTTP方法，避免PUT/DELETE等不常用方法。在接口设计时，尽量使用RESTful风格的URL和HTTP方法，但在实现时统一使用GET和POST，以简化权限控制和前端调用。
  - 使用 `@RestController + @RequestMapping`（按模块前缀，如 `/system/menu`、`/console/rdsservice`）。
  - 查询分页走 `BaseController.startPage()` + `getDataTable(...)`。
  - 增删改优先走 `toAjax(...)` 统一返回。
  - 权限注解与前端按钮权限一致（如 `@PrePermission("system:menu:list")`）。
  - 需要审计的写操作加 `@Log`。
- Service：
  - 接口放 `service`，实现放 `service/impl`，实现类命名 `*ServiceImpl`。
  - 多表写入、级联删除、状态联动必须加 `@Transactional`。
  - 时间、创建人/更新人按现有方式补齐（如 `DateUtils.getNowDate()`）。
- Mapper/MyBatis：
  - XML `namespace` 必须是 Mapper 接口全限定名。
  - 多参数显式 `@Param`，避免参数名歧义。
  - 优先复用已有 `resultMap`/`<sql id="...">` 片段，避免重复 SQL。
  - 不在 Java 里拼 SQL，不在 XML 里写与业务强耦合的流程控制。
  - 角色菜单授权当前存储于 `sys_role_menu.menu_code`；后端入参/持久化字段使用 `menuCodes` / `menuCode`，不是旧的 `menuIds` / `menuId`。

## 禁止事项

- 不要在一个任务里顺手重构无关模块。
- 不要吞异常（catch 后无处理/无抛出）。
- 不要新增与现有分层冲突的“跨层直连”（例如 Controller 直接操作 Mapper）。
