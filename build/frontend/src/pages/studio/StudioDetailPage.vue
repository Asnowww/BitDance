<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import {
  CalendarClock,
  ChevronRight,
  Clock,
  Heart,
  MapPin,
  Music,
  Navigation,
  Phone,
  UserRound,
  Users
} from 'lucide-vue-next';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import PenActionBar from '@/components/pen/PenActionBar.vue';
import ReviewAggregatePanel from '@/components/review/ReviewAggregatePanel.vue';
import { fetchCoachDetail, type CoachDetail } from '@/api/course';
import { fetchStudioDetail, type StudioDetail } from '@/api/studio';
import { fetchStudioSchedule, type ScheduleSlot } from '@/api/trial';
import { toggleFavorite } from '@/api/favorite';
import { buildTencentMarkerUrl, buildTencentSearchUrl } from '@/utils/tencentMap';

type DetailTab = '概览' | '课程' | '老师' | '评价' | '课表';

interface CourseCard {
  id: number;
  coachId: number;
  title: string;
  meta: string;
  tag: string;
  price: string;
  slotCount: number;
}

const route = useRoute();
const router = useRouter();
const studioId = computed(() => Number(route.params.id) || 1);
const detail = ref<StudioDetail | null>(null);
const schedule = ref<ScheduleSlot[]>([]);
const coaches = ref<CoachDetail[]>([]);
const favored = computed(() => detail.value?.favored ?? false);

const tabs: DetailTab[] = ['概览', '课程', '老师', '评价', '课表'];
const queryTab = route.query.tab === 'reviews' ? '评价' : undefined;
const activeTab = ref<DetailTab>((queryTab as DetailTab) || '概览');

const styleNames = ['Hip-hop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'K-pop', 'Waacking', 'Urban'];
const courseNames = ['Hip-hop 基础律动', 'Jazz 入门训练', 'Breaking 体能课', 'Mira Locking Workshop', 'Popping 控制课', 'K-pop 成品班'];

const subtitle = computed(() => {
  const parts = [
    detail.value?.address,
    detail.value?.danceStyleIds?.slice(0, 2).map((id) => styleNames[(id - 1) % styleNames.length]).join(' / '),
    detail.value?.distanceKm ? `距离 ${detail.value.distanceKm}km` : ''
  ].filter(Boolean);
  return parts.join(' · ') || '舞室信息待完善';
});

const overviewTags = computed(() => {
  const styles = detail.value?.danceStyleIds?.slice(0, 2).map((id) => styleNames[(id - 1) % styleNames.length]) ?? [];
  return ['零基础友好', detail.value?.transportInfo ? '近地铁' : '交通便利', '课程丰富', ...styles].slice(0, 5);
});

const capacityText = computed(() => {
  const total = schedule.value.reduce((sum, item) => sum + Number(item.capacity || 0), 0);
  const booked = schedule.value.reduce((sum, item) => sum + Number(item.bookedCount || 0), 0);
  if (!total) return '课表待更新';
  return `本周余位 ${Math.max(total - booked, 0)} / ${total}`;
});

const courseCards = computed<CourseCard[]>(() => {
  const map = new Map<number, CourseCard>();
  schedule.value.forEach((slot) => {
    const styleIndex = Math.abs(slot.courseId) % courseNames.length;
    const existing = map.get(slot.courseId);
    if (existing) {
      existing.slotCount += 1;
      return;
    }
    map.set(slot.courseId, {
      id: slot.courseId,
      coachId: slot.coachId,
      title: courseNames[styleIndex],
      meta: `${formatWeekday(slot.startAt)} ${formatTime(slot.startAt)} · ${slot.classroomName}`,
      tag: styleIndex % 2 === 0 ? '零基础' : '进阶',
      price: `¥${79 + (styleIndex % 4) * 20} 试听`,
      slotCount: 1
    });
  });
  return Array.from(map.values()).slice(0, 6);
});

const visibleSchedule = computed(() => schedule.value.slice(0, 8));

const loadCoaches = async (slots: ScheduleSlot[]) => {
  const ids = Array.from(new Set(slots.map((item) => item.coachId))).slice(0, 4);
  const results = await Promise.allSettled(ids.map((id) => fetchCoachDetail(id)));
  coaches.value = results
    .filter((item): item is PromiseFulfilledResult<CoachDetail> => item.status === 'fulfilled')
    .map((item) => item.value);
};

const formatWeekday = (iso: string) =>
  ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(iso).getDay()] || '待定';

const formatTime = (iso: string) =>
  new Date(iso).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });

const coachStyle = (coach: CoachDetail) =>
  coach.styles?.map((item) => styleNames[(item.danceStyleId - 1) % styleNames.length]).join(' / ') || '舞种待完善';

const setTab = (tab: DetailTab) => {
  activeTab.value = tab;
};

