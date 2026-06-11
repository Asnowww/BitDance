<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Check, ChevronDown, ChevronRight, ListFilter, Music, Search, Star, UserRound, X } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import StudioFilterDrawer, { type StudioFilterValue } from '@/components/studio/StudioFilterDrawer.vue';
import StudioSearchEditor, { type StudioSearchEditorValue } from '@/components/studio/StudioSearchEditor.vue';
import { fetchNearbyStudios, type StudioCard, type StudioListQuery } from '@/api/studio';
import { fetchFavorites, toggleFavorite } from '@/api/favorite';
import { searchPublicUsers, type PublicUserProfile } from '@/api/userHome';
import { getToken } from '@/utils/request';
import { getCityName } from '@/constants/cities';
import { hasTencentMapConfig, loadTencentMap } from '@/utils/tencentMap';

const route = useRoute();
const router = useRouter();

type SearchPreset = 'zero-basic' | 'trial';
type SearchMode = 'studio' | 'user';

interface SearchResult {
  kind: SearchMode;
  id: string;
  title: string;
  meta: string;
  tags: string[];
  hint: string;
  to: string;
  favored: boolean;
  compareSelected: boolean;
}

const parseRouteSearchMode = (): SearchMode => (route.query.mode === 'user' ? 'user' : 'studio');

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
const searchModeSheetVisible = ref(false);
const searchMode = ref<SearchMode>(parseRouteSearchMode());
const searchValue = ref<StudioSearchEditorValue>(parseRouteSearch());
const locatedCoords = ref<{ latitude: number; longitude: number } | null>(null);
const studios = ref<StudioCard[]>([]);
const users = ref<PublicUserProfile[]>([]);
const resultCount = ref<number>();
const userResultCount = ref<number>();
const loading = ref(false);
const query = ref<StudioListQuery>({ page: 1, pageSize: 20 });
const appliedFilters = ref<StudioFilterValue>(buildPresetFilters(parseRoutePreset()));
const compareMode = ref(false);
const compareSelection = ref<Record<string, boolean>>({});
const favoriteState = ref<Record<number, boolean>>({});
const canFavorite = computed(() => Boolean(getToken()));
const mapContainer = ref<HTMLElement | null>(null);
const mapStatus = ref(hasTencentMapConfig() ? '地图加载中' : '未配置腾讯地图 Key，显示列表和坐标');
const selectedMapStudioId = ref<number>();
const resultItemRefs = new Map<number, HTMLElement>();
let tencentMapApi: Record<string, any> | null = null;
let map: any = null;
let markerLayer: any = null;

const searchModeOptions: Array<{ value: SearchMode; label: string; meta: string }> = [
  { value: 'studio', label: '舞室等', meta: '舞室、课程、老师与评价' },
  { value: 'user', label: '用户', meta: '昵称、公开主页与舞蹈资料' }
];

const isUserMode = computed(() => searchMode.value === 'user');
const activeSearchModeLabel = computed(
  () => searchModeOptions.find((option) => option.value === searchMode.value)?.label ?? '舞室等'
);
const selectedMapStudio = computed(() => studios.value.find((studio) => studio.id === selectedMapStudioId.value) ?? null);
const compareSelectedStudioIds = computed(() =>
  Object.entries(compareSelection.value).filter(([, on]) => on).map(([id]) => Number(id))
);
const compareCount = computed(() => compareSelectedStudioIds.value.length);
const canEnterCompare = computed(() => compareCount.value >= 2 && compareCount.value <= 3);

