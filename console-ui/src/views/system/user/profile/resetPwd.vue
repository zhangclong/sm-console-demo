<template>
  <div class="no-padding">
    <el-form ref="form" :model="user" :rules="rules" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="user.oldPassword" placeholder="请输入旧密码" type="password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="user.newPassword" placeholder="请输入新密码" type="password" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="user.confirmPassword" placeholder="请确认密码" type="password" />
      </el-form-item>
      <el-form-item v-if="!passwordDialog">
        <el-button type="primary" size="small" @click="submit">保存</el-button>
        <el-button type="danger" size="small" @click="close">关闭</el-button>
      </el-form-item>
    </el-form>
    <div class="dialog-footer" v-if="passwordDialog" style="text-align: right;">
      <el-button type="primary" size="small" @click="submit">保 存</el-button>
    </div>
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue'
import { updateUserPwd } from "@/api/system/user";
import { checkPassword } from "@/utils/validate";
import util from "@/utils/aesutils";
import { useUserStore } from '@/store/modules/user'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { ElMessage } from 'element-plus'

const props = defineProps({
  passwordDialog: { type: Boolean }
})
const emit = defineEmits(['close'])

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const form = ref(null)
const user = reactive({
  oldPassword: undefined,
  newPassword: undefined,
  confirmPassword: undefined
})

const equalToPassword = (rule, value, callback) => {
  if (user.newPassword !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const validatePwd = (rule, value, callback) => {
  checkPassword(proxy.$appConfig.pwdStrength, value, callback)
}

// 表单校验
const rules = {
  oldPassword: [
    { required: true, message: "旧密码不能为空", trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: "新密码不能为空", trigger: "blur" },
    { required: true, validator: validatePwd, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: "确认密码不能为空", trigger: "blur" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ]
}

function submit() {
  form.value.validate(valid => {
    if (valid) {
      let oldPassword = util.encrypt(user.oldPassword)
      let newPassword = util.encrypt(user.newPassword)
      updateUserPwd(oldPassword, newPassword).then(response => {
        ElMessage.success("修改成功")
        if (props.passwordDialog) {
          emit('close')
        }
        useUserStore().LogOut().then(() => {
          location.href = import.meta.env.VUE_APP_CONTEXT_PATH + 'index'
          sessionStorage.removeItem('serciveObj')
          sessionStorage.removeItem('commandObj')
        })
      })
    }
  })
}

function close() {
  useTagsViewStore().delView(route)
  router.push({ path: "/index" })
}
</script>
