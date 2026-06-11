import { mock } from '../index';

const POST_KEY = 'bitdance_mock_practices_m4_v2';
const REQUEST_KEY = 'bitdance_mock_practice_requests_m4_v2';
const GROUP_KEY = 'bitdance_mock_group_class_intents_m4_v2';
const CONFIRM_KEY = 'bitdance_mock_practice_completion_confirms_m4_v2';
const RATING_KEY = 'bitdance_mock_practice_ratings';
const USER_ID = 999;
const TEST_PRACTICE_ID = 100014;
const TEST_PEER_ID = 100001;

type PracticeStatus = 'PUBLISHED' | 'MATCHED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELED' | 'EXPIRED';
type JoinStatus = 'pending' | 'accepted' | 'rejected' | 'canceled';
type IntentStatus = 'collecting' | 'matched' | 'closed' | 'canceled';

interface Item {
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
  status: PracticeStatus;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: number;
  distanceMeters?: number | null;
  participants?: PracticeParticipant[];
  completionConfirmedByMe?: boolean;
  allCompletedConfirmed?: boolean;
  ratingTargets?: PracticeParticipant[];
  ratedUserIds?: number[];
}

interface PracticeParticipant {
  userId: number;
  role: 'creator' | 'participant';
  completionConfirmed?: boolean;
  ratedByMe?: boolean;
}

interface JoinRequest {
  id: number;
  practicePostId: number;
  applicantUserId: number;
  joinStatus: JoinStatus;
  joinMessage?: string;
  actedByUserId?: number | null;
  actedAt?: string | null;
  createdAt: string;
}

interface GroupClassIntent {
  id: number;
  creatorUserId: number;
  studioId: number;
  danceStyleId: number;
  preferredTimeNote?: string | null;
  targetPeopleCount: number;
  currentPeopleCount: number;
  intentStatus: IntentStatus;
  joinedByMe?: boolean;
  createdAt: string;
}

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const CITIES = ['北京', '上海', '广州', '深圳', '杭州'];
const AREAS = ['海淀区', '朝阳区', '浦东新区', '天河区', '南山区'];
const LEVELS = ['零基础', '入门', '初级', '进阶', '高阶'];
const NICK = ['小喵', '舞月', '阿橘', '云朵', '羊羊', '团团', '可可'];

const STYLE_BY_ID: Record<string, string> = {
  '1': 'Hiphop',
  '2': 'Jazz',
  '3': 'Breaking',
  '4': 'Locking',
  '5': 'Popping',
  '6': 'Kpop',
  '7': 'Waacking'
};

const CITY_BY_ID: Record<string, string> = {
  '1': '北京',
  '2': '上海',
  '3': '广州',
  '4': '深圳',
  '5': '杭州'
};

const seed = (): Item[] => {
  const out: Item[] = [];
  const today = new Date();
  for (let i = 1; i <= 24; i += 1) {
    const d = new Date(today);
    d.setDate(today.getDate() + (i % 8));
    const style = STYLES[i % STYLES.length];
    const area = AREAS[i % AREAS.length];
    out.push({
      id: i,
      title: `${style} 找搭子`,
      style,
      level: LEVELS[i % LEVELS.length],
      date: d.toISOString().slice(0, 10),
      time: ['19:00-21:00', '14:00-16:00', '20:00-22:00'][i % 3],
      city: CITIES[i % CITIES.length],
      area,
      location: `${area} 舞星 Studio ${(i % 6) + 1}`,
      capacity: 4 + (i % 4),
      takenCount: 1 + (i % 3),
      remark: '欢迎守时、愿意互相录视频复盘的舞友一起练。',
      status: i % 11 === 0 ? 'COMPLETED' : 'PUBLISHED',
      authorId: 200 + i,
      authorName: NICK[i % NICK.length],
      authorAvatar: '',
      createdAt: Date.now() - i * 3600_000,
      distanceMeters: 400 + i * 180
    });
  }
  return out;
};

