<template>
  <div class="chat-button-container" v-if="shouldShowButton">
    <!-- Main Chat Button -->
    <button
      @click="toggleChat"
      class="chat-button"
      :class="{ 
        'active': showChatWindow,
        'has-unread': unreadCount > 0,
        'connecting': connecting
      }"
      :title="buttonTooltip"
    >
      <!-- Button Icon -->
      <div class="button-icon">
        <span v-if="connecting" class="loading-icon">⏳</span>
        <span v-else-if="showChatWindow" class="close-icon">❌</span>
        <span v-else class="chat-icon">💬</span>
      </div>
      
      <!-- Unread Badge -->
      <div v-if="unreadCount > 0 && !showChatWindow" class="unread-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </div>
      
      <!-- Connection Status -->
      <div class="connection-status" :class="connectionStatusClass">
        <div class="status-dot"></div>
      </div>
    </button>

    <!-- Seller Info Preview (when hovering) -->
    <transition name="fade-slide">
      <div v-if="showSellerPreview && !showChatWindow" class="seller-preview">
        <div class="seller-avatar">
          <img :src="sellerInfo?.avatar || '/placeholder-avatar.jpg'" :alt="sellerInfo?.name" />
          <div class="online-indicator" v-if="sellerInfo?.isOnline"></div>
        </div>
        <div class="seller-details">
          <h4>{{ sellerInfo?.name || 'Người bán' }}</h4>
          <p class="seller-status">
            <span v-if="sellerInfo?.isOnline" class="status-online">🟢 Đang hoạt động</span>
            <span v-else class="status-offline">⚫ Không hoạt động</span>
          </p>
          <div class="seller-stats">
            <span class="stat">⭐ {{ sellerInfo?.rating || 0 }}/5</span>
            <span class="stat">📞 Phản hồi trong {{ sellerInfo?.responseTime || '1h' }}</span>
          </div>
        </div>
        <div class="preview-action">
          <span class="action-text">Nhấn để chat</span>
        </div>
      </div>
    </transition>

    <!-- Chat Window -->
    <transition name="slide-up">
      <ChatWindow
        v-if="showChatWindow"
        :conversation-id="conversationId"
        :seller-id="sellerId"
        :product-id="productId"
        :product-name="productName"
        :seller-info="sellerInfo"
        @close="closeChat"
        @message-sent="handleMessageSent"
        @conversation-created="handleConversationCreated"
        @unread-count-changed="handleUnreadCountChanged"
        class="chat-window"
      />
    </transition>

    <!-- Quick Action Buttons (when chat is minimized) -->
    <transition name="fade">
      <div v-if="showQuickActions && !showChatWindow" class="quick-actions">
        <button
          @click="sendQuickMessage('Xin chào! Tôi quan tâm đến sản phẩm này.')"
          class="quick-action-btn"
          title="Gửi tin nhắn nhanh"
        >
          👋 Chào bán
        </button>
        
        <button
          @click="sendQuickMessage('Sản phẩm này còn hàng không ạ?')"
          class="quick-action-btn"
          title="Hỏi về tình trạng hàng"
        >
          📦 Còn hàng?
        </button>
        
        <button
          @click="sendQuickMessage('Có thể thương lượng giá được không?')"
          class="quick-action-btn"
          title="Thương lượng giá"
        >
          💰 Thương lượng
        </button>
      </div>
    </transition>

    <!-- Connection Error Notification -->
    <transition name="fade">
      <div v-if="connectionError" class="connection-error">
        <div class="error-content">
          <span class="error-icon">⚠️</span>
          <span class="error-text">{{ connectionError }}</span>
          <button @click="retryConnection" class="retry-btn">🔄</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/services/api'
import websocketService from '@/services/websocketService'
import ChatWindow from './ChatWindow.vue'

