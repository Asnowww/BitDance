<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { ImageIcon, Play, Trash2 } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { deleteGrowthWork, fetchGrowthWorks, type GrowthWork } from '@/api/growth';

const router = useRouter();
const works = ref<GrowthWork[]>([]);
const loading = ref(false);

const titleOf = (work: GrowthWork) => work.workTitle || work.title || '阶段作品';
const noteOf = (work: GrowthWork) => work.workDescription || work.description || '暂无复盘说明';
const mediaOf = (work: GrowthWork) => work.mediaAssets?.[0];
const styleName = (id?: number | null) => ({
  1: 'Hiphop',
  2: 'Jazz',
  3: 'Breaking',
  4: 'Locking',
  5: 'Popping',
  6: 'K-pop'
} as Record<number, string>)[Number(id)] || '未关联舞种';

const createdLabel = (work: GrowthWork) => {
  const date = work.createdAt ? new Date(work.createdAt) : new Date();
  return Number.isNaN(date.getTime()) ? '今天' : `${date.getMonth() + 1}/${date.getDate()}`;
};

const summary = computed(() =>
  `${works.value.length} 个作品 · ${works.value.filter((work) => mediaOf(work)?.assetType === 'video').length} 个视频`
);

const loadWorks = async () => {
  loading.value = true;
  try {
    works.value = await fetchGrowthWorks();
  } finally {
    loading.value = false;
  }
};

const removeWork = async (id: number) => {
  await deleteGrowthWork(id);
  works.value = works.value.filter((work) => work.id !== id);
  showSuccessToast('作品已删除');
};

onMounted(loadWorks);
</script>

<template>
  <main class="works-page">
    <PenTopBar title="阶段作品" @share="showSuccessToast('作品页链接已复制')" />

    <section class="hero">
      <p>GROWTH WORKS</p>
      <h1>MY PROGRESS</h1>
      <span>{{ loading ? '同步中...' : summary }}</span>
    </section>

    <section class="list">
      <p v-if="loading" class="empty">正在加载作品...</p>
      <p v-else-if="!works.length" class="empty">还没有作品，上传练习图片或视频后会出现在这里。</p>

      <article v-for="work in works" :key="work.id" class="work">
        <div class="date">
          <strong>{{ createdLabel(work) }}</strong>
          <span>{{ mediaOf(work)?.assetType === 'video' ? '视频' : '作品' }}</span>
        </div>

        <div class="card">
          <video v-if="mediaOf(work)?.assetType === 'video'" class="media" controls :src="mediaOf(work)?.url" />
          <img v-else-if="mediaOf(work)?.url || work.coverUrl" class="media" :src="mediaOf(work)?.url || work.coverUrl || ''" :alt="titleOf(work)" />
          <div v-else class="media placeholder">
            <component :is="mediaOf(work)?.assetType === 'video' ? Play : ImageIcon" :size="30" />
          </div>

          <div class="copy">
            <div>
              <strong>{{ titleOf(work) }}</strong>
              <p>{{ styleName(work.danceStyleId) }} · {{ work.isPublic ? '公开展示' : '仅自己可见' }}</p>
            </div>
            <button type="button" aria-label="删除作品" @click="removeWork(work.id)">
              <Trash2 :size="18" />
            </button>
          </div>
          <p class="note">{{ noteOf(work) }}</p>
        </div>
      </article>
    </section>

    <footer class="save-bar">
      <button type="button" @click="router.push('/me/works/upload')">上传新作品</button>
    </footer>
  </main>
</template>

<style scoped lang="scss">
.works-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; padding-bottom: calc(86px + env(safe-area-inset-bottom)); }
.hero { margin: 0 18px 16px; min-height: 170px; border-radius: 8px; background: #111; color: #fff; padding: 18px; display: flex; flex-direction: column; justify-content: flex-end; gap: 6px; }
.hero p { margin: 0; color: #b8b8bb; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.hero h1 { margin: 0; font-size: 34px; line-height: 1; font-weight: 950; }
.hero span { color: #e5e5e5; font-size: 13px; font-weight: 800; }
.list { display: flex; flex-direction: column; gap: 16px; padding: 0 18px; }
.empty { margin: 0; padding: 18px 0; color: #707072; text-align: center; font-size: 13px; font-weight: 800; }
.work { display: flex; gap: 12px; align-items: flex-start; }
.date { flex: none; width: 44px; display: flex; flex-direction: column; gap: 2px; }
.date strong { font-size: 14px; font-weight: 950; }
.date span { color: #707072; font-size: 11px; font-weight: 800; }
.card { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; padding-bottom: 16px; border-bottom: 1px solid #e5e5e5; }
.media { width: 100%; height: 156px; object-fit: cover; border: 0; border-radius: 8px; background: #f5f5f5; color: #111; display: block; }
.placeholder { display: grid; place-items: center; background: #111; color: #fff; }
.copy { display: flex; align-items: center; gap: 8px; }
.copy div { flex: 1; min-width: 0; }
.copy strong { display: block; font-size: 16px; font-weight: 950; }
.copy p, .note { margin: 4px 0 0; color: #707072; font-size: 12px; font-weight: 700; line-height: 1.4; }
.copy button { flex: none; width: 36px; height: 36px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.save-bar { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); background: #fff; border-top: 1px solid #e5e5e5; box-sizing: border-box; transform: translateX(-50%); }
.save-bar button { width: 100%; height: 48px; border: 0; border-radius: 999px; background: #111; color: #fff; font-size: 15px; font-weight: 950; }
</style>
