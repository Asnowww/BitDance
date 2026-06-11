import request from '@/utils/request';

export type ReviewTargetType = 'studio' | 'course' | 'coach' | 'workshop';

export interface ReviewDimension {
  key: string;
  label: string;
}

export const REVIEW_DIMENSIONS: Record<ReviewTargetType, ReviewDimension[]> = {
  studio: [
    { key: 'traffic', label: '交通便利度' },
    { key: 'hygiene', label: '环境卫生' },
    { key: 'venue', label: '场地条件' },
    { key: 'vibe', label: '整体氛围' }
  ],
  coach: [
    { key: 'patience', label: '耐心程度' },
    { key: 'correction', label: '纠错质量' },
    { key: 'explanation', label: '讲解清晰度' },
    { key: 'beginnerFriendly', label: '零基础友好' }
  ],
  course: [
    { key: 'difficulty', label: '上手难度' },
    { key: 'rhythm', label: '节奏合理性' },
    { key: 'intensity', label: '练习强度' },
    { key: 'gain', label: '实际收获' }
  ],
  workshop: [
    { key: 'organization', label: '活动组织' },
    { key: 'teaching', label: '教学质量' },
    { key: 'venue', label: '场地体验' },
    { key: 'gain', label: '参与收获' }
  ]
};

export interface DimensionScoreDto {
  code: string;
  name: string;
  score: number;
}

export interface ReviewMediaDto {
  assetId?: number;
  type: 'image' | 'video';
  url: string;
  name: string;
  size: number;
  previewUrl?: string;
}

export interface ReviewItem {
  id: number;
  userId: number;
  targetType: ReviewTargetType;
  targetId: number;
  overallScore: number;
  contentText: string;
  isVerified: boolean;
  verifiedSourceType?: string;
  weightFactor: number;
  reviewStatus: 'published' | 'pending' | 'folded' | 'hidden' | string;
  riskLevel: number;
  helpfulCount: number;
  isPinned: boolean;
  publishedAt: string;
  dimensions: DimensionScoreDto[];
  mediaAssets?: ReviewMediaDto[];
  latestAppeal?: ReviewAppealDto;
}

export interface ReviewListQuery {
  targetType: ReviewTargetType;
  targetId: number;
  sort?: 'latest' | 'helpful' | 'verified';
  status?: 'published' | 'folded';
  page?: number;
  pageSize?: number;
}

export interface ReviewListResp {
  list: ReviewItem[];
  page: number;
  pageSize: number;
  total: number;
}

export interface ReviewSummary {
  targetType: ReviewTargetType;
  targetId: number;
  count: number;
  verifiedCount: number;
  weightedAvgScore: number;
  dimensionAvg: Record<string, number>;
}

export interface ReviewCreateBody {
  targetType: ReviewTargetType;
  targetId: number;
  overallScore: number;
  contentText: string;
  dimensions: DimensionScoreDto[];
  mediaAssets?: ReviewMediaDto[];
  sourceType?: 'trial' | 'order' | 'checkin';
  sourceRefId?: number;
}

export interface ReviewAppealDto {
  id: number;
  reviewId: number;
  appellantUserId: number;
  appealReason: string;
  appealStatus: string;
  evidenceNote?: string;
  reviewedByUserId?: number;
  reviewedAt?: string;
  reviewRemark?: string;
  createdAt: string;
}

export interface ReviewReplyDto {
  id: number;
  reviewId: number;
  replierUserId: number;
  replyContent: string;
  isOfficial: boolean;
  createdAt: string;
}

export const fetchReviews = (q: ReviewListQuery) =>
  request.get<unknown, ReviewListResp>('/public/reviews', { params: q });

export const fetchReviewSummary = (targetType: ReviewTargetType, targetId: number) =>
  request.get<unknown, ReviewSummary>('/public/reviews/summary', {
    params: { targetType, targetId }
  });

export const createReview = (body: ReviewCreateBody) =>
  request.post<unknown, ReviewItem>('/h5/reviews', body);

export const fetchMyReviews = (params: { page?: number; pageSize?: number } = {}) =>
  // M2 风控验收：本人列表读取 h5 接口，保留 pending/folded 等审核状态用于前端可视化。
  request.get<unknown, ReviewListResp>('/h5/reviews/mine', { params });

export const fetchReviewReplyQueue = (params: { page?: number; pageSize?: number } = {}) =>
  // M2 回复治理：教练/商家端读取真实评价队列，替代静态样例数据。
  request.get<unknown, ReviewListResp>('/h5/reviews/reply-queue', { params });

export const deleteReview = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/reviews/${id}`);

export const createReviewAppeal = (body: { reviewId: number; appealReason: string; evidenceNote?: string }) =>
  // M2 商家/教练申诉：写入后端 review_appeal 表，进入平台人工审核流。
  request.post<unknown, ReviewAppealDto>('/h5/review-appeals', body);

export const fetchMyReviewAppeals = () =>
  request.get<unknown, ReviewAppealDto[]>('/h5/review-appeals/mine');

export const fetchReviewReplies = (reviewId: number) =>
  request.get<unknown, ReviewReplyDto[]>('/public/review-replies', { params: { reviewId } });

export const createReviewReply = (body: { reviewId: number; replyContent: string; isOfficial?: boolean }) =>
  // M2 回复治理：商家/教练回复写入 review_reply，公开详情页可继续读取。
  request.post<unknown, ReviewReplyDto>('/h5/review-replies', body);
