<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, Plus, RefreshCw, Users } from 'lucide-vue-next';
import {
  cancelGroupClassIntent,
  fetchGroupClassIntents,
  fetchMyGroupClassIntents,
  joinGroupClassIntent,
  type GroupClassIntent
} from '@/api/practice';
import { getToken } from '@/utils/request';

const router = useRouter();
const tab = ref<'public' | 'mine'>('public');
const styleFilter = ref<number | null>(null);
const items = ref<GroupClassIntent[]>([]);
const loading = ref(false);
const error = ref('');

const styles = [
  { id: null, name: '全部' },
  { id: 1, name: 'Hiphop' },
  { id: 2, name: 'Jazz' },
  { id: 3, name: 'Breaking' },
  { id: 5, name: 'Popping' },
  { id: 6, name: 'K-pop' }
] as const;

const styleNames: Record<number, string> = {
  1: 'Hiphop',
  2: 'Jazz',
  3: 'Breaking',
  4: 'Locking',
  5: 'Popping',
  6: 'K-pop',
  7: 'Waacking'
};

const statusText: Record<GroupClassIntent['intentStatus'], string> = {
  collecting: '招募中',
  matched: '已达标',
  closed: '已关闭',
  canceled: '已取消'
};

const title = computed(() => (tab.value === 'public' ? '拼课广场' : '我的拼课'));

const visibleItems = computed(() =>
  styleFilter.value == null ? items.value : items.value.filter((item) => item.danceStyleId === styleFilter.value)
);

const progress = (item: GroupClassIntent) =>
  Math.min(100, Math.round((item.currentPeopleCount / item.targetPeopleCount) * 100));

const requireLogin = () => {
  if (getToken()) return true;
  router.push({ path: '/login', query: { redirect: '/practice/group-class' } });
  return false;
};

const load = async () => {
  loading.value = true;
  error.value = '';
  try {
    items.value = tab.value === 'mine'
      ? await fetchMyGroupClassIntents()
      : await fetchGroupClassIntents({ limit: 30 });
  } catch {
    error.value = '拼课信息加载失败';
    items.value = [];
  } finally {
    loading.value = false;
  }
};

const setTab = (next: 'public' | 'mine') => {
  if (next === 'mine' && !requireLogin()) return;
  tab.value = next;
  load();
};

const setStyle = (id: number | null) => {
  styleFilter.value = id;
};

const join = async (item: GroupClassIntent) => {
  if (!requireLogin()) return;
  try {
    await joinGroupClassIntent(item.id);
    showSuccessToast('已加入拼课');
    await load();
  } catch {
    showFailToast('加入失败，可能已加入或人数已满');
  }
};

const cancel = async (item: GroupClassIntent) => {
  if (!requireLogin()) return;
  try {
    await cancelGroupClassIntent(item.id);
    showSuccessToast('已取消参与');
    await load();
  } catch {
    showFailToast('取消失败，请稍后重试');
  }
};

onMounted(load);
</script>

<template>
  <main class="group-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div class="topbar__copy">
        <h1>{{ title }}</h1>
        <p>凑齐人数后通知舞室确认开课</p>
      </div>
      <button class="icon-btn icon-btn--dark" type="button" aria-label="刷新" @click="load">
        <RefreshCw :size="18" />
      </button>
    </header>

    <section class="hero">
      <span>GROUP CLASS</span>
      <h2>想上同一节课的人，先在这里集合。</h2>
      <button type="button" @click="router.push('/practice/group-class/create')">
        <Plus :size="17" />
        发起拼课
      </button>
    </section>

    <section class="seg">
      <button :class="{ on: tab === 'public' }" type="button" @click="setTab('public')">公开拼课</button>
      <button :class="{ on: tab === 'mine' }" type="button" @click="setTab('mine')">我的拼课</button>
    </section>

    <section class="chips">
      <button v-for="style in styles" :key="style.name" class="chip" :class="{ active: styleFilter === style.id }" type="button" @click="setStyle(style.id)">
        {{ style.name }}
      </button>
    </section>

    <section v-if="loading" class="state-card">正在同步拼课信息...</section>
    <section v-else-if="error" class="state-card">
      {{ error }}
      <button type="button" @click="load">重试</button>
    </section>
    <section v-else-if="visibleItems.length === 0" class="state-card">
      暂时没有符合条件的拼课，先发起一个试试。
    </section>

    <section v-else class="intent-list">
      <article v-for="item in visibleItems" :key="item.id" class="intent-card">
        <div class="intent-card__head">
          <span>{{ styleNames[item.danceStyleId] ?? `舞种 #${item.danceStyleId}` }}</span>
          <em :class="`status status--${item.intentStatus}`">{{ statusText[item.intentStatus] }}</em>
        </div>

        <h3>舞室 #{{ item.studioId }} 拼课</h3>
        <p>{{ item.preferredTimeNote || '时间可协商' }}</p>

        <div class="progress">
          <span :style="{ width: `${progress(item)}%` }" />
        </div>

        <div class="intent-card__meta">
          <span><Users :size="15" /> {{ item.currentPeopleCount }}/{{ item.targetPeopleCount }} 人</span>
          <span>意向 #{{ item.id }}</span>
        </div>

        <footer class="intent-card__actions">
          <button v-if="item.joinedByMe" class="btn btn--soft" type="button" @click="cancel(item)">
            取消参与
          </button>
          <button v-else class="btn btn--dark" type="button" :disabled="item.intentStatus !== 'collecting'" @click="join(item)">
            加入拼课
          </button>
        </footer>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
