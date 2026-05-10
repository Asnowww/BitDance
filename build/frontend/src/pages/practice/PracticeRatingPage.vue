<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import StarRating from '@/components/StarRating.vue';
import { submitPracticeRating } from '@/api/buddy';

const route = useRoute();
const router = useRouter();

const practiceId = Number(route.params.id);
const toUserId = Number(route.query.to ?? 0);

const punctuality = ref(5);
const friendliness = ref(5);
const levelMatch = ref(5);
const comment = ref('');
const submitting = ref(false);

const canSubmit = computed(() => toUserId > 0 && !submitting.value);

const onSubmit = async () => {
  submitting.value = true;
  try {
    await submitPracticeRating({
      practiceId,
      toUserId,
      punctuality: punctuality.value,
      friendliness: friendliness.value,
      levelMatch: levelMatch.value,
      comment: comment.value || undefined
    });
    showSuccessToast('已提交，感谢评价');
    router.replace(`/practice/${practiceId}`);
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">约练后评价</span>
    </header>
    <section class="form">
      <p class="tip">为对方打分，完成后双方将自动建立"搭子"关系。</p>
      <div class="dim">
        <span>守时</span>
        <StarRating v-model="punctuality" />
      </div>
      <div class="dim">
        <span>友好度</span>
        <StarRating v-model="friendliness" />
      </div>
      <div class="dim">
        <span>水平匹配</span>
        <StarRating v-model="levelMatch" />
      </div>
      <textarea v-model="comment" rows="4" class="ta" placeholder="可选：写一句话给 TA" />
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交评价' }}
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
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
.form {
  background: #fff;
  padding: 16px;
}
.tip {
  margin: 0 0 16px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.dim {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  span {
    width: 76px;
    font-size: 13px;
  }
}
.ta {
  margin-top: 12px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
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
  background: var(--bd-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
</style>
