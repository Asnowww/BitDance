import request from '@/utils/request';

export type ReviewTargetType = 'studio' | 'course' | 'coach';

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
  ]
};

export interface DimensionScoreDto {
  code: string;
  name: string;
  score: number;
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
}

export interface ReviewListQuery {
  targetType: ReviewTargetType;
  targetId: number;
  sort?: 'latest' | 'helpful' | 'verified';
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
  reviewCount: number;
  verifiedCount: number;
  ratingAvg: number;
  dimensionAvg: Record<string, number>;
}

export interface ReviewCreateBody {
  targetType: ReviewTargetType;
  targetId: number;
  overallScore: number;
  contentText: string;
  dimensions: DimensionScoreDto[];
  sourceType?: 'trial' | 'order' | 'checkin';
  sourceRefId?: number;
}

export const fetchReviews = (q: ReviewListQuery) =>
  request.get<unknown, ReviewListResp>('/public/reviews', { params: q });

export const fetchReviewSummary = (targetType: ReviewTargetType, targetId: number) =>
  request.get<unknown, ReviewSummary>('/public/reviews/summary', {
    params: { targetType, targetId }
  });

export const createReview = (body: ReviewCreateBody) =>
  request.post<unknown, ReviewItem>('/h5/reviews', body);

export const deleteReview = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/h5/reviews/${id}`);
