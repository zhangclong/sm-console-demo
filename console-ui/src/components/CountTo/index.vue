<template>
  <span>{{ formattedValue }}</span>
</template>

<script setup>
const props = defineProps({
  startVal: { type: Number, default: 0 },
  endVal: { type: Number, default: 0 },
  duration: { type: Number, default: 2000 },
  decimals: { type: Number, default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
})

const currentValue = ref(props.startVal)
const animationFrameId = ref(null)
const startTime = ref(null)

const formattedValue = computed(() =>
  `${props.prefix}${currentValue.value.toFixed(props.decimals)}${props.suffix}`
)

function cancelAnimation() {
  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrameId.value)
    animationFrameId.value = null
  }
  startTime.value = null
}

function animate(timestamp) {
  if (startTime.value === null) startTime.value = timestamp
  const progress = Math.min((timestamp - startTime.value) / props.duration, 1)
  const nextValue = props.startVal + (props.endVal - props.startVal) * progress
  currentValue.value = progress === 1 ? props.endVal : nextValue
  if (progress < 1) {
    animationFrameId.value = requestAnimationFrame(animate)
  } else {
    animationFrameId.value = null
    startTime.value = null
  }
}

function startAnimation() {
  cancelAnimation()
  currentValue.value = props.startVal
  if (props.duration <= 0) {
    currentValue.value = props.endVal
    return
  }
  animationFrameId.value = requestAnimationFrame(animate)
}

watch(() => props.endVal, () => startAnimation())
watch(() => props.startVal, () => startAnimation())

onMounted(() => startAnimation())
onBeforeUnmount(() => cancelAnimation())
</script>