const toggleStudioFavorite = async () => {
  const result = await toggleFavorite('studio', studioId.value);
  if (detail.value) detail.value.favored = result.favored;
  showToast(result.favored ? '已收藏' : '已取消收藏');
};

const openNavigation = () => {
  const longitude = detail.value?.longitude;
  const latitude = detail.value?.latitude;
  // M1 腾讯地图联动：有后端标注坐标时打开 marker，缺坐标时保留地址搜索兜底。
  const url =
    longitude !== undefined && latitude !== undefined
      ? buildTencentMarkerUrl(latitude, longitude, detail.value?.name ?? '舞室', detail.value?.address)
      : buildTencentSearchUrl(detail.value?.address ?? detail.value?.name ?? '舞室');
  window.open(url);
};

const callStudio = () => {
  if (detail.value?.contactPhone) window.location.href = `tel:${detail.value.contactPhone}`;
  else showToast('暂无联系电话');
};

const onBook = () => router.push(`/studio/${studioId.value}/trial`);

onMounted(async () => {
  // M1 详情页先渲染舞室主档，避免课表为空或接口短暂失败时把名称、地址等基础信息一起降级成静态兜底。
  detail.value = await fetchStudioDetail(studioId.value);
  try {
    const scheduleResp = await fetchStudioSchedule(studioId.value);
    schedule.value = scheduleResp;
    await loadCoaches(scheduleResp);
  } catch {
    schedule.value = [];
    coaches.value = [];
    showToast('课表暂未更新');
  }
});
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="舞室详情" @share="showToast('舞室链接已复制')" />

    <section class="pen-scroll">
      <section class="body">
        <section class="profile-card">
          <div class="profile-card__icon">
            <Music :size="30" :stroke-width="2" />
          </div>
          <div class="profile-card__content">
            <h1>{{ detail?.name || 'Urban Flow 舞室' }}</h1>
            <p>{{ subtitle }}</p>
          </div>
        </section>

        <div class="action-row">
          <button type="button" class="action-pill" @click="toggleStudioFavorite">
            <Heart :size="18" :fill="favored ? 'currentColor' : 'none'" />
            <span>{{ favored ? '已收藏' : '收藏' }}</span>
          </button>
          <button type="button" class="action-pill" @click="openNavigation">
            <Navigation :size="18" />
            <span>导航</span>
          </button>
          <button type="button" class="action-pill" @click="callStudio">
            <Phone :size="18" />
            <span>联系</span>
          </button>
        </div>

        <nav class="tab-row" aria-label="舞室详情栏目">
          <button
            v-for="tab in tabs"
            :key="tab"
            type="button"
            class="tab-row__item"
            :class="{ 'tab-row__item--active': activeTab === tab }"
            @click="setTab(tab)"
          >
            {{ tab }}
          </button>
        </nav>

        <section v-if="activeTab === '概览'" class="tab-panel">
          <section class="intro-card">
            <header class="section-title">
              <h2>概览</h2>
              <span>{{ capacityText }}</span>
            </header>
            <div class="chip-row">
              <span v-for="tag in overviewTags" :key="tag" class="chip chip--inactive">{{ tag }}</span>
            </div>
            <p>{{ detail?.intro || '主打街舞与成人舞蹈课程，支持试听预约、课程详情、老师主页和课表查看。' }}</p>
            <div class="intro-card__addr">
              <MapPin :size="16" />
              <span>{{ detail?.transportInfo || detail?.address || '地址待完善' }}</span>
            </div>
          </section>

          <section class="info-grid">
            <article>
              <Users :size="18" />
              <strong>{{ coaches.length || '-' }}</strong>
              <span>可预约老师</span>
            </article>
            <article>
              <Music :size="18" />
              <strong>{{ courseCards.length || '-' }}</strong>
              <span>本周课程</span>
            </article>
            <article>
              <CalendarClock :size="18" />
              <strong>{{ schedule.length || '-' }}</strong>
              <span>课表场次</span>
            </article>
          </section>
        </section>

        <section v-else-if="activeTab === '课程'" class="tab-panel">
          <header class="section-title">
            <h2>课程</h2>
            <span>点击查看课程详情</span>
          </header>
          <article v-for="course in courseCards" :key="course.id" class="course" @click="router.push(`/course/${course.id}`)">
            <div class="course__cover" aria-hidden="true">
              <Music :size="28" :stroke-width="2" />
            </div>
            <div class="course__body">
              <strong class="course__title">{{ course.title }}</strong>
              <p class="course__meta">{{ course.meta }} · {{ course.slotCount }} 场可约</p>
              <span class="tag">{{ course.tag }}</span>
              <span class="course__price">{{ course.price }}</span>
            </div>
            <ChevronRight :size="18" class="course__arrow" />
          </article>
        </section>

        <section v-else-if="activeTab === '老师'" class="tab-panel">
          <header class="section-title">
            <h2>老师</h2>
            <span>进入老师详情查看教学雷达图</span>
          </header>
          <article v-for="coach in coaches" :key="coach.id" class="coach-card" @click="router.push(`/coach/${coach.id}`)">
            <div class="coach-card__avatar">
              <UserRound :size="26" />
            </div>
            <div class="coach-card__body">
              <strong>{{ coach.displayName }}</strong>
              <p>{{ coachStyle(coach) }} · {{ coach.teachingStyle || '教学风格待完善' }}</p>
              <span>评分 {{ Number(coach.avgRating ?? 0).toFixed(1) }} · 点击查看 02B 老师详情</span>
            </div>
            <ChevronRight :size="18" />
          </article>
        </section>

        <section v-else-if="activeTab === '评价'" class="tab-panel">
          <ReviewAggregatePanel target-type="studio" :target-id="studioId" />
        </section>

        <section v-else class="tab-panel">
          <header class="section-title">
            <h2>课表</h2>
            <span>按时间展示可预约场次</span>
          </header>
          <article v-for="slot in visibleSchedule" :key="slot.id" class="schedule-card" @click="router.push(`/studio/${studioId}/trial?courseScheduleId=${slot.id}`)">
            <div class="schedule-card__date">
              <strong>{{ formatDate(slot.startAt) }}</strong>
              <span>{{ formatWeekday(slot.startAt) }}</span>
            </div>
            <div class="schedule-card__body">
              <strong>{{ courseNames[Math.abs(slot.courseId) % courseNames.length] }}</strong>
              <p><Clock :size="13" /> {{ formatTime(slot.startAt) }}-{{ formatTime(slot.endAt) }} · {{ slot.classroomName }}</p>
              <span>余位 {{ Math.max(slot.capacity - slot.bookedCount, 0) }} / {{ slot.capacity }}</span>
            </div>
            <button type="button">预约</button>
          </article>
        </section>
      </section>
    </section>

    <PenActionBar
      :soft-label="favored ? '已收藏' : '收藏'"
      dark-label="预约试听"
      @soft="toggleStudioFavorite"
      @dark="onBook"
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

