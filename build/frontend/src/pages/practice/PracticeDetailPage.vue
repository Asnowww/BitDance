<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import {
  fetchPracticeDetail,
  joinPractice,
  cancelJoin,
  confirmPractice,
  type PracticePost
} from '@/api/practice';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();
const detail = ref<PracticePost | null>(null);
const loading = ref(true);
const id = computed(() => Number(route.params.id));

const JOIN_KEY = 'bitdance_mock_practice_joins';
const isJoined = computed(() => {
  try {
    const arr = JSON.parse(localStorage.getItem(JOIN_KEY) ?? '[]') as number[];
    return arr.includes(id.value);
  } catch {
    return false;
  }
});

const isAuthor = computed(() => detail.value?.authorId === 999);

const reload = async () => {
  loading.value = true;
  try {
    detail.value = await fetchPracticeDetail(id.value);
  } finally {
    loading.value = false;
  }
};

const onJoin = async () => {
  await joinPractice(id.value);
  showSuccessToast('已申请加入');
  void reload();
};

const onCancel = async () => {
  await showConfirmDialog({ title: '取消报名？', message: '取消后名额释放给其他人' }).catch(() => {
    throw new Error('cancel');
  });
  await cancelJoin(id.value);
  showSuccessToast('已取消');
  void reload();
};

const onConfirm = async () => {
  await confirmPractice(id.value);
  showSuccessToast('已确认成行');
  void reload();
};

onMounted(reload);

const STATUS_LABEL: Record<string, string> = {
  PUBLISHED: '招募中',
  MATCHED: '人满',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELED: '已取消',
  EXPIRED: '已过期'
};
</script>

<template>
  <div v-if="loading" class="empty">加载中…</div>
  <div v-else-if="!detail" class="empty">约练不存在或已删除</div>
  <div v-else class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">约练详情</span>
    </header>
    <section class="hero">
      <div class="hero__title">{{ detail.title }}</div>
      <div class="hero__meta">
        <span class="status" :data-s="detail.status">{{ STATUS_LABEL[detail.status] }}</span>
        <span>{{ detail.style }} · {{ detail.level }}</span>
      </div>
    </section>
    <section class="block">
      <h3>时间地点</h3>
      <div class="row"><span>📅</span><span>{{ detail.date }} {{ detail.time }}</span></div>
      <div class="row"><span>📍</span><span>{{ detail.city }} {{ detail.area }} · {{ detail.location }}</span></div>
      <div class="row"><span>👥</span><span>已报名 {{ detail.takenCount }} / {{ detail.capacity }}</span></div>
    </section>
    <section v-if="detail.remark" class="block">
      <h3>备注</h3>
      <p>{{ detail.remark }}</p>
    </section>
    <section class="block">
      <h3>发起人</h3>
      <div class="author">
        <span class="avatar">{{ detail.authorName.charAt(0) }}</span>
        <div>
          <div class="author__name">{{ detail.authorName }}</div>
          <div class="author__time">{{ new Date(detail.createdAt).toLocaleString() }}</div>
        </div>
      </div>
    </section>
    <footer class="footer">
      <template v-if="isAuthor">
        <button v-if="detail.status === 'MATCHED' || detail.status === 'PUBLISHED'" class="btn btn--primary" @click="onConfirm">
          确认成行
        </button>
        <button v-else class="btn btn--ghost" disabled>{{ STATUS_LABEL[detail.status] }}</button>
      </template>
      <template v-else>
        <button v-if="isJoined" class="btn btn--ghost" @click="onCancel">取消报名</button>
        <button
          v-else
          class="btn btn--primary"
          :disabled="detail.status !== 'PUBLISHED' || detail.takenCount >= detail.capacity"
          @click="onJoin"
        >
          {{ detail.takenCount >= detail.capacity ? '人满了' : (user.isLogin ? '申请加入' : '登录后加入') }}
        </button>
      </template>
      <button
        v-if="detail.status === 'CONFIRMED' || detail.status === 'COMPLETED'"
        class="btn btn--ghost"
        @click="router.push(`/practice/${detail.id}/rate?to=${detail.authorId}`)"
      >
        给 TA 评价
      </button>
    </footer>
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
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.hero {
  padding: 16px;
  background: linear-gradient(135deg, #fff7f8, #fff);
  &__title {
    font-size: 20px;
    font-weight: 700;
  }
  &__meta {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.block {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 8px;
    font-size: 14px;
  }
  p {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
  }
}
.row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  padding: 4px 0;
}
.author {
  display: flex;
  gap: 10px;
  align-items: center;
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.status {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 170, 51, 0.15);
  color: #c87a00;
  &[data-s='MATCHED'],
  &[data-s='CONFIRMED'] {
    background: rgba(54, 165, 255, 0.12);
    color: #36a5ff;
  }
  &[data-s='COMPLETED'] {
    background: rgba(0, 168, 84, 0.12);
    color: #00a854;
  }
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
}
.btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  &--primary {
    background: var(--bd-primary);
    color: #fff;
  }
  &--ghost {
    background: rgba(255, 36, 66, 0.08);
    color: var(--bd-primary);
  }
  &:disabled {
    opacity: 0.5;
  }
}
</style>
