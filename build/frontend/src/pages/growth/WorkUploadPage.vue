<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { ChevronLeft, Image, Trash2, Upload, Video } from 'lucide-vue-next';
import { createGrowthWork, uploadMediaAsset, type MediaAssetDto } from '@/api/growth';

const router = useRouter();
const title = ref('');
const description = ref('');
const danceStyleId = ref(1);
const isPublic = ref(true);
const assets = ref<MediaAssetDto[]>([]);
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const submitting = ref(false);

const canSubmit = computed(() => title.value.trim().length > 0 && !submitting.value);
const cover = computed(() => assets.value.find((item) => item.assetType === 'image') ?? assets.value[0]);

const pickFile = () => fileInput.value?.click();

const onFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []);
  if (!files.length) return;
  uploading.value = true;
  try {
    for (const file of files) {
      const asset = await uploadMediaAsset(file, 'growth_work');
      assets.value.push(asset);
    }
    showSuccessToast('媒体已上传');
  } finally {
    uploading.value = false;
    input.value = '';
  }
};

const removeAsset = (id: number) => {
  assets.value = assets.value.filter((item) => item.id !== id);
};

const submit = async () => {
  if (!canSubmit.value) {
    showFailToast('请先填写作品标题');
    return;
  }
  submitting.value = true;
  try {
    await createGrowthWork({
      danceStyleId: danceStyleId.value,
      workTitle: title.value.trim(),
      workDescription: description.value.trim(),
      coverAssetId: cover.value?.id ?? null,
      mediaAssetIds: assets.value.map((item) => item.id),
      isPublic: isPublic.value
    });
    showSuccessToast('作品已保存到成长档案');
    router.replace('/me/works');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="upload-page">
    <header class="topbar">
      <button class="icon-btn" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" />
      </button>
      <div>
        <p>GROWTH WORK</p>
        <h1>上传作品</h1>
      </div>
      <button class="publish" type="button" :disabled="!canSubmit" @click="submit">保存</button>
    </header>

    <section class="hero">
      <div v-if="cover" class="preview">
        <img v-if="cover.assetType === 'image'" :src="cover.url" alt="作品封面" />
        <video v-else :src="cover.url" controls />
      </div>
      <button v-else class="empty-preview" type="button" @click="pickFile">
        <Upload :size="28" />
        <span>上传图片或视频</span>
      </button>
    </section>

    <section class="form">
      <label>
        <span>作品标题</span>
        <input v-model="title" placeholder="例如：Hiphop 周末练习片段" />
      </label>
      <label>
        <span>复盘说明</span>
        <textarea v-model="description" rows="4" placeholder="记录动作完成度、卡点问题、下次目标" />
      </label>

      <div class="field">
        <span>关联舞种</span>
        <div class="chips">
          <button v-for="item in [[1, 'Hiphop'], [2, 'Jazz'], [4, 'Locking'], [5, 'Popping']]" :key="item[0]" type="button" class="chip" :class="{ active: danceStyleId === item[0] }" @click="danceStyleId = Number(item[0])">
            {{ item[1] }}
          </button>
        </div>
      </div>

      <div class="field">
        <span>可见范围</span>
        <div class="chips">
          <button class="chip" :class="{ active: isPublic }" type="button" @click="isPublic = true">公开展示</button>
          <button class="chip" :class="{ active: !isPublic }" type="button" @click="isPublic = false">仅自己</button>
        </div>
      </div>

      <input ref="fileInput" class="file" type="file" accept="image/*,video/*" multiple @change="onFileChange" />
      <button class="upload-btn" type="button" :disabled="uploading" @click="pickFile">
        <Image :size="18" />
        <Video :size="18" />
        {{ uploading ? '上传中...' : '添加媒体' }}
      </button>
    </section>

    <section v-if="assets.length" class="assets">
      <article v-for="asset in assets" :key="asset.id" class="asset">
        <img v-if="asset.assetType === 'image'" :src="asset.url" :alt="asset.originFileName" />
        <video v-else :src="asset.url" />
        <button type="button" @click="removeAsset(asset.id)"><Trash2 :size="16" /></button>
      </article>
    </section>
  </main>
</template>

<style scoped lang="scss">
.upload-page { min-height: 100vh; max-width: 430px; margin: 0 auto; background: #fff; color: #111; padding-bottom: calc(24px + env(safe-area-inset-bottom)); }
.topbar { display: flex; align-items: center; gap: 12px; padding: 14px 18px; position: sticky; top: 0; z-index: 5; background: rgba(255,255,255,.94); backdrop-filter: blur(10px); }
.topbar div { flex: 1; min-width: 0; }
.topbar p { margin: 0; color: #707072; font-size: 11px; font-weight: 900; letter-spacing: .08em; }
.topbar h1 { margin: 2px 0 0; font-size: 22px; font-weight: 900; line-height: 1.05; }
.icon-btn, .publish { border: 0; border-radius: 999px; font-weight: 900; }
.icon-btn { width: 38px; height: 38px; background: #f5f5f5; display: grid; place-items: center; }
.publish { height: 38px; padding: 0 16px; background: #111; color: #fff; }
.publish:disabled { opacity: .35; }
.hero { padding: 0 18px 16px; }
.preview, .empty-preview { width: 100%; aspect-ratio: 1 / .78; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
.preview img, .preview video { width: 100%; height: 100%; object-fit: cover; display: block; }
.empty-preview { border: 0; display: grid; place-items: center; color: #111; font-size: 14px; font-weight: 900; gap: 8px; }
.form { display: flex; flex-direction: column; gap: 14px; padding: 0 18px; }
label, .field { display: flex; flex-direction: column; gap: 8px; }
label span, .field > span { color: #707072; font-size: 12px; font-weight: 900; }
input, textarea { width: 100%; box-sizing: border-box; border: 0; border-radius: 8px; background: #f5f5f5; padding: 13px 14px; color: #111; font-size: 15px; font-weight: 800; outline: none; }
textarea { resize: vertical; line-height: 1.45; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip { height: 38px; padding: 0 16px; border: 0; border-radius: 999px; background: #f5f5f5; color: #111; font-weight: 900; }
.chip.active { background: #111; color: #fff; }
.file { display: none; }
.upload-btn { height: 46px; border: 0; border-radius: 999px; background: #111; color: #fff; font-size: 15px; font-weight: 900; display: inline-flex; align-items: center; justify-content: center; gap: 8px; }
.assets { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; padding: 16px 18px 0; }
.asset { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
.asset img, .asset video { width: 100%; height: 100%; object-fit: cover; display: block; }
.asset button { position: absolute; top: 6px; right: 6px; width: 28px; height: 28px; border: 0; border-radius: 999px; background: rgba(17,17,17,.88); color: #fff; display: grid; place-items: center; }
</style>
