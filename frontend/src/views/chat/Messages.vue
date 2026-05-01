<template>
  <div class="messages-page">
    <PageHeader title="消息中心" :show-back="false">
      <template #extra>
        <el-badge :value="totalUnread" :hidden="totalUnread === 0" :max="99">
          <el-icon :size="20" class="header-icon"><ChatDotRound /></el-icon>
        </el-badge>
      </template>
    </PageHeader>

    <div class="messages-content">
      <div class="connection-status" v-if="wsStatus !== 'connected'">
        <el-icon class="status-icon" :class="wsStatus"><WarningFilled /></el-icon>
        <span>{{ wsStatus === 'error' ? '连接异常' : '正在连接...' }}</span>
      </div>

      <div class="conversation-list" v-loading="loading">
        <div v-if="conversations.length === 0 && !loading" class="empty-state">
          <el-icon :size="48" class="empty-icon"><ChatLineSquare /></el-icon>
          <p>暂无消息</p>
          <span>去逛逛看看有没有感兴趣的商品吧</span>
        </div>

        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          @click="openChat(conv)"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="48" :src="conv.otherUserAvatar">
              {{ conv.otherUserName?.charAt(0) }}
            </el-avatar>
            <span v-if="conv.unreadCount > 0" class="unread-badge">
              {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
            </span>
          </div>
          <div class="conversation-info">
            <div class="info-top">
              <span class="user-name">{{ conv.otherUserName }}</span>
              <span class="time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="last-message">
              {{ conv.lastMessage || '暂无消息' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, WarningFilled, ChatLineSquare } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getConversations, getUnreadCount } from '@/api/chat'
import wsClient from '@/utils/websocket'

const router = useRouter()
const loading = ref(false)
const conversations = ref([])
const totalUnread = ref(0)
const wsStatus = ref('disconnected')

const fetchConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data || []
    totalUnread.value = conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  } catch (e) {
    console.error('获取会话列表失败', e)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    totalUnread.value = res.data?.count || 0
  } catch (e) {
    console.error('获取未读数失败', e)
  }
}

const openChat = (conv) => {
  router.push({
    path: '/chat',
    query: {
      conversationId: conv.id,
      otherUserId: conv.otherUserId,
      otherUserName: conv.otherUserName,
      otherUserAvatar: conv.otherUserAvatar || ''
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const handleWsMessage = (data) => {
  if (data.type === 'CHAT') {
    fetchConversations()
    fetchUnreadCount()
  }
}

const handleWsStatus = (status) => {
  wsStatus.value = status
}

onMounted(() => {
  fetchConversations()
  fetchUnreadCount()
  wsClient.on('CHAT', handleWsMessage)
  wsClient.onStatusChange(handleWsStatus)
  wsClient.connect()
  wsStatus.value = wsClient.isConnected() ? 'connected' : 'disconnected'
})

onUnmounted(() => {
  wsClient.off('CHAT')
  wsClient.offStatusChange(handleWsStatus)
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.messages-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.messages-content {
  max-width: 720px;
  margin: 0 auto;
  padding: $spacing-4;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  background: $warning-50;
  border-radius: $radius-md;
  margin-bottom: $spacing-4;
  font-size: $font-size-sm;
  color: $warning-600;

  .status-icon {
    font-size: 16px;

    &.error {
      color: $danger-500;
    }
  }
}

.conversation-list {
  background: $color-bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;
  overflow: hidden;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-16 $spacing-4;
  color: $color-text-secondary;

  .empty-icon {
    color: $neutral-300;
    margin-bottom: $spacing-4;
  }

  p {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-regular;
    margin: 0 0 $spacing-2;
  }

  span {
    font-size: $font-size-sm;
  }
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: $spacing-4 $spacing-5;
  cursor: pointer;
  transition: background $transition-fast;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: $color-bg-hover;
  }

  &:active {
    background: $color-bg-active;
  }
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  margin-right: $spacing-4;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: $danger-500;
  color: white;
  font-size: 11px;
  font-weight: $font-weight-semibold;
  line-height: 18px;
  text-align: center;
  border-radius: $radius-full;
  border: 2px solid $color-bg-card;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-1;
}

.user-name {
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  flex-shrink: 0;
  margin-left: $spacing-2;
}

.last-message {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-icon {
  color: $color-text-regular;
  cursor: pointer;
  transition: color $transition-fast;

  &:hover {
    color: $color-primary;
  }
}
</style>
