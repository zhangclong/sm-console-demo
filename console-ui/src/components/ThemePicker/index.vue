<template>
  <el-color-picker
    v-model="theme"
    :predefine="['#409EFF', '#1890ff', '#304156','#212121','#11a983', '#13c2c2', '#6959CD', '#f5222d']"
    class="theme-picker"
    popper-class="theme-picker-dropdown"
    @change="themeChange"
  />
</template>

<script setup>
import { useSettingsStore } from '@/store/modules/settings'

const settingsStore = useSettingsStore()

const theme = ref('#409EFF')
const defaultTheme = computed(() => settingsStore.theme)

watch(defaultTheme, (val) => { theme.value = val }, { immediate: true })

function themeChange(val) {
  settingsStore.changeSetting({ key: 'theme', value: val })
  document.documentElement.style.setProperty('--el-color-primary', val)
}
</script>

<style>
.theme-picker .el-color-picker__trigger {
  vertical-align: middle;
}

.theme-picker-dropdown .el-color-dropdown__link-btn {
  display: none;
}
</style>
