<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const phone = ref('');
const code = ref('');
const cooldown = ref(0);
const submitting = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;

const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value));
const canSendCode = computed(() => isPhoneValid.value && cooldown.value === 0);
const canSubmit = computed(() => isPhoneValid.value && code.value.length >= 4 && !submitting.value);

const startCooldown = () => {
  cooldown.value = 60;
  timer = setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
};

const onSendCode = async () => {
  if (!canSendCode.value) return;
  try {
    await userStore.sendSmsCode(phone.value);
    showSuccessToast('验证码已发送');
    startCooldown();
  } catch {
    /* request 拦截器已弹错误 toast */
  }
};

const onSubmit = async () => {
  if (!canSubmit.value) return;
  submitting.value = true;
  try {
    await userStore.login(phone.value, code.value);
    showSuccessToast('登录成功');
    const redirect = (route.query.redirect as string) || '/home';
    router.replace(redirect);
  } catch {
    /* toast 已弹 */
  } finally {
    submitting.value = false;
  }
};

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <div class="login">
    <header class="login__header">
      <div class="login__brand">BitDance</div>
      <div class="login__sub">用手机号登录，开始记录你的舞蹈学习之旅</div>
    </header>
    <section class="login__form">
      <div class="field">
        <label class="field__label">手机号</label>
        <input
          v-model="phone"
          class="field__input"
          type="tel"
          inputmode="numeric"
          maxlength="11"
          placeholder="请输入手机号"
        />
      </div>
      <div class="field">
        <label class="field__label">验证码</label>
        <div class="field__row">
          <input
            v-model="code"
            class="field__input"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="请输入验证码"
          />
          <button class="code-btn" :disabled="!canSendCode" @click="onSendCode">
            {{ cooldown > 0 ? `${cooldown}s 后重发` : '获取验证码' }}
          </button>
        </div>
      </div>
      <button class="submit" :disabled="!canSubmit" @click="onSubmit">
        {{ submitting ? '登录中…' : '登录' }}
      </button>
      <p class="login__tip">
        登录即代表同意《用户协议》与《隐私政策》。开发期默认使用 mock 接口，任意 6 位数字即可登录。
      </p>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.login {
  min-height: 100vh;
  padding: 64px 28px 32px;
  background: linear-gradient(180deg, #fff7f8 0%, var(--bd-bg) 60%);
  &__header {
    margin-bottom: 36px;
  }
  &__brand {
    font-size: 32px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__sub {
    margin-top: 8px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
  &__form {
    background: var(--bd-surface);
    border-radius: var(--bd-radius-lg);
    padding: 20px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  }
  &__tip {
    margin-top: 16px;
    font-size: 11px;
    color: var(--bd-text-secondary);
    line-height: 1.6;
  }
}
.field {
  & + & {
    margin-top: 16px;
  }
  &__label {
    display: block;
    font-size: 12px;
    color: var(--bd-text-secondary);
    margin-bottom: 6px;
  }
  &__input {
    width: 100%;
    height: 44px;
    padding: 0 12px;
    border: 1px solid var(--bd-border);
    border-radius: 10px;
    background: #fafafa;
    font-size: 15px;
    outline: none;
    &:focus {
      border-color: var(--bd-primary);
      background: #fff;
    }
  }
  &__row {
    display: flex;
    gap: 10px;
    .field__input {
      flex: 1;
    }
  }
}
.code-btn {
  height: 44px;
  padding: 0 14px;
  border: 1px solid var(--bd-primary);
  background: rgba(255, 36, 66, 0.06);
  color: var(--bd-primary);
  border-radius: 10px;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  &:disabled {
    border-color: var(--bd-border);
    background: #f5f5f5;
    color: var(--bd-text-secondary);
    cursor: not-allowed;
  }
}
.submit {
  margin-top: 24px;
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--bd-primary), var(--bd-primary-dark));
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.18s;
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
