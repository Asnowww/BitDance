<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Bookmark, ChevronLeft, ChevronRight, Ellipsis, Flag, Heart, MapPin, Pencil, Share2, Trash2, X } from 'lucide-vue-next';
import {
  createComment,
  deletePost,
  fetchComments,
  fetchFollowStatus,
  fetchPostDetail,
  reportComment,
  reportPost,
  sharePost,
  toggleFollow,
  togglePostCollect,
  togglePostLike,
  type ContentComment,
  type ContentPost
} from '@/api/community';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const postId = Number(route.params.id) || 1;
const menuOpen = ref(false);
const post = ref<ContentPost | null>(null);
const comments = ref<ContentComment[]>([]);
const commentText = ref('');
const reportOpen = ref(false);
const shareOpen = ref(false);
const selectedReason = ref('');
const reportNote = ref('');
const loading = ref(false);
const following = ref(false);
const replyingTo = ref<ContentComment | null>(null);
const reportTarget = ref<{ type: 'post' | 'comment'; comment?: ContentComment }>({ type: 'post' });
const currentUserId = computed(() => user.profile?.id ?? 1);
const isMine = computed(() => Boolean(post.value && post.value.authorId === currentUserId.value));

const reportReasons = [
  '低俗或不适内容',
  '骚扰、辱骂或人身攻击',
  '虚假宣传或引流',
  '侵犯版权或盗用作品',
  '危险行为或线下安全风险',
  '其他问题'
];

const shareChannels = [
  { key: 'wechat', label: '微信好友' },
  { key: 'moments', label: '朋友圈' },
  { key: 'copy', label: '复制链接' }
] as const;

const rootComments = computed(() => comments.value.filter((item) => !item.parentCommentId));
const repliesByParent = computed(() => {
  const map = new Map<number, ContentComment[]>();
  for (const item of comments.value) {
    if (!item.parentCommentId) continue;
    const list = map.get(item.parentCommentId) ?? [];
    list.push(item);
    map.set(item.parentCommentId, list);
  }
  return map;
});

const relativeTime = computed(() => {
  if (!post.value) return '';
  const minutes = Math.max(1, Math.floor((Date.now() - post.value.createdAt) / 60000));
  if (minutes < 60) return `${minutes} 分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} 小时前`;
  return `${Math.floor(hours / 24)} 天前`;
});

const load = async () => {
  loading.value = true;
  try {
    const [detail, list] = await Promise.all([fetchPostDetail(postId), fetchComments(postId)]);
    post.value = detail;
    comments.value = list;
    if (detail.authorId) {
      const status = await fetchFollowStatus(detail.authorId).catch(() => null);
      following.value = Boolean(status?.following);
    }
  } finally {
    loading.value = false;
  }
};

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};

const onCollect = async () => {
  if (!post.value) return;
  const next = await togglePostCollect(post.value.id);
  post.value.collected = next.collected;
  post.value.collectCount = next.collectCount;
  menuOpen.value = false;
  showToast(next.collected ? '已收藏' : '已取消收藏');
};

const onReport = () => {
  menuOpen.value = false;
  reportTarget.value = { type: 'post' };
  reportOpen.value = true;
};

const onReportComment = (comment: ContentComment) => {
  reportTarget.value = { type: 'comment', comment };
  selectedReason.value = '';
  reportNote.value = '';
  reportOpen.value = true;
};

const openShare = () => {
  menuOpen.value = false;
  shareOpen.value = true;
};

const onEdit = () => {
  if (!post.value || !isMine.value) return;
  menuOpen.value = false;
  router.push(`/community/post/${post.value.id}/edit`);
};

const onDelete = async () => {
  if (!post.value || !isMine.value) return;
  await deletePost(post.value.id);
  menuOpen.value = false;
  showToast('动态已删除');
  router.replace('/community');
};

const closeReport = () => {
  reportOpen.value = false;
};

const closeShare = () => {
  shareOpen.value = false;
};