const results = computed<SearchResult[]>(() => {
  if (isUserMode.value) {
    return users.value.map((user) => {
      const tags = (user.styles ?? [])
        .map((style) => style.name || style.skillLevel)
        .filter(Boolean)
        .slice(0, 3) as string[];
      return {
        kind: 'user',
        id: String(user.userId),
        title: user.nickname || `用户 ${user.userId}`,
        meta: user.bio || user.currentLevel || '公开用户资料',
        tags: tags.length ? tags : [user.currentLevel || '舞者'],
        hint: '查看公开主页',
        to: `/user/${user.userId}`,
        favored: false,
        compareSelected: false
      };
    });
  }

  return studios.value.map((studio) => {
    const favored = Boolean(favoriteState.value[studio.id] ?? studio.favored);
    return {
      kind: 'studio',
      id: String(studio.id),
      title: studio.name,
      meta: `${studio.distanceKm ?? '-'}km · ${studio.address || '地址待完善'}`,
      tags: [favored ? '已收藏' : '附近舞室'],
      hint: '课程 · 老师 · 评价',
      to: `/studio/${studio.id}`,
      favored,
      compareSelected: Boolean(compareSelection.value[String(studio.id)])
    };
  });
});

const resultSummary = computed(() => {
  if (isUserMode.value) return `找到 ${userResultCount.value ?? users.value.length} 位用户`;
  const place = searchValue.value.useNearby ? '附近' : getCityName(searchValue.value.cityId) || '当前城市';
  return `${place}找到 ${resultCount.value ?? studios.value.length} 家舞室`;
});

const searchContextMeta = computed(() => {
  if (isUserMode.value) {
    return searchValue.value.keyword ? `按昵称搜索「${searchValue.value.keyword}」` : '全部公开用户';
  }
  const parts = [searchValue.value.useNearby ? '附近结果' : getCityName(searchValue.value.cityId) || '指定城市'];
  if (searchValue.value.keyword) parts.push(`关键词「${searchValue.value.keyword}」`);
  return parts.join(' · ');
});

