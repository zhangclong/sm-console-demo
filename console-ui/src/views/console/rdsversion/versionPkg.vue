<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" >
      <el-row>
          <el-form-item label="版本信息"> {{rdsVersion.softwareName}} {{rdsVersion.versionNo}} </el-form-item>
      </el-row>
      <el-form-item label="包类型" prop="pkgType">
        <el-select v-model="queryParams.pkgType" placeholder="请选择包类型" clearable>
          <el-option
            v-for="dict in packageTypeOptions"
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
          v-hasPermi="['console:rdsversionpkg:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"

          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['console:rdsversionpkg:remove']"
        >删除</el-button>
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

    <el-table v-loading="loading" :data="rdsversionpkgList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="包名称" align="center" prop="pkgName" />
      <el-table-column label="包类型" align="center" prop="pkgType">
        <template #default="scope">
          <dict-tag :options="dict.type.cnsl_package_type" :value="scope.row.pkgType"/>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSizeDesc" />
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
            icon="Download"
            @click="handleDownload(scope.row)"
            v-hasPermi="['console:rdsversionpkg:query']"
          >下载</el-button>
          <el-button
            size="small"
            link
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['console:rdsversionpkg:remove']"
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

    <!-- 添加或修改安装包信息对话框 -->
    <el-dialog v-if="open" :title="title" v-model="open" width="500px" append-to-body :close-on-press-escape="false" :close-on-click-modal="false"	>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="包类型" prop="pkgType">
          <el-select v-model="form.pkgType" placeholder="请选择包类型">
            <el-option
              v-for="dict in packageTypeOptions"
              :key="dict.value"
              :label="dict.label" :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-radio-group v-model="fileSelectType" style="margin-bottom: 16px">
          <el-radio :label="1">上传文件</el-radio>
          <el-radio :label="2">选择服务器文件</el-radio>
        </el-radio-group>
        <template v-if="fileSelectType === 1">
          <el-form-item label="导入文件" prop="fileName">
            <el-upload
              ref="upload"
              :limit="1"
              :headers="upload.headers"
              :disabled="upload.isUploading"
              :on-progress="handleFileUploadProgress"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :action="upload.url"
              :data="form"
              :on-success="handleFileSuccess"
              :auto-upload="false"
              drag
            >
              <el-icon><Upload /></el-icon>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip text-center">
                  <span>仅允许导入tar.gz, .tgz格式文件。</span>
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </template>
        <file-selection @fileSelected="fileSelected" v-if="fileSelectType === 2" />
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
import { listRdsversionpkg, delRdsversionpkg, updateRdsversionpkg, importPackage } from "@/api/console/rdsversionpkg";
import { getRdsversion } from "@/api/console/rdsversion";
import { getToken } from "@/utils/auth";
import fileSelection from '@/views/console/components/fileSelection/index.vue'

export default {
  name: "Rdsversionpkg",
  dicts: ['cnsl_package_type'],
  components: {
    fileSelection
  },
  data() {
    return {
      rdsVersion: {},
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
      // 安装包信息表格数据
      rdsversionpkgList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 上传参数
      upload: {
        // 是否禁用上传(正在上传中禁止再次上传)
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: import.meta.env.VUE_APP_BASE_API + "/console/rdsversionpkg/upload"
      },
      packageTypeOptions: [
        {label: '服务节点', value: 'worker'},
        {label: '中心节点', value: 'center'},
        {label: '代理节点', value: 'proxy'},
        {label: 'exporter节点', value: 'exporter'},
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        versionId: null,
        pkgType: null
      },
      // 表单参数
      form: {},
      // 表单校验
      // 表单校验
      rules: {
        pkgType: [
          { required: true, message: "请选择包类型", trigger: "blur" }
        ],
        fileName : [
          { required: true, message: "请选择导入文件", trigger: "blur" },
          //校验文件名必须要以.tar.gz或者.tgz结尾
          { pattern: /\.(tar\.gz|tgz)$/, message: "文件名必须要以.tar.gz或者.tgz结尾", trigger: "blur" }
        ],
      },
      fileSelectType: 1,
      filePath: ''
    };
  },
  created() {
    const versionId = this.$route.params && this.$route.params.versionId;
    this.queryParams.versionId = versionId;
    getRdsversion(versionId).then(response => {
        this.rdsVersion = response.data;
      });

    //console.log("versionId:" + versionId);
    this.getList();
  },
  methods: {
    /** 查询安装包信息列表 */
    getList() {
      this.loading = true;
      listRdsversionpkg(this.queryParams).then(response => {
        this.rdsversionpkgList = response.rows;
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
        packageId: null,
        versionId: this.rdsVersion.versionId,
        pkgType: null,
        fileName: null
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
      this.ids = selection.map(item => item.packageId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.title = "添加安装包信息";
      this.open = true;

      this.$nextTick(() => {
        // 清除 <el-upload ref="upload" 之前选择的文件
        this.$refs.upload && this.$refs.upload.clearFiles();
      })

    },
    /** 下载按钮操作 */
    handleDownload(row) {
      const packageId = row.packageId;
      const downloadUrl = "/console/rdsversionpkg/download/" + packageId;
      this.download(downloadUrl, {}, row.fileName);
    },
    // 返回按钮
    handleClose() {
      const obj = { path: "/system/rdsversion" };
      this.$tab.closeOpenPage(obj);
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.packageId != null) {
            if (this.fileSelectType === 1) {
              updateRdsversionpkg(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              this.pathUpload()
            }
          } else {
            if (this.fileSelectType === 1) {
              this.form.versionId = this.rdsVersion.versionId;
              //console.log("this.form.versionId", this.form.versionId);
              this.$refs.upload.submit();//提交上传信息
            } else {
              this.pathUpload()
            }
          }
        }
      });
    },
    pathUpload() {
      if (this.filePath === '') {
        this.$message.error('请先选择一个文件')
        return
      }
      const formData = new FormData();
      formData.append('file', this.filePath)
      Object.keys(this.form).forEach(k => {
        formData.append(k, this.form[k])
      })
      importPackage(formData).then(res => {
        if (res.code === 200) {
          this.$modal.msgSuccess("修改成功");
          this.open = false;
          this.getList();
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const packageIds = row.packageId || this.ids;
      this.$modal.confirm('是否确认删除安装包信息编号为"' + packageIds + '"的数据项？').then(function() {
        return delRdsversionpkg(packageIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('console/rdsversionpkg/export', {
        ...this.queryParams
      }, `rdsversionpkg_${new Date().getTime()}.xlsx`)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileChange(file, fileList) {
      if(file) {
        this.form.fileName = file.name;
      }
    },
    handleFileRemove(file, fileList) {
      if(file) {
        this.form.fileName = null;
      }
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.open = false;
      this.upload.isUploading = false;
      this.$refs.upload && this.$refs.upload.clearFiles();
      this.getList();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "上载结果", { dangerouslyUseHTMLString: true });
    },
    fileSelected(path) {
      this.filePath = path
    },
  }
};
</script>
