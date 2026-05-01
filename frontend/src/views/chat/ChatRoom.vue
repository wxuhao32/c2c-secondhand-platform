<template>
  <div class="chat-page">
    <div class="chat-header">
      <div class="header-left">
        <el-icon :size="22" class="back-btn" @click="goBack"><ArrowLeft /></el-icon>
        <el-avatar :size="36" :src="otherUserAvatar">
          {{ otherUserName?.charAt(0) }}
        </el-avatar>
        <div class="header-info">
          <span class="header-name">{{ otherUserName || '聊天' }}</span>
          <span class="header-status" :class="wsStatus">
            {{ wsStatus === 'connected' ? '在线' : '离线' }}
          </span>
        </div>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
      <div class="load-more" v-if="hasMore">
        <el-button text size="small" @click="loadMore" :loading="loadingMore">
          加载更多消息
        </el-button>
      </div>

      <div class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-wrapper"
          :class="{ 'is-self': msg.senderId === currentUserId }"
        >
          <div class="message-time-divider" v-if="shouldShowTime(msg)">
            {{ formatMessageTime(msg.createdAt) }}
          </div>
          <div class="message-item">
            <el-avatar :size="32" :src="msg.senderId === currentUserId ? '' : otherUserAvatar" class="msg-avatar">
              {{ msg.senderId === currentUserId ? '我' : otherUserName?.charAt(0) }}
            </el-avatar>
            <div class="message-body">
              <div class="message-bubble" :class="{ 'self-bubble': msg.senderId === currentUserId }">
                <span class="message-text">{{ msg.content }}</span>
              </div>
              <div class="message-meta" v-if="msg.senderId === currentUserId">
                <el-icon :size="12" class="status-icon" :class="getMessageStatusClass(msg)">
                  <component :is="getMessageStatusIcon(msg)" />
                </el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="typing-indicator" v-if="otherTyping">
        <span class="typing-dots">
          <span></span><span></span><span></span>
        </span>
        对方正在输入...
      </div>
    </div>

    <div class="chat-input-area">
      <div class="input-wrapper">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入消息..."
          resize="none"
          @keydown.enter.exact="handleSend"
          :disabled="sending"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          circle
          class="send-btn"
          @click="handleSend"
          :loading="sending"
          :disabled="!inputMessage.trim()"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Promotion, Clock, Check, WarningFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getMessages, sendMessage, markAsRead } from '@/api/chat'
import wsClient from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const conversationId = ref(Number(route.query.conversationId) || 0)
const otherUserId = ref(Number(route.query.otherUserId) || 0)
const otherUserName = ref(route.query.otherUserName || '')
const otherUserAvatar = ref(route.query.otherUserAvatar || '')
const currentUserId = computed(() => authStore.userId)

const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const wsStatus = ref('disconnected')
const otherTyping = ref(false)
const messagesContainer = ref(null)

const messageStatusMap = ref(new Map())

