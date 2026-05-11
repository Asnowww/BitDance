<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import {
  fetchPostDetail,
  togglePostLike,
  togglePostCollect,
  reportPost,
  fetchComments,
  createComment,
  type ContentPost,
  type ContentComment
} from '@/api/community';

const route = useRoute();
const router = useRouter();

const id = computed(() => Number(route.params.id));
const post = ref<ContentPost | null>(null);
const comments = ref<ContentComment[]>([]);
const newComment = ref('');

const reload = async () => {
  post.value = await fetchPostDetail(id.value);
  comments.value = await fetchComments(id.value);
};

const onLike = async () => {
  if (!post.value) return;
  const r = await togglePostLike(post.value.id);
  post.value.liked = r.liked;
  post.value.likeCount = r.likeCount;
};

const onCollect = async () => {
  if (!post.value) return;
  const r = await togglePostCollect(post.value.id);
  post.value.collected = r.collected;
  post.value.collectCount = r.collectCount;
};

const reportSheetVisible = ref(false);
const reportActions = [
  { name: '内容不实' },
  { name: '广告 / 引流' },
  { name: '低俗 / 不适' },
  { name: '其他' }
];

const onReport = () => {
  reportSheetVisible.value = true;
};

const onPickReport = async (a: { name: string }) => {
  reportSheetVisible.value = false;
  if (!post.value) return;
  await reportPost(post.value.id, a.name);
  showSuccessToast('已提交举报，感谢反馈');
};

const onSendComment = async () => {
  const text = newComment.value.trim();
  if (!text) {
    showFailToast('评论不能为空');
    return;
  }
  if (!post.value) return;
  await createComment(post.value.id, text);
  newComment.value = '';
  void reload();
};

onMounted(reload);
</script>

<template>
  <div v-if="!post" class="empty">加载中…</div>
  <div v-else class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <button class="report" @click="onReport">举报</button>
    </header>
    <section class="author" @click="router.push(`/u/${post.authorId}`)">
      <span class="avatar">{{ post.authorName.charAt(0) }}</span>
      <div class="author__body">
        <div class="author__name">{{ post.authorName }}</div>
        <div class="author__time">{{ new Date(post.createdAt).toLocaleString() }}</div>
      </div>
    </section>
    <section class="cover">
      <span v-if="post.hasVideo" class="cover__video">▶</span>
      <span class="cover__text">{{ post.style ?? '✨' }}</span>
    </section>
    <section class="content">
      <p class="content__text">{{ post.text }}</p>
      <div v-if="post.topics.length" class="topics">
        <span
          v-for="t in post.topics"
          :key="t"
          class="topic"
          @click="router.push(`/community/topic/${encodeURIComponent(t)}`)"
        >
          #{{ t }}
        </span>
      </div>
      <div v-if="post.location" class="loc">📍 {{ post.location }}</div>
    </section>
    <section class="comments">
      <h3>评论 ({{ comments.length }})</h3>
      <article v-for="c in comments" :key="c.id" class="comment">
        <span class="avatar avatar--sm">{{ c.authorName.charAt(0) }}</span>
        <div class="comment__body">
          <div class="comment__name">{{ c.authorName }}</div>
          <p class="comment__text">{{ c.text }}</p>
          <div class="comment__time">{{ new Date(c.createdAt).toLocaleString() }}</div>
        </div>
      </article>
      <div v-if="!comments.length" class="empty-tip">还没有评论，第一个抢沙发吧</div>
    </section>
    <footer class="footer">
      <input v-model="newComment" class="input" placeholder="说点什么…" @keyup.enter="onSendComment" />
      <button class="action" :class="{ active: post.liked }" @click="onLike">♥ {{ post.likeCount }}</button>
      <button class="action" :class="{ active: post.collected }" @click="onCollect">⭐ {{ post.collectCount }}</button>
      <button class="send" @click="onSendComment">发送</button>
    </footer>
    <van-action-sheet
      v-model:show="reportSheetVisible"
      :actions="reportActions"
      cancel-text="取消"
      close-on-click-action
      @select="onPickReport"
      @cancel="reportSheetVisible = false"
    />
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
.empty {
  padding: 80px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.bar {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.report {
  border: none;
  background: none;
  color: var(--bd-text-secondary);
  font-size: 13px;
  cursor: pointer;
}
.author {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  cursor: pointer;
  &__name {
    font-size: 14px;
    font-weight: 600;
  }
  &__time {
    margin-top: 2px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  &--sm {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
}
.cover {
  aspect-ratio: 1;
  background: linear-gradient(135deg, #ffd2da, #ff2442);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: 700;
  position: relative;
  &__video {
    position: absolute;
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
  }
}
.content {
  padding: 16px;
  background: #fff;
  &__text {
    margin: 0;
    font-size: 14px;
    line-height: 1.6;
  }
}
.topics {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic {
  font-size: 12px;
  color: var(--bd-primary);
  cursor: pointer;
}
.loc {
  margin-top: 12px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.comments {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 12px;
    font-size: 14px;
  }
}
.comment {
  display: flex;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--bd-border);
  &:last-child {
    border-bottom: none;
  }
  &__name {
    font-size: 12px;
    font-weight: 600;
    color: var(--bd-text-secondary);
  }
  &__text {
    margin: 4px 0;
    font-size: 13px;
  }
  &__time {
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.empty-tip {
  padding: 24px;
  text-align: center;
  color: var(--bd-text-secondary);
  font-size: 12px;
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
  display: flex;
  align-items: center;
  gap: 8px;
}
.input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  background: #fafafa;
  font-size: 13px;
  outline: none;
  &:focus {
    background: #fff;
    border-color: var(--bd-primary);
  }
}
.action {
  border: none;
  background: none;
  font-size: 13px;
  color: var(--bd-text-secondary);
  cursor: pointer;
  &.active {
    color: var(--bd-primary);
  }
}
.send {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
}
</style>
