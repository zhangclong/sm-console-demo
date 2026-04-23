import request from '@/utils/request'

// 查询执行命令历史列表
export function listCommandhistory(query) {
  return request({
    url: '/console/commandhistory/list',
    method: 'get',
    params: query
  })
}

// 查询执行命令历史详细
export function getCommandhistory(historyId) {
  return request({
    url: '/console/commandhistory/' + historyId,
    method: 'get'
  })
}

// 删除执行命令历史
export function delCommandhistory(historyId) {
  return request({
    url: '/console/commandhistory/delete/' + historyId,
    method: 'get'
  })
}
