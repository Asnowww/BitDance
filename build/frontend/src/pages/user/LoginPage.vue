<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showFailToast, showSuccessToast, showToast } from 'vant';
import { Smartphone, KeyRound, Lock, LockKeyhole, MessageSquareCode } from 'lucide-vue-next';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

type Mode = 'code' | 'password';
const mode = ref<Mode>('code');
const methods = [
  { key: 'password' as Mode, label: '密码登录', icon: LockKeyhole },
  { key: 'code' as Mode, label: '验证码登录', icon: MessageSquareCode }
];

const phone = ref('');
const code = ref('');
const password = ref('');
const cooldown = ref(0);
const submitting = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;

const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value));
const canSendCode = computed(() => isPhoneValid.value && cooldown.value === 0);
const canSubmit = computed(
  () =>
    isPhoneValid.value &&
    !submitting.value &&
    (mode.value === 'code' ? code.value.length >= 4 : password.value.length >= 6)
);

const heroImage =
  'https://images.unsplash.com/photo-1761882628233-1e23102da76d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w4NDM0ODN8MHwxfHJhbmRvbXx8fHx8fHx8fDE3Nzk3ODEzMzV8&ixlib=rb-4.1.0&q=80&w=1080';

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
  if (!canSendCode.value) {
    if (!isPhoneValid.value) showFailToast('请输入正确的手机号');
    return;
  }
  try {
    await userStore.sendSmsCode(phone.value);
    showSuccessToast('验证码已发送');
    startCooldown();
  } catch {
    /* request 拦截器已弹错误 toast */
  }
};

const onSubmit = async () => {
  if (!canSubmit.value) {
    showFailToast(mode.value === 'code' ? '请输入手机号与验证码' : '请输入手机号与密码');
    return;
  }
  submitting.value = true;
  try {
    if (mode.value === 'password') {
      // 密码登录：待后端 /auth/login/password 接口接入
      showToast('密码登录待后端接口接入，请先用验证码登录');
      return;
    }
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

const onWechat = () => showToast('请在微信客户端中授权登录');

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<template>
  <div class="login">
    <section class="hero" :style="{ backgroundImage: `url(${heroImage})` }">
      <div class="hero__overlay">
        <strong class="hero__title">MOVE<br />WITH<br />BITDANCE</strong>
      </div>
    </section>

    <main class="login__form">
      <div class="mode-seg">
        <button
          v-for="m in methods"
          :key="m.key"
          class="mode-seg__btn"
          :class="{ 'mode-seg__btn--active': mode === m.key }"
          type="button"
          @click="mode = m.key"
        >
          <component :is="m.icon" :size="22" :stroke-width="2" />
          <span>{{ m.label }}</span>
        </button>
      </div>

      <h1 class="login__title">{{ mode === 'code' ? '手机号验证码登录' : '手机号密码登录' }}</h1>

      <div class="field">
        <Smartphone class="field__icon" :size="18" :stroke-width="2" />
        <input
          v-model="phone"
          class="field__input"
          type="tel"
          inputmode="numeric"
          maxlength="11"
          placeholder="输入手机号"
        />
        <button
          v-if="mode === 'code'"
          class="field__action"
          type="button"
          :disabled="!canSendCode"
          @click="onSendCode"
        >
          {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
        </button>
      </div>

      <div v-if="mode === 'code'" class="field">
        <KeyRound class="field__icon" :size="18" :stroke-width="2" />
        <input
          v-model="code"
          class="field__input"
          type="text"
          inputmode="numeric"
          maxlength="6"
          placeholder="输入验证码"
        />
      </div>
      <div v-else class="field">
        <Lock class="field__icon" :size="18" :stroke-width="2" />
        <input
          v-model="password"
          class="field__input"
          type="password"
          maxlength="32"
          placeholder="输入密码"
        />
      </div>

      <button class="btn btn--dark" type="button" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '登录中…' : mode === 'code' ? '登录 / 注册' : '登录' }}
      </button>
      <button class="btn btn--soft" type="button" @click="onWechat">微信授权登录</button>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.login {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-hairline-soft: #e5e5e5;

  min-height: 100vh;
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.hero {
  height: 300px;
  background-color: var(--nike-ink);
  background-size: cover;
  background-position: center;
  overflow: hidden;

  &__overlay {
    height: 100%;
    padding: 18px;
    background: rgba(17, 17, 17, 0.2);
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
  }

  &__title {
    color: #fff;
    font-size: 34px;
    font-weight: 900;
    line-height: 1.25;
    letter-spacing: 0;
  }
}

.login__form {
  padding: 24px 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.mode-seg {
  display: flex;
  gap: 8px;
}

.mode-seg__btn {
  flex: 1;
  height: 64px;
  border: 0;
  border-radius: 16px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
  cursor: pointer;

  &--active {
    background: var(--nike-ink);
    color: #fff;
  }
}

.login__title {
  margin: 0;
  font-size: 24px;
  font-weight: 900;
  line-height: 1.25;
}

.field {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 48px;
  padding: 0 16px;
  border-radius: 24px;
  background: var(--nike-soft-cloud);
  color: var(--nike-mute);

  &__icon {
    flex: none;
    color: var(--nike-mute);
  }

  &__input {
    flex: 1;
    min-width: 0;
    border: 0;
    background: transparent;
    font-size: 15px;
    font-weight: 500;
    color: var(--nike-ink);
    outline: none;

    &::placeholder {
      color: var(--nike-mute);
      font-weight: 500;
    }
  }

  &__action {
    flex: none;
    height: 36px;
    padding: 8px 14px;
    border: 0;
    border-radius: 999px;
    background: var(--nike-ink);
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.25;
    cursor: pointer;

    &:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }
  }
}

.btn {
  height: 48px;
  border: 0;
  border-radius: 999px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.25;
  cursor: pointer;

  &--dark {
    background: var(--nike-ink);
    color: #fff;

    &:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }
  }

  &--soft {
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
  }
}
</style>
