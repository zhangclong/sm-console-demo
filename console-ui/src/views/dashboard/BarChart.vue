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

const animationDuration = 6000
const instance = getCurrentInstance()
let chart = null
let resizeHandler = null
let sidebarElm = null

function sidebarResizeHandler(e) {
  if (e.propertyName === 'width') {
    resizeHandler && resizeHandler()
  }
}

function initListener() {
  resizeHandler = debounce(() => {
    chart && chart.resize()
  }, 100)
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
    tooltip: {
      trigger: 'axis',
      axisPointer: { // 坐标轴指示器，坐标轴触发有效
        type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
      }
    },
    grid: {
      top: 10,
      left: '2%',
      right: '2%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [{
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      axisTick: { alignWithLabel: true }
    }],
    yAxis: [{
      type: 'value',
      axisTick: { show: false }
    }],
    series: [{
      name: 'pageA',
      type: 'bar',
      stack: 'vistors',
      barWidth: '60%',
      data: [79, 52, 200, 334, 390, 330, 220],
      animationDuration
    }, {
      name: 'pageB',
      type: 'bar',
      stack: 'vistors',
      barWidth: '60%',
      data: [80, 52, 200, 334, 390, 330, 220],
      animationDuration
    }, {
      name: 'pageC',
      type: 'bar',
      stack: 'vistors',
      barWidth: '60%',
      data: [30, 52, 200, 334, 390, 330, 220],
      animationDuration
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

onDeactivated(() => {
  destroyListener()
})
</script>
