<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showFailToast, showSuccessToast } from 'vant';
import { Check, ChevronLeft, Image, LoaderCircle, Plus, Trash2, Video, X } from 'lucide-vue-next';
import PenSettingRow from '@/components/pen/PenSettingRow.vue';
import {
  createPost,
  fetchPostDetail,
  fetchTopics,
  updatePost,
  uploadPostMedia,
  type CommunityTopic,
  type MediaAsset
} from '@/api/community';
import {
  buildFallbackMapGeocodeResult,
  getTencentMapDefaultLocation,
  locateTencentByIp,
  reverseGeocodeTencentLocation,
  searchFallbackTencentPlaces,
  searchTencentPlaces,
  type MapGeocodeResult,
  type MapPlace
} from '@/api/maps';
import { hasTencentMapConfig, loadTencentMap } from '@/utils/tencentMap';
import { captureVideoPoster } from '@/utils/videoPoster';

interface UploadedMedia extends MediaAsset {
  id: number;
  previewUrl?: string;
  posterUrl?: string;
}

type Visibility = 'public' | 'followers' | 'private';
type SheetKey = 'topic' | 'related' | 'style' | 'location' | 'visibility' | 'work-type' | 'practice-date';

const route = useRoute();
const router = useRouter();
const content = ref('');
const uploadedMedia = ref<UploadedMedia[]>([]);
const imageInput = ref<HTMLInputElement | null>(null);
const videoInput = ref<HTMLInputElement | null>(null);
const publishing = ref(false);
const uploading = ref(false);
const loading = ref(false);
const activeSheet = ref<SheetKey | null>(null);
const topicInput = ref('');
const topicSuggestions = ref<CommunityTopic[]>([]);
const topicLoading = ref(false);
const selectedTopics = ref<string[]>([]);
const selectedStyle = ref('Locking');
const selectedLocation = ref('不显示位置');
const locationInput = ref('');
const geoPoint = ref<{ longitude: number; latitude: number } | null>(null);
const locating = ref(false);
const locationSearching = ref(false);
const locationCandidates = ref<MapPlace[]>([]);
const locationError = ref('');
const locationResolvedAddress = ref('');
const locationReady = ref(false);
const selectedVisibility = ref<Visibility>('public');
const selectedRelated = ref('Urban Flow');
const selectedWorkType = ref('阶段作品');
const selectedPracticeDate = ref('今天');

const danceStyles = ['Hiphop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking'];
const relatedOptions = ['Urban Flow', '舞星 Studio', '节奏盒子课程', '小黑老师', '不关联'];
const visibilityOptions: Array<{ value: Visibility; label: string; desc: string }> = [
  { value: 'public', label: '公开', desc: '所有人可见' },
  { value: 'followers', label: '粉丝可见', desc: '关注你的人可见' },
  { value: 'private', label: '仅自己', desc: '只保存在个人主页' }
];
const workTypes = ['阶段作品', '课堂作业', 'Battle 片段', '排练记录'];
const practiceDates = ['今天', '昨天', '本周', '自定义'];
const isWorkMode = computed(() => route.name === 'publish-work' || route.path.includes('/works/upload'));
const editPostId = computed(() => Number(route.params.id) || null);
const isEditMode = computed(() => route.name === 'edit-post' && Boolean(editPostId.value));
const images = computed(() => uploadedMedia.value.filter((item) => item.mediaType === 'image'));
const video = computed(() => uploadedMedia.value.find((item) => item.mediaType === 'video'));
const imageCountLabel = computed(() => `${images.value.length}/9`);
const hasVideo = computed(() => Boolean(video.value));
const visibilityLabel = computed(() => visibilityOptions.find((item) => item.value === selectedVisibility.value)?.label ?? '公开');
const topicLabel = computed(() => selectedTopics.value.length ? selectedTopics.value.map((topic) => `#${topic}`).join(' ') : '未选择');

const pageCopy = computed(() =>
  isWorkMode.value
    ? {
        title: '上传作品',
        meta: '保存到阶段作品',
        action: '保存作品',
        placeholder: '记录这次作品的练习目标、完成度或想复盘的动作。',
        success: '作品已保存',
        draft: '退出后进入作品草稿，可继续修改或删除。'
      }
    : {
        title: isEditMode.value ? '编辑动态' : '发动态',
        meta: '草稿已自动保存',
        action: isEditMode.value ? '保存' : '发布',
        placeholder: '分享课堂记录、练舞片段或 Workshop 体验',
        success: isEditMode.value ? '已保存' : '已发布',
        draft: '退出后进入草稿箱，可继续修改或删除。'
      }
);

