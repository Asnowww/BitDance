<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { MessageSquareReply, RefreshCw, ShieldCheck, Star } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  createReviewReply,
  fetchReviewReplies,
  fetchReviewReplyQueue,
  type ReviewItem,
  type ReviewReplyDto
} from '@/api/review';

const cats = ['全部', '待回复', '已回复'] as const;
const activeCat = ref<(typeof cats)[number]>('待回复');
const reviews = ref<ReviewItem[]>([]);
const replies = ref<Record<number, ReviewReplyDto[]>>({});
const drafts = reactive<Record<number, string>>({});
const loading = ref(false);
const submittingId = ref<number | null>(null);
const loadError = ref('');

const filteredReviews = computed(() => {
  if (activeCat.value === '全部') return reviews.value;
  return reviews.value.filter((item) => {
    const hasReply = Boolean(replies.value[item.id]?.length);
    return activeCat.value === '已回复' ? hasReply : !hasReply;
  });
});

const targetLabel = (review: ReviewItem) =>
  ({ studio: '舞室', coach: '老师', course: '课程', workshop: 'Workshop' }[review.targetType] ?? review.targetType);

const statusLabel = (review: ReviewItem) => {
  if (review.reviewStatus === 'folded') return '折叠中';
  if (review.isVerified) return '已验证';
  return '普通权重';
};

const dateLabel = (value: string) =>
  new Date(value).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });

const loadReplies = async (list: ReviewItem[]) => {
  const entries = await Promise.allSettled(
    list.map(async (review) => [review.id, await fetchReviewReplies(review.id)] as const)
  );
  const next: Record<number, ReviewReplyDto[]> = {};
  entries.forEach((item) => {
    if (item.status === 'fulfilled') next[item.value[0]] = item.value[1] ?? [];
  });
  replies.value = next;
};

const loadQueue = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    // M2 回复治理：读取真实后端队列和公开回复，支撑 UI 端到端验收。
    const resp = await fetchReviewReplyQueue({ page: 1, pageSize: 50 });
    reviews.value = resp.list ?? [];
    await loadReplies(reviews.value);
  } catch {
    reviews.value = [];
    replies.value = {};
    loadError.value = '评价回复队列暂不可用，请检查登录态或后端服务';
  } finally {
    loading.value = false;
  }
};

const submitReply = async (review: ReviewItem) => {
  const text = (drafts[review.id] ?? '').trim();
  if (text.length < 2) {
    showFailToast('请填写至少 2 个字的回复');
    return;
  }
  submittingId.value = review.id;
  try {
    const saved = await createReviewReply({
      reviewId: review.id,
      replyContent: text,
      isOfficial: true
    });
    replies.value = {
      ...replies.value,
      [review.id]: [...(replies.value[review.id] ?? []), saved]
    };
    drafts[review.id] = '';
    showSuccessToast('已回复');
  } finally {
    submittingId.value = null;
  }
};

onMounted(loadQueue);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="评价回复" :show-share="false" />

    <section class="pen-scroll">
      <div class="toolbar">
        <div class="chip-row">
          <button
            v-for="c in cats"
            :key="c"
            class="chip"
            :class="activeCat === c ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="activeCat = c"
          >
            {{ c }}
          </button>
        </div>
        <button class="refresh" type="button" aria-label="刷新评价回复队列" @click="loadQueue">
          <RefreshCw :size="16" :stroke-width="2" />
        </button>
      </div>

      <p v-if="loading" class="empty">评价回复队列同步中...</p>
      <p v-else-if="loadError" class="empty">{{ loadError }}</p>
      <p v-else-if="!filteredReviews.length" class="empty">暂无需要处理的评价</p>

      <article v-for="review in filteredReviews" :key="review.id" class="rev">
        <header class="rev__head">
          <span class="rev__avatar" aria-hidden="true" />
          <div class="rev__who">
            <strong class="rev__name">{{ targetLabel(review) }} #{{ review.targetId }}</strong>
            <span class="rev__stars">
              <Star
                v-for="i in 5"
                :key="i"
                :size="14"
                :stroke-width="2"
                :fill="i <= Math.round(Number(review.overallScore ?? 0)) ? '#111111' : 'none'"
                :color="i <= Math.round(Number(review.overallScore ?? 0)) ? '#111111' : '#E5E5E5'"
              />
            </span>
          </div>
          <span class="rev__badge">
            <ShieldCheck v-if="review.isVerified" :size="12" :stroke-width="2" />
            {{ statusLabel(review) }}
          </span>
        </header>

        <p class="rev__meta">{{ dateLabel(review.publishedAt) }} · 权重 {{ Number(review.weightFactor ?? 0).toFixed(2) }}</p>
        <p class="rev__content">{{ review.contentText || '用户暂未填写评价内容。' }}</p>

        <div v-if="replies[review.id]?.length" class="rev__quote">
          <MessageSquareReply :size="14" :stroke-width="2" />
          <span>{{ replies[review.id][replies[review.id].length - 1].replyContent }}</span>
        </div>
        <div v-else class="rev__reply">
          <input
            v-model="drafts[review.id]"
            class="rev__reply-input"
            type="text"
            placeholder="回复学员..."
          />
          <button
            class="rev__send"
            type="button"
            :disabled="submittingId === review.id"
            @click="submitReply(review)"
          >
            {{ submittingId === review.id ? '发送中' : '发送' }}
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chip-row { display: flex; flex: 1; flex-wrap: wrap; gap: 8px; }
.chip { @include pen-chip; }

.refresh {
  flex: none;
  width: 36px;
  height: 36px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  display: grid;
  place-items: center;
}

.empty {
  margin: 24px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.rev {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__head { display: flex; align-items: center; gap: 10px; }
  &__avatar { flex: none; width: 36px; height: 36px; border-radius: 999px; background: $pen-ink; }
  &__who { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 4px; }
  &__name { overflow: hidden; font-size: 14px; font-weight: 900; line-height: $pen-lh; text-overflow: ellipsis; white-space: nowrap; }
  &__stars { display: inline-flex; gap: 3px; }
  &__badge {
    flex: none;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    min-height: 24px;
    padding: 0 8px;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 11px;
    font-weight: 800;
  }
  &__meta { margin: 0; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: $pen-lh; }
  &__content { margin: 0; font-size: 14px; font-weight: 500; line-height: 1.4; }

  &__quote {
    display: flex;
    gap: 8px;
    padding: 12px;
    border-radius: 10px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.4;
  }

  &__reply {
    display: flex;
    align-items: center;
    gap: 8px;
    min-height: 42px;
    padding: 0 8px 0 16px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
  }
  &__reply-input {
    min-width: 0;
    flex: 1;
    border: 0;
    outline: 0;
    background: transparent;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 600;
  }
  &__send {
    flex: none;
    min-width: 58px;
    height: 30px;
    padding: 6px 12px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;

    &:disabled {
      opacity: 0.55;
      cursor: progress;
    }
  }
}
</style>
