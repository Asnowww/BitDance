import request from '@/utils/request';

export type WorkshopStatus = 'PUBLISHED' | 'CLOSED' | 'CANCELED';
export type OrderStatus = 'UNPAID' | 'PAID' | 'CANCELED' | 'REFUNDED' | 'CHECKED_IN' | 'COMPLETED';

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
  signupDeadline?: string | null;
  pastReviews: Array<{ id: number; author: string; text: string; rating: number }>;
  sessions: WorkshopSession[];
  status: WorkshopStatus;
}

export interface WorkshopOrder {
  id: number;
  orderNo?: string;
  workshopId: number;
  workshopTitle: string;
  sessionId: number;
  workshopSessionId?: number;
  sessionDate: string;
  sessionTime: string;
  amount: number;
  amountPayable?: number;
  amountPaid?: number;
  status: OrderStatus;
  orderStatus?: OrderStatus | string;
  checkinCode: string;
  createdAt: number;
}

interface SessionResp {
  id: number;
  workshopId: number;
  sessionName?: string | null;
  startAt?: string;
  endAt?: string;
  date?: string;
  startTime?: string;
  endTime?: string;
  capacity: number;
  soldCount?: number;
  taken?: number;
  checkinCount?: number;
  price?: number;
  sessionStatus?: string;
}

interface WorkshopBriefResp {
  id: number;
  studioId?: number | null;
  coachId?: number | null;
  cityId?: number | null;
  danceStyleId?: number | null;
  workshopName?: string;
  title?: string;
  coverAssetId?: number | null;
  locationName?: string | null;
  area?: string;
  priceAmount?: number | string | null;
  priceMin?: number;
  priceMax?: number;
  signupDeadline?: string | null;
  startDate?: string;
  endDate?: string;
  publishStatus?: string;
  styles?: string[];
  city?: string;
  coachName?: string;
  hot?: boolean;
  capacity?: number;
  soldCount?: number;
  taken?: number;
}

interface WorkshopDetailResp extends WorkshopBriefResp {
  intro?: string | null;
  address?: string | null;
  minPeople?: number | null;
  maxPeople?: number | null;
  auditStatus?: string | null;
  sessions: SessionResp[];
  studioName?: string;
  pastReviews?: Array<{ id: number; author: string; text: string; rating: number }>;
  favored?: boolean;
}

interface WorkshopOrderResp {
  id: number;
  orderNo?: string;
  workshopId: number;
  workshopTitle?: string;
  workshopSessionId?: number;
  sessionId?: number;
  sessionDate?: string;
  sessionTime?: string;
  userId?: number;
  amountPayable?: number | string | null;
  amountPaid?: number | string | null;
  amount?: number | string | null;
  orderStatus?: string;
  status?: string;
  paymentTxnNo?: string | null;
  checkinCode?: string | null;
  paidAt?: string | null;
  canceledAt?: string | null;
  refundedAt?: string | null;
  createdAt: string | number;
}

const STYLE_ID: Record<string, number> = {
  Hiphop: 1,
  Jazz: 2,
  Breaking: 3,
  Locking: 4,
  Popping: 5,
  Kpop: 6,
  Waacking: 7
};

const STYLE_NAME: Record<number, string> = Object.fromEntries(
  Object.entries(STYLE_ID).map(([name, id]) => [id, name])
) as Record<number, string>;

const CITY_ID: Record<string, number> = {
  北京: 1,
  上海: 2,
  广州: 3,
  深圳: 4,
  杭州: 5
};

const toNumber = (value: number | string | null | undefined) => Number(value ?? 0);
const toMs = (value?: string | number | null) => {
  if (typeof value === 'number') return value;
  if (!value) return Date.now();
  const parsed = Date.parse(value);
  return Number.isNaN(parsed) ? Date.now() : parsed;
};
const datePart = (value?: string | null) => (value ? value.slice(0, 10) : '');
const timePart = (value?: string | null) => {
  if (!value) return '';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value.slice(11, 16);
  return parsed.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });
};
const mapOrderStatus = (status?: string | null): OrderStatus => {
  const s = (status ?? '').toLowerCase();
  if (s === 'pending_payment' || s === 'unpaid') return 'UNPAID';
  if (s === 'paid') return 'PAID';
  if (s === 'checked_in') return 'CHECKED_IN';
  if (s === 'completed') return 'COMPLETED';
  if (s === 'refunded' || s === 'refunding') return 'REFUNDED';
  if (s === 'canceled' || s === 'cancelled') return 'CANCELED';
  return 'UNPAID';
};

const mapSession = (s: SessionResp, fallbackPrice = 0): WorkshopSession => ({
  id: s.id,
  date: s.date ?? datePart(s.startAt),
  startTime: s.startTime ?? timePart(s.startAt),
  endTime: s.endTime ?? timePart(s.endAt),
  capacity: s.capacity,
  taken: s.soldCount ?? s.taken ?? 0,
  price: toNumber(s.price ?? fallbackPrice)
});

