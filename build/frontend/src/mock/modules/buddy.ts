import { mock } from '../index';

const RATING_KEY = 'bitdance_mock_practice_ratings';
const BUDDY_KEY = 'bitdance_mock_buddies';

interface Buddy {
  userId: number;
  name: string;
  avatar: string;
  sharedStyles: string[];
  pastSessions: number;
  lastAt: number;
}

const seedBuddies = (): Buddy[] => [
  {
    userId: 201,
    name: '小喵',
    avatar: '',
    sharedStyles: ['Hiphop', 'Jazz'],
    pastSessions: 3,
    lastAt: Date.now() - 86400_000 * 5
  },
  {
    userId: 203,
    name: '阿橘',
    avatar: '',
    sharedStyles: ['Breaking'],
    pastSessions: 2,
    lastAt: Date.now() - 86400_000 * 9
  }
];

const loadBuddies = (): Buddy[] => {
  try {
    const raw = localStorage.getItem(BUDDY_KEY);
    return raw ? (JSON.parse(raw) as Buddy[]) : seedBuddies();
  } catch {
    return seedBuddies();
  }
};

mock('get', /\/buddies\/mine$/, () => loadBuddies());

mock('get', /\/practices\/recommend$/, () => {
  // 简化：从已有 practice mock 中选取与本人偏好接近的前 8 条
  try {
    const raw = localStorage.getItem('bitdance_mock_practices');
    const all = raw ? JSON.parse(raw) : [];
    const prefRaw = localStorage.getItem('bitdance_preferences');
    const pref = prefRaw ? JSON.parse(prefRaw) : { styles: [] };
    const wanted: string[] = pref.styles ?? [];
    let scored = (all as Array<{ style: string }>).map((it, i) => ({
      it,
      score:
        (wanted.length === 0 ? 1 : wanted.includes(it.style) ? 2 : 0) + Math.max(0, 5 - (i % 6))
    }));
    scored = scored.sort((a, b) => b.score - a.score).slice(0, 8);
    return scored.map((s) => s.it);
  } catch {
    return [];
  }
});

mock('post', /\/practices\/ratings$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  try {
    const arr = JSON.parse(localStorage.getItem(RATING_KEY) ?? '[]');
    arr.push({ ...body, ts: Date.now() });
    localStorage.setItem(RATING_KEY, JSON.stringify(arr));
    // 自动晋升搭子
    const buddies = loadBuddies();
    const exists = buddies.find((b) => b.userId === Number(body.toUserId));
    if (!exists) {
      buddies.unshift({
        userId: Number(body.toUserId),
        name: '搭子' + body.toUserId,
        avatar: '',
        sharedStyles: [],
        pastSessions: 1,
        lastAt: Date.now()
      });
    } else {
      exists.pastSessions += 1;
      exists.lastAt = Date.now();
    }
    localStorage.setItem(BUDDY_KEY, JSON.stringify(buddies));
  } catch {
    /* ignore */
  }
  return { ok: true };
});
