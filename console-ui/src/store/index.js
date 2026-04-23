import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// Re-export stores for convenience
export { useAppStore } from './modules/app'
export { useDictStore } from './modules/dict'
export { useUserStore } from './modules/user'
export { useTagsViewStore } from './modules/tagsView'
export { usePermissionStore } from './modules/permission'
export { useSettingsStore } from './modules/settings'
