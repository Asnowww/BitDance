import request from '@/utils/request';
import type { PracticePost } from './practice';

export interface Buddy {
  userId: number;
  name: string;
  avatar: string;
  sharedStyles: string[];
  pastSessions: number;
  lastAt: number;
}

export interface PracticeRating {
  practiceId: number;
  toUserId: number;
  punctuality: number;
  friendliness: number;
  levelMatch: number;
  comment?: string;
}

export const fetchPracticeRecommend = () =>
  request.get<unknown, PracticePost[]>('/practices/recommend');

export const fetchMyBuddies = () => request.get<unknown, Buddy[]>('/buddies/mine');

export const submitPracticeRating = (body: PracticeRating) =>
  request.post<unknown, { ok: boolean }>('/practices/ratings', body);
