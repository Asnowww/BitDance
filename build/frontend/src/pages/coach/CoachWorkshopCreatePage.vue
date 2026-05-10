<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import { createCoachWorkshop } from '@/api/coachOps';

const router = useRouter();

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop'];

const title = ref('');
const styles = ref<string[]>([]);
const intro = ref('');
const startDate = ref(new Date().toISOString().slice(0, 10));
const endDate = ref(new Date().toISOString().slice(0, 10));
const city = ref('北京');
const area = ref('海淀区');
const sessions = ref<Array<{ date: string; startTime: string; endTime: string; capacity: number; price: number }>>([
  { date: startDate.value, startTime: '14:00', endTime: '16:00', capacity: 30, price: 199 }
]);
const submitting = ref(false);

const toggleStyle = (s: string) => {
  const i = styles.value.indexOf(s);
  if (i >= 0) styles.value.splice(i, 1);
  else styles.value.push(s);
};

const addSession = () =>
  sessions.value.push({ date: startDate.value, startTime: '14:00', endTime: '16:00', capacity: 30, price: 199 });

const removeSession = (i: number) => sessions.value.splice(i, 1);

const onSubmit = async () => {
  if (!title.value.trim() || !styles.value.length || !sessions.value.length) {
    showFailToast('请完善标题、舞种与至少一个场次');
    return;
  }
  submitting.value = true;
  try {
    const result = await createCoachWorkshop({
      title: title.value,
      styles: styles.value,
      intro: intro.value,
      startDate: startDate.value,
      endDate: endDate.value,
      city: city.value,
      area: area.value,
      sessions: sessions.value
    });
    showSuccessToast(`已提交，状态：${result.status}`);
    router.replace('/me/coach-home');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <div class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">创建 Workshop</span>
      <button class="post" :disabled="submitting" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交' }}
      </button>
    </header>
    <section class="form">
      <div class="row">
        <span>标题</span>
        <input v-model="title" class="input" placeholder="例：Yumi 暑期 Hiphop 集训" />
      </div>
      <div class="group">
        <div class="group__title">舞种（多选）</div>
        <div class="chips">
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: styles.includes(s) }"
            @click="toggleStyle(s)"
            >{{ s }}</span
          >
        </div>
      </div>
      <div class="row row--top">
        <span>简介</span>
        <textarea v-model="intro" rows="3" class="input ta" />
      </div>
      <div class="row">
        <span>开始日期</span>
        <input v-model="startDate" type="date" class="input" />
      </div>
      <div class="row">
        <span>结束日期</span>
        <input v-model="endDate" type="date" class="input" />
      </div>
      <div class="row">
        <span>城市</span>
        <input v-model="city" class="input" />
      </div>
      <div class="row">
        <span>区域</span>
        <input v-model="area" class="input" />
      </div>
      <div class="group">
        <div class="group__title">
          场次
          <button class="add" @click="addSession">+ 新增场次</button>
        </div>
        <div v-for="(s, i) in sessions" :key="i" class="session">
          <div class="session__row">
            <input v-model="s.date" type="date" class="input" />
            <input v-model="s.startTime" placeholder="14:00" class="input session__t" />
            <input v-model="s.endTime" placeholder="16:00" class="input session__t" />
          </div>
          <div class="session__row">
            <input v-model.number="s.capacity" type="number" placeholder="人数" class="input" />
            <input v-model.number="s.price" type="number" placeholder="价格 (元)" class="input" />
            <button class="session__del" @click="removeSession(i)">删除</button>
          </div>
        </div>
      </div>
      <p class="tip">教练直发或审批由后端按身份判断；mock 阶段统一返回 PENDING_REVIEW。</p>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid var(--bd-border);
  &__title {
    flex: 1;
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
.post {
  border: none;
  background: var(--bd-primary);
  color: #fff;
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  &:disabled {
    opacity: 0.5;
  }
}
.form {
  padding: 16px;
  background: #fff;
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  span {
    width: 72px;
    font-size: 13px;
    color: var(--bd-text-secondary);
  }
  &--top {
    align-items: flex-start;
    span {
      padding-top: 8px;
    }
  }
}
.input {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
  outline: none;
  &:focus {
    background: #fff;
    border-color: var(--bd-primary);
  }
}
.ta {
  height: auto;
  padding: 8px 12px;
  resize: none;
  font-family: inherit;
}
.group {
  padding: 8px 0;
  &__title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
.add {
  border: none;
  background: none;
  color: var(--bd-primary);
  font-size: 12px;
  cursor: pointer;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 5px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 999px;
  font-size: 12px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.session {
  background: #fafafa;
  border-radius: 10px;
  padding: 10px;
  margin-bottom: 8px;
  &__row {
    display: flex;
    gap: 8px;
    margin-bottom: 6px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  &__t {
    flex: 0 0 90px;
  }
  &__del {
    border: 1px solid var(--bd-border);
    background: #fff;
    border-radius: 8px;
    padding: 4px 10px;
    font-size: 12px;
    color: var(--bd-text-secondary);
    cursor: pointer;
  }
}
.tip {
  margin-top: 12px;
  font-size: 11px;
  color: var(--bd-text-secondary);
}
</style>
