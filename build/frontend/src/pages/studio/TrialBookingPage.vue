<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { createTrialBooking } from '@/api/trial';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const studioId = Number(route.params.id);
const courseId = route.query.courseId ? Number(route.query.courseId) : undefined;
const coachId = route.query.coachId ? Number(route.query.coachId) : undefined;

const today = new Date();
const dateOptions = Array.from({ length: 7 }).map((_, i) => {
  const d = new Date(today);
  d.setDate(today.getDate() + i);
  return d.toISOString().slice(0, 10);
});
const TIME_SLOTS = ['10:00', '14:00', '16:00', '19:00', '20:30'];

const date = ref(dateOptions[0]);
const time = ref(TIME_SLOTS[0]);
const phone = ref(user.profile?.phone ?? '');
const remark = ref('');
const submitting = ref(false);

const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value));
const canSubmit = computed(() => phoneValid.value && !submitting.value);

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast('请检查手机号');
    return;
  }
  submitting.value = true;
  try {
    await createTrialBooking({
      studioId,
      courseId,
      coachId,
      date: date.value,
      time: time.value,
      contactPhone: phone.value,
      remark: remark.value || undefined,
      idempotencyToken: `trial-${studioId}-${Date.now()}`
    });
    showSuccessToast('预约成功，等待舞室确认');
    router.replace('/me/trials');
  } catch {
    /* toast 已弹 */
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">试听预约</span>
    </header>
    <section class="form">
      <div class="group">
        <div class="group__title">选择日期</div>
        <div class="chips">
          <span
            v-for="d in dateOptions"
            :key="d"
            class="chip"
            :class="{ active: date === d }"
            @click="date = d"
            >{{ d.slice(5) }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">选择时段</div>
        <div class="chips">
          <span
            v-for="t in TIME_SLOTS"
            :key="t"
            class="chip"
            :class="{ active: time === t }"
            @click="time = t"
            >{{ t }}</span
          >
        </div>
      </div>
      <div class="group">
        <div class="group__title">联系手机号</div>
        <input v-model="phone" class="input" inputmode="numeric" maxlength="11" placeholder="11 位手机号" />
      </div>
      <div class="group">
        <div class="group__title">备注（选填）</div>
        <textarea
          v-model="remark"
          class="input input--textarea"
          rows="3"
          placeholder="想试听哪位老师 / 哪门课，给舞室一个提示"
        />
      </div>
    </section>
    <footer class="footer">
      <button class="btn" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交预约' }}
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
  padding: 8px 16px 16px;
  background: #fff;
}
.group {
  padding: 12px 0;
  &__title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 8px 14px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 10px;
  background: #fafafa;
  font-size: 14px;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
    background: #fff;
  }
  &--textarea {
    resize: none;
    font-family: inherit;
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
