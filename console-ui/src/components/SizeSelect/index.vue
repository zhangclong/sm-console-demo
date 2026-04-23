<template>
  <el-dropdown trigger="click" @command="handleSetSize">
    <div>
      <svg-icon class-name="size-icon" icon-class="size" />
    </div>
    <template #dropdown><el-dropdown-menu>
      <el-dropdown-item v-for="item of sizeOptions" :key="item.value" :disabled="size===item.value" :command="item.value">
        {{ item.label }}
      </el-dropdown-item>
    </el-dropdown-menu></template>
  </el-dropdown>
</template>

<script setup>
import { useAppStore } from '@/store/modules/app'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const sizeOptions = [
  { label: 'Default', value: 'default' },
  { label: 'Small', value: 'small' },
  { label: 'Large', value: 'large' }
]

const size = computed(() => useAppStore().size)

function handleSetSize(size) {
  useAppStore().setSize(size)
  refreshView()
  ElMessage({ message: 'Switch Size Success', type: 'success' })
}

function refreshView() {
  // In order to make the cached page re-rendered
  useTagsViewStore().delAllCachedViews()
  const { fullPath } = route
  nextTick(() => {
    router.replace({ path: '/redirect' + fullPath })
  })
}
</script>
