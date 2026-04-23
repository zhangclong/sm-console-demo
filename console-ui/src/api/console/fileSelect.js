import request from '@/utils/request'

// 查询监控exporter节点信息详细
export function fileBrowser(query) {
  return request({
    url: '/fileBrowser/ls',
    method: 'get',
    params: query
  })
}