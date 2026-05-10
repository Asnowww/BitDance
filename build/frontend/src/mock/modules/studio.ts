import { mock } from '../index';

const studios = Array.from({ length: 12 }).map((_, i) => ({
  id: i + 1,
  name: `示例舞室 ${i + 1}`,
  cover: '',
  city: '北京',
  area: '海淀区',
  distanceKm: +(0.3 + i * 0.6).toFixed(1),
  ratingAvg: +(4 + Math.random()).toFixed(1),
  reviewCount: 20 + i * 7,
  topStyles: ['Hiphop', 'Jazz', 'Breaking'].slice(0, (i % 3) + 1),
  beginnerFriendly: i % 2 === 0
}));

mock('get', /\/studios\/nearby/, () => ({
  list: studios,
  page: 1,
  pageSize: 20,
  total: studios.length
}));

mock('get', /\/studios\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const base = studios.find((s) => s.id === id) ?? studios[0];
  return {
    ...base,
    intro: '主打街舞与爵士的连锁舞室，零基础友好。',
    address: '北京市海淀区学院路 1 号',
    openHours: '10:00 - 22:00',
    photos: [],
    courses: []
  };
});
