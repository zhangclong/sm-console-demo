import { defineStore } from 'pinia'
import { markRaw } from 'vue'
import { constantRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index.vue'
import ParentView from '@/components/ParentView/index.vue'

const viewModules = import.meta.glob('../../views/**/*.vue')

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: [],
  }),
  actions: {
    setSidebarRouters(routes) {
      this.sidebarRouters = routes
    },
    GenerateRoutes() {
      return getRouters().then((res) => {
        const sdata = JSON.parse(JSON.stringify(res.data))
        const rdata = JSON.parse(JSON.stringify(res.data))
        const sidebarRoutes = filterAsyncRouter(sdata)
        const rewriteRoutes = filterAsyncRouter(rdata, false, true)
        rewriteRoutes.push({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
        this.addRoutes = rewriteRoutes
        this.routes = constantRoutes.concat(rewriteRoutes)
        this.sidebarRouters = constantRoutes.concat(sidebarRoutes)
        this.defaultRoutes = sidebarRoutes
        this.topbarRouters = sidebarRoutes.concat([{
          path: 'index',
          meta: { title: '统计报表', icon: 'dashboard' },
        }])


        return rewriteRoutes
      })
    },
  },
})

function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter((route) => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = markRaw(Layout)
      } else if (route.component === 'ParentView') {
        route.component = markRaw(ParentView)
      } else {
        route.component = markRaw(loadView(route.component))
      }
    }

    if (route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
      if (!route.component && route.children.length) {
        route.component = markRaw(ParentView)
      }
    } else {
      delete route.children
      delete route.redirect
    }

    // Drop invalid leaf routes from backend menu data to prevent null component rendering.
    if (!route.component && !route.redirect) {
      console.warn('[permission] drop route without component:', route.path)
      return false
    }

    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  const children = []
  childrenMap.forEach((el) => {
    if (el.children && el.children.length) {
      if (el.component === 'ParentView') {
        el.children.forEach((c) => {
          c.path = el.path + '/' + c.path
          if (c.children && c.children.length) {
            children.push(...filterChildren(c.children, c))
            return
          }
          children.push(c)
        })
        return
      }
    }
    if (lastRouter) {
      el.path = lastRouter.path + '/' + el.path
    }
    children.push(el)
  })
  return children
}

export function loadView(view) {
  const normalizedView = view.replace(/^\/+|\.vue$/g, '')
  const candidates = [
    `../../views/${normalizedView}.vue`,
    `../../views/${normalizedView}/index.vue`,
  ]

  if (normalizedView.endsWith('/index')) {
    candidates.unshift(`../../views/${normalizedView}.vue`)
  }

  const matchedView = candidates.find((candidate) => Object.prototype.hasOwnProperty.call(viewModules, candidate))

  if (!matchedView) {
    console.error(`[permission] Unknown view component: ${view}`)
    return viewModules['../../views/error/404.vue']
  }

  return viewModules[matchedView]
}
