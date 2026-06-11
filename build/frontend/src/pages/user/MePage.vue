<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  CalendarDays,
  Heart,
  LogOut,
  PackageCheck,
  Shield,
  Star,
  UserRound
} from 'lucide-vue-next';
import { showConfirmDialog, showToast } from 'vant';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const user = useUserStore();

const profileName = computed(() => user.profile?.nickname || '顾同学');
const profileMeta = computed(() => '普通用户 · Jazz 初级 · 连续打卡 12 天');

const quickActions = [
  { label: '课程订单', icon: PackageCheck, path: '/me/course-orders' },
  { label: '活动订单', icon: PackageCheck, path: '/me/workshop-orders' },
  { label: '预约', icon: CalendarDays, path: '/me/trials' },
  { label: '评价', icon: Star, path: '/me/reviews' },
  { label: '消息', icon: Bell, path: '/messages' },
  { label: '收藏', icon: Heart, path: '/favorites' }
];

const workbench = [
  { title: '运营工作台', status: '商家 / 教练 / 平台', path: '/coach/dashboard' },
  { title: '舞室入驻 / 认领', status: '开通商家后台', path: '/coach/studio-claim' },
  { title: '教练资质申请', status: '成为认证教练', path: '/coach/certification' }
];

const goProfileHome = () => {
  router.push('/me/home');
};

const switchRole = () => {
  const next = user.activeRole === 'coach' ? 'user' : 'coach';
  user.switchRole(next);
  showToast(next === 'coach' ? '已切换为教练视角' : '已切换为用户视角');
};

const goWorkbench = (path: string) => {
  if (!path) {
    showToast('管理员入口待开放');
    return;
  }
  router.push(path);
};

const onLogout = async () => {
  await showConfirmDialog({ title: '退出登录', message: '确认退出当前账号?' });
  user.logout();
  localStorage.removeItem('bitdance_ops_role');
  localStorage.removeItem('bitdance_ops_studio');
  showToast('已退出登录');
  router.replace('/login');
};
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
        <button class="profile-card__switch" type="button" @click="switchRole">切换角色</button>
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

      <section class="workbench" aria-labelledby="workbench-title">
        <div class="section-title">
          <h2 id="workbench-title">角色工作台</h2>
          <span>已隐藏</span>
        </div>

        <button
          v-for="item in workbench"
          :key="item.title"
          class="workbench-row"
          type="button"
          @click="goWorkbench(item.path)"
        >
          <span>{{ item.title }}</span>
          <em>{{ item.status }} &gt;</em>
        </button>
      </section>

      <button class="logout-btn" type="button" @click="onLogout">
        <LogOut :size="18" :stroke-width="2" />
        退出登录
      </button>
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

.logout-btn {
  min-height: 48px;
  margin-top: 10px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--canvas);
  color: #d30005;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
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
