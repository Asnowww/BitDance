<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ImageIcon, Play, Trash2 } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { deleteGrowthWork, fetchGrowthWorks, type GrowthWork } from '@/api/growth';

const router = useRouter();
const works = ref<GrowthWork[]>([]);
const loading = ref(false);

const loadWorks = async () => {
  loading.value = true;
  try {
    works.value = await fetchGrowthWorks();
  } finally {
    loading.value = false;
  }
};

const titleOf = (w: GrowthWork) => w.workTitle || w.title || '阶段作品';
const noteOf = (w: GrowthWork) => w.workDescription || w.description || '暂无描述';
const mediaOf = (w: GrowthWork) => w.mediaAssets?.[0];
const createdLabel = (w: GrowthWork) => {
  const raw = w.createdAt;
  const d = raw ? new Date(raw) : new Date();
  return `${d.getMonth() + 1}/${d.getDate()}`;
};
const summary = computed(() => `${works.value.length} 个作品 · ${works.value.filter((w) => mediaOf(w)?.assetType === 'video').length} 个视频`);

const removeWork = async (id: number) => {
  await deleteGrowthWork(id);
  works.value = works.value.filter((w) => w.id !== id);
  showToast('作品已删除');
};

onMounted(loadWorks);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="阶段作品" @share="showToast('作品页链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <strong class="hero__title">MY PROGRESS</strong>
        <p class="hero__meta">{{ loading ? '同步中' : summary }}</p>
      </section>

      <p v-if="!loading && !works.length" class="empty">还没有作品，上传一张练习图或视频，成长档案就会更完整。</p>

      <section class="timeline">
        <article v-for="w in works" :key="w.id" class="entry">
          <div class="entry__date">
            <strong>{{ createdLabel(w) }}</strong>
            <span>{{ mediaOf(w)?.assetType === 'video' ? '视频' : '作品' }}</span>
          </div>
          <div class="entry__card">
            <video
              v-if="mediaOf(w)?.assetType === 'video'"
              class="entry__media"
              controls
              :src="mediaOf(w)?.url"
            />
            <img
              v-else-if="mediaOf(w)?.url || w.coverUrl"
              class="entry__media"
              :src="mediaOf(w)?.url || w.coverUrl || ''"
              :alt="titleOf(w)"
            />
            <div v-else class="entry__media entry__media--placeholder" aria-hidden="true">
              <component :is="mediaOf(w)?.assetType === 'video' ? Play : ImageIcon" :size="30" :stroke-width="2" />
            </div>
            <div class="entry__copy">
              <strong class="entry__title">{{ titleOf(w) }}</strong>
              <button class="entry__delete" type="button" aria-label="删除作品" @click="removeWork(w.id)">
                <Trash2 :size="18" :stroke-width="2" />
              </button>
            </div>
            <p class="entry__note">{{ noteOf(w) }}</p>
          </div>
        </article>
      </section>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" @click="router.push('/me/works/upload')">上传新作品</button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
  &--with-bar { padding-bottom: calc(76px + env(safe-area-inset-bottom)); }
}
.pen-scroll { display: flex; flex-direction: column; gap: 16px; }
.hero { display: flex; flex-direction: column; justify-content: flex-end; gap: 8px; height: 180px; padding: 18px; background: $pen-ink; color: $pen-on-primary; box-sizing: border-box; }
.hero__title { margin: 0; font-size: 30px; font-weight: 900; line-height: $pen-lh; }
.hero__meta { margin: 0; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
.empty { margin: 0; padding: 0 18px; color: $pen-mute; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
.timeline { display: flex; flex-direction: column; gap: 16px; padding: 0 18px 20px; }
.entry { display: flex; gap: 12px; align-items: flex-start; }
.entry__date { flex: none; width: 44px; display: flex; flex-direction: column; gap: 2px; }
.entry__date strong { font-size: 14px; font-weight: 900; line-height: $pen-lh; }
.entry__date span { color: $pen-mute; font-size: 11px; font-weight: 600; line-height: $pen-lh; }
.entry__card { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.entry__media { width: 100%; height: 156px; object-fit: cover; border: 0; border-radius: 14px; background: $pen-soft; color: $pen-ink; }
.entry__media--placeholder { display: grid; place-items: center; background: $pen-ink; color: $pen-on-primary; }
.entry__copy { display: flex; align-items: center; gap: 8px; }
.entry__title { flex: 1; min-width: 0; font-size: 15px; font-weight: 900; line-height: $pen-lh; }
.entry__delete { width: 36px; height: 36px; border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink; display: grid; place-items: center; }
.entry__note { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 600; line-height: $pen-lh; }
.save-bar { position: fixed; right: 0; bottom: var(--app-tabbar-offset, 0px); left: 0; z-index: 10; width: 100%; max-width: 480px; height: 76px; margin: 0 auto; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); background: $pen-canvas; border-top: 1px solid $pen-hairline; box-sizing: border-box; }
.save-bar__btn { width: 100%; height: 48px; border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary; font-size: 15px; font-weight: 800; line-height: $pen-lh; cursor: pointer; }
</style>
