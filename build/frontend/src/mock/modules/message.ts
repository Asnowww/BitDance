import { mock } from '../index';

const KEY = 'bitdance_mock_messages';

interface Item {
  id: number;
  category: 'system' | 'practice' | 'review' | 'trial';
  title: string;
  body: string;
  read: boolean;
  ts: number;
}

const seed = (): Item[] => [
  {
    id: 1,
    category: 'system',
    title: '欢迎来到 BitDance',
    body: '完成首次打卡可获得徽章 🎉',
    read: false,
    ts: Date.now() - 60_000
  },
  {
    id: 2,
    category: 'practice',
    title: '约练有新报名',
    body: '小喵申请加入你的「Hiphop 找搭子」',
    read: false,
    ts: Date.now() - 3600_000
  },
  {
    id: 3,
    category: 'review',
    title: '评价收到点赞',
    body: '你对舞星 Studio 1 的评价收到 3 次点赞',
    read: true,
    ts: Date.now() - 86400_000
  },
  {
    id: 4,
    category: 'trial',
    title: '试听已确认',
    body: '舞室已确认你的试听预约',
    read: false,
    ts: Date.now() - 7200_000
  }
];

const load = (): Item[] => {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Item[]) : seed();
  } catch {
    return seed();
  }
};
const save = (items: Item[]) => localStorage.setItem(KEY, JSON.stringify(items));

mock('get', /\/messages$/, () => load().sort((a, b) => b.ts - a.ts));

mock('post', /\/messages\/\d+\/read$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx >= 0) {
    items[idx].read = true;
    save(items);
  }
  return { read: true };
});

mock('post', /\/messages\/read-all$/, () => {
  const items = load().map((it) => ({ ...it, read: true }));
  save(items);
  return { ok: true };
});
