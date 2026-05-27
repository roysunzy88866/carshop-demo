// 06 admin-web · 真正的应用入口(00 的 demo 已废弃)
import { RouterProvider } from 'react-router-dom';
import { App as AntApp } from 'antd';
import { router } from './router';
import { AuthProvider } from './auth/AuthContext';

// AntApp 包一层 → 全局可用 useApp().message / modal / notification
export default function App() {
  return (
    <AntApp>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </AntApp>
  );
}
