<template>
  <transition-group name="fade-transform">
    <inner-link
      v-for="(item, index) in iframeViews"
      :key="item.path"
      :iframeId="'iframe' + index"
      v-show="$route.path === item.path"
      :src="item.meta.link"
    ></inner-link>
  </transition-group>
</template>

<script setup>
import InnerLink from "../InnerLink/index.vue"
import { useTagsViewStore } from '@/store/modules/tagsView'

const iframeViews = computed(() => useTagsViewStore().iframeViews)
</script>
