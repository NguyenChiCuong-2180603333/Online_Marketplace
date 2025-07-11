<template>
  <div class="customer-chat-modal">
    <!-- Chat Header -->
    <div class="chat-header">
      <div class="customer-info">
        <div class="customer-avatar">
          {{ order.customerName.charAt(0).toUpperCase() }}
        </div>
        <div class="customer-details">
          <h3>{{ order.customerName }}</h3>
          <p class="customer-status">
            <span class="status-dot" :class="customerStatus"></span>
            {{ getCustomerStatusText() }}
          </p>
          <p class="order-info">Đơn hàng #{{ order.id.slice(-8) }}</p>
        </div>
      </div>
      
      <div class="chat-actions">
        <button @click="toggleCustomerInfo" class="action-btn info-btn">
          <span class="icon">ℹ️</span>
          Chi tiết
        </button>
        <button @click="$emit('close')" class="action-btn close-btn">
          <span class="icon">✕</span>
        </button>
      </div>
    </div>

    <!-- Customer Info Panel (Collapsible) -->
    <div v-if="showCustomerInfo" class="customer-info-panel">
      <div class="info-grid">
        <div class="info-item">
          <span class="label">Email:</span>
          <span class="value">{{ order.customerEmail }}</span>
          <button @click="copyToClipboard(order.customerEmail)" class="copy-btn">📋</button>
        </div>
        <div class="info-item">
          <span class="label">Điện thoại:</span>
          <span class="value">{{ order.customerPhone }}</span>
          <button @click="copyToClipboard(order.customerPhone)" class="copy-btn">📋</button>
        </div>
        <div class="info-item">
          <span class="label">Địa chỉ:</span>
          <span class="value">{{ order.shippingAddress }}</span>
          <button @click="copyToClipboard(order.shippingAddress)" class="copy-btn">📋</button>
        </div>
        <div class="info-item">
          <span class="label">Giá trị đơn:</span>
          <span class="value highlight">{{ formatCurrency(order.totalAmount) }}</span>
        </div>
        <div class="info-item">
          <span class="label">Trạng thái:</span>
          <span class="value">
            <span class="status-badge" :style="{ backgroundColor: getStatusColor(order.status) }">
              {{ getStatusLabel(order.status) }}
            </span>
          </span>
        </div>
        <div class="info-item">
          <span class="label">Ngày đặt:</span>
          <span class="value">{{ formatDate(order.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- Chat Messages -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loading" class="loading-messages">
        <div class="spinner"></div>
        <p>Đang tải tin nhắn...</p>
      </div>
      
      <div v-else-if="messages.length === 0" class="empty-messages">
        <div class="empty-icon">💬</div>
        <h4>Bắt đầu cuộc trò chuyện</h4>
        <p>Gửi tin nhắn đầu tiên để liên hệ với khách hàng</p>
      </div>
      
      <div v-else class="messages-list">
        <!-- Messages -->
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="message-wrapper"
          :class="{ 'seller-message': message.sender === 'seller' }"
        >
          <div class="message-bubble">
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-meta">
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              <span v-if="message.sender === 'seller'" class="message-status">
                <span v-if="message.status === 'sent'" class="status-icon">✓</span>
                <span v-else-if="message.status === 'delivered'" class="status-icon">✓✓</span>
                <span v-else-if="message.status === 'read'" class="status-icon read">✓✓</span>
              </span>
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="customerTyping" class="typing-indicator">
          <div class="typing-bubble">
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
          <span class="typing-text">{{ order.customerName }} đang nhập...</span>
        </div>
      </div>
    </div>

    <!-- Quick Replies -->
    <div class="quick-replies">
      <h4>📝 Phản hồi nhanh</h4>
      <div class="replies-grid">
        <button 
          v-for="reply in quickReplies" 
          :key="reply.id"
          @click="useQuickReply(reply.content)"
          class="quick-reply-btn"
          :title="reply.description"
        >
          {{ reply.name }}
        </button>
      </div>
    </div>

    <!-- Message Input -->
    <div class="message-input-section">
      <div class="input-wrapper">
        <div class="input-tools">
          <button @click="toggleEmoji" class="tool-btn" title="Emoji">
            😊
          </button>
          <button @click="attachFile" class="tool-btn" title="Đính kèm file">
            📎
          </button>
          <button @click="toggleTemplates" class="tool-btn" title="Mẫu tin nhắn">
            📋
          </button>
        </div>
        
        <div class="input-container">
          <textarea 
            v-model="newMessage"
            @keydown="handleKeyDown"
            @input="handleTyping"
            placeholder="Nhập tin nhắn..."
            rows="3"
            class="message-input"
            ref="messageInput"
          ></textarea>
          
          <!-- Character Counter -->
          <div class="character-counter" :class="{ warning: newMessage.length > 1000 }">
            {{ newMessage.length }}/1200
          </div>
        </div>
        
        <button 
          @click="sendMessage"
          :disabled="!canSendMessage"
          class="send-btn"
          :class="{ 'sending': sendingMessage }"
        >
          <span v-if="sendingMessage" class="sending-icon">⏳</span>
          <span v-else class="send-icon">📤</span>
        </button>
      </div>

      <!-- Emoji Picker -->
      <div v-if="showEmojiPicker" class="emoji-picker">
        <div class="emoji-grid">
          <button 
            v-for="emoji in commonEmojis" 
            :key="emoji"
            @click="addEmoji(emoji)"
            class="emoji-btn"
          >
            {{ emoji }}
          </button>
        </div>
      </div>

      <!-- Message Templates -->
      <div v-if="showTemplates" class="message-templates">
        <h5>Mẫu tin nhắn thường dùng</h5>
        <div class="templates-list">
          <div 
            v-for="template in messageTemplates" 
            :key="template.id"
            class="template-item"
            @click="useTemplate(template.content)"
          >
            <div class="template-name">{{ template.name }}</div>
            <div class="template-preview">{{ template.content.substring(0, 60) }}...</div>
          </div>
        </div>
      </div>
    </div>

    <!-- File Upload Modal -->
    <div v-if="showFileUpload" class="file-upload-overlay" @click="showFileUpload = false">
      <div class="file-upload-modal" @click.stop>
        <h3>📎 Đính kèm file</h3>
        <div class="upload-area" @drop="handleFileDrop" @dragover.prevent>
          <input 
            type="file" 
            ref="fileInput" 
            @change="handleFileSelect"
            accept="image/*,.pdf,.doc,.docx,.txt"
            multiple
            style="display: none"
          />
          <div class="upload-content">
            <div class="upload-icon">📁</div>
            <p>Kéo thả file vào đây hoặc <button @click="$refs.fileInput.click()" class="browse-btn">chọn file</button></p>
            <small>Hỗ trợ: Hình ảnh, PDF, Word, Text (Max 10MB)</small>
          </div>
        </div>
        <div v-if="selectedFiles.length > 0" class="selected-files">
          <h4>File đã chọn:</h4>
          <div 
            v-for="(file, index) in selectedFiles" 
            :key="index"
            class="file-item"
          >
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
            <button @click="removeFile(index)" class="remove-file">✕</button>
          </div>
        </div>
        <div class="upload-actions">
          <button @click="showFileUpload = false" class="btn-cancel">Hủy</button>
          <button 
            @click="uploadFiles" 
            :disabled="selectedFiles.length === 0 || uploadingFiles"
            class="btn-upload"
          >
            {{ uploadingFiles ? 'Đang tải...' : `Gửi (${selectedFiles.length})` }}
          </button>
        </div>
      </div>
    </div>

    <!-- Chat Settings -->
    <div class="chat-settings">
      <div class="settings-row">
        <label class="setting-item">
          <input type="checkbox" v-model="autoRefresh" />
          <span>Tự động làm mới tin nhắn</span>
        </label>
        <label class="setting-item">
          <input type="checkbox" v-model="soundNotifications" />
          <span>Âm thanh thông báo</span>
        </label>
        <button @click="markAllAsRead" class="btn-mark-read">
          Đánh dấu đã đọc
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useSellerStore } from '@/stores/seller'

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'message-sent'])

