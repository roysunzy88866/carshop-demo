import React from 'react';
import ReactDOM from 'react-dom/client';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { carshopAntdTheme } from './theme/antdTheme';
import App from './App';

// MSW Mock 模式 · VITE_USE_MOCK=true 时启用,完整契约对齐(见 src/mocks/handlers.ts)。
// 真后端模式(默认 / VITE_USE_MOCK=false):
//   - 未设 VITE_API_BASE_URL → 走 vite proxy → http://localhost:8000(本地同源)
//   - 设了 VITE_API_BASE_URL → 直接打到该域(07 起用于真跨域 / 公网调试)
async function bootstrap() {
  if (import.meta.env.VITE_USE_MOCK === 'true') {
    const { worker } = await import('./mocks/browser');
    await worker.start({ onUnhandledRequest: 'bypass' });
    console.info('[carshop-admin] MSW Mock 模式已启用');
  } else {
    const target = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000 (via vite proxy)';
    console.info(`[carshop-admin] 真后端模式 · API → ${target}`);
  }

  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <ConfigProvider locale={zhCN} theme={carshopAntdTheme}>
        <App />
      </ConfigProvider>
    </React.StrictMode>,
  );
}

void bootstrap();
