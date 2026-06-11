<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { Bell } from 'lucide-vue-next';
import { useOpsStore, type OpsRole } from '@/stores/ops';
import merchantHero from '@/assets/pencil/merchant-ops-hero.jpg';
import {
  fetchOpsDashboard,
  fetchMerchantCourseOrders,
  fetchMerchantWorkshopOrders,
  type OpsDashboard,
  type CourseOrder,
  type WorkshopOrder
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const { activeRole, studioId, approvedStudioIds, availableRoles, isCertifiedCoach } =
  storeToRefs(ops);

const data = ref<OpsDashboard | null>(null);
const loading = ref(false);
const loadError = ref(false);

const roleLabels: Record<OpsRole, string> = {
  studio_admin: '舞室管理员',
  coach: '教练',
  platform: '平台管理员'
};

const heroTitle = computed(() => {
  if (activeRole.value === 'studio_admin') return '商家工作台';
  if (activeRole.value === 'platform') return '平台审核台';
  return '教练工作台';
});

const activeStudioName = computed(() => {
  return studioId.value ? `舞室 #${studioId.value}` : '已认证舞室';
});

const photoHeaderSubtitle = computed(() => {
  if (activeRole.value === 'studio_admin') return `舞室管理员 · ${activeStudioName.value}`;
  return isCertifiedCoach.value ? '认证教练 · 个人经营与履约' : '教练 · 资质与合作管理';
});

// ---------- 核心指标(2×2 大卡) ----------
const money = (v?: number | null) =>
  v == null ? '—' : v >= 10000 ? `¥${(v / 10000).toFixed(1)}w` : `¥${Number(v).toLocaleString('zh-CN')}`;

const bigMetrics = computed(() => {
  const d = data.value;
  return [
    { label: '本月收入', value: money(d?.monthIncome), sub: '正式课 + Workshop' },
    { label: '订单量', value: d?.monthOrderCount ?? '—', sub: `课程 ${d?.courseBookingCount ?? 0} · 活动 ${d?.workshopSignupCount ?? 0}` },
    { label: '核销 / 退款', value: `${d?.checkinCount ?? '—'} / ${d?.refundCount ?? '—'}`, sub: '本月累计' },
    {
      label: '待回复评价',
      value: d?.pendingReviewReplies ?? '—',
      sub: d?.avgRating != null ? `平均评分 ${Number(d.avgRating).toFixed(1)}` : '暂无评分',
      alert: (d?.pendingReviewReplies ?? 0) > 0
    }
  ];
});

// ---------- 图表数据(由真实订单聚合) ----------
interface PaidLike {
  paidAt: string | null;
  amountPaid: number;
  orderStatus: string;
}

const courseOrders = ref<CourseOrder[]>([]);
const workshopOrders = ref<WorkshopOrder[]>([]);
const chartsAvailable = ref(false);

const INCOME_STATUSES = ['paid', 'checked_in', 'completed'];
const isIncome = (o: PaidLike) => o.paidAt && INCOME_STATUSES.includes(o.orderStatus);

/** 近 7 日收入柱状图 */
const trend = computed(() => {
  const days: { label: string; amount: number; isToday: boolean }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const next = new Date(d);
    next.setDate(next.getDate() + 1);
    const all: PaidLike[] = [...courseOrders.value, ...workshopOrders.value];
    const amount = all
      .filter(isIncome)
      .filter((o) => {
        const t = new Date(o.paidAt as string);
        return t >= d && t < next;
      })
      .reduce((s, o) => s + Number(o.amountPaid), 0);
    days.push({ label: `${d.getMonth() + 1}/${d.getDate()}`, amount, isToday: i === 0 });
  }
  const max = Math.max(...days.map((x) => x.amount), 1);
  return { days, max, total: days.reduce((s, x) => s + x.amount, 0) };
});

/** 收入构成:课程 vs Workshop */
const split = computed(() => {
  const course = courseOrders.value.filter(isIncome).reduce((s, o) => s + Number(o.amountPaid), 0);
  const workshop = workshopOrders.value.filter(isIncome).reduce((s, o) => s + Number(o.amountPaid), 0);
  const total = course + workshop;
  return {
    course,
    workshop,
    total,
    coursePct: total > 0 ? Math.round((course / total) * 100) : 0
  };
});

