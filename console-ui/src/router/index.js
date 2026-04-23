import { createRouter, createWebHistory } from 'vue-router'

/* Layout */
import Layout from '@/layout/index.vue'

// Public routes (always available)
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect'),
      },
    ],
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true,
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true,
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true,
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: '概览',
        meta: { title: '概览', icon: 'dashboard', noCache: true, affix: true },
      },
    ],
  },
  {
    path: '/system/ucenter',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' },
      },
    ],
  },
  {
    path: '/system/role',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        component: () => import('@/views/system/role/index.vue'),
        name: 'RoleBootstrap',
        meta: { title: '角色管理' },
      },
    ],
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' },
      },
    ],
  },
  {
    path: '/rds/version-pkg',
    component: Layout,
    hidden: true,
    permissions: ['console:rdsversion:query'],
    children: [
      {
        path: 'version/:versionId(\\d+)',
        component: () => import('@/views/console/rdsversion/versionPkg'),
        name: 'VersionPkg',
        meta: { title: '安装包管理', activeMenu: '/system/rdsversion' },
      },
    ],
  },
  {
    path: '/rds/template-mgr',
    component: Layout,
    hidden: true,
    permissions: ['console:tempgroup:query'],
    children: [
      {
        path: 'group/:groupId(\\d+)',
        component: () => import('@/views/console/tempgroup/templateMgr'),
        name: 'hostTemplatesMgr',
        meta: { title: '模版管理', activeMenu: '/system/tempgroup' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.VUE_APP_CONTEXT_PATH),
  scrollBehavior: () => ({ top: 0 }),
  routes: constantRoutes,
})

export default router
