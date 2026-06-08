<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Search, X } from 'lucide-vue-next';
import { CITY_OPTIONS, getCityName } from '@/constants/cities';

export interface StudioSearchEditorValue {
  keyword?: string;
  cityId?: number;
  useNearby: boolean;
}

const props = defineProps<{
  visible: boolean;
  value: StudioSearchEditorValue;
  resultCount?: number;
}>();

const emit = defineEmits<{
  close: [];
  apply: [value: StudioSearchEditorValue];
}>();

const draftKeyword = ref('');
const draftUseNearby = ref(true);
const draftCityId = ref<number>();

const resetDraft = (value: StudioSearchEditorValue) => {
  draftKeyword.value = value.keyword ?? '';
  draftUseNearby.value = value.useNearby;
  draftCityId.value = value.cityId;
};

watch(
  () => [props.visible, props.value] as const,
  () => resetDraft(props.value),
  { immediate: true, deep: true }
);

const currentScopeLabel = computed(() =>
  draftUseNearby.value ? '附近结果' : getCityName(draftCityId.value) || '指定城市'
);

const currentMeta = computed(() => {
  if (!draftKeyword.value.trim()) return `${currentScopeLabel.value} · 不限关键词`;
  return `${currentScopeLabel.value} · 关键词「${draftKeyword.value.trim()}」`;
});

const apply = () => {
  emit('apply', {
    keyword: draftKeyword.value.trim() || undefined,
    cityId: draftUseNearby.value ? undefined : draftCityId.value,
    useNearby: draftUseNearby.value
  });
};

const reset = () => {
  draftKeyword.value = '';
  draftUseNearby.value = true;
  draftCityId.value = undefined;
};

const pickNearby = () => {
  draftUseNearby.value = true;
  draftCityId.value = undefined;
};

const pickCity = (cityId: number) => {
  draftUseNearby.value = false;
  draftCityId.value = cityId;
};
</script>

<template>
  <Teleport to="body">
    <Transition name="search-editor-fade">
      <div
        v-if="visible"
        class="search-editor-mask"
        role="button"
        tabindex="0"
        aria-label="关闭修改搜索"
        @pointerdown.prevent="emit('close')"
        @keydown.esc="emit('close')"
      />
    </Transition>
    <Transition name="search-editor-slide">
      <aside v-if="visible" class="search-editor" aria-label="修改搜索">
        <div class="search-editor__handle" />
        <header class="search-editor__head">
          <div class="search-editor__title">
            <h2>修改搜索</h2>
            <p>{{ currentMeta }}</p>
          </div>
          <button type="button" class="search-editor__close" aria-label="关闭修改搜索" @click="emit('close')">
            <X :size="17" :stroke-width="2.5" />
          </button>
        </header>

        <div class="search-editor__body">
          <section class="search-editor__section">
            <div class="search-editor__section-head">
              <h3>关键词</h3>
              <span>可搜舞室名、地址、舞种</span>
            </div>
            <label class="search-editor__input">
              <Search :size="16" :stroke-width="2" />
              <input
                v-model="draftKeyword"
                type="text"
                maxlength="30"
                placeholder="比如：朝阳、Jazz、零基础"
              />
            </label>
          </section>

          <section class="search-editor__section">
            <div class="search-editor__section-head">
              <h3>搜索范围</h3>
              <span>附近和指定城市二选一</span>
            </div>
            <div class="search-editor__chips">
              <button
                type="button"
                class="search-editor__chip"
                :class="{ 'search-editor__chip--active': draftUseNearby }"
                @click="pickNearby"
              >
                附近
              </button>
              <button
                v-for="city in CITY_OPTIONS"
                :key="city.id"
                type="button"
                class="search-editor__chip"
                :class="{ 'search-editor__chip--active': !draftUseNearby && draftCityId === city.id }"
                @click="pickCity(city.id)"
              >
                {{ city.name }}
              </button>
            </div>
          </section>
        </div>

        <footer class="search-editor__foot">
          <button type="button" class="search-editor__reset" @click="reset">重置</button>
          <button type="button" class="search-editor__apply" @click="apply">
            {{ resultCount === undefined ? '刷新结果' : `查看 ${resultCount} 家舞室` }}
          </button>
        </footer>
      </aside>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.search-editor-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: calc(72px + env(safe-area-inset-bottom));
  left: 0;
  z-index: 120;
  background: rgb(17 17 17 / 42%);
  backdrop-filter: blur(5px);
}

.search-editor {
  position: fixed;
  right: 0;
  bottom: calc(72px + env(safe-area-inset-bottom));
  left: 0;
  z-index: 130;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  max-height: min(520px, calc(100vh - 190px - env(safe-area-inset-bottom)));
  margin: 0 auto;
  overflow: hidden;
  border-radius: 24px 24px 0 0;
  background: $pen-canvas;
  box-shadow: 0 -4px 18px rgb(0 0 0 / 12%);

  &__handle {
    width: 42px;
    height: 4px;
    margin: 10px auto 6px;
    border-radius: 999px;
    background: $pen-hairline-strong;
  }

  &__head,
  &__section-head,
  &__foot {
    display: flex;
    align-items: center;
  }

  &__head {
    justify-content: space-between;
    gap: 12px;
    padding: 4px 18px 12px;
  }

  &__title {
    min-width: 0;

    h2,
    p {
      margin: 0;
    }

    h2 {
      font-size: 20px;
      font-weight: 900;
      line-height: $pen-lh;
      color: $pen-ink;
    }

    p {
      margin-top: 4px;
      color: $pen-mute;
      font-size: 13px;
      font-weight: 600;
      line-height: $pen-lh;
    }
  }

  &__close {
    display: grid;
    width: 32px;
    height: 32px;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    place-items: center;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 18px;
    overflow-y: auto;
    padding: 0 18px 18px;
  }

  &__section {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__section-head {
    justify-content: space-between;
    gap: 12px;

    h3,
    span {
      margin: 0;
    }

    h3 {
      color: $pen-ink;
      font-size: 15px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 600;
      line-height: $pen-lh;
      text-align: right;
    }
  }

  &__input {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 48px;
    padding: 0 14px;
    border-radius: 18px;
    background: $pen-soft;
    color: $pen-ink;

    input {
      width: 100%;
      border: 0;
      outline: 0;
      background: transparent;
      color: $pen-ink;
      font-size: 15px;
      font-weight: 700;
      line-height: $pen-lh;
    }

    input::placeholder {
      color: $pen-mute;
      font-weight: 600;
    }
  }

  &__chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  &__chip {
    min-height: 40px;
    padding: 8px 16px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-canvas;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 800;
    line-height: $pen-lh;

    &--active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;
    }
  }

  &__foot {
    gap: 10px;
    padding: 12px 18px calc(12px + env(safe-area-inset-bottom));
    border-top: 1px solid $pen-hairline;
  }

  &__reset,
  &__apply {
    flex: 1;
    height: 48px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &__reset {
    background: $pen-soft;
    color: $pen-ink;
  }

  &__apply {
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.search-editor-fade-enter-active,
.search-editor-fade-leave-active,
.search-editor-slide-enter-active,
.search-editor-slide-leave-active {
  transition: all 0.2s ease;
}

.search-editor-fade-enter-from,
.search-editor-fade-leave-to {
  opacity: 0;
}

.search-editor-slide-enter-from,
.search-editor-slide-leave-to {
  transform: translateY(100%);
}
</style>
