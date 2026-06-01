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

mock('post', /\/h5\/trial-bookings$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const items = load();
  const id = Date.now();
  const item = {
    id,
    userId: 1,
    courseId: body.courseId,
    courseScheduleId: body.courseScheduleId,
    studioId: Math.max(1, Math.floor(Number(body.courseId) / 100)),
    contactPhone: body.contactPhone,
    bookingNote: body.bookingNote,
    bookingStatus: 'pending',
    createdAt: new Date(id).toISOString()
  };
  items.unshift(item);
  save(items);
  return item;
});

mock('get', /\/h5\/trial-bookings$/, () => load());

mock('post', /\/h5\/trial-bookings\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const items = load();
  const idx = (items as Array<Record<string, unknown>>).findIndex((it) => it.id === id);
  if (idx >= 0) {
    (items as Array<Record<string, unknown>>)[idx].bookingStatus = 'canceled';
    save(items);
    return { canceled: true };
  }
  return { canceled: false };
});

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];
const WEEKDAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const TIMES = ['10:00-11:00', '14:00-15:00', '19:00-20:00', '20:00-21:00'];

mock('get', /\/public\/studios\/\d+\/schedules$/, ({ url }) => {
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
        courseId: studioId * 100 + sIdx,
        studioId,
        coachId: 1000 + sIdx,
        classroomName: `${t + 1} 号厅`,
        startAt: `${dateStr}T${TIMES[(d + t) % TIMES.length].slice(0, 5)}:00+08:00`,
        endAt: `${dateStr}T${TIMES[(d + t) % TIMES.length].slice(6)}:00+08:00`,
        capacity: 12,
        bookedCount: 4 + ((d + t) % 8),
        status: 'scheduled'
      });
    }
  }
  return slots;
});
