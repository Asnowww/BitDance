import request from '@/utils/request';

export type PracticePostStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'MATCHED'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELED'
  | 'EXPIRED';

export interface PracticePost {
  id: number;
  title: string;
  style: string;
  level: string;
  date: string;
  time: string;
  city: string;
  area: string;
  location: string;
  capacity: number;
  takenCount: number;
  remark?: string;
  status: PracticePostStatus;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: number;
}

export interface PracticeListQuery {
  city?: string;
  style?: string;
  level?: string;
  scope?: 'nearby' | 'city';
  page?: number;
  pageSize?: number;
}

export interface PracticeListResp {
  list: PracticePost[];
  page: number;
  pageSize: number;
  total: number;
}

export interface PracticeCreateBody {
  title: string;
  style: string;
  level: string;
  date: string;
  time: string;
  city: string;
  area: string;
  location: string;
  capacity: number;
  remark?: string;
  idempotencyToken: string;
}

export const fetchPractices = (q: PracticeListQuery) =>
  request.get<unknown, PracticeListResp>('/practices', { params: q });

export const fetchPracticeDetail = (id: number) =>
  request.get<unknown, PracticePost>(`/practices/${id}`);

export const createPractice = (body: PracticeCreateBody) =>
  request.post<unknown, PracticePost>('/practices', body);

export const joinPractice = (id: number) =>
  request.post<unknown, { joined: boolean; takenCount: number }>(`/practices/${id}/join`);

export const cancelJoin = (id: number) =>
  request.post<unknown, { canceled: boolean; takenCount: number }>(`/practices/${id}/cancel`);

export const confirmPractice = (id: number) =>
  request.post<unknown, PracticePost>(`/practices/${id}/confirm`);
