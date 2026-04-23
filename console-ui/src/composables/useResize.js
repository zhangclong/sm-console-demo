import { useAppStore } from '@/store'

const { body } = document
const WIDTH = 992

export function useResize() {
  const route = useRoute()
  const appStore = useAppStore()

  function isMobile() {
    const rect = body.getBoundingClientRect()
    return rect.width - 1 < WIDTH
  }

  function resizeHandler() {
    if (!document.hidden) {
      const mobile = isMobile()
      appStore.toggleDevice(mobile ? 'mobile' : 'desktop')
      if (mobile) {
        appStore.closeSideBar({ withoutAnimation: true })
      }
    }
  }

  watch(route, () => {
    if (appStore.device === 'mobile' && appStore.sidebar.opened) {
      appStore.closeSideBar({ withoutAnimation: false })
    }
  })

  onMounted(() => {
    window.addEventListener('resize', resizeHandler)
    if (isMobile()) {
      appStore.toggleDevice('mobile')
      appStore.closeSideBar({ withoutAnimation: true })
    }
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeHandler)
  })
}
