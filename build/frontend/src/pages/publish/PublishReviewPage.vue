<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast, showToast } from 'vant';
import { Image, Music, Plus, ShieldCheck, Trash2, Video } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  createReview,
  REVIEW_DIMENSIONS,
  type ReviewCreateBody,
  type ReviewMediaDto,
  type ReviewTargetType
} from '@/api/review';

const route = useRoute();
const router = useRouter();

const targetTypes: Array<{ type: ReviewTargetType; label: string }> = [
  { type: 'studio', label: '舞室' },
  { type: 'coach', label: '老师' },
  { type: 'course', label: '课程' }
];

const targetNames: Record<ReviewTargetType, string> = {
  studio: 'Urban Flow 舞室',
  coach: 'Mia 老师',
  course: 'K-pop 入门课'
};

const draftKey = 'bitdance_review_draft';
const activeType = ref<ReviewTargetType>('studio');
const content = ref('');
const anonymous = ref(false);
const allowReply = ref(true);
const submitting = ref(false);
const imageInput = ref<HTMLInputElement | null>(null);
const videoInput = ref<HTMLInputElement | null>(null);
const mixedInput = ref<HTMLInputElement | null>(null);
const mediaAssets = ref<ReviewMediaDto[]>([]);
const scores = reactive<Record<string, number>>({});

const sourceType = computed(() => {
  const raw = route.query.sourceType;
  return raw === 'trial' || raw === 'order' || raw === 'checkin' ? raw : 'trial';
});
const sourceRefId = computed(() => Number(route.query.sourceRefId) || undefined);
const targetId = computed(() => Number(route.query.targetId) || 1);
const targetName = computed(
  () => String(route.query.targetName || targetNames[activeType.value])
);
const currentDimensions = computed(() => REVIEW_DIMENSIONS[activeType.value]);
const averageScore = computed(() => {
  const values = currentDimensions.value
    .map((item) => scores[item.key])
    .filter((score): score is number => score !== undefined);
  if (!values.length) return 0;
  const total = values.reduce((sum, score) => sum + score, 0);
  return Number((total / values.length).toFixed(1));
});

const resetScores = () => {
  Object.keys(scores).forEach((key) => delete scores[key]);
};

const setActiveType = (type: ReviewTargetType) => {
  activeType.value = type;
};

const setScore = (key: string, score: number) => {
  scores[key] = score;
};

const fileToMedia = (file: File): Promise<ReviewMediaDto | null> =>
  new Promise((resolve) => {
    const kind = file.type.startsWith('video/') ? 'video' : file.type.startsWith('image/') ? 'image' : null;
    if (!kind) {
      resolve(null);
      return;
    }
    const reader = new FileReader();
    reader.onload = () =>
      resolve({
        type: kind,
        url: String(reader.result),
        name: file.name,
        size: file.size
      });
    reader.onerror = () => resolve(null);
    reader.readAsDataURL(file);
  });

const pickMedia = (kind: 'image' | 'video' | 'mixed') => {
  if (kind === 'image') imageInput.value?.click();
  else if (kind === 'video') videoInput.value?.click();
  else mixedInput.value?.click();
};

const onMediaSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []);
  input.value = '';
  if (!files.length) return;

  const slots = Math.max(0, 6 - mediaAssets.value.length);
  if (!slots) {
    showFailToast('最多添加 6 个媒体');
    return;
  }

  const picked = await Promise.all(files.slice(0, slots).map(fileToMedia));
  const valid = picked.filter((item): item is ReviewMediaDto => Boolean(item));
  if (!valid.length) {
    showFailToast('请选择图片或视频文件');
    return;
  }
  mediaAssets.value = [...mediaAssets.value, ...valid];
  if (files.length > slots) showToast('已达到 6 个媒体上限');
};

const removeMedia = (index: number) => {
  mediaAssets.value = mediaAssets.value.filter((_, itemIndex) => itemIndex !== index);
};

const saveDraft = () => {
  localStorage.setItem(
    draftKey,
    JSON.stringify({
      targetType: activeType.value,
      targetId: targetId.value,
      content: content.value,
      anonymous: anonymous.value,
      allowReply: allowReply.value,
      mediaAssets: mediaAssets.value,
      scores: { ...scores }
    })
  );
  showSuccessToast('草稿已保存');
};

