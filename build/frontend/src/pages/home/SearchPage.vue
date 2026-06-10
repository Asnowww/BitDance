<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ChevronRight, ListFilter, Music, Search, Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import StudioFilterDrawer, { type StudioFilterValue } from '@/components/studio/StudioFilterDrawer.vue';
import StudioSearchEditor, { type StudioSearchEditorValue } from '@/components/studio/StudioSearchEditor.vue';
import { fetchNearbyStudios, type StudioCard, type StudioListQuery } from '@/api/studio';
import { fetchFavorites, toggleFavorite } from '@/api/favorite';
import { getToken } from '@/utils/request';
import { getCityName } from '@/constants/cities';
import { hasTencentMapConfig, loadTencentMap } from '@/utils/tencentMap';

const route = useRoute();
const router = useRouter();

type SearchPreset = 'zero-basic' | 'trial';

const parseRoutePreset = (): SearchPreset | undefined => {
  const preset = typeof route.query.preset === 'string' ? route.query.preset : '';
  return preset === 'zero-basic' || preset === 'trial' ? preset : undefined;
};

const parseRouteSearch = (): StudioSearchEditorValue => {
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword.trim() : '';
  const rawCityId = typeof route.query.cityId === 'string' ? Number(route.query.cityId) : NaN;
  const cityId = Number.isFinite(rawCityId) && rawCityId > 0 ? rawCityId : undefined;
  return {
    keyword: keyword || undefined,
    cityId,
    useNearby: !cityId
  };
};

const buildPresetFilters = (preset?: SearchPreset): StudioFilterValue => {
  const base: StudioFilterValue = { distanceKm: 5 };
  if (preset === 'zero-basic') base.zeroBasicFriendly = true;
  if (preset === 'trial') base.trialAvailable = true;
  return base;
};

const drawerVisible = ref(false);
const searchEditorVisible = ref(false);
const searchValue = ref<StudioSearchEditorValue>(parseRouteSearch());
const locatedCoords = ref<{ latitude: number; longitude: number } | null>(null);

interface SearchResult {
  id: string;
  title: string;
  meta: string;
  tags: string[];
  hint: string;
  to: string;
  favored: boolean;
  compareSelected: boolean;
}

const studios = ref<StudioCard[]>([]);
const resultCount = ref<number>();
const loading = ref(false);
const query = ref<StudioListQuery>({ page: 1, pageSize: 20 });
const appliedFilters = ref<StudioFilterValue>(buildPresetFilters(parseRoutePreset()));
const compareMode = ref(false);
const compareSelection = ref<Record<string, boolean>>({});
const favoriteState = ref<Record<number, boolean>>({});
const canFavorite = computed(() => Boolean(getToken()));
const mapContainer = ref<HTMLElement | null>(null);
const mapStatus = ref(hasTencentMapConfig() ? '地图加载中' : '未配置腾讯地图 Key，展示坐标降级视图');
const selectedMapStudioId = ref<number>();
const resultItemRefs = new Map<number, HTMLElement>();
let tencentMapApi: Record<string, any> | null = null;
let map: any = null;
let markerLayer: any = null;

const results = computed<SearchResult[]>(() =>
  studios.value.map((studio) => {
    const favored = Boolean(favoriteState.value[studio.id] ?? studio.favored);
    return {
      id: String(studio.id),
      title: studio.name,
      meta: `${studio.distanceKm ?? '-'}km · ${studio.address || '地址待完善'}`,
      tags: [favored ? '已收藏' : '附近舞室'],
      hint: '课程 · 老师 · 评价',
      to: `/studio/${studio.id}`,
      favored,
      compareSelected: Boolean(compareSelection.value[String(studio.id)])
    };
  })
);

