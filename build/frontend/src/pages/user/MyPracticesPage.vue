<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { CalendarDays, Users } from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import {
  acceptPracticeRequest,
  cancelPracticeRequest,
  confirmPracticeCompleted,
  fetchPracticeDetail,
  fetchMyPracticeRequests,
  fetchMyPractices,
  fetchPracticeRequests,
  rejectPracticeRequest,
  type PracticeJoinRequest,
  type PracticeParticipant,
  type PracticePost
} from '@/api/practice';

const router = useRouter();
const tab = ref<'created' | 'joined' | 'requests'>('created');
const loading = ref(false);
const created = ref<PracticePost[]>([]);
const joined = ref<PracticeJoinRequest[]>([]);
const requests = ref<Record<number, PracticeJoinRequest[]>>({});
const joinedPractices = ref<Record<number, PracticePost>>({});

const statusText: Record<string, string> = {
  PUBLISHED: '招募中',
  MATCHED: '人数达标',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELED: '已取消',
  EXPIRED: '已过期',
  pending: '待确认',
  accepted: '已通过',
  rejected: '已拒绝',
  canceled: '已撤回'
};

const pendingRequests = computed(() =>
  Object.values(requests.value).flat().filter((item) => item.joinStatus === 'pending')
);

const practiceMap = computed<Record<number, PracticePost>>(() => ({
  ...Object.fromEntries(created.value.map((item) => [item.id, item])),
  ...joinedPractices.value
}));

const load = async () => {
  loading.value = true;
  try {
    const [mine, myRequests] = await Promise.all([fetchMyPractices(), fetchMyPracticeRequests()]);
    created.value = mine;
    joined.value = myRequests;
    const joinedIds = [...new Set(myRequests.map((item) => item.practicePostId))];
    const [pairs, joinedPairs] = await Promise.all([
      Promise.all(mine.map(async (post) => [post.id, await fetchPracticeRequests(post.id)] as const)),
      Promise.all(joinedIds.map(async (practiceId) => {
        try {
          return [practiceId, await fetchPracticeDetail(practiceId)] as const;
        } catch {
          return null;
        }
      }))
    ]);
    requests.value = Object.fromEntries(pairs);
    joinedPractices.value = Object.fromEntries(
      joinedPairs.filter((item): item is readonly [number, PracticePost] => Boolean(item))
    );
  } finally {
    loading.value = false;
  }
};

const dateLine = (item: PracticePost) => `${item.date} ${item.time} · ${item.area || item.city}`;

const practiceOf = (practiceId: number) => practiceMap.value[practiceId];

const canConfirmCompletion = (item?: PracticePost) =>
  Boolean(item
    && (['CONFIRMED', 'COMPLETED'].includes(item.status) || (item.status === 'MATCHED' && item.endAt && new Date(item.endAt).getTime() <= Date.now()))
    && !item.completionConfirmedByMe);

const pendingRatingTargets = (item?: PracticePost) =>
  (item?.ratingTargets ?? []).filter((target) => !item?.ratedUserIds?.includes(target.userId));

const participantLabel = (item: PracticeParticipant) =>
  item.role === 'creator' ? `发起者 #${item.userId}` : `舞友 #${item.userId}`;

const confirmComplete = async (item: PracticePost) => {
  const updated = await confirmPracticeCompleted(item.id);
  created.value = created.value.map((post) => post.id === updated.id ? updated : post);
  joinedPractices.value = { ...joinedPractices.value, [updated.id]: updated };
  showSuccessToast('已确认完成，可以去互评了');
};

const goRate = (practice: PracticePost, target: PracticeParticipant) => {
  router.push(`/practice/${practice.id}/rate?toUserId=${target.userId}&toName=${encodeURIComponent(participantLabel(target))}`);
};

const requestDate = (item: PracticeJoinRequest) =>
  item.createdAt ? new Date(item.createdAt).toLocaleString() : '申请时间待同步';

const handleRequest = async (item: PracticeJoinRequest, action: 'accept' | 'reject') => {
  if (action === 'accept') {
    await acceptPracticeRequest(item.id);
    showSuccessToast('已通过申请');
  } else {
    await rejectPracticeRequest(item.id);
    showSuccessToast('已拒绝申请');
  }
  await load();
};

const cancelMine = async (item: PracticeJoinRequest) => {
  await cancelPracticeRequest(item.id);
  showSuccessToast('已撤回申请');
  await load();
};

onMounted(load);
</script>

