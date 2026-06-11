<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { useUserStore } from '@/stores/user';
import type { StylePreference } from '@/api/profile';

const router = useRouter();
const user = useUserStore();

const form = reactive({
  nickname: '',
  gender: 'unknown',
  birthday: '',
  bio: '',
  currentLevel: '',
  learningGoal: ''
});

const styles = ref<StylePreference[]>([]);
const loading = ref(false);
const saving = ref(false);

const styleOptions = [
  { danceStyleId: 1, name: 'Hiphop', hint: '律动、基础 groove、freestyle' },
  { danceStyleId: 2, name: 'Jazz', hint: '线条、爆发、舞台表现' },
  { danceStyleId: 3, name: 'Breaking', hint: '地板、力量、技巧组合' },
  { danceStyleId: 4, name: 'Locking', hint: '锁舞、funk、节奏控制' },
  { danceStyleId: 5, name: 'Popping', hint: '震感、控制、音乐切分' },
  { danceStyleId: 6, name: 'K-pop', hint: '成品舞、镜面扒舞、表现力' },
  { danceStyleId: 7, name: 'Waacking', hint: '手臂线条、姿态、音乐表达' },
  { danceStyleId: 8, name: 'Urban', hint: '编舞、质感、课堂组合' }
];

const genderOptions = [
  { value: 'female', label: '女' },
  { value: 'male', label: '男' },
  { value: 'unknown', label: '不公开' }
];

const levelOptions = ['beginner', 'intermediate', 'advanced'];
const levelLabel: Record<string, string> = {
  beginner: '入门',
  intermediate: '进阶',
  advanced: '高阶'
};
const levelHint: Record<string, string> = {
  beginner: '刚开始系统学习，需要基础课和节奏训练',
  intermediate: '有一定基础，想提升质感和完整作品',
  advanced: '有稳定训练经验，关注风格深度和舞台表达'
};
const goalOptions = ['零基础入门', '提升基本功', '学习成品舞', '准备比赛/演出', '塑形减脂', '认识舞友'];

const primaryStyleId = computed(() => styles.value.find((item) => item.isPrimary)?.danceStyleId);
const selectedStyleIds = computed(() => new Set(styles.value.map((item) => item.danceStyleId)));
const selectedStyleNames = computed(() =>
  styles.value.map((item) => item.name || styleOptions.find((option) => option.danceStyleId === item.danceStyleId)?.name)
    .filter(Boolean)
);

const fillForm = async () => {
  loading.value = true;
  try {
    const data = await user.refreshProfile();
    if (!data) return;
    form.nickname = data.nickname ?? '';
    form.gender = data.gender ?? 'unknown';
    form.birthday = data.birthday ?? '';
    form.bio = data.bio ?? '';
    form.currentLevel = data.currentLevel ?? '';
    form.learningGoal = data.learningGoal ?? '';
    styles.value = normalizeStyles(data.styles ?? []);
  } finally {
    loading.value = false;
  }
};

const normalizeStyles = (next: StylePreference[]) => {
  const deduped = new Map<number, StylePreference>();
  next.forEach((item) => {
    const option = styleOptions.find((it) => it.danceStyleId === item.danceStyleId);
    if (!item.danceStyleId) return;
    deduped.set(item.danceStyleId, {
      danceStyleId: item.danceStyleId,
      name: item.name || option?.name || `舞种 ${item.danceStyleId}`,
      skillLevel: item.skillLevel || form.currentLevel || 'beginner',
      isPrimary: Boolean(item.isPrimary)
    });
  });
  const out = Array.from(deduped.values());
  if (out.length && !out.some((item) => item.isPrimary)) {
    out[0] = { ...out[0], isPrimary: true };
  }
  return out.map((item, index) => ({ ...item, isPrimary: index === out.findIndex((it) => it.isPrimary) }));
};

const toggleStyle = (option: (typeof styleOptions)[number]) => {
  if (selectedStyleIds.value.has(option.danceStyleId)) {
    const next = styles.value.filter((item) => item.danceStyleId !== option.danceStyleId);
    styles.value = normalizeStyles(next);
    return;
  }
  styles.value = normalizeStyles([
    ...styles.value,
    {
      danceStyleId: option.danceStyleId,
      name: option.name,
      skillLevel: form.currentLevel || 'beginner',
      isPrimary: !styles.value.length
    }
  ]);
};

