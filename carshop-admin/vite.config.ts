import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 06 admin-web · dev 走 vite proxy 到 localhost:8000(真后端模式)。
// VITE_USE_MOCK=true 时 main.tsx 启 MSW Service Worker 拦截 /api/*,
// proxy 永远配着不影响——MSW 拦在更早的层。
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: false,
      },
    },
  },
});