const submitReport = async () => {
  if (!post.value) return;
  if (!selectedReason.value) {
    showToast('请选择举报原因');
    return;
  }
  const reason = `${selectedReason.value}${reportNote.value ? `：${reportNote.value}` : ''}`;
  if (reportTarget.value.type === 'comment' && reportTarget.value.comment) {
    await reportComment(reportTarget.value.comment.id, reason);
  } else {
    await reportPost(post.value.id, reason);
  }
  reportOpen.value = false;
  selectedReason.value = '';
  reportNote.value = '';
  reportTarget.value = { type: 'post' };
  showToast('举报已提交，平台会尽快处理');
};

const onLike = async () => {
  if (!post.value) return;
  const next = await togglePostLike(post.value.id);
  post.value.liked = next.liked;
  post.value.likeCount = next.likeCount;
};

const shareUrl = () => `${window.location.origin}${window.location.pathname}#/community/post/${postId}`;

const copyShareLink = async () => {
  const url = shareUrl();
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(url);
    return;
  }
  const input = document.createElement('input');
  input.value = url;
  document.body.appendChild(input);
  input.select();
  document.execCommand('copy');
  document.body.removeChild(input);
};

const onShare = async (channel: 'wechat' | 'moments' | 'copy') => {
  if (!post.value) return;
  const next = await sharePost(post.value.id, channel === 'copy' ? 'copy' : channel);
  post.value.shareCount = next.shareCount;
  if (channel === 'copy' || !navigator.share) {
    await copyShareLink();
    showToast(channel === 'copy' ? '链接已复制' : '分享链接已复制');
  } else {
    await navigator.share({ title: 'BitDance 动态', text: post.value.text, url: shareUrl() }).catch(() => undefined);
    showToast('已记录分享');
  }
  shareOpen.value = false;
};

const onFollow = async () => {
  if (!post.value || isMine.value) return;
  const next = await toggleFollow(post.value.authorId);
  following.value = next.following;
  showToast(next.following ? '已关注' : '已取消关注');
};

const openAuthorHome = () => {
  if (!post.value) return;
  router.push(`/user/${post.value.authorId}`);
};

const openMap = () => {
  if (!post.value) return;
  if (post.value.latitude !== undefined && post.value.longitude !== undefined) {
    window.open(
      `https://www.google.com/maps/search/?api=1&query=${post.value.latitude},${post.value.longitude}`,
      '_blank'
    );
    return;
  }
  showToast(post.value.location ? '该动态没有保存坐标' : '未显示位置');
};

const onComment = async () => {
  const text = commentText.value.trim();
  if (!text) {
    showToast('请输入评论');
    return;
  }
  const saved = await createComment(postId, text, replyingTo.value
    ? { parentCommentId: replyingTo.value.parentCommentId ?? replyingTo.value.id, replyToUserId: replyingTo.value.authorId }
    : undefined);
  comments.value.push(saved);
  commentText.value = '';
  replyingTo.value = null;
  if (post.value) post.value.commentCount += 1;
};

const startReply = (comment: ContentComment) => {
  replyingTo.value = comment;
};

const cancelReply = () => {
  replyingTo.value = null;
};

const authorNameById = (userId?: number | null) => {
  if (!userId) return 'TA';
  if (post.value?.authorId === userId) return post.value.authorName;
  return comments.value.find((item) => item.authorId === userId)?.authorName ?? `舞者${String(userId).slice(-4)}`;
};

const reportTitle = computed(() => (reportTarget.value.type === 'comment' ? '举报评论' : '举报动态'));

