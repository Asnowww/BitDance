<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import StarRating from '@/components/StarRating.vue';
import {
  REVIEW_DIMENSIONS,
  createReview,
  updateReview,
  fetchMyReviews,
  type ReviewTargetType
} from '@/api/review';

const route = useRoute();
const router = useRouter();

const targetType = ref<ReviewTargetType>(
  (route.query.targetType as ReviewTargetType) || 'studio'
);
const targetId = ref<number>(Number(route.query.targetId) || 1);
const editId = computed(() => (route.query.editId ? Number(route.query.editId) : null));

const text = ref('');
const scores = ref<Record<string, number>>({});
const submitting = ref(false);
const attachments = ref<Array<{ type: 'image' | 'video'; placeholder: string }>>([]);

const addAttachment = (type: 'image' | 'video') => {
  if (attachments.value.length >= 9) return;
  attachments.value.push({ type, placeholder: type === 'image' ? '🖼' : '▶' });
};
const removeAttachment = (i: number) => attachments.value.splice(i, 1);

const dims = computed(() => REVIEW_DIMENSIONS[targetType.value] ?? []);

const initScores = () => {
  scores.value = {};
  dims.value.forEach((d) => (scores.value[d.key] = 5));
};

initScores();

onMounted(async () => {
  if (editId.value) {
    const mine = await fetchMyReviews();
    const found = mine.find((r) => r.id === editId.value);
    if (found) {
      targetType.value = found.targetType;
      targetId.value = found.targetId;
      text.value = found.text;
      scores.value = { ...found.dimensionScores };
    }
  }
});

const onSwitchType = (t: ReviewTargetType) => {
  targetType.value = t;
  initScores();
};

const canSubmit = computed(() => text.value.trim().length >= 5 && !submitting.value);

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('评价内容至少 5 个字');
    return;
  }
  submitting.value = true;
  try {
    if (editId.value) {
      await updateReview(editId.value, {
        text: text.value,
        dimensionScores: scores.value
      });
      showSuccessToast('已更新');
    } else {
      await createReview({
        targetType: targetType.value,
        targetId: targetId.value,
        text: text.value,
        dimensionScores: scores.value,
        images: attachments.value.filter((a) => a.type === 'image').map(() => 'mock-image'),
        idempotencyToken: `review-${targetType.value}-${targetId.value}-${Date.now()}`
      });
      showSuccessToast('评价已发布');
    }
    router.replace('/me/reviews');
  } finally {
    submitting.value = false;
  }
};

const TYPE_TABS: Array<{ key: ReviewTargetType; label: string }> = [
  { key: 'studio', label: '舞室' },
  { key: 'coach', label: '教练' },
  { key: 'course', label: '课程' }
];
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">{{ editId ? '编辑评价' : '写评价' }}</span>
    </header>
    <section v-if="!editId" class="tabs">
      <button
        v-for="t in TYPE_TABS"
        :key="t.key"
        class="tab"
        :class="{ active: targetType === t.key }"
        @click="onSwitchType(t.key)"
      >
        {{ t.label }}
      </button>
    </section>
    <section class="form">
      <div v-if="!editId" class="row">
        <span class="row__label">对象 ID</span>
        <input v-model.number="targetId" type="number" class="input" />
      </div>
      <div class="dim-list">
        <div v-for="d in dims" :key="d.key" class="dim">
          <span class="dim__label">{{ d.label }}</span>
          <StarRating v-model="scores[d.key]" />
          <span class="dim__num">{{ scores[d.key] }}/5</span>
        </div>
      </div>
      <textarea
        v-model="text"
        class="text"
        rows="6"
        placeholder="说说你的真实体验，至少 5 个字。诚实 + 具体 = 帮到下一个人。"
      />
      <div class="attach">
        <div v-for="(a, i) in attachments" :key="i" class="attach__item">
          <span class="attach__icon">{{ a.placeholder }}</span>
          <button class="attach__del" @click="removeAttachment(i)">×</button>
        </div>
        <button v-if="attachments.length < 9" class="attach__add" @click="addAttachment('image')">+ 图</button>
        <button v-if="attachments.length < 9" class="attach__add" @click="addAttachment('video')">+ 频</button>
      </div>
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '提交中…' : (editId ? '保存修改' : '发布评价') }}
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
.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
}
.tab {
  flex: 1;
  border: none;
  background: none;
  padding: 12px;
  font-size: 14px;
  color: var(--bd-text-secondary);
  cursor: pointer;
  &.active {
    color: var(--bd-primary);
    font-weight: 600;
    border-bottom: 2px solid var(--bd-primary);
  }
}
.form {
  padding: 16px;
  background: #fff;
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  &__label {
    width: 64px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
}
.input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}
.dim-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0 16px;
  border-bottom: 1px dashed var(--bd-border);
}
.dim {
  display: flex;
  align-items: center;
  gap: 10px;
  &__label {
    width: 84px;
    font-size: 13px;
  }
  &__num {
    margin-left: auto;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.text {
  width: 100%;
  margin-top: 16px;
  padding: 12px;
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
.attach {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  &__item {
    width: 64px;
    height: 64px;
    border-radius: 10px;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    position: relative;
  }
  &__del {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    font-size: 12px;
    cursor: pointer;
  }
  &__add {
    width: 64px;
    height: 64px;
    border: 1px dashed var(--bd-border);
    background: #fafafa;
    border-radius: 10px;
    color: var(--bd-text-secondary);
    font-size: 12px;
    cursor: pointer;
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
