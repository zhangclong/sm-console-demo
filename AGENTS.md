# AGENTS Guide for sm-console
## Scope and instruction precedence
- Follow `.github/copilot-instructions.md` first, then task-specific files in `.github/instructions/*.instructions.md`.
- This repo is currently single-tenant runtime; implement changes for single-tenant behavior but avoid blocking future tenant isolation (`docs/multitenancy/tenant.instructions.md`).
- Keep changes minimal and local; do not refactor unrelated modules.
## Big picture architecture (what talks to what)
- Root Maven aggregator (`pom.xml`) has 3 active modules: `console-ui` (Vue frontend), `console-client` (standalone HTTP integration/E2E client), and `console-admin` (Spring MVC + MyBatis backend).
- `console-ui` builds static assets into `console-admin/src/main/resources/public/` via Maven (`console-ui/pom.xml`).
- Backend entrypoint is `console-admin/src/main/java/com/uh/ConsoleApplication.java`; runtime working directory is `console-admin/apphome`.
- Typical request flow: Vue page -> API wrapper -> backend controller -> service -> mapper XML SQL.
- Menu/permission runtime chain: `console-ui/src/api/system/menuTree.js` -> `/system/menu/**` -> `console-admin/src/main/java/com/uh/system/web/SystemMenuController.java` -> `SystemMenuService` -> `MenuRegistry` -> `console-admin/src/main/resources/menu.yml`.
- Dict runtime chain: `console-ui/src/api/system/dict/data.js` -> `/system/dict/data/type/{dictType}` -> `console-admin/src/main/java/com/uh/system/web/SysDictDataController.java` -> `DictRegistry` -> enum classes under `com.uh.system.dict` / `com.uh.console.dict`.
## Backend conventions you must match
- Layering is strict: Controller (HTTP + permission) / Service (business + transactions) / Mapper interface + XML (SQL).
- Controllers use GET/POST only, `@PrePermission` for non-public APIs, and `@Log` on write operations.
- Use `BaseController.startPage()` + `getDataTable(...)` for paged lists and `toAjax(...)` for write responses (`console-admin/src/main/java/com/uh/common/core/controller/BaseController.java`).
- Keep SQL in `console-admin/src/main/resources/mapper/{system,console}/*Mapper.xml`; avoid SQL string building in Java.
- For date-range filters, pass `params.begin*/end*` end-to-end and validate in Service with `DateUtils.checkDateFormat(...)` before mapper `${}` usage.
## Frontend conventions you must match
- All HTTP calls go through `console-ui/src/utils/request.js`; do not call axios directly from views.
- API wrappers live in `console-ui/src/api/**/*.js`; views import these wrappers from `console-ui/src/views/**/*.vue`.
- Permission points must align in three runtime places: backend `@PrePermission`, frontend `v-hasPermi`, and `console-admin/src/main/resources/menu.yml` `menuCode`.
- Role authorization now submits `menuCodes` from `console-ui/src/views/system/role/index.vue`, and backend persists them in `sys_role_menu.menu_code`.
- List pages consume `response.rows/response.total`; non-list endpoints consume `response.data`.
- Date filters must use `addDateRange(queryParams, dateRange)` (see usages in `console-ui/src/views/system/user/index.vue`).
## Build, test, and run workflows (project-specific)
- Frontend build in Maven lifecycle:
  - `cd console-ui && mvn clean compile` (installs isolated Node, runs `npm install`, runs `build:prod`, copies `dist` to backend public dir).
- Backend compile/package:
  - `cd console-admin && mvn clean compile`
  - `cd console-admin && mvn clean package -DskipTests=true`
- Backend tests (Surefire uses `apphome` as working dir):
  - `cd console-admin && mvn test -DskipTests=false`
- Frontend tests:
  - Contract tests: `cd console-ui && npm run test`
  - Integration tests: start backend with `-Ptest` (enables `/interLogin`), then `cd console-ui && npm run test:integration`.
- Standalone HTTP integration tests: `bash console-admin/src/test/shell/standalone-integration-test.sh` (builds `console-admin`, starts via `apphome/bin/console.sh`, then runs `console-client` tests).
## Data and config integration points
- SQL initialization scripts are in `console-admin/sql/`; current first-start initialization uses `1.commons.sql`.
- Runtime menu structure comes from `console-admin/src/main/resources/menu.yml`; do not treat database menu data as the runtime source of truth.
- Runtime dict data comes from `DictRegistry` + enum classes; do not add new runtime dictionary records in `sys_dict_type` / `sys_dict_data`.
- `console-admin` no longer prebuilds the database during `mvn compile`; on first application startup the runtime `MySqlDatabaseInitializer` detects whether the `sys_user` table exists in the configured MySQL database and, if not, executes `sql/1.commons.sql`.
- Runtime-overridable config belongs in `console-admin/src/main/run-resources/config/application.yml`; packaged defaults are in `console-admin/src/main/resources/application*.yml`.
- Frontend env contract: `VUE_APP_CONTEXT_PATH` must start/end with `/`, and `VUE_APP_BASE_API` should be `${VUE_APP_CONTEXT_PATH}web-api`.
## Guardrails for AI changes
- Do not introduce `spring-boot-*` dependencies (this project uses Spring Framework, not Spring Boot).
- Keep permission/security behavior explicit; do not add silent fallbacks for validation or auth failures.
- If changing menus or permissions, update backend annotations, frontend `v-hasPermi`, and `menu.yml` together.
- If changing dictionaries, update enum definitions / `@DictType` usage and verify `DictRegistry` scanning instead of editing runtime dictionary tables.
- Validate with the smallest relevant command set before finishing (module-level build/test first, then broader checks only if needed).
