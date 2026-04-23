## 规则分级草案（建议）

### A 级：必须阻断（`error`）
用于防止真实 bug 与运行风险，保留为红线。
- JS 安全与正确性：`no-undef`、`no-unreachable`、`no-dupe-keys`、`no-dupe-args`、`no-duplicate-case`、`no-const-assign`、`no-class-assign`、`no-unsafe-finally`、`valid-typeof`、`eqeqeq`
- Vue 结构正确性（由 `plugin:vue/recommended` 继承的关键规则）：重复属性、模板解析错误、`v-for` key 等
- 结论：A 级只保留“会出错/高风险”规则，不含纯格式要求

### B 级：质量提醒（`warn`）
不阻断交付，但提示逐步改善。
- `no-unused-vars`（建议先保留 `args: none`）
- `prefer-const`
- `no-console`（开发阶段可 `off`，CI 可 `warn`）
- 少量 Vue 可维护性规则（不影响运行但影响长期质量）

### C 级：风格类暂缓（`off` 或后续再开）
你当前报错大头，先不阻断，后续通过自动修复逐步收口。
- `semi`、`quotes`、`eol-last`、`object-curly-spacing`、`key-spacing`
- `space-before-function-paren`、`array-bracket-spacing`、`no-trailing-spaces`
- `no-multiple-empty-lines`、`padded-blocks`、`indent`
- `vue/max-attributes-per-line`、`vue/singleline-html-element-content-newline` 等模板排版类规则

## 落地节奏（建议）

1. **第 1 阶段（立即）**  
   在 `console-ui/.eslintrc.js` 落地 A/B/C 分级；目标是 `npm run lint` 先能稳定反映“真实问题”，不再被风格噪音淹没。
2. **第 2 阶段（1~2 周）**  
   对 C 级规则做一次自动修复与批量格式化，再把一部分 C 级提升为 `warn`。
3. **第 3 阶段（稳定后）**  
   仅对新增/改动文件逐步收紧（而不是全量旧代码一次性收紧）。

## CI 策略建议

- 当前阶段：CI 只对 **A 级 error** 阻断。
- B 级仅统计不阻断（可做报表）。
- C 级不纳入阻断，避免影响现有功能迭代效率。
