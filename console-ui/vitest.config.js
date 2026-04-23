import { defineConfig } from 'vitest/config';
import path from 'path';
import { fileURLToPath } from 'url';

/**
 * Vitest 配置，专用于 console-ui 单元测试。
 * 与 vite.config.mjs 独立，不依赖 VUE_APP_CONTEXT_PATH 等运行时环境变量。
 */
export default defineConfig({
  test: {
    // 使用 jsdom 环境模拟浏览器 API（如 window/document）
    environment: 'jsdom',
    // 全局注入 describe/it/expect 等，无需每个测试文件单独 import
    globals: true,
    // 测试文件匹配规则：src/ 下的 __tests__ 目录或 .test.js 文件
    include: ['src/**/__tests__/**/*.{js,ts}', 'src/**/*.{test,spec}.{js,ts}'],
    // 排除集成测试目录（集成测试使用独立的 vitest.integration.config.js）
    exclude: ['src/**/__integration_tests__/**'],
    // 覆盖率报告（可选）
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['src/api/**/*.js'],
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
