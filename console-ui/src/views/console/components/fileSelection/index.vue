<template>
  <div class="file-browser" v-loading="listLoading">
    <div class="breadcrumb">
      <span v-for="(part, index) in breadcrumb" :key="index" @click="navigateTo(index)">
        <span class="breadcrumb-item">{{ part }}</span>/
      </span>
      <el-tooltip class="item" effect="dark" content="刷新" placement="top">
        <el-button class="refresh-button" size="small" circle icon="Refresh" @click="getFileList(currentPath)" />
      </el-tooltip>
    </div>

    <div class="file-list">
      <div v-if="currentPath !== rootPath" class="file-item" @click="backTop">
        <span>📁</span> ..
      </div>
      <el-empty v-if="fileList == 0 && currentPath == rootPath" description="" :image-size="80"></el-empty>
      <div
        v-for="item in fileList"
        :key="item.fileName"
        class="file-item"
        :class="{ selected: item.fileName === selectedItem }"
        @click="selectItem(item)"
      >
        <span v-if="item.dir">📁</span>
        <span v-else>📄</span>
        {{ item.fileName }}
      </div>
    </div>
  </div>
</template>

<script>
import { fileBrowser } from '@/api/console/fileSelect.js'

export default {
  data() {
    return {
      fileList: [], // 当前文件目录的列表
      selectedItem: null, // 当前选中的文件或目录
      breadcrumb: [], // 当前的路径（面包屑）
      currentPath: '', // 当前路径
      rootPath: '',
      listLoading: false,
    };
  },
  methods: {
    // 获取文件列表 movedUp: boolean
    async getFileList(path, moveUp = false) {
      this.listLoading = true
        fileBrowser({
          path: path,
          moveUp: moveUp
        }).then(res => {
          this.listLoading = false
          if (res.code === 200) {
            this.rootPath = res.root
            this.currentPath = res.currentPath
            this.fileList = res.data // 假设返回的是一个文件列表
            this.generateBreadcrumb()
          }
        })
    },

    // 选择文件或目录
    selectItem(item) {
      if (!item.dir) {
        this.selectedItem = item.fileName
        this.$emit('fileSelected', `${this.currentPath}/${item.fileName}`)
      } else {
        this.navigate(item)
      }
    },

    // 进入子目录
    navigate(item) {
      if (item.dir) {
        this.getFileList(this.currentPath + '/' + item.fileName)
        this.$emit('fileSelected', ``)
      }
    },
    // 生成面包屑导航
    generateBreadcrumb() {
      const relativePath = this.currentPath.replace(this.rootPath, '')
      const pathParts = relativePath.split('/').filter(part => part)
      this.breadcrumb = [this.rootPath, ...pathParts]
    },

    // 面包屑点击导航
    navigateTo(index) {
      const newBreadcrumb = this.breadcrumb.slice(0, index + 1)
      const path = newBreadcrumb.join('/')
      this.getFileList(path)
    },
    // 返回上一级
    backTop() {
      if (this.rootPath !== this.currentPath) {
        this.getFileList(this.currentPath, true)
      }
    }
  },

  // 初始加载文件列表
  created() {
    this.getFileList('')
  }
}
</script>

<style lang="scss" scoped>
.breadcrumb {
  margin-bottom: 10px;
  font-size: 14px;
  &-item {
    color: #4a75bb;
    cursor: pointer;
  }
  .refresh-button {
    float: right;
  }
}

.file-list {
  // display: flex;
  // flex-wrap: wrap;
  max-height: 400px;
  overflow: auto;
}

.file-item {
  width: 100%;
  border-bottom: 1px solid #cccccc;
  padding: 8px;
  cursor: pointer;
  user-select: none;
}

.file-item.selected {
  background-color: #d3f4f6;
  border-color: #67c4d7;
}

.file-item:hover {
  background-color: #f1f1f1;
}
</style>
