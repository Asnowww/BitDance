import request from '@/utils/request';

export interface StudioCard {
  id: number;
  name: string;
  cover: string;
  city: string;
  area: string;
  distanceKm: number;
  ratingAvg: number;
  reviewCount: number;
  topStyles: string[];
  beginnerFriendly: boolean;
}

export interface StudioListResp {
  list: StudioCard[];
  page: number;
  pageSize: number;
  total: number;
}

export interface StudioListQuery {
  city?: string;
  page?: number;
  pageSize?: number;
  styles?: string[];
  priceMin?: number;
  priceMax?: number;
  distanceKm?: number;
  difficulty?: string;
  audience?: string;
  beginnerFriendly?: boolean;
  keyword?: string;
  latitude?: number;
  longitude?: number;
}

export const fetchNearbyStudios = (params: StudioListQuery) =>
  // Backend exposes studio discovery under /public; using the public path keeps real-server fallback from hitting auth-only routes.
  request.get<unknown, StudioListResp>('/public/studios/nearby', { params });

export interface StudioDetail extends StudioCard {
  intro: string;
  address: string;
  openHours: string;
  photos: string[];
  courses: Array<{ id: number; name: string; style: string; difficulty: string; price: number }>;
}

export const fetchStudioDetail = (id: number) =>
  request.get<unknown, StudioDetail>(`/studios/${id}`);