const rows = computed(() =>
  isWorkMode.value
    ? [
        { key: 'work-type' as const, label: '作品类型', trailing: selectedWorkType.value },
        { key: 'style' as const, label: '关联舞种', trailing: selectedStyle.value },
        { key: 'practice-date' as const, label: '练习日期', trailing: selectedPracticeDate.value },
        { key: 'visibility' as const, label: '可见范围', trailing: visibilityLabel.value }
      ]
    : [
        { key: 'topic' as const, label: '# 添加话题', trailing: topicLabel.value },
        { key: 'related' as const, label: '关联舞室 / 课程 / 老师', trailing: selectedRelated.value },
        { key: 'location' as const, label: '所在位置', trailing: selectedLocation.value },
        { key: 'visibility' as const, label: '谁可以看', trailing: visibilityLabel.value }
      ]
);

const pickImage = () => imageInput.value?.click();
const pickVideo = () => videoInput.value?.click();

const ensureCanAddImage = (count: number) => {
  if (hasVideo.value) {
    showFailToast('视频动态不能同时添加图片');
    return false;
  }
  if (images.value.length + count > 9) {
    showFailToast('最多添加 9 张图片');
    return false;
  }
  return true;
};

const uploadFiles = async (files: File[]) => {
  if (!files.length) return;
  uploading.value = true;
  try {
    const uploaded: UploadedMedia[] = [];
    for (const file of files) {
      const previewUrl = URL.createObjectURL(file);
      try {
        const asset = await uploadPostMedia(file);
        const posterUrl = file.type.startsWith('video/') ? (await captureVideoPoster(previewUrl)) ?? undefined : undefined;
        uploaded.push({ ...asset, previewUrl, posterUrl });
      } catch (error) {
        URL.revokeObjectURL(previewUrl);
        throw error;
      }
    }
    uploadedMedia.value = [...uploadedMedia.value, ...uploaded];
    showSuccessToast(files.length > 1 ? '媒体已上传' : '媒体已上传');
  } finally {
    uploading.value = false;
  }
};

const onImagesSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []);
  input.value = '';
  if (!ensureCanAddImage(files.length)) return;
  await uploadFiles(files);
};

const onVideoSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []).slice(0, 1);
  input.value = '';
  if (!files.length) return;
  if (uploadedMedia.value.length > 0) {
    showFailToast('视频动态只能上传 1 个视频');
    return;
  }
  await uploadFiles(files);
};

const removeMedia = (id: number) => {
  const removed = uploadedMedia.value.find((item) => item.id === id);
  if (removed?.previewUrl) URL.revokeObjectURL(removed.previewUrl);
  uploadedMedia.value = uploadedMedia.value.filter((item) => item.id !== id);
};

const mediaPreviewUrl = (item: UploadedMedia) => item.previewUrl || item.url;
const mediaPosterUrl = (item: UploadedMedia) => item.posterUrl;

const clearPreviewUrls = () => {
  uploadedMedia.value.forEach((item) => {
    if (item.previewUrl) URL.revokeObjectURL(item.previewUrl);
  });
};

