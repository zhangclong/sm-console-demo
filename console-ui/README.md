# 管理控制台-前端工程

## Maven集成说明
本项目已集成到 Maven 构建生命周期中。
在项目根目录或本目录下执行 `mvn clean compile` 时，插件会自动执行：
1. 下载并安装 Node.js（要求 18+，实际版本以 `pom.xml` 中插件配置为准）到 `node/` 目录（隔离环境，maven插件自动创建）。
2. 执行 `npm install --registry=https://registry.npmmirror.com --legacy-peer-deps` 安装依赖。
3. 执行 `npm run build:prod` 构建生产环境代码。
4. 将 `dist/` 目录下的静态资源自动复制到 `../console-admin/src/main/resources/public`。

注意：Maven 插件使用的 Node 环境与系统环境隔离。

## 开发调试

如果您需要进行前端代码开发调试（热重载），请确保您的系统安装了 Node.js 18+（建议使用 LTS 版本，配合 nvm 使用）。

```bash
# 安装依赖（建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm install 下载速度慢的问题）
npm install --registry=https://registry.npmmirror.com --legacy-peer-deps

# 启动开发调试服务
npm run dev
```
浏览器访问 http://localhost:80/

## 主要环境变量定义和修改
公共的环境变量在 .env 文件中修改
```bash
# 页面标题
VUE_APP_TITLE = 管理控制台
# 应用路径, 必须以 / 开头和结尾
VUE_APP_CONTEXT_PATH = '/'
# 后台API路径
VUE_APP_BASE_API = "${VUE_APP_CONTEXT_PATH}web-api"
```

## 发布

```bash
# 构建企业版生产环境, 构建结果或输出到 dist 目录
npm run build:prod
# 拷贝 dist 目录下的静态资源到 ../console-admin/src/main/resources/public 目录
cp -r dist/* ../console-admin/src/main/resources/public/
```



## 环境变量约定(.env)
  - `VUE_APP_CONTEXT_PATH` 必须以 `/` 开头和结尾（如 `/`、`/uhrds/`）。
  - `VUE_APP_BASE_API` 约定为 `${VUE_APP_CONTEXT_PATH}web-api`。
  - 本地调试建议 `VUE_APP_CONTEXT_PATH='/'`，发布时按后端 `server.context-path` 对齐。 比如：后端 `server.context-path=/uhrds`，则前端 `VUE_APP_CONTEXT_PATH='/uhrds/'`。


