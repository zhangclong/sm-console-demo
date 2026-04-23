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
  height: { type: String, default: '350px' },
  autoResize: { type: Boolean, default: true },
  chartData: { type: Object, required: true }
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

function setOptions({ expectedData, actualData } = {}) {
  chart.setOption({
    xAxis: {
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      boundaryGap: false,
      axisTick: { show: false }
    },
    grid: { left: 10, right: 10, bottom: 20, top: 30, containLabel: true },
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' }, padding: [5, 10] },
    yAxis: { axisTick: { show: false } },
    legend: { data: ['expected', 'actual'] },
    series: [{
      name: 'expected',
      itemStyle: { normal: { color: '#FF005A', lineStyle: { color: '#FF005A', width: 2 } } },
      smooth: true, type: 'line', data: expectedData,
      animationDuration: 2800, animationEasing: 'cubicInOut'
    }, {
      name: 'actual', smooth: true, type: 'line',
      itemStyle: { normal: { color: '#3888fa', lineStyle: { color: '#3888fa', width: 2 }, areaStyle: { color: '#f3f8ff' } } },
      data: actualData, animationDuration: 2800, animationEasing: 'quadraticOut'
    }]
  })
}

function initChart() {
  chart = echarts.init(instance.proxy.$el, 'macarons')
  setOptions(props.chartData)
}

watch(() => props.chartData, (val) => { setOptions(val) }, { deep: true })

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
