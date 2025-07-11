<template>
  <div class="admin-users">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <h1>👥 Quản lý người dùng</h1>
          <p>Quản lý tất cả người dùng trong hệ thống</p>
        </div>
        <button @click="showCreateUserModal = true" class="btn-create">
          ➕ Thêm người dùng mới
        </button>
      </div>

      <!-- Filter và Search (gọn) -->
      <div class="filter-section">
        <div class="search-box">
          <input
            v-model="filters.search"
            type="text"
            placeholder="Tìm theo tên, email, điện thoại..."
            class="search-input"
            @input="debounceSearch"
          />
        </div>
        <button class="btn-advanced-filter" @click="showFilterModal = true">
          <span class="filter-icon">🔍</span> Bộ lọc nâng cao
        </button>
      </div>

      <!-- Popup Modal Advanced Filter -->
      <div v-if="showFilterModal" class="modal-overlay" @click="closeFilterModal">
        <div class="modal-content filter-modal" @click.stop>
          <div class="modal-header">
            <h2>Bộ lọc nâng cao</h2>
            <button @click="closeFilterModal" class="close-btn">✕</button>
          </div>
          <div class="modal-body">
            <div class="filter-grid">
              <div class="form-group">
                <label>Trạng thái</label>
                <select v-model="tempFilters.status" class="filter-select">
                  <option value="">Tất cả trạng thái</option>
                  <option value="active">Hoạt động</option>
                  <option value="blocked">Bị khóa</option>
                </select>
              </div>
              <div class="form-group">
                <label>Vai trò</label>
                <select v-model="tempFilters.role" class="filter-select">
                  <option value="">Tất cả vai trò</option>
                  <option value="USER">Người dùng</option>
                  <option value="ADMIN">Quản trị viên</option>
                </select>
              </div>
              <div class="form-group">
                <label>Loại tài khoản</label>
                <select v-model="tempFilters.accountType" class="filter-select">
                  <option value="">Tất cả loại tài khoản</option>
                  <option value="vip">VIP</option>
                  <option value="regular">Thường</option>
                </select>
              </div>
              <div class="form-group">
                <label>Từ ngày</label>
                <input type="date" v-model="tempFilters.startDate" class="filter-select" />
              </div>
              <div class="form-group">
                <label>Đến ngày</label>
                <input type="date" v-model="tempFilters.endDate" class="filter-select" />
              </div>
              <div class="form-group">
                <label>Sắp xếp</label>
                <select v-model="tempSortBy" class="filter-select">
                  <option value="newest">Mới nhất</option>
                  <option value="oldest">Cũ nhất</option>
                  <option value="nameAsc">Tên A → Z</option>
                  <option value="nameDesc">Tên Z → A</option>
                  <option value="mostActive">Hoạt động nhiều</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="clearTempFilters" class="btn-clear">Xóa bộ lọc</button>
            <button @click="applyTempFilters" class="btn-primary">Áp dụng</button>
            <button @click="closeFilterModal" class="btn-secondary">Đóng</button>
          </div>
        </div>
      </div>

      <!-- Statistics -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <h3>{{ userStats.total }}</h3>
            <p>Tổng người dùng</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <h3>{{ userStats.active }}</h3>
            <p>Hoạt động</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🚫</div>
          <div class="stat-info">
            <h3>{{ userStats.blocked }}</h3>
            <p>Bị khóa</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">👑</div>
          <div class="stat-info">
            <h3>{{ userStats.vip }}</h3>
            <p>VIP</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <h3>{{ userStats.verified }}</h3>
            <p>Đã xác thực</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🛡️</div>
          <div class="stat-info">
            <h3>{{ userStats.admins }}</h3>
            <p>Quản trị viên</p>
          </div>
        </div>
      </div>

      <!-- Users Table -->
      <div class="users-table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải người dùng...</p>
        </div>
        <div v-else-if="paginatedUsers.length === 0" class="empty-state">
          <div class="empty-icon">👥</div>
          <h3>Không có người dùng nào</h3>
          <p>
            {{
              filters.search ||
              filters.status ||
              filters.role ||
              filters.accountType ||
              filters.startDate ||
              filters.endDate
                ? 'Không tìm thấy người dùng nào với bộ lọc hiện tại. Hãy thử thay đổi điều kiện tìm kiếm.'
                : 'Chưa có người dùng nào trong hệ thống.'
            }}
          </p>
          <button
            v-if="
              filters.search ||
              filters.status ||
              filters.role ||
              filters.accountType ||
              filters.startDate ||
              filters.endDate
            "
            @click="clearFilters"
            class="btn-add-first"
          >
            🔄 Xóa bộ lọc
          </button>
        </div>
        <div v-else class="users-table">
          <table>
            <thead>
              <tr>
                <th style="width: 120px">🆔 ID</th>
                <th style="width: 220px">📧 Email</th>
                <th style="width: 120px">🎭 Vai trò</th>
                <th style="width: 120px">🔄 Trạng thái</th>
                <th style="width: 120px">📅 Ngày tạo</th>
                <th style="width: 140px">⚡ Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in paginatedUsers" :key="user.id" class="user-row">
                <td class="user-id">#{{ user.id.slice(-8) }}</td>
                <td class="user-email">{{ user.email }}</td>
                <td class="user-role">{{ getRoleText(user.role) }}</td>
                <td>
                  <span class="status-badge" :class="user.status.toLowerCase()">
                    {{ getStatusText(user.status) }}
                  </span>
                </td>
                <td class="created-date">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <div class="action-buttons">
                    <button
                      @click="viewUserDetails(user)"
                      class="btn btn-view"
                      title="Xem chi tiết"
                    >
                      <span class="action-icon">👁️</span>
                    </button>
                    <button @click="editUser(user)" class="btn btn-edit" title="Chỉnh sửa">
                      <span class="action-icon">✏️</span>
                    </button>
                    <button
                      @click="toggleUserStatus(user)"
                      class="btn btn-block"
                      :title="user.status === 'active' ? 'Khóa' : 'Mở khóa'"
                    >
                      <span class="action-icon">{{ user.status === 'active' ? '🚫' : '✅' }}</span>
                    </button>
                    <button @click="deleteUser(user)" class="btn btn-delete" title="Xóa">
                      <span class="action-icon">🗑️</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">
          « Trước
        </button>
        <span class="page-info"> Trang {{ currentPage }} / {{ totalPages }} </span>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          Sau »
        </button>
      </div>
    </div>

    <!-- User Details Modal -->
    <div v-if="showUserModal" class="modal-overlay" @click="closeUserModal">
      <div class="modal-content user-modal redesigned-user-modal" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết người dùng</h2>
          <button @click="closeUserModal" class="close-btn">✕</button>
        </div>
        <div class="modal-body" v-if="selectedUser">
          <div class="user-details-flex">
            <div class="profile-avatar-large">
              <img
                :src="selectedUser.avatar || '/default-avatar.jpg'"
                :alt="selectedUser.firstName + ' ' + selectedUser.lastName"
              />
            </div>
            <div class="profile-info-block">
              <div style="margin-bottom: 8px; color: #888; font-size: 0.98em">
                ID: #{{ selectedUser.id?.slice(-8) }}
              </div>
              <p><strong>Email:</strong> {{ selectedUser.email }}</p>
              <p><strong>Họ:</strong> {{ selectedUser.firstName }}</p>
              <p><strong>Tên:</strong> {{ selectedUser.lastName }}</p>
              <p><strong>Điện thoại:</strong> {{ selectedUser.phone || 'Chưa cập nhật' }}</p>
              <p><strong>Địa chỉ:</strong> {{ selectedUser.address || 'Chưa cập nhật' }}</p>
              <p><strong>Ngày sinh:</strong> {{ selectedUser.birthday || 'Chưa cập nhật' }}</p>
              <p><strong>Vai trò:</strong> {{ getRoleText(selectedUser.role) }}</p>
              <p><strong>Trạng thái:</strong> {{ getStatusText(selectedUser.status) }}</p>
              <p><strong>VIP:</strong> {{ selectedUser.isVip ? 'Có' : 'Không' }}</p>
              <p>
                <strong>Đã xác thực email:</strong> {{ selectedUser.isVerified ? 'Có' : 'Không' }}
              </p>
              <p><strong>Ngày tạo:</strong> {{ formatDate(selectedUser.createdAt) }}</p>
              <p><strong>Ngày cập nhật:</strong> {{ formatDate(selectedUser.updatedAt) }}</p>
              <p>
                <strong>Lần đăng nhập cuối:</strong>
                {{
                  selectedUser.lastLogin
                    ? formatDateTime(selectedUser.lastLogin)
                    : 'Chưa từng đăng nhập'
                }}
              </p>
            </div>
          </div>
          <div class="user-stats-block">
            <h4>📊 Thống kê tài khoản</h4>
            <div class="stats-list">
              <div class="stat-item">
                <span>Tổng đơn hàng:</span> <b>{{ selectedUser.totalOrders }}</b>
              </div>
              <div class="stat-item">
                <span>Tổng chi tiêu:</span> <b>{{ formatPrice(selectedUser.totalSpent) }}</b>
              </div>
              <div class="stat-item">
                <span>Đánh giá:</span> <b>{{ selectedUser.reviewCount }}</b>
              </div>
              <div class="stat-item">
                <span>Điểm loyalty:</span> <b>{{ selectedUser.loyaltyPoints }}</b>
              </div>
            </div>
          </div>
          <div class="user-stats-block">
            <h4>🛒 Lịch sử mua hàng</h4>
            <ul
              v-if="selectedUser.purchaseHistory && selectedUser.purchaseHistory.length"
              style="margin: 0 0 8px 0; padding-left: 18px"
            >
              <li v-for="item in selectedUser.purchaseHistory" :key="item">{{ item }}</li>
            </ul>
            <div v-else style="color: #888">Không có dữ liệu</div>
            <h4>📦 Sản phẩm đã bán</h4>
            <ul
              v-if="selectedUser.productsSold && selectedUser.productsSold.length"
              style="margin: 0 0 8px 0; padding-left: 18px"
            >
              <li v-for="item in selectedUser.productsSold" :key="item">{{ item }}</li>
            </ul>
            <div v-else style="color: #888">Không có dữ liệu</div>
            <h4>💾 Sản phẩm lưu lại</h4>
            <ul
              v-if="selectedUser.savedItems && selectedUser.savedItems.length"
              style="margin: 0 0 8px 0; padding-left: 18px"
            >
              <li v-for="item in selectedUser.savedItems" :key="item">{{ item }}</li>
            </ul>
            <div v-else style="color: #888">Không có dữ liệu</div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="editUser(selectedUser)" class="btn-secondary">✏️ Chỉnh sửa</button>
          <button @click="closeUserModal" class="btn-primary">Đóng</button>
        </div>
      </div>
    </div>

    <!-- Create/Edit User Modal -->
    <div
      v-if="showCreateUserModal || showEditUserModal"
      class="modal-overlay"
      @click="closeCreateUserModal"
    >
      <div class="modal-content create-user-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ showEditUserModal ? 'Chỉnh sửa người dùng' : 'Thêm người dùng mới' }}</h2>
          <button @click="closeCreateUserModal" class="close-btn">✕</button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="saveUser" class="user-form">
            <div class="form-grid">
              <div class="form-group">
                <label>Họ *</label>
                <input v-model="userForm.firstName" type="text" required />
              </div>

              <div class="form-group">
                <label>Tên *</label>
                <input v-model="userForm.lastName" type="text" required />
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
              <!-- Không còn checkbox nào ở đây -->
            </div>
          </form>
        </div>

        <div class="modal-footer">
          <button @click="closeCreateUserModal" class="btn-secondary">Hủy</button>
          <button @click="saveUser" class="btn-primary" :disabled="saving">
            {{ saving ? '⏳ Đang lưu...' : showEditUserModal ? 'Cập nhật' : 'Tạo tài khoản' }}
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
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'

