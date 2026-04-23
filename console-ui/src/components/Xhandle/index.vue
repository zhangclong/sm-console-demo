<template>
  <div class="x-handle" @mousedown="mouseDown"></div>
</template>
 
<script setup>
const emit = defineEmits(['widthChange'])
const lastX = ref("")

onMounted(() => {
  document.addEventListener("mouseup", mouseUp)
})

onUnmounted(() => {
  document.removeEventListener("mouseup", mouseUp)
})

function mouseDown(event) {
  document.addEventListener("mousemove", mouseMove)
  lastX.value = event.screenX
}

function mouseMove(event) {
  emit("widthChange", lastX.value - event.screenX)
  lastX.value = event.screenX
}

function mouseUp() {
  lastX.value = ""
  document.removeEventListener("mousemove", mouseMove)
}
</script>
<style lang="scss">
.x-handle {
  margin-left: 10px;
  width: 1.5px;
  z-index: 10;
  background: #d7d7d7;
  height: 100vh;
}
</style>