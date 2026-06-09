<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Heart, Image as ImageIcon } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchFavorites, type FavoriteDto, type FavoriteTargetType } from '@/api/favorite';

const router = useRouter();
const active = ref<FavoriteTargetType | 'all'>('all');
const loading = ref(false);
const favorites = ref<FavoriteDto[]>([]);

const tabs: Array<{ key: FavoriteTargetType | 'all'; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'studio', label: '舞室' },
  { key: 'course', label: '课程' },
  { key: 'coach', label: '老师' },
  { key: 'workshop', label: '活动' },
  { key: 'content_post', label: '动态' }
];

const titleOf = (item: FavoriteDto) => item.card?.title || `${item.targetType} #${item.targetId}`;
const subtitleOf = (item: FavoriteDto) => item.card?.subtitle || '暂无摘要';
const actionOf = (item: FavoriteDto) => item.card?.actionText || '查看';
const pathOf = (item: FavoriteDto) => item.card?.path || '/';
const createdLabel = (item: FavoriteDto) => item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '刚刚收藏';

const filtered = computed(() =>
  active.value === 'all' ? favorites.value : favorites.value.filter((item) => item.targetType === active.value)
);

const load = async () => {
  loading.value = true;
  try {
    favorites.value = await fetchFavorites();
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="收藏管理" :show-share="false" />

    <section class="pen-scroll">
      <section class="hero">
        <div>
          <p>GROWTH FAVORITES</p>
          <h1>{{ favorites.length }} 个真实收藏</h1>
        </div>
        <Heart :size="30" />
      </section>

      <div class="chips">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="chip"
          :class="{ 'chip--on': active === tab.key }"
          type="button"
          @click="active = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <p v-if="loading" class="empty">正在加载收藏...</p>

      <article v-for="item in filtered" v-else :key="item.id" class="fav" @click="router.push(pathOf(item))">
        <img v-if="item.card?.coverUrl" class="cover cover--image" :src="item.card.coverUrl" :alt="titleOf(item)" />
        <div v-else class="cover"><ImageIcon :size="24" /></div>
        <div class="body">
          <div class="body__top">
            <strong>{{ titleOf(item) }}</strong>
            <span>{{ createdLabel(item) }}</span>
          </div>
          <p>{{ subtitleOf(item) }}</p>
          <div class="foot">
            <em>{{ item.targetType }}</em>
            <button type="button" @click.stop="router.push(pathOf(item))">{{ actionOf(item) }}</button>
          </div>
        </div>
      </article>

      <p v-if="!loading && !filtered.length" class="empty">还没有这个分类的收藏，去舞室、课程或 Workshop 详情页点收藏即可出现。</p>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }
.pen-scroll { display: flex; flex-direction: column; gap: 14px; padding: 16px 18px calc(24px + env(safe-area-inset-bottom)); }
.hero { display: flex; justify-content: space-between; align-items: center; padding: 18px; border-radius: 8px; background: $pen-ink; color: $pen-on-primary; }
.hero p { margin: 0 0 5px; color: $pen-subtle-text; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.hero h1 { margin: 0; font-size: 25px; font-weight: 900; line-height: 1.05; }
.chips { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 2px; }
.chip { flex: none; height: 38px; padding: 0 16px; border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink; font-weight: 900; }
.chip--on { background: $pen-ink; color: $pen-on-primary; }
.fav { display: flex; gap: 12px; padding: 14px 0; border-bottom: 1px solid $pen-hairline; cursor: pointer; }
.cover { flex: none; width: 82px; height: 82px; border-radius: 8px; background: $pen-soft; color: $pen-ink; display: grid; place-items: center; object-fit: cover; }
.cover--image { display: block; }
.body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 7px; }
.body__top { display: flex; justify-content: space-between; gap: 8px; }
.body strong { font-size: 16px; font-weight: 900; line-height: $pen-lh; }
.body__top span, .body p { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: 1.4; }
.foot { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.foot em { font-style: normal; color: $pen-mute; font-size: 11px; font-weight: 900; text-transform: uppercase; }
.foot button { height: 34px; padding: 0 14px; border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary; font-weight: 900; }
.empty { padding: 22px 8px; color: $pen-mute; text-align: center; font-size: 13px; font-weight: 700; }
</style>
