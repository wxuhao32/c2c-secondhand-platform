import { getToken } from '@/utils/storage'
import request from '@/utils/request'

class WebSocketClient {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.heartbeatInterval = null
    this.messageHandlers = new Map()
    this.statusHandlers = new Set()
    this.connected = false
    this.pollingInterval = null
    this.pollingEnabled = false
    this.pollingDelay = /Mobi|Android/i.test(navigator.userAgent) ? 30000 : 15000
    this.lastPollTime = null
  }

  connect() {
    const token = getToken()
    if (!token) {
      console.warn('WebSocket: 未登录，跳过连接')
      return
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = import.meta.env.VITE_WS_URL || window.location.host
    const url = `${protocol}//${host}/ws/chat?token=${token}`

    if (this.ws && (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)) {
      return
    }

    try {
      this.ws = new WebSocket(url)

      this.ws.onopen = () => {
        this.connected = true
        this.reconnectAttempts = 0
        this._notifyStatus('connected')
        this._startHeartbeat()
        this._stopPolling()
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          const handler = this.messageHandlers.get(data.type)
          if (handler) {
            handler(data)
          }
          this.messageHandlers.forEach((handler, key) => {
            if (key === '*') {
              handler(data)
            }
          })
        } catch (e) {
          console.error('WebSocket消息解析失败:', e)
        }
      }

      this.ws.onclose = () => {
        this.connected = false
        this._notifyStatus('disconnected')
        this._stopHeartbeat()
        this._tryReconnect()
        this._startPolling()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        this._notifyStatus('error')
      }
    } catch (e) {
      console.error('WebSocket连接创建失败:', e)
      this._tryReconnect()
      this._startPolling()
    }
  }

  disconnect() {
    this._stopHeartbeat()
    this._stopPolling()
    this.reconnectAttempts = this.maxReconnectAttempts
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.connected = false
    this._notifyStatus('disconnected')
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    }
    return false
  }

  sendChatMessage(receiverId, content, messageType = 0) {
    if (this.send({ type: 'CHAT', receiverId, content, messageType })) {
      return true
    }
    return this._sendViaPolling(receiverId, content, messageType)
  }

  sendReadNotification(conversationId) {
    return this.send({ type: 'READ', conversationId })
  }

  on(type, handler) {
    this.messageHandlers.set(type, handler)
  }

  onAny(handler) {
    this.messageHandlers.set('*', handler)
  }

  off(type) {
    this.messageHandlers.delete(type)
  }

  onStatusChange(handler) {
    this.statusHandlers.add(handler)
  }

  offStatusChange(handler) {
    this.statusHandlers.delete(handler)
  }

  isConnected() {
    return this.connected
  }

  enablePolling(enabled = true) {
    this.pollingEnabled = enabled
    if (enabled && !this.connected) {
      this._startPolling()
    }
  }

  _startPolling() {
    if (!this.pollingEnabled || this.pollingInterval) return
    const token = getToken()
    if (!token) return

    console.log('消息轮询: 启动轮询降级模式')
    this.pollingInterval = setInterval(async () => {
      try {
        const res = await request({ url: '/messages/unread-count', method: 'get' })
        if (res.data && res.data.unreadCount > 0) {
          this.messageHandlers.forEach((handler, key) => {
            if (key === 'POLL_UNREAD' || key === '*') {
              handler({ type: 'POLL_UNREAD', data: res.data })
            }
          })
        }
      } catch (e) {
        console.warn('消息轮询查询失败:', e)
      }
    }, this.pollingDelay)
  }

  _stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval)
      this.pollingInterval = null
      console.log('消息轮询: 停止轮询')
    }
  }

  async _sendViaPolling(receiverId, content, messageType) {
    try {
      const { sendMessage } = await import('@/api/chat')
      await sendMessage({ receiverId, content, messageType })
      return true
    } catch (e) {
      console.error('轮询发送消息失败:', e)
      return false
    }
  }

  _notifyStatus(status) {
    this.statusHandlers.forEach(handler => handler(status))
  }

  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatInterval = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'PING' }))
      }
    }, 30000)
  }

  _stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  _tryReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn('WebSocket: 达到最大重连次数，切换到轮询模式')
      this._notifyStatus('polling')
      return
    }

    this.reconnectAttempts++
    const delay = this.reconnectInterval * this.reconnectAttempts
    console.log(`WebSocket: ${delay}ms后尝试第${this.reconnectAttempts}次重连`)

    setTimeout(() => {
      this.connect()
    }, delay)
  }
}

const wsClient = new WebSocketClient()

export default wsClient
