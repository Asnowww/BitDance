<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showSuccessToast } from 'vant';
import { fetchMyTrialBookings, cancelTrialBooking, type TrialBooking, type TrialStatus } from '@/api/trial';

const router = useRouter();
const list = ref<TrialBooking[]>([]);
const loading = ref(true);

const STATUS_LABEL: Record<TrialStatus, string> = {
  pending: '待确认',
  confirmed: '已确认',
  rejected: '已拒绝',
  arrived: '已到店',
  noshow: '已失约',
  canceled: '已取消'
};

const reload = async () => {
  loading.value = true;
  try {
    list.value = await fetchMyTrialBookings();
  } finally {
    loading.value = false;
  }
};

const onCancel = async (item: TrialBooking) => {
  await showConfirmDialog({ title: '取消预约？', message: '取消后无法恢复' }).catch(() => {
    throw new Error('cancel');
  });
  await cancelTrialBooking(item.id);
  showSuccessToast('已取消');
  void reload();
};

onMounted(reload);
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">我的试听</span>
    </header>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!list.length" class="empty">还没有预约任何试听</div>
    <article v-for="it in list" :key="it.id" class="item">
      <div class="item__head">
        <span class="item__title" @click="router.push(`/studio/${it.studioId}`)">{{ it.studioName }}</span>
        <span class="status" :data-status="it.status">{{ STATUS_LABEL[it.status] }}</span>
      </div>
      <div class="item__meta">
        <span>{{ it.date }} {{ it.time }}</span>
      </div>
      <div v-if="it.courseName" class="item__sub">课程：{{ it.courseName }}</div>
      <div v-if="it.coachName" class="item__sub">教练：{{ it.coachName }}</div>
      <div v-if="it.remark" class="item__sub">备注：{{ it.remark }}</div>
      <footer v-if="it.status === 'pending' || it.status === 'confirmed'" class="item__foot">
        <button class="btn-ghost" @click="onCancel(it)">取消预约</button>
      </footer>
    </article>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 24px;
}
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    font-size: 16px;
    font-weight: 600;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.empty {
  padding: 60px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
.item {
  margin: 8px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  &__title {
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
  }
  &__meta {
    margin-top: 6px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--bd-text-secondary);
  }
  &__foot {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
  }
}
.status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 170, 51, 0.15);
  color: #c87a00;
  &[data-status='confirmed'] {
    background: rgba(54, 165, 255, 0.12);
    color: #36a5ff;
  }
  &[data-status='canceled'],
  &[data-status='rejected'],
  &[data-status='noshow'] {
    background: #f3f3f3;
    color: var(--bd-text-secondary);
  }
  &[data-status='arrived'] {
    background: rgba(0, 168, 84, 0.12);
    color: #00a854;
  }
}
.btn-ghost {
  border: 1px solid var(--bd-border);
  background: #fff;
  color: var(--bd-text-secondary);
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
}
</style>