const compareSelectedStudioIds = computed(() =>
  Object.entries(compareSelection.value).filter(([, on]) => on).map(([id]) => Number(id))
);
const compareCount = computed(() => compareSelectedStudioIds.value.length);
const canEnterCompare = computed(() => compareCount.value >= 2 && compareCount.value <= 3);
const resultSummary = computed(() => {
  const place = searchValue.value.useNearby ? '附近' : getCityName(searchValue.value.cityId) || '当前城市';
  return `${place}找到 ${resultCount.value ?? studios.value.length} 家舞室`;
});
const searchContextMeta = computed(() => {
  const parts = [searchValue.value.useNearby ? '附近结果' : getCityName(searchValue.value.cityId) || '指定城市'];
  if (searchValue.value.keyword) parts.push(`关键词「${searchValue.value.keyword}」`);
  return parts.join(' · ');
});
const selectionHint = computed(() => {
  if (!compareMode.value) return '点卡片可查看详情，点右侧星标收藏';
  if (compareCount.value === 0) return '点卡片加入对比，最多同时选 3 家';
  if (compareCount.value === 1) return '再选 1 家就能进入对比';
  return `已选 ${compareCount.value} 家，可进入对比`;
});
const compareHint = computed(() => {
  if (compareCount.value === 0) return '开启后点卡片加入对比，右侧星标仍然只负责收藏。';
  if (compareCount.value === 1) return '再点 1 家就能进入对比页。';
  if (compareCount.value === 2) return '已经够开始对比了，还可以再补 1 家。';
  return '已满 3 家，想换对象先取消 1 家。';
});
const selectedMapStudio = computed(() => studios.value.find((studio) => studio.id === selectedMapStudioId.value) ?? null);
const mapFocusHint = computed(() =>
  selectedMapStudio.value
    ? `已在地图定位 ${selectedMapStudio.value.name}，列表会同步高亮。`
    : '点地图上的舞室，列表会跟着定位到同一条结果。'
);
const activeFilterTags = computed(() => {
  const tags: string[] = [];
  if (searchValue.value.useNearby && appliedFilters.value.distanceKm) tags.push(`${appliedFilters.value.distanceKm}km 内`);
  if (appliedFilters.value.danceStyleId) tags.push('已选舞种');
  if (appliedFilters.value.minPrice !== undefined || appliedFilters.value.maxPrice !== undefined) {
    tags.push(`¥${appliedFilters.value.minPrice ?? 0}-${appliedFilters.value.maxPrice ?? 500}`);
  }
  if (appliedFilters.value.timeSlot) {
    tags.push({ morning: '上午', afternoon: '下午', evening: '晚上', weekend: '周末' }[appliedFilters.value.timeSlot]);
  }
  if (appliedFilters.value.trialAvailable) tags.push('可试听');
  if (appliedFilters.value.zeroBasicFriendly) tags.push('新手友好');
  if (appliedFilters.value.nearMetro) tags.push('近地铁');
  if (!searchValue.value.useNearby && searchValue.value.cityId) tags.push(getCityName(searchValue.value.cityId));
  if (searchValue.value.keyword) tags.push(`关键词：${searchValue.value.keyword}`);
  return tags;
});
const searchSummaryTitle = computed(() => resultSummary.value);
const searchSummaryMeta = computed(() => searchContextMeta.value);

const syncRouteQuery = () => {
  const nextQuery: Record<string, string> = {};
  if (searchValue.value.keyword) nextQuery.keyword = searchValue.value.keyword;
  if (!searchValue.value.useNearby && searchValue.value.cityId) {
    nextQuery.scope = 'city';
    nextQuery.cityId = String(searchValue.value.cityId);
  }
  void router.replace({ name: 'search', query: nextQuery });
};

const syncQueryFromState = () => {
  query.value = {
    page: 1,
    pageSize: 20,
    keyword: searchValue.value.keyword,
    cityId: searchValue.value.useNearby ? undefined : searchValue.value.cityId,
    distanceKm: searchValue.value.useNearby ? appliedFilters.value.distanceKm : undefined,
    latitude: searchValue.value.useNearby ? locatedCoords.value?.latitude : undefined,
    longitude: searchValue.value.useNearby ? locatedCoords.value?.longitude : undefined,
    danceStyleId: appliedFilters.value.danceStyleId,
    minPrice: appliedFilters.value.minPrice,
    maxPrice: appliedFilters.value.maxPrice,
    timeSlot: appliedFilters.value.timeSlot,
    trialAvailable: appliedFilters.value.trialAvailable,
    zeroBasicFriendly: appliedFilters.value.zeroBasicFriendly,
    nearMetro: appliedFilters.value.nearMetro
  };
};

