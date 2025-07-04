<template>
  <div class="admin-dashboard">
    <div class="container">
      <!-- Header -->
      <div class="dashboard-header">
        <div class="header-content">
          <h1>👑 Admin Dashboard</h1>
          <p>Bảng điều khiển quản trị hệ thống</p>
        </div>
        <div class="header-actions">
          <button @click="refreshData" class="btn btn-secondary" :disabled="loading">
            <span v-if="loading">🔄 Đang tải...</span>
            <span v-else>🔄 Làm mới</span>
          </button>
          <div class="time-range-selector">
            <select
              v-model="selectedTimeRange"
              @change="loadDashboardData"
              class="form-select"
              :disabled="loading"
            >
              <option value="7">7 ngày qua</option>
              <option value="30">30 ngày qua</option>
              <option value="90">3 tháng qua</option>
              <option value="365">1 năm qua</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-if="error" class="error-container">
        <div class="error-icon">⚠️</div>
        <h3>Lỗi tải dữ liệu</h3>
        <p>{{ error }}</p>
        <button @click="loadDashboardData" class="btn btn-primary">🔄 Thử lại</button>
      </div>

      <!-- Loading State -->
      <div v-else-if="loading" class="loading-container">
        <div class="cosmic-loader">
          <div class="planet">🌍</div>
          <div class="orbit"></div>
          <div class="orbit orbit-2"></div>
        </div>
        <p>Đang tải dữ liệu dashboard...</p>
      </div>

      <!-- Dashboard Content -->
      <div v-else class="dashboard-content">
        <!-- Stats Cards -->
        <div class="stats-grid">
          <div class="stat-card space-card">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
              <h3>{{ formatNumber(stats.users.total) }}</h3>
              <p>Tổng người dùng</p>
              <div
                class="stat-change"
                :class="{ positive: stats.users.change > 0, negative: stats.users.change < 0 }"
              >
                <span
                  >{{ stats.users.change > 0 ? '↗' : '↘' }}
                  {{ Math.abs(stats.users.change || 0).toFixed(1) }}%</span
                >
                <small>so với kỳ trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">📦</div>
            <div class="stat-content">
              <h3>{{ formatNumber(stats.products.total) }}</h3>
              <p>Tổng sản phẩm</p>
              <div
                class="stat-change"
                :class="{
                  positive: stats.products.change > 0,
                  negative: stats.products.change < 0,
                }"
              >
                <span
                  >{{ stats.products.change > 0 ? '↗' : '↘' }}
                  {{ Math.abs(stats.products.change || 0).toFixed(1) }}%</span
                >
                <small>so với kỳ trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">🛒</div>
            <div class="stat-content">
              <h3>{{ formatNumber(stats.orders.total) }}</h3>
              <p>Tổng đơn hàng</p>
              <div
                class="stat-change"
                :class="{ positive: stats.orders.change > 0, negative: stats.orders.change < 0 }"
              >
                <span
                  >{{ stats.orders.change > 0 ? '↗' : '↘' }}
                  {{ Math.abs(stats.orders.change || 0).toFixed(1) }}%</span
                >
                <small>so với kỳ trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <h3>{{ formatCurrency(stats.revenue.total) }}</h3>
              <p>Tổng doanh thu</p>
              <div
                class="stat-change"
                :class="{ positive: stats.revenue.change > 0, negative: stats.revenue.change < 0 }"
              >
                <span
                  >{{ stats.revenue.change > 0 ? '↗' : '↘' }}
                  {{ Math.abs(stats.revenue.change || 0).toFixed(1) }}%</span
                >
                <small>so với kỳ trước</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="quick-actions-section">
          <h2>⚡ Hành động nhanh</h2>
          <div class="quick-actions-grid">
            <router-link
              to="/admin/users"
              class="quick-action-card space-card"
              :class="{ loading: loading }"
            >
              <div class="action-icon">👥</div>
              <div class="action-content">
                <h3>Quản lý người dùng</h3>
                <p>
                  {{
                    loading ? 'Đang tải...' : (stats.users.pending || 0) + ' tài khoản chờ duyệt'
                  }}
                </p>
              </div>
              <div class="action-arrow">→</div>
            </router-link>

            <router-link
              to="/admin/orders"
              class="quick-action-card space-card"
              :class="{ loading: loading }"
            >
              <div class="action-icon">📋</div>
              <div class="action-content">
                <h3>Đơn hàng mới</h3>
                <p>
                  {{
                    loading ? 'Đang tải...' : (stats.orders.pending || 0) + ' đơn hàng chờ xử lý'
                  }}
                </p>
              </div>
              <div class="action-arrow">→</div>
            </router-link>

            <router-link
              to="/admin/products"
              class="quick-action-card space-card"
              :class="{ loading: loading }"
            >
              <div class="action-icon">📦</div>
              <div class="action-content">
                <h3>Sản phẩm cần duyệt</h3>
                <p>
                  {{ loading ? 'Đang tải...' : (stats.products.pending || 0) + ' sản phẩm mới' }}
                </p>
              </div>
              <div class="action-arrow">→</div>
            </router-link>
          </div>
        </div>

        <!-- Recent Activity -->
        <div class="activity-section">
          <div class="activity-grid">
            <!-- Recent Orders -->
            <div class="activity-card space-card">
              <div class="activity-header">
                <h3>🛒 Đơn hàng gần đây</h3>
                <router-link to="/admin/orders" class="view-all-link">Xem tất cả →</router-link>
              </div>
              <div class="activity-list">
                <div v-if="recentOrders.length === 0" class="empty-state">
                  <div class="empty-icon">📦</div>
                  <p>Chưa có đơn hàng nào</p>
                </div>
                <div
                  v-else
                  v-for="order in recentOrders"
                  :key="order.id || Math.random()"
                  class="activity-item"
                >
                  <div class="activity-icon">
                    <span
                      class="status-icon"
                      :class="getOrderStatusClass(order.status || 'UNKNOWN')"
                    >
                      {{ getOrderStatusIcon(order.status || 'UNKNOWN') }}
                    </span>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">Đơn hàng #{{ order.id || 'N/A' }}</div>
                    <div class="activity-subtitle">
                      {{ order.customerName || 'Khách hàng' }} •
                      {{ formatCurrency(order.total || 0) }}
                    </div>
                    <div class="activity-time">{{ getTimeAgo(order.createdAt || new Date()) }}</div>
                  </div>
                  <div class="activity-status">
                    <span
                      class="status-badge"
                      :class="getOrderStatusClass(order.status || 'UNKNOWN')"
                    >
                      {{ getOrderStatusText(order.status || 'UNKNOWN') }}
                    </span>
                  </div>
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
import { ref, computed, onMounted } from 'vue'
import { adminAPI } from '@/services/api.js'

