import request from '@/utils/request'

// 查询版本信息列表
export function listRdsversion(query) {
  return request({
    url: '/console/rdsversion/list',
    method: 'get',
    params: query
  })
}

// 查询版本信息详细
export function getRdsversion(versionId) {
  return request({
    url: '/console/rdsversion/' + versionId,
    method: 'get'
  })
}

// 通过状态返回所有版本信息， status ： "1" 启用状态，"0" 停用状态， null 查询所有版本
export function listVersionByStatus(status) {
  return request({
    url: '/console/rdsversion/listAll',
    method: 'get',
    params: {'status': status}
  })
}

// 新增版本信息
export function addRdsversion(data) {
  return request({
    url: '/console/rdsversion',
    method: 'post',
    data: data
  })
}

// 修改版本信息
export function updateRdsversion(data) {
  return request({
    url: '/console/rdsversion/edit',
    method: 'post',
    data: data
  })
}

// 删除版本信息
export function delRdsversion(versionId) {
  return request({
    url: '/console/rdsversion/delete/' + versionId,
    method: 'get'
  })
}

// 状态修改
export function changeVersionStatus(versionId, status) {
  const data = {
    versionId,
    status
  }
  return request({
    url: '/console/rdsversion/changeStatus',
    method: 'post',
    data: data
  })
}

// 设置为默认
export function changeVersionDefault(versionId) {
  return request({
    url: '/console/rdsversion/changeDefault/' + versionId,
    method: 'post'
  })
}

/**
 * 根据传入包类型获取version信息
 * @param {exporter worker center} pkgType 
 * @returns 
 */
export function getPkgTypeVersionList(pkgType){
  return request({
    url:'/console/rdsversion/getPkgTypeVersionList',
    method: 'get',
    params: {'pkgType': pkgType}
  })
}