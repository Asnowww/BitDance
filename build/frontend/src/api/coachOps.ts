import request from '@/utils/request';

export interface ReviewAppeal {
  id: number;
  reviewId: number;
  reason: string;
  evidence: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: number;
}

export interface CoachWorkshopDraft {
  title: string;
  styles: string[];
  intro: string;
  startDate: string;
  endDate: string;
  city: string;
  area: string;
  studioId?: number;
  sessions: Array<{ date: string; startTime: string; endTime: string; capacity: number; price: number }>;
}

export interface CreateWorkshopPayload {
  studioId: number;
  coachId?: number;
  cityId: number;
  danceStyleId?: number;
  workshopName: string;
  coverAssetId?: number;
  intro?: string;
  address: string;
  locationName: string;
  priceAmount: number;
  minPeople?: number;
  maxPeople?: number;
  signupDeadline?: string | null;
  sourceType?: 'studio' | 'coach';
}

export interface CoachWorkshopOrderRow {
  orderId: number;
  workshopId: number;
  workshopTitle: string;
  buyerName: string;
  sessionDate: string;
  sessionTime: string;
  amount: number;
  status: string;
  checkinCode: string;
}

export interface CoachReviewReplyBody {
  reviewId: number;
  text: string;
}

export interface CoachDashboard {
  monthSessions: number;
  monthStudents?: number;
  monthWorkshopOrders?: number;
  monthIncome: number;
  pendingReplies?: number;
  pendingReviewReplies?: number;
  ratingAvg?: number;
  avgRating?: number;
  ratingCount: number;
  conversionRate?: number;
}

export const submitAppeal = (body: { reviewId: number; reason: string; evidence: string }) =>
  request.post<unknown, ReviewAppeal>('/h5/review-appeals', body);

export const fetchAppeals = () => request.get<unknown, ReviewAppeal[]>('/h5/review-appeals/mine');

export const createCoachWorkshop = (body: CreateWorkshopPayload) =>
  request.post<unknown, { id: number; status: string }>('/merchant/workshops', body);

export const fetchCoachOrders = () =>
  request.get<unknown, CoachWorkshopOrderRow[]>('/merchant/workshop-orders');

export const checkinByCoach = (orderId: number, code: string) =>
  request.post<unknown, { ok: boolean }>(`/merchant/workshop-orders/${orderId}/checkin`, { code });

export const replyReview = (body: CoachReviewReplyBody) =>
  request.post<unknown, { ok: boolean }>('/h5/review-replies', body);

export const fetchCoachDashboard = () =>
  request.get<unknown, CoachDashboard>('/h5/coach/dashboard');
