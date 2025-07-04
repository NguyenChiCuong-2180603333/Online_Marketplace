<template>
  <div class="chat-window">
    <!-- Chat Header -->
    <div class="chat-header">
      <div class="header-content">
        <!-- Seller Info -->
        <div class="seller-info">
          <div class="seller-avatar">
            <img :src="sellerInfo?.avatar || '/placeholder-avatar.jpg'" :alt="sellerInfo?.name" />
            <div class="online-status" :class="{ online: sellerInfo?.isOnline }"></div>
          </div>
          <div class="seller-details">
            <h3>{{ sellerInfo?.name || 'Người bán' }}</h3>
            <p class="status-text">
              <span v-if="sellerInfo?.isOnline" class="online">🟢 Đang hoạt động</span>
              <span v-else class="offline">⚫ Offline</span>
              <span v-if="sellerInfo?.lastSeen" class="last-seen">
                • {{ formatLastSeen(sellerInfo.lastSeen) }}
              </span>
            </p>
          </div>
        </div>

        <!-- Header Actions -->
        <div class="header-actions">
          <button @click="toggleProductInfo" class="action-btn" title="Thông tin sản phẩm">
            📦
          </button>
          <button @click="$emit('close')" class="action-btn close-btn" title="Đóng chat">❌</button>
        </div>
      </div>

      <!-- Product Info (collapsible) -->
      <transition name="slide-down">
        <div v-if="showProductInfo && productName" class="product-info">
          <div class="product-summary">
            <span class="product-icon">📦</span>
            <span class="product-name">{{ productName }}</span>
            <button @click="viewProduct" class="view-product-btn">Xem sản phẩm</button>
          </div>
        </div>
      </transition>
    </div>

    <!-- Messages Container -->
    <div class="messages-container" ref="messagesContainer">
      <!-- Loading State -->
      <div v-if="loadingMessages" class="loading-messages">
        <div class="loading-spinner">⏳</div>
        <p>Đang tải tin nhắn...</p>
      </div>

      <!-- Welcome Message -->
      <div v-if="!loadingMessages && messages.length === 0" class="welcome-message">
        <div class="welcome-icon">👋</div>
        <h4>Bắt đầu cuộc trò chuyện</h4>
        <p>Gửi tin nhắn đầu tiên để bắt đầu chat với {{ sellerInfo?.name || 'người bán' }}!</p>

        <!-- Quick Start Options -->
        <div class="quick-start-options">
          <button
            v-for="option in quickStartMessages"
            :key="option.id"
            @click="sendQuickMessage(option.text)"
            class="quick-start-btn"
          >
            {{ option.icon }} {{ option.text }}
          </button>
        </div>
      </div>

      <!-- Messages List -->
      <div v-else class="messages-list">
        <!-- Date Separators and Messages -->
        <template
          v-for="(item, index) in messagesWithSeparators"
          :key="item.id || `separator-${index}`"
        >
          <!-- Date Separator -->
          <div v-if="item.type === 'separator'" class="date-separator">
            <span class="date-text">{{ item.date }}</span>
          </div>

          <!-- Message -->
          <div v-else class="message-wrapper" :class="{ 'own-message': item.isOwn }">
            <div class="message" :class="getMessageClass(item)">
              <!-- Message Content -->
              <div class="message-content">
                <!-- Text Message -->
                <div v-if="item.messageType === 'TEXT'" class="text-content">
                  {{ item.content }}
                </div>

                <!-- Image Message -->
                <div v-else-if="item.messageType === 'IMAGE'" class="image-content">
                  <img
                    :src="item.content"
                    :alt="'Hình ảnh'"
                    @click="openImageModal(item.content)"
                  />
                </div>

                <!-- File Message -->
                <div v-else-if="item.messageType === 'FILE'" class="file-content">
                  <div class="file-icon">📎</div>
                  <div class="file-info">
                    <span class="file-name">{{ getFileName(item.content) }}</span>
                    <span class="file-size">{{ getFileSize(item.metadata?.size) }}</span>
                  </div>
                  <button @click="downloadFile(item.content)" class="download-btn">⬇️</button>
                </div>

                <!-- System Message -->
                <div v-else-if="item.messageType === 'SYSTEM'" class="system-content">
                  <span class="system-icon">ℹ️</span>
                  {{ item.content }}
                </div>
              </div>

              <!-- Message Footer -->
              <div class="message-footer">
                <span class="message-time">{{ formatTime(item.createdAt) }}</span>
                <span v-if="item.isOwn" class="message-status" :class="getStatusClass(item.status)">
                  {{ getStatusIcon(item.status) }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- Typing Indicator -->
        <transition name="fade">
          <div v-if="isTyping" class="typing-indicator">
            <div class="typing-animation">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span class="typing-text">{{ sellerInfo?.name || 'Người bán' }} đang nhập...</span>
          </div>
        </transition>
      </div>
    </div>

    <!-- Message Input -->
    <div class="message-input-container">
      <!-- File Preview (if uploading) -->
      <transition name="slide-up">
        <div v-if="uploadingFile" class="file-preview">
          <div class="preview-content">
            <div class="file-info">
              <span class="file-icon">📎</span>
              <span class="file-name">{{ uploadingFile.name }}</span>
              <span class="file-size">({{ formatFileSize(uploadingFile.size) }})</span>
            </div>
            <button @click="cancelFileUpload" class="cancel-upload">❌</button>
          </div>
          <div class="upload-progress">
            <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
          </div>
        </div>
      </transition>

      <!-- Input Area -->
      <div class="input-area">
        <!-- Attachment Button -->
        <button @click="openFileSelector" class="attach-btn" title="Đính kèm file">📎</button>

        <!-- Text Input -->
        <div class="text-input-wrapper">
          <textarea
            v-model="messageText"
            @keydown="handleKeyDown"
            @input="handleTyping"
            @paste="handlePaste"
            ref="messageInput"
            placeholder="Nhập tin nhắn..."
            class="message-input"
            rows="1"
            :disabled="sendingMessage || !connected"
          ></textarea>

          <!-- Emoji Button -->
          <button @click="toggleEmojiPicker" class="emoji-btn" title="Chọn emoji">😊</button>
        </div>

        <!-- Send Button -->
        <button
          @click="sendMessage"
          :disabled="!canSendMessage"
          class="send-btn"
          :class="{ sending: sendingMessage }"
          title="Gửi tin nhắn"
        >
          <span v-if="sendingMessage" class="sending-icon">⏳</span>
          <span v-else class="send-icon">🚀</span>
        </button>
      </div>

      <!-- Connection Status -->
      <div v-if="!connected" class="connection-warning">
        <span class="warning-icon">⚠️</span>
        <span class="warning-text">Mất kết nối. Đang thử kết nối lại...</span>
        <button @click="reconnect" class="reconnect-btn">🔄</button>
      </div>
    </div>

    <!-- Hidden File Input -->
    <input
      ref="fileInput"
      type="file"
      @change="handleFileSelect"
      accept="image/*,.pdf,.doc,.docx,.txt"
      style="display: none"
    />

    <!-- Emoji Picker Modal -->
    <transition name="fade">
      <div v-if="showEmojiPicker" class="emoji-picker-overlay" @click="closeEmojiPicker">
        <div class="emoji-picker" @click.stop>
          <div class="emoji-categories">
            <button
              v-for="category in emojiCategories"
              :key="category.name"
              @click="selectedEmojiCategory = category.name"
              class="emoji-category-btn"
              :class="{ active: selectedEmojiCategory === category.name }"
            >
              {{ category.icon }}
            </button>
          </div>
          <div class="emoji-grid">
            <button
              v-for="emoji in currentEmojis"
              :key="emoji"
              @click="insertEmoji(emoji)"
              class="emoji-btn"
            >
              {{ emoji }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Image Modal -->
    <transition name="fade">
      <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
        <div class="image-modal" @click.stop>
          <button @click="closeImageModal" class="modal-close">❌</button>
          <img :src="modalImage" :alt="'Hình ảnh'" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/services/api'
import websocketService from '@/services/websocket'

export default {
  name: 'ChatWindow',
  props: {
    conversationId: {
      type: String,
      default: null,
    },

    sellerId: {
      type: String,
      required: true,
    },

    productId: {
      type: String,
      default: null,
    },

    productName: {
      type: String,
      default: null,
    },

    sellerInfo: {
      type: Object,
      default: () => ({}),
    },
  },

  emits: ['close', 'message-sent', 'conversation-created', 'unread-count-changed'],

  setup(props, { emit }) {
    const router = useRouter()
    const authStore = useAuthStore()

    // Reactive state
    const messages = ref([])
    const messageText = ref('')
    const loadingMessages = ref(false)
    const sendingMessage = ref(false)
    const connected = ref(false)
    const isTyping = ref(false)
    const showProductInfo = ref(false)
    const showEmojiPicker = ref(false)
    const showImageModal = ref(false)
    const modalImage = ref('')
    const selectedEmojiCategory = ref('smileys')

    // File upload
    const uploadingFile = ref(null)
    const uploadProgress = ref(0)

    // Typing timer
    let typingTimer = null
    let isUserTyping = false

    // Refs
    const messagesContainer = ref(null)
    const messageInput = ref(null)
    const fileInput = ref(null)

    // Quick start messages
    const quickStartMessages = ref([
      { id: 1, icon: '👋', text: 'Xin chào! Tôi quan tâm đến sản phẩm này.' },
      { id: 2, icon: '📦', text: 'Sản phẩm này còn hàng không ạ?' },
      { id: 3, icon: '💰', text: 'Có thể thương lượng giá được không?' },
      { id: 4, icon: '🚚', text: 'Thời gian giao hàng là bao lâu?' },
    ])

    // Emoji data
    const emojiCategories = ref([
      {
        name: 'smileys',
        icon: '😊',
        emojis: ['😊', '😂', '🥰', '😍', '🤔', '😢', '😭', '😡', '👍', '👎', '❤️', '💯'],
      },
      {
        name: 'objects',
        icon: '📦',
        emojis: ['📦', '💰', '🛒', '🚚', '📱', '💻', '🎁', '⭐', '🔥', '✨', '💎', '🏆'],
      },
      {
        name: 'activity',
        icon: '🎉',
        emojis: ['🎉', '🎊', '🎁', '🛍️', '💳', '🎯', '🚀', '⚡', '💫', '🌟', '🔔', '📢'],
      },
    ])

    // Computed properties
    const currentEmojis = computed(() => {
      const category = emojiCategories.value.find((cat) => cat.name === selectedEmojiCategory.value)
      return category ? category.emojis : []
    })

    const canSendMessage = computed(() => {
      return (
        connected.value &&
        messageText.value.trim().length > 0 &&
        !sendingMessage.value &&
        !uploadingFile.value
      )
    })

    const messagesWithSeparators = computed(() => {
      const result = []
      let lastDate = null

      messages.value.forEach((message) => {
        const messageDate = new Date(message.createdAt).toDateString()

        if (messageDate !== lastDate) {
          result.push({
            type: 'separator',
            date: formatDate(new Date(message.createdAt)),
            id: `separator-${messageDate}`,
          })
          lastDate = messageDate
        }

        result.push({
          ...message,
          isOwn: message.senderId === authStore.user?.id,
        })
      })

      return result
    })

    // Load conversation messages
    const loadMessages = async () => {
      if (!props.conversationId) return

      loadingMessages.value = true

      try {
        const response = await chatAPI.getMessages(props.conversationId)
        messages.value = response.data || []

        // Mark messages as read
        await markAsRead()

        // Scroll to bottom
        await nextTick()
        scrollToBottom()
      } catch (error) {
        console.error('Error loading messages:', error)
        messages.value = []
      } finally {
        loadingMessages.value = false
      }
    }

    // Send message
    const sendMessage = async () => {
      if (!canSendMessage.value) return

      const content = messageText.value.trim()
      if (!content) return

      sendingMessage.value = true

      try {
        // Send via WebSocket
        if (websocketService.connected && props.conversationId) {
          await websocketService.sendMessage(props.conversationId, content, 'TEXT')

          // Clear input
          messageText.value = ''

          // Stop typing indicator
          stopTyping()

          // Emit event
          emit('message-sent', {
            conversationId: props.conversationId,
            content,
            messageType: 'TEXT',
          })

          // Auto-resize textarea
          autoResizeTextarea()
        } else {
          throw new Error('WebSocket not connected')
        }
      } catch (error) {
        console.error('Error sending message:', error)

        // TODO: Add to retry queue
      } finally {
        sendingMessage.value = false
      }
    }

    // Send quick message
    const sendQuickMessage = async (content) => {
      messageText.value = content
      await sendMessage()
    }

    // Handle file selection
    const handleFileSelect = async (event) => {
      const file = event.target.files[0]
      if (!file) return

      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        alert('File quá lớn! Vui lòng chọn file nhỏ hơn 10MB.')
        return
      }

      uploadingFile.value = file
      uploadProgress.value = 0

      try {
        // Mock file upload progress
        const uploadInterval = setInterval(() => {
          uploadProgress.value += 10
          if (uploadProgress.value >= 100) {
            clearInterval(uploadInterval)
            completeFileUpload(file)
          }
        }, 200)
      } catch (error) {
        console.error('Error uploading file:', error)
        uploadingFile.value = null
        uploadProgress.value = 0
      }
    }

    // Complete file upload
    const completeFileUpload = async (file) => {
      try {
        // TODO: Replace with actual file upload
        const fileUrl = URL.createObjectURL(file)
        const messageType = file.type.startsWith('image/') ? 'IMAGE' : 'FILE'

        // Send file message
        if (websocketService.connected && props.conversationId) {
          await websocketService.sendMessage(props.conversationId, fileUrl, messageType)

          emit('message-sent', {
            conversationId: props.conversationId,
            content: fileUrl,
            messageType,
            metadata: {
              filename: file.name,
              size: file.size,
              type: file.type,
            },
          })
        }
      } catch (error) {
        console.error('Error sending file:', error)
      } finally {
        uploadingFile.value = null
        uploadProgress.value = 0

        // Clear file input
        if (fileInput.value) {
          fileInput.value.value = ''
        }
      }
    }

    // Handle typing
    const handleTyping = () => {
      if (!connected.value || !props.conversationId) return

      // Start typing indicator
      if (!isUserTyping) {
        isUserTyping = true
        websocketService.sendTypingIndicator(props.conversationId, true)
      }

      // Auto-resize textarea
      autoResizeTextarea()

      // Reset typing timer
      clearTimeout(typingTimer)
      typingTimer = setTimeout(() => {
        stopTyping()
      }, 2000)
    }

    // Stop typing indicator
    const stopTyping = () => {
      if (isUserTyping && connected.value && props.conversationId) {
        isUserTyping = false
        websocketService.sendTypingIndicator(props.conversationId, false)
      }
      clearTimeout(typingTimer)
    }

    // Handle key down
    const handleKeyDown = (event) => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        sendMessage()
      }
    }

    // Handle paste
    const handlePaste = (event) => {
      const items = event.clipboardData?.items
      if (!items) return

      for (const item of items) {
        if (item.type.startsWith('image/')) {
          event.preventDefault()
          const file = item.getAsFile()
          if (file) {
            // Handle pasted image
            handleFileSelect({ target: { files: [file] } })
          }
          break
        }
      }
    }

    // Auto-resize textarea
    const autoResizeTextarea = () => {
      if (messageInput.value) {
        messageInput.value.style.height = 'auto'
        messageInput.value.style.height = Math.min(messageInput.value.scrollHeight, 120) + 'px'
      }
    }

    // Mark messages as read
    const markAsRead = async () => {
      if (props.conversationId) {
        try {
          await chatAPI.markAsRead(props.conversationId)
          emit('unread-count-changed', 0)
        } catch (error) {
          console.error('Error marking as read:', error)
        }
      }
    }

    // Scroll to bottom
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // WebSocket event handlers
    const handleIncomingMessage = (messageData) => {
      // Add message to list
      messages.value.push({
        ...messageData,
        status: 'delivered',
      })

      // Mark as read if window is focused
      if (document.hasFocus()) {
        markAsRead()
      }

      // Scroll to bottom
      nextTick(() => scrollToBottom())
    }

    const handleTypingIndicator = (typingData) => {
      isTyping.value = typingData.isTyping

      if (typingData.isTyping) {
        // Auto-hide after 3 seconds
        setTimeout(() => {
          isTyping.value = false
        }, 3000)
      }
    }

    // UI event handlers
    const toggleProductInfo = () => {
      showProductInfo.value = !showProductInfo.value
    }

    const viewProduct = () => {
      if (props.productId) {
        router.push(`/products/${props.productId}`)
      }
    }

    const openFileSelector = () => {
      if (fileInput.value) {
        fileInput.value.click()
      }
    }

    const cancelFileUpload = () => {
      uploadingFile.value = null
      uploadProgress.value = 0
    }

    const toggleEmojiPicker = () => {
      showEmojiPicker.value = !showEmojiPicker.value
    }

    const closeEmojiPicker = () => {
      showEmojiPicker.value = false
    }

    const insertEmoji = (emoji) => {
      messageText.value += emoji
      closeEmojiPicker()
      messageInput.value?.focus()
    }

    const openImageModal = (imageUrl) => {
      modalImage.value = imageUrl
      showImageModal.value = true
    }

    const closeImageModal = () => {
      showImageModal.value = false
      modalImage.value = ''
    }

    const reconnect = async () => {
      try {
        const token = localStorage.getItem('token')
        if (token) {
          await websocketService.connect(authStore.user.id, token)
        }
      } catch (error) {
        console.error('Reconnection failed:', error)
      }
    }

    // Utility functions
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    }

    const formatDate = (date) => {
      const today = new Date()
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)

      if (date.toDateString() === today.toDateString()) {
        return 'Hôm nay'
      } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Hôm qua'
      } else {
        return date.toLocaleDateString('vi-VN')
      }
    }

    const formatLastSeen = (timestamp) => {
      const now = new Date()
      const lastSeen = new Date(timestamp)
      const diff = now - lastSeen
      const minutes = Math.floor(diff / 60000)

      if (minutes < 1) return 'vừa xong'
      if (minutes < 60) return `${minutes} phút trước`

      const hours = Math.floor(minutes / 60)
      if (hours < 24) return `${hours} giờ trước`

      const days = Math.floor(hours / 24)
      return `${days} ngày trước`
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const getMessageClass = (message) => {
      return {
        own: message.isOwn,
        other: !message.isOwn,
        system: message.messageType === 'SYSTEM',
        image: message.messageType === 'IMAGE',
        file: message.messageType === 'FILE',
      }
    }

    const getStatusClass = (status) => {
      return {
        sent: status === 'sent',
        delivered: status === 'delivered',
        read: status === 'read',
      }
    }

    const getStatusIcon = (status) => {
      switch (status) {
        case 'sent':
          return '✓'
        case 'delivered':
          return '✓✓'
        case 'read':
          return '✓✓'
        default:
          return '⏳'
      }
    }

    const getFileName = (url) => {
      return url.split('/').pop() || 'file'
    }

    const getFileSize = (size) => {
      return size ? formatFileSize(size) : ''
    }

    const downloadFile = (url) => {
      const link = document.createElement('a')
      link.href = url
      link.download = getFileName(url)
      link.click()
    }

    // Watch for connection status
    watch(
      () => websocketService.connected,
      (isConnected) => {
        connected.value = isConnected
      }
    )

    // Watch for conversation changes
    watch(
      () => props.conversationId,
      (newConversationId) => {
        if (newConversationId) {
          loadMessages()

          // Join conversation room
          websocketService.joinConversation(newConversationId)
        }
      }
    )

    // Lifecycle
    onMounted(() => {
      // Set initial connection status
      connected.value = websocketService.connected

      // Load messages if conversation exists
      if (props.conversationId) {
        loadMessages()
        websocketService.joinConversation(props.conversationId)
      }

      // Setup WebSocket event listeners
      const unsubscribeMessage = websocketService.onMessage(
        props.conversationId || '*',
        handleIncomingMessage
      )
      const unsubscribeTyping = websocketService.onTyping(
        props.conversationId || '*',
        handleTypingIndicator
      )

      // Focus input
      nextTick(() => {
        messageInput.value?.focus()
      })

      // Cleanup function
      return () => {
        unsubscribeMessage()
        unsubscribeTyping()
      }
    })

    onUnmounted(() => {
      // Leave conversation room
      if (props.conversationId) {
        websocketService.leaveConversation(props.conversationId)
      }

      // Stop typing
      stopTyping()
    })

    return {
      // State
      messages,
      messageText,
      loadingMessages,
      sendingMessage,
      connected,
      isTyping,
      showProductInfo,
      showEmojiPicker,
      showImageModal,
      modalImage,
      selectedEmojiCategory,
      uploadingFile,
      uploadProgress,
      quickStartMessages,
      emojiCategories,

      // Computed
      currentEmojis,
      canSendMessage,
      messagesWithSeparators,

      // Refs
      messagesContainer,
      messageInput,
      fileInput,

      // Methods
      sendMessage,
      sendQuickMessage,
      handleFileSelect,
      handleTyping,
      handleKeyDown,
      handlePaste,
      toggleProductInfo,
      viewProduct,
      openFileSelector,
      cancelFileUpload,
      toggleEmojiPicker,
      closeEmojiPicker,
      insertEmoji,
      openImageModal,
      closeImageModal,
      reconnect,

      // Utilities
      formatTime,
      formatDate,
      formatLastSeen,
      formatFileSize,
      getMessageClass,
      getStatusClass,
      getStatusIcon,
      getFileName,
      getFileSize,
      downloadFile,
    }
  },
}
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border: 1px solid #e2e8f0;
}

