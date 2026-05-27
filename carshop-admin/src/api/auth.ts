import { apiClient, unwrap } from './client';

export interface AdminUser {
  id: number;
  username: string;
}

export const authApi = {
  login: (username: string, password: string) =>
    unwrap<AdminUser>(apiClient.post('/admin/login', { username, password })),
  logout: () => unwrap<null>(apiClient.post('/admin/logout')),
  me: () => unwrap<AdminUser>(apiClient.get('/admin/me')),
};