const activeFilterTags = computed(() => {
  if (isUserMode.value) return [searchValue.value.keyword ? `昵称：${searchValue.value.keyword}` : '全部公开用户'];
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
const selectionHint = computed(() => {
  if (!compareMode.value) return '点卡片可查看详情，点右侧星标收藏';
  if (compareCount.value === 0) return '点卡片加入对比，最多同时选择 3 家';
  if (compareCount.value === 1) return '再选 1 家就能进入对比';
  return `已选 ${compareCount.value} 家，可以进入对比`;
});
const compareHint = computed(() => {
  if (compareCount.value === 0) return '开启后点卡片加入对比，右侧星标仍然负责收藏';
  if (compareCount.value === 1) return '再点 1 家就能进入对比页';
  if (compareCount.value === 2) return '已经足够开始对比，还可以再补 1 家';
  return '已满 3 家，想换对象先取消 1 家';
});
const mapFocusHint = computed(() =>
  selectedMapStudio.value
    ? `已在地图定位 ${selectedMapStudio.value.name}，列表会同步高亮`
    : '点地图上的舞室，列表会定位到同一条结果'
);

const syncRouteQuery = () => {
  const nextQuery: Record<string, string> = {};
  if (isUserMode.value) nextQuery.mode = 'user';
  if (searchValue.value.keyword) nextQuery.keyword = searchValue.value.keyword;
  if (!isUserMode.value && !searchValue.value.useNearby && searchValue.value.cityId) {
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

const loadUsers = async () => {
  loading.value = true;
  try {
    const response = await searchPublicUsers(searchValue.value.keyword ?? '', 1, 20);
    users.value = response.list ?? [];
    userResultCount.value = response.total ?? users.value.length;
  } finally {
    loading.value = false;
  }
};

const loadStudios = async () => {
  loading.value = true;
  try {
    syncQueryFromState();
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
  if (isUserMode.value) return;
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
      map = new TMap.Map(mapContainer.value, { viewMode: '2D', zoom: points.length ? 13 : 11, center });
    } else {
      map.setCenter?.(center);
      map.setZoom?.(points.length ? 13 : 11);
    }
    resetMarkers();
    markerLayer = new TMap.MultiMarker({
      id: 'bitdance-studio-markers',
      map,
      styles: {
        studio: new TMap.MarkerStyle({ width: 28, height: 36, anchor: { x: 14, y: 36 } }),
        studioActive: new TMap.MarkerStyle({ width: 34, height: 42, anchor: { x: 17, y: 42 } })
      },
      geometries: points.map((studio) => ({
        id: String(studio.id),
        styleId: selectedMapStudioId.value === studio.id ? 'studioActive' : 'studio',
        position: new TMap.LatLng(Number(studio.latitude), Number(studio.longitude)),
        properties: { studioId: studio.id, title: studio.name }
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
    mapStatus.value = '地图加载失败，已回退到列表视图数据';
  }
};

const locate = () => {
  let settled = false;
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
  if (isUserMode.value) return;
  drawerVisible.value = true;
};

const openSearchEditor = () => {
  searchEditorVisible.value = true;
};

const openSearchModeSheet = () => {
  searchModeSheetVisible.value = true;
};

const applySearch = (value: StudioSearchEditorValue) => {
  searchValue.value = value;
  searchEditorVisible.value = false;
  syncRouteQuery();
  if (isUserMode.value) {
    void loadUsers();
    return;
  }
  void loadStudios().then(renderMap);
};

const applySearchMode = (mode: SearchMode) => {
  searchModeSheetVisible.value = false;
  if (searchMode.value === mode) return;
  searchMode.value = mode;
  compareMode.value = false;
  compareSelection.value = {};
  selectedMapStudioId.value = undefined;
  syncRouteQuery();
  if (isUserMode.value) {
    resetMarkers();
    void loadUsers();
    return;
  }
  if (searchValue.value.useNearby && !locatedCoords.value) {
    locate();
    return;
  }
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
  if (compareSelection.value[id]) focusStudio(Number(id));
};

const handleResultClick = (item: SearchResult) => {
  if (item.kind === 'user') {
    router.push(item.to);
    return;
  }
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
  if (item.kind !== 'studio') return;
  if (!canFavorite.value) {
    showToast('请先登录后收藏');
    return;
  }
  const id = Number(item.id);
  const { favored } = await toggleFavorite('studio', id);
  favoriteState.value = { ...favoriteState.value, [id]: favored };
  studios.value = studios.value.map((studio) => (studio.id === id ? { ...studio, favored } : studio));
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
  if (item.kind === 'user') return item.hint;
  if (compareMode.value) return item.compareSelected ? '已加入对比' : '点卡片加入对比';
  if (selectedMapStudioId.value === Number(item.id)) return '已和地图对齐，再点一次看详情';
  return item.hint;
};

onMounted(() => {
  if (isUserMode.value) {
    void loadUsers();
    return;
  }
  locate();
});

watch(selectedMapStudioId, () => {
  void renderMap();
});

watch(studios, (list) => {
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
      <section class="search-summary" aria-label="搜索结果摘要">
        <Search class="search-summary__icon" :size="18" :stroke-width="2" />
        <button
          type="button"
          class="search-summary__mode"
          aria-label="选择搜索类型"
          @click.stop="openSearchModeSheet"
        >
          <span>{{ activeSearchModeLabel }}</span>
          <ChevronDown :size="15" :stroke-width="2.4" />
        </button>
        <button
          type="button"
          class="search-summary__button"
          @click="openSearchEditor"
          @keydown.enter.prevent="openSearchEditor"
          @keydown.space.prevent="openSearchEditor"
        >
          <span class="search-summary__body">
            <strong class="search-summary__title">{{ searchSummaryTitle }}</strong>
            <span class="search-summary__meta">{{ searchSummaryMeta }}</span>
          </span>
          <ChevronRight class="search-summary__chevron" :size="18" :stroke-width="2" />
        </button>
      </section>

      <section class="filter-toolbar" aria-label="筛选与选择">
        <div class="filter-toolbar__body">
          <strong class="filter-toolbar__title">{{ isUserMode ? '当前搜索' : '当前筛选' }}</strong>
          <div class="filter-toolbar__chips">
            <span v-for="tag in activeFilterTags" :key="tag" class="filter-toolbar__chip">{{ tag }}</span>
            <span v-if="!activeFilterTags.length" class="filter-toolbar__chip filter-toolbar__chip--muted">默认附近结果</span>
          </div>
        </div>
        <button v-if="!isUserMode" type="button" class="filter-trigger" @click="openFilter">
          <ListFilter :size="16" :stroke-width="2" />
          <span>筛选</span>
        </button>
      </section>

      <section v-if="!isUserMode" class="map-panel" aria-label="舞室地图">
        <div ref="mapContainer" class="map-panel__canvas" />
        <p class="map-panel__status">{{ mapStatus }}</p>
        <div class="map-panel__footer">
          <p class="map-panel__focus">{{ mapFocusHint }}</p>
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

      <p v-if="loading" class="loading-state">加载中...</p>
      <ul v-else class="result-list">
        <li
          v-for="item in results"
          :key="`${item.kind}-${item.id}`"
          :ref="item.kind === 'studio' ? (el) => bindResultRef(Number(item.id), el as Element | null) : undefined"
          class="result"
          :class="{
            'result--focused': item.kind === 'studio' && selectedMapStudioId === Number(item.id),
            'result--compare': item.compareSelected,
            'result--user': item.kind === 'user'
          }"
          @click="handleResultClick(item)"
        >
          <div class="result__cover" aria-hidden="true">
            <UserRound v-if="item.kind === 'user'" :size="28" :stroke-width="2" />
            <Music v-else :size="28" :stroke-width="2" />
          </div>
          <div class="result__body">
            <div class="result__title-row">
              <strong class="result__title">{{ item.title }}</strong>
              <span v-if="item.kind === 'studio' && selectedMapStudioId === Number(item.id)" class="result__focus-tag">地图已定位</span>
              <span v-if="item.compareSelected" class="result__focus-tag result__focus-tag--compare">已选对比</span>
            </div>
            <p class="result__meta">{{ item.meta }}</p>
            <div class="result__tags">
              <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <span class="result__hint">{{ resultHintFor(item) }}</span>
          </div>
          <button
            v-if="item.kind === 'studio'"
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
      <p v-if="!loading && !results.length" class="loading-state">
        {{ isUserMode ? '没有找到公开用户' : '没有找到符合条件的舞室' }}
      </p>
    </section>

    <StudioFilterDrawer
      :visible="drawerVisible"
      :value="appliedFilters"
      :result-count="resultCount"
      @close="drawerVisible = false"
      @apply="applyFilters"
    />

    <Teleport to="body">
      <Transition name="mode-sheet-fade">
        <button
          v-if="searchModeSheetVisible"
          class="mode-sheet-mask"
          type="button"
          aria-label="关闭搜索类型选择"
          @click="searchModeSheetVisible = false"
        />
      </Transition>
      <Transition name="mode-sheet-slide">
        <aside v-if="searchModeSheetVisible" class="mode-sheet" role="dialog" aria-modal="true" aria-label="选择搜索类型">
          <div class="mode-sheet__handle" />
          <header class="mode-sheet__head">
            <div>
              <span>Search Scope</span>
              <h2>选择搜索类型</h2>
            </div>
            <button type="button" class="mode-sheet__close" aria-label="关闭" @click="searchModeSheetVisible = false">
              <X :size="18" :stroke-width="2.5" />
            </button>
          </header>
          <div class="mode-sheet__options">
            <button
              v-for="option in searchModeOptions"
              :key="option.value"
              type="button"
              class="mode-sheet__option"
              :class="{ 'mode-sheet__option--active': searchMode === option.value }"
              @click="applySearchMode(option.value)"
            >
              <span class="mode-sheet__icon" aria-hidden="true">
                <Music v-if="option.value === 'studio'" :size="20" :stroke-width="2.3" />
                <UserRound v-else :size="20" :stroke-width="2.3" />
              </span>
              <span class="mode-sheet__copy">
                <strong>{{ option.label }}</strong>
                <em>{{ option.meta }}</em>
              </span>
              <span class="mode-sheet__check" aria-hidden="true">
                <Check v-if="searchMode === option.value" :size="17" :stroke-width="2.8" />
              </span>
            </button>
          </div>
        </aside>
      </Transition>
    </Teleport>

    <StudioSearchEditor
      :visible="searchEditorVisible"
      :value="searchValue"
      :result-count="isUserMode ? userResultCount : resultCount"
      @close="searchEditorVisible = false"
      @apply="applySearch"
    />

    <PenActionBar
      v-if="compareMode && !isUserMode"
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
  padding: 12px 14px;
  border: 1px solid $pen-hairline;
  border-radius: 18px;
  background: $pen-canvas;

  &__icon,
  &__chevron {
    flex: none;
    color: $pen-ink;
  }

  &__mode {
    flex: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    min-width: 88px;
    height: 40px;
    padding: 0 10px;
    border: 1px solid $pen-hairline;
    border-radius: 12px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &__button {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0;
    border: 0;
    background: transparent;
    color: inherit;
    cursor: pointer;
    text-align: left;
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
    color: $pen-mute;
    font-size: 13px;
    font-weight: 600;
    line-height: $pen-lh;
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

.map-panel__status,
.map-panel__hint,
.map-panel__focus {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  line-height: $pen-lh;
}

.map-panel__status,
.map-panel__hint {
  color: $pen-mute;
}

.map-panel__focus {
  flex: 1;
  min-width: 0;
  color: $pen-ink;
}

.map-panel__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

  &--user {
    border: 1px solid $pen-hairline;
    background: $pen-canvas;
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

.loading-state {
  margin: 0;
  padding: 18px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 900;
  line-height: $pen-lh;
  text-align: center;
}

.mode-sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 120;
  border: 0;
  background: rgb(17 17 17 / 42%);
  backdrop-filter: blur(5px);
  cursor: pointer;
}

.mode-sheet {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 130;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  padding: 10px 18px calc(18px + env(safe-area-inset-bottom));
  border-radius: 24px 24px 0 0;
  background: $pen-canvas;
  box-shadow: 0 -4px 18px rgb(0 0 0 / 12%);
  box-sizing: border-box;

  &__handle {
    width: 46px;
    height: 5px;
    margin: 0 auto 14px;
    border-radius: 999px;
    background: $pen-hairline;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;

    div {
      min-width: 0;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      line-height: $pen-lh;
      font-weight: 900;
      text-transform: uppercase;
    }

    h2 {
      margin: 6px 0 0;
      color: $pen-ink;
      font-size: 24px;
      line-height: $pen-lh;
      font-weight: 900;
      letter-spacing: 0;
    }
  }

  &__close {
    flex: none;
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    cursor: pointer;
  }

  &__options {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-top: 18px;
  }

  &__option {
    min-height: 74px;
    padding: 12px;
    border: 1px solid $pen-hairline;
    border-radius: 18px;
    background: $pen-canvas;
    color: $pen-ink;
    display: flex;
    align-items: center;
    gap: 12px;
    text-align: left;
    cursor: pointer;

    &--active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;

      .mode-sheet__icon,
      .mode-sheet__check {
        background: $pen-on-primary;
        color: $pen-ink;
      }

      .mode-sheet__copy em {
        color: $pen-subtle-text;
      }
    }
  }

  &__icon,
  &__check {
    flex: none;
    display: grid;
    place-items: center;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__icon {
    width: 42px;
    height: 42px;
  }

  &__check {
    width: 30px;
    height: 30px;
  }

  &__copy {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 5px;

    strong {
      font-size: 16px;
      line-height: $pen-lh;
      font-weight: 900;
      letter-spacing: 0;
    }

    em {
      color: $pen-mute;
      font-size: 12px;
      line-height: $pen-lh;
      font-style: normal;
      font-weight: 700;
      letter-spacing: 0;
    }
  }
}

.mode-sheet-fade-enter-active,
.mode-sheet-fade-leave-active,
.mode-sheet-slide-enter-active,
.mode-sheet-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.mode-sheet-fade-enter-from,
.mode-sheet-fade-leave-to {
  opacity: 0;
}

.mode-sheet-slide-enter-from,
.mode-sheet-slide-leave-to {
  opacity: 0;
  transform: translateY(24px);
}
</style>
