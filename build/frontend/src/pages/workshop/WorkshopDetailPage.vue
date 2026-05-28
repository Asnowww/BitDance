<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';
import { createWorkshopOrder, payWorkshopOrder } from '@/api/workshop';
import { useFavoriteStore } from '@/stores/favorite';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();
const user = useUserStore();

const selectedSession = ref('5/30 14:00');
const submitting = ref(false);
const sessions = ['5/30 14:00', '5/31 19:30'];
const rules = ['活动前 24 小时可退', '到场扫码签到', '活动后评价老师和课程'];
const workshopId = Number(route.params.id) || 1;

const onFav = () => {
  fav.toggle({
    targetType: 'workshop',
    targetId: workshopId,
    title: 'Locking 大师课',
    subtitle: '周日 14:00 · ¥199'
  });
  showToast(fav.isFav('workshop', workshopId) ? '已收藏' : '已取消收藏');
};

const onPay = async () => {
  if (!user.isLogin) {
    router.push(`/login?redirect=/workshop/${workshopId}`);
    return;
  }
  if (submitting.value) return;
  submitting.value = true;
  try {
    const order = await createWorkshopOrder({
      workshopId,
      sessionId: selectedSession.value === '5/30 14:00' ? 1 : 2,
      idempotencyToken: `ws-${workshopId}-${Date.now()}`
    });
    if (order) {
      await payWorkshopOrder(order.id);
    }
    showSuccessToast('报名成功');
    router.push('/me/workshop-orders');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="Workshop 详情" @share="showToast('Workshop 链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <div class="hero__bars" aria-hidden="true">
          <span v-for="i in 6" :key="i" />
        </div>
        <Music class="hero__icon" :size="42" :stroke-width="2" />
        <strong class="hero__title">LOCKING<br />MASTER</strong>
        <p class="hero__sub">周日 14:00 · 剩余 8 位</p>
      </section>

      <section class="detail">
        <h2 class="pen-h2">Locking 大师课</h2>
        <p class="detail__sub">Pop 老师 · 中级 · ¥199 · 朝阳 Joy Studio</p>

        <div class="detail__block">
          <h3 class="detail__section">场次选择</h3>
          <div class="sessions">
            <button
              v-for="session in sessions"
              :key="session"
              type="button"
              class="session-pill"
              :class="selectedSession === session ? 'session-pill--active' : 'session-pill--inactive'"
              @click="selectedSession = session"
            >
              {{ session }}
            </button>
          </div>
        </div>

        <div class="detail__block">
          <h3 class="detail__section">报名须知与退款规则</h3>
          <PenSettingRow
            v-for="rule in rules"
            :key="rule"
            :label="rule"
            trailing="查看"
            @click="showToast(rule)"
          />
        </div>
      </section>
    </section>

    <PenActionBar
      :soft-label="fav.isFav('workshop', workshopId) ? '已收藏' : '收藏'"
      :dark-label="submitting ? '报名中…' : '立即报名'"
      :dark-disabled="submitting"
      @soft="onFav"
      @dark="onPay"
    />
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;

  &--with-bar {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 8px;
  height: 280px;
  padding: 18px;
  background: $pen-ink;
  color: $pen-on-primary;
  box-sizing: border-box;

  &__bars {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
    height: 30px;
    margin-bottom: auto;

    span {
      height: 100%;
      background: $pen-charcoal;
    }
  }

  &__icon {
    flex-shrink: 0;
    color: $pen-on-primary;
  }

  &__title {
    margin: 0;
    font-size: 32px;
    font-weight: 900;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__sub {
    margin: 0;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
    letter-spacing: 0;
  }
}

.detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 18px 20px;

  &__sub {
    margin: 0;
    color: $pen-mute;
    font-size: 14px;
    font-weight: 700;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  &__block {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__section {
    @include pen-h3-section;
  }
}

.pen-h2 {
  @include pen-h2;
}

.sessions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.session-pill {
  height: 40px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
  letter-spacing: 0;
  cursor: pointer;

  &--inactive {
    border: 1px solid $pen-hairline;
    background: $pen-canvas;
    color: $pen-ink;
  }

  &--active {
    border: none;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}
</style>
