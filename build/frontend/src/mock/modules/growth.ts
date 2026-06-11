import { mock } from '../index';

const CHECKIN_KEY = 'bitdance_mock_growth_checkins_m5_v2';
const WORK_KEY = 'bitdance_mock_growth_works_m5_v2';
const GOAL_KEY = 'bitdance_mock_growth_goal_m5_v2';
const ASSET_KEY = 'bitdance_mock_growth_assets_m5_v2';

const STYLE_NAMES: Record<number, string> = {
  1: 'Hiphop',
  2: 'Jazz',
  3: 'Breaking',
  4: 'Locking',
  5: 'Popping',
  6: 'K-pop',
  7: 'Waacking'
};

interface CheckinItem {
  id: number;
  userId: number;
  danceStyleId: number | null;
  studioId?: number | null;
  courseScheduleId?: number | null;
  practicePostId?: number | null;
  durationMinutes: number;
  feelingText: string;
  isPublic: boolean;
  checkinAt: string;
  style: string;
  durationMin: number;
  location: string;
  feeling: string;
  visibility: 'public' | 'private' | 'friends';
  createdAt: number;
}

interface MediaAssetDto {
  id: number;
  assetType: 'image' | 'video' | 'document' | 'audio';
  bizType: string;
  originFileName: string;
  mimeType: string;
  fileSize: number;
  url: string;
  createdAt: string;
}

interface GrowthWork {
  id: number;
  userId: number;
  danceStyleId: number | null;
  workTitle: string;
  workDescription: string;
  coverAssetId: number | null;
  isPublic: boolean;
  coverUrl: string | null;
  mediaAssets: MediaAssetDto[];
  createdAt: string;
  type: 'image' | 'video';
  title: string;
  description: string;
  style: string;
  visibility: 'public' | 'private' | 'friends';
}

interface GrowthGoal {
  id: number;
  userId: number;
  goalPeriod: 'weekly' | 'monthly';
  targetMinutes: number;
  targetTimes: number;
  currentMinutes: number;
  currentTimes: number;
  startDate: string;
  endDate: string;
  goalStatus: 'active' | 'completed';
  period: 'week' | 'month';
  targetSessions: number;
}

const read = <T>(key: string, fallback: T): T => {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) as T : fallback;
  } catch {
    return fallback;
  }
};

const write = <T>(key: string, value: T) => localStorage.setItem(key, JSON.stringify(value));

