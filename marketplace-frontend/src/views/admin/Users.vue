<template>
  <div class="admin-users">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-info">
          <h1>👥 Quản lý người dùng</h1>
          <p>Quản lý tất cả người dùng trong hệ thống</p>
        </div>
        <div class="header-actions">
          <button @click="exportUsers" class="btn-export">
            📊 Xuất Excel
          </button>
          <button @click="refreshUsers" class="btn-refresh" :disabled="loading">
            <span v-if="loading">🔄 Đang tải...</span>
            <span v-else>🔄 Làm mới</span>
          </button>
        </div>
      </div>

      <!-- Filter và Search -->
      <div class="filter-section space-card">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="🔍 Tìm theo tên, email..."
            class="search-input"
            @input="handleSearch"
          >
          <button @click="clearSearch" v-if="searchQuery" class="clear-search">✕</button>
        </div>
        
        <div class="filters">
          <select v-model="statusFilter" @change="handleFilterChange" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Đang hoạt động</option>
            <option value="inactive">Đã khóa</option>
          </select>

          <select v-model="roleFilter" @change="handleFilterChange" class="filter-select">
            <option value="">Tất cả vai trò</option>
            <option value="USER">Người dùng</option>
            <option value="ADMIN">Quản trị viên</option>
          </select>

          <select v-model="dateFilter" @change="handleFilterChange" class="filter-select">
            <option value="">Tất cả thời gian</option>
            <option value="today">Hôm nay</option>
            <option value="week">Tuần này</option>
            <option value="month">Tháng này</option>
            <option value="year">Năm này</option>
          </select>

          <button @click="clearAllFilters" class="btn-clear">
            🗑️ Xóa bộ lọc
          </button>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="stats-grid">
        <div class="stat-card space-card">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <h3>{{ formatNumber(userStats.total) }}</h3>
            <p>Tổng người dùng</p>
            <div class="stat-change positive">
              <span>↗ +{{ userStats.newThisMonth }}</span>
              <small>người dùng mới tháng này</small>
            </div>
          </div>
        </div>

        <div class="stat-card space-card">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <h3>{{ formatNumber(userStats.active) }}</h3>
            <p>Đang hoạt động</p>
            <div class="stat-percentage">
              {{ Math.round((userStats.active / userStats.total) * 100) }}% tổng số
            </div>
          </div>
        </div>

        <div class="stat-card space-card">
          <div class="stat-icon">🚫</div>
          <div class="stat-content">
            <h3>{{ formatNumber(userStats.blocked) }}</h3>
            <p>Đã bị khóa</p>
            <div class="stat-percentage">
              {{ Math.round((userStats.blocked / userStats.total) * 100) }}% tổng số
            </div>
          </div>
        </div>

        <div class="stat-card space-card">
          <div class="stat-icon">👑</div>
          <div class="stat-content">
            <h3>{{ adminCount }}</h3>
            <p>Quản trị viên</p>
            <div class="stat-note">
              Có quyền admin
            </div>
          </div>
        </div>
      </div>

      <!-- Users Table -->
      <div class="users-table-container space-card">
        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="cosmic-loader">
            <div class="orbit"></div>
            <div class="planet">👥</div>
          </div>
          <p>Đang tải danh sách người dùng...</p>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-state">
          <div class="error-icon">❌</div>
          <h3>Có lỗi xảy ra</h3>
          <p>{{ error }}</p>
          <button @click="loadUsers" class="btn-retry">🔄 Thử lại</button>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredUsers.length === 0" class="empty-state">
          <div class="empty-icon">👥</div>
          <h3>Không tìm thấy người dùng</h3>
          <p>Không có người dùng nào phù hợp với bộ lọc hiện tại</p>
          <button @click="clearAllFilters" class="btn-clear-filters">🔄 Xóa bộ lọc</button>
        </div>

        <!-- Users Table -->
        <div v-else class="users-table">
          <div class="table-header">
            <h3>Danh sách người dùng ({{ filteredUsers.length }})</h3>
            <div class="table-actions">
              <button @click="selectAll" class="btn-select-all">
                {{ selectedUsers.length === filteredUsers.length ? '❌ Bỏ chọn tất cả' : '✅ Chọn tất cả' }}
              </button>
              <button 
                v-if="selectedUsers.length > 0" 
                @click="bulkAction('block')" 
                class="btn-bulk-action"
              >
                🚫 Khóa ({{ selectedUsers.length }})
              </button>
            </div>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th class="checkbox-column">
                    <input 
                      type="checkbox" 
                      :checked="selectedUsers.length === filteredUsers.length"
                      @change="selectAll"
                    >
                  </th>
                  <th>Thông tin người dùng</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Ngày tham gia</th>
                  <th>Hoạt động cuối</th>
                  <th>Thống kê</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="user in paginatedUsers" 
                  :key="user.id" 
                  class="user-row"
                  :class="{ 
                    'selected': selectedUsers.includes(user.id),
                    'blocked': !user.enabled 
                  }"
                >
                  <td class="checkbox-column">
                    <input 
                      type="checkbox" 
                      :value="user.id"
                      v-model="selectedUsers"
                    >
                  </td>
                  
                  <td class="user-info">
                    <div class="user-avatar">
                      <img 
                        :src="user.avatar || '/default-avatar.png'" 
                        :alt="user.firstName"
                        @error="handleImageError"
                      >
                      <div v-if="user.isVip" class="vip-badge">👑</div>
                    </div>
                    <div class="user-details">
                      <div class="user-name">
                        {{ user.firstName }} {{ user.lastName }}
                        <span v-if="user.isVerified" class="verified-badge">✓</span>
                      </div>
                      <div class="user-email">{{ user.email }}</div>
                      <div v-if="user.phone" class="user-phone">📞 {{ user.phone }}</div>
                    </div>
                  </td>

                  <td class="user-role">
                    <span class="role-badge" :class="user.role.toLowerCase()">
                      {{ user.role === 'ADMIN' ? '👑 Admin' : '👤 User' }}
                    </span>
                  </td>

                  <td class="user-status">
                    <span class="status-badge" :class="user.enabled ? 'active' : 'inactive'">
                      {{ user.enabled ? '✅ Hoạt động' : '🚫 Đã khóa' }}
                    </span>
                  </td>

                  <td class="user-joined">
                    <div class="date">{{ formatDate(user.createdAt) }}</div>
                    <div class="time-ago">{{ getTimeAgo(user.createdAt) }}</div>
                  </td>

                  <td class="user-last-active">
                    <div class="date">{{ formatDate(user.lastActiveAt) }}</div>
                    <div class="time-ago">{{ getTimeAgo(user.lastActiveAt) }}</div>
                  </td>

                  <td class="user-stats">
                    <div class="stat-item">
                      <span class="stat-label">Đơn hàng:</span>
                      <span class="stat-value">{{ user.totalOrders || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="stat-label">Chi tiêu:</span>
                      <span class="stat-value">{{ formatCurrency(user.totalSpent || 0) }}</span>
                    </div>
                  </td>

                  <td class="user-actions">
                    <div class="action-buttons">
                      <button 
                        @click="viewUserDetail(user)"
                        class="btn-action btn-view"
                        title="Xem chi tiết"
                      >
                        👁️
                      </button>
                      
                      <button 
                        @click="toggleUserStatus(user)"
                        class="btn-action"
                        :class="user.enabled ? 'btn-block' : 'btn-unblock'"
                        :title="user.enabled ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'"
                        :disabled="user.actionLoading"
                      >
                        <span v-if="user.actionLoading">⏳</span>
                        <span v-else>{{ user.enabled ? '🚫' : '✅' }}</span>
                      </button>
                      
                      <button 
                        @click="sendMessage(user)"
                        class="btn-action btn-message"
                        title="Gửi tin nhắn"
                      >
                        💬
                      </button>
                      
                      <div class="dropdown">
                        <button class="btn-action btn-more" @click="toggleDropdown(user.id)">
                          ⋯
                        </button>
                        <div v-if="activeDropdown === user.id" class="dropdown-menu">
                          <button @click="resetPassword(user)">🔑 Reset mật khẩu</button>
                          <button @click="viewLoginHistory(user)">📊 Lịch sử đăng nhập</button>
                          <button @click="exportUserData(user)">📥 Xuất dữ liệu</button>
                          <hr>
                          <button @click="deleteUser(user)" class="danger">🗑️ Xóa tài khoản</button>
                        </div>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="pagination-container">
            <div class="pagination-info">
              Hiển thị {{ startIndex }}-{{ endIndex }} của {{ filteredUsers.length }} người dùng
            </div>
            <div class="pagination">
              <button 
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 1"
                class="page-btn"
              >
                ←
              </button>
              
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="['page-btn', { active: page === currentPage, ellipsis: page === '...' }]"
                :disabled="page === '...'"
              >
                {{ page }}
              </button>
              
              <button 
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="page-btn"
              >
                →
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- User Detail Modal -->
    <div v-if="showUserDetail" class="modal-overlay" @click="closeUserDetail">
      <div class="modal-content user-detail-modal" @click.stop>
        <div class="modal-header">
          <h3>👤 Chi tiết người dùng</h3>
          <button @click="closeUserDetail" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedUser" class="user-detail-content">
            <!-- User profile info, orders, etc. -->
            <div class="detail-section">
              <h4>Thông tin cá nhân</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>Tên đầy đủ:</label>
                  <span>{{ selectedUser.firstName }} {{ selectedUser.lastName }}</span>
                </div>
                <div class="detail-item">
                  <label>Email:</label>
                  <span>{{ selectedUser.email }}</span>
                </div>
                <div class="detail-item">
                  <label>Số điện thoại:</label>
                  <span>{{ selectedUser.phone || 'Chưa cập nhật' }}</span>
                </div>
                <div class="detail-item">
                  <label>Ngày sinh:</label>
                  <span>{{ selectedUser.birthday || 'Chưa cập nhật' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/stores/admin'

export default {
  name: 'AdminUsers',
  setup() {
    const adminStore = useAdminStore()
    
    // Reactive data
    const searchQuery = ref('')
    const statusFilter = ref('')
    const roleFilter = ref('')
    const dateFilter = ref('')
    const selectedUsers = ref([])
    const activeDropdown = ref(null)
    const showUserDetail = ref(false)
    const selectedUser = ref(null)
    const currentPage = ref(1)
    const itemsPerPage = 20
    
    // Computed properties
    const loading = computed(() => adminStore.loading.users)
    const error = computed(() => adminStore.errors.users)
    const userStats = computed(() => adminStore.userStats)
    const users = computed(() => adminStore.users)
    
    const filteredUsers = computed(() => {
      let filtered = [...users.value]
      
      // Apply search filter
      if (searchQuery.value) {
        const search = searchQuery.value.toLowerCase()
        filtered = filtered.filter(user =>
          user.firstName.toLowerCase().includes(search) ||
          user.lastName.toLowerCase().includes(search) ||
          user.email.toLowerCase().includes(search)
        )
      }
      
      // Apply status filter
      if (statusFilter.value) {
        filtered = filtered.filter(user => 
          statusFilter.value === 'active' ? user.enabled : !user.enabled
        )
      }
      
      // Apply role filter
      if (roleFilter.value) {
        filtered = filtered.filter(user => user.role === roleFilter.value)
      }
      
      // Apply date filter
      if (dateFilter.value) {
        const now = new Date()
        const filterDate = new Date()
        
        switch (dateFilter.value) {
          case 'today':
            filterDate.setDate(now.getDate())
            break
          case 'week':
            filterDate.setDate(now.getDate() - 7)
            break
          case 'month':
            filterDate.setMonth(now.getMonth() - 1)
            break
          case 'year':
            filterDate.setFullYear(now.getFullYear() - 1)
            break
        }
        
        filtered = filtered.filter(user => 
          new Date(user.createdAt) >= filterDate
        )
      }
      
      return filtered
    })
    
    const adminCount = computed(() => 
      users.value.filter(user => user.role === 'ADMIN').length
    )
    
    const totalPages = computed(() => 
      Math.ceil(filteredUsers.value.length / itemsPerPage)
    )
    
    const paginatedUsers = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return filteredUsers.value.slice(start, end)
    })
    
    const startIndex = computed(() => 
      (currentPage.value - 1) * itemsPerPage + 1
    )
    
    const endIndex = computed(() => 
      Math.min(currentPage.value * itemsPerPage, filteredUsers.value.length)
    )
    
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
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
    
    // Methods
    const formatNumber = (num) => {
      return new Intl.NumberFormat('vi-VN').format(num)
    }
    
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Chưa cập nhật'
      return new Date(dateString).toLocaleDateString('vi-VN')
    }
    
    const getTimeAgo = (dateString) => {
      if (!dateString) return ''
      
      const now = new Date()
      const date = new Date(dateString)
      const diffMs = now - date
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
      
      if (diffDays === 0) return 'Hôm nay'
      if (diffDays === 1) return 'Hôm qua'
      if (diffDays < 7) return `${diffDays} ngày trước`
      if (diffDays < 30) return `${Math.floor(diffDays / 7)} tuần trước`
      if (diffDays < 365) return `${Math.floor(diffDays / 30)} tháng trước`
      return `${Math.floor(diffDays / 365)} năm trước`
    }
    
    const handleImageError = (event) => {
      event.target.src = '/default-avatar.png'
    }
    
    const handleSearch = () => {
      currentPage.value = 1
      adminStore.setUserFilter('search', searchQuery.value)
    }
    
    const handleFilterChange = () => {
      currentPage.value = 1
      adminStore.setUserFilter('status', statusFilter.value)
      adminStore.setUserFilter('role', roleFilter.value)
      adminStore.setUserFilter('dateRange', dateFilter.value)
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
      handleSearch()
    }
    
    const clearAllFilters = () => {
      searchQuery.value = ''
      statusFilter.value = ''
      roleFilter.value = ''
      dateFilter.value = ''
      currentPage.value = 1
      adminStore.resetUserFilters()
    }
    
    const selectAll = () => {
      if (selectedUsers.value.length === filteredUsers.value.length) {
        selectedUsers.value = []
      } else {
        selectedUsers.value = filteredUsers.value.map(user => user.id)
      }
    }
    
    const bulkAction = async (action) => {
      if (selectedUsers.value.length === 0) return
      
      const confirmMsg = action === 'block' 
        ? `Bạn có chắc muốn khóa ${selectedUsers.value.length} người dùng?`
        : `Bạn có chắc muốn thực hiện hành động này với ${selectedUsers.value.length} người dùng?`
      
      if (!confirm(confirmMsg)) return
      
      try {
        for (const userId of selectedUsers.value) {
          if (action === 'block') {
            await adminStore.toggleUserStatus(userId)
          }
        }
        
        selectedUsers.value = []
        alert('Thực hiện thành công!')
      } catch (error) {
        alert('Có lỗi xảy ra: ' + error.message)
      }
    }
    
    const toggleUserStatus = async (user) => {
      if (user.role === 'ADMIN' && user.enabled) {
        alert('Không thể khóa tài khoản admin!')
        return
      }
      
      const action = user.enabled ? 'khóa' : 'kích hoạt'
      if (!confirm(`Bạn có chắc muốn ${action} tài khoản ${user.email}?`)) return
      
      try {
        user.actionLoading = true
        await adminStore.toggleUserStatus(user.id)
        alert(`${action} tài khoản thành công!`)
      } catch (error) {
        alert('Có lỗi xảy ra: ' + error.message)
      } finally {
        user.actionLoading = false
      }
    }
    
    const viewUserDetail = (user) => {
      selectedUser.value = user
      showUserDetail.value = true
    }
    
    const closeUserDetail = () => {
      showUserDetail.value = false
      selectedUser.value = null
    }
    
    const toggleDropdown = (userId) => {
      activeDropdown.value = activeDropdown.value === userId ? null : userId
    }
    
    const sendMessage = (user) => {
      // TODO: Implement messaging
      alert(`Gửi tin nhắn đến ${user.email}`)
    }
    
    const resetPassword = (user) => {
      if (!confirm(`Reset mật khẩu cho ${user.email}?`)) return
      // TODO: Implement password reset
      alert('Đã gửi email reset mật khẩu!')
    }
    
    const viewLoginHistory = (user) => {
      // TODO: Implement login history view
      alert(`Xem lịch sử đăng nhập của ${user.email}`)
    }
    
    const exportUserData = (user) => {
      // TODO: Implement user data export
      alert(`Xuất dữ liệu của ${user.email}`)
    }
    
    const deleteUser = (user) => {
      if (user.role === 'ADMIN') {
        alert('Không thể xóa tài khoản admin!')
        return
      }
      
      if (!confirm(`CẢNH BÁO: Bạn có chắc muốn xóa vĩnh viễn tài khoản ${user.email}? Hành động này không thể hoàn tác!`)) return
      
      // TODO: Implement user deletion
      alert('Chức năng đang được phát triển')
    }
    
    const refreshUsers = () => {
      adminStore.loadUsers()
    }
    
    const exportUsers = () => {
      // TODO: Implement Excel export
      alert('Xuất Excel đang được phát triển')
    }
    
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }
    
    // Lifecycle
    onMounted(() => {
      adminStore.loadUsers()
    })
    
    // Watch for clicks outside dropdown
    const handleClickOutside = (event) => {
      if (!event.target.closest('.dropdown')) {
        activeDropdown.value = null
      }
    }
    
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })
    
    // Cleanup
    const cleanup = () => {
      document.removeEventListener('click', handleClickOutside)
    }
    
    return {
      // Data
      searchQuery,
      statusFilter,
      roleFilter,
      dateFilter,
      selectedUsers,
      activeDropdown,
      showUserDetail,
      selectedUser,
      currentPage,
      
      // Computed
      loading,
      error,
      userStats,
      users,
      filteredUsers,
      adminCount,
      totalPages,
      paginatedUsers,
      startIndex,
      endIndex,
      visiblePages,
      
      // Methods
      formatNumber,
      formatCurrency,
      formatDate,
      getTimeAgo,
      handleImageError,
      handleSearch,
      handleFilterChange,
      clearSearch,
      clearAllFilters,
      selectAll,
      bulkAction,
      toggleUserStatus,
      viewUserDetail,
      closeUserDetail,
      toggleDropdown,
      sendMessage,
      resetPassword,
      viewLoginHistory,
      exportUserData,
      deleteUser,
      refreshUsers,
      exportUsers,
      goToPage,
      cleanup
    }
  },
  
  beforeUnmount() {
    this.cleanup()
  }
}
</script>

