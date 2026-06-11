<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { CalendarDays, ChevronLeft, MapPin, RefreshCw, Users } from 'lucide-vue-next';
import { confirmPracticeCompleted, fetchPracticeDetail, joinPractice, type PracticeParticipant, type PracticePost } from '@/api/practice';
import { getToken } from '@/utils/request';

const route = useRoute();
const router = useRouter();
const practice = ref<PracticePost | null>(null);
const loading = ref(false);
const joining = ref(false);
const completing = ref(false);
const error = ref('');

const id = computed(() => Number(route.params.id));

const statusText: Record<string, string> = {
  PUBLISHED: '招募中',
  MATCHED: '人数达标',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELED: '已取消',
  EXPIRED: '已过期'
};

const canJoin = computed(() =>
  Boolean(practice.value && ['PUBLISHED', 'MATCHED'].includes(practice.value.status) && practice.value.takenCount < practice.value.capacity)
);

const isEnded = computed(() =>
  Boolean(practice.value?.endAt && new Date(practice.value.endAt).getTime() <= Date.now())
);

const canConfirmCompletion = computed(() =>
  Boolean(practice.value
    && getToken()
    && ratingTargets.value.length > 0
    && (['CONFIRMED', 'COMPLETED'].includes(practice.value.status) || (practice.value.status === 'MATCHED' && isEnded.value))
    && !practice.value.completionConfirmedByMe)
);

const ratingTargets = computed(() => practice.value?.ratingTargets ?? []);

const pendingRatingTargets = computed(() =>
  ratingTargets.value.filter((item) => !practice.value?.ratedUserIds?.includes(item.userId))
);

const participantLabel = (item: PracticeParticipant) =>
  item.role === 'creator' ? `发起者 #${item.userId}` : `舞友 #${item.userId}`;

const distanceLabel = computed(() => {
  const meters = practice.value?.distanceMeters;
  if (meters == null) return '距离待确认';
  return meters < 1000 ? `${meters}m` : `${(meters / 1000).toFixed(1)}km`;
});

const load = async () => {
  if (!Number.isFinite(id.value)) {
    error.value = '约练不存在';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    practice.value = await fetchPracticeDetail(id.value);
  } catch {
    error.value = '约练详情加载失败';
  } finally {
    loading.value = false;
  }
};

const join = async () => {
  if (!practice.value) return;
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: `/practice/${practice.value.id}` } });
    return;
  }
  joining.value = true;
  try {
    await joinPractice(practice.value.id);
    showSuccessToast('申请已提交，等待发起人确认');
    router.push('/me/practices');
  } catch {
    showFailToast('申请失败，请检查是否已申请或人数已满');
  } finally {
    joining.value = false;
  }
};

const confirmComplete = async () => {
  if (!practice.value) return;
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: `/practice/${practice.value.id}` } });
    return;
  }
  completing.value = true;
  try {
    practice.value = await confirmPracticeCompleted(practice.value.id);
    showSuccessToast('已确认完成，可以去互评了');
  } catch {
    showFailToast('暂时不能确认完成，请检查约练状态或参与身份');
  } finally {
    completing.value = false;
  }
};

const rateTarget = (item: PracticeParticipant) => {
  if (!practice.value || practice.value.ratedUserIds?.includes(item.userId)) return;
  router.push(`/practice/${practice.value.id}/rate?toUserId=${item.userId}&toName=${encodeURIComponent(participantLabel(item))}`);
};

onMounted(load);
</script>

<template>
  <main class="detail-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div>
        <p>PRACTICE DETAIL</p>
        <h1>约练详情</h1>
      </div>
      <button class="icon-btn icon-btn--dark" type="button" aria-label="刷新" @click="load">
        <RefreshCw :size="18" />
      </button>
    </header>

    <section v-if="loading" class="state">正在加载真实约练...</section>
    <section v-else-if="error" class="state">
      <p>{{ error }}</p>
      <button type="button" @click="load">重试</button>
    </section>

    <template v-else-if="practice">
      <section class="hero">
        <span>{{ statusText[practice.status] || practice.status }}</span>
        <h2>{{ practice.style }} {{ practice.level || '不限水平' }}</h2>
        <p>{{ practice.remark || '发起人还没有补充备注，加入后可以进一步沟通练习目标。' }}</p>
      </section>

      <section class="stats">
        <div>
          <strong>{{ practice.takenCount }}/{{ practice.capacity }}</strong>
          <span>报名人数</span>
        </div>
        <div>
          <strong>{{ practice.level || '不限' }}</strong>
          <span>水平要求</span>
        </div>
        <div>
          <strong>{{ distanceLabel }}</strong>
          <span>距离</span>
        </div>
      </section>

      <section class="panel">
        <article>
          <CalendarDays :size="20" />
          <div>
            <strong>约练时间</strong>
            <p>{{ practice.date }} {{ practice.time }}</p>
          </div>
        </article>
        <article>
          <MapPin :size="20" />
          <div>
            <strong>地点</strong>
            <p>{{ practice.city }} {{ practice.area }} · {{ practice.location }}</p>
          </div>
        </article>
        <article>
          <Users :size="20" />
          <div>
            <strong>发起人</strong>
            <p>{{ practice.authorName || `用户 #${practice.authorId}` }}</p>
          </div>
        </article>
      </section>

      <section v-if="canConfirmCompletion || ratingTargets.length || practice.completionConfirmedByMe" class="review-panel">
        <div class="review-panel__head">
          <div>
            <strong>{{ practice.completionConfirmedByMe ? '完成已确认' : '约练完成确认' }}</strong>
            <p>{{ practice.allCompletedConfirmed ? '全员已确认，约练已完成。' : '确认后可以对本次约练的舞友进行互评。' }}</p>
          </div>
          <button v-if="canConfirmCompletion" type="button" :disabled="completing" @click="confirmComplete">
            {{ completing ? '确认中...' : '确认已完成' }}
          </button>
        </div>

        <div v-if="ratingTargets.length" class="rating-list">
          <button
            v-for="target in ratingTargets"
            :key="target.userId"
            type="button"
            :class="{ done: practice.ratedUserIds?.includes(target.userId) }"
            @click="rateTarget(target)"
          >
            <span>{{ participantLabel(target) }}</span>
            <em>{{ practice.ratedUserIds?.includes(target.userId) ? '已评价' : '去评价' }}</em>
          </button>
        </div>
      </section>

      <section class="actions">
        <button type="button" @click="router.push('/me/practices')">我的约练</button>
        <button v-if="canConfirmCompletion" class="primary" type="button" :disabled="completing" @click="confirmComplete">
          {{ completing ? '确认中...' : '确认已完成' }}
        </button>
        <button v-else-if="pendingRatingTargets.length" class="primary" type="button" @click="rateTarget(pendingRatingTargets[0])">
          去互评
        </button>
        <button v-else class="primary" type="button" :disabled="!canJoin || joining" @click="join">
          {{ joining ? '申请中...' : canJoin ? '申请加入' : '暂不可加入' }}
        </button>
      </section>
    </template>
  </main>
