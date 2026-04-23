import { watchEffect } from 'vue'
import { useSettingsStore } from '@/store/modules/settings'

export function useHead() {
  const settingsStore = useSettingsStore()
  watchEffect(() => {
    const title = settingsStore.dynamicTitle ? settingsStore.title : null
    document.title = title
      ? `${title} - ${import.meta.env.VUE_APP_TITLE}`
      : (import.meta.env.VUE_APP_TITLE || '管理控制台')
  })
}
