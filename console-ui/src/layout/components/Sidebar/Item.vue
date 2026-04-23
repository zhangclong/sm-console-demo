<template>
  <span class="menu-item-content">
    <svg-icon v-if="icon" :icon-class="icon" />
    <span v-if="title" class="menu-item-label" :title="title.length > 5 ? title : ''">{{ title }}</span>
  </span>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, default: '' }
})
</script>