onMounted(load);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">动态</h1>
      <div class="more">
        <button class="topbar__icon" type="button" aria-label="更多" @click="toggleMenu">
          <Ellipsis :size="22" :stroke-width="2.2" />
        </button>
        <div v-if="menuOpen" class="more__menu">
          <button class="more__item" type="button" @click="onCollect">
            <Bookmark :size="18" :stroke-width="2" />
            <span>{{ post?.collected ? '取消收藏' : '收藏' }}</span>
          </button>
          <button class="more__item" type="button" @click="openShare">
            <Share2 :size="18" :stroke-width="2" />
            <span>分享</span>
          </button>
          <button v-if="isMine" class="more__item" type="button" @click="onEdit">
            <Pencil :size="18" :stroke-width="2" />
            <span>编辑</span>
          </button>
          <button v-if="isMine" class="more__item more__item--danger" type="button" @click="onDelete">
            <Trash2 :size="18" :stroke-width="2" />
            <span>删除</span>
          </button>
          <button class="more__item more__item--danger" type="button" @click="onReport">
            <Flag :size="18" :stroke-width="2" />
            <span>举报</span>
          </button>
        </div>
      </div>
    </header>

    <section v-if="loading || !post" class="pen-scroll">
      <p class="empty">{{ loading ? '加载中' : '动态不存在' }}</p>
    </section>

    <section v-else class="pen-scroll">
      <header class="author" @click="openAuthorHome">
        <span class="author__avatar" aria-hidden="true" />
        <div class="author__who">
          <strong class="author__name">{{ post.authorName }}</strong>
          <span class="author__meta">{{ post.location ?? post.style ?? '社区' }} · {{ relativeTime }}</span>
        </div>
        <button v-if="!isMine" class="author__follow" type="button" @click.stop="onFollow">
          {{ following ? '已关注' : '关注' }}
        </button>
      </header>

      <p class="text">
        {{ post.text }}
      </p>

      <div v-if="post.mediaAssets.length" class="media">
        <video
          v-if="post.hasVideo && post.mediaAssets[0]"
          :src="post.mediaAssets[0].url"
          controls
          playsinline
          preload="metadata"
        />
        <template v-else>
          <img
            v-for="image in post.mediaAssets.filter((item) => item.mediaType === 'image')"
            :key="image.id"
            :src="image.url"
            :alt="image.originalFilename || '动态图片'"
          />
        </template>
      </div>

      <button class="anchor" type="button" @click="openMap">
        <MapPin :size="20" :stroke-width="2" />
        <div class="anchor__copy">
          <strong>{{ post.location ?? post.style ?? '社区动态' }}</strong>
          <span>
            {{ post.latitude !== undefined && post.longitude !== undefined
              ? `${post.latitude.toFixed(5)}, ${post.longitude.toFixed(5)}`
              : post.topics.map((topic) => `#${topic}`).join(' ') || '来自真实社区数据' }}
          </span>
        </div>
        <ChevronRight class="anchor__chev" :size="18" :stroke-width="2" />
      </button>

      <p class="stats">{{ post.likeCount }} 赞 · {{ post.commentCount }} 评论 · {{ post.collectCount }} 收藏 · {{ post.shareCount }} 分享</p>

      <article v-for="c in rootComments" :key="c.id" class="comment">
        <span class="comment__avatar" aria-hidden="true" />
        <div class="comment__body">
          <strong class="comment__name">{{ c.authorName }}</strong>
          <p class="comment__text">{{ c.text }}</p>
          <div class="comment__ops">
            <button type="button" @click="startReply(c)">回复</button>
            <button type="button" @click="onReportComment(c)">
              <Flag :size="13" :stroke-width="2" />
              举报
            </button>
          </div>
          <div v-if="repliesByParent.get(c.id)?.length" class="reply-list">
            <article v-for="reply in repliesByParent.get(c.id)" :key="reply.id" class="reply">
              <strong>{{ reply.authorName }}</strong>
              <span>回复 {{ authorNameById(reply.replyToUserId) }}</span>
              <p>{{ reply.text }}</p>
              <div class="reply__ops">
                <button type="button" @click="startReply(reply)">回复</button>
                <button type="button" @click="onReportComment(reply)">
                  <Flag :size="12" :stroke-width="2" />
                  举报
                </button>
              </div>
            </article>
          </div>
        </div>
        <Heart class="comment__like" :size="16" :stroke-width="2" />
      </article>
      <p v-if="comments.length === 0" class="empty">暂无评论</p>
    </section>

    <footer class="comment-bar">
      <div v-if="replyingTo" class="replying">
        <span>回复 {{ replyingTo.authorName }}</span>
        <button type="button" aria-label="取消回复" @click="cancelReply">
          <X :size="14" :stroke-width="2" />
        </button>
      </div>
      <input
        v-model="commentText"
        class="comment-bar__input"
        type="text"
        :placeholder="replyingTo ? `回复 ${replyingTo.authorName}…` : '写评论…'"
        @keyup.enter="onComment"
      />
      <button v-if="commentText.trim()" class="comment-bar__send" type="button" @click="onComment">发送</button>
      <button v-else class="comment-bar__like" type="button" aria-label="点赞" @click="onLike">
        <Heart :size="20" :stroke-width="2" :fill="post?.liked ? 'currentColor' : 'none'" />
      </button>
    </footer>

    <div v-if="shareOpen" class="share-layer" role="dialog" aria-modal="true" aria-label="分享动态">
      <button class="share-layer__backdrop" type="button" aria-label="关闭分享弹框" @click="closeShare" />
      <section class="share-sheet">
        <div class="share-sheet__handle" aria-hidden="true" />
        <header class="share-sheet__head">
          <h2>分享动态</h2>
          <p>{{ post?.shareCount ?? 0 }} 次分享</p>
        </header>
        <div class="share-grid">
          <button v-for="channel in shareChannels" :key="channel.key" type="button" @click="onShare(channel.key)">
            <Share2 :size="20" :stroke-width="2" />
            <span>{{ channel.label }}</span>
          </button>
        </div>
      </section>
    </div>

    <div v-if="reportOpen" class="report-layer" role="dialog" aria-modal="true" :aria-label="reportTitle">
      <button class="report-layer__backdrop" type="button" aria-label="关闭举报弹框" @click="closeReport" />
      <section class="report-sheet">
        <div class="report-sheet__handle" aria-hidden="true" />
        <header class="report-sheet__head">
          <h2>{{ reportTitle }}</h2>
          <p>{{ reportTarget.type === 'comment' ? `来自 ${reportTarget.comment?.authorName ?? 'TA'} 的评论` : '请选择最符合的问题类型。' }}</p>
        </header>

        <div class="reason-list">
          <button
            v-for="reason in reportReasons"
            :key="reason"
            class="reason"
            :class="{ 'reason--active': selectedReason === reason }"
            type="button"
            @click="selectedReason = reason"
          >
            <span>{{ reason }}</span>
          </button>
        </div>

        <textarea
          v-model="reportNote"
          class="report-note"
          rows="3"
          placeholder="补充说明，例如涉及的具体内容、截图来源或线下风险。"
        />

        <footer class="report-actions">
          <button class="report-actions__cancel" type="button" @click="closeReport">取消</button>
          <button class="report-actions__submit" type="button" @click="submitReport">提交举报</button>
        </footer>
      </section>
    </div>
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

