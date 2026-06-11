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
  id?: string;
  refId?: number;
  type: 'checkin' | 'trial' | 'practice' | 'review' | 'work';
  title: string;
  subtitle?: string;
  ts: number | string;
}

export interface MediaAssetDto {
  id: number;
  assetType: 'image' | 'video' | 'document' | 'audio';
  bizType: string;
  originFileName: string;
  mimeType: string;
  fileSize: number;
  url: string;
  createdAt?: string;
}

export interface GrowthWork {
  id: number;
  userId?: number;
  danceStyleId?: number | null;
  workTitle?: string;
  workDescription?: string | null;
  coverAssetId?: number | null;
  isPublic?: boolean;
  coverUrl?: string | null;
  mediaAssets?: MediaAssetDto[];
  createdAt?: string | number;
  type?: 'image' | 'video';
  title?: string;
  description?: string;
  style?: string;
  visibility?: 'public' | 'private' | 'friends';
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

export interface GrowthReport {
  period: 'monthly' | 'quarterly';
  startDate: string;
  endDate: string;
  totalSessions: number;
  totalMinutes: number;
  activeDays: number;
  styleCount: number;
  workCount: number;
  badgeCount: number;
  goalTargetTimes?: number | null;
  goalCurrentTimes?: number | null;
  goalTargetMinutes?: number | null;
  goalCurrentMinutes?: number | null;
  goalProgress: number;
  styleSessions: Record<string, number>;
  highlights: TimelineItem[];
  suggestion: string;
}

export interface BadgeDefinition {
  id: number;
  badgeCode: string;
  badgeName: string;
  description?: string | null;
  iconAssetId?: number | null;
  ruleType: string;
  ruleConfig?: string | null;
  status: string;
}

export interface CreateGrowthWorkBody {
  danceStyleId?: number | null;
  workTitle: string;
  workDescription?: string;
  coverAssetId?: number | null;
  isPublic?: boolean;
  mediaAssetIds?: number[];
}

export const createCheckin = (body: CheckinCreateBody) =>
  request.post<unknown, CheckinItem>('/h5/growth/checkins', {
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
  request.get<unknown, CheckinItem[]>('/h5/growth/checkins');

export const fetchGrowthStats = () =>
  request.get<unknown, GrowthStats>('/h5/growth/stats');

export const fetchGrowthTimeline = () =>
  request.get<unknown, TimelineItem[]>('/h5/growth/timeline');

export const fetchGrowthWorks = () =>
  request.get<unknown, GrowthWork[]>('/h5/growth/works');

export const createGrowthWork = (body: CreateGrowthWorkBody) =>
  request.post<unknown, GrowthWork>('/h5/growth/works', body);

export const deleteGrowthWork = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/growth/works/${id}`);

export const fetchGrowthGoal = () =>
  request.get<unknown, GrowthGoal | null>('/h5/growth/goals/active');

export const saveGrowthGoal = (body: GrowthGoal) =>
  request.put<unknown, GrowthGoal>('/h5/growth/goals/active', body);

export const fetchGrowthBadges = () =>
  request.get<unknown, Array<{ id: number; badgeId: number; sourceType?: string; sourceRefId?: number; awardedAt: string }>>('/h5/growth/badges');

export const fetchBadgeDefinitions = () =>
  request.get<unknown, BadgeDefinition[]>('/public/badges/definitions');

export const fetchGrowthReport = (period: 'monthly' | 'quarterly', anchorDate?: string) =>
  request.get<unknown, GrowthReport>('/h5/growth/reports', { params: { period, anchorDate } });

export const uploadMediaAsset = (file: File, bizType = 'growth_work') => {
  const form = new FormData();
  form.append('file', file);
  form.append('bizType', bizType);
  return request.post<unknown, MediaAssetDto>('/h5/media-assets', form);
};
