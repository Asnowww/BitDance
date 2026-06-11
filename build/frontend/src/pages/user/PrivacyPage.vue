<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { showSuccessToast, showToast } from 'vant';
import { ShieldCheck } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenFieldRow from '@/components/pen/PenFieldRow.vue';
import { fetchLoginDevices, trustLoginDevice } from '@/api/security';
import type { LoginDevice } from '@/api/security';
import { useUserStore } from '@/stores/user';
import type { PrivacySettings } from '@/api/profile';

const user = useUserStore();
const loading = ref(false);
const saving = ref(false);
const deviceLoading = ref(false);
const devices = ref<LoginDevice[]>([]);

const form = reactive<PrivacySettings>({
  profileVisibility: 'public',
  growthVisibility: 'public',
  practiceVisibility: 'public',
  contentVisibility: 'public'
});

const visibilityOptions = [
  { value: 'public', label: '公开' },
  { value: 'followers', label: '仅关注者' },
  { value: 'private', label: '仅自己' }
];

const fields: Array<{ key: keyof PrivacySettings; label: string }> = [
  { key: 'profileVisibility', label: '个人资料' },
  { key: 'growthVisibility', label: '训练打卡' },
  { key: 'practiceVisibility', label: '约练动态' },
  { key: 'contentVisibility', label: '社区动态' }
];

const account = computed(() => [
  { label: '绑定手机', value: user.profile?.phone?.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') ?? '未登录' },
  { label: '当前角色', value: (user.profile?.roles ?? []).join(' / ') || 'USER' },
  { label: '认证方式', value: '手机号 / 密码 / 微信授权' }
]);

const labelOf = (value: string) =>
  visibilityOptions.find((item) => item.value === value)?.label ?? value;

const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '暂无记录');

const loadDevices = async () => {
  deviceLoading.value = true;
  try {
    devices.value = await fetchLoginDevices();
  } finally {
    deviceLoading.value = false;
  }
};

const loadPrivacy = async () => {
  loading.value = true;
  try {
    const data = await user.refreshProfile();
    Object.assign(form, data?.privacy ?? user.privacy);
    await loadDevices();
  } finally {
    loading.value = false;
  }
};

const cycleVisibility = (key: keyof PrivacySettings) => {
  const index = visibilityOptions.findIndex((item) => item.value === form[key]);
  form[key] = visibilityOptions[(index + 1) % visibilityOptions.length].value;
};

const save = async () => {
  saving.value = true;
  try {
    await user.updatePrivacy({ ...form });
    showSuccessToast('隐私设置已保存');
  } finally {
    saving.value = false;
  }
};

const trustDevice = async (device: LoginDevice) => {
  const updated = await trustLoginDevice(device.id);
  devices.value = devices.value.map((item) => (item.id === updated.id ? updated : item));
  showSuccessToast('已设为可信设备');
};

onMounted(loadPrivacy);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="隐私设置" :show-share="false" />

    <section class="pen-scroll">
      <h2 class="block-title">内容可见范围</h2>
      <p v-if="loading" class="hint">正在读取后端隐私设置...</p>
      <PenFieldRow
        v-for="field in fields"
        :key="field.key"
        :label="field.label"
        :value="labelOf(form[field.key])"
        @click="cycleVisibility(field.key)"
      />

      <button class="save-btn" type="button" :disabled="saving" @click="save">
        {{ saving ? '保存中...' : '保存隐私设置' }}
      </button>

      <h2 class="block-title">账号安全</h2>
      <PenFieldRow
        v-for="a in account"
        :key="a.label"
        :label="a.label"
        :value="a.value"
        @click="showToast(a.value)"
      />

      <section class="device-section" aria-label="登录设备">
        <div class="device-section__head">
          <h2>登录设备</h2>
          <button type="button" @click="loadDevices">{{ deviceLoading ? '刷新中' : '刷新' }}</button>
        </div>
        <article v-for="device in devices" :key="device.id" class="device-card">
          <div class="device-card__main">
            <strong>{{ device.deviceName }}</strong>
            <span>{{ device.platform }} · {{ device.ipAddress || '未知 IP' }}</span>
            <em>最近登录 {{ formatTime(device.lastLoginAt) }}</em>
          </div>
          <div class="device-card__state">
            <span v-if="device.isCurrent" class="pill pill--active">当前</span>
            <span class="pill" :class="{ 'pill--active': device.isTrusted }">
              {{ device.isTrusted ? '可信' : '未信任' }}
            </span>
            <button v-if="!device.isTrusted" type="button" @click="trustDevice(device)">
              <ShieldCheck :size="16" />
              <span>设为可信</span>
            </button>
          </div>
        </article>
        <p v-if="!deviceLoading && !devices.length" class="hint">暂无登录设备记录</p>
      </section>
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
  gap: 4px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.block-title {
  @include pen-h3-section;
  margin: 12px 0 4px;
}

.save-btn {
  width: 100%;
  margin: 12px 0;
  @include pen-primary-btn;
}

.hint {
  margin: 4px 0 10px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.5;
}

.device-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      flex: 1;
      margin: 0;
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    button {
      height: 34px;
      padding: 0 14px;
      border: 1px solid $pen-hairline;
      border-radius: 999px;
      background: $pen-canvas;
      color: $pen-ink;
      font-size: 12px;
      font-weight: 900;
      cursor: pointer;
    }
  }
}

.device-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;

  &__main {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;

    strong,
    span,
    em {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    strong {
      font-size: 15px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span,
    em {
      color: $pen-mute;
      font-size: 12px;
      font-style: normal;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }

  &__state {
    display: flex;
    align-items: flex-end;
    flex-direction: column;
    gap: 6px;

    button {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      height: 32px;
      padding: 0 10px;
      border: 0;
      border-radius: 999px;
      background: $pen-ink;
      color: $pen-on-primary;
      font-size: 12px;
      font-weight: 900;
      cursor: pointer;
    }
  }
}

.pill {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 4px 9px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  color: $pen-mute;
  font-size: 11px;
  font-weight: 900;
  line-height: $pen-lh;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}
</style>
