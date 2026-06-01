import { mock } from '../index';

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];

mock('get', /\/courses\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const styleIdx = id % STYLES.length;
  return {
    id,
    courseName: `${STYLES[styleIdx]} 入门班`,
    studioId: Math.max(1, Math.floor(id / 100)),
    danceStyleId: styleIdx + 1,
    difficultyLevel: id % 2 === 0 ? '入门' : '进阶',
    targetAudience: id % 3 === 0 ? '零基础' : '成人',
    durationMinutes: 60 + (id % 4) * 15,
    intensityLevel: ['低', '中', '高'][id % 3],
    priceAmount: 99 + (id % 5) * 30,
    courseType: 'group',
    zeroBasicFriendly: id % 2 === 0,
    description: '从基础律动到 Routine，循序渐进；老师耐心细致，零基础友好。',
    coachId: 1000 + (id % 30),
    status: 'published',
    favored: false
  };
});

mock('get', /\/coaches\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const styleIdx = id % STYLES.length;
  return {
    id,
    userId: id + 1000,
    displayName: ['Yumi', 'Leo', 'Aki', 'Mira', 'Bobo'][id % 5],
    teachingStyle: '注重基础律动与节奏感培养，鼓励学生表达自己的风格。',
    intro: '7 年舞龄，5 年教学经验，曾参演多场 Showcase 与街舞综艺。',
    availableTimeSlots: '周一/周三 19:00-20:30，周六 14:00-16:00',
    certificationStatus: 'certified',
    homeStudioId: Math.max(1, id % 80),
    avgRating: +(4.4 + (id % 6) * 0.1).toFixed(1),
    styles: [{ danceStyleId: styleIdx + 1, proficiencyLevel: 'advanced' }],
    favored: false
  };
});
