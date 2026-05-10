<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showConfirmDialog, showDialog } from 'vant';
import {
  fetchMyCoachProfile,
  updateMyCoachProfile,
  addMyCoachWork,
  removeMyCoachWork,
  type CoachProfile
} from '@/api/coach';
import { fetchCoachDashboard, type CoachDashboard } from '@/api/coachOps';

const router = useRouter();
const profile = ref<CoachProfile | null>(null);
const dashboard = ref<CoachDashboard | null>(null);
const editing = ref(false);
const editIntro = ref('');
const editTeachStyle = ref('');
const editStyles = ref<string[]>([]);
const editSlots = ref<Array<{ day: string; time: string }>>([]);

const STYLES = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', '中国舞', 'Urban'];
const DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

const reload = async () => {
  [profile.value, dashboard.value] = await Promise.all([
    fetchMyCoachProfile(),
    fetchCoachDashboard()
  ]);
};

const enterEdit = () => {
  if (!profile.value) return;
  editIntro.value = profile.value.intro;
  editTeachStyle.value = profile.value.teachStyle;
  editStyles.value = [...profile.value.styles];
  editSlots.value = profile.value.availableSlots.map((s) => ({ ...s }));
  editing.value = true;
};

const toggleStyle = (s: string) => {
  const i = editStyles.value.indexOf(s);
  if (i >= 0) editStyles.value.splice(i, 1);
  else editStyles.value.push(s);
};

const addSlot = () => editSlots.value.push({ day: '周一', time: '19:00-20:30' });
const removeSlot = (i: number) => editSlots.value.splice(i, 1);

const onSave = async () => {
  await updateMyCoachProfile({
    intro: editIntro.value,
    teachStyle: editTeachStyle.value,
    styles: editStyles.value,
    availableSlots: editSlots.value
  });
  showSuccessToast('已保存');
  editing.value = false;
  void reload();
};

const onAddWork = async () => {
  const result = await showDialog({
    title: '新增作品',
    message: '示例提示：开发期使用 mock，仅记录标题与类型。',
    showCancelButton: true,
    confirmButtonText: '添加图片作品'
  }).catch(() => null);
  if (!result) return;
  await addMyCoachWork({
    type: 'image',
    title: `作品 ${(profile.value?.works.length ?? 0) + 1}`,
    cover: ''
  });
  showSuccessToast('已添加');
  void reload();
};

const onRemoveWork = async (id: number) => {
  await showConfirmDialog({ title: '删除作品？' }).catch(() => {
    throw new Error('cancel');
  });
  await removeMyCoachWork(id);
  void reload();
};

onMounted(reload);
</script>

