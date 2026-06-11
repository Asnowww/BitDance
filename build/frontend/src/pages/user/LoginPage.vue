<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast, showToast } from 'vant';
import { KeyRound, Lock, LockKeyhole, MessageSquareCode, Smartphone } from 'lucide-vue-next';
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
const newPassword = ref('');
const confirmPassword = ref('');
const needsPasswordSetup = ref(false);
const cooldown = ref(0);
const sendingCode = ref(false);
const smsError = ref('');
const loginError = ref('');
const submitting = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;
const WECHAT_STATE_KEY = 'bitdance_wechat_state';
const WECHAT_REDIRECT_KEY = 'bitdance_wechat_redirect';

const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value));
const canSendCode = computed(() => isPhoneValid.value && cooldown.value === 0 && !sendingCode.value);
const canSetPassword = computed(
  () => newPassword.value.length >= 6 && newPassword.value.length <= 32 && newPassword.value === confirmPassword.value
);
const canSubmit = computed(
  () =>
    !submitting.value &&
    (needsPasswordSetup.value
      ? canSetPassword.value
      : isPhoneValid.value && (mode.value === 'code' ? code.value.length >= 4 : password.value.length >= 6))
);
const submitText = computed(() => {
  if (submitting.value) return needsPasswordSetup.value ? '设置中...' : '登录中...';
  if (needsPasswordSetup.value) return '完成设置并进入';
  return mode.value === 'code' ? '登录 / 注册' : '登录';
});
const canQuickEnter = import.meta.env.DEV;

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
  smsError.value = '';
  if (!canSendCode.value) {
    if (!isPhoneValid.value) showFailToast('请输入正确的手机号');
    return;
  }
  sendingCode.value = true;
  try {
    await userStore.sendSmsCode(phone.value);
    showSuccessToast('验证码已发送');
    startCooldown();
    sendingCode.value = false;
  } catch (error) {
    smsError.value = getErrorMessage(error);
    sendingCode.value = false;
    /* request 拦截器已弹错误 toast */
  }
};

const getErrorMessage = (error: unknown) => {
  const err = error as {
    message?: string;
    response?: { data?: { message?: string } };
  };
  return err?.response?.data?.message || err?.message || '验证码发送失败';
};

const enterPasswordSetup = () => {
  password.value = '';
  newPassword.value = '';
  confirmPassword.value = '';
  needsPasswordSetup.value = true;
};

const onSubmit = async () => {
  loginError.value = '';
  if (needsPasswordSetup.value) {
    await onSetPassword();
    return;
  }
  if (!canSubmit.value) {
    showFailToast(mode.value === 'code' ? '请输入手机号与验证码' : '请输入手机号与密码');
    return;
  }
  submitting.value = true;
  try {
    if (mode.value === 'password') {
      await userStore.loginWithPassword(phone.value, password.value);
    } else {
      const result = await userStore.login(phone.value, code.value);
      if (result.passwordRequired) {
        enterPasswordSetup();
        showToast('验证码通过，请设置登录密码');
        return;
      }
    }
    showSuccessToast('登录成功');
    const redirect = (route.query.redirect as string) || '/home';
    router.replace(redirect);
  } catch (error) {
    loginError.value = getErrorMessage(error);
    /* request 拦截器已弹错误 toast */
  } finally {
    submitting.value = false;
  }
};

const onSetPassword = async () => {
  if (newPassword.value.length < 6 || newPassword.value.length > 32) {
    showFailToast('密码长度需为 6-32 位');
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    showFailToast('两次输入的密码不一致');
    return;
  }
  submitting.value = true;
  try {
    await userStore.setPassword(newPassword.value);
    showSuccessToast('密码设置成功');
    const redirect = (route.query.redirect as string) || sessionStorage.getItem(WECHAT_REDIRECT_KEY) || '/home';
    sessionStorage.removeItem(WECHAT_STATE_KEY);
    sessionStorage.removeItem(WECHAT_REDIRECT_KEY);
    router.replace(redirect);
  } catch (error) {
    loginError.value = getErrorMessage(error);
  } finally {
    submitting.value = false;
  }
};

