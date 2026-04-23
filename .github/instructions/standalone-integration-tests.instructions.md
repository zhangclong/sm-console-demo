---
applyTo: "console-admin/src/test/shell/standalone-integration-test.sh"
---

# 单实例（Standalone）集成测试指南

## 1. 目的

该脚本用于在当前仓库的一套本地闭环环境中完成：

- 构建 `console-admin`（使用 `-Ptest` 开启 `/interLogin`）
- 进入 `apphome/bin`，通过 `console.sh start` 启动单实例后端
- 运行 `console-client` 的真实 HTTP E2E 测试
- 通过 `console.sh stop` 停止后端并收集 `apphome/logs/` 下日志

## 2. 位置与前提

- 脚本路径：`console-admin/src/test/shell/standalone-integration-test.sh`
- 客户端配置：`console-client/src/test/resources/console-config.yml`
- 后端默认健康检查地址：`http://localhost:8083/web-api/captchaImage`

依赖要求：

- JDK 11+
- Maven
- `bash`
- `curl`

## 3. 默认行为

不带参数执行时，默认跑完整流程：

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh
```

等价于：构建 → 启动 → 测试 → 停止。

## 4. 可选参数

- `--build`：仅构建 `console-admin`
- `--start`：启动 `console-admin`
- `--test`：运行 `console-client` 测试
- `--stop`：停止 `console-admin`
- `--scope all|writeOps|readOps`：控制 `console-client` 测试范围

## 5. 常用示例

### 5.1 全流程

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh
```

### 5.2 只构建并启动，保留环境用于手工调试

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh --build --start
```

### 5.3 在现有环境上仅跑只读测试

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh --test --scope readOps
```

### 5.4 在现有环境上仅跑写操作测试

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh --test --scope writeOps
```

### 5.5 手动清场

```bash
bash console-admin/src/test/shell/standalone-integration-test.sh --stop
```

## 6. 注意事项

1. 构建阶段必须使用 `-Ptest`，否则 `/interLogin` 不可用。
2. 构建阶段的 `-Ptest` 会通过 Maven 资源过滤把 `console.adminPasswordExpireDays` 替换为 `30`，确保首次初始化数据库时 admin 账号不过期。
3. 后端统一通过 `console-admin/apphome/bin/console.sh` 管理：`start` 后台启动、`stop` 停止、`run` 前台运行。
4. 失败排查优先查看 `console-admin/apphome/logs/` 目录下的实际日志文件，而不是依赖额外的手工重定向日志。
5. `console-client` 默认用 `admin / admin123` 登录；如果变更了测试配置，需要同步更新 `console-config.yml`。
6. 当前脚本面向**单实例**场景，不负责多节点或外部依赖环境。
7. 写操作测试会创建并删除测试用户/角色；若中途失败，可重新执行全流程以恢复干净环境。

