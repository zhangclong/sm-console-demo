import request from '@/utils/request'

// 查询配置模版列表
export function listTemplate(query) {
  return request({
    url: '/console/template/list',
    method: 'get',
    params: query
  })
}

// 查询配置模版详细
export function getTemplate(templateId) {
  return request({
    url: '/console/template/' + templateId,
    method: 'get'
  })
}

// 新增配置模版
export function addTemplate(data) {
  return request({
    url: '/console/template',
    method: 'post',
    data: data
  })
}

// 修改配置模版
export function updateTemplate(data) {
  return request({
    url: '/console/template/edit',
    method: 'post',
    data: data
  })
}

// 删除配置模版
export function delTemplate(templateId) {
  return request({
    url: '/console/template/delete/' + templateId,
    method: 'get'
  })
}
