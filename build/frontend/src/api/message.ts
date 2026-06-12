import request from '@/utils/request';

export type MessageCategory = 'system' | 'practice' | 'review' | 'trial' | 'workshop';

export interface MessageItem {
  id: number;
  noticeType?: string;
  category: MessageCategory;
  title: string;
  content?: string;
  body?: string;
  targetType?: string;
  targetId?: number;
  isRead?: boolean;
  read?: boolean;
  createdAt?: string;
  ts?: number;
}

export interface MessageListResponse {
  list: MessageItem[];
  page: number;
  pageSize: number;
  total: number;
  unread: number;
}

export const fetchMessages = (category?: string) =>
  request.get<unknown, MessageListResponse>('/h5/messages', {
    params: category && category !== 'all' ? { category } : undefined
  });
export const markRead = (id: number) =>
  request.post<unknown, { ok: boolean }>(`/h5/messages/${id}/read`);
export const markAllRead = () => request.post<unknown, { ok: boolean; affected: number }>('/h5/messages/read-all');
