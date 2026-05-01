import request from '@/utils/request'

export function getConversations() {
  return request.get('/chat/conversations')
}

export function getMessages(conversationId, page = 1, size = 20) {
  return request.get(`/chat/messages/${conversationId}`, { params: { page, size } })
}

export function sendMessage(data) {
  return request.post('/chat/send', data)
}

export function markAsRead(conversationId) {
  return request.put(`/chat/read/${conversationId}`)
}

export function getUnreadCount() {
  return request.get('/chat/unread-count')
}

export function getOrCreateConversation(otherUserId) {
  return request.post('/chat/conversation', { otherUserId })
}
