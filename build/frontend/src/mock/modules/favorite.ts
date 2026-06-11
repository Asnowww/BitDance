import { mock } from '../index';

const KEY = 'bitdance_mock_server_favorites';
const seed = () => {
  const raw = localStorage.getItem(KEY);
  if (raw) {
    try {
      const existing = JSON.parse(raw) as unknown[];
      if (Array.isArray(existing) && existing.length > 0) return;
    } catch {
      // Re-seed malformed local demo data so the favorites page remains usable.
    }
  }
  localStorage.setItem(KEY, JSON.stringify([
    {
      id: 3001,
      targetType: 'studio',
      targetId: 1,
      createdAt: new Date(Date.now() - 86400000 * 3).toISOString(),
      card: {
        title: '舞星 Studio 1',
        subtitle: '北京海淀区学院路 1 号 · 零基础友好',
        path: '/studio/1',
        actionText: '预约试听'
      }
    },
    {
      id: 3002,
      targetType: 'workshop',
      targetId: 1,
      createdAt: new Date(Date.now() - 86400000).toISOString(),
      card: {
        title: 'Hiphop Groove Workshop',
        subtitle: '北京 · 周末强化课 · 可报名',
        path: '/workshop/1',
        actionText: '查看活动'
      }
    }
  ]));
};

const load = () => {
  seed();
  return JSON.parse(localStorage.getItem(KEY) ?? '[]') as Array<Record<string, unknown>>;
};
const save = (items: Array<Record<string, unknown>>) => localStorage.setItem(KEY, JSON.stringify(items));

mock('post', /\/h5\/favorites$/, ({ data }) => {
  const body = data as { targetType: string; targetId: number };
  const items = load();
  const index = items.findIndex((item) => item.targetType === body.targetType && item.targetId === body.targetId);
  if (index >= 0) {
    items.splice(index, 1);
    save(items);
    return { favored: false };
  }
  items.unshift({ id: Date.now(), ...body, createdAt: new Date().toISOString() });
  save(items);
  return { favored: true };
});

mock('get', /\/h5\/favorites$/, ({ params }) => {
  const targetType = (params as Record<string, string> | undefined)?.targetType;
  return targetType ? load().filter((item) => item.targetType === targetType) : load();
});

mock('get', /\/h5\/favorites\/check$/, ({ params }) => {
  const query = params as Record<string, unknown>;
  return { favored: load().some((item) => item.targetType === query.targetType && item.targetId === Number(query.targetId)) };
});
