import { mock } from '../index';

const PLACES = [
  {
    id: 'bd-map-universal-mels',
    title: '梅尔斯餐厅',
    address: '北京市通州区北京环球度假区好莱坞景区',
    category: '餐饮服务;西式快餐',
    latitude: 39.85362,
    longitude: 116.67618,
    adcode: '110112'
  },
  {
    id: 'bd-map-universal-peets',
    title: '皮爷咖啡',
    address: '北京市通州区北京环球城市大道',
    category: '餐饮服务;咖啡厅',
    latitude: 39.85518,
    longitude: 116.67562,
    adcode: '110112'
  },
  {
    id: 'bd-map-universal-resort',
    title: '北京环球度假区',
    address: '北京市通州区京哈高速与东六环路交会处西北角',
    category: '旅游景点;主题公园',
    latitude: 39.85506,
    longitude: 116.67595,
    adcode: '110112'
  },
  {
    id: 'bd-map-1',
    title: '灵动空间',
    address: '北京市朝阳区望京街道阜通东大街',
    category: '舞蹈工作室',
    latitude: 39.992748,
    longitude: 116.480283,
    adcode: '110105'
  },
  {
    id: 'bd-map-2',
    title: 'Urban Flow 舞室',
    address: '北京市海淀区五道口成府路',
    category: '舞蹈工作室',
    latitude: 39.993975,
    longitude: 116.337977,
    adcode: '110108'
  },
  {
    id: 'bd-map-3',
    title: '节奏盒子课程中心',
    address: '上海市浦东新区世纪大道',
    category: '舞蹈培训',
    latitude: 31.230382,
    longitude: 121.527229,
    adcode: '310115'
  }
];

const distance = (aLat?: number, aLng?: number, bLat?: number, bLng?: number) => {
  if (![aLat, aLng, bLat, bLng].every((n) => Number.isFinite(n))) return 0;
  return Math.hypot(Number(aLat) - Number(bLat), Number(aLng) - Number(bLng));
};

const nearestPlace = (latitude: number, longitude: number) =>
  PLACES.slice().sort((a, b) => distance(latitude, longitude, a.latitude, a.longitude) - distance(latitude, longitude, b.latitude, b.longitude))[0];

const nearbyPlaces = (latitude: number, longitude: number) =>
  PLACES.slice().sort((a, b) => distance(latitude, longitude, a.latitude, a.longitude) - distance(latitude, longitude, b.latitude, b.longitude)).slice(0, 10);

const districtOf = (place: (typeof PLACES)[number]) => {
  if (place.address.includes('通州')) return '通州区';
  if (place.address.includes('朝阳')) return '朝阳区';
  if (place.address.includes('浦东')) return '浦东新区';
  if (place.address.includes('海淀')) return '海淀区';
  return '北京市';
};

mock('get', /\/(?:admin|h5)\/maps\/tencent\/places$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const keyword = String(p.keyword ?? '').trim().toLowerCase();
  const lat = Number(p.latitude);
  const lng = Number(p.longitude);
  const list = PLACES
    .filter((place) => !keyword || `${place.title}${place.address}${place.category}`.toLowerCase().includes(keyword))
    .sort((a, b) => distance(lat, lng, a.latitude, a.longitude) - distance(lat, lng, b.latitude, b.longitude));
  return {
    list,
    page: Number(p.page ?? 1),
    pageSize: Number(p.pageSize ?? 10),
    total: list.length
  };
});

mock('get', /\/(?:admin|h5)\/maps\/tencent\/geocode$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const address = String(p.address ?? '').trim();
  const matched =
    PLACES.find((place) => `${place.title}${place.address}`.includes(address)) ||
    PLACES.find((place) => address.includes(place.title)) ||
    PLACES[0];
  return {
    title: matched.title,
    address: matched.address,
    latitude: matched.latitude,
    longitude: matched.longitude,
    adcode: matched.adcode,
    province: matched.address.startsWith('上海') ? '上海市' : '北京市',
    city: matched.address.startsWith('上海') ? '上海市' : '北京市',
    district: matched.address.includes('朝阳') ? '朝阳区' : matched.address.includes('浦东') ? '浦东新区' : '海淀区'
  };
});

mock('get', /\/(?:admin|h5)\/maps\/tencent\/reverse-geocode$/, ({ params }) => {
  const p = (params ?? {}) as Record<string, unknown>;
  const latitude = Number(p.latitude);
  const longitude = Number(p.longitude);
  const matched = nearestPlace(latitude, longitude);
  const pois = nearbyPlaces(latitude, longitude);
  const district = districtOf(matched);
  return {
    title: matched.title,
    address: matched.address,
    latitude,
    longitude,
    adcode: matched.adcode,
    province: latitude > 30 && latitude < 32 && longitude > 120 && longitude < 122 ? '上海市' : '北京市',
    city: latitude > 30 && latitude < 32 && longitude > 120 && longitude < 122 ? '上海市' : '北京市',
    district,
    pois
  };
});

mock('get', /\/h5\/maps\/tencent\/ip-location$/, () => {
  const matched = PLACES[0];
  return {
    title: '通州区',
    address: '北京市通州区',
    latitude: matched.latitude,
    longitude: matched.longitude,
    adcode: matched.adcode,
    province: '北京市',
    city: '北京市',
    district: '通州区',
    pois: []
  };
});
