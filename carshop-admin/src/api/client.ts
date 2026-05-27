import axios, { AxiosError } from 'axios';

// 后端统一响应壳 · 跟 SPEC §12 一致
export interface Envelope<T> {
  code: number;
  data: T;
  message: string;
}

// 自定义业务异常 · 调用方按 code 判断业务结果(SPEC §12)
export class ApiError extends Error {
  code: number;
  httpStatus: number;
  constructor(code: number, message: string, httpStatus: number) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
  }
}

// 07 加:支持 VITE_API_BASE_URL 切目标(本地真跨域 / 部署到不同域名)
// - 未设 → 走相对路径 '/api/v1',由 vite proxy 转发到 localhost:8000(本地同源)
// - 设了 → 走完整 URL(如 https://carshop.hearagain.space/api/v1),真跨域,验证 CORS / cookie
const apiBase = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api/v1`
  : '/api/v1';

export const apiClient = axios.create({
  baseURL: apiBase,
  withCredentials: true,
  timeout: 15000,
});

// 401 / code=2000 全局跳登录(除了 /admin/me 自身,它就是用来探测登录态的)
let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(fn: () => void) {
  onUnauthorized = fn;
}

apiClient.interceptors.response.use(
  (resp) => {
    const env = resp.data as Envelope<unknown>;
    if (env && typeof env.code === 'number' && env.code !== 0) {
      // 业务错误 — 转 ApiError 让调用方走 catch
      throw new ApiError(env.code, env.message ?? 'error', resp.status);
    }
    return resp;
  },
  (err: AxiosError) => {
    const status = err.response?.status ?? 0;
    const body = err.response?.data as Envelope<unknown> | undefined;
    const code = body?.code ?? 9000;
    const message = body?.message ?? err.message ?? '网络错误';
    const apiErr = new ApiError(code, message, status);
    if (
      (code === 2000 || status === 401) &&
      !err.config?.url?.endsWith('/admin/me') &&
      onUnauthorized
    ) {
      onUnauthorized();
    }
    return Promise.reject(apiErr);
  },
);

// 拆 envelope 拿 data 的小工具
export async function unwrap<T>(p: Promise<{ data: Envelope<T> }>): Promise<T> {
  const resp = await p;
  return resp.data.data;
}

// 分页返回结构(SPEC §13)
export interface Page<T> {
  list: T[];
  total: number;
  page: number;
  page_size: number;
}