const sellerStore = useSellerStore()

// Reactive data
const loading = ref(true)
const messages = ref([])
const newMessage = ref('')
const sendingMessage = ref(false)
const showCustomerInfo = ref(false)
const showEmojiPicker = ref(false)
const showTemplates = ref(false)
const showFileUpload = ref(false)
const customerTyping = ref(false)
const customerStatus = ref('online') // online, offline, away
const autoRefresh = ref(true)
const soundNotifications = ref(true)
const selectedFiles = ref([])
const uploadingFiles = ref(false)

// Refs
const messagesContainer = ref(null)
const messageInput = ref(null)

// Quick replies
const quickReplies = ref([
  { id: 1, name: '👋 Chào', content: 'Xin chào! Cảm ơn bạn đã liên hệ. Tôi có thể giúp gì cho bạn?', description: 'Lời chào thân thiện' },
  { id: 2, name: '✅ Xác nhận', content: 'Đã xác nhận đơn hàng của bạn. Chúng tôi sẽ xử lý trong thời gian sớm nhất.', description: 'Xác nhận đơn hàng' },
  { id: 3, name: '🚚 Giao hàng', content: 'Đơn hàng của bạn đã được gửi đi. Mã vận đơn sẽ được cập nhật sớm nhất.', description: 'Thông báo giao hàng' },
  { id: 4, name: '❓ Hỗ trợ', content: 'Bạn có thắc mắc gì về đơn hàng không? Tôi sẵn sàng hỗ trợ bạn.', description: 'Đề nghị hỗ trợ' },
  { id: 5, name: '🙏 Cảm ơn', content: 'Cảm ơn bạn đã mua hàng! Hy vọng bạn hài lòng với sản phẩm.', description: 'Lời cảm ơn' }
])

