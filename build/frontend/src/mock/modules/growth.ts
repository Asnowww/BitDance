import { mock } from '../index';

const KEY = 'bitdance_mock_checkins';

interface Item {
  id: number;
  userId?: number;
  danceStyleId?: number | null;
  studioId?: number | null;
  courseScheduleId?: number | null;
  practicePostId?: number | null;
  durationMinutes?: number;
  feelingText?: string;
  isPublic?: boolean;
  checkinAt?: string;
  style?: string;
  durationMin?: number;
  location?: string;
  feeling?: string;
  visibility?: string;
  createdAt?: number;
}

const load = (): Item[] => {
  try {
    return JSON.parse(localStorage.getItem(KEY) ?? '[]') as Item[];
  } catch {
    return [];
  }
};
const save = (items: Item[]) => localStorage.setItem(KEY, JSON.stringify(items));

const minutesOf = (it: Item) => Number(it.durationMinutes ?? it.durationMin ?? 0);
const timeOf = (it: Item) => new Date(it.checkinAt ?? it.createdAt ?? Date.now());

mock('post', /\/growth\/checkins$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const now = new Date();
  const duration = Number(body.durationMinutes ?? body.durationMin ?? 0);
  const item: Item = {
    id: Date.now(),
    userId: 999,
    danceStyleId: (body.danceStyleId as number | undefined) ?? null,
    studioId: (body.studioId as number | undefined) ?? null,
    courseScheduleId: (body.courseScheduleId as number | undefined) ?? null,
    practicePostId: (body.practicePostId as number | undefined) ?? null,
    durationMinutes: duration,
    feelingText: ((body.feelingText ?? body.feeling) as string) || '',
    isPublic: (body.isPublic as boolean | undefined) ?? body.visibility !== 'private',
    checkinAt: (body.checkinAt as string | undefined) ?? now.toISOString(),
    style: (body.style as string | undefined) ?? `舞种${body.danceStyleId ?? ''}`,
    durationMin: duration,
    location: (body.location as string | undefined) ?? '',
    feeling: ((body.feelingText ?? body.feeling) as string) || '',
    visibility: ((body.isPublic as boolean | undefined) ?? true) ? 'public' : 'private',
    createdAt: now.getTime()
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('get', /\/growth\/checkins$/, () => load());

mock('get', /\/growth\/stats$/, () => {
  const items = load();
  const now = new Date();
  const weekStart = new Date(now);
  weekStart.setDate(now.getDate() - ((now.getDay() + 6) % 7));
  weekStart.setHours(0, 0, 0, 0);
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
  const dates = new Set(items.map((it) => timeOf(it).toDateString()));
  const totalMinutes = items.reduce((sum, it) => sum + minutesOf(it), 0);
  const styles = new Set(items.map((it) => it.danceStyleId ?? it.style).filter(Boolean));
  const courses = new Set(items.map((it) => it.courseScheduleId).filter(Boolean));
  const inWeek = items.filter((it) => timeOf(it) >= weekStart && timeOf(it) <= now);
  const inMonth = items.filter((it) => timeOf(it) >= monthStart && timeOf(it) <= now);

  let streak = 0;
  const cursor = new Date();
  while (dates.has(cursor.toDateString())) {
    streak += 1;
    cursor.setDate(cursor.getDate() - 1);
  }

  const recent = items.length ? timeOf(items[0]) : null;
  return {
    totalSessions: items.length,
    totalMinutes,
    totalDays: dates.size,
    styleCount: styles.size,
    streakDays: streak,
    lastCheckinAt: recent ? recent.toISOString() : null,
    courseCount: courses.size,
    weekSessions: inWeek.length,
    weekMinutes: inWeek.reduce((sum, it) => sum + minutesOf(it), 0),
    monthSessions: inMonth.length,
    monthMinutes: inMonth.reduce((sum, it) => sum + minutesOf(it), 0),
    recentAt: recent ? recent.getTime() : null,
    goalProgress: Math.min(100, Math.round((items.length / 12) * 100))
  };
});

const WORK_KEY = 'bitdance_mock_growth_works';
const GOAL_KEY = 'bitdance_mock_growth_goal';

interface Work {
  id: number;
  type: 'image' | 'video';
  title: string;
  description: string;
  style?: string;
  visibility: 'public' | 'private' | 'friends';
  createdAt: number;
}

const loadWorks = (): Work[] => {
  try {
    return JSON.parse(localStorage.getItem(WORK_KEY) ?? '[]') as Work[];
  } catch {
    return [];
  }
};
const saveWorks = (items: Work[]) => localStorage.setItem(WORK_KEY, JSON.stringify(items));

mock('get', /\/growth\/works$/, () => loadWorks());

mock('post', /\/growth\/works$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = loadWorks();
  const item: Work = {
    id: Date.now(),
    type: (body.type as 'image' | 'video') ?? 'image',
    title: (body.title as string) ?? '',
    description: (body.description as string) ?? '',
    style: body.style as string | undefined,
    visibility: (body.visibility as Work['visibility']) ?? 'public',
    createdAt: Date.now()
  };
  items.unshift(item);
  saveWorks(items);
  return item;
});

mock('delete', /\/growth\/works\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const items = loadWorks();
  const next = items.filter((w) => w.id !== id);
  saveWorks(next);
  return { deleted: items.length !== next.length };
});

const readGoal = () => {
  try {
    const raw = localStorage.getItem(GOAL_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

mock('get', /\/growth\/goal$/, readGoal);
mock('get', /\/growth\/goals\/active$/, readGoal);

mock('put', /\/growth\/goal$/, ({ data }) => {
  localStorage.setItem(GOAL_KEY, JSON.stringify(data));
  return data;
});

mock('put', /\/growth\/goals\/active$/, ({ data }) => {
  localStorage.setItem(GOAL_KEY, JSON.stringify(data));
  return data;
});

mock('get', /\/growth\/timeline$/, () => {
  const out: Array<Record<string, unknown>> = [];
  load().forEach((c) => {
    out.push({
      id: `checkin-${c.id}`,
      type: 'checkin',
      title: `打卡 ${c.style ?? ''} ${minutesOf(c)}min`,
      subtitle: c.location,
      ts: timeOf(c).getTime()
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
  loadWorks().forEach((w) => {
    out.push({
      id: `work-${w.id}`,
      type: 'work',
      title: `上传作品 · ${w.title}`,
      subtitle: w.description,
      ts: w.createdAt
    });
  });
  return out.sort((a, b) => Number(b.ts) - Number(a.ts));
});
