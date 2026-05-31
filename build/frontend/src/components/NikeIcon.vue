<script setup lang="ts">
import { computed } from 'vue';

const iconPaths = {
  activity: [
    'M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2'
  ],
  bag: [
    'M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z',
    'M3 6h18',
    'M16 10a4 4 0 0 1-8 0'
  ],
  bell: [
    'M10.268 21a2 2 0 0 0 3.464 0',
    'M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.674C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326Z'
  ],
  'calendar-check': [
    'M8 2v4',
    'M16 2v4',
    'M3 10h18',
    'M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z',
    'm9 16 2 2 4-4'
  ],
  'chevron-right': ['m9 18 6-6-6-6'],
  heart: [
    'M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z'
  ],
  search: [
    'm21 21-4.34-4.34',
    'M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15Z'
  ],
  shield: [
    'M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.68 0C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.5 3.8 17 5 19 5a1 1 0 0 1 1 1Z'
  ],
  sparkles: [
    'M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .962 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.582a.5.5 0 0 1 0 .962L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.962 0Z',
    'M20 3v4',
    'M22 5h-4',
    'M4 17v2',
    'M5 18H3'
  ],
  star: [
    'M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.12 2.12 0 0 0 1.595 1.16l5.166.751a.53.53 0 0 1 .294.904l-3.738 3.644a2.12 2.12 0 0 0-.611 1.878l.882 5.145a.53.53 0 0 1-.771.56l-4.62-2.428a2.12 2.12 0 0 0-1.964 0l-4.62 2.428a.53.53 0 0 1-.77-.56l.881-5.145a2.12 2.12 0 0 0-.611-1.878L2.16 9.789a.53.53 0 0 1 .294-.904l5.165-.752a2.12 2.12 0 0 0 1.596-1.16Z'
  ],
  user: [
    'M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2',
    'M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z'
  ],
  users: [
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2',
    'M16 3.128a4 4 0 0 1 0 7.744',
    'M22 21v-2a4 4 0 0 0-3-3.87',
    'M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z'
  ]
} as const;

type NikeIconName = keyof typeof iconPaths;

const props = withDefaults(
  defineProps<{
    name: NikeIconName;
    size?: number | string;
    strokeWidth?: number;
  }>(),
  {
    size: 20,
    strokeWidth: 2
  }
);

const paths = computed(() => iconPaths[props.name]);
const iconSize = computed(() => (typeof props.size === 'number' ? String(props.size) : props.size));
</script>

<template>
  <svg
    class="nike-icon"
    :width="iconSize"
    :height="iconSize"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path v-for="path in paths" :key="path" :d="path" />
  </svg>
</template>

<style scoped>
.nike-icon {
  display: block;
  flex: none;
}
</style>
