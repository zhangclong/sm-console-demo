import { createApp } from 'vue'
import Cookies from 'js-cookie'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'virtual:svg-icons-register'
import '@/assets/styles/index.scss'

import App from './App.vue'
import store from './store'
import router from './router'
import directive from './directive'
import plugins from './plugins'
import { download } from '@/utils/request'
import './permission'

import { getDicts } from '@/api/system/dict/data'
import { getConfigKey } from '@/api/system/config'
import {
  parseTime,
  resetForm,
  addDateRange,
  selectDictLabel,
  selectDictLabels,
  handleTree,
} from '@/utils/ruoyi'

import Pagination from '@/components/Pagination/index.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import Editor from '@/components/Editor/index.vue'
import FileUpload from '@/components/FileUpload/index.vue'
import ImageUpload from '@/components/ImageUpload/index.vue'
import ImagePreview from '@/components/ImagePreview/index.vue'
import DictTag from '@/components/DictTag/index.vue'
import DictData from '@/components/DictData'
import SvgIcon from '@/components/SvgIcon/index.vue'
import { getAppConfigKeys } from '@/api/system/config'

const app = createApp(App)

// Ensure home view can safely access $appConfig before async config is loaded.
app.config.globalProperties.$appConfig = {}

// Global properties (replaces Vue.prototype.*)
app.config.globalProperties.getDicts = getDicts
app.config.globalProperties.getConfigKey = getConfigKey
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels
app.config.globalProperties.download = download
app.config.globalProperties.handleTree = handleTree

// Global components
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)
app.component('SvgIcon', SvgIcon)

app.use(store)
app.use(router)
app.use(directive)
app.use(plugins)

app.use(ElementPlus, {
  size: Cookies.get('size') || 'default',
  locale: zhCn,
})


// Register all Element Plus icons globally
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

DictData.install(app)

// Fetch app config and set favicon
getAppConfigKeys()
  .then((response) => {
    app.config.globalProperties.$appConfig = response.data
  })
  .catch((error) => {
    console.error('Failed to fetch appConfig:', error)
  })

app.mount('#app')
