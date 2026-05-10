import request from '@/utils/request';

export type TrialStatus = 'pending' | 'confirmed' | 'rejected' | 'arrived' | 'noshow' | 'canceled';

export interface TrialBooking {
  id: number;
  studioId: number;
  studioName: string;
  courseId?: number;
  courseName?: string;
  coachId?: number;
  coachName?: string;
  date: string;
  time: string;
  contactPhone: string;
  remark?: string;
  status: TrialStatus;
  createdAt: number;
}

export interface TrialCreateBody {
  studioId: number;
  courseId?: number;
  coachId?: number;
  date: string;
  time: string;
  contactPhone: string;
  remark?: string;
  idempotencyToken: string;
}

export interface ScheduleSlot {
  id: number;
  date: string;
  weekday: string;
  time: string;
  courseId: number;
  courseName: string;
  style: string;
  difficulty: string;
  coachName: string;
  capacity: number;
  taken: number;
}

export const createTrialBooking = (body: TrialCreateBody) =>
  request.post<unknown, TrialBooking>('/trial-bookings', body);

export const fetchMyTrialBookings = () =>
  request.get<unknown, TrialBooking[]>('/trial-bookings/mine');

export const cancelTrialBooking = (id: number) =>
  request.post<unknown, { canceled: boolean }>(`/trial-bookings/${id}/cancel`);

export const fetchStudioSchedule = (studioId: number) =>
  request.get<unknown, ScheduleSlot[]>(`/studios/${studioId}/schedule`);
