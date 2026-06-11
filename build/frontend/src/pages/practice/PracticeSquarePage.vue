<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, ChevronDown, Heart, Search, SlidersHorizontal, User } from 'lucide-vue-next';
import {
  fetchMyPractices,
  fetchPracticeRecommendations,
  fetchPractices,
  type PracticeListQuery,
  type PracticePost
} from '@/api/practice';
import { getToken } from '@/utils/request';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const scopes = [
  { key: 'all', label: '全部' },
  { key: 'recommend', label: '推荐' },
  { key: 'sameStyle', label: '同舞种' },
  { key: 'mine', label: '我的' }
] as const;
const filters = [
  { key: 'hiphop', label: 'Hiphop' },
  { key: 'intermediate', label: '中级' },
  { key: 'weekend', label: '周末' },
  { key: 'threePeople', label: '3人' }
] as const;
type ScopeKey = (typeof scopes)[number]['key'];
type FilterKey = (typeof filters)[number]['key'];
const activeScope = ref<ScopeKey>('all');
const distanceMode = ref<'all' | 'nearby'>('all');
const activeFilters = reactive<Record<string, boolean>>({});
const filterOpen = ref(false);
const loading = ref(false);
const error = ref('');

interface PracticeCard {
  id: string;
  cover: string;
  tag: string;
  title: string;
  area: string;
  studio: string;
  time: string;
  joined: number;
  capacity: number;
  host: string;
  distance?: string;
}

const covers = [
  'https://images.unsplash.com/photo-1547153760-18fc86324498?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1667384447307-9ae9cd6ff1d8?w=640&q=80&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=640&q=80&auto=format&fit=crop'
];

const cards = ref<PracticeCard[]>([]);
const filteredCards = computed(() => cards.value);
const sameStyle = computed(() => userStore.preferences.styles[0] || 'Hiphop');
const resultText = computed(() => {
  if (loading.value) return '同步中';
  if (error.value) return '加载失败';
  return `${filteredCards.value.length} 个约练`;
});
const activeFilterCount = computed(() =>
  (activeScope.value === 'all' ? 0 : 1)
  + (distanceMode.value === 'nearby' ? 1 : 0)
  + Object.values(activeFilters).filter(Boolean).length
);
const activeFilterSummary = computed(() => {
  const labels: string[] = [];
  const scope = scopes.find((item) => item.key === activeScope.value);
  if (scope && scope.key !== 'all') labels.push(scope.label);
  if (distanceMode.value === 'nearby') labels.push('附近优先');
  filters.forEach((item) => {
    if (activeFilters[item.key]) labels.push(item.label);
  });
  return labels.length ? labels.join(' · ') : '不限条件';
});

const coverOf = (index: number) => covers[index % covers.length];

const splitPlace = (location: string, area: string) => {
  const normalized = location.trim();
  if (!normalized) return { area, studio: '待定场地' };
  const parts = normalized.split(/\s*[·|,，]\s*/).filter(Boolean);
  if (parts.length >= 2) return { area: parts[0], studio: parts.slice(1).join(' ') };
  return { area: area || '同城', studio: normalized };
};

const formatDistance = (meters?: number | null) => {
  if (meters == null) return '';
  if (meters < 1000) return `${Math.round(meters)}m`;
  return `${(meters / 1000).toFixed(1)}km`;
};

const toCard = (item: PracticePost, index: number): PracticeCard => {
  const place = splitPlace(item.location, item.area);
  return {
    id: String(item.id),
    cover: coverOf(index),
    tag: item.style,
    title: item.title,
    area: place.area,
    studio: place.studio,
    time: item.time || item.date,
    joined: item.takenCount,
    capacity: item.capacity,
    host: item.authorName,
    distance: formatDistance(item.distanceMeters)
  };
};

const isWeekendPractice = (item: PracticePost) => {
  const date = new Date(`${item.date}T00:00:00+08:00`);
  if (!Number.isNaN(date.getTime())) return date.getDay() === 0 || date.getDay() === 6;
  return /周末|周六|周日|Saturday|Sunday/i.test(`${item.date} ${item.time}`);
};

const applyLocalFilters = (list: PracticePost[]) => list.filter((item) => {
  if (activeFilters.weekend && !isWeekendPractice(item)) return false;
  if (activeFilters.threePeople && item.capacity !== 3 && item.takenCount !== 3) return false;
  return true;
});

