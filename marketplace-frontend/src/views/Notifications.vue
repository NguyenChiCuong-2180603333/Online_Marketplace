<template>
  <div class="notifications-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <span class="notifications-icon">🔔</span>
          Thông báo
        </h1>
        <p class="page-subtitle">Quản lý và xem tất cả thông báo của bạn</p>
      </div>
    </div>

    <!-- Notifications Content -->
    <div class="container">
      <!-- Filters -->
      <div class="notifications-filters">
        <div class="filter-controls">
          <div class="filter-group">
            <label>Loại thông báo:</label>
            <select v-model="selectedType" @change="applyFilters" class="form-select">
              <option value="">Tất cả</option>
              <option value="NEW_PRODUCT">Sản phẩm mới</option>
              <option value="PROMOTION">Khuyến mãi</option>
              <option value="ORDER_UPDATE">Cập nhật đơn hàng</option>
              <option value="SYSTEM_UPDATE">Hệ thống</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Trạng thái:</label>
            <select v-model="selectedStatus" @change="applyFilters" class="form-select">
              <option value="">Tất cả</option>
              <option value="unread">Chưa đọc</option>
              <option value="read">Đã đọc</option>
            </select>
          </div>
          <button @click="markAllAsRead" class="btn btn-secondary">
            ✅ Đánh dấu tất cả đã đọc
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <p>Đang tải thông báo...</p>
        </div>
      </div>

      <!-- Notifications List -->
      <div v-else-if="filteredNotifications.length > 0" class="notifications-list">
        <div class="notifications-header">
          <h3>{{ filteredNotifications.length }} thông báo</h3>
        </div>

        <div class="notifications-container">
          <div
            v-for="notification in paginatedNotifications"
            :key="notification.id"
            class="notification-item"
            :class="{
              unread: !notification.read,
            }"
          >
            <div class="notification-icon">
              <span :class="getNotificationIcon(notification.type)">
                {{ getNotificationEmoji(notification.type) }}
              </span>
            </div>

            <div class="notification-content">
              <div class="notification-header">
                <h4 class="notification-title">{{ notification.title }}</h4>
                <div class="notification-meta">
                  <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
                  <span v-if="!notification.read" class="unread-indicator">●</span>
                </div>
              </div>

              <p class="notification-message">{{ notification.message }}</p>

              <div v-if="notification.data" class="notification-data">
                <div v-if="notification.data.orderId" class="data-item">
                  <span class="data-label">Mã đơn hàng:</span>
                  <span class="data-value">{{ notification.data.orderId }}</span>
                </div>
                <div v-if="notification.data.productName" class="data-item">
                  <span class="data-label">Sản phẩm:</span>
                  <span class="data-value">{{ notification.data.productName }}</span>
                </div>
                <div v-if="notification.data.amount" class="data-item">
                  <span class="data-label">Số tiền:</span>
                  <span class="data-value">{{ formatCurrency(notification.data.amount) }}</span>
                </div>
              </div>

              <div class="notification-actions">
                <button
                  v-if="notification.actionUrl"
                  @click="handleNotificationAction(notification)"
                  class="btn btn-primary btn-sm"
                >
                  {{ getActionText(notification.type) }}
                </button>

                <button
                  @click="markAsRead(notification.id)"
                  v-if="!notification.read"
                  class="btn btn-secondary btn-sm"
                >
                  Đánh dấu đã đọc
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            ⬅️ Trước
          </button>

          <div class="pagination-numbers">
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="changePage(page)"
              class="pagination-btn"
              :class="{ active: page === currentPage }"
            >
              {{ page }}
            </button>
          </div>

          <button
            @click="changePage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            Sau ➡️
          </button>
        </div>
      </div>

      <!-- No Notifications -->
      <div v-else class="no-notifications">
        <div class="no-notifications-content">
          <div class="no-notifications-icon">🔔</div>
          <h3>Không có thông báo nào</h3>
          <p v-if="hasFilters">Thử thay đổi bộ lọc để xem thông báo khác</p>
          <p v-else>Bạn chưa có thông báo nào. Chúng tôi sẽ thông báo khi có tin mới!</p>
          <button v-if="hasFilters" @click="clearFilters" class="btn btn-primary">
            Xóa bộ lọc
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationAPI } from '@/services/api'

