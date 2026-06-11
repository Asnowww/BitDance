import { mock } from '../index';

const APPEAL_KEY = 'bitdance_mock_appeals';
const COACH_WS_KEY = 'bitdance_mock_coach_workshops';
const REPLY_KEY = 'bitdance_mock_review_replies';

mock('post', /\/coach\/appeals$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const arr = JSON.parse(localStorage.getItem(APPEAL_KEY) ?? '[]');
  const item = {
    id: Date.now(),
    reviewId: Number(body.reviewId),
    reason: (body.reason as string) ?? '',
    evidence: (body.evidence as string) ?? '',
    status: 'PENDING',
    createdAt: Date.now()
  };
  arr.unshift(item);
  localStorage.setItem(APPEAL_KEY, JSON.stringify(arr));
  return item;
});

mock('get', /\/coach\/appeals$/, () => {
  try {
    return JSON.parse(localStorage.getItem(APPEAL_KEY) ?? '[]');
  } catch {
    return [];
  }
});

mock('post', /\/coach\/workshops$/, ({ data }) => {
  const arr = JSON.parse(localStorage.getItem(COACH_WS_KEY) ?? '[]');
  const id = Date.now();
  arr.unshift({ id, status: 'PENDING_REVIEW', body: data, createdAt: id });
  localStorage.setItem(COACH_WS_KEY, JSON.stringify(arr));
  return { id, status: 'PENDING_REVIEW' };
});

mock('get', /\/(?:coach|merchant)\/workshop-orders$/, () => {
  try {
    const orders = JSON.parse(localStorage.getItem('bitdance_mock_workshop_orders') ?? '[]');
    return (orders as Array<Record<string, unknown>>).map((o) => ({
      orderId: o.id,
      workshopId: o.workshopId,
      workshopTitle: o.workshopTitle,
      buyerName: '学员',
      sessionDate: o.sessionDate,
      sessionTime: o.sessionTime,
      amount: o.amount,
      status: o.status,
      checkinCode: o.checkinCode
    }));
  } catch {
    return [];
  }
});

mock('post', /\/(?:coach|merchant)\/workshop-orders\/\d+\/checkin$/, ({ url, data }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const body = (data ?? {}) as Record<string, unknown>;
  try {
    const orders = JSON.parse(localStorage.getItem('bitdance_mock_workshop_orders') ?? '[]') as Array<{
      id: number;
      status: string;
      checkinCode: string;
    }>;
    const idx = orders.findIndex((o) => o.id === id);
    if (idx < 0) return { ok: false };
    if (orders[idx].checkinCode !== body.code) return { ok: false };
    if (orders[idx].status !== 'PAID') return { ok: false };
    orders[idx].status = 'CHECKED_IN';
    localStorage.setItem('bitdance_mock_workshop_orders', JSON.stringify(orders));
    return { ok: true };
  } catch {
    return { ok: false };
  }
});

mock('post', /\/coach\/review-replies$/, ({ data }) => {
  const arr = JSON.parse(localStorage.getItem(REPLY_KEY) ?? '[]');
  arr.push({ ...(data as Record<string, unknown>), createdAt: Date.now() });
  localStorage.setItem(REPLY_KEY, JSON.stringify(arr));
  return { ok: true };
});

mock('get', /\/coach\/dashboard$/, () => {
  try {
    const orders = JSON.parse(localStorage.getItem('bitdance_mock_workshop_orders') ?? '[]') as Array<{
      status: string;
      amount: number;
    }>;
    const reviews = JSON.parse(localStorage.getItem('bitdance_mock_reviews') ?? '[]') as Array<{
      ratingAvg: number;
    }>;
    const replies = JSON.parse(localStorage.getItem(REPLY_KEY) ?? '[]') as Array<unknown>;
    const paid = orders.filter((o) => o.status === 'PAID' || o.status === 'CHECKED_IN');
    const income = paid.reduce((s, o) => s + Number(o.amount), 0);
    const avg = reviews.length ? reviews.reduce((s, r) => s + r.ratingAvg, 0) / reviews.length : 0;
    return {
      monthSessions: paid.length,
      monthStudents: paid.length,
      monthIncome: income,
      pendingReplies: Math.max(0, reviews.length - replies.length),
      ratingAvg: +avg.toFixed(1),
      ratingCount: reviews.length,
      conversionRate: orders.length ? Math.round((paid.length / orders.length) * 100) : 0
    };
  } catch {
    return {
      monthSessions: 0,
      monthStudents: 0,
      monthIncome: 0,
      pendingReplies: 0,
      ratingAvg: 0,
      ratingCount: 0,
      conversionRate: 0
    };
  }
});
