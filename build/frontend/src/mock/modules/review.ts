import { mock } from '../index';

const KEY = 'bitdance_mock_reviews';

interface Item {
  id: number;
  targetType: string;
  targetId: number;
  authorId: number;
  authorName: string;
  authorAvatar: string;
  text: string;
  images: string[];
  dimensionScores: Record<string, number>;
  ratingAvg: number;
  isVerified: boolean;
  verifiedSourceType?: string;
  helpfulCount: number;
  createdAt: number;
}

const seed = (): Item[] => {
  const out: Item[] = [];
  let id = 100;
  for (let s = 1; s <= 6; s += 1) {
    for (let r = 0; r < 5; r += 1) {
      const ratings = { traffic: 4 + (r % 2), hygiene: 3 + (r % 3), venue: 4, vibe: 5 };
      const avg = (ratings.traffic + ratings.hygiene + ratings.venue + ratings.vibe) / 4;
      out.push({
        id: id++,
        targetType: 'studio',
        targetId: s,
        authorId: 1000 + r,
        authorName: ['小喵', '舞月', '阿橘', '云朵', '羊羊'][r],
        authorAvatar: '',
        text: ['场地干净，老师超耐心，零基础也能跟上！', '环境一般但老师讲解清晰', '氛围超棒，下次还来'][r % 3],
        images: [],
        dimensionScores: ratings,
        ratingAvg: +avg.toFixed(1),
        isVerified: r % 2 === 0,
        verifiedSourceType: r % 2 === 0 ? '试听预约' : undefined,
        helpfulCount: 3 + r * 2,
        createdAt: Date.now() - r * 86400000
      });
    }
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

mock('get', /\/reviews$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const targetType = p.targetType as string;
  const targetId = Number(p.targetId);
  const sort = (p.sort as string) ?? 'latest';
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);

  let items = load().filter((it) => it.targetType === targetType && it.targetId === targetId);
  if (sort === 'helpful') items = items.slice().sort((a, b) => b.helpfulCount - a.helpfulCount);
  else if (sort === 'verified')
    items = items.slice().sort((a, b) => Number(b.isVerified) - Number(a.isVerified));
  else items = items.slice().sort((a, b) => b.createdAt - a.createdAt);

  const total = items.length;
  const start = (page - 1) * pageSize;
  const sumDim: Record<string, number> = {};
  let sumAvg = 0;
  items.forEach((it) => {
    sumAvg += it.ratingAvg;
    Object.entries(it.dimensionScores).forEach(([k, v]) => {
      sumDim[k] = (sumDim[k] ?? 0) + v;
    });
  });
  const dimensionAvg: Record<string, number> = {};
  Object.entries(sumDim).forEach(([k, v]) => {
    dimensionAvg[k] = items.length ? +(v / items.length).toFixed(2) : 0;
  });

  return {
    list: items.slice(start, start + pageSize),
    page,
    pageSize,
    total,
    summary: {
      ratingAvg: items.length ? +(sumAvg / items.length).toFixed(1) : 0,
      reviewCount: total,
      dimensionAvg
    }
  };
});

mock('post', /\/reviews$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const dim = (body.dimensionScores ?? {}) as Record<string, number>;
  const vals = Object.values(dim);
  const avg = vals.length ? vals.reduce((a, b) => a + b, 0) / vals.length : 0;
  const item: Item = {
    id: Date.now(),
    targetType: body.targetType as string,
    targetId: Number(body.targetId),
    authorId: 999,
    authorName: '我',
    authorAvatar: '',
    text: (body.text as string) ?? '',
    images: (body.images as string[]) ?? [],
    dimensionScores: dim,
    ratingAvg: +avg.toFixed(1),
    isVerified: true,
    verifiedSourceType: '试听预约',
    helpfulCount: 0,
    createdAt: Date.now()
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('put', /\/reviews\/\d+$/, ({ url, data }) => {
  const id = Number(url.split('/').pop());
  const items = load();
  const idx = items.findIndex((it) => it.id === id);
  if (idx >= 0) {
    const body = (data ?? {}) as Record<string, unknown>;
    if (body.text !== undefined) items[idx].text = body.text as string;
    if (body.dimensionScores) {
      items[idx].dimensionScores = body.dimensionScores as Record<string, number>;
      const vals = Object.values(items[idx].dimensionScores);
      items[idx].ratingAvg = vals.length ? +(vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(1) : 0;
    }
    save(items);
    return items[idx];
  }
  return null;
});

mock('delete', /\/reviews\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const items = load();
  const next = items.filter((it) => it.id !== id);
  save(next);
  return { deleted: items.length !== next.length };
});

mock('get', /\/reviews\/mine$/, () => load().filter((it) => it.authorId === 999));