const normalizeTopic = (value: string) => value.trim().replace(/^#+/, '').trim();

const addTopic = (value: string) => {
  const name = normalizeTopic(value);
  if (!name) return;
  if (selectedTopics.value.includes(name)) return;
  if (selectedTopics.value.length >= 5) {
    showFailToast('最多添加 5 个话题');
    return;
  }
  selectedTopics.value = [...selectedTopics.value, name];
  topicInput.value = '';
};

const removeTopic = (name: string) => {
  selectedTopics.value = selectedTopics.value.filter((item) => item !== name);
};

const loadTopicSuggestions = async () => {
  topicLoading.value = true;
  try {
    topicSuggestions.value = await fetchTopics({ scope: 'hot', keyword: topicInput.value.trim() || undefined, limit: 12 });
  } finally {
    topicLoading.value = false;
  }
};

const openSheet = async (key: SheetKey) => {
  activeSheet.value = key;
  if (key === 'topic') await loadTopicSuggestions();
  if (key === 'location') {
    locationInput.value = '';
    if (locationCandidates.value.length === 0 || !locationReady.value) {
      try {
        await ensureLocationChoicesLoaded(false);
      } catch {
        // Keep sheet interactive even if both remote and local fallback fail unexpectedly.
      }
    } else if (geoPoint.value) {
      try {
        const { geo, places } = await loadNearbyLocationChoices(geoPoint.value.latitude, geoPoint.value.longitude);
        locationResolvedAddress.value = geo.address || locationResolvedAddress.value;
        locationCandidates.value = places;
      } catch {
        // Ignore sheet-open refresh failures and keep the current manual state visible.
      }
    }
  }
};

const closeSheet = () => {
  activeSheet.value = null;
};

const chooseRelated = (value: string) => {
  selectedRelated.value = value;
  closeSheet();
};

const chooseStyle = (value: string) => {
  selectedStyle.value = value;
  closeSheet();
};

const chooseLocation = (value: string) => {
  selectedLocation.value = value;
  locationInput.value = value === '不显示位置' ? '' : value;
  if (value === '不显示位置') {
    geoPoint.value = null;
    locationReady.value = false;
  }
  locationError.value = '';
  locationResolvedAddress.value = '';
  closeSheet();
};

const chooseMapPlace = (place: MapPlace) => {
  geoPoint.value = { latitude: place.latitude, longitude: place.longitude };
  selectedLocation.value = place.title;
  locationResolvedAddress.value = place.address || '';
  locationInput.value = '';
  locationError.value = '';
  locationReady.value = true;
  closeSheet();
};

const formatCoordinate = (latitude: number, longitude: number) =>
  `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;

const reverseGeocodeByTencentJs = async (latitude: number, longitude: number) => {
  if (!hasTencentMapConfig()) throw new Error('Tencent map key missing');
  const TMap = await loadTencentMap();
  const LatLng = TMap.LatLng;
  const Geocoder = TMap.service?.Geocoder;
  if (!LatLng || !Geocoder) throw new Error('Tencent map geocoder missing');
  const geocoder = new Geocoder();
  const result = await geocoder.getAddress({ location: new LatLng(latitude, longitude) });
  const detail = result?.result ?? result;
  return (
    detail?.formatted_addresses?.recommend ||
    detail?.address ||
    detail?.address_component?.street ||
    formatCoordinate(latitude, longitude)
  );
};

const dedupePlaces = (places: MapPlace[]) => {
  const seen = new Set<string>();
  return places.filter((place) => {
    const key = `${place.title}|${place.address || ''}|${place.latitude.toFixed(5)}|${place.longitude.toFixed(5)}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
};

const fetchRemoteNearbyPlaces = async (geo: MapGeocodeResult, latitude: number, longitude: number, keyword?: string) => {
  const keywordText = keyword?.trim();
  if (keywordText) {
    return searchTencentPlaces({
      keyword: keywordText,
      city: geo.city || '北京',
      latitude,
      longitude,
      radiusMeters: 5000,
      pageSize: 12
    });
  }
  const genericKeywords = ['写字楼', '商场', '学校', '餐厅'];
  const groups = await Promise.allSettled(
    genericKeywords.map((item) =>
      searchTencentPlaces({
        keyword: item,
        city: geo.city || '北京',
        latitude,
        longitude,
        radiusMeters: 3000,
        pageSize: 6
      })
    )
  );
  return groups.flatMap((item) => (item.status === 'fulfilled' ? item.value : []));
};

const loadNearbyLocationChoices = async (latitude: number, longitude: number, keyword?: string) => {
  let geo: MapGeocodeResult;
  try {
    geo = await reverseGeocodeTencentLocation(latitude, longitude);
  } catch {
    try {
      const fallbackTitle = await reverseGeocodeByTencentJs(latitude, longitude);
      geo = {
        ...buildFallbackMapGeocodeResult(latitude, longitude),
        title: fallbackTitle,
        address: fallbackTitle
      };
    } catch {
      geo = buildFallbackMapGeocodeResult(latitude, longitude);
    }
  }
  const keywordText = keyword?.trim();
  let searchedPlaces: MapPlace[] = [];
  try {
    searchedPlaces = await fetchRemoteNearbyPlaces(geo, latitude, longitude, keywordText);
  } catch {
    searchedPlaces = [];
  }
  if (searchedPlaces.length === 0) {
    searchedPlaces = searchFallbackTencentPlaces({
      keyword: keywordText,
      latitude,
      longitude,
      pageSize: 12
    });
  }
  let places = dedupePlaces([
    {
      title: geo.title || geo.address || '附近位置',
      address: geo.address,
      latitude: geo.latitude,
      longitude: geo.longitude
    },
    ...(geo.pois ?? []),
    ...searchedPlaces
  ]);
  if (keywordText) {
    const normalizedKeyword = keywordText.toLowerCase();
    places = places.filter((place) => `${place.title}${place.address || ''}${place.category || ''}`.toLowerCase().includes(normalizedKeyword));
  }
  return {
    geo,
    places
  };
};

