<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-row>
          <el-form-item label="模版组"> {{templateGroup.groupName}} </el-form-item>
      </el-row>
      <el-form-item label="模版类型" prop="tempType">
        <el-select v-model="queryParams.tempType" placeholder="请选择模版类型" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.cnsl_template_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"

          icon="Plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['console:tempgroup:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"

          icon="Close"
          size="small"
          @click="handleClose"
        >关闭</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模版类型" align="center" prop="tempType">
        <template #default="scope">
          <dict-tag :options="dict.type.cnsl_template_type" :value="scope.row.tempType"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            link
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['console:tempgroup:edit']"
          >修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改配置模版对话框 -->
    <el-dialog :title="title" v-model="open" width="1000px" append-to-body :close-on-press-escape="false" :close-on-click-modal="false"	:destroy-on-close="true">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模版类型" prop="tempType">
          <el-select v-model="form.tempType" placeholder="请选择模版类型">
            <el-option
              v-for="dict in dict.type.cnsl_template_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="1" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="模版内容" prop="tempContent">
          <textarea
            v-model="form.tempContent"
            class="yaml-textarea"
            rows="14"
            placeholder="请输入 YAML 内容"
            @blur="$refs.form && $refs.form.validateField('tempContent', () => {})"
          ></textarea>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, getTemplate, delTemplate, addTemplate, updateTemplate } from "@/api/console/template";
import { getTempgroup } from "@/api/console/tempgroup";

export default {
  name: "Template",
  dicts: ['cnsl_template_type'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 对应模版组信息
      templateGroup: {},
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 配置模版表格数据
      templateList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupId: null,
        tempName: null,
        tempContent: null,
        tempType: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        tempContent: [
          { required: true, message: "模版内容不能为空", trigger: "blur" }
        ],
        tempType: [
          { required: true, message: "模版类型不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    const groupId = this.$route.params && this.$route.params.groupId;
    this.queryParams.groupId = groupId;
    getTempgroup(groupId).then(response => {
      this.templateGroup = response.data;
    });

    this.getList();
  },
  methods: {
    /** 查询配置模版列表 */
    getList() {
      this.loading = true;
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        templateId: null,
        groupId: this.templateGroup.groupId,
        tempName: null,
        tempContent: '',
        tempType: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      };
      //this.$refs.customCodeMirror.actionResetCode();
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.templateId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    async handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加配置模版";

    },
    // 返回按钮
    handleClose() {
      const obj = { path: "/system/tempgroup" };
      this.$tab.closeOpenPage(obj);
    },
    /** 修改按钮操作 */
    async handleUpdate(row) {
      const templateId = row.templateId || this.ids
      let data = await getTemplate(templateId)
      if(data.code === 200){
        this.open = true;
        this.title = "修改配置模版";
        this.form = data.data;
      }
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.templateId != null) {
            updateTemplate(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            this.form.groupId = this.templateGroup.groupId;
            addTemplate(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.yaml-textarea {
  width: 100%;
  min-height: 260px;
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #606266;
  background: #fff;
  resize: vertical;
  outline: none;
}

.yaml-textarea:focus {
  border-color: #409eff;
}
</style>

