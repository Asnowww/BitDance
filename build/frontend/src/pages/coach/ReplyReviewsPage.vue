<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import EmptyState from '@/components/EmptyState.vue';
import { storeToRefs } from 'pinia';
import { useOpsStore } from '@/stores/ops';
import {
  fetchPendingReplyReviews,
  fetchRepliesByReview,
  createReviewReply,
  deleteReviewReply,
  fetchMyReplies,
  type PendingReview,
  type ReviewReply
} from '@/api/coachOps';

const router = useRouter();
const ops = useOpsStore();
const { activeRole } = storeToRefs(ops);

const filter = ref<'pending' | 'mine'>('pending');
const loading = ref(true);
const reviews = ref<PendingReview[]>([]);
const myReplies = ref<ReviewReply[]>([]);
const repliesMap = ref<Record<number, ReviewReply[]>>({});

const targetLabels: Record<string, string> = {
  studio: '舞室评价',
  course: '课程评价',
  coach: '教练评价'
};

const fmt = (t?: string | null) =>
  t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : '—';

const myUserId = computed(() => ops.me?.id);

const load = async () => {
  await ops.refresh();
  loading.value = true;
  try {
    if (filter.value === 'pending') {
      if (!ops.studioId) {
        reviews.value = [];
        return;
      }
      reviews.value = await fetchPendingReplyReviews(ops.studioId);
      const entries = await Promise.all(
        reviews.value.map(
          async (r) => [r.id, await fetchRepliesByReview(r.id).catch(() => [])] as const
        )
      );
      repliesMap.value = Object.fromEntries(entries);
    } else {
      myReplies.value = await fetchMyReplies();
    }
  } finally {
    loading.value = false;
  }
};

// ---------- 回复 ----------
const replyTarget = ref<PendingReview | null>(null);
const replyText = ref('');
const replying = ref(false);

const submitReply = async () => {
  if (!replyTarget.value || !replyText.value.trim() || replying.value) return;
  replying.value = true;
  try {
    await createReviewReply({
      reviewId: replyTarget.value.id,
      replyContent: replyText.value.trim(),
      isOfficial: true
    });
    showSuccessToast('已回复');
    replyTarget.value = null;
    replyText.value = '';
    load();
  } finally {
    replying.value = false;
  }
};

const removeReply = async (r: ReviewReply) => {
  await showConfirmDialog({
    title: '删除回复',
    message: '回复删除后不可恢复(可重新回复),确认删除?'
  });
  await deleteReviewReply(r.id);
  showSuccessToast('已删除');
  load();
};
</script>

