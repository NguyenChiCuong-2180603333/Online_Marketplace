<template>
  <div class="chat-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <span class="chat-icon">💬</span>
          Tin nhắn
        </h1>
        <p class="page-subtitle">Liên lạc với người bán</p>

        <!-- Debug Actions (only in development) -->
        <!-- Đã xóa nút TEST WEBSOCKET -->
      </div>
    </div>

    <!-- Chat Content -->
    <div class="container">
      <div class="chat-container">
        <!-- Chat Sidebar -->
        <div class="chat-sidebar">
          <!-- Conversations List -->
          <div class="conversations-list">
            <div
              v-for="conversation in conversations"
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
              </div>
            </div>
          </div>

          <!-- No Conversations -->
          <div v-if="conversations.length === 0" class="no-conversations">
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
                </div>

                <div class="user-details">
                  <h3 class="user-name">{{ selectedConversation.name }}</h3>
                </div>
              </div>

              <div class="chat-actions">
                <button @click="deleteConversation" class="action-btn" title="Xóa cuộc trò chuyện">
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
                  }"
                >
                  <div class="user-message">
                    <div class="message-avatar" v-if="message.senderId !== currentUserId">
                      <img
                        :src="message.senderAvatar || '/placeholder-avatar.jpg'"
                        :alt="message.senderName"
                      />
                    </div>

                    <div class="message-content">
                      <div class="message-bubble">
                        <p class="message-text">{{ message.content }}</p>

                        <div class="message-meta">
                          <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Message Input -->
            <div class="message-input-container">
              <div class="message-input-wrapper">
                <div class="input-container">
                  <textarea
                    v-model="newMessage"
                    placeholder="Nhập tin nhắn..."
                    @keydown.enter.prevent="sendMessage"
                    ref="messageInput"
                    rows="1"
                  ></textarea>
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
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { chatAPI } from '@/services/api'
import getWebSocketService from '@/services/websocket'
import { useChatStore } from '@/stores/chat'

