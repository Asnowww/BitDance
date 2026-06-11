<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchCoachDetail, fetchCourseDetail } from '@/api/course';
import { fetchFavorites, type FavoriteDto, type FavoriteTargetType } from '@/api/favorite';
import { fetchStudioDetail } from '@/api/studio';
import { fetchWorkshopDetail } from '@/api/workshop';

const router = useRouter();

interface FavoriteCard {
  id: string;
  title: string;
  meta: string;
  tag: string;
  action: string;
  to: string;
}

const favorites = ref<FavoriteCard[]>([]);
const loading = ref(false);
const favoriteStatus = ref('');
const favoriteCountText = computed(() => (loading.value ? '同步中' : `${favorites.value.length} 项`));

const typeLabel: Record<FavoriteTargetType, string> = {
  studio: '舞室',
  course: '课程',
  coach: '老师',
  workshop: '活动',
  content_post: '动态'
};

const typeAction: Record<FavoriteTargetType, string> = {
  studio: '预约试听',
  course: '查看课程',
  coach: '查看老师',
  workshop: '查看活动',
  content_post: '查看动态'
};

const routeForFavorite = (item: FavoriteDto) => {
  if (item.targetType === 'studio') return `/studio/${item.targetId}`;
  if (item.targetType === 'course') return `/course/${item.targetId}`;
  if (item.targetType === 'coach') return `/coach/${item.targetId}`;
  if (item.targetType === 'content_post') return '/community/post/' + item.targetId;
  return `/workshop/${item.targetId}`;
};

const formatCreatedAt = (value: string) =>
  new Date(value).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });

const fallbackCard = (item: FavoriteDto): FavoriteCard => ({
  id: String(item.id),
  title: `${typeLabel[item.targetType] ?? item.targetType} #${item.targetId}`,
  meta: `收藏于 ${formatCreatedAt(item.createdAt)} · 详情待补全`,
  tag: typeLabel[item.targetType] ?? item.targetType,
  action: typeAction[item.targetType] ?? '查看',
  to: routeForFavorite(item)
});

const buildFavoriteCard = async (item: FavoriteDto): Promise<FavoriteCard> => {
  if (item.card) {
    return {
      id: String(item.id),
      title: item.card.title,
      meta: item.card.subtitle,
      tag: typeLabel[item.targetType] ?? item.targetType,
      action: item.card.actionText,
      to: item.card.path || routeForFavorite(item)
    };
  }
  try {
    // M1 收藏管理：收藏接口只返回类型和 ID，这里按对象类型补详情，不完整时降级显示。
    if (item.targetType === 'studio') {
      const detail = await fetchStudioDetail(item.targetId);
      return {
        id: String(item.id),
        title: detail.name,
        meta: `收藏于 ${formatCreatedAt(item.createdAt)} · ${detail.address || '地址待补'}`,
        tag: typeLabel[item.targetType],
        action: typeAction[item.targetType],
        to: routeForFavorite(item)
      };
    }
    if (item.targetType === 'course') {
      const detail = await fetchCourseDetail(item.targetId);
      return {
        id: String(item.id),
        title: detail.courseName,
        meta: `收藏于 ${formatCreatedAt(item.createdAt)} · ¥${detail.priceAmount} · ${detail.difficultyLevel}`,
        tag: typeLabel[item.targetType],
        action: typeAction[item.targetType],
        to: routeForFavorite(item)
      };
    }
    if (item.targetType === 'coach') {
      const detail = await fetchCoachDetail(item.targetId);
      return {
        id: String(item.id),
        title: detail.displayName,
        meta: `收藏于 ${formatCreatedAt(item.createdAt)} · 评分 ${Number(detail.avgRating ?? 0).toFixed(1)}`,
        tag: typeLabel[item.targetType],
        action: typeAction[item.targetType],
        to: routeForFavorite(item)
      };
    }
    const detail = await fetchWorkshopDetail(item.targetId);
    return {
      id: String(item.id),
      title: detail.title,
      meta: `收藏于 ${formatCreatedAt(item.createdAt)} · ${detail.city} · ${detail.coachName}`,
      tag: typeLabel[item.targetType],
      action: typeAction[item.targetType],
      to: routeForFavorite(item)
    };
  } catch {
    return fallbackCard(item);
  }
};

const loadFavorites = async () => {
  loading.value = true;
  favoriteStatus.value = '';
  try {
    const list = await fetchFavorites();
    favorites.value = await Promise.all(list.map(buildFavoriteCard));
    favoriteStatus.value = favorites.value.length ? '' : '暂无收藏，可先在舞室、课程或老师详情页点收藏';
  } catch {
    favorites.value = [];
    favoriteStatus.value = '收藏接口暂不可用，请检查登录态或后端服务';
  } finally {
    loading.value = false;
  }
};

onMounted(loadFavorites);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="收藏管理" @share="showToast('收藏清单链接已复制')" />

    <section class="pen-scroll">
      <section class="favorites">
        <header class="favorites__head">
          <h3>收藏管理</h3>
          <span class="favorites__sub">{{ favoriteCountText }}</span>
        </header>

        <p v-if="favoriteStatus" class="favorites__empty">{{ favoriteStatus }}</p>

        <article
          v-for="item in favorites"
          :key="item.id"
          class="fav"
          @click="router.push(item.to)"
        >
          <div class="fav__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="fav__body">
            <strong class="fav__title">{{ item.title }}</strong>
            <p class="fav__meta">{{ item.meta }}</p>
            <span class="tag">{{ item.tag }}</span>
            <button class="fav__action" type="button" @click.stop="router.push(item.to)">
              {{ item.action }}
            </button>
          </div>
        </article>
      </section>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.favorites {
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__head h3 {
    @include pen-h3-section;
    flex: 1;
  }

  &__sub {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__empty {
    margin: 0;
    padding: 14px;
    border: 1px solid $pen-hairline;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.fav {
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

  &__action {
    align-self: flex-start;
    padding: 0;
    border: 0;
    background: transparent;
    color: $pen-success;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.tag {
  align-self: flex-start;
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
</style>
