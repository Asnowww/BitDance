<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { useAppStore, CITY_LIST } from '@/stores/app';
import { fetchNearbyStudios, type StudioCard } from '@/api/studio';

const appStore = useAppStore();
const router = useRouter();

const list = ref<StudioCard[]>([]);
const page = ref(1);
const pageSize = 20;
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const cityPickerVisible = ref(false);
const keyword = ref('');

const load = async (reset = false) => {
  if (loading.value) return;
  loading.value = true;
  if (reset) {
    page.value = 1;
    finished.value = false;
  }
  try {
    const data = await fetchNearbyStudios({
      city: appStore.city,
      page: page.value,
      pageSize,
      keyword: keyword.value || undefined
    });
    if (reset) list.value = data.list;
    else list.value = list.value.concat(data.list);
    if (list.value.length >= data.total || data.list.length === 0) finished.value = true;
    else page.value += 1;
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const onRefresh = () => {
  refreshing.value = true;
  void load(true);
};

const onLoad = () => {
  if (finished.value) return;
  void load(false);
};

const onPickCity = (city: string) => {
  appStore.setCity(city);
  cityPickerVisible.value = false;
  void load(true);
};

const onLocate = () => {
  if (!navigator.geolocation) {
    showToast('当前环境不支持定位');
    return;
  }
  showToast('定位中…');
  navigator.geolocation.getCurrentPosition(
    () => showToast('已获取定位（mock 阶段不调用真实地理编码）'),
    () => showToast('定位失败，请检查权限')
  );
};

onMounted(() => void load(true));
</script>

<template>
  <div class="home">
    <header class="home__header">
      <div class="home__top">
        <button class="city" @click="cityPickerVisible = true">
          <span>📍 {{ appStore.city }}</span>
          <span class="city__caret">▾</span>
        </button>
        <button class="locate" @click="onLocate">定位</button>
      </div>
      <div class="search" @click="router.push('/search')">
        <span class="search__icon">🔍</span>
        <span class="search__placeholder">搜索舞室、舞种或老师</span>
      </div>
    </header>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="到底啦"
        @load="onLoad"
      >
        <section class="home__list">
          <article
            v-for="s in list"
            :key="s.id"
            class="card"
            @click="router.push(`/studio/${s.id}`)"
          >
            <div class="card__cover">
              <span class="card__cover-fallback">{{ s.topStyles[0] || '舞' }}</span>
            </div>
            <div class="card__body">
              <div class="card__title">{{ s.name }}</div>
              <div class="card__meta">
                <span>{{ s.area }}</span>
                <span>·</span>
                <span>{{ s.distanceKm }}km</span>
              </div>
              <div class="card__rating">
                <span class="rating__star">★</span>
                <span class="rating__num">{{ s.ratingAvg }}</span>
                <span class="rating__count">({{ s.reviewCount }})</span>
              </div>
              <div class="card__tags">
                <span v-for="t in s.topStyles" :key="t" class="tag">{{ t }}</span>
                <span v-if="s.beginnerFriendly" class="tag tag--accent">零基础友好</span>
              </div>
            </div>
          </article>
        </section>
      </van-list>
    </van-pull-refresh>

    <van-popup
      v-model:show="cityPickerVisible"
      position="bottom"
      round
      :style="{ height: '52%' }"
    >
      <div class="city-picker">
        <div class="city-picker__title">切换城市</div>
        <div class="city-picker__grid">
          <button
            v-for="c in CITY_LIST"
            :key="c"
            class="city-picker__item"
            :class="{ active: c === appStore.city }"
            @click="onPickCity(c)"
          >
            {{ c }}
          </button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style lang="scss" scoped>
.home {
  padding: 0 0 16px;
  &__header {
    position: sticky;
    top: 0;
    background: var(--bd-bg);
    padding: 12px 12px 8px;
    z-index: 10;
  }
  &__top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  &__list {
    column-count: 2;
    column-gap: 8px;
    padding: 0 12px;
  }
}
.city {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--bd-surface);
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--bd-text);
  cursor: pointer;
  &__caret {
    font-size: 10px;
    color: var(--bd-text-secondary);
  }
}
.locate {
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--bd-primary);
  cursor: pointer;
}
.search {
  height: 36px;
  background: var(--bd-surface);
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  &__icon {
    font-size: 14px;
    color: var(--bd-text-secondary);
  }
  &__placeholder {
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.card {
  break-inside: avoid;
  margin-bottom: 8px;
  background: var(--bd-surface);
  border-radius: var(--bd-radius-md);
  overflow: hidden;
  cursor: pointer;
  &__cover {
    width: 100%;
    aspect-ratio: 3 / 4;
    background: linear-gradient(135deg, #ffd2da, #ff2442);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
    font-weight: 600;
  }
  &__body {
    padding: 8px 10px 10px;
  }
  &__title {
    font-size: 14px;
    font-weight: 600;
    line-height: 1.3;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  &__meta {
    margin-top: 4px;
    display: flex;
    gap: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
  &__rating {
    margin-top: 4px;
    display: flex;
    align-items: baseline;
    gap: 2px;
    font-size: 11px;
  }
  &__tags {
    margin-top: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
.rating {
  &__star {
    color: #ffaa33;
  }
  &__num {
    font-weight: 600;
  }
  &__count {
    color: var(--bd-text-secondary);
  }
}
.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(255, 36, 66, 0.08);
  color: var(--bd-primary);
  &--accent {
    background: rgba(54, 165, 255, 0.1);
    color: #36a5ff;
  }
}
.city-picker {
  padding: 20px 16px 32px;
  &__title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 16px;
  }
  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
  }
  &__item {
    height: 36px;
    border: 1px solid var(--bd-border);
    border-radius: 8px;
    background: #fafafa;
    color: var(--bd-text);
    font-size: 13px;
    cursor: pointer;
    &.active {
      border-color: var(--bd-primary);
      background: rgba(255, 36, 66, 0.06);
      color: var(--bd-primary);
    }
  }
}
</style>
