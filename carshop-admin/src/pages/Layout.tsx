import { Layout, Menu, Dropdown, Avatar, Spin, App } from 'antd';
import { Outlet, useNavigate, useLocation, Navigate } from 'react-router-dom';
import {
  AppstoreOutlined,
  ShoppingOutlined,
  PictureOutlined,
  OrderedListOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useAuth } from '../auth/AuthContext';
import { colors } from '../theme/tokens';

const { Header, Sider, Content } = Layout;

const MENU = [
  { key: '/categories', icon: <AppstoreOutlined />, label: '分类管理' },
  { key: '/products', icon: <ShoppingOutlined />, label: '商品管理' },
  { key: '/banners', icon: <PictureOutlined />, label: 'Banner 管理' },
  { key: '/orders', icon: <OrderedListOutlined />, label: '订单查看' },
];

export default function AppLayout() {
  const { user, loading, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { message } = App.useApp();

  if (loading) {
    return (
      <Layout style={{ minHeight: '100vh', justifyContent: 'center', alignItems: 'center' }}>
        <Spin size="large" />
      </Layout>
    );
  }
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const selectedKey = MENU.find((m) => location.pathname.startsWith(m.key))?.key ?? '/products';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        width={220}
        theme="light"
        style={{
          borderRight: `1px solid ${colors.outline}`,
          background: colors.surface,
        }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: colors.primary,
            fontWeight: 700,
            fontSize: 16,
            borderBottom: `1px solid ${colors.outline}`,
          }}
        >
          车机商店 · 后台
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          onClick={(e) => navigate(e.key)}
          items={MENU}
          style={{ borderInlineEnd: 'none', paddingTop: 8 }}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: colors.surface,
            padding: '0 24px',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
            borderBottom: `1px solid ${colors.outline}`,
          }}
        >
          <Dropdown
            menu={{
              items: [
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: async () => {
                    await logout();
                    message.success('已退出');
                    navigate('/login', { replace: true });
                  },
                },
              ],
            }}
          >
            <span style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 8 }}>
              <Avatar size="small" icon={<UserOutlined />} />
              <span>{user.username}</span>
            </span>
          </Dropdown>
        </Header>
        <Content style={{ padding: 24, background: colors.background }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
