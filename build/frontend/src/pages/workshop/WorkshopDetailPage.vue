<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showToast } from 'vant';
import { Clock3, MapPin, Music4, Star, Ticket, UserRound } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import workshopHero from '@/assets/pencil/kMcxs.png';
import { fetchMyWorkshopOrders, fetchWorkshopDetail, type WorkshopDetail, type WorkshopOrder } from '@/api/workshop';
import { useFavoriteStore } from '@/stores/favorite';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const favoriteStore = useFavoriteStore();
const user = useUserStore();

const workshopId = Number(route.params.id) || 1;
const workshop = ref<WorkshopDetail | null>(null);
const selectedSessionId = ref<number | null>(null);
const myOrders = ref<WorkshopOrder[]>([]);
const loading = ref(false);

const selectedSession = computed(() =>
  workshop.value?.sessions.find((session) => session.id === selectedSessionId.value) ?? null
);
const title = computed(() => workshop.value?.title ?? 'Workshop');
const coachMeta = computed(() => {
  if (!workshop.value) return '';
  return `${workshop.value.styles[0] ?? 'Workshop'} · ${workshop.value.coachRating ? `评分 ${workshop.value.coachRating.toFixed(1)}` : '特邀导师'}`;
});
const remaining = (capacity: number, taken: number) => Math.max(0, capacity - taken);
const signupDeadlineText = computed(() => {
  if (!workshop.value?.signupDeadline) return '开放报名';
  const date = new Date(workshop.value.signupDeadline);
  if (Number.isNaN(date.getTime())) return workshop.value.signupDeadline;
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')} 截止`;
});
const isFav = computed(() => favoriteStore.isFav('workshop', workshopId));
const activeOrder = computed(
  () =>
    myOrders.value.find(
      (order) =>
        order.workshopId === workshopId &&
        !['CANCELED', 'REFUNDED'].includes(order.status)
    ) ?? null
);
const isWorkshopEnded = computed(() => Boolean(workshop.value?.ended));
const isSignupClosed = computed(() => Boolean(workshop.value?.signupClosed));
const isSelectedSessionEnded = computed(() => Boolean(selectedSession.value?.ended));
const isSelectedSessionAvailable = computed(
  () => Boolean(selectedSession.value) && !isSelectedSessionEnded.value
);
const ctaLabel = computed(() => {
  if (!activeOrder.value) {
    if (isWorkshopEnded.value || isSelectedSessionEnded.value) return '活动已结束';
    if (isSignupClosed.value) return '报名已截止';
    return '选择场次并报名';
  }
  if (activeOrder.value.status === 'UNPAID') return '继续支付';
  return '已报名，查看详情';
});

const load = async () => {
  loading.value = true;
  try {
    const [data, orders] = await Promise.all([
      fetchWorkshopDetail(workshopId),
      user.isLogin ? fetchMyWorkshopOrders().catch(() => []) : Promise.resolve([])
    ]);
    workshop.value = data;
    myOrders.value = orders;
    const defaultSession = data.sessions.find((session) => !session.ended) ?? data.sessions[0] ?? null;
    selectedSessionId.value =
      orders.find((order) => order.workshopId === workshopId && !['CANCELED', 'REFUNDED'].includes(order.status))
        ?.sessionId ?? defaultSession?.id ?? null;
  } finally {
    loading.value = false;
  }
};

const toggleFavorite = () => {
  favoriteStore.toggle({
    targetType: 'workshop',
    targetId: workshopId,
    title: title.value,
    subtitle: `${selectedSession.value?.date ?? workshop.value?.startDate ?? '开放报名'} · ¥${selectedSession.value?.price ?? workshop.value?.priceMin ?? 0}`
  });
  showToast(isFav.value ? '已收藏' : '已取消收藏');
};

