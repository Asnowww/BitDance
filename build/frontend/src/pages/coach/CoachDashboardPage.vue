<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Home, MessageSquareText, PenLine, RefreshCw, Star } from 'lucide-vue-next';
import { fetchCoachDashboard, type CoachDashboard } from '@/api/coachOps';

const router = useRouter();
const data = ref<CoachDashboard | null>(null);
const loading = ref(false);

const load = async () => {
  loading.value = true;
  try {
    data.value = await fetchCoachDashboard();
  } finally {
    loading.value = false;
  }
};

const quickActions = [
  { title: '个人主页', desc: '维护学员可见资料', icon: Home, path: '/me/coach-home' },
  { title: '回复评价', desc: '处理老师评价反馈', icon: MessageSquareText, path: '/coach/replies' },
  { title: '发布课程', desc: '创建可预约课程内容', icon: PenLine, path: '/coach/workshop-create' }
];

const metrics = computed(() => [
  { value: data.value?.monthSessions ?? 0, label: '本月课时' },
  { value: data.value?.pendingReviewReplies ?? data.value?.pendingReplies ?? 0, label: '待回复评价' },
  { value: data.value?.ratingCount ?? 0, label: '评价数' },
  { value: Number(data.value?.avgRating ?? data.value?.ratingAvg ?? 0).toFixed(1), label: '老师评分' }
]);

onMounted(load);
</script>

<template>
  <main class="coach-page">
    <header class="hero">
      <button class="back" type="button" aria-label="返回" @click="router.back()">‹</button>
      <span>COACH WORKSPACE</span>
      <h1>教练工作台</h1>
      <p>{{ loading ? '正在同步后端数据' : '管理主页、评价与可预约课程' }}</p>
    </header>

    <section class="metric-grid" aria-label="教练经营数据">
      <article v-for="item in metrics" :key="item.label" class="metric">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </article>
    </section>

    <section class="panel">
      <div class="section-head">
        <div>
          <h2>教练权限</h2>
          <p>来自 /h5/coach/dashboard 的实时数据</p>
        </div>
        <button type="button" aria-label="刷新教练数据" @click="load">
          <RefreshCw :size="17" :stroke-width="2.3" />
        </button>
      </div>
      <button v-for="item in quickActions" :key="item.title" class="action-row" type="button" @click="router.push(item.path)">
        <span class="action-row__icon">
          <component :is="item.icon" :size="19" :stroke-width="2.3" />
        </span>
        <span class="action-row__copy">
          <strong>{{ item.title }}</strong>
          <em>{{ item.desc }}</em>
        </span>
        <span class="action-row__arrow">›</span>
      </button>
    </section>

    <section class="rating-panel">
      <Star :size="20" :stroke-width="2.3" />
      <div>
        <strong>{{ Number(data?.avgRating ?? data?.ratingAvg ?? 0).toFixed(1) }}</strong>
        <span>{{ data?.ratingCount ?? 0 }} 条公开评价，{{ data?.pendingReviewReplies ?? data?.pendingReplies ?? 0 }} 条待回复</span>
      </div>
    </section>
  </main>
</template>

<style lang="scss" scoped>
.coach-page {
  min-height: 100vh;
  padding: 18px 18px 32px;
  background: #fff;
  color: #111;
  box-sizing: border-box;
}

.hero {
  position: relative;
  min-height: 172px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px;
  background: #111;
  color: #fff;
  box-sizing: border-box;

  span,
  h1,
  p {
    margin: 0;
    letter-spacing: 0;
  }

  span {
    color: #d8d8d8;
    font-size: 11px;
    line-height: 1.2;
    font-weight: 900;
  }

  h1 {
    margin-top: 8px;
    font-size: 36px;
    line-height: 1.02;
    font-weight: 900;
  }

  p {
    margin-top: 8px;
    color: #d8d8d8;
    font-size: 13px;
    line-height: 1.35;
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
  background: #fff;
  color: #111;
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  margin-top: 14px;
  background: #111;
}

.metric {
  min-height: 88px;
  padding: 10px 8px;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  box-sizing: border-box;

  strong {
    font-size: 23px;
    line-height: 1;
    font-weight: 900;
  }

  span {
    color: #707072;
    font-size: 11px;
    line-height: 1.2;
    font-weight: 800;
  }
}

.panel {
  margin-top: 22px;
}

.section-head {
  min-height: 44px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 12px;

  div {
    min-width: 0;
    flex: 1;
  }

  h2,
  p {
    margin: 0;
    letter-spacing: 0;
  }

  h2 {
    font-size: 22px;
    line-height: 1.2;
    font-weight: 900;
  }

  p {
    margin-top: 3px;
    color: #707072;
    font-size: 12px;
    line-height: 1.3;
    font-weight: 700;
  }

  button {
    width: 40px;
    height: 40px;
    border: 0;
    border-radius: 999px;
    background: #f5f5f5;
    color: #111;
    display: grid;
    place-items: center;
    cursor: pointer;
  }
}

.action-row {
  width: 100%;
  min-height: 68px;
  padding: 10px 0;
  border: 0;
  border-bottom: 1px solid #e5e5e5;
  background: #fff;
  color: #111;
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  cursor: pointer;
}

.action-row__icon {
  width: 42px;
  height: 42px;
  flex: none;
  border-radius: 999px;
  background: #111;
  color: #fff;
  display: grid;
  place-items: center;
}

.action-row__copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong,
  em {
    overflow-wrap: anywhere;
    letter-spacing: 0;
  }

  strong {
    font-size: 15px;
    line-height: 1.25;
    font-weight: 900;
  }

  em {
    color: #707072;
    font-size: 12px;
    line-height: 1.35;
    font-style: normal;
    font-weight: 700;
  }
}

.action-row__arrow {
  color: #707072;
  font-size: 26px;
  line-height: 1;
}

.rating-panel {
  margin-top: 22px;
  min-height: 74px;
  padding: 14px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  gap: 12px;
  box-sizing: border-box;

  div {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  strong {
    font-size: 22px;
    line-height: 1;
    font-weight: 900;
  }

  span {
    color: #707072;
    font-size: 12px;
    line-height: 1.35;
    font-weight: 700;
  }
}

@media (max-width: 360px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