<template>
  <div v-if="!profile" class="empty">加载中…</div>
  <div v-else class="page">
    <header class="bar">
      <button class="back" @click="router.back()">←</button>
      <span class="bar__title">教练主页</span>
      <button v-if="!editing" class="bar__edit" @click="enterEdit">编辑</button>
    </header>

    <section class="hero">
      <div class="avatar">{{ profile.name.charAt(0) }}</div>
      <div class="name">{{ profile.name }}</div>
      <div class="rating">★ {{ profile.ratingAvg }} · {{ profile.reviewCount }} 条评价</div>
    </section>

    <section v-if="dashboard && !editing" class="kpi">
      <div class="kpi__cell">
        <div class="kpi__num">{{ dashboard.monthSessions }}</div>
        <div class="kpi__label">本月授课</div>
      </div>
      <div class="kpi__cell">
        <div class="kpi__num">¥{{ dashboard.monthIncome }}</div>
        <div class="kpi__label">本月收益</div>
      </div>
      <div class="kpi__cell">
        <div class="kpi__num">{{ dashboard.pendingReplies }}</div>
        <div class="kpi__label">待回复</div>
      </div>
      <div class="kpi__cell">
        <div class="kpi__num">{{ dashboard.conversionRate }}%</div>
        <div class="kpi__label">转化率</div>
      </div>
    </section>

    <section v-if="!editing" class="ops">
      <button class="op" @click="router.push('/coach/orders')">📋 学员订单与核销</button>
      <button class="op" @click="router.push('/coach/replies')">💬 评价回复</button>
      <button class="op" @click="router.push('/coach/workshop-create')">🎤 创建 Workshop</button>
      <button class="op" @click="router.push('/coach/dashboard')">📊 经营看板</button>
    </section>

    <template v-if="!editing">
      <section class="block">
        <h3>教学风格</h3>
        <p>{{ profile.teachStyle }}</p>
      </section>
      <section class="block">
        <h3>个人介绍</h3>
        <p>{{ profile.intro }}</p>
      </section>
      <section class="block">
        <h3>擅长舞种</h3>
        <div class="chips">
          <span v-for="s in profile.styles" :key="s" class="chip active">{{ s }}</span>
        </div>
      </section>
      <section class="block">
        <div class="block__head">
          <h3>代表作品 ({{ profile.works.length }})</h3>
          <button class="add" @click="onAddWork">+ 新增</button>
        </div>
        <div v-if="!profile.works.length" class="tip">还没作品，加一个吧</div>
        <div v-else class="works">
          <div v-for="w in profile.works" :key="w.id" class="work">
            <div class="work__cover">{{ w.type === 'video' ? '▶' : '🖼' }}</div>
            <div class="work__title">{{ w.title }}</div>
            <button class="work__del" @click="onRemoveWork(w.id)">×</button>
          </div>
        </div>
      </section>
      <section class="block">
        <h3>可约时段</h3>
        <div class="slots">
          <span v-for="s in profile.availableSlots" :key="`${s.day}${s.time}`" class="slot">
            {{ s.day }} {{ s.time }}
          </span>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="block">
        <h3>教学风格</h3>
        <textarea v-model="editTeachStyle" rows="3" class="ta" />
      </section>
      <section class="block">
        <h3>个人介绍</h3>
        <textarea v-model="editIntro" rows="4" class="ta" />
      </section>
      <section class="block">
        <h3>擅长舞种（多选）</h3>
        <div class="chips">
          <span
            v-for="s in STYLES"
            :key="s"
            class="chip"
            :class="{ active: editStyles.includes(s) }"
            @click="toggleStyle(s)"
            >{{ s }}</span
          >
        </div>
      </section>
      <section class="block">
        <div class="block__head">
          <h3>可约时段</h3>
          <button class="add" @click="addSlot">+ 新增</button>
        </div>
        <div v-for="(s, i) in editSlots" :key="i" class="slot-edit">
          <select v-model="s.day" class="select">
            <option v-for="d in DAYS" :key="d" :value="d">{{ d }}</option>
          </select>
          <input v-model="s.time" class="input" placeholder="19:00-20:30" />
          <button class="slot-edit__del" @click="removeSlot(i)">删除</button>
        </div>
      </section>
      <footer class="footer">
        <button class="btn btn--ghost" @click="editing = false">取消</button>
        <button class="btn btn--primary" @click="onSave">保存</button>
      </footer>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: calc(72px + env(safe-area-inset-bottom));
}
.empty {
  padding: 80px 24px;
  text-align: center;
  color: var(--bd-text-secondary);
}
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
  &__edit {
    border: none;
    background: none;
    color: var(--bd-primary);
    font-size: 13px;
    cursor: pointer;
  }
}
.back {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.hero {
  text-align: center;
  background: linear-gradient(180deg, #ffe2e8, #fff);
  padding: 24px 16px 16px;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  margin: 0 auto;
  background: var(--bd-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.name {
  margin-top: 12px;
  font-size: 20px;
  font-weight: 700;
}
.rating {
  margin-top: 4px;
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.kpi {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  background: var(--bd-border);
  margin-top: 8px;
  &__cell {
    background: #fff;
    padding: 12px 6px;
    text-align: center;
  }
  &__num {
    font-size: 16px;
    font-weight: 700;
    color: var(--bd-primary);
  }
  &__label {
    margin-top: 2px;
    font-size: 11px;
    color: var(--bd-text-secondary);
  }
}
.ops {
  background: #fff;
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
}
.op {
  border: none;
  background: none;
  padding: 14px;
  text-align: left;
  font-size: 13px;
  border-right: 1px solid var(--bd-border);
  border-bottom: 1px solid var(--bd-border);
  cursor: pointer;
  &:nth-child(even) {
    border-right: none;
  }
}
.block {
  margin-top: 8px;
  padding: 16px;
  background: #fff;
  h3 {
    margin: 0 0 8px;
    font-size: 14px;
  }
  p {
    margin: 0;
    font-size: 13px;
    line-height: 1.6;
  }
  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    h3 {
      margin: 0;
    }
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
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  &.active {
    border-color: var(--bd-primary);
    background: rgba(255, 36, 66, 0.06);
    color: var(--bd-primary);
  }
}
.tip {
  font-size: 12px;
  color: var(--bd-text-secondary);
}
.works {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.work {
  background: #fafafa;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  &__cover {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #ffd2da, #ff7799);
    color: #fff;
    font-size: 32px;
  }
  &__title {
    padding: 6px 8px;
    font-size: 12px;
  }
  &__del {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 22px;
    height: 22px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    font-size: 14px;
    cursor: pointer;
  }
}
.slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.slot {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(54, 165, 255, 0.1);
  color: #36a5ff;
  font-size: 12px;
}
.ta,
.input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  &:focus {
    border-color: var(--bd-primary);
  }
}
.ta {
  resize: none;
}
.slot-edit {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  &__del {
    border: 1px solid var(--bd-border);
    background: #fff;
    color: var(--bd-text-secondary);
    border-radius: 8px;
    padding: 6px 10px;
    font-size: 12px;
    cursor: pointer;
  }
}
.select,
.input {
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--bd-border);
  border-radius: 8px;
  background: #fff;
}
.select {
  width: 80px;
}
.footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid var(--bd-border);
  display: flex;
  gap: 10px;
}
.btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  &--primary {
    background: var(--bd-primary);
    color: #fff;
  }
  &--ghost {
    background: rgba(255, 36, 66, 0.08);
    color: var(--bd-primary);
  }
}
</style>
