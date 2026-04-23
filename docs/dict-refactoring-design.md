# 字典管理重构设计方案（基于 Java Enum 的代码级驱动）

## 1. 背景与目标
在现有系统架构中，"字典数据"（如性别、状态、节点类型、部署模式等）存放在 `sys_dict_type` 和 `sys_dict_data` 数据库表中。对于实际发布的软件产品，随着版本的不断迭代，大量预置的字典数据只能依靠升级 SQL 脚本进行维护。

为了避免由于 SQL 漏刷、错刷带来的运行时错误，并消除每次启动、字典调取时产生的冗余数据库 IO 请求，本方案旨在将"数据库驱动的高级字典"改为"代码级驱动的枚举字典"。

**核心目标：**
1. **剔除冗余表**：彻底废弃和删除 `sys_dict_type`、`sys_dict_data` 数据表及其对应的 Mapper/XML。
2. **强类型约束**：在后端通过 Java 枚举呈现，业务代码里可以直接调用枚举获得编译期的数据检查，消除魔法字符串（Magic Strings）。
3. **前端 0 感知**：继续对外提供标准 HTTP 查询接口，将枚举转换为兼容前端消费的 `SysDictData` 结构输出（直接输出中文字符串作为 Label，无需国际化转换）。

> 补充说明：在当前实现中，`SysDictData` 运行时模型已经进一步瘦身，移除了历史遗留但已无消费方依赖的 `dictSort`、`status` 两个属性。

---

## 2. 总体架构设计

### 2.1 核心组件
1. **基础字典接口 `SystemDictEnum`**：所有需要暴露给前端的字典枚举都必须实现此接口。
2. **字典标识注解 `@DictType`**：标注在枚举类上，用于声明当前枚举映射到前端的哪个 `dictType` 键名（比如 `@DictType("sys_user_sex")`）。
3. **字典注册中心 `DictRegistry`**：负责在 Spring 容器启动时，扫描所有被 `@DictType` 标记的枚举，并转换并缓存为 `Map<String, List<SysDictData>>` 结构。
4. **接口适配器**：将旧的 `SysDictDataController` 的查询接口重定向至 `DictRegistry`，屏蔽其它增删改查操作。

### 2.2 运行时兼容字段范围
当前字典查询接口对外返回的 `SysDictData` 字段以"实际消费最小集"为准：

- **保留**：`dictCode`、`dictLabel`、`dictValue`、`dictType`、`listClass`、`isDefault`
- **已移除**：`dictSort`、`status`、`dictI18nKey`（sm-console 无需国际化）

移除依据：
1. `DictRegistry.getDictDataList()` 运行时不会填充 `dictSort` 和 `status` 字段；
2. 前端组件未消费这两个字段；
3. 本项目无多语言需求，无需 `dictI18nKey`。

注意：历史版本升级 SQL 中仍可能保留 `sys_dict_data`、`dict_sort`、`status` 的旧结构或数据，这是迁移历史记录的一部分，**不建议回改旧升级脚本**。

### 2.3 数据流向
1. **系统启动拦截**：注册中心基于反射将枚举装载入内存。
2. **前端发拉取请求**：前端调用 `getDicts("sys_user_sex")`。
3. **后端响应**：Controller 利用 `dictType` 查内存，直接读取枚举的 `label` 属性并拼装成 List 返回，最终达到和查数据库完全相同的接口契约效果。

---

## 3. 具体实施步骤

### 阶段一：建立枚举规范及注册机制

1. **创建基础枚举接口 `SystemDictEnum.java`**
```java
package com.uh.common.core.domain;

public interface SystemDictEnum {
    String getValue();       // 对应原来的 dictValue，如 "0", "1"
    String getLabel();       // 对应原来的 dictLabel，直接返回中文，如 "男", "女"
    String getListClass();   // 对应原来的 listClass，如 "primary", "danger"
}
```

2. **创建字典类型注解 `@DictType.java`**
```java
package com.uh.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictType {
    String value(); // 如 "sys_user_sex"
}
```

3. **创建字典注册器 `DictRegistry.java`**
```java
package com.uh.system.manage;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;
import com.uh.system.domain.SysDictData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DictRegistry {

    private final Map<String, List<SysDictData>> dictCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DictType.class));

        Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.uh.system.dict");
                    // Add more base packages here for business-domain enums, e.g.:
                    // candidates.addAll(scanner.findCandidateComponents("com.uh.console.dict"));
        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                if (clazz.isEnum() && SystemDictEnum.class.isAssignableFrom(clazz)) {
                    DictType dictTypeAnnotation = clazz.getAnnotation(DictType.class);
                    String dictType = dictTypeAnnotation.value();
                    Object[] constants = clazz.getEnumConstants();
                    List<SysDictData> dictDataList = new ArrayList<>();
                    long dictCode = 1;
                    for (Object constant : constants) {
                        SystemDictEnum dictEnum = (SystemDictEnum) constant;
                        SysDictData dictData = new SysDictData();
                        dictData.setDictCode(dictCode++);
                        dictData.setDictLabel(dictEnum.getLabel());
                        dictData.setDictValue(dictEnum.getValue());
                        dictData.setDictType(dictType);
                        dictData.setListClass(dictEnum.getListClass());
                        dictData.setIsDefault("N");
                        dictDataList.add(dictData);
                    }
                    dictCache.put(dictType, Collections.unmodifiableList(dictDataList));
                }
            } catch (ClassNotFoundException e) {
                // ignore classes that cannot be loaded
            }
        }
    }

    /**
     * 根据字典类型获取字典数据列表；若注册中心中无该类型，返回 null。
     */
    public List<SysDictData> getDictDataList(String dictType) {
        return dictCache.get(dictType);
    }

    /**
     * 获取注册中心中全部已注册的字典类型键名列表。
     */
    public List<String> getDictTypes() {
        return new ArrayList<>(dictCache.keySet());
    }
}
```

