<template>
  <div :class="classObj" class="app-wrapper" :style="{'--current-color': theme}">
    <div v-if="device==='mobile'&&sidebarState.opened" class="drawer-bg" @click="handleClickOutside"/>
    <Sidebar v-if="!sidebarState.hide" class="sidebar-container" />
    <div :class="{hasTagsView:needTagsView,sidebarHide:sidebarState.hide}" class="main-container">
      <div :class="{'fixed-header':fixedHeader}">
        <Navbar />
        <TagsView v-if="needTagsView" />
      </div>
      <AppMain />
      <RightPanel>
        <Settings />
      </RightPanel>
    </div>
  </div>
</template>

<script setup>
import RightPanel from '@/components/RightPanel/index.vue'
import AppMain from './components/AppMain.vue'
import Navbar from './components/Navbar.vue'
import Settings from './components/Settings/index.vue'
import Sidebar from './components/Sidebar/index.vue'
import TagsView from './components/TagsView/index.vue'
import { useResize } from '@/composables/useResize'
import { useAppStore } from '@/store/modules/app'
import { useSettingsStore } from '@/store/modules/settings'

useResize()

const theme = computed(() => useSettingsStore().theme)
const sideTheme = computed(() => useSettingsStore().sideTheme)
const sidebarState = computed(() => useAppStore().sidebar)
const device = computed(() => useAppStore().device)
const needTagsView = computed(() => useSettingsStore().tagsView)
const fixedHeader = computed(() => useSettingsStore().fixedHeader)
const classObj = computed(() => ({
  hideSidebar: !sidebarState.value.opened,
  openSidebar: sidebarState.value.opened,
  withoutAnimation: sidebarState.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

function handleClickOutside() {
  useAppStore().closeSideBar({ withoutAnimation: false })
}
</script>

<style lang="scss" scoped>
  @use "@/assets/styles/mixin.scss" as *;
  @use "@/assets/styles/variables.scss" as *;

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;

    &.mobile.openSidebar {
      position: fixed;
      top: 0;
    }
  }

  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

 .el-login-footer {
  height: 48px !important;
  line-height: 48px;
  position: absolute;
  bottom: 0px;
  width: 100%;
  text-align: center;
  background-color: #f7f8f9;
  color: rgb(168, 168, 168);
  font-size: 12px;
  font-weight: 400;
}

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$base-sidebar-width});
    transition: width 0.28s;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 54px);
  }

  .sidebarHide .fixed-header {
    width: 100%;
  }

  .mobile .fixed-header {
    width: 100%;
  }
</style>