export default {
  name: 'ChatButton',
  components: {
    ChatWindow
  },
  
  props: {
    // Seller information
    sellerId: {
      type: String,
      required: true
    },
    
    // Product context (optional)
    productId: {
      type: String,
      default: null
    },
    
    productName: {
      type: String,
      default: null
    },
    
    // Position of the button
    position: {
      type: String,
      default: 'bottom-right', // bottom-right, bottom-left, top-right, top-left
      validator: (value) => ['bottom-right', 'bottom-left', 'top-right', 'top-left'].includes(value)
    },
    
    // Auto-open chat
    autoOpen: {
      type: Boolean,
      default: false
    },
    
    // Show quick actions
    enableQuickActions: {
      type: Boolean,
      default: true
    }
  },
  
  emits: ['chat-opened', 'chat-closed', 'message-sent', 'conversation-created'],
  
  setup(props, { emit }) {
    const authStore = useAuthStore()
    
    // Reactive state
    const showChatWindow = ref(false)
    const showSellerPreview = ref(false)
    const showQuickActions = ref(false)
    const connecting = ref(false)
    const connectionError = ref(null)
    const conversationId = ref(null)
    const sellerInfo = ref(null)
    const unreadCount = ref(0)
    
    // Loading states
    const loadingSellerInfo = ref(false)
    const creatingConversation = ref(false)
    
    // Computed properties
    const shouldShowButton = computed(() => {
      return authStore.isAuthenticated && 
             props.sellerId && 
             props.sellerId !== authStore.user?.id
    })
    
    const buttonTooltip = computed(() => {
      if (connecting.value) return 'Đang kết nối...'
      if (showChatWindow.value) return 'Đóng chat'
      if (sellerInfo.value?.isOnline) return `Chat với ${sellerInfo.value.name} (đang online)`
      return `Chat với ${sellerInfo.value?.name || 'người bán'}`
    })
    
    const connectionStatusClass = computed(() => {
      if (connecting.value) return 'connecting'
      if (websocketService.connected) return 'connected'
      return 'disconnected'
    })
    
    // Load seller information
    const loadSellerInfo = async () => {
      if (!props.sellerId || loadingSellerInfo.value) return
      
      loadingSellerInfo.value = true
      
      try {
        // TODO: Replace with actual seller API
        // const response = await sellerAPI.getById(props.sellerId)
        // sellerInfo.value = response.data
        
        // Mock seller info for now
        sellerInfo.value = {
          id: props.sellerId,
          name: 'Cửa hàng ABC',
          avatar: '/placeholder-avatar.jpg',
          rating: 4.8,
          responseTime: '15 phút',
          isOnline: Math.random() > 0.3, // Mock online status
          totalProducts: 245,
          completedOrders: 1532
        }
        
      } catch (error) {
        console.error('Error loading seller info:', error)
        sellerInfo.value = {
          id: props.sellerId,
          name: 'Người bán',
          avatar: '/placeholder-avatar.jpg',
          rating: 0,
          responseTime: '1h',
          isOnline: false
        }
      } finally {
        loadingSellerInfo.value = false
      }
    }
    
    // Get or create conversation
    const getOrCreateConversation = async () => {
      if (conversationId.value || creatingConversation.value) return conversationId.value
      
      creatingConversation.value = true
      
      try {
        const response = await chatAPI.createConversation(props.sellerId, props.productId)
        conversationId.value = response.data.id
        
        emit('conversation-created', {
          conversationId: conversationId.value,
          sellerId: props.sellerId,
          productId: props.productId
        })
        
        return conversationId.value
        
      } catch (error) {
        console.error('Error creating conversation:', error)
        connectionError.value = 'Không thể tạo cuộc hội thoại'
        throw error
      } finally {
        creatingConversation.value = false
      }
    }
    
    // Connect to WebSocket
    const connectWebSocket = async () => {
      if (websocketService.connected || connecting.value) return
      
      connecting.value = true
      connectionError.value = null
      
      try {
        const token = localStorage.getItem('token')
        if (!token) {
          throw new Error('No authentication token')
        }
        
        await websocketService.connect(authStore.user.id, token)
        
      } catch (error) {
        console.error('WebSocket connection failed:', error)
        connectionError.value = 'Không thể kết nối chat'
      } finally {
        connecting.value = false
      }
    }
    
    // Toggle chat window
    const toggleChat = async () => {
      if (showChatWindow.value) {
        closeChat()
      } else {
        await openChat()
      }
    }
    
    // Open chat window
    const openChat = async () => {
      try {
        // Ensure WebSocket connection
        await connectWebSocket()
        
        // Get or create conversation
        await getOrCreateConversation()
        
        // Open chat window
        showChatWindow.value = true
        showQuickActions.value = false
        
        // Reset unread count
        unreadCount.value = 0
        
        emit('chat-opened', {
          conversationId: conversationId.value,
          sellerId: props.sellerId,
          productId: props.productId
        })
        
      } catch (error) {
        console.error('Error opening chat:', error)
        connectionError.value = 'Không thể mở chat'
      }
    }
    
    // Close chat window
    const closeChat = () => {
      showChatWindow.value = false
      
      // Show quick actions after a delay
      setTimeout(() => {
        if (!showChatWindow.value && props.enableQuickActions) {
          showQuickActions.value = true
        }
      }, 500)
      
      emit('chat-closed', {
        conversationId: conversationId.value,
        sellerId: props.sellerId
      })
    }
    
    // Send quick message
    const sendQuickMessage = async (message) => {
      try {
        // Ensure conversation exists
        await getOrCreateConversation()
        
        // Send message via WebSocket
        if (websocketService.connected) {
          await websocketService.sendMessage(conversationId.value, message, 'TEXT')
          
          emit('message-sent', {
            conversationId: conversationId.value,
            content: message,
            messageType: 'TEXT'
          })
          
          // Open chat window to see the conversation
          showChatWindow.value = true
          showQuickActions.value = false
          
        } else {
          throw new Error('WebSocket not connected')
        }
        
      } catch (error) {
        console.error('Error sending quick message:', error)
        connectionError.value = 'Không thể gửi tin nhắn'
      }
    }
    
    // Retry connection
    const retryConnection = async () => {
      connectionError.value = null
      await connectWebSocket()
    }
    
    // Handle button hover
    const handleButtonHover = (hovering) => {
      if (!showChatWindow.value) {
        showSellerPreview.value = hovering
        
        if (!hovering) {
          // Show quick actions after preview disappears
          setTimeout(() => {
            if (!showSellerPreview.value && !showChatWindow.value && props.enableQuickActions) {
              showQuickActions.value = true
            }
          }, 300)
        } else {
          showQuickActions.value = false
        }
      }
    }
    
    // Event handlers
    const handleMessageSent = (data) => {
      emit('message-sent', data)
    }
    
    const handleConversationCreated = (data) => {
      conversationId.value = data.conversationId
      emit('conversation-created', data)
    }
    
    const handleUnreadCountChanged = (count) => {
      unreadCount.value = count
    }
    
    // Watch for seller changes
    watch(() => props.sellerId, (newSellerId) => {
      if (newSellerId) {
        conversationId.value = null
        loadSellerInfo()
      }
    })
    
    // Auto-open functionality
    watch(() => props.autoOpen, (shouldAutoOpen) => {
      if (shouldAutoOpen && !showChatWindow.value) {
        setTimeout(() => openChat(), 1000)
      }
    })
    
    // Lifecycle
    onMounted(() => {
      if (props.sellerId) {
        loadSellerInfo()
        
        // Auto-connect WebSocket in background
        if (authStore.isAuthenticated) {
          connectWebSocket()
        }
        
        // Setup button hover events
        const chatButton = document.querySelector('.chat-button')
        if (chatButton) {
          chatButton.addEventListener('mouseenter', () => handleButtonHover(true))
          chatButton.addEventListener('mouseleave', () => handleButtonHover(false))
        }
        
        // Auto-open if requested
        if (props.autoOpen) {
          setTimeout(() => openChat(), 1000)
        } else if (props.enableQuickActions) {
          setTimeout(() => {
            showQuickActions.value = true
          }, 2000)
        }
      }
    })
    
    onUnmounted(() => {
      // Clean up WebSocket subscriptions
      if (conversationId.value) {
        websocketService.leaveConversation(conversationId.value)
      }
    })
    
    return {
      // State
      showChatWindow,
      showSellerPreview,
      showQuickActions,
      connecting,
      connectionError,
      conversationId,
      sellerInfo,
      unreadCount,
      
      // Computed
      shouldShowButton,
      buttonTooltip,
      connectionStatusClass,
      
      // Methods
      toggleChat,
      openChat,
      closeChat,
      sendQuickMessage,
      retryConnection,
      handleMessageSent,
      handleConversationCreated,
      handleUnreadCountChanged
    }
  }
}
</script>

<style scoped>
.chat-button-container {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1rem;
}

/* Main Chat Button */
.chat-button {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.chat-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
}

.chat-button.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.4);
}

.chat-button.has-unread {
  animation: pulse-unread 2s infinite;
}

.chat-button.connecting {
  background: linear-gradient(135deg, #ffa500 0%, #ff8c00 100%);
}

@keyframes pulse-unread {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.button-icon {
  font-size: 1.5rem;
  transition: all 0.3s ease;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Unread Badge */
.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  min-width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  border: 2px solid white;
  animation: bounce 0.6s ease-in-out;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
  60% { transform: translateY(-3px); }
}

/* Connection Status */
.connection-status {
  position: absolute;
  bottom: 5px;
  right: 5px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6c757d;
  transition: background-color 0.3s ease;
}

.connection-status.connected .status-dot {
  background: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
}

.connection-status.connecting .status-dot {
  background: #ffa500;
  animation: pulse 1s infinite;
}

.connection-status.disconnected .status-dot {
  background: #ef4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Seller Preview */
.seller-preview {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 1rem;
  max-width: 280px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.seller-avatar {
  position: relative;
  flex-shrink: 0;
}

.seller-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #10b981;
  border-radius: 50%;
  border: 2px solid white;
}

.seller-details {
  flex: 1;
}

.seller-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 0.25rem 0;
}

.seller-status {
  font-size: 0.8rem;
  margin: 0 0 0.5rem 0;
}

.status-online {
  color: #10b981;
}

.status-offline {
  color: #6b7280;
}

.seller-stats {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.stat {
  font-size: 0.7rem;
  color: #6b7280;
}

.preview-action {
  flex-shrink: 0;
}

.action-text {
  font-size: 0.7rem;
  color: #667eea;
  font-weight: 600;
  text-transform: uppercase;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.quick-action-btn {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(102, 126, 234, 0.2);
  color: #667eea;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-action-btn:hover {
  background: #667eea;
  color: white;
  transform: translateX(-5px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* Chat Window */
.chat-window {
  width: 350px;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

/* Connection Error */
.connection-error {
  background: rgba(239, 68, 68, 0.95);
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3);
  max-width: 280px;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-icon {
  font-size: 1rem;
}

.error-text {
  flex: 1;
  font-size: 0.8rem;
}

.retry-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.7rem;
}

.retry-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .chat-button-container {
    bottom: 1.5rem;
    right: 1.5rem;
  }
  
  .chat-button {
    width: 55px;
    height: 55px;
  }
  
  .button-icon {
    font-size: 1.3rem;
  }
  
  .chat-window {
    width: 320px;
    height: 450px;
  }
  
  .seller-preview {
    max-width: 250px;
    padding: 0.75rem;
  }
  
  .quick-action-btn {
    font-size: 0.75rem;
    padding: 0.4rem 0.8rem;
  }
}

@media (max-width: 480px) {
  .chat-button-container {
    bottom: 1rem;
    right: 1rem;
  }
  
  .chat-window {
    width: calc(100vw - 2rem);
    height: 400px;
  }
  
  .seller-preview {
    max-width: 220px;
    padding: 0.5rem;
  }
  
  .seller-details h4 {
    font-size: 0.9rem;
  }
  
  .quick-actions {
    gap: 0.3rem;
  }
}

/* Position variants */
.chat-button-container.bottom-left {
  left: 2rem;
  right: auto;
  align-items: flex-start;
}

.chat-button-container.top-right {
  top: 2rem;
  bottom: auto;
}

.chat-button-container.top-left {
  top: 2rem;
  left: 2rem;
  right: auto;
  bottom: auto;
  align-items: flex-start;
}
</style>