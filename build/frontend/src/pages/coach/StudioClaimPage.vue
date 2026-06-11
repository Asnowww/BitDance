<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast, showFailToast } from 'vant';
import PenTopBar from '@/components/pen/PenTopBar.vue';
import { fetchNearbyStudios, type StudioCard } from '@/api/studio';
import {
  submitStudioClaim,
  submitNewStudioClaim,
  uploadMediaAsset,
  mediaContentUrl
} from '@/api/coachOps';

const router = useRouter();
const tab = ref<'claim' | 'new'>('claim');

// ---------- 认领已有舞室 ----------
const keyword = ref('');
const searching = ref(false);
const results = ref<StudioCard[]>([]);
const selectedStudio = ref<StudioCard | null>(null);
const claimRemark = ref('');

const searchStudios = async () => {
  searching.value = true;
  try {
    const resp = await fetchNearbyStudios({ keyword: keyword.value, page: 1, pageSize: 20 });
    results.value = resp.list ?? [];
  } finally {
    searching.value = false;
  }
};

// ---------- 新舞室入驻 ----------
const form = ref({
  studioName: '',
  brandName: '',
  cityId: '' as string | number,
  businessDistrictId: '' as string | number,
  address: '',
  longitude: '' as string | number,
  latitude: '' as string | number,
  contactPhone: '',
  intro: '',
  businessHours: '',
  submittedRemark: ''
});

// ---------- 资质材料(两个 Tab 共用) ----------
const licenseAssetId = ref<number | null>(null);
const licenseIsImage = ref(true);
const licenseName = ref('');
const uploading = ref(false);

const onPickLicense = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  (e.target as HTMLInputElement).value = '';
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    showFailToast('文件不能超过 5MB');
    return;
  }
  uploading.value = true;
  try {
    const asset = await uploadMediaAsset(file, 'business_license');
    licenseAssetId.value = asset.assetId;
    licenseIsImage.value = file.type.startsWith('image/');
    licenseName.value = file.name;
    showSuccessToast('上传成功');
  } finally {
    uploading.value = false;
  }
};

// ---------- 提交 ----------
const submitting = ref(false);

const claimReady = computed(() => Boolean(selectedStudio.value && licenseAssetId.value));
const newReady = computed(
  () =>
    form.value.studioName.trim() &&
    form.value.cityId !== '' &&
    form.value.address.trim() &&
    form.value.contactPhone.trim() &&
    Boolean(licenseAssetId.value)
);