const dayStart = (date: Date) => new Date(date.getFullYear(), date.getMonth(), date.getDate());
const isoDate = (date: Date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

const rangeFor = (period: 'weekly' | 'monthly', anchor = new Date()) => {
  if (period === 'weekly') {
    const start = dayStart(anchor);
    start.setDate(anchor.getDate() - ((anchor.getDay() + 6) % 7));
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    return { start, end };
  }
  return {
    start: new Date(anchor.getFullYear(), anchor.getMonth(), 1),
    end: new Date(anchor.getFullYear(), anchor.getMonth() + 1, 0)
  };
};

const inRange = (value: string | number, start: Date, end: Date) => {
  const date = new Date(value);
  const from = dayStart(start).getTime();
  const to = new Date(end.getFullYear(), end.getMonth(), end.getDate(), 23, 59, 59, 999).getTime();
  return date.getTime() >= from && date.getTime() <= to;
};

const styleName = (id?: number | null, fallback?: string) =>
  fallback || STYLE_NAMES[Number(id)] || '自由练习';

const seed = () => {
  if (!localStorage.getItem(CHECKIN_KEY)) {
    const now = new Date();
    const mk = (daysAgo: number, styleId: number, minutes: number, location: string, feeling: string): CheckinItem => {
      const date = new Date(now);
      date.setDate(now.getDate() - daysAgo);
      date.setHours(20 - (daysAgo % 3), 30, 0, 0);
      return {
        id: 8000 + daysAgo,
        userId: 1,
        danceStyleId: styleId,
        studioId: daysAgo % 2 ? 2 : 1,
        courseScheduleId: null,
        practicePostId: null,
        durationMinutes: minutes,
        feelingText: feeling,
        isPublic: daysAgo !== 5,
        checkinAt: date.toISOString(),
        style: styleName(styleId),
        durationMin: minutes,
        location,
        feeling,
        visibility: daysAgo === 5 ? 'private' : 'public',
        createdAt: date.getTime()
      };
    };
    write(CHECKIN_KEY, [
      mk(0, 1, 90, 'Urban Flow 舞室', '今天复习律动和重心，状态稳定。'),
      mk(2, 2, 75, 'DanceLab 五道口', 'Jazz 手臂线条比上周更顺。'),
      mk(5, 4, 60, '家中自练', 'Locking 基础点位还需要慢速拆解。'),
      mk(9, 1, 110, 'Urban Flow 舞室', '和搭子一起复盘成品舞段落。')
    ]);
  }

  if (!localStorage.getItem(ASSET_KEY)) {
    const now = new Date().toISOString();
    write<MediaAssetDto[]>(ASSET_KEY, [
      {
        id: 7101,
        assetType: 'image',
        bizType: 'growth_work',
        originFileName: 'hiphop-practice.jpg',
        mimeType: 'image/jpeg',
        fileSize: 248000,
        url: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=720&q=80&auto=format&fit=crop',
        createdAt: now
      },
      {
        id: 7102,
        assetType: 'video',
        bizType: 'growth_work',
        originFileName: 'jazz-review.mp4',
        mimeType: 'video/mp4',
        fileSize: 1280000,
        url: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
        createdAt: now
      }
    ]);
  }

  if (!localStorage.getItem(WORK_KEY)) {
    const assets = read<MediaAssetDto[]>(ASSET_KEY, []);
    const now = new Date();
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    const lastWeek = new Date(now);
    lastWeek.setDate(now.getDate() - 7);
    write<GrowthWork[]>(WORK_KEY, [
      {
        id: 9001,
        userId: 1,
        danceStyleId: 1,
        workTitle: 'Hiphop 周末练习片段',
        workDescription: '记录 bounce 和手部 groove，下一次重点练卡点。',
        coverAssetId: assets[0]?.id ?? null,
        isPublic: true,
        coverUrl: assets[0]?.url ?? null,
        mediaAssets: assets[0] ? [assets[0]] : [],
        createdAt: yesterday.toISOString(),
        type: 'image',
        title: 'Hiphop 周末练习片段',
        description: '记录 bounce 和手部 groove，下一次重点练卡点。',
        style: 'Hiphop',
        visibility: 'public'
      },
      {
        id: 9002,
        userId: 1,
        danceStyleId: 2,
        workTitle: 'Jazz 线条复盘',
        workDescription: '视频里能看到转身前准备不足，后续补核心稳定。',
        coverAssetId: assets[1]?.id ?? null,
        isPublic: false,
        coverUrl: assets[1]?.url ?? null,
        mediaAssets: assets[1] ? [assets[1]] : [],
        createdAt: lastWeek.toISOString(),
        type: 'video',
        title: 'Jazz 线条复盘',
        description: '视频里能看到转身前准备不足，后续补核心稳定。',
        style: 'Jazz',
        visibility: 'private'
      }
    ]);
  }

  if (!localStorage.getItem(GOAL_KEY)) {
    const range = rangeFor('weekly');
    write<Partial<GrowthGoal>>(GOAL_KEY, {
      id: 6001,
      userId: 1,
      goalPeriod: 'weekly',
      targetTimes: 5,
      targetMinutes: 300,
      startDate: isoDate(range.start),
      endDate: isoDate(range.end),
      goalStatus: 'active'
    });
  }
};

seed();

const checkins = () => read<CheckinItem[]>(CHECKIN_KEY, []);
const saveCheckins = (items: CheckinItem[]) => write(CHECKIN_KEY, items);
const assets = () => read<MediaAssetDto[]>(ASSET_KEY, []);
const saveAssets = (items: MediaAssetDto[]) => write(ASSET_KEY, items);
const works = () => read<GrowthWork[]>(WORK_KEY, []);
const saveWorks = (items: GrowthWork[]) => write(WORK_KEY, items);

const calcStats = () => {
  const items = checkins();
  const now = new Date();
  const week = rangeFor('weekly', now);
  const month = rangeFor('monthly', now);
  const dates = new Set(items.map((item) => dayStart(new Date(item.checkinAt)).toDateString()));
  const totalMinutes = items.reduce((sum, item) => sum + Number(item.durationMinutes || 0), 0);
  const styles = new Set(items.map((item) => item.danceStyleId || item.style).filter(Boolean));
  const courses = new Set(items.map((item) => item.courseScheduleId).filter(Boolean));
  const inWeek = items.filter((item) => inRange(item.checkinAt, week.start, week.end));
  const inMonth = items.filter((item) => inRange(item.checkinAt, month.start, month.end));
  let streakDays = 0;
  const cursor = dayStart(now);
  while (dates.has(cursor.toDateString())) {
    streakDays += 1;
    cursor.setDate(cursor.getDate() - 1);
  }
  const recent = [...items].sort((a, b) => b.createdAt - a.createdAt)[0];
  const weekMinutes = inWeek.reduce((sum, item) => sum + item.durationMinutes, 0);
  const goal = goalWithProgress();
  return {
    totalSessions: items.length,
    totalMinutes,
    totalDays: dates.size,
    styleCount: styles.size,
    streakDays,
    lastCheckinAt: recent?.checkinAt ?? null,
    courseCount: courses.size,
    weekSessions: inWeek.length,
    weekMinutes,
    monthSessions: inMonth.length,
    monthMinutes: inMonth.reduce((sum, item) => sum + item.durationMinutes, 0),
    recentAt: recent?.createdAt ?? null,
    goalProgress: goal ? progressOf(goal) : 0
  };
};

const goalWithProgress = (): GrowthGoal | null => {
  const stored = read<Partial<GrowthGoal> | null>(GOAL_KEY, null);
  if (!stored) return null;
  const period = stored.goalPeriod ?? 'weekly';
  const range = rangeFor(period);
  const scoped = checkins().filter((item) => inRange(item.checkinAt, range.start, range.end));
  const currentTimes = scoped.length;
  const currentMinutes = scoped.reduce((sum, item) => sum + item.durationMinutes, 0);
  const targetTimes = Number(stored.targetTimes ?? stored.targetSessions ?? 5);
  const targetMinutes = Number(stored.targetMinutes ?? 300);
  const done = currentTimes >= targetTimes || currentMinutes >= targetMinutes;
  return {
    id: stored.id ?? 6001,
    userId: stored.userId ?? 1,
    goalPeriod: period,
    targetTimes,
    targetMinutes,
    currentTimes,
    currentMinutes,
    startDate: stored.startDate ?? isoDate(range.start),
    endDate: stored.endDate ?? isoDate(range.end),
    goalStatus: done ? 'completed' : 'active',
    period: period === 'weekly' ? 'week' : 'month',
    targetSessions: targetTimes
  };
};

const progressOf = (goal: GrowthGoal) => {
  const byTimes = goal.targetTimes ? goal.currentTimes / goal.targetTimes : 0;
  const byMinutes = goal.targetMinutes ? goal.currentMinutes / goal.targetMinutes : 0;
  return Math.min(100, Math.round(Math.max(byTimes, byMinutes) * 100));
};

const buildTimeline = () => {
  const out: Array<{ id: string; refId?: number; type: 'checkin' | 'trial' | 'practice' | 'review' | 'work'; title: string; subtitle?: string; ts: number | string }> = [];
  checkins().forEach((item) => {
    out.push({
      id: `checkin-${item.id}`,
      refId: item.id,
      type: 'checkin',
      title: `训练打卡 · ${item.style} ${item.durationMinutes}min`,
      subtitle: `${item.location || '未填写地点'} · ${item.feelingText || '暂无感受'}`,
      ts: item.checkinAt
    });
  });
  works().forEach((item) => {
    out.push({
      id: `work-${item.id}`,
      refId: item.id,
      type: 'work',
      title: `阶段作品 · ${item.workTitle}`,
      subtitle: item.workDescription,
      ts: item.createdAt
    });
  });
  read<Array<{ id: number; studioName?: string; status?: string; createdAt?: number; date?: string; time?: string }>>('bitdance_mock_trial_bookings', []).forEach((item) => {
    out.push({
      id: `trial-${item.id}`,
      refId: item.id,
      type: 'trial',
      title: `试听记录 · ${item.studioName || '舞室'}`,
      subtitle: `${item.date || ''} ${item.time || ''} · ${item.status || '已预约'}`,
      ts: item.createdAt || Date.now()
    });
  });
  read<Array<{ id: number; status?: string; style?: string; title?: string; date?: string; time?: string; createdAt?: string | number }>>('bitdance_mock_practices_m4_v2', [])
    .filter((item) => item.status === 'COMPLETED')
    .forEach((item) => {
      out.push({
        id: `practice-${item.id}`,
        refId: item.id,
        type: 'practice',
        title: `完成约练 · ${item.style || item.title || '舞蹈练习'}`,
        subtitle: `${item.date || ''} ${item.time || ''}`,
        ts: item.createdAt || Date.now()
      });
    });
  read<Array<{ id: number; authorId?: number; targetType?: string; targetId?: number; text?: string; contentText?: string; createdAt?: number }>>('bitdance_mock_reviews', [])
    .filter((item) => item.authorId === 999 || item.authorId === 1)
    .forEach((item) => {
      out.push({
        id: `review-${item.id}`,
        refId: item.id,
        type: 'review',
        title: `发布评价 · ${item.targetType || '对象'} #${item.targetId || ''}`,
        subtitle: item.text || item.contentText || '已发布结构化评价',
        ts: item.createdAt || Date.now()
      });
    });
  return out.sort((a, b) => new Date(b.ts).getTime() - new Date(a.ts).getTime());
};

const badgeDefinitions = [
  { id: 1, badgeCode: 'FIRST_CHECKIN', badgeName: '第一滴汗', description: '完成第一次训练打卡', iconAssetId: null, ruleType: 'checkin_count', ruleConfig: '{"count":1}', status: 'active' },
  { id: 2, badgeCode: 'THREE_DAY_STREAK', badgeName: '连续练习', description: '连续打卡 3 天', iconAssetId: null, ruleType: 'streak_days', ruleConfig: '{"days":3}', status: 'active' },
  { id: 3, badgeCode: 'FIRST_WORK', badgeName: '作品上墙', description: '保存第一条阶段作品', iconAssetId: null, ruleType: 'work_count', ruleConfig: '{"count":1}', status: 'active' },
  { id: 4, badgeCode: 'GOAL_KEEPER', badgeName: '目标推进者', description: '训练目标进度达到 60%', iconAssetId: null, ruleType: 'goal_progress', ruleConfig: '{"progress":60}', status: 'active' }
];

const earnedBadges = () => {
  const stats = calcStats();
  const goal = goalWithProgress();
  const now = new Date().toISOString();
  const out: Array<{ id: number; badgeId: number; sourceType?: string; sourceRefId?: number; awardedAt: string }> = [];
  if (stats.totalSessions >= 1) out.push({ id: 101, badgeId: 1, sourceType: 'checkin', sourceRefId: checkins()[0]?.id, awardedAt: now });
  if (stats.streakDays >= 3) out.push({ id: 102, badgeId: 2, sourceType: 'checkin', sourceRefId: checkins()[0]?.id, awardedAt: now });
  if (works().length >= 1) out.push({ id: 103, badgeId: 3, sourceType: 'work', sourceRefId: works()[0]?.id, awardedAt: now });
  if (goal && progressOf(goal) >= 60) out.push({ id: 104, badgeId: 4, sourceType: 'goal', sourceRefId: goal.id, awardedAt: now });
  return out;
};

const buildReport = (period: 'monthly' | 'quarterly') => {
  const now = new Date();
  const start = period === 'monthly'
    ? new Date(now.getFullYear(), now.getMonth(), 1)
    : new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1);
  const end = period === 'monthly'
    ? new Date(now.getFullYear(), now.getMonth() + 1, 0)
    : new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3 + 3, 0);
  const scoped = checkins().filter((item) => inRange(item.checkinAt, start, end));
  const scopedWorks = works().filter((item) => inRange(item.createdAt, start, end));
  const styleSessions = scoped.reduce<Record<string, number>>((acc, item) => {
    const key = String(item.danceStyleId || item.style || 'other');
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});
  const activeDays = new Set(scoped.map((item) => dayStart(new Date(item.checkinAt)).toDateString())).size;
  const totalMinutes = scoped.reduce((sum, item) => sum + item.durationMinutes, 0);
  const goal = goalWithProgress();
  const highlights = buildTimeline().filter((item) => inRange(item.ts, start, end)).slice(0, 5);
  return {
    period,
    startDate: isoDate(start),
    endDate: isoDate(end),
    totalSessions: scoped.length,
    totalMinutes,
    activeDays,
    styleCount: Object.keys(styleSessions).length,
    workCount: scopedWorks.length,
    badgeCount: earnedBadges().length,
    goalTargetTimes: goal?.targetTimes ?? null,
    goalCurrentTimes: goal?.currentTimes ?? null,
    goalTargetMinutes: goal?.targetMinutes ?? null,
    goalCurrentMinutes: goal?.currentMinutes ?? null,
    goalProgress: goal ? progressOf(goal) : 0,
    styleSessions,
    highlights,
    suggestion: totalMinutes >= 300
      ? '本周期练习节奏很好，可以把作品复盘和约练评价结合起来，沉淀一条更完整的成长记录。'
      : '建议先保持每周 3 次、每次 60 分钟以上的稳定节奏，再逐步增加作品复盘和公开展示。'
  };
};

