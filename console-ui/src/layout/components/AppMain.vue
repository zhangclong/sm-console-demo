<template>
  <section class="app-main">
    <router-view v-slot="{ Component, route }">
      <component :is="Component" v-if="Component && !route.meta?.link" :key="routeKey(route)" />
    </router-view>
    <IframeToggle />
  </section>
</template>

<script setup>
import IframeToggle from "./IframeToggle/index.vue"

function routeKey(route) {
  return route.path
}
</script>

<style lang="scss" scoped>
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 17px;
  }
}
</style>
