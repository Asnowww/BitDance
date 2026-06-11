import request from '@/utils/request';

export interface MapPlace {
  id?: string;
  title: string;
  address?: string;
  category?: string;
  latitude: number;
  longitude: number;
  tel?: string;
  adcode?: string;
}

interface MapPlaceResp {
  id?: string;
  title?: string;
  address?: string;
  category?: string;
  latitude?: number | string | null;
  longitude?: number | string | null;
  tel?: string;
  adcode?: string;
}

interface MapPlaceListResp {
  list?: MapPlaceResp[];
  page?: number;
  pageSize?: number;
  total?: number | null;
}

interface MapGeocodeResp {
  title?: string;
  address?: string;
  latitude?: number | string | null;
  longitude?: number | string | null;
  adcode?: string;
  province?: string;
  city?: string;
  district?: string;
  pois?: MapPlaceResp[];
}

export interface MapGeocodeResult {
  title: string;
  address: string;
  latitude: number;
  longitude: number;
  adcode?: string;
  province?: string;
  city?: string;
  district?: string;
  pois: MapPlace[];
}

const DEFAULT_TENCENT_LOCATION: MapGeocodeResult = {
  title: '中关村',
  address: '北京市海淀区中关村大街',
  latitude: 39.98412,
  longitude: 116.307484,
  adcode: '110108',
  province: '北京市',
  city: '北京市',
  district: '海淀区',
  pois: []
};

const FALLBACK_PLACES: MapPlace[] = [
  { id: 'zgc-plaza', title: '中关村广场', address: '北京市海淀区中关村大街15号', category: '商场', latitude: 39.98382, longitude: 116.30702, adcode: '110108' },
  { id: 'zgc-soho', title: '中关村SOHO', address: '北京市海淀区海淀北二街8号', category: '商务楼宇', latitude: 39.98347, longitude: 116.31261, adcode: '110108' },
  { id: 'peking-university', title: '北京大学', address: '北京市海淀区颐和园路5号', category: '学校', latitude: 39.9928, longitude: 116.30547, adcode: '110108' },
  { id: 'tsinghua-university', title: '清华大学', address: '北京市海淀区双清路30号', category: '学校', latitude: 40.00311, longitude: 116.3269, adcode: '110108' },
  { id: 'haidian-huangzhuang', title: '海淀黄庄', address: '北京市海淀区中关村大街与北四环西路交叉口', category: '地铁站', latitude: 39.98311, longitude: 116.31608, adcode: '110108' },
  { id: 'renmin-university', title: '中国人民大学', address: '北京市海淀区中关村大街59号', category: '学校', latitude: 39.96852, longitude: 116.31734, adcode: '110108' },
  { id: 'zgc-capital', title: '中关村创业大街', address: '北京市海淀区海淀西大街48号', category: '街区', latitude: 39.98616, longitude: 116.30946, adcode: '110108' },
  { id: 'haidian-park', title: '海淀公园', address: '北京市海淀区新建宫门路2号', category: '公园', latitude: 39.99991, longitude: 116.29831, adcode: '110108' },
  { id: 'new-oriental', title: '新东方大厦', address: '北京市海淀区海淀中街6号', category: '商务楼宇', latitude: 39.98247, longitude: 116.30935, adcode: '110108' },
  { id: 'shuangan', title: '双安商场', address: '北京市海淀区北三环西路38号', category: '商场', latitude: 39.97277, longitude: 116.31759, adcode: '110108' }
];

const silentMapRequest = { silentErrorToast: true } as const;

const toPlace = (item: MapPlaceResp): MapPlace | null => {
  const latitude = Number(item.latitude);
  const longitude = Number(item.longitude);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return null;
  return {
    id: item.id,
    title: item.title || item.address || '地图位置',
    address: item.address,
    category: item.category,
    latitude,
    longitude,
    tel: item.tel,
    adcode: item.adcode
  };
};

const distance = (aLat: number, aLng: number, bLat: number, bLng: number) =>
  Math.hypot(aLat - bLat, aLng - bLng);

const includesKeyword = (place: MapPlace, keyword?: string) => {
  const text = keyword?.trim().toLowerCase();
  if (!text) return true;
  return `${place.title}${place.address ?? ''}${place.category ?? ''}`.toLowerCase().includes(text);
};

export const getTencentMapDefaultLocation = () => ({
  ...DEFAULT_TENCENT_LOCATION,
  pois: FALLBACK_PLACES.slice(0, 8)
});