mock('post', /\/growth\/checkins$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const now = new Date();
  const styleId = Number(body.danceStyleId || 1);
  const duration = Math.max(1, Number(body.durationMinutes ?? body.durationMin ?? 60));
  const item: CheckinItem = {
    id: Date.now(),
    userId: 1,
    danceStyleId: styleId,
    studioId: (body.studioId as number | undefined) ?? null,
    courseScheduleId: (body.courseScheduleId as number | undefined) ?? null,
    practicePostId: (body.practicePostId as number | undefined) ?? null,
    durationMinutes: duration,
    feelingText: String(body.feelingText ?? body.feeling ?? ''),
    isPublic: Boolean(body.isPublic ?? body.visibility !== 'private'),
    checkinAt: String(body.checkinAt ?? now.toISOString()),
    style: styleName(styleId, body.style as string | undefined),
    durationMin: duration,
    location: String(body.location ?? '未填写地点'),
    feeling: String(body.feelingText ?? body.feeling ?? ''),
    visibility: ((body.visibility as CheckinItem['visibility'] | undefined) ?? (body.isPublic === false ? 'private' : 'public')),
    createdAt: now.getTime()
  };
  const items = checkins();
  items.unshift(item);
  saveCheckins(items);
  return item;
});

mock('get', /\/growth\/checkins$/, () => checkins());

