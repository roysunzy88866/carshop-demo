import { Card, Form, Input, Button, Typography, App, Layout } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { authApi } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';
import { colors } from '../theme/tokens';

const { Title, Paragraph } = Typography;

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { refresh } = useAuth();
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);

  const from = (location.state as any)?.from?.pathname ?? '/products';

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      await authApi.login(values.username, values.password);
      await refresh();
      message.success('登录成功');
      navigate(from, { replace: true });
    } catch (e) {
      const err = e instanceof ApiError ? e : new Error('登录失败');
      message.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: colors.background, justifyContent: 'center', alignItems: 'center' }}>
      <Card style={{ width: 420 }} bordered>
        <Title level={3} style={{ marginTop: 0, marginBottom: 4 }}>
          车机商店 · 运营后台
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 24 }}>
          请使用运营账号登录
        </Paragraph>
        <Form
          layout="vertical"
          initialValues={{ username: 'admin', password: 'admin123' }}
          onFinish={onFinish}
          autoComplete="off"
          data-testid="login-form"
        >
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="admin" autoFocus />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="admin123" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </Layout>
  );
}
