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
  request.get<unknown, MapGeocodeResp>('/h5/maps/tencent/geocode', { params: { address } }).then((data) => {
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
      };
    });

export const locateTencentByIp = () =>
  request.get<unknown, MapGeocodeResp>('/h5/maps/tencent/ip-location').then((data) => {
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
      district: data.district
    };
  });