// Message templates
const messageTemplates = ref([
  {
    id: 1,
    name: 'Chào hỏi khách hàng',
    content: 'Xin chào bạn! Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi. Tôi là [Tên], người phụ trách đơn hàng #' + props.order.id.slice(-8) + '. Có điều gì tôi có thể hỗ trợ bạn không?'
  },
  {
    id: 2,
    name: 'Xác nhận đơn hàng',
    content: 'Đơn hàng #' + props.order.id.slice(-8) + ' của bạn đã được xác nhận thành công. Thời gian xử lý dự kiến là 1-2 ngày làm việc. Chúng tôi sẽ thông báo ngay khi đơn hàng được gửi đi.'
  },
  {
    id: 3,
    name: 'Thông báo đóng gói',
    content: 'Đơn hàng của bạn đang được đóng gói cẩn thận. Chúng tôi cam kết sản phẩm sẽ được bảo vệ tốt nhất trong quá trình vận chuyển. Dự kiến sẽ gửi hàng trong hôm nay.'
  },
  {
    id: 4,
    name: 'Thông báo giao hàng',
    content: 'Tuyệt vời! Đơn hàng #' + props.order.id.slice(-8) + ' đã được gửi đi. Mã vận đơn: [MÃ_VẬN_ĐƠN]. Bạn có thể theo dõi tình trạng giao hàng qua link tracking. Dự kiến giao hàng trong 2-3 ngày làm việc.'
  },
  {
    id: 5,
    name: 'Hỏi phản hồi',
    content: 'Bạn đã nhận được đơn hàng chưa? Chúng tôi rất mong nhận được phản hồi của bạn về chất lượng sản phẩm và dịch vụ. Nếu có bất kỳ vấn đề gì, xin hãy liên hệ ngay để chúng tôi hỗ trợ.'
  },
  {
    id: 6,
    name: 'Xử lý khiếu nại',
    content: 'Chúng tôi rất tiếc khi biết bạn gặp vấn đề với đơn hàng. Để hỗ trợ bạn một cách tốt nhất, vui lòng cung cấp thêm thông tin chi tiết về vấn đề. Chúng tôi cam kết sẽ giải quyết nhanh chóng và thỏa đáng.'
  }
])

