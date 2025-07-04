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
        <div class="stat-icon">📤</div>
        <div class="stat-content">
          <h3>{{ sentNotifications }}</h3>
          <p>Đã gửi</p>
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
            <option value="NEW_PRODUCT">Sản phẩm mới</option>
            <option value="PROMOTION">Khuyến mãi</option>
            <option value="ORDER_UPDATE">Cập nhật đơn hàng</option>
            <option value="SYSTEM_UPDATE">Hệ thống</option>
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
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="notification in filteredNotifications" :key="notification.id">
              <td>{{ truncateId(notification.id) }}</td>
              <td>
                <div class="notification-title">
                  <strong>{{ truncateTitleEllipsis(notification.title) }}</strong>
                </div>
              </td>
              <td>
                <span :class="['type-badge', `type-${notification.type}`]">
                  {{ getTypeLabel(notification.type) }}
                </span>
              </td>
              <td>
                <template v-if="!notification.userId">Tất cả người dùng</template>
                <template v-else-if="notification.userId">ID: {{ notification.userId }}</template>
                <template v-else>Không xác định</template>
              </td>
              <td>
                <span :class="['status-badge', `status-${notification.status}`]">
                  {{ getStatusLabel(notification.status) }}
                </span>
              </td>
              <td>{{ formatDate(notification.createdAt) }}</td>
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
              v-model="notificationForm.message"
              rows="4"
              required
              class="form-textarea"
              placeholder="Nhập nội dung thông báo..."
            ></textarea>
          </div>

          <div class="form-group">
            <label>Loại thông báo *</label>
            <select v-model="notificationForm.type" required class="form-select">
              <option value="NEW_PRODUCT">Sản phẩm mới</option>
              <option value="PROMOTION">Khuyến mãi</option>
              <option value="ORDER_UPDATE">Cập nhật đơn hàng</option>
              <option value="SYSTEM_UPDATE">Hệ thống</option>
            </select>
          </div>

          <div class="form-group">
            <label>Gửi cho user cụ thể (userId, để trống nếu gửi toàn hệ thống)</label>
            <input
              v-model="notificationForm.userId"
              type="text"
              class="form-input"
              placeholder="Nhập userId nếu muốn gửi cho 1 user"
            />
          </div>

          <div class="form-group">
            <label>relatedId (nếu có)</label>
            <input
              v-model="notificationForm.relatedId"
              type="text"
              class="form-input"
              placeholder="ID liên quan (orderId, productId, ... nếu có)"
            />
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
import api from '@/services/api'

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
      message: '',
      type: 'NEW_PRODUCT',
      userId: '',
      relatedId: '',
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

      return filtered
    })

    const totalNotifications = computed(() => notifications.value.length)
    const sentNotifications = computed(
      () => notifications.value.filter((notif) => notif.status === 'sent').length
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
        const response = await api.get('/notifications/admin/all')
        notifications.value = response.data.map((n) => ({
          ...n,
          content: n.content || n.message || '',
          createdAt: n.createdAt || n.createdDate || n.timestamp || '',
          recipientCount: n.recipientCount || 0,
          recipientType: n.recipientType || 'all',
          status: n.status || 'sent',
          scheduledAt: n.scheduledAt || '',
          sentAt: n.sentAt || '',
        }))
        totalPages.value = 1 // Không phân trang do backend trả về toàn bộ
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
      notificationForm.value = {
        id: notification.id,
        title: notification.title,
        message: notification.content || notification.message || '',
        type: notification.type,
        userId: notification.userId || '',
        relatedId: notification.relatedId || '',
      }
      showEditModal.value = true
    }

    const saveNotification = async () => {
      saving.value = true
      try {
        const payload = {
          title: notificationForm.value.title,
          message: notificationForm.value.message,
          type: notificationForm.value.type,
        }
        if (notificationForm.value.userId) payload.userId = notificationForm.value.userId
        if (notificationForm.value.relatedId) payload.relatedId = notificationForm.value.relatedId

        if (showEditModal.value) {
          await api.put(`/notifications/admin/${notificationForm.value.id}`, payload)        } else {
          if (notificationForm.value.userId) {
            await api.post('/notifications/send', payload)
          } else {
            await api.post('/notifications/broadcast', payload)
          }
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
        await api.delete(`/notifications/admin/${notificationToDelete.value.id}`)
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
        message: '',
        type: 'NEW_PRODUCT',
        userId: '',
        relatedId: '',
      }
    }

    const getTypeLabel = (type) => {
      const labels = {
        NEW_PRODUCT: 'Sản phẩm mới',
        PROMOTION: 'Khuyến mãi',
        ORDER_UPDATE: 'Cập nhật đơn hàng',
        SYSTEM_UPDATE: 'Hệ thống',
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

    const truncateId = (id) => {
      if (!id) return ''
      return id.length > 12 ? `${id.slice(0, 6)}...${id.slice(-5)}` : id
    }

    const truncateTitle = (title) => {
      if (!title) return ''
      return title.length > 20 ? title.slice(0, 20) + '...' : title
    }

    const truncateTitleEllipsis = (title) => {
      if (!title) return ''
      return title.length > 12 ? `${title.slice(0, 6)}...${title.slice(-5)}` : title
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
      showCreateModal,
      showEditModal,
      showViewModal,
      showDeleteModal,
      selectedNotification,
      notificationToDelete,
      notificationForm,
      filteredNotifications,
      totalNotifications,
      sentNotifications,
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
      truncateId,
      truncateTitle,
      truncateTitleEllipsis,
    }
  },
}
</script>

<style scoped>
.admin-notifications {
  padding: 2rem;
  color: #222;
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
  color: #222;
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
  color: #222;
  margin-bottom: 0.25rem;
}

.stat-content p {
  color: #444;
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
  color: #222;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table th:nth-child(2),
.data-table td:nth-child(2) {
  min-width: 180px;
  max-width: 300px;
  width: 220px;
}

.data-table th:nth-child(3),
.data-table td:nth-child(3) {
  min-width: 120px;
  max-width: 180px;
  width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.data-table th:nth-child(5),
.data-table td:nth-child(5) {
  min-width: 80px;
  max-width: 100px;
  width: 90px;
  text-align: center;
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-title {
  display: flex;
  flex-direction: column;
  color: #222;
}

.notification-preview {
  font-size: 0.8rem;
  color: #444;
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
  color: #222;
}

.recipient-type {
  font-size: 0.8rem;
  color: #666;
}

.status-badge {
  display: inline-block;
  min-width: 60px;
  max-width: 100%;
  padding: 0.2rem 0.6rem;
  font-size: 0.85rem;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  color: #222;
  min-width: 120px;
}

.content-display {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  white-space: pre-wrap;
  color: #222;
}

.text-muted {
  color: #444;
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
