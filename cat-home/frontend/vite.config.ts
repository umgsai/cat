import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  base: '/cat/assets/vue/',
  build: {
    outDir: '../src/main/webapp/assets/vue',
    emptyOutDir: true
  },
  server: {
    port: 5173,
    strictPort: false
  }
})
