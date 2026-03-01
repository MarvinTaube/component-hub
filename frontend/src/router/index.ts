import { createRouter, createWebHistory } from 'vue-router';
import PartList from '../views/PartList.vue';
import PartDetail from '../views/PartDetail.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/Dashboard.vue'),
    },
    {
      path: '/inventory',
      name: 'home',
      component: PartList,
    },
    {
      path: '/part/:id',
      name: 'part-detail',
      component: PartDetail,
      props: true
    },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('../views/ProjectList.vue'),
    },
    {
      path: '/storage',
      name: 'storage',
      component: () => import('../views/StorageView.vue'),
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('../views/CategoryList.vue'),
    },
    {
      path: '/tags',
      name: 'tags',
      component: () => import('../views/TagList.vue'),
    }
  ],
});

export default router;