/** 核销率:已核销 / 已支付族订单 */
const checkinRate = computed(() => {
  const all: PaidLike[] = [...courseOrders.value, ...workshopOrders.value];
  const paidFamily = all.filter((o) => INCOME_STATUSES.includes(o.orderStatus));
  const checked = paidFamily.filter((o) => ['checked_in', 'completed'].includes(o.orderStatus));
  return {
    pct: paidFamily.length > 0 ? Math.round((checked.length / paidFamily.length) * 100) : 0,
    checked: checked.length,
    paid: paidFamily.length
  };
});

const loadCharts = async () => {
  chartsAvailable.value = false;
  if (activeRole.value !== 'studio_admin' || !studioId.value) return;
  try {
    const [c, w] = await Promise.all([
      fetchMerchantCourseOrders(studioId.value).catch(() => [] as CourseOrder[]),
      fetchMerchantWorkshopOrders({ studioId: studioId.value }).catch(() => [] as WorkshopOrder[])
    ]);
    courseOrders.value = c;
    workshopOrders.value = w;
    chartsAvailable.value = c.length + w.length > 0;
  } catch {
    chartsAvailable.value = false;
  }
};

// ---------- 管理入口 ----------
interface Entry {
  title: string;
  desc: string;
  path: string;
}

const studioEntries: Entry[] = [
  { title: '课程管理', desc: '课程创建 / 发布 / 下架', path: '/coach/courses' },
  { title: '周课表', desc: '排期与预约名单', path: '/coach/schedule' },
  { title: '订单中心', desc: '正式课 / Workshop / 退款', path: '/coach/orders' },
  { title: 'Workshop 管理', desc: '发布与审批', path: '/coach/workshops' },
  { title: '教练管理', desc: '邀请 / 分成 / 终止', path: '/coach/coaches' },
  { title: '收益统计', desc: '应归属收入', path: '/coach/settlement' },
  { title: '舞室信息', desc: '入驻与认领进度', path: '/coach/studio-claim/status' }
];

const coachEntries: Entry[] = [
  { title: '我的邀请', desc: '舞室合作邀请确认', path: '/coach/invitations' },
  { title: '教练资质', desc: '资质提交与进度', path: '/coach/certification' },
  { title: 'Workshop 管理', desc: '创建与报名情况', path: '/coach/workshops' },
  { title: '收益统计', desc: '我的应归属收入', path: '/coach/settlement' },
  { title: '教练主页', desc: '介绍 / 风格 / 可约时段', path: '/me/coach-home' }
];

const platformEntries: Entry[] = [
  { title: '平台审核中心', desc: '舞室 / 教练 / Workshop / 申诉', path: '/coach/platform/reviews' }
];

const entries = computed(() => {
  if (activeRole.value === 'studio_admin') return studioEntries;
  if (activeRole.value === 'platform') return platformEntries;
  return coachEntries;
});

/** 快捷操作 pill(对照设计稿:新建课程 / 扫码核销 / 回复评价) */
const quickPills = computed(() => {
  if (activeRole.value === 'studio_admin') {
    return [
      { label: '新建课程', path: '/coach/course-edit', primary: true },
      { label: '扫码核销', path: '/coach/checkin', primary: false },
      { label: '回复评价', path: '/coach/replies', primary: false }
    ];
  }
  return [
    { label: '创建 Workshop', path: '/coach/workshop-create', primary: true },
    { label: '扫码核销', path: '/coach/checkin', primary: false },
    { label: '回复评价', path: '/coach/replies', primary: false }
  ];
});

/** 工作台未开通时的引导 */
const onboarding = computed(() => {
  if (activeRole.value === 'studio_admin' && approvedStudioIds.value.length === 0) {
    return {
      title: '尚未开通商家后台',
      desc: '提交舞室认领或新舞室入驻,通过平台审核后即可管理舞室。',
      action: '去入驻 / 认领',
      path: '/coach/studio-claim'
    };
  }
  if (activeRole.value === 'coach' && !isCertifiedCoach.value) {
    return {
      title: '尚未获得教练身份',
      desc: '提交教练资质审核,或等待舞室管理员邀请绑定。',
      action: '提交教练资质',
      path: '/coach/certification'
    };
  }
  return null;
});

const loadDashboard = async () => {
  if (activeRole.value === 'platform') return;
  loading.value = true;
  loadError.value = false;
  try {
    data.value = await fetchOpsDashboard({
      role: activeRole.value,
      studioId: activeRole.value === 'studio_admin' ? (studioId.value ?? undefined) : undefined
    });
  } catch {
    loadError.value = true;
  } finally {
    loading.value = false;
  }
  loadCharts();
};

