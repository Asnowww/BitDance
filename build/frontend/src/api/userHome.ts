import request from '@/utils/request';

export interface UserPostTopic {
  id?: number;
  name?: string;
  topicName?: string;
}

export interface UserContentPost {
  id: number;
  authorId?: number;
  authorUserId?: number;
  authorName?: string;
  text?: string;
  contentText?: string;
  topics?: Array<string | UserPostTopic>;
  style?: string;
  location?: string;
  locationName?: string;
  likeCount?: number;
  commentCount?: number;
  createdAt?: number | string;
  publishedAt?: string;
}

export interface UserPracticePost {
  id: number;
  title?: string;
  style?: string;
  level?: string;
  date?: string;
  time?: string;
  location?: string;
  status?: string;
  takenCount?: number;
  capacity?: number;
  creatorUserId?: number;
  danceStyleId?: number;
  locationName?: string;
  skillLevel?: string;
  expectedPeopleMin?: number;
  expectedPeopleMax?: number;
  currentPeopleCount?: number;
  startAt?: string;
  endAt?: string;
  postStatus?: string;
  description?: string;
}

export interface UserReviewItem {
  id: number;
  userId: number;
  targetType: 'studio' | 'course' | 'coach' | string;
  targetId: number;
  overallScore: number | string;
  contentText: string;
  isVerified?: boolean;
  reviewStatus?: string;
  publishedAt?: string;
}

export interface ListResponse<T> {
  list: T[];
  page: number;
  pageSize: number;
  total: number;
}

export interface PublicUserAccess {
  profileVisible: boolean;
  contentVisible: boolean;
  practiceVisible: boolean;
  growthVisible: boolean;
}

export interface PublicUserStyle {
  danceStyleId?: number;
  name?: string;
  skillLevel?: string;
  isPrimary?: boolean;
}

export interface PublicUserProfile {
  userId: number;
  nickname?: string;
  avatarAssetId?: number;
  bio?: string;
  cityId?: number;
  currentLevel?: string;
  learningGoal?: string;
  styles?: PublicUserStyle[];
  access: PublicUserAccess;
}

export type PublicUserSearchResponse = ListResponse<PublicUserProfile>;

export const searchPublicUsers = (q = '', page = 1, pageSize = 20) =>
  request.get<unknown, PublicUserSearchResponse>('/public/users/search', {
    params: { q, page, pageSize }
  });

export const fetchPublicUserProfile = (userId: number) =>
  request.get<unknown, PublicUserProfile>(`/public/users/${userId}/profile`);

export const fetchUserPosts = (userId: number, page = 1, pageSize = 10) =>
  request.get<unknown, ListResponse<UserContentPost>>(`/public/users/${userId}/community/posts`, {
    params: { page, pageSize }
  });

export const fetchUserReviews = (userId: number, page = 1, pageSize = 10) =>
  request.get<unknown, ListResponse<UserReviewItem>>(`/public/users/${userId}/reviews`, {
    params: { page, pageSize }
  });

export const fetchUserPractices = (userId: number) =>
  request.get<unknown, UserPracticePost[]>(`/public/users/${userId}/practices`);
