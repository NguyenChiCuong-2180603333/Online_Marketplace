<template>
  <div class="chat-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <span class="chat-icon">💬</span>
          Tin nhắn
        </h1>
        <p class="page-subtitle">Liên lạc với người bán và hỗ trợ khách hàng</p>
      </div>
    </div>

    <!-- Chat Content -->
    <div class="container">
      <div class="chat-container">
        <!-- Chat Sidebar -->
        <div class="chat-sidebar">
          <!-- Search Conversations -->
          <div class="search-section">
            <div class="search-input">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm kiếm cuộc trò chuyện..."
                @input="searchConversations"
              />
              <span class="search-icon">🔍</span>
            </div>
          </div>

          <!-- Conversations List -->
          <div class="conversations-list">
            <div
              v-for="conversation in filteredConversations"
              :key="conversation.id"
              class="conversation-item"
              :class="{
                active: selectedConversation?.id === conversation.id,
                unread: conversation.unreadCount > 0,
              }"
              @click="selectConversation(conversation)"
            >
              <div class="conversation-avatar">
                <img
                  :src="conversation.avatar || '/placeholder-avatar.jpg'"
                  :alt="conversation.name"
                  @error="handleImageError"
                />
                <div class="status-indicator" :class="{ online: conversation.isOnline }"></div>
              </div>

              <div class="conversation-info">
                <div class="conversation-header">
                  <h4 class="conversation-name">{{ conversation.name }}</h4>
                  <span class="conversation-time">{{
                    formatTime(conversation.lastMessage?.createdAt)
                  }}</span>
                </div>

                <div class="conversation-preview">
                  <p class="last-message">
                    {{ conversation.lastMessage?.content || 'Chưa có tin nhắn' }}
                  </p>
                  <div v-if="conversation.unreadCount > 0" class="unread-badge">
                    {{ conversation.unreadCount }}
                  </div>
                </div>

                <div class="conversation-meta">
                  <span class="conversation-type">{{
                    getConversationType(conversation.type)
                  }}</span>
                  <span v-if="conversation.isTyping" class="typing-indicator">Đang nhập...</span>
                </div>
              </div>
            </div>
          </div>

          <!-- No Conversations -->
          <div v-if="filteredConversations.length === 0" class="no-conversations">
            <div class="no-conversations-content">
              <div class="no-conversations-icon">💬</div>
              <h3>Không có cuộc trò chuyện nào</h3>
              <p>Bắt đầu trò chuyện với người bán hoặc hỗ trợ khách hàng</p>
            </div>
          </div>
        </div>

        <!-- Chat Main Area -->
        <div class="chat-main">
          <!-- No Conversation Selected -->
          <div v-if="!selectedConversation" class="no-conversation-selected">
            <div class="no-conversation-content">
              <div class="no-conversation-icon">💬</div>
              <h3>Chọn một cuộc trò chuyện</h3>
              <p>Chọn cuộc trò chuyện từ danh sách bên trái để bắt đầu nhắn tin</p>
            </div>
          </div>

          <!-- Chat Window -->
          <div v-else class="chat-window">
            <!-- Chat Header -->
            <div class="chat-header">
              <div class="chat-user-info">
                <div class="user-avatar">
                  <img
                    :src="selectedConversation.avatar || '/placeholder-avatar.jpg'"
                    :alt="selectedConversation.name"
                  />
                  <div
                    class="status-indicator"
                    :class="{ online: selectedConversation.isOnline }"
                  ></div>
                </div>

                <div class="user-details">
                  <h3 class="user-name">{{ selectedConversation.name }}</h3>
                  <p class="user-status">
                    {{ selectedConversation.isOnline ? 'Đang hoạt động' : 'Không hoạt động' }}
                  </p>
                </div>
              </div>

              <div class="chat-actions">
                <button @click="toggleUserInfo" class="action-btn" title="Thông tin người dùng">
                  ℹ️
                </button>
                <button @click="toggleSearch" class="action-btn" title="Tìm kiếm tin nhắn">
                  🔍
                </button>
                <button @click="clearChat" class="action-btn" title="Xóa cuộc trò chuyện">
                  🗑️
                </button>
              </div>
            </div>

            <!-- Messages Container -->
            <div class="messages-container" ref="messagesContainer">
              <!-- Loading Messages -->
              <div v-if="loadingMessages" class="loading-messages">
                <div class="loading-spinner">
                  <div class="spinner"></div>
                  <p>Đang tải tin nhắn...</p>
                </div>
              </div>

              <!-- Messages List -->
              <div v-else class="messages-list">
                <div
                  v-for="message in messages"
                  :key="message.id"
                  class="message-item"
                  :class="{
                    'own-message': message.senderId === currentUserId,
                    'system-message': message.type === 'system',
                  }"
                >
                  <!-- System Message -->
                  <div v-if="message.type === 'system'" class="system-message">
                    <span class="system-text">{{ message.content }}</span>
                    <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                  </div>

                  <!-- User Message -->
                  <div v-else class="user-message">
                    <div class="message-avatar" v-if="message.senderId !== currentUserId">
                      <img
                        :src="message.senderAvatar || '/placeholder-avatar.jpg'"
                        :alt="message.senderName"
                      />
                    </div>

                    <div class="message-content">
                      <div class="message-bubble">
                        <p class="message-text">{{ message.content }}</p>

                        <!-- Message Attachments -->
                        <div
                          v-if="message.attachments && message.attachments.length > 0"
                          class="message-attachments"
                        >
                          <div
                            v-for="attachment in message.attachments"
                            :key="attachment.id"
                            class="attachment-item"
                          >
                            <img
                              v-if="attachment.type === 'image'"
                              :src="attachment.url"
                              :alt="attachment.name"
                              @click="viewImage(attachment.url)"
                            />
                            <div v-else class="file-attachment">
                              <span class="file-icon">📎</span>
                              <span class="file-name">{{ attachment.name }}</span>
                            </div>
                          </div>
                        </div>

                        <div class="message-meta">
                          <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                          <span v-if="message.senderId === currentUserId" class="message-status">
                            {{ getMessageStatus(message.status) }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Typing Indicator -->
                <div v-if="selectedConversation.isTyping" class="typing-indicator">
                  <div class="typing-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span class="typing-text">Đang nhập...</span>
                </div>
              </div>
            </div>

            <!-- Message Input -->
            <div class="message-input-container">
              <div class="message-input-wrapper">
                <button @click="toggleEmojiPicker" class="emoji-btn">😊</button>

                <div class="input-container">
                  <textarea
                    v-model="newMessage"
                    placeholder="Nhập tin nhắn..."
                    @keydown.enter.prevent="sendMessage"
                    @input="handleTyping"
                    ref="messageInput"
                    rows="1"
                  ></textarea>

                  <div class="input-actions">
                    <button @click="attachFile" class="attach-btn" title="Đính kèm file">📎</button>
                    <button @click="attachImage" class="attach-btn" title="Đính kèm ảnh">🖼️</button>
                  </div>
                </div>

                <button
                  @click="sendMessage"
                  :disabled="!newMessage.trim() || sending"
                  class="send-btn"
                >
                  <span v-if="sending" class="sending">⏳</span>
                  <span v-else>📤</span>
                </button>
              </div>

              <!-- Emoji Picker -->
              <div v-if="showEmojiPicker" class="emoji-picker">
                <div class="emoji-grid">
                  <button
                    v-for="emoji in emojis"
                    :key="emoji"
                    @click="addEmoji(emoji)"
                    class="emoji-btn"
                  >
                    {{ emoji }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- User Info Sidebar -->
        <div v-if="showUserInfo && selectedConversation" class="user-info-sidebar">
          <div class="user-info-header">
            <h3>Thông tin người dùng</h3>
            <button @click="toggleUserInfo" class="close-btn">✕</button>
          </div>

          <div class="user-info-content">
            <div class="user-avatar-large">
              <img
                :src="selectedConversation.avatar || '/placeholder-avatar.jpg'"
                :alt="selectedConversation.name"
              />
            </div>

            <h4 class="user-name-large">{{ selectedConversation.name }}</h4>
            <p class="user-role">{{ getConversationType(selectedConversation.type) }}</p>

            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-label">Tin nhắn đã gửi</span>
                <span class="stat-value">{{ selectedConversation.messageCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Thời gian hoạt động</span>
                <span class="stat-value">{{ formatLastSeen(selectedConversation.lastSeen) }}</span>
              </div>
            </div>

            <div class="user-actions">
              <button @click="viewProfile" class="btn btn-primary">👤 Xem hồ sơ</button>
              <button @click="blockUser" class="btn btn-danger">🚫 Chặn người dùng</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/services/api'

export default {
  name: 'Chat',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    // Reactive data
    const loadingMessages = ref(false)
    const sending = ref(false)
    const searchQuery = ref('')
    const newMessage = ref('')
    const showEmojiPicker = ref(false)
    const showUserInfo = ref(false)

    // Chat data
    const conversations = ref([])
    const selectedConversation = ref(null)
    const messages = ref([])

    // Refs
    const messagesContainer = ref(null)
    const messageInput = ref(null)

    // Computed
    const currentUserId = computed(() => authStore.user?.id)

    const filteredConversations = computed(() => {
      if (!searchQuery.value) return conversations.value

      return conversations.value.filter((conversation) =>
        conversation.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
    })

    // Emojis
    const emojis = [
      '😊',
      '😂',
      '❤️',
      '👍',
      '👎',
      '🎉',
      '🔥',
      '💯',
      '😍',
      '🤔',
      '😭',
      '😡',
      '😱',
      '😴',
      '🤗',
      '🤫',
      '🤐',
      '🤯',
      '😵',
      '🥳',
      '😎',
      '🤩',
      '🥺',
      '😤',
      '😫',
      '😩',
      '😪',
      '😴',
      '🤤',
      '😵‍💫',
    ]

    // Methods
    const loadConversations = async () => {
      try {
        const response = await chatAPI.getConversations()
        conversations.value = response.data || []
      } catch (err) {
        console.error('Load conversations error:', err)
      }
    }

    const selectConversation = async (conversation) => {
      selectedConversation.value = conversation
      showUserInfo.value = false
      await loadMessages(conversation.id)
      scrollToBottom()
    }

    const loadMessages = async (conversationId) => {
      loadingMessages.value = true
      try {
        const response = await chatAPI.getMessages(conversationId)
        messages.value = response.data || []
        await nextTick()
        scrollToBottom()
      } catch (err) {
        console.error('Load messages error:', err)
      } finally {
        loadingMessages.value = false
      }
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim() || !selectedConversation.value) return

      sending.value = true
      try {
        const messageData = {
          conversationId: selectedConversation.value.id,
          content: newMessage.value.trim(),
          type: 'text',
        }

        const response = await chatAPI.sendMessage(messageData)
        messages.value.push(response.data)
        newMessage.value = ''

        // Update conversation last message
        selectedConversation.value.lastMessage = response.data
        selectedConversation.value.unreadCount = 0

        await nextTick()
        scrollToBottom()
      } catch (err) {
        console.error('Send message error:', err)
      } finally {
        sending.value = false
      }
    }

    const handleTyping = () => {
      if (selectedConversation.value) {
        chatAPI.sendTyping(selectedConversation.value.id)
      }
    }

    const searchConversations = () => {
      // Search logic is handled by computed property
    }

    const toggleEmojiPicker = () => {
      showEmojiPicker.value = !showEmojiPicker.value
    }

    const addEmoji = (emoji) => {
      newMessage.value += emoji
      showEmojiPicker.value = false
      messageInput.value?.focus()
    }

    const toggleUserInfo = () => {
      showUserInfo.value = !showUserInfo.value
    }

    const toggleSearch = () => {
      // Implement message search
    }

    const clearChat = async () => {
      if (!selectedConversation.value) return

      if (!confirm('Bạn có chắc muốn xóa tất cả tin nhắn trong cuộc trò chuyện này?')) return

      try {
        await chatAPI.clearConversation(selectedConversation.value.id)
        messages.value = []
      } catch (err) {
        console.error('Clear chat error:', err)
      }
    }

    const attachFile = () => {
      // Implement file attachment
    }

    const attachImage = () => {
      // Implement image attachment
    }

    const viewImage = (imageUrl) => {
      // Implement image viewer
    }

    const viewProfile = () => {
      if (selectedConversation.value?.userId) {
        router.push(`/profile/${selectedConversation.value.userId}`)
      }
    }

    const blockUser = async () => {
      if (!selectedConversation.value) return

      if (!confirm('Bạn có chắc muốn chặn người dùng này?')) return

      try {
        await chatAPI.blockUser(selectedConversation.value.userId)
        // Remove conversation from list
        conversations.value = conversations.value.filter(
          (c) => c.id !== selectedConversation.value.id
        )
        selectedConversation.value = null
      } catch (err) {
        console.error('Block user error:', err)
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
      const diff = now - messageDate
      const minutes = Math.floor(diff / 60000)

      if (minutes < 1) return 'Vừa xong'
      if (minutes < 60) return `${minutes} phút trước`
      if (minutes < 1440) return `${Math.floor(minutes / 60)} giờ trước`
      return messageDate.toLocaleDateString('vi-VN')
    }

    const formatLastSeen = (date) => {
      if (!date) return 'Không xác định'
      return formatTime(date)
    }

    const getConversationType = (type) => {
      const types = {
        seller: 'Người bán',
        support: 'Hỗ trợ khách hàng',
        user: 'Người dùng',
      }
      return types[type] || 'Người dùng'
    }

    const getMessageStatus = (status) => {
      const statuses = {
        sent: '✓',
        delivered: '✓✓',
        read: '✓✓',
      }
      return statuses[status] || ''
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-avatar.jpg'
    }

    // Watch for new messages
    watch(messages, () => {
      nextTick(() => {
        scrollToBottom()
      })
    })

    // Lifecycle
    onMounted(() => {
      loadConversations()
    })

    return {
      loadingMessages,
      sending,
      searchQuery,
      newMessage,
      showEmojiPicker,
      showUserInfo,
      conversations,
      selectedConversation,
      messages,
      currentUserId,
      filteredConversations,
      emojis,
      messagesContainer,
      messageInput,
      selectConversation,
      sendMessage,
      handleTyping,
      searchConversations,
      toggleEmojiPicker,
      addEmoji,
      toggleUserInfo,
      toggleSearch,
      clearChat,
      attachFile,
      attachImage,
      viewImage,
      viewProfile,
      blockUser,
      formatTime,
      formatLastSeen,
      getConversationType,
      getMessageStatus,
      handleImageError,
    }
  },
}
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem 0;
  background: linear-gradient(135deg, var(--accent-blue) 0%, var(--accent-purple) 100%);
  color: white;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.chat-icon {
  font-size: 3rem;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Chat Container */
.chat-container {
  display: grid;
  grid-template-columns: 300px 1fr 300px;
  gap: 1rem;
  height: 70vh;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
}

/* Chat Sidebar */
.chat-sidebar {
  background: rgba(26, 26, 46, 0.8);
  border-right: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  flex-direction: column;
}

.search-section {
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.search-input {
  position: relative;
}

.search-input input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.search-input input:focus {
  outline: none;
  border-color: var(--text-accent);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.conversation-item:hover {
  background: rgba(0, 212, 255, 0.1);
}

.conversation-item.active {
  background: rgba(0, 212, 255, 0.2);
  border-left: 3px solid var(--text-accent);
}

.conversation-item.unread {
  background: rgba(0, 212, 255, 0.15);
}

.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.conversation-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #6b7280;
  border: 2px solid #1a1a2e;
}

.status-indicator.online {
  background: #10b981;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.25rem;
}

.conversation-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 0.75rem;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.last-message {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.unread-badge {
  background: var(--text-accent);
  color: #1a1a2e;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.4rem;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.conversation-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-type {
  font-size: 0.7rem;
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
}

.typing-indicator {
  font-size: 0.7rem;
  color: var(--text-accent);
  font-style: italic;
}

/* No Conversations */
.no-conversations {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.no-conversations-content {
  text-align: center;
}

.no-conversations-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.no-conversations h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.no-conversations p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

/* Chat Main */
.chat-main {
  display: flex;
  flex-direction: column;
  background: rgba(26, 26, 46, 0.4);
}

.no-conversation-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-conversation-content {
  text-align: center;
}

.no-conversation-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-conversation-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.no-conversation-content p {
  color: var(--text-secondary);
}

/* Chat Window */
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  position: relative;
}

.user-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details h3 {
  color: var(--text-primary);
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
}

.user-status {
  color: var(--text-secondary);
  font-size: 0.85rem;
  margin: 0;
}

.chat-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.action-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

/* Messages Container */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.loading-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.loading-spinner {
  text-align: center;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-top: 2px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  display: flex;
  flex-direction: column;
}

.message-item.own-message {
  align-items: flex-end;
}

.system-message {
  text-align: center;
  margin: 1rem 0;
}

.system-text {
  background: rgba(0, 0, 0, 0.3);
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
}

.message-time {
  font-size: 0.7rem;
  color: var(--text-secondary);
  margin-left: 0.5rem;
}

.user-message {
  display: flex;
  gap: 0.75rem;
  max-width: 70%;
}

.message-item.own-message .user-message {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  flex: 1;
}

.message-bubble {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 0.75rem 1rem;
  position: relative;
}

.message-item.own-message .message-bubble {
  background: var(--text-accent);
  color: #1a1a2e;
  border-color: var(--text-accent);
}

.message-text {
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-attachments {
  margin-bottom: 0.5rem;
}

.attachment-item img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.attachment-item img:hover {
  transform: scale(1.05);
}

.file-attachment {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  cursor: pointer;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.7rem;
  color: var(--text-secondary);
}

.message-item.own-message .message-meta {
  color: rgba(26, 26, 46, 0.7);
}

.message-status {
  margin-left: 0.5rem;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.typing-dots {
  display: flex;
  gap: 0.2rem;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: var(--text-secondary);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}
.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Message Input */
.message-input-container {
  padding: 1rem;
  background: rgba(26, 26, 46, 0.8);
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  position: relative;
}

.message-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
}

.emoji-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.emoji-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.input-container {
  flex: 1;
  position: relative;
}

.input-container textarea {
  width: 100%;
  min-height: 40px;
  max-height: 120px;
  padding: 0.75rem 1rem 0.75rem 3rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.9rem;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
}

.input-container textarea:focus {
  border-color: var(--text-accent);
}

.input-actions {
  position: absolute;
  left: 0.5rem;
  bottom: 0.5rem;
  display: flex;
  gap: 0.25rem;
}

.attach-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.attach-btn:hover {
  color: var(--text-accent);
}

.send-btn {
  background: var(--text-accent);
  color: #1a1a2e;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  min-width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  background: #00c4ef;
  transform: translateY(-2px);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sending {
  animation: spin 1s linear infinite;
}

/* Emoji Picker */
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 0.5rem;
  z-index: 10;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 0.5rem;
}

.emoji-picker .emoji-btn {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.emoji-picker .emoji-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  transform: scale(1.1);
}

/* User Info Sidebar */
.user-info-sidebar {
  background: rgba(26, 26, 46, 0.8);
  border-left: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  flex-direction: column;
}

.user-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.user-info-header h3 {
  color: var(--text-primary);
  margin: 0;
  font-size: 1.1rem;
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  color: var(--text-accent);
}

.user-info-content {
  flex: 1;
  padding: 1.5rem;
  text-align: center;
}

.user-avatar-large {
  margin-bottom: 1rem;
}

.user-avatar-large img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(0, 212, 255, 0.3);
}

.user-name-large {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
}

.user-role {
  color: var(--text-accent);
  margin-bottom: 2rem;
  font-size: 0.9rem;
}

.user-stats {
  margin-bottom: 2rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.stat-value {
  color: var(--text-primary);
  font-weight: 600;
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.btn {
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-primary {
  background: var(--text-accent);
  color: #1a1a2e;
}

.btn-primary:hover {
  background: #00c4ef;
  transform: translateY(-2px);
}

.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.btn-danger:hover {
  background: rgba(239, 68, 68, 0.3);
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 1200px) {
  .chat-container {
    grid-template-columns: 300px 1fr;
  }

  .user-info-sidebar {
    position: fixed;
    top: 0;
    right: -300px;
    width: 300px;
    height: 100vh;
    z-index: 1000;
    transition: right 0.3s ease;
  }

  .user-info-sidebar.show {
    right: 0;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .chat-container {
    grid-template-columns: 1fr;
    height: 60vh;
  }

  .chat-sidebar {
    position: fixed;
    top: 0;
    left: -300px;
    width: 300px;
    height: 100vh;
    z-index: 1000;
    transition: left 0.3s ease;
  }

  .chat-sidebar.show {
    left: 0;
  }

  .user-message {
    max-width: 85%;
  }

  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}
</style>
