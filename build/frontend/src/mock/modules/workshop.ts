import { mock } from '../index';

const ORDER_KEY = 'bitdance_mock_workshop_orders';
const WORKSHOP_KEY = 'bitdance_mock_workshops';

interface Session {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  capacity: number;
  taken: number;
  price: number;
}

interface Workshop {
  id: number;
  title: string;
  cover: string;
  city: string;
  area: string;
  styles: string[];
  startDate: string;
  endDate: string;
  priceMin: number;
  priceMax: number;
  capacity: number;
  taken: number;
  coachId: number;
  coachName: string;
  coachIntro: string;
  coachRating: number;
  studioId: number;
  studioName: string;
  studioAddress: string;
  studioTransportInfo: string;
  latitude: number;
  longitude: number;
  intro: string;
  pastReviews: Array<{ id: number; author: string; text: string; rating: number }>;
  sessions: Session[];
  status: 'PUBLISHED' | 'CLOSED' | 'CANCELED';
  hot: boolean;
}

interface Order {
  id: number;
  workshopId: number;
  workshopTitle: string;
  sessionId: number;
  sessionDate: string;
  sessionTime: string;
  amount: number;
  status: 'UNPAID' | 'PAID' | 'CANCELED' | 'REFUNDED' | 'CHECKED_IN' | 'COMPLETED';
  checkinCode: string;
  createdAt: number;
}

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const CITIES = ['北京', '上海', '广州', '深圳', '杭州'];
const AREAS = ['海淀区', '朝阳区', '浦东新区', '天河区', '南山区'];
const COACHES = ['Yumi', 'Leo', 'Aki', 'Mira', 'Bobo', 'Sara', 'Ken'];
const STYLE_ID: Record<string, number> = {
  Hiphop: 1,
  Jazz: 2,
  Breaking: 3,
  Locking: 4,
  Popping: 5,
  Kpop: 6,
  Waacking: 7
};

const seed = (): Workshop[] => {
  const out: Workshop[] = [];
  const today = new Date();
  for (let i = 1; i <= 14; i += 1) {
    const start = new Date(today);
    start.setDate(today.getDate() + 7 + i);
    const end = new Date(start);
    end.setDate(start.getDate() + 1);
    const sessions: Session[] = [];
    for (let s = 0; s < 2; s += 1) {
      const sd = new Date(start);
      sd.setDate(start.getDate() + s);
      sessions.push({
        id: i * 100 + s,
        date: sd.toISOString().slice(0, 10),
        startTime: ['14:00', '19:00'][s % 2],
        endTime: ['16:00', '21:00'][s % 2],
        capacity: 30,
        taken: 8 + (i % 12),
        price: 199 + (i % 5) * 50
      });
    }
    out.push({
      id: i,
      title: `${COACHES[i % COACHES.length]} ${STYLES[i % STYLES.length]} Workshop`,
      cover: '',
      city: CITIES[i % CITIES.length],
      area: AREAS[i % AREAS.length],
      styles: [STYLES[i % STYLES.length]],
      startDate: start.toISOString().slice(0, 10),
      endDate: end.toISOString().slice(0, 10),
      priceMin: Math.min(...sessions.map((s) => s.price)),
      priceMax: Math.max(...sessions.map((s) => s.price)),
      capacity: 60,
      taken: sessions.reduce((s, x) => s + x.taken, 0),
      coachId: 1000 + (i % 30),
      coachName: COACHES[i % COACHES.length],
      coachIntro: `${COACHES[i % COACHES.length]} 擅长以律动拆解和编舞复盘帮助舞者建立稳定表达。`,
      coachRating: 4.4 + ((i % 5) * 0.1),
      studioId: (i % 6) + 1,
      studioName: `舞星 Studio ${(i % 6) + 1}`,
      studioAddress: `${CITIES[i % CITIES.length]}${AREAS[i % AREAS.length]}灵动街区 ${(i % 9) + 1} 号`,
      studioTransportInfo: '地铁步行 5 分钟可达，场馆支持更衣与寄存。',
      latitude: 39.90 + i * 0.01,
      longitude: 116.40 + i * 0.01,
      intro: '一场围绕基础律动与编舞 routine 的深度课程，限定档期，适合有 1-3 个月经验的舞者。',
      pastReviews: [
        { id: 1, author: '小喵', text: '老师超棒，节奏抓得稳！', rating: 5 },
        { id: 2, author: '云朵', text: '内容紧凑，但建议增加一节复盘', rating: 4 }
      ],
      sessions,
      status: 'PUBLISHED',
      hot: i <= 3
    });
  }
  return out;
};

