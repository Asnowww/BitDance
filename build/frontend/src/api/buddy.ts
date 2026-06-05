import request from '@/utils/request';
import { fetchPractices, type PracticePost } from './practice';

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

interface BackendBuddy {
  relationId: number;
  peerUserId: number;
  sourcePracticePostId?: number;
  relationStatus: string;
  createdAt?: string;
}

const toBuddy = (raw: BackendBuddy | Buddy): Buddy => {
  if ('peerUserId' in raw) {
    return {
      userId: raw.peerUserId,
      name: `用户 ${raw.peerUserId}`,
      avatar: '',
      sharedStyles: [],
      pastSessions: raw.sourcePracticePostId ? 1 : 0,
      lastAt: raw.createdAt ? new Date(raw.createdAt).getTime() : Date.now()
    };
  }
  return raw;
};

export const fetchPracticeRecommend = () =>
  fetchPractices({ page: 1, pageSize: 8 }).then((resp) => resp.list as PracticePost[]);

export const fetchMyBuddies = () =>
  request
    .get<unknown, Array<BackendBuddy | Buddy>>('/h5/buddies')
    .then((list) => list.map(toBuddy));

export const submitPracticeRating = (body: PracticeRating) =>
  request
    .post<unknown, unknown>(`/h5/practices/${body.practiceId}/ratings`, {
      toUserId: body.toUserId,
      punctuality: body.punctuality,
      friendliness: body.friendliness,
      skillMatch: body.levelMatch,
      comment: body.comment
    })
    .then(() => ({ ok: true }));