const loadJson = <T>(key: string, fallback: T): T => {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
};

const saveJson = (key: string, value: unknown) => localStorage.setItem(key, JSON.stringify(value));

const ensureTestPractice = (items: Item[]) => {
  const today = new Date();
  const date = today.toISOString().slice(0, 10);
  const testItem: Item = {
    id: TEST_PRACTICE_ID,
    title: 'M4/M5 互评流程测试约练',
    style: 'Hiphop',
    level: 'beginner',
    date,
    time: '10:00-12:00',
    city: '北京',
    area: 'M4/M5互评流程测试地址',
    location: 'BitDance测试舞室',
    capacity: 2,
    takenCount: 2,
    remark: 'M4/M5 test mutual rating seed - confirmed practice ready for completion confirmation',
    status: 'CONFIRMED',
    authorId: USER_ID,
    authorName: '我',
    authorAvatar: '',
    createdAt: Date.now() - 3 * 3600_000,
    distanceMeters: 320
  };
  const existing = items.find((item) => item.id === TEST_PRACTICE_ID);
  if (existing) {
    Object.assign(existing, testItem);
  } else {
    items.unshift(testItem);
  }
  saveJson(POST_KEY, items);
  return items;
};

const ensureTestRequest = (items: JoinRequest[]) => {
  const existing = items.find((item) => item.practicePostId === TEST_PRACTICE_ID && item.applicantUserId === TEST_PEER_ID);
  const testReq: JoinRequest = {
    id: TEST_PRACTICE_ID + 1,
    practicePostId: TEST_PRACTICE_ID,
    applicantUserId: TEST_PEER_ID,
    joinStatus: 'accepted',
    joinMessage: 'M4/M5 mutual rating seed accepted participant',
    actedByUserId: USER_ID,
    actedAt: new Date().toISOString(),
    createdAt: new Date(Date.now() - 2 * 3600_000).toISOString()
  };
  if (existing) {
    Object.assign(existing, testReq, { id: existing.id });
  } else {
    items.unshift(testReq);
  }
  saveJson(REQUEST_KEY, items);
  return items;
};

const load = () => ensureTestPractice(loadJson<Item[]>(POST_KEY, seed()));
const save = (items: Item[]) => saveJson(POST_KEY, items);
const loadRequests = () => ensureTestRequest(loadJson<JoinRequest[]>(REQUEST_KEY, []));
const saveRequests = (items: JoinRequest[]) => saveJson(REQUEST_KEY, items);
const loadConfirms = () => loadJson<Record<string, number[]>>(CONFIRM_KEY, {});
const saveConfirms = (items: Record<string, number[]>) => saveJson(CONFIRM_KEY, items);
const loadRatings = () => loadJson<Array<{ practiceId: number; toUserId: number }>>(RATING_KEY, []);

const enrich = (post: Item | undefined | null): Item | null => {
  if (!post) return null;
  const accepted = loadRequests()
    .filter((req) => req.practicePostId === post.id && req.joinStatus === 'accepted')
    .map((req) => req.applicantUserId);
  const participants: PracticeParticipant[] = [
    { userId: post.authorId, role: 'creator' },
    ...accepted.filter((userId) => userId !== post.authorId).map((userId) => ({ userId, role: 'participant' as const }))
  ];
  const confirmedIds = new Set(loadConfirms()[String(post.id)] ?? []);
  const ratedUserIds = loadRatings()
    .filter((rating) => rating.practiceId === post.id)
    .map((rating) => Number(rating.toUserId));
  const participatedByMe = participants.some((item) => item.userId === USER_ID);
  return {
    ...post,
    participants: participants.map((item) => ({
      ...item,
      completionConfirmed: confirmedIds.has(item.userId),
      ratedByMe: ratedUserIds.includes(item.userId)
    })),
    completionConfirmedByMe: participatedByMe && confirmedIds.has(USER_ID),
    allCompletedConfirmed: participants.length > 0 && participants.every((item) => confirmedIds.has(item.userId)),
    ratingTargets: participatedByMe ? participants.filter((item) => item.userId !== USER_ID) : [],
    ratedUserIds
  };
};