const onQuickEnter = async () => {
  submitting.value = true;
  loginError.value = '';
  try {
    await userStore.login('13800138000', '123456');
    showSuccessToast('已进入开发测试模式');
    const redirect = (route.query.redirect as string) || '/home';
    router.replace(redirect);
  } catch (error) {
    try {
      userStore.logout();
      localStorage.setItem('bitdance_token', 'dev-bypass-token');
      localStorage.setItem(
        'bitdance_profile',
        JSON.stringify({
          id: 999,
          phone: '13800000000',
          nickname: '舞者0000',
          avatar: null,
          roles: ['USER', 'COACH']
        })
      );
      showSuccessToast('已进入开发测试模式');
      const redirect = (route.query.redirect as string) || '/home';
      window.location.replace(`${window.location.origin}${window.location.pathname}#${redirect}`);
    } catch {
      loginError.value = getErrorMessage(error);
    }
  } finally {
    submitting.value = false;
  }
};

const onWechat = async () => {
  submitting.value = true;
  try {
    const redirect = (route.query.redirect as string) || '/home';
    const state = createWechatState();
    sessionStorage.setItem(WECHAT_STATE_KEY, state);
    sessionStorage.setItem(WECHAT_REDIRECT_KEY, redirect);
    const url = await userStore.getWechatAuthorizeUrl(state);
    window.location.href = url;
  } catch (error) {
    loginError.value = getErrorMessage(error);
    showToast('微信授权登录暂不可用，请使用手机号登录');
  } finally {
    submitting.value = false;
  }
};

const createWechatState = () => {
  const bytes = new Uint8Array(16);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
};

const consumeWechatCallback = async () => {
  const wechatCode = route.query.wechatCode as string | undefined;
  if (!wechatCode) return;

  const expectedState = sessionStorage.getItem(WECHAT_STATE_KEY);
  const actualState = (route.query.wechatState as string | undefined) ?? '';
  if (expectedState && actualState !== expectedState) {
    showFailToast('微信授权状态校验失败，请重试');
    return;
  }

  submitting.value = true;
  try {
    const result = await userStore.loginWithWechat(wechatCode);
    if (result.passwordRequired) {
      enterPasswordSetup();
      showToast('微信授权成功，请设置登录密码');
      return;
    }
    showSuccessToast('微信授权登录成功');
    const redirect = sessionStorage.getItem(WECHAT_REDIRECT_KEY) || '/home';
    sessionStorage.removeItem(WECHAT_STATE_KEY);
    sessionStorage.removeItem(WECHAT_REDIRECT_KEY);
    router.replace(redirect);
  } catch (error) {
    loginError.value = getErrorMessage(error);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  if ((userStore.passwordRequired || route.query.setupPassword === '1') && userStore.token) {
    enterPasswordSetup();
  }
  consumeWechatCallback();
});

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
      <div v-if="!needsPasswordSetup" class="mode-seg">
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

      <h1 class="login__title">
        {{ needsPasswordSetup ? '设置登录密码' : mode === 'code' ? '手机号验证码登录' : '手机号密码登录' }}
      </h1>

      <p v-if="needsPasswordSetup" class="verified-phone">已验证 {{ userStore.profile?.phone || phone }}，请设置密码完成注册。</p>

      <div v-if="!needsPasswordSetup" class="field">
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
          {{ sendingCode ? '发送中...' : cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
        </button>
      </div>

      <p v-if="smsError" class="sms-error">{{ smsError }}</p>

      <template v-if="needsPasswordSetup">
        <div class="field">
          <Lock class="field__icon" :size="18" :stroke-width="2" />
          <input
            v-model="newPassword"
            class="field__input"
            type="password"
            maxlength="32"
            placeholder="设置 6-32 位密码"
          />
        </div>
        <div class="field">
          <Lock class="field__icon" :size="18" :stroke-width="2" />
          <input
            v-model="confirmPassword"
            class="field__input"
            type="password"
            maxlength="32"
            placeholder="再次输入密码"
          />
        </div>
      </template>

      <div v-else-if="mode === 'code'" class="field">
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
        {{ submitText }}
      </button>
      <p v-if="loginError" class="sms-error">{{ loginError }}</p>
      <button
        v-if="!needsPasswordSetup && canQuickEnter"
        class="btn btn--soft"
        type="button"
        :disabled="submitting"
        @click="onQuickEnter"
      >
        直接进入测试
      </button>
      <button v-if="!needsPasswordSetup" class="btn btn--soft" type="button" @click="onWechat">微信授权登录</button>
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

.sms-error {
  margin: -8px 4px 0;
  color: #c02626;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.verified-phone {
  margin: -6px 4px 0;
  color: var(--nike-mute);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.45;
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