.topbar {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__title {
    flex: 1;
    margin: 0;
    font-size: 18px;
    font-weight: 900;
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
}

.more {
  position: relative;
  flex: none;

  &__menu {
    position: absolute;
    top: 48px;
    right: 0;
    z-index: 30;
    width: 132px;
    padding: 6px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-canvas;
    box-shadow: 0 12px 28px rgba(17, 17, 17, 0.12);
  }

  &__item {
    width: 100%;
    height: 42px;
    border: 0;
    border-radius: 12px;
    background: transparent;
    color: $pen-ink;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 10px;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;

    &--danger {
      color: #d30005;
    }
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px;
}

.empty {
  margin: 20px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;

  &__avatar {
    flex: none;
    width: 44px;
    height: 44px;
    border-radius: 999px;
    background: $pen-ink;
  }

  &__who {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__follow {
    flex: none;
    height: 34px;
    padding: 8px 16px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.text {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.5;
}

.media {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  overflow: hidden;
  min-height: 190px;
  border-radius: 14px;
  background: $pen-soft;

  img,
  video {
    width: 100%;
    min-height: 190px;
    height: 100%;
    object-fit: cover;
  }

  video {
    grid-column: 1 / -1;
  }
}

.anchor {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: 0;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  cursor: pointer;
  text-align: left;

  &__copy {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    strong {
      font-size: 14px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 600;
      line-height: $pen-lh;
    }
  }

  &__chev {
    flex: none;
    color: $pen-mute;
  }
}

.stats {
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}

.comment {
  display: flex;
  align-items: flex-start;
  gap: 10px;

  &__avatar {
    flex: none;
    width: 32px;
    height: 32px;
    border-radius: 999px;
    background: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__name {
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__text {
    margin: 0;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.4;
  }

  &__ops {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  &__ops button {
    align-self: flex-start;
    height: 26px;
    padding: 0 10px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
  }

  &__like {
    flex: none;
    color: $pen-mute;
    margin-top: 8px;
  }
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
  padding: 10px 12px;
  border-radius: 12px;
  background: $pen-soft;
}

.reply {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 4px 6px;

  strong {
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  p {
    flex-basis: 100%;
    margin: 0;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.4;
  }

  &__ops {
    flex-basis: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  button {
    height: 24px;
    padding: 0 8px;
    border: 0;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-mute;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
  }
}

.comment-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  .replying {
    position: absolute;
    left: 18px;
    right: 18px;
    bottom: calc(62px + env(safe-area-inset-bottom));
    min-height: 34px;
    padding: 0 10px 0 14px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-mute;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;

    button {
      width: 24px;
      height: 24px;
      border: 0;
      border-radius: 999px;
      background: $pen-soft;
      color: $pen-ink;
      display: grid;
      place-items: center;
      cursor: pointer;
    }
  }

  &__input {
    flex: 1;
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 500;
    outline: none;

    &::placeholder {
      color: $pen-mute;
    }
  }

  &__like,
  &__send {
    flex: none;
    width: 44px;
    height: 44px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__send {
    width: auto;
    padding: 0 16px;
    font-size: 13px;
    font-weight: 800;
  }
}

.share-layer {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.share-layer__backdrop {
  position: absolute;
  inset: 0;
  border: 0;
  background: rgba(17, 17, 17, 0.28);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.share-sheet {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 10px 18px calc(18px + env(safe-area-inset-bottom));
  border-radius: 26px 26px 0 0;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 -18px 40px rgba(17, 17, 17, 0.18);
  display: flex;
  flex-direction: column;
  gap: 14px;

  &__handle {
    align-self: center;
    width: 42px;
    height: 4px;
    border-radius: 999px;
    background: $pen-hairline-strong;
  }

  &__head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 12px;

    h2 {
      margin: 0;
      color: $pen-ink;
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    p {
      margin: 0;
      color: $pen-mute;
      font-size: 13px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }
}

.share-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;

  button {
    min-height: 76px;
    border: 1px solid $pen-hairline;
    border-radius: 16px;
    background: $pen-soft;
    color: $pen-ink;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.report-layer {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.report-layer__backdrop {
  position: absolute;
  inset: 0;
  border: 0;
  background: rgba(17, 17, 17, 0.28);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.report-sheet {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 10px 18px calc(18px + env(safe-area-inset-bottom));
  border-radius: 26px 26px 0 0;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 -18px 40px rgba(17, 17, 17, 0.18);
  display: flex;
  flex-direction: column;
  gap: 14px;

  &__handle {
    align-self: center;
    width: 42px;
    height: 4px;
    border-radius: 999px;
    background: $pen-hairline-strong;
  }

  &__head {
    display: flex;
    flex-direction: column;
    gap: 4px;

    h2 {
      margin: 0;
      color: $pen-ink;
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    p {
      margin: 0;
      color: $pen-mute;
      font-size: 13px;
      font-weight: 700;
      line-height: $pen-lh;
    }
  }
}

.reason-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reason {
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;
  color: $pen-ink;
  display: flex;
  align-items: center;
  text-align: left;
  font-size: 14px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.report-note {
  width: 100%;
  min-height: 78px;
  padding: 12px 14px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  resize: none;
  outline: none;

  &::placeholder {
    color: $pen-mute;
    font-weight: 600;
  }
}

.report-actions {
  display: flex;
  gap: 10px;

  button {
    height: 48px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;
  }

  &__cancel {
    width: 104px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__submit {
    flex: 1;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}
</style>