// Common emojis
const commonEmojis = ['😊', '😍', '👍', '👏', '🙏', '❤️', '🎉', '✅', '🚚', '📦', '💯', '🔥', '⭐', '💪', '👌', '🙂', '😉', '🤝', '🎯', '💡']

// Computed
const canSendMessage = computed(() => {
  return newMessage.value.trim().length > 0 && 
         newMessage.value.length <= 1200 && 
         !sendingMessage.value
})

// Methods
const loadMessages = async () => {
  try {
    loading.value = true
    const orderMessages = await sellerStore.loadOrderMessages(props.order.id)
    messages.value = orderMessages || generateSampleMessages()
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Error loading messages:', error)
    messages.value = generateSampleMessages()
  } finally {
    loading.value = false
  }
}

const generateSampleMessages = () => {
  return [
    {
      id: 1,
      sender: 'customer',
      content: 'Xin chào, tôi muốn hỏi về đơn hàng #' + props.order.id.slice(-8),
      timestamp: new Date(Date.now() - 3600000).toISOString(),
      status: 'read'
    },
    {
      id: 2,
      sender: 'seller',
      content: 'Xin chào bạn! Cảm ơn bạn đã liên hệ. Đơn hàng của bạn đang được xử lý. Có điều gì tôi có thể hỗ trợ không?',
      timestamp: new Date(Date.now() - 3500000).toISOString(),
      status: 'read'
    },
    {
      id: 3,
      sender: 'customer',
      content: 'Khi nào thì đơn hàng sẽ được giao? Tôi cần gấp lắm.',
      timestamp: new Date(Date.now() - 1800000).toISOString(),
      status: 'read'
    }
  ]
}

const sendMessage = async () => {
  if (!canSendMessage.value) return
  
  const messageContent = newMessage.value.trim()
  const tempMessage = {
    id: Date.now(),
    sender: 'seller',
    content: messageContent,
    timestamp: new Date().toISOString(),
    status: 'sending'
  }
  
  messages.value.push(tempMessage)
  newMessage.value = ''
  await nextTick()
  scrollToBottom()
  
  try {
    sendingMessage.value = true
    const sentMessage = await sellerStore.sendOrderMessage(props.order.id, messageContent)
    
    const messageIndex = messages.value.findIndex(m => m.id === tempMessage.id)
    if (messageIndex !== -1) {
      messages.value[messageIndex] = {
        ...sentMessage,
        status: 'sent'
      }
    }
    
    emit('message-sent', props.order.id, sentMessage)
    
    if (soundNotifications.value) {
      playNotificationSound()
    }
    
    setTimeout(() => {
      if (messageIndex !== -1) {
        messages.value[messageIndex].status = 'delivered'
      }
    }, 2000)
    
  } catch (error) {
    messages.value = messages.value.filter(m => m.id !== tempMessage.id)
    alert('Có lỗi xảy ra khi gửi tin nhắn')
  } finally {
    sendingMessage.value = false
  }
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleTyping = () => {
}

const useQuickReply = (content) => {
  newMessage.value = content
  messageInput.value?.focus()
}

const useTemplate = (content) => {
  newMessage.value = content
  showTemplates.value = false
  messageInput.value?.focus()
}

const toggleCustomerInfo = () => {
  showCustomerInfo.value = !showCustomerInfo.value
}

const toggleEmoji = () => {
  showEmojiPicker.value = !showEmojiPicker.value
  showTemplates.value = false
}

const toggleTemplates = () => {
  showTemplates.value = !showTemplates.value
  showEmojiPicker.value = false
}

const addEmoji = (emoji) => {
  newMessage.value += emoji
  messageInput.value?.focus()
}

const attachFile = () => {
  showFileUpload.value = true
}

const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  selectedFiles.value = [...selectedFiles.value, ...files]
}

