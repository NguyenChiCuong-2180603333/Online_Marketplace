<template>
  <div class="admin-users">
    <div class="container">
      <div class="page-header">
        <h1>👥 Quản lý người dùng</h1>
        <p>Quản lý tất cả người dùng trong hệ thống</p>
      </div>

      <!-- Filter và Search -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm theo tên, email, số điện thoại..."
            class="search-input"
          >
          <button @click="searchUsers" class="search-btn">🔍</button>
        </div>
        
        <div class="filters">
          <select v-model="statusFilter" @change="filterUsers" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Bị khóa</option>
          </select>

          <select v-model="roleFilter" @change="filterUsers" class="filter-select">
            <option value="">Tất cả vai trò</option>
            <option value="USER">Khách hàng</option>
            <option value="ADMIN">Quản trị viên</option>
          </select>

          <select v-model="dateFilter" @change="filterUsers" class="filter-select">
            <option value="">Tất cả thời gian</option>
            <option value="today">Đăng ký hôm nay</option>
            <option value="week">Đăng ký tuần này</option>
            <option value="month">Đăng ký tháng này</option>
          </select>

          <button @click="exportUsers" class="export-btn">📊 Xuất Excel</button>
        </div>
      </div>

      <!-- Statistics -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <h3>{{ stats.total }}</h3>
            <p>Tổng người dùng</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <h3>{{ stats.active }}</h3>
            <p>Đang hoạt động</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">❌</div>
          <div class="stat-info">
            <h3>{{ stats.inactive }}</h3>
            <p>Bị khóa</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🆕</div>
          <div class="stat-info">
            <h3>{{ stats.newToday }}</h3>
            <p>Đăng ký hôm nay</p>
          </div>
        </div>
      </div>

      <!-- Users Table -->
      <div class="users-table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải danh sách người dùng...</p>
        </div>

        <div v-else-if="users.length === 0" class="empty-state">
          <div class="empty-icon">👥</div>
          <h3>Không có người dùng nào</h3>
          <p>Chưa có người dùng nào phù hợp với bộ lọc hiện tại</p>
        </div>

        <div v-else class="users-table">
          <table>
            <thead>
              <tr>
                <th>Avatar</th>
                <th>Thông tin</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày đăng ký</th>
                <th>Hoạt động cuối</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in paginatedUsers" :key="user.id" class="user-row">
                <td>
                  <div class="user-avatar">
                    <img 
                      :src="user.avatar || '/default-avatar.png'" 
                      :alt="user.firstName + ' ' + user.lastName"
                      class="avatar-image"
                    >
                  </div>
                </td>
                
                <td>
                  <div class="user-info">
                    <h4>{{ user.firstName }} {{ user.lastName }}</h4>
                    <p class="email">{{ user.email }}</p>
                    <p v-if="user.phone" class="phone">📞 {{ user.phone }}</p>
                    <span class="user-id">ID: {{ user.id.substring(0, 8) }}</span>
                  </div>
                </td>
                
                <td>
                  <div class="user-role">
                    <span class="role-badge" :class="getRoleClass(user.role)">
                      {{ getRoleText(user.role) }}
                    </span>
                  </div>
                </td>
                
                <td>
                  <div class="user-status">
                    <span class="status-badge" :class="user.enabled ? 'status-active' : 'status-inactive'">
                      {{ user.enabled ? '✅ Hoạt động' : '❌ Bị khóa' }}
                    </span>
                  </div>
                </td>
                
                <td>
                  <div class="join-date">
                    <div>{{ formatDate(user.createdAt) }}</div>
                    <small>{{ formatTime(user.createdAt) }}</small>
                  </div>
                </td>
                
                <td>
                  <div class="last-activity">
                    <div>{{ formatDate(user.lastLoginAt || user.createdAt) }}</div>
                    <small>{{ getTimeSince(user.lastLoginAt || user.createdAt) }}</small>
                  </div>
                </td>
                
                <td>
                  <div class="action-buttons">
                    <button @click="viewUserDetail(user)" class="btn-view" title="Xem chi tiết">👁️</button>
                    <button 
                      @click="toggleUserStatus(user)" 
                      :class="user.enabled ? 'btn-disable' : 'btn-enable'"
                      :title="user.enabled ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                    >
                      {{ user.enabled ? '🔒' : '🔓' }}
                    </button>
                    <button @click="viewUserOrders(user.id)" class="btn-orders" title="Xem đơn hàng">📦</button>
                    <button @click="sendNotification(user)" class="btn-notify" title="Gửi thông báo">📧</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            @click="currentPage = 1" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ⏪
          </button>
          <button 
            @click="currentPage--" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ◀️
          </button>
          
          <span class="page-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            @click="currentPage++" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ▶️
          </button>
          <button 
            @click="currentPage = totalPages" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ⏩
          </button>
        </div>
      </div>
    </div>

    <!-- User Detail Modal -->
    <div v-if="selectedUser" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết người dùng</h2>
          <button @click="closeModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="user-profile">
            <div class="profile-header">
              <img 
                :src="selectedUser.avatar || '/default-avatar.png'" 
                :alt="selectedUser.firstName + ' ' + selectedUser.lastName"
                class="profile-avatar"
              >
              <div class="profile-info">
                <h3>{{ selectedUser.firstName }} {{ selectedUser.lastName }}</h3>
                <p class="user-email">{{ selectedUser.email }}</p>
                <span class="role-badge" :class="getRoleClass(selectedUser.role)">
                  {{ getRoleText(selectedUser.role) }}
                </span>
              </div>
              <div class="profile-status">
                <span class="status-badge" :class="selectedUser.enabled ? 'status-active' : 'status-inactive'">
                  {{ selectedUser.enabled ? '✅ Hoạt động' : '❌ Bị khóa' }}
                </span>
              </div>
            </div>

            <div class="profile-details">
              <div class="detail-section">
                <h4>📱 Thông tin liên hệ</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>Email:</label>
                    <span>{{ selectedUser.email }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Số điện thoại:</label>
                    <span>{{ selectedUser.phone || 'Chưa cập nhật' }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>📊 Thống kê hoạt động</h4>
                <div class="stats-row">
                  <div class="stat-item">
                    <span class="stat-number">{{ userStats.totalOrders || 0 }}</span>
                    <span class="stat-label">Đơn hàng</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ formatCurrency(userStats.totalSpent || 0) }}</span>
                    <span class="stat-label">Tổng chi tiêu</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ userStats.loyaltyPoints || 0 }}</span>
                    <span class="stat-label">Điểm tích lũy</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>⏰ Thông tin thời gian</h4>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>Ngày đăng ký:</label>
                    <span>{{ formatDateTime(selectedUser.createdAt) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Đăng nhập cuối:</label>
                    <span>{{ formatDateTime(selectedUser.lastLoginAt) || 'Chưa từng đăng nhập' }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h4>📦 Đơn hàng gần đây</h4>
                <div v-if="userRecentOrders.length === 0" class="no-orders">
                  Chưa có đơn hàng nào
                </div>
                <div v-else class="recent-orders">
                  <div v-for="order in userRecentOrders" :key="order.id" class="order-item">
                    <div class="order-info">
                      <strong>#{{ order.id.substring(0, 8) }}</strong>
                      <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                    </div>
                    <div class="order-amount">{{ formatCurrency(order.totalAmount) }}</div>
                    <span class="order-status" :class="getStatusClass(order.status)">
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-actions">
              <button 
                @click="toggleUserStatus(selectedUser)" 
                :class="selectedUser.enabled ? 'btn-danger' : 'btn-success'"
              >
                {{ selectedUser.enabled ? '🔒 Khóa tài khoản' : '🔓 Kích hoạt tài khoản' }}
              </button>
              <button @click="viewAllUserOrders(selectedUser.id)" class="btn-primary">
                📦 Xem tất cả đơn hàng
              </button>
              <button @click="sendNotification(selectedUser)" class="btn-info">
                📧 Gửi thông báo
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Send Notification Modal -->
    <div v-if="showNotificationModal" class="modal-overlay" @click="closeNotificationModal">
      <div class="modal-content notification-modal" @click.stop>
        <div class="modal-header">
          <h2>📧 Gửi thông báo</h2>
          <button @click="closeNotificationModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="submitNotification">
            <div class="form-group">
              <label>Người nhận:</label>
              <div class="recipient-info">
                <img :src="notificationTarget.avatar || '/default-avatar.png'" class="recipient-avatar">
                <span>{{ notificationTarget.firstName }} {{ notificationTarget.lastName }}</span>
                <small>({{ notificationTarget.email }})</small>
              </div>
            </div>

            <div class="form-group">
              <label for="notificationTitle">Tiêu đề *</label>
              <input 
                id="notificationTitle"
                v-model="notificationForm.title" 
                type="text" 
                required
                placeholder="Nhập tiêu đề thông báo"
                class="form-input"
              >
            </div>

            <div class="form-group">
              <label for="notificationMessage">Nội dung *</label>
              <textarea 
                id="notificationMessage"
                v-model="notificationForm.message" 
                required
                placeholder="Nhập nội dung thông báo"
                class="form-textarea"
                rows="4"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="notificationType">Loại thông báo</label>
              <select id="notificationType" v-model="notificationForm.type" class="form-select">
                <option value="INFO">Thông tin</option>
                <option value="WARNING">Cảnh báo</option>
                <option value="SUCCESS">Thành công</option>
                <option value="ERROR">Lỗi</option>
              </select>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeNotificationModal" class="btn-cancel">
                Hủy
              </button>
              <button type="submit" class="btn-send" :disabled="sending">
                {{ sending ? 'Đang gửi...' : '📤 Gửi thông báo' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminUsers',
  data() {
    return {
      users: [],
      filteredUsers: [],
      loading: true,
      searchQuery: '',
      statusFilter: '',
      roleFilter: '',
      dateFilter: '',
      currentPage: 1,
      itemsPerPage: 15,
      selectedUser: null,
      userStats: {},
      userRecentOrders: [],
      showNotificationModal: false,
      notificationTarget: null,
      notificationForm: {
        title: '',
        message: '',
        type: 'INFO'
      },
      sending: false,
      stats: {
        total: 0,
        active: 0,
        inactive: 0,
        newToday: 0
      }
    }
  },
  computed: {
    paginatedUsers() {
      const start = (this.currentPage - 1) * this.itemsPerPage
      const end = start + this.itemsPerPage
      return this.filteredUsers.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.filteredUsers.length / this.itemsPerPage)
    }
  },
  async mounted() {
    await this.loadUsers()
    this.calculateStats()
  },
  methods: {
    async loadUsers() {
      try {
        this.loading = true
        const response = await axios.get('/api/admin/users', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.users = response.data
        this.filteredUsers = [...this.users]
      } catch (error) {
        console.error('Lỗi khi tải danh sách người dùng:', error)
        this.$toast.error('Không thể tải danh sách người dùng')
      } finally {
        this.loading = false
      }
    },

    async toggleUserStatus(user) {
      const action = user.enabled ? 'khóa' : 'kích hoạt'
      if (!confirm(`Bạn có chắc chắn muốn ${action} tài khoản ${user.email}?`)) return

      try {
        await axios.put(`/api/admin/users/${user.id}/toggle-status`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        // Update user status locally
        user.enabled = !user.enabled
        
        this.$toast.success(`Đã ${action} tài khoản thành công`)
        this.calculateStats()
      } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái người dùng:', error)
        this.$toast.error(`Không thể ${action} tài khoản`)
      }
    },

    async viewUserDetail(user) {
      this.selectedUser = user
      await this.loadUserStats(user.id)
      await this.loadUserRecentOrders(user.id)
    },

    async loadUserStats(userId) {
      try {
        // Load user specific stats (orders, spending, loyalty points)
        const response = await axios.get(`/api/admin/users/${userId}/stats`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.userStats = response.data
      } catch (error) {
        console.error('Lỗi khi tải thống kê người dùng:', error)
        this.userStats = {}
      }
    },

    async loadUserRecentOrders(userId) {
      try {
        const response = await axios.get(`/api/admin/users/${userId}/orders?limit=5`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.userRecentOrders = response.data
      } catch (error) {
        console.error('Lỗi khi tải đơn hàng gần đây:', error)
        this.userRecentOrders = []
      }
    },

    viewAllUserOrders(userId) {
      this.$router.push(`/admin/orders?userId=${userId}`)
    },

    sendNotification(user) {
      this.notificationTarget = user
      this.showNotificationModal = true
      this.notificationForm = {
        title: '',
        message: '',
        type: 'INFO'
      }
    },

    async submitNotification() {
      try {
        this.sending = true
        await axios.post('/api/notifications/send', {
          recipientId: this.notificationTarget.id,
          title: this.notificationForm.title,
          message: this.notificationForm.message,
          type: this.notificationForm.type
        }, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.$toast.success('Đã gửi thông báo thành công')
        this.closeNotificationModal()
      } catch (error) {
        console.error('Lỗi khi gửi thông báo:', error)
        this.$toast.error('Không thể gửi thông báo')
      } finally {
        this.sending = false
      }
    },

    filterUsers() {
      let filtered = [...this.users]

      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(user => 
          user.email.toLowerCase().includes(query) ||
          user.firstName.toLowerCase().includes(query) ||
          user.lastName.toLowerCase().includes(query) ||
          (user.phone && user.phone.includes(query))
        )
      }

      if (this.statusFilter) {
        filtered = filtered.filter(user => 
          this.statusFilter === 'active' ? user.enabled : !user.enabled
        )
      }

      if (this.roleFilter) {
        filtered = filtered.filter(user => user.role === this.roleFilter)
      }

      if (this.dateFilter) {
        const now = new Date()
        filtered = filtered.filter(user => {
          const userDate = new Date(user.createdAt)
          switch (this.dateFilter) {
            case 'today':
              return userDate.toDateString() === now.toDateString()
            case 'week':
              const weekAgo = new Date(now - 7 * 24 * 60 * 60 * 1000)
              return userDate >= weekAgo
            case 'month':
              const monthAgo = new Date(now - 30 * 24 * 60 * 60 * 1000)
              return userDate >= monthAgo
            default:
              return true
          }
        })
      }

      this.filteredUsers = filtered
      this.currentPage = 1
    },

    searchUsers() {
      this.filterUsers()
    },

    calculateStats() {
      this.stats.total = this.users.length
      this.stats.active = this.users.filter(u => u.enabled).length
      this.stats.inactive = this.users.filter(u => !u.enabled).length
      
      const today = new Date().toDateString()
      this.stats.newToday = this.users.filter(u => 
        new Date(u.createdAt).toDateString() === today
      ).length
    },

    closeModal() {
      this.selectedUser = null
      this.userStats = {}
      this.userRecentOrders = []
    },

    closeNotificationModal() {
      this.showNotificationModal = false
      this.notificationTarget = null
    },

    viewUserOrders(userId) {
      this.$router.push(`/admin/orders?userId=${userId}`)
    },

    exportUsers() {
      // Implement Excel export
      this.$toast.info('Tính năng xuất Excel đang được phát triển')
    },

    getRoleClass(role) {
      return {
        'USER': 'role-user',
        'ADMIN': 'role-admin'
      }[role] || 'role-default'
    },

    getRoleText(role) {
      return {
        'USER': '👤 Khách hàng',
        'ADMIN': '👑 Quản trị viên'
      }[role] || role
    },

    getStatusClass(status) {
      const statusClasses = {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-processing',
        'SHIPPED': 'status-shipped',
        'DELIVERED': 'status-delivered',
        'CANCELLED': 'status-cancelled'
      }
      return statusClasses[status] || 'status-default'
    },

    getStatusText(status) {
      const statusTexts = {
        'PENDING': 'Chờ xử lý',
        'PROCESSING': 'Đang xử lý',
        'SHIPPED': 'Đã gửi',
        'DELIVERED': 'Đã giao',
        'CANCELLED': 'Đã hủy'
      }
      return statusTexts[status] || status
    },

    formatCurrency(amount) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('vi-VN')
    },

    formatTime(dateString) {
      return new Date(dateString).toLocaleTimeString('vi-VN')
    },

    formatDateTime(dateString) {
      return new Date(dateString).toLocaleString('vi-VN')
    },

    getTimeSince(dateString) {
      const now = new Date()
      const date = new Date(dateString)
      const diffInSeconds = Math.floor((now - date) / 1000)
      
      if (diffInSeconds < 60) return 'Vừa mới'
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} phút trước`
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} giờ trước`
      return `${Math.floor(diffInSeconds / 86400)} ngày trước`
    }
  }
}
</script>

<style scoped>
.admin-users {
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 5px;
}

.page-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px 0 0 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #3498db;
}

.search-btn {
  padding: 12px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #2980b9;
}

.filters {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-select {
  padding: 10px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  background: white;
  cursor: pointer;
}

.export-btn {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.export-btn:hover {
  background: #219a52;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 3rem;
  opacity: 0.8;
}

.stat-info h3 {
  font-size: 2rem;
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 1rem;
}

.users-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.5;
}

.users-table {
  overflow-x: auto;
}

.users-table table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  background: #f8f9fa;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e1e5e9;
}

.users-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
}

.user-row:hover {
  background: #f8f9fa;
}

.user-avatar {
  display: flex;
  justify-content: center;
}

.avatar-image {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e1e5e9;
}

.user-info h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 16px;
}

.user-info .email {
  color: #3498db;
  margin: 2px 0;
  font-size: 14px;
}

.user-info .phone {
  color: #7f8c8d;
  margin: 2px 0;
  font-size: 13px;
}

.user-info .user-id {
  color: #95a5a6;
  font-size: 12px;
  font-family: monospace;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.role-user {
  background: #e8f4fd;
  color: #2980b9;
}

.role-admin {
  background: #ffeaa7;
  color: #d63031;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.join-date div {
  font-weight: 500;
  color: #2c3e50;
}

.join-date small {
  color: #7f8c8d;
}

.last-activity div {
  font-weight: 500;
  color: #2c3e50;
}

.last-activity small {
  color: #7f8c8d;
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.action-buttons button {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-width: 32px;
}

.btn-view {
  background: #3498db;
  color: white;
}

.btn-view:hover {
  background: #2980b9;
}

.btn-enable {
  background: #27ae60;
  color: white;
}

.btn-enable:hover {
  background: #229954;
}

.btn-disable {
  background: #e74c3c;
  color: white;
}

.btn-disable:hover {
  background: #c0392b;
}

.btn-orders {
  background: #f39c12;
  color: white;
}

.btn-orders:hover {
  background: #e67e22;
}

.btn-notify {
  background: #9b59b6;
  color: white;
}

.btn-notify:hover {
  background: #8e44ad;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e1e5e9;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 15px;
  font-weight: 500;
  color: #2c3e50;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 900px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.notification-modal {
  max-width: 600px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h2 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 5px;
  color: #7f8c8d;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 25px;
}

.user-profile {
  max-width: 100%;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.profile-info {
  flex: 1;
}

.profile-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.5rem;
}

.user-email {
  color: #3498db;
  font-size: 1.1rem;
  margin: 0 0 10px 0;
}

.profile-status {
  text-align: right;
}

.profile-details {
  display: grid;
  gap: 25px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.1rem;
  padding-bottom: 8px;
  border-bottom: 2px solid #3498db;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item label {
  font-weight: 600;
  color: #7f8c8d;
  font-size: 14px;
}

.detail-item span {
  color: #2c3e50;
  font-size: 15px;
}

.stats-row {
  display: flex;
  gap: 30px;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  flex: 1;
}

.stat-number {
  display: block;
  font-size: 1.8rem;
  font-weight: 700;
  color: #3498db;
  margin-bottom: 5px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 14px;
}

.no-orders {
  text-align: center;
  color: #7f8c8d;
  font-style: italic;
  padding: 20px;
}

.recent-orders {
  display: grid;
  gap: 10px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.order-info strong {
  color: #2c3e50;
  margin-right: 10px;
}

.order-date {
  color: #7f8c8d;
  font-size: 13px;
}

.order-amount {
  font-weight: 600;
  color: #27ae60;
}

.order-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-pending { background: #fff3cd; color: #856404; }
.status-processing { background: #d4edda; color: #155724; }
.status-shipped { background: #cce5ff; color: #004085; }
.status-delivered { background: #d1ecf1; color: #0c5460; }
.status-cancelled { background: #f8d7da; color: #721c24; }

.modal-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.modal-actions button {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover {
  background: #229954;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background: #c0392b;
}

.btn-info {
  background: #9b59b6;
  color: white;
}

.btn-info:hover {
  background: #8e44ad;
}

/* Notification Modal Styles */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.recipient-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px solid #e1e5e9;
}

.recipient-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.recipient-info span {
  font-weight: 500;
  color: #2c3e50;
}

.recipient-info small {
  color: #7f8c8d;
  margin-left: auto;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  border-color: #3498db;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.btn-cancel {
  padding: 12px 20px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.btn-send {
  padding: 12px 25px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-send:hover:not(:disabled) {
  background: #2980b9;
}

.btn-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .stats-row {
    flex-direction: column;
    gap: 15px;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-users {
    padding: 10px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filters {
    flex-wrap: wrap;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .users-table {
    font-size: 14px;
  }
  
  .users-table th,
  .users-table td {
    padding: 10px 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .modal-body {
    padding: 15px;
  }

  .modal-actions {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-info h3 {
    font-size: 1.5rem;
  }

  .avatar-image {
    width: 40px;
    height: 40px;
  }

  .profile-avatar {
    width: 60px;
    height: 60px;
  }
}