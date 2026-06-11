<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ChevronLeft, ChevronRight, Play, X } from 'lucide-vue-next';
import type { MediaAsset } from '@/api/community';
import { captureVideoPoster } from '@/utils/videoPoster';

const props = withDefaults(
  defineProps<{
    assets: MediaAsset[];
    rounded?: boolean;
  }>(),
  {
    rounded: true
  }
);

const activeVideoUrl = ref('');
const activeImageIndex = ref<number | null>(null);
const activeImageUrl = computed(() =>
  activeImageIndex.value == null ? '' : imageAssets.value[activeImageIndex.value]?.url ?? ''
);
const videoPoster = ref('');
const imageAssets = computed(() => props.assets.filter((item) => item.mediaType === 'image'));
const videoAsset = computed(() => props.assets.find((item) => item.mediaType === 'video') ?? null);
const imageCount = computed(() => imageAssets.value.length);
const layoutClass = computed(() => {
  if (imageCount.value <= 1) return 'community-media--single';
  if (imageCount.value === 2 || imageCount.value === 4) return 'community-media--double';
  return 'community-media--triple';
});

const openImagePreview = (startPosition: number) => {
  activeImageIndex.value = startPosition;
};

const openVideo = () => {
  if (!videoAsset.value?.url) return;
  activeVideoUrl.value = videoAsset.value.url;
};

const closeVideo = () => {
  activeVideoUrl.value = '';
};

const closeImagePreview = () => {
  activeImageIndex.value = null;
};

const showPrevImage = computed(() => activeImageIndex.value != null && activeImageIndex.value > 0);
const showNextImage = computed(() => activeImageIndex.value != null && activeImageIndex.value < imageAssets.value.length - 1);

const goPrevImage = () => {
  if (!showPrevImage.value || activeImageIndex.value == null) return;
  activeImageIndex.value -= 1;
};

const goNextImage = () => {
  if (!showNextImage.value || activeImageIndex.value == null) return;
  activeImageIndex.value += 1;
};

watch(
  videoAsset,
  async (next) => {
    videoPoster.value = next?.url ? (await captureVideoPoster(next.url)) ?? '' : '';
  },
  { immediate: true }
);
</script>