const applyResolvedLocation = async (latitude: number, longitude: number, successMessage?: string) => {
  geoPoint.value = { latitude, longitude };
  const { geo, places } = await loadNearbyLocationChoices(latitude, longitude);
  locationResolvedAddress.value = geo.address || '';
  locationCandidates.value = places;
  selectedLocation.value = places[0]?.title || geo.title || geo.address || '不显示位置';
  locationInput.value = '';
  locationError.value = '';
  locationReady.value = true;
  if (successMessage) showSuccessToast(successMessage);
};

const useApproximateLocation = async () => {
  const approx = await locateTencentByIp();
  await applyResolvedLocation(approx.latitude, approx.longitude, '已通过网络位置估算附近位置');
};

const useDefaultLocation = async () => {
  const fallback = getTencentMapDefaultLocation();
  await applyResolvedLocation(fallback.latitude, fallback.longitude, '已使用北京中关村位置兜底');
};

const ensureLocationChoicesLoaded = async (showToast = false) => {
  if (geoPoint.value) {
    await applyResolvedLocation(geoPoint.value.latitude, geoPoint.value.longitude, showToast ? '已刷新附近位置' : undefined);
    return;
  }
  await useCurrentLocation(showToast);
};

const useCurrentLocation = async (showToast = true) => {
  locating.value = true;
  locationError.value = '';
  try {
    if (!navigator.geolocation) {
      await useApproximateLocation();
      return;
    }
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 12000,
        maximumAge: 60000
      });
    });
    const latitude = Number(position.coords.latitude.toFixed(6));
    const longitude = Number(position.coords.longitude.toFixed(6));
    await applyResolvedLocation(latitude, longitude, showToast ? '已获取附近位置' : undefined);
  } catch (error) {
    try {
      const approx = await locateTencentByIp();
      await applyResolvedLocation(approx.latitude, approx.longitude, showToast ? '已通过网络位置估算附近位置' : undefined);
      const code = typeof error === 'object' && error && 'code' in error ? Number((error as GeolocationPositionError).code) : 0;
      locationError.value =
        code === 1
          ? '系统定位不可用，已为你切换到附近地点列表'
          : code === 2
            ? '当前环境拿不到精确定位，已切换到附近地点列表'
            : code === 3
              ? '系统定位超时，已切换到附近地点列表'
              : '系统定位失败，已切换到附近地点列表';
    } catch {
      const code = typeof error === 'object' && error && 'code' in error ? Number((error as GeolocationPositionError).code) : 0;
      await useDefaultLocation();
      locationError.value =
        code === 1
          ? '系统定位不可用，已使用北京中关村兜底地点'
          : code === 2
            ? '当前环境暂时拿不到系统定位，已使用北京中关村兜底地点'
            : code === 3
              ? '系统定位超时，已使用北京中关村兜底地点'
              : '定位失败，已使用北京中关村兜底地点';
    }
  } finally {
    locating.value = false;
  }
};

const searchLocation = async () => {
  const keyword = locationInput.value.trim();
  locationSearching.value = true;
  locationError.value = '';
  try {
    if (!geoPoint.value) {
      await ensureLocationChoicesLoaded(false);
    }
    if (!geoPoint.value) {
      throw new Error('定位上下文为空');
    }
    const { geo, places } = await loadNearbyLocationChoices(geoPoint.value.latitude, geoPoint.value.longitude, keyword || undefined);
    locationResolvedAddress.value = geo.address || locationResolvedAddress.value;
    locationCandidates.value = places;
    locationReady.value = true;
    if (places.length === 0) {
      locationError.value = '附近没有匹配地点，请换个关键词再试';
      showFailToast(locationError.value);
      return;
    }
    showSuccessToast(keyword ? '已更新附近地点' : '已刷新附近地点');
  } catch {
    locationError.value = '地图搜索失败，请检查地图 Key 或稍后再试';
    showFailToast(locationError.value);
  } finally {
    locationSearching.value = false;
  }
};

