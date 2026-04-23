<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script setup>
import { getCurrentInstance, onActivated, onDeactivated } from 'vue'
import * as echarts from 'echarts'
import 'echarts/theme/macarons'
import { debounce } from '@/utils'

const props = defineProps({
  className: { type: String, default: 'chart' },
  width: { type: String, default: '100%' },
  height: { type: String, default: '300px' }
})

const instance = getCurrentInstance()
let chart = null
let resizeHandler = null
let sidebarElm = null

function sidebarResizeHandler(e) {
  if (e.propertyName === 'width') { resizeHandler && resizeHandler() }
}
function initListener() {
  resizeHandler = debounce(() => { chart && chart.resize() }, 100)
  window.addEventListener('resize', resizeHandler)
  sidebarElm = document.getElementsByClassName('sidebar-container')[0]
  sidebarElm && sidebarElm.addEventListener('transitionend', sidebarResizeHandler)
}
function destroyListener() {
  window.removeEventListener('resize', resizeHandler)
  resizeHandler = null
  sidebarElm && sidebarElm.removeEventListener('transitionend', sidebarResizeHandler)
}

function initChart() {
  chart = echarts.init(instance.proxy.$el, 'macarons')
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c} ({d}%)' },
    legend: {
      left: 'center', bottom: '10',
      data: ['Industries', 'Technology', 'Forex', 'Gold', 'Forecasts']
    },
    series: [{
      name: 'WEEKLY WRITE ARTICLES',
      type: 'pie', roseType: 'radius',
      radius: [15, 95], center: ['50%', '38%'],
      data: [
        { value: 320, name: 'Industries' },
        { value: 240, name: 'Technology' },
        { value: 149, name: 'Forex' },
        { value: 100, name: 'Gold' },
        { value: 59, name: 'Forecasts' }
      ],
      animationEasing: 'cubicInOut', animationDuration: 2600
    }]
  })
}

onMounted(() => {
  nextTick(() => { initChart() })
  initListener()
})
onBeforeUnmount(() => {
  destroyListener()
  if (!chart) return
  chart.dispose()
  chart = null
})
onActivated(() => {
  if (!resizeHandler) { initListener() }
  chart && chart.resize()
})
onDeactivated(() => { destroyListener() })
</script>
