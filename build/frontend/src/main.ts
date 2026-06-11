import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
// 函数式组件(showToast/showDialog)的样式不会被 VantResolver 自动注入,必须手动引入
import 'vant/es/toast/style';
import 'vant/es/dialog/style';
import './styles/index.scss';
import './mock';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
