# DB Development 指南

## 何时使用本文件

- 对初始化数据库脚本进行变更，包括数据库定义和初始化数据进行变更（例如添加/修改表、列、索引、初始化数据等）。

## 通用规范和原则
- 所有数据库变更都必须通过 SQL 脚本进行。
- 脚本必须符合 MySQL 8.4 的 SQL 语法（InnoDB、utf8mb4 字符集 + `utf8mb4_0900_ai_ci` 排序规则）。
- 如果需要审计字段（创建者、创建时间、修改者、修改时间），请按以下名称和定义命名：
  ```sql
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  ```

## 菜单与权限初始化数据

- 当前运行时菜单树事实源是 `console-admin/src/main/resources/menu.yml`，由 `com.uh.system.manage.MenuRegistry` 在启动时加载；不要把数据库菜单数据当作运行时菜单来源。
- 调整菜单、权限点或角色默认授权时，先更新 `menu.yml` 中的 `menuCode`，再同步检查数据库初始化脚本中的默认 RBAC 数据，避免初始化数据库与运行时权限码脱节。
- 角色菜单授权当前落在 `sys_role_menu.menu_code`，与后端 `menuCodes` / `menuCode` 字段保持一致。

## 字典初始化数据

- 当前运行时字典由 `DictRegistry` 扫描 `com.uh.system.dict` / `com.uh.console.dict` 下枚举提供，不再依赖 `sys_dict_type` / `sys_dict_data` 作为运行时事实源。
- 新增或调整字典项时，优先修改枚举与 `@DictType`，不要再往运行时数据库脚本中补充字典初始化数据。

## 初始化DB的SQL脚本
- 初始化DB的SQL脚本位于 `console-admin/sql/` 目录下。
- 包括：`1.commons.sql`：定义公共表结构和基础数据。
  是通过模板自动生成的，不应该直接修改。
- `1.commons.sql` 会作为类路径资源打包进 `console-admin`。
- 应用或测试在首次启动、连接目标 MySQL 库后，若 `sys_user` 表不存在，会通过 `com.uh.system.service.MySqlDatabaseInitializer` 自动执行 `1.commons.sql` 完成数据库初始化。
