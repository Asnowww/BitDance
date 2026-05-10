import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import { getToken } from '@/utils/request';

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
    meta: { title: '训练打卡', requiresAuth: true }
  },
  {
    path: '/publish/practice',
    name: 'publish-practice',
    component: () => import('@/pages/publish/PublishPracticePage.vue'),
    meta: { title: '发起约练', requiresAuth: true }
  },
  {
    path: '/publish/review',
    name: 'publish-review',
    component: () => import('@/pages/publish/PublishReviewPage.vue'),
    meta: { title: '写评价', requiresAuth: true }
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
    meta: { tab: 'me', title: '我的', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/user/LoginPage.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/pages/home/SearchPage.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/studio/:id',
    name: 'studio-detail',
    component: () => import('@/pages/studio/StudioDetailPage.vue'),
    meta: { title: '舞室详情' }
  },
  {
    path: '/course/:id',
    name: 'course-detail',
    component: () => import('@/pages/studio/CourseDetailPage.vue'),
    meta: { title: '课程详情' }
  },
  {
    path: '/coach/:id',
    name: 'coach-detail',
    component: () => import('@/pages/studio/CoachDetailPage.vue'),
    meta: { title: '教练详情' }
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('@/pages/user/FavoritesPage.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
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

router.beforeEach((to) => {
  if (to.meta?.requiresAuth && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  return true;
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title);
  }
});

export default router;
