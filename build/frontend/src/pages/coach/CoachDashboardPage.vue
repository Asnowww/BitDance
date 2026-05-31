<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchCoachDashboard, type CoachDashboard } from '@/api/coachOps';

const router = useRouter();
const data = ref<CoachDashboard | null>(null);

const quickActions = [
  { title: '发布课程', path: '/coach/workshop-create' },
  { title: '发布 Workshop', path: '/coach/workshop-create' },
  { title: '签到核销', path: '/coach/orders' },
  { title: '回复评价', path: '/coach/replies' }
];

const manageRows = [
  { title: '舞室入驻认领', path: '/me/coach-home' },
  { title: '课表管理', path: '/coach/workshop-create' },
  { title: '教练账号管理', path: '/me/profile' },
  { title: '订单与退款', path: '/coach/orders' },
  { title: '数据看板', path: '/coach/dashboard' },
  { title: '收益结算', path: '/coach/dashboard' }
];

const overview = computed(() => [
  { value: data.value?.monthStudents ?? 18, label: '预约' },
  { value: data.value?.monthSessions ?? 11, label: '核销' },
  { value: data.value?.pendingReplies ?? 3, label: '待评价' },
  { value: data.value ? `¥${(data.value.monthIncome / 1000).toFixed(1)}k` : '¥2.4k', label: '营收' }
]);

onMounted(async () => {
  data.value = await fetchCoachDashboard();
});
</script>

<template>
  <main class="dashboard-page">
    <header class="hero">
      <button class="icon-btn" aria-label="返回" @click="router.back()">‹</button>
      <p>ROLE WORKSPACE</p>
      <h1>舞室管理员工作台</h1>
    </header>

    <section class="metric-grid">
      <article v-for="item in overview" :key="item.label" class="metric">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </article>
    </section>

    <section class="panel">
      <div class="section-head">
        <h2>快捷操作</h2>
        <span>QUICK</span>
      </div>
      <div class="quick-grid">
        <button v-for="item in quickActions" :key="item.title" @click="router.push(item.path)">
          <span>{{ item.title.slice(0, 2) }}</span>
          {{ item.title }}
        </button>
      </div>
    </section>

    <section class="panel">
      <div class="section-head">
        <h2>管理页面</h2>
        <span>MANAGE</span>
      </div>
      <button v-for="item in manageRows" :key="item.title" class="manage-row" @click="router.push(item.path)">
        <span>{{ item.title }}</span>
        <em>进入</em>
      </button>
    </section>
  </main>
</template>

<style lang="scss" scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 18px 18px 28px;
  background: #fff;
  color: #111;
}

.hero {
  position: relative;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px;
  border-radius: 30px;
  background:
    linear-gradient(135deg, rgba(17, 17, 17, 0.05), rgba(17, 17, 17, 0.74)),
    linear-gradient(135deg, #e5e5e5, #9e9ea0);
  color: #fff;
  p {
    margin: 0 0 8px;
    color: #e5e5e5;
    font-size: 11px;
    font-weight: 900;
  }
  h1 {
    width: 260px;
    margin: 0;
    font-size: 36px;
    line-height: 0.95;
    font-weight: 900;
  }
}

.icon-btn {
  position: absolute;
  top: 14px;
  left: 14px;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #111;
  font-size: 30px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  overflow: hidden;
  margin-top: 16px;
  border-radius: 28px;
  background: #111;
}

.metric {
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 10px 8px;
  background: #111;
  color: #fff;
  strong {
    font-size: 24px;
    line-height: 1;
    font-weight: 900;
  }
  span {
    margin-top: 8px;
    color: #cacacb;
    font-size: 11px;
    font-weight: 800;
  }
}

.panel {
  margin-top: 24px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 12px;
  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 900;
  }
  span {
    color: #707072;
    font-size: 11px;
    font-weight: 900;
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  button {
    min-height: 64px;
    display: flex;
    align-items: center;
    gap: 10px;
    border: 0;
    border-radius: 24px;
    padding: 12px;
    background: #f5f5f5;
    color: #111;
    font-size: 14px;
    font-weight: 900;
    text-align: left;
    span {
      width: 36px;
      height: 36px;
      display: grid;
      place-items: center;
      flex: 0 0 auto;
      border-radius: 999px;
      background: #111;
      color: #fff;
      font-size: 12px;
    }
  }
}

.manage-row {
  width: 100%;
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  border: 0;
  border-radius: 22px;
  padding: 0 14px 0 18px;
  background: #f5f5f5;
  color: #111;
  text-align: left;
  span {
    font-size: 15px;
    font-weight: 900;
  }
  em {
    padding: 7px 12px;
    border-radius: 999px;
    background: #fff;
    color: #111;
    font-size: 12px;
    font-style: normal;
    font-weight: 900;
  }
}
</style>