const submitReview = async () => {
  if (currentDimensions.value.some((item) => scores[item.key] === undefined)) {
    showFailToast('请完成所有维度评分');
    return;
  }
  if (!content.value.trim()) {
    showFailToast('请填写评价内容');
    return;
  }
  submitting.value = true;
  try {
    const body: ReviewCreateBody = {
      targetType: activeType.value,
      targetId: targetId.value,
      overallScore: averageScore.value,
      contentText: content.value.trim(),
      dimensions: currentDimensions.value.map((item) => ({
        code: item.key,
        name: item.label,
        score: scores[item.key] as number
      })),
      mediaAssets: mediaAssets.value,
      sourceType: sourceType.value,
      sourceRefId: sourceRefId.value
    };
    await createReview(body);
    localStorage.removeItem(draftKey);
    showSuccessToast('评价已提交');
    router.back();
  } finally {
    submitting.value = false;
  }
};

watch(activeType, resetScores, { immediate: true });
</script>

<template>
  <main class="review-page">
    <PenTopBar title="写评价" @share="showToast('评价草稿已准备')" />

    <section class="review-scroll">
      <section class="target-card">
        <div class="target-card__icon" aria-hidden="true">
          <Music :size="20" :stroke-width="2" />
        </div>
        <div class="target-card__copy">
          <strong>{{ targetName }}</strong>
          <span>已完成试听 · 权重 1.5x</span>
        </div>
      </section>

      <nav class="segment" aria-label="评价对象">
        <button
          v-for="item in targetTypes"
          :key="item.type"
          class="segment__btn"
          :class="{ 'segment__btn--active': activeType === item.type }"
          type="button"
          @click="setActiveType(item.type)"
        >
          {{ item.label }}
        </button>
      </nav>

      <section class="block">
        <h2 class="block__title">结构化评分</h2>
        <div class="dimension-list">
          <div v-for="item in currentDimensions" :key="item.key" class="dimension">
            <span class="dimension__label">{{ item.label }}</span>
            <div class="dimension__scores" :aria-label="`${item.label}评分`">
              <button
                v-for="score in 5"
                :key="score"
                class="score-dot"
                :class="{ 'score-dot--active': scores[item.key] !== undefined && score <= scores[item.key] }"
                type="button"
                @click="setScore(item.key, score)"
              >
                {{ score }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="verify-card">
        <ShieldCheck :size="17" :stroke-width="2" />
        <span>{{ averageScore ? `综合 ${averageScore}` : '待评分' }}，将按已验证试听评价计入聚合</span>
      </section>

      <label class="content-box">
        <span>评价内容</span>
        <textarea
          v-model="content"
          rows="3"
          maxlength="5000"
          placeholder="说说真实体验：交通、环境、老师引导、是否适合零基础……"
        />
      </label>

      <section class="block">
        <h2 class="block__title">照片 / 短视频</h2>
        <input ref="imageInput" class="media-input" type="file" accept="image/*" multiple @change="onMediaSelected" />
        <input ref="videoInput" class="media-input" type="file" accept="video/*" multiple @change="onMediaSelected" />
        <input ref="mixedInput" class="media-input" type="file" accept="image/*,video/*" multiple @change="onMediaSelected" />
        <div class="media-grid">
          <article v-for="(item, index) in mediaAssets" :key="`${item.name}-${index}`" class="media-preview">
            <img v-if="item.type === 'image'" :src="item.url" :alt="item.name" />
            <video v-else :src="item.url" muted playsinline preload="metadata" />
            <button type="button" aria-label="删除媒体" @click="removeMedia(index)">
              <Trash2 :size="14" :stroke-width="2" />
            </button>
            <span>{{ item.type === 'image' ? '照片' : '视频' }}</span>
          </article>
          <button class="media-cell" type="button" @click="pickMedia('image')">
            <Image :size="19" :stroke-width="2" />
            <span>照片</span>
          </button>
          <button class="media-cell" type="button" @click="pickMedia('video')">
            <Video :size="19" :stroke-width="2" />
            <span>视频</span>
          </button>
          <button class="media-cell" type="button" @click="pickMedia('mixed')">
            <Plus :size="19" :stroke-width="2" />
            <span>添加</span>
          </button>
        </div>
      </section>

      <section class="option-list">
        <label class="option-row">
          <span>
            <strong>匿名展示</strong>
            <em>隐藏昵称，仅展示已验证标签</em>
          </span>
          <input v-model="anonymous" type="checkbox" />
        </label>
        <label class="option-row">
          <span>
            <strong>允许商家回复</strong>
            <em>收到回复后通知我</em>
          </span>
          <input v-model="allowReply" type="checkbox" />
        </label>
      </section>
    </section>

    <footer class="submit-bar">
      <button class="submit-bar__draft" type="button" @click="saveDraft">存草稿</button>
      <button
        class="submit-bar__submit"
        type="button"
        :disabled="submitting"
        @click="submitReview"
      >
        {{ submitting ? '提交中' : '提交评价' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.review-page {
  @include pen-page;
  min-height: 100%;
  padding-bottom: calc(74px + var(--app-tabbar-offset, 0px));
}

.review-scroll {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 18px;
}

.target-card {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 66px;
  padding: 10px;
  background: $pen-soft;

  &__icon {
    flex: none;
    display: grid;
    width: 44px;
    height: 44px;
    border-radius: 10px;
    background: $pen-hairline;
    color: $pen-ink;
    place-items: center;
  }

  &__copy {
    min-width: 0;
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 3px;
  }

  strong {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 11px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.segment {
  display: flex;
  gap: 8px;
  height: 38px;

  &__btn {
    flex: 1;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;

    &--active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}

.block {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__title {
    margin: 0;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }
}

.dimension-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.dimension {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 30px;

  &__label {
    flex: none;
    width: 82px;
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__scores {
    display: flex;
    flex: 1;
    gap: 5px;
  }
}

.score-dot {
  display: grid;
  width: 24px;
  height: 24px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
  place-items: center;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.verify-card {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  padding: 0 12px;
  border-radius: 8px;
  background: #f1f8f3;
  color: $pen-success;

  span {
    min-width: 0;
    flex: 1;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.content-box {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 86px;
  padding: 10px;
  border-radius: 8px;
  background: $pen-soft;

  span {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  textarea {
    width: 100%;
    border: 0;
    outline: 0;
    background: transparent;
    color: $pen-ink;
    font-family: $pen-font;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.4;
    resize: none;
    box-sizing: border-box;

    &::placeholder {
      color: $pen-mute;
    }
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.media-input {
  display: none;
}

.media-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  height: 60px;
  border: 1px solid $pen-hairline;
  border-radius: 8px;
  background: $pen-soft;
  color: $pen-mute;
  cursor: pointer;

  span {
    font-size: 11px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.media-preview {
  position: relative;
  min-width: 0;
  height: 60px;
  overflow: hidden;
  border: 1px solid $pen-hairline;
  border-radius: 8px;
  background: $pen-ink;

  img,
  video {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  button {
    position: absolute;
    top: 4px;
    right: 4px;
    display: grid;
    width: 22px;
    height: 22px;
    padding: 0;
    border: 0;
    border-radius: 999px;
    background: rgba(17, 17, 17, 0.78);
    color: $pen-on-primary;
    cursor: pointer;
    place-items: center;
  }

  span {
    position: absolute;
    right: 6px;
    bottom: 4px;
    left: 6px;
    overflow: hidden;
    color: $pen-on-primary;
    font-size: 10px;
    font-weight: 800;
    line-height: $pen-lh;
    text-overflow: ellipsis;
    text-shadow: 0 1px 5px rgba(17, 17, 17, 0.85);
    white-space: nowrap;
  }
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  border-bottom: 1px solid $pen-hairline;

  span {
    min-width: 0;
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 2px;
  }

  strong {
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    color: $pen-mute;
    font-size: 10px;
    font-style: normal;
    font-weight: 600;
    line-height: $pen-lh;
  }

  input {
    position: relative;
    width: 40px;
    height: 22px;
    flex: none;
    margin: 0;
    border: 0;
    border-radius: 999px;
    appearance: none;
    background: #dadada;
    cursor: pointer;

    &::after {
      position: absolute;
      top: 3px;
      left: 3px;
      width: 16px;
      height: 16px;
      border-radius: 999px;
      background: $pen-canvas;
      content: '';
      transition: transform 0.18s ease;
    }

    &:checked {
      background: $pen-ink;
    }

    &:checked::after {
      transform: translateX(18px);
    }
  }
}

.submit-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 90;
  display: flex;
  width: 100%;
  max-width: 480px;
  height: 74px;
  margin: 0 auto;
  padding: 12px 18px;
  border-top: 1px solid $pen-hairline;
  background: $pen-canvas;
  gap: 10px;
  box-sizing: border-box;

  button {
    height: 48px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 800;
    cursor: pointer;
  }

  &__draft {
    width: 104px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__submit {
    flex: 1;
    background: $pen-ink;
    color: $pen-on-primary;

    &:disabled {
      opacity: 0.65;
      cursor: not-allowed;
    }
  }
}
</style>