<template>
  <main class="replies-page">
    <PenTopBar title="评价回复" :show-share="false" />

    <nav class="chips">
      <button :class="{ active: filter === 'pending' }" @click="filter = 'pending'; load()">
        待回复
      </button>
      <button :class="{ active: filter === 'mine' }" @click="filter = 'mine'; load()">
        我的回复
      </button>
      <button class="appeal-link" @click="router.push('/coach/appeal')">评价申诉 ›</button>
    </nav>

    <section class="body">
      <p v-if="loading" class="loading">加载中…</p>

      <!-- 待回复 -->
      <template v-else-if="filter === 'pending'">
        <EmptyState
          v-if="!ops.studioId && activeRole === 'studio_admin'"
          title="尚未开通商家后台"
          desc="完成舞室入驻后查看待回复评价"
        />
        <EmptyState
          v-else-if="!reviews.length"
          title="暂无待回复评价"
          desc="用户的新评价会出现在这里"
        />
        <article v-for="r in reviews" :key="r.id" class="card">
          <div class="head">
            <h3>{{ targetLabels[r.targetType] ?? r.targetType }} · 用户 #{{ r.userId }}</h3>
            <span class="badge ink">{{ Number(r.overallScore).toFixed(1) }} 分</span>
          </div>
          <p class="content">{{ r.contentText }}</p>
          <p class="meta">
            <span v-if="r.isVerified">已核验消费</span>
            <span>{{ fmt(r.publishedAt) }}</span>
          </p>

          <div v-if="repliesMap[r.id]?.length" class="reply-list">
            <div v-for="rep in repliesMap[r.id]" :key="rep.id" class="reply-item">
              <p>
                <em v-if="rep.isOfficial" class="official">官方回复</em>
                {{ rep.replyContent }}
              </p>
              <button
                v-if="rep.replierUserId === myUserId"
                class="del"
                @click="removeReply(rep)"
              >
                删除
              </button>
            </div>
          </div>

          <div class="actions">
            <button class="primary" @click="replyTarget = r">回复</button>
            <button @click="router.push({ path: '/coach/appeal', query: { reviewId: r.id } })">
              申诉
            </button>
          </div>
        </article>
      </template>

      <!-- 我的回复 -->
      <template v-else>
        <EmptyState v-if="!myReplies.length" title="还没有回复过评价" />
        <article v-for="rep in myReplies" :key="rep.id" class="card">
          <div class="head">
            <h3>评价 #{{ rep.reviewId }}</h3>
            <em v-if="rep.isOfficial" class="official">官方回复</em>
          </div>
          <p class="content">{{ rep.replyContent }}</p>
          <p class="meta"><span>{{ fmt(rep.createdAt) }}</span></p>
          <div class="actions">
            <button class="danger" @click="removeReply(rep)">删除</button>
          </div>
        </article>
      </template>
    </section>

    <!-- 回复弹层 -->
    <van-popup :show="Boolean(replyTarget)" position="bottom" round @update:show="replyTarget = null">
      <div class="sheet">
        <h2>回复评价</h2>
        <p class="quote">{{ replyTarget?.contentText }}</p>
        <textarea
          v-model="replyText"
          maxlength="1000"
          placeholder="以官方身份回复,注意礼貌与事实依据…"
        />
        <button class="sheet-submit" :disabled="replying || !replyText.trim()" @click="submitReply">
          {{ replying ? '发送中…' : '发送官方回复' }}
        </button>
      </div>
    </van-popup>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.replies-page {
  @include ops-page;
}
.chips {
  @include ops-chip-row;
  .appeal-link {
    margin-left: auto;
    border: 0;
    background: none;
    color: $pen-mute;
    font-size: 12.5px;
    font-weight: 800;
    height: auto;
    padding: 0;
  }
}
.body {
  @include ops-body;
}
.loading {
  @include ops-loading;
}
.card {
  @include ops-card;
}
.head {
  @include ops-card-head;
}
.badge {
  @include ops-badge;
}
.meta {
  @include ops-meta;
}
.actions {
  @include ops-actions;
}

.content {
  margin: 10px 0 0;
  color: $pen-charcoal;
  font-size: 13.5px;
  line-height: 1.6;
}

.official {
  flex: 0 0 auto;
  font-style: normal;
  font-size: 10.5px;
  font-weight: 900;
  color: #fff;
  background: $pen-ink;
  border-radius: 999px;
  padding: 3px 8px;
  margin-right: 6px;
}

.reply-list {
  margin-top: 12px;
  border-left: 2px solid $pen-hairline-strong;
  padding-left: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  .reply-item {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 10px;
    p {
      margin: 0;
      font-size: 13px;
      color: $pen-ink;
      line-height: 1.5;
    }
    .del {
      flex: 0 0 auto;
      border: 0;
      background: none;
      color: #d30005;
      font-size: 12px;
      font-weight: 800;
      cursor: pointer;
    }
  }
}

.sheet {
  padding: 24px 20px calc(24px + env(safe-area-inset-bottom));
  h2 {
    margin: 0 0 12px;
    font-size: 20px;
    font-weight: 900;
  }
  .quote {
    margin: 0 0 12px;
    border-radius: 14px;
    background: $pen-soft;
    padding: 10px 12px;
    color: $pen-mute;
    font-size: 12.5px;
    line-height: 1.5;
    max-height: 80px;
    overflow: hidden;
  }
  textarea {
    width: 100%;
    min-height: 100px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-soft;
    padding: 12px 14px;
    font-size: 14px;
    font-family: inherit;
    outline: none;
    box-sizing: border-box;
    resize: vertical;
    &:focus {
      border-color: $pen-ink;
      background: $pen-canvas;
    }
  }
  .sheet-submit {
    @include pen-primary-btn;
    width: 100%;
    margin-top: 14px;
  }
}
</style>
