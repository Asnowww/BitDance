<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAppStore } from '@/stores/app';
import { fetchNearbyStudios, type StudioCard } from '@/api/studio';

const router = useRouter();
const appStore = useAppStore();

const HOT_KEYWORDS = ['Hiphop', 'Jazz', 'Breaking', '零基础', '海淀区', '朝阳区'];
const HISTORY_KEY = 'bitdance_search_history';

const keyword = ref('');
const list = ref<StudioCard[]>([]);
const loading = ref(false);
const filterVisible = ref(false);
const history = ref<string[]>(JSON.parse(localStorage.getItem(HISTORY_KEY) ?? '[]'));

const filter = ref({
  styles: [] as string[],
  priceMin: undefined as number | undefined,
  priceMax: undefined as number | undefined,
  distanceKm: undefined as number | undefined,
  difficulty: '' as string,
  audience: '' as string,
  beginnerFriendly: false
});

const STYLE_OPTIONS = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const DISTANCE_OPTIONS = [
  { label: '1km', value: 1 },
  { label: '3km', value: 3 },
  { label: '5km', value: 5 },
  { label: '10km', value: 10 },
  { label: '不限', value: undefined }
];
const DIFFICULTY_OPTIONS = ['不限', '入门', '初级', '进阶', '高阶'];
const AUDIENCE_OPTIONS = ['不限', '青少年', '成人', '零基础', '考级'];
const PRICE_RANGES = [
  { label: '不限', min: undefined, max: undefined },
  { label: '<100', min: 0, max: 100 },
  { label: '100-200', min: 100, max: 200 },
  { label: '200-400', min: 200, max: 400 },
  { label: '>400', min: 400, max: undefined }
];

const activeFilterCount = computed(() => {
  const f = filter.value;
  let n = 0;
  if (f.styles.length) n += 1;
  if (f.priceMin !== undefined || f.priceMax !== undefined) n += 1;
  if (f.distanceKm !== undefined) n += 1;
  if (f.difficulty) n += 1;
  if (f.audience) n += 1;
  if (f.beginnerFriendly) n += 1;
  return n;
});

const toggleStyle = (s: string) => {
  const idx = filter.value.styles.indexOf(s);
  if (idx >= 0) filter.value.styles.splice(idx, 1);
  else filter.value.styles.push(s);
};

const onSearch = async () => {
  const k = keyword.value.trim();
  if (k && !history.value.includes(k)) {
    history.value.unshift(k);
    history.value = history.value.slice(0, 10);
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value));
  }
  loading.value = true;
  try {
    const data = await fetchNearbyStudios({
      city: appStore.city,
      keyword: k || undefined,
      styles: filter.value.styles.length ? filter.value.styles : undefined,
      priceMin: filter.value.priceMin,
      priceMax: filter.value.priceMax,
      distanceKm: filter.value.distanceKm,
      difficulty: filter.value.difficulty || undefined,
      audience: filter.value.audience || undefined,
      beginnerFriendly: filter.value.beginnerFriendly || undefined,
      page: 1,
      pageSize: 30
    });
    list.value = data.list;
  } finally {
    loading.value = false;
  }
};

const onClearHistory = () => {
  history.value = [];
  localStorage.removeItem(HISTORY_KEY);
};

const onPickHistory = (k: string) => {
  keyword.value = k;
  void onSearch();
};

const onResetFilter = () => {
  filter.value = {
    styles: [],
    priceMin: undefined,
    priceMax: undefined,
    distanceKm: undefined,
    difficulty: '',
    audience: '',
    beginnerFriendly: false
  };
};

const onApplyFilter = () => {
  filterVisible.value = false;
  void onSearch();
};

const setPriceRange = (r: { min?: number; max?: number }) => {
  filter.value.priceMin = r.min;
  filter.value.priceMax = r.max;
};
</script>