const loadWorkshops = (): Workshop[] => {
  try {
    const raw = localStorage.getItem(WORKSHOP_KEY);
    return raw ? (JSON.parse(raw) as Workshop[]) : seed();
  } catch {
    return seed();
  }
};
const saveWorkshops = (items: Workshop[]) => localStorage.setItem(WORKSHOP_KEY, JSON.stringify(items));

const seedOrders = (): Order[] => {
  const workshops = seed();
  const now = Date.now();
  const samples = [
    { status: 'UNPAID' as const, offset: 0 },
    { status: 'PAID' as const, offset: 1 },
    { status: 'COMPLETED' as const, offset: 2 },
    { status: 'REFUNDED' as const, offset: 3 }
  ];
  return samples.map(({ status, offset }) => {
    const w = workshops[offset];
    const session = w.sessions[0];
    const id = now - offset;
    return {
      id,
      workshopId: w.id,
      workshopTitle: w.title,
      sessionId: session.id,
      sessionDate: session.date,
      sessionTime: `${session.startTime}-${session.endTime}`,
      amount: session.price,
      status,
      checkinCode: status === 'PAID' || status === 'COMPLETED' ? `BD-${id.toString(36).toUpperCase().slice(-6)}` : '',
      createdAt: now - offset * 86400000
    };
  });
};

const loadOrders = (): Order[] => {
  try {
    const raw = localStorage.getItem(ORDER_KEY);
    return raw ? (JSON.parse(raw) as Order[]) : seedOrders();
  } catch {
    return seedOrders();
  }
};
const saveOrders = (items: Order[]) => localStorage.setItem(ORDER_KEY, JSON.stringify(items));

mock('get', /\/workshops$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  let items = loadWorkshops();
  if (p.cityId) items = items.filter((it) => CITIES[Number(p.cityId) - 1] === it.city);
  if (p.danceStyleId) {
    const styleName = Object.entries(STYLE_ID).find(([, id]) => id === Number(p.danceStyleId))?.[0];
    items = styleName ? items.filter((it) => it.styles.includes(styleName)) : [];
  }
  const page = Number(p.page ?? 1);
  const pageSize = Number(p.pageSize ?? 20);
  const start = (page - 1) * pageSize;
  return {
    list: items.slice(start, start + pageSize).map((item) => {
      const nextSession = item.sessions[0];
      return {
        ...item,
        workshopName: item.title,
        locationName: item.area,
        nextSessionStartAt: nextSession ? `${nextSession.date}T${nextSession.startTime}:00+08:00` : null,
        nextSessionEndAt: nextSession ? `${nextSession.date}T${nextSession.endTime}:00+08:00` : null,
        capacity: nextSession?.capacity ?? 0,
        soldCount: nextSession?.taken ?? 0
      };
    }),
    page,
    pageSize,
    total: items.length
  };
});

mock('get', /\/workshops\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  const workshop = loadWorkshops().find((it) => it.id === id);
  if (!workshop) return null;
  return {
    ...workshop,
    workshopName: workshop.title,
    locationName: workshop.studioName,
    address: workshop.studioAddress,
    reviewCount: workshop.pastReviews.length,
    reviewAverage:
      workshop.pastReviews.reduce((sum, review) => sum + review.rating, 0) / Math.max(1, workshop.pastReviews.length)
  };
});

mock('post', /\/(?:h5\/)?workshop-orders$/, ({ data }) => {
  const body = data as Record<string, unknown>;
  const ws = loadWorkshops();
  const w = ws.find((it) => it.id === Number(body.workshopId));
  if (!w) return null;
  const session = w.sessions.find((s) => s.id === Number(body.sessionId));
  if (!session) return null;
  if (session.taken >= session.capacity) return null;
  const orders = loadOrders();
  const order: Order = {
    id: Date.now(),
    workshopId: w.id,
    workshopTitle: w.title,
    sessionId: session.id,
    sessionDate: session.date,
    sessionTime: `${session.startTime}-${session.endTime}`,
    amount: session.price,
    status: 'UNPAID',
    checkinCode: '',
    createdAt: Date.now()
  };
  orders.unshift(order);
  saveOrders(orders);
  return order;
});