### 阶段二：实现具体枚举

为每个需要暴露给前端的字典项创建枚举，放在 `com.uh.system.dict` 包下（或按业务模块就近放置）。

**示例：`SysUserSexDict.java`（用户性别）**
```java
package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_user_sex")
public enum SysUserSexDict implements SystemDictEnum {
    MALE("0", "男", ""),
    FEMALE("1", "女", ""),
    UNKNOWN("2", "未知", "");

    private final String value;
    private final String label;
    private final String listClass;

    SysUserSexDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
```

**示例：`SysNormalDisableDict.java`（系统开关）**
```java
package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_normal_disable")
public enum SysNormalDisableDict implements SystemDictEnum {
    NORMAL("0", "正常", "primary"),
    DISABLE("1", "停用", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysNormalDisableDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
```

**示例：`SysYesNoDict.java`（系统是否）**
```java
package com.uh.system.dict;

import com.uh.common.annotation.DictType;
import com.uh.common.core.domain.SystemDictEnum;

@DictType("sys_yes_no")
public enum SysYesNoDict implements SystemDictEnum {
    YES("Y", "是", "primary"),
    NO("N", "否", "danger");

    private final String value;
    private final String label;
    private final String listClass;

    SysYesNoDict(String value, String label, String listClass) {
        this.value = value;
        this.label = label;
        this.listClass = listClass;
    }

    @Override public String getValue()     { return value; }
    @Override public String getLabel()     { return label; }
    @Override public String getListClass() { return listClass; }
}
```

### 阶段三：Controller 适配

修改 `SysDictDataController.dictType()` 接口，由查询数据库改为查询 `DictRegistry`：

```java
// 注入 DictRegistry
@Autowired
private DictRegistry dictRegistry;

/**
 * 根据字典类型查询字典数据信息（枚举驱动，不再查数据库）
 */
@GetMapping(value = "/type/{dictType}")
public AjaxResult dictType(@PathVariable("dictType") String dictType) {
    List<SysDictData> data = dictRegistry.getDictDataList(dictType);
    if (StringUtils.isNull(data)) {
        data = new ArrayList<SysDictData>();
    }
    return AjaxResult.success(data);
}
```

### 阶段四：后续清理（可选，视业务需求推进）

以下为完整迁移所需的清理工作，在各字典枚举均已迁移完毕后统一执行：

1. 删除 `sys_dict_type` 和 `sys_dict_data` 数据库表的 `DROP TABLE` 脚本（写入升级 SQL）。
2. 删除 `SysDictTypeMapper`、`SysDictDataMapper` 接口及对应的 XML 文件。
3. 删除 `SysDictTypeService`、`SysDictDataService` 接口及其实现类。
4. 简化 `SysDictDataController`，保留 `/type/{dictType}` 查询接口，移除其它增删改查操作（因为字典不再由数据库维护，增删改不再有意义）。
5. 移除前端字典管理页面（`views/system/dict/index.vue` 和 `views/system/dict/data.vue`）及对应路由和菜单项。

---

## 4. 新增字典枚举操作手册

当业务需要新增一个字典类型时，操作如下：

1. 在 `com.uh.system.dict`（或业务模块对应包）下新建 `XxxDict.java` 枚举，实现 `SystemDictEnum` 接口，并标注 `@DictType("xxx_dict_key")`。
2. 无需改数据库脚本，无需重启后重刷缓存，Spring 容器初始化时 `DictRegistry` 会自动装载。
3. 前端继续使用 `getDicts("xxx_dict_key")` 消费，行为不变。

---

## 5. 迁移兼容性说明

| 场景 | 处理方式 |
|------|---------|
| 旧升级 SQL 中有 `sys_dict_data` 表结构或数据 | 保留旧脚本，不回改；新部署会先建表再插数据，不影响枚举路径 |
| 前端已存在 `getDicts(dictType)` 调用 | 接口路径不变，返回结构不变，0 修改成本 |
| 前端消费了 `dictSort` 或 `status` 字段 | 需检查并移除相关消费代码（目前前端未直接依赖这两个字段） |
| 业务代码直接查数据库取字典 label | 替换为 `DictRegistry.getDictDataList(dictType)` 或直接引用枚举常量 |