const mapBrief = (w: WorkshopBriefResp): WorkshopBrief => {
  const styleName = w.styles?.[0] ?? (w.danceStyleId ? STYLE_NAME[w.danceStyleId] : 'Workshop');
  const price = toNumber(w.priceAmount ?? w.priceMin);
  return {
    id: w.id,
    title: w.workshopName ?? w.title ?? `Workshop #${w.id}`,
    cover: '',
    city: w.city ?? (w.cityId ? Object.entries(CITY_ID).find(([, id]) => id === w.cityId)?.[0] ?? '同城' : '同城'),
    area: w.locationName ?? w.area ?? '线下舞室',
    styles: [styleName],
    startDate: w.startDate ?? (w.signupDeadline ? `截止 ${datePart(w.signupDeadline)}` : '开放报名'),
    endDate: w.endDate ?? '',
    priceMin: price,
    priceMax: toNumber(w.priceMax ?? price),
    capacity: w.capacity ?? 0,
    taken: w.soldCount ?? w.taken ?? 0,
    coachName: w.coachName ?? (w.coachId ? `教练 #${w.coachId}` : '特邀导师'),
    hot: w.hot ?? (w.publishStatus === 'published' || w.publishStatus === undefined)
  };
};

const mapDetail = (w: WorkshopDetailResp): WorkshopDetail => {
  const price = toNumber(w.priceAmount ?? w.priceMin);
  const sessions = (w.sessions ?? []).map((s) => mapSession(s, price));
  const taken = sessions.reduce((sum, s) => sum + s.taken, 0);
  const capacity = sessions.reduce((sum, s) => sum + s.capacity, 0);
  const brief = mapBrief(w);
  return {
    ...brief,
    startDate: sessions[0]?.date ?? brief.startDate,
    endDate: sessions.length ? sessions[sessions.length - 1].date : brief.endDate,
    capacity: capacity || brief.capacity,
    taken: taken || brief.taken,
    intro: w.intro ?? '活动介绍待补充，报名后请按订单内时间到场签到。',
    studioName: w.studioName ?? w.locationName ?? '合作舞室',
    studioId: w.studioId ?? 0,
    coachId: w.coachId ?? 0,
    signupDeadline: w.signupDeadline ?? null,
    pastReviews: w.pastReviews ?? [],
    sessions,
    status: w.publishStatus === 'published' || w.publishStatus === undefined ? 'PUBLISHED' : 'CLOSED'
  };
};

const mapOrder = (o: WorkshopOrderResp): WorkshopOrder => ({
  id: o.id,
  orderNo: o.orderNo,
  workshopId: o.workshopId,
  workshopTitle: o.workshopTitle ?? `Workshop #${o.workshopId}`,
  sessionId: o.workshopSessionId ?? o.sessionId ?? 0,
  workshopSessionId: o.workshopSessionId ?? o.sessionId ?? 0,
  sessionDate: o.sessionDate ?? '',
  sessionTime: o.sessionTime ?? '',
  amount: toNumber(o.amountPaid) || toNumber(o.amountPayable) || toNumber(o.amount),
  amountPayable: toNumber(o.amountPayable ?? o.amount),
  amountPaid: toNumber(o.amountPaid),
  status: mapOrderStatus(o.orderStatus ?? o.status),
  orderStatus: mapOrderStatus(o.orderStatus ?? o.status),
  checkinCode: o.checkinCode ?? '',
  createdAt: toMs(o.createdAt)
});

const hydrateOrders = async (items: WorkshopOrderResp[]): Promise<WorkshopOrder[]> => {
  const mapped = (items ?? []).map(mapOrder);
  const ids = Array.from(new Set(mapped.map((item) => item.workshopId)));
  const details = new Map<number, WorkshopDetail>();
  await Promise.all(
    ids.map(async (id) => {
      try {
        details.set(id, await fetchWorkshopDetail(id));
      } catch {
        /* order history can outlive a workshop detail */
      }
    })
  );
  return mapped.map((order) => {
    const detail = details.get(order.workshopId);
    const session = detail?.sessions.find((item) => item.id === order.sessionId);
    return {
      ...order,
      workshopTitle: detail?.title ?? order.workshopTitle,
      sessionDate: session?.date ?? order.sessionDate,
      sessionTime: session ? `${session.startTime}-${session.endTime}` : order.sessionTime,
      amount: session?.price ?? order.amount
    };
  });
};

export const fetchWorkshops = (params: { city?: string; style?: string; page?: number; pageSize?: number }) =>
  request
    .get<unknown, { list: WorkshopBriefResp[]; page: number; pageSize: number; total: number }>(
      '/public/workshops',
      {
        params: {
          cityId: params.city ? CITY_ID[params.city] : undefined,
          danceStyleId: params.style ? STYLE_ID[params.style] : undefined,
          page: params.page,
          pageSize: params.pageSize
        }
      }
    )
    .then((data) => ({ ...data, list: (data.list ?? []).map(mapBrief) }));

export const fetchWorkshopDetail = (id: number) =>
  request.get<unknown, WorkshopDetailResp>(`/public/workshops/${id}`).then(mapDetail);

export const createWorkshopOrder = (body: {
  workshopId: number;
  sessionId: number;
  idempotencyToken: string;
}) =>
  request
    .post<unknown, WorkshopOrderResp>('/h5/workshop-orders', {
      workshopId: body.workshopId,
      sessionId: body.sessionId
    })
    .then(mapOrder);

export const payWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrderResp>(`/h5/workshop-orders/${id}/pay`).then(mapOrder);

export const cancelWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrderResp>(`/h5/workshop-orders/${id}/cancel`).then(mapOrder);

export const refundWorkshopOrder = (id: number) =>
  request.post<unknown, WorkshopOrderResp>(`/h5/workshop-orders/${id}/refund`).then(mapOrder);

export const fetchMyWorkshopOrders = () =>
  request.get<unknown, WorkshopOrderResp[]>('/h5/workshop-orders/mine').then(hydrateOrders);

export const checkinWorkshopOrder = (id: number, code: string) =>
  request.post<unknown, WorkshopOrderResp>(`/h5/workshop-orders/${id}/checkin`, { code }).then(mapOrder);
