import { mock } from '../index';

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const CITIES = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '长沙'];
const AREAS = ['海淀区', '朝阳区', '东城区', '西城区', '丰台区', '通州区'];

const allStudios = Array.from({ length: 80 }).map((_, i) => ({
  id: i + 1,
  name: `舞星 Studio ${i + 1}`,
  address: `${CITIES[i % CITIES.length]}${AREAS[i % AREAS.length]}学院路 ${i + 1} 号`,
  cityId: (i % CITIES.length) + 1,
  businessDistrictId: (i % AREAS.length) + 1,
  coverAssetId: undefined,
  distanceKm: +(0.3 + (i % 20) * 0.6).toFixed(1),
  latitude: 39.9 + i * 0.001,
  longitude: 116.3 + i * 0.001,
  favored: false,
  danceStyleId: (i % STYLES.length) + 1,
  minPrice: 69 + (i % 8) * 20,
  timeSlots: [['morning', 'weekend'], ['afternoon'], ['evening'], ['afternoon', 'weekend']][i % 4],
  trialAvailable: i % 3 !== 0,
  zeroBasicFriendly: i % 2 === 0,
  nearMetro: i % 4 !== 0
}));

mock('get', /\/studios\/nearby/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const keyword = (p.keyword as string) ?? '';
  const distanceMax = Number(p.distanceKm ?? 0);
  const danceStyleId = Number(p.danceStyleId ?? 0);
  const minPrice = Number(p.minPrice ?? 0);
  const maxPrice = Number(p.maxPrice ?? 0);
  const timeSlot = (p.timeSlot as string) ?? '';

  let pool = allStudios.slice();
  if (keyword) pool = pool.filter((s) => s.name.includes(keyword) || s.address.includes(keyword));
  if (distanceMax) pool = pool.filter((s) => s.distanceKm <= distanceMax);
  if (danceStyleId) pool = pool.filter((s) => s.danceStyleId === danceStyleId);
  if (minPrice) pool = pool.filter((s) => s.minPrice >= minPrice);
  if (maxPrice) pool = pool.filter((s) => s.minPrice <= maxPrice);
  if (timeSlot) pool = pool.filter((s) => s.timeSlots.includes(timeSlot));
  if (p.trialAvailable) pool = pool.filter((s) => s.trialAvailable);
  if (p.zeroBasicFriendly) pool = pool.filter((s) => s.zeroBasicFriendly);
  if (p.nearMetro) pool = pool.filter((s) => s.nearMetro);

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
    brandName: '舞星',
    intro: '主打街舞与爵士的连锁舞室，零基础友好，老师耐心。',
    transportInfo: '地铁站步行 5 分钟',
    contactPhone: '13800000789',
    claimStatus: 'claimed',
    danceStyleIds: [1, 2, 3]
  };
});
