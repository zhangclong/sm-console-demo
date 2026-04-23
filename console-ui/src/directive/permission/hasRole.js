import { useUserStore } from '@/store/modules/user'

export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const super_admin = 'admin'
    const roles = userStore.roles

    if (value && value instanceof Array && value.length > 0) {
      const roleFlag = value
      const hasRole = roles.some((role) => super_admin === role || roleFlag.includes(role))
      if (!hasRole) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置角色权限标签值`)
    }
  },
}
