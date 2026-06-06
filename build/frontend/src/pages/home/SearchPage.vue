<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music, Search } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import StudioFilterDrawer, { type StudioFilterValue } from '@/components/studio/StudioFilterDrawer.vue';
import { fetchNearbyStudios, type StudioCard, type StudioListQuery } from '@/api/studio';
import { toggleFavorite } from '@/api/favorite';
import { hasTencentMapConfig, loadTencentMap } from '@/utils/tencentMap';

const router = useRouter();

const filters = ['舞种', '距离', '价格', '时段', '舞室'];
const activeFilter = ref('距离');
const drawerVisible = ref(false);
const viewMode = ref<'list' | 'map'>('list');

interface SearchResult {
  id: string;
  title: string;
  meta: string;
  tags: string[];
  price: string;
  priceTone: 'ink' | 'success';
  to: string;
}

const studios = ref<StudioCard[]>([]);
const resultCount = ref<number>();
const loading = ref(false);
const query = ref<StudioListQuery>({ page: 1, pageSize: 20, distanceKm: 5 });
const appliedFilters = ref<StudioFilterValue>({ distanceKm: 5 });
const mapContainer = ref<HTMLElement | null>(null);
const mapStatus = ref(hasTencentMapConfig() ? '地图加载中' : '未配置腾讯地图 Key，展示坐标降级视图');
const selectedMapStudioId = ref<number>();
let tencentMapApi: Record<string, any> | null = null;
let map: any = null;
let markerLayer: any = null;

const results = computed<SearchResult[]>(() =>
  studios.value.map((studio) => ({
    id: String(studio.id),
    title: studio.name,
    meta: `${studio.distanceKm ?? '-'}km · ${studio.address || '地址待完善'}`,
    tags: studio.favored ? ['已收藏'] : ['附近舞室'],
    price: '查看详情',
    priceTone: 'success',
    to: `/studio/${studio.id}`
  }))
);

const loadStudios = async () => {
  loading.value = true;
  try {
    const response = await fetchNearbyStudios(query.value);
    studios.value = response.list;
    resultCount.value = response.total;
  } finally {
    loading.value = false;
  }
};

const resetMarkers = () => {
  markerLayer?.setMap?.(null);
  markerLayer = null;
};

const renderMap = async () => {
  if (viewMode.value !== 'map') return;
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
        })
      },
      geometries: points.map((studio) => ({
        id: String(studio.id),
        styleId: 'studio',
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
      selectedMapStudioId.value = id;
      router.push(`/studio/${id}`);
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
    void loadStudios().then(renderMap);
  }, 1200);

  navigator.geolocation?.getCurrentPosition(
    ({ coords }) => {
      if (settled) return;
      settled = true;
      window.clearTimeout(fallbackTimer);
      query.value = { ...query.value, latitude: coords.latitude, longitude: coords.longitude };
      void loadStudios().then(renderMap);
    },
    () => {
      if (settled) return;
      settled = true;
      window.clearTimeout(fallbackTimer);
      void loadStudios().then(renderMap);
    }
  );
};

const openFilter = (filter: string) => {
  activeFilter.value = filter;
  drawerVisible.value = true;
};

const applyFilters = (filters: StudioFilterValue) => {
  appliedFilters.value = filters;
  const {
    danceStyleId: _danceStyleId,
    minPrice: _minPrice,
    maxPrice: _maxPrice,
    timeSlot: _timeSlot,
    trialAvailable: _trialAvailable,
    zeroBasicFriendly: _zeroBasicFriendly,
    nearMetro: _nearMetro,
    ...baseQuery
  } = query.value;
  query.value = {
    ...baseQuery,
    ...filters,
    page: 1
  };
  drawerVisible.value = false;
  void loadStudios().then(renderMap);
};

const filterSummary = (filter: string) => {
  if (filter === '舞种' && appliedFilters.value.danceStyleId) return `${filter} · 已选`;
  if (filter === '距离' && appliedFilters.value.distanceKm) return `${appliedFilters.value.distanceKm}km`;
  if (filter === '价格' && (appliedFilters.value.minPrice || appliedFilters.value.maxPrice !== undefined)) {
    return `¥${appliedFilters.value.minPrice ?? 0}-${appliedFilters.value.maxPrice ?? 500}`;
  }
  if (filter === '时段' && appliedFilters.value.timeSlot) {
    return { morning: '上午', afternoon: '下午', evening: '晚上', weekend: '周末' }[appliedFilters.value.timeSlot];
  }
  if (
    filter === '舞室' &&
    (appliedFilters.value.trialAvailable || appliedFilters.value.zeroBasicFriendly || appliedFilters.value.nearMetro)
  ) {
    return `${filter} · 已选`;
  }
  return filter;
};

