<template>
  <div class="admin-users-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">👥 Quản lý Người dùng</h1>
        <p class="page-subtitle">Quản lý tài khoản và quyền hạn của tất cả người dùng</p>
      </div>
      
      <div class="header-actions">
        <button @click="refreshUsers" class="refresh-btn" :disabled="loading">
          {{ loading ? '⏳ Đang tải...' : '🔄 Làm mới' }}
        </button>
        <button @click="exportUsers" class="export-btn">
          📊 Xuất Excel
        </button>
        <button @click="showCreateUserModal = true" class="create-btn">
          ➕ Thêm người dùng
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.total }}</h3>
            <p class="stat-label">Tổng người dùng</p>
            <span class="stat-change positive">+{{ userStats.newThisMonth }} tháng này</span>
          </div>
        </div>
        
        <div class="stat-card active">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.active }}</h3>
            <p class="stat-label">Đang hoạt động</p>
            <span class="stat-percentage">{{ getPercentage(userStats.active, userStats.total) }}%</span>
          </div>
        </div>
        
        <div class="stat-card blocked">
          <div class="stat-icon">🚫</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.blocked }}</h3>
            <p class="stat-label">Bị khóa</p>
            <span class="stat-percentage">{{ getPercentage(userStats.blocked, userStats.total) }}%</span>
          </div>
        </div>
        
        <div class="stat-card admins">
          <div class="stat-icon">👑</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.admins }}</h3>
            <p class="stat-label">Quản trị viên</p>
          </div>
        </div>
        
        <div class="stat-card vip">
          <div class="stat-icon">💎</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.vip }}</h3>
            <p class="stat-label">VIP</p>
            <span class="stat-percentage">{{ getPercentage(userStats.vip, userStats.total) }}%</span>
          </div>
        </div>
        
        <div class="stat-card verified">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ userStats.verified }}</h3>
            <p class="stat-label">Đã xác thực</p>
            <span class="stat-percentage">{{ getPercentage(userStats.verified, userStats.total) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="filters-section">
      <div class="filters-grid">
        <!-- Status Filter -->
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="filters.status" @change="applyFilters">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="blocked">Bị khóa</option>
            <option value="pending">Chờ xác thực</option>
          </select>
        </div>
        
        <!-- Role Filter -->
        <div class="filter-group">
          <label>Vai trò:</label>
          <select v-model="filters.role" @change="applyFilters">
            <option value="">Tất cả</option>
            <option value="USER">Người dùng</option>
            <option value="ADMIN">Quản trị viên</option>
            <option value="SELLER">Người bán</option>
          </select>
        </div>
        
        <!-- VIP Filter -->
        <div class="filter-group">
          <label>Loại tài khoản:</label>
          <select v-model="filters.accountType" @change="applyFilters">
            <option value="">Tất cả</option>
            <option value="vip">VIP</option>
            <option value="regular">Thường</option>
          </select>
        </div>
        
        <!-- Date Range Filter -->
        <div class="filter-group">
          <label>Ngày tham gia từ:</label>
          <input 
            v-model="filters.startDate" 
            type="date" 
            @change="applyFilters"
          />
        </div>
        
        <div class="filter-group">
          <label>Đến:</label>
          <input 
            v-model="filters.endDate" 
            type="date" 
            @change="applyFilters"
          />
        </div>
        
        <!-- Search -->
        <div class="filter-group search-group">
          <label>Tìm kiếm:</label>
          <input 
            v-model="filters.search" 
            type="text" 
            placeholder="Tên, email, điện thoại..."
            @input="debounceSearch"
          />
        </div>
        
        <!-- Sort -->
        <div class="filter-group">
          <label>Sắp xếp:</label>
          <select v-model="sortBy" @change="applySort">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="nameAsc">Tên A → Z</option>
            <option value="nameDesc">Tên Z → A</option>
            <option value="mostActive">Hoạt động nhiều</option>
          </select>
        </div>
        
        <div class="filter-actions">
          <button @click="clearFilters" class="clear-btn">
            🗑️ Xóa bộ lọc
          </button>
        </div>
      </div>
    </div>

    <!-- Users Table -->
    <div class="users-section">
      <div class="table-container">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">⏳ Đang tải người dùng...</div>
        </div>
        
        <div v-else-if="error" class="error-state">
          <div class="error-content">
            <div class="error-icon">⚠️</div>
            <h3>Có lỗi xảy ra</h3>
            <p>{{ error }}</p>
            <button @click="refreshUsers" class="retry-btn">Thử lại</button>
          </div>
        </div>
        
        <table v-else-if="paginatedUsers.length > 0" class="users-table">
          <thead>
            <tr>
              <th>Người dùng</th>
              <th>Email & Điện thoại</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Hoạt động</th>
              <th>Ngày tham gia</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in paginatedUsers" :key="user.id" class="user-row">
              <td>
                <div class="user-info">
                  <div class="user-avatar">
                    <img :src="user.avatar || '/default-avatar.jpg'" :alt="user.name" />
                    <div v-if="user.isOnline" class="online-indicator"></div>
                  </div>
                  <div class="user-details">
                    <h4 class="user-name">
                      {{ user.name }}
                      <span v-if="user.isVip" class="vip-badge">👑 VIP</span>
                      <span v-if="user.isVerified" class="verified-badge">✅</span>
                    </h4>
                    <p class="user-id">ID: {{ user.id }}</p>
                  </div>
                </div>
              </td>
              
              <td>
                <div class="contact-info">
                  <div class="email">📧 {{ user.email }}</div>
                  <div class="phone">📱 {{ user.phone || 'Chưa cập nhật' }}</div>
                </div>
              </td>
              
              <td>
                <select 
                  v-model="user.role" 
                  @change="updateUserRole(user)"
                  class="role-select"
                  :class="user.role.toLowerCase()"
                >
                  <option value="USER">Người dùng</option>
                  <option value="SELLER">Người bán</option>
                  <option value="ADMIN">Quản trị viên</option>
                </select>
              </td>
              
              <td>
                <div class="status-container">
                  <span class="status-badge" :class="user.status.toLowerCase()">
                    {{ getStatusText(user.status) }}
                  </span>
                  <div class="status-actions">
                    <button 
                      v-if="user.status === 'active'"
                      @click="toggleUserStatus(user)"
                      class="status-btn block-btn"
                      title="Khóa tài khoản"
                    >
                      🚫
                    </button>
                    <button 
                      v-else
                      @click="toggleUserStatus(user)"
                      class="status-btn unblock-btn"
                      title="Mở khóa tài khoản"
                    >
                      ✅
                    </button>
                  </div>
                </div>
              </td>
              
              <td>
                <div class="activity-info">
                  <div class="orders-count">📋 {{ user.totalOrders }} đơn hàng</div>
                  <div class="total-spent">💰 {{ formatPrice(user.totalSpent) }}</div>
                  <div class="last-login">🕒 {{ formatDateTime(user.lastLogin) }}</div>
                </div>
              </td>
              
              <td>
                <div class="join-date">
                  <div class="date">{{ formatDate(user.createdAt) }}</div>
                  <div class="time">{{ formatTime(user.createdAt) }}</div>
                </div>
              </td>
              
              <td>
                <div class="action-buttons">
                  <button 
                    @click="viewUserDetails(user)" 
                    class="action-btn view-btn"
                    title="Xem chi tiết"
                  >
                    👁️
                  </button>
                  
                  <button 
                    @click="editUser(user)" 
                    class="action-btn edit-btn"
                    title="Chỉnh sửa"
                  >
                    ✏️
                  </button>
                  
                  <button 
                    @click="toggleVipStatus(user)" 
                    class="action-btn vip-btn"
                    :class="{ active: user.isVip }"
                    :title="user.isVip ? 'Hủy VIP' : 'Nâng cấp VIP'"
                  >
                    👑
                  </button>
                  
                  <button 
                    @click="deleteUser(user)" 
                    class="action-btn delete-btn"
                    title="Xóa tài khoản"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-else class="no-users">
          <div class="no-users-content">
            <div class="no-users-icon">👥</div>
            <h3>Không tìm thấy người dùng nào</h3>
            <p>Thử thay đổi bộ lọc hoặc kiểm tra lại từ khóa tìm kiếm</p>
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
        
        <div class="pagination-info">
          Trang {{ currentPage }} / {{ totalPages }} 
          ({{ filteredUsers.length }} người dùng)
        </div>
        
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

    <!-- User Details Modal -->
    <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
      <div class="modal-content user-modal" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết người dùng</h2>
          <button @click="closeUserModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body" v-if="selectedUser">
          <div class="user-details-grid">
            <!-- Profile Information -->
            <div class="detail-section">
              <h3>👤 Thông tin cá nhân</h3>
              <div class="profile-details">
                <div class="profile-avatar">
                  <img :src="selectedUser.avatar || '/default-avatar.jpg'" :alt="selectedUser.name" />
                  <div class="avatar-badges">
                    <span v-if="selectedUser.isVip" class="badge vip">👑 VIP</span>
                    <span v-if="selectedUser.isVerified" class="badge verified">✅ Xác thực</span>
                  </div>
                </div>
                <div class="profile-info">
                  <p><strong>Tên:</strong> {{ selectedUser.name }}</p>
                  <p><strong>Email:</strong> {{ selectedUser.email }}</p>
                  <p><strong>Điện thoại:</strong> {{ selectedUser.phone || 'Chưa cập nhật' }}</p>
                  <p><strong>Địa chỉ:</strong> {{ selectedUser.address || 'Chưa cập nhật' }}</p>
                  <p><strong>Ngày sinh:</strong> {{ selectedUser.birthday || 'Chưa cập nhật' }}</p>
                </div>
              </div>
            </div>
            
            <!-- Account Stats -->
            <div class="detail-section">
              <h3>📊 Thống kê tài khoản</h3>
              <div class="stats-list">
                <div class="stat-item">
                  <span class="stat-label">Tổng đơn hàng:</span>
                  <span class="stat-value">{{ selectedUser.totalOrders }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Tổng chi tiêu:</span>
                  <span class="stat-value">{{ formatPrice(selectedUser.totalSpent) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Đánh giá:</span>
                  <span class="stat-value">{{ selectedUser.reviewCount }} đánh giá</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">Điểm loyalty:</span>
                  <span class="stat-value">{{ selectedUser.loyaltyPoints }} điểm</span>
                </div>
              </div>
            </div>
            
            <!-- Activity Timeline -->
            <div class="detail-section full-width">
              <h3>📅 Hoạt động gần đây</h3>
              <div class="activity-timeline">
                <div v-for="activity in selectedUser.recentActivities" :key="activity.id" class="activity-item">
                  <div class="activity-icon">{{ activity.icon }}</div>
                  <div class="activity-content">
                    <h4>{{ activity.title }}</h4>
                    <p>{{ activity.description }}</p>
                    <span class="activity-date">{{ formatDateTime(activity.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="editUser(selectedUser)" class="btn-secondary">
            ✏️ Chỉnh sửa
          </button>
          <button @click="closeUserModal" class="btn-primary">
            Đóng
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit User Modal -->
    <div v-if="showCreateUserModal || showEditUserModal" class="modal-overlay" @click="closeCreateUserModal">
      <div class="modal-content create-user-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ showEditUserModal ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới' }}</h2>
          <button @click="closeCreateUserModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveUser" class="user-form">
            <div class="form-grid">
              <div class="form-group">
                <label>Họ tên *</label>
                <input v-model="userForm.name" type="text" required />
              </div>
              
              <div class="form-group">
                <label>Email *</label>
                <input v-model="userForm.email" type="email" required />
              </div>
              
              <div class="form-group">
                <label>Điện thoại</label>
                <input v-model="userForm.phone" type="tel" />
              </div>
              
              <div class="form-group">
                <label>Vai trò *</label>
                <select v-model="userForm.role" required>
                  <option value="USER">Người dùng</option>
                  <option value="SELLER">Người bán</option>
                  <option value="ADMIN">Quản trị viên</option>
                </select>
              </div>
              
              <div v-if="!showEditUserModal" class="form-group">
                <label>Mật khẩu *</label>
                <input v-model="userForm.password" type="password" required />
              </div>
              
              <div class="form-group">
                <label>Địa chỉ</label>
                <textarea v-model="userForm.address" rows="3"></textarea>
              </div>
            </div>
            
            <div class="form-checkboxes">
              <label class="checkbox-label">
                <input v-model="userForm.isVip" type="checkbox" />
                <span>Tài khoản VIP</span>
              </label>
              
              <label class="checkbox-label">
                <input v-model="userForm.isVerified" type="checkbox" />
                <span>Đã xác thực</span>
              </label>
              
              <label class="checkbox-label">
                <input v-model="userForm.isActive" type="checkbox" />
                <span>Kích hoạt tài khoản</span>
              </label>
            </div>
          </form>
        </div>
        
        <div class="modal-footer">
          <button @click="closeCreateUserModal" class="btn-secondary">
            Hủy
          </button>
          <button @click="saveUser" class="btn-primary" :disabled="saving">
            {{ saving ? '⏳ Đang lưu...' : (showEditUserModal ? 'Cập nhật' : 'Tạo tài khoản') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminAPI } from '@/services/api'

export default {
  name: 'AdminUsers',
  setup() {
    const router = useRouter()
    
    // Reactive data
    const users = ref([])
    const loading = ref(false)
    const error = ref(null)
    const selectedUser = ref(null)
    const showUserModal = ref(false)
    const showCreateUserModal = ref(false)
    const showEditUserModal = ref(false)
    const saving = ref(false)
    
    // Stats
    const userStats = ref({
      total: 0,
      active: 0,
      blocked: 0,
      admins: 0,
      vip: 0,
      verified: 0,
      newThisMonth: 0
    })
    
    // Filters
    const filters = ref({
      status: '',
      role: '',
      accountType: '',
      startDate: '',
      endDate: '',
      search: ''
    })
    
    const sortBy = ref('newest')
    
    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = 10
    
    // User form
    const userForm = ref({
      name: '',
      email: '',
      phone: '',
      role: 'USER',
      password: '',
      address: '',
      isVip: false,
      isVerified: false,
      isActive: true
    })
    
    // Debounce timer for search
    let searchTimeout = null
    
    // Computed properties
    const filteredUsers = computed(() => {
      let filtered = [...users.value]
      
      // Status filter
      if (filters.value.status) {
        filtered = filtered.filter(user => user.status === filters.value.status)
      }
      
      // Role filter
      if (filters.value.role) {
        filtered = filtered.filter(user => user.role === filters.value.role)
      }
      
      // Account type filter
      if (filters.value.accountType) {
        if (filters.value.accountType === 'vip') {
          filtered = filtered.filter(user => user.isVip)
        } else if (filters.value.accountType === 'regular') {
          filtered = filtered.filter(user => !user.isVip)
        }
      }
      
      // Date filter
      if (filters.value.startDate) {
        filtered = filtered.filter(user => 
          new Date(user.createdAt) >= new Date(filters.value.startDate)
        )
      }
      
      if (filters.value.endDate) {
        filtered = filtered.filter(user => 
          new Date(user.createdAt) <= new Date(filters.value.endDate)
        )
      }
      
      // Search filter
      if (filters.value.search) {
        const searchTerm = filters.value.search.toLowerCase()
        filtered = filtered.filter(user => 
          user.name.toLowerCase().includes(searchTerm) ||
          user.email.toLowerCase().includes(searchTerm) ||
          (user.phone && user.phone.includes(searchTerm))
        )
      }
      
      return filtered
    })
    
    const sortedUsers = computed(() => {
      const sorted = [...filteredUsers.value]
      
      switch (sortBy.value) {
        case 'oldest':
          return sorted.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
        case 'nameAsc':
          return sorted.sort((a, b) => a.name.localeCompare(b.name))
        case 'nameDesc':
          return sorted.sort((a, b) => b.name.localeCompare(a.name))
        case 'mostActive':
          return sorted.sort((a, b) => b.totalOrders - a.totalOrders)
        case 'newest':
        default:
          return sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }
    })
    
    const paginatedUsers = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      return sortedUsers.value.slice(start, start + itemsPerPage)
    })
    
    const totalPages = computed(() => 
      Math.ceil(sortedUsers.value.length / itemsPerPage)
    )
    
    const visiblePages = computed(() => {
      const total = totalPages.value
      const current = currentPage.value
      const delta = 2
      
      let start = Math.max(1, current - delta)
      let end = Math.min(total, current + delta)
      
      if (end - start < delta * 2) {
        if (start === 1) {
          end = Math.min(total, start + delta * 2)
        } else {
          start = Math.max(1, end - delta * 2)
        }
      }
      
      return Array.from({ length: end - start + 1 }, (_, i) => start + i)
    })
    
    // Methods
    const loadUsers = async () => {
      loading.value = true
      error.value = null
      
      try {
        const response = await adminAPI.getUsers()
        users.value = response.data.users || response.data
        calculateStats()
      } catch (err) {
        error.value = 'Không thể tải danh sách người dùng'
        console.error('Error loading users:', err)
      } finally {
        loading.value = false
      }
    }
    
    const calculateStats = () => {
      const stats = {
        total: users.value.length,
        active: 0,
        blocked: 0,
        admins: 0,
        vip: 0,
        verified: 0,
        newThisMonth: 0
      }
      
      const oneMonthAgo = new Date()
      oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1)
      
      users.value.forEach(user => {
        if (user.status === 'active') stats.active++
        if (user.status === 'blocked') stats.blocked++
        if (user.role === 'ADMIN') stats.admins++
        if (user.isVip) stats.vip++
        if (user.isVerified) stats.verified++
        if (new Date(user.createdAt) > oneMonthAgo) stats.newThisMonth++
      })
      
      userStats.value = stats
    }
    
    const toggleUserStatus = async (user) => {
      const newStatus = user.status === 'active' ? 'blocked' : 'active'
      const action = newStatus === 'blocked' ? 'khóa' : 'mở khóa'
      
      if (confirm(`Bạn có chắc chắn muốn ${action} tài khoản của ${user.name}?`)) {
        try {
          await adminAPI.toggleUserStatus(user.id)
          user.status = newStatus
          calculateStats()
        } catch (err) {
          console.error('Error toggling user status:', err)
        }
      }
    }
    
    const updateUserRole = async (user) => {
      try {
        // API call to update user role
        console.log('Updating user role:', user.id, user.role)
      } catch (err) {
        console.error('Error updating user role:', err)
        await loadUsers()
      }
    }
    
    const toggleVipStatus = async (user) => {
      user.isVip = !user.isVip
      calculateStats()
      // API call would go here
    }
    
    const viewUserDetails = (user) => {
      selectedUser.value = {
        ...user,
        recentActivities: [
          {
            id: 1,
            icon: '🛒',
            title: 'Đặt hàng thành công',
            description: 'Đã đặt đơn hàng #12345 với tổng giá trị 2.500.000đ',
            createdAt: '2024-12-22T10:30:00'
          },
          {
            id: 2,
            icon: '⭐',
            title: 'Đánh giá sản phẩm',
            description: 'Đã đánh giá 5 sao cho sản phẩm iPhone 15 Pro',
            createdAt: '2024-12-21T15:20:00'
          }
        ]
      }
      showUserModal.value = true
    }
    
    const closeUserModal = () => {
      showUserModal.value = false
      selectedUser.value = null
    }
    
    const editUser = (user) => {
      userForm.value = {
        ...user,
        password: '' // Don't include password in edit form
      }
      showEditUserModal.value = true
      showUserModal.value = false
    }
    
    const deleteUser = async (user) => {
      if (confirm(`Bạn có chắc chắn muốn xóa tài khoản của ${user.name}? Hành động này không thể hoàn tác.`)) {
        try {
          // API call to delete user
          users.value = users.value.filter(u => u.id !== user.id)
          calculateStats()
        } catch (err) {
          console.error('Error deleting user:', err)
        }
      }
    }
    
    const closeCreateUserModal = () => {
      showCreateUserModal.value = false
      showEditUserModal.value = false
      resetUserForm()
    }
    
    const resetUserForm = () => {
      userForm.value = {
        name: '',
        email: '',
        phone: '',
        role: 'USER',
        password: '',
        address: '',
        isVip: false,
        isVerified: false,
        isActive: true
      }
    }
    
    const saveUser = async () => {
      saving.value = true
      try {
        if (showEditUserModal.value) {
          // Update existing user
          console.log('Updating user:', userForm.value)
        } else {
          // Create new user
          console.log('Creating user:', userForm.value)
        }
        closeCreateUserModal()
        await loadUsers()
      } catch (err) {
        console.error('Error saving user:', err)
      } finally {
        saving.value = false
      }
    }
    
    const exportUsers = () => {
      // Export users to Excel logic
      console.log('Exporting users...')
    }
    
    const refreshUsers = () => {
      loadUsers()
    }
    
    const applyFilters = () => {
      currentPage.value = 1
    }
    
    const applySort = () => {
      currentPage.value = 1
    }
    
    const clearFilters = () => {
      filters.value = {
        status: '',
        role: '',
        accountType: '',
        startDate: '',
        endDate: '',
        search: ''
      }
      sortBy.value = 'newest'
      currentPage.value = 1
    }
    
    const debounceSearch = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        applyFilters()
      }, 500)
    }
    
    const changePage = (page) => {
      currentPage.value = page
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
    
    const getStatusText = (status) => {
      const statusTexts = {
        active: 'Hoạt động',
        blocked: 'Bị khóa',
        pending: 'Chờ xác thực'
      }
      return statusTexts[status] || status
    }
    
    const getPercentage = (value, total) => {
      return total > 0 ? Math.round((value / total) * 100) : 0
    }
    
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }
    
    const formatTime = (date) => {
      return new Date(date).toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    const formatDateTime = (date) => {
      return new Date(date).toLocaleString('vi-VN')
    }
    
    // Mock data for development
    const loadMockData = () => {
      users.value = [
        {
          id: 1,
          name: 'Nguyễn Văn A',
          email: 'user1@example.com',
          phone: '0123456789',
          role: 'USER',
          status: 'active',
          isVip: true,
          isVerified: true,
          isOnline: true,
          avatar: '/default-avatar.jpg',
          totalOrders: 15,
          totalSpent: 25000000,
          reviewCount: 8,
          loyaltyPoints: 1250,
          lastLogin: '2024-12-22T10:30:00',
          createdAt: '2024-01-15T08:00:00',
          address: '123 Đường ABC, Quận 1, TP.HCM'
        },
        {
          id: 2,
          name: 'Trần Thị B',
          email: 'user2@example.com',
          phone: '0987654321',
          role: 'SELLER',
          status: 'active',
          isVip: false,
          isVerified: true,
          isOnline: false,
          avatar: '/default-avatar.jpg',
          totalOrders: 3,
          totalSpent: 5000000,
          reviewCount: 2,
          loyaltyPoints: 350,
          lastLogin: '2024-12-21T16:45:00',
          createdAt: '2024-03-20T10:15:00'
        }
        // Add more mock users...
      ]
      calculateStats()
    }
    
    // Lifecycle
    onMounted(() => {
      // Load real data or mock data for development
      loadMockData() // Remove this and uncomment loadUsers() for production
      // loadUsers()
    })
    
    return {
      users,
      loading,
      error,
      userStats,
      filters,
      sortBy,
      currentPage,
      filteredUsers,
      paginatedUsers,
      totalPages,
      visiblePages,
      selectedUser,
      showUserModal,
      showCreateUserModal,
      showEditUserModal,
      userForm,
      saving,
      loadUsers,
      toggleUserStatus,
      updateUserRole,
      toggleVipStatus,
      viewUserDetails,
      closeUserModal,
      editUser,
      deleteUser,
      closeCreateUserModal,
      saveUser,
      exportUsers,
      refreshUsers,
      applyFilters,
      applySort,
      clearFilters,
      debounceSearch,
      changePage,
      getStatusText,
      getPercentage,
      formatPrice,
      formatDate,
      formatTime,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.admin-users-page {
  padding: 24px;
  background: var(--bg-primary);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn,
.export-btn,
.create-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: var(--accent-blue);
  color: white;
}

.export-btn {
  background: var(--accent-green);
  color: white;
}

.create-btn {
  background: var(--accent-purple);
  color: white;
}

.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.1);
}

.stat-icon {
  font-size: 2rem;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.stat-change.positive {
  color: var(--accent-green);
  font-size: 0.8rem;
}

.stat-percentage {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  position: relative;
  width: 48px;
  height: 48px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: var(--accent-green);
  border: 2px solid var(--bg-secondary);
  border-radius: 50%;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 4px;
}

.vip-badge,
.verified-badge {
  font-size: 0.7rem;
}

.user-id {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.email,
.phone {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.role-select {
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.8rem;
  cursor: pointer;
}

.role-select.user { border-color: var(--accent-blue); }
.role-select.seller { border-color: var(--accent-orange); }
.role-select.admin { border-color: var(--accent-red); }

.status-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  text-align: center;
}

.status-badge.active {
  background: rgba(76, 175, 80, 0.2);
  color: var(--accent-green);
}

.status-badge.blocked {
  background: rgba(244, 67, 54, 0.2);
  color: var(--accent-red);
}

.status-badge.pending {
  background: rgba(255, 152, 0, 0.2);
  color: var(--accent-orange);
}

.status-actions {
  display: flex;
  justify-content: center;
}

.status-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.block-btn {
  background: var(--accent-red);
  color: white;
}

.unblock-btn {
  background: var(--accent-green);
  color: white;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.8rem;
}

.orders-count {
  color: var(--accent-blue);
}

.total-spent {
  color: var(--accent-green);
}

.last-login {
  color: var(--text-secondary);
}

.join-date {
  text-align: center;
}

.date {
  font-weight: 500;
  margin-bottom: 2px;
}

.time {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.action-btn {
  padding: 6px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s ease;
}

.view-btn {
  background: var(--accent-blue);
  color: white;
}

.edit-btn {
  background: var(--accent-orange);
  color: white;
}

.vip-btn {
  background: var(--bg-primary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.vip-btn.active {
  background: var(--accent-yellow);
  color: #8B4513;
}

.delete-btn {
  background: var(--accent-red);
  color: white;
}

.filters-section {
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.filter-group input,
.filter-group select {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.search-group {
  grid-column: span 2;
}

.clear-btn {
  padding: 10px 16px;
  background: var(--accent-red);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  height: fit-content;
}

.users-section {
  background: var(--bg-secondary);
  border-radius: 12px;
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  background: var(--bg-primary);
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.user-row {
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s ease;
}

.user-row:hover {
  background: var(--bg-primary);
}

.users-table td {
  padding: 16px;
  vertical-align: top;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-secondary);
  border-radius: 12px;
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
}

.modal-body {
  padding: 24px;
}

.user-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.detail-section {
  background: var(--bg-primary);
  padding: 20px;
  border-radius: 8px;
}

.detail-section.full-width {
  grid-column: 1 / -1;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.profile-details {
  display: flex;
  gap: 16px;
}

.profile-avatar {
  position: relative;
}

.profile-avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-badges {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
}

.badge {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 4px;
}

.badge.vip {
  background: var(--accent-yellow);
  color: #8B4513;
}

.badge.verified {
  background: var(--accent-green);
  color: white;
}

.profile-info {
  flex: 1;
}

.profile-info p {
  margin: 8px 0;
  color: var(--text-secondary);
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.stat-label {
  color: var(--text-secondary);
}

.stat-value {
  font-weight: 500;
  color: var(--text-primary);
}

.activity-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 12px;
}

.activity-icon {
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
}

.activity-content h4 {
  margin-bottom: 4px;
  color: var(--text-primary);
}

.activity-content p {
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.activity-date {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

.user-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.form-checkboxes {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid var(--border-color);
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--accent-blue);
  color: white;
}

.btn-secondary {
  background: var(--bg-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: var(--bg-primary);
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: var(--accent-blue);
  color: white;
}

.pagination-btn.active {
  background: var(--accent-blue);
  color: white;
}

@media (max-width: 768px) {
  .admin-users-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .filters-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .search-group {
    grid-column: span 1;
  }
  
  .users-table {
    font-size: 0.8rem;
  }
  
  .modal-content {
    margin: 10px;
  }
  
  .user-details-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-section.full-width {
    grid-column: 1;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-checkboxes {
    flex-direction: column;
    gap: 12px;
  }
}
</style>