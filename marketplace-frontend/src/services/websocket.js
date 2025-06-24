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
    
    console.log('💬 WebSocketService initialized')
  }

  async connect(userId, token) {
    if (this.connecting || this.connected) {
      console.log('💬 Already connected or connecting...')
      return this.connected
    }

    console.log('💬 Connecting to WebSocket server...', { userId })
    
    this.connecting = true
    this.currentUserId = userId

    try {
      this.client = new Client({
        brokerURL: null, 
        webSocketFactory: () => {
          const socket = new SockJS(`${this.getBaseURL()}/ws`)
          return socket
        },
        
        connectHeaders: {
          'Authorization': `Bearer ${token}`,
          'X-User-ID': userId
        },
        
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        reconnectDelay: this.reconnectDelay,
        
        debug: (str) => {
          if (import.meta.env.MODE === 'development') {
            console.log('💬 STOMP Debug:', str)
          }
        },

        onConnect: (frame) => {
          console.log('💬 WebSocket connected successfully!', frame)
          this.connected = true
          this.connecting = false
          this.reconnectAttempts = 0
          
          this.setupSubscriptions()
          this.startHeartbeat()
          this.processMessageQueue()
          this.notifyConnectionCallbacks(true)
        },

        onStompError: (frame) => {
          console.error('💬 STOMP error:', frame)
          this.connected = false
          this.connecting = false
          this.notifyConnectionCallbacks(false, frame.headers?.message)
        },

        onWebSocketError: (error) => {
          console.error('💬 WebSocket error:', error)
          this.connected = false
          this.connecting = false
          this.handleConnectionError()
        },

        onWebSocketClose: (event) => {
          console.log('💬 WebSocket connection closed:', event)
          this.connected = false
          this.connecting = false
          this.stopHeartbeat()
          
          if (event.code !== 1000) { 
            this.handleConnectionError()
          }
        }
      })

      this.client.activate()
      
      return new Promise((resolve, reject) => {
        const timeout = setTimeout(() => {
          reject(new Error('Connection timeout'))
        }, 10000)

        this.connectionCallbacks.push((success, error) => {
          clearTimeout(timeout)
          if (success) {
            resolve(true)
          } else {
            reject(new Error(error || 'Connection failed'))
          }
        })
      })

    } catch (error) {
      console.error('💬 Failed to connect:', error)
      this.connecting = false
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
    if (!this.client || !this.connected) return

    this.client.subscribe(`/user/queue/messages`, (message) => {
      try {
        const messageData = JSON.parse(message.body)
        this.handleIncomingMessage(messageData)
      } catch (error) {
        console.error('💬 Error parsing message:', error)
      }
    })

    this.client.subscribe(`/topic/chat/typing`, (message) => {
      try {
        const typingData = JSON.parse(message.body)
        this.handleTypingIndicator(typingData)
      } catch (error) {
        console.error('💬 Error parsing typing indicator:', error)
      }
    })

    this.client.subscribe(`/topic/chat/status`, (message) => {
      try {
        const statusData = JSON.parse(message.body)
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
        timestamp: new Date().toISOString()
      }

      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(messageData)
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
        timestamp: new Date().toISOString()
      }

      this.client.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify(typingData)
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
        action: 'JOIN'
      }

      this.client.publish({
        destination: '/app/chat.join',
        body: JSON.stringify(joinData)
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
        action: 'LEAVE'
      }

      this.client.publish({
        destination: '/app/chat.leave',
        body: JSON.stringify(leaveData)
      })

      console.log('💬 Left conversation:', conversationId)

    } catch (error) {
      console.error('💬 Failed to leave conversation:', error)
    }
  }


  handleIncomingMessage(messageData) {
    console.log('💬 Received message:', messageData)
    
    const conversationId = messageData.conversationId
    

    if (this.messageCallbacks.has(conversationId)) {
      this.messageCallbacks.get(conversationId).forEach(callback => {
        try {
          callback(messageData)
        } catch (error) {
          console.error('💬 Error in message callback:', error)
        }
      })
    }
    

    if (this.messageCallbacks.has('*')) {
      this.messageCallbacks.get('*').forEach(callback => {
        try {
          callback(messageData)
        } catch (error) {
          console.error('💬 Error in global message callback:', error)
        }
      })
    }
  }

  handleTypingIndicator(typingData) {
    if (typingData.userId === this.currentUserId) return
    
    console.log('💬 Typing indicator:', typingData)
    
    const conversationId = typingData.conversationId
    
    if (this.typingCallbacks.has(conversationId)) {
      this.typingCallbacks.get(conversationId).forEach(callback => {
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
      this.userCallbacks.get(statusData.userId).forEach(callback => {
        try {
          callback(statusData)
        } catch (error) {
          console.error('💬 Error in user status callback:', error)
        }
      })
    }
  }

  onMessage(conversationId, callback) {
    if (!this.messageCallbacks.has(conversationId)) {
      this.messageCallbacks.set(conversationId, new Set())
    }
    
    this.messageCallbacks.get(conversationId).add(callback)
    
    return () => {
      if (this.messageCallbacks.has(conversationId)) {
        this.messageCallbacks.get(conversationId).delete(callback)
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
              timestamp: this.lastHeartbeat.toISOString()
            })
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
    
    console.log(`💬 Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
    
    setTimeout(() => {
      if (this.currentUserId && !this.connected && !this.connecting) {
        const token = localStorage.getItem('token')
        if (token) {
          this.connect(this.currentUserId, token).catch(error => {
            console.error('💬 Reconnection failed:', error)
          })
        }
      }
    }, delay)
  }

  notifyConnectionCallbacks(success, error = null) {
    this.connectionCallbacks.forEach(callback => {
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
      lastHeartbeat: this.lastHeartbeat
    }
  }

  cleanup() {
    this.stopHeartbeat()
    this.disconnect()
    console.log('💬 WebSocketService cleaned up')
  }
}

const websocketService = new WebSocketService()

if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', () => {
    websocketService.cleanup()
  })
}

export default websocketService

if (import.meta.env.MODE === 'development') {
  window.websocketService = websocketService
}