const selected = ref<Record<string, boolean>>({});
const toggleSelect = (id: string) => {
  selected.value[id] = !selected.value[id];
};
const compareCount = () => Object.values(selected.value).filter(Boolean).length;

const onCompare = () => {
  if (compareCount() < 2) {
    showToast('请至少选择 2 个舞室进行对比');
    return;
  }
  const ids = Object.entries(selected.value).filter(([, on]) => on).map(([id]) => Number(id)).slice(0, 3);
  sessionStorage.setItem('bitdance_compare_studio_ids', JSON.stringify(ids));
  router.push('/studio/compare');
};

const onFavorite = async () => {
  const id = Number(Object.keys(selected.value).find((key) => selected.value[key]) ?? results.value[0]?.id);
  if (!id) return;
  const { favored } = await toggleFavorite('studio', id);
  showToast(favored ? '已加入收藏' : '已取消收藏');
  await loadStudios();
};

onMounted(() => {
  locate();
});

watch(viewMode, (mode) => {
  if (mode === 'map') void renderMap();
});

watch(studios, () => {
  if (viewMode.value === 'map') void renderMap();
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
      <button class="search-field" type="button" @click="router.back()">
        <Search class="search-field__icon" :size="18" :stroke-width="2" />
        <span class="search-field__text">韩舞 零基础 5km 内</span>
      </button>

      <div class="chip-row" aria-label="筛选条件">
        <button
          v-for="filter in filters"
          :key="filter"
          type="button"
          class="chip"
          :class="activeFilter === filter ? 'chip--active' : 'chip--inactive'"
          @click="openFilter(filter)"
        >
          {{ filterSummary(filter) }}
        </button>
      </div>

      <div class="toggle">
        <button
          type="button"
          class="toggle__btn"
          :class="{ 'toggle__btn--active': viewMode === 'list' }"
          @click="viewMode = 'list'"
        >
          列表
        </button>
        <button
          type="button"
          class="toggle__btn"
          :class="{ 'toggle__btn--active': viewMode === 'map' }"
          @click="viewMode = 'map'"
        >
          地图
        </button>
      </div>

      <section v-if="viewMode === 'map'" class="map-panel" aria-label="舞室地图">
        <div ref="mapContainer" class="map-panel__canvas" />
        <p class="map-panel__status">{{ mapStatus }}</p>
        <div v-if="!hasTencentMapConfig()" class="map-panel__fallback">
          <button
            v-for="studio in studios.slice(0, 8)"
            :key="studio.id"
            type="button"
            class="map-chip"
            :class="{ 'map-chip--active': selectedMapStudioId === studio.id }"
            @click="selectedMapStudioId = studio.id; router.push(`/studio/${studio.id}`)"
          >
            {{ studio.name }} · {{ studio.longitude ?? '-' }}, {{ studio.latitude ?? '-' }}
          </button>
        </div>
      </section>

      <ul class="result-list">
        <li v-for="item in results" :key="item.id" class="result" @click="router.push(item.to)">
          <div class="result__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="result__body">
            <strong class="result__title">{{ item.title }}</strong>
            <p class="result__meta">{{ item.meta }}</p>
            <div class="result__tags">
              <span v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <span class="result__price" :class="`result__price--${item.priceTone}`">
              {{ item.price }}
            </span>
          </div>
          <button
            type="button"
            class="radio"
            :class="{ 'radio--on': selected[item.id] }"
            :aria-label="`选择 ${item.title}`"
            @click.stop="toggleSelect(item.id)"
          />
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

    <PenActionBar
      soft-label="收藏"
      dark-label="加入对比"
      @soft="onFavorite"
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

.search-field {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border: 0;
  border-radius: 24px;
  background: $pen-soft;
  cursor: pointer;
  text-align: left;

  &__icon {
    flex: none;
    color: $pen-mute;
  }

  &__text {
    flex: 1;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 600;
    line-height: $pen-lh;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.toggle {
  display: flex;
  gap: 8px;

  &__btn {
    flex: 1;
    height: 48px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;

    &--active {
      background: $pen-ink;
      color: $pen-on-primary;
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
  cursor: pointer;

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

  &__title {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
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

  &__price {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;

    &--ink {
      color: $pen-ink;
    }

    &--success {
      color: $pen-success;
    }
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

.radio {
  flex: none;
  width: 24px;
  height: 24px;
  border: 2px solid $pen-ink;
  border-radius: 999px;
  background: $pen-canvas;
  cursor: pointer;

  &--on {
    background: $pen-ink;
  }
}
</style>