export default {
  name: 'AdminDashboard',
  setup() {
    // Reactive data
    const selectedTimeRange = ref(30)
    const loading = ref(false)
    const error = ref(null)

    // Real data from API
    const dashboardData = ref({
      totalUsers: 0,
      activeUsers: 0,
      totalProducts: 0,
      activeProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      processingOrders: 0,
      completedOrders: 0,
      totalRevenue: 0,
      recentOrders: [],
      userChange: 0,
      productChange: 0,
      orderChange: 0,
      revenueChange: 0,
    })

    // Computed properties
    const stats = computed(() => ({
      users: {
        total: dashboardData.value.totalUsers,
        change: dashboardData.value.userChange || 0,
        pending: Math.max(0, dashboardData.value.totalUsers - dashboardData.value.activeUsers),
      },
      products: {
        total: dashboardData.value.totalProducts,
        change: dashboardData.value.productChange || 0,
        pending: Math.max(
          0,
          dashboardData.value.totalProducts - dashboardData.value.activeProducts
        ),
      },
      orders: {
        total: dashboardData.value.totalOrders,
        change: dashboardData.value.orderChange || 0,
        pending: dashboardData.value.pendingOrders,
      },
      revenue: {
        total: dashboardData.value.totalRevenue,
        change: dashboardData.value.revenueChange || 0,
      },
    }))

    const recentOrders = computed(() =>
      dashboardData.value.recentOrders.map((order) => ({
        id: order.id || 'N/A',
        customerName: order.customerName || 'Khách hàng',
        total: order.totalAmount || 0,
        status: order.status || 'UNKNOWN',
        createdAt: order.createdAt ? new Date(order.createdAt) : new Date(),
      }))
    )

    // Methods
    const formatNumber = (num) => {
      if (!num && num !== 0) return '0'
      return new Intl.NumberFormat('vi-VN').format(num)
    }

    const formatCurrency = (amount) => {
      if (!amount && amount !== 0) return '₫0'
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const getTimeAgo = (date) => {
      try {
        const now = new Date()
        const diffMs = now - date
        const diffMins = Math.floor(diffMs / (1000 * 60))
        const diffHours = Math.floor(diffMins / 60)

        if (diffMins < 1) return 'Vừa xong'
        if (diffMins < 60) return `${diffMins} phút trước`
        if (diffHours < 24) return `${diffHours} giờ trước`
        return 'Hôm qua'
      } catch (error) {
        return 'Không xác định'
      }
    }

    const getOrderStatusClass = (status) => {
      const classes = {
        PENDING: 'warning',
        PROCESSING: 'info',
        SHIPPED: 'primary',
        DELIVERED: 'success',
        CANCELLED: 'danger',
      }
      return classes[status] || 'secondary'
    }

    const getOrderStatusIcon = (status) => {
      const icons = {
        PENDING: '⏳',
        PROCESSING: '⚙️',
        SHIPPED: '🚚',
        DELIVERED: '✅',
        CANCELLED: '❌',
      }
      return icons[status] || '📋'
    }

    const getOrderStatusText = (status) => {
      const texts = {
        PENDING: 'Chờ xử lý',
        PROCESSING: 'Đang xử lý',
        SHIPPED: 'Đã gửi',
        DELIVERED: 'Đã giao',
        CANCELLED: 'Đã hủy',
      }
      return texts[status] || status
    }

    const loadDashboardData = async () => {
      try {
        loading.value = true
        error.value = null

        console.log('🔄 Loading dashboard data...')
        const response = await adminAPI.getDashboard(selectedTimeRange.value)

        if (response.data) {
          dashboardData.value = response.data
          console.log('✅ Dashboard data loaded:', response.data)
          console.log('📊 Stats computed:', stats.value)
          console.log('🛒 Recent orders:', recentOrders.value)
        }
      } catch (err) {
        console.error('❌ Error loading dashboard data:', err)
        error.value = 'Không thể tải dữ liệu dashboard. Vui lòng thử lại.'

        // Set default values to prevent UI errors
        dashboardData.value = {
          totalUsers: 0,
          activeUsers: 0,
          totalProducts: 0,
          activeProducts: 0,
          totalOrders: 0,
          pendingOrders: 0,
          processingOrders: 0,
          completedOrders: 0,
          totalRevenue: 0,
          recentOrders: [],
          userChange: 0,
          productChange: 0,
          orderChange: 0,
          revenueChange: 0,
        }
      } finally {
        loading.value = false
      }
    }

    const refreshData = async () => {
      await loadDashboardData()
    }

    // Lifecycle
    onMounted(() => {
      console.log('🚀 Admin Dashboard mounted')
      loadDashboardData()
    })

    return {
      selectedTimeRange,
      loading,
      error,
      stats,
      recentOrders,
      formatNumber,
      formatCurrency,
      getTimeAgo,
      getOrderStatusClass,
      getOrderStatusIcon,
      getOrderStatusText,
      refreshData,
      loadDashboardData,
    }
  },
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  padding: 2rem 0;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-content h1 {
  font-size: 2.5rem;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.header-content p {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.form-select {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.error-container {
  text-align: center;
  padding: 4rem 0;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.error-container h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.error-container p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.loading-container {
  text-align: center;
  padding: 4rem 0;
}

.cosmic-loader {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 2rem;
}

.planet {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 3rem;
  animation: float 3s ease-in-out infinite;
}

.orbit {
  position: absolute;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  width: 100%;
  height: 100%;
  animation: spin 2s linear infinite;
}

.orbit-2 {
  width: 130%;
  height: 130%;
  top: -15%;
  left: -15%;
  animation-duration: 3s;
  animation-direction: reverse;
}

@keyframes float {
  0%,
  100% {
    transform: translate(-50%, -50%) translateY(0);
  }
  50% {
    transform: translate(-50%, -50%) translateY(-10px);
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-card .stat-icon + .stat-content h3 {
  font-size: 1.3rem;
}

.stat-content h3 {
  font-size: 2rem;
  color: var(--text-accent);
  margin-bottom: 0.25rem;
  font-weight: 700;
  word-break: break-all;
  white-space: normal;
}

.stat-content p {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
}

.stat-change.positive {
  color: #10b981;
}

.stat-change.negative {
  color: #ef4444;
}

.quick-actions-section {
  margin-bottom: 3rem;
}

.quick-actions-section h2 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.quick-action-card {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
  text-decoration: none;
  color: inherit;
}

.quick-action-card:hover,
.quick-action-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
  cursor: pointer;
}

.quick-action-card.loading {
  opacity: 0.7;
  pointer-events: none;
}

.quick-action-card.loading .action-content p {
  color: var(--text-secondary);
  font-style: italic;
}

.action-icon {
  font-size: 2rem;
  opacity: 0.8;
}

.action-content {
  flex: 1;
}

.action-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 1.1rem;
}

.action-content p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.action-arrow {
  color: var(--text-accent);
  font-size: 1.2rem;
  opacity: 0.7;
}

.activity-section {
  margin-bottom: 3rem;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}

.activity-card {
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.activity-header h3 {
  color: var(--text-accent);
  font-size: 1.1rem;
}

.view-all-link {
  color: var(--text-accent);
  text-decoration: none;
  font-size: 0.9rem;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.view-all-link:hover {
  opacity: 1;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty-state {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.activity-item:hover {
  background: rgba(0, 212, 255, 0.1);
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.status-icon {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-icon.warning {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
}

.status-icon.info {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
}

.status-icon.primary {
  background: rgba(0, 123, 255, 0.2);
  color: #007bff;
}

.status-icon.success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-icon.danger {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.activity-content {
  flex: 1;
}

.activity-title {
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.activity-subtitle {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.activity-time {
  color: var(--text-secondary);
  font-size: 0.8rem;
  opacity: 0.7;
}

.activity-status {
  flex: none;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.warning {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
}

.status-badge.info {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
}

.status-badge.primary {
  background: rgba(0, 123, 255, 0.2);
  color: #007bff;
}

.status-badge.success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.danger {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: space-between;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }

  .quick-actions-grid {
    grid-template-columns: 1fr;
  }

  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style>
