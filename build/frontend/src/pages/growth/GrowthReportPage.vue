<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Award, BarChart3, ChevronLeft } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import { fetchBadgeDefinitions, fetchGrowthBadges, fetchGrowthReport, type BadgeDefinition, type GrowthReport, type TimelineItem } from '@/api/growth';

const router = useRouter();
const period = ref<'monthly' | 'quarterly'>('monthly');
const report = ref<GrowthReport | null>(null);
const badges = ref<Array<{ id: number; badgeId: number; sourceType?: string; awardedAt: string }>>([]);
const badgeDefinitions = ref<BadgeDefinition[]>([]);
const loading = ref(false);
const error = ref('');
const styleNames: Record<string, string> = {
  '1': 'Hiphop',
  '2': 'Jazz',
  '3': 'Breaking',
  '4': 'Locking',
  '5': 'Popping',
  '6': 'K-pop',
  '7': 'Waacking'
};

const minutesLabel = computed(() => {
  const minutes = report.value?.totalMinutes ?? 0;
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return hours ? `${hours}h ${rest}m` : `${minutes}min`;
});

const styleRows = computed(() => Object.entries(report.value?.styleSessions ?? {}));
const badgeMap = computed(() => new Map(badgeDefinitions.value.map((item) => [item.id, item])));

const eventTime = (item: TimelineItem) => {
  const date = new Date(item.ts);
  return Number.isNaN(date.getTime()) ? '' : date.toLocaleDateString();
};

const load = async () => {
  loading.value = true;
  error.value = '';
  try {
    const [nextReport, nextBadges] = await Promise.all([
      fetchGrowthReport(period.value),
      fetchGrowthBadges()
    ]);
    report.value = nextReport;
    badges.value = nextBadges;
    badgeDefinitions.value = await fetchBadgeDefinitions();
  } catch {
    report.value = null;
    badges.value = [];
    badgeDefinitions.value = [];
    error.value = '成长报告加载失败，请确认已登录且后端成长接口可用。';
  } finally {
    loading.value = false;
  }
};

const switchPeriod = (next: 'monthly' | 'quarterly') => {
  if (period.value === next) return;
  period.value = next;
  load();
};

onMounted(load);
</script>

<template>
  <main class="report-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()"><ChevronLeft :size="20" /></button>
      <div>
        <p>GROWTH REPORT</p>
        <h1>成长报告</h1>
      </div>
    </header>

    <section class="content">
      <div class="seg">
        <button :class="{ on: period === 'monthly' }" type="button" @click="switchPeriod('monthly')">月报</button>
        <button :class="{ on: period === 'quarterly' }" type="button" @click="switchPeriod('quarterly')">季报</button>
      </div>

      <p v-if="loading" class="empty">正在生成报告...</p>
      <section v-else-if="error" class="empty empty--error">
        <p>{{ error }}</p>
        <button type="button" @click="load">重试</button>
      </section>

      <template v-else-if="report">
        <section class="hero">
          <div>
            <p>{{ report.startDate }} - {{ report.endDate }}</p>
            <h2>{{ minutesLabel }}</h2>
            <span>{{ report.totalSessions }} 次训练 · {{ report.activeDays }} 个活跃日</span>
          </div>
          <BarChart3 :size="30" />
        </section>

        <section class="grid">
          <div><strong>{{ report.styleCount }}</strong><span>舞种数</span></div>
          <div><strong>{{ report.workCount }}</strong><span>作品</span></div>
          <div><strong>{{ report.badgeCount }}</strong><span>新徽章</span></div>
          <div><strong>{{ Math.round(report.goalProgress) }}%</strong><span>目标进度</span></div>
        </section>

        <section class="panel">
          <h3>舞种分布</h3>
          <div v-if="styleRows.length" class="bars">
            <div v-for="[styleId, count] in styleRows" :key="styleId" class="bar">
              <span>{{ styleNames[styleId] || `舞种 #${styleId}` }}</span>
              <i><b :style="{ width: `${Math.min(100, Number(count) * 24)}%` }" /></i>
              <em>{{ count }} 次</em>
            </div>
          </div>
          <p v-else class="muted">本周期还没有舞种打卡数据。</p>
        </section>

        <section class="panel">
          <h3>高光时间线</h3>
          <article v-for="item in report.highlights" :key="`${item.type}-${item.refId ?? item.id}`" class="timeline">
            <span>{{ eventTime(item) }}</span>
            <div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.subtitle || '暂无补充说明' }}</p>
            </div>
          </article>
          <p v-if="!report.highlights.length" class="muted">本周期还没有高光事件。</p>
        </section>

        <section class="panel">
          <h3>已获得徽章</h3>
          <div class="badges">
            <article v-for="badge in badges" :key="badge.id" class="badge">
              <Award :size="22" />
              <strong>{{ badgeMap.get(badge.badgeId)?.badgeName || `徽章 #${badge.badgeId}` }}</strong>
              <span>{{ badgeMap.get(badge.badgeId)?.description || badge.sourceType || '成长里程碑' }}</span>
            </article>
          </div>
          <p v-if="!badges.length" class="muted">继续打卡、发布作品和完成约练后会获得徽章。</p>
        </section>

        <section class="suggestion">
          <strong>成长建议</strong>
          <p>{{ report.suggestion }}</p>
        </section>
      </template>
    </section>
  </main>
