<template>
  <el-drawer size="280px" v-model="visible" :with-header="false" :append-to-body="true" :show-close="false">
    <div class="drawer-container">
      <div>
        <div class="setting-drawer-content">
          <div class="setting-drawer-title">
            <h3 class="drawer-title">主题风格设置</h3>
          </div>
          <div class="setting-drawer-block-checbox">
            <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-dark')">
              <img src="@/assets/images/dark.svg" alt="dark">
              <div v-if="sideTheme === 'theme-dark'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
                <i aria-label="图标: check" class="anticon anticon-check">
                  <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class="">
                    <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                  </svg>
                </i>
              </div>
            </div>
            <div class="setting-drawer-block-checbox-item" @click="handleTheme('theme-light')">
              <img src="@/assets/images/light.svg" alt="light">
              <div v-if="sideTheme === 'theme-light'" class="setting-drawer-block-checbox-selectIcon" style="display: block;">
                <i aria-label="图标: check" class="anticon anticon-check">
                  <svg viewBox="64 64 896 896" data-icon="check" width="1em" height="1em" :fill="theme" aria-hidden="true" focusable="false" class="">
                    <path d="M912 190h-69.9c-9.8 0-19.1 4.5-25.1 12.2L404.7 724.5 207 474a32 32 0 0 0-25.1-12.2H112c-6.7 0-10.4 7.7-6.3 12.9l273.9 347c12.8 16.2 37.4 16.2 50.3 0l488.4-618.9c4.1-5.1.4-12.8-6.3-12.8z"/>
                  </svg>
                </i>
              </div>
            </div>
          </div>

          <div class="drawer-item">
            <span>主题颜色</span>
            <theme-picker style="float: right;height: 26px;margin: -3px 8px 0 0;" @change="themeChange" />
          </div>
        </div>

        <el-divider/>

        <h3 class="drawer-title">系统布局配置</h3>

        <div class="drawer-item">
          <span>开启 TopNav</span>
          <el-switch v-model="topNav" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>开启 Tags-Views</span>
          <el-switch v-model="tagsView" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>固定 Header</span>
          <el-switch v-model="fixedHeader" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>显示 Logo</span>
          <el-switch v-model="sidebarLogo" class="drawer-switch" />
        </div>

        <div class="drawer-item">
          <span>动态标题</span>
          <el-switch v-model="dynamicTitle" class="drawer-switch" />
        </div>

        <el-divider/>

        <el-button size="small" type="primary"  icon="DocumentAdd" @click="saveSetting">保存配置</el-button>
        <el-button size="small"  icon="Refresh" @click="resetSetting">重置配置</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import ThemePicker from '@/components/ThemePicker'
import { useSettingsStore } from '@/store/modules/settings'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'
import modal from '@/plugins/modal'
import cache from '@/plugins/cache'

const theme = ref(useSettingsStore().theme)
const sideTheme = ref(useSettingsStore().sideTheme)

const visible = computed(() => useSettingsStore().showSettings)
const fixedHeader = computed({
  get: () => useSettingsStore().fixedHeader,
  set: (val) => useSettingsStore().changeSetting({ key: 'fixedHeader', value: val })
})
const topNav = computed({
  get: () => useSettingsStore().topNav,
  set: (val) => {
    useSettingsStore().changeSetting({ key: 'topNav', value: val })
    if (!val) {
      useAppStore().toggleSideBarHide(false)
      usePermissionStore().setSidebarRouters(usePermissionStore().defaultRoutes)
    }
  }
})
const tagsView = computed({
  get: () => useSettingsStore().tagsView,
  set: (val) => useSettingsStore().changeSetting({ key: 'tagsView', value: val })
})
const sidebarLogo = computed({
  get: () => useSettingsStore().sidebarLogo,
  set: (val) => useSettingsStore().changeSetting({ key: 'sidebarLogo', value: val })
})
const dynamicTitle = computed({
  get: () => useSettingsStore().dynamicTitle,
  set: (val) => useSettingsStore().changeSetting({ key: 'dynamicTitle', value: val })
})

function themeChange(val) {
  useSettingsStore().changeSetting({ key: 'theme', value: val })
  theme.value = val
}

function handleTheme(val) {
  useSettingsStore().changeSetting({ key: 'sideTheme', value: val })
  sideTheme.value = val
}

function saveSetting() {
  modal.loading("正在保存到本地，请稍候...")
  cache.local.set(
    "layout-setting",
    `{"topNav":${topNav.value},"tagsView":${tagsView.value},"fixedHeader":${fixedHeader.value},"sidebarLogo":${sidebarLogo.value},"dynamicTitle":${dynamicTitle.value},"sideTheme":"${sideTheme.value}","theme":"${theme.value}"}`
  )
  setTimeout(modal.closeLoading(), 1000)
}

function resetSetting() {
  modal.loading("正在清除设置缓存并刷新，请稍候...")
  cache.local.remove("layout-setting")
  setTimeout("window.location.reload()", 1000)
}
</script>

<style lang="scss" scoped>
  .setting-drawer-content {
    .setting-drawer-title {
      margin-bottom: 12px;
      color: rgba(0, 0, 0, .85);
      font-size: 14px;
      line-height: 22px;
      font-weight: bold;
    }

    .setting-drawer-block-checbox {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin-top: 10px;
      margin-bottom: 20px;

      .setting-drawer-block-checbox-item {
        position: relative;
        margin-right: 16px;
        border-radius: 2px;
        cursor: pointer;

        img {
          width: 48px;
          height: 48px;
        }

        .setting-drawer-block-checbox-selectIcon {
          position: absolute;
          top: 0;
          right: 0;
          width: 100%;
          height: 100%;
          padding-top: 15px;
          padding-left: 24px;
          color: #1890ff;
          font-weight: 700;
          font-size: 14px;
        }
      }
    }
  }

  .drawer-container {
    padding: 20px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;

    .drawer-title {
      margin-bottom: 12px;
      color: rgba(0, 0, 0, .85);
      font-size: 14px;
      line-height: 22px;
    }

    .drawer-item {
      color: rgba(0, 0, 0, .65);
      font-size: 14px;
      padding: 12px 0;
    }

    .drawer-switch {
      float: right
    }
  }
</style>
