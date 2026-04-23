<template>
  <div class="lookCommand">
    <el-dialog v-model="visible" :before-close="close" title="查看执行命令" width="80%" :close-on-press-escape="false" :close-on-click-modal="false"	>
      <div class="uhry-block-main" v-loading="loading">
        <el-card>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">操作类型</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">
                      <span v-if="form.commandType === 'manager'">节点管理器操作</span>
                      <span v-if="form.commandType === 'node'">节点操作</span>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">节点管理器</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ (form.managerName) ? form.managerName : form.managerId }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">节点</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ (form.nodeName) ? form.nodeName : form.nodeId }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">指令</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ form.cmd }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">内容</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ form.cmdMsg }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">文件</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ form.cmdFile }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">结果状态</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">
                      <dict-tag :options="dict.type.cnsl_command_result" :value="form.resStatus" />
                    </div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">结果内容</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ form.resMsg }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf" style="width:15%">
                    <div class="cell">执行时长(毫秒)</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">{{ form.duration }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="close()">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  getCommandhistory,
} from "@/api/console/commandhistory";
export default {
  dicts: ["cnsl_command_result", "cnsl_command_type"],
  props: {
    historyId: {
      type: Number
    }
  },
  data() {
    return {
      visible: true,
      form: {},
      loading: false,
    }
  },
  mounted() {
    this.init()
  },
  methods: {

    async init() {
      this.loading = true
      let data = await getCommandhistory(this.historyId)
      this.loading = false
      if (data.code === 200) {
        this.form = data.data
      }
    },
    close() {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss" scoped>
.lookCommand {}
</style>