const loadStudios = async () => {
  loading.value = true;
  try {
    syncQueryFromState();
    // M1 收藏链路：列表接口只负责发现舞室，星标状态从用户收藏接口合并，避免隐式默认收藏第一条。
    const [response, favorites] = await Promise.all([
      fetchNearbyStudios(query.value),
      canFavorite.value ? fetchFavorites('studio', { silentError: true }).catch(() => []) : Promise.resolve([])
    ]);
    const favoriteMap = Object.fromEntries((favorites ?? []).map((item) => [item.targetId, true]));
    favoriteState.value = favoriteMap;
    studios.value = response.list.map((studio) => ({
      ...studio,
      favored: Boolean(favoriteMap[studio.id] ?? studio.favored)
    }));
    resultCount.value = response.total ?? response.list.length;
  } finally {
    loading.value = false;
  }
};

const resetMarkers = () => {
  markerLayer?.setMap?.(null);
  markerLayer = null;
};

const bindResultRef = (id: number, el: Element | null) => {
  if (el instanceof HTMLElement) {
    resultItemRefs.set(id, el);
    return;
  }
  resultItemRefs.delete(id);
};

const focusStudio = (id: number, options?: { scrollIntoView?: boolean }) => {
  selectedMapStudioId.value = id;
  if (options?.scrollIntoView) {
    nextTick(() => resultItemRefs.get(id)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' }));
  }
};

const renderMap = async () => {
  if (!hasTencentMapConfig()) {
    mapStatus.value = '未配置腾讯地图 Key，已保留列表数据和坐标用于验收';
    return;
  }
  await nextTick();
  if (!mapContainer.value) return;
  try {
    const TMap = tencentMapApi ?? (await loadTencentMap());
    tencentMapApi = TMap;
    const points = studios.value.filter((studio) => {
      const latitude = Number(studio.latitude);
      const longitude = Number(studio.longitude);
      return Number.isFinite(latitude) && Number.isFinite(longitude);
    });
    const center = points[0]
      ? new TMap.LatLng(Number(points[0].latitude), Number(points[0].longitude))
      : new TMap.LatLng(39.90923, 116.397428);
    if (!map) {
      // 搜索页地图只承载 M1 附近舞室点位，不把腾讯地图实例状态写入业务 store。
      map = new TMap.Map(mapContainer.value, {
        viewMode: '2D',
        zoom: points.length ? 13 : 11,
        center
      });
    } else {
      map.setCenter?.(center);
      map.setZoom?.(points.length ? 13 : 11);
    }
    resetMarkers();
    markerLayer = new TMap.MultiMarker({
      id: 'bitdance-studio-markers',
      map,
      styles: {
        studio: new TMap.MarkerStyle({
          width: 28,
          height: 36,
          anchor: { x: 14, y: 36 }
        }),
        studioActive: new TMap.MarkerStyle({
          width: 34,
          height: 42,
          anchor: { x: 17, y: 42 }
        })
      },
      geometries: points.map((studio) => ({
        id: String(studio.id),
        styleId: selectedMapStudioId.value === studio.id ? 'studioActive' : 'studio',
        position: new TMap.LatLng(Number(studio.latitude), Number(studio.longitude)),
        properties: {
          studioId: studio.id,
          title: studio.name
        }
      }))
    });
    markerLayer.on('click', (event: any) => {
      const id = Number(event?.geometry?.properties?.studioId ?? event?.geometry?.id);
      if (!id) return;
      if (compareMode.value) {
        toggleCompare(String(id));
        return;
      }
      focusStudio(id, { scrollIntoView: true });
    });
    if (points.length > 1 && TMap.LatLngBounds && typeof map.fitBounds === 'function') {
      const bounds = new TMap.LatLngBounds();
      points.forEach((studio) => bounds.extend(new TMap.LatLng(Number(studio.latitude), Number(studio.longitude))));
      map.fitBounds(bounds, { padding: 40 });
    }
    mapStatus.value = points.length ? `已标注 ${points.length} 家舞室` : '暂无可标注经纬度';
  } catch {
    mapStatus.value = '地图加载失败，已回退列表视图数据';
  }
};

const locate = () => {
  let settled = false;
  // M1 定位兜底：浏览器权限弹窗可能长时间无回调，先保证舞室列表和腾讯地图点位可见。
  const fallbackTimer = window.setTimeout(() => {
    if (settled) return;
    settled = true;
    locatedCoords.value = null;
    void loadStudios().then(renderMap);
  }, 1200);

  navigator.geolocation?.getCurrentPosition(
    ({ coords }) => {
      if (settled) return;
      settled = true;
      window.clearTimeout(fallbackTimer);
      locatedCoords.value = { latitude: coords.latitude, longitude: coords.longitude };
      void loadStudios().then(renderMap);
    },
    () => {
      if (settled) return;
      settled = true;
      window.clearTimeout(fallbackTimer);
      locatedCoords.value = null;
      void loadStudios().then(renderMap);
    }
  );
};

const openFilter = () => {
  drawerVisible.value = true;
};

const openSearchEditor = () => {
  searchEditorVisible.value = true;
};

const applySearch = (value: StudioSearchEditorValue) => {
  searchValue.value = value;
  searchEditorVisible.value = false;
  syncRouteQuery();
  void loadStudios().then(renderMap);
};

const applyFilters = (filters: StudioFilterValue) => {
  appliedFilters.value = filters;
  drawerVisible.value = false;
  void loadStudios().then(renderMap);
};

const toggleCompare = (id: string) => {
  if (!compareSelection.value[id] && compareCount.value >= 3) {
    showToast('最多选择 3 家舞室进行对比，请先取消 1 家');
    return;
  }
  compareSelection.value[id] = !compareSelection.value[id];
  if (compareSelection.value[id]) {
    focusStudio(Number(id));
  }
};

const handleResultClick = (item: SearchResult) => {
  const id = Number(item.id);
  if (compareMode.value) {
    toggleCompare(item.id);
    return;
  }
  if (selectedMapStudioId.value !== id) {
    focusStudio(id);
    return;
  }
  router.push(item.to);
};

const toggleResultFavorite = async (item: SearchResult) => {
  if (!canFavorite.value) {
    showToast('请先登录后收藏');
    return;
  }
  const id = Number(item.id);
  const { favored } = await toggleFavorite('studio', id);
  favoriteState.value = { ...favoriteState.value, [id]: favored };
  studios.value = studios.value.map((studio) =>
    studio.id === id ? { ...studio, favored } : studio
  );
  showToast(favored ? '已加入收藏' : '已取消收藏');
};

const onCompare = () => {
  if (!canEnterCompare.value) {
    showToast('请至少选择 2 个舞室进行对比');
    return;
  }
  const ids = compareSelectedStudioIds.value.slice(0, 3);
  sessionStorage.setItem('bitdance_compare_studio_ids', JSON.stringify(ids));
  router.push('/studio/compare');
};

const clearCompareSelection = () => {
  compareSelection.value = {};
  showToast('已清空对比选择');
};

const resultHintFor = (item: SearchResult) => {
  if (compareMode.value) return item.compareSelected ? '已加入对比' : '点卡片加入对比';
  if (selectedMapStudioId.value === Number(item.id)) {
    return '已和地图对齐，再点一次看详情';
  }
  return item.hint;
};

onMounted(() => {
  locate();
});

watch(selectedMapStudioId, () => {
  void renderMap();
});

watch(studios, (list) => {
  // 搜索页的“已选舞室”只应属于当前结果集，筛选后移除失效选择，避免底栏动作指向不可见对象。
  const visibleIds = new Set(list.map((studio) => String(studio.id)));
  if (selectedMapStudioId.value && !visibleIds.has(String(selectedMapStudioId.value))) {
    selectedMapStudioId.value = undefined;
  }
  compareSelection.value = Object.fromEntries(
    Object.entries(compareSelection.value).filter(([id, checked]) => checked && visibleIds.has(id))
  );
  void renderMap();
});

onUnmounted(() => {
  resetMarkers();
  map?.destroy?.();
  map = null;
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="搜索结果" @share="showToast('搜索结果链接已复制')" />

    <section class="pen-scroll">
      <section
        class="search-summary"
        aria-label="搜索结果摘要"
        role="button"
        tabindex="0"
        @click="openSearchEditor"
        @keydown.enter.prevent="openSearchEditor"
        @keydown.space.prevent="openSearchEditor"
      >
        <Search class="search-summary__icon" :size="18" :stroke-width="2" />
        <div class="search-summary__body">
          <strong class="search-summary__title">{{ searchSummaryTitle }}</strong>
          <p class="search-summary__meta">{{ searchSummaryMeta }}</p>
        </div>
        <ChevronRight class="search-summary__chevron" :size="18" :stroke-width="2" />
      </section>

      <section class="filter-toolbar" aria-label="筛选与选择">
        <div class="filter-toolbar__body">
          <strong class="filter-toolbar__title">当前筛选</strong>
          <div class="filter-toolbar__chips">
            <span v-for="tag in activeFilterTags" :key="tag" class="filter-toolbar__chip">{{ tag }}</span>
            <span v-if="!activeFilterTags.length" class="filter-toolbar__chip filter-toolbar__chip--muted">默认附近结果</span>
          </div>
        </div>
        <button type="button" class="filter-trigger" @click="openFilter">
          <ListFilter :size="16" :stroke-width="2" />
          <span>筛选</span>
        </button>
      </section>

      <section class="map-panel" aria-label="舞室地图">
        <div ref="mapContainer" class="map-panel__canvas" />
        <p class="map-panel__status">{{ mapStatus }}</p>
        <div class="map-panel__footer">
          <p class="map-panel__focus">{{ mapFocusHint }}</p>
          <!-- M1 对比入口：滑钮只切换选择模式，底部确认/取消按钮才执行对比动作。 -->
          <button
            type="button"
            class="compare-switch"
            :class="{ 'compare-switch--active': compareMode }"
            @click="compareMode = !compareMode"
          >
            <span class="compare-switch__label">对比</span>
            <span class="compare-switch__track" aria-hidden="true">
              <span class="compare-switch__thumb" />
            </span>
          </button>
        </div>
        <p class="map-panel__hint">{{ compareMode ? compareHint : selectionHint }}</p>
        <div v-if="!hasTencentMapConfig()" class="map-panel__fallback">
          <button
            v-for="studio in studios.slice(0, 8)"
            :key="studio.id"
            type="button"
            class="map-chip"
            :class="{ 'map-chip--active': selectedMapStudioId === studio.id }"
            @click="selectedMapStudioId === studio.id ? router.push(`/studio/${studio.id}`) : focusStudio(studio.id, { scrollIntoView: true })"
          >
            {{ studio.name }} · {{ studio.longitude ?? '-' }}, {{ studio.latitude ?? '-' }}
          </button>
        </div>
      </section>

      <ul class="result-list">
        <li
          v-for="item in results"
          :key="item.id"
          :ref="(el) => bindResultRef(Number(item.id), el as Element | null)"
          class="result"
          :class="{
            'result--focused': selectedMapStudioId === Number(item.id),
            'result--compare': item.compareSelected
          }"
          @click="handleResultClick(item)"
        >
          <div class="result__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="result__body">
            <div class="result__title-row">
              <strong class="result__title">{{ item.title }}</strong>
              <span v-if="selectedMapStudioId === Number(item.id)" class="result__focus-tag">地图已定位</span>
              <span v-if="item.compareSelected" class="result__focus-tag result__focus-tag--compare">已选对比</span>
            </div>
            <p class="result__meta">{{ item.meta }}</p>
            <div class="result__tags">
              <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <span class="result__hint">{{ resultHintFor(item) }}</span>
          </div>
          <button
            type="button"
            class="favorite-chip"
            :class="{ 'favorite-chip--on': item.favored, 'favorite-chip--disabled': !canFavorite }"
            :disabled="!canFavorite"
            :aria-label="item.favored ? `取消收藏 ${item.title}` : `收藏 ${item.title}`"
            :aria-pressed="item.favored ? 'true' : 'false'"
            @click.stop="toggleResultFavorite(item)"
          >
            <Star :size="16" :stroke-width="2.25" :fill="item.favored ? 'currentColor' : 'none'" />
          </button>
        </li>
      </ul>
    </section>

    <StudioFilterDrawer
      :visible="drawerVisible"
      :value="appliedFilters"
      :result-count="resultCount"
      @close="drawerVisible = false"
      @apply="applyFilters"
    />

    <StudioSearchEditor
      :visible="searchEditorVisible"
      :value="searchValue"
      :result-count="resultCount"
      @close="searchEditorVisible = false"
      @apply="applySearch"
    />

    <PenActionBar
      v-if="compareMode"
      soft-label="取消全部"
      dark-label="进入对比"
      :soft-disabled="!compareCount"
      :dark-disabled="!canEnterCompare"
      @soft="clearCompareSelection"
      @dark="onCompare"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.search-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 68px;
  padding: 14px 16px;
  border: 1px solid $pen-hairline;
  border-radius: 18px;
  background: $pen-canvas;
  cursor: pointer;

  &__icon {
    flex: none;
    color: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__title {
    color: $pen-ink;
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__chevron {
    flex: none;
    color: $pen-ink;
  }
}

.filter-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__title {
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  &__chip {
    display: inline-flex;
    align-items: center;
    min-height: 34px;
    padding: 6px 12px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;

    &--muted {
      background: $pen-soft;
      color: $pen-mute;
    }
  }
}

.filter-trigger {
  display: inline-flex;
  flex: none;
  align-items: center;
  gap: 6px;
  height: 42px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}

.compare-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: $pen-ink;
  cursor: pointer;

  &__label {
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__track {
    width: 42px;
    height: 24px;
    padding: 2px;
    border-radius: 999px;
    background: $pen-hairline-strong;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    box-sizing: border-box;
  }

  &__thumb {
    width: 20px;
    height: 20px;
    border-radius: 999px;
    background: $pen-canvas;
    box-shadow: 0 1px 3px rgb(0 0 0 / 18%);
    transition: transform .18s ease;
  }

  &--active {
    .compare-switch__track {
      background: $pen-ink;
      justify-content: flex-end;
    }
  }
}

.map-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.map-panel__canvas {
  min-height: 280px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-soft;
  overflow: hidden;
}

.map-panel__status {
  margin: 0;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;

  &--focus {
    color: $pen-ink;
  }
}

.map-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.map-panel__focus {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
}

.map-panel__hint {
  margin: 0;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
}

.map-panel__fallback {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.map-chip {
  min-height: 44px;
  padding: 8px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 12px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
  text-align: left;
  cursor: pointer;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.result {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 124px;
  padding: 10px 12px;
  border-radius: 16px;
  cursor: pointer;

  &--focused {
    background: $pen-soft;
  }

  &--compare {
    background: #f9f9f9;
    outline: 1px solid $pen-ink;
  }

  &__cover {
    flex: none;
    display: grid;
    place-items: center;
    width: 112px;
    align-self: stretch;
    border-radius: 14px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px 0;
  }

  &__title-row {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  &__title {
    min-width: 0;
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__focus-tag {
    flex: none;
    display: inline-flex;
    align-items: center;
    min-height: 28px;
    padding: 4px 10px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;

    &--compare {
      background: $pen-success;
    }
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  &__hint {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    color: $pen-mute;
  }
}

.tag {
  height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.favorite-chip {
  flex: none;
  width: 36px;
  height: 36px;
  border: 1px solid $pen-hairline-strong;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  display: grid;
  place-items: center;
  line-height: $pen-lh;
  cursor: pointer;

  &--on {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &--disabled {
    opacity: 0.5;
  }
}
</style>