.body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 0 18px 20px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 22px;
  background: #efece6;

  &__icon {
    display: grid;
    flex: none;
    place-items: center;
    width: 72px;
    height: 72px;
    border-radius: 20px;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &__content {
    min-width: 0;
  }

  h1 {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 900;
    line-height: $pen-lh;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.45;
  }
}

.action-row,
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-pill {
  flex: 1;
  min-width: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 44px;
  border: 0;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 14px;
  font-weight: 800;
  line-height: $pen-lh;
  cursor: pointer;
}

.tab-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: $pen-soft;

  &__item {
    height: 36px;
    border: 0;
    border-radius: 999px;
    background: transparent;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 900;
    line-height: $pen-lh;
    cursor: pointer;

    &--active {
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}

.tab-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.chip {
  @include pen-chip;
}

.intro-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: $pen-soft;

  p {
    margin: 0;
    color: $pen-mute;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.45;
  }

  &__addr {
    display: flex;
    align-items: center;
    gap: 6px;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;

  article {
    display: flex;
    min-height: 92px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 5px;
    border-radius: 16px;
    background: $pen-soft;
    text-align: center;
  }

  strong {
    font-size: 22px;
    font-weight: 900;
    line-height: 1;
  }

  span {
    color: $pen-mute;
    font-size: 11px;
    font-weight: 800;
    line-height: $pen-lh;
  }
}

.course,
.coach-card,
.schedule-card {
  cursor: pointer;
}

.course {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 124px;

  &__cover {
    flex: none;
    display: grid;
    place-items: center;
    width: 96px;
    align-self: stretch;
    border-radius: 14px;
    background: $pen-soft;
    color: $pen-ink;
  }

  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px 0;
  }

  &__title {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__price {
    font-size: 14px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__arrow {
    flex: none;
    color: $pen-mute;
  }
}

.tag {
  align-self: flex-start;
  height: 34px;
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
}

.coach-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid $pen-hairline;
  border-radius: 18px;
  background: $pen-canvas;

  &__avatar {
    display: grid;
    flex: none;
    place-items: center;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    background: $pen-ink;
    color: $pen-on-primary;
  }

  &__body {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }

  strong {
    font-size: 16px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  p,
  span {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.4;
  }

  span {
    color: $pen-ink;
  }
}

.schedule-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: $pen-soft;

  &__date {
    display: flex;
    flex: none;
    width: 54px;
    height: 54px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 16px;
    background: $pen-ink;
    color: $pen-on-primary;

    strong {
      font-size: 14px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      font-size: 11px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }

  &__body {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;

    strong {
      font-size: 15px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    p,
    span {
      display: flex;
      align-items: center;
      gap: 4px;
      margin: 0;
      color: $pen-mute;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
    }

    span {
      color: $pen-ink;
    }
  }

  button {
    flex: none;
    height: 34px;
    padding: 0 13px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 12px;
    font-weight: 900;
  }
}
</style>