const chooseVisibility = (value: Visibility) => {
  selectedVisibility.value = value;
  closeSheet();
};

const chooseWorkType = (value: string) => {
  selectedWorkType.value = value;
  closeSheet();
};

const choosePracticeDate = (value: string) => {
  selectedPracticeDate.value = value;
  closeSheet();
};

const onPublish = async () => {
  if (!content.value.trim()) {
    showFailToast('请输入内容');
    return;
  }
  if (publishing.value) return;
  publishing.value = true;
  try {
    if (!isWorkMode.value) {
      const payload = {
        text: content.value.trim(),
        mediaAssetIds: uploadedMedia.value.map((item) => item.id),
        hasVideo: hasVideo.value,
        topics: selectedTopics.value,
        style: selectedStyle.value,
        location: selectedLocation.value === '不显示位置' ? undefined : selectedLocation.value,
        longitude: geoPoint.value?.longitude,
        latitude: geoPoint.value?.latitude,
        visibility: selectedVisibility.value,
        idempotencyToken: `post-${Date.now()}`
      };
      let saved;
      if (isEditMode.value && editPostId.value) {
        saved = await updatePost(editPostId.value, payload);
      } else {
        saved = await createPost(payload);
      }
      clearPreviewUrls();
      showSuccessToast(pageCopy.value.success);
      router.replace({ path: '/community', query: { published: String(saved.id) } });
      return;
    }
    showSuccessToast(pageCopy.value.success);
    router.replace(isWorkMode.value ? '/me/works' : '/community');
  } finally {
    publishing.value = false;
  }
};

onMounted(async () => {
  const fromQuery = normalizeTopic(String(route.query.topic ?? ''));
  selectedTopics.value = fromQuery ? [fromQuery] : ['Locking入门'];
  locationInput.value = selectedLocation.value;
  locationResolvedAddress.value = '';
  if (!isEditMode.value || !editPostId.value) return;
  loading.value = true;
  try {
    const detail = await fetchPostDetail(editPostId.value);
    content.value = detail.text;
    uploadedMedia.value = detail.mediaAssets;
    for (const item of uploadedMedia.value) {
      if (item.mediaType === 'video') {
        item.posterUrl = (await captureVideoPoster(item.url)) ?? undefined;
      }
    }
    selectedTopics.value = detail.topics.length ? detail.topics : selectedTopics.value;
    selectedStyle.value = detail.style || selectedStyle.value;
    selectedLocation.value = detail.location || selectedLocation.value;
    geoPoint.value = detail.longitude !== undefined && detail.latitude !== undefined
      ? { longitude: detail.longitude, latitude: detail.latitude }
      : null;
    locationReady.value = Boolean(geoPoint.value);
    locationInput.value = selectedLocation.value === '不显示位置' ? '' : selectedLocation.value;
  } finally {
    loading.value = false;
  }
});

onUnmounted(clearPreviewUrls);
</script>