const submit = async () => {
  if (submitting.value) return;
  submitting.value = true;
  try {
    if (tab.value === 'claim') {
      if (!claimReady.value) return;
      await submitStudioClaim({
        studioId: selectedStudio.value!.id,
        claimType: 'owner_claim',
        businessLicenseAssetId: licenseAssetId.value!,
        submittedRemark: claimRemark.value || undefined
      });
    } else {
      if (!newReady.value) return;
      const f = form.value;
      await submitNewStudioClaim({
        claimType: 'new_studio',
        businessLicenseAssetId: licenseAssetId.value!,
        submittedRemark: f.submittedRemark || undefined,
        studioName: f.studioName.trim(),
        brandName: f.brandName.trim() || undefined,
        cityId: Number(f.cityId),
        businessDistrictId: f.businessDistrictId === '' ? undefined : Number(f.businessDistrictId),
        address: f.address.trim(),
        longitude: f.longitude === '' ? undefined : Number(f.longitude),
        latitude: f.latitude === '' ? undefined : Number(f.latitude),
        contactPhone: f.contactPhone.trim(),
        intro: f.intro.trim() || undefined,
        businessHours: f.businessHours.trim() || undefined
      });
    }
    showSuccessToast('已提交,等待平台审核');
    router.replace('/coach/studio-claim/status');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <main class="claim-page">
    <PenTopBar title="舞室入驻 / 认领" :show-share="false" />

    <nav class="tabs">
      <button :class="{ active: tab === 'claim' }" @click="tab = 'claim'">认领已有舞室</button>
      <button :class="{ active: tab === 'new' }" @click="tab = 'new'">新舞室入驻</button>
      <button class="status-link" @click="router.push('/coach/studio-claim/status')">
        审核进度 ›
      </button>
    </nav>

    <section v-if="tab === 'claim'" class="body">
      <div class="search-row">
        <input
          v-model="keyword"
          placeholder="搜索舞室名称"
          @keyup.enter="searchStudios"
        />
        <button :disabled="searching" @click="searchStudios">
          {{ searching ? '搜索中…' : '搜索' }}
        </button>
      </div>

      <p v-if="!results.length && !searching" class="tip">输入舞室名称搜索并选择要认领的舞室</p>

      <button
        v-for="s in results"
        :key="s.id"
        class="studio-row"
        :class="{ chosen: selectedStudio?.id === s.id }"
        @click="selectedStudio = s"
      >
        <span class="info">
          <strong>{{ s.name }}</strong>
          <small>{{ s.address }}</small>
        </span>
        <em v-if="selectedStudio?.id === s.id">已选</em>
      </button>

      <div class="form">
        <p class="form-section">资质材料</p>
        <div class="upload">
          <label class="upload-tile">
            <template v-if="licenseAssetId">
              <img v-if="licenseIsImage" :src="mediaContentUrl(licenseAssetId)" alt="营业执照" />
              <span v-else class="pdf">PDF<br />{{ licenseName.slice(0, 10) }}</span>
            </template>
            <span v-else>{{ uploading ? '上传中…' : '+ 营业执照' }}</span>
            <input type="file" accept="image/jpeg,image/png,image/webp,application/pdf" hidden @change="onPickLicense" />
          </label>
        </div>
        <p class="hint">支持 jpg / png / webp / pdf,不超过 5MB</p>

        <div class="field">
          <label>备注说明</label>
          <textarea v-model="claimRemark" maxlength="1000" placeholder="补充认领说明(选填)" />
        </div>
      </div>
    </section>

    <section v-else class="body form">
      <p class="form-section">基础信息</p>
      <div class="field">
        <label>舞室名称 <em>*</em></label>
        <input v-model="form.studioName" maxlength="50" placeholder="如:燃舞工作室" />
      </div>
      <div class="field">
        <label>品牌名</label>
        <input v-model="form.brandName" maxlength="50" placeholder="选填" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>城市 ID <em>*</em></label>
          <input v-model="form.cityId" type="number" placeholder="如 1" />
        </div>
        <div class="field">
          <label>商圈 ID</label>
          <input v-model="form.businessDistrictId" type="number" placeholder="选填" />
        </div>
      </div>
      <div class="field">
        <label>详细地址 <em>*</em></label>
        <input v-model="form.address" maxlength="200" placeholder="街道、门牌号" />
      </div>
      <div class="field-pair">
        <div class="field">
          <label>经度</label>
          <input v-model="form.longitude" type="number" step="0.000001" placeholder="选填" />
        </div>
        <div class="field">
          <label>纬度</label>
          <input v-model="form.latitude" type="number" step="0.000001" placeholder="选填" />
        </div>
      </div>
      <div class="field">
        <label>联系电话 <em>*</em></label>
        <input v-model="form.contactPhone" maxlength="20" placeholder="座机或手机号" />
      </div>
      <div class="field">
        <label>营业时间</label>
        <input v-model="form.businessHours" maxlength="100" placeholder="如:10:00-22:00" />
      </div>
      <div class="field">
        <label>舞室简介</label>
        <textarea v-model="form.intro" maxlength="2000" placeholder="舞种特色、环境、师资…" />
      </div>

      <p class="form-section">资质材料</p>
      <div class="upload">
        <label class="upload-tile">
          <template v-if="licenseAssetId">
            <img v-if="licenseIsImage" :src="mediaContentUrl(licenseAssetId)" alt="营业执照" />
            <span v-else class="pdf">PDF<br />{{ licenseName.slice(0, 10) }}</span>
          </template>
          <span v-else>{{ uploading ? '上传中…' : '+ 营业执照' }}</span>
          <input type="file" accept="image/jpeg,image/png,image/webp,application/pdf" hidden @change="onPickLicense" />
        </label>
      </div>
      <p class="hint">营业执照等资质,支持 jpg / png / webp / pdf,不超过 5MB</p>

      <div class="field">
        <label>备注说明</label>
        <textarea v-model="form.submittedRemark" maxlength="1000" placeholder="补充入驻说明(选填)" />
      </div>
      <p class="tip">审核通过前舞室不会出现在用户端搜索;通过后自动开通商家工作台。</p>
    </section>

    <footer class="submit-bar">
      <button
        :disabled="submitting || uploading || (tab === 'claim' ? !claimReady : !newReady)"
        @click="submit"
      >
        {{ submitting ? '提交中…' : '提交平台审核' }}
      </button>
    </footer>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/ops.scss';

.claim-page {
  @include ops-page;
}

.tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px 0;
  button {
    @include pen-chip;
    border: 1px solid $pen-hairline;
    background: $pen-canvas;
    color: $pen-ink;
    &.active {
      border-color: $pen-ink;
      background: $pen-ink;
      color: #fff;
    }
  }
  .status-link {
    margin-left: auto;
    border: 0;
    background: none;
    color: $pen-mute;
    font-size: 12.5px;
    font-weight: 800;
  }
}

.body {
  @include ops-body;
}

.form {
  @include ops-form;
}

.search-row {
  display: flex;
  gap: 8px;
  input {
    flex: 1;
    height: 44px;
    border: 1px solid $pen-hairline;
    border-radius: 999px;
    background: $pen-soft;
    padding: 0 16px;
    font-size: 14px;
    font-weight: 600;
    outline: none;
    &:focus {
      border-color: $pen-ink;
      background: $pen-canvas;
    }
  }
  button {
    height: 44px;
    padding: 0 18px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: #fff;
    font-size: 13px;
    font-weight: 800;
    &:disabled {
      opacity: 0.5;
    }
  }
}

.tip {
  margin: 14px 2px;
  color: $pen-mute;
  font-size: 12.5px;
  line-height: 1.5;
}

.studio-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 10px;
  border: 1px solid $pen-hairline;
  border-radius: 20px;
  background: $pen-canvas;
  padding: 14px 16px;
  text-align: left;
  cursor: pointer;
  &.chosen {
    border-color: $pen-ink;
    background: $pen-soft;
  }
  .info {
    display: flex;
    flex-direction: column;
    gap: 3px;
    strong {
      font-size: 14.5px;
      font-weight: 900;
    }
    small {
      color: $pen-mute;
      font-size: 12px;
    }
  }
  em {
    flex: 0 0 auto;
    font-style: normal;
    font-size: 11px;
    font-weight: 900;
    color: #fff;
    background: $pen-ink;
    border-radius: 999px;
    padding: 4px 10px;
  }
}

.upload {
  @include ops-upload;
  .pdf {
    font-size: 11px;
    text-align: center;
    line-height: 1.4;
    word-break: break-all;
    padding: 4px;
  }
}

.hint {
  margin: 6px 2px 16px;
  color: $pen-mute;
  font-size: 11.5px;
}

.submit-bar {
  @include ops-submit-bar;
}
</style>
