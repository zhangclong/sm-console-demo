<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true"
      v-show="showSearch" label-width="88px">
      <el-form-item label="操作类型" prop="commandType">
        <el-select v-model="queryParams.commandType" placeholder="请选择操作类型" clearable>
          <el-option v-for="dict in dict.type.cnsl_command_type"
            :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="节点名称" prop="nodeName">
        <el-input v-model="queryParams.nodeName" placeholder="请输入节点" clearable
          @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item label="指令" prop="cmd">
        <el-input v-model="queryParams.cmd" placeholder="请输入指令" clearable
          @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item label="结果状态" prop="resStatus">
        <el-select v-model="queryParams.resStatus" placeholder="请选择结果状态" clearable >
          <el-option v-for="dict in dict.type.cnsl_command_result"
            :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px" value-format="yyyy-MM-dd"
          type="daterange" range-separator="-"
          start-placeholder="开始日期" end-placeholder="结束日期" >
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="Search"
          size="small"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="Refresh" size="small" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="table-operator">
      <el-col :span="1.5">
        <el-button
          type="danger"

          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['console:commandhistory:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"

          icon="Download"
          size="small"
          @click="handleExport"
          v-hasPermi="['console:commandhistory:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="commandhistoryList"
      @selection-change="handleSelectionChange"
      :header-cell-style="{background: '#f3f5f6',}"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="历史ID" align="center" prop="historyId" />
      <el-table-column label="操作类型" align="center" prop="commandType">
        <template #default="scope">
          <dict-tag :options="dict.type.cnsl_command_type" :value="scope.row.commandType"/>
        </template>
      </el-table-column>
      <el-table-column label="节点" align="center" prop="nodeId" >
        <template #default="scope">
          <span>{{ (scope.row.nodeName) ? scope.row.nodeName : scope.row.nodeId}}</span>
        </template>
      </el-table-column>
      <el-table-column label="节点管理器" align="center" prop="managerId">
        <template #default="scope">
          <span>{{ (scope.row.managerName) ? scope.row.managerName : scope.row.managerId}}</span>
        </template>
      </el-table-column>
      <el-table-column label="指令" align="center" prop="cmd" />
      <el-table-column label="内容" align="center" prop="cmdMsg" :show-overflow-tooltip="true" />
      <el-table-column label="结果状态" align="center" prop="resStatus">
        <template #default="scope">
          <dict-tag :options="dict.type.cnsl_command_result" :value="scope.row.resStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时长(毫秒)" align="center" prop="duration" />
      <el-table-column width="200px" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" link icon="View" @click="look(scope.row)"
          v-hasPermi="['console:commandhistory:look']">查看</el-button>
          <el-button size="small" link icon="Delete" @click="handleDelete(scope.row)"
            v-hasPermi="['console:commandhistory:remove']" >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <LookDialog v-if="open" @close="lookClose" :historyId="historyId"/>
  </div>
</template>

<script>
import {listCommandhistory, delCommandhistory,} from "@/api/console/commandhistory";
import LookDialog from "./lookCommand.vue"
export default {
  name: "Commandhistory",
  dicts: ["cnsl_command_result", "cnsl_command_type"],
  components:{
    LookDialog
  },
  data() {
    return {
      // 遮罩层
      historyId:'',
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
      // 执行命令历史表格数据
      commandhistoryList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 执行时长(毫秒)时间范围
      daterangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        commandType: null,
        nodeName: null,
        managerId: null,
        cmd: null,
        resStatus: null,
        createTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        commandType: [
          { required: true, message: "操作类型不能为空", trigger: "change" },
        ],
        cmd: [{ required: true, message: "指令不能为空", trigger: "blur" }],
        resStatus: [
          { required: true, message: "结果状态不能为空", trigger: "change" },
        ],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询执行命令历史列表 */
    getList() {
      this.loading = true;
      this.queryParams.params = {};
      if (null != this.daterangeCreateTime && "" != this.daterangeCreateTime) {
        this.queryParams.params["beginCreateTime"] =
          this.daterangeCreateTime[0];
        this.queryParams.params["endCreateTime"] = this.daterangeCreateTime[1];
      }
      listCommandhistory(this.queryParams).then((response) => {
        this.commandhistoryList = response.rows;
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
        historyId: null,
        commandType: null,
        nodeName: null,
        managerId: null,
        cmd: null,
        cmdMsg: null,
        cmdFile: null,
        resStatus: null,
        resMsg: null,
        createTime: null,
        duration: null,
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
      this.daterangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.historyId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 查看按钮操作 */
    look(row) {
      const historyId = row.historyId || this.ids;
      this.historyId = historyId
      this.open = true;
    },
    lookClose(){
      this.open=false
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const historyIds = row.historyId || this.ids;
      this.$modal
        .confirm('是否确认删除执行命令历史编号为"' + historyIds + '"的数据项？')
        .then(function () {
          return delCommandhistory(historyIds);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "console/commandhistory/export",
        {
          ...this.queryParams,
        },
        `commandhistory_${new Date().getTime()}.xlsx`
      );
    },
  },
};
</script>
