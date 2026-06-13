<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, ImagePlus, Plus, CircleCheckBig, Trash2 } from 'lucide-vue-next';
import { createCoachWorkshop, type CreateWorkshopPayload } from '@/api/coachOps';

const router = useRouter();
const submitting = ref(false);

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const STYLE_IDS: Record<string, number> = { Hiphop: 1, Jazz: 2, Breaking: 3, Locking: 4, Popping: 5, Kpop: 6, Waacking: 7 };
const CITY_IDS: Record<string, number> = { 北京: 1, 上海: 2, 广州: 3, 深圳: 4, 杭州: 5 };
const CITIES = Object.keys(CITY_IDS);

const form = reactive({
  title: '',
  style: 'Locking',
  intro: '',
  city: '北京',
  locationName: '',
  address: '',
  price: 199,
  maxPeople: 20,
  signupDeadline: '',
});

interface SessionDraft {
  date: string;
  startTime: string;
  endTime: string;
  capacity: number;
}

const sessions = ref<SessionDraft[]>([
  { date: nextDate(2), startTime: '14:00', endTime: '16:00', capacity: 20 }
]);

function nextDate(daysAhead: number) {
  const d = new Date();
  d.setDate(d.getDate() + daysAhead);
  return d.toISOString().slice(0, 10);
}

const addSession = () => {
  const last = sessions.value[sessions.value.length - 1];
  const baseDate = last?.date || nextDate(3);
  const d = new Date(`${baseDate}T00:00:00`);
  d.setDate(d.getDate() + 1);
  sessions.value.push({
    date: d.toISOString().slice(0, 10),
    startTime: last?.startTime || '14:00',
    endTime: last?.endTime || '16:00',
    capacity: form.maxPeople
  });
};

const removeSession = (index: number) => {
  if (sessions.value.length > 1) sessions.value.splice(index, 1);
};

const canSubmit = computed(() =>
  form.title.trim().length >= 2 &&
  form.style &&
  form.city &&
  form.locationName.trim() &&
  form.address.trim() &&
  form.price > 0 &&
  form.maxPeople >= 1 &&
  sessions.value.length > 0 &&
  sessions.value.every((s) => s.date && s.startTime && s.endTime) &&
  !submitting.value
);

const toIso = (date: string, time: string) => {
  const [h, m] = time.split(':').map(Number);
  const d = new Date(`${date}T00:00:00+08:00`);
  d.setHours(h, m, 0, 0);
  return d.toISOString();
};

