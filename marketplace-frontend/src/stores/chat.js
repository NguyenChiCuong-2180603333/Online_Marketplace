import { defineStore } from 'pinia'
import { chatAPI } from '@/services/api'
import getWebSocketService from '@/services/websocket'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    activeConversationId: null,

    messagesByConversation: {},

    connected: false,
    connecting: false,
    connectionError: null,

    totalUnreadCount: 0,
    unreadByConversation: {},

    typingUsers: {},

    onlineUsers: new Set(),
    userLastSeen: {},

    chatWindowOpen: false,
    selectedConversationId: null,

    loading: {
      conversations: false,
      messages: {},
      sending: {},
    },

    errors: {
      conversations: null,
      messages: {},
      sending: {},
    },

    settings: {
      soundEnabled: true,
      notificationsEnabled: true,
      showOnlineStatus: true,
      autoMarkAsRead: true, 
    },
  }),

  getters: {
    activeConversation: (state) => {
      return state.conversations.find((conv) => conv.id === state.activeConversationId)
    },

    activeMessages: (state) => {
      return state.messagesByConversation[state.activeConversationId] || []
    },

    sortedConversations: (state) => {
      return [...state.conversations].sort((a, b) => {
        const timeA = new Date(a.lastMessageTime || a.createdAt)
        const timeB = new Date(b.lastMessageTime || b.createdAt)
        return timeB - timeA
      })
    },

    unreadConversationsCount: (state) => {
      return Object.values(state.unreadByConversation).filter((count) => count > 0).length
    },

    getConversationByContext: (state) => {
      return (sellerId, productId = null) => {
        return state.conversations.find((conv) => {
          const isSeller = conv.participantIds.includes(sellerId)
          const isProduct = productId ? conv.productId === productId : true
          return isSeller && isProduct
        })
      }
    },

    isUserTyping: (state) => {
      return (conversationId, userId) => {
        const typing = state.typingUsers[conversationId]
        return typing?.userId === userId && typing?.isTyping
      }
    },

    isUserOnline: (state) => {
      return (userId) => {
        return state.onlineUsers.has(userId)
      }
    },

    getUserLastSeen: (state) => {
      return (userId) => {
        return state.userLastSeen[userId]
      }
    },
  },

  actions: {
    async initializeChat(userId, token) {
      this.connecting = true
      this.connectionError = null

      try {
        const websocketService = getWebSocketService()
        await websocketService.connect(userId, token)

        this.setupWebSocketListeners()

        await this.loadConversations()

        this.connected = true
        this.connecting = false

        console.log('💬 Chat initialized successfully')
      } catch (error) {
        console.error('💬 Failed to initialize chat:', error)
        this.connecting = false
        this.connectionError = error.message
        throw error
      }
    },

    disconnectChat() {
      const websocketService = getWebSocketService()
      websocketService.disconnect()

      this.connected = false
      this.connecting = false
      this.activeConversationId = null
      this.selectedConversationId = null
      this.typingUsers = {}
      this.onlineUsers.clear()
      this.userLastSeen = {}

      console.log('💬 Chat disconnected')
    },

    async loadConversations() {
      this.loading.conversations = true
      this.errors.conversations = null

      try {
        const response = await chatAPI.getConversations()
        this.conversations = response.data || []

        this.unreadByConversation = {}
        this.conversations.forEach((conversation) => {
          this.unreadByConversation[conversation.id] = conversation.unreadCount || 0
        })
        this.calculateTotalUnreadCount()

        console.log(`💬 Loaded ${this.conversations.length} conversations`)
      } catch (error) {
        console.error('💬 Failed to load conversations:', error)
        this.errors.conversations = error.response?.data?.message || 'Không thể tải cuộc hội thoại'
        this.conversations = []
        this.unreadByConversation = {}
        this.totalUnreadCount = 0
      } finally {
        this.loading.conversations = false
      }
    },

    async loadMessages(conversationId) {
      if (!conversationId) return

      this.loading.messages[conversationId] = true
      this.errors.messages[conversationId] = null

      try {
        const response = await chatAPI.getMessages(conversationId)
        const messages = response.data || []

        this.messagesByConversation[conversationId] = messages

        if (this.settings.autoMarkAsRead) {
          await this.markAsRead(conversationId)
        }

        console.log(`💬 Loaded ${messages.length} messages for conversation ${conversationId}`)
      } catch (error) {
        console.error('💬 Failed to load messages:', error)
        this.errors.messages[conversationId] =
          error.response?.data?.message || 'Không thể tải tin nhắn'
        this.messagesByConversation[conversationId] = []
      } finally {
        this.loading.messages[conversationId] = false
      }
    },

    async createConversation(sellerId, productId = null) {
      try {
        const response = await chatAPI.createConversation(sellerId, productId)
        const newConversation = response.data

        this.conversations.unshift(newConversation)

        this.messagesByConversation[newConversation.id] = []

        this.setActiveConversation(newConversation.id)

        console.log('💬 Created new conversation:', newConversation.id)

        return newConversation
      } catch (error) {
        console.error('💬 Failed to create conversation:', error)
        throw error
      }
    },

    async sendMessage(conversationId, content, messageType = 'TEXT') {
      if (!conversationId || !content.trim()) return

      this.loading.sending[conversationId] = true
      this.errors.sending[conversationId] = null

      try {
        const websocketService = getWebSocketService()
        const success = await websocketService.sendMessage(conversationId, content, messageType)

        if (!success) {
          throw new Error('Failed to send message via WebSocket')
        }

        const optimisticMessage = {
          id: 'temp_' + Date.now(),
          conversationId,
          senderId: this.getCurrentUserId(),
          receiverId: this.getReceiverIdForConversation(conversationId),
          content,
          messageType,
          status: 'sending',
          createdAt: new Date().toISOString(),
          isTemporary: true,
        }

        if (!this.messagesByConversation[conversationId]) {
          this.messagesByConversation[conversationId] = []
        }
        this.messagesByConversation[conversationId].push(optimisticMessage)

        this.updateConversationLastMessage(conversationId, content)

        console.log('💬 Message sent successfully')
      } catch (error) {
        console.error('💬 Failed to send message:', error)
        this.errors.sending[conversationId] = error.message
        throw error
      } finally {
        this.loading.sending[conversationId] = false
      }
    },

    async markAsRead(conversationId) {
      if (!conversationId) return

      console.log('chatStore: markAsRead được gọi cho conversation:', conversationId)
      console.log('chatStore: totalUnreadCount trước khi markAsRead:', this.totalUnreadCount)

      try {
        await chatAPI.markAsRead(conversationId)

        this.unreadByConversation[conversationId] = 0
        this.calculateTotalUnreadCount()

        console.log('chatStore: totalUnreadCount sau khi markAsRead:', this.totalUnreadCount)

        const messages = this.messagesByConversation[conversationId] || []
        messages.forEach((message) => {
          if (message.senderId !== this.getCurrentUserId()) {
            message.isRead = true
          }
        })
      } catch (error) {
        console.error('💬 Failed to mark as read:', error)
      }
    },

    setActiveConversation(conversationId) {
      this.activeConversationId = conversationId
      this.selectedConversationId = conversationId

      const websocketService = getWebSocketService()
      if (conversationId && websocketService.connected) {
        websocketService.joinConversation(conversationId)
      }

      this.loadMessages(conversationId)

      if (conversationId && this.settings.autoMarkAsRead) {
        this.markAsRead(conversationId)
      }
    },

    openChatWindow(conversationId = null) {
      this.chatWindowOpen = true

      if (conversationId) {
        this.setActiveConversation(conversationId)
      }
    },

    closeChatWindow() {
      this.chatWindowOpen = false

      const websocketService = getWebSocketService()
      if (this.activeConversationId && websocketService.connected) {
        websocketService.leaveConversation(this.activeConversationId)
      }

      this.activeConversationId = null
    },

    sendTypingIndicator(conversationId, isTyping) {
      const websocketService = getWebSocketService()
      if (websocketService.connected) {
        websocketService.sendTypingIndicator(conversationId, isTyping)
      }
    },

    setupWebSocketListeners() {
      console.log('chatStore: setupWebSocketListeners được gọi')
      const websocketService = getWebSocketService()

      websocketService.onMessage('*', (messageData) => {
        console.log('chatStore: Nhận message từ websocketService.onMessage(*)', messageData)
        this.handleIncomingMessage(messageData)
      })

      websocketService.onTyping('*', (typingData) => {
        this.handleTypingIndicator(typingData)
      })

      websocketService.onUserStatus('*', (statusData) => {
        this.handleUserStatusUpdate(statusData)
      })

      websocketService.onConnection((connected, error) => {
        this.connected = connected
        this.connecting = false
        this.connectionError = error
      })
    },

    async enrichMessageSenderInfo(msg) {
      const authStore = useAuthStore()
      if (!msg) return msg
      if (msg.senderId === authStore.user?.id) {
        msg.senderAvatar = authStore.user?.avatar || '/placeholder-avatar.jpg'
        msg.senderName =
          (authStore.user?.firstName + ' ' + authStore.user?.lastName).trim() || 'Bạn'
      } else {
        const conv = this.conversations.find((c) => c.id === msg.conversationId)
        if (conv && conv.avatar && conv.name) {
          msg.senderAvatar = conv.avatar
          msg.senderName = conv.name
        } else {
          try {
            const userRes = await chatAPI.getUserById(msg.senderId)
            const userData = userRes.data
            msg.senderAvatar = userData.avatar || '/placeholder-avatar.jpg'
            msg.senderName =
              `${userData.firstName || ''} ${userData.lastName || ''}`.trim() || 'Người dùng'
            this.updateMessageSenderInfo(msg)
          } catch (e) {
            console.error('Lỗi fetch user profile:', e)
            msg.senderAvatar = '/placeholder-avatar.jpg'
            msg.senderName = 'Người dùng'
          }
        }
      }
      return msg
    },

    updateMessageSenderInfo(msg) {
      const arr = this.messagesByConversation[msg.conversationId]
      if (arr) {
        const idx = arr.findIndex((m) => m.id === msg.id)
        if (idx !== -1) {
          arr[idx] = { ...arr[idx], senderAvatar: msg.senderAvatar, senderName: msg.senderName }
        }
      }
    },

    async handleIncomingMessage(messageData) {
      if (!messageData.conversationId || messageData.error) {
        console.warn('Bỏ qua message không hợp lệ:', messageData)
        return
      }
      console.log('chatStore: handleIncomingMessage được gọi', messageData)
      console.log('chatStore: Current activeConversationId:', this.activeConversationId)
      console.log('chatStore: Current totalUnreadCount before:', this.totalUnreadCount)

      const conversationId = messageData.conversationId

      if (!this.messagesByConversation[conversationId]) {
        this.messagesByConversation[conversationId] = []
      }

      const messages = this.messagesByConversation[conversationId]
      const tempIndex = messages.findIndex(
        (m) => m.isTemporary && m.content === messageData.content
      )
      if (tempIndex !== -1) {
        messages.splice(tempIndex, 1)
      }

      const enrichedMsg = await this.enrichMessageSenderInfo(messageData)
      messages.push({
        ...enrichedMsg,
        status: 'delivered',
      })

      this.updateConversationLastMessage(conversationId, messageData.content)

      if (
        conversationId !== this.activeConversationId &&
        messageData.senderId !== this.getCurrentUserId()
      ) {
        console.log('chatStore: Tăng unread count cho conversation:', conversationId)
        this.unreadByConversation[conversationId] =
          (this.unreadByConversation[conversationId] || 0) + 1
        this.calculateTotalUnreadCount()
        console.log('chatStore: totalUnreadCount sau khi tăng:', this.totalUnreadCount)
      } else {
        console.log(
          'chatStore: Không tăng unread vì đang ở conversation này hoặc là tin nhắn của mình'
        )
      }

      if (this.settings.soundEnabled && this.settings.notificationsEnabled) {
        this.playNotificationSound()
      }

      if (this.settings.notificationsEnabled && document.hidden) {
        this.showBrowserNotification(messageData)
      }
    },

    handleTypingIndicator(typingData) {
      const { conversationId, userId, isTyping } = typingData

      if (isTyping) {
        this.typingUsers[conversationId] = {
          userId,
          isTyping: true,
          timestamp: Date.now(),
        }

        setTimeout(() => {
          if (this.typingUsers[conversationId]?.userId === userId) {
            this.typingUsers[conversationId] = { userId, isTyping: false, timestamp: Date.now() }
          }
        }, 3000)
      } else {
        this.typingUsers[conversationId] = { userId, isTyping: false, timestamp: Date.now() }
      }
    },

    handleUserStatusUpdate(statusData) {
      const { userId, status, lastSeen } = statusData

      if (status === 'online') {
        this.onlineUsers.add(userId)
      } else {
        this.onlineUsers.delete(userId)
        if (lastSeen) {
          this.userLastSeen[userId] = lastSeen
        }
      }
    },

    calculateUnreadCounts() {
      this.unreadByConversation = {}

      this.conversations.forEach((conversation) => {
        this.unreadByConversation[conversation.id] = conversation.unreadCount || 0
      })

      this.calculateTotalUnreadCount()
    },

    calculateTotalUnreadCount() {
      this.totalUnreadCount = Object.values(this.unreadByConversation).reduce(
        (sum, count) => sum + count,
        0
      )
    },

    updateConversationLastMessage(conversationId, content) {
      const conversation = this.conversations.find((conv) => conv.id === conversationId)
      if (conversation) {
        conversation.lastMessageContent = content
        conversation.lastMessageTime = new Date().toISOString()
      }
    },

    playNotificationSound() {
      try {
        const audio = new Audio('/notification.mp3')
        audio.volume = 0.3
        audio.play().catch(() => {})
      } catch (error) {}
    },

    showBrowserNotification(messageData) {
      if (!('Notification' in window)) return

      if (Notification.permission === 'granted') {
        const conversation = this.conversations.find(
          (conv) => conv.id === messageData.conversationId
        )
        const senderName = this.getSenderName(messageData.senderId, conversation)

        new Notification(`Tin nhắn mới từ ${senderName}`, {
          body: messageData.content,
          icon: '/notification-icon.png',
          tag: messageData.conversationId,
        })
      } else if (Notification.permission !== 'denied') {
        Notification.requestPermission()
      }
    },

    getSenderName(senderId, conversation) {
      return conversation?.participants?.find((p) => p.id === senderId)?.name || 'Người dùng'
    },

    getCurrentUserId() {
      const authStore = useAuthStore()
      return authStore.user?.id
    },

    getReceiverIdForConversation(conversationId) {
      const conversation = this.conversations.find((conv) => conv.id === conversationId)
      if (!conversation) return null

      const currentUserId = this.getCurrentUserId()
      return conversation.participantIds.find((id) => id !== currentUserId)
    },

    updateSettings(newSettings) {
      this.settings = { ...this.settings, ...newSettings }

      localStorage.setItem('chatSettings', JSON.stringify(this.settings))
    },

    loadSettings() {
      try {
        const saved = localStorage.getItem('chatSettings')
        if (saved) {
          this.settings = { ...this.settings, ...JSON.parse(saved) }
        }
      } catch (error) {
        console.error('Failed to load chat settings:', error)
      }
    },

    clearChatData() {
      this.conversations = []
      this.messagesByConversation = {}
      this.unreadByConversation = {}
      this.typingUsers = {}
      this.onlineUsers.clear()
      this.userLastSeen = {}
      this.totalUnreadCount = 0
      this.activeConversationId = null
      this.selectedConversationId = null
      this.chatWindowOpen = false

      this.loading = {
        conversations: false,
        messages: {},
        sending: {},
      }
      this.errors = {
        conversations: null,
        messages: {},
        sending: {},
      }
    },
  },
})

function useAuthStore() {
  return {
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }
}
