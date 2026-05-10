import axios from 'axios';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

type Handler = (config: { url: string; method: string; data?: unknown; params?: unknown }) =>
  | Promise<unknown>
  | unknown;

const handlers: Array<{ method: string; pattern: RegExp; handler: Handler }> = [];

export function mock(method: string, pattern: RegExp, handler: Handler) {
  handlers.push({ method: method.toLowerCase(), pattern, handler });
}

if (USE_MOCK) {
  // 注册各业务模块的 mock 路由（按需在每次新增功能点时往这里挂）
  void import('./modules/auth');
  void import('./modules/studio');

  axios.interceptors.request.use(async (config) => {
    const method = (config.method ?? 'get').toLowerCase();
    const url = (config.url ?? '').replace(config.baseURL ?? '', '');
    const matched = handlers.find((h) => h.method === method && h.pattern.test(url));
    if (!matched) return config;
    const data =
      typeof config.data === 'string' ? safeJson(config.data) : config.data;
    const result = await matched.handler({ url, method, data, params: config.params });
    // 通过 adapter 短路返回
    config.adapter = async () =>
      ({
        data: { code: 0, message: 'ok', data: result, traceId: `mock-${Date.now()}` },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
        request: {}
      } as never);
    return config;
  });
}

function safeJson(s: string) {
  try {
    return JSON.parse(s);
  } catch {
    return s;
  }
}
