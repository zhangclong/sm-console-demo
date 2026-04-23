<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="模版组名称" prop="groupName">
        <el-input
          v-model="queryParams.groupName"
          placeholder="请输入模版组名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-operator">
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
          type="danger"

          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['console:tempgroup:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tempgroupList" stripe @selection-change="handleSelectionChange" :header-cell-style="{background: '#f3f5f6',}">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模版组名称" align="center" prop="groupName" />
      <el-table-column label="模版数" width="55" align="center" prop="templateCount" />
      <el-table-column label="适用版本" align="center" prop="versions" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            link
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['console:tempgroup:edit']"
          >修改</el-button>
          <el-button
            size="small"
            link
            icon="Edit"
            @click="handleOpenTemplates(scope.row)"
            v-hasPermi="['console:tempgroup:edit']"
          >管理模版</el-button>
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
    <el-dialog :title="title" v-model="open" width="500px" append-to-body :close-on-press-escape="false" :close-on-click-modal="false"	>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模版组名称" prop="groupName">
          <el-input v-model="form.groupName" placeholder="请输入模版组名称" />
        </el-form-item>


        <el-form-item label="拷贝已有模版" prop="fromGroupId" v-if="form.groupId == null">
          <el-select v-model="form.fromGroupId" placeholder="请选择已有模版组，如保持为空则不拷贝" style="width:360px;" clearable>
            <el-option
              v-for="item in tempgroupList"
              :key="item.groupId"
              :label="item.groupName"
              :value="item.groupId"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="适用版本">
          <el-select :popper-append-to-body="false" v-model="form.versionIds" multiple placeholder="请选择"  style="width:360px;">
            <el-option
              v-for="item in versionOptions"
              :key="item.versionId"
              :label="item.versionDesc"
              :value="item.versionId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
import { listTempgroup, getTempgroup, delTempgroup, addTempgroup, updateTempgroup } from "@/api/console/tempgroup";
import { listVersionByStatus } from "@/api/console/rdsversion";

export default {
  data() {
    return {
      // 遮罩层
      loading: true,
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
      tempgroupList: [],
      // 所有版本信息列表
      versionOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupName: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        groupName: [
          { required: true, message: "模版组名称不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    listVersionByStatus().then(response => {
        this.versionOptions = response.data;
      });
    this.getList();
  },
  methods: {
    /** 查询配置模版列表 */
    getList() {
      this.loading = true;
      listTempgroup(this.queryParams).then(response => {
        this.tempgroupList = response.rows;
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
        groupId: null,
        fromGroupId: null,
        groupName: null,
        versionIds : [],
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      };
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
      this.ids = selection.map(item => item.groupId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加模版组";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const groupId = row.groupId || this.ids
      getTempgroup(groupId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改模版组";
      });
    },
    /** 打开模版管理页面操作 */
    handleOpenTemplates: function(row) {
      const groupId = row.groupId;
      this.$router.push("/rds/template-mgr/group/" + groupId);
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.groupId != null) {
            updateTempgroup(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTempgroup(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const groupIds = row.groupId || this.ids;
      this.$modal.confirm('是否确认删除，所选择的模版组数据项？').then(function() {
        return delTempgroup(groupIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
