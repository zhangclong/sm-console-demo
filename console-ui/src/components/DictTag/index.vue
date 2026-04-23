<template>
  <span>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span
          v-if="item.raw.listClass == 'default' || item.raw.listClass == ''"
          :key="item.value"
          :index="index"
          :class="item.raw.cssClass"
          >{{ item.label }}</span
        >
        <el-tag
          v-else
          :disable-transitions="true"
          :key="item.value"
          :index="index"
          :type="item.raw.listClass"
          :class="item.raw.cssClass"
        >
          {{ item.label }}
        </el-tag>
      </template>
    </template>
  </span>
</template>

<script setup>
const props = defineProps({
  options: { type: Array, default: null },
  value: [Number, String, Array]
})

const values = computed(() => {
  if (props.value !== null && typeof props.value !== 'undefined') {
    return Array.isArray(props.value) ? props.value : [String(props.value)]
  }
  return []
})
</script>
<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
