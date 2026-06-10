export interface CityOption {
  id: number;
  name: string;
}

export const CITY_OPTIONS: CityOption[] = [
  { id: 1, name: '北京' },
  { id: 2, name: '上海' },
  { id: 3, name: '广州' },
  { id: 4, name: '深圳' },
  { id: 5, name: '杭州' },
  { id: 6, name: '成都' },
  { id: 7, name: '武汉' },
  { id: 8, name: '西安' },
  { id: 9, name: '南京' },
  { id: 10, name: '长沙' }
];

export const CITY_NAME_BY_ID: Record<number, string> = Object.fromEntries(
  CITY_OPTIONS.map((city) => [city.id, city.name])
) as Record<number, string>;

export const getCityName = (cityId?: number | null) =>
  cityId ? CITY_NAME_BY_ID[cityId] ?? `城市 #${cityId}` : '';
