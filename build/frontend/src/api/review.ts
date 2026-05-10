import request from '@/utils/request';

export type ReviewTargetType = 'studio' | 'course' | 'coach';

export const REVIEW_DIMENSIONS: Record<ReviewTargetType, Array<{ key: string; label: string }>> = {
  studio: [
    { key: 'traffic', label: '交通' },
    { key: 'hygiene', label: '卫生' },
    { key: 'venue', label: '场地' },
    { key: 'vibe', label: '氛围' }
  ],
  coach: [
    { key: 'patience', label: '耐心' },
    { key: 'correction', label: '纠错' },
    { key: 'explanation', label: '讲解' },
    { key: 'beginnerFriendly', label: '零基础友好' }
  ],
  course: [
    { key: 'difficulty', label: '上手难度' },
    { key: 'rhythm', label: '节奏' },
    { key: 'intensity', label: '强度' },
    { key: 'gain', label: '收获' }
  ]
};

export interface ReviewItem {
  id: number;
  targetType: ReviewTargetType;
  targetId: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  dimensionScores: Record<string, number>;
  ratingAvg: number;
  isVerified: boolean;
  verifiedSourceType?: string;
  helpfulCount: number;
  createdAt: number;
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
  summary: {
    ratingAvg: number;
    reviewCount: number;
    dimensionAvg: Record<string, number>;
  };
}

export interface ReviewCreateBody {
  targetType: ReviewTargetType;
  targetId: number;
  text: string;
  images?: string[];
  dimensionScores: Record<string, number>;
  idempotencyToken: string;
}

export const fetchReviews = (q: ReviewListQuery) =>
  request.get<unknown, ReviewListResp>('/reviews', { params: q });

export const createReview = (body: ReviewCreateBody) =>
  request.post<unknown, ReviewItem>('/reviews', body);

export const updateReview = (id: number, body: Partial<ReviewCreateBody>) =>
  request.put<unknown, ReviewItem>(`/reviews/${id}`, body);

export const deleteReview = (id: number) =>
  request.delete<unknown, { deleted: boolean }>(`/reviews/${id}`);

export const fetchMyReviews = () => request.get<unknown, ReviewItem[]>('/reviews/mine');
