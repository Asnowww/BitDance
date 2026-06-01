import request from '@/utils/request';

export interface StudioCard {
  id: number;
  name: string;
  address: string;
  cityId: number;
  businessDistrictId: number;
  coverAssetId?: number;
  distanceKm: number;
  latitude?: number;
  longitude?: number;
  favored: boolean;
}

export interface StudioListResp {
  list: StudioCard[];
  page: number;
  pageSize: number;
  total: number;
}

export interface StudioListQuery {
  cityId?: number;
  page?: number;
  pageSize?: number;
  danceStyleId?: number;
  distanceKm?: number;
  keyword?: string;
  latitude?: number;
  longitude?: number;
}

export const fetchNearbyStudios = (params: StudioListQuery) =>
  // Backend exposes studio discovery under /public; using the public path keeps real-server fallback from hitting auth-only routes.
  request.get<unknown, StudioListResp>('/public/studios/nearby', { params });

export interface StudioDetail extends StudioCard {
  brandName?: string;
  intro: string;
  transportInfo?: string;
  contactPhone?: string;
  address: string;
  claimStatus?: string;
  danceStyleIds: number[];
}

export const fetchStudioDetail = (id: number) =>
  request.get<unknown, StudioDetail>(`/public/studios/${id}`);
