import request from '@/utils/request';

export interface CheckinItem {
  id: number;
  style: string;
  durationMin: number;
  location: string;
  feeling: string;
  visibility: 'public' | 'private' | 'friends';
  createdAt: number;
}

export interface CheckinCreateBody {
  style: string;
  durationMin: number;
  location: string;
  feeling: string;
  visibility: 'public' | 'private' | 'friends';
  idempotencyToken: string;
}

export interface GrowthStats {
  totalDays: number;
  totalMinutes: number;
  totalSessions: number;
  styleCount: number;
  streakDays: number;
  recentAt: number | null;
  goalProgress: number;
}

export interface TimelineItem {
  id: string;
  type: 'checkin' | 'trial' | 'practice' | 'review';
  title: string;
  subtitle?: string;
  ts: number;
}

export const createCheckin = (body: CheckinCreateBody) =>
  request.post<unknown, CheckinItem>('/growth/checkins', body);

export const fetchCheckins = () =>
  request.get<unknown, CheckinItem[]>('/growth/checkins');

export const fetchGrowthStats = () =>
  request.get<unknown, GrowthStats>('/growth/stats');

export const fetchGrowthTimeline = () =>
  request.get<unknown, TimelineItem[]>('/growth/timeline');
