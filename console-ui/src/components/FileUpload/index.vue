<template>
  <div class="upload-file">
    <el-upload
      multiple
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      class="upload-file-uploader"
      ref="fileUpload"
    >
      <!-- 上传按钮 -->
      <el-button size="small" type="primary">选取文件</el-button>
      <!-- 上传提示 -->
      <template #tip><div class="el-upload__tip" v-if="showTip">
        请上传
        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
        <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
        的文件
      </div></template>
    </el-upload>

    <!-- 文件列表 -->
    <transition-group class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.url" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="`${baseUrl}${file.url}`" :underline="false" target="_blank">
          <span><el-icon><Document /></el-icon> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link :underline="false" @click="handleDelete(index)" type="danger">删除</el-link>
        </div>
      </li>
    </transition-group>
  </div>
</template>

<script setup>
import { getToken } from "@/utils/auth";
import modal from '@/plugins/modal';

const props = defineProps({
  value: [String, Object, Array],
  limit: { type: Number, default: 5 },
  fileSize: { type: Number, default: 5 },
  fileType: { type: Array, default: () => ["doc", "xls", "ppt", "txt", "pdf"] },
  isShowTip: { type: Boolean, default: true }
})

const emit = defineEmits(['input'])

const number = ref(0)
const uploadList = ref([])
const baseUrl = import.meta.env.VUE_APP_BASE_API
const uploadFileUrl = import.meta.env.VUE_APP_BASE_API + "/common/upload"
const headers = { Authorization: "Bearer " + getToken() }
const fileList = ref([])
const fileUpload = ref(null)

const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize))

watch(() => props.value, (val) => {
  if (val) {
    let temp = 1
    const list = Array.isArray(val) ? val : props.value.split(',')
    fileList.value = list.map(item => {
      if (typeof item === "string") {
        item = { name: item, url: item }
      }
      item.uid = item.uid || new Date().getTime() + temp++
      return item
    })
  } else {
    fileList.value = []
  }
}, { deep: true, immediate: true })

function handleBeforeUpload(file) {
  if (props.fileType) {
    let fileExtension = ""
    if (file.name.lastIndexOf(".") > -1) {
      fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1)
    }
    const isTypeOk = props.fileType.some((type) => {
      if (file.type.indexOf(type) > -1) return true
      if (fileExtension && fileExtension.indexOf(type) > -1) return true
      return false
    })
    if (!isTypeOk) {
      modal.msgError(`文件格式不正确, 请上传${props.fileType.join("/")}格式文件!`)
      return false
    }
  }
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }
  modal.loading("正在上传文件，请稍候...")
  number.value++
  return true
}

function handleExceed() {
  modal.msgError(`上传文件数量不能超过 ${props.limit} 个!`)
}

function handleUploadError() {
  modal.msgError("上传图片失败，请重试")
  modal.closeLoading()
}

function handleUploadSuccess(res, file) {
  if (res.code === 200) {
    uploadList.value.push({ name: res.fileName, url: res.fileName })
    uploadedSuccessfully()
  } else {
    number.value--
    modal.closeLoading()
    modal.msgError(res.msg)
    fileUpload.value.handleRemove(file)
    uploadedSuccessfully()
  }
}

function handleDelete(index) {
  fileList.value.splice(index, 1)
  emit("input", listToString(fileList.value))
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

function getFileName(name) {
  if (name.lastIndexOf("/") > -1) {
    return name.slice(name.lastIndexOf("/") + 1)
  } else {
    return ""
  }
}

function listToString(list, separator) {
  let strs = ""
  separator = separator || ","
  for (let i in list) {
    strs += list[i].url + separator
  }
  return strs != '' ? strs.substr(0, strs.length - 1) : ''
}
</script>

<style scoped lang="scss">
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
