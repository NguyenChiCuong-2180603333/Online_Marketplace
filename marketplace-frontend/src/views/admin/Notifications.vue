<template>
  <div class="admin-notifications">
    <div class="page-header">
      <h1>Quản lý thông báo</h1>
      <button @click="showCreateModal = true" class="btn btn-primary">📢 Tạo thông báo mới</button>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📢</div>
        <div class="stat-content">
          <h3>{{ totalNotifications }}</h3>
          <p>Tổng thông báo</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ totalRecipients }}</h3>
          <p>Người nhận</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📤</div>
        <div class="stat-content">
          <h3>{{ sentNotifications }}</h3>
          <p>Đã gửi</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏰</div>
        <div class="stat-content">
          <h3>{{ scheduledNotifications }}</h3>
          <p>Lên lịch</p>
        </div>
      </div>
    </div>

    <!-- Notifications Table -->
    <div class="table-container">
      <div class="table-header">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm thông báo..."
            class="form-input"
          />
        </div>
        <div class="table-actions">
          <select v-model="typeFilter" class="form-select">
            <option value="">Tất cả loại</option>
            <option value="system">Hệ thống</option>
            <option value="promotion">Khuyến mãi</option>
            <option value="announcement">Thông báo</option>
            <option value="maintenance">Bảo trì</option>
          </select>
          <select v-model="statusFilter" class="form-select">
            <option value="">Tất cả trạng thái</option>
            <option value="draft">Nháp</option>
            <option value="scheduled">Lên lịch</option>
            <option value="sent">Đã gửi</option>
            <option value="failed">Lỗi</option>
          </select>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tiêu đề</th>
              <th>Loại</th>
              <th>Người nhận</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Ngày gửi</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="notification in filteredNotifications" :key="notification.id">
              <td>{{ notification.id }}</td>
              <td>
                <div class="notification-title">
                  <strong>{{ notification.title }}</strong>
                  <div class="notification-preview">
                    {{ truncateText(notification.content, 60) }}
                  </div>
                </div>
              </td>
              <td>
                <span :class="['type-badge', `type-${notification.type}`]">
                  {{ getTypeLabel(notification.type) }}
                </span>
              </td>
              <td>
                <div class="recipients-info">
                  <span class="recipient-count">{{ notification.recipientCount }}</span>
                  <span class="recipient-type">{{
                    getRecipientTypeLabel(notification.recipientType)
                  }}</span>
                </div>
              </td>
              <td>
                <span :class="['status-badge', `status-${notification.status}`]">
                  {{ getStatusLabel(notification.status) }}
                </span>
              </td>
              <td>{{ formatDate(notification.createdAt) }}</td>
              <td>
                <span v-if="notification.scheduledAt">
                  {{ formatDate(notification.scheduledAt) }}
                </span>
                <span v-else class="text-muted">-</span>
              </td>
              <td>
                <div class="action-buttons">
                  <button
                    @click="viewNotification(notification)"
                    class="btn btn-sm btn-outline"
                    title="Xem chi tiết"
                  >
                    👁️
                  </button>
                  <button
                    @click="editNotification(notification)"
                    class="btn btn-sm btn-outline"
                    title="Chỉnh sửa"
                  >
                    ✏️
                  </button>
                  <button
                    @click="sendNotification(notification)"
                    class="btn btn-sm btn-primary"
                    title="Gửi ngay"
                    :disabled="notification.status === 'sent'"
                  >
                    📤
                  </button>
                  <button
                    @click="deleteNotification(notification)"
                    class="btn btn-sm btn-danger"
                    title="Xóa"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="btn btn-outline"
        >
          ← Trước
        </button>

        <div class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="changePage(page)"
            :class="['btn', page === currentPage ? 'btn-primary' : 'btn-outline']"
          >
            {{ page }}
          </button>
        </div>

        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="btn btn-outline"
        >
          Sau →
        </button>
      </div>
    </div>

    <!-- Create/Edit Notification Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ showEditModal ? 'Chỉnh sửa thông báo' : 'Tạo thông báo mới' }}</h2>
          <button @click="closeModal" class="btn btn-icon">✕</button>
        </div>

        <form @submit.prevent="saveNotification" class="modal-body">
          <div class="form-group">
            <label>Tiêu đề *</label>
            <input
              v-model="notificationForm.title"
              type="text"
              required
              class="form-input"
              placeholder="Nhập tiêu đề thông báo"
            />
          </div>

          <div class="form-group">
            <label>Nội dung *</label>
            <textarea
              v-model="notificationForm.content"
              rows="4"
              required
              class="form-textarea"
              placeholder="Nhập nội dung thông báo..."
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Loại thông báo *</label>
              <select v-model="notificationForm.type" required class="form-select">
                <option value="system">Hệ thống</option>
                <option value="promotion">Khuyến mãi</option>
                <option value="announcement">Thông báo</option>
                <option value="maintenance">Bảo trì</option>
              </select>
            </div>

            <div class="form-group">
              <label>Người nhận *</label>
              <select v-model="notificationForm.recipientType" required class="form-select">
                <option value="all">Tất cả người dùng</option>
                <option value="customers">Chỉ khách hàng</option>
                <option value="sellers">Chỉ người bán</option>
                <option value="admins">Chỉ admin</option>
                <option value="specific">Người dùng cụ thể</option>
              </select>
            </div>
          </div>

          <div v-if="notificationForm.recipientType === 'specific'" class="form-group">
            <label>Danh sách email (phân cách bằng dấu phẩy)</label>
            <textarea
              v-model="notificationForm.recipientEmails"
              rows="2"
              class="form-textarea"
              placeholder="email1@example.com, email2@example.com"
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Thời gian gửi</label>
              <select v-model="notificationForm.scheduleType" class="form-select">
                <option value="now">Gửi ngay</option>
                <option value="scheduled">Lên lịch</option>
              </select>
            </div>

            <div v-if="notificationForm.scheduleType === 'scheduled'" class="form-group">
              <label>Ngày giờ gửi</label>
              <input
                v-model="notificationForm.scheduledAt"
                type="datetime-local"
                class="form-input"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="notificationForm.includeEmail" type="checkbox" />
              <span>Gửi qua email</span>
            </label>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="notificationForm.includePush" type="checkbox" />
              <span>Gửi thông báo đẩy</span>
            </label>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-outline">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu...' : showEditModal ? 'Cập nhật' : 'Tạo' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- View Notification Modal -->
    <div v-if="showViewModal" class="modal-overlay" @click="showViewModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết thông báo</h2>
          <button @click="showViewModal = false" class="btn btn-icon">✕</button>
        </div>

        <div class="modal-body">
          <div class="notification-detail">
            <div class="detail-row">
              <label>Tiêu đề:</label>
              <span>{{ selectedNotification?.title }}</span>
            </div>

            <div class="detail-row">
              <label>Nội dung:</label>
              <div class="content-display">{{ selectedNotification?.content }}</div>
            </div>

            <div class="detail-row">
              <label>Loại:</label>
              <span :class="['type-badge', `type-${selectedNotification?.type}`]">
                {{ getTypeLabel(selectedNotification?.type) }}
              </span>
            </div>

            <div class="detail-row">
              <label>Người nhận:</label>
              <span>{{ getRecipientTypeLabel(selectedNotification?.recipientType) }}</span>
            </div>

            <div class="detail-row">
              <label>Trạng thái:</label>
              <span :class="['status-badge', `status-${selectedNotification?.status}`]">
                {{ getStatusLabel(selectedNotification?.status) }}
              </span>
            </div>

            <div class="detail-row">
              <label>Ngày tạo:</label>
              <span>{{ formatDate(selectedNotification?.createdAt) }}</span>
            </div>

            <div v-if="selectedNotification?.scheduledAt" class="detail-row">
              <label>Ngày gửi:</label>
              <span>{{ formatDate(selectedNotification?.scheduledAt) }}</span>
            </div>

            <div v-if="selectedNotification?.sentAt" class="detail-row">
              <label>Đã gửi lúc:</label>
              <span>{{ formatDate(selectedNotification?.sentAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Xác nhận xóa</h2>
          <button @click="showDeleteModal = false" class="btn btn-icon">✕</button>
        </div>

        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa thông báo "{{ notificationToDelete?.title }}" không?</p>

          <div class="form-actions">
            <button @click="showDeleteModal = false" class="btn btn-outline">Hủy</button>
            <button @click="confirmDelete" class="btn btn-danger">Xóa</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { api } from '@/services/api'

export default {
  name: 'AdminNotifications',
  setup() {
    // Reactive data
    const notifications = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const currentPage = ref(1)
    const totalPages = ref(1)
    const searchQuery = ref('')
    const typeFilter = ref('')
    const statusFilter = ref('')

    // Modal states
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const showViewModal = ref(false)
    const showDeleteModal = ref(false)
    const selectedNotification = ref(null)
    const notificationToDelete = ref(null)

    // Form data
    const notificationForm = ref({
      title: '',
      content: '',
      type: 'system',
      recipientType: 'all',
      recipientEmails: '',
      scheduleType: 'now',
      scheduledAt: '',
      includeEmail: true,
      includePush: true,
    })

    // Computed
    const filteredNotifications = computed(() => {
      let filtered = notifications.value

      if (searchQuery.value) {
        filtered = filtered.filter(
          (notification) =>
            notification.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
            notification.content.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      }

      if (typeFilter.value) {
        filtered = filtered.filter((notification) => notification.type === typeFilter.value)
      }

      if (statusFilter.value) {
        filtered = filtered.filter((notification) => notification.status === statusFilter.value)
      }

      return filtered
    })

    const totalNotifications = computed(() => notifications.value.length)
    const totalRecipients = computed(() =>
      notifications.value.reduce((sum, notif) => sum + notif.recipientCount, 0)
    )
    const sentNotifications = computed(
      () => notifications.value.filter((notif) => notif.status === 'sent').length
    )
    const scheduledNotifications = computed(
      () => notifications.value.filter((notif) => notif.status === 'scheduled').length
    )

    const visiblePages = computed(() => {
      const pages = []
      const start = Math.max(1, currentPage.value - 2)
      const end = Math.min(totalPages.value, currentPage.value + 2)

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    })

    // Methods
    const loadNotifications = async () => {
      loading.value = true
      try {
        const response = await api.get('/admin/notifications', {
          params: {
            page: currentPage.value,
            limit: 20,
          },
        })
        notifications.value = response.data.notifications
        totalPages.value = response.data.totalPages
      } catch (error) {
        console.error('Error loading notifications:', error)
      } finally {
        loading.value = false
      }
    }

    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const viewNotification = (notification) => {
      selectedNotification.value = notification
      showViewModal.value = true
    }

    const editNotification = (notification) => {
      notificationForm.value = { ...notification }
      showEditModal.value = true
    }

    const saveNotification = async () => {
      saving.value = true
      try {
        if (showEditModal.value) {
          await api.put(`/admin/notifications/${notificationForm.value.id}`, notificationForm.value)
        } else {
          await api.post('/admin/notifications', notificationForm.value)
        }

        closeModal()
        loadNotifications()
      } catch (error) {
        console.error('Error saving notification:', error)
      } finally {
        saving.value = false
      }
    }

    const sendNotification = async (notification) => {
      try {
        await api.post(`/admin/notifications/${notification.id}/send`)
        loadNotifications()
      } catch (error) {
        console.error('Error sending notification:', error)
      }
    }

    const deleteNotification = (notification) => {
      notificationToDelete.value = notification
      showDeleteModal.value = true
    }

    const confirmDelete = async () => {
      if (!notificationToDelete.value) return

      try {
        await api.delete(`/admin/notifications/${notificationToDelete.value.id}`)
        showDeleteModal.value = false
        notificationToDelete.value = null
        loadNotifications()
      } catch (error) {
        console.error('Error deleting notification:', error)
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      showEditModal.value = false
      notificationForm.value = {
        title: '',
        content: '',
        type: 'system',
        recipientType: 'all',
        recipientEmails: '',
        scheduleType: 'now',
        scheduledAt: '',
        includeEmail: true,
        includePush: true,
      }
    }

    const getTypeLabel = (type) => {
      const labels = {
        system: 'Hệ thống',
        promotion: 'Khuyến mãi',
        announcement: 'Thông báo',
        maintenance: 'Bảo trì',
      }
      return labels[type] || type
    }

    const getRecipientTypeLabel = (type) => {
      const labels = {
        all: 'Tất cả người dùng',
        customers: 'Chỉ khách hàng',
        sellers: 'Chỉ người bán',
        admins: 'Chỉ admin',
        specific: 'Người dùng cụ thể',
      }
      return labels[type] || type
    }

    const getStatusLabel = (status) => {
      const labels = {
        draft: 'Nháp',
        scheduled: 'Lên lịch',
        sent: 'Đã gửi',
        failed: 'Lỗi',
      }
      return labels[status] || status
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    const formatDate = (date) => {
      if (!date) return '-'
      return new Date(date).toLocaleString('vi-VN')
    }

    // Lifecycle
    onMounted(() => {
      loadNotifications()
    })

    return {
      notifications,
      loading,
      saving,
      currentPage,
      totalPages,
      searchQuery,
      typeFilter,
      statusFilter,
      showCreateModal,
      showEditModal,
      showViewModal,
      showDeleteModal,
      selectedNotification,
      notificationToDelete,
      notificationForm,
      filteredNotifications,
      totalNotifications,
      totalRecipients,
      sentNotifications,
      scheduledNotifications,
      visiblePages,
      changePage,
      viewNotification,
      editNotification,
      saveNotification,
      sendNotification,
      deleteNotification,
      confirmDelete,
      closeModal,
      getTypeLabel,
      getRecipientTypeLabel,
      getStatusLabel,
      truncateText,
      formatDate,
    }
  },
}
</script>

<style scoped>
.admin-notifications {
  padding: 2rem;
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

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.search-box {
  flex: 1;
  max-width: 300px;
}

.table-actions {
  display: flex;
  gap: 1rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.form-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.notification-title {
  display: flex;
  flex-direction: column;
}

.notification-preview {
  font-size: 0.8rem;
  color: #666;
  margin-top: 0.25rem;
}

.type-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.type-system {
  background: #e3f2fd;
  color: #1976d2;
}

.type-promotion {
  background: #fff3e0;
  color: #f57c00;
}

.type-announcement {
  background: #e8f5e8;
  color: #2e7d32;
}

.type-maintenance {
  background: #ffebee;
  color: #c62828;
}

.recipients-info {
  display: flex;
  flex-direction: column;
}

.recipient-count {
  font-weight: 600;
  color: #333;
}

.recipient-type {
  font-size: 0.8rem;
  color: #666;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-draft {
  background: #f5f5f5;
  color: #666;
}

.status-scheduled {
  background: #fff3e0;
  color: #f57c00;
}

.status-sent {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-failed {
  background: #ffebee;
  color: #c62828;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
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

.btn-danger {
  background: #ff4757;
  color: white;
}

.btn-danger:hover {
  background: #e63946;
}

.btn-sm {
  padding: 0.4rem 0.6rem;
  font-size: 0.8rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  border-top: 1px solid #eee;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
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
  max-width: 600px;
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

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  resize: vertical;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-label input[type='checkbox'] {
  width: auto;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.notification-detail {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-row {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.detail-row label {
  font-weight: 600;
  color: #333;
  min-width: 120px;
}

.content-display {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  white-space: pre-wrap;
}

.text-muted {
  color: #666;
}

@media (max-width: 768px) {
  .admin-notifications {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .table-header {
    flex-direction: column;
    gap: 1rem;
  }

  .search-box {
    max-width: none;
  }

  .table-actions {
    flex-direction: column;
  }

  .action-buttons {
    flex-direction: column;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
