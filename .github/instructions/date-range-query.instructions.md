---
applyTo: "**/*.vue,**/*.java,**/*Mapper.xml"
---
# 日期范围查询规范（begin* ~ end*）

## 何时使用本文件

- 页面需要按日期范围筛选列表数据时。
- 后端 Controller/Service/Mapper 需要接收并处理 `begin*`、`end*` 条件时。
- 需要保证日期条件查询安全（防 SQL 注入）与前后端参数一致性时。

## 标准链路（必须按层传递）

1. **UI（Vue）**：页面使用日期范围控件（如 `el-date-picker type=\"daterange\"`）绑定 `dateRange`。
2. **API 请求**：调用 `this.addDateRange(queryParams, dateRange)`，将范围写入 `queryParams.params` 下的一对开始/结束字段（如 `beginTime/endTime`、`beginCreateTime/endCreateTime`）。
3. **Controller**：接收查询对象（如 `SysUser user`），不在 Controller 里解析日期字符串。
4. **Service**：调用 `DateUtils.checkDateFormat(user.getParams(), beginKey, endKey)` 做格式白名单校验。
5. **Mapper.xml**：通过与 Service 相同的 `params.begin*/end*` 字段拼接查询条件（仅允许在完成 Service 校验后使用）。

## UI/Vue 规范

- 查询参数统一放在 `queryParams`，日期范围单独使用 `dateRange` 数组（`yyyy-MM-dd`）。
- 列表查询必须通过 `this.addDateRange(this.queryParams, this.dateRange)` 传参，禁止手工拼接 URL 参数。
- 日期控件建议固定：
  - `type="daterange"`
  - `value-format="yyyy-MM-dd"`
- 重置查询时必须同时清空 `dateRange`，避免残留条件。

示例：

```js
listUser(this.addDateRange(this.queryParams, this.dateRange))
```

## Service 规范

- 所有使用 `params.begin*/end*` 的列表查询方法，必须先调用 `checkDateFormat(query.getParams(), beginKey, endKey)`。
- `beginKey` 与 `endKey` 必须和 UI 传入字段、Mapper 引用字段完全一致。
- 日期格式校验必须在 Service 层完成，禁止在 Mapper 或 Controller 中补校验。
- 校验失败应抛出业务异常，禁止静默忽略非法日期字符串。

示例：

```java
checkDateFormat(query.getParams(), "beginCreateTime", "endCreateTime");
```

## Mapper.xml 规范

- 条件字段名不强制固定为 `beginTime/endTime`，但必须使用成对命名（`begin*` + `end*`），且与 UI/Service 保持一致。
- 开始时间和结束时间必须成对支持，结束时间补 `23:59:59`。
- 参考写法（以 `beginCreateTime/endCreateTime` 为例）：

```xml
<if test="params.beginCreateTime != null and params.beginCreateTime != ''">
  AND create_time &gt;= '${params.beginCreateTime} 00:00:00'
</if>
<if test="params.endCreateTime != null and params.endCreateTime != ''">
  AND create_time &lt;= '${params.endCreateTime} 23:59:59'
</if>
```

- 注意：上述 `${}` 仅可用于已在 Service 层通过 `checkDateFormat` 校验的日期字段；其他场景仍应优先使用 `#{}` 参数化。

## 常见错误

- 在 Vue 页面直接拼接日期参数（如 `beginTime=...&endTime=...`）。
- 只改 Mapper，不在 Service 增加 `checkDateFormat`。
- Mapper 使用了 `params.begin*/end*`，但前端未调用 `addDateRange(...)`。
- UI、Service、Mapper 三层字段名不一致（例如 UI 传 `beginCreateTime`，Mapper 却写 `beginTime`）。
- `end*` 未补全天结束时间，导致当日数据被漏查。
