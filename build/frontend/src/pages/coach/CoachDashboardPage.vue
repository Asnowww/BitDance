<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useOpsStore, type OpsRole } from '@/stores/ops';
import { fetchOpsDashboard, type OpsDashboard } from '@/api/coachOps';

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

const metrics = computed(() => {
  const d = data.value;
  const money = (v?: number | null) =>
    v == null ? '—' : v >= 10000 ? `¥${(v / 10000).toFixed(1)}w` : `¥${Number(v).toFixed(0)}`;
  return [
    { label: '本月收入', value: money(d?.monthIncome) },
    { label: '本月订单', value: d?.monthOrderCount ?? '—' },
    { label: '核销数', value: d?.checkinCount ?? '—' },
    { label: '退款数', value: d?.refundCount ?? '—' },
    { label: '课程预约', value: d?.courseBookingCount ?? '—' },
    { label: 'Workshop 报名', value: d?.workshopSignupCount ?? '—' },
    { label: '待回复评价', value: d?.pendingReviewReplies ?? '—' },
    { label: '平均评分', value: d?.avgRating != null ? Number(d.avgRating).toFixed(1) : '—' }
  ];
});

interface Entry {
  title: string;
  desc: string;
  path: string;
}

const studioEntries: Entry[] = [
  { title: '课程管理', desc: '课程创建 / 发布 / 下架', path: '/coach/courses' },
  { title: '周课表', desc: '排期与预约名单', path: '/coach/schedule' },
  { title: '订单中心', desc: '正式课 / Workshop / 退款', path: '/coach/orders' },
  { title: '签到核销', desc: '8 位核销码 / 扫码', path: '/coach/checkin' },
  { title: 'Workshop 管理', desc: '发布与审批', path: '/coach/workshops' },
  { title: '教练管理', desc: '邀请 / 分成 / 终止', path: '/coach/coaches' },
  { title: '评价回复', desc: '回复与申诉', path: '/coach/replies' },
  { title: '收益统计', desc: '应归属收入', path: '/coach/settlement' },
  { title: '舞室信息', desc: '入驻与认领进度', path: '/coach/studio-claim/status' }
];

const coachEntries: Entry[] = [
  { title: '我的邀请', desc: '舞室合作邀请确认', path: '/coach/invitations' },
  { title: '教练资质', desc: '资质提交与进度', path: '/coach/certification' },
  { title: 'Workshop 管理', desc: '创建与报名情况', path: '/coach/workshops' },
  { title: '签到核销', desc: '核销自己负责的订单', path: '/coach/checkin' },
  { title: '评价回复', desc: '回复自己相关评价', path: '/coach/replies' },
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
    <header class="hero">
      <button class="back" aria-label="返回" @click="router.push('/me')">‹</button>
      <p>OPERATIONS WORKSPACE</p>
      <h1>{{ heroTitle }}</h1>
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

      <section class="metrics" :class="{ dim: loading }">
        <article v-for="m in metrics" :key="m.label">
          <strong>{{ m.value }}</strong>
          <span>{{ m.label }}</span>
        </article>
      </section>
      <p v-if="loadError" class="load-error">
        看板数据加载失败
        <button @click="loadDashboard">重试</button>
      </p>
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

.hero {
  position: relative;
  border-radius: 30px;
  padding: 64px 20px 20px;
  background: linear-gradient(150deg, #2b2b2d, #111);
  color: #fff;
  p {
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
  margin-top: 18px;
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

.metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  overflow: hidden;
  margin-top: 16px;
  border-radius: 24px;
  background: $pen-hairline;
  transition: opacity 0.2s;
  &.dim {
    opacity: 0.5;
  }
  article {
    min-height: 76px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 10px 6px;
    background: $pen-soft;
    text-align: center;
    strong {
      font-size: 17px;
      font-weight: 900;
      line-height: 1.1;
    }
    span {
      margin-top: 6px;
      color: $pen-mute;
      font-size: 10.5px;
      font-weight: 800;
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
