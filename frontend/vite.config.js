import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080/projec1_Web_exploded',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080/projec1_Web_exploded',
        changeOrigin: true
      }
    }
  }
})
