<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    @select="handleSelect"
  >
    <template v-for="(item, index) in topMenus">
      <el-menu-item :style="{'--theme': theme}" :index="item.path" :key="index" v-if="index < visibleNumber"
        ><svg-icon :icon-class="item.meta.icon" />
        {{ item.meta.title }}</el-menu-item
      >
    </template>

    <!-- 顶部菜单超出数量折叠 -->
    <el-sub-menu :style="{'--theme': theme}" index="more" v-if="topMenus.length > visibleNumber">
      <template #title>更多菜单</template>
      <template v-for="(item, index) in topMenus">
        <el-menu-item
          :index="item.path"
          :key="index"
          v-if="index >= visibleNumber"
          ><svg-icon :icon-class="item.meta.icon" />
          {{ item.meta.title }}</el-menu-item
        >
      </template>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { constantRoutes } from "@/router";
import { useSettingsStore } from '@/store/modules/settings'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'

const hideList = ['/index', '/user/profile']

const route = useRoute()
const router = useRouter()

const visibleNumber = ref(5)
const currentIndex = ref(undefined)

const theme = computed(() => useSettingsStore().theme)
const routers = computed(() => usePermissionStore().topbarRouters)

const topMenus = computed(() => {
  let topMenus = []
  routers.value.map((menu) => {
    if (menu.hidden !== true) {
      if (menu.path === "/") { topMenus.push(menu.children[0]) }
      else { topMenus.push(menu) }
    }
  })
  return topMenus
})

const childrenMenus = computed(() => {
  var childrenMenus = []
  routers.value.map((r) => {
    for (var item in r.children) {
      if (r.children[item].parentPath === undefined) {
        if (r.path === "/") {
          r.children[item].path = "/" + r.children[item].path
        } else {
          if (!ishttp(r.children[item].path)) {
            r.children[item].path = r.path + "/" + r.children[item].path
          }
        }
        r.children[item].parentPath = r.path
      }
      childrenMenus.push(r.children[item])
    }
  })
  return constantRoutes.concat(childrenMenus)
})

const activeMenu = computed(() => {
  const path = route.path
  let activePath = path
  if (path !== undefined && path.lastIndexOf("/") > 0 && hideList.indexOf(path) === -1) {
    const tmpPath = path.substring(1, path.length)
    activePath = "/" + tmpPath.substring(0, tmpPath.indexOf("/"))
    useAppStore().toggleSideBarHide(false)
  } else if (!route.children) {
    activePath = path
    useAppStore().toggleSideBarHide(true)
  }
  activeRoutes(activePath)
  return activePath
})

function setVisibleNumber() {
  const width = document.body.getBoundingClientRect().width / 3
  visibleNumber.value = parseInt(width / 85)
}

function handleSelect(key, keyPath) {
  currentIndex.value = key
  const routeItem = routers.value.find(item => item.path === key)
  if (ishttp(key)) {
    window.open(key, "_blank")
  } else if (!routeItem || !routeItem.children) {
    router.push({ path: key })
    useAppStore().toggleSideBarHide(true)
  } else {
    activeRoutes(key)
    useAppStore().toggleSideBarHide(false)
  }
}

function activeRoutes(key) {
  var routes = []
  if (childrenMenus.value && childrenMenus.value.length > 0) {
    childrenMenus.value.map((item) => {
      if (key == item.parentPath || (key == "index" && "" == item.path)) {
        routes.push(item)
      }
    })
  }
  if (routes.length > 0) {
    usePermissionStore().setSidebarRouters(routes)
  }
}

function ishttp(url) {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

onMounted(() => {
  window.addEventListener('resize', setVisibleNumber)
  setVisibleNumber()
})

onBeforeUnmount(() => window.removeEventListener('resize', setVisibleNumber))
</script>

<style lang="scss">
.topmenu-container.el-menu--horizontal > .el-menu-item {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}

.topmenu-container.el-menu--horizontal > .el-menu-item.is-active, .el-menu--horizontal > .el-submenu.is-active .el-submenu__title {
  border-bottom: 2px solid #{'var(--theme)'} !important;
  color: #303133;
}

/* submenu item */
.topmenu-container.el-menu--horizontal > .el-submenu .el-submenu__title {
  float: left;
  height: 50px !important;
  line-height: 50px !important;
  color: #999093 !important;
  padding: 0 5px !important;
  margin: 0 10px !important;
}
</style>