<style scoped>
.admin-users {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-info h1 {
  font-size: 2rem;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.header-info p {
  color: var(--text-secondary);
  font-size: 1rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-export,
.btn-refresh {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  border: 1px solid var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-export:hover,
.btn-refresh:hover {
  background: var(--text-accent);
  color: white;
}

.filter-section {
  padding: 1.5rem;
  margin-bottom: 2rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  font-size: 1rem;
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.clear-search {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem;
}

.filters {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  cursor: pointer;
}

.btn-clear {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-clear:hover {
  background: rgba(239, 68, 68, 0.2);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-content h3 {
  font-size: 2rem;
  color: var(--text-accent);
  margin-bottom: 0.25rem;
}

.stat-content p {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.stat-change {
  font-size: 0.8rem;
  color: var(--text-success);
}

.stat-change.positive {
  color: var(--text-success);
}

.stat-change.negative {
  color: var(--text-danger);
}

.stat-percentage,
.stat-note {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.users-table-container {
  padding: 1.5rem;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 3rem;
}

.cosmic-loader {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 1rem;
}

.orbit {
  position: absolute;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  width: 100%;
  height: 100%;
  animation: spin 2s linear infinite;
}

.planet {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 2rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon,
.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.btn-retry,
.btn-clear-filters {
  padding: 0.75rem 1.5rem;
  border: 1px solid var(--text-accent);
  border-radius: 8px;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.table-header h3 {
  color: var(--text-accent);
}

.table-actions {
  display: flex;
  gap: 1rem;
}

.btn-select-all,
.btn-bulk-action {
  padding: 0.5rem 1rem;
  border: 1px solid var(--text-accent);
  border-radius: 6px;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-bulk-action {
  border-color: var(--text-danger);
  background: rgba(239, 68, 68, 0.1);
  color: var(--text-danger);
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

th,
td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

th {
  background: rgba(0, 212, 255, 0.05);
  color: var(--text-accent);
  font-weight: 600;
  position: sticky;
  top: 0;
}

.checkbox-column {
  width: 50px;
  text-align: center;
}

.user-row {
  transition: all 0.3s ease;
}

.user-row:hover {
  background: rgba(0, 212, 255, 0.05);
}

.user-row.selected {
  background: rgba(0, 212, 255, 0.1);
}

.user-row.blocked {
  opacity: 0.6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.vip-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--text-warning);
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.verified-badge {
  color: var(--text-success);
  margin-left: 0.25rem;
}

.user-email {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.user-phone {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.role-badge.admin {
  background: rgba(255, 193, 7, 0.2);
  color: var(--text-warning);
}

.role-badge.user {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.2);
  color: var(--text-success);
}

.status-badge.inactive {
  background: rgba(239, 68, 68, 0.2);
  color: var(--text-danger);
}

.date {
  color: var(--text-primary);
  font-weight: 500;
}

.time-ago {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.user-stats {
  font-size: 0.8rem;
}

.stat-item {
  margin-bottom: 0.25rem;
}

.stat-label {
  color: var(--text-secondary);
}

.stat-value {
  color: var(--text-primary);
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.btn-action {
  padding: 0.5rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-action:hover {
  background: var(--text-accent);
  color: white;
}

.btn-block {
  border-color: var(--text-danger);
  background: rgba(239, 68, 68, 0.1);
  color: var(--text-danger);
}

.btn-block:hover {
  background: var(--text-danger);
  color: white;
}

.btn-unblock {
  border-color: var(--text-success);
  background: rgba(16, 185, 129, 0.1);
  color: var(--text-success);
}

.btn-unblock:hover {
  background: var(--text-success);
  color: white;
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 0.5rem;
  min-width: 180px;
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.dropdown-menu button {
  display: block;
  width: 100%;
  padding: 0.5rem;
  background: none;
  border: none;
  color: var(--text-secondary);
  text-align: left;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.dropdown-menu button:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.dropdown-menu button.danger {
  color: var(--text-danger);
}

.dropdown-menu button.danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.dropdown-menu hr {
  border: none;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  margin: 0.5rem 0;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.pagination {
  display: flex;
  gap: 0.5rem;
}

.page-btn {
  padding: 0.5rem 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: var(--text-accent);
  color: white;
}

.page-btn.active {
  background: var(--text-accent);
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.ellipsis {
  border: none;
  background: none;
  cursor: default;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  max-width: 600px;
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

.modal-header h3 {
  color: var(--text-accent);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
}

.modal-body {
  padding: 1.5rem;
}

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h4 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.detail-item span {
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section {
    flex-direction: column;
    gap: 1rem;
  }
  
  .filters {
    justify-content: stretch;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .table-actions {
    justify-content: stretch;
  }
  
  .pagination-container {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
}
</style>