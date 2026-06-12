<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  CalendarDays,
  CheckCheck,
  ChevronRight,
  Star,
  Ticket,
  User,
  Users
} from 'lucide-vue-next';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchMessages, markAllRead, markRead, type MessageItem } from '@/api/message';

type MessageTab = 'all' | 'practice' | 'review' | 'trial' | 'system' | 'workshop';

const router = useRouter();

const cats: Array<{ key: MessageTab; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'practice', label: '约练' },
  { key: 'review', label: '评价' },
  { key: 'trial', label: '试听' },
  { key: 'workshop', label: '活动' },
  { key: 'system', label: '系统' }
];

const iconMap = {
  practice: Users,
  review: Star,
  trial: Ticket,
  workshop: CalendarDays,
  system: Bell
};

const activeCat = ref<MessageTab>('all');
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

const targetPath = (item: MessageItem) => {
  const id = item.targetId;
  switch (item.targetType) {
    case 'practice_post':
      return id ? `/practice/${id}` : '/me/practices';
    case 'practice_request':
      return '/me/practices';
    case 'review':
    case 'review_reply':
      return '/me/reviews';
    case 'trial':
    case 'trial_order':
      return '/me/trials';
    case 'workshop':
      return id ? `/workshop/${id}` : '/workshops';
    case 'workshop_order':
      return '/me/workshop-orders';
    case 'workshop_session':
      return '/me/workshop-calendar';
    case 'content_post':
      return id ? `/community/post/${id}` : '/community';
    case 'group_class_intent':
      return '/practice/group-class';
    case 'user':
      return id ? `/user/${id}` : undefined;
    default:
      if (item.category === 'practice') return '/me/practices';
      if (item.category === 'review') return '/me/reviews';
      if (item.category === 'trial') return '/me/trials';
      if (item.category === 'workshop') return '/me/workshop-orders';
      return undefined;
  }
};

const loadMessages = async () => {
  loading.value = true;
  try {
    const data = await fetchMessages(activeCat.value);
    messages.value = data.list ?? [];
    unread.value = data.unread ?? 0;
  } finally {
    loading.value = false;
  }
};

const selectCat = async (key: MessageTab) => {
  activeCat.value = key;
  await loadMessages();
};

const readOne = async (item: MessageItem) => {
  const wasUnread = !(item.isRead || item.read);
  if (wasUnread) {
    await markRead(item.id);
    item.isRead = true;
    item.read = true;
    unread.value = Math.max(0, unread.value - 1);
  }
  const path = targetPath(item);
  if (path) {
    await router.push(path);
  }
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
        <div class="summary__count">
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
        <button
          v-for="m in visibleMessages"
          :key="m.id"
          class="msg"
          :class="{ 'msg--read': m.isRead || m.read }"
          type="button"
          @click="readOne(m)"
        >
          <span class="msg__dot" :class="{ 'msg__dot--on': !(m.isRead || m.read) }" aria-hidden="true" />
          <span class="msg__avatar" aria-hidden="true">
            <component :is="iconMap[m.category] || User" :size="22" :stroke-width="2" />
          </span>
          <span class="msg__body">
            <span class="msg__top">
              <span class="msg__name">{{ m.title }}</span>
              <span class="msg__time">{{ formatTime(m.createdAt) }}</span>
            </span>
            <span class="msg__preview">{{ m.content || m.body }}</span>
          </span>
          <ChevronRight class="msg__arrow" :size="18" aria-hidden="true" />
        </button>
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

  &__count {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  strong {
    font-size: 30px;
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
    min-width: 104px;
    height: 40px;
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
  flex-wrap: nowrap;
  gap: 8px;
  margin: 0 -18px;
  padding: 0 18px 2px;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.chip {
  @include pen-chip;
  flex: none;
}

.empty {
  margin: 0;
  padding: 28px 12px;
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
  width: 100%;
  min-height: 74px;
  padding: 12px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: transparent;
  color: $pen-ink;
  text-align: left;
  cursor: pointer;

  &--read {
    opacity: 0.7;
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
    gap: 5px;
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
    display: block;
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 500;
    line-height: $pen-lh;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  &__arrow {
    flex: none;
    color: $pen-mute;
  }
}
</style>