<template>
  <main class="pen-page">
    <header class="topbar">
      <button class="topbar__icon" type="button" aria-label="返回" @click="router.back()">
        <ChevronLeft :size="20" :stroke-width="2" />
      </button>
      <div class="topbar__copy">
        <h1 class="topbar__title">{{ pageCopy.title }}</h1>
        <p class="topbar__meta">{{ pageCopy.meta }}</p>
      </div>
      <button class="topbar__pub" type="button" :disabled="publishing || uploading || loading" @click="onPublish">
        {{ publishing ? '发布中…' : pageCopy.action }}
      </button>
    </header>

    <section class="pen-scroll">
      <p v-if="loading" class="loading-text">加载中</p>
      <textarea
        v-else
        v-model="content"
        class="editor"
        rows="5"
        :placeholder="pageCopy.placeholder"
      />

      <section class="media-section" aria-label="添加媒体">
        <input ref="imageInput" class="media-input" type="file" accept="image/*" multiple @change="onImagesSelected" />
        <input ref="videoInput" class="media-input" type="file" accept="video/*" @change="onVideoSelected" />
        <div class="media-section__head">
          <h2>{{ isWorkMode ? '作品媒体' : '媒体' }}</h2>
          <span>{{ uploading ? '上传中' : hasVideo ? '1/1' : imageCountLabel }}</span>
        </div>

        <div class="media-actions">
          <button class="media-action" type="button" :disabled="uploading || hasVideo" @click="pickImage">
            <LoaderCircle v-if="uploading" class="spin" :size="22" :stroke-width="2" />
            <Image v-else :size="22" :stroke-width="2" />
            <span>添加图片</span>
          </button>
          <button class="media-action" type="button" :disabled="uploading || uploadedMedia.length > 0" @click="pickVideo">
            <Video :size="22" :stroke-width="2" />
            <span>添加视频</span>
          </button>
        </div>

        <div v-if="video" class="video-preview">
          <video :src="mediaPreviewUrl(video)" :poster="mediaPosterUrl(video)" controls playsinline preload="auto" />
          <button class="media-remove" type="button" aria-label="删除视频" @click="removeMedia(video.id)">
            <Trash2 :size="14" :stroke-width="2" />
          </button>
        </div>

        <div v-if="images.length" class="image-grid">
          <article v-for="image in images" :key="image.id" class="image-tile">
            <img :src="mediaPreviewUrl(image)" :alt="image.originalFilename || '动态图片'" />
            <button
              class="media-remove"
              type="button"
              :aria-label="`删除${image.originalFilename || '图片'}`"
              @click="removeMedia(image.id)"
            >
              <Trash2 :size="14" :stroke-width="2" />
            </button>
          </article>
          <button v-if="images.length < 9" class="image-tile image-tile--add" type="button" @click="pickImage">
            <Plus :size="22" :stroke-width="2" />
            <span>继续添加</span>
          </button>
        </div>
      </section>

      <div class="rows">
        <PenSettingRow
          v-for="r in rows"
          :key="r.label"
          :label="r.label"
          :trailing="r.trailing"
          @click="openSheet(r.key)"
        />
      </div>

      <p class="draft-tip">{{ pageCopy.draft }}</p>
    </section>

    <div v-if="activeSheet" class="choice-layer" role="dialog" aria-modal="true" :aria-label="rows.find((r) => r.key === activeSheet)?.label">
      <button class="choice-layer__backdrop" type="button" aria-label="关闭设置" @click="closeSheet" />
      <section class="choice-sheet">
        <header class="choice-sheet__head">
          <h2>{{ rows.find((r) => r.key === activeSheet)?.label }}</h2>
          <button type="button" aria-label="关闭" @click="closeSheet">
            <X :size="16" :stroke-width="2" />
          </button>
        </header>

        <div v-if="activeSheet === 'topic'" class="choice-block">
          <div class="topic-editor">
            <input
              v-model="topicInput"
              type="text"
              maxlength="50"
              placeholder="输入话题名称"
              @keyup.enter="addTopic(topicInput)"
            />
            <button type="button" @click="addTopic(topicInput)">添加</button>
          </div>
          <div class="chip-row">
            <button
              v-for="topic in selectedTopics"
              :key="topic"
              class="chip chip--dark"
              type="button"
              @click="removeTopic(topic)"
            >
              #{{ topic }}
              <X :size="13" :stroke-width="2" />
            </button>
          </div>
          <div class="section-mini">
            <span>{{ topicLoading ? '加载中' : '热门推荐' }}</span>
            <button type="button" @click="loadTopicSuggestions">刷新</button>
          </div>
          <div class="option-list">
            <button
              v-for="topic in topicSuggestions"
              :key="topic.name"
              type="button"
              class="option-row"
              @click="addTopic(topic.name)"
            >
              <span>#{{ topic.name }}</span>
              <em>{{ topic.count }} 条</em>
            </button>
          </div>
        </div>

        <div v-else-if="activeSheet === 'related'" class="option-list">
          <button
            v-for="item in relatedOptions"
            :key="item"
            type="button"
            class="option-row"
            @click="chooseRelated(item)"
          >
            <span>{{ item }}</span>
            <Check v-if="selectedRelated === item" :size="17" :stroke-width="2.4" />
          </button>
        </div>

        <div v-else-if="activeSheet === 'style'" class="chip-grid">
          <button
            v-for="style in danceStyles"
            :key="style"
            type="button"
            :class="['choice-chip', { 'choice-chip--active': selectedStyle === style }]"
            @click="chooseStyle(style)"
          >
            {{ style }}
          </button>
        </div>

        <div v-else-if="activeSheet === 'location'" class="choice-block">
          <button class="locate-button" type="button" :disabled="locating" @click="() => useCurrentLocation()">
            {{ locating ? '定位中…' : geoPoint ? '重新定位' : '使用当前位置' }}
          </button>
          <p v-if="locationError" class="location-error">{{ locationError }}</p>
          <p v-if="geoPoint" class="location-hint">
            附近位置基于 {{ geoPoint.latitude.toFixed(5) }}, {{ geoPoint.longitude.toFixed(5) }}
          </p>
          <p v-if="geoPoint && selectedLocation !== '不显示位置'" class="location-current">
            <strong>{{ selectedLocation }}</strong>
            <small>{{ locationResolvedAddress || '可从下方附近位置中重新选择' }}</small>
          </p>
          <div class="topic-editor">
            <input v-model="locationInput" type="text" maxlength="80" placeholder="搜索附近位置" @keyup.enter="searchLocation" />
            <button type="button" :disabled="locationSearching" @click="searchLocation">
              {{ locationSearching ? '搜索中' : '搜索' }}
            </button>
          </div>
          <div class="option-list">
            <button
              type="button"
              class="option-row option-row--two"
              @click="chooseLocation('不显示位置')"
            >
              <span>
                <strong>不显示位置</strong>
              </span>
              <Check v-if="selectedLocation === '不显示位置'" :size="17" :stroke-width="2.4" />
            </button>
          </div>
          <div v-if="locationCandidates.length" class="option-list">
            <button
              v-for="place in locationCandidates"
              :key="`${place.id || place.title}-${place.latitude}-${place.longitude}`"
              type="button"
              class="option-row option-row--two"
              @click="chooseMapPlace(place)"
            >
              <span>
                <strong>{{ place.title }}</strong>
                <small>{{ place.address || `${place.latitude.toFixed(5)}, ${place.longitude.toFixed(5)}` }}</small>
              </span>
              <Check v-if="selectedLocation === place.title" :size="17" :stroke-width="2.4" />
            </button>
          </div>
        </div>

        <div v-else-if="activeSheet === 'visibility'" class="option-list">
          <button
            v-for="item in visibilityOptions"
            :key="item.value"
            type="button"
            class="option-row option-row--two"
            @click="chooseVisibility(item.value)"
          >
            <span>
              <strong>{{ item.label }}</strong>
              <small>{{ item.desc }}</small>
            </span>
            <Check v-if="selectedVisibility === item.value" :size="17" :stroke-width="2.4" />
          </button>
        </div>

        <div v-else-if="activeSheet === 'work-type'" class="chip-grid">
          <button
            v-for="item in workTypes"
            :key="item"
            type="button"
            :class="['choice-chip', { 'choice-chip--active': selectedWorkType === item }]"
            @click="chooseWorkType(item)"
          >
            {{ item }}
          </button>
        </div>

        <div v-else-if="activeSheet === 'practice-date'" class="chip-grid">
          <button
            v-for="item in practiceDates"
            :key="item"
            type="button"
            :class="['choice-chip', { 'choice-chip--active': selectedPracticeDate === item }]"
            @click="choosePracticeDate(item)"
          >
            {{ item }}
          </button>
        </div>
      </section>
    </div>
  </main>