<template>
  <div class="search-page">
    <header class="search-bar">
      <button class="search-bar__back" @click="router.back()">←</button>
      <input
        v-model="keyword"
        class="search-bar__input"
        placeholder="搜索舞室、舞种或老师"
        @keyup.enter="onSearch"
      />
      <button class="search-bar__btn" @click="onSearch">搜索</button>
    </header>

    <section class="filter-row">
      <button class="chip" :class="{ active: activeFilterCount > 0 }" @click="filterVisible = true">
        筛选 {{ activeFilterCount > 0 ? `· ${activeFilterCount}` : '' }}
      </button>
    </section>

    <template v-if="!list.length">
      <section v-if="history.length" class="block">
        <div class="block__head">
          <span>历史搜索</span>
          <button class="block__action" @click="onClearHistory">清空</button>
        </div>
        <div class="chips">
          <span v-for="h in history" :key="h" class="chip" @click="onPickHistory(h)">{{ h }}</span>
        </div>
      </section>
      <section class="block">
        <div class="block__head"><span>热门搜索</span></div>
        <div class="chips">
          <span v-for="h in HOT_KEYWORDS" :key="h" class="chip" @click="onPickHistory(h)">{{ h }}</span>
        </div>
      </section>
    </template>

    <section v-else class="result">
      <article
        v-for="s in list"
        :key="s.id"
        class="result__item"
        @click="router.push(`/studio/${s.id}`)"
      >
        <div class="result__cover" />
        <div class="result__body">
          <div class="result__title">{{ s.name }}</div>
          <div class="result__meta">{{ s.area }} · {{ s.distanceKm }}km · ★{{ s.ratingAvg }}</div>
          <div class="result__tags">
            <span v-for="t in s.topStyles" :key="t" class="tag">{{ t }}</span>
          </div>
        </div>
      </article>
      <div v-if="loading" class="loading">加载中…</div>
      <div v-if="!loading && !list.length" class="empty">没有匹配的舞室，换个关键词试试</div>
    </section>

    <van-popup
      v-model:show="filterVisible"
      position="right"
      :style="{ width: '85%', height: '100%' }"
    >
      <div class="filter-panel">
        <header class="filter-panel__head">
          <span>筛选</span>
          <button class="filter-panel__reset" @click="onResetFilter">重置</button>
        </header>
        <div class="filter-panel__body">
          <div class="group">
            <div class="group__title">舞种（多选）</div>
            <div class="chips">
              <span
                v-for="s in STYLE_OPTIONS"
                :key="s"
                class="chip"
                :class="{ active: filter.styles.includes(s) }"
                @click="toggleStyle(s)"
                >{{ s }}</span
              >
            </div>
          </div>
          <div class="group">
            <div class="group__title">价格</div>
            <div class="chips">
              <span
                v-for="r in PRICE_RANGES"
                :key="r.label"
                class="chip"
                :class="{ active: filter.priceMin === r.min && filter.priceMax === r.max }"
                @click="setPriceRange(r)"
                >{{ r.label }}</span
              >
            </div>
          </div>
          <div class="group">
            <div class="group__title">距离</div>
            <div class="chips">
              <span
                v-for="d in DISTANCE_OPTIONS"
                :key="d.label"
                class="chip"
                :class="{ active: filter.distanceKm === d.value }"
                @click="filter.distanceKm = d.value"
                >{{ d.label }}</span
              >
            </div>
          </div>
          <div class="group">
            <div class="group__title">难度</div>
            <div class="chips">
              <span
                v-for="d in DIFFICULTY_OPTIONS"
                :key="d"
                class="chip"
                :class="{ active: filter.difficulty === (d === '不限' ? '' : d) }"
                @click="filter.difficulty = d === '不限' ? '' : d"
                >{{ d }}</span
              >
            </div>
          </div>
          <div class="group">
            <div class="group__title">适合人群</div>
            <div class="chips">
              <span
                v-for="d in AUDIENCE_OPTIONS"
                :key="d"
                class="chip"
                :class="{ active: filter.audience === (d === '不限' ? '' : d) }"
                @click="filter.audience = d === '不限' ? '' : d"
                >{{ d }}</span
              >
            </div>
          </div>
          <div class="group">
            <div class="group__title">其他</div>
            <label class="toggle">
              <input v-model="filter.beginnerFriendly" type="checkbox" />
              <span>仅看零基础友好</span>
            </label>
          </div>
        </div>
        <footer class="filter-panel__foot">
          <button class="apply" @click="onApplyFilter">应用筛选</button>
        </footer>
      </div>
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
.search-page {
  padding: 8px 0 24px;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px 8px;
  &__back {
    background: none;
    border: none;
    font-size: 22px;
    width: 28px;
    cursor: pointer;
  }
  &__input {
    flex: 1;
    height: 36px;
    padding: 0 14px;
    border: none;
    border-radius: 999px;
    background: var(--bd-surface);
    font-size: 13px;
    outline: none;
  }
  &__btn {
    border: none;
    background: none;
    color: var(--bd-primary);
    font-size: 14px;
    cursor: pointer;
  }
}
.filter-row {
  padding: 4px 12px 12px;
}
.block {
  padding: 12px 12px 4px;
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  &__action {
    border: none;
    background: none;
    font-size: 12px;
    color: var(--bd-text-secondary);
    cursor: pointer;
  }
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--bd-surface);
  border: 1px solid var(--bd-border);
  font-size: 12px;
  color: var(--bd-text);
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.result {
  padding: 0 12px;
  &__item {
    display: flex;
    gap: 10px;
    padding: 12px 0;
    border-bottom: 1px solid var(--bd-border);
    cursor: pointer;
  }
  &__cover {
    width: 80px;
    height: 80px;
    border-radius: 10px;
    background: linear-gradient(135deg, #ffd2da, #ff2442);
    flex-shrink: 0;
  }
  &__body {
    flex: 1;
    min-width: 0;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
  }
  &__meta {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__tags {
    margin-top: 6px;
    display: flex;
    gap: 4px;
  }
}
.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(255, 36, 66, 0.08);
  color: var(--bd-primary);
}
.loading,
.empty {
  text-align: center;
  padding: 24px;
  color: var(--bd-text-secondary);
  font-size: 13px;
}
.filter-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  &__head {
    padding: 16px 16px 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid var(--bd-border);
  }
  &__reset {
    border: none;
    background: none;
    color: var(--bd-text-secondary);
    font-size: 13px;
    cursor: pointer;
  }
  &__body {
    flex: 1;
    overflow-y: auto;
    padding: 8px 16px 16px;
  }
  &__foot {
    padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
    border-top: 1px solid var(--bd-border);
  }
}
.group {
  padding: 12px 0;
  &__title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 10px;
  }
}
.toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  cursor: pointer;
}
.apply {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
</style>
