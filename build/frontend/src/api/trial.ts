import request from '@/utils/request';

export type TrialStatus = 'pending' | 'confirmed' | 'rejected' | 'arrived' | 'noshow' | 'canceled';

export interface TrialBooking {
  id: number;
  userId: number;
  courseId: number;
  courseScheduleId?: number;
  studioId: number;
  bookingStatus: TrialStatus;
  contactPhone: string;
  bookingNote?: string;
  createdAt: string;
}

export interface TrialCreateBody {
  courseId: number;
  courseScheduleId?: number;
  contactPhone: string;
  bookingNote?: string;
}

export interface ScheduleSlot {
  id: number;
  courseId: number;
  studioId: number;
  coachId: number;
  classroomName: string;
  startAt: string;
  endAt: string;
  capacity: number;
  bookedCount: number;
  status: string;
}

export const createTrialBooking = (body: TrialCreateBody) =>
  request.post<unknown, TrialBooking>('/h5/trial-bookings', body);

export const fetchMyTrialBookings = () =>
  request.get<unknown, TrialBooking[]>('/h5/trial-bookings');

export const cancelTrialBooking = (id: number) =>
  request.post<unknown, TrialBooking>(`/h5/trial-bookings/${id}/cancel`);

export const fetchStudioSchedule = (studioId: number) =>
  request.get<unknown, ScheduleSlot[]>(`/public/studios/${studioId}/schedules`);
