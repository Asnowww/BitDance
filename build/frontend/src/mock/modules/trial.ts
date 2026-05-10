import { mock } from '../index';

const KEY = 'bitdance_mock_trial_bookings';

const load = (): unknown[] => {
  try {
    return JSON.parse(localStorage.getItem(KEY) ?? '[]');
  } catch {
    return [];
  }
};
const save = (items: unknown[]) => localStorage.setItem(KEY, JSON.stringify(items));

mock('post', /\/trial-bookings$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const id = Date.now();
  const item = {
    id,
    studioId: body.studioId,
    studioName: `舞星 Studio ${body.studioId}`,
    courseId: body.courseId,
    courseName: body.courseId ? '试听课程' : undefined,
    coachId: body.coachId,
    coachName: body.coachId ? '试听教练' : undefined,
    date: body.date,
    time: body.time,
    contactPhone: body.contactPhone,
    remark: body.remark,
    status: 'pending',
    createdAt: id
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('get', /\/trial-bookings\/mine$/, () => load());

mock('post', /\/trial-bookings\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = (items as Array<Record<string, unknown>>).findIndex((it) => it.id === id);
  if (idx >= 0) {
    (items as Array<Record<string, unknown>>)[idx].status = 'canceled';
    save(items);
    return { canceled: true };
  }
  return { canceled: false };
});

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const WEEKDAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const TIMES = ['10:00-11:00', '14:00-15:00', '19:00-20:00', '20:00-21:00'];

mock('get', /\/studios\/\d+\/schedule$/, ({ url }) => {
  const studioId = Number(url.split('/').slice(-2)[0]);
  const slots = [];
  let id = 0;
  const today = new Date();
  for (let d = 0; d < 7; d += 1) {
    const date = new Date(today);
    date.setDate(today.getDate() + d);
    const dateStr = date.toISOString().slice(0, 10);
    for (let t = 0; t < 2 + (d % 2); t += 1) {
      const sIdx = (d * 3 + t) % STYLES.length;
      slots.push({
        id: studioId * 1000 + id++,
        date: dateStr,
        weekday: WEEKDAYS[(today.getDay() + d) % 7],
        time: TIMES[(d + t) % TIMES.length],
        courseId: studioId * 100 + sIdx,
        courseName: `${STYLES[sIdx]} 入门班`,
        style: STYLES[sIdx],
        difficulty: t % 2 === 0 ? '入门' : '进阶',
        coachName: ['Yumi', 'Leo', 'Aki', 'Mira'][sIdx % 4],
        capacity: 12,
        taken: 4 + ((d + t) % 8)
      });
    }
  }
  return slots;
});