mock('delete', /\/growth\/checkins\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const before = checkins();
  const next = before.filter((item) => item.id !== id);
  saveCheckins(next);
  return { deleted: before.length !== next.length };
});

mock('get', /\/growth\/stats$/, () => calcStats());

mock('get', /\/growth\/works$/, () => works());

mock('post', /\/growth\/works$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const mediaAssetIds = (body.mediaAssetIds as number[] | undefined) ?? [];
  const mediaAssets = assets().filter((item) => mediaAssetIds.includes(item.id));
  const coverAsset = assets().find((item) => item.id === Number(body.coverAssetId)) ?? mediaAssets[0];
  const styleId = Number(body.danceStyleId || 1);
  const title = String(body.workTitle ?? body.title ?? '新的阶段作品');
  const description = String(body.workDescription ?? body.description ?? '');
  const createdAt = new Date().toISOString();
  const item: GrowthWork = {
    id: Date.now(),
    userId: 1,
    danceStyleId: styleId,
    workTitle: title,
    workDescription: description,
    coverAssetId: coverAsset?.id ?? null,
    isPublic: Boolean(body.isPublic ?? body.visibility !== 'private'),
    coverUrl: coverAsset?.url ?? null,
    mediaAssets,
    createdAt,
    type: coverAsset?.assetType === 'video' ? 'video' : 'image',
    title,
    description,
    style: styleName(styleId, body.style as string | undefined),
    visibility: body.isPublic === false ? 'private' : 'public'
  };
  const items = works();
  items.unshift(item);
  saveWorks(items);
  return item;
});

