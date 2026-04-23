<template>
  <div>
    <svg-icon :icon-class="isFullscreen?'exit-fullscreen':'fullscreen'" @click="click" />
  </div>
</template>

<script setup>
import screenfull from 'screenfull'
import { ElMessage } from 'element-plus'

const isFullscreen = ref(false)

function click() {
  if (!screenfull.isEnabled) {
    ElMessage({ message: '你的浏览器不支持全屏', type: 'warning' })
    return false
  }
  screenfull.toggle()
}

function change() {
  isFullscreen.value = screenfull.isFullscreen
}

onMounted(() => {
  if (screenfull.isEnabled) {
    screenfull.on('change', change)
  }
})

onBeforeUnmount(() => {
  if (screenfull.isEnabled) {
    screenfull.off('change', change)
  }
})
</script>

<style scoped>
.screenfull-svg {
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;;
  width: 20px;
  height: 20px;
  vertical-align: 10px;
}
</style>
