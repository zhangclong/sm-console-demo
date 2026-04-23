import router from './router'
import { useUserStore } from './store/modules/user'
import { useSettingsStore } from './store/modules/settings'
import { usePermissionStore } from './store/modules/permission'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/auth-redirect', '/bind']

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  if (getToken()) {
    const settingsStore = useSettingsStore()
    if (to.meta?.title) settingsStore.setTitle(to.meta.title)

    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      const userStore = useUserStore()
      if (userStore.roles.length === 0) {
        isRelogin.show = true
        try {
          await userStore.GetInfo()
          isRelogin.show = false
          const permissionStore = usePermissionStore()
          const accessRoutes = await permissionStore.GenerateRoutes()
          accessRoutes.forEach((route) => router.addRoute(route))
          next({ ...to, replace: true })
        } catch (err) {
          await userStore.LogOut()
          ElMessage.error(err)
          next({ path: '/' })
        }
      } else {
        next()
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