export default {
  name: 'AdminUsers',
  components: { Multiselect },
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
      newThisMonth: 0,
    })

    // Filters
    const filters = ref({
      status: '',
      role: '',
      accountType: '',
      startDate: '',
      endDate: '',
      search: '',
    })

    const sortBy = ref('newest')

    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = 10

    // User form
    const userForm = ref({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      role: 'USER',
      password: '',
      address: '',
      isVip: false,
      isVerified: false,
      isActive: true,
    })

    // Debounce timer for search
    let searchTimeout = null

    // Popup filter modal state
    const showFilterModal = ref(false)
    const tempFilters = ref({ ...filters.value })
    const tempSortBy = ref(sortBy.value)

    // Computed properties
    const filteredUsers = computed(() => {
      let filtered = [...users.value]

      // Status filter
      if (filters.value.status) {
        filtered = filtered.filter((user) => user.status === filters.value.status)
      }

      // Role filter
      if (filters.value.role) {
        filtered = filtered.filter((user) => user.role === filters.value.role)
      }

      // Account type filter
      if (filters.value.accountType) {
        if (filters.value.accountType === 'vip') {
          filtered = filtered.filter((user) => user.isVip)
        } else if (filters.value.accountType === 'regular') {
          filtered = filtered.filter((user) => !user.isVip)
        }
      }

      // Date filter
      if (filters.value.startDate) {
        filtered = filtered.filter(
          (user) => new Date(user.createdAt) >= new Date(filters.value.startDate)
        )
      }

      if (filters.value.endDate) {
        filtered = filtered.filter(
          (user) => new Date(user.createdAt) <= new Date(filters.value.endDate)
        )
      }

      // Search filter
      if (filters.value.search) {
        const searchTerm = filters.value.search.toLowerCase()
        filtered = filtered.filter(
          (user) =>
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

    const totalPages = computed(() => Math.ceil(sortedUsers.value.length / itemsPerPage))

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
        const [usersResponse, statsResponse] = await Promise.all([
          adminAPI.getUsers(),
          adminAPI.getUserStats(),
        ])

        users.value = usersResponse.data.map((user) => ({
          ...user,
          name: `${user.firstName} ${user.lastName}`,
          status: user.enabled ? 'active' : 'blocked',
          totalOrders: user.purchaseHistory ? user.purchaseHistory.length : 0,
          totalSpent: 0, 
          reviewCount: 0, 
          loyaltyPoints: 0, 
          isOnline: false, 
          address: user.address || 'Chưa cập nhật',
          birthday: user.birthday || 'Chưa cập nhật',
        }))

        userStats.value = {
          total: statsResponse.data.totalUsers,
          active: statsResponse.data.activeUsers,
          blocked: statsResponse.data.inactiveUsers,
          admins: statsResponse.data.adminUsers,
          vip: statsResponse.data.vipUsers,
          verified: statsResponse.data.verifiedUsers,
          newThisMonth: statsResponse.data.newUsersThisMonth,
        }
      } catch (err) {
        error.value = 'Không thể tải danh sách người dùng'
        console.error('Error loading users:', err)
      } finally {
        loading.value = false
      }
    }

    const toggleUserStatus = async (user) => {
      const newStatus = user.status === 'active' ? 'blocked' : 'active'
      const action = newStatus === 'blocked' ? 'khóa' : 'mở khóa'

      if (confirm(`Bạn có chắc chắn muốn ${action} tài khoản của ${user.name}?`)) {
        try {
          await adminAPI.toggleUserStatus(user.id)
          user.status = newStatus
          await loadUsers() 
        } catch (err) {
          console.error('Error toggling user status:', err)
          alert('Có lỗi xảy ra khi cập nhật trạng thái người dùng')
        }
      }
    }

    const updateUserRole = async (user) => {
      try {
        await adminAPI.updateUserRole(user.id, user.role)
        console.log('User role updated successfully')
      } catch (err) {
        console.error('Error updating user role:', err)
        alert('Có lỗi xảy ra khi cập nhật vai trò người dùng')
        await loadUsers() 
      }
    }

    const toggleVipStatus = async (user) => {
      try {
        await adminAPI.toggleVipStatus(user.id)
        user.isVip = !user.isVip
        await loadUsers() 
      } catch (err) {
        console.error('Error toggling VIP status:', err)
        alert('Có lỗi xảy ra khi cập nhật trạng thái VIP')
      }
    }

    const viewUserDetails = async (user) => {
      try {
        const res = await adminAPI.getUserById(user.id)
        selectedUser.value = res.data
        showUserModal.value = true
      } catch (e) {
        alert('Không lấy được thông tin người dùng!')
      }
    }

    const closeUserModal = () => {
      showUserModal.value = false
      selectedUser.value = null
    }

    const editUser = (user) => {
      userForm.value = {
        id: user.id,
        firstName: user.firstName || user.name?.split(' ')[0] || '',
        lastName: user.lastName || user.name?.split(' ').slice(1).join(' ') || '',
        email: user.email,
        phone: user.phone,
        role: user.role,
        password: '', 
        address: user.address,
        isVip: user.isVip,
        isVerified: user.isVerified,
        isActive: user.status === 'active',
      }
      showEditUserModal.value = true
      showUserModal.value = false
    }

    const deleteUser = async (user) => {
      if (
        confirm(
          `Bạn có chắc chắn muốn xóa tài khoản của ${user.name}? Hành động này không thể hoàn tác.`
        )
      ) {
        try {
          await adminAPI.deleteUser(user.id)
          users.value = users.value.filter((u) => u.id !== user.id)
          await loadUsers() 
        } catch (err) {
          console.error('Error deleting user:', err)
          alert('Có lỗi xảy ra khi xóa người dùng')
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
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        role: 'USER',
        password: '',
        address: '',
        isVip: false,
        isVerified: false,
        isActive: true,
      }
    }

    const saveUser = async () => {
      saving.value = true
      try {
        const userData = {
          firstName: userForm.value.firstName,
          lastName: userForm.value.lastName,
          email: userForm.value.email,
          phone: userForm.value.phone,
          role: userForm.value.role,
          password: userForm.value.password,
          address: userForm.value.address,
          isVip: userForm.value.isVip,
          isVerified: userForm.value.isVerified,
          enabled: userForm.value.isActive,
        }

        if (showEditUserModal.value) {
          await adminAPI.updateUser(userForm.value.id, userData)
        } else {
          await adminAPI.createUser(userData)
        }
        closeCreateUserModal()
        await loadUsers()
      } catch (err) {
        console.error('Error saving user:', err)
        alert('Có lỗi xảy ra khi lưu người dùng')
      } finally {
        saving.value = false
      }
    }

    const exportUsers = () => {
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
        search: '',
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
        pending: 'Chờ xác thực',
      }
      return statusTexts[status] || status
    }

    const getPercentage = (value, total) => {
      return total > 0 ? Math.round((value / total) * 100) : 0
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(price)
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const formatTime = (date) => {
      return new Date(date).toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    }

    const formatDateTime = (date) => {
      return new Date(date).toLocaleString('vi-VN')
    }

    const handleImageError = (event) => {
      event.target.src = '/default-avatar.jpg'
    }

    const getRoleText = (role) => {
      switch (role) {
        case 'ADMIN':
          return 'Quản trị viên'
        case 'USER':
          return 'Người dùng'
        default:
          return role
      }
    }

    const openFilterModal = () => {
      tempFilters.value = { ...filters.value }
      tempSortBy.value = sortBy.value
      showFilterModal.value = true
    }
    const closeFilterModal = () => {
      showFilterModal.value = false
    }
    const clearTempFilters = () => {
      tempFilters.value = {
        status: '',
        role: '',
        accountType: '',
        startDate: '',
        endDate: '',
        search: '',
      }
      tempSortBy.value = 'newest'
    }
    const applyTempFilters = () => {
      filters.value = { ...tempFilters.value }
      sortBy.value = tempSortBy.value
      currentPage.value = 1
      showFilterModal.value = false
    }

    onMounted(() => {
      loadUsers()
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
      formatDateTime,
      handleImageError,
      getRoleText,
      showFilterModal,
      tempFilters,
      tempSortBy,
      openFilterModal,
      closeFilterModal,
      clearTempFilters,
      applyTempFilters,
    }
  },
}
</script>

<style scoped>
.admin-users {
  padding: 24px;
  background: var(--bg-primary);
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.header-info h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 8px;
}

.header-info p {
  color: #666;
  font-size: 1.1rem;
}

.btn-create {
  padding: 12px 24px;
  background: var(--accent-purple);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-create:hover {
  background: var(--accent-purple-dark);
  transform: translateY(-2px);
}

.filter-section {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  justify-content: flex-start;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: nowrap;
}

.search-box {
  flex: 0 1 400px;
  max-width: 400px;
  min-width: 260px;
  width: 100%;
  margin-right: 0;
}

.search-input {
  width: 100%;
  padding: 14px 20px;
  border: 1px solid #e0e7ef;
  border-radius: 8px;
  font-size: 1.08rem;
  background: #f6f8fa;
  color: #222;
  box-sizing: border-box;
}

.btn-advanced-filter {
  padding: 12px 20px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-advanced-filter:hover {
  background: #1d4ed8;
}

.filter-icon {
  font-size: 1.2em;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px 18px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 2rem;
  color: #2563eb;
}

.stat-info h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 2px;
}

.stat-info p {
  color: #666;
  font-size: 0.95rem;
}

.users-table-container {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.07);
  padding: 24px;
  margin-bottom: 32px;
  overflow-x: auto;
}

.users-table table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 1rem;
}

.users-table th {
  background: #2563eb;
  color: #fff;
  font-weight: 700;
  padding: 14px 12px;
  text-align: left;
}

.users-table td {
  padding: 12px 10px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.user-id {
  font-weight: 600;
  color: #2563eb;
}

.user-email {
  font-size: 0.98em;
  color: #666;
}

.user-role {
  font-size: 0.98em;
  color: #333;
}

.status-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 16px;
  font-size: 0.98em;
  font-weight: 600;
  color: #fff;
  background: #64748b;
  min-width: 90px;
  text-align: center;
}

.status-badge.active {
  background: #059669;
}

.status-badge.blocked {
  background: #ef4444;
}

.vip-badge {
  background: #fde68a;
  color: #8b4513;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.85em;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.btn {
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  padding: 7px 10px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 1.1em;
  position: relative;
}

.btn:hover {
  background: #dbeafe;
}

.btn-block:hover,
.btn-delete:hover {
  background: #fee2e2;
}

.action-icon {
  vertical-align: middle;
}

.loading {
  text-align: center;
  padding: 40px 0;
}

.spinner {
  border: 4px solid #e0e7ef;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px auto;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  color: #888;
  padding: 32px 0 16px 0;
  font-size: 1.1em;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
}

.page-btn {
  padding: 10px 16px;
  border: 1px solid #e0e7ef;
  background: #f6f8fa;
  color: #2563eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 1rem;
  margin: 0 16px;
}

.date-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date-label {
  font-size: 0.85em;
  color: #2563eb;
  margin-bottom: 2px;
  font-weight: 500;
}

@media (max-width: 1024px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    flex-wrap: wrap;
  }
  .search-box {
    max-width: 100%;
    min-width: 0;
    margin-right: 0;
  }
  .filters {
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-start;
    overflow-x: auto;
  }
}

