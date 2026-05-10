<script setup lang="ts">
import { useRouter } from 'vue-router';
import { showConfirmDialog } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

interface MenuItem {
  icon: string;
  label: string;
  to?: string;
  action?: () => void;
}

const sections: Array<{ title: string; items: MenuItem[] }> = [
  {
    title: '舞蹈',
    items: [
      { icon: '🎟', label: '我的试听', to: '/me/trials' },
      { icon: '✍️', label: '我的评价', to: '/me/reviews' },
      { icon: '⭐', label: '我的收藏', to: '/favorites' },
      { icon: '🤝', label: '我的约练', to: '/me/practices' }
    ]
  },
  {
    title: '账号',
    items: [
      { icon: '👤', label: '资料与偏好', to: '/me/profile' },
      { icon: '🔒', label: '隐私设置', to: '/me/privacy' },
      { icon: '💬', label: '消息中心', to: '/messages' }
    ]
  }
];

const onLogout = async () => {
  await showConfirmDialog({ title: '退出登录？' }).catch(() => {
    throw new Error('cancel');
  });
  user.logout();
  router.replace('/home');
};

const onSwitchRole = () => {
  user.switchRole(user.activeRole === 'user' ? 'coach' : 'user');
};
</script>

<template>
  <div class="page">
    <header class="head">
      <div class="avatar">{{ (user.profile?.nickname ?? '?').charAt(0) }}</div>
      <div class="info">
        <div class="info__name">{{ user.profile?.nickname ?? '未登录' }}</div>
        <div class="info__sub">{{ user.profile?.phone ?? '' }}</div>
        <div class="info__roles">
          <span class="role" :class="{ active: user.activeRole === 'user' }" @click="user.switchRole('user')">
            普通用户
          </span>
          <span class="role" :class="{ active: user.activeRole === 'coach' }" @click="onSwitchRole">
            教练 {{ user.isCoach ? '' : '（开通）' }}
          </span>
        </div>
      </div>
    </header>

    <section v-for="sec in sections" :key="sec.title" class="block">
      <div class="block__title">{{ sec.title }}</div>
      <button
        v-for="m in sec.items"
        :key="m.label"
        class="row"
        @click="m.to ? router.push(m.to) : m.action?.()"
      >
        <span class="row__icon">{{ m.icon }}</span>
        <span class="row__label">{{ m.label }}</span>
        <span class="row__more">›</span>
      </button>
    </section>

    <section v-if="user.activeRole === 'coach'" class="block">
      <div class="block__title">教练</div>
      <button class="row" @click="router.push('/me/coach-home')">
        <span class="row__icon">🎤</span>
        <span class="row__label">教练主页</span>
        <span class="row__more">›</span>
      </button>
    </section>

    <button v-if="user.isLogin" class="logout" @click="onLogout">退出登录</button>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding: 0 0 24px;
}
.head {
  background: linear-gradient(180deg, #ffe2e8, #fff);
  padding: 28px 16px 20px;
  display: flex;
  gap: 14px;
  align-items: center;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--bd-primary);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.info {
  flex: 1;
  &__name {
    font-size: 18px;
    font-weight: 700;
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__roles {
    margin-top: 8px;
    display: flex;
    gap: 8px;
  }
}
.role {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 11px;
  background: rgba(0, 0, 0, 0.06);
  color: var(--bd-text-secondary);
  cursor: pointer;
  &.active {
    background: var(--bd-primary);
    color: #fff;
  }
}
.block {
  margin-top: 8px;
  background: #fff;
  &__title {
    padding: 12px 16px 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
}
.row {
  width: 100%;
  background: none;
  border: none;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid var(--bd-border);
  &:last-child {
    border-bottom: none;
  }
  &__icon {
    width: 24px;
    text-align: center;
  }
  &__label {
    flex: 1;
    text-align: left;
    font-size: 14px;
  }
  &__more {
    color: var(--bd-text-secondary);
  }
}
.logout {
  margin: 24px 16px 0;
  width: calc(100% - 32px);
  height: 44px;
  border: 1px solid var(--bd-border);
  background: #fff;
  color: var(--bd-text-secondary);
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
}
</style>