<template>
  <div v-if="videoAsset" class="community-video">
    <button class="community-video__trigger" type="button" aria-label="播放视频" @click.stop="openVideo">
      <video :src="videoAsset.url" :poster="videoPoster || undefined" muted playsinline preload="auto" />
      <span class="community-video__play">
        <Play :size="20" :stroke-width="2.2" fill="currentColor" />
      </span>
    </button>
  </div>

  <div v-else-if="imageCount" class="community-media" :class="layoutClass">
    <button
      v-for="(image, index) in imageAssets"
      :key="image.id"
      class="community-media__item"
      :class="{ 'community-media__item--single': imageCount === 1, 'community-media__item--rounded': rounded }"
      type="button"
      :aria-label="`查看第 ${index + 1} 张图片`"
      @click.stop="openImagePreview(index)"
    >
      <img :src="image.url" :alt="image.originalFilename || '动态图片'" />
    </button>
  </div>

  <div v-if="activeVideoUrl" class="video-layer" role="dialog" aria-modal="true" aria-label="播放动态视频" @click.stop>
    <button class="video-layer__backdrop" type="button" aria-label="关闭视频" @click.stop="closeVideo" />
    <section class="video-layer__sheet" @click.stop>
      <button class="video-layer__close" type="button" aria-label="关闭视频" @click.stop="closeVideo">关闭</button>
      <video :src="activeVideoUrl" controls autoplay playsinline preload="metadata" />
    </section>
  </div>

  <div v-if="activeImageIndex !== null" class="image-layer" role="dialog" aria-modal="true" aria-label="查看大图" @click.stop>
    <button class="image-layer__backdrop" type="button" aria-label="关闭大图" @click.stop="closeImagePreview" />
    <section class="image-layer__sheet" @click.stop>
      <button class="image-layer__close" type="button" aria-label="关闭大图" @click.stop="closeImagePreview">
        <X :size="18" :stroke-width="2.2" />
      </button>
      <button
        class="image-layer__nav"
        :class="{ 'image-layer__nav--hidden': !showPrevImage }"
        type="button"
        aria-label="上一张"
        :disabled="!showPrevImage"
        @click.stop="goPrevImage"
      >
        <ChevronLeft :size="22" :stroke-width="2.4" />
      </button>
      <button class="image-layer__image" type="button" aria-label="关闭大图" @click.stop="closeImagePreview">
        <img :src="activeImageUrl" alt="大图预览" />
      </button>
      <button
        class="image-layer__nav"
        :class="{ 'image-layer__nav--hidden': !showNextImage }"
        type="button"
        aria-label="下一张"
        :disabled="!showNextImage"
        @click.stop="goNextImage"
      >
        <ChevronRight :size="22" :stroke-width="2.4" />
      </button>
    </section>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.community-media {
  display: grid;
  gap: 4px;
  align-self: flex-start;
  max-width: min(100%, 320px);

  &--single {
    grid-template-columns: minmax(0, 1fr);
    max-width: min(72vw, 260px);
  }

  &--double {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  &--triple {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  &__item {
    border: 0;
    padding: 0;
    background: transparent;
    cursor: pointer;
    overflow: hidden;
    aspect-ratio: 1;

    &--single {
      aspect-ratio: 4 / 5;
    }

    &--rounded {
      border-radius: 14px;
    }
  }

  img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.community-video {
  width: 100%;

  &__trigger {
    position: relative;
    width: 100%;
    border: 0;
    padding: 0;
    overflow: hidden;
    border-radius: 14px;
    background: $pen-ink;
    aspect-ratio: 16 / 9;
    cursor: pointer;
  }

  video {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    opacity: 0.92;
  }

  &__play {
    position: absolute;
    inset: 0;
    margin: auto;
    width: 54px;
    height: 54px;
    border-radius: 999px;
    background: rgba(17, 17, 17, 0.68);
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    pointer-events: none;
  }
}

.image-layer,
.video-layer {
  position: fixed;
  inset: 0;
  z-index: 80;

  &__backdrop {
    position: absolute;
    inset: 0;
    border: 0;
    background: rgba(0, 0, 0, 0.8);
  }

  &__sheet {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 12px;
    width: min(100vw, 480px);
    height: 100%;
    margin: 0 auto;
    padding: 18px;
    box-sizing: border-box;
  }

  &__close {
    align-self: flex-end;
    height: 34px;
    padding: 0 14px;
    border: 0;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.12);
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }

  video {
    display: block;
    width: 100%;
    max-height: calc(100vh - 120px);
    border-radius: 16px;
    background: #000;
  }
}

.image-layer {
  &__sheet {
    position: relative;
    z-index: 1;
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    align-items: center;
    gap: 12px;
    width: min(100vw, 480px);
    height: 100%;
    margin: 0 auto;
    padding: 18px;
    box-sizing: border-box;
  }

  &__close {
    position: absolute;
    top: 18px;
    right: 18px;
    z-index: 2;
    width: 38px;
    height: 38px;
    border: 0;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.14);
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__image {
    border: 0;
    padding: 0;
    background: transparent;
    cursor: pointer;

    img {
      display: block;
      width: 100%;
      max-height: calc(100vh - 120px);
      border-radius: 16px;
      object-fit: contain;
      background: rgba(255, 255, 255, 0.04);
    }
  }

  &__nav {
    width: 42px;
    height: 42px;
    border: 0;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.14);
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    cursor: pointer;

    &:disabled {
      opacity: 0.34;
      cursor: default;
    }

    &--hidden {
      opacity: 0.34;
    }
  }
}
</style>
