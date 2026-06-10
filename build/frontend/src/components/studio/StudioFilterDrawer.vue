<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import { X } from 'lucide-vue-next';
import type { StudioListQuery } from '@/api/studio';

export interface StudioFilterValue {
  danceStyleId?: number;
  distanceKm?: number;
  minPrice?: number;
  maxPrice?: number;
  timeSlot?: StudioListQuery['timeSlot'];
  trialAvailable?: boolean;
  zeroBasicFriendly?: boolean;
  nearMetro?: boolean;
}

const props = defineProps<{
  visible: boolean;
  value: StudioFilterValue;
  resultCount?: number;
}>();

const emit = defineEmits<{
  close: [];
  apply: [value: StudioFilterValue];
}>();

const styles = [
  { id: undefined, label: '不限' },
  { id: 1, label: 'Hiphop' },
  { id: 2, label: 'Jazz' },
  { id: 3, label: 'Breaking' },
  { id: 4, label: 'Locking' },
  { id: 5, label: 'Popping' },
  { id: 6, label: 'K-pop' }
];
const distances = [1, 3, 5, 10];
const priceRanges = [
  { label: '不限', min: undefined, max: undefined },
  { label: '¥80 以下', min: undefined, max: 80 },
  { label: '¥80-150', min: 80, max: 150 },
  { label: '¥150-250', min: 150, max: 250 },
  { label: '¥250 以上', min: 250, max: undefined }
];
const slots: Array<{ id: StudioListQuery['timeSlot']; label: string }> = [
  { id: 'morning', label: '上午' },
  { id: 'afternoon', label: '下午' },
  { id: 'evening', label: '晚上' },
  { id: 'weekend', label: '周末' }
];
const draft = reactive<StudioFilterValue>({});
const emptyValue = (): StudioFilterValue => ({ distanceKm: 5 });
const replaceDraft = (value: StudioFilterValue) => {
  for (const key of Object.keys(draft) as Array<keyof StudioFilterValue>) delete draft[key];
  Object.assign(draft, value);
};

watch(
  () => [props.visible, props.value] as const,
  () => replaceDraft({ ...emptyValue(), ...props.value }),
  { immediate: true, deep: true }
);

const priceText = computed(() => `¥${draft.minPrice ?? 0} - ¥${draft.maxPrice ?? 500}`);
const isPriceRangeActive = (min?: number, max?: number) => draft.minPrice === min && draft.maxPrice === max;
const setPriceRange = (min?: number, max?: number) => {
  draft.minPrice = min;
  draft.maxPrice = max;
};
const reset = () => replaceDraft(emptyValue());
const apply = () => {
  const minPrice = draft.minPrice ?? 0;
  const maxPrice = draft.maxPrice ?? 500;
  const normalizedMin = Math.min(minPrice, maxPrice);
  const normalizedMax = Math.max(minPrice, maxPrice);
  emit('apply', {
    ...draft,
    minPrice: normalizedMin === 0 ? undefined : normalizedMin,
    maxPrice: normalizedMax === 500 ? undefined : normalizedMax
  });
};
</script>

