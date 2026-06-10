import { mock } from '../index';

const KEY = 'bitdance_mock_reviews';

type TargetType = 'studio' | 'course' | 'coach';
type ReviewStatus = 'published' | 'folded';

interface Dimension {
  code: string;
  name: string;
  score: number;
}

interface MediaAsset {
  type: 'image' | 'video';
  url: string;
  name: string;
  size: number;
}

interface Item {
  id: number;
  userId: number;
  targetType: TargetType;
  targetId: number;
  overallScore: number;
  contentText: string;
  isVerified: boolean;
  verifiedSourceType?: string;
  weightFactor: number;
  reviewStatus: ReviewStatus;
  riskLevel: number;
  helpfulCount: number;
  isPinned: boolean;
  publishedAt: string;
  dimensions: Dimension[];
  mediaAssets: MediaAsset[];
}

const mockMediaUrls = [
  'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=960&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=960&q=80&auto=format&fit=crop'
];

const defaultMediaForReview = (id: number, targetType: TargetType): MediaAsset[] => {
  // M2 媒体评价：mock 种子也带图片，便于无后端数据库时验证评价聚合面板展示。
  if (targetType !== 'studio' || id % 4 !== 0) return [];
  const url = mockMediaUrls[id % mockMediaUrls.length];
  return [{ type: 'image', url, name: `review-${id}.jpg`, size: 180000 }];
};

const names: Record<TargetType, Record<string, string>> = {
  studio: {
    traffic: '交通便利度',
    hygiene: '环境卫生',
    venue: '场地条件',
    vibe: '整体氛围'
  },
  coach: {
    patience: '耐心程度',
    correction: '纠错质量',
    explanation: '讲解清晰度',
    beginnerFriendly: '零基础友好'
  },
  course: {
    difficulty: '上手难度',
    rhythm: '节奏合理性',
    intensity: '练习强度',
    gain: '实际收获'
  }
};

const buildDims = (targetType: TargetType, scores: Record<string, number>) =>
  Object.entries(scores).map(([code, score]) => ({
    code,
    name: names[targetType][code] ?? code,
    score
  }));

const createItem = (
  id: number,
  targetType: TargetType,
  targetId: number,
  scores: Record<string, number>,
  contentText: string,
  isVerified: boolean,
  daysAgo: number,
  overrides: Partial<Item> = {}
): Item => {
  const values = Object.values(scores);
  const overallScore = +(values.reduce((sum, item) => sum + item, 0) / values.length).toFixed(1);
  return {
    id,
    userId: 1000 + id,
    targetType,
    targetId,
    overallScore,
    contentText,
    isVerified,
    verifiedSourceType: isVerified ? 'trial' : undefined,
    weightFactor: isVerified ? 1.5 : 1,
    reviewStatus: 'published',
    riskLevel: 0,
    helpfulCount: 3 + (id % 9),
    isPinned: false,
    publishedAt: new Date(Date.now() - daysAgo * 86400000).toISOString(),
    dimensions: buildDims(targetType, scores),
    mediaAssets: defaultMediaForReview(id, targetType),
    ...overrides
  };
};

