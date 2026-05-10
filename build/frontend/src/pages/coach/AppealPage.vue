<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { submitAppeal, fetchAppeals, type ReviewAppeal } from '@/api/coachOps';

const route = useRoute();
const router = useRouter();

const reviewId = computed(() => Number(route.query.reviewId ?? 0));
const reason = ref('');
const evidence = ref('');
const list = ref<ReviewAppeal[]>([]);
const submitting = ref(false);

const reload = async () => {
  list.value = await fetchAppeals();
};

const onSubmit = async () => {
  if (!reviewId.value) {
    showFailToast('缺少 reviewId');
    return;
  }
  if (reason.value.length < 5) {
    showFailToast('申诉理由至少 5 字');
    return;
  }
  submitting.value = true;
  try {
    await submitAppeal({
      reviewId: reviewId.value,
      reason: reason.value,
      evidence: evidence.value
    });
    showSuccessToast('已提交申诉，平台将在 3 个工作日内处理');
    reason.value = '';
    evidence.value = '';
    void reload();
  } finally {
    submitting.value = false;
  }
};

const STATUS_LABEL: Record<string, string> = {
  PENDING: '待平台处理',
  APPROVED: '已支持',
  REJECTED: '已驳回'
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">评价申诉</span>
    </header>
    <section v-if="reviewId > 0" class="form">
      <p class="tip">针对评价 #{{ reviewId }} 提交申诉，请描述事实并附上证据线索。</p>
      <textarea v-model="reason" rows="4" class="ta" placeholder="申诉理由（至少 5 字）" />
      <textarea v-model="evidence" rows="4" class="ta" placeholder="证据线索（如订单号、签到记录、聊天片段等）" />
      <button class="btn" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交申诉' }}
      </button>
    </section>
    <section class="list">
      <h3>历史申诉</h3>
      <div v-if="!list.length" class="empty">没有申诉记录</div>
      <article v-for="a in list" :key="a.id" class="item">
        <div class="item__head">
          <span>评价 #{{ a.reviewId }}</span>
          <span class="status" :data-s="a.status">{{ STATUS_LABEL[a.status] }}</span>
        </div>
        <p class="item__reason">{{ a.reason }}</p>
        <div class="item__time">{{ new Date(a.createdAt).toLocaleString() }}</div>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
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
.form {
  background: #fff;
  padding: 16px;
}
.tip {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.ta {
  width: 100%;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 10px;
  font-size: 13px;
  font-family: inherit;
  resize: none;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
  }
}
.btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 999px;
  background: var(--bd-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
.list {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 12px;
    font-size: 14px;
  }
}
.empty {
  text-align: center;
  padding: 24px;
  color: var(--bd-text-secondary);
}
.item {
  padding: 12px 0;
  border-bottom: 1px dashed var(--bd-border);
  &:last-child {
    border-bottom: none;
  }
  &__head {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
  }
  &__reason {
    margin: 6px 0;
    font-size: 13px;
  }
  &__time {
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 170, 51, 0.15);
  color: #c87a00;
  &[data-s='APPROVED'] {
    background: rgba(0, 168, 84, 0.12);
    color: #00a854;
  }
  &[data-s='REJECTED'] {
    background: #f3f3f3;
    color: var(--bd-text-secondary);
  }
}
</style>
