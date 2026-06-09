import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { showFailToast } from 'vant';
import { attachMockAdapter } from '@/mock';

declare module 'axios' {
  export interface AxiosRequestConfig {
    silentError?: boolean;
  }

  export interface InternalAxiosRequestConfig {
    silentError?: boolean;
  }
}

export interface ApiResp<T = unknown> {
  code: number | string;
  message: string;
  data: T;
  traceId?: string;
}

const TOKEN_KEY = 'bitdance_token';
const PROFILE_KEY = 'bitdance_profile';

export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const getToken = () => localStorage.getItem(TOKEN_KEY) ?? '';
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

const redirectToLoginWhenAuthExpired = () => {
  if (!getToken() || window.location.hash.startsWith('#/login')) return false;
  clearToken();
  localStorage.removeItem(PROFILE_KEY);
  const redirect = window.location.hash.replace(/^#/, '') || '/home';
  // 登录态失效兜底：旧 JWT 会让路由误以为已登录，这里清理后带 redirect 回到登录页。
  window.location.hash = `#/login?redirect=${encodeURIComponent(redirect)}`;
  return true;
};

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 15000
});

attachMockAdapter(request);

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

request.interceptors.response.use(
  (response: AxiosResponse<ApiResp>) => {
    const body = response.data;
    if (body.code === 0 || body.code === 200 || body.code === 'SUCCESS') {
      return body.data as never;
    }
    // M1/M2 可选链路：收藏等登录态接口允许页面自行降级时，不弹全局错误遮挡主流程。
    if (!response.config.silentError) showFailToast(body.message || '请求失败');
    return Promise.reject(body);
  },
  (error) => {
    const serverMessage = error?.response?.data?.message;
    const status = error?.response?.status;
    if ((status === 401 || status === 403) && redirectToLoginWhenAuthExpired()) {
      showFailToast('登录已失效，请重新登录');
      return Promise.reject(error);
    }
    if (!error?.config?.silentError) showFailToast(serverMessage || error?.message || '网络异常');
    return Promise.reject(error);
  }
);

export default request;
