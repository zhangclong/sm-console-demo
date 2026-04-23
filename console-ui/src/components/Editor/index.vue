<template>
  <div>
    <el-upload
      :action="uploadUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      name="file"
      :show-file-list="false"
      :headers="headers"
      style="display: none"
      ref="upload"
      v-if="this.type == 'url'"
    >
    </el-upload>
    <div class="editor" ref="editor" :style="styles"></div>
  </div>
</template>

<script setup>
import Quill from "quill";
import "quill/dist/quill.core.css";
import "quill/dist/quill.snow.css";
import "quill/dist/quill.bubble.css";
import { getToken } from "@/utils/auth";
import modal from '@/plugins/modal';

const props = defineProps({
  value: { type: String, default: "" },
  height: { type: Number, default: null },
  minHeight: { type: Number, default: null },
  readOnly: { type: Boolean, default: false },
  fileSize: { type: Number, default: 5 },
  type: { type: String, default: "url" }
})

const emit = defineEmits(['input', 'on-change', 'on-text-change', 'on-selection-change', 'on-editor-change'])

const uploadUrl = import.meta.env.VUE_APP_BASE_API + "/common/upload"
const headers = { Authorization: "Bearer " + getToken() }
const upload = ref(null)
const editor = ref(null)
const QuillInstance = ref(null)
const currentValue = ref("")
const uploadType = ref("")

const options = {
  theme: "snow",
  bounds: document.body,
  debug: "warn",
  modules: {
    toolbar: [
      ["bold", "italic", "underline", "strike"],
      ["blockquote", "code-block"],
      [{ list: "ordered" }, { list: "bullet" }],
      [{ indent: "-1" }, { indent: "+1" }],
      [{ size: ["small", false, "large", "huge"] }],
      [{ header: [1, 2, 3, 4, 5, 6, false] }],
      [{ color: [] }, { background: [] }],
      [{ align: [] }],
      ["clean"],
      ["link", "image", "video"]
    ],
  },
  placeholder: "请输入内容",
  readOnly: props.readOnly,
}

const styles = computed(() => {
  let style = {}
  if (props.minHeight) style.minHeight = `${props.minHeight}px`
  if (props.height) style.height = `${props.height}px`
  return style
})

watch(() => props.value, (val) => {
  if (val !== currentValue.value) {
    currentValue.value = val === null ? "" : val
    if (QuillInstance.value) {
      QuillInstance.value.pasteHTML(currentValue.value)
    }
  }
}, { immediate: true })

function init() {
  QuillInstance.value = new Quill(editor.value, options)
  if (props.type == 'url') {
    let toolbar = QuillInstance.value.getModule("toolbar")
    toolbar.addHandler("image", (value) => {
      uploadType.value = "image"
      if (value) {
        upload.value.$el.querySelector('input').click()
      } else {
        QuillInstance.value.format("image", false)
      }
    })
  }
  QuillInstance.value.pasteHTML(currentValue.value)
  QuillInstance.value.on("text-change", (delta, oldDelta, source) => {
    const html = editor.value.children[0].innerHTML
    const text = QuillInstance.value.getText()
    const quill = QuillInstance.value
    currentValue.value = html
    emit("input", html)
    emit("on-change", { html, text, quill })
  })
  QuillInstance.value.on("text-change", (delta, oldDelta, source) => {
    emit("on-text-change", delta, oldDelta, source)
  })
  QuillInstance.value.on("selection-change", (range, oldRange, source) => {
    emit("on-selection-change", range, oldRange, source)
  })
  QuillInstance.value.on("editor-change", (eventName, ...args) => {
    emit("on-editor-change", eventName, ...args)
  })
}

function handleBeforeUpload(file) {
  if (props.fileSize) {
    const isLt = file.size / 1024 / 1024 < props.fileSize
    if (!isLt) {
      modal.msgError(`上传文件大小不能超过 ${props.fileSize} MB!`)
      return false
    }
  }
  return true
}

function handleUploadSuccess(res) {
  let quill = QuillInstance.value
  if (res.code == 200) {
    let length = quill.getSelection().index
    quill.insertEmbed(length, "image", import.meta.env.VUE_APP_BASE_API + res.fileName)
    quill.setSelection(length + 1)
  } else {
    modal.msgError("图片插入失败")
  }
}

function handleUploadError() {
  modal.msgError("图片插入失败")
}

onMounted(() => init())
onBeforeUnmount(() => { QuillInstance.value = null })
</script>

<style>
.editor, .ql-toolbar {
  white-space: pre-wrap !important;
  line-height: normal !important;
}
.quill-img {
  display: none;
}
.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "请输入链接地址:";
}
.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "保存";
  padding-right: 0px;
}

.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "请输入视频地址:";
}

.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}
.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}

.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "文本";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "标题1";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "标题2";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "标题3";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "标题4";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "标题5";
}
.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "标题6";
}

.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "标准字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "衬线字体";
}
.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "等宽字体";
}
</style>
