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
  void import('./modules/auth');
  void import('./modules/studio');
  void import('./modules/course');
  void import('./modules/review');
  void import('./modules/practice');
  void import('./modules/growth');
  void import('./modules/message');
  void import('./modules/trial');
  void import('./modules/coach');
  void import('./modules/workshop');
  void import('./modules/community');
  void import('./modules/buddy');
  void import('./modules/coachOps');

  axios.interceptors.request.use(async (config) => {
    const method = (config.method ?? 'get').toLowerCase();
    const url = (config.url ?? '').replace(config.baseURL ?? '', '');
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