mock('post', /\/(?:h5\/)?workshop-orders\/\d+\/pay$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const orders = loadOrders();
  const idx = orders.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  if (orders[idx].status !== 'UNPAID') return orders[idx];
  orders[idx].status = 'PAID';
  orders[idx].checkinCode = `BD-${id.toString(36).toUpperCase().slice(-6)}`;
  saveOrders(orders);
  // 占座
  const ws = loadWorkshops();
  const w = ws.find((it) => it.id === orders[idx].workshopId);
  if (w) {
    const s = w.sessions.find((x) => x.id === orders[idx].sessionId);
    if (s && s.taken < s.capacity) {
      s.taken += 1;
      w.taken += 1;
      saveWorkshops(ws);
    }
  }
  return orders[idx];
});

mock('post', /\/(?:h5\/)?workshop-orders\/\d+\/cancel$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const orders = loadOrders();
  const idx = orders.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  if (orders[idx].status === 'PAID') return orders[idx]; // 已支付走退款
  orders[idx].status = 'CANCELED';
  saveOrders(orders);
  return orders[idx];
});

mock('post', /\/(?:h5\/)?workshop-orders\/\d+\/refund$/, ({ url }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const orders = loadOrders();
  const idx = orders.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  if (orders[idx].status !== 'PAID') return orders[idx];
  orders[idx].status = 'REFUNDED';
  saveOrders(orders);
  // 释放座位
  const ws = loadWorkshops();
  const w = ws.find((it) => it.id === orders[idx].workshopId);
  if (w) {
    const s = w.sessions.find((x) => x.id === orders[idx].sessionId);
    if (s && s.taken > 0) {
      s.taken -= 1;
      w.taken = Math.max(0, w.taken - 1);
      saveWorkshops(ws);
    }
  }
  return orders[idx];
});

mock('get', /\/(?:h5\/)?workshop-orders\/mine$/, () => loadOrders());

mock('get', /\/(?:h5\/)?workshop-orders\/\d+$/, ({ url }) => {
  const id = Number(url.split('/').pop());
  return loadOrders().find((it) => it.id === id) ?? null;
});

mock('post', /\/(?:h5\/)?workshop-orders\/\d+\/checkin$/, ({ url, data }) => {
  const id = Number(url.split('/').slice(-2)[0]);
  const body = (data ?? {}) as Record<string, unknown>;
  const orders = loadOrders();
  const idx = orders.findIndex((it) => it.id === id);
  if (idx < 0) return null;
  if (orders[idx].status !== 'PAID') return orders[idx];
  if (body.code !== orders[idx].checkinCode) return null;
  orders[idx].status = 'CHECKED_IN';
  saveOrders(orders);
  return orders[idx];
});

mock('get', /\/(?:h5\/)?workshop-calendar$/, () => {
  const workshops = loadWorkshops();
  const now = Date.now();
  return loadOrders()
    .filter((order) => ['PAID', 'CHECKED_IN', 'COMPLETED'].includes(order.status))
    .map((order) => {
      const workshop = workshops.find((item) => item.id === order.workshopId);
      const session = workshop?.sessions.find((item) => item.id === order.sessionId);
      const startAt = session ? `${session.date}T${session.startTime}:00+08:00` : new Date(now).toISOString();
      const endAt = session ? `${session.date}T${session.endTime}:00+08:00` : new Date(now + 7200000).toISOString();
      const startMs = Date.parse(startAt);
      let reminderStage = 'upcoming';
      let reminderTitle = '已加入日历';
      let reminderBody = '后续会在开场前继续提醒你。';
      if (now >= startMs - 24 * 60 * 60 * 1000 && now < startMs - 60 * 60 * 1000) {
        reminderStage = 'tomorrow';
        reminderTitle = '明日开跳';
        reminderBody = '记得提前安排出发时间和穿着装备。';
      } else if (now >= startMs - 60 * 60 * 1000 && now < Date.parse(endAt)) {
        reminderStage = 'starting_soon';
        reminderTitle = '即将开场';
        reminderBody = '已进入签到时间，打开签到页完成扫码。';
      } else if (now >= Date.parse(endAt)) {
        reminderStage = 'ended';
        reminderTitle = '活动已结束';
        reminderBody = '欢迎回到订单页补充评价与复盘。';
      }
      return {
        orderId: order.id,
        workshopId: order.workshopId,
        sessionId: order.sessionId,
        workshopName: order.workshopTitle,
        coachName: workshop?.coachName ?? '特邀导师',
        locationName: workshop?.studioName ?? '活动场地',
        address: workshop?.studioAddress ?? workshop?.area ?? '',
        orderStatus: order.status,
        amountPaid: order.amount,
        checkinCode: order.checkinCode || null,
        startAt,
        endAt,
        reminderStage,
        reminderTitle,
        reminderBody,
        allowCheckin: now >= startMs - 60 * 60 * 1000 && now < Date.parse(endAt)
      };
    });
});
