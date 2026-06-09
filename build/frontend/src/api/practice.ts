import request from '@/utils/request';

export type PracticePostStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'MATCHED'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELED'
  | 'EXPIRED';

export interface PracticePost {
  id: number;
  title: string;
  style: string;
  level: string;
  date: string;
  time: string;
  city: string;
  area: string;
  location: string;
  capacity: number;
  takenCount: number;
  remark?: string;
  status: PracticePostStatus;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: number;
  distanceMeters?: number | null;
}

export interface PracticeListQuery {
  city?: string;
  style?: string;
  level?: string;
  scope?: 'nearby' | 'city';
  sort?: 'time' | 'distance';
  longitude?: number;
  latitude?: number;
  page?: number;
  pageSize?: number;
}

export interface PracticeListResp {
  list: PracticePost[];
  page: number;
  pageSize: number;
  total: number;
}

export interface PracticeJoinRequest {
  id: number;
  practicePostId: number;
  applicantUserId: number;
  joinStatus: 'pending' | 'accepted' | 'rejected' | 'canceled';
  joinMessage?: string | null;
  actedByUserId?: number | null;
  actedAt?: string | null;
  createdAt?: string | null;
}

export interface PracticeCreateBody {
  title: string;
  style: string;
  level: string;
  date: string;
  time: string;
  city: string;
  area: string;
  location: string;
  capacity: number;
  remark?: string;
  idempotencyToken: string;
}

interface BackendPracticePost {
  id: number;
  creatorUserId: number;
  danceStyleId: number;
  studioId?: number;
  cityId: number;
  locationName: string;
  locationAddress?: string;
  longitude?: number;
  latitude?: number;
  skillLevel?: string;
  expectedPeopleMin?: number;
  expectedPeopleMax?: number;
  currentPeopleCount?: number;
  startAt: string;
  endAt: string;
  expiresAt?: string;
  postStatus: string;
  description?: string;
  createdAt?: string;
  distanceMeters?: number | null;
}

interface BackendPracticeListResp {
  list: BackendPracticePost[];
  page: number;
  pageSize: number;
  total: number;
}

interface BackendJoinRequest {
  id: number;
  practicePostId: number;
  applicantUserId: number;
  joinStatus: string;
}

const styleIds: Record<string, number> = {
  Hiphop: 1,
  Jazz: 2,
  Breaking: 3,
  Locking: 4,
  Popping: 5,
  Kpop: 6,
  Waacking: 7
};

const styleNames: Record<number, string> = Object.fromEntries(
  Object.entries(styleIds).map(([name, id]) => [id, name])
) as Record<number, string>;

const cityIds: Record<string, number> = {
  北京: 1,
  上海: 2,
  广州: 3,
  深圳: 4,
  杭州: 5
};

const cityNames: Record<number, string> = Object.fromEntries(
  Object.entries(cityIds).map(([name, id]) => [id, name])
) as Record<number, string>;

const normalizeStatus = (status?: string): PracticePostStatus =>
  ((status || 'published').toUpperCase() as PracticePostStatus);

const parseRange = (time: string) => {
  const matched = time.match(/(\d{1,2}:\d{2})\s*[-–—]\s*(\d{1,2}:\d{2})/);
  return matched ? [matched[1], matched[2]] : ['15:00', '17:00'];
};

const toIsoAt = (date: string, time: string) => {
  const [hour, minute] = time.split(':').map(Number);
  const d = new Date(`${date}T00:00:00+08:00`);
  d.setHours(hour, minute, 0, 0);
  return d.toISOString();
};

const formatDate = (iso?: string) => iso ? iso.slice(0, 10) : '';

const formatTime = (start?: string, end?: string) => {
  if (!start || !end) return '';
  return `${start.slice(11, 16)}-${end.slice(11, 16)}`;
};

const toPracticePost = (raw: BackendPracticePost | PracticePost): PracticePost => {
  if ('postStatus' in raw) {
    const capacity = raw.expectedPeopleMax ?? raw.expectedPeopleMin ?? 4;
    const takenCount = raw.currentPeopleCount ?? 1;
    const style = styleNames[raw.danceStyleId] ?? `舞种 #${raw.danceStyleId}`;
    const city = cityNames[raw.cityId] ?? `城市 #${raw.cityId}`;
    return {
      id: Number(raw.id),
      title: `${style} ${raw.skillLevel || ''}`.trim() || `约练 #${raw.id}`,
      style,
      level: raw.skillLevel || '不限',
      date: formatDate(raw.startAt),
      time: formatTime(raw.startAt, raw.endAt),
      city,
      area: raw.locationAddress || '',
      location: raw.locationName,
      capacity,
      takenCount,
      remark: raw.description,
      status: normalizeStatus(raw.postStatus),
      authorId: raw.creatorUserId,
      authorName: `用户 ${raw.creatorUserId}`,
      authorAvatar: '',
      createdAt: raw.createdAt ? new Date(raw.createdAt).getTime() : Date.now(),
      distanceMeters: raw.distanceMeters ?? null
    };
  }
  return raw;
};

const resolveDate = (date: string) => {
  const picked = new Date(`${date}T00:00:00+08:00`);
  const today = new Date();
  if (Number.isNaN(picked.getTime()) || picked <= today) {
    const next = new Date();
    next.setDate(next.getDate() + 3);
    return next.toISOString().slice(0, 10);
  }
  return date;
};

