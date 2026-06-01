<script setup lang="ts">
import { ref } from 'vue';
import { Users, User, Star, Ticket, Bell } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';

const cats = ['全部', '约练', '评价', '活动', '系统'];
const activeCat = ref('全部');

const messages = [
  { icon: Users, name: '约练助手', time: '5 分钟前', preview: '你发起的周六 Hiphop 约练有 2 人报名', unread: true },
  { icon: User, name: 'Mia 老师', time: '1 小时前', preview: '试听课已确认，周日 14:00 见～', unread: true },
  { icon: Star, name: '评价提醒', time: '昨天', preview: '本次课程体验如何？来写下结构化评价', unread: false },
  { icon: Ticket, name: '活动通知', time: '周三', preview: 'Locking 大师课开始报名，剩 8 位', unread: false },
  { icon: Bell, name: '系统通知', time: '5/28', preview: '你的资料偏好已更新', unread: false }
];
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="消息" :show-share="false" />

    <section class="pen-scroll">
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

      <article v-for="m in messages" :key="m.name" class="msg">
        <span class="msg__dot" :class="{ 'msg__dot--on': m.unread }" aria-hidden="true" />
        <span class="msg__avatar" aria-hidden="true">
          <component :is="m.icon" :size="22" :stroke-width="2" />
        </span>
        <div class="msg__body">
          <div class="msg__top">
            <span class="msg__name">{{ m.name }}</span>
            <span class="msg__time">{{ m.time }}</span>
          </div>
          <p class="msg__preview">{{ m.preview }}</p>
        </div>
      </article>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.msg {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid $pen-hairline;

  &__dot {
    flex: none;
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: transparent;

    &--on {
      background: $pen-ink;
    }
  }

  &__avatar {
    flex: none;
    width: 48px;
    height: 48px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__name {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__time {
    flex: none;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__preview {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 500;
    line-height: $pen-lh;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