mock('delete', /\/growth\/works\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const before = works();
  const next = before.filter((item) => item.id !== id);
  saveWorks(next);
  return { deleted: before.length !== next.length };
});

mock('post', /\/media-assets$/, ({ data }) => {
  const existing = assets();
  const id = Date.now();
  const file = data instanceof FormData ? data.get('file') as File | null : null;
  const name = file?.name || `growth-work-${id}.jpg`;
  const mimeType = file?.type || 'image/jpeg';
  const assetType: MediaAssetDto['assetType'] = mimeType.startsWith('video/') ? 'video' : 'image';
  const item: MediaAssetDto = {
    id,
    assetType,
    bizType: data instanceof FormData ? String(data.get('bizType') || 'growth_work') : 'growth_work',
    originFileName: name,
    mimeType,
    fileSize: file?.size || 0,
    url: assetType === 'video'
      ? 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
      : 'https://images.unsplash.com/photo-1547153760-18fc86324498?w=720&q=80&auto=format&fit=crop',
    createdAt: new Date().toISOString()
  };
  existing.unshift(item);
  saveAssets(existing);
  return item;
});

mock('get', /\/growth\/goals\/active$/, () => goalWithProgress());
mock('get', /\/growth\/goal$/, () => goalWithProgress());

mock('put', /\/growth\/goals\/active$/, ({ data }) => {
  const body = data as Partial<GrowthGoal>;
  const stored = {
    ...goalWithProgress(),
    ...body,
    id: body.id ?? goalWithProgress()?.id ?? 6001,
    userId: 1,
    goalPeriod: body.goalPeriod ?? (body.period === 'month' ? 'monthly' : 'weekly'),
    targetTimes: Number(body.targetTimes ?? body.targetSessions ?? 5),
    targetMinutes: Number(body.targetMinutes ?? 300),
    goalStatus: 'active'
  };
  write(GOAL_KEY, stored);
  return goalWithProgress();
});

mock('put', /\/growth\/goal$/, ({ data }) => {
  write(GOAL_KEY, data);
  return goalWithProgress();
});

mock('get', /\/growth\/timeline$/, () => buildTimeline());

mock('get', /\/growth\/badges$/, () => earnedBadges());

mock('get', /\/public\/badges\/definitions$/, () => badgeDefinitions);

mock('get', /\/growth\/reports$/, ({ params }) => {
  const period = ((params as { period?: 'monthly' | 'quarterly' } | undefined)?.period ?? 'monthly');
  return buildReport(period);
});
