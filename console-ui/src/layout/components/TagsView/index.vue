<template>
  <div id="tags-view-container" ref="tagsViewContainer" class="tags-view-container">
    <scroll-pane ref="scrollPane" class="tags-view-wrapper" @scroll="handleScroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :class="isActive(tag)?'active':''"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        :data-full-path="tag.fullPath || tag.path"
        tag="span"
        class="tags-view-item"
        :style="activeStyle(tag)"
        @click.middle="!isAffix(tag)?closeSelectedTag(tag):''"
        @contextmenu.prevent="openMenu(tag,$event)"
      >
        {{ tag.title }}
        <el-icon
          v-if="!isAffix(tag)"
          class="tags-close-icon"
          @click.prevent.stop="closeSelectedTag(tag)"
        >
          <Close />
        </el-icon>
      </router-link>
    </scroll-pane>
    <ul v-show="visible" :style="{left:left+'px',top:top+'px'}" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)"><el-icon><RefreshRight /></el-icon> 刷新页面</li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)"><el-icon><Close /></el-icon> 关闭当前</li>
      <li @click="closeOthersTags"><el-icon><CircleClose /></el-icon> 关闭其他</li>
      <li v-if="!isFirstView()" @click="closeLeftTags"><el-icon><Back /></el-icon> 关闭左侧</li>
      <li v-if="!isLastView()" @click="closeRightTags"><el-icon><Right /></el-icon> 关闭右侧</li>
      <li @click="closeAllTags(selectedTag)"><el-icon><CircleClose /></el-icon> 全部关闭</li>
    </ul>
  </div>
</template>

<script setup>
import ScrollPane from './ScrollPane.vue'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { usePermissionStore } from '@/store/modules/permission'
import { useSettingsStore } from '@/store/modules/settings'
import tab from '@/plugins/tab'

function resolveRoutePath(basePath, routePath) {
  const normalizedBase = (basePath || '/').startsWith('/') ? (basePath || '/') : `/${basePath}`
  const base = normalizedBase.endsWith('/') ? normalizedBase : `${normalizedBase}/`
  const resolved = new URL(routePath || '', `http://localhost${base}`).pathname
  return resolved.length > 1 ? resolved.replace(/\/+$/, '') : resolved
}

const route = useRoute()
const router = useRouter()

const scrollPane = ref(null)
const tagsViewContainer = ref(null)

const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})
const affixTags = ref([])

const visitedViews = computed(() => useTagsViewStore().visitedViews)
const routes = computed(() => usePermissionStore().routes)
const theme = computed(() => useSettingsStore().theme)

watch(route, () => {
  addTags()
  moveToCurrentTag()
})

watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu)
  } else {
    document.body.removeEventListener('click', closeMenu)
  }
})

onMounted(() => {
  initTags()
  addTags()
})

function isActive(routeItem) {
  return routeItem.path === route.path
}

function activeStyle(tagItem) {
  if (!isActive(tagItem)) return {}
  return {
    "background-color": theme.value,
    "border-color": theme.value
  }
}

function isAffix(tagItem) {
  return tagItem.meta && tagItem.meta.affix
}

function isFirstView() {
  try {
    return selectedTag.value.fullPath === visitedViews.value[1].fullPath || selectedTag.value.fullPath === '/index'
  } catch (err) {
    return false
  }
}

function isLastView() {
  try {
    return selectedTag.value.fullPath === visitedViews.value[visitedViews.value.length - 1].fullPath
  } catch (err) {
    return false
  }
}

