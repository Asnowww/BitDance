import { defineStore } from 'pinia';
import { ref } from 'vue';

const CITY_KEY = 'bitdance_city';

export const CITY_LIST = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '长沙'];

export const useAppStore = defineStore('app', () => {
  const city = ref<string>(localStorage.getItem(CITY_KEY) ?? '北京');

  const setCity = (next: string) => {
    city.value = next;
    localStorage.setItem(CITY_KEY, next);
  };

  return { city, setCity };
});
