<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, Image, Plus, Trash2, Video } from 'lucide-vue-next';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';

interface UploadedImage {
  id: number;
  name: string;
}

const route = useRoute();
const router = useRouter();
const content = ref('');
const uploadedImages = ref<UploadedImage[]>([]);
const hasVideo = ref(false);
const nextImageId = ref(1);

const isWorkMode = computed(() => route.name === 'publish-work' || route.path.includes('/works/upload'));
const imageCountLabel = computed(() => `${uploadedImages.value.length}/9`);

const pageCopy = computed(() =>
  isWorkMode.value
    ? {
        title: '上传作品',
        meta: '保存到阶段作品',
        action: '保存作品',
        placeholder: '记录这次作品的练习目标、完成度或想复盘的动作。',
        success: '作品已保存',
        draft: '退出后进入作品草稿，可继续修改或删除。'
      }
    : {
        title: '编辑动态',
        meta: '草稿已自动保存',
        action: '发布',
        placeholder: '分享课堂记录、练舞片段或 Workshop 体验',
        success: '已发布',
        draft: '退出后进入草稿箱，可继续修改或删除。'
      }
);

const rows = computed(() =>
  isWorkMode.value
    ? [
        { label: '作品类型', trailing: '阶段作品' },
        { label: '关联舞种', trailing: 'Locking' },
        { label: '练习日期', trailing: '今天' },
        { label: '可见范围', trailing: '仅自己' }
      ]
    : [
        { label: '# 添加话题', trailing: '#Locking入门' },
        { label: '关联舞室 / 课程 / 老师', trailing: 'Urban Flow' },
        { label: '所在位置', trailing: '五道口' },
        { label: '谁可以看', trailing: '公开' }
      ]
);

const addImage = () => {
  if (uploadedImages.value.length >= 9) {
    showFailToast('最多添加 9 张图片');
    return;
  }
  const id = nextImageId.value++;
  uploadedImages.value.push({ id, name: `图片 ${String(id).padStart(2, '0')}` });
  showSuccessToast('图片已添加');
};

const addVideo = () => {
  hasVideo.value = true;
  showSuccessToast('视频已添加');
};

const removeImage = (id: number) => {
  uploadedImages.value = uploadedImages.value.filter((image) => image.id !== id);
};

const onPublish = () => {
  showSuccessToast(pageCopy.value.success);
  router.replace(isWorkMode.value ? '/me/works' : '/community');
};
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <div class="topbar__copy">
        <h1 class="topbar__title">{{ pageCopy.title }}</h1>
        <p class="topbar__meta">{{ pageCopy.meta }}</p>
      </div>
      <button class="topbar__pub" type="button" @click="onPublish">{{ pageCopy.action }}</button>
    </header>

    <section class="pen-scroll">
      <textarea
        v-model="content"
        class="editor"
        rows="5"
        :placeholder="pageCopy.placeholder"
      />

      <section class="media-section" aria-label="添加媒体">
        <div class="media-section__head">
          <h2>{{ isWorkMode ? '作品媒体' : '媒体' }}</h2>
          <span>{{ imageCountLabel }}</span>
        </div>

        <div class="media-actions">
          <button class="media-action" type="button" @click="addImage">
            <Image :size="22" :stroke-width="2" />
            <span>添加图片</span>
          </button>
          <button class="media-action" type="button" @click="addVideo">
            <Video :size="22" :stroke-width="2" />
            <span>添加视频</span>
          </button>
        </div>

        <div v-if="hasVideo" class="video-strip">
          <Video :size="18" :stroke-width="2" />
          <span>已添加 1 个视频</span>
        </div>

        <div v-if="uploadedImages.length" class="image-grid">
          <article v-for="image in uploadedImages" :key="image.id" class="image-tile">
            <Image :size="22" :stroke-width="2" />
            <span>{{ image.name }}</span>
            <button
              class="image-tile__remove"
              type="button"
              :aria-label="`删除${image.name}`"
              @click="removeImage(image.id)"
            >
              <Trash2 :size="14" :stroke-width="2" />
            </button>
          </article>
          <button v-if="uploadedImages.length < 9" class="image-tile image-tile--add" type="button" @click="addImage">
            <Plus :size="22" :stroke-width="2" />
            <span>继续添加</span>
          </button>
        </div>
      </section>

      <div class="rows">
        <PenSettingRow
          v-for="r in rows"
          :key="r.label"
          :label="r.label"
          :trailing="r.trailing"
        />
      </div>

      <p class="draft-tip">{{ pageCopy.draft }}</p>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__copy {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__icon {
    width: 40px;
    height: 40px;
    flex: none;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__pub {
    flex: none;
    height: 36px;
    padding: 8px 16px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.editor {
  width: 100%;
  min-height: 132px;
  border: 0;
  background: transparent;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.45;
  resize: none;
  outline: none;

  &::placeholder {
    color: $pen-mute;
    font-weight: 600;
  }
}

.media-section {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;

    h2 {
      margin: 0;
      color: $pen-ink;
      font-size: 17px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }
}

.media-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.media-action {
  height: 72px;
  border: 1px solid $pen-hairline;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 900;
  line-height: $pen-lh;
  cursor: pointer;
}

.video-strip {
  min-height: 40px;
  padding: 0 12px;
  border-radius: 12px;
  background: $pen-soft;
  color: $pen-ink;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 800;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.image-tile {
  position: relative;
  aspect-ratio: 1;
  border: 1px solid $pen-ink;
  border-radius: 14px;
  background: $pen-ink;
  color: $pen-on-primary;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 900;
  line-height: $pen-lh;
  overflow: hidden;

  &--add {
    border-color: $pen-hairline;
    background: $pen-canvas;
    color: $pen-mute;
    cursor: pointer;
  }

  &__remove {
    position: absolute;
    top: 6px;
    right: 6px;
    width: 24px;
    height: 24px;
    border: 0;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }
}

.rows {
  display: flex;
  flex-direction: column;
}

.draft-tip {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  background: #f1f8f3;
  color: #007d48;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
}
</style>
