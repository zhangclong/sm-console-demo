<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      :on-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{hide: this.fileList.length >= this.limit}"
    >
      <el-icon><Plus /></el-icon>
      <!-- 上传提示 -->
      <template #tip><div class="el-upload__tip" v-if="showTip">
        请上传
        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
        <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
        的文件
      </div></template>
    </el-upload>

    <el-dialog
      v-model="dialogVisible"
      title="预览"
      width="800"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { getToken } from "@/utils/auth";
import modal from '@/plugins/modal';

const props = defineProps({
  value: [String, Object, Array],
  limit: { type: Number, default: 5 },
  fileSize: { type: Number, default: 5 },
  fileType: { type: Array, default: () => ["png", "jpg", "jpeg"] },
  isShowTip: { type: Boolean, default: true }
})

const emit = defineEmits(['input'])

const number = ref(0)
const uploadList = ref([])
const dialogImageUrl = ref("")
const dialogVisible = ref(false)
const hideUpload = ref(false)
const baseUrl = import.meta.env.VUE_APP_BASE_API
const uploadImgUrl = import.meta.env.VUE_APP_BASE_API + "/common/upload"
const headers = { Authorization: "Bearer " + getToken() }
const fileList = ref([])
const imageUpload = ref(null)

const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize))

watch(() => props.value, (val) => {
  if (val) {
    const list = Array.isArray(val) ? val : props.value.split(',')
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        if (item.indexOf(baseUrl) === -1) {
          item = { name: baseUrl + item, url: baseUrl + item }
        } else {
          item = { name: item, url: item }
        }
      }
      return item
    })
  } else {
    fileList.value = []
  }
}, { deep: true, immediate: true })

function handleBeforeUpload(file) {
  let isImg = false
  if (props.fileType.length) {
    let fileExtension = ""
    if (file.name.lastIndexOf(".") > -1) {
      fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1)
    }
    isImg = props.fileType.some(type => {
      if (file.type.indexOf(type) > -1) return true
      if (fileExtension && fileExtension.indexOf(type) > -1) return true
      return false
    })
  } else {
    isImg = file.type.indexOf("image") > -1
  }
  if (!isImg) {
    modal.msgError(`文件格式不正确, 请上传${props.fileType.join("/")}图片格式文件!`)
    return false
  }
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      modal.msgError(`上传头像图片大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }
  modal.loading("正在上传图片，请稍候...")
  number.value++
}

function handleExceed() {
  modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

function handleUploadSuccess(res, file) {
  if (res.code === 200) {
    uploadList.value.push({ name: res.fileName, url: res.fileName })
    uploadedSuccessfully()
  } else {
    number.value--
    modal.closeLoading()
    modal.msgError(res.msg)
    imageUpload.value.handleRemove(file)
    uploadedSuccessfully()
  }
}

function handleDelete(file) {
  const findex = fileList.value.map(f => f.name).indexOf(file.name)
  if (findex > -1) {
    fileList.value.splice(findex, 1)
    emit("input", listToString(fileList.value))
  }
}

function handleUploadError() {
  modal.msgError("上传图片失败，请重试")
  modal.closeLoading()
}

function uploadedSuccessfully() {
  if (number.value > 0 && uploadList.value.length === number.value) {
    fileList.value = fileList.value.concat(uploadList.value)
    uploadList.value = []
    number.value = 0
    emit("input", listToString(fileList.value))
    modal.closeLoading()
  }
}

function handlePictureCardPreview(file) {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    if (list[i].url) {
      strs += list[i].url.replace(baseUrl, "") + separator
    }
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}
</script>
<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
:deep(.hide .el-upload--picture-card) {
    display: none;
}
// 去掉动画效果
:deep(.el-list-enter-active),
:deep(.el-list-leave-active) {
    transition: all 0s;
}

:deep(.el-list-enter), .el-list-leave-active {
    opacity: 0;
    transform: translateY(0);
}
</style>

