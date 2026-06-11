import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { showFailToast } from 'vant';
import { attachMockAdapter } from '@/mock';

export interface ApiResp<T = unknown> {
  code: number | string;
  message: string;
  data: T;
  traceId?: string;
}

const TOKEN_KEY = 'bitdance_token';

export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const getToken = () => localStorage.getItem(TOKEN_KEY) ?? '';
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

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

// 后端部分源码文件中文编码损坏,message 会显示乱码;按错误码兜底成可读提示
const ERROR_TEXT: Record<string, string> = {
  SMS_EXPIRED: '验证码已过期,请先点击"获取验证码"',
  SMS_INVALID: '验证码错误',
  SMS_COOLDOWN: '验证码发送太频繁,请 60 秒后再试',
  IAM_TOKEN_EXPIRED: '登录已过期,请重新登录',
  FORBIDDEN: '没有操作权限',
  INTERNAL_ERROR: '服务异常,请稍后再试'
};

const readableMessage = (body: ApiResp) => {
  const mapped = ERROR_TEXT[String(body.code)];
  if (mapped) return mapped;
  // 中文被错误解码后常出现这些替换字符/生僻字干扰,直接回退到通用提示
  const msg = body.message ?? '';
  return msg && !/[�锘鈥]/.test(msg) ? msg : `请求失败(${body.code})`;
};

request.interceptors.response.use(
  (response: AxiosResponse<ApiResp>) => {
    const body = response.data;
    if (body.code === 0 || body.code === 200 || body.code === 'SUCCESS') {
      return body.data as never;
    }
    showFailToast(readableMessage(body));
    return Promise.reject(body);
  },
  (error) => {
    showFailToast(error?.message || '网络异常');
    return Promise.reject(error);
  }
);

export default request;