.group-page { min-height: 100vh; max-width: 430px; margin: 0 auto; padding-bottom: 20px; background: #f5f5f5; color: #111; }
.topbar { height: 68px; padding: 12px 14px; background: #fff; border-bottom: 1px solid #e5e5e5; display: flex; align-items: center; gap: 10px; box-sizing: border-box; }
.topbar__copy { flex: 1; min-width: 0; }
.topbar h1, .topbar p { margin: 0; }
.topbar h1 { font-size: 18px; line-height: 1.2; font-weight: 900; }
.topbar p { margin-top: 3px; color: #707072; font-size: 12px; font-weight: 700; }
.icon-btn { width: 40px; height: 40px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.icon-btn--dark { background: #111; color: #fff; }
.hero { margin: 14px; padding: 18px; border-radius: 8px; background: #111; color: #fff; }
.hero span { color: #bdbdbd; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.hero h2 { max-width: 260px; margin: 8px 0 16px; font-size: 25px; line-height: 1.05; font-weight: 950; }
.hero button, .btn { height: 40px; border: 0; border-radius: 999px; padding: 0 16px; display: inline-flex; align-items: center; justify-content: center; gap: 6px; font-size: 13px; font-weight: 900; }
.hero button { background: #fff; color: #111; }
.seg { margin: 0 14px 12px; padding: 4px; border-radius: 999px; background: #fff; display: grid; grid-template-columns: repeat(2, 1fr); gap: 4px; }
.seg button { height: 38px; border: 0; border-radius: 999px; background: transparent; color: #707072; font-size: 13px; font-weight: 900; }
.seg .on { background: #111; color: #fff; }
.chips { display: flex; gap: 8px; overflow-x: auto; padding: 0 14px 12px; }
.chip { flex: none; height: 36px; padding: 0 14px; border: 0; border-radius: 999px; background: #fff; color: #111; font-size: 13px; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.state-card { margin: 0 14px; padding: 22px 16px; border: 1px solid #e5e5e5; border-radius: 8px; background: #fff; color: #707072; font-size: 13px; font-weight: 800; text-align: center; }
.state-card button { margin-left: 8px; border: 0; background: transparent; color: #111; font: inherit; }
.intent-list { margin: 0 14px; display: flex; flex-direction: column; gap: 10px; }
.intent-card { padding: 14px; border: 1px solid #e5e5e5; border-radius: 8px; background: #fff; }
.intent-card__head, .intent-card__meta, .intent-card__actions { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.intent-card__head span { font-size: 12px; font-weight: 900; }
.status { padding: 5px 9px; border-radius: 999px; background: #f5f5f5; color: #707072; font-size: 11px; font-style: normal; font-weight: 900; }
.status--matched { background: #111; color: #fff; }
.intent-card h3 { margin: 12px 0 4px; font-size: 18px; line-height: 1.2; font-weight: 950; }
.intent-card p { margin: 0; color: #707072; font-size: 13px; font-weight: 700; }
.progress { height: 8px; margin: 14px 0 10px; border-radius: 999px; background: #f5f5f5; overflow: hidden; }
.progress span { display: block; height: 100%; border-radius: inherit; background: #111; }
.intent-card__meta { color: #707072; font-size: 12px; font-weight: 800; }
.intent-card__meta span { display: inline-flex; align-items: center; gap: 4px; }
.intent-card__actions { margin-top: 14px; justify-content: flex-end; }
.btn--dark { background: #111; color: #fff; }
.btn--soft { background: #f5f5f5; color: #111; }
.btn:disabled { opacity: .45; }
</style>
