<template>
  <div class="seller-chat">
    <div class="page-header">
      <h1>Tin nhắn khách hàng</h1>
      <div class="header-actions">
        <button @click="markAllAsRead" class="btn btn-outline">✅ Đánh dấu tất cả đã đọc</button>
        <button @click="refreshChats" class="btn btn-outline">🔄 Làm mới</button>
      </div>
    </div>

    <!-- Chat Statistics -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">💬</div>
        <div class="stat-content">
          <h3>{{ totalConversations }}</h3>
          <p>Cuộc hội thoại</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📨</div>
        <div class="stat-content">
          <h3>{{ unreadMessages }}</h3>
          <p>Tin nhắn chưa đọc</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <h3>{{ avgResponseTime }}</h3>
          <p>Thời gian phản hồi TB (phút)</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-content">
          <h3>{{ responseRate }}%</h3>
          <p>Tỷ lệ phản hồi</p>
        </div>
      </div>
    </div>

    <!-- Chat Interface -->
    <div class="chat-container">
      <!-- Conversations List -->
      <div class="conversations-sidebar">
        <div class="sidebar-header">
          <h3>Cuộc hội thoại</h3>
          <div class="search-box">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm kiếm khách hàng..."
              class="form-input"
            />
          </div>
        </div>

        <div class="conversations-list">
          <div
            v-for="conversation in filteredConversations"
            :key="conversation.id"
            @click="selectConversation(conversation)"
            :class="['conversation-item', { active: selectedConversation?.id === conversation.id }]"
          >
            <div class="conversation-avatar">
              <img
                :src="conversation.customer.avatar || '/default-avatar.png'"
                :alt="conversation.customer.name"
                @error="handleImageError"
              />
              <div v-if="conversation.unreadCount > 0" class="unread-badge">
                {{ conversation.unreadCount }}
              </div>
            </div>

            <div class="conversation-info">
              <div class="conversation-header">
                <h4>{{ conversation.customer.name }}</h4>
                <span class="conversation-time">
                  {{ formatTime(conversation.lastMessage?.createdAt) }}
                </span>
              </div>

              <div class="conversation-preview">
                <span
                  v-if="conversation.lastMessage?.senderId === currentUserId"
                  class="sent-indicator"
                >
                  ✓
                </span>
                {{ truncateText(conversation.lastMessage?.content || 'Chưa có tin nhắn', 40) }}
              </div>

              <div class="conversation-meta">
                <span v-if="conversation.customer.isOnline" class="online-indicator">
                  ● Trực tuyến
                </span>
                <span v-else class="last-seen">
                  {{ formatLastSeen(conversation.customer.lastSeen) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Chat Messages -->
      <div class="chat-messages">
        <div v-if="selectedConversation" class="chat-header">
          <div class="chat-customer-info">
            <img
              :src="selectedConversation.customer.avatar || '/default-avatar.png'"
              :alt="selectedConversation.customer.name"
              class="customer-avatar"
              @error="handleImageError"
            />
            <div class="customer-details">
              <h3>{{ selectedConversation.customer.name }}</h3>
              <span v-if="selectedConversation.customer.isOnline" class="online-status">
                ● Trực tuyến
              </span>
              <span v-else class="offline-status">
                {{ formatLastSeen(selectedConversation.customer.lastSeen) }}
              </span>
            </div>
          </div>

          <div class="chat-actions">
            <button @click="viewCustomerProfile" class="btn btn-sm btn-outline">👤 Hồ sơ</button>
            <button @click="viewCustomerOrders" class="btn btn-sm btn-outline">📦 Đơn hàng</button>
          </div>
        </div>

        <div v-else class="no-conversation">
          <div class="no-conversation-icon">💬</div>
          <h3>Chọn cuộc hội thoại</h3>
          <p>Chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu chat</p>
        </div>

        <div v-if="selectedConversation" class="messages-container" ref="messagesContainer">
          <div
            v-for="message in selectedConversation.messages"
            :key="message.id"
            :class="['message', message.senderId === currentUserId ? 'sent' : 'received']"
          >
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">
                {{ formatTime(message.createdAt) }}
                <span v-if="message.senderId === currentUserId" class="message-status">
                  {{ message.read ? '✓✓' : '✓' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="selectedConversation" class="message-input">
          <div class="input-container">
            <textarea
              v-model="newMessage"
              @keydown.enter.prevent="sendMessage"
              placeholder="Nhập tin nhắn..."
              class="message-textarea"
              rows="1"
            ></textarea>
            <button @click="sendMessage" class="send-button" :disabled="!newMessage.trim()">
              📤
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Profile Modal -->
    <div v-if="showCustomerModal" class="modal-overlay" @click="showCustomerModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Hồ sơ khách hàng</h2>
          <button @click="showCustomerModal = false" class="btn btn-icon">✕</button>
        </div>

        <div class="modal-body" v-if="selectedCustomer">
          <div class="customer-profile">
            <div class="profile-header">
              <img
                :src="selectedCustomer.avatar || '/default-avatar.png'"
                :alt="selectedCustomer.name"
                class="profile-avatar"
                @error="handleImageError"
              />
              <div class="profile-info">
                <h3>{{ selectedCustomer.name }}</h3>
                <p>{{ selectedCustomer.email }}</p>
                <p>Thành viên từ: {{ formatDate(selectedCustomer.createdAt) }}</p>
              </div>
            </div>

            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-label">Tổng đơn hàng:</span>
                <span class="stat-value">{{ selectedCustomer.totalOrders }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Tổng chi tiêu:</span>
                <span class="stat-value">{{ formatPrice(selectedCustomer.totalSpent) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Đánh giá trung bình:</span>
                <span class="stat-value">{{ selectedCustomer.averageRating }}/5</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Orders Modal -->
    <div v-if="showOrdersModal" class="modal-overlay" @click="showOrdersModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Đơn hàng của khách hàng</h2>
          <button @click="showOrdersModal = false" class="btn btn-icon">✕</button>
        </div>

        <div class="modal-body">
          <div v-if="customerOrders.length > 0" class="orders-list">
            <div v-for="order in customerOrders" :key="order.id" class="order-item">
              <div class="order-header">
                <span class="order-id">#{{ order.id }}</span>
                <span :class="['order-status', `status-${order.status}`]">
                  {{ getOrderStatusLabel(order.status) }}
                </span>
              </div>

              <div class="order-details">
                <p>{{ order.items.length }} sản phẩm</p>
                <p>{{ formatPrice(order.totalAmount) }}</p>
                <p>{{ formatDate(order.createdAt) }}</p>
              </div>
            </div>
          </div>

          <div v-else class="no-orders">
            <p>Khách hàng chưa có đơn hàng nào</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { api } from '@/services/api'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'SellerChat',
  setup() {
    const authStore = useAuthStore()

    // Reactive data
    const conversations = ref([])
    const selectedConversation = ref(null)
    const searchQuery = ref('')
    const newMessage = ref('')
    const loading = ref(false)

    // Modal states
    const showCustomerModal = ref(false)
    const showOrdersModal = ref(false)
    const selectedCustomer = ref(null)
    const customerOrders = ref([])

    // Refs
    const messagesContainer = ref(null)

    // Computed
    const currentUserId = computed(() => authStore.user?.id)

    const filteredConversations = computed(() => {
      if (!searchQuery.value) return conversations.value

      return conversations.value.filter(
        (conversation) =>
          conversation.customer.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          conversation.customer.email.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
    })

    const totalConversations = computed(() => conversations.value.length)
    const unreadMessages = computed(() =>
      conversations.value.reduce((sum, conv) => sum + conv.unreadCount, 0)
    )
    const avgResponseTime = computed(() => {
      // Calculate average response time logic
      return 15 // Placeholder
    })
    const responseRate = computed(() => {
      // Calculate response rate logic
      return 95 // Placeholder
    })

    // Methods
    const loadConversations = async () => {
      loading.value = true
      try {
        const response = await api.get('/seller/conversations')
        conversations.value = response.data.conversations
      } catch (error) {
        console.error('Error loading conversations:', error)
      } finally {
        loading.value = false
      }
    }

    const selectConversation = async (conversation) => {
      selectedConversation.value = conversation

      // Mark messages as read
      if (conversation.unreadCount > 0) {
        try {
          await api.post(`/seller/conversations/${conversation.id}/mark-read`)
          conversation.unreadCount = 0
        } catch (error) {
          console.error('Error marking messages as read:', error)
        }
      }

      // Scroll to bottom of messages
      await nextTick()
      scrollToBottom()
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim() || !selectedConversation.value) return

      const messageContent = newMessage.value.trim()
      newMessage.value = ''

      // Optimistic update
      const tempMessage = {
        id: Date.now(),
        content: messageContent,
        senderId: currentUserId.value,
        createdAt: new Date().toISOString(),
        read: false,
      }

      selectedConversation.value.messages.push(tempMessage)
      selectedConversation.value.lastMessage = tempMessage

      // Scroll to bottom
      await nextTick()
      scrollToBottom()

      try {
        const response = await api.post(
          `/seller/conversations/${selectedConversation.value.id}/messages`,
          {
            content: messageContent,
          }
        )

        // Replace temp message with real one
        const messageIndex = selectedConversation.value.messages.findIndex(
          (m) => m.id === tempMessage.id
        )
        if (messageIndex !== -1) {
          selectedConversation.value.messages[messageIndex] = response.data.message
        }
      } catch (error) {
        console.error('Error sending message:', error)
        // Remove temp message on error
        selectedConversation.value.messages = selectedConversation.value.messages.filter(
          (m) => m.id !== tempMessage.id
        )
      }
    }

    const markAllAsRead = async () => {
      try {
        await api.post('/seller/conversations/mark-all-read')
        conversations.value.forEach((conv) => (conv.unreadCount = 0))
      } catch (error) {
        console.error('Error marking all as read:', error)
      }
    }

    const refreshChats = () => {
      loadConversations()
    }

    const viewCustomerProfile = async () => {
      if (!selectedConversation.value) return

      try {
        const response = await api.get(
          `/seller/customers/${selectedConversation.value.customer.id}`
        )
        selectedCustomer.value = response.data.customer
        showCustomerModal.value = true
      } catch (error) {
        console.error('Error loading customer profile:', error)
      }
    }

    const viewCustomerOrders = async () => {
      if (!selectedConversation.value) return

      try {
        const response = await api.get(
          `/seller/customers/${selectedConversation.value.customer.id}/orders`
        )
        customerOrders.value = response.data.orders
        showOrdersModal.value = true
      } catch (error) {
        console.error('Error loading customer orders:', error)
      }
    }

    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const formatTime = (date) => {
      if (!date) return ''
      const now = new Date()
      const messageDate = new Date(date)
      const diffInHours = (now - messageDate) / (1000 * 60 * 60)

      if (diffInHours < 24) {
        return messageDate.toLocaleTimeString('vi-VN', {
          hour: '2-digit',
          minute: '2-digit',
        })
      } else {
        return messageDate.toLocaleDateString('vi-VN')
      }
    }

    const formatLastSeen = (date) => {
      if (!date) return 'Không xác định'

      const now = new Date()
      const lastSeen = new Date(date)
      const diffInMinutes = (now - lastSeen) / (1000 * 60)

      if (diffInMinutes < 1) return 'Vừa xong'
      if (diffInMinutes < 60) return `${Math.floor(diffInMinutes)} phút trước`
      if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)} giờ trước`
      return `${Math.floor(diffInMinutes / 1440)} ngày trước`
    }

    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const formatPrice = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const getOrderStatusLabel = (status) => {
      const labels = {
        pending: 'Chờ xử lý',
        processing: 'Đang xử lý',
        shipped: 'Đã gửi hàng',
        delivered: 'Đã giao hàng',
        cancelled: 'Đã hủy',
        refunded: 'Đã hoàn tiền',
      }
      return labels[status] || status
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    const handleImageError = (event) => {
      event.target.src = '/default-avatar.png'
    }

    // Watchers
    watch(selectedConversation, () => {
      nextTick(() => {
        scrollToBottom()
      })
    })

    // Lifecycle
    onMounted(() => {
      loadConversations()
    })

    return {
      conversations,
      selectedConversation,
      searchQuery,
      newMessage,
      loading,
      showCustomerModal,
      showOrdersModal,
      selectedCustomer,
      customerOrders,
      messagesContainer,
      currentUserId,
      filteredConversations,
      totalConversations,
      unreadMessages,
      avgResponseTime,
      responseRate,
      selectConversation,
      sendMessage,
      markAllAsRead,
      refreshChats,
      viewCustomerProfile,
      viewCustomerOrders,
      formatTime,
      formatLastSeen,
      formatDate,
      formatPrice,
      getOrderStatusLabel,
      truncateText,
      handleImageError,
    }
  },
}
</script>

<style scoped>
.seller-chat {
  padding: 2rem;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-content h3 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.stat-content p {
  color: #666;
  margin: 0;
}

.chat-container {
  flex: 1;
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.conversations-sidebar {
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.sidebar-header h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.2rem;
}

.search-box input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 1rem;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background: #f8f9fa;
}

.conversation-item.active {
  background: #e3f2fd;
  border-left: 4px solid #667eea;
}

.conversation-avatar {
  position: relative;
  margin-right: 1rem;
}

.conversation-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.conversation-header h4 {
  margin: 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.conversation-time {
  font-size: 0.8rem;
  color: #666;
}

.conversation-preview {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sent-indicator {
  color: #667eea;
  margin-right: 0.25rem;
}

.conversation-meta {
  font-size: 0.8rem;
}

.online-indicator {
  color: #2ed573;
}

.last-seen {
  color: #666;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.chat-customer-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.customer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.customer-details h3 {
  margin: 0 0 0.25rem 0;
  color: #333;
}

.online-status {
  color: #2ed573;
  font-size: 0.9rem;
}

.offline-status {
  color: #666;
  font-size: 0.9rem;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.no-conversation {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.no-conversation-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-conversation h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.messages-container {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message {
  display: flex;
  margin-bottom: 1rem;
}

.message.sent {
  justify-content: flex-end;
}

.message.received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 1rem;
  border-radius: 12px;
  position: relative;
}

.message.sent .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.received .message-content {
  background: #f1f3f4;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-text {
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.message-time {
  font-size: 0.8rem;
  opacity: 0.8;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.message-status {
  font-size: 0.7rem;
}

.message-input {
  padding: 1.5rem;
  border-top: 1px solid #eee;
  background: #f8f9fa;
}

.input-container {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.message-textarea {
  flex: 1;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-size: 0.9rem;
  max-height: 100px;
}

.send-button {
  padding: 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
  transition: background-color 0.2s;
}

.send-button:hover {
  background: #5a6fd8;
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-outline {
  background: transparent;
  border: 1px solid #ddd;
  color: #666;
}

.btn-outline:hover {
  background: #f8f9fa;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
}

.modal-overlay {
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

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #333;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 1.5rem;
}

.customer-profile {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.profile-info p {
  margin: 0.25rem 0;
  color: #666;
}

.profile-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-label {
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #333;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 8px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.order-id {
  font-weight: 600;
  color: #333;
}

.order-status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
}

.status-processing {
  background: #e3f2fd;
  color: #1976d2;
}

.status-shipped {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-delivered {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-cancelled {
  background: #ffebee;
  color: #c62828;
}

.status-refunded {
  background: #f3e5f5;
  color: #7b1fa2;
}

.order-details {
  font-size: 0.9rem;
  color: #666;
}

.order-details p {
  margin: 0.25rem 0;
}

.no-orders {
  text-align: center;
  color: #666;
  padding: 2rem;
}

@media (max-width: 768px) {
  .seller-chat {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .chat-container {
    grid-template-columns: 1fr;
    height: calc(100vh - 200px);
  }

  .conversations-sidebar {
    border-right: none;
    border-bottom: 1px solid #eee;
  }

  .conversation-item {
    padding: 0.75rem;
  }

  .conversation-avatar img {
    width: 40px;
    height: 40px;
  }

  .chat-header {
    padding: 1rem;
  }

  .customer-avatar {
    width: 40px;
    height: 40px;
  }

  .messages-container {
    padding: 1rem;
  }

  .message-input {
    padding: 1rem;
  }
}
</style>