const switchRole = (role: OpsRole) => {
  ops.setRole(role);
};

watch([activeRole, studioId], loadDashboard);

onMounted(async () => {
  await ops.refresh();
  await loadDashboard();
});
</script>

<template>
  <main class="dash">
    <header v-if="activeRole !== 'platform'" class="merchant-hero">
      <div class="merchant-topbar">
        <div>
          <h1>{{ activeRole === 'studio_admin' ? '运营工作台' : '教练工作台' }}</h1>
          <p>{{ photoHeaderSubtitle }}</p>
        </div>
        <button type="button" aria-label="消息通知" @click="router.push('/messages')">
          <Bell :size="20" :stroke-width="2.2" />
        </button>
      </div>

      <div class="merchant-photo" :style="{ backgroundImage: `url(${merchantHero})` }">
        <p>TODAY OPERATIONS</p>
        <h2>收入、核销、评价待办一屏处理</h2>
      </div>

      <div class="merchant-role-switch">
        <button
          v-for="r in availableRoles"
          :key="r"
          :class="{ active: activeRole === r }"
          @click="switchRole(r)"
        >
          {{ roleLabels[r] }}
        </button>
      </div>
    </header>

    <header v-else class="hero">
      <button class="back" aria-label="返回" @click="router.push('/coach/me')">‹</button>
      <p class="hero-kicker">OPERATIONS WORKSPACE</p>
      <h1>{{ heroTitle }}</h1>
      <p class="hero-tagline">收入、核销、评价 待办一屏处理</p>
      <div class="role-switch">
        <button
          v-for="r in availableRoles"
          :key="r"
          :class="{ active: activeRole === r }"
          @click="switchRole(r)"
        >
          {{ roleLabels[r] }}
        </button>
      </div>
    </header>

    <section v-if="onboarding" class="onboard">
      <h2>{{ onboarding.title }}</h2>
      <p>{{ onboarding.desc }}</p>
      <button @click="router.push(onboarding.path)">{{ onboarding.action }}</button>
    </section>

    <template v-else-if="activeRole !== 'platform'">
      <section v-if="activeRole === 'studio_admin' && approvedStudioIds.length > 1" class="studio-pick">
        <button
          v-for="id in approvedStudioIds"
          :key="id"
          :class="{ active: studioId === id }"
          @click="ops.setStudio(id)"
        >
          舞室 #{{ id }}
        </button>
      </section>

      <!-- 2×2 大数字指标卡 -->
      <section class="metric-grid" :class="{ dim: loading }">
        <article v-for="m in bigMetrics" :key="m.label" :class="{ alert: m.alert }">
          <strong>{{ m.value }}</strong>
          <span>{{ m.label }}</span>
          <small>{{ m.sub }}</small>
        </article>
      </section>
      <p v-if="loadError" class="load-error">
        看板数据加载失败
        <button @click="loadDashboard">重试</button>
      </p>

      <!-- 图表:近 7 日收入 -->
      <section v-if="chartsAvailable" class="chart-card">
        <div class="chart-head">
          <h2>近 7 日收入</h2>
          <span>{{ money(trend.total) }}</span>
        </div>
        <div class="bars">
          <div v-for="d in trend.days" :key="d.label" class="bar-col">
            <em v-if="d.amount > 0">{{ d.amount >= 1000 ? (d.amount / 1000).toFixed(1) + 'k' : d.amount }}</em>
            <div class="bar-track">
              <div
                class="bar-fill"
                :class="{ today: d.isToday }"
                :style="{ height: Math.max((d.amount / trend.max) * 100, d.amount > 0 ? 6 : 2) + '%' }"
              />
            </div>
            <span>{{ d.label }}</span>
          </div>
        </div>
      </section>

      <!-- 图表:收入构成 + 核销率 -->
      <section v-if="chartsAvailable" class="chart-card">
        <div class="chart-head">
          <h2>收入构成</h2>
          <span>{{ money(split.total) }}</span>
        </div>
        <div class="stacked-bar">
          <div class="seg-course" :style="{ width: split.coursePct + '%' }" />
          <div class="seg-workshop" :style="{ width: 100 - split.coursePct + '%' }" />
        </div>
        <div class="legend">
          <span><i class="dot ink" />正式课 {{ money(split.course) }}</span>
          <span><i class="dot gray" />Workshop {{ money(split.workshop) }}</span>
        </div>

        <div class="chart-head rate-head">
          <h2>核销率</h2>
          <span>{{ checkinRate.checked }}/{{ checkinRate.paid }} 单</span>
        </div>
        <div class="rate-bar">
          <div class="rate-fill" :style="{ width: checkinRate.pct + '%' }" />
        </div>
        <p class="rate-pct">{{ checkinRate.pct }}%</p>
      </section>

      <!-- 快捷操作 pill -->
      <section class="quick-pills">
        <button
          v-for="q in quickPills"
          :key="q.label"
          :class="{ primary: q.primary }"
          @click="router.push(q.path)"
        >
          {{ q.label }}
        </button>
      </section>
    </template>

    <section class="panel">
      <div class="panel-head">
        <h2>管理入口</h2>
        <span>MANAGE</span>
      </div>
      <button v-for="e in entries" :key="e.title" class="entry" @click="router.push(e.path)">
        <span class="entry-main">
          <strong>{{ e.title }}</strong>
          <small>{{ e.desc }}</small>
        </span>
        <em>›</em>
      </button>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.dash {
  @include ops-page;
  padding: 18px 18px 40px;
}