export default {
  name: 'Notifications',
  setup() {
    const router = useRouter()

    // Reactive data
    const loading = ref(false)
    const error = ref(null)
    const notifications = ref([])

    // Filters
    const selectedType = ref('')
    const selectedStatus = ref('')

    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = ref(10)

    // Computed properties
    const filteredNotifications = computed(() => {
      let filtered = [...notifications.value]

      if (selectedType.value) {
        filtered = filtered.filter((n) => n.type === selectedType.value)
      }
      if (selectedStatus.value === 'read') {
        filtered = filtered.filter((n) => n.read)
      } else if (selectedStatus.value === 'unread') {
        filtered = filtered.filter((n) => !n.read)
      }

      return filtered
    })

    const paginatedNotifications = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      return filteredNotifications.value.slice(start, start + itemsPerPage.value)
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredNotifications.value.length / itemsPerPage.value)
    })

    const visiblePages = computed(() => {
      const pages = []
      const current = currentPage.value
      const total = totalPages.value

      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) pages.push(i)
          pages.push('...')
          pages.push(total)
        } else if (current >= total - 3) {
          pages.push(1)
          pages.push('...')
          for (let i = total - 4; i <= total; i++) pages.push(i)
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = current - 1; i <= current + 1; i++) pages.push(i)
          pages.push('...')
          pages.push(total)
        }
      }

      return pages
    })

    const hasFilters = computed(() => {
      return selectedType.value || selectedStatus.value
    })

    // Methods
    const loadNotifications = async () => {
      loading.value = true
      error.value = null

      try {
        const res = await notificationAPI.getUserNotifications()
        notifications.value = res.data || []
      } catch (e) {
        error.value = 'Không thể tải thông báo'
        console.error('Error loading notifications:', e)
      } finally {
        loading.value = false
      }
    }

    const applyFilters = () => {
      currentPage.value = 1
    }

    const clearFilters = () => {
      selectedType.value = ''
      selectedStatus.value = ''
      currentPage.value = 1
    }

    const markAsRead = async (notificationId) => {
      try {
        await notificationAPI.markAsRead(notificationId)
        const notification = notifications.value.find((n) => n.id === notificationId)
        if (notification) {
          notification.read = true
        }
      } catch (err) {
        console.error('Mark as read error:', err)
      }
    }

    const markAllAsRead = async () => {
      try {
        await notificationAPI.markAllAsRead()
        notifications.value.forEach((n) => (n.read = true))
      } catch (err) {
        console.error('Mark all as read error:', err)
      }
    }

    const handleNotificationAction = (notification) => {
      if (notification.actionUrl) {
        if (notification.actionUrl.startsWith('http')) {
          window.open(notification.actionUrl, '_blank')
        } else {
          router.push(notification.actionUrl)
        }
      }

      if (!notification.read) {
        markAsRead(notification.id)
      }
    }

    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
    }

    const getNotificationIcon = (type) => {
      const icons = {
        order: 'notification-icon order',
        product: 'notification-icon product',
        promotion: 'notification-icon promotion',
        system: 'notification-icon system',
      }
      return icons[type] || 'notification-icon'
    }

    const getNotificationEmoji = (type) => {
      const emojis = {
        order: '📦',
        product: '🛍️',
        promotion: '🎉',
        system: '⚙️',
      }
      return emojis[type] || '🔔'
    }

    const getActionText = (type) => {
      const actions = {
        order: 'Xem đơn hàng',
        product: 'Xem sản phẩm',
        promotion: 'Xem khuyến mãi',
        system: 'Xem chi tiết',
      }
      return actions[type] || 'Xem chi tiết'
    }

    const formatTime = (date) => {
      const now = new Date()
      const notificationDate = new Date(date)
      const diff = now - notificationDate
      const minutes = Math.floor(diff / 60000)

      if (minutes < 1) return 'Vừa xong'
      if (minutes < 60) return `${minutes} phút trước`
      if (minutes < 1440) return `${Math.floor(minutes / 60)} giờ trước`
      if (minutes < 43200) return `${Math.floor(minutes / 1440)} ngày trước`
      return notificationDate.toLocaleDateString('vi-VN')
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    // Lifecycle
    onMounted(() => {
      loadNotifications()
    })

    return {
      loading,
      error,
      notifications,
      selectedType,
      selectedStatus,
      currentPage,
      filteredNotifications,
      paginatedNotifications,
      totalPages,
      visiblePages,
      hasFilters,
      loadNotifications,
      applyFilters,
      clearFilters,
      markAsRead,
      markAllAsRead,
      handleNotificationAction,
      changePage,
      getNotificationIcon,
      getNotificationEmoji,
      getActionText,
      formatTime,
      formatCurrency,
    }
  },
}
</script>

<style scoped>
.notifications-page {
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

.notifications-icon {
  font-size: 3rem;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Filters */
.notifications-filters {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.filter-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: var(--text-primary);
}

.form-select {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-select:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
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

.btn-secondary {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent);
}

.btn-secondary:hover {
  background: rgba(0, 212, 255, 0.3);
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

.btn-sm {
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 0;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top: 3px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Notifications List */
.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.notifications-header h3 {
  color: var(--text-primary);
  margin: 0;
}

.notifications-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.notification-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.notification-item:hover {
  border-color: var(--text-accent);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 212, 255, 0.1);
}

.notification-item.unread {
  background: rgba(0, 212, 255, 0.1);
  border-color: var(--text-accent);
}

.notification-icon {
  display: flex;
  align-items: flex-start;
  padding-top: 0.25rem;
}

.notification-icon span {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 212, 255, 0.1);
}

.notification-icon.order {
  background: rgba(0, 212, 255, 0.1);
}

.notification-icon.product {
  background: rgba(16, 185, 129, 0.1);
}

.notification-icon.promotion {
  background: rgba(255, 215, 0, 0.1);
}

.notification-icon.system {
  background: rgba(139, 92, 246, 0.1);
}

.notification-content {
  flex: 1;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.notification-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.notification-time {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.unread-indicator {
  color: var(--text-accent);
  font-size: 0.8rem;
}

.notification-message {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.notification-data {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.data-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.data-item:last-child {
  margin-bottom: 0;
}

.data-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.data-value {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.85rem;
}

.notification-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.pagination-btn {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.1);
  border-color: var(--text-accent);
  color: var(--text-accent);
}

.pagination-btn.active {
  background: var(--text-accent);
  color: #1a1a2e;
  border-color: var(--text-accent);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-numbers {
  display: flex;
  gap: 0.5rem;
}

/* No Notifications */
.no-notifications {
  text-align: center;
  padding: 4rem 0;
}

.no-notifications-content {
  max-width: 400px;
  margin: 0 auto;
}

.no-notifications-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-notifications h3 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.no-notifications p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 2rem;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .filter-controls {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .notifications-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .notification-item {
    flex-direction: column;
    gap: 1rem;
  }

  .notification-header {
    flex-direction: column;
    gap: 0.5rem;
  }

  .notification-actions {
    justify-content: stretch;
  }

  .btn {
    flex: 1;
    justify-content: center;
  }

  .pagination {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .pagination-numbers {
    flex-wrap: wrap;
  }
}
</style>
