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
  type: 'checkin' | 'trial' | 'practice' | 'review' | 'work';
  title: string;
  subtitle?: string;
  ts: number;
}

export interface GrowthWork {
  id: number;
  type: 'image' | 'video';
  title: string;
  description: string;
  style?: string;
  visibility: 'public' | 'private' | 'friends';
  createdAt: number;
}

export interface GrowthGoal {
  period: 'week' | 'month';
  targetSessions: number;
  targetMinutes: number;
  startDate: string;
  endDate: string;
}

export const createCheckin = (body: CheckinCreateBody) =>
  request.post<unknown, CheckinItem>('/growth/checkins', body);

export const fetchCheckins = () =>
  request.get<unknown, CheckinItem[]>('/growth/checkins');

export const fetchGrowthStats = () =>
  request.get<unknown, GrowthStats>('/growth/stats');

export const fetchGrowthTimeline = () =>
  request.get<unknown, TimelineItem[]>('/growth/timeline');

export const fetchGrowthWorks = () =>
  request.get<unknown, GrowthWork[]>('/growth/works');

export const createGrowthWork = (body: Omit<GrowthWork, 'id' | 'createdAt'>) =>
  request.post<unknown, GrowthWork>('/growth/works', body);

export const deleteGrowthWork = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/growth/works/${id}`);

export const fetchGrowthGoal = () =>
  request.get<unknown, GrowthGoal | null>('/growth/goal');

export const saveGrowthGoal = (body: GrowthGoal) =>
  request.put<unknown, GrowthGoal>('/growth/goal', body);
