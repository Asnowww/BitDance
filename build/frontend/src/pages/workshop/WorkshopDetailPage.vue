<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import {
  fetchWorkshopDetail,
  createWorkshopOrder,
  payWorkshopOrder,
  type WorkshopDetail
} from '@/api/workshop';
import { useFavoriteStore } from '@/stores/favorite';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();
const user = useUserStore();

const detail = ref<WorkshopDetail | null>(null);
const selectedSessionId = ref<number | null>(null);
const submitting = ref(false);

const id = computed(() => Number(route.params.id));

const reload = async () => {
  detail.value = await fetchWorkshopDetail(id.value);
  if (detail.value && detail.value.sessions.length) {
    selectedSessionId.value = detail.value.sessions[0].id;
  }
};

const onPay = async () => {
  if (!detail.value || !selectedSessionId.value) return;
  if (!user.isLogin) {
    router.push(`/login?redirect=/workshop/${id.value}`);
    return;
  }
  submitting.value = true;
  try {
    const order = await createWorkshopOrder({
      workshopId: detail.value.id,
      sessionId: selectedSessionId.value,
      idempotencyToken: `ws-${detail.value.id}-${selectedSessionId.value}-${Date.now()}`
    });
    if (!order) {
      showFailToast('该场次名额已满');
      return;
    }
    // mock 直接调用 pay 模拟微信支付完成
    await payWorkshopOrder(order.id);
    showSuccessToast('支付成功');
    router.push('/me/workshop-orders');
  } finally {
    submitting.value = false;
  }
};

const onFav = () => {
  if (!detail.value) return;
  fav.toggle({
    targetType: 'workshop',
    targetId: detail.value.id,
    title: detail.value.title,
    subtitle: `${detail.value.startDate} · ¥${detail.value.priceMin}`
  });
};

onMounted(reload);
</script>

<template>
  <div v-if="!detail" class="empty">加载中…</div>
  <div v-else class="page">
    <div class="hero">
      <button class="back" @click="router.back()">←</button>
      <button class="fav" @click="onFav">{{ fav.isFav('workshop', detail.id) ? '♥' : '♡' }}</button>
      <span class="hero__title">{{ detail.title }}</span>
    </div>
    <section class="info">
      <div class="info__title">{{ detail.title }}</div>
      <div class="info__meta">
        <span>{{ detail.area }}</span>
        <span>·</span>
        <span>{{ detail.startDate }} ~ {{ detail.endDate }}</span>
      </div>
      <div class="info__tags">
        <span v-for="s in detail.styles" :key="s" class="tag">{{ s }}</span>
      </div>
    </section>
    <section class="block">
      <h3>活动介绍</h3>
      <p>{{ detail.intro }}</p>
    </section>
    <section class="block">
      <h3>师资 / 场地</h3>
      <div class="row" @click="router.push(`/coach/${detail.coachId}`)">
        <span>🎤 教练：{{ detail.coachName }}</span>
        <span class="row__action">查看</span>
      </div>
      <div class="row" @click="router.push(`/studio/${detail.studioId}`)">
        <span>🏠 舞室：{{ detail.studioName }}</span>
        <span class="row__action">查看</span>
      </div>
    </section>
    <section class="block">
      <h3>选择场次</h3>
      <div class="sessions">
        <button
          v-for="s in detail.sessions"
          :key="s.id"
          class="session"
          :class="{ active: selectedSessionId === s.id, full: s.taken >= s.capacity }"
          :disabled="s.taken >= s.capacity"
          @click="selectedSessionId = s.id"
        >
          <div class="session__date">{{ s.date }}</div>
          <div class="session__time">{{ s.startTime }}-{{ s.endTime }}</div>
          <div class="session__price">¥{{ s.price }}</div>
          <div class="session__cap">{{ s.taken }}/{{ s.capacity }}</div>
        </button>
      </div>
    </section>
    <section class="block">
      <h3>往期评价</h3>
      <article v-for="r in detail.pastReviews" :key="r.id" class="rev">
        <div class="rev__head">
          <span>{{ r.author }}</span>
          <span class="rev__star">{{ '★'.repeat(r.rating) }}</span>
        </div>
        <p>{{ r.text }}</p>
      </article>
    </section>
    <footer class="footer">
      <button class="btn btn--ghost" @click="onFav">
        {{ fav.isFav('workshop', detail.id) ? '♥ 已收藏' : '♡ 收藏' }}
      </button>
      <button class="btn btn--primary" :disabled="!selectedSessionId || submitting" @click="onPay">
        {{ submitting ? '支付中…' : '立即报名 / 支付' }}
      </button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}
.empty {
  padding: 80px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.hero {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #ff7799, #ff2442);
  display: flex;
  align-items: flex-end;
  padding: 24px;
  color: #fff;
  &__title {
    font-size: 22px;
    font-weight: 700;
  }
}
.back,
.fav {
  position: absolute;
  top: calc(12px + env(safe-area-inset-top));
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  border: none;
  height: 32px;
  border-radius: 16px;
  padding: 0 12px;
  font-size: 14px;
  cursor: pointer;
}
.back {
  left: 12px;
  width: 32px;
  padding: 0;
}
.fav {
  right: 12px;
  font-size: 18px;
}
.info {
  background: #fff;
  padding: 16px;
  &__title {
    font-size: 18px;
    font-weight: 700;
  }
  &__meta {
    margin-top: 6px;
    display: flex;
    gap: 6px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__tags {
    margin-top: 8px;
    display: flex;
    gap: 6px;
  }
}
.tag {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  background: rgba(255, 36, 66, 0.08);
  color: var(--bd-primary);
}
.block {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 8px;
    font-size: 14px;
  }
  p {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
  }
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  cursor: pointer;
  &__action {
    color: var(--bd-primary);
  }
}
.sessions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.session {
  border: 1px solid var(--bd-border);
  border-radius: 10px;
  background: #fff;
  padding: 10px;
  text-align: left;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
  }
  &.full {
    opacity: 0.5;
    cursor: not-allowed;
  }
  &__date {
    font-size: 13px;
    font-weight: 600;
  }
  &__time {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__price {
    margin-top: 6px;
    color: var(--bd-primary);
    font-weight: 700;
  }
  &__cap {
    margin-top: 4px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.rev {
  padding: 10px 0;
  border-bottom: 1px dashed var(--bd-border);
  &:last-child {
    border-bottom: none;
  }
  &__head {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__star {
    color: #ffaa33;
  }
  p {
    margin: 6px 0 0;
  }
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
  display: flex;
  gap: 10px;
}
.btn {
  flex: 1;
  height: 46px;
  border: none;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  &--primary {
    background: var(--bd-primary);
    color: #fff;
  }
  &--ghost {
    background: rgba(255, 36, 66, 0.08);
    color: var(--bd-primary);
  }
  &:disabled {
    opacity: 0.5;
  }
}
</style>
