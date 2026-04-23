<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { useHead } from '@/composables/useHead'
useHead()
</script>

<style lang="scss">
label {
  font-weight: 400;
}

.uhry-card-wrapper {
  .uhry-card-head {
    padding-bottom: 10px;

    .uhry-card-head-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.uhry-font-red { color: #F56C6C; }
.uhry-font-green { color: #67C23A; }
.uhry-font-yellow { color: #E6A23C; }
.uhry-font-blue { color: #409EFF; }
.uhry-font-minor { color: #909399; }
</style>
