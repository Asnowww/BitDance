<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Bookmark, ChevronLeft, ChevronRight, Ellipsis, Flag, Heart, MapPin } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const postId = String(route.params.id || '1');
const menuOpen = ref(false);
const collected = ref(false);
const reportOpen = ref(false);
const selectedReason = ref('');
const reportNote = ref('');

const reportReasons = [
  '低俗或不适内容',
  '骚扰、辱骂或人身攻击',
  '虚假宣传或引流',
  '侵犯版权或盗用作品',
  '危险行为或线下安全风险',
  '其他问题'
];

const comments = [
  { id: 'k', name: '小 K', text: '看起来好棒，下次一起约练？' },
  { id: 'm', name: 'Mia 老师', text: '动作进步很大，继续保持～' }
];

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value;
};

const onCollect = () => {
  collected.value = !collected.value;
  menuOpen.value = false;
  showToast(collected.value ? '已收藏' : '已取消收藏');
};

const onReport = () => {
  menuOpen.value = false;
  reportOpen.value = true;
};

const closeReport = () => {
  reportOpen.value = false;
};

const submitReport = () => {
  if (!selectedReason.value) {
    showToast('请选择举报原因');
    return;
  }
  reportOpen.value = false;
  selectedReason.value = '';
  reportNote.value = '';
  showToast('举报已提交，平台会尽快处理');
};
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
            <span>{{ collected ? '取消收藏' : '收藏' }}</span>
          </button>
          <button class="more__item more__item--danger" type="button" @click="onReport">
            <Flag :size="18" :stroke-width="2" />
            <span>举报</span>
          </button>
        </div>
      </div>
    </header>

    <section class="pen-scroll">
      <header class="author">
        <span class="author__avatar" aria-hidden="true" />
        <div class="author__who">
          <strong class="author__name">小鹿</strong>
          <span class="author__meta">五道口 · 2 小时前</span>
        </div>
        <button class="author__follow" type="button">关注</button>
      </header>

      <p class="text">
        今天试听了 Urban Flow 的韩舞课，老师会拆动作、节奏适合第一次学韩舞的人，零基础也跟得上，强烈推荐给想入门的姐妹！
      </p>

      <div class="media" aria-hidden="true" />

      <button class="anchor" type="button" @click="showToast('打开 Urban Flow 舞室')">
        <MapPin :size="20" :stroke-width="2" />
        <div class="anchor__copy">
          <strong>Urban Flow 舞室</strong>
          <span>韩舞课 · 可预约试听</span>
        </div>
        <ChevronRight class="anchor__chev" :size="18" :stroke-width="2" />
      </button>

      <p class="stats">32 赞 · 8 评论</p>

      <article v-for="c in comments" :key="c.id" class="comment">
        <span class="comment__avatar" aria-hidden="true" />
        <div class="comment__body">
          <strong class="comment__name">{{ c.name }}</strong>
          <p class="comment__text">{{ c.text }}</p>
        </div>
        <Heart class="comment__like" :size="16" :stroke-width="2" />
      </article>
    </section>

    <footer class="comment-bar">
      <div class="comment-bar__input">写评论…</div>
      <button class="comment-bar__like" type="button" aria-label="点赞">
        <Heart :size="20" :stroke-width="2" />
      </button>
    </footer>

    <div v-if="reportOpen" class="report-layer" role="dialog" aria-modal="true" aria-label="举报动态">
      <button class="report-layer__backdrop" type="button" aria-label="关闭举报弹框" @click="closeReport" />
      <section class="report-sheet">
        <div class="report-sheet__handle" aria-hidden="true" />
        <header class="report-sheet__head">
          <h2>举报动态</h2>
          <p>请选择最符合的问题类型。</p>
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
  height: 190px;
  border-radius: 14px;
  background: $pen-soft;
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

  &__like {
    flex: none;
    color: $pen-mute;
    margin-top: 8px;
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

  &__input {
    flex: 1;
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-mute;
    font-size: 14px;
    font-weight: 500;
  }

  &__like {
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
