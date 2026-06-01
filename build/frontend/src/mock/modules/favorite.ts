import { mock } from '../index';

const KEY = 'bitdance_mock_server_favorites';
const load = () => JSON.parse(localStorage.getItem(KEY) ?? '[]') as Array<Record<string, unknown>>;
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
