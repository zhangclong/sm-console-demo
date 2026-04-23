<template>
  <div v-if="!item.hidden">
    <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown':!isNest}">
          <item :icon="onlyOneChild.meta.icon||(item.meta&&item.meta.icon)" :title="onlyOneChild.meta.title" />
        </el-menu-item>
      </app-link>
    </template>

    <el-sub-menu v-else ref="subMenu" :index="resolvePath(item.path)" popper-append-to-body>
      <template #title>
        <item v-if="item.meta" :icon="item.meta && item.meta.icon" :title="item.meta.title" />
      </template>
      <SidebarItem
        v-for="child in item.children"
        :key="child.path"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { isExternal } from '@/utils/validate'
import Item from './Item.vue'
import AppLink from './Link.vue'
import { useAppStore } from '@/store/modules/app'

defineOptions({ name: 'SidebarItem' })

function resolveRoutePath(basePath, routePath) {
  const normalizedBase = (basePath || '/').startsWith('/') ? (basePath || '/') : `/${basePath}`
  const base = normalizedBase.endsWith('/') ? normalizedBase : `${normalizedBase}/`
  const resolved = new URL(routePath || '', `http://localhost${base}`).pathname
  return resolved.length > 1 ? resolved.replace(/\/+$/, '') : resolved
}

const props = defineProps({
  item: { type: Object, required: true },
  isNest: { type: Boolean, default: false },
  basePath: { type: String, default: '' }
})

const onlyOneChild = ref(null)
const subMenu = ref(null)

// inlined from FixiOSBug mixin
const device = computed(() => useAppStore().device)

function fixBugIniOS() {
  const $subMenu = subMenu.value
  if ($subMenu) {
    const handleMouseleave = $subMenu.handleMouseleave
    $subMenu.handleMouseleave = (e) => {
      if (device.value === 'mobile') return
      handleMouseleave(e)
    }
  }
}

onMounted(() => fixBugIniOS())

function hasOneShowingChild(children = [], parent) {
  if (!children) children = []
  const showingChildren = children.filter(item => {
    if (item.hidden) { return false }
    else { onlyOneChild.value = item; return true }
  })
  if (showingChildren.length === 1) return true
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }
  return false
}

function resolvePath(routePath, routeQuery) {
  if (isExternal(routePath)) return routePath
  if (isExternal(props.basePath)) return props.basePath
  if (routeQuery) {
    let query = JSON.parse(routeQuery)
    return { path: resolveRoutePath(props.basePath, routePath), query: query }
  }
  return resolveRoutePath(props.basePath, routePath)
}
</script>
