import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import path from 'path'
import { fileURLToPath } from 'url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VUE_APP_')

  // Validate required environment variables
  const contextPath = env.VUE_APP_CONTEXT_PATH;
  if (!contextPath) {
    throw new Error('[env] VUE_APP_CONTEXT_PATH is required, e.g. /uhrds/');
  }
  if (!contextPath.startsWith('/')) {
    throw new Error(`[env] VUE_APP_CONTEXT_PATH must start with "/": ${contextPath}`);
  }
  if (!contextPath.endsWith('/')) {
    throw new Error(`[env] VUE_APP_CONTEXT_PATH must end with "/": ${contextPath}`);
  }

  return {
    base: env.VUE_APP_CONTEXT_PATH,
    plugins: [
      vue(),
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        dts: false,
      }),
      Components({
        dirs: ['src/components'],
        dts: false,
        extensions: ['vue'],
      }),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
        symbolId: 'icon-[name]',
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
      extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    },
    envPrefix: 'VUE_APP_',
    css: {
      preprocessorOptions: {
        scss: {
          silenceDeprecations: ['legacy-js-api'],
        },
      },
    },
    server: {
      host: '0.0.0.0',
      port: 80,
      proxy: {
        [env.VUE_APP_BASE_API]: {
          target: 'http://localhost:8083',
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: 'dist',
      assetsDir: 'static',
      sourcemap: false,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
          manualChunks(id) {
            if (id.includes('node_modules')) {
              if (id.includes('element-plus')) {
                return 'chunk-element-plus'
              }
              return 'chunk-vendors'
            }
          },
        },
      },
    },
    optimizeDeps: {
      include: ['element-plus', '@element-plus/icons-vue'],
    },
  }
})

