import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    port: 8080,
    host: 'localhost',  // 指定主机
    proxy: {
      '/api': {
        target: 'http://localhost:8088',  // 你的后端地址
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8088',  // 后端图片上传目录
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
