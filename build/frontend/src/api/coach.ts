import request from '@/utils/request';

export interface CoachWork {
  id: number;
  type: 'image' | 'video';
  title: string;
  cover: string;
  createdAt: number;
}

export interface CoachAvailableSlot {
  day: string;
  time: string;
}

export interface CoachProfile {
  id: number;
  name: string;
  avatar: string;
  styles: string[];
  teachStyle: string;
  intro: string;
  works: CoachWork[];
  availableSlots: CoachAvailableSlot[];
  ratingAvg: number;
  reviewCount: number;
}

export const fetchMyCoachProfile = () =>
  request.get<unknown, CoachProfile>('/coach/me/profile');

export const updateMyCoachProfile = (body: Partial<CoachProfile>) =>
  request.put<unknown, CoachProfile>('/coach/me/profile', body);

export const addMyCoachWork = (body: { type: 'image' | 'video'; title: string; cover: string }) =>
  request.post<unknown, CoachWork>('/coach/me/works', body);

export const removeMyCoachWork = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/coach/me/works/${id}`);
