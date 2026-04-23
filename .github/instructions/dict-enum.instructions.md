---
applyTo: "**/dict/**/*.java,**/SystemDictEnum.java,**/DictType.java,**/DictRegistry.java"
---
# DictEnum 枚举字典开发指南

## 何时使用本文件

- 新增业务字典类型时。
- 修改或扩展现有字典枚举时。
- 了解字典枚举的运行机制和开发规范时。

## 背景

本项目已将"数据库驱动的字典管理"迁移为"代码级枚举驱动的字典管理"。所有字典数据均通过 Java 枚举定义，Spring 启动时由 `DictRegistry` 自动扫描并缓存到内存。前端通过 `GET /system/dict/data/type/{dictType}` 接口获取字典数据，接口契约与原有数据库方式完全兼容。

设计文档参见：`docs/dict-refactoring-design.md`。

## 核心组件

| 组件 | 位置 | 职责 |
|------|------|------|
| `SystemDictEnum` | `com.uh.common.core.domain.SystemDictEnum` | 所有字典枚举必须实现的接口 |
| `@DictType` | `com.uh.common.annotation.DictType` | 标注在枚举类上，声明对应的前端 `dictType` 键名 |
| `DictRegistry` | `com.uh.system.manage.DictRegistry` | Spring 启动时扫描枚举、转换为 `SysDictData` 列表并缓存 |
| `SysDictData` | `com.uh.system.domain.SysDictData` | 运行时数据传输对象，兼容前端消费格式 |
| `DictUtils` | `com.uh.common.utils.DictUtils` | 工具类，提供 `getDictLabel` / `getDictValue` 方法（底层读 `DictRegistry`） |

## 新增字典枚举步骤

### 1. 创建枚举类

在合适的包下新建 `XxxDict.java`：

- **系统通用字典**放在 `com.uh.system.dict` 包下。
- **业务模块字典**放在 `com.uh.console.dict` 包下（或其他业务模块对应的 `dict` 包）。

```java
package com.uh.system.dict; // 或 com.uh.console.dict

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("your_dict_type_key")   // 前端 getDicts("your_dict_type_key") 使用的键名
public enum YourDict implements SystemDictEnum {
    ITEM_A("value_a", "标签A", "primary"),
    ITEM_B("value_b", "标签B", "danger");

    private final String value;     // 对应 dictValue
    private final String label;     // 对应 dictLabel（直接写中文）
    private final String listClass; // 对应 listClass（primary/danger/warning/info/空）

    YourDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
```

### 2. 确认扫描包已注册

`DictRegistry.init()` 当前扫描以下包：

- `com.uh.system.dict`
- `com.uh.console.dict`

如果新增枚举放在其他包下（如 `com.uh.newmodule.dict`），需要在 `DictRegistry.init()` 中追加：

```java
candidates.addAll(scanner.findCandidateComponents("com.uh.newmodule.dict"));
```

### 3. 前端消费

前端无需任何额外配置，直接使用：

```js
// 在 Vue 组件的 dicts 选项中声明
dicts: ['your_dict_type_key']

// 在模板中使用
<dict-tag :options="dict.type.your_dict_type_key" :value="scope.row.fieldName"/>
```

### 4. 后端代码中获取字典标签

```java
// 方式一：通过 DictUtils 工具类
String label = DictUtils.getDictLabel("your_dict_type_key", "value_a");

// 方式二：直接引用枚举常量（推荐，编译期检查）
String label = YourDict.ITEM_A.getLabel();
String value = YourDict.ITEM_A.getValue();
```

## `listClass` 取值约定

| 值 | 含义 | 适用场景 |
|----|------|---------|
| `"primary"` | 蓝色/主色调 | 正常、启用、是 |
| `"success"` | 绿色 | 成功状态 |
| `"warning"` | 黄色/橙色 | 警告、待处理 |
| `"danger"` | 红色 | 停用、失败、否、锁定 |
| `"info"` | 灰色 | 信息、默认 |
| `""` | 无样式 | 不需要特殊样式标识的条目 |

## `@DictType` 键名命名规范

- 系统通用字典：以 `sys_` 开头，如 `sys_user_sex`、`sys_normal_disable`。
- 控制台业务字典：以 `cnsl_` 开头，如 `cnsl_node_type`、`cnsl_deploy_mode`。
- 键名全小写，单词间以下划线 `_` 分隔。
- 键名在全局唯一，不得重复。

## 现有字典枚举清单

### 系统通用（`com.uh.system.dict`）

| 枚举类 | dictType | 说明 |
|--------|----------|------|
| `SysUserSexDict` | `sys_user_sex` | 用户性别 |
| `SysNormalDisableDict` | `sys_normal_disable` | 正常/停用 |
| `SysYesNoDict` | `sys_yes_no` | 是/否 |
| `SysShowHideDict` | `sys_show_hide` | 显示/隐藏 |
| `SysCommonStatusDict` | `sys_common_status` | 成功/失败 |
| `SysUserLoginLockedDict` | `sys_user_login_locked` | 正常/锁定 |
| `SysOperTypeDict` | `sys_oper_type` | 业务操作类型 |

### 控制台业务（`com.uh.console.dict`）

| 枚举类 | dictType | 说明 |
|--------|----------|------|
| `CnslNodeTypeDict` | `cnsl_node_type` | 节点类型 |
| `CnslCommandResultDict` | `cnsl_command_result` | 指令执行结果 |
| `CnslCommandTypeDict` | `cnsl_command_type` | 指令类型 |
| `CnslDeployModeDict` | `cnsl_deploy_mode` | 部署模式 |
| `CnslPackageTypeDict` | `cnsl_package_type` | 安装包类型 |
| `CnslTemplateTypeDict` | `cnsl_template_type` | 模版配置类型 |
| `CnslNodeManagedDict` | `cnsl_node_managed` | 节点管理状态 |

## 禁止事项

- **禁止**在数据库中新建 `sys_dict_type` / `sys_dict_data` 表或数据。字典已完全迁移到枚举。
- **禁止**在代码中直接拼写字典值的魔法字符串，应使用枚举常量引用。
- **禁止**修改 `SystemDictEnum` 接口签名（`getValue` / `getLabel` / `getListClass`），否则会影响所有已有枚举。
- **禁止**在 `@DictType` 注解中使用重复的键名，否则后注册的枚举会覆盖先注册的。

## 与旧有业务枚举的关系

`com.uh.console.enums` 下的业务枚举（如 `CommandResultEnum`、`PackageTypeEnum`、`TemplateTypeEnum`）主要用于后端业务逻辑处理（解析、比较等），不实现 `SystemDictEnum` 接口。

`com.uh.console.dict` 下的字典枚举专门用于前端展示。两者各司其职，互不干扰：

- **业务枚举**：在 Service 层做值解析和业务逻辑判断。
- **字典枚举**：在 Controller 层通过 `DictRegistry` 输出给前端做下拉选项和标签展示。
