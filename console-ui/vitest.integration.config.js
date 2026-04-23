import { defineConfig } from 'vitest/config';
import { fileURLToPath } from 'url';

/**
 * Vitest 配置，专用于前端 API 集成测试（真实 HTTP 请求，不使用 mock）。
 * 需要后端服务在 http://localhost:8083 运行（使用 -Ptest profile 启动）。
 */
export default defineConfig({
  test: {
    // 使用 node 环境，真实发 HTTP 请求（不需要 jsdom）
    environment: 'node',
    // 全局注入 describe/it/expect 等
    globals: true,
    // 仅匹配集成测试文件
    include: ['src/api/**/__integration_tests__/**/*.integration.test.js'],
    // 超时设置为 15 秒，等待后端响应
    testTimeout: 15000,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