/* Style cho vue-multiselect dark mode */
.multiselect-dark .multiselect__select {
  background: var(--bg-secondary);
  color: var(--text-primary);
}
.multiselect-dark .multiselect__content-wrapper {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.multiselect-dark .multiselect__option--highlight {
  background: var(--accent-blue);
  color: white;
}
.multiselect-dark .multiselect__option {
  color: var(--text-primary);
}
.multiselect-dark .multiselect__single {
  color: var(--text-primary);
  background: var(--bg-secondary);
}

/* Modal filter */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content.filter-modal {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 32px rgba(0, 0, 0, 0.18);
  padding: 32px 28px 20px 28px;
  min-width: 340px;
  max-width: 95vw;
  width: 420px;
  position: relative;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.modal-header h2 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #2563eb;
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
  color: #888;
}
.filter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group label {
  font-size: 0.98em;
  color: #222;
  font-weight: 500;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 600;
  cursor: pointer;
  font-size: 1rem;
}
.btn-primary:hover {
  background: #1d4ed8;
}
.btn-secondary {
  background: #e0e7ef;
  color: #2563eb;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 600;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.2s;
}
.btn-secondary:hover {
  background: #c7d2fe;
  color: #1d4ed8;
}
.btn-clear {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 500;
  cursor: pointer;
  font-size: 1rem;
}
.btn-clear:hover {
  background: #dc2626;
}
@media (max-width: 600px) {
  .modal-content.filter-modal {
    width: 98vw;
    min-width: 0;
    padding: 18px 6vw 12px 6vw;
  }
  .filter-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.created-date {
  background: #e6f0ff;
  color: #2563eb;
  font-weight: 600;
  border-radius: 6px;
  padding: 4px 8px;
  text-align: center;
}

.redesigned-user-modal {
  max-width: 540px;
  width: 98vw;
  padding: 0 0 24px 0;
  border-radius: 18px;
  overflow-y: auto;
  max-height: 90vh;
  background: #f6f8fa;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}
.redesigned-user-modal .modal-header {
  padding: 24px 32px 10px 32px;
  margin-bottom: 0;
}
.redesigned-user-modal .modal-header h2 {
  font-size: 1.35rem;
  font-weight: 700;
  color: #2563eb;
}
.redesigned-user-modal .close-btn {
  margin-right: -8px;
  margin-top: -8px;
}
.redesigned-user-modal .modal-footer {
  padding: 18px 32px 0 32px;
  margin-top: 18px;
}
.user-details-flex {
  display: flex;
  gap: 28px;
  align-items: flex-start;
  padding: 18px 32px 0 32px;
  flex-wrap: wrap;
}
.profile-avatar-large {
  flex: 0 0 110px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.profile-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.profile-info-block {
  flex: 1;
  min-width: 180px;
  font-size: 1.05em;
  color: #222;
}
.user-stats-block,
.user-activity-block {
  padding: 18px 32px 0 32px;
}
.stats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 24px;
  margin-top: 8px;
}
.stat-item {
  min-width: 120px;
  color: #2563eb;
  font-weight: 500;
}
.user-activity-block {
  margin-top: 8px;
}
.activity-timeline {
  margin-top: 8px;
  max-height: 180px;
  overflow-y: auto;
  padding-right: 6px;
}
.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
}
.activity-icon {
  font-size: 1.3em;
  margin-top: 2px;
}
.activity-content h5 {
  margin: 0 0 2px 0;
  font-size: 1em;
  color: #2563eb;
}
.activity-date {
  font-size: 0.92em;
  color: #888;
}
@media (max-width: 600px) {
  .user-details-flex,
  .user-stats-block,
  .user-activity-block {
    padding: 12px 6vw 0 6vw;
  }
  .redesigned-user-modal {
    padding: 0 0 8px 0;
  }
  .redesigned-user-modal .modal-header,
  .redesigned-user-modal .modal-footer {
    padding-left: 6vw;
    padding-right: 6vw;
  }
}
.user-stats-block h4 {
  color: #2563eb;
  font-weight: 700;
  margin-top: 18px;
  margin-bottom: 6px;
  font-size: 1.08em;
}

.create-user-modal {
  max-width: 480px;
  width: 98vw;
  background: #f6f8fa;
  border-radius: 18px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  padding: 0 0 24px 0;
  overflow-y: auto;
  max-height: 90vh;
}
.create-user-modal .modal-header {
  padding: 24px 32px 10px 32px;
  margin-bottom: 0;
}
.create-user-modal .modal-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #2563eb;
}
.create-user-modal .close-btn {
  margin-right: -8px;
  margin-top: -8px;
}
.create-user-modal .modal-footer {
  padding: 18px 32px 0 32px;
  margin-top: 18px;
}
.create-user-modal .user-form {
  padding: 18px 32px 0 32px;
}
.create-user-modal .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 16px;
}
.create-user-modal .form-group label {
  color: #2563eb;
  font-weight: 600;
  margin-bottom: 4px;
}
.create-user-modal .form-group input,
.create-user-modal .form-group textarea,
.create-user-modal .form-group select {
  border-radius: 8px;
  border: 1px solid #e0e7ef;
  padding: 10px 12px;
  font-size: 1em;
  background: #fff;
  color: #222;
  margin-bottom: 0;
}
.create-user-modal .form-checkboxes {
  display: flex;
  gap: 18px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.create-user-modal .checkbox-label {
  color: #2563eb;
  font-weight: 500;
  font-size: 1em;
  display: flex;
  align-items: center;
  gap: 6px;
}
@media (max-width: 600px) {
  .create-user-modal .modal-header,
  .create-user-modal .modal-footer,
  .create-user-modal .user-form {
    padding-left: 6vw;
    padding-right: 6vw;
  }
  .create-user-modal .form-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
