<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter, type RouteLocationRaw } from 'vue-router';
import { Bell, Search, MapPin, Sparkles, CalendarDays, Star, Ticket } from 'lucide-vue-next';
import { fetchCourseDetail } from '@/api/course';
import { fetchNearbyStudios, type StudioCard } from '@/api/studio';
import { fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';

const router = useRouter();

interface QuickEntry {
  icon: typeof MapPin;
  label: string;
  meta: string;
  to: RouteLocationRaw;
}

const quickEntries: QuickEntry[] = [
  { icon: MapPin, label: '附近', meta: '搜附近结果', to: { name: 'search' } },
  {
    icon: Sparkles,
    label: '新手',
    meta: '搜零基础结果',
    to: { name: 'search', query: { preset: 'zero-basic' } }
  },
  {
    icon: CalendarDays,
    label: '试听',
    meta: '搜可试听结果',
    to: { name: 'search', query: { preset: 'trial' } }
  },
  {
    icon: Star,
    label: '老师',
    meta: '搜老师相关结果',
    to: { name: 'search', query: { keyword: '老师' } }
  },
  { icon: Ticket, label: 'Workshop', meta: '进入活动专题页', to: '/workshops' }
];

type RecommendType = 'studio' | 'course';

interface RecommendCard {
  id: string;
  type: RecommendType;
  title: string;
  meta: string;
  action: string;
  to: string;
  imageUrl: string;
}

const studioRecommends = ref<RecommendCard[]>([]);
const courseRecommends = ref<RecommendCard[]>([]);
const activeRecommendType = ref<RecommendType>('studio');
const recommendTabs: Array<{ id: RecommendType; label: string }> = [
  { id: 'studio', label: '舞室' },
  { id: 'course', label: '课程' }
];

const activeRecommends = computed(() =>
  activeRecommendType.value === 'studio' ? studioRecommends.value : courseRecommends.value
);

const recommendMoreTarget = computed(() =>
  activeRecommendType.value === 'studio' ? '/search' : { name: 'search', query: { preset: 'trial' } }
);

const studioCoverUrls = [
  'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=720&q=80',
  'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=720&q=80',
  'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?auto=format&fit=crop&w=720&q=80'
];

const courseCoverUrls = [
  'https://images.unsplash.com/photo-1535525153412-5a42439a210d?auto=format&fit=crop&w=720&q=80',
  'https://images.unsplash.com/photo-1504609813442-a8924e83f76e?auto=format&fit=crop&w=720&q=80',
  'https://images.unsplash.com/photo-1547153760-18fc86324498?auto=format&fit=crop&w=720&q=80'
];

const fallbackCoverUrl =
  'https://images.unsplash.com/photo-1529111290557-82f6d5c6cf85?auto=format&fit=crop&w=720&q=80';

const resolveCoverUrl = (type: RecommendType, id: number, coverAssetId?: number) => {
  // M1 首页推荐封面：后端当前只返回 coverAssetId，不返回可访问 URL；先用稳定图片池映射，有缺口时落到通用舞蹈图。
  const pool = type === 'studio' ? studioCoverUrls : courseCoverUrls;
  const key = coverAssetId ?? id;
  return pool[Math.abs(key) % pool.length] ?? fallbackCoverUrl;
};

const toStudioCard = (studio: StudioCard): RecommendCard => ({
  id: `studio-${studio.id}`,
  type: 'studio',
  title: studio.name,
  meta: `${studio.distanceKm ?? '-'}km · ${studio.address || '地址待完善'}`,
  action: '查看',
  to: `/studio/${studio.id}`,
  imageUrl: resolveCoverUrl('studio', studio.id, studio.coverAssetId)
});

const collectCourseSlots = async (studios: StudioCard[]) => {
  const scheduleResults = await Promise.allSettled(
    studios.map(async (studio) => {
      const slots = await fetchStudioSchedule(studio.id).catch(() => []);
      return slots.map((slot) => ({ slot, studio }));
    })
  );
  const slots: Array<{ slot: ScheduleSlot; studio: StudioCard }> = [];
  scheduleResults.forEach((result) => {
    if (result.status === 'fulfilled') slots.push(...result.value);
  });
  return slots;
};

const loadRecommendations = async () => {
  // M1 首页推荐：舞室和课程分开加载，避免“为你推荐”只混合展示两张卡。
  const nearby = await fetchNearbyStudios({ page: 1, pageSize: 8, distanceKm: 5 });
  studioRecommends.value = nearby.list.slice(0, 6).map(toStudioCard);

  const uniqueCourses = new Map<number, { slot: ScheduleSlot; studio: StudioCard }>();
  (await collectCourseSlots(nearby.list)).forEach((item) => {
    if (!uniqueCourses.has(item.slot.courseId)) uniqueCourses.set(item.slot.courseId, item);
  });
  const courseResults = await Promise.allSettled(
    Array.from(uniqueCourses.values())
      .slice(0, 6)
      .map(async ({ slot, studio }) => {
        const course = await fetchCourseDetail(slot.courseId);
        return {
          id: `course-${course.id}`,
          type: 'course' as const,
          title: course.courseName,
          meta: `${studio.name} · ${course.difficultyLevel} · ¥${course.priceAmount}/节`,
          action: '预约',
          to: `/course/${course.id}`,
          imageUrl: resolveCoverUrl('course', course.id, course.coverAssetId)
        };
      })
  );
  courseRecommends.value = courseResults
    .flatMap((item) => (item.status === 'fulfilled' ? [item.value as RecommendCard] : []));
};

const heroImage =
  'https://images.unsplash.com/photo-1667384447307-9ae9cd6ff1d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w4NDM0ODN8MHwxfHJhbmRvbXx8fHx8fHx8fDE3Nzk3ODEzMzZ8&ixlib=rb-4.1.0&q=80&w=1080';

onMounted(() => {
  void loadRecommendations();
});

const openQuickEntry = (entry: QuickEntry) => {
  void router.push(entry.to);
};
</script>

<template>
  <div class="home">
    <header class="home__header">
      <div class="home__copy">
        <h1>北京 · 海淀</h1>
        <p>找舞室、课程、老师</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <Bell :size="20" :stroke-width="2" />
      </button>
    </header>

    <main class="home__content">
      <button class="search-pill" type="button" @click="router.push('/search')">
        <Search :size="18" :stroke-width="2" />
        <span>搜索舞室、课程、老师、舞种</span>
      </button>

      <section
        class="hero"
        :style="{ backgroundImage: `url(${heroImage})` }"
        @click="router.push('/search')"
      >
        <div class="hero__overlay">
          <strong class="hero__title">FIND<br />YOUR<br />STUDIO</strong>
          <p class="hero__sub">附近零基础友好课程</p>
        </div>
      </section>

      <section class="quick-block" aria-label="快捷搜索入口">
        <header class="quick-block__head">
          <h2>快捷入口</h2>
          <p>前 4 个入口会进入对应搜索结果页</p>
        </header>
        <div class="quick">
          <button
            v-for="entry in quickEntries"
            :key="entry.label"
            class="quick__item"
            type="button"
            @click="openQuickEntry(entry)"
          >
            <component :is="entry.icon" :size="20" :stroke-width="2" />
            <strong>{{ entry.label }}</strong>
            <span>{{ entry.meta }}</span>
          </button>
        </div>
      </section>

      <section class="recommend">
        <header class="recommend__head">
          <h2>为你推荐</h2>
          <div class="recommend__tabs" role="tablist" aria-label="推荐类型">
            <button
              v-for="tab in recommendTabs"
              :key="tab.id"
              type="button"
              class="recommend__tab"
              :class="{ 'recommend__tab--active': activeRecommendType === tab.id }"
              role="tab"
              :aria-selected="activeRecommendType === tab.id"
              @click="activeRecommendType = tab.id"
            >
              {{ tab.label }}
            </button>
          </div>
          <button class="recommend__more" type="button" @click="router.push(recommendMoreTarget)">全部</button>
        </header>
        <div v-if="activeRecommends.length" class="recommend__grid">
          <article
            v-for="card in activeRecommends"
            :key="card.id"
            class="rec-card"
            @click="router.push(card.to)"
          >
            <div class="rec-card__cover" :style="{ backgroundImage: `url(${card.imageUrl})` }" aria-hidden="true" />
            <div class="rec-card__row">
              <span class="rec-card__title">{{ card.title }}</span>
              <button class="rec-card__pill" type="button" @click.stop="router.push(card.to)">
                {{ card.action }}
              </button>
            </div>
            <p class="rec-card__meta">{{ card.meta }}</p>
          </article>
        </div>
        <p v-else class="recommend__empty">暂无可推荐内容</p>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.home {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-hairline-soft: #e5e5e5;

  min-height: 100%;
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.home__header {
  height: 68px;
  padding: 14px 18px;
  background: var(--nike-canvas);
  border-bottom: 1px solid var(--nike-hairline-soft);
  display: flex;
  align-items: center;
  gap: 12px;
}

.home__copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;

  h1,
  p {
    margin: 0;
  }

  h1 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 800;
  }

  p {
    color: var(--nike-mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.icon-button {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: grid;
  place-items: center;
  flex: none;
  cursor: pointer;
}

.home__content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.search-pill {
  width: 100%;
  height: 44px;
  border: 0;
  border-radius: 24px;
  padding: 0 16px;
  background: var(--nike-soft-cloud);
  color: var(--nike-mute);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-align: left;

  span {
    min-width: 0;
    flex: 1;
    color: var(--nike-mute);
    font-size: 14px;
    line-height: 1.25;
    font-weight: 500;
  }
}

.hero {
  height: 184px;
  border-radius: 0;
  background-color: var(--nike-ink);
  background-size: cover;
  background-position: center;
  overflow: hidden;
  cursor: pointer;

  &__overlay {
    height: 100%;
    padding: 18px;
    background: rgba(17, 17, 17, 0.2);
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    gap: 10px;
  }

  &__title {
    color: #fff;
    font-size: 34px;
    font-weight: 900;
    line-height: 1.25;
    letter-spacing: 0;
  }

  &__sub {
    margin: 0;
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.25;
  }
}

.quick-block {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__head {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__head h2,
  &__head p {
    margin: 0;
  }

  &__head h2 {
    font-size: 20px;
    font-weight: 800;
    line-height: 1.25;
  }

  &__head p {
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 600;
    line-height: 1.25;
  }
}

.quick {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;

  &__item {
    min-width: 0;
    min-height: 96px;
    border: 0;
    border-radius: 16px;
    padding: 12px 10px;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    justify-content: flex-start;
    gap: 6px;
    cursor: pointer;
    text-align: left;

    strong,
    span {
      display: block;
    }

    strong {
      font-size: 12px;
      font-weight: 800;
      line-height: 1.25;
    }

    span {
      color: var(--nike-mute);
      font-size: 10px;
      font-weight: 700;
      line-height: 1.3;
    }
  }
}

.recommend {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__head {
    display: grid;
    grid-template-columns: 1fr auto auto;
    align-items: center;
    gap: 8px;
  }

  &__head h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    line-height: 1.25;
  }

  &__tabs {
    display: inline-flex;
    align-items: center;
    padding: 3px;
    border-radius: 999px;
    background: var(--nike-soft-cloud);
  }

  &__tab {
    min-width: 44px;
    height: 30px;
    border: 0;
    border-radius: 999px;
    background: transparent;
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 800;
    cursor: pointer;

    &--active {
      background: var(--nike-ink);
      color: #fff;
    }
  }

  &__more {
    border: 0;
    background: transparent;
    color: var(--nike-mute);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  &__empty {
    min-height: 80px;
    margin: 0;
    padding: 20px;
    border-radius: 12px;
    background: var(--nike-soft-cloud);
    color: var(--nike-mute);
    font-size: 13px;
    font-weight: 700;
    line-height: 1.25;
  }
}

.rec-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;

  &__cover {
    height: 124px;
    border-radius: 14px;
    background: var(--nike-soft-cloud);
    background-size: cover;
    background-position: center;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__title {
    flex: 1;
    min-width: 0;
    font-size: 14px;
    font-weight: 800;
    line-height: 1.25;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__pill {
    flex: none;
    height: 36px;
    padding: 8px 12px;
    border: 1px solid var(--nike-hairline-soft);
    border-radius: 999px;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    font-size: 12px;
    font-weight: 800;
    line-height: 1.25;
    cursor: pointer;
  }

  &__meta {
    margin: 0;
    color: var(--nike-mute);
    font-size: 12px;
    font-weight: 500;
    line-height: 1.35;
  }
}
</style>
