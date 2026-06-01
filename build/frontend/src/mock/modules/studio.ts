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
  favored: false
}));

mock('get', /\/studios\/nearby/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const keyword = (p.keyword as string) ?? '';
  const distanceMax = Number(p.distanceKm ?? 0);

  let pool = allStudios.slice();
  if (keyword) pool = pool.filter((s) => s.name.includes(keyword) || s.address.includes(keyword));
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
    brandName: '舞星',
    intro: '主打街舞与爵士的连锁舞室，零基础友好，老师耐心。',
    transportInfo: '地铁站步行 5 分钟',
    contactPhone: '13800000789',
    claimStatus: 'claimed',
    danceStyleIds: [1, 2, 3]
  };
});