const buildQuery = (): PracticeListQuery => {
  const query: PracticeListQuery = { page: 1, pageSize: 20 };
  if (activeFilters.hiphop) query.style = 'Hiphop';
  if (activeScope.value === 'sameStyle') query.style = sameStyle.value;
  if (activeFilters.intermediate) query.level = 'intermediate';
  if (distanceMode.value === 'nearby') {
    query.scope = 'nearby';
    query.sort = 'distance';
    query.longitude = 116.397;
    query.latitude = 39.908;
  }
  return query;
};

const loadPractices = async () => {
  loading.value = true;
  error.value = '';
  try {
    const query = buildQuery();
    const list = activeScope.value === 'mine'
      ? await fetchMyPractices()
      : activeScope.value === 'recommend'
        ? await fetchPracticeRecommendations(query)
        : (await fetchPractices(query)).list;
    cards.value = applyLocalFilters(list).map(toCard);
  } catch {
    error.value = '约练列表加载失败';
    cards.value = [];
  } finally {
    loading.value = false;
  }
};

const setScope = (scope: ScopeKey) => {
  if (scope === activeScope.value) {
    activeScope.value = 'all';
    return;
  }
  if ((scope === 'mine' || scope === 'recommend') && !getToken()) {
    router.push(scope === 'mine' ? '/me/practices' : { path: '/login', query: { redirect: '/practice' } });
    return;
  }
  activeScope.value = scope;
};

const setExclusiveFilter = (filter: FilterKey, enabled: boolean) => {
  activeFilters[filter] = enabled;
};

const resetFilters = () => {
  activeScope.value = 'all';
  distanceMode.value = 'all';
  filters.forEach((filter) => {
    activeFilters[filter.key] = false;
  });
};

const applyFilters = () => {
  filterOpen.value = false;
};

const visibleSlots = (capacity: number) => Math.min(Math.max(capacity, 1), 4);
const coverHeights = [150, 120, 112, 160, 132, 122];
const coverH = (i: number) => `${coverHeights[i % coverHeights.length]}px`;
const goDetail = (id: string) => router.push(`/practice/${id}`);

onMounted(loadPractices);
watch(
  [
    activeScope,
    distanceMode,
    () => activeFilters.hiphop,
    () => activeFilters.intermediate,
    () => activeFilters.weekend,
    () => activeFilters.threePeople
  ],
  loadPractices
);
</script>

