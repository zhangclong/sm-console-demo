<template>
  <el-scrollbar ref="scrollContainer" :vertical="false" class="scroll-container" @wheel.prevent="handleScroll">
    <slot />
  </el-scrollbar>
</template>

<script setup>
const tagAndTagSpacing = 4

const emit = defineEmits(['scroll'])

const scrollContainer = ref(null)
const left = ref(0)

const scrollWrapper = computed(() => {
  const sc = scrollContainer.value
  if (!sc) return null
  return sc.wrapRef || sc.$refs?.wrap || sc.$el?.querySelector('.el-scrollbar__wrap') || null
})

function getTagElements() {
  const wrapper = scrollWrapper.value
  if (!wrapper) return []
  return Array.from(wrapper.querySelectorAll('.tags-view-item'))
}

onMounted(() => {
  if (scrollWrapper.value) {
    scrollWrapper.value.addEventListener('scroll', emitScroll, true)
  }
})

onBeforeUnmount(() => {
  if (scrollWrapper.value) {
    scrollWrapper.value.removeEventListener('scroll', emitScroll, true)
  }
})

function handleScroll(e) {
  const eventDelta = e.wheelDelta || -e.deltaY * 40
  const $scrollWrapper = scrollWrapper.value
  if (!$scrollWrapper) return
  $scrollWrapper.scrollLeft = $scrollWrapper.scrollLeft + eventDelta / 4
}

function emitScroll() {
  emit('scroll')
}

function moveToTargetByKey(currentKey) {
  const $container = scrollContainer.value?.$el
  if (!$container) return
  const $containerWidth = $container.offsetWidth
  const $scrollWrapper = scrollWrapper.value
  if (!$scrollWrapper) return
  if (!currentKey) return

  const tagList = getTagElements()
  if (tagList.length === 0) return

  const currentTag = tagList.find((item) => item.dataset.fullPath === currentKey)
  if (!currentTag) return

  let firstTag = null
  let lastTag = null
  if (tagList.length > 0) {
    firstTag = tagList[0]
    lastTag = tagList[tagList.length - 1]
  }

  if (firstTag === currentTag) {
    $scrollWrapper.scrollLeft = 0
  } else if (lastTag === currentTag) {
    $scrollWrapper.scrollLeft = $scrollWrapper.scrollWidth - $containerWidth
  } else {
    const currentIndex = tagList.findIndex(item => item === currentTag)
    if (currentIndex <= 0 || currentIndex >= tagList.length - 1) return
    const prevTag = tagList[currentIndex - 1]
    const nextTag = tagList[currentIndex + 1]
    if (!prevTag?.$el || !nextTag?.$el) return
    const afterNextTagOffsetLeft = nextTag.$el.offsetLeft + nextTag.$el.offsetWidth + tagAndTagSpacing
    const beforePrevTagOffsetLeft = prevTag.$el.offsetLeft - tagAndTagSpacing
    if (afterNextTagOffsetLeft > $scrollWrapper.scrollLeft + $containerWidth) {
      $scrollWrapper.scrollLeft = afterNextTagOffsetLeft - $containerWidth
    } else if (beforePrevTagOffsetLeft < $scrollWrapper.scrollLeft) {
      $scrollWrapper.scrollLeft = beforePrevTagOffsetLeft
    }
  }
}

defineExpose({ moveToTargetByKey })
</script>

<style lang="scss" scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  width: 100%;
  :deep(.el-scrollbar__bar) {
    bottom: 0px;
  }
  :deep(.el-scrollbar__wrap) {
    height: 49px;
  }
}
</style>
