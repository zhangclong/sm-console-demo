<template>
  <div :style="'height:' + height" v-loading="loading" element-loading-text="正在加载页面，请稍候！">
    <iframe
      :id="iframeId"
      style="width: 100%; height: 100%"
      :src="src"
      frameborder="no"
    ></iframe>
  </div>
</template>

<script setup>
const props = defineProps({
  src: { type: String, default: "/" },
  iframeId: { type: String }
})

const loading = ref(false)
const height = ref(document.documentElement.clientHeight - 94.5 + "px;")

onMounted(() => {
  const iframeSelector = '#' + CSS.escape(props.iframeId)
  const iframe = document.querySelector(iframeSelector)
  if (iframe.attachEvent) {
    loading.value = true
    iframe.attachEvent("onload", () => { loading.value = false })
  } else {
    loading.value = true
    iframe.onload = () => { loading.value = false }
  }
})
</script>
