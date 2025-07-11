import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.connecting = false
    this.connectionCallbacks = []
    this.messageCallbacks = new Map()
    this.typingCallbacks = new Map()
    this.userCallbacks = new Map()

    this.currentUserId = null
    this.activeConversations = new Set()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000

    this.messageQueue = []

    this.heartbeatInterval = null
    this.lastHeartbeat = null

    console.log(
      '💬 WebSocketService initialized - Instance ID:',
      Math.random().toString(36).substr(2, 9)
    )
  }

  async connect(userId, token) {
    if (this.connected || this.connecting) {
      console.log(
        '💬 Already connected or connecting - userId:',
        userId,
        'currentUserId:',
        this.currentUserId
      )
      return
    }

    this.connecting = true
    this.currentUserId = userId

    try {
      let wsURL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws-native'
      if (token) {
        wsURL += (wsURL.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token)
      }
      console.log('💬 Connecting to native WebSocket:', wsURL)

      this.client = new Client({
        brokerURL: wsURL,
        connectHeaders: {
          Authorization: 'Bearer ' + token,
          token: token,
        },
        debug: (str) => {
          console.log('💬 STOMP Debug:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      })

      this.client.onConnect = () => {
        console.log('💬 WebSocket connected successfully!')
        this.connected = true
        this.connecting = false
        this.reconnectAttempts = 0
        console.log('💬 Calling setupSubscriptions...')
        this.setupSubscriptions()
        this.startHeartbeat()
        this.processMessageQueue()
        this.notifyConnectionCallbacks(true)
      }

      this.client.onStompError = (error) => {
        console.error('💬 STOMP error:', error)
        this.connected = false
        this.connecting = false
        this.notifyConnectionCallbacks(false, error)
        this.handleConnectionError()
      }

      this.client.onWebSocketError = (error) => {
        console.error('💬 WebSocket error:', error)
        this.connected = false
        this.connecting = false
        this.notifyConnectionCallbacks(false, error)
        this.handleConnectionError()
      }

      this.client.onWebSocketClose = () => {
        console.log('💬 WebSocket connection closed')
        this.connected = false
        this.connecting = false
        this.stopHeartbeat()
        this.notifyConnectionCallbacks(false, 'Connection closed')
        this.handleConnectionError()
      }

      console.log('💬 Activating WebSocket client...')
      await this.client.activate()
      console.log('💬 WebSocket client activated')
    } catch (error) {
      console.error('💬 Failed to connect:', error)
      this.connected = false
      this.connecting = false
      this.notifyConnectionCallbacks(false, error)
      this.handleConnectionError()
      throw error
    }
  }

  disconnect() {
    console.log('💬 Disconnecting from WebSocket server...')

    this.stopHeartbeat()
    this.activeConversations.clear()
    this.messageCallbacks.clear()
    this.typingCallbacks.clear()
    this.userCallbacks.clear()
    this.connectionCallbacks = []

    if (this.client && this.connected) {
      this.client.deactivate()
    }

    this.client = null
    this.connected = false
    this.connecting = false
    this.currentUserId = null

    console.log('💬 WebSocket disconnected')
  }

  setupSubscriptions() {
    if (!this.client || !this.connected) {
      console.warn('💬 Cannot setup subscriptions: client not ready')
      return
    }

    console.log('💬 Setting up WebSocket subscriptions...')

    // Subscribe to user-specific messages
    this.client.subscribe(`/user/queue/messages`, (message) => {
      try {
        console.log('💬 [WS] 🎉 MESSAGE RECEIVED! Raw message:', message)
        console.log('💬 [WS] Message body:', message.body)
        console.log('💬 [WS] Message headers:', message.headers)
        console.log('💬 [WS] Subscription ID:', message.headers['subscription'])
        console.log('💬 [WS] Destination:', message.headers['destination'])

        const messageData = JSON.parse(message.body)
        console.log('💬 [WS] Parsed message data:', messageData)
        console.log('💬 [WS] Current page:', window.location.pathname)
        console.log('💬 [WS] Message callbacks count:', this.messageCallbacks.size)
        console.log('💬 [WS] Current user ID:', this.currentUserId)
        console.log('💬 [WS] Message sender ID:', messageData.senderId)
        console.log('💬 [WS] Message receiver ID:', messageData.receiverId)

        this.handleIncomingMessage(messageData)
      } catch (error) {
        console.error('💬 Error parsing message from /user/queue/messages:', error)
        console.error('💬 Raw message that failed to parse:', message)
      }
    })

    // Subscribe to public topic as backup
    this.client.subscribe(`/topic/chat`, (message) => {
      try {
        console.log('💬 [WS] 🎉 MESSAGE RECEIVED from /topic/chat! Raw message:', message)
        const messageData = JSON.parse(message.body)
        console.log('💬 [WS] Parsed message data from /topic/chat:', messageData)
        this.handleIncomingMessage(messageData)
      } catch (error) {
        console.error('💬 Error parsing message from /topic/chat:', error)
      }
    })

    this.client.subscribe(`/topic/chat/typing`, (message) => {
      try {
        const typingData = JSON.parse(message.body)
        console.log('💬 WebSocket received typing indicator:', typingData)
        this.handleTypingIndicator(typingData)
      } catch (error) {
        console.error('💬 Error parsing typing indicator:', error)
      }
    })

    this.client.subscribe(`/topic/chat/status`, (message) => {
      try {
        const statusData = JSON.parse(message.body)
        console.log('💬 WebSocket received user status:', statusData)
        this.handleUserStatus(statusData)
      } catch (error) {
        console.error('💬 Error parsing user status:', error)
      }
    })

    console.log('💬 WebSocket subscriptions setup completed')
  }

  async sendMessage(conversationId, content, messageType = 'TEXT') {
    if (!this.connected) {
      console.warn('💬 Not connected, queuing message...')
      this.messageQueue.push({ conversationId, content, messageType })
      return false
    }

    try {
      const messageData = {
        conversationId,
        content,
        messageType,
        timestamp: new Date().toISOString(),
      }

      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(messageData),
      })

      console.log('💬 Message sent:', { conversationId, messageType })
      return true
    } catch (error) {
      console.error('💬 Failed to send message:', error)

      this.messageQueue.push({ conversationId, content, messageType })
      return false
    }
  }

  sendTypingIndicator(conversationId, isTyping) {
    if (!this.connected) return

    try {
      const typingData = {
        conversationId,
        userId: this.currentUserId,
        isTyping,
        timestamp: new Date().toISOString(),
      }

      this.client.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(typingData),
      })
    } catch (error) {
      console.error('💬 Failed to send typing indicator:', error)
    }
  }

  joinConversation(conversationId) {
    if (!this.connected) return

    try {
      this.activeConversations.add(conversationId)

      const joinData = {
        conversationId,
        userId: this.currentUserId,
        action: 'JOIN',
      }

      this.client.publish({
        destination: '/app/chat.join',
        body: JSON.stringify(joinData),
      })

      console.log('💬 Joined conversation:', conversationId)
    } catch (error) {
      console.error('💬 Failed to join conversation:', error)
    }
  }

  leaveConversation(conversationId) {
    if (!this.connected) return

    try {
      this.activeConversations.delete(conversationId)

      const leaveData = {
        conversationId,
        userId: this.currentUserId,
        action: 'LEAVE',
      }

      this.client.publish({
        destination: '/app/chat.leave',
        body: JSON.stringify(leaveData),
      })

      console.log('💬 Left conversation:', conversationId)
    } catch (error) {
      console.error('💬 Failed to leave conversation:', error)
    }
  }

  handleIncomingMessage(messageData) {
    console.log('💬 WebSocket handleIncomingMessage:', messageData)

    const conversationId = messageData.conversationId
    console.log('💬 Message conversation ID:', conversationId)
    console.log('💬 Available message callbacks:', Array.from(this.messageCallbacks.keys()))

    if (this.messageCallbacks.has(conversationId)) {
      console.log('💬 Found specific callback for conversation:', conversationId)
      this.messageCallbacks.get(conversationId).forEach((callback) => {
        try {
          callback(messageData)
        } catch (error) {
          console.error('💬 Error in message callback:', error)
        }
      })
    }

    // Gọi tất cả callback đã đăng ký qua onMessage('*', ...)
    if (this.messageCallbacks.has('*')) {
      console.log('websocket.js: Gọi callback onMessage(*) với message', messageData)
      this.messageCallbacks.get('*').forEach((cb) => cb(messageData))
    }

    console.log('💬 Message handling completed')
  }

  handleTypingIndicator(typingData) {
    if (typingData.userId === this.currentUserId) return

    console.log('💬 Typing indicator:', typingData)

    const conversationId = typingData.conversationId

    if (this.typingCallbacks.has(conversationId)) {
      this.typingCallbacks.get(conversationId).forEach((callback) => {
        try {
          callback(typingData)
        } catch (error) {
          console.error('💬 Error in typing callback:', error)
        }
      })
    }
  }

  handleUserStatus(statusData) {
    console.log('💬 User status update:', statusData)

    if (this.userCallbacks.has(statusData.userId)) {
      this.userCallbacks.get(statusData.userId).forEach((callback) => {
        try {
          callback(statusData)
        } catch (error) {
          console.error('💬 Error in user status callback:', error)
        }
      })
    }
  }

  onMessage(conversationId, callback) {
    console.log('💬 Registering message callback for:', conversationId)

    if (!this.messageCallbacks.has(conversationId)) {
      this.messageCallbacks.set(conversationId, new Set())
    }

    this.messageCallbacks.get(conversationId).add(callback)
    console.log(
      '💬 Message callbacks for',
      conversationId,
      ':',
      this.messageCallbacks.get(conversationId).size
    )

    return () => {
      console.log('💬 Unregistering message callback for:', conversationId)
      if (this.messageCallbacks.has(conversationId)) {
        this.messageCallbacks.get(conversationId).delete(callback)
        console.log(
          '💬 Remaining callbacks for',
          conversationId,
          ':',
          this.messageCallbacks.get(conversationId).size
        )
      }
    }
  }

  onTyping(conversationId, callback) {
    if (!this.typingCallbacks.has(conversationId)) {
      this.typingCallbacks.set(conversationId, new Set())
    }

    this.typingCallbacks.get(conversationId).add(callback)

    return () => {
      if (this.typingCallbacks.has(conversationId)) {
        this.typingCallbacks.get(conversationId).delete(callback)
      }
    }
  }

  onUserStatus(userId, callback) {
    if (!this.userCallbacks.has(userId)) {
      this.userCallbacks.set(userId, new Set())
    }

    this.userCallbacks.get(userId).add(callback)

    return () => {
      if (this.userCallbacks.has(userId)) {
        this.userCallbacks.get(userId).delete(callback)
      }
    }
  }

  onConnection(callback) {
    this.connectionCallbacks.push(callback)
  }

  startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatInterval = setInterval(() => {
      if (this.connected && this.client) {
        this.lastHeartbeat = new Date()

        try {
          this.client.publish({
            destination: '/app/chat.ping',
            body: JSON.stringify({
              userId: this.currentUserId,
              timestamp: this.lastHeartbeat.toISOString(),
            }),
          })
        } catch (error) {
          console.error('💬 Heartbeat failed:', error)
        }
      }
    }, 30000) // Every 30 seconds
  }

  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  processMessageQueue() {
    if (this.messageQueue.length === 0) return

    console.log(`💬 Processing ${this.messageQueue.length} queued messages...`)

    const messages = [...this.messageQueue]
    this.messageQueue = []

    messages.forEach(async (message) => {
      try {
        await this.sendMessage(message.conversationId, message.content, message.messageType)
      } catch (error) {
        console.error('💬 Failed to send queued message:', error)
        this.messageQueue.push(message)
      }
    })
  }

  handleConnectionError() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('💬 Max reconnection attempts reached')
      this.notifyConnectionCallbacks(false, 'Max reconnection attempts reached')
      return
    }

    this.reconnectAttempts++
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1) // Exponential backoff

    console.log(
      `💬 Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
    )

    setTimeout(() => {
      if (this.currentUserId && !this.connected && !this.connecting) {
        const token = localStorage.getItem('token')
        if (token) {
          this.connect(this.currentUserId, token).catch((error) => {
            console.error('💬 Reconnection failed:', error)
          })
        }
      }
    }, delay)
  }

  notifyConnectionCallbacks(success, error = null) {
    this.connectionCallbacks.forEach((callback) => {
      try {
        callback(success, error)
      } catch (err) {
        console.error('💬 Error in connection callback:', err)
      }
    })

    this.connectionCallbacks = []
  }

  getBaseURL() {
    const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    return baseURL.replace('/api', '')
  }

  getStatus() {
    return {
      connected: this.connected,
      connecting: this.connecting,
      currentUserId: this.currentUserId,
      activeConversations: Array.from(this.activeConversations),
      reconnectAttempts: this.reconnectAttempts,
      messageQueueLength: this.messageQueue.length,
      lastHeartbeat: this.lastHeartbeat,
    }
  }

  cleanup() {
    this.stopHeartbeat()
    this.disconnect()
    console.log('💬 WebSocketService cleaned up')
  }

  subscribe(topic, callback) {
    if (!this.client || !this.connected) {
      console.warn('WebSocket chưa kết nối, không thể SUBSCRIBE:', topic)
      return
    }
    this.client.subscribe(topic, (message) => {
      try {
        const messageData = JSON.parse(message.body)
        console.log('💬 [WebSocket] Nhận message từ backend:', messageData)
        callback(messageData)
      } catch (error) {
        console.error('💬 [WebSocket] Lỗi khi parse message:', error, message)
      }
    })
    console.log('💬 [WebSocket] Đã SUBSCRIBE topic:', topic)
  }
}

// Singleton instance
let websocketService = null

function getWebSocketService() {
  if (!websocketService) {
    websocketService = new WebSocketService()
    console.log('💬 WebSocketService singleton created')

    if (typeof window !== 'undefined') {
      // Cleanup existing listener if any
      if (window._websocketCleanupListener) {
        window.removeEventListener('beforeunload', window._websocketCleanupListener)
      }

      window._websocketCleanupListener = () => {
        websocketService.cleanup()
      }
      window.addEventListener('beforeunload', window._websocketCleanupListener)
    }
  }
  return websocketService
}

// Export function instead of instance to prevent multiple instances
export default getWebSocketService

// For development debugging
if (import.meta.env.MODE === 'development') {
  window.getWebSocketService = getWebSocketService
}
