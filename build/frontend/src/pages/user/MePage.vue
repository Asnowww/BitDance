<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  CalendarDays,
  Heart,
  ImageIcon,
  LogOut,
  MessageSquareText,
  PackageCheck,
  RefreshCw,
  Shield,
  Star,
  Target,
  Users,
  UserRound
} from 'lucide-vue-next';
import { showConfirmDialog, showSuccessToast, showToast } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

const profileName = computed(() => user.profile?.nickname || 'BitDance 用户');
const profileMeta = computed(() => {
  const level = user.detail?.currentLevel || '未设置水平';
  const goal = user.detail?.learningGoal || '未设置目标';
  return `${(user.profile?.roles ?? ['USER']).join(' / ')} · ${level} · ${goal}`;
});

const quickActions = [
  { label: '约练', icon: Users, path: '/me/practices' },
  { label: '作品', icon: ImageIcon, path: '/me/works' },
  { label: '目标', icon: Target, path: '/me/goal' },
  { label: '订单', icon: PackageCheck, path: '/me/workshop-orders' },
  { label: '预约', icon: CalendarDays, path: '/me/trials' },
  { label: '评价', icon: Star, path: '/me/reviews' },
  { label: '消息', icon: Bell, path: '/messages' },
  { label: '动态', icon: MessageSquareText, path: '/me/home?tab=posts' },
  { label: '收藏', icon: Heart, path: '/favorites' },
  { label: '隐私', icon: Shield, path: '/me/privacy' }
];

const workbench = computed(() => [
  { title: '教练主页运营', status: user.isCoach ? '已开通' : '未认证', path: '/me/coach-home', enabled: user.isCoach },
  { title: '舞室管理员入口', status: user.isStudioAdmin ? '已开通' : '未认证', path: '/coach/dashboard', enabled: user.isStudioAdmin },
  { title: '平台举报后台', status: user.isPlatformAdmin ? '已开通' : '未开通', path: '/admin/reports', enabled: user.isPlatformAdmin }
]);

const goProfileHome = () => {
  router.push('/me/home');
};

const switchRole = () => {
  const next = user.activeRole === 'coach' ? 'user' : 'coach';
  const ok = user.switchRole(next);
  if (!ok) {
    showToast('当前账号没有教练角色，请先完成教练认证');
    return;
  }
  showToast(next === 'coach' ? '已切换为教练视角' : '已切换为用户视角');
};

const goWorkbench = (item: { path: string; enabled: boolean }) => {
  if (!item.enabled || !item.path) {
    showToast('该角色尚未开通');
    return;
  }
  router.push(item.path);
};

const switchAccount = async () => {
  try {
    await showConfirmDialog({
      title: '切换账号',
      message: '将退出当前账号并返回登录页。',
      confirmButtonText: '切换',
      cancelButtonText: '取消'
    });
    user.logout();
    router.replace({ path: '/login', query: { redirect: '/me' } });
  } catch {
    // User canceled.
  }
};

const logout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '退出后仍可浏览公开内容，需要账号的功能会要求重新登录。',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    });
    user.logout();
    showSuccessToast('已退出登录');
    router.replace('/home');
  } catch {
    // User canceled.
  }
};

onMounted(() => {
  user.refreshProfile();
});
</script>

<template>
  <main class="me-page">
    <header class="me-topbar">
      <div>
        <h1>我的</h1>
        <p>账号、消息、角色工作台</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息" @click="router.push('/messages')">
        <Bell :size="20" :stroke-width="2" />
      </button>
    </header>

    <section class="me-scroll">
      <section class="profile-card" aria-label="账号资料">
        <button class="profile-card__avatar" type="button" aria-label="进入我的个人主页" @click="goProfileHome">
          <UserRound :size="34" :stroke-width="2.2" />
        </button>
        <span class="profile-card__copy">
          <strong>{{ profileName }}</strong>
          <em>{{ profileMeta }}</em>
        </span>
        <button class="profile-card__switch" type="button" @click="switchRole">
          {{ user.activeRole === 'coach' ? '用户视角' : '教练视角' }}
        </button>
      </section>

      <section class="quick-grid" aria-label="快捷入口">
        <button
          v-for="item in quickActions"
          :key="item.label"
          class="quick-grid__item"
          type="button"
          @click="router.push(item.path)"
        >
          <component :is="item.icon" :size="22" :stroke-width="2" />
          <span>{{ item.label }}</span>
        </button>
      </section>

      <section class="account-actions" aria-label="账号操作">
        <button class="account-action" type="button" @click="switchAccount">
          <RefreshCw :size="20" :stroke-width="2" />
          <span>切换账号</span>
        </button>
        <button class="account-action account-action--danger" type="button" @click="logout">
          <LogOut :size="20" :stroke-width="2" />
          <span>退出登录</span>
        </button>
      </section>

      <section class="workbench" aria-labelledby="workbench-title">
        <div class="section-title">
          <h2 id="workbench-title">角色工作台</h2>
          <span>来自后端 roles</span>
        </div>

        <button
          v-for="item in workbench"
          :key="item.title"
          class="workbench-row"
          type="button"
          @click="goWorkbench(item)"
        >
          <span>{{ item.title }}</span>
          <em>{{ item.status }} &gt;</em>
        </button>
      </section>
    </section>
  </main>
