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
const PASSWORD_REQUIRED_KEY = 'bitdance_password_required';

export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const getToken = () => localStorage.getItem(TOKEN_KEY) ?? '';
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);
export const setPasswordRequired = (required: boolean) => {
  if (required) {
    localStorage.setItem(PASSWORD_REQUIRED_KEY, 'true');
    return;
  }
  localStorage.removeItem(PASSWORD_REQUIRED_KEY);
};
export const isPasswordRequired = () => localStorage.getItem(PASSWORD_REQUIRED_KEY) === 'true';

const redirectToLoginWhenAuthExpired = () => {
  if (!getToken() || window.location.hash.startsWith('#/login')) return false;
  clearToken();
  localStorage.removeItem(PROFILE_KEY);
  setPasswordRequired(false);
  const redirect = window.location.hash.replace(/^#/, '') || '/home';
  window.location.hash = `#/login?redirect=${encodeURIComponent(redirect)}`;
  return true;
};

const redirectToPasswordSetup = () => {
  if (!getToken()) return false;
  setPasswordRequired(true);
  if (window.location.hash.startsWith('#/login')) return false;
  const redirect = window.location.hash.replace(/^#/, '') || '/home';
  window.location.hash = `#/login?setupPassword=1&redirect=${encodeURIComponent(redirect)}`;
  return true;
};

type RequestConfigWithSilentError = InternalAxiosRequestConfig & {
  silentErrorToast?: boolean;
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
    if (body.code === 'PASSWORD_REQUIRED') {
      redirectToPasswordSetup();
    }
    if (!response.config.silentError) showFailToast(body.message || '\u8bf7\u6c42\u5931\u8d25');
    return Promise.reject(body);
  },
  (error) => {
    const serverMessage = error?.response?.data?.message;
    const serverCode = error?.response?.data?.code;
    const status = error?.response?.status;
    if (serverCode === 'PASSWORD_REQUIRED') {
      redirectToPasswordSetup();
      if (!error?.config?.silentError) showFailToast(serverMessage || '\u8bf7\u5148\u8bbe\u7f6e\u767b\u5f55\u5bc6\u7801');
      return Promise.reject(error);
    }
    if ((status === 401 || status === 403) && redirectToLoginWhenAuthExpired()) {
      showFailToast('\u767b\u5f55\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55');
      return Promise.reject(error);
    }
    if (!error?.config?.silentError) showFailToast(serverMessage || error?.message || '\u7f51\u7edc\u5f02\u5e38');
    return Promise.reject(error);
  }
);

export default request;