const handleFileDrop = (event) => {
  event.preventDefault()
  const files = Array.from(event.dataTransfer.files)
  selectedFiles.value = [...selectedFiles.value, ...files]
}

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return
  
  try {
    uploadingFiles.value = true
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const fileMessage = {
      id: Date.now(),
      sender: 'seller',
      content: `Đã gửi ${selectedFiles.value.length} file đính kèm`,
      timestamp: new Date().toISOString(),
      status: 'sent',
      files: selectedFiles.value.map(file => ({
        name: file.name,
        size: file.size,
        type: file.type,
        url: URL.createObjectURL(file) 
      }))
    }
    
    messages.value.push(fileMessage)
    selectedFiles.value = []
    showFileUpload.value = false
    
    await nextTick()
    scrollToBottom()
    
  } catch (error) {
    alert('Có lỗi xảy ra khi tải file')
  } finally {
    uploadingFiles.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const markAllAsRead = () => {
  messages.value.forEach(message => {
    if (message.sender === 'customer') {
      message.status = 'read'
    }
  })
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    const notification = document.createElement('div')
    notification.textContent = 'Đã copy vào clipboard'
    notification.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      background: #10b981;
      color: white;
      padding: 10px 20px;
      border-radius: 6px;
      z-index: 10000;
      font-size: 14px;
    `
    document.body.appendChild(notification)
    setTimeout(() => notification.remove(), 2000)
  } catch (error) {
    alert('Không thể copy vào clipboard')
  }
}

const playNotificationSound = () => {
  const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+Tv0Xg...')
  audio.volume = 0.3
  audio.play().catch(() => {}) 
}

const getCustomerStatusText = () => {
  switch (customerStatus.value) {
    case 'online': return 'Đang hoạt động'
    case 'away': return 'Vắng mặt'
    case 'offline': return 'Không hoạt động'
    default: return 'Không xác định'
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) { 
    return 'Vừa xong'
  } else if (diff < 3600000) { 
    return `${Math.floor(diff / 60000)} phút trước`
  } else if (diff < 86400000) { 
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getStatusLabel = (status) => {
  return sellerStore.getStatusLabel(status)
}

const getStatusColor = (status) => {
  return sellerStore.getStatusColor(status)
}

let refreshInterval = null

const startAutoRefresh = () => {
  if (!autoRefresh.value) return
  
  refreshInterval = setInterval(async () => {
    if (document.visibilityState === 'visible') {
      try {
        const newMessages = await sellerStore.loadOrderMessages(props.order.id)
        if (newMessages && newMessages.length > messages.value.length) {
          messages.value = newMessages
          await nextTick()
          scrollToBottom()
          
          if (soundNotifications.value) {
            playNotificationSound()
          }
        }
      } catch (error) {
        console.error('Error auto-refreshing messages:', error)
      }
    }
  }, 5000) 
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}


watch(autoRefresh, (newValue) => {
  if (newValue) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
})

onMounted(async () => {
  await loadMessages()
  startAutoRefresh()
  
  setTimeout(() => customerStatus.value = 'away', 30000)
  setTimeout(() => customerTyping.value = true, 10000)
  setTimeout(() => customerTyping.value = false, 13000)
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.customer-chat-modal {
  width: 100%;
  max-width: 600px;
  height: 80vh;
  max-height: 800px;
  display: flex;
  flex-direction: column;
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  color: #ffffff;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  background: rgba(16, 16, 24, 0.8);
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.customer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  color: white;
  flex-shrink: 0;
}

.customer-details h3 {
  margin: 0 0 0.25rem 0;
  color: #ffffff;
  font-size: 1.1rem;
}

.customer-status {
  margin: 0 0 0.25rem 0;
  color: #a0aec0;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6b7280;
}

.status-dot.online { background: #10b981; }
.status-dot.away { background: #f59e0b; }
.status-dot.offline { background: #6b7280; }

.order-info {
  margin: 0;
  color: #00d4ff;
  font-size: 0.875rem;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(107, 114, 128, 0.5);
  color: #ffffff;
}

.action-btn.close-btn:hover {
  background: rgba(239, 68, 68, 0.3);
  border-color: rgba(239, 68, 68, 0.5);
  color: #ef4444;
}

.customer-info-panel {
  padding: 1rem 1.5rem;
  background: rgba(16, 16, 24, 0.6);
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 0.75rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 6px;
}

.info-item .label {
  color: #a0aec0;
  font-size: 0.875rem;
  min-width: 80px;
}

.info-item .value {
  flex: 1;
  color: #e2e8f0;
  font-size: 0.875rem;
}

.info-item .value.highlight {
  color: #00d4ff;
  font-weight: 500;
}

.copy-btn {
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  font-size: 0.875rem;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.1);
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  color: white;
  font-size: 0.75rem;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: rgba(16, 16, 24, 0.4);
}

.loading-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #a0aec0;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(0, 212, 255, 0.2);
  border-top: 3px solid #00d4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  text-align: center;
  color: #a0aec0;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-messages h4 {
  margin: 0 0 0.5rem 0;
  color: #e2e8f0;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-wrapper {
  display: flex;
  justify-content: flex-start;
}

.message-wrapper.seller-message {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 18px;
  position: relative;
}

.message-wrapper:not(.seller-message) .message-bubble {
  background: rgba(107, 114, 128, 0.3);
  color: #e2e8f0;
  border-bottom-left-radius: 6px;
}

.message-wrapper.seller-message .message-bubble {
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  color: #000000;
  border-bottom-right-radius: 6px;
}

.message-content {
  margin-bottom: 0.5rem;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  opacity: 0.7;
}

.message-status .status-icon {
  margin-left: 0.25rem;
}

.message-status .status-icon.read {
  color: #10b981;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.typing-bubble {
  background: rgba(107, 114, 128, 0.3);
  padding: 0.75rem 1rem;
  border-radius: 18px;
  border-bottom-left-radius: 6px;
}

.typing-dots {
  display: flex;
  gap: 0.25rem;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: #a0aec0;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(1); }
  30% { opacity: 1; transform: scale(1.2); }
}

.typing-text {
  font-size: 0.875rem;
  color: #a0aec0;
  font-style: italic;
}

.quick-replies {
  padding: 1rem 1.5rem;
  background: rgba(16, 16, 24, 0.6);
  border-top: 1px solid rgba(0, 212, 255, 0.1);
}

.quick-replies h4 {
  margin: 0 0 0.75rem 0;
  color: #e2e8f0;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.replies-grid {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.quick-reply-btn {
  padding: 0.5rem 0.75rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  color: #00d4ff;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.quick-reply-btn:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-1px);
}

.message-input-section {
  padding: 1rem 1.5rem;
  background: rgba(26, 26, 46, 0.8);
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
}

.input-tools {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tool-btn {
  width: 36px;
  height: 36px;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 1rem;
}

.tool-btn:hover {
  background: rgba(107, 114, 128, 0.5);
  color: #ffffff;
}

.input-container {
  flex: 1;
  position: relative;
}

.message-input {
  width: 100%;
  padding: 0.75rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #ffffff;
  resize: none;
  font-family: inherit;
  font-size: 0.875rem;
  line-height: 1.4;
}

.message-input:focus {
  outline: none;
  border-color: #00d4ff;
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

.character-counter {
  position: absolute;
  bottom: 0.5rem;
  right: 0.5rem;
  font-size: 0.75rem;
  color: #a0aec0;
  background: rgba(26, 26, 46, 0.8);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.character-counter.warning {
  color: #f59e0b;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  border: none;
  border-radius: 50%;
  color: #000000;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 1.2rem;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.send-btn.sending {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.emoji-picker {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(16, 16, 24, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 0.5rem;
}

.emoji-btn {
  width: 40px;
  height: 40px;
  background: none;
  border: 1px solid rgba(107, 114, 128, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.3);
  transform: scale(1.1);
}

.message-templates {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(16, 16, 24, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  max-height: 200px;
  overflow-y: auto;
}

.message-templates h5 {
  margin: 0 0 0.75rem 0;
  color: #e2e8f0;
  font-size: 0.875rem;
}

.templates-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.template-item {
  padding: 0.75rem;
  background: rgba(26, 26, 46, 0.5);
  border: 1px solid rgba(107, 114, 128, 0.3);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.template-item:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.3);
}

.template-name {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.25rem;
}

.template-preview {
  font-size: 0.875rem;
  color: #a0aec0;
  line-height: 1.3;
}

.file-upload-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.file-upload-modal {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  color: #ffffff;
}

.file-upload-modal h3 {
  margin: 0 0 1rem 0;
  color: #00d4ff;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: rgba(0, 212, 255, 0.5);
  background: rgba(0, 212, 255, 0.05);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.browse-btn {
  background: none;
  border: none;
  color: #00d4ff;
  cursor: pointer;
  text-decoration: underline;
}

.selected-files {
  margin-top: 1rem;
}

.selected-files h4 {
  margin: 0 0 0.5rem 0;
  color: #e2e8f0;
  font-size: 1rem;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem;
  background: rgba(16, 16, 24, 0.6);
  border-radius: 6px;
  margin-bottom: 0.5rem;
}

.file-name {
  flex: 1;
  color: #e2e8f0;
  font-size: 0.875rem;
}

.file-size {
  color: #a0aec0;
  font-size: 0.75rem;
  margin-right: 0.5rem;
}

.remove-file {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
}

.remove-file:hover {
  background: rgba(239, 68, 68, 0.1);
}

.upload-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-cancel {
  padding: 0.75rem 1rem;
  background: rgba(107, 114, 128, 0.5);
  border: 1px solid rgba(107, 114, 128, 0.7);
  border-radius: 6px;
  color: #ffffff;
  cursor: pointer;
}

.btn-upload {
  padding: 0.75rem 1rem;
  background: #00d4ff;
  border: none;
  border-radius: 6px;
  color: #000000;
  cursor: pointer;
  font-weight: 500;
}

.btn-upload:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-settings {
  padding: 0.75rem 1.5rem;
  background: rgba(16, 16, 24, 0.6);
  border-top: 1px solid rgba(0, 212, 255, 0.1);
}

.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #a0aec0;
  font-size: 0.875rem;
  cursor: pointer;
}

.setting-item input[type="checkbox"] {
  accent-color: #00d4ff;
}

.btn-mark-read {
  padding: 0.5rem 1rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #00d4ff;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.btn-mark-read:hover {
  background: rgba(0, 212, 255, 0.2);
}

/* Responsive Design */
@media (max-width: 768px) {
  .customer-chat-modal {
    width: 95%;
    height: 90vh;
  }
  
  .chat-header {
    padding: 1rem;
  }
  
  .customer-info {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .input-wrapper {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .input-tools {
    flex-direction: row;
    justify-content: center;
  }
  
  .replies-grid {
    justify-content: center;
  }
  
  .settings-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .emoji-grid {
    grid-template-columns: repeat(auto-fill, minmax(35px, 1fr));
  }
}
</style>