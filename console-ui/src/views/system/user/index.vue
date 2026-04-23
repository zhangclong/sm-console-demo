<template>
  <div class="app-container user">
    <el-row :gutter="20">
      <!--用户数据-->
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" size="small">
          <el-form-item label="用户名称" prop="userName">
            <el-input
              v-model="queryParams.userName"
              placeholder="请输入用户名称"
              clearable
              size="small"
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input
              v-model="queryParams.phonenumber"
              placeholder="请输入手机号码"
              clearable
              size="small"
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select
              v-model="queryParams.status"
              placeholder="用户状态"
              clearable
              size="small"
              style="width: 240px"
              :popper-append-to-body="false"
            >
              <el-option
                v-for="dict in dict.type.sys_normal_disable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="登录锁定" prop="status">
            <el-select
              v-model="queryParams.loginLocked"
              placeholder="登录锁定"
              clearable
              size="small"
              :popper-append-to-body="false"
            >
              <el-option
                v-for="dict in dict.type.sys_user_login_locked"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              class="selectTime"
              v-model="dateRange"
              size="small"
              style="width: 240px;"
              value-format="yyyy-MM-dd"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item class="botton" style="border:none">
            <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" size="small" class="grayBtn" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="table-operator" >
          <el-col :span="1.5">
            <el-button
              type="primary"
              icon="Plus"
              size="small"
              @click="handleAdd"
              v-hasPermi="['system:user:add']"
            >新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="primary"
              icon="Edit"
              size="small"
              class="compile"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:user:edit']"
            >修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              icon="Delete"
              size="small"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:user:remove']"
            >删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="primary"
              size="small"
              @click="handleLevelSetting"
            >密码规则管理</el-button>
          </el-col>
          <!-- <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="Upload"
              size="small"
              @click="handleImport"
              v-hasPermi="['system:user:import']"
            >导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="warning"
              plain
              icon="Download"
              size="small"
              @click="handleExport"
              v-hasPermi="['system:user:export']"
            >导出</el-button>
          </el-col> -->
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table :data="userList" @selection-change="handleSelectionChange" stripe :header-cell-style="{background: '#F2F4F5',}">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户编号" align="center" key="userId" prop="userId" v-if="columns[0].visible" />
          <el-table-column label="用户名称" align="center" key="userName" prop="userName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" v-if="columns[2].visible" :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" v-if="columns[3].visible" width="120" />
          <el-table-column label="状态"   align="center" key="status" v-if="columns[4].visible">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns[5].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="登录锁定" align="center" key="loginLocked" v-if="columns[5].visible">
            <template #default="scope">
              <dict-tag :options="dict.type.sys_user_login_locked" :value="scope.row.loginLocked" />
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            align="center"
            width="180"
            class-name="small-padding fixed-width"
          >
            <template #default="scope">
              <el-button
                size="small"
                link
                icon="Edit"
                class="compile"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:user:edit']"
              >修改</el-button>
              <el-button
                v-if="scope.row.userId !== 1"
                size="small"
                link
                class="compile"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:user:remove']"
              >删除</el-button>
              <el-button
                size="small"
                link
                icon="Key"
                class="compile"
                @click="handleResetPwd(scope.row)"
                v-hasPermi="['system:user:resetPwd']"
              >重置</el-button>
              <el-button
                v-if="scope.row.loginLocked === '1'"
                size="small"
                link
                class="compile"
                icon="Unlock"
                @click="handleUnlock(scope.row)"
                v-hasPermi="['system:user:edit']"
              >解锁</el-button>
            </template>

          </el-table-column>

        </el-table>
        <!--分页组件-->
          <pagination

              v-show="total > 0"
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              @getList="getList"
            />
      </el-col>
    </el-row>

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body align-center :close-on-press-escape="false" :close-on-click-modal="false"	>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称"  prop="nickName">
              <el-input
                v-model="form.nickName"
                placeholder="请输入用户昵称"
                :validate-event="false"
                @blur="handleFieldBlur('nickName')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select :popper-append-to-body="false" v-model="form.roleIds" multiple placeholder="请选择">
                <el-option
                  v-for="item in roleOptions"
                  :key="item.roleId"
                  :label="item.roleName"
                  :value="item.roleId"
                  :disabled="item.status == 1"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名称" prop="userName">
              <el-input
                v-model="form.userName"
                placeholder="请输入用户名称"
                :validate-event="false"
                @blur="handleFieldBlur('userName')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
              <el-input
                v-model="form.password"
                placeholder="请输入用户密码"
                type="password"
                :validate-event="false"
                @blur="handleFieldBlur('password')"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select :popper-append-to-body="false" v-model="form.sex" placeholder="请选择">
                <el-option
                  v-for="dict in dict.type.sys_user_sex"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
             <el-col :span="12">
            <el-form-item v-if="form.userId && form.passwordExpired" label="密码有效期">
              {{form.passwordExpired}}
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>

          <el-col :span="12">
          <el-form-item label="登录锁定">
              <el-radio v-model="form.loginLocked" label="0">正常</el-radio>
              <el-radio v-model="form.loginLocked" label="1">锁定</el-radio>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio v-model="form.status" label="0" >正常</el-radio>
              <el-radio v-model="form.status" label="1" >停用</el-radio>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" maxlength="100"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel" size="small" class="grayBtn">取 消</el-button>
          <el-button type="primary" size="small" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body align-center :close-on-press-escape="false" :close-on-click-modal="false"	>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon><Upload /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或
          <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的用户数据
            <el-link type="info" style="font-size:12px" @click="importTemplate">下载模板</el-link>
          </div>
          <div class="el-upload__tip" style="color:red">提示：仅允许导入“xls”或“xlsx”格式文件！</div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false" class="grayBtn">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="ruleVisible" title="设置密码等级" align-center>
      <el-form label-width="80px" :rules="passwordRules" :model="ruleForm" ref="ruleFormRef">
        <el-form-item label="最小长度" prop="minLength">
          <el-input v-model="ruleForm.minLength"></el-input>
        </el-form-item>
        <el-form-item label="最大长度" prop="maxLength">
          <el-input v-model="ruleForm.maxLength"></el-input>
        </el-form-item>
        <el-form-item label="密码等级" prop="level">
          <el-checkbox-group v-model="ruleForm.level">
            <el-checkbox :label="1">必须包含小写字母</el-checkbox>
            <el-checkbox :label="2">必须包含大写字母</el-checkbox>
            <el-checkbox :label="3">必须包含数字</el-checkbox>
            <el-checkbox :label="4">必须包含特殊字符</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRuleForm">确 定</el-button>
          <el-button @click="ruleVisible = false" class="grayBtn">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue';
