# Agent 开发任务模板（sm-console）

你在仓库 `zhangclong/sm-console` 工作。请严格遵守：
- `.github/copilot-instructions.md`
- `.github/instructions/**/*.instructions.md`

---

## 1. 任务目标（必填）
<一句话描述要实现的目标>

## 2. 变更范围（必填）
- 允许修改的模块/目录：
    - `console-admin/...`
    - `console-ui/...`
- 禁止修改范围：
    - <无关模块/公共基础代码/非本需求文件>

## 3. 强约束（必填）
- 仅做**最小且安全改动**，禁止无关重构
- 禁止引入 `spring-boot-*` 依赖
- 优先复用现有 `service/mapper/util`
- 若涉及接口字段变化，必须前后端联动一致
- 若涉及日期范围筛选，必须保证 `beginTime/endTime` 从 Vue → Controller/Service → Mapper 全链路一致
- 保持现有代码风格、命名与目录结构

## 4. 验收标准（必填，可勾选）
- [ ] 功能按预期可用
- [ ] 不影响现有主要流程
- [ ] 编译通过
- [ ] 相关测试通过（如有）
- [ ] 关键边界/空值/异常场景已覆盖

## 5. 输出格式（严格按顺序）
请按以下结构返回，不要省略：

### A. 变更计划
- 受影响文件列表
- 每个文件的修改目的
- 风险点与注意事项

### B. 实施改动
- 逐文件说明实际改动
- 仅展示必要代码片段/差异点

### C. 自验证与自测试（必填）
根据改动范围执行最小验证命令，并贴“命令 + 结果摘要”。

```bash
# 若改前端
cd console-ui && mvn clean compile

# 若改后端
cd console-admin && mvn clean compile

# 若改后端逻辑/接口
cd console-admin && mvn test -DskipTests=false