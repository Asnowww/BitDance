import { mock } from '../index';

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];

mock('get', /\/courses\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const styleIdx = id % STYLES.length;
  return {
    id,
    name: `${STYLES[styleIdx]} 入门班`,
    studioId: Math.max(1, Math.floor(id / 100)),
    studioName: `舞星 Studio ${Math.max(1, Math.floor(id / 100))}`,
    style: STYLES[styleIdx],
    difficulty: id % 2 === 0 ? '入门' : '进阶',
    audience: id % 3 === 0 ? '零基础' : '成人',
    durationMin: 60 + (id % 4) * 15,
    intensity: 1 + (id % 5),
    price: 99 + (id % 5) * 30,
    frequency: ['每周一/三/五', '每周二/四', '周末双休'][id % 3],
    intro: '从基础律动到 Routine，循序渐进；老师耐心细致，零基础友好。',
    coverDesc: STYLES[styleIdx],
    coachId: 1000 + (id % 30),
    coachName: ['Yumi', 'Leo', 'Aki', 'Mira', 'Bobo'][id % 5]
  };
});

mock('get', /\/coaches\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const styleIdx = id % STYLES.length;
  return {
    id,
    name: ['Yumi', 'Leo', 'Aki', 'Mira', 'Bobo'][id % 5],
    studioId: Math.max(1, id % 80),
    studioName: `舞星 Studio ${Math.max(1, id % 80)}`,
    style: STYLES[styleIdx],
    teachStyle: '注重基础律动与节奏感培养，鼓励学生表达自己的风格。',
    intro: '7 年舞龄，5 年教学经验，曾参演多场 Showcase 与街舞综艺。',
    ratingAvg: +(4.4 + (id % 6) * 0.1).toFixed(1),
    reviewCount: 30 + (id % 50) * 4,
    works: Array.from({ length: 4 }).map((_, i) => ({
      id: id * 10 + i,
      type: i % 2 === 0 ? 'image' : 'video',
      title: `作品 ${i + 1}`
    })),
    courses: Array.from({ length: 3 }).map((_, i) => ({
      id: id * 100 + i,
      name: `${STYLES[styleIdx]} 第${i + 1}阶段`,
      difficulty: ['入门', '初级', '进阶'][i]
    })),
    availableSlots: [
      { day: '周一', time: '19:00-20:30' },
      { day: '周三', time: '19:00-20:30' },
      { day: '周六', time: '14:00-16:00' }
    ]
  };
});
