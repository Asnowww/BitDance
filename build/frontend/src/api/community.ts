import request from '@/utils/request';

export interface ContentPost {
  id: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  videoCover?: string;
  hasVideo?: boolean;
  topics: string[];
  style?: string;
  location?: string;
  likeCount: number;
  commentCount: number;
  collectCount: number;
  liked: boolean;
  collected: boolean;
  createdAt: number;
}

export interface ContentComment {
  id: number;
  postId: number;
  authorId: number;
  authorName: string;
  text: string;
  createdAt: number;
}

export interface FeedQuery {
  scope?: 'recommend' | 'follow';
  topic?: string;
  style?: string;
  page?: number;
  pageSize?: number;
}

export interface FeedResp {
  list: ContentPost[];
  page: number;
  pageSize: number;
  total: number;
}

export const fetchFeed = (q: FeedQuery) =>
  request.get<unknown, FeedResp>('/community/feed', { params: q });

export const fetchPostDetail = (id: number) =>
  request.get<unknown, ContentPost>(`/community/posts/${id}`);

export const createPost = (body: {
  text: string;
  images?: string[];
  hasVideo?: boolean;
  topics?: string[];
  style?: string;
  location?: string;
  idempotencyToken: string;
}) => request.post<unknown, ContentPost>('/community/posts', body);

export const togglePostLike = (id: number) =>
  request.post<unknown, { liked: boolean; likeCount: number }>(`/community/posts/${id}/like`);

export const togglePostCollect = (id: number) =>
  request.post<unknown, { collected: boolean; collectCount: number }>(`/community/posts/${id}/collect`);

export const reportPost = (id: number, reason: string) =>
  request.post<unknown, { reported: boolean }>(`/community/posts/${id}/report`, { reason });

export const fetchComments = (postId: number) =>
  request.get<unknown, ContentComment[]>(`/community/posts/${postId}/comments`);

export const createComment = (postId: number, text: string) =>
  request.post<unknown, ContentComment>(`/community/posts/${postId}/comments`, { text });

export const fetchTopics = () =>
  request.get<unknown, Array<{ name: string; count: number; hot: boolean }>>('/community/topics');

export const fetchTopicPosts = (topic: string) =>
  request.get<unknown, FeedResp>('/community/feed', { params: { topic, page: 1, pageSize: 50 } });

export const toggleFollow = (userId: number) =>
  request.post<unknown, { following: boolean }>(`/community/follow/${userId}`);

export const fetchFollowing = () =>
  request.get<unknown, Array<{ id: number; name: string; avatar: string; followed: boolean }>>(
    '/community/follow/me'
  );

export const searchContent = (q: string) =>
  request.get<unknown, FeedResp>('/community/search', { params: { q } });
