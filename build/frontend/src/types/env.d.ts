/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE: string;
  readonly VITE_USE_MOCK: string;
  // M1 腾讯地图：浏览器端只读取 JSAPI Key，真实值放本地 .env，不提交仓库。
  readonly VITE_TENCENT_MAP_KEY?: string;
  readonly VITE_TENCENT_MAP_REFERER?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
