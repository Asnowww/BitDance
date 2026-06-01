import request from '@/utils/request';

export type WorkshopStatus = 'PUBLISHED' | 'CLOSED' | 'CANCELED';
export type OrderStatus =
  | 'UNPAID'
  | 'PAID'
  | 'CANCELED'
  | 'REFUNDED'
  | 'CHECKED_IN'
  | 'COMPLETED'
  | 'pending_payment'
  | 'paid'
  | 'canceled'
  | 'refunded'
  | 'completed';

export interface WorkshopSession {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  capacity: number;
  taken: number;
  price: number;
}

export interface WorkshopBrief {
  id: number;
  title: string;
  cover: string;
  city: string;
  area: string;
  styles: string[];
  startDate: string;
  endDate: string;
  priceMin: number;
  priceMax: number;
  capacity: number;
  taken: number;
  coachName: string;
  hot: boolean;
}

export interface WorkshopDetail extends WorkshopBrief {
  intro: string;
  studioName: string;
  studioId: number;
  coachId: number;
  pastReviews: Array<{ id: number; author: string; text: string; rating: number }>;
  sessions: WorkshopSession[];
  status: WorkshopStatus;
}

export interface WorkshopOrder {
  id: number;
  orderNo?: string;
  workshopId: number;
  workshopSessionId?: number;
  workshopTitle?: string;
  sessionId?: number;
  sessionDate?: string;
  sessionTime?: string;
  userId?: number;
  amount?: number;
  amountPayable?: number;
  amountPaid?: number;
  status?: OrderStatus;
  orderStatus?: OrderStatus;
  paymentTxnNo?: string;
  checkinCode?: string;
  paidAt?: string;
  canceledAt?: string;
  refundedAt?: string;
  createdAt: number | string;
}

export const fetchWorkshops = (params: { city?: string; style?: string; page?: number; pageSize?: number }) =>
  request.get<unknown, { list: WorkshopBrief[]; page: number; pageSize: number; total: number }>(
    '/workshops',
    { params }
  );

export const fetchWorkshopDetail = (id: number) =>
  request.get<unknown, WorkshopDetail>(`/workshops/${id}`);

export const createWorkshopOrder = (body: {
  workshopId: number;
  sessionId: number;
  idempotencyToken: string;
}) => request.post<unknown, WorkshopOrder>('/workshop-orders', body);

export const payWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrder>(`/h5/workshop-orders/${id}/pay`);

export const cancelWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrder>(`/h5/workshop-orders/${id}/cancel`);

export const refundWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrder>(`/h5/workshop-orders/${id}/refund`);

export const fetchMyWorkshopOrders = () =>
  request.get<unknown, WorkshopOrder[]>('/h5/workshop-orders/mine');

export const checkinWorkshopOrder = (id: number, code: string) =>
  request.post<unknown, WorkshopOrder>(`/h5/workshop-orders/${id}/checkin`, { code });
