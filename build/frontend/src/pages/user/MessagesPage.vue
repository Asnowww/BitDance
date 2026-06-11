<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Bell, CheckCheck, Star, Ticket, User, Users } from 'lucide-vue-next';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchMessages, markAllRead, markRead, type MessageItem } from '@/api/message';

const cats = [
  { key: 'all', label: '全部' },
  { key: 'practice', label: '约练' },
  { key: 'review', label: '评价' },
  { key: 'trial', label: '试听' },
  { key: 'system', label: '系统' }
];

const iconMap = {
  practice: Users,
  review: Star,
  trial: Ticket,
  system: Bell
};

const activeCat = ref('all');
const messages = ref<MessageItem[]>([]);
const unread = ref(0);
const loading = ref(false);

const visibleMessages = computed(() => messages.value);

const formatTime = (value?: string) => {
  if (!value) return '刚刚';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '刚刚';
  return date.toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const loadMessages = async () => {
  loading.value = true;
  try {
    const data = await fetchMessages(activeCat.value);
    messages.value = data.list;
    unread.value = data.unread;
  } finally {
    loading.value = false;
  }
};

const selectCat = async (key: string) => {
  activeCat.value = key;
  await loadMessages();
};

const readOne = async (item: MessageItem) => {
  if (item.isRead || item.read) return;
  await markRead(item.id);
  item.isRead = true;
  item.read = true;
  unread.value = Math.max(0, unread.value - 1);
};

const readAll = async () => {
  await markAllRead();
  messages.value = messages.value.map((item) => ({ ...item, isRead: true, read: true }));
  unread.value = 0;
  showSuccessToast('已全部标记为已读');
};

onMounted(loadMessages);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="消息" :show-share="false" />

    <section class="pen-scroll">
      <header class="summary">
        <div>
          <strong>{{ unread }}</strong>
          <span>条未读消息</span>
        </div>
        <button type="button" :disabled="unread === 0" @click="readAll">
          <CheckCheck :size="18" />
          <span>全部已读</span>
        </button>
      </header>

      <div class="chip-row" aria-label="消息分类">
        <button
          v-for="c in cats"
          :key="c.key"
          class="chip"
          :class="activeCat === c.key ? 'chip--active' : 'chip--inactive'"
          type="button"
          @click="selectCat(c.key)"
        >
          {{ c.label }}
        </button>
      </div>

      <p v-if="loading" class="empty">正在加载消息...</p>
      <p v-else-if="!visibleMessages.length" class="empty">当前分类暂无消息</p>

      <template v-else>
        <article
          v-for="m in visibleMessages"
          :key="m.id"
          class="msg"
          :class="{ 'msg--read': m.isRead || m.read }"
          @click="readOne(m)"
        >
          <span class="msg__dot" :class="{ 'msg__dot--on': !(m.isRead || m.read) }" aria-hidden="true" />
          <span class="msg__avatar" aria-hidden="true">
            <component :is="iconMap[m.category] || User" :size="22" :stroke-width="2" />
          </span>
          <div class="msg__body">
            <div class="msg__top">
              <span class="msg__name">{{ m.title }}</span>
              <span class="msg__time">{{ formatTime(m.createdAt) }}</span>
            </div>
            <p class="msg__preview">{{ m.content || m.body }}</p>
          </div>
        </article>
      </template>
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

.summary {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 64px;

  div {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  strong {
    font-size: 28px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    height: 38px;
    padding: 8px 14px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;

    &:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.empty {
  margin: 0;
  padding: 22px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 8px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 800;
  text-align: center;
}

.msg {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: transparent;
  text-align: left;
  cursor: pointer;

  &--read {
    opacity: 0.72;
  }

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
    min-width: 0;
    overflow: hidden;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
    text-overflow: ellipsis;
    white-space: nowrap;
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
