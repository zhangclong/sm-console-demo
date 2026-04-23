<template>
  <div class="login">
    <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">

    <h3 class="title"><img :src="logo" alt=""> 管理控制台</h3>
    <el-form-item prop="username">
      <el-input
        v-model="loginForm.username"
        type="text"
        auto-complete="off"
        placeholder="账号"
      >
        <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
      </el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input
        v-model="loginForm.password"
        type="password"
        auto-complete="off"
        placeholder="密码"
        @keyup.enter="handleLogin"
      >
        <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
      </el-input>
    </el-form-item>
    <el-form-item prop="code">
      <el-input
        v-model="loginForm.code"
        auto-complete="off"
        placeholder="验证码"
        style="width: 63%"
        @keyup.enter="handleLogin"
      >
        <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
      </el-input>
      <div class="login-code">
        <img :src="codeUrl" @click="getCode" class="login-code-img"/>
      </div>
    </el-form-item>
    <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
    <el-form-item style="width:100%;">
      <el-button
        :loading="loading"
        size="default"

        style="width:100%;"
        @click.prevent="handleLogin"
      >
        <span v-if="!loading">登 录</span>
        <span v-else>登 录 中...</span>
      </el-button>
    </el-form-item>
  </el-form>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'
import logo from '@/assets/logo/menulogo.png'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)
const codeUrl = ref('')
const loginForm = reactive({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})
const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}
const loading = ref(false)
const redirect = ref(undefined)

watch(route, (r) => {
  redirect.value = r.query && r.query.redirect
}, { immediate: true })

function getCode() {
  getCodeImg().then(res => {
    codeUrl.value = "data:image/png;base64," + res.img;
    loginForm.uuid = res.uuid
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get('rememberMe')
  loginForm.username = username === undefined ? loginForm.username : username
  loginForm.password = password === undefined ? loginForm.password : decrypt(password)
  loginForm.rememberMe = rememberMe === undefined ? false : Boolean(rememberMe)
}

function handleLogin() {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      if (loginForm.rememberMe) {
        Cookies.set("username", loginForm.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.password), { expires: 30 })
        Cookies.set('rememberMe', loginForm.rememberMe, { expires: 30 })
      } else {
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove('rememberMe')
      }
      useUserStore().Login(loginForm).then(() => {
        sessionStorage.removeItem('serciveObj')
        sessionStorage.removeItem('commandObj')
        router.push({ path: redirect.value || "/" }).catch(() => {})
      }).catch(() => {
        loading.value = false
        getCode()
      })
    }
  })
}

getCode()
getCookie()
</script>

<style  lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-image: url("../assets/images/login-background.jpg");
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
  display: flex;
  align-items: center;
justify-content: center;
  img{
    width: 120px;
  }
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  border-top:4px solid #DF2525;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  button{
    background: #DF2525;
    color:white
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
</style>