</template>

<style lang="scss" scoped>
@import '@/styles/pen-nike.scss';

.pen-page {
  @include pen-page;
}

.topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 68px;
  padding: 14px 18px;
  background: $pen-canvas;
  border-bottom: 1px solid $pen-hairline;

  &__copy {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  &__meta {
    margin: 0;
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }

  &__icon {
    width: 40px;
    height: 40px;
    flex: none;
    border: 0;
    border-radius: 999px;
    background: $pen-soft;
    color: $pen-ink;
    display: grid;
    place-items: center;
    cursor: pointer;
  }

  &__pub {
    flex: none;
    height: 36px;
    padding: 8px 16px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 14px;
    font-weight: 800;
    line-height: $pen-lh;
    cursor: pointer;
  }
}

.pen-scroll {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.editor {
  width: 100%;
  min-height: 132px;
  border: 0;
  background: transparent;
  color: $pen-ink;
  font-family: $pen-font;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.45;
  resize: none;
  outline: none;

  &::placeholder {
    color: $pen-mute;
    font-weight: 600;
  }
}

.loading-text {
  margin: 20px 0;
  color: $pen-mute;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
}

.media-section {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;

    h2 {
      margin: 0;
      color: $pen-ink;
      font-size: 17px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    span {
      color: $pen-mute;
      font-size: 12px;
      font-weight: 800;
      line-height: $pen-lh;
    }
  }
}

.media-input {
  display: none;
}

.media-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.media-action {
  height: 72px;
  border: 1px solid $pen-hairline;
  border-radius: 16px;
  background: $pen-soft;
  color: $pen-ink;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 900;
  line-height: $pen-lh;
  cursor: pointer;

  &:disabled {
    color: $pen-mute;
    cursor: not-allowed;
    opacity: 0.56;
  }
}

.video-preview {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  background: $pen-ink;

  video {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    object-fit: cover;
  }
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.image-tile {
  position: relative;
  aspect-ratio: 1;
  border: 1px solid $pen-hairline;
  border-radius: 14px;
  background: $pen-soft;
  color: $pen-ink;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 900;
  line-height: $pen-lh;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--add {
    border-color: $pen-hairline;
    background: $pen-canvas;
    color: $pen-mute;
    cursor: pointer;
  }

}

.media-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: $pen-ink;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.spin {
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.rows {
  display: flex;
  flex-direction: column;
}

.draft-tip {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  background: #f1f8f3;
  color: #007d48;
  font-size: 13px;
  font-weight: 800;
  line-height: $pen-lh;
}

.choice-layer {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: flex-end;

  &__backdrop {
    position: absolute;
    inset: 0;
    border: 0;
    background: rgba(0, 0, 0, 0.28);
  }
}

.choice-sheet {
  position: relative;
  z-index: 1;
  width: 100%;
  max-height: 72vh;
  overflow-y: auto;
  padding: 16px 18px calc(18px + env(safe-area-inset-bottom));
  border-radius: 18px 18px 0 0;
  background: $pen-canvas;
  box-shadow: 0 -12px 36px rgba(0, 0, 0, 0.14);

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 900;
      line-height: $pen-lh;
    }

    button {
      width: 34px;
      height: 34px;
      border: 0;
      border-radius: 999px;
      background: $pen-soft;
      color: $pen-ink;
      display: grid;
      place-items: center;
      cursor: pointer;
    }
  }
}

.choice-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;

  input {
    min-width: 0;
    height: 42px;
    padding: 0 12px;
    border: 1px solid $pen-hairline;
    border-radius: 12px;
    background: $pen-soft;
    color: $pen-ink;
    font-size: 14px;
    font-weight: 800;
    outline: none;
  }

  button {
    height: 42px;
    padding: 0 14px;
    border: 0;
    border-radius: 999px;
    background: $pen-ink;
    color: $pen-on-primary;
    font-size: 13px;
    font-weight: 900;
    cursor: pointer;
  }
}

