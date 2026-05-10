import { mock } from '../index';

const KEY = 'bitdance_mock_coach_profile';

interface Work {
  id: number;
  type: 'image' | 'video';
  title: string;
  cover: string;
  createdAt: number;
}

interface Profile {
  id: number;
  name: string;
  avatar: string;
  styles: string[];
  teachStyle: string;
  intro: string;
  works: Work[];
  availableSlots: Array<{ day: string; time: string }>;
  ratingAvg: number;
  reviewCount: number;
}

const seed = (): Profile => ({
  id: 999,
  name: '我',
  avatar: '',
  styles: ['Hiphop'],
  teachStyle: '注重基础律动与节奏感培养，鼓励学生表达自己的风格。',
  intro: '7 年舞龄，5 年教学经验。',
  works: [],
  availableSlots: [
    { day: '周一', time: '19:00-20:30' },
    { day: '周三', time: '19:00-20:30' }
  ],
  ratingAvg: 4.8,
  reviewCount: 0
});

const load = (): Profile => {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Profile) : seed();
  } catch {
    return seed();
  }
};
const save = (p: Profile) => localStorage.setItem(KEY, JSON.stringify(p));

mock('get', /\/coach\/me\/profile$/, () => load());

mock('put', /\/coach\/me\/profile$/, ({ data }) => {
  const p = load();
  const body = (data ?? {}) as Partial<Profile>;
  Object.assign(p, body);
  save(p);
  return p;
});

mock('post', /\/coach\/me\/works$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const p = load();
  const work: Work = {
    id: Date.now(),
    type: (body.type as 'image' | 'video') ?? 'image',
    title: (body.title as string) ?? '新作品',
    cover: (body.cover as string) ?? '',
    createdAt: Date.now()
  };
  p.works.unshift(work);
  save(p);
  return work;
});

mock('delete', /\/coach\/me\/works\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const p = load();
  const before = p.works.length;
  p.works = p.works.filter((w) => w.id !== id);
  save(p);
  return { deleted: before !== p.works.length };
});