export default {
  name: 'Chat',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const authStore = useAuthStore()
    const chatStore = useChatStore()

    // Reactive data
    const loadingMessages = ref(false)
    const sending = ref(false)
    const newMessage = ref('')

    // Chat data
    const conversations = ref([])
    const selectedConversation = ref(null)
    const messages = computed(
      () => chatStore.messagesByConversation[chatStore.activeConversationId] || []
    )

    // Refs
    const messagesContainer = ref(null)
    const messageInput = ref(null)

    // Computed
    const currentUserId = computed(() => authStore.user?.id)
    const token = computed(() => authStore.token)

    // Log dữ liệu messages để debug avatar
    watch(
      messages,
      (val) => {
        console.log('DEBUG messages:', val)
      },
      { immediate: true }
    )

    // Methods
    const loadConversations = async () => {
      try {
        const response = await chatAPI.getConversations()
        const convs = response.data || []

        // Process conversations
        for (const conv of convs) {
          await enrichConversationData(conv)
        }

        conversations.value = convs
      } catch (err) {
        console.error('Load conversations error:', err)
      }
    }

    const enrichConversationData = async (conversation) => {
      try {
        // Find the other participant
        const otherId = conversation.participantIds?.find((id) => id !== currentUserId.value)

        if (otherId) {
          const userRes = await chatAPI.getUserById(otherId)
          const userData = userRes.data

          conversation.name =
            `${userData.firstName || ''} ${userData.lastName || ''}`.trim() || 'Người dùng'
          conversation.avatar = userData.avatar || '/placeholder-avatar.jpg'
          conversation.customer = userData // For seller context
          conversation.userId = otherId
        }
      } catch (e) {
        console.error('Error enriching conversation data:', e)
        conversation.name = 'Người dùng'
        conversation.avatar = '/placeholder-avatar.jpg'
      }
    }

    const selectConversation = async (conversation) => {
      selectedConversation.value = conversation
      chatStore.setActiveConversation(conversation.id)
      scrollToBottom()
      // Mark as read - sử dụng chatStore để cập nhật badge
      if (conversation.unreadCount > 0) {
        console.log('💬 Chat.vue: Marking conversation as read:', conversation.id)
        await chatStore.markAsRead(conversation.id)
        // Sau khi markAsRead, đồng bộ lại conversations và badge
        await chatStore.loadConversations()
      }
    }

    const markAsRead = async (conversationId) => {
      try {
        await chatAPI.markAsRead(conversationId)

        // Update local state
        const conv = conversations.value.find((c) => c.id === conversationId)
        if (conv) {
          conv.unreadCount = 0
        }
      } catch (err) {
        console.error('Mark as read error:', err)
      }
    }

    const autoSelectConversation = async () => {
      const sellerId = route.query.sellerId
      const productId = route.query.productId

      if (sellerId && productId) {
        await loadConversations()

        let found = conversations.value.find(
          (c) =>
            c.participantIds && c.participantIds.includes(sellerId) && c.productId === productId
        )

        if (!found) {
          try {
            const res = await chatAPI.createConversation(sellerId, productId)
            found = res.data
            await enrichConversationData(found)
            await loadConversations()
          } catch (e) {
            console.error('Không thể tạo cuộc trò chuyện:', e)
            return
          }
        }

        if (found) {
          await selectConversation(found)
        }
      }
    }

    // Hàm gửi tin nhắn dùng chung
    const sendMessageUnified = async (conversationId, content, messageType = 'TEXT') => {
      const ws = getWebSocketService()
      let sent = false
      if (ws.connected) {
        sent = await ws.sendMessage(conversationId, content, messageType)
      }
      if (!sent) {
        // Fallback REST
        try {
          await chatAPI.sendMessage(conversationId, content, messageType)
          sent = true
        } catch (e) {
          console.error('REST gửi tin nhắn thất bại:', e)
        }
      }
      return sent
    }

    // Sửa lại hàm sendMessage sử dụng sendMessageUnified
    const sendMessage = async () => {
      if (!newMessage.value.trim() || !selectedConversation.value) return
      if (!currentUserId.value || !token.value) {
        alert('Bạn cần đăng nhập lại để sử dụng chat!')
        router.push('/login')
        return
      }
      sending.value = true
      try {
        // Optimistic update
        const optimisticMessage = enrichMessageSenderInfo({
          id: 'temp_' + Date.now(),
          conversationId: selectedConversation.value.id,
          senderId: currentUserId.value,
          content: newMessage.value.trim(),
          messageType: 'TEXT',
          status: 'sending',
          createdAt: new Date().toISOString(),
          isTemporary: true,
        })
        messages.value.push(optimisticMessage)
        const messageText = newMessage.value.trim()
        newMessage.value = ''
        await nextTick()
        scrollToBottom()
        // Gửi qua WebSocket ưu tiên, fallback REST
        const sent = await sendMessageUnified(selectedConversation.value.id, messageText, 'TEXT')
        if (sent) {
          optimisticMessage.status = 'sent'
        } else {
          messages.value = messages.value.filter((m) => !m.isTemporary)
        }
      } catch (err) {
        console.error('Send message error:', err)
        messages.value = messages.value.filter((m) => !m.isTemporary)
      } finally {
        sending.value = false
      }
    }

    // Xóa cuộc trò chuyện
    const deleteConversation = async () => {
      if (!selectedConversation.value) return
      if (!confirm('Bạn có chắc muốn xóa toàn bộ cuộc trò chuyện này?')) return
      try {
        await chatAPI.deleteConversation(selectedConversation.value.id)
        // Xóa khỏi danh sách local
        conversations.value = conversations.value.filter(
          (c) => c.id !== selectedConversation.value.id
        )
        selectedConversation.value = null
        chatStore.setActiveConversation(null)
        alert('Đã xóa cuộc trò chuyện!')
      } catch (err) {
        alert('Xóa cuộc trò chuyện thất bại!')
        console.error('Delete conversation error:', err)
      }
    }

    const clearChat = async () => {
      if (!selectedConversation.value) return

      if (!confirm('Bạn có chắc muốn xóa tất cả tin nhắn trong cuộc trò chuyện này?')) return

      try {
        messages.value = []
        console.warn('Clear chat feature not implemented yet')
      } catch (err) {
        console.error('Clear chat error:', err)
      }
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
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

    const handleImageError = (event) => {
      event.target.src = '/placeholder-avatar.jpg'
    }

    watch(messages, () => {
      nextTick(() => {
        scrollToBottom()
      })
    })

    watch(
      () => selectedConversation.value?.id,
      (newId, oldId) => {
        console.log('💬 Conversation changed from', oldId, 'to', newId)

        const websocketService = getWebSocketService()
        if (newId && websocketService.connected) {
          console.log('💬 Setting up specific listener for new conversation:', newId)
        }
      }
    )

    function enrichMessageSenderInfo(msg) {
      if (!msg) return msg
      if (msg.senderId === currentUserId.value) {
        msg.senderAvatar = authStore.user?.avatar || '/placeholder-avatar.jpg'
        msg.senderName =
          (authStore.user?.firstName + ' ' + authStore.user?.lastName).trim() || 'Bạn'
      } else if (selectedConversation.value) {
        msg.senderAvatar = selectedConversation.value.avatar || '/placeholder-avatar.jpg'
        msg.senderName = selectedConversation.value.name || 'Người dùng'
      } else {
        msg.senderAvatar = '/placeholder-avatar.jpg'
        msg.senderName = 'Người dùng'
      }
      return msg
    }

    // Hàm xử lý message realtime (từ WebSocket)
    const handleIncomingMessage = (messageData) => {
      console.log('💬 [WebSocket] Nhận message realtime:', messageData)
      if (messageData.conversationId === selectedConversation.value?.id) {
        // Nếu là message của chính mình và đã có message tạm cùng content, thay thế message tạm bằng message thật
        if (messageData.senderId === currentUserId.value) {
          const tempIdx = messages.value.findIndex(
            (m) => m.isTemporary && m.content === messageData.content
          )
          if (tempIdx !== -1) {
            messages.value[tempIdx] = messageData
            nextTick(() => scrollToBottom())
            return
          }
        }
        if (!messages.value.some((m) => m.id === messageData.id)) {
          messages.value.push(messageData)
          nextTick(() => scrollToBottom())
        }
      }
    }

    // Chỉ load conversations, không setup WebSocket lại
    onMounted(async () => {
      console.log('💬 Chat.vue: onMounted - load conversations và auto select theo query')
      await loadConversations()
      const conversationId = route.query.conversationId
      if (conversationId) {
        const found = conversations.value.find((c) => c.id === conversationId)
        if (found) {
          await selectConversation(found)
        }
      } else {
        await autoSelectConversation()
      }
    })

    onUnmounted(() => {
      chatStore.setActiveConversation(null)
    })

    return {
      loadingMessages,
      sending,
      newMessage,
      conversations,
      selectedConversation,
      messages,
      currentUserId,
      messagesContainer,
      messageInput,

      selectConversation,
      sendMessage,
      deleteConversation,
      clearChat,
      formatTime,
      handleImageError,

    }
  },
}
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #181c2f;
}