.chip-row,
.chip-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  min-height: 34px;
  padding: 0 12px;
  border: 0;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;

  &--dark {
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.choice-chip {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid $pen-hairline;
  border-radius: 999px;
  background: $pen-soft;
  color: $pen-ink;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;

  &--active {
    border-color: $pen-ink;
    background: $pen-ink;
    color: $pen-on-primary;
  }
}

.section-mini {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 900;
  line-height: $pen-lh;

  button {
    border: 0;
    background: transparent;
    color: $pen-ink;
    font-size: 12px;
    font-weight: 900;
    cursor: pointer;
  }
}

.option-list {
  display: flex;
  flex-direction: column;
}

.option-row {
  min-height: 50px;
  padding: 12px 0;
  border: 0;
  border-bottom: 1px solid $pen-hairline;
  background: $pen-canvas;
  color: $pen-ink;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  text-align: left;
  cursor: pointer;

  span {
    min-width: 0;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  em {
    flex: none;
    color: $pen-mute;
    font-size: 12px;
    font-style: normal;
    font-weight: 800;
    line-height: $pen-lh;
  }

  &--two span {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  small {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.locate-button {
  width: 100%;
  min-height: 44px;
  border: 0;
  border-radius: 999px;
  background: $pen-ink;
  color: $pen-on-primary;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;

  &:disabled {
    opacity: 0.56;
    cursor: not-allowed;
  }
}

.location-hint {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  background: $pen-soft;
  color: $pen-mute;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
}

.location-current {
  margin: 0;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid $pen-hairline;
  background: $pen-canvas;
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong,
  small {
    margin: 0;
  }

  strong {
    color: $pen-ink;
    font-size: 15px;
    font-weight: 900;
    line-height: $pen-lh;
  }

  small {
    color: $pen-mute;
    font-size: 12px;
    font-weight: 700;
    line-height: $pen-lh;
  }
}

.location-error {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(211, 0, 5, 0.08);
  color: #d30005;
  font-size: 12px;
  font-weight: 800;
  line-height: $pen-lh;
}
</style>
