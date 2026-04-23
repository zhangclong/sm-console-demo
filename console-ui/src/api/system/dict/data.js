import request from '@/utils/request'

// 根据字典类型查询字典数据信息（枚举驱动，不再查数据库）
export function getDicts(dictType) {
  return request({
    url: '/system/dict/data/type/' + dictType,
    method: 'get'
  })
}