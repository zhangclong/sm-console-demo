import request from '@/utils/request'

// 查询配置模版列表
export function listTempgroup(query) {
  return request({
    url: '/console/tempgroup/list',
    method: 'get',
    params: query
  })
}

// 查询配置模版组列表 by versionId
export function listGroupByVersionId(versionId) {
  return request({
    url: '/console/tempgroup/listByVersionId/' + versionId,
    method: 'get'
  })
}

export function listAllGroups() {
  return request({
    url: '/console/tempgroup/listAll',
    method: 'get'
  })
}

// 查询配置模版详细
export function getTempgroup(groupId) {
  return request({
    url: '/console/tempgroup/' + groupId,
    method: 'get'
  })
}

// 新增配置模版
export function addTempgroup(data) {
  return request({
    url: '/console/tempgroup',
    method: 'post',
    data: data
  })
}

// 修改配置模版
export function updateTempgroup(data) {
  return request({
    url: '/console/tempgroup/edit',
    method: 'post',
    data: data
  })
}

// 删除配置模版
export function delTempgroup(groupId) {
  return request({
    url: '/console/tempgroup/delete/' + groupId,
    method: 'get'
  })
}
