import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';

export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

type Handler = (config: { url: string; method: string; data?: unknown; params?: unknown }) =>
  | Promise<unknown>
  | unknown;

const handlers: Array<{ method: string; pattern: RegExp; handler: Handler }> = [];

export function mock(method: string, pattern: RegExp, handler: Handler) {
  handlers.push({ method: method.toLowerCase(), pattern, handler });
}

function safeJson(s: string) {
  try {
    return JSON.parse(s);
  } catch {
    return s;
  }
}

function resolveUrl(config: InternalAxiosRequestConfig): string {
  const raw = config.url ?? '';
  if (raw.startsWith('http')) {
    try {
      return new URL(raw).pathname;
    } catch {
      return raw;
    }
  }
  return raw;
}

const mockRequestInterceptor = async (config: InternalAxiosRequestConfig) => {
  const method = (config.method ?? 'get').toLowerCase();
  const url = resolveUrl(config);
  const matched = handlers.find((h) => h.method === method && h.pattern.test(url));
  if (!matched) return config;
  const data = typeof config.data === 'string' ? safeJson(config.data) : config.data;
  const result = await matched.handler({ url, method, data, params: config.params });
  config.adapter = async () =>
    ({
      data: { code: 0, message: 'ok', data: result, traceId: `mock-${Date.now()}` },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {}
    }) as never;
  return config;
};

/** 将 mock 挂到实际发请求的 axios 实例（request.ts 里 create 的那个） */
export function attachMockAdapter(instance: AxiosInstance) {
  if (!USE_MOCK) return;
  instance.interceptors.request.use(mockRequestInterceptor);
}

if (USE_MOCK) {
  import('./modules/auth');
  import('./modules/studio');
  import('./modules/course');
  import('./modules/review');
  import('./modules/practice');
  import('./modules/growth');
  import('./modules/message');
  import('./modules/trial');
  import('./modules/coach');
  import('./modules/workshop');
  import('./modules/community');
  import('./modules/maps');
  import('./modules/buddy');
  import('./modules/coachOps');
  import('./modules/favorite');
}
