import { mock } from '../index';

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const CITIES = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '长沙'];
const AREAS = ['海淀区', '朝阳区', '东城区', '西城区', '丰台区', '通州区'];

const allStudios = Array.from({ length: 80 }).map((_, i) => ({
  id: i + 1,
  name: `舞星 Studio ${i + 1}`,
  cover: '',
  city: CITIES[i % CITIES.length],
  area: AREAS[i % AREAS.length],
  distanceKm: +(0.3 + (i % 20) * 0.6).toFixed(1),
  ratingAvg: +(3.8 + (i % 12) * 0.1).toFixed(1),
  reviewCount: 8 + ((i * 13) % 240),
  topStyles: STYLES.slice(i % 6, (i % 6) + ((i % 3) + 1)),
  beginnerFriendly: i % 2 === 0
}));

mock('get', /\/studios\/nearby/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const city = (p.city as string) ?? '';
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const styles = (p.styles as string[] | string | undefined) ?? [];
  const styleArr = Array.isArray(styles) ? styles : [styles].filter(Boolean);
  const keyword = (p.keyword as string) ?? '';
  const beginnerFriendly = p.beginnerFriendly === true || p.beginnerFriendly === 'true';
  const distanceMax = Number(p.distanceKm ?? 0);

  let pool = allStudios.slice();
  if (city) pool = pool.filter((s) => s.city === city);
  if (styleArr.length) pool = pool.filter((s) => s.topStyles.some((t) => styleArr.includes(t)));
  if (keyword) pool = pool.filter((s) => s.name.includes(keyword) || s.area.includes(keyword));
  if (beginnerFriendly) pool = pool.filter((s) => s.beginnerFriendly);
  if (distanceMax) pool = pool.filter((s) => s.distanceKm <= distanceMax);

  const start = (page - 1) * pageSize;
  return {
    list: pool.slice(start, start + pageSize),
    page,
    pageSize,
    total: pool.length
  };
});

mock('get', /\/studios\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const base = allStudios.find((s) => s.id === id) ?? allStudios[0];
  return {
    ...base,
    intro: '主打街舞与爵士的连锁舞室，零基础友好，老师耐心。',
    address: `${base.city}${base.area}学院路 ${(id % 50) + 1} 号`,
    openHours: '10:00 - 22:00',
    photos: [],
    courses: STYLES.slice(0, 4).map((style, idx) => ({
      id: id * 100 + idx,
      name: `${style} 入门班`,
      style,
      difficulty: idx % 2 === 0 ? '入门' : '进阶',
      price: 99 + idx * 30
    }))
  };
});
