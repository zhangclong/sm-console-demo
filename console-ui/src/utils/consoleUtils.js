/**
 * 对比两个对象，有哪些属性不同，返回值不相同的属性名称数组
 * @param {Object} obj
 * @param {Object} otherObj
 * @param {Array} skipProps
 */
export function getDiffProps(obj, otherObj, skipProps=['__ob__']) {
  let changed = [];
  for(var propName in obj) {
    if(skipProps.includes(propName))  continue;
    if(obj[propName] != otherObj[propName]) changed.push(propName);
  }
  return changed;
}

export function getDeployModeTitle(deployMode) {
  switch(deployMode) {
    case 'center': return '中心服务';
    case 'single': return '单节点服务';
    case 'sentinel': return '哨兵组';
    case 'sentinel_worker': return '哨兵主从服务';
    case 'cluster': return '集群服务';
    case 'scalable': return '可伸缩集群服务';
    case 'adjustable': return '代理集群服务';
    default : return '未知模式服务！！！';
  }
}

export function getNodeTypeTitle(nodeType) {
    switch(nodeType) {
      case 'center': return '中心节点';
      case 'worker': return '服务节点';
      case 'sentinel': return '哨兵节点';
      case 'proxy': return '代理节点';
      case 'exporter': return '监控节点';
      default : return '';
    }
}
