import request from '@/utils/request'
import util from "@/utils/aesutils";

// 查询参数列表
export function listConfig(query) {
  return request({
    url: '/system/config/list',
    method: 'get',
    params: query
  })
}

// 查询参数详细
export function getConfig(configId) {
  return request({
    url: '/system/config/' + configId,
    method: 'get'
  })
}

// 根据参数键名查询参数值
export function getConfigKey(configKey) {
  return request({
    url: '/system/config/configKey/' + configKey,
    method: 'get'
  })
}

// 根据参数键名查询参数值
export function getAppConfigKey(key) {
  return request({
    url: '/system/config/appConfigKey/' + key,
    method: 'get'
  })
}

export function getAppConfigKeys() {
  return request({
    url: '/system/config/appConfigKeys',
    method: 'get'
  })
}

// 新增参数配置
export function addConfig(data) {
  return request({
    url: '/system/config',
    method: 'post',
    data: data
  })
}

// 修改参数配置
export function updateConfig(data) {
  return request({
    url: '/system/config/edit',
    method: 'post',
    data: data
  })
}

// 删除参数配置
export function delConfig(configId) {
  return request({
    url: '/system/config/delete/' + configId,
    method: 'get'
  })
}


// 开启开发者模式
export function switchOnDev(passwd) {

  const data = {
    username: 'admin',
    password: passwd
  }

  return request({
    url: '/system/config/switchondev',
    method: 'post',
    data: data
  })
}

// 关闭开发者模式
export function switchOffDev(passwd) {
  const data = {
    username: 'admin',
    password: passwd
  }

  return request({
    url: '/system/config/switchoffdev',
    method: 'post',
    data: data
  })
}


// 清理参数缓存
export function clearCache() {
  return request({
    url: '/system/config/clearCache',
    method: 'get'
  })
}

// 兼容旧页面调用名称
export function refreshCache() {
  return clearCache()
}

// 导出参数
export function exportConfig(query) {
  return request({
    url: '/system/config/export',
    method: 'get',
    params: query
  })
}