.container {
  flex: 1 1 auto;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chat-container {
  width: 90vw;
  max-width: 1600px;
  height: 80vh;
  min-height: 500px;
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 2rem;
  background: rgba(26, 26, 46, 0.85);
  border: 1.5px solid rgba(0, 212, 255, 0.18);
  border-radius: 24px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
  overflow: hidden;
  margin: 0 auto;
}

.chat-main {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.messages-container {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 1rem;
  background: transparent;
}

.message-input-container {
  flex-shrink: 0;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.85);
  border-top: 1px solid rgba(0, 212, 255, 0.1);
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

/* Chat Sidebar */
.chat-sidebar {
  background: rgba(26, 26, 46, 0.8);
  border-right: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  flex-direction: column;
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

/* Chat Header */
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
.message-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
}

.input-container {
  flex: 1;
  position: relative;
}

.input-container textarea {
  width: 100%;
  min-height: 40px;
  max-height: 120px;
  padding: 0.75rem 1rem;
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

/* Seller Statistics */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  border-color: rgba(0, 212, 255, 0.4);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 2rem;
  color: var(--text-accent);
}

.stat-content h3 {
  color: var(--text-primary);
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
}

.stat-content p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

/* Header Actions */
.header-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-outline {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

/* Seller Actions */
.seller-actions {
  display: flex;
  gap: 0.5rem;
}

/* Customer Profile Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.modal-header h2 {
  color: var(--text-primary);
  margin: 0;
  font-size: 1.3rem;
}

.btn-icon {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.btn-icon:hover {
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.modal-body {
  padding: 1.5rem;
}

.customer-profile {
  text-align: center;
}

.profile-header {
  margin-bottom: 2rem;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(0, 212, 255, 0.3);
  margin-bottom: 1rem;
}

.profile-info h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
}

.profile-info p {
  color: var(--text-secondary);
  margin: 0.25rem 0;
  font-size: 0.9rem;
}

.profile-stats {
  margin-bottom: 2rem;
}

.profile-stats .stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.profile-stats .stat-item:last-child {
  border-bottom: none;
}

.profile-stats .stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.profile-stats .stat-value {
  color: var(--text-primary);
  font-weight: 600;
}

.profile-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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
