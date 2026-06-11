type TencentMapApi = Record<string, any>;

declare global {
  interface Window {
    TMap?: TencentMapApi;
  }
}

let loadingPromise: Promise<TencentMapApi> | null = null;

export const hasTencentMapConfig = () => Boolean(import.meta.env.VITE_TENCENT_MAP_KEY);

export const getTencentMapReferer = () => import.meta.env.VITE_TENCENT_MAP_REFERER || 'BitDance';

export const loadTencentMap = async () => {
  if (window.TMap) return window.TMap;
  if (!hasTencentMapConfig()) {
    throw new Error('VITE_TENCENT_MAP_KEY is required');
  }
  if (loadingPromise) return loadingPromise;

  loadingPromise = new Promise<TencentMapApi>((resolve, reject) => {
    const existing = document.getElementById('bitdance-tencent-map-jsapi') as HTMLScriptElement | null;
    if (existing) {
      existing.addEventListener('load', () => (window.TMap ? resolve(window.TMap) : reject(new Error('TMap missing'))), {
        once: true
      });
      existing.addEventListener('error', () => reject(new Error('Tencent map script failed')), { once: true });
      return;
    }

    const script = document.createElement('script');
    script.id = 'bitdance-tencent-map-jsapi';
    script.async = true;
    // M1 腾讯地图 JSAPI：Key 只从本地 Vite 环境变量读取，避免把地图密钥写入仓库。
    script.src = `https://map.qq.com/api/gljs?v=1.exp&libraries=service&key=${encodeURIComponent(import.meta.env.VITE_TENCENT_MAP_KEY ?? '')}`;
    script.onload = () => (window.TMap ? resolve(window.TMap) : reject(new Error('TMap missing')));
    script.onerror = () => reject(new Error('Tencent map script failed'));
    document.head.appendChild(script);
  }).finally(() => {
    loadingPromise = null;
  });

  return loadingPromise;
};

export const buildTencentMarkerUrl = (
  latitude: number | string,
  longitude: number | string,
  title: string,
  address?: string
) => {
  const marker = [`coord:${latitude},${longitude}`, `title:${title}`, address ? `addr:${address}` : '']
    .filter(Boolean)
    .join(';');
  // M1 导航联动：腾讯 URI marker 使用纬度、经度顺序，referer 走环境变量，便于正式 Key 做白名单限制。
  return `https://apis.map.qq.com/uri/v1/marker?marker=${encodeURIComponent(marker)}&referer=${encodeURIComponent(getTencentMapReferer())}`;
};

export const buildTencentSearchUrl = (keyword: string) =>
  // M1 地址兜底：舞室缺少经纬度时跳到腾讯地图搜索页，保留用户可达的导航路径。
  `https://apis.map.qq.com/uri/v1/search?keyword=${encodeURIComponent(keyword)}&referer=${encodeURIComponent(getTencentMapReferer())}`;
