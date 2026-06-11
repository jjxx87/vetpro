/**
 * @cn-file
 * @file src/router/index.js
 * @desc 路由：页面路由配置
 */

import { createRouter, createWebHistory } from 'vue-router'
import List from '../views/ReimbursementList.vue'
import Detail from '../views/ReimbursementDetail.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'list',
      component: List,
      meta: { keepAlive: true }
    },
    {
      path: '/detail/:id?',
      name: 'detail',
      component: Detail
    }
  ]
})

export default router
