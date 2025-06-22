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
            <select v-model="selectedTimeRange" @change="loadDashboardData" class="form-select">
              <option value="7">7 ngày qua</option>
              <option value="30">30 ngày qua</option>
              <option value="90">3 tháng qua</option>
              <option value="365">1 năm qua</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="cosmic-loader">
          <div class="planet"></div>
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
              <div class="stat-change" :class="{ positive: stats.users.change > 0, negative: stats.users.change < 0 }">
                <span>{{ stats.users.change > 0 ? '↗' : '↘' }} {{ Math.abs(stats.users.change) }}%</span>
                <small>so với tháng trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">📦</div>
            <div class="stat-content">
              <h3>{{ formatNumber(stats.products.total) }}</h3>
              <p>Tổng sản phẩm</p>
              <div class="stat-change" :class="{ positive: stats.products.change > 0, negative: stats.products.change < 0 }">
                <span>{{ stats.products.change > 0 ? '↗' : '↘' }} {{ Math.abs(stats.products.change) }}%</span>
                <small>so với tháng trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">🛒</div>
            <div class="stat-content">
              <h3>{{ formatNumber(stats.orders.total) }}</h3>
              <p>Tổng đơn hàng</p>
              <div class="stat-change" :class="{ positive: stats.orders.change > 0, negative: stats.orders.change < 0 }">
                <span>{{ stats.orders.change > 0 ? '↗' : '↘' }} {{ Math.abs(stats.orders.change) }}%</span>
                <small>so với tháng trước</small>
              </div>
            </div>
          </div>

          <div class="stat-card space-card">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <h3>{{ formatCurrency(stats.revenue.total) }}</h3>
              <p>Tổng doanh thu</p>
              <div class="stat-change" :class="{ positive: stats.revenue.change > 0, negative: stats.revenue.change < 0 }">
                <span>{{ stats.revenue.change > 0 ? '↗' : '↘' }} {{ Math.abs(stats.revenue.change) }}%</span>
                <small>so với tháng trước</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Charts Section -->
        <div class="charts-section">
          <div class="chart-row">
            <!-- Revenue Chart -->
            <div class="chart-container space-card">
              <div class="chart-header">
                <h3>📈 Doanh thu theo thời gian</h3>
                <div class="chart-actions">
                  <button 
                    v-for="period in chartPeriods" 
                    :key="period.value"
                    @click="selectedChartPeriod = period.value"
                    class="period-btn"
                    :class="{ active: selectedChartPeriod === period.value }"
                  >
                    {{ period.label }}
                  </button>
                </div>
              </div>
              <div class="chart-content">
                <canvas ref="revenueChart" width="400" height="200"></canvas>
              </div>
            </div>

            <!-- Order Status Chart -->
            <div class="chart-container space-card">
              <div class="chart-header">
                <h3>📊 Trạng thái đơn hàng</h3>
              </div>
              <div class="chart-content">
                <canvas ref="orderStatusChart" width="300" height="300"></canvas>
              </div>
            </div>
          </div>

          <!-- Category Performance -->
          <div class="chart-container space-card">
            <div class="chart-header">
              <h3>🏷️ Hiệu suất theo danh mục</h3>
            </div>
            <div class="category-performance">
              <div v-for="category in categoryPerformance" :key="category.id" class="category-item">
                <div class="category-info">
                  <h4>{{ category.name }}</h4>
                  <div class="category-stats">
                    <span>{{ category.productCount }} sản phẩm</span>
                    <span>{{ formatCurrency(category.revenue) }}</span>
                  </div>
                </div>
                <div class="category-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: category.percentage + '%' }"></div>
                  </div>
                  <span class="percentage">{{ category.percentage }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tables Section -->
        <div class="tables-section">
          <div class="table-row">
            <!-- Recent Orders -->
            <div class="table-container space-card">
              <div class="table-header">
                <h3>🛒 Đơn hàng gần đây</h3>
                <router-link to="/admin/orders" class="btn btn-secondary btn-small">Xem tất cả</router-link>
              </div>
              <div class="table-content">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Mã đơn</th>
                      <th>Khách hàng</th>
                      <th>Tổng tiền</th>
                      <th>Trạng thái</th>
                      <th>Ngày tạo</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in recentOrders" :key="order.id">
                      <td>
                        <router-link :to="`/admin/orders/${order.id}`" class="order-link">
                          #{{ order.id.slice(-6) }}
                        </router-link>
                      </td>
                      <td>
                        <div class="customer-info">
                          <img :src="order.customer.avatar" :alt="order.customer.name" class="customer-avatar" />
                          <span>{{ order.customer.name }}</span>
                        </div>
                      </td>
                      <td class="currency">{{ formatCurrency(order.total) }}</td>
                      <td>
                        <span class="status-badge" :class="order.status.toLowerCase()">
                          {{ getStatusText(order.status) }}
                        </span>
                      </td>
                      <td>{{ formatDate(order.createdAt) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Top Products -->
            <div class="table-container space-card">
              <div class="table-header">
                <h3>🏆 Sản phẩm bán chạy</h3>
                <router-link to="/admin/products" class="btn btn-secondary btn-small">Xem tất cả</router-link>
              </div>
              <div class="table-content">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th>Đã bán</th>
                      <th>Doanh thu</th>
                      <th>Đánh giá</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="product in topProducts" :key="product.id">
                      <td>
                        <div class="product-info">
                          <img :src="product.image" :alt="product.name" class="product-image" />
                          <div>
                            <router-link :to="`/products/${product.id}`" class="product-name">
                              {{ product.name }}
                            </router-link>
                            <p class="product-price">{{ formatCurrency(product.price) }}</p>
                          </div>
                        </div>
                      </td>
                      <td class="sold-count">{{ formatNumber(product.soldCount) }}</td>
                      <td class="currency">{{ formatCurrency(product.revenue) }}</td>
                      <td>
                        <div class="rating">
                          <span class="rating-value">{{ product.rating }}</span>
                          <div class="stars">
                            <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= product.rating }">⭐</span>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Recent Users -->
          <div class="table-container space-card">
            <div class="table-header">
              <h3>👤 Người dùng mới</h3>
              <router-link to="/admin/users" class="btn btn-secondary btn-small">Xem tất cả</router-link>
            </div>
            <div class="table-content">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Người dùng</th>
                    <th>Email</th>
                    <th>Vai trò</th>
                    <th>Trạng thái</th>
                    <th>Ngày tham gia</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="user in recentUsers" :key="user.id">
                    <td>
                      <div class="user-info">
                        <img :src="user.avatar" :alt="user.name" class="user-avatar" />
                        <span>{{ user.name }}</span>
                      </div>
                    </td>
                    <td>{{ user.email }}</td>
                    <td>
                      <span class="role-badge" :class="user.role.toLowerCase()">
                        {{ getRoleText(user.role) }}
                      </span>
                    </td>
                    <td>
                      <span class="status-badge" :class="user.status.toLowerCase()">
                        {{ user.status === 'active' ? 'Hoạt động' : 'Bị khóa' }}
                      </span>
                    </td>
                    <td>{{ formatDate(user.createdAt) }}</td>
                    <td>
                      <div class="action-buttons">
                        <button @click="toggleUserStatus(user)" class="btn btn-sm" :class="user.status === 'active' ? 'btn-warning' : 'btn-success'">
                          {{ user.status === 'active' ? '🔒 Khóa' : '🔓 Mở khóa' }}
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Alerts & Notifications -->
        <div class="alerts-section">
          <div class="alert-container space-card">
            <h3>⚠️ Cảnh báo hệ thống</h3>
            <div class="alerts-list">
              <div v-for="alert in systemAlerts" :key="alert.id" class="alert-item" :class="alert.type">
                <div class="alert-icon">{{ alert.icon }}</div>
                <div class="alert-content">
                  <h4>{{ alert.title }}</h4>
                  <p>{{ alert.message }}</p>
                  <small>{{ formatDate(alert.createdAt) }}</small>
                </div>
                <button @click="dismissAlert(alert.id)" class="alert-dismiss">×</button>
              </div>
              
              <div v-if="systemAlerts.length === 0" class="no-alerts">
                <span>✅ Không có cảnh báo nào</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'AdminDashboard',
  setup() {
    const authStore = useAuthStore()
    
    // Reactive data
    const loading = ref(true)
    const selectedTimeRange = ref('30')
    const selectedChartPeriod = ref('week')
    const stats = ref({
      users: { total: 0, change: 0 },
      products: { total: 0, change: 0 },
      orders: { total: 0, change: 0 },
      revenue: { total: 0, change: 0 }
    })
    const recentOrders = ref([])
    const topProducts = ref([])
    const recentUsers = ref([])
    const categoryPerformance = ref([])
    const systemAlerts = ref([])
    
    // Chart refs
    const revenueChart = ref(null)
    const orderStatusChart = ref(null)
    
    // Chart periods
    const chartPeriods = [
      { label: 'Tuần', value: 'week' },
      { label: 'Tháng', value: 'month' },
      { label: 'Quý', value: 'quarter' },
      { label: 'Năm', value: 'year' }
    ]
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const formatNumber = (number) => {
      return new Intl.NumberFormat('vi-VN').format(number)
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        'pending': 'Chờ xử lý',
        'confirmed': 'Đã xác nhận',
        'shipping': 'Đang giao',
        'delivered': 'Đã giao',
        'cancelled': 'Đã hủy'
      }
      return statusMap[status] || status
    }
    
    const getRoleText = (role) => {
      const roleMap = {
        'admin': 'Quản trị viên',
        'seller': 'Người bán',
        'user': 'Khách hàng'
      }
      return roleMap[role] || role
    }
    
    const loadDashboardData = async () => {
      try {
        loading.value = true
        
        // Simulate API calls
        await Promise.all([
          loadStats(),
          loadRecentOrders(),
          loadTopProducts(),
          loadRecentUsers(),
          loadCategoryPerformance(),
          loadSystemAlerts()
        ])
        
        // Initialize charts after data is loaded
        await nextTick()
        initializeCharts()
        
      } catch (error) {
        console.error('Error loading dashboard data:', error)
      } finally {
        loading.value = false
      }
    }
    
    const loadStats = async () => {
      // Mock data
      stats.value = {
        users: { total: 15420, change: 12.5 },
        products: { total: 8750, change: 8.2 },
        orders: { total: 3240, change: -2.1 },
        revenue: { total: 2450000000, change: 15.8 }
      }
    }
    
    const loadRecentOrders = async () => {
      // Mock data
      recentOrders.value = [
        {
          id: 'order_001_recent',
          customer: {
            name: 'Nguyễn Văn A',
            avatar: '/user-avatar-1.jpg'
          },
          total: 2500000,
          status: 'pending',
          createdAt: '2024-01-20T10:30:00Z'
        },
        {
          id: 'order_002_recent',
          customer: {
            name: 'Trần Thị B',
            avatar: '/user-avatar-2.jpg'
          },
          total: 1800000,
          status: 'confirmed',
          createdAt: '2024-01-20T09:15:00Z'
        },
        {
          id: 'order_003_recent',
          customer: {
            name: 'Lê Văn C',
            avatar: '/user-avatar-3.jpg'
          },
          total: 3200000,
          status: 'shipping',
          createdAt: '2024-01-19T16:45:00Z'
        },
        {
          id: 'order_004_recent',
          customer: {
            name: 'Phạm Thị D',
            avatar: '/user-avatar-4.jpg'
          },
          total: 950000,
          status: 'delivered',
          createdAt: '2024-01-19T14:20:00Z'
        }
      ]
    }
    
    const loadTopProducts = async () => {
      // Mock data
      topProducts.value = [
        {
          id: 'product_001',
          name: 'Laptop Gaming Galactic Pro',
          image: '/placeholder-product.jpg',
          price: 25000000,
          soldCount: 245,
          revenue: 6125000000,
          rating: 4.8
        },
        {
          id: 'product_002',
          name: 'Smartphone Cosmic X12',
          image: '/placeholder-phone.jpg',
          price: 15000000,
          soldCount: 189,
          revenue: 2835000000,
          rating: 4.6
        },
        {
          id: 'product_003',
          name: 'Gaming Mouse Nebula',
          image: '/placeholder-mouse.jpg',
          price: 1500000,
          soldCount: 567,
          revenue: 850500000,
          rating: 4.7
        },
        {
          id: 'product_004',
          name: 'Mechanical Keyboard Cosmos',
          image: '/placeholder-keyboard.jpg',
          price: 2200000,
          soldCount: 334,
          revenue: 734800000,
          rating: 4.5
        }
      ]
    }
    
    const loadRecentUsers = async () => {
      // Mock data
      recentUsers.value = [
        {
          id: 'user_001',
          name: 'Hoàng Văn E',
          email: 'hoang.e@example.com',
          avatar: '/user-avatar-5.jpg',
          role: 'user',
          status: 'active',
          createdAt: '2024-01-20T08:30:00Z'
        },
        {
          id: 'user_002',
          name: 'Ngô Thị F',
          email: 'ngo.f@example.com',
          avatar: '/user-avatar-6.jpg',
          role: 'seller',
          status: 'active',
          createdAt: '2024-01-19T20:15:00Z'
        },
        {
          id: 'user_003',
          name: 'Vũ Văn G',
          email: 'vu.g@example.com',
          avatar: '/user-avatar-7.jpg',
          role: 'user',
          status: 'blocked',
          createdAt: '2024-01-19T15:45:00Z'
        }
      ]
    }
    
    const loadCategoryPerformance = async () => {
      // Mock data
      categoryPerformance.value = [
        {
          id: 'cat_001',
          name: 'Công nghệ',
          productCount: 2450,
          revenue: 15600000000,
          percentage: 85
        },
        {
          id: 'cat_002',
          name: 'Thời trang',
          productCount: 1890,
          revenue: 8900000000,
          percentage: 68
        },
        {
          id: 'cat_003',
          name: 'Gia dụng',
          productCount: 1230,
          revenue: 5400000000,
          percentage: 52
        },
        {
          id: 'cat_004',
          name: 'Sách & Văn phòng phẩm',
          productCount: 890,
          revenue: 2100000000,
          percentage: 28
        },
        {
          id: 'cat_005',
          name: 'Thể thao',
          productCount: 567,
          revenue: 1800000000,
          percentage: 22
        }
      ]
    }
    
    const loadSystemAlerts = async () => {
      // Mock data
      systemAlerts.value = [
        {
          id: 'alert_001',
          type: 'warning',
          icon: '⚠️',
          title: 'Sản phẩm sắp hết hàng',
          message: 'Có 15 sản phẩm có số lượng tồn kho dưới 10.',
          createdAt: '2024-01-20T10:00:00Z'
        },
        {
          id: 'alert_002',
          type: 'info',
          icon: 'ℹ️',
          title: 'Cập nhật hệ thống',
          message: 'Hệ thống sẽ được bảo trì vào 2h sáng ngày mai.',
          createdAt: '2024-01-20T09:30:00Z'
        },
        {
          id: 'alert_003',
          type: 'error',
          icon: '🚨',
          title: 'Thanh toán thất bại',
          message: 'Có 3 đơn hàng thanh toán thất bại cần xử lý.',
          createdAt: '2024-01-20T08:45:00Z'
        }
      ]
    }
    
    const initializeCharts = () => {
      if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded')
        return
      }
      
      // Revenue Chart
      if (revenueChart.value) {
        const ctx = revenueChart.value.getContext('2d')
        new Chart(ctx, {
          type: 'line',
          data: {
            labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
            datasets: [{
              label: 'Doanh thu',
              data: [120000000, 190000000, 300000000, 500000000, 200000000, 300000000, 450000000],
              borderColor: '#00d4ff',
              backgroundColor: 'rgba(0, 212, 255, 0.1)',
              tension: 0.4,
              fill: true
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              }
            },
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function(value) {
                    return new Intl.NumberFormat('vi-VN', {
                      style: 'currency',
                      currency: 'VND',
                      notation: 'compact'
                    }).format(value)
                  }
                }
              }
            }
          }
        })
      }
      
      // Order Status Chart
      if (orderStatusChart.value) {
        const ctx = orderStatusChart.value.getContext('2d')
        new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels: ['Đã giao', 'Đang giao', 'Đã xác nhận', 'Chờ xử lý', 'Đã hủy'],
            datasets: [{
              data: [45, 25, 15, 10, 5],
              backgroundColor: [
                '#10b981',
                '#3b82f6', 
                '#f59e0b',
                '#6b7280',
                '#ef4444'
              ]
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                position: 'bottom'
              }
            }
          }
        })
      }
    }
    
    const refreshData = () => {
      loadDashboardData()
    }
    
    const toggleUserStatus = async (user) => {
      try {
        // TODO: API call to toggle user status
        user.status = user.status === 'active' ? 'blocked' : 'active'
        alert(`Đã ${user.status === 'active' ? 'mở khóa' : 'khóa'} người dùng ${user.name}`)
      } catch (error) {
        console.error('Error toggling user status:', error)
      }
    }
    
    const dismissAlert = (alertId) => {
      systemAlerts.value = systemAlerts.value.filter(alert => alert.id !== alertId)
    }
    
    // Auto refresh every 5 minutes
    let refreshInterval
    
    // Lifecycle
    onMounted(() => {
      loadDashboardData()
      refreshInterval = setInterval(() => {
        loadDashboardData()
      }, 5 * 60 * 1000) // 5 minutes
    })
    
    onUnmounted(() => {
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
    })
    
    return {
      loading,
      selectedTimeRange,
      selectedChartPeriod,
      stats,
      recentOrders,
      topProducts,
      recentUsers,
      categoryPerformance,
      systemAlerts,
      revenueChart,
      orderStatusChart,
      chartPeriods,
      formatCurrency,
      formatNumber,
      formatDate,
      getStatusText,
      getRoleText,
      loadDashboardData,
      refreshData,
      toggleUserStatus,
      dismissAlert
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  padding: 2rem 0;
  background: var(--space-gradient);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 2rem;
}