const goToPayment = () => {
  if (!user.isLogin) {
    router.push(`/login?redirect=/workshop/${workshopId}`);
    return;
  }
  if (activeOrder.value) {
    if (activeOrder.value.status === 'UNPAID') {
      router.push({
        path: `/workshop/${workshopId}/pay`,
        query: { sessionId: String(activeOrder.value.sessionId) }
      });
      return;
    }
    router.push('/me/workshop-orders');
    return;
  }
  if (!selectedSessionId.value) {
    showFailToast('请选择场次');
    return;
  }
  if (isWorkshopEnded.value || isSelectedSessionEnded.value) {
    showFailToast('活动已结束');
    return;
  }
  if (isSignupClosed.value) {
    showFailToast('报名已截止');
    return;
  }
  router.push({ path: `/workshop/${workshopId}/pay`, query: { sessionId: String(selectedSessionId.value) } });
};

const openMap = () => {
  if (!workshop.value) return;
  if (workshop.value.latitude !== undefined && workshop.value.longitude !== undefined) {
    window.open(
      `https://www.google.com/maps/search/?api=1&query=${workshop.value.latitude},${workshop.value.longitude}`,
      '_blank'
    );
    return;
  }
  showToast(workshop.value.studioAddress || workshop.value.area);
};

onMounted(load);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="Workshop 详情" @share="showToast('Workshop 链接已复制')" />

    <section class="pen-scroll">
      <section class="hero">
        <img :src="workshopHero" alt="Workshop" />
        <div class="hero__overlay">
          <span class="hero__eyebrow">WORKSHOP</span>
          <h1>{{ title }}</h1>
          <p>
            {{ selectedSession ? `${selectedSession.date} ${selectedSession.startTime}-${selectedSession.endTime}` : workshop?.startDate }}
            · {{ signupDeadlineText }}
          </p>
        </div>
      </section>

      <section class="summary">
        <div class="summary__price">
          <strong>¥{{ selectedSession?.price ?? workshop?.priceMin ?? 0 }}</strong>
          <span>起</span>
        </div>
        <div class="summary__stats">
          <span><Music4 :size="15" :stroke-width="2" />{{ workshop?.styles.join(' / ') }}</span>
          <span><Ticket :size="15" :stroke-width="2" />剩 {{ selectedSession ? remaining(selectedSession.capacity, selectedSession.taken) : remaining(workshop?.capacity ?? 0, workshop?.taken ?? 0) }} 位</span>
          <span><Clock3 :size="15" :stroke-width="2" />{{ signupDeadlineText }}</span>
        </div>
      </section>

      <section class="panel">
        <h2 class="panel__title">活动介绍</h2>
        <p class="panel__text">{{ workshop?.intro || '活动介绍加载中…' }}</p>
      </section>

      <section class="panel">
        <div class="panel__head">
          <h2 class="panel__title">场次选择</h2>
          <span>{{ workshop?.sessions.length ?? 0 }} 场</span>
        </div>
        <div class="session-list">
          <button
            v-for="session in workshop?.sessions ?? []"
            :key="session.id"
            class="session-card"
            :class="{
              'session-card--active': session.id === selectedSessionId,
              'session-card--disabled': session.ended
            }"
            type="button"
            :disabled="session.ended"
            @click="selectedSessionId = session.id"
          >
            <div>
              <strong>{{ session.date }}</strong>
              <p>{{ session.startTime }}-{{ session.endTime }}</p>
            </div>
            <div class="session-card__side">
              <span>¥{{ session.price }}</span>
              <em>{{ session.ended ? '活动已结束' : `剩 ${remaining(session.capacity, session.taken)} 位` }}</em>
            </div>
          </button>
        </div>
      </section>

      <section class="panel">
        <h2 class="panel__title">师资介绍</h2>
        <div class="entity-card">
          <div class="entity-card__icon"><UserRound :size="20" :stroke-width="2" /></div>
          <div class="entity-card__copy">
            <strong>{{ workshop?.coachName }}</strong>
            <span>{{ coachMeta }}</span>
            <p>{{ workshop?.coachIntro || '导师介绍待补充。' }}</p>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel__head">
          <h2 class="panel__title">场地信息</h2>
          <button class="ghost-btn" type="button" @click="openMap">地图</button>
        </div>
        <div class="entity-card">
          <div class="entity-card__icon"><MapPin :size="20" :stroke-width="2" /></div>
          <div class="entity-card__copy">
            <strong>{{ workshop?.studioName }}</strong>
            <span>{{ workshop?.studioAddress || workshop?.area }}</span>
            <p>{{ workshop?.studioTransportInfo || '建议提前 15 分钟到场，凭签到码完成核销。' }}</p>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel__head">
          <h2 class="panel__title">往期评价</h2>
          <span>{{ workshop?.reviewCount ?? 0 }} 条 · {{ workshop?.reviewAverage?.toFixed(1) ?? '0.0' }}</span>
        </div>
        <p v-if="!workshop?.pastReviews.length" class="empty">还没有评价，参加后可以成为第一条。</p>
        <article v-for="review in workshop?.pastReviews ?? []" :key="review.id" class="review-card">
          <div class="review-card__head">
            <strong>{{ review.author }}</strong>
            <span><Star :size="14" :stroke-width="2" fill="currentColor" />{{ review.rating.toFixed(1) }}</span>
          </div>
          <p>{{ review.text || '这位舞者给出了高分，但还没留下更多文字。' }}</p>
          <em>{{ review.verified ? '已核验报名/签到' : '公开评价' }}</em>
        </article>
      </section>
    </section>

    <PenActionBar
      :soft-label="isFav ? '已收藏' : '收藏'"
      :dark-label="ctaLabel"
      :dark-disabled="((!selectedSessionId || !isSelectedSessionAvailable) && !activeOrder) || loading"
      @soft="toggleFavorite"
      @dark="goToPayment"
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
  gap: 14px;
}

