<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast, showToast } from 'vant';
import { Music } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';
import { createWorkshopOrder, fetchWorkshopDetail, payWorkshopOrder, type WorkshopDetail } from '@/api/workshop';
import { useFavoriteStore } from '@/stores/favorite';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const fav = useFavoriteStore();
const user = useUserStore();

const workshop = ref<WorkshopDetail | null>(null);
const selectedSessionId = ref<number | null>(null);
const submitting = ref(false);
const loading = ref(false);
const rules = ['活动前 24 小时可退', '到场扫码签到', '活动后评价老师和课程'];
const workshopId = Number(route.params.id) || 1;

const selectedSession = computed(() =>
  workshop.value?.sessions.find((session) => session.id === selectedSessionId.value)
);
const title = computed(() => workshop.value?.title ?? 'Workshop');
const price = computed(() => selectedSession.value?.price ?? workshop.value?.priceMin ?? 0);
const remaining = computed(() => {
  const s = selectedSession.value;
  return s ? Math.max(0, s.capacity - s.taken) : Math.max(0, (workshop.value?.capacity ?? 0) - (workshop.value?.taken ?? 0));
});
const signupDeadlineText = computed(() => {
  if (!workshop.value?.signupDeadline) return '';
  const parsed = new Date(workshop.value.signupDeadline);
  if (Number.isNaN(parsed.getTime())) return workshop.value.signupDeadline;
  return parsed.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
});
const signupClosed = computed(() => {
  if (!workshop.value?.signupDeadline) return false;
  const deadline = Date.parse(workshop.value.signupDeadline);
  return Number.isNaN(deadline) ? false : deadline < Date.now();
});
const heroSub = computed(() => {
  const s = selectedSession.value;
  const time = s ? `${s.date} ${s.startTime}` : workshop.value?.startDate ?? '开放报名';
  const deadline = signupClosed.value
    ? '报名已截止'
    : signupDeadlineText.value
      ? `${signupDeadlineText.value} 截止`
      : '开放报名';
  return `${time} · 剩余 ${remaining.value} 位 · ${deadline}`;
});
const detailSub = computed(() =>
  `${workshop.value?.coachName ?? '特邀导师'} · ${workshop.value?.styles[0] ?? 'Workshop'} · ¥${price.value} · ${
    workshop.value?.studioName ?? workshop.value?.area ?? '合作舞室'
  }`
);

const loadDetail = async () => {
  loading.value = true;
  try {
    const data = await fetchWorkshopDetail(workshopId);
    workshop.value = data;
    selectedSessionId.value = data.sessions[0]?.id ?? null;
  } finally {
    loading.value = false;
  }
};

const onFav = () => {
  fav.toggle({
    targetType: 'workshop',
    targetId: workshopId,
    title: title.value,
    subtitle: `${selectedSession.value?.date ?? workshop.value?.startDate ?? '开放报名'} · ¥${price.value}`
  });
  showToast(fav.isFav('workshop', workshopId) ? '已收藏' : '已取消收藏');
};

const onPay = async () => {
  if (!user.isLogin) {
    router.push(`/login?redirect=/workshop/${workshopId}`);
    return;
  }
  if (!selectedSessionId.value) {
    showFailToast('请选择可报名场次');
    return;
  }
  if (remaining.value <= 0) {
    showFailToast('该场次已满');
    return;
  }
  if (signupClosed.value) {
    showFailToast('报名已截止');
    return;
  }
  if (submitting.value) return;
  submitting.value = true;
  try {
    const order = await createWorkshopOrder({
      workshopId,
      sessionId: selectedSessionId.value,
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

onMounted(loadDetail);
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
        <strong class="hero__title">{{ title }}</strong>
        <p class="hero__sub">{{ loading ? '加载中…' : heroSub }}</p>
      </section>

      <section class="detail">
        <h2 class="pen-h2">{{ title }}</h2>
        <p class="detail__sub">{{ detailSub }}</p>
        <p v-if="signupDeadlineText" class="detail__deadline">
          {{ signupClosed ? '报名已截止' : '报名截止' }} · {{ signupDeadlineText }}
        </p>
        <p v-if="workshop?.intro" class="detail__intro">{{ workshop.intro }}</p>

        <div class="detail__block">
          <h3 class="detail__section">场次选择</h3>
          <div class="sessions">
            <button
              v-for="session in workshop?.sessions ?? []"
              :key="session.id"
              type="button"
              class="session-pill"
              :class="selectedSessionId === session.id ? 'session-pill--active' : 'session-pill--inactive'"
              @click="selectedSessionId = session.id"
            >
              {{ session.date }} {{ session.startTime }} · 剩 {{ Math.max(0, session.capacity - session.taken) }}
            </button>
          </div>
          <p v-if="!loading && !workshop?.sessions.length" class="detail__empty">暂无可报名场次</p>
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
      :dark-label="signupClosed ? '报名已截止' : submitting ? '报名中…' : '立即报名'"
      :dark-disabled="submitting || !selectedSessionId || signupClosed"
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

  &__intro {
    margin: 0;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.45;
    letter-spacing: 0;
  }

  &__empty {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: $pen-lh;
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

  &__deadline {
    margin: 0;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 800;
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