.header-content h1 {
  font-size: 2.5rem;
  font-weight: 700;
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

.time-range-selector .form-select {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  min-width: 120px;
}

.loading-container {
  text-align: center;
  padding: 4rem 0;
}

.cosmic-loader {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
}

.planet {
  width: 40px;
  height: 40px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

.orbit:nth-child(2) {
  width: 60px;
  height: 60px;
  top: 10px;
  left: 10px;
}

.orbit-2 {
  width: 80px;
  height: 80px;
  top: 0;
  left: 0;
  animation-duration: 4s;
  animation-direction: reverse;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.1); }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.stat-card {
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--aurora-gradient);
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 3rem;
  opacity: 0.8;
}

.stat-content h3 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.stat-content p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.stat-change {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-change span {
  font-weight: 600;
  font-size: 0.9rem;
}

.stat-change.positive span {
  color: #10b981;
}

.stat-change.negative span {
  color: #ef4444;
}

.stat-change small {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.charts-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.chart-container {
  padding: 2rem;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.chart-header h3 {
  color: var(--text-accent);
  font-size: 1.3rem;
}

.chart-actions {
  display: flex;
  gap: 0.5rem;
}

.period-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.period-btn:hover,
.period-btn.active {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.chart-content {
  height: 300px;
  position: relative;
}

.category-performance {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.1);
}

.category-info {
  flex: 1;
}

.category-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.category-stats {
  display: flex;
  gap: 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.category-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 150px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--aurora-gradient);
  transition: width 0.8s ease;
}

.percentage {
  color: var(--text-accent);
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

.tables-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.table-container {
  padding: 2rem;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.table-header h3 {
  color: var(--text-accent);
  font-size: 1.3rem;
}

.table-content {
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
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.data-table th {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.9rem;
  background: rgba(26, 26, 46, 0.3);
}

.data-table td {
  color: var(--text-primary);
}

.order-link,
.product-name {
  color: var(--text-accent);
  text-decoration: none;
  font-weight: 500;
}

.order-link:hover,
.product-name:hover {
  text-decoration: underline;
}

.customer-info,
.user-info,
.product-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.customer-avatar,
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.product-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
}

.product-price {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.currency {
  color: var(--text-accent);
  font-weight: 600;
}

.sold-count {
  color: var(--text-primary);
  font-weight: 600;
}

.status-badge,
.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.pending {
  background: rgba(107, 114, 128, 0.2);
  color: #6b7280;
}

.status-badge.confirmed {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.status-badge.shipping {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.status-badge.delivered {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.blocked {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.role-badge.admin {
  background: rgba(147, 51, 234, 0.2);
  color: #9333ea;
}

.role-badge.seller {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.role-badge.user {
  background: rgba(107, 114, 128, 0.2);
  color: #6b7280;
}

.rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.rating-value {
  color: var(--text-accent);
  font-weight: 600;
}

.stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  font-size: 0.8rem;
  opacity: 0.3;
}

.star.filled {
  opacity: 1;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  border-radius: 6px;
}

.btn-warning {
  background: #f59e0b;
  color: white;
  border: none;
}

.btn-warning:hover {
  background: #d97706;
}

.btn-success {
  background: #10b981;
  color: white;
  border: none;
}

.btn-success:hover {
  background: #059669;
}

.alerts-section {
  margin-top: 2rem;
}

.alert-container {
  padding: 2rem;
}

.alert-container h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 12px;
  border-left: 4px solid;
  position: relative;
}

.alert-item.warning {
  background: rgba(245, 158, 11, 0.1);
  border-color: #f59e0b;
}

.alert-item.info {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
}

.alert-item.error {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

.alert-icon {
  font-size: 1.5rem;
  flex: none;
}

.alert-content {
  flex: 1;
}

.alert-content h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.alert-content p {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.alert-content small {
  color: var(--text-secondary);
  opacity: 0.7;
}

.alert-dismiss {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1.2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.alert-dismiss:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.no-alerts {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }

  .chart-row {
    grid-template-columns: 1fr;
  }

  .table-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 1rem 0;
  }

  .dashboard-header h1 {
    font-size: 2rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 1.5rem;
  }

  .chart-container,
  .table-container,
  .alert-container {
    padding: 1.5rem;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .data-table {
    font-size: 0.9rem;
  }

  .data-table th,
  .data-table td {
    padding: 0.75rem 0.5rem;
  }

  .alert-item {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }

  .category-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .category-progress {
    width: 100%;
    justify-content: space-between;
  }

  .customer-info,
  .user-info,
  .product-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>