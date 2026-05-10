import request from '@/utils/request';

export type MessageCategory = 'system' | 'practice' | 'review' | 'trial';

export interface MessageItem {
  id: number;
  category: MessageCategory;
  title: string;
  body: string;
  read: boolean;
  ts: number;
}

export const fetchMessages = () => request.get<unknown, MessageItem[]>('/messages');
export const markRead = (id: number) =>
  request.post<unknown, { read: boolean }>(`/messages/${id}/read`);
export const markAllRead = () => request.post<unknown, { ok: boolean }>('/messages/read-all');
