import request from '@/utils/request'

// 查询安装包信息列表
export function listRdsversionpkg(query) {
  return request({
    url: '/console/rdsversionpkg/list',
    method: 'get',
    params: query
  })
}

// 查询安装包信息详细
export function getRdsversionpkg(packageId) {
  return request({
    url: '/console/rdsversionpkg/' + packageId,
    method: 'get'
  })
}


// 新增安装包信息
export function addRdsversionpkg(data) {
  return request({
    url: '/console/rdsversionpkg',
    method: 'post',
    data: data
  })
}

// 修改安装包信息
export function updateRdsversionpkg(data) {
  return request({
    url: '/console/rdsversionpkg/edit',
    method: 'post',
    data: data
  })
}

// 选择服务器安装包上传
export function importPackage(data) {
  return request({
    url: '/fileBrowser/importPackage',
    method: 'post',
    data: data
  })
}

// 删除安装包信息
export function delRdsversionpkg(packageId) {
  return request({
    url: '/console/rdsversionpkg/delete/' + packageId,
    method: 'get'
  })
}