/* Chat Header */
.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.seller-avatar {
  position: relative;
}

.seller-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #6b7280;
  border: 2px solid white;
}

.online-status.online {
  background: #10b981;
}

.seller-details h3 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.25rem 0;
}

.status-text {
  font-size: 0.8rem;
  margin: 0;
  opacity: 0.9;
}

.online {
  color: #86efac;
}

.offline {
  color: #d1d5db;
}

.last-seen {
  opacity: 0.7;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.close-btn:hover {
  background: #ef4444;
}

/* Product Info */
.product-info {
  background: rgba(255, 255, 255, 0.1);
  padding: 0.75rem 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.product-summary {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.product-icon {
  font-size: 1rem;
}

.product-name {
  flex: 1;
  font-size: 0.9rem;
  font-weight: 500;
}

.view-product-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.view-product-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Messages Container */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: #f8fafc;
}

.loading-messages {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

.loading-spinner {
  font-size: 2rem;
  margin-bottom: 1rem;
}

/* Welcome Message */
.welcome-message {
  text-align: center;
  padding: 2rem 1rem;
  color: #6b7280;
}

.welcome-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.welcome-message h4 {
  color: #374151;
  margin-bottom: 0.5rem;
}

.welcome-message p {
  margin-bottom: 1.5rem;
}

.quick-start-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 300px;
  margin: 0 auto;
}

.quick-start-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.quick-start-btn:hover {
  background: #5a6fd8;
  transform: translateY(-2px);
}

/* Messages List */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.date-separator {
  text-align: center;
  margin: 1rem 0;
}

.date-text {
  background: #e5e7eb;
  color: #6b7280;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 500;
}

.message-wrapper {
  display: flex;
  justify-content: flex-start;
}

.message-wrapper.own-message {
  justify-content: flex-end;
}

.message {
  max-width: 70%;
  background: white;
  border-radius: 18px;
  padding: 0.75rem 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.message.own {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.system {
  background: #f3f4f6;
  color: #6b7280;
  font-style: italic;
  text-align: center;
  max-width: 80%;
  margin: 0 auto;
}

.message-content {
  margin-bottom: 0.5rem;
}

.text-content {
  word-wrap: break-word;
  white-space: pre-wrap;
}

.image-content img {
  max-width: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.image-content img:hover {
  transform: scale(1.05);
}

.file-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.file-icon {
  font-size: 1.5rem;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.file-size {
  font-size: 0.7rem;
  opacity: 0.7;
}

.download-btn {
  background: transparent;
  border: 1px solid currentColor;
  color: inherit;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.download-btn:hover {
  background: currentColor;
  color: white;
}

.system-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
}

.system-icon {
  font-size: 1rem;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.7rem;
  opacity: 0.7;
}

.message-time {
  font-size: 0.7rem;
}

.message-status {
  margin-left: 0.5rem;
}

.message-status.read {
  color: #10b981;
}

.message-status.delivered {
  color: #6b7280;
}

.message-status.sent {
  color: #9ca3af;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 18px;
  max-width: 70%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.typing-animation {
  display: flex;
  gap: 0.2rem;
}

.typing-animation span {
  width: 6px;
  height: 6px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-animation span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-animation span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.typing-text {
  font-size: 0.8rem;
  color: #6b7280;
}

/* Message Input */
.message-input-container {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 1rem;
}

.file-preview {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 0.75rem;
}

.preview-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cancel-upload {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.upload-progress {
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #667eea;
  transition: width 0.3s ease;
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
}

.attach-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #d1d5db;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.attach-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.text-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: flex-end;
  background: #f8fafc;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  padding: 0.5rem 3rem 0.5rem 1rem;
}

.message-input {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  outline: none;
  font-family: inherit;
  font-size: 0.9rem;
  line-height: 1.4;
  max-height: 120px;
  min-height: 20px;
}

.message-input::placeholder {
  color: #9ca3af;
}

.emoji-btn {
  position: absolute;
  right: 0.5rem;
  bottom: 0.5rem;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-btn:hover {
  background: #e5e7eb;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.send-btn.sending {
  background: #fbbf24;
}

.sending-icon {
  animation: spin 1s linear infinite;
}

.send-icon {
  font-size: 1rem;
}

/* Connection Warning */
.connection-warning {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: #fef3cd;
  border: 1px solid #f59e0b;
  border-radius: 6px;
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #92400e;
}

.warning-icon {
  font-size: 1rem;
}

.warning-text {
  flex: 1;
}

.reconnect-btn {
  background: transparent;
  border: 1px solid #f59e0b;
  color: #92400e;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reconnect-btn:hover {
  background: #f59e0b;
  color: white;
}

/* Emoji Picker */
.emoji-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.emoji-picker {
  background: white;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 300px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.emoji-categories {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  justify-content: center;
}

.emoji-category-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.emoji-category-btn:hover,
.emoji-category-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.5rem;
  max-height: 300px;
  overflow-y: auto;
}

.emoji-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.emoji-btn:hover {
  background: #f3f4f6;
  transform: scale(1.2);
}

/* Image Modal */
.image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.image-modal {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.image-modal img {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
}

.modal-close {
  position: absolute;
  top: -40px;
  right: 0;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Transitions */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
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
@media (max-width: 480px) {
  .chat-header {
    padding: 0.75rem;
  }

  .seller-info {
    gap: 0.5rem;
  }

  .seller-avatar img {
    width: 32px;
    height: 32px;
  }

  .seller-details h3 {
    font-size: 0.9rem;
  }

  .status-text {
    font-size: 0.7rem;
  }

  .messages-container {
    padding: 0.75rem;
  }

  .message {
    max-width: 85%;
    padding: 0.5rem 0.75rem;
  }

  .message-input-container {
    padding: 0.75rem;
  }

  .input-area {
    gap: 0.4rem;
  }

  .attach-btn,
  .send-btn {
    width: 36px;
    height: 36px;
  }

  .emoji-picker {
    width: 280px;
    margin: 1rem;
  }

  .emoji-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}
</style>
