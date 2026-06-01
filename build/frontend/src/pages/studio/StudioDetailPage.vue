<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Music, Heart, Navigation, Phone } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { toggleFavorite } from '@/api/favorite';

const route = useRoute();
const router = useRouter();
const studioId = Number(route.params.id) || 1;
const detail = ref<StudioDetail | null>(null);
const favored = computed(() => detail.value?.favored ?? false);

const toggleStudioFavorite = async () => {
  const result = await toggleFavorite('studio', studioId);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

const actions = [
  { icon: Heart, label: '收藏', handler: toggleStudioFavorite },
  { icon: Navigation, label: '导航', handler: () => window.open(`https://uri.amap.com/search?keyword=${encodeURIComponent(detail.value?.address ?? '')}`) },
  { icon: Phone, label: '联系', handler: () => { if (detail.value?.contactPhone) window.location.href = `tel:${detail.value.contactPhone}`; } }
];

const tags = ['零基础友好', '地铁近', '课程多'];
const tabs = ['概况', '课程', '老师', '评价', '课表'];
const activeTab = ref('概况');

const recommendCourse = {
  id: 'kpop-intro',
  title: 'K-pop 入门班',
  meta: '今晚 19:30 · 小鹿老师',
  tag: '零基础',
  price: '¥79 试听'
};

const onBook = () => router.push(`/studio/${studioId}/trial`);
onMounted(async () => {
  detail.value = await fetchStudioDetail(studioId);
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="舞室详情" @share="showToast('舞室链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true">
          <span v-for="i in 6" :key="i" />
        </div>
        <Music class="hero__icon" :size="42" :stroke-width="2" />
        <strong class="hero__title">URBAN<br />FLOW</strong>
        <p class="hero__meta">{{ detail?.distanceKm ?? '-' }}km · {{ detail?.transportInfo || '交通信息待完善' }}</p>
      </section>

      <section class="body">
        <h2 class="body__title">{{ detail?.name || '舞室详情' }}</h2>
        <p class="body__sub">{{ detail?.address || '地址待完善' }}</p>

        <div class="action-row">
          <button
            v-for="action in actions"
            :key="action.label"
            type="button"
            class="action-pill"
            @click="action.handler"
          >
            <component :is="action.icon" :size="18" :stroke-width="2" />
            <span>{{ action.label }}</span>
          </button>
        </div>

        <div class="chip-row">
          <span v-for="tag in tags" :key="tag" class="chip chip--inactive">{{ tag }}</span>
        </div>

        <div class="chip-row">
          <button
            v-for="tab in tabs"
            :key="tab"
            type="button"
            class="chip"
            :class="activeTab === tab ? 'chip--active' : 'chip--inactive'"
            @click="activeTab = tab"
          >
            {{ tab }}
          </button>
        </div>

        <header class="section-head">
          <h3>本周推荐课程</h3>
          <button type="button" class="section-head__more" @click="router.push(`/studio/${studioId}/schedule`)">
            查看课表
          </button>
        </header>

        <article class="course" @click="router.push(`/course/${recommendCourse.id}`)">
          <div class="course__cover" aria-hidden="true">
            <Music :size="28" :stroke-width="2" />
          </div>
          <div class="course__body">
            <strong class="course__title">{{ recommendCourse.title }}</strong>
            <p class="course__meta">{{ recommendCourse.meta }}</p>
            <span class="tag">{{ recommendCourse.tag }}</span>
            <span class="course__price">{{ recommendCourse.price }}</span>
          </div>
        </article>
      </section>
    </section>

    <PenActionBar
      :soft-label="favored ? '已收藏' : '收藏'"
      dark-label="预约试听"
      @soft="toggleStudioFavorite"
      @dark="onBook"
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
}

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 260px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
    height: 30px;
    margin-bottom: auto;

    span {
      height: 100%;
      background: $pen-charcoal;
    }
  }

  &__icon {
    flex-shrink: 0;
    color: $pen-on-primary;
  }

  &__title {
    margin: 0;
    font-size: 32px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 18px 20px;

  &__title {
    @include pen-h2;
  }

  &__sub {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 600;
    line-height: $pen-lh;
  }
}

.action-row {
  display: flex;
  gap: 8px;
}

.action-pill {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 44px;
  border: 0;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 14px;
  font-weight: 700;
  line-height: $pen-lh;
  cursor: pointer;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;

  h3 {
    @include pen-h3-section;
    flex: 1;
  }

  &__more {
    border: 0;
    background: transparent;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
  }
}

.course {
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

  &__price {
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
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
