<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, CircleCheckBig, RefreshCw } from 'lucide-vue-next';
import { fetchGrowthTimeline, type TimelineItem } from '@/api/growth';

const router = useRouter();
const items = ref<TimelineItem[]>([]);
const loading = ref(false);
const error = ref('');
const filter = ref<'all' | TimelineItem['type']>('all');

const tabs: Array<{ key: 'all' | TimelineItem['type']; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'checkin', label: '打卡' },
  { key: 'work', label: '作品' },
  { key: 'practice', label: '约练' },
  { key: 'trial', label: '试听' },
  { key: 'review', label: '评价' }
];

const typeLabel: Record<TimelineItem['type'], string> = {
  checkin: '训练打卡',
  work: '阶段作品',
  practice: '约练完成',
  trial: '试听完成',
  review: '发布评价'
};

const filtered = computed(() => filter.value === 'all' ? items.value : items.value.filter((item) => item.type === filter.value));

const timeLabel = (item: TimelineItem) => {
  const date = new Date(item.ts);
  return Number.isNaN(date.getTime()) ? '' : `${date.getMonth() + 1}/${date.getDate()}`;
};

const openItem = (item: TimelineItem) => {
  if (item.type === 'work') router.push('/me/works');
  else if (item.type === 'practice' && item.refId) router.push(`/practice/${item.refId}`);
  else if (item.type === 'review') router.push('/me/reviews');
  else if (item.type === 'trial') router.push('/me/trials');
  else router.push('/growth');
};

const load = async () => {
  loading.value = true;
  error.value = '';
  try {
    items.value = await fetchGrowthTimeline();
  } catch {
    error.value = '成长时间线加载失败';
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="timeline-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()"><ChevronLeft :size="20" /></button>
      <div>
        <p>GROWTH TIMELINE</p>
        <h1>成长时间线</h1>
      </div>
      <button class="icon-btn icon-btn--dark" type="button" aria-label="刷新" @click="load"><RefreshCw :size="18" /></button>
    </header>

    <section class="hero">
      <strong>{{ items.length }}</strong>
      <span>个成长事件，来自打卡、作品、约练、试听和评价。</span>
    </section>

    <section class="chips">
      <button v-for="tab in tabs" :key="tab.key" class="chip" :class="{ active: filter === tab.key }" type="button" @click="filter = tab.key">
        {{ tab.label }}
      </button>
    </section>

    <p v-if="loading" class="state">正在同步时间线...</p>
    <p v-else-if="error" class="state">{{ error }}</p>

    <section v-else class="list">
      <article v-for="item in filtered" :key="`${item.type}-${item.refId ?? item.id}`" class="event" @click="openItem(item)">
        <span class="date">{{ timeLabel(item) }}</span>
        <span class="dot"><CircleCheckBig :size="18" /></span>
        <div>
          <em>{{ typeLabel[item.type] }}</em>
          <strong>{{ item.title }}</strong>
          <p>{{ item.subtitle || '点击查看相关记录' }}</p>
        </div>
      </article>
      <p v-if="!filtered.length" class="state">这个分类暂时没有事件。</p>
    </section>
  </main>
</template>

<style scoped lang="scss">
.timeline-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; padding-bottom: 24px; }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; position: sticky; top: 0; z-index: 5; background: rgba(255,255,255,.94); backdrop-filter: blur(10px); border-bottom: 1px solid #e5e5e5; }
.topbar div { flex: 1; min-width: 0; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 21px; font-weight: 900; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.icon-btn--dark { background: #111; color: #fff; }
.hero { margin: 16px 18px 12px; padding: 18px; border-radius: 8px; background: #111; color: #fff; display: flex; flex-direction: column; gap: 6px; }
.hero strong { font-size: 44px; line-height: 1; font-weight: 950; }
.hero span { color: #e5e5e5; font-size: 13px; font-weight: 800; line-height: 1.4; }
.chips { display: flex; gap: 8px; overflow-x: auto; padding: 0 18px 12px; }
.chip { flex: none; height: 38px; padding: 0 16px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.list { padding: 0 18px; }
.event { display: grid; grid-template-columns: 44px 28px 1fr; gap: 10px; padding: 14px 0; border-bottom: 1px solid #e5e5e5; cursor: pointer; }
.date { color: #707072; font-size: 12px; font-weight: 900; }
.dot { width: 28px; height: 28px; border-radius: 999px; background: #111; color: #fff; display: grid; place-items: center; }
.event em { color: #707072; font-style: normal; font-size: 11px; font-weight: 900; letter-spacing: .04em; }
.event strong { display: block; margin-top: 4px; font-size: 16px; font-weight: 900; }
.event p, .state { margin: 5px 0 0; color: #707072; font-size: 13px; font-weight: 700; line-height: 1.4; }
.state { padding: 22px 18px; text-align: center; }
</style>