export const searchFallbackTencentPlaces = (q?: {
  keyword?: string;
  latitude?: number;
  longitude?: number;
  pageSize?: number;
}) => {
  const latitude = Number.isFinite(q?.latitude) ? Number(q?.latitude) : DEFAULT_TENCENT_LOCATION.latitude;
  const longitude = Number.isFinite(q?.longitude) ? Number(q?.longitude) : DEFAULT_TENCENT_LOCATION.longitude;
  const size = Math.max(1, Math.min(q?.pageSize ?? 12, 20));
  return FALLBACK_PLACES
    .filter((place) => includesKeyword(place, q?.keyword))
    .slice()
    .sort((a, b) => distance(latitude, longitude, a.latitude, a.longitude) - distance(latitude, longitude, b.latitude, b.longitude))
    .slice(0, size);
};

export const buildFallbackMapGeocodeResult = (latitude?: number, longitude?: number): MapGeocodeResult => {
  const lat = Number.isFinite(latitude) ? Number(latitude) : DEFAULT_TENCENT_LOCATION.latitude;
  const lng = Number.isFinite(longitude) ? Number(longitude) : DEFAULT_TENCENT_LOCATION.longitude;
  const nearest = searchFallbackTencentPlaces({ latitude: lat, longitude: lng, pageSize: 1 })[0] ?? FALLBACK_PLACES[0];
  return {
    ...DEFAULT_TENCENT_LOCATION,
    title: nearest?.title || DEFAULT_TENCENT_LOCATION.title,
    address: nearest?.address || DEFAULT_TENCENT_LOCATION.address,
    latitude: lat,
    longitude: lng,
    pois: searchFallbackTencentPlaces({ latitude: lat, longitude: lng, pageSize: 12 })
  };
};

export const searchTencentPlaces = (q: {
  keyword: string;
  city?: string;
  latitude?: number;
  longitude?: number;
  radiusMeters?: number;
  page?: number;
  pageSize?: number;
}) =>
  request
    .get<unknown, MapPlaceListResp>('/h5/maps/tencent/places', {
      ...silentMapRequest,
      params: {
        keyword: q.keyword,
        city: q.city ?? '北京',
        latitude: q.latitude,
        longitude: q.longitude,
        radiusMeters: q.radiusMeters ?? 3000,
        page: q.page ?? 1,
        pageSize: q.pageSize ?? 10
      }
    })
    .then((data) => (data.list ?? []).map(toPlace).filter(Boolean) as MapPlace[]);

export const geocodeTencentAddress = (address: string) =>
  request.get<unknown, MapGeocodeResp>('/h5/maps/tencent/geocode', { ...silentMapRequest, params: { address } }).then((data) => {
    const latitude = Number(data.latitude);
    const longitude = Number(data.longitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      throw new Error('地图未返回有效坐标');
    }
    return {
      title: data.title || address,
      address: data.address || address,
      latitude,
      longitude,
      adcode: data.adcode,
      province: data.province,
      city: data.city,
      district: data.district
    };
  });

export const reverseGeocodeTencentLocation = (latitude: number, longitude: number) =>
  request
    .get<unknown, MapGeocodeResp>('/h5/maps/tencent/reverse-geocode', {
      ...silentMapRequest,
      params: { latitude, longitude }
    })
    .then((data) => {
      const lat = Number(data.latitude ?? latitude);
      const lng = Number(data.longitude ?? longitude);
      if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        throw new Error('地图未返回有效坐标');
      }
      return {
        title: data.title || data.address || '当前位置',
        address: data.address || data.title || '当前位置',
        latitude: lat,
        longitude: lng,
        adcode: data.adcode,
        province: data.province,
        city: data.city,
        district: data.district,
        pois: (data.pois ?? []).map(toPlace).filter(Boolean) as MapPlace[]
      } as MapGeocodeResult;
    });

export const locateTencentByIp = () =>
  request.get<unknown, MapGeocodeResp>('/h5/maps/tencent/ip-location', silentMapRequest as never)
    .then((data) => {
      const latitude = Number(data.latitude);
      const longitude = Number(data.longitude);
      if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
        throw new Error('地图未返回有效坐标');
      }
      return {
        title: data.title || data.address || '当前位置',
        address: data.address || data.title || '当前位置',
        latitude,
        longitude,
        adcode: data.adcode,
        province: data.province,
        city: data.city,
        district: data.district,
        pois: []
      } as MapGeocodeResult;
    })
    .catch(() => getTencentMapDefaultLocation());