</template>

<style scoped lang="scss">
.detail-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; padding-bottom: calc(92px + env(safe-area-inset-bottom)); }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; position: sticky; top: 0; z-index: 5; background: rgba(255,255,255,.94); backdrop-filter: blur(10px); border-bottom: 1px solid #e5e5e5; }
.topbar div { flex: 1; min-width: 0; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 21px; line-height: 1.1; font-weight: 900; }
.icon-btn { width: 38px; height: 38px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; display: grid; place-items: center; }
.icon-btn--dark { background: #111; color: #fff; }
.state { margin: 18px; padding: 24px 16px; border-radius: 8px; background: #f5f5f5; color: #707072; text-align: center; font-size: 13px; font-weight: 800; }
.state button { margin-top: 10px; height: 36px; padding: 0 16px; border: 0; border-radius: 999px; background: #111; color: #fff; font-weight: 900; }
.hero { margin: 16px 18px 12px; min-height: 190px; border-radius: 8px; background: #111; color: #fff; padding: 18px; display: flex; flex-direction: column; justify-content: flex-end; gap: 10px; }
.hero span { align-self: flex-start; height: 28px; padding: 0 10px; border-radius: 999px; background: #fff; color: #111; display: inline-flex; align-items: center; font-size: 12px; font-weight: 900; }
.hero h2 { margin: 0; font-size: 34px; line-height: 1.02; font-weight: 950; }
.hero p { margin: 0; color: #e5e5e5; font-size: 13px; font-weight: 700; line-height: 1.45; }
.stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 0 18px 14px; }
.stats div { min-height: 78px; border-radius: 8px; background: #f5f5f5; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 4px; }
.stats strong { font-size: 19px; font-weight: 950; }
.stats span { color: #707072; font-size: 12px; font-weight: 800; }
.panel { margin: 0 18px; display: flex; flex-direction: column; border-top: 1px solid #e5e5e5; }
.panel article { display: flex; gap: 12px; padding: 15px 0; border-bottom: 1px solid #e5e5e5; }
.panel strong { display: block; font-size: 15px; font-weight: 900; }
.panel p { margin: 4px 0 0; color: #707072; font-size: 13px; font-weight: 700; line-height: 1.4; }
.review-panel { margin: 16px 18px 0; padding: 14px; border-radius: 8px; background: #f5f5f5; }
.review-panel__head { display: flex; align-items: center; gap: 12px; }
.review-panel__head div { flex: 1; min-width: 0; }
.review-panel__head strong { display: block; font-size: 15px; font-weight: 950; }
.review-panel__head p { margin: 4px 0 0; color: #707072; font-size: 12px; font-weight: 700; line-height: 1.4; }
.review-panel__head button { flex: none; height: 36px; padding: 0 13px; border: 0; border-radius: 999px; background: #111; color: #fff; font-weight: 900; }
.rating-list { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
.rating-list button { min-height: 42px; padding: 0 12px; border: 1px solid #d1d1d1; border-radius: 8px; background: #fff; color: #111; display: flex; align-items: center; justify-content: space-between; gap: 10px; font-weight: 900; }
.rating-list em { color: #111; font-style: normal; font-size: 12px; }
.rating-list .done { color: #707072; background: #ededed; }
.rating-list .done em { color: #707072; }
.actions { position: fixed; left: 50%; bottom: 0; width: 100%; max-width: 430px; padding: 12px 18px calc(12px + env(safe-area-inset-bottom)); background: #fff; border-top: 1px solid #e5e5e5; box-sizing: border-box; transform: translateX(-50%); display: grid; grid-template-columns: 1fr 1.3fr; gap: 8px; }
.actions button { height: 48px; border: 1px solid #111; border-radius: 999px; background: #fff; color: #111; font-size: 15px; font-weight: 900; }
.actions .primary { background: #111; color: #fff; }
.actions button:disabled { opacity: .42; }
</style>