const onPublish = async () => {
  if (!canSubmit.value) {
    showFailToast('请先补全 Workshop 信息');
    return;
  }
  submitting.value = true;
  try {
    const payload: CreateWorkshopPayload = {
      studioId: 1,
      cityId: CITY_IDS[form.city] ?? 1,
      danceStyleId: STYLE_IDS[form.style] ?? 1,
      workshopName: form.title.trim(),
      intro: form.intro.trim() || form.title.trim(),
      address: form.address.trim(),
      locationName: form.locationName.trim(),
      priceAmount: form.price,
      minPeople: 1,
      maxPeople: form.maxPeople,
      signupDeadline: form.signupDeadline ? toIso(form.signupDeadline, '23:59') : null,
      sourceType: 'coach'
    };

    const resp = await createCoachWorkshop(payload);
    showSuccessToast(`Workshop 已创建 #${resp.id}`);
    router.back();
  } catch {
    showFailToast('创建失败，请检查信息或权限');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <h1 class="topbar__title">创建 Workshop</h1>
      <button class="topbar__pub" type="button" :disabled="!canSubmit" @click="onPublish">
        {{ submitting ? '提交中...' : '发布' }}
      </button>
    </header>

    <section class="pen-scroll">
      <button class="cover" type="button">
        <ImagePlus :size="30" :stroke-width="2" />
        <span>上传活动封面</span>
      </button>

      <div class="rows">
        <label class="field-row">
          <span>标题</span>
          <input v-model="form.title" type="text" placeholder="例如：Locking Workshop 基础训练营" />
        </label>

        <div class="field-row">
          <span>舞种</span>
          <div class="chips">
            <button
              v-for="s in STYLES"
              :key="s"
              class="chip"
              :class="{ 'chip--active': form.style === s }"
              type="button"
              @click="form.style = s"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div class="field-row">
          <span>城市</span>
          <div class="chips">
            <button
              v-for="c in CITIES"
              :key="c"
              class="chip"
              :class="{ 'chip--active': form.city === c }"
              type="button"
              @click="form.city = c"
            >
              {{ c }}
            </button>
          </div>
        </div>

        <label class="field-row">
          <span>场地名称</span>
          <input v-model="form.locationName" type="text" placeholder="例如：Urban Flow 舞室" />
        </label>

        <label class="field-row">
          <span>详细地址</span>
          <input v-model="form.address" type="text" placeholder="例如：朝阳区三里屯 SOHO" />
        </label>

        <label class="field-row">
          <span>活动介绍</span>
          <textarea v-model="form.intro" rows="3" placeholder="介绍活动内容、适合人群等" />
        </label>

        <div class="two-col">
          <label class="field-row">
            <span>价格 (¥)</span>
            <input v-model.number="form.price" type="number" min="0" />
          </label>
          <label class="field-row">
            <span>人数上限</span>
            <input v-model.number="form.maxPeople" type="number" min="1" max="500" />
          </label>
        </div>

        <label class="field-row">
          <span>报名截止日期</span>
          <input v-model="form.signupDeadline" type="date" />
        </label>
      </div>

      <h2 class="block-title">场次</h2>
      <div v-for="(s, i) in sessions" :key="i" class="session-card">
        <div class="session-card__head">
          <strong>场次 {{ i + 1 }}</strong>
          <button v-if="sessions.length > 1" type="button" class="session-card__del" @click="removeSession(i)">
            <Trash2 :size="14" :stroke-width="2" />
          </button>
        </div>
        <div class="session-card__row">
          <label><span>日期</span><input v-model="s.date" type="date" /></label>
          <label><span>开始</span><input v-model="s.startTime" type="time" /></label>
          <label><span>结束</span><input v-model="s.endTime" type="time" /></label>
        </div>
        <label class="session-card__cap">
          <span>容量</span>
          <input v-model.number="s.capacity" type="number" min="1" />
        </label>
      </div>
      <button class="add-session" type="button" @click="addSession">
        <Plus :size="18" :stroke-width="2" />
        添加场次
      </button>

      <div class="role">
        <CircleCheckBig :size="16" :stroke-width="2" />
        <span>教练身份 · 可独立发布</span>
      </div>
    </section>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page { @include pen-page; }

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__title { flex: 1; margin: 0; font-size: 18px; font-weight: 900; line-height: $pen-lh; }
  &__icon {
    width: 40px; height: 40px; flex: none;
    border: 0; border-radius: 999px; background: $pen-soft; color: $pen-ink;
    display: grid; place-items: center; cursor: pointer;
  }
  &__pub {
    flex: none; height: 36px; padding: 8px 16px;
    border: 0; border-radius: 999px; background: $pen-ink; color: $pen-on-primary;
    font-size: 14px; font-weight: 800; line-height: $pen-lh; cursor: pointer;
    &:disabled { opacity: 0.42; cursor: not-allowed; }
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 18px calc(20px + env(safe-area-inset-bottom));
}

.cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 140px;
  border: 0;
  border-radius: 14px;
  background: $pen-ink;
  color: $pen-on-primary;
  cursor: pointer;
  span { color: $pen-subtle-text; font-size: 13px; font-weight: 700; line-height: $pen-lh; }
}

.rows { display: flex; flex-direction: column; gap: 0; }

.field-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 0;
  border-bottom: 1px solid $pen-hairline;

  > span {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  input, textarea {
    width: 100%;
    border: 0;
    background: transparent;
    color: $pen-ink;
    font-family: inherit;
    font-size: 16px;
    font-weight: 800;
    line-height: $pen-lh;
    outline: none;
    box-sizing: border-box;
  }

  textarea {
    resize: none;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.45;
  }
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  height: 34px;
  padding: 0 12px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.block-title {
  margin: 8px 0 0;
  font-size: 18px;
  font-weight: 900;
  line-height: $pen-lh;
}

.session-card {
  padding: 14px;
  border-radius: 14px;
  background: $pen-soft;
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    strong { font-size: 14px; font-weight: 900; }
  }

  &__del {
    width: 28px; height: 28px;
    border: 0; border-radius: 999px;
    background: $pen-canvas; color: $pen-mute;
    display: grid; place-items: center; cursor: pointer;
  }

  &__row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 8px;

    label {
      display: flex;
      flex-direction: column;
      gap: 4px;

      span { color: $pen-mute; font-size: 11px; font-weight: 800; }
      input {
        height: 36px;
        padding: 0 8px;
        border: 1px solid $pen-hairline;
        border-radius: 8px;
        background: $pen-canvas;
        color: $pen-ink;
        font: inherit;
        font-size: 14px;
        font-weight: 800;
        outline: none;
        box-sizing: border-box;
      }
    }
  }

  &__cap {
    display: flex;
    align-items: center;
    gap: 8px;
    span { color: $pen-mute; font-size: 11px; font-weight: 800; }
    input {
      width: 60px;
      height: 36px;
      padding: 0 8px;
      border: 1px solid $pen-hairline;
      border-radius: 8px;
      background: $pen-canvas;
      color: $pen-ink;
      font: inherit;
      font-size: 14px;
      font-weight: 800;
      outline: none;
      box-sizing: border-box;
    }
  }
}

.add-session {
  height: 44px;
  border: 1px dashed $pen-hairline;
  border-radius: 999px;
  background: $pen-canvas;
  color: $pen-ink;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.role {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 6px 14px;
  border: 1px solid $pen-success;
  border-radius: 999px;
  color: $pen-success;
  font-size: 13px;
  font-weight: 700;
  line-height: $pen-lh;
}
</style>
