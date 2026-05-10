import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/pages/home/HomePage.vue'),
    meta: { tab: 'home', title: 'BitDance' }
  },
  {
    path: '/practice',
    name: 'practice-square',
    component: () => import('@/pages/practice/PracticeSquarePage.vue'),
    meta: { tab: 'practice', title: '约练广场' }
  },
  {
    path: '/publish/checkin',
    name: 'publish-checkin',
    component: () => import('@/pages/publish/PublishCheckinPage.vue'),
    meta: { title: '训练打卡' }
  },
  {
    path: '/publish/practice',
    name: 'publish-practice',
    component: () => import('@/pages/publish/PublishPracticePage.vue'),
    meta: { title: '发起约练' }
  },
  {
    path: '/publish/review',
    name: 'publish-review',
    component: () => import('@/pages/publish/PublishReviewPage.vue'),
    meta: { title: '写评价' }
  },
  {
    path: '/growth',
    name: 'growth',
    component: () => import('@/pages/growth/GrowthPage.vue'),
    meta: { tab: 'growth', title: '成长' }
  },
  {
    path: '/me',
    name: 'me',
    component: () => import('@/pages/user/UserCenterPage.vue'),
    meta: { tab: 'me', title: '我的' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/user/LoginPage.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/pages/common/NotFoundPage.vue')
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title);
  }
});

export default router;