const fetchMessages = async (isLoadMore = false) => {
  if (!conversationId.value) return

  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res = await getMessages(conversationId.value, page.value, 20)
    const newMessages = res.data || []

    if (isLoadMore) {
      const scrollHeight = messagesContainer.value?.scrollHeight || 0
      messages.value = [...newMessages, ...messages.value]
      await nextTick()
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight - scrollHeight
      }
    } else {
      messages.value = newMessages
      await nextTick()
      scrollToBottom()
    }

    hasMore.value = newMessages.length >= 20
  } catch (e) {
    console.error('获取消息失败', e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchMessages(true)
}

const handleSend = async (e) => {
  if (e && e.type === 'keydown' && e.shiftKey) return
  if (e) e.preventDefault()

  const content = inputMessage.value.trim()
  if (!content || sending.value) return

  sending.value = true
  const tempId = `temp_${Date.now()}`

  const optimisticMsg = {
    id: tempId,
    conversationId: conversationId.value,
    senderId: currentUserId.value,
    receiverId: otherUserId.value,
    content,
    messageType: 0,
    status: -1,
    createdAt: new Date().toISOString(),
    senderName: authStore.username
  }

  messages.value.push(optimisticMsg)
  messageStatusMap.value.set(tempId, 'sending')
  inputMessage.value = ''
  scrollToBottom()

  try {
    const wsSent = wsClient.sendChatMessage(otherUserId.value, content)

    if (!wsSent) {
      const res = await sendMessage({
        receiverId: otherUserId.value,
        content,
        messageType: 0
      })
      const realMsg = res.data
      const idx = messages.value.findIndex(m => m.id === tempId)
      if (idx !== -1) {
        messages.value[idx] = realMsg
      }
      messageStatusMap.value.set(realMsg.id || tempId, 'sent')
    } else {
      const idx = messages.value.findIndex(m => m.id === tempId)
      if (idx !== -1) {
        messages.value[idx].status = 0
      }
      messageStatusMap.value.set(tempId, 'sent')
    }
  } catch (e) {
    const idx = messages.value.findIndex(m => m.id === tempId)
    if (idx !== -1) {
      messages.value[idx].status = -2
    }
    messageStatusMap.value.set(tempId, 'failed')
    console.error('发送消息失败', e)
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

const handleWsMessage = (data) => {
  if (data.type === 'CHAT') {
    const msgSenderId = data.senderId
    if (msgSenderId === otherUserId.value) {
      const exists = messages.value.find(m =>
        m.id === data.id || (m.content === data.content && m.senderId === data.senderId)
      )
      if (!exists) {
        messages.value.push({
          id: data.id,
          conversationId: conversationId.value,
          senderId: data.senderId,
          receiverId: data.receiverId,
          content: data.content,
          messageType: data.messageType || 0,
          status: 0,
          createdAt: data.createdAt,
          senderName: otherUserName.value
        })
        scrollToBottom()
      }

      if (conversationId.value) {
        markAsRead(conversationId.value)
        wsClient.sendReadNotification(conversationId.value)
      }
    }
  }
}

const handleWsStatus = (status) => {
  wsStatus.value = status
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleScroll = () => {
  if (messagesContainer.value && messagesContainer.value.scrollTop === 0 && hasMore.value && !loadingMore.value) {
    loadMore()
  }
}

const shouldShowTime = (msg) => {
  const idx = messages.value.findIndex(m => m.id === msg.id)
  if (idx === 0) return true
  const prev = messages.value[idx - 1]
  if (!prev || !prev.createdAt || !msg.createdAt) return false
  return new Date(msg.createdAt) - new Date(prev.createdAt) > 300000
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  const isYesterday = date.toDateString() === yesterday.toDateString()

  const timeStr = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`

  if (isToday) return timeStr
  if (isYesterday) return `昨天 ${timeStr}`
  return `${date.getMonth() + 1}/${date.getDate()} ${timeStr}`
}

const getMessageStatusClass = (msg) => {
  const status = messageStatusMap.value.get(msg.id)
  if (status === 'sending') return 'sending'
  if (status === 'sent') return 'sent'
  if (status === 'failed') return 'failed'
  if (msg.status === -1) return 'sending'
  if (msg.status === -2) return 'failed'
  return 'sent'
}

const getMessageStatusIcon = (msg) => {
  const status = messageStatusMap.value.get(msg.id)
  if (status === 'sending' || msg.status === -1) return Clock
  if (status === 'failed' || msg.status === -2) return WarningFilled
  return Check
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/messages')
  }
}

onMounted(() => {
  fetchMessages()
  wsClient.on('CHAT', handleWsMessage)
  wsClient.onStatusChange(handleWsStatus)
  wsClient.connect()
  wsStatus.value = wsClient.isConnected() ? 'connected' : 'disconnected'

  if (conversationId.value) {
    markAsRead(conversationId.value)
  }
})

onUnmounted(() => {
  wsClient.off('CHAT')
  wsClient.offStatusChange(handleWsStatus)
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables' as *;

.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $color-bg-page;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-3 $spacing-4;
  background: $color-bg-card;
  border-bottom: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  position: sticky;
  top: 0;
  z-index: $z-sticky;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-3;
  }

  .back-btn {
    cursor: pointer;
    color: $color-text-regular;
    transition: color $transition-fast;
    &:hover {
      color: $color-primary;
    }
  }

  .header-info {
    display: flex;
    flex-direction: column;
  }

  .header-name {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .header-status {
    font-size: $font-size-xs;
    &.connected {
      color: $success-500;
    }
    &.disconnected, &.error {
      color: $neutral-400;
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-4;
  scroll-behavior: smooth;
}

.load-more {
  text-align: center;
  padding: $spacing-2 0;
}

.message-time-divider {
  text-align: center;
  padding: $spacing-3 0;
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

.message-wrapper {
  margin-bottom: $spacing-4;
  animation: fadeInUp 0.3s ease;

  &.is-self {
    .message-item {
      flex-direction: row-reverse;
    }
    .msg-avatar {
      margin-left: $spacing-3;
      margin-right: 0;
    }
    .message-body {
      align-items: flex-end;
    }
    .message-bubble {
      background: $color-primary;
      color: white;
      border-radius: $radius-lg $radius-sm $radius-lg $radius-lg;
    }
    .message-meta {
      text-align: right;
    }
  }
}

.message-item {
  display: flex;
  align-items: flex-start;
}

.msg-avatar {
  margin-right: $spacing-3;
  flex-shrink: 0;
}

.message-body {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-bubble {
  padding: $spacing-3 $spacing-4;
  background: $color-bg-card;
  border-radius: $radius-sm $radius-lg $radius-lg $radius-lg;
  box-shadow: $shadow-xs;
  word-break: break-word;
  line-height: $line-height-normal;

  .message-text {
    font-size: $font-size-base;
  }
}

.message-meta {
  margin-top: $spacing-1;
  padding: 0 $spacing-1;

  .status-icon {
    &.sending {
      color: $color-text-placeholder;
      animation: pulse 1.5s infinite;
    }
    &.sent {
      color: $success-500;
    }
    &.failed {
      color: $danger-500;
    }
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  font-size: $font-size-xs;
  color: $color-text-secondary;

  .typing-dots {
    display: inline-flex;
    gap: 3px;

    span {
      width: 5px;
      height: 5px;
      background: $color-text-placeholder;
      border-radius: 50%;
      animation: typingBounce 1.4s infinite ease-in-out;

      &:nth-child(1) { animation-delay: 0s; }
      &:nth-child(2) { animation-delay: 0.2s; }
      &:nth-child(3) { animation-delay: 0.4s; }
    }
  }
}

.chat-input-area {
  padding: $spacing-3 $spacing-4;
  background: $color-bg-card;
  border-top: 1px solid $color-border-light;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);

  .input-wrapper {
    display: flex;
    align-items: flex-end;
    gap: $spacing-3;
  }

  .send-btn {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    transition: transform $transition-fast;

    &:hover:not(:disabled) {
      transform: scale(1.05);
    }

    &:disabled {
      opacity: 0.5;
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
