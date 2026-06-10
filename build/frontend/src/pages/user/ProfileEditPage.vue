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

const primaryStyleId = computed(() => styles.value.find((item) => item.isPrimary)?.danceStyleId);

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
    styles.value = (data.styles ?? []).map((item) => ({ ...item }));
  } finally {
    loading.value = false;
  }
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

const onSave = async () => {
  saving.value = true;
  try {
    await user.saveProfileDetail({
      ...(user.detail ?? {}),
      nickname: form.nickname.trim(),
      gender: form.gender,
      birthday: form.birthday || null,
      bio: form.bio,
      currentLevel: form.currentLevel,
      learningGoal: form.learningGoal,
      styles: styles.value
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
        <h2 class="block__title">舞蹈偏好</h2>
        <p v-if="!styles.length" class="empty">数据库暂无偏好舞种，可由种子数据或后台补充后展示。</p>
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