const toCreatePayload = (body: PracticeCreateBody) => {
  const date = resolveDate(body.date);
  const [startTime, endTime] = parseRange(body.time);
  return {
    danceStyleId: styleIds[body.style] ?? 1,
    cityId: cityIds[body.city] ?? 1,
    locationName: body.location,
    locationAddress: body.area,
    skillLevel: body.level,
    expectedPeopleMin: 2,
    expectedPeopleMax: body.capacity,
    startAt: toIsoAt(date, startTime),
    endAt: toIsoAt(date, endTime),
    description: body.remark || body.title
  };
};

const toSquareParams = (q: PracticeListQuery) => ({
  cityId: q.city ? cityIds[q.city] : undefined,
  danceStyleId: q.style ? styleIds[q.style] : undefined,
  skillLevel: q.level,
  scope: q.scope,
  sort: q.sort,
  longitude: q.longitude,
  latitude: q.latitude,
  page: q.page,
  pageSize: q.pageSize
});

export const fetchPractices = (q: PracticeListQuery) =>
  request
    .get<unknown, BackendPracticeListResp | PracticeListResp>('/public/practices', { params: toSquareParams(q) })
    .then((resp) => ({
      ...resp,
      list: resp.list.map((item) => toPracticePost(item as BackendPracticePost | PracticePost))
    }));

export const fetchPracticeDetail = (id: number) =>
  request
    .get<unknown, BackendPracticePost | PracticePost>(`/public/practices/${id}`)
    .then(toPracticePost);

export const createPractice = (body: PracticeCreateBody) =>
  request
    .post<unknown, BackendPracticePost | PracticePost>('/h5/practices', toCreatePayload(body))
    .then(toPracticePost);

export const joinPractice = (id: number) =>
  request
    .post<unknown, BackendJoinRequest | { joined: boolean; takenCount: number }>(`/h5/practices/${id}/join`, {})
    .then((resp) => ('joinStatus' in resp
      ? { joined: resp.joinStatus === 'pending' || resp.joinStatus === 'accepted', takenCount: 0 }
      : resp));

export const cancelJoin = (id: number) =>
  request
    .post<unknown, BackendPracticePost | { canceled: boolean; takenCount: number }>(`/h5/practices/${id}/cancel`)
    .then((resp) => ('postStatus' in resp ? { canceled: resp.postStatus === 'canceled', takenCount: 0 } : resp));

export const confirmPractice = (id: number) =>
  fetchPracticeDetail(id);

export const fetchPracticeRecommendations = (q: PracticeListQuery = {}) =>
  request
    .get<unknown, Array<BackendPracticePost | PracticePost>>('/h5/practices/recommendations', {
      params: { ...toSquareParams(q), limit: q.pageSize ?? 10 }
    })
    .then((list) => list.map((item) => toPracticePost(item)));

export const fetchMyPractices = () =>
  request
    .get<unknown, Array<BackendPracticePost | PracticePost>>('/h5/practices/mine')
    .then((list) => list.map((item) => toPracticePost(item)));

export const fetchMyPracticeRequests = () =>
  request.get<unknown, PracticeJoinRequest[]>('/h5/practice-requests/mine');

export const fetchPracticeRequests = (practiceId: number) =>
  request.get<unknown, PracticeJoinRequest[]>(`/h5/practices/${practiceId}/requests`);

export const acceptPracticeRequest = (requestId: number) =>
  request.post<unknown, PracticeJoinRequest>(`/h5/practice-requests/${requestId}/accept`);

export const rejectPracticeRequest = (requestId: number) =>
  request.post<unknown, PracticeJoinRequest>(`/h5/practice-requests/${requestId}/reject`);

export const cancelPracticeRequest = (requestId: number) =>
  request.post<unknown, PracticeJoinRequest>(`/h5/practice-requests/${requestId}/cancel`);

export interface GroupClassIntent {
  id: number;
  creatorUserId: number;
  studioId: number;
  danceStyleId: number;
  preferredTimeNote?: string | null;
  targetPeopleCount: number;
  currentPeopleCount: number;
  intentStatus: 'collecting' | 'matched' | 'closed' | 'canceled';
  joinedByMe?: boolean;
  createdAt?: string;
}

export interface CreateGroupClassIntentBody {
  studioId: number;
  danceStyleId: number;
  preferredTimeNote?: string;
  targetPeopleCount: number;
}

export const fetchGroupClassIntents = (params: { studioId?: number; danceStyleId?: number; limit?: number } = {}) =>
  request.get<unknown, GroupClassIntent[]>('/public/group-class-intents', { params });

export const fetchMyGroupClassIntents = () =>
  request.get<unknown, GroupClassIntent[]>('/h5/group-class-intents/mine');

export const createGroupClassIntent = (body: CreateGroupClassIntentBody) =>
  request.post<unknown, GroupClassIntent>('/h5/group-class-intents', body);

export const joinGroupClassIntent = (id: number) =>
  request.post<unknown, GroupClassIntent>(`/h5/group-class-intents/${id}/join`);

export const cancelGroupClassIntent = (id: number) =>
  request.post<unknown, GroupClassIntent>(`/h5/group-class-intents/${id}/cancel`);
