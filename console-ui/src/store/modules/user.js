import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import defaultAvatar from '@/assets/images/profile.jpg'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: [],
    permissions: [],
  }),
  actions: {
    Login(userInfo) {
      const { username, password, code, uuid } = userInfo
      return login(username.trim(), password, code, uuid).then((res) => {
        setToken(res.token)
        this.token = res.token
      })
    },
    GetInfo() {
      return getInfo().then((res) => {
        const user = res.user
        this.avatar =
          user.avatar == null || user.avatar === ''
            ? defaultAvatar
            : import.meta.env.VUE_APP_BASE_API + user.avatar
        if (res.roles && res.roles.length > 0) {
          this.roles = res.roles
          this.permissions = res.permissions
        } else {
          this.roles = ['ROLE_DEFAULT']
        }
        this.name = user.userName
        return res
      })
    },
    LogOut() {
      return logout(this.token).then(() => {
        this.token = ''
        this.roles = []
        this.permissions = []
        removeToken()
      })
    },
    FedLogOut() {
      this.token = ''
      removeToken()
    },
  },
})