import { listUser, getUser, delUser, addUser, updateUser, exportUser, resetUserPwd, changeUserStatus, importTemplate as importTemplateApi, unlockUser, userPwdStrength } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import { getAppConfigKey } from '@/api/system/config';
import { checkPassword } from "@/utils/validate";
import { addDateRange } from "@/utils/ruoyi";
import { ElMessage, ElMessageBox } from 'element-plus';
import modal from '@/plugins/modal';
import util from '@/utils/aesutils.js';

defineOptions({
  name: 'User',
  dicts: ['sys_normal_disable', 'sys_user_login_locked', 'sys_user_sex']
});

const { proxy } = getCurrentInstance();

// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 用户表格数据
const userList = ref(null);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 默认密码
const initPassword = ref(undefined);
// 日期范围
const dateRange = ref([]);
// 岗位选项
const postOptions = ref([]);
// 角色选项
const roleOptions = ref([]);
const ruleVisible = ref(false);
const ruleForm = ref({
  minLength: 6,
  maxLength: 39,
  level: [],
});
const passwordRules = {
  minLength: [{ required: true, message: "最小长度不能为空", trigger: "blur" }],
  maxLength: [{ required: true, message: "最大长度不能为空", trigger: "blur" }],
  level: [{ required: true, message: "规则不能为空", trigger: "blur" }],
};
function createFormData() {
  return {
    userId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    loginLocked: "0",
    status: "0",
    remark: undefined,
    postIds: [],
    roleIds: [],
    passwordExpired: ''
  };
}

// 表单参数
const form = ref(createFormData());
// 用户导入参数
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VUE_APP_BASE_API + "/system/user/importData"
});
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  phonenumber: undefined,
  status: undefined,
  loginLocked: undefined
});
// 列信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `用户名称`, visible: true },
  { key: 2, label: `用户昵称`, visible: true },
  { key: 3, label: `手机号码`, visible: true },
  { key: 4, label: `状态`, visible: true },
  { key: 5, label: `登录锁定`, visible: true },
  { key: 5, label: `创建时间`, visible: true }
]);
// 表单校验
const rules = {
  userName: [{ required: true, message: "用户名称不能为空", trigger: "blur" }],
  nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
  password: [{ required: true, message: "用户密码不能为空", trigger: "blur" }],
  email: [{ type: "email", message: "'请输入正确的邮箱地址", trigger: ["blur", "change"] }],
  phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
};

// refs
const formRef = ref(null);
const queryFormRef = ref(null);
const uploadRef = ref(null);
const ruleFormRef = ref(null);