</template>

<style scoped lang="scss">
.report-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; position: sticky; top: 0; background: rgba(255,255,255,.94); backdrop-filter: blur(10px); z-index: 4; }
.topbar div { flex: 1; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 22px; font-weight: 900; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; display: grid; place-items: center; }
.content { display: flex; flex-direction: column; gap: 14px; padding: 0 18px 24px; }
.seg { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.seg button { height: 42px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; font-weight: 900; }
.seg .on { background: #111; color: #fff; }
.hero { display: flex; justify-content: space-between; gap: 16px; padding: 18px; border-radius: 8px; background: #111; color: #fff; }
.hero p { margin: 0 0 8px; color: #b8b8bb; font-size: 12px; font-weight: 800; }
.hero h2 { margin: 0; font-size: 38px; line-height: 1; font-weight: 900; }
.hero span { display: block; margin-top: 8px; color: #e5e5e5; font-size: 12px; font-weight: 800; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.grid div { min-height: 74px; border-radius: 8px; background: #f5f5f5; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 4px; }
.grid strong { font-size: 22px; font-weight: 900; }
.grid span, .muted { color: #707072; font-size: 12px; font-weight: 800; }
.panel { display: flex; flex-direction: column; gap: 12px; padding: 16px 0; border-bottom: 1px solid #e5e5e5; }
.panel h3 { margin: 0; font-size: 18px; font-weight: 900; }
.bar { display: grid; grid-template-columns: 58px 1fr 42px; align-items: center; gap: 10px; font-size: 12px; font-weight: 900; }
.bar i { height: 9px; border-radius: 999px; background: #f5f5f5; overflow: hidden; }
.bar b { display: block; height: 100%; border-radius: 999px; background: #111; }
.bar em { color: #707072; font-style: normal; text-align: right; }
.timeline { display: flex; gap: 12px; }
.timeline > span { flex: none; width: 78px; color: #707072; font-size: 12px; font-weight: 900; }
.timeline strong { font-size: 15px; font-weight: 900; }
.timeline p { margin: 4px 0 0; color: #707072; font-size: 12px; font-weight: 700; line-height: 1.4; }
.badges { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.badge { border-radius: 8px; background: #f5f5f5; padding: 12px; display: flex; flex-direction: column; gap: 5px; }
.badge strong { font-size: 14px; font-weight: 900; }
.badge span { color: #707072; font-size: 11px; font-weight: 800; }
.suggestion { border-radius: 8px; background: #111; color: #fff; padding: 16px; }
.suggestion strong { font-size: 16px; font-weight: 900; }
.suggestion p { margin: 8px 0 0; color: #e5e5e5; font-size: 13px; font-weight: 700; line-height: 1.5; }
.empty { padding: 24px 8px; color: #707072; text-align: center; font-size: 13px; font-weight: 800; }
.empty--error { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.empty--error p { margin: 0; }
.empty--error button { height: 38px; padding: 0 18px; border: 0; border-radius: 999px; background: #111; color: #fff; font-size: 13px; font-weight: 900; }
</style>