const setPrimary = (danceStyleId: number) => {
  styles.value = styles.value.map((item) => ({
    ...item,
    isPrimary: item.danceStyleId === danceStyleId
  }));
};

const setStyleLevel = (danceStyleId: number, skillLevel: string) => {
  styles.value = styles.value.map((item) =>
    item.danceStyleId === danceStyleId ? { ...item, skillLevel } : item
  );
};

const applyGoalOption = (goal: string) => {
  const parts = form.learningGoal
    .split(/[、,，]/)
    .map((item) => item.trim())
    .filter(Boolean);
  if (parts.includes(goal)) {
    form.learningGoal = parts.filter((item) => item !== goal).join('、');
    return;
  }
  form.learningGoal = [...parts, goal].join('、');
};

const isGoalActive = (goal: string) =>
  form.learningGoal
    .split(/[、,，]/)
    .map((item) => item.trim())
    .includes(goal);

const onSave = async () => {
  saving.value = true;
  try {
    const normalizedStyles = normalizeStyles(styles.value);
    styles.value = normalizedStyles;
    await user.saveProfileDetail({
      ...(user.detail ?? {}),
      nickname: form.nickname.trim(),
      gender: form.gender,
      birthday: form.birthday || null,
      bio: form.bio,
      currentLevel: form.currentLevel,
      learningGoal: form.learningGoal,
      styles: normalizedStyles
    });
    showSuccessToast('资料已保存');
    router.back();
  } finally {
    saving.value = false;
  }
};

onMounted(fillForm);
</script>

<template>
  <main class="pen-page pen-page--with-bar">
    <PenTopBar title="资料与偏好" :show-share="false" />

    <section class="pen-scroll">
      <p v-if="loading" class="empty">正在读取后端资料...</p>

      <div class="avatar">
        <span class="avatar__img" aria-hidden="true">{{ (form.nickname || 'B').slice(0, 1) }}</span>
        <span class="avatar__hint">头像资源 ID：{{ user.detail?.avatarAssetId ?? '未绑定' }}</span>
      </div>

      <label class="field">
        <span>昵称</span>
        <input v-model="form.nickname" maxlength="30" placeholder="填写昵称" />
      </label>

      <label class="field">
        <span>生日</span>
        <input v-model="form.birthday" type="date" />
      </label>

      <label class="field">
        <span>个人简介</span>
        <textarea v-model="form.bio" maxlength="160" rows="3" placeholder="介绍一下你的舞蹈状态" />
      </label>

      <section class="block">
        <h2 class="block__title">性别</h2>
        <div class="chip-row">
          <button
            v-for="item in genderOptions"
            :key="item.value"
            class="chip"
            :class="form.gender === item.value ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="form.gender = item.value"
          >
            {{ item.label }}
          </button>
        </div>
      </section>

      <section class="block">
        <h2 class="block__title">当前水平</h2>
        <p class="block__meta">
          {{ form.currentLevel ? levelHint[form.currentLevel] : '选择一个最贴近当前状态的水平，用于推荐课程、约练和活动。' }}
        </p>
        <div class="chip-row">
          <button
            v-for="item in levelOptions"
            :key="item"
            class="chip"
            :class="form.currentLevel === item ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="form.currentLevel = item"
          >
            {{ levelLabel[item] }}
          </button>
        </div>
      </section>

      <label class="field">
        <span>学习目标</span>
        <input v-model="form.learningGoal" maxlength="80" placeholder="例如：成品舞、比赛、塑形" />
      </label>

      <section class="block">
        <h2 class="block__title block__title--compact">目标快捷选择</h2>
        <div class="chip-row">
          <button
            v-for="goal in goalOptions"
            :key="goal"
            class="chip"
            :class="isGoalActive(goal) ? 'chip--active' : 'chip--inactive'"
            type="button"
            @click="applyGoalOption(goal)"
          >
            {{ goal }}
          </button>
        </div>
      </section>

      <section class="block">
        <div class="block__heading">
          <h2 class="block__title">感兴趣的舞种</h2>
          <span>{{ styles.length ? `已选 ${styles.length} 项` : '至少选 1 项更好推荐' }}</span>
        </div>
        <p class="block__meta">
          {{ selectedStyleNames.length ? `当前偏好：${selectedStyleNames.join(' / ')}` : '选择你想学或正在练的舞种，可以多选。' }}
        </p>

        <div class="style-picker">
          <button
            v-for="option in styleOptions"
            :key="option.danceStyleId"
            class="style-option"
            :class="{ 'style-option--active': selectedStyleIds.has(option.danceStyleId) }"
            type="button"
            @click="toggleStyle(option)"
          >
            <strong>{{ option.name }}</strong>
            <span>{{ option.hint }}</span>
          </button>
        </div>

        <p v-if="!styles.length" class="empty">还没有选择舞种，保存前建议至少选择一个主要兴趣。</p>
        <article v-for="style in styles" :key="style.danceStyleId" class="style-card">
          <header>
            <strong>{{ style.name || `舞种 ${style.danceStyleId}` }}</strong>
            <button
              type="button"
              :class="{ 'is-primary': primaryStyleId === style.danceStyleId }"
              @click="setPrimary(style.danceStyleId)"
            >
              {{ primaryStyleId === style.danceStyleId ? '主舞种' : '设为主舞种' }}
            </button>
          </header>
          <p>这个舞种的当前水平</p>
          <div class="chip-row">
            <button
              v-for="item in levelOptions"
              :key="item"
              class="chip"
              :class="style.skillLevel === item ? 'chip--active' : 'chip--inactive'"
              type="button"
              @click="setStyleLevel(style.danceStyleId, item)"
            >
              {{ levelLabel[item] }}
            </button>
          </div>
        </article>
      </section>
    </section>

    <footer class="save-bar">
      <button class="save-bar__btn" type="button" :disabled="saving" @click="onSave">
        {{ saving ? '保存中...' : '保存资料' }}
      </button>
    </footer>
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
  padding: 16px 18px;
}

.avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  &__img {
    display: grid;
    width: 84px;
    height: 84px;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 28px;
    font-weight: 900;
    place-items: center;
  }

  &__hint {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 800;
  }
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;

  span {
    color: $pen-mute;
    font-size: 13px;
    font-weight: 800;
  }

  input,
  textarea {
    width: 100%;
    border: 1px solid $pen-hairline;
    border-radius: 8px;
    background: $pen-canvas;
    color: $pen-ink;
    font: inherit;
    font-size: 15px;
    font-weight: 800;
    line-height: 1.4;
    outline: none;
    box-sizing: border-box;
  }

  input {
    height: 46px;
    padding: 0 12px;
  }

  textarea {
    resize: vertical;
    min-height: 86px;
    padding: 11px 12px;
  }
}

.block {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__title {
    @include pen-h3-section;
    margin-top: 4px;

    &--compact {
      font-size: 17px;
    }
  }

  &__heading {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      flex: 1;
      min-width: 0;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      line-height: $pen-lh;
      font-weight: 800;
      white-space: nowrap;
    }
  }

  &__meta {
    margin: -4px 0 2px;
    color: $pen-mute;
    font-size: 12px;
    line-height: 1.45;
    font-weight: 700;
  }
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  @include pen-chip;
}

.style-picker {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.style-option {
  min-height: 92px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 8px;
  background: $pen-canvas;
  color: $pen-ink;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
  text-align: left;
  cursor: pointer;
  box-sizing: border-box;

  strong,
  span {
    letter-spacing: 0;
  }

  strong {
    font-size: 15px;
    line-height: $pen-lh;
    font-weight: 900;
  }

  span {
    color: $pen-mute;
    font-size: 11px;
    line-height: 1.35;
    font-weight: 700;
  }

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;

    span {
      color: $pen-subtle-text;
    }
  }
}

.style-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border: 1px solid $pen-hairline;
  border-radius: 8px;

  header {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  strong {
    flex: 1;
    font-size: 15px;
    font-weight: 900;
  }

  p {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    line-height: $pen-lh;
    font-weight: 800;
  }

  header button {
    height: 32px;
    padding: 6px 11px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    cursor: pointer;

    &.is-primary {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }
}

@media (max-width: 360px) {
  .style-picker {
    grid-template-columns: minmax(0, 1fr);
  }
}

.empty {
  margin: 0;
  padding: 14px 12px;
  border: 1px solid $pen-hairline;
  border-radius: 8px;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 800;
  text-align: center;
}

.save-bar {
  position: fixed;
  right: 0;
  bottom: var(--app-tabbar-offset, 0px);
  left: 0;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  height: 76px;
  margin: 0 auto;
  padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
  background: $pen-canvas;
  border-top: 1px solid $pen-hairline;
  box-sizing: border-box;

  &__btn {
    width: 100%;
    @include pen-primary-btn;
  }
}
</style>
