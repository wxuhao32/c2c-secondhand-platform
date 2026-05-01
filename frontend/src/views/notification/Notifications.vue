<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>站内消息</h2>
      <div class="header-actions">
        <el-tag type="danger" v-if="unreadCount > 0">{{ unreadCount }} 条未读</el-tag>
        <el-button size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">全部已读</el-button>
        <el-radio-group v-model="filterType" size="small" @change="loadMessages">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">系统通知</el-radio-button>
          <el-radio-button :value="1">用户消息</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="message-list" v-loading="loading">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息" />
      </div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-item"
        :class="{ unread: msg.isRead === 0 }"
        @click="handleRead(msg)"
      >
        <div class="msg-icon">
          <el-icon :size="24" :color="msg.type === 0 ? '#409eff' : '#67c23a'">
            <Bell v-if="msg.type === 0" />
            <ChatDotRound v-else />
          </el-icon>
        </div>
        <div class="msg-content">
          <div class="msg-header">
            <span class="msg-title">{{ msg.title || msg.typeName }}</span>
            <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
          </div>
          <div class="msg-body">{{ msg.content }}</div>
        </div>
        <div class="msg-actions">
          <el-button size="small" text type="danger" @click.stop="handleDelete(msg)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, ChatDotRound, Delete } from '@element-plus/icons-vue'
import { getMessageList, getUnreadCount, markAsRead, markAllAsRead, deleteMessage } from '@/api/notification'
import wsClient from '@/utils/websocket'

const messages = ref([])
const unreadCount = ref(0)
const loading = ref(false)
const filterType = ref(null)
let pollTimer = null

const loadMessages = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterType.value !== null) params.type = filterType.value
    const res = await getMessageList(params)
    messages.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.unreadCount || 0
  } catch (e) { console.error(e) }
}

const handleRead = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await markAsRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { console.error(e) }
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    messages.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) { console.error(e) }
}

const handleDelete = async (msg) => {
  try {
    await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })
    await deleteMessage(msg.id)
    messages.value = messages.value.filter(m => m.id !== msg.id)
    if (msg.isRead === 0) unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('删除成功')
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

const startPolling = () => {
  pollTimer = setInterval(() => {
    loadUnreadCount()
  }, 10000)
}

onMounted(() => {
  loadMessages()
  loadUnreadCount()
  startPolling()
  wsClient.enablePolling(true)
  wsClient.on('POLL_UNREAD', () => {
    loadMessages()
    loadUnreadCount()
  })
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  wsClient.off('POLL_UNREAD')
})
</script>

<style lang="scss" scoped>
.notification-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 22px;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.message-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f9fafc;
  }

  &.unread {
    background: #ecf5ff;

    .msg-title {
      font-weight: 600;
    }
  }

  &:last-child {
    border-bottom: none;
  }
}

.msg-icon {
  flex-shrink: 0;
  margin-right: 14px;
  margin-top: 2px;
}

.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;

  .msg-title {
    font-size: 15px;
    color: #303133;
  }

  .msg-time {
    font-size: 12px;
    color: #c0c4cc;
    flex-shrink: 0;
  }
}

.msg-body {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-actions {
  flex-shrink: 0;
  margin-left: 8px;
}

.empty-state {
  padding: 60px 0;
}
</style>