function handleFieldBlur(field) {
  formRef.value && formRef.value.validateField(field, () => {});
}

/** 查询用户列表 */
function getList(params) {
  if (params) {
    queryParams.pageSize = params.pageSize;
    queryParams.pageNum = params.pageNum;
  }
  listUser(addDateRange(queryParams, dateRange.value)).then(response => {
    userList.value = response.rows;
    total.value = response.total;
  });
}

// 筛选节点
function filterNode(value, data) {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
}

// 用户状态修改
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  ElMessageBox.confirm('确认要"' + text + '""' + row.userName + '"用户吗?', "警告", {
    customClass: 'message-logout',
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return changeUserStatus(row.userId, row.status);
  }).then(() => {
    modal.msgSuccess(text + "成功");
  }).catch(function() {
    row.status = row.status === "0" ? "1" : "0";
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = createFormData();
  formRef.value?.resetFields();
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.page = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  queryFormRef.value?.resetFields();
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  getUser().then(response => {
    postOptions.value = response.posts;
    roleOptions.value = response.roles;
    open.value = true;
    title.value = "添加用户";
    form.value.password = initPassword.value;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const userId = row.userId || ids.value;
  getUser(userId).then(response => {
    form.value = {
      ...createFormData(),
      ...response.data,
      password: "",
      postIds: response.postIds ?? [],
      roleIds: response.roleIds ?? []
    };
    postOptions.value = response.posts;
    roleOptions.value = response.roles;
    open.value = true;
    title.value = "修改用户";
  });
}

/** 用户登录解锁 */
function handleUnlock(row) {
  const userId = row.userId;
  ElMessageBox.confirm('请确认是否为该用户解锁?', "确认", {
    customClass: 'message-logout',
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return unlockUser(userId);
  }).then(() => {
    getList();
    modal.msgSuccess("解锁成功");
  });
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  ElMessageBox.prompt('请输入"' + row.userName + '"的新密码', "提示", {
    customClass: 'message-logout',
    cancelButtonText: "取消",
    confirmButtonText: "确定",
  }).then(({ value }) => {
    const valid = checkPassword(proxy.$appConfig.pwdStrength, value);
    if (valid !== true) {
      return ElMessage.error(valid);
    }
    resetUserPwd(row.userId, value).then(response => {
      if (response.code == 200) {
        modal.msgSuccess("修改成功，新密码是：" + value);
      } else {
        ElMessage.error(response.msg);
      }
    });
  }).catch(() => {});
}

/** 提交按钮 */
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      let userData = JSON.parse(JSON.stringify(form.value));
      userData.password = util.encrypt(userData.password);
      if (form.value.userId != undefined) {
        updateUser(userData).then(response => {
          modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addUser(userData).then(response => {
          modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value;
  ElMessageBox.confirm('是否确认删除用户编号为"' + userIds + '"的数据项?', "警告", {
    customClass: 'message-logout',
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return delUser(userIds);
  }).then(() => {
    getList();
    modal.msgSuccess("删除成功");
  });
}

/** 导出按钮操作 */
function handleExport() {
  const params = queryParams;
  ElMessageBox.confirm('是否确认导出所有用户数据项?', "警告", {
    customClass: 'message-logout',
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function() {
    return exportUser(params);
  }).then(response => {
    proxy.download(response.msg);
  });
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "用户导入";
  upload.open = true;
}

/** 下载模板操作 */
function importTemplate() {
  importTemplateApi().then(response => {
    proxy.download(response.msg);
  });
}

// 文件上传中处理
function handleFileUploadProgress(event, file, fileList) {
  upload.isUploading = true;
}

// 文件上传成功处理
function handleFileSuccess(response, file, fileList) {
  upload.open = false;
  upload.isUploading = false;
  uploadRef.value.clearFiles();
  ElMessageBox.alert(response.msg, "导入结果", { dangerouslyUseHTMLString: true });
  getList();
}

// 提交上传文件
function submitFileForm() {
  uploadRef.value.submit();
}

function submitRuleForm() {
  ruleFormRef.value.validate(valid => {
    if (valid) {
      userPwdStrength(ruleForm.value).then(res => {
        if (res.code === 200) {
          ruleVisible.value = false;
        }
      });
    }
  });
}

function handleLevelSetting() {
  const { pwdStrength } = proxy.$appConfig;
  ruleForm.value = pwdStrength;
  ruleVisible.value = true;
}

getList();
getAppConfigKey("user.initPassword").then(response => {
  initPassword.value = response.data;
});
</script>
<style lang="scss" scoped>
// .user{

// }




:deep(.el-select .el-select-dropdown){
  position: absolute !important;
  top: 25px !important;
  left: 0px !important;
}


</style>