<template>
  <main class="pen-page">
    <PenTopBar title="我的约练" :show-share="false" />

    <section class="pen-scroll">
      <section class="summary">
        <div>
          <p>REAL PRACTICE</p>
          <h1>{{ created.length }} 个发布 · {{ joined.length }} 个申请</h1>
        </div>
        <button type="button" @click="router.push('/publish/practice')">发布约练</button>
      </section>

      <div class="seg">
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'created' }" type="button" @click="tab = 'created'">我发起的</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'joined' }" type="button" @click="tab = 'joined'">我申请的</button>
        <button class="seg__btn" :class="{ 'seg__btn--on': tab === 'requests' }" type="button" @click="tab = 'requests'">待处理 {{ pendingRequests.length }}</button>
      </div>

      <p v-if="loading" class="empty">正在同步约练数据...</p>

      <template v-else-if="tab === 'created'">
        <article v-for="item in created" :key="item.id" class="rec">
          <div class="cover"><CalendarDays :size="24" /></div>
          <div class="body">
            <strong>{{ item.style }} {{ item.level || '不限水平' }}</strong>
            <p>{{ dateLine(item) }}</p>
            <div class="foot">
              <span>{{ statusText[item.status] || item.status }} · {{ item.takenCount }}/{{ item.capacity }} 人</span>
              <div class="actions">
                <button v-if="canConfirmCompletion(item)" type="button" @click="confirmComplete(item)">确认完成</button>
                <button v-else-if="pendingRatingTargets(item).length" class="primary" type="button" @click="goRate(item, pendingRatingTargets(item)[0])">去互评</button>
                <button v-else type="button" @click="router.push(`/practice/${item.id}`)">{{ item.status === 'COMPLETED' ? '查看评价' : '详情' }}</button>
              </div>
            </div>
          </div>
        </article>
        <p v-if="!created.length" class="empty">你还没有发布约练，先发起一条练习局。</p>
      </template>

      <template v-else-if="tab === 'joined'">
        <article v-for="item in joined" :key="item.id" class="rec">
          <div class="cover"><Users :size="24" /></div>
          <div class="body">
            <template v-if="practiceOf(item.practicePostId)">
              <strong>{{ practiceOf(item.practicePostId).style }} {{ practiceOf(item.practicePostId).level || '不限水平' }}</strong>
              <p>{{ dateLine(practiceOf(item.practicePostId)) }}</p>
              <p class="meta-line">
                {{ practiceOf(item.practicePostId).takenCount }}/{{ practiceOf(item.practicePostId).capacity }} 人 ·
                {{ statusText[practiceOf(item.practicePostId).status] || practiceOf(item.practicePostId).status }} ·
                {{ requestDate(item) }}
              </p>
            </template>
            <template v-else>
              <strong>约练 #{{ item.practicePostId }}</strong>
              <p>{{ requestDate(item) }}</p>
            </template>
            <div class="foot">
              <span>{{ statusText[item.joinStatus] || item.joinStatus }}</span>
              <div class="actions">
                <button v-if="item.joinStatus === 'pending'" type="button" @click="cancelMine(item)">撤回</button>
                <template v-else-if="practiceOf(item.practicePostId)">
                  <button v-if="canConfirmCompletion(practiceOf(item.practicePostId))" type="button" @click="confirmComplete(practiceOf(item.practicePostId))">确认完成</button>
                  <button v-else-if="pendingRatingTargets(practiceOf(item.practicePostId)).length" class="primary" type="button" @click="goRate(practiceOf(item.practicePostId), pendingRatingTargets(practiceOf(item.practicePostId))[0])">去互评</button>
                  <button v-else type="button" @click="router.push(`/practice/${item.practicePostId}`)">查看</button>
                </template>
                <button v-else type="button" @click="router.push(`/practice/${item.practicePostId}`)">查看</button>
              </div>
            </div>
          </div>
        </article>
        <p v-if="!joined.length" class="empty">你还没有申请加入别人的约练。</p>
      </template>

      <template v-else>
        <article v-for="item in pendingRequests" :key="item.id" class="rec">
          <div class="cover"><Users :size="24" /></div>
          <div class="body">
            <strong>用户 #{{ item.applicantUserId }} 申请加入</strong>
            <p v-if="practiceOf(item.practicePostId)">
              {{ practiceOf(item.practicePostId).style }} {{ practiceOf(item.practicePostId).level || '不限水平' }} ·
              {{ dateLine(practiceOf(item.practicePostId)) }}
            </p>
            <p v-else>约练 #{{ item.practicePostId }}</p>
            <p class="meta-line">{{ item.joinMessage || '暂无留言' }} · {{ requestDate(item) }}</p>
            <div class="foot">
              <span>待确认</span>
              <div class="actions">
                <button type="button" @click="handleRequest(item, 'reject')">拒绝</button>
                <button class="primary" type="button" @click="handleRequest(item, 'accept')">通过</button>
              </div>
            </div>
          </div>
        </article>
        <p v-if="!pendingRequests.length" class="empty">暂无待处理申请。</p>
      </template>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }
.pen-scroll { display: flex; flex-direction: column; gap: 14px; padding: 16px 18px calc(24px + env(safe-area-inset-bottom)); }
.summary { display: flex; justify-content: space-between; align-items: flex-end; gap: 12px; padding: 18px; border-radius: 8px; background: $pen-ink; color: $pen-on-primary; }
.summary p { margin: 0 0 5px; color: $pen-subtle-text; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.summary h1 { margin: 0; font-size: 24px; line-height: 1.1; font-weight: 900; }
.summary button { flex: none; height: 38px; padding: 0 14px; border: 0; border-radius: 999px; background: $pen-canvas; color: $pen-ink; font-weight: 900; }
.seg { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.seg__btn { min-height: 42px; border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink; font-size: 13px; font-weight: 900; }
.seg__btn--on { background: $pen-ink; color: $pen-on-primary; }
.rec { display: flex; gap: 12px; padding: 14px 0; border-bottom: 1px solid $pen-hairline; }
.cover { flex: none; width: 72px; height: 72px; border-radius: 8px; background: $pen-soft; display: grid; place-items: center; color: $pen-ink; }
.body { flex: 1; min-width: 0; }
.body strong { display: block; font-size: 16px; font-weight: 900; line-height: $pen-lh; }
.body p { margin: 4px 0 10px; color: $pen-mute; font-size: 12px; font-weight: 700; line-height: 1.45; }
.body .meta-line { margin-top: -4px; font-size: 11px; font-weight: 800; }
.foot { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.foot span { color: $pen-mute; font-size: 12px; font-weight: 900; }
.foot button { height: 34px; padding: 0 14px; border: 1px solid $pen-ink; border-radius: 999px; background: $pen-canvas; color: $pen-ink; font-weight: 900; }
.actions { display: flex; gap: 8px; }
.actions .primary { background: $pen-ink; color: $pen-on-primary; }
.empty { padding: 20px 8px; color: $pen-mute; text-align: center; font-size: 13px; font-weight: 700; }
</style>