.merchant-hero {
  margin: -18px -18px 0;
  background: $pen-canvas;
}

.merchant-topbar {
  min-height: 84px;
  padding: 14px 18px 12px;
  border-bottom: 1px solid $pen-hairline;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  box-sizing: border-box;

  h1,
  p {
    margin: 0;
  }

  h1 {
    color: $pen-ink;
    font-size: 28px;
    font-weight: 900;
    line-height: 1.15;
    letter-spacing: -0.03em;
  }

  p {
    margin-top: 4px;
    color: $pen-mute;
    font-size: 12.5px;
    font-weight: 600;
  }

  button {
    flex: 0 0 auto;
    width: 44px;
    height: 44px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }
}

.merchant-photo {
  min-height: 164px;
  margin: 20px 18px 0;
  border-radius: 26px;
  padding: 20px;
  background-color: $pen-ink;
  background-position: center 43%;
  background-size: cover;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
  box-sizing: border-box;
  position: relative;
  isolation: isolate;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    z-index: -1;
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.58), rgba(0, 0, 0, 0.08) 78%);
  }

  p,
  h2 {
    margin: 0;
  }

  p {
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 0.02em;
  }

  h2 {
    max-width: 390px;
    margin-top: 14px;
    font-size: 26px;
    font-weight: 900;
    line-height: 1.22;
    letter-spacing: -0.03em;
  }
}

.merchant-role-switch {
  display: flex;
  gap: 8px;
  padding: 14px 18px 0;
  overflow-x: auto;

  button {
    flex: 0 0 auto;
    height: 40px;
    padding: 0 18px;
    border: 1px solid $pen-hairline-strong;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12.5px;
    font-weight: 800;
    cursor: pointer;

    &.active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: #fff;
    }
  }
}

.hero {
  position: relative;
  border-radius: 30px;
  padding: 64px 20px 20px;
  background: linear-gradient(150deg, #2b2b2d, #111);
  color: #fff;
  .hero-kicker {
    margin: 0 0 6px;
    color: #9e9ea0;
    font-size: 11px;
    font-weight: 900;
    letter-spacing: 0.08em;
  }
  h1 {
    margin: 0;
    font-size: 34px;
    font-weight: 900;
    line-height: 1;
  }
  .hero-tagline {
    margin: 10px 0 0;
    color: $pen-hairline-strong;
    font-size: 12.5px;
    font-weight: 700;
  }
}

.back {
  position: absolute;
  top: 14px;
  left: 14px;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
}

.role-switch {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  button {
    height: 36px;
    padding: 0 16px;
    border: 1px solid rgba(255, 255, 255, 0.32);
    border-radius: 999px;
    background: transparent;
    color: #fff;
    font-size: 12.5px;
    font-weight: 800;
    cursor: pointer;
    &.active {
      border-color: #fff;
      background: #fff;
      color: $pen-ink;
    }
  }
}

.onboard {
  margin-top: 16px;
  border-radius: 24px;
  background: $pen-soft;
  padding: 24px 20px;
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 900;
  }
  p {
    margin: 8px 0 16px;
    color: $pen-mute;
    font-size: 13px;
    line-height: 1.5;
  }
  button {
    @include pen-primary-btn;
  }
}

