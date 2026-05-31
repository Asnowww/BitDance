<script setup lang="ts">
import { useRouter } from 'vue-router';
import NikeIcon from '@/components/NikeIcon.vue';

type UserTileIcon = 'bag' | 'calendar-check' | 'star' | 'bell' | 'heart' | 'shield';

interface UserTile {
  label: string;
  icon: UserTileIcon;
  to: string;
}

interface WorkbenchRow {
  label: string;
  status: string;
  to: string;
}

const router = useRouter();

const tiles: UserTile[] = [
  { label: '订单', icon: 'bag', to: '/me/workshop-orders' },
  { label: '预约', icon: 'calendar-check', to: '/me/trials' },
  { label: '评价', icon: 'star', to: '/me/reviews' },
  { label: '消息', icon: 'bell', to: '/messages' },
  { label: '收藏', icon: 'heart', to: '/favorites' },
  { label: '隐私', icon: 'shield', to: '/me/privacy' }
];

const workbenchRows: WorkbenchRow[] = [
  { label: '申请成为教练', status: '待认证', to: '/coach/appeal' },
  { label: '申请舞室管理员', status: '待认证', to: '/me/profile' },
  { label: '平台管理员入口', status: '待认证', to: '/coach/dashboard' }
];
</script>

<template>
  <div class="me-page">
    <header class="me-topbar">
      <div class="me-topbar__copy">
        <h1>我的</h1>
        <p>账号、消息、角色工作台</p>
      </div>
      <button class="icon-button" type="button" aria-label="消息提醒" @click="router.push('/messages')">
        <NikeIcon name="bell" :size="20" />
      </button>
    </header>

    <main class="me-content">
      <section class="profile-card" aria-label="个人资料">
        <div class="profile-card__avatar" />
        <div class="profile-card__copy">
          <h2>顾同学</h2>
          <p>普通用户 · Jazz 初级 · 连续<br />打卡 12 天</p>
        </div>
        <button type="button" @click="router.push('/me/profile')">切换角色</button>
      </section>

      <section class="tile-grid" aria-label="常用功能">
        <button v-for="tile in tiles" :key="tile.label" type="button" @click="router.push(tile.to)">
          <NikeIcon :name="tile.icon" :size="22" :stroke-width="2.4" />
          <span>{{ tile.label }}</span>
        </button>
      </section>

      <section class="workbench" aria-labelledby="workbench-title">
        <div class="workbench__head">
          <h2 id="workbench-title">角色工作台</h2>
          <span>已隐藏</span>
        </div>

        <button
          v-for="row in workbenchRows"
          :key="row.label"
          class="workbench-row"
          type="button"
          @click="router.push(row.to)"
        >
          <span class="workbench-row__label">{{ row.label }}</span>
          <span class="workbench-row__status">{{ row.status }}</span>
          <NikeIcon name="chevron-right" :size="18" />
        </button>
      </section>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.me-page {
  --nike-ink: #111111;
  --nike-canvas: #ffffff;
  --nike-soft-cloud: #f5f5f5;
  --nike-mute: #707072;
  --nike-hairline-soft: #e5e5e5;

  min-height: calc(100vh - 72px - env(safe-area-inset-bottom));
  background: var(--nike-canvas);
  color: var(--nike-ink);
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Arial,
    sans-serif;
}

.me-topbar {
  height: 68px;
  padding: 14px 18px;
  background: var(--nike-canvas);
  border-bottom: 1px solid var(--nike-hairline-soft);
  display: flex;
  align-items: center;
  gap: 12px;

  &__copy {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  h1,
  p {
    margin: 0;
  }

  h1 {
    font-size: 18px;
    line-height: 1.25;
    font-weight: 800;
    letter-spacing: 0;
  }

  p {
    color: var(--nike-mute);
    font-size: 12px;
    line-height: 1.25;
    font-weight: 500;
    letter-spacing: 0;
  }
}

.icon-button {
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: var(--nike-soft-cloud);
  color: var(--nike-ink);
  display: grid;
  place-items: center;
  flex: none;
  cursor: pointer;
}

.me-content {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-card {
  height: 97px;
  padding: 18px;
  background: var(--nike-soft-cloud);
  display: flex;
  align-items: center;
  gap: 14px;

  &__avatar {
    width: 58px;
    height: 58px;
    border-radius: 999px;
    background: var(--nike-ink);
    flex: none;
  }

  &__copy {
    min-width: 0;
    flex: 1;
  }

  h2,
  p {
    margin: 0;
  }

  h2 {
    font-size: 20px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  p {
    margin-top: 4px;
    color: var(--nike-mute);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 700;
    letter-spacing: 0;
  }

  button {
    width: 81px;
    height: 40px;
    border: 1px solid var(--nike-hairline-soft);
    border-radius: 999px;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    font-size: 13px;
    line-height: 1.25;
    font-weight: 800;
    cursor: pointer;
    flex: none;
  }
}

.tile-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;

  button {
    height: 86px;
    border: 0;
    background: var(--nike-soft-cloud);
    color: var(--nike-ink);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    span {
      font-size: 12px;
      line-height: 1.25;
      font-weight: 800;
      letter-spacing: 0;
    }
  }
}

.workbench {
  display: flex;
  flex-direction: column;
  gap: 18px;

  &__head {
    height: 25px;
    display: flex;
    align-items: center;
    gap: 16px;

    h2 {
      margin: 0;
      min-width: 0;
      flex: 1;
      font-size: 20px;
      line-height: 1.25;
      font-weight: 900;
      letter-spacing: 0;
    }

    span {
      color: var(--nike-mute);
      font-size: 13px;
      line-height: 1.25;
      font-weight: 700;
    }
  }
}

.workbench-row {
  width: 100%;
  height: 51px;
  border: 0;
  border-bottom: 1px solid var(--nike-hairline-soft);
  padding: 0;
  background: var(--nike-canvas);
  color: var(--nike-ink);
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  &__label {
    min-width: 0;
    flex: 1;
    text-align: left;
    font-size: 15px;
    line-height: 1.25;
    font-weight: 900;
    letter-spacing: 0;
  }

  &__status {
    color: var(--nike-mute);
    font-size: 14px;
    line-height: 1.25;
    font-weight: 700;
  }
}
</style>
