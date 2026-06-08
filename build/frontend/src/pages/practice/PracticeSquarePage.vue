<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, Heart, Search, User } from 'lucide-vue-next';
import { fetchMyPractices, fetchPractices, type PracticeListQuery, type PracticePost } from '@/api/practice';
import { getToken } from '@/utils/request';

const router = useRouter();

const scopes = ['推荐', '附近', '同舞种', '我的'];
const filters = ['Hiphop', '中级', '周末', '3人'];
const activeScope = ref(scopes[0]);
const activeFilters = reactive<Record<string, boolean>>({});
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

const activeScopeIndex = computed(() => scopes.indexOf(activeScope.value));
const filteredCards = computed(() => cards.value);
const resultText = computed(() => {
  if (loading.value) return '同步中';
  if (error.value) return '加载失败';
  return `${filteredCards.value.length} 个约练`;
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
  if (activeFilters[filters[2]] && !isWeekendPractice(item)) return false;
  if (activeFilters[filters[3]] && item.capacity !== 3 && item.takenCount !== 3) return false;
  return true;
});

const buildQuery = (): PracticeListQuery => {
  const query: PracticeListQuery = { page: 1, pageSize: 20 };
  if (activeFilters[filters[0]] || activeScopeIndex.value === 2) query.style = 'Hiphop';
  if (activeFilters[filters[1]]) query.level = filters[1];
  if (activeScopeIndex.value === 1) {
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
    const list = activeScopeIndex.value === 3
      ? await fetchMyPractices()
      : (await fetchPractices(buildQuery())).list;
    cards.value = applyLocalFilters(list).map(toCard);
  } catch {
    error.value = '约练列表加载失败';
    cards.value = [];
  } finally {
    loading.value = false;
  }
};

const setScope = (scope: string) => {
  if (scope === activeScope.value) {
    activeScope.value = '';
    return;
  }
  if (scopes.indexOf(scope) === 3 && !getToken()) {
    router.push('/me/practices');
    return;
  }
  activeScope.value = scope;
};

const toggleFilter = (filter: string) => {
  activeFilters[filter] = !activeFilters[filter];
};

const visibleSlots = (capacity: number) => Math.min(Math.max(capacity, 1), 4);
const coverHeights = [150, 120, 112, 160, 132, 122];
const coverH = (i: number) => `${coverHeights[i % coverHeights.length]}px`;
const goDetail = (id: string) => router.push(`/practice/${id}`);

onMounted(loadPractices);
watch(
  [
    activeScope,
    () => activeFilters[filters[0]],
    () => activeFilters[filters[1]],
    () => activeFilters[filters[2]],
    () => activeFilters[filters[3]]
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

      <section class="filter-panel" aria-label="约练筛选">
        <div class="chips">
          <button
            v-for="scope in scopes"
            :key="scope"
            class="chip"
            :class="{ 'chip--active': activeScope === scope }"
            type="button"
            @click="setScope(scope)"
          >
            {{ scope }}
          </button>
        </div>
        <div class="chips">
          <button
            v-for="filter in filters"
            :key="filter"
            class="chip"
            :class="{ 'chip--active': activeFilters[filter] }"
            type="button"
            @click="toggleFilter(filter)"
          >
            {{ filter }}
          </button>
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
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
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

  h1,
  p {
    margin: 0;
    letter-spacing: 0;
  }

  h1 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 900;
  }

  p {
    margin-top: 2px;
    color: var(--mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 500;
  }
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
  cursor: pointer;
}

.square__content {
  padding: 12px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search {
  width: 100%;
  height: 44px;
  border: 1px solid var(--line);
  border-radius: 24px;
  padding: 0 16px;
  background: var(--canvas);
  color: var(--mute);
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
  box-sizing: border-box;

  span {
    min-width: 0;
    flex: 1;
    color: var(--mute);
    font-size: 14px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.filter-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--canvas);
  color: var(--ink);
  font-size: 13px;
  line-height: 1.25;
  font-weight: 700;
  cursor: pointer;
  box-sizing: border-box;

  &--active {
    border-color: var(--ink);
    background: var(--ink);
    color: var(--canvas);
  }
}

.filter-status {
  min-height: 22px;
  color: var(--mute);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 700;

  button {
    border: 0;
    background: transparent;
    color: var(--ink);
    font: inherit;
    cursor: pointer;
  }
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

  &__cover {
    background-color: var(--charcoal);
    background-position: center;
    background-size: cover;
    padding: 10px;
    box-sizing: border-box;
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    border-radius: 999px;
    background: var(--canvas);
    color: var(--ink);
    font-size: 11px;
    line-height: 1.25;
    font-weight: 700;
  }

  &__body {
    padding: 10px 12px 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__title {
    margin: 0;
    font-size: 15px;
    line-height: 1.3;
    font-weight: 800;
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  &__meta {
    margin: 0;
    color: var(--mute);
    font-size: 12px;
    line-height: 1.3;
    font-weight: 500;
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  &__foot {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__host {
    flex: 1;
    min-width: 0;
    color: var(--mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 600;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  &__like {
    flex: none;
    color: var(--mute);
  }
}

.slot-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 24px;
  height: 24px;
  flex: none;
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  background: var(--canvas);
  color: var(--canvas);
  display: grid;
  place-items: center;
  box-sizing: border-box;

  &--filled {
    border-color: var(--ink);
    background: var(--ink);
  }
}

.slot-row__count {
  margin-left: 2px;
  color: var(--mute);
  font-size: 12px;
  line-height: 1.25;
  font-weight: 700;
}

.host-avatar {
  width: 20px;
  height: 20px;
  flex: none;
  border-radius: 999px;
  background: var(--charcoal);
}
</style>