function filterAffixTags(routeList, basePath = '/') {
  let tags = []
  routeList.forEach(routeItem => {
    if (routeItem.meta && routeItem.meta.affix) {
      const tagPath = resolveRoutePath(basePath, routeItem.path)
      tags.push({
        fullPath: tagPath,
        path: tagPath,
        name: routeItem.name,
        meta: { ...routeItem.meta }
      })
    }
    if (routeItem.children) {
      const tempTags = filterAffixTags(routeItem.children, routeItem.path)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })
  return tags
}

function initTags() {
  const tags = affixTags.value = filterAffixTags(routes.value)
  for (const tagItem of tags) {
    if (tagItem.name) {
      useTagsViewStore().addVisitedView(tagItem)
    }
  }
}

function addTags() {
  const { name } = route
  if (name) {
    useTagsViewStore().addView(route)
    if (route.meta.link) {
      useTagsViewStore().addIframeView(route)
    }
  }
  return false
}

function moveToCurrentTag() {
  const currentKey = route.fullPath || route.path
  nextTick(() => {
    scrollPane.value?.moveToTargetByKey(currentKey)
    const currentVisited = visitedViews.value.find((item) => (item.fullPath || item.path) === currentKey)
    if (currentVisited && currentVisited.fullPath !== route.fullPath) {
      useTagsViewStore().updateVisitedView(route)
    }
  })
}

function refreshSelectedTag(view) {
  tab.refreshPage(view)
  if (route.meta.link) {
    useTagsViewStore().delIframeView(route)
  }
}

function closeSelectedTag(view) {
  tab.closePage(view).then(({ visitedViews }) => {
    if (isActive(view)) {
      toLastView(visitedViews, view)
    }
  })
}

function closeRightTags() {
  tab.closeRightPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeLeftTags() {
  tab.closeLeftPage(selectedTag.value).then(visitedViews => {
    if (!visitedViews.find(i => i.fullPath === route.fullPath)) {
      toLastView(visitedViews)
    }
  })
}

function closeOthersTags() {
  router.push(selectedTag.value).catch(() => {})
  tab.closeOtherPage(selectedTag.value).then(() => {
    moveToCurrentTag()
  })
}

function closeAllTags(view) {
  tab.closeAllPage().then(({ visitedViews }) => {
    if (affixTags.value.some(tagItem => tagItem.path === route.path)) {
      return
    }
    toLastView(visitedViews, view)
  })
}

function toLastView(visitedViews, view) {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    if (view.name === 'Dashboard') {
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      router.push('/')
    }
  }
}

function openMenu(tagItem, e) {
  const menuMinWidth = 105
  const container = tagsViewContainer.value
  if (!container) return
  const offsetLeft = container.getBoundingClientRect().left
  const offsetWidth = container.offsetWidth
  const maxLeft = offsetWidth - menuMinWidth
  const leftVal = e.clientX - offsetLeft + 15
  left.value = leftVal > maxLeft ? maxLeft : leftVal
  top.value = e.clientY
  visible.value = true
  selectedTag.value = tagItem
}

function closeMenu() {
  visible.value = false
}

function handleScroll() {
  closeMenu()
}
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
  .tags-view-wrapper {
    .tags-view-item {
      display: inline-block;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid #d8dce5;
      color: #495060;
      background: #fff;
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;
      &:first-of-type {
        margin-left: 15px;
      }
      &:last-of-type {
        margin-right: 15px;
      }
      &.active {
        background-color: #42b983;
        color: #fff;
        border-color: #42b983;
        &::before {
          content: '';
          background: #fff;
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          position: relative;
          margin-right: 2px;
        }
      }
    }
  }
  .contextmenu {
    margin: 0;
    background: #fff;
    z-index: 3000;
    position: absolute;
    list-style-type: none;
    padding: 5px 0;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 400;
    color: #333;
    box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, .3);
    li {
      margin: 0;
      padding: 7px 16px;
      cursor: pointer;
      &:hover {
        background: #eee;
      }
    }
  }
}
</style>

<style lang="scss">
// reset close icon style in tags view
.tags-view-wrapper {
  .tags-view-item {
    .tags-close-icon {
      width: 16px;
      height: 16px;
      vertical-align: 2px;
      border-radius: 50%;
      text-align: center;
      transition: all .3s cubic-bezier(.645, .045, .355, 1);
      transform-origin: 100% 50%;
      svg {
        width: 12px;
        height: 12px;
      }
      &:hover {
        background-color: #b4bccc;
        color: #fff;
      }
    }
  }
}
</style>
