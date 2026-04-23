<template>
  <div :class="{'show':show}" class="header-search">
    <svg-icon class-name="search-icon" icon-class="search" @click.stop="click" />
    <el-select
      ref="headerSearchSelect"
      v-model="search"
      :remote-method="querySearch"
      filterable
      default-first-option
      remote
      placeholder="Search"
      class="header-search-select"
      @change="change"
    >
      <el-option v-for="option in options" :key="option.item.path" :value="option.item" :label="option.item.title.join(' > ')" />
    </el-select>
  </div>
</template>

<script setup>
// fuse is a lightweight fuzzy-search module
// make search results more in line with expectations
import Fuse from 'fuse.js'
import { switchOnDev, switchOffDev } from '@/api/system/config'
import { usePermissionStore } from '@/store/modules/permission'
import { ElMessageBox, ElMessage } from 'element-plus'

function resolveRoutePath(basePath, routePath) {
  const normalizedBase = (basePath || '/').startsWith('/') ? (basePath || '/') : `/${basePath}`
  const base = normalizedBase.endsWith('/') ? normalizedBase : `${normalizedBase}/`
  const resolved = new URL(routePath || '', `http://localhost${base}`).pathname
  return resolved.length > 1 ? resolved.replace(/\/+$/, '') : resolved
}

const router = useRouter()
const permissionStore = usePermissionStore()

const headerSearchSelect = ref(null)
const search = ref('')
const options = ref([])
const searchPool = ref([])
const show = ref(false)
let fuse

const routes = computed(() => permissionStore.routes)

watch(routes, () => { searchPool.value = generateRoutes(routes.value) })
watch(searchPool, (list) => { initFuse(list) })
watch(show, (value) => {
  if (value) { document.body.addEventListener('click', close) }
  else { document.body.removeEventListener('click', close) }
})

onMounted(() => {
  searchPool.value = generateRoutes(routes.value)
})

function click() {
  show.value = !show.value
  if (show.value) { headerSearchSelect.value && headerSearchSelect.value.focus() }
}

function close() {
  headerSearchSelect.value && headerSearchSelect.value.blur()
  options.value = []
  show.value = false
}

function change(val) {
  const path = val.path
  if (ishttp(val.path)) {
    // http(s):// 路径新窗口打开
    const pindex = path.indexOf("http")
    window.open(path.substr(pindex, path.length), "_blank")
  } else {
    router.push(val.path)
  }
  search.value = ''
  options.value = []
  nextTick(() => { show.value = false })
}

function initFuse(list) {
  fuse = new Fuse(list, {
    shouldSort: true,
    threshold: 0.4,
    location: 0,
    distance: 100,
    maxPatternLength: 32,
    minMatchCharLength: 1,
    keys: [{ name: 'title', weight: 0.7 }, { name: 'path', weight: 0.3 }]
  })
}

// Filter out the routes that can be displayed in the sidebar
// And generate the internationalized title
function generateRoutes(routeList, basePath = '/', prefixTitle = []) {
  let res = []
  for (const route of routeList) {
    // skip hidden router
    if (route.hidden) { continue }
    const data = {
      path: !ishttp(route.path) ? resolveRoutePath(basePath, route.path) : route.path,
      title: [...prefixTitle]
    }
    if (route.meta && route.meta.title) {
      data.title = [...data.title, route.meta.title]
      if (route.redirect !== 'noRedirect') {
        // only push the routes with title
        // special case: need to exclude parent router without redirect
        res.push(data)
      }
    }
    // recursive child routes
    if (route.children) {
      const tempRoutes = generateRoutes(route.children, data.path, data.title)
      if (tempRoutes.length >= 1) { res = [...res, ...tempRoutes] }
    }
  }
  return res
}

function querySearch(query) {
  if (query !== '') {
    let queryLcase = query.trim().toLowerCase()
    if (queryLcase == 'switch on development mode') {
      ElMessageBox.prompt('请输入管理员密码', '开启开发者模式', {
        inputType: 'password',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => { requestSwitchOnDev(value) }).catch(() => {})
    } else if (queryLcase == 'switch off development mode') {
      ElMessageBox.prompt('请输入管理员密码', '关闭开发者模式', {
        inputType: 'password',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => { requestSwitchOffDev(value) }).catch(() => {})
    } else {
      options.value = fuse.search(query)
    }
  } else {
    options.value = []
  }
}

function ishttp(url) {
  return url.indexOf('http://') !== -1 || url.indexOf('https://') !== -1
}

async function requestSwitchOnDev(passwd) {
  let data = await switchOnDev(passwd)
  if (data.code === 200) {
    ElMessage({ type: 'success', message: '已切换为开发者模式！' })
  }
}

async function requestSwitchOffDev(passwd) {
  let data = await switchOffDev(passwd)
  if (data.code === 200) {
    ElMessage({ type: 'success', message: '已关闭开发者模式！' })
  }
}
</script>

<style lang="scss" scoped>
.header-search {
  font-size: 16px !important;

  .search-icon {
    cursor: pointer;
    font-size: 18px;
    vertical-align: middle;
  }

  .header-search-select {
    font-size: 18px;
    transition: width 0.2s;
    width: 0;
    overflow: hidden;
    background: transparent;
    border-radius: 0;
    display: inline-block;
    vertical-align: middle;

    :deep(.el-input__inner) {
      border-radius: 0;
      border: 0;
      padding-left: 0;
      padding-right: 0;
      box-shadow: none !important;
      border-bottom: 1px solid #d9d9d9;
      vertical-align: middle;
    }
  }

  &.show {
    .header-search-select {
      width: 210px;
      margin-left: 10px;
    }
  }
}
</style>
