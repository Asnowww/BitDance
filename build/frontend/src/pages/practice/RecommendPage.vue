<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { MapPin, Sparkles, Users } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchMyBuddies, type Buddy } from '@/api/buddy';
import { fetchPracticeRecommendations, joinPractice, type PracticePost } from '@/api/practice';
import { getToken } from '@/utils/request';

const router = useRouter();
const tab = ref<'recommend' | 'buddies'>('recommend');
const loading = ref(false);
const error = ref('');
const recommendations = ref<PracticePost[]>([]);
const buddies = ref<Buddy[]>([]);

const title = computed(() => (tab.value === 'recommend' ? '推荐约练' : '我的搭子'));

const distanceLabel = (meters?: number | null) => {
  if (meters == null) return '距离待确认';
  return meters < 1000 ? `${meters}m` : `${(meters / 1000).toFixed(1)}km`;
};

const timeLabel = (item: PracticePost) => `${item.date} ${item.time}`;

const load = async () => {
  loading.value = true;
  error.value = '';
  try {
    if (tab.value === 'recommend') {
      recommendations.value = await fetchPracticeRecommendations({
        city: '北京',
        longitude: 116.397,
        latitude: 39.908,
        pageSize: 12
      });
    } else {
      if (!getToken()) {
        router.push({ path: '/login', query: { redirect: '/practice/recommend' } });
        return;
      }
      buddies.value = await fetchMyBuddies();
    }
  } catch {
    error.value = '数据加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const applyJoin = async (item: PracticePost) => {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: '/practice/recommend' } });
    return;
  }
  await joinPractice(item.id);
  showSuccessToast('申请已提交，等待发起人确认');
};

const switchTab = (next: 'recommend' | 'buddies') => {
  if (tab.value === next) return;
  tab.value = next;
  load();
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar :title="title" :show-share="false" />

    <section class="pen-scroll">
      <section class="hero">
        <div>
          <p class="eyebrow">M4 PRACTICE SOCIAL</p>
          <h1>找同城、同舞种、同水平的人一起练。</h1>
        </div>
        <Sparkles :size="28" :stroke-width="2.2" />
      </section>

      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'recommend' }" type="button" @click="switchTab('recommend')">
          推荐约练
        </button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'buddies' }" type="button" @click="switchTab('buddies')">
          我的搭子
        </button>
      </div>

      <div v-if="error" class="state">
        <p>{{ error }}</p>
        <button type="button" @click="load">重试</button>
      </div>

      <div v-else-if="loading" class="state">正在同步真实推荐...</div>

      <template v-else-if="tab === 'recommend'">
        <article v-for="item in recommendations" :key="item.id" class="card">
          <header class="card__head">
            <span class="avatar"><Users :size="22" /></span>
            <div>
              <strong>{{ item.style }} {{ item.level || '不限水平' }}</strong>
              <p>{{ timeLabel(item) }}</p>
            </div>
            <b>{{ Math.min(98, 70 + item.takenCount * 7) }}%</b>
          </header>

          <div class="meta-row">
            <span><MapPin :size="14" />{{ item.area || item.city }} · {{ distanceLabel(item.distanceMeters) }}</span>
            <span>{{ item.takenCount }}/{{ item.capacity }} 人</span>
          </div>

          <p class="desc">{{ item.remark || '发起人还没有补充备注，可以先申请加入再沟通细节。' }}</p>

          <footer class="card__foot">
            <button type="button" @click="router.push(`/practice/${item.id}`)">看详情</button>
            <button class="primary" type="button" @click="applyJoin(item)">申请加入</button>
          </footer>
        </article>
        <p v-if="!recommendations.length" class="empty">暂无符合条件的约练推荐，先去约练广场发布一条。</p>
      </template>

      <template v-else>
        <article v-for="item in buddies" :key="item.userId" class="buddy-card">
          <span class="avatar avatar--dark">{{ item.name.slice(0, 1) }}</span>
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.pastSessions }} 次共同约练 · {{ item.sharedStyles.join(' / ') || '舞种待补充' }}</p>
          </div>
          <button type="button" @click="router.push('/publish/practice')">再约</button>
        </article>
        <p v-if="!buddies.length" class="empty">完成约练并互评后，会自动沉淀为搭子关系。</p>
      </template>
    </section>
  </main>
</template>

<style scoped lang="scss">
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }
.pen-scroll { display: flex; flex-direction: column; gap: 14px; padding: 16px 18px calc(24px + env(safe-area-inset-bottom)); }
.hero { display: flex; justify-content: space-between; gap: 16px; padding: 18px; border-radius: 8px; background: $pen-ink; color: $pen-on-primary; }
.hero h1 { margin: 4px 0 0; font-size: 25px; line-height: 1.05; font-weight: 900; }
.eyebrow { margin: 0; color: $pen-subtle-text; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.seg { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.seg__btn { height: 44px; border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink; font-weight: 900; }
.seg__btn--on { background: $pen-ink; color: $pen-on-primary; }
.state, .empty { padding: 22px 12px; color: $pen-mute; text-align: center; font-size: 13px; font-weight: 700; }
.state button { margin-top: 10px; height: 36px; padding: 0 18px; border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary; font-weight: 800; }
.card, .buddy-card { border-bottom: 1px solid $pen-hairline; padding: 14px 0; }
.card__head { display: flex; align-items: center; gap: 12px; }
.card__head div { flex: 1; min-width: 0; }
.card__head strong, .buddy-card strong { display: block; font-size: 17px; font-weight: 900; line-height: $pen-lh; }
.card__head p, .buddy-card p, .desc { margin: 4px 0 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: 1.45; }
.card__head b { font-size: 22px; font-weight: 900; }
.avatar { flex: none; width: 46px; height: 46px; border-radius: 999px; background: $pen-soft; display: grid; place-items: center; font-weight: 900; }
.avatar--dark { background: $pen-ink; color: $pen-on-primary; }
.meta-row { display: flex; justify-content: space-between; gap: 10px; margin-top: 12px; color: $pen-mute; font-size: 12px; font-weight: 800; }
.meta-row span { display: inline-flex; align-items: center; gap: 4px; }
.card__foot { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 12px; }
.card__foot button, .buddy-card button { height: 40px; border: 1px solid $pen-ink; border-radius: 999px; background: $pen-canvas; color: $pen-ink; font-weight: 900; }
.card__foot .primary, .buddy-card button { background: $pen-ink; color: $pen-on-primary; }
.buddy-card { display: flex; align-items: center; gap: 12px; }
.buddy-card div { flex: 1; min-width: 0; }
.buddy-card button { flex: none; width: 74px; }
</style>