const normalizeLevel = (level?: unknown) => {
  const v = String(level ?? '');
  return ({ beginner: '入门', intermediate: '中级', advanced: '进阶' } as Record<string, string>)[v] ?? v;
};

const parseRange = (value?: unknown) => {
  const text = String(value ?? '15:00-17:00');
  const matched = text.match(/(\d{1,2}:\d{2})\s*[-~]\s*(\d{1,2}:\d{2})/);
  return matched ? [matched[1], matched[2]] : ['15:00', '17:00'];
};

const pad2 = (value: number) => String(value).padStart(2, '0');

const localDate = (iso: string) => {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso.slice(0, 10);
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
};

const localTime = (iso: string) => {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso.slice(11, 16);
  return `${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
};

const dateOf = (value?: unknown) => {
  const raw = String(value ?? '');
  if (raw.length >= 10) return raw.slice(0, 10);
  return new Date(Date.now() + 2 * 86400_000).toISOString().slice(0, 10);
};

const refreshCountAndStatus = (post: Item, requests = loadRequests()) => {
  const acceptedCount = requests.filter((r) => r.practicePostId === post.id && r.joinStatus === 'accepted').length;
  post.takenCount = Math.max(1, post.authorId === USER_ID ? 1 : post.takenCount, 1 + acceptedCount);
  if (post.status !== 'COMPLETED' && post.status !== 'CANCELED' && post.takenCount >= post.capacity) {
    post.status = 'MATCHED';
  }
};

const listWithFilters = (params?: unknown) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = load().filter((item) => item.status !== 'CANCELED');
  if (p.city) items = items.filter((it) => it.city === p.city);
  if (p.cityId) items = items.filter((it) => it.city === CITY_BY_ID[String(p.cityId)] || it.city === String(p.cityId));
  if (p.style) items = items.filter((it) => it.style === p.style);
  if (p.danceStyleId) items = items.filter((it) => it.style === STYLE_BY_ID[String(p.danceStyleId)] || it.style === String(p.danceStyleId));
  if (p.level || p.skillLevel) {
    const wanted = normalizeLevel(p.level ?? p.skillLevel);
    items = items.filter((it) => it.level === wanted || it.level === String(p.level ?? p.skillLevel));
  }
  if (p.sort === 'distance') items = items.slice().sort((a, b) => (a.distanceMeters ?? 99999) - (b.distanceMeters ?? 99999));
  return items;
};

const paged = (items: Item[], params?: unknown) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? p.limit ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
};

const createItem = (body: Record<string, unknown>): Item => {
  const items = load();
  const id = Date.now();
  const startAt = body.startAt as string | undefined;
  const endAt = body.endAt as string | undefined;
  const date = startAt ? localDate(startAt) : dateOf(body.date);
  const [startTime, endTime] = startAt && endAt
    ? [localTime(startAt), localTime(endAt)]
    : parseRange(body.time);
  const style = STYLE_BY_ID[String(body.danceStyleId)] ?? String(body.style ?? 'Hiphop');
  const item: Item = {
    id,
    title: String(body.title ?? `${style} ${normalizeLevel(body.skillLevel ?? body.level)}约练`),
    style,
    level: normalizeLevel(body.skillLevel ?? body.level),
    date,
    time: `${startTime}-${endTime}`,
    city: CITY_BY_ID[String(body.cityId)] ?? String(body.city ?? '北京'),
    area: String(body.locationAddress ?? body.area ?? '朝阳区'),
    location: String(body.locationName ?? body.location ?? 'Urban Flow 舞室'),
    capacity: Number(body.expectedPeopleMax ?? body.capacity ?? 4),
    takenCount: 1,
    remark: String(body.description ?? body.remark ?? ''),
    status: 'PUBLISHED',
    authorId: USER_ID,
    authorName: '我',
    authorAvatar: '',
    createdAt: id,
    distanceMeters: 320
  };
  items.unshift(item);
  save(items);
  return item;
};

mock('get', /^\/practices$/, ({ params }) => paged(listWithFilters(params), params));
mock('get', /^\/public\/practices$/, ({ params }) => paged(listWithFilters(params), params));

mock('get', /\/public\/practices\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return enrich(load().find((it) => it.id === id));
});

mock('get', /\/practices\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return enrich(load().find((it) => it.id === id));
});

mock('get', /\/h5\/practices\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return enrich(load().find((it) => it.id === id));
});

mock('post', /^\/practices$/, ({ data }) => createItem(data as Record<string, unknown>));
mock('post', /^\/h5\/practices$/, ({ data }) => createItem(data as Record<string, unknown>));

mock('get', /^\/h5\/practices\/mine$/, () => load().filter((item) => item.authorId === USER_ID).map((item) => enrich(item)));

mock('get', /^\/h5\/practice-requests\/mine$/, () =>
  loadRequests().filter((item) => item.applicantUserId === USER_ID)
);

mock('get', /\/h5\/practices\/\d+\/requests$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  return loadRequests().filter((item) => item.practicePostId === id);
});

mock('post', /\/h5\/practices\/\d+\/join$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const post = items.find((it) => it.id === id);
  if (!post || post.authorId === USER_ID || post.status === 'CANCELED') {
    return { id: Date.now(), practicePostId: id, applicantUserId: USER_ID, joinStatus: 'rejected' };
  }
  const requests = loadRequests();
  const existing = requests.find((r) => r.practicePostId === id && r.applicantUserId === USER_ID && r.joinStatus !== 'canceled');
  if (existing) return existing;
  const req: JoinRequest = {
    id: Date.now(),
    practicePostId: id,
    applicantUserId: USER_ID,
    joinStatus: post.takenCount < post.capacity ? 'pending' : 'rejected',
    joinMessage: '我想一起练，可以准时到。',
    actedByUserId: null,
    actedAt: null,
    createdAt: new Date().toISOString()
  };
  requests.unshift(req);
  saveRequests(requests);
  return req;
});

mock('post', /\/h5\/practice-requests\/\d+\/accept$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const requests = loadRequests();
  const req = requests.find((item) => item.id === id);
  if (req) {
    req.joinStatus = 'accepted';
    req.actedByUserId = USER_ID;
    req.actedAt = new Date().toISOString();
    saveRequests(requests);
    const items = load();
    const post = items.find((item) => item.id === req.practicePostId);
    if (post) {
      refreshCountAndStatus(post, requests);
      save(items);
    }
  }
  return req ?? null;
});

mock('post', /\/h5\/practice-requests\/\d+\/reject$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const requests = loadRequests();
  const req = requests.find((item) => item.id === id);
  if (req) {
    req.joinStatus = 'rejected';
    req.actedByUserId = USER_ID;
    req.actedAt = new Date().toISOString();
    saveRequests(requests);
  }
  return req ?? null;
});

mock('post', /\/h5\/practice-requests\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const requests = loadRequests();
  const req = requests.find((item) => item.id === id);
  if (req) {
    req.joinStatus = 'canceled';
    req.actedAt = new Date().toISOString();
    saveRequests(requests);
  }
  return req ?? null;
});

mock('post', /\/h5\/practices\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const post = items.find((item) => item.id === id);
  if (post && post.authorId === USER_ID) {
    post.status = 'CANCELED';
    save(items);
  }
  return post ?? { canceled: false, takenCount: 0 };
});

mock('post', /\/h5\/practices\/\d+\/complete-confirm$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const post = items.find((item) => item.id === id);
  if (!post) return null;
  const confirms = loadConfirms();
  const list = new Set(confirms[String(id)] ?? []);
  list.add(USER_ID);
  confirms[String(id)] = Array.from(list);
  saveConfirms(confirms);
  const full = enrich(post);
  if (full?.allCompletedConfirmed) {
    post.status = 'COMPLETED';
    save(items);
  }
  return enrich(post);
});

mock('get', /^\/h5\/practices\/recommendations$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  return listWithFilters(p)
    .filter((item) => item.authorId !== USER_ID && item.status === 'PUBLISHED' && item.takenCount < item.capacity)
    .sort((a, b) => (a.distanceMeters ?? 99999) - (b.distanceMeters ?? 99999))
    .slice(0, Number(p.limit ?? 10));
});

mock('get', /\/public\/users\/\d+\/practices$/, ({ url }) => {
  const userId = Number(url.split('/').slice(-2)[0]);
  return load().filter((item) => item.authorId === userId);
});

const seedGroups = (): GroupClassIntent[] => [
  {
    id: 5001,
    creatorUserId: 201,
    studioId: 1,
    danceStyleId: 1,
    preferredTimeNote: '周六下午 14:00，零基础友好班',
    targetPeopleCount: 5,
    currentPeopleCount: 3,
    intentStatus: 'collecting',
    joinedByMe: false,
    createdAt: new Date(Date.now() - 3600_000).toISOString()
  },
  {
    id: 5002,
    creatorUserId: 202,
    studioId: 2,
    danceStyleId: 2,
    preferredTimeNote: '工作日晚上 19:30，想拼 Jazz 基础',
    targetPeopleCount: 4,
    currentPeopleCount: 4,
    intentStatus: 'matched',
    joinedByMe: true,
    createdAt: new Date(Date.now() - 7200_000).toISOString()
  }
];

const loadGroups = () => loadJson<GroupClassIntent[]>(GROUP_KEY, seedGroups());
const saveGroups = (items: GroupClassIntent[]) => saveJson(GROUP_KEY, items);

mock('get', /^\/public\/group-class-intents$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = loadGroups();
  if (p.studioId) items = items.filter((item) => item.studioId === Number(p.studioId));
  if (p.danceStyleId) items = items.filter((item) => item.danceStyleId === Number(p.danceStyleId));
  return items.slice(0, Number(p.limit ?? 30));
});

mock('get', /^\/h5\/group-class-intents\/mine$/, () =>
  loadGroups().filter((item) => item.creatorUserId === USER_ID || item.joinedByMe)
);

mock('post', /^\/h5\/group-class-intents$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = loadGroups();
  const item: GroupClassIntent = {
    id: Date.now(),
    creatorUserId: USER_ID,
    studioId: Number(body.studioId ?? 1),
    danceStyleId: Number(body.danceStyleId ?? 1),
    preferredTimeNote: String(body.preferredTimeNote ?? ''),
    targetPeopleCount: Number(body.targetPeopleCount ?? 4),
    currentPeopleCount: 1,
    intentStatus: 'collecting',
    joinedByMe: true,
    createdAt: new Date().toISOString()
  };
  items.unshift(item);
  saveGroups(items);
  return item;
});

mock('post', /\/h5\/group-class-intents\/\d+\/join$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadGroups();
  const item = items.find((it) => it.id === id);
  if (item && item.intentStatus === 'collecting' && !item.joinedByMe) {
    item.joinedByMe = true;
    item.currentPeopleCount += 1;
    if (item.currentPeopleCount >= item.targetPeopleCount) item.intentStatus = 'matched';
    saveGroups(items);
  }
  return item ?? null;
});

mock('post', /\/h5\/group-class-intents\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = loadGroups();
  const item = items.find((it) => it.id === id);
  if (item?.joinedByMe) {
    item.joinedByMe = false;
    item.currentPeopleCount = Math.max(1, item.currentPeopleCount - 1);
    if (item.intentStatus === 'matched') item.intentStatus = 'collecting';
    saveGroups(items);
  }
  return item ?? null;
});
