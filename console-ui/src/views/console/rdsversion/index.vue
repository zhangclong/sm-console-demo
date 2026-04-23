<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" >
      <el-form-item label="软件名称" prop="softwareName">
        <el-input
          v-model="queryParams.softwareName"
          placeholder="请输入软件名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="版本编号" prop="versionNo">
        <el-input
          v-model="queryParams.versionNo"
          placeholder="请输入版本编号"
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
          v-hasPermi="['console:rdsversion:add']"
        >新增</el-button>
      </el-col>
      <!--
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['console:rdsversion:edit']"
        >修改</el-button>
      </el-col>
      -->
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['console:rdsversion:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rdsversionList" stripe @selection-change="handleSelectionChange" :header-cell-style="{background: '#f3f5f6',}">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="软件名称" align="center" prop="softwareName" width="180"/>
      <el-table-column label="版本编号" align="center" prop="versionNo" width="150"/>
      <el-table-column label="默认模版组" align="center" prop="defaultGroupName" width="150"/>
      <el-table-column label="安装包" align="center" prop="packageCount" width="80"/>
      <el-table-column label="默认版本" align="center" prop="defaultVersion" width="100">
        <template #default="scope">
            <el-switch
              v-model="scope.row.defaultVersion"
              @change="handleDefaultChange(scope.row)"
            ></el-switch>
          </template>
      </el-table-column>
      <el-table-column label="启用状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark"  :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" link icon="Edit"  @click="handleUpdate(scope.row)"
            v-hasPermi="['console:rdsversion:edit']">修改</el-button>
          <el-button size="small" link icon="Edit" @click="handleOpenPackages(scope.row)"
            v-hasPermi="['console:rdsversion:query']">安装包</el-button>
          <el-button
            size="small"
            link
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['console:rdsversion:remove']"
          >删除</el-button>
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

    <!-- 添加或修改版本信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body align-center :close-on-press-escape="false" :close-on-click-modal="false"	>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="软件名称" prop="softwareName">
          <el-input
            v-model="form.softwareName"
            placeholder="请输入软件名称"
            :validate-event="false"
            @blur="handleFieldBlur('softwareName')"
          />
        </el-form-item>
        <el-form-item label="版本编号" prop="versionNo">
          <el-input
            v-model="form.versionNo"
            placeholder="请输入版本编号"
            :validate-event="false"
            @blur="handleFieldBlur('versionNo')"
          />
        </el-form-item>
        <el-form-item label="默认模版组">
          <el-select :popper-append-to-body="false" v-model="form.defaultGroupId" placeholder="请选择" clearable>
            <template v-if="form.versionId">
              <el-option v-if="form.versionId"
                         v-for="group in form.templateGroups"
                         :key="group.groupId"
                         :label="group.groupName"
                         :value="group.groupId"
              />
            </template>
            <template v-if="!form.versionId">
              <el-option
                v-for="group in groupOptions"
                :key="group.groupId"
                :label="group.groupName"
                :value="group.groupId"
              />
            </template>
          </el-select>
          <div v-if="form.versionId" class="el-form-item__error">请注意：此处选项需要在配置模版中加入对应的适用版本。</div>
        </el-form-item>
        <el-form-item label="默认版本" prop="defaultVersion">
          <el-radio-group v-model="form.defaultVersion">
            <el-radio :value="false">非默认</el-radio>
            <el-radio :value="true">默认</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="'1'">启用</el-radio>
            <el-radio :value="'0'">停用</el-radio>
          </el-radio-group>
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
import { listRdsversion, getRdsversion, delRdsversion, addRdsversion, updateRdsversion, changeVersionStatus, changeVersionDefault } from "@/api/console/rdsversion";
import {listAllGroups} from "@/api/console/tempgroup";

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
      // 版本信息表格数据
      rdsversionList: [],
      // 所有模版列表
      groupOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        softwareName: null,
        versionNo: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        softwareName: [
          { required: true, message: "软件名称不能为空", trigger: "blur" }
        ],
        versionNo: [
          { required: true, message: "版本编号不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getAllGroups();
  },
  methods: {
    handleFieldBlur(field) {
      this.$refs.form && this.$refs.form.validateField(field, () => {});
    },
    /** 查询版本信息列表 */
    getList() {

      this.loading = true;
      listRdsversion(this.queryParams).then(response => {
        this.rdsversionList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getAllGroups() {
      listAllGroups().then(res => {
        this.groupOptions = res.data;
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
        versionId: null,
        softwareName: null,
        versionNo: null,
        defaultGroupId: null,
        defaultVersion: false,
        status: '1',
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
      this.ids = selection.map(item => item.versionId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加版本信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const versionId = row.versionId || this.ids
      getRdsversion(versionId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改版本信息";
      });
    },
    /** 打开安装包管理操作 */
    handleOpenPackages: function(row) {
      const versionId = row.versionId;
      this.$router.push("/rds/version-pkg/version/" + versionId);
    },
    // 启用状态修改
    handleStatusChange(row) {
      let text = row.status === "1" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.softwareName + ' ' + row.versionNo + '" 版本吗？').then(function() {
        return changeVersionStatus(row.versionId, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    // 默认版本修改
    handleDefaultChange(row) {
      if(row.defaultVersion === false) {
        this.$alert("请选择非默认版本，设置为默认版！", "请注意", { dangerouslyUseHTMLString: true });
        row.defaultVersion = true;
        return;
      }

      this.$modal.confirm('确认要把"' + row.softwareName + ' ' + row.versionNo + '" 设置为默认版本吗？注意该版本被设为默认后其他版本会自动设置为非默认！').then(function() {
        return changeVersionDefault(row.versionId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("设置成功");
      }).catch(function() {
        row.defaultVersion = false;
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.versionId != null) {
            updateRdsversion(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRdsversion(this.form).then(response => {
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
      const versionIds = row.versionId || this.ids;
      this.$modal.confirm('是否确认删除选择的版本信息？').then(function() {
        return delRdsversion(versionIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
