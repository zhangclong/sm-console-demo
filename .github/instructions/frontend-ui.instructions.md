# Frontend/UI 指南（Vue.js + Element UI）

## 何时使用本文件

- 任务涉及 `console-ui` 模块、页面交互、接口联调、表单/表格开发时。

## 前后端对应关系（本仓库约定）

- 前端接口层：`console-ui/src/api/**/*.js`
  - 每个文件按业务域分组（如 `api/system/menuTree.js`、`api/console/rdsversion.js`）。
  - 每个函数对应一个后端接口 URL 与 method。
- 前端页面层：`console-ui/src/views/**/*.vue`
  - 页面在 `<script>` 中 `import` 对应 `@/api/...` 函数并在 methods 中调用。
- 后端接口层（对应）：
  - `console-admin/src/main/java/com/uh/**/web/*Controller.java`
  - `@RequestMapping` + `@GetMapping/@PostMapping` 组合出的路径必须与 `src/api/*.js` 中 `url` 一致。

### 典型映射示例

- `src/api/system/menuTree.js` → `/system/menu/**` → `com.uh.system.web.SystemMenuController`
- `src/api/system/dict/data.js` → `/system/dict/data/type/{dictType}` → `com.uh.system.web.SysDictDataController`
- `src/api/console/rdsversion.js` → `/console/rdsversion/**` → `com.uh.console.web.RdsVersionController`
- `src/views/system/role/index.vue` 通过 `import { treeselect, roleMenuTreeselect } from "@/api/system/menuTree"` 获取菜单树与角色授权树。
- `src/views/console/rdsversion/index.vue` 通过 `import {...} from "@/api/console/rdsversion"` 调 RDS 服务接口。

## 开发规则

1. 所有 HTTP 请求统一走 `@/utils/request`（禁止页面里直接写 axios）。
2. API 层只做请求封装：函数名清晰（动词+对象），参数透传，不写页面状态逻辑。
3. Vue 页面负责：
   - 调 API；
   - 管理 `loading`、分页、弹窗、表单校验；
   - 渲染 Element UI 组件（`el-table`/`el-form` 等）。
4. 权限一致性：
   - 前端按钮权限 `v-hasPermi="['xxx:yyy:zzz']"` 必须与后端 `@PrePermission("xxx:yyy:zzz")` 对齐。
   - 当前运行时菜单/权限事实源是 `console-admin/src/main/resources/menu.yml` 中的 `menuCode`；前端不得保留 `menu.yml` 中已删除的权限标识。
5. 返回结构约定：
   - 列表页通常消费 `response.rows/response.total`；
   - 普通接口消费 `response.data`；
   - 错误提示依赖 `request.js` 拦截器统一处理。
6. 接口变更时必须同步修改三处：
   - 后端 Controller 路径/入参/返回；
   - `src/api/*.js` 对应函数；
   - 所有引用该 API 的 `src/views/*.vue` 调用与数据处理。
7. 菜单授权页约定：
   - 角色授权提交字段是 `menuCodes`，不是旧的 `menuIds`。
   - `roleMenuTreeselect` 返回的 `checkedKeys` 和树节点 `id` 都是字符串型 `menuCode`；`el-tree node-key="id"` 保持不变。
8. 字典消费约定：
   - 前端继续通过 `getDicts(dictType)` / `/system/dict/data/type/{dictType}` 消费字典。
   - 字典来源是后端枚举 + `DictRegistry`，不要把字典改动理解为前端需要配套数据库字典初始化。

## 构建与运行规则（console-ui）

- Node.js 版本要求：`18+`（建议使用 LTS；具体以项目 `package.json` 的 `engines` 约束和仓库构建配置为准）。

1. 依赖安装统一使用：
   ```bash
   npm install --registry=https://registry.npmmirror.com --legacy-peer-deps
   ```
2. 本地调试使用：
   ```bash
   npm run dev
   ```
3. 生产构建使用：
   ```bash
   npm run build:prod
   ```
4. 生产构建产物位于 `console-ui/dist/`，由流程复制到 `console-admin/src/main/resources/public/`。

## 环境变量约束（console-ui/.env）

- `VUE_APP_CONTEXT_PATH` 必须以 `/` 开头，并以 `/` 结尾。
  - 示例：`/`、`/uhrds/`
- `VUE_APP_BASE_API` 必须与 `VUE_APP_CONTEXT_PATH` 保持一致，格式为：
  - `"${VUE_APP_CONTEXT_PATH}web-api"`
- 本地调试时建议 `VUE_APP_CONTEXT_PATH='/'`，发布时按部署路径配置（如 `/uhrds/`）。
- 调整上述变量后，需同步验证 `vite.config.mjs` 中 `base` 与 `server.proxy` 的行为是否符合预期。

## 禁止事项

- 不要把业务逻辑散落在多个页面里重复实现，优先沉淀到 `src/api` 与通用组件。
- 不要在页面中拼接后端 URL 常量；路径统一收敛在 `src/api/*.js`。
- 不要在代码中硬编码 `"/uhrds"` 等部署路径；统一通过环境变量或集中配置读取。

## 菜单裁剪联动规则

- 运行时菜单树与权限码以 `console-admin/src/main/resources/menu.yml` 为准；`docs/菜单项整理.csv` 若仍作为整理参考，内容也应与 `menu.yml` 保持一致。
- 删除目录或菜单时，要同步删除该菜单在前端页面中的入口、按钮、角色授权引用和 `v-hasPermi` 标识。
- 菜单裁剪后，前端路由与页面按钮必须同步清理已删除 `menuCode` 对应的权限标识，避免“页面有按钮但后端无权限点”的脏状态。