<template>
  <div class="square">
    <header class="square__top">
      <div class="square__copy">
        <h1>约练广场</h1>
        <p>找同城舞友一起练</p>
      </div>
      <button class="icon-btn" type="button" aria-label="消息" @click="router.push('/messages')">
        <Bell :size="20" :stroke-width="2" />
      </button>
    </header>

    <main class="square__content">
      <button class="search" type="button" @click="router.push('/search')">
        <Search :size="18" :stroke-width="2" />
        <span>搜索舞种、地点、发起人</span>
      </button>

      <div class="quick-actions">
        <button type="button" @click="router.push('/publish/practice')">发布约练</button>
        <button type="button" @click="router.push('/me/practices')">我的约练</button>
      </div>

      <div class="entry-row">
        <button class="group-entry" type="button" @click="router.push('/practice/group-class')">
          <span>
            <strong>拼课广场</strong>
            <em>凑齐人数后通知舞室</em>
          </span>
          <b>去看看</b>
        </button>

        <button class="group-entry" type="button" @click="router.push('/practice/recommend')">
          <span>
            <strong>推荐约练 / 我的搭子</strong>
            <em>按偏好找合适约练</em>
          </span>
          <b>去匹配</b>
        </button>
      </div>

      <section class="filter-panel" aria-label="约练筛选">
        <button class="filter-trigger" type="button" @click="filterOpen = !filterOpen">
          <span>
            <SlidersHorizontal :size="18" :stroke-width="2.2" />
            筛选
            <b v-if="activeFilterCount">{{ activeFilterCount }}</b>
          </span>
          <em>{{ activeFilterSummary }}</em>
          <ChevronDown class="filter-trigger__chevron" :class="{ 'filter-trigger__chevron--open': filterOpen }" :size="18" />
        </button>

        <div v-if="filterOpen" class="filter-drawer">
          <div class="filter-group">
            <h3>约练类型</h3>
            <div class="filter-options">
              <button
                v-for="scope in scopes"
                :key="scope.key"
                class="filter-option"
                :class="{ 'filter-option--active': activeScope === scope.key }"
                type="button"
                @click="setScope(scope.key)"
              >
                {{ scope.label }}
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>舞种水平</h3>
            <div class="filter-options">
              <button
                class="filter-option"
                :class="{ 'filter-option--active': !activeFilters.hiphop }"
                type="button"
                @click="setExclusiveFilter('hiphop', false)"
              >
                舞种不限
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': activeFilters.hiphop }"
                type="button"
                @click="setExclusiveFilter('hiphop', true)"
              >
                Hiphop
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': !activeFilters.intermediate }"
                type="button"
                @click="setExclusiveFilter('intermediate', false)"
              >
                水平不限
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': activeFilters.intermediate }"
                type="button"
                @click="setExclusiveFilter('intermediate', true)"
              >
                中级
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>练习时间</h3>
            <div class="filter-options">
              <button
                class="filter-option"
                :class="{ 'filter-option--active': !activeFilters.weekend }"
                type="button"
                @click="setExclusiveFilter('weekend', false)"
              >
                不限
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': activeFilters.weekend }"
                type="button"
                @click="setExclusiveFilter('weekend', true)"
              >
                周末
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>位置距离</h3>
            <div class="filter-options">
              <button
                class="filter-option"
                :class="{ 'filter-option--active': distanceMode === 'all' }"
                type="button"
                @click="distanceMode = 'all'"
              >
                不限
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': distanceMode === 'nearby' }"
                type="button"
                @click="distanceMode = 'nearby'"
              >
                附近优先
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>人数状态</h3>
            <div class="filter-options">
              <button
                class="filter-option"
                :class="{ 'filter-option--active': !activeFilters.threePeople }"
                type="button"
                @click="setExclusiveFilter('threePeople', false)"
              >
                不限
              </button>
              <button
                class="filter-option"
                :class="{ 'filter-option--active': activeFilters.threePeople }"
                type="button"
                @click="setExclusiveFilter('threePeople', true)"
              >
                3人局
              </button>
            </div>
          </div>

          <div class="filter-actions">
            <button type="button" @click="resetFilters">重置</button>
            <button class="filter-actions__primary" type="button" @click="applyFilters">收起</button>
          </div>
        </div>

        <div class="filter-status">
          <span>{{ resultText }}</span>
          <button v-if="error" type="button" @click="loadPractices">重试</button>
        </div>
      </section>

      <section v-if="loading" class="state-card">正在按筛选条件刷新约练...</section>
      <section v-else-if="!error && filteredCards.length === 0" class="state-card">
        暂时没有符合条件的约练，换个筛选试试。
      </section>

      <section v-else class="masonry" aria-label="约练列表">
        <article
          v-for="(card, i) in filteredCards"
          :key="card.id"
          class="card"
          @click="goDetail(card.id)"
        >
          <div
            class="card__cover"
            :style="{ backgroundImage: `url(${card.cover})`, height: coverH(i) }"
          >
            <span class="card__tag">{{ card.tag }}</span>
          </div>

          <div class="card__body">
            <h2 class="card__title">{{ card.title }}</h2>
            <p class="card__meta">
              {{ card.area }} {{ card.studio }} · {{ card.time }}
              <template v-if="card.distance"> · {{ card.distance }}</template>
            </p>

            <div class="slot-row" :aria-label="`${card.joined}/${card.capacity} 人`">
              <span
                v-for="slot in visibleSlots(card.capacity)"
                :key="slot"
                class="dot"
                :class="{ 'dot--filled': slot <= card.joined }"
              >
                <User v-if="slot <= card.joined" :size="13" :stroke-width="2" />
              </span>
              <span class="slot-row__count">{{ card.joined }}/{{ card.capacity }} 人</span>
            </div>

            <footer class="card__foot">
              <span class="host-avatar" aria-hidden="true" />
              <span class="card__host">{{ card.host }} 发起</span>
              <Heart class="card__like" :size="16" :stroke-width="2" />
            </footer>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.square {
  --ink: #111111;
  --canvas: #ffffff;
  --soft: #f5f5f5;
  --mute: #707072;
  --line: #e5e5e5;
  --line-strong: #cacacb;
  --charcoal: #39393b;
  min-height: 100%;
  background: var(--soft);
  color: var(--ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

.square__top {
  height: 68px;
  padding: 14px 18px;
  background: var(--canvas);
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  gap: 12px;
  box-sizing: border-box;
}

.square__copy {
  min-width: 0;
  flex: 1;
}

.square__copy h1,
.square__copy p {
  margin: 0;
}

.square__copy h1 {
  font-size: 18px;
  line-height: 1.25;
  font-weight: 900;
}

.square__copy p {
  margin-top: 2px;
  color: var(--mute);
  font-size: 12px;
  font-weight: 500;
}

.icon-btn {
  width: 40px;
  height: 40px;
  flex: none;
  border: 0;
  border-radius: 999px;
  background: var(--soft);
  color: var(--ink);
  display: grid;
  place-items: center;
}

.square__content {
  padding: 12px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search,
.group-entry {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 24px;
  background: var(--canvas);
  color: var(--ink);
  cursor: pointer;
  box-sizing: border-box;
}

.search {
  height: 44px;
  padding: 0 16px;
  color: var(--mute);
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
}

.search span {
  color: var(--mute);
  font-size: 14px;
  font-weight: 500;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.quick-actions button {
  height: 42px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--canvas);
  color: var(--ink);
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
}

.quick-actions button + button {
  background: var(--canvas);
  color: var(--ink);
}

.entry-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.group-entry {
  min-height: 104px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
}

.group-entry span {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.group-entry strong {
  font-size: 16px;
  font-weight: 950;
  line-height: 1.15;
}

.group-entry em {
  color: var(--mute);
  font-size: 11px;
  line-height: 1.35;
  font-style: normal;
  font-weight: 700;
}

.group-entry b {
  height: 32px;
  padding: 0 11px;
  border-radius: 999px;
  background: var(--ink);
  color: var(--canvas);
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 900;
}

.filter-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-trigger {
  width: 100%;
  min-height: 46px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 24px;
  background: var(--canvas);
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
  box-sizing: border-box;
}

.filter-trigger span {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 950;
}

.filter-trigger b {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--ink);
  color: var(--canvas);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 900;
  box-sizing: border-box;
}

.filter-trigger em {
  flex: 1;
  min-width: 0;
  color: var(--mute);
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.filter-trigger__chevron {
  flex: none;
  transition: transform .18s ease;
}

.filter-trigger__chevron--open {
  transform: rotate(180deg);
}

.filter-drawer {
  padding: 16px 14px 14px;
  border: 1px solid var(--line);
  border-radius: 24px;
  background: var(--canvas);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.filter-group h3 {
  margin: 0;
  color: var(--ink);
  font-size: 14px;
  line-height: 1.4;
  font-weight: 950;
}

.filter-options {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.filter-option {
  min-height: 40px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: var(--soft);
  color: var(--ink);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.filter-option--active {
  border-color: var(--ink);
  background: var(--ink);
  color: var(--canvas);
}

.filter-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding-top: 2px;
}

.filter-actions button {
  height: 44px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--canvas);
  color: var(--ink);
  font-size: 14px;
  font-weight: 950;
  cursor: pointer;
}

.filter-actions .filter-actions__primary {
  border-color: var(--ink);
  background: var(--ink);
  color: var(--canvas);
}

.filter-status {
  min-height: 22px;
  color: var(--mute);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;
}

.filter-status button {
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
}

.state-card {
  padding: 22px 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--canvas);
  color: var(--mute);
  font-size: 13px;
  line-height: 1.5;
  font-weight: 700;
  text-align: center;
}

.masonry {
  column-count: 2;
  column-gap: 10px;
}

.card {
  break-inside: avoid;
  margin-bottom: 10px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--canvas);
  overflow: hidden;
  cursor: pointer;
}

.card__cover {
  background-color: var(--charcoal);
  background-position: center;
  background-size: cover;
  padding: 10px;
  box-sizing: border-box;
}

.card__tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--canvas);
  color: var(--ink);
  font-size: 11px;
  font-weight: 700;
}

.card__body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card__title {
  margin: 0;
  font-size: 15px;
  line-height: 1.3;
  font-weight: 800;
}

.card__meta,
.card__host,
.slot-row__count {
  color: var(--mute);
  font-size: 12px;
  font-weight: 600;
}

.card__meta {
  margin: 0;
  line-height: 1.3;
}

.card__foot,
.slot-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card__host {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.card__like {
  color: var(--mute);
}

.dot {
  width: 24px;
  height: 24px;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  background: var(--canvas);
  color: var(--canvas);
  display: grid;
  place-items: center;
}

.dot--filled {
  border-color: var(--ink);
  background: var(--ink);
}

.host-avatar {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: var(--charcoal);
}
</style>
