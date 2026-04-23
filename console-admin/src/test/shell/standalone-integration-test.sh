#!/bin/bash
set -euo pipefail

DO_BUILD=false
DO_START=false
DO_TEST=false
DO_STOP=false
TEST_SCOPE="all"

if [ $# -eq 0 ]; then
  DO_BUILD=true
  DO_START=true
  DO_TEST=true
  DO_STOP=true
else
  while [ $# -gt 0 ]; do
    case "$1" in
      --build)
        DO_BUILD=true
        ;;
      --start)
        DO_START=true
        ;;
      --test)
        DO_TEST=true
        ;;
      --stop)
        DO_STOP=true
        ;;
      --scope)
        shift
        if [ $# -eq 0 ]; then
          echo "--scope requires one of: all, writeOps, readOps"
          exit 1
        fi
        TEST_SCOPE="$1"
        ;;
      *)
        echo "未知参数: $1"
        echo "支持参数: --build --start --test --stop --scope all|writeOps|readOps"
        exit 1
        ;;
    esac
    shift
  done
fi

case "$TEST_SCOPE" in
  all|writeOps|readOps)
    ;;
  *)
    echo "不支持的测试范围: $TEST_SCOPE"
    exit 1
    ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
WORK_DIR="$(cd "$SCRIPT_DIR/../../../../" >/dev/null 2>&1 && pwd)"
ADMIN_DIR="$WORK_DIR/console-admin"
CLIENT_DIR="$WORK_DIR/console-client"
APP_BIN_DIR="$ADMIN_DIR/apphome/bin"
APP_LOG_FILE="$ADMIN_DIR/apphome/logs/console-admin.jar.log"
HEALTH_URL="http://localhost:8083/web-api/captchaImage"
BUILD_COMMAND="mvn clean package -DskipTests=true -Ptest --no-transfer-progress"

cleanup_on_exit() {
  if [ "$DO_STOP" = true ] && [ -d "$APP_BIN_DIR" ]; then
    echo ">>> [cleanup] 停止后台服务..."
    cd "$APP_BIN_DIR"
    bash ./console.sh stop || true
  fi
}
trap cleanup_on_exit EXIT

wait_for_backend() {
  echo ">>> 等待后端就绪（最多 120 秒）..."
  for i in $(seq 1 24); do
    if curl -sf "$HEALTH_URL" >/dev/null 2>&1; then
      echo ">>> 后端已就绪。"
      return 0
    fi

    if [ -d "$APP_BIN_DIR" ]; then
      cd "$APP_BIN_DIR"
      if bash ./console.sh status | grep -q 'not running'; then
        echo "!!! 后端进程已退出。"
        tail -n 100 "$APP_LOG_FILE" 2>/dev/null || true
        return 1
      fi
    fi

    echo ">>> 第 $i/24 次检查未就绪，5 秒后重试..."
    sleep 5
  done

  echo "!!! 后端在规定时间内未启动完成。"
  tail -n 100 "$APP_LOG_FILE" 2>/dev/null || true
  return 1
}

run_client_tests() {
  local mvn_cmd=(mvn test -DskipTests=false -Dconsole.config.file=console-config.yml --no-transfer-progress)
  if [ "$TEST_SCOPE" = "writeOps" ]; then
    mvn_cmd+=("-Dgroups=writeOps")
  elif [ "$TEST_SCOPE" = "readOps" ]; then
    mvn_cmd+=("-Dgroups=readOps")
  fi

  echo ">>> 执行 console-client 测试，scope=$TEST_SCOPE"
  cd "$CLIENT_DIR"
  "${mvn_cmd[@]}"
}

if [ "$DO_BUILD" = true ]; then
  echo ">>> [1/4] 构建 console-admin（-Ptest）..."
  cd "$ADMIN_DIR"
  $BUILD_COMMAND
fi

if [ "$DO_START" = true ]; then
  echo ">>> [2/4] 启动 console-admin..."
  cd "$APP_BIN_DIR"
  bash ./console.sh start
  wait_for_backend
fi

if [ "$DO_TEST" = true ]; then
  echo ">>> [3/4] 运行 console-client E2E 测试..."
  run_client_tests
fi

if [ "$DO_STOP" = true ]; then
  echo ">>> [4/4] 停止 console-admin..."
  cd "$APP_BIN_DIR"
  bash ./console.sh stop
fi

echo ">>> 单实例集成测试流程结束。"

