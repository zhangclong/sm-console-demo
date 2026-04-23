<template>
<span>
  <el-button link size="small" v-if="copyBtn" @click="handleCopyText">复制</el-button>
  <el-icon v-if="!copyBtn" style="margin-left: 2px; cursor: pointer" @click="handleCopyText"><DocumentCopy /></el-icon>
</span>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  value: { type: String },
  copyBtn: { type: Boolean },
  flag: { type: Number }
})

function handleCopyText() {
  const cInput = document.createElement('input')
  if (props.flag === 0) {
    let arr = []
    props.value.forEach((item) => {
      arr.push({ key: item.Lkey, value: item.Lval })
    })
    cInput.value = JSON.stringify(arr)
    document.body.appendChild(cInput)
    cInput.select() // 选取文本域内容;
    document.execCommand('Copy')
    ElMessage.success("复制成功")
    cInput.remove()
  } else {
    cInput.value = props.value
    document.body.appendChild(cInput)
    cInput.select() // 选取文本域内容;
    document.execCommand('Copy')
    ElMessage.success("复制成功")
    cInput.remove()
  }
}
</script>

<style></style>