.studio-pick {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-top: 14px;
  button {
    @include pen-chip;
    border: 1px solid $pen-hairline;
    background: $pen-canvas;
    color: $pen-ink;
    &.active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: #fff;
    }
  }
}

// 2×2 大数字指标卡(对照设计稿)
.metric-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 16px;
  transition: opacity 0.2s;
  &.dim {
    opacity: 0.5;
  }
  article {
    border-radius: 22px;
    background: $pen-soft;
    padding: 16px;
    strong {
      display: block;
      font-size: 26px;
      font-weight: 900;
      line-height: 1.05;
      letter-spacing: -0.01em;
    }
    span {
      display: block;
      margin-top: 8px;
      color: $pen-mute;
      font-size: 11.5px;
      font-weight: 800;
    }
    small {
      display: block;
      margin-top: 2px;
      color: #9e9ea0;
      font-size: 10.5px;
      font-weight: 600;
    }
    &.alert strong {
      color: #d30005;
    }
  }
}

.load-error {
  margin: 10px 0 0;
  color: #d30005;
  font-size: 12.5px;
  font-weight: 700;
  button {
    margin-left: 8px;
    border: 0;
    border-radius: 999px;
    padding: 4px 12px;
    background: $pen-ink;
    color: #fff;
    font-size: 12px;
    font-weight: 800;
    cursor: pointer;
  }
}

// 图表卡
.chart-card {
  margin-top: 12px;
  border-radius: 22px;
  background: $pen-soft;
  padding: 16px;
}

.chart-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
  h2 {
    margin: 0;
    font-size: 14px;
    font-weight: 900;
  }
  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 800;
  }
  &.rate-head {
    margin-top: 18px;
  }
}

// 近 7 日柱状图
.bars {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  align-items: end;
}

.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  em {
    font-style: normal;
    font-size: 9.5px;
    font-weight: 800;
    color: $pen-charcoal;
  }
  .bar-track {
    width: 100%;
    height: 88px;
    display: flex;
    align-items: flex-end;
    border-radius: 8px;
    background: $pen-canvas;
    overflow: hidden;
  }
  .bar-fill {
    width: 100%;
    border-radius: 8px 8px 0 0;
    background: $pen-hairline-strong;
    transition: height 0.4s ease;
    &.today {
      background: $pen-ink;
    }
  }
  span {
    color: $pen-mute;
    font-size: 9.5px;
    font-weight: 700;
  }
}

// 收入构成堆叠条
.stacked-bar {
  display: flex;
  height: 14px;
  border-radius: 999px;
  overflow: hidden;
  background: $pen-canvas;
  .seg-course {
    background: $pen-ink;
  }
  .seg-workshop {
    background: $pen-hairline-strong;
  }
}

.legend {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: $pen-charcoal;
    font-size: 11.5px;
    font-weight: 700;
  }
  .dot {
    width: 10px;
    height: 10px;
    border-radius: 999px;
    &.ink {
      background: $pen-ink;
    }
    &.gray {
      background: $pen-hairline-strong;
    }
  }
}

// 核销率
.rate-bar {
  height: 10px;
  border-radius: 999px;
  background: $pen-canvas;
  overflow: hidden;
  .rate-fill {
    height: 100%;
    border-radius: 999px;
    background: $pen-success;
    transition: width 0.4s ease;
  }
}

.rate-pct {
  margin: 8px 0 0;
  font-size: 13px;
  font-weight: 900;
  color: $pen-success;
}

// 快捷操作 pill
.quick-pills {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  button {
    flex: 1;
    height: 44px;
    border: 1px solid $pen-hairline-strong;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;
    white-space: nowrap;
    &.primary {
      border-color: $pen-ink;
      background: $pen-ink;
      color: #fff;
    }
  }
}

.panel {
  margin-top: 26px;
}

.panel-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 12px;
  h2 {
    @include pen-h3-section;
  }
  span {
    color: $pen-mute;
    font-size: 11px;
    font-weight: 900;
  }
}

.entry {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  border: 0;
  border-radius: 22px;
  padding: 14px 16px;
  background: $pen-soft;
  text-align: left;
  cursor: pointer;
  .entry-main {
    display: flex;
    flex-direction: column;
    gap: 3px;
    strong {
      font-size: 15px;
      font-weight: 900;
      color: $pen-ink;
    }
    small {
      font-size: 12px;
      font-weight: 600;
      color: $pen-mute;
    }
  }
  em {
    font-style: normal;
    color: $pen-mute;
    font-size: 20px;
  }
}
</style>
