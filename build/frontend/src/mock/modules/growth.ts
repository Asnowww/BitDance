import { mock } from '../index';

const KEY = 'bitdance_mock_checkins';

interface Item {
  id: number;
  style: string;
  durationMin: number;
  location: string;
  feeling: string;
  visibility: string;
  createdAt: number;
}

const load = (): Item[] => {
  try {
    return JSON.parse(localStorage.getItem(KEY) ?? '[]') as Item[];
  } catch {
    return [];
  }
};
const save = (items: Item[]) => localStorage.setItem(KEY, JSON.stringify(items));

mock('post', /\/growth\/checkins$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const item: Item = {
    id: Date.now(),
    style: body.style as string,
    durationMin: Number(body.durationMin),
    location: body.location as string,
    feeling: (body.feeling as string) || '',
    visibility: (body.visibility as string) || 'public',
    createdAt: Date.now()
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('get', /\/growth\/checkins$/, () => load());

mock('get', /\/growth\/stats$/, () => {
  const items = load();
  const dates = new Set(items.map((it) => new Date(it.createdAt).toDateString()));
  const totalMinutes = items.reduce((s, it) => s + it.durationMin, 0);
  const styles = new Set(items.map((it) => it.style));
  const recent = items.length ? items[0].createdAt : null;
  // 简化的连续天数：基于已打卡日期集合，从今天起向前数
  let streak = 0;
  const cur = new Date();
  while (dates.has(cur.toDateString())) {
    streak += 1;
    cur.setDate(cur.getDate() - 1);
  }
  return {
    totalDays: dates.size,
    totalMinutes,
    totalSessions: items.length,
    styleCount: styles.size,
    streakDays: streak,
    recentAt: recent,
    goalProgress: Math.min(100, Math.round((items.length / 12) * 100))
  };
});

mock('get', /\/growth\/timeline$/, () => {
  const out: Array<Record<string, unknown>> = [];
  load().forEach((c) => {
    out.push({
      id: `checkin-${c.id}`,
      type: 'checkin',
      title: `打卡 ${c.style} ${c.durationMin}min`,
      subtitle: c.location,
      ts: c.createdAt
    });
  });
  try {
    const trials = JSON.parse(localStorage.getItem('bitdance_mock_trial_bookings') ?? '[]') as Array<{
      id: number;
      studioName: string;
      status: string;
      createdAt: number;
      date: string;
      time: string;
    }>;
    trials.forEach((t) => {
      out.push({
        id: `trial-${t.id}`,
        type: 'trial',
        title: `预约试听 · ${t.studioName}`,
        subtitle: `${t.date} ${t.time} · ${t.status}`,
        ts: t.createdAt
      });
    });
  } catch {
    /* ignore */
  }
  try {
    const reviews = JSON.parse(localStorage.getItem('bitdance_mock_reviews') ?? '[]') as Array<{
      id: number;
      authorId: number;
      targetType: string;
      targetId: number;
      text: string;
      createdAt: number;
    }>;
    reviews
      .filter((r) => r.authorId === 999)
      .forEach((r) => {
        out.push({
          id: `review-${r.id}`,
          type: 'review',
          title: `发表评价 · ${r.targetType} #${r.targetId}`,
          subtitle: r.text,
          ts: r.createdAt
        });
      });
  } catch {
    /* ignore */
  }
  return out.sort((a, b) => Number(b.ts) - Number(a.ts));
});
