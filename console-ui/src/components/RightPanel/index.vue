<template>
  <div ref="rightPanel" class="rightPanel-container">
    <div class="rightPanel-background" />
    <div class="rightPanel">
      <div class="rightPanel-items">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useSettingsStore } from '@/store/modules/settings'

const props = defineProps({
  clickNotClose: { default: false, type: Boolean }
})

const rightPanel = ref(null)

const show = computed({
  get: () => useSettingsStore().showSettings,
  set: (val) => useSettingsStore().changeSetting({ key: 'showSettings', value: val })
})

watch(show, (value) => {
  if (value && !props.clickNotClose) {
    addEventClick()
  }
})

onMounted(() => {
  insertToBody()
  addEventClick()
})

onBeforeUnmount(() => {
  const elx = rightPanel.value
  elx && elx.remove()
})

function addEventClick() {
  window.addEventListener('click', closeSidebar)
}

function closeSidebar(evt) {
  const parent = evt.target.closest('.el-drawer__body')
  if (!parent) {
    show.value = false
    window.removeEventListener('click', closeSidebar)
  }
}

function insertToBody() {
  const elx = rightPanel.value
  const body = document.querySelector('body')
  body.insertBefore(elx, body.firstChild)
}
</script>

<style lang="scss" scoped>
.rightPanel-background {
  position: fixed;
  top: 0;
  left: 0;
  opacity: 0;
  transition: opacity .3s cubic-bezier(.7, .3, .1, 1);
  background: rgba(0, 0, 0, .2);
  z-index: -1;
}

.rightPanel {
  width: 100%;
  max-width: 260px;
  height: 100vh;
  position: fixed;
  top: 0;
  right: 0;
  box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
  transition: all .25s cubic-bezier(.7, .3, .1, 1);
  transform: translate(100%);
  background: #fff;
  z-index: 40000;
}

.handle-button {
  width: 48px;
  height: 48px;
  position: absolute;
  left: -48px;
  text-align: center;
  font-size: 24px;
  border-radius: 6px 0 0 6px !important;
  z-index: 0;
  pointer-events: auto;
  cursor: pointer;
  color: #fff;
  line-height: 48px;
  i {
    font-size: 24px;
    line-height: 48px;
  }
}
</style>