<template>
  <Teleport to="body">
    <Transition name="filter-fade">
      <div
        v-if="visible"
        class="filter-mask"
        role="button"
        tabindex="0"
        aria-label="关闭筛选"
        @pointerdown.prevent="emit('close')"
        @keydown.esc="emit('close')"
      />
    </Transition>
    <Transition name="filter-slide">
      <aside v-if="visible" class="filter-drawer" aria-label="筛选舞室">
        <div class="filter-drawer__handle" />
        <header class="filter-drawer__head">
          <h2>筛选舞室</h2>
          <button type="button" class="filter-drawer__close" aria-label="关闭筛选" @click="emit('close')">
            <X :size="17" :stroke-width="2.5" />
          </button>
        </header>

        <div class="filter-drawer__body">
          <section>
            <h3>主打舞种</h3>
            <div class="option-row option-row--wrap">
              <button v-for="item in styles" :key="item.label" type="button" :class="{ active: draft.danceStyleId === item.id }" @click="draft.danceStyleId = item.id">
                {{ item.label }}
              </button>
            </div>
          </section>

          <section>
            <h3>距离范围</h3>
            <div class="option-row">
              <button v-for="distance in distances" :key="distance" type="button" :class="{ active: draft.distanceKm === distance }" @click="draft.distanceKm = distance">
                {{ distance }}km
              </button>
              <button type="button" :class="{ active: !draft.distanceKm }" @click="draft.distanceKm = undefined">不限</button>
            </div>
          </section>

          <section>
            <div class="filter-drawer__section-head">
              <h3>人均价格</h3>
              <strong>{{ draft.minPrice !== undefined || draft.maxPrice !== undefined ? priceText : '不限' }}</strong>
            </div>
            <div class="option-row option-row--wrap">
              <button
                v-for="range in priceRanges"
                :key="range.label"
                type="button"
                :class="{ active: isPriceRangeActive(range.min, range.max) }"
                @click="setPriceRange(range.min, range.max)"
              >
                {{ range.label }}
              </button>
            </div>
            <div class="price-custom">
              <label>
                <span>最低价</span>
                <input v-model.number="draft.minPrice" type="number" min="0" max="500" placeholder="¥0" />
              </label>
              <i />
              <label>
                <span>最高价</span>
                <input v-model.number="draft.maxPrice" type="number" min="0" max="500" placeholder="¥500" />
              </label>
            </div>
          </section>

          <section>
            <h3>可约时段</h3>
            <div class="option-row">
              <button v-for="slot in slots" :key="slot.id" type="button" :class="{ active: draft.timeSlot === slot.id }" @click="draft.timeSlot = draft.timeSlot === slot.id ? undefined : slot.id">
                {{ slot.label }}
              </button>
            </div>
          </section>

          <section>
            <h3>舞室属性</h3>
            <div class="option-row option-row--wrap">
              <button type="button" :class="{ active: draft.trialAvailable }" @click="draft.trialAvailable = !draft.trialAvailable">可试听</button>
              <button type="button" :class="{ active: draft.zeroBasicFriendly }" @click="draft.zeroBasicFriendly = !draft.zeroBasicFriendly">新手友好</button>
              <button type="button" disabled title="数据库暂未提供淋浴设施字段">有淋浴</button>
              <button type="button" :class="{ active: draft.nearMetro }" @click="draft.nearMetro = !draft.nearMetro">近地铁</button>
            </div>
          </section>
        </div>

        <footer class="filter-drawer__foot">
          <button type="button" class="filter-drawer__reset" @click="reset">重置</button>
          <button type="button" class="filter-drawer__apply" @click="apply">
            {{ resultCount === undefined ? '查看结果' : `查看 ${resultCount} 家舞室` }}
          </button>
        </footer>
      </aside>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.filter-mask {
  position: fixed;
  top: 0;
  right: 0;
  /* M1 搜索筛选：遮罩覆盖到底部，避免抽屉打开时地图区域仍可被误操作。 */
  bottom: 0;
  left: 0;
  z-index: 120;
  background: rgb(17 17 17 / 42%);
  backdrop-filter: blur(5px);
}

.filter-drawer {
  position: fixed;
  right: 0;
  /* M1 搜索筛选：抽屉贴底，和全屏遮罩形成同一可操作范围。 */
  bottom: 0;
  left: 0;
  z-index: 130;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  max-height: min(620px, calc(100vh - 168px - env(safe-area-inset-bottom)));
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
    padding: 4px 18px 10px;
  }

  &__head h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 900;
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
    gap: 14px;
    overflow-y: auto;
    padding: 4px 18px 14px;
  }

  &__body section {
    display: flex;
    flex-direction: column;
    gap: 9px;
  }

  h3 {
    margin: 0;
    font-size: 14px;
    font-weight: 800;
  }

  &__section-head {
    justify-content: space-between;
  }

  &__section-head strong {
    color: $pen-success;
    font-size: 13px;
  }

  &__foot {
    gap: 10px;
    padding: 12px 18px;
    border-top: 1px solid $pen-hairline;
  }

  &__reset,
  &__apply {
    height: 48px;
    border: 0;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 800;
  }

  &__reset {
    width: 104px;
    background: $pen-soft;
  }

  &__apply {
    flex: 1;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.option-row {
  display: flex;
  gap: 8px;

  &--wrap {
    flex-wrap: wrap;
  }

  button {
    height: 34px;
    padding: 8px 13px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 700;

    &.active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: $pen-on-primary;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.42;
    }
  }
}

.price-custom {
  display: flex;
  align-items: center;
  gap: 10px;

  label {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 6px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid $pen-hairline;
    border-radius: 8px;
    background: $pen-canvas;
  }

  span {
    flex: none;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
  }

  input {
    width: 100%;
    min-width: 0;
    border: 0;
    outline: 0;
    background: transparent;
    color: $pen-ink;
    font-size: 13px;
    font-weight: 800;
  }

  i {
    width: 10px;
    height: 1px;
    background: $pen-hairline-strong;
  }
}

.filter-fade-enter-active,
.filter-fade-leave-active,
.filter-slide-enter-active,
.filter-slide-leave-active {
  transition: 0.22s ease;
}

.filter-fade-enter-from,
.filter-fade-leave-to {
  opacity: 0;
}

.filter-slide-enter-from,
.filter-slide-leave-to {
  transform: translateY(100%);
}
</style>
