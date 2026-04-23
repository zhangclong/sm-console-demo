<template>
  <div class="message-container">
    <el-popover
      placement="right"
      width="400"
      trigger="click">
      <div class="message-list">
        <div class="message-item" @click="handleOpenInfo(item.alarmMailId)" v-for="item in dataList" :key="item.alarmMailId">
          <div class="item-top">
            <div class="message-title">{{ item.alarmServiceName }}</div>
            <div class="message-time">{{ item.alarmSendDate }}</div>
          </div>
          <div class="message-info">{{ item.alarmDetailed }}</div>
        </div>
        <el-empty v-if="dataList.length === 0" description="暂无消息" :image-size="80"></el-empty>
      </div>
      <template #reference>
        <div class="message-number">
          <div class="number" v-if="dataList.length > 0">{{ dataList.length > 99 ? 99 : dataList.length }}</div>
          <Bell />
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup>
import auth from '@/plugins/auth.js'

const router = useRouter()
const timer = ref(null)
const dataList = ref([])

onBeforeUnmount(() => {
  if (timer.value) clearTimeout(timer.value)
})

// 轮训查询站内信
function createTimer() {
  setTimeout(() => {
    getAlarmMail()
    createTimer()
  }, 15000)
}

// 获取站内信列表
function getAlarmMail() {
  // mailList().then(res => {
  //   if (res.code === 200) {
  //     dataList.value = res.data
  //   }
  // })
}

function handleOpenInfo(alarmMailId) {
  // readMessage(alarmMailId).then(res => {
  //   if (res.code === 200) {
  //     getAlarmMail()
  //   }
  // })
  // 判断用户是否有信息页面权限
  if (auth.hasPermi(['console:alarminfo:list'])) {
    router.push('/monitor/alarminfo?msgId=' + alarmMailId)
  } else {
    router.push('/401')
  }
}
</script>

<style lang="scss" scoped>
.message-container {
  display: inline-block;
  margin-right: 20px;
}
.message-list {
  width: 100%;
  max-height: 380px;
  overflow: auto;
  .item-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-block: 4px;
  }
  .message-title {
    font-size: 16px;
    color: #000000;
    font-weight: bold;
  }
  .message-time {
    font-size: 14px;
    color: #999999;
  }
  .message-info {
    max-height: 40px;
    overflow: hidden;
    color: #333333;
  }
  .message-item {
    padding: 12px 8px;
    border-bottom: 1px solid #eeeeee;
    cursor: pointer;
  }
}
:deep(.message-number) {
  width: 40px;
  height: 40px;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  > svg {
    color: #5a5e66;
    font-size: 26px;
    width: 26px;
    height: 26px;
  }
  .number {
    width: 24px;
    height: 24px;
    line-height: 24px;
    font-size: 16px;
    text-align: center;
    font-weight: bold;
    border-radius: 50%;
    color: #ffffff;
    background: red;
    position: absolute;
    right: 0;
    top: 0;
    transform: scale(0.7);
  }
}
</style>
