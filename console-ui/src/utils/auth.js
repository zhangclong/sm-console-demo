import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
// setting()
// function setting() {
//   let setting = {
//     searchOrPerson: true
//   }
//   sessionStorage.setItem('layout-setting', JSON.stringify(setting))
// }

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