.hero {
  position: relative;
  aspect-ratio: 16 / 11;
  overflow: hidden;
  background: $pen-soft;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    filter: grayscale(1);
  }

  &__overlay {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    gap: 6px;
    padding: 18px;
    background: linear-gradient(180deg, rgba(17, 17, 17, 0.06), rgba(17, 17, 17, 0.78));
    color: $pen-on-primary;
  }

  &__eyebrow {
    font-size: 11px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  h1, p {
    margin: 0;
  }

  h1 {
    font-size: 28px;
    font-weight: 900;
    line-height: 1.05;
  }

  p {
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
    color: rgba(255, 255, 255, 0.76);
  }
}

.summary,
.panel {
  padding: 0 18px;
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__price {
    display: flex;
    align-items: baseline;
    gap: 6px;

    strong {
      font-size: 30px;
      font-weight: 900;
      line-height: 1;
    }

    span {
      color: $pen-mute;
      font-size: 13px;
      font-weight: 700;
      line-height: $pen-lh;
    }
  }

  &__stats {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    span {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      height: 30px;
      padding: 0 12px;
      border-radius: 999px;
      border: 1px solid $pen-hairline;
      font-size: 12px;
      font-weight: 700;
      line-height: $pen-lh;
    }
  }
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 700;
      line-height: $pen-lh;
    }
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__text {
    margin: 0;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.55;
  }
}

.session-list,
.review-card,
.entity-card {
  display: flex;
  flex-direction: column;
}

.session-list {
  gap: 8px;
}

.session-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-canvas;
  text-align: left;
  cursor: pointer;

  strong, p, span, em {
    margin: 0;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p, em {
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 600;
    line-height: $pen-lh;
  }

  &__side {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
    flex: none;
  }

  span {
    font-size: 18px;
    font-weight: 900;
    line-height: 1;
  }

  &--active {
    border-color: $pen-ink;
    background: $pen-soft;
  }

  &--disabled {
    opacity: 0.48;
    cursor: not-allowed;
  }
}

.entity-card {
  flex-direction: row;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    background: $pen-ink;
    color: $pen-on-primary;
    display: grid;
    place-items: center;
    flex: none;
  }

  &__copy {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  strong {
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span, p {
    margin: 0;
  }

  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  p {
    font-size: 13px;
    font-weight: 500;
    line-height: 1.45;
  }
}

.review-card {
  gap: 6px;
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;

    strong,
    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      font-weight: 900;
      line-height: $pen-lh;
    }
  }

  p, em {
    margin: 0;
  }

  p {
    font-size: 13px;
    font-weight: 500;
    line-height: 1.45;
  }

  em {
    color: $pen-mute;
    font-style: normal;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.ghost-btn {
  height: 30px;
  padding: 0 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.empty {
  margin: 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
