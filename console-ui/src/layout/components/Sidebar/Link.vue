<template>
  <component :is="type" v-bind="linkProps(to)">
    <slot />
  </component>
</template>

<script setup>
import { isExternal as checkExternal } from '@/utils/validate'

const props = defineProps({
  to: { type: [String, Object], required: true }
})

const isExternalLink = computed(() => checkExternal(props.to))
const type = computed(() => isExternalLink.value ? 'a' : 'router-link')

function linkProps(to) {
  if (isExternalLink.value) {
    return { href: to, target: '_blank', rel: 'noopener' }
  }
  return { to }
}
</script>
