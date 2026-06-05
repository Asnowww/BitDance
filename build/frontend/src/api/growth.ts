import request from '@/utils/request';

export interface CheckinItem {
  id: number;
  userId?: number;
  danceStyleId?: number | null;
  studioId?: number | null;
  courseScheduleId?: number | null;
  practicePostId?: number | null;
  durationMinutes?: number;
  feelingText?: string | null;
  isPublic?: boolean;
  checkinAt?: string;
  style?: string;
  durationMin?: number;
  location?: string;
  feeling?: string;
  visibility?: 'public' | 'private' | 'friends';
  createdAt?: number;
}

export interface CheckinCreateBody {
  style?: string;
  durationMin?: number;
  location?: string;
  feeling?: string;
  visibility?: 'public' | 'private' | 'friends';
  idempotencyToken?: string;
  danceStyleId?: number;
  studioId?: number;
  courseScheduleId?: number;
  practicePostId?: number;
  durationMinutes?: number;
  feelingText?: string;
  isPublic?: boolean;
  checkinAt?: string;
}

export interface GrowthStats {
  totalSessions: number;
  totalMinutes: number;
  totalDays: number;
  styleCount: number;
  streakDays: number;
  lastCheckinAt: string | null;
  courseCount: number;
  weekSessions: number;
  weekMinutes: number;
  monthSessions: number;
  monthMinutes: number;
  recentAt?: number | null;
  goalProgress?: number;
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
  id?: number;
  userId?: number;
  goalPeriod?: 'weekly' | 'monthly';
  targetMinutes: number;
  targetTimes?: number;
  currentMinutes?: number;
  currentTimes?: number;
  startDate: string;
  endDate: string;
  goalStatus?: 'active' | 'completed' | 'expired' | 'canceled';
  period?: 'week' | 'month';
  targetSessions?: number;
}

export const createCheckin = (body: CheckinCreateBody) =>
  request.post<unknown, CheckinItem>('/growth/checkins', {
    danceStyleId: body.danceStyleId,
    studioId: body.studioId,
    courseScheduleId: body.courseScheduleId,
    practicePostId: body.practicePostId,
    durationMinutes: body.durationMinutes ?? body.durationMin,
    feelingText: body.feelingText ?? body.feeling,
    isPublic: body.isPublic ?? body.visibility !== 'private',
    checkinAt: body.checkinAt
  });

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
  request.get<unknown, GrowthGoal | null>('/growth/goals/active');

export const saveGrowthGoal = (body: GrowthGoal) =>
  request.put<unknown, GrowthGoal>('/growth/goals/active', body);
