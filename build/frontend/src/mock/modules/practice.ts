import { mock } from '../index';

const KEY = 'bitdance_mock_practices';
const JOIN_KEY = 'bitdance_mock_practice_joins';

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
  status: string;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  createdAt: number;
}

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const CITIES = ['北京', '上海', '广州', '深圳', '杭州'];
const AREAS = ['海淀区', '朝阳区', '浦东新区', '天河区', '南山区'];
const LEVELS = ['零基础', '入门', '初级', '进阶', '高阶'];
const NICK = ['小喵', '舞月', '阿橘', '云朵', '羊羊', '团团', '可可'];

const seed = (): Item[] => {
  const out: Item[] = [];
  const today = new Date();
  for (let i = 1; i <= 30; i += 1) {
    const d = new Date(today);
    d.setDate(today.getDate() + (i % 7));
    out.push({
      id: i,
      title: `${STYLES[i % STYLES.length]} 找搭子`,
      style: STYLES[i % STYLES.length],
      level: LEVELS[i % LEVELS.length],
      date: d.toISOString().slice(0, 10),
      time: ['19:00-21:00', '14:00-16:00', '20:00-22:00'][i % 3],
      city: CITIES[i % CITIES.length],
      area: AREAS[i % AREAS.length],
      location: `${AREAS[i % AREAS.length]}舞星 Studio ${(i % 6) + 1}`,
      capacity: 4 + (i % 4),
      takenCount: i % 3,
      remark: '欢迎零基础，老手也来！',
      status: 'PUBLISHED',
      authorId: 200 + i,
      authorName: NICK[i % NICK.length],
      authorAvatar: '',
      createdAt: Date.now() - i * 3600_000
    });
  }
  return out;
};

const load = (): Item[] => {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Item[]) : seed();
  } catch {
    return seed();
  }
};
const save = (items: Item[]) => localStorage.setItem(KEY, JSON.stringify(items));

const loadJoins = (): number[] => {
  try {
    return JSON.parse(localStorage.getItem(JOIN_KEY) ?? '[]') as number[];
  } catch {
    return [];
  }
};
const saveJoins = (ids: number[]) => localStorage.setItem(JOIN_KEY, JSON.stringify(ids));

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
  '1': CITIES[0],
  '2': CITIES[1],
  '3': CITIES[2],
  '4': CITIES[3],
  '5': CITIES[4]
};

mock('get', /^\/practices$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = load();
  if (p.city) items = items.filter((it) => it.city === p.city);
  if (p.style) items = items.filter((it) => it.style === p.style);
  if (p.level) items = items.filter((it) => it.level === p.level);
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
});

mock('get', /^\/public\/practices$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = load();
  if (p.cityId) items = items.filter((it) => it.city === CITY_BY_ID[String(p.cityId)] || it.city === String(p.cityId));
  if (p.danceStyleId) items = items.filter((it) => it.style === STYLE_BY_ID[String(p.danceStyleId)] || it.style === String(p.danceStyleId));
  if (p.skillLevel) items = items.filter((it) => it.level === p.skillLevel);
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
});

mock('get', /\/practices\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return load().find((it) => it.id === id) ?? null;
});

mock('get', /\/public\/practices\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return load().find((it) => it.id === id) ?? null;
});

mock('post', /\/practices$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const id = Date.now();
  const item: Item = {
    id,
    title: (body.title as string) || `${body.style} 找搭子`,
    style: body.style as string,
    level: body.level as string,
    date: body.date as string,
    time: body.time as string,
    city: body.city as string,
    area: body.area as string,
    location: body.location as string,
    capacity: Number(body.capacity ?? 4),
    takenCount: 0,
    remark: body.remark as string | undefined,
    status: 'PUBLISHED',
    authorId: 999,
    authorName: '我',
    authorAvatar: '',
    createdAt: id
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('post', /^\/h5\/practices$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const id = Date.now();
  const startAt = String(body.startAt ?? new Date().toISOString());
  const endAt = String(body.endAt ?? new Date(Date.now() + 7200_000).toISOString());
  const item: Item = {
    id,
    title: `${STYLE_BY_ID[String(body.danceStyleId)] ?? 'Hiphop'} ${body.skillLevel ?? ''}`.trim(),
    style: STYLE_BY_ID[String(body.danceStyleId)] ?? 'Hiphop',
    level: String(body.skillLevel ?? ''),
    date: startAt.slice(0, 10),
    time: `${startAt.slice(11, 16)}-${endAt.slice(11, 16)}`,
    city: CITY_BY_ID[String(body.cityId)] ?? CITIES[0],
    area: String(body.locationAddress ?? ''),
    location: String(body.locationName ?? ''),
    capacity: Number(body.expectedPeopleMax ?? 4),
    takenCount: 1,
    remark: body.description as string | undefined,
    status: 'PUBLISHED',
    authorId: 999,
    authorName: 'Me',
    authorAvatar: '',
    createdAt: id
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('post', /\/practices\/\d+\/join$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { joined: false, takenCount: 0 };
  const joins = loadJoins();
  if (joins.includes(id)) return { joined: true, takenCount: items[idx].takenCount };
  if (items[idx].takenCount >= items[idx].capacity) return { joined: false, takenCount: items[idx].takenCount };
  items[idx].takenCount += 1;
  if (items[idx].takenCount >= items[idx].capacity) items[idx].status = 'MATCHED';
  joins.push(id);
  save(items);
  saveJoins(joins);
  return { joined: true, takenCount: items[idx].takenCount };
});

mock('post', /\/h5\/practices\/\d+\/join$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx >= 0 && items[idx].takenCount < items[idx].capacity) {
    items[idx].takenCount += 1;
    if (items[idx].takenCount >= items[idx].capacity) items[idx].status = 'MATCHED';
    save(items);
  }
  return { id: Date.now(), practicePostId: id, applicantUserId: 999, joinStatus: idx >= 0 ? 'pending' : 'rejected' };
});

mock('post', /\/practices\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { canceled: false, takenCount: 0 };
  const joins = loadJoins();
  const j = joins.indexOf(id);
  if (j < 0) return { canceled: false, takenCount: items[idx].takenCount };
  items[idx].takenCount = Math.max(0, items[idx].takenCount - 1);
  if (items[idx].status === 'MATCHED') items[idx].status = 'PUBLISHED';
  joins.splice(j, 1);
  save(items);
  saveJoins(joins);
  return { canceled: true, takenCount: items[idx].takenCount };
});

mock('post', /\/h5\/practices\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return { canceled: false, takenCount: 0 };
  items[idx].status = 'CANCELED';
  save(items);
  return items[idx];
});

mock('post', /\/practices\/\d+\/confirm$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  items[idx].status = 'CONFIRMED';
  save(items);
  return items[idx];
});

mock('get', /\/public\/users\/\d+\/practices$/, ({ url }) => {
  const userId = Number(url.split('/').slice(-2)[0]);
  let items = load().filter((item) => item.authorId === userId);
  if (items.length === 0 && userId === 1) {
    items = [
      {
        id: 9301,
        title: '韩舞成品复盘找搭子',
        style: '韩舞',
        level: '零基础',
        date: new Date(Date.now() + 2 * 86400_000).toISOString().slice(0, 10),
        time: '14:00-16:00',
        city: '北京',
        area: '海淀区',
        location: '五道口 Urban Flow',
        capacity: 4,
        takenCount: 2,
        remark: '一起复盘副歌段落，零基础友好。',
        status: 'PUBLISHED',
        authorId: 1,
        authorName: '小李',
        authorAvatar: '',
        createdAt: Date.now() - 7200_000
      }
    ];
  }
  return items;
});