const seed = (): Item[] => {
  const out: Item[] = [];
  let id = 100;
  for (let s = 1; s <= 8; s += 1) {
    out.push(
      createItem(
        id++,
        'studio',
        s,
        { traffic: 5, hygiene: 5, venue: 5, vibe: 4 },
        '交通很方便，地板和镜面状态好，老师会主动纠正动作。',
        true,
        1
      ),
      createItem(
        id++,
        'studio',
        s,
        { traffic: 4, hygiene: 4, venue: 5, vibe: 5 },
        '晚课时间丰富，氛围比较轻松，零基础第一次来也能跟上。',
        true,
        3
      ),
      createItem(
        id++,
        'studio',
        s,
        { traffic: 4, hygiene: 4, venue: 4, vibe: 4 },
        '场地干净，课表更新及时，适合下班后练习。',
        false,
        6
      ),
      createItem(
        id++,
        'studio',
        s,
        { traffic: 2, hygiene: 3, venue: 3, vibe: 2 },
        '短时间集中低分评价，已折叠等待复核。',
        false,
        8,
        { reviewStatus: 'folded', riskLevel: 2, weightFactor: 0.5, helpfulCount: 0 }
      )
    );
  }

  for (let c = 1; c <= 8; c += 1) {
    out.push(
      createItem(
        id++,
        'coach',
        c,
        { patience: 5, correction: 5, explanation: 5, beginnerFriendly: 5 },
        '讲解非常清楚，会把动作拆成小节，零基础跟得上。',
        true,
        1
      ),
      createItem(
        id++,
        'coach',
        c,
        { patience: 5, correction: 4, explanation: 5, beginnerFriendly: 4 },
        '老师会逐个看动作，纠错细节很具体。',
        true,
        2
      ),
      createItem(
        id++,
        'coach',
        c,
        { patience: 4, correction: 4, explanation: 4, beginnerFriendly: 5 },
        '节奏适中，适合想长期打基础的人。',
        false,
        5
      ),
      createItem(
        id++,
        'coach',
        c,
        { patience: 2, correction: 2, explanation: 3, beginnerFriendly: 2 },
        '新账号同质化低分，已折叠等待人工复核。',
        false,
        7,
        { reviewStatus: 'folded', riskLevel: 2, weightFactor: 0.5, helpfulCount: 0 }
      )
    );
  }

  const courseIds = [...Array.from({ length: 8 }, (_, index) => index + 1), ...Array.from({ length: 6 }, (_, index) => 100 + index)];
  courseIds.forEach((courseId) => {
    out.push(
      createItem(
        id++,
        'course',
        courseId,
        { difficulty: 4, rhythm: 5, intensity: 4, gain: 5 },
        '课程节奏很清楚，动作拆解到位，练完之后能明显记住成品段落。',
        true,
        1
      ),
      createItem(
        id++,
        'course',
        courseId,
        { difficulty: 3, rhythm: 5, intensity: 4, gain: 4 },
        '对零基础比较友好，老师会留时间练习和复盘，强度适中。',
        true,
        3
      ),
      createItem(
        id++,
        'course',
        courseId,
        { difficulty: 4, rhythm: 4, intensity: 5, gain: 4 },
        '体能消耗比预期高一点，但课后收获很扎实，适合想提升表现力的人。',
        false,
        5
      ),
      createItem(
        id++,
        'course',
        courseId,
        { difficulty: 2, rhythm: 2, intensity: 3, gain: 2 },
        '短时间重复低分反馈，已折叠等待人工复核。',
        false,
        7,
        { reviewStatus: 'folded', riskLevel: 2, weightFactor: 0.5, helpfulCount: 0 }
      )
    );
  });
  return out;
};

const load = (): Item[] => {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return seed();
    const parsed = JSON.parse(raw) as Item[];
    if (Array.isArray(parsed) && parsed.some((item) => item.targetType === 'course')) {
      const normalized = parsed.map((item) => ({ ...item, mediaAssets: item.mediaAssets ?? [] }));
      if (normalized.every((item) => Array.isArray(item.dimensions) && item.contentText !== undefined)) {
        return normalized;
      }
    }
    const next = seed();
    save(next);
    return next;
  } catch {
    return seed();
  }
};

const save = (items: Item[]) => localStorage.setItem(KEY, JSON.stringify(items));

const published = (targetType: TargetType, targetId: number) =>
  load().filter((item) => item.targetType === targetType && item.targetId === targetId && item.reviewStatus === 'published');