</template>

<style lang="scss" scoped>
.me-page {
  --ink: #111111;
  --canvas: #ffffff;
  --soft: #f6f6f6;
  --muted: #707072;
  --line: #e5e5e5;

  min-height: 100%;
  background: var(--canvas);
  color: var(--ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.me-topbar {
  height: 92px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid var(--line);
  background: var(--canvas);
  display: flex;
  align-items: center;
  gap: 12px;
  box-sizing: border-box;

  div {
    min-width: 0;
    flex: 1;
  }

  h1,
  p {
    margin: 0;
    letter-spacing: 0;
  }

  h1 {
    font-size: 24px;
    line-height: 1.15;
    font-weight: 900;
  }

  p {
    margin-top: 4px;
    color: var(--muted);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 700;
  }
}

.icon-button {
  width: 42px;
  height: 42px;
  flex: none;
  border: 0;
  border-radius: 999px;
  background: var(--soft);
  color: var(--ink);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.me-scroll {
  padding: 14px 14px calc(24px + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-card {
  width: 100%;
  min-height: 96px;
  padding: 16px;
  background: var(--soft);
  color: var(--ink);
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  text-align: left;
  box-sizing: border-box;

  &__avatar {
    width: 58px;
    height: 58px;
    border-radius: 999px;
    background: var(--ink);
    color: var(--canvas);
    display: grid;
    place-items: center;
    border: 0;
    cursor: pointer;
  }

  &__copy {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 5px;

    strong {
      font-size: 18px;
      line-height: 1.2;
      font-weight: 900;
      letter-spacing: 0;
    }

    em {
      color: var(--muted);
      font-size: 12px;
      line-height: 1.35;
      font-style: normal;
      font-weight: 700;
      letter-spacing: 0;
    }
  }

  &__switch {
    min-height: 36px;
    padding: 9px 14px;
    border: 1px solid var(--line);
    border-radius: 999px;
    background: var(--canvas);
    font-size: 12px;
    line-height: 1.2;
    font-weight: 900;
    white-space: nowrap;
    box-sizing: border-box;
    cursor: pointer;
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
}

.quick-grid__item {
  min-height: 74px;
  border: 0;
  background: var(--soft);
  color: var(--ink);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;

  span {
    font-size: 12px;
    line-height: 1.2;
    font-weight: 900;
  }
}

.account-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.account-action {
  min-height: 52px;
  padding: 0 14px;
  border: 1px solid var(--line);
  background: var(--canvas);
  color: var(--ink);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  line-height: 1.2;
  font-weight: 900;
  cursor: pointer;
  box-sizing: border-box;

  span {
    min-width: 0;
  }

  &--danger {
    color: #b42318;
  }
}

.workbench {
  display: flex;
  flex-direction: column;
}

.section-title {
  min-height: 46px;
  display: flex;
  align-items: center;
  gap: 12px;

  h2 {
    flex: 1;
    margin: 0;
    font-size: 22px;
    line-height: 1.2;
    font-weight: 900;
    letter-spacing: 0;
  }

  span {
    color: var(--muted);
    font-size: 12px;
    line-height: 1.2;
    font-weight: 700;
  }
}

.workbench-row {
  min-height: 58px;
  padding: 0;
  border: 0;
  border-bottom: 1px solid var(--line);
  background: var(--canvas);
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  cursor: pointer;

  span {
    min-width: 0;
    flex: 1;
    font-size: 14px;
    line-height: 1.25;
    font-weight: 900;
  }

  em {
    color: var(--muted);
    font-size: 13px;
    line-height: 1.25;
    font-style: normal;
    font-weight: 700;
    white-space: nowrap;
  }
}

@media (max-width: 360px) {
  .profile-card {
    grid-template-columns: 54px minmax(0, 1fr);

    &__switch {
      grid-column: 2;
      justify-self: start;
    }
  }
}
</style>
