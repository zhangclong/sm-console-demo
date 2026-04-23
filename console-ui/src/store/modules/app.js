import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true,
      withoutAnimation: false,
      hide: false,
    },
    device: 'desktop',
    size: Cookies.get('size') || 'default',
  }),
  actions: {
    toggleSideBar() {
      if (this.sidebar.hide) return
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
      Cookies.set('sidebarStatus', this.sidebar.opened ? 1 : 0)
    },
    closeSideBar({ withoutAnimation }) {
      Cookies.set('sidebarStatus', 0)
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },
    toggleDevice(device) {
      this.device = device
    },
    setSize(size) {
      const nextSize = size || 'default'
      this.size = nextSize
      Cookies.set('size', nextSize)
    },
    toggleSideBarHide(status) {
      this.sidebar.hide = status
    },
  },
})