const summarize = (items: Item[], targetType: TargetType, targetId: number) => {
  if (!items.length) {
    return { targetType, targetId, count: 0, verifiedCount: 0, weightedAvgScore: 0, dimensionAvg: {} };
  }

  const weightSum = items.reduce((sum, item) => sum + item.weightFactor, 0);
  const weighted = items.reduce((sum, item) => sum + item.overallScore * item.weightFactor, 0);
  const dimensionSum: Record<string, { sum: number; count: number }> = {};
  items.forEach((item) => {
    item.dimensions.forEach((dim) => {
      dimensionSum[dim.code] ??= { sum: 0, count: 0 };
      dimensionSum[dim.code].sum += dim.score;
      dimensionSum[dim.code].count += 1;
    });
  });
  const dimensionAvg = Object.fromEntries(
    Object.entries(dimensionSum).map(([code, agg]) => [code, +(agg.sum / agg.count).toFixed(2)])
  );

  return {
    targetType,
    targetId,
    count: items.length,
    verifiedCount: items.filter((item) => item.isVerified).length,
    weightedAvgScore: +(weighted / Math.max(weightSum, 1)).toFixed(2),
    dimensionAvg
  };
};

mock('get', /\/public\/reviews\/summary$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const targetType = p.targetType as TargetType;
  const targetId = Number(p.targetId);
  return summarize(published(targetType, targetId), targetType, targetId);
});

mock('get', /^\/(?:public\/)?reviews$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const targetType = p.targetType as TargetType;
  const targetId = Number(p.targetId);
  const sort = (p.sort as string) ?? 'latest';
  const status = (p.status as ReviewStatus | undefined) ?? undefined;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);

  let items = load().filter((item) => item.targetType === targetType && item.targetId === targetId);
  if (status) items = items.filter((item) => item.reviewStatus === status);
  if (sort === 'helpful') items = items.slice().sort((a, b) => b.helpfulCount - a.helpfulCount);
  else if (sort === 'verified')
    items = items.slice().sort((a, b) => Number(b.isVerified) - Number(a.isVerified) || Date.parse(b.publishedAt) - Date.parse(a.publishedAt));
  else items = items.slice().sort((a, b) => Date.parse(b.publishedAt) - Date.parse(a.publishedAt));

  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total: items.length
  };
});

mock('post', /\/h5\/reviews$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const targetType = body.targetType as TargetType;
  const dimensions = (body.dimensions ?? []) as Dimension[];
  const scores = Object.fromEntries(dimensions.map((item) => [item.code, Number(item.score)]));
  const item = createItem(
    Date.now(),
    targetType,
    Number(body.targetId),
    scores,
    (body.contentText as string) ?? '',
    Boolean(body.sourceRefId),
    0,
    {
      userId: 999,
      overallScore: Number(body.overallScore ?? 0),
      verifiedSourceType: body.sourceType ? String(body.sourceType) : undefined,
      dimensions: dimensions.map((dim) => ({
        code: dim.code,
        name: dim.name || names[targetType][dim.code] || dim.code,
        score: Number(dim.score)
      })),
      mediaAssets: ((body.mediaAssets ?? []) as MediaAsset[]).slice(0, 6)
    }
  );
  items.unshift(item);
  save(items);
  return item;
});

mock('delete', /\/h5\/reviews\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const items = load();
  const next = items.filter((item) => item.id !== id);
  save(next);
  return { deleted: items.length !== next.length };
});

mock('get', /\/reviews\/mine$/, () => load().filter((item) => item.userId === 999));

mock('get', /\/public\/users\/\d+\/reviews$/, ({ url, params }) => {
  const userId = Number(url.split('/').slice(-2)[0]);
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 10);
  let items = load().filter((item) => item.userId === userId && item.reviewStatus === 'published');
  if (items.length === 0 && userId === 1) {
    items = [
      createItem(
        9201,
        'studio',
        1,
        { traffic: 5, hygiene: 5, venue: 5, vibe: 4 },
        'Urban Flow 的韩舞课很适合零基础，老师会拆动作，场地也干净。',
        true,
        2,
        { userId: 1 }
      )
    ];
  }
  items = items.slice().sort((a, b) => Date.parse(b.publishedAt) - Date.parse(a.publishedAt));
  const start = (page - 1) * pageSize;
  return { list: items.slice(start, start + pageSize), page, pageSize, total: items.length };
});
