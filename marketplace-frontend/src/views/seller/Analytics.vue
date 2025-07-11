<template>
  <div class="seller-analytics-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1>📊 Phân tích & Báo cáo</h1>
          <p>Theo dõi hiệu suất kinh doanh và xu hướng bán hàng</p>
        </div>
        <div class="header-actions">
          <div class="period-selector">
            <select v-model="selectedPeriod" @change="loadAnalytics" class="period-select">
              <option value="7d">7 ngày qua</option>
              <option value="30d">30 ngày qua</option>
              <option value="90d">90 ngày qua</option>
              <option value="1y">1 năm qua</option>
              <option value="custom">Tùy chỉnh</option>
            </select>
          </div>
          <button @click="refreshAnalytics" :disabled="loading" class="btn-refresh">
            <span class="icon">🔄</span>
            {{ loading ? 'Đang tải...' : 'Làm mới' }}
          </button>
          <button @click="exportReport" :disabled="exporting" class="btn-export">
            <span class="icon">📄</span>
            {{ exporting ? 'Đang xuất...' : 'Xuất báo cáo' }}
          </button>
        </div>
      </div>

      <!-- Period Comparison -->
      <div class="period-comparison">
        <div class="comparison-item">
          <span class="period-label">Kỳ hiện tại</span>
          <span class="period-value">{{ formatDateRange(currentPeriod) }}</span>
        </div>
        <div class="comparison-divider">vs</div>
        <div class="comparison-item">
          <span class="period-label">Kỳ trước</span>
          <span class="period-value">{{ formatDateRange(previousPeriod) }}</span>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu phân tích...</p>
    </div>

    <!-- Analytics Content -->
    <div v-else class="analytics-content">
      <!-- KPI Cards -->
      <div class="kpi-section">
        <h2>🎯 Chỉ số kinh doanh chính</h2>
        <div class="kpi-grid">
          <div class="kpi-card revenue">
            <div class="kpi-header">
              <span class="kpi-title">Doanh thu</span>
              <span class="kpi-icon">💰</span>
            </div>
            <div class="kpi-value">{{ formatCurrency(analytics.revenue.current) }}</div>
            <div class="kpi-change" :class="getChangeClass(analytics.revenue.growth)">
              <span class="change-icon">{{ getChangeIcon(analytics.revenue.growth) }}</span>
              <span class="change-value">{{ formatPercentage(analytics.revenue.growth) }}</span>
              <span class="change-label">so với kỳ trước</span>
            </div>
          </div>

          <div class="kpi-card orders">
            <div class="kpi-header">
              <span class="kpi-title">Đơn hàng</span>
              <span class="kpi-icon">📦</span>
            </div>
            <div class="kpi-value">{{ analytics.orders.current.toLocaleString() }}</div>
            <div class="kpi-change" :class="getChangeClass(analytics.orders.growth)">
              <span class="change-icon">{{ getChangeIcon(analytics.orders.growth) }}</span>
              <span class="change-value">{{ formatPercentage(analytics.orders.growth) }}</span>
              <span class="change-label">so với kỳ trước</span>
            </div>
          </div>

          <div class="kpi-card conversion">
            <div class="kpi-header">
              <span class="kpi-title">Tỷ lệ chuyển đổi</span>
              <span class="kpi-icon">📈</span>
            </div>
            <div class="kpi-value">{{ formatPercentage(analytics.conversion.current) }}</div>
            <div class="kpi-change" :class="getChangeClass(analytics.conversion.growth)">
              <span class="change-icon">{{ getChangeIcon(analytics.conversion.growth) }}</span>
              <span class="change-value">{{ formatPercentage(analytics.conversion.growth) }}</span>
              <span class="change-label">so với kỳ trước</span>
            </div>
          </div>

          <div class="kpi-card aov">
            <div class="kpi-header">
              <span class="kpi-title">Giá trị đơn hàng TB</span>
              <span class="kpi-icon">🛒</span>
            </div>
            <div class="kpi-value">{{ formatCurrency(analytics.averageOrderValue.current) }}</div>
            <div class="kpi-change" :class="getChangeClass(analytics.averageOrderValue.growth)">
              <span class="change-icon">{{ getChangeIcon(analytics.averageOrderValue.growth) }}</span>
              <span class="change-value">{{ formatPercentage(analytics.averageOrderValue.growth) }}</span>
              <span class="change-label">so với kỳ trước</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="charts-section">
        <!-- Revenue Chart -->
        <div class="chart-container revenue-chart">
          <div class="chart-header">
            <h3>📈 Xu hướng Doanh thu</h3>
            <div class="chart-controls">
              <div class="chart-type-selector">
                <button 
                  v-for="type in ['line', 'bar', 'area']" 
                  :key="type"
                  @click="revenueChartType = type"
                  :class="{ active: revenueChartType === type }"
                  class="chart-type-btn"
                >
                  {{ getChartTypeIcon(type) }}
                </button>
              </div>
            </div>
          </div>
          <div class="chart-content">
            <RevenueChart 
              :data="analytics.revenueChart" 
              :type="revenueChartType"
              :height="300"
            />
          </div>
        </div>

        <!-- Product Performance -->
        <div class="chart-container product-performance">
          <div class="chart-header">
            <h3>🏆 Hiệu suất Sản phẩm</h3>
            <div class="chart-controls">
              <select v-model="productMetric" @change="updateProductChart" class="metric-select">
                <option value="revenue">Doanh thu</option>
                <option value="orders">Số đơn hàng</option>
                <option value="views">Lượt xem</option>
                <option value="conversion">Tỷ lệ chuyển đổi</option>
              </select>
            </div>
          </div>
          <div class="chart-content">
            <ProductChart 
              :data="analytics.productPerformance" 
              :metric="productMetric"
              :height="300"
            />
          </div>
        </div>

        <!-- Order Status Distribution -->
        <div class="chart-container order-status">
          <div class="chart-header">
            <h3>📋 Phân bố Trạng thái Đơn hàng</h3>
          </div>
          <div class="chart-content">
            <OrderStatusChart 
              :data="analytics.orderStatus" 
              :height="300"
            />
          </div>
        </div>

        <!-- Customer Insights -->
        <div class="chart-container customer-insights">
          <div class="chart-header">
            <h3>👥 Phân tích Khách hàng</h3>
            <div class="chart-controls">
              <button 
                v-for="view in ['acquisition', 'retention', 'value']" 
                :key="view"
                @click="customerView = view"
                :class="{ active: customerView === view }"
                class="view-btn"
              >
                {{ getCustomerViewLabel(view) }}
              </button>
            </div>
          </div>
          <div class="chart-content">
            <CustomerChart 
              :data="analytics.customerInsights[customerView]" 
              :view="customerView"
              :height="300"
            />
          </div>
        </div>
      </div>

      <!-- Detailed Analytics -->
      <div class="detailed-analytics">
        <!-- Top Products Table -->
        <div class="analytics-table top-products">
          <div class="table-header">
            <h3>🔥 Sản phẩm bán chạy nhất</h3>
            <button @click="showAllProducts = !showAllProducts" class="toggle-view">
              {{ showAllProducts ? 'Thu gọn' : 'Xem tất cả' }}
            </button>
          </div>
          <div class="table-content">
            <div class="table-scroll">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Doanh thu</th>
                    <th>Đơn hàng</th>
                    <th>Chuyển đổi</th>
                    <th>Xu hướng</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="product in displayedTopProducts" 
                    :key="product.id"
                    class="table-row"
                  >
                    <td>
                      <div class="product-info">
                        <img :src="product.image" :alt="product.name" class="product-thumb" />
                        <div class="product-details">
                          <div class="product-name">{{ product.name }}</div>
                          <div class="product-sku">{{ product.sku }}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div class="metric-value">{{ formatCurrency(product.revenue) }}</div>
                    </td>
                    <td>
                      <div class="metric-value">{{ product.orders.toLocaleString() }}</div>
                    </td>
                    <td>
                      <div class="metric-value">{{ formatPercentage(product.conversion) }}</div>
                    </td>
                    <td>
                      <div class="trend-indicator" :class="getTrendClass(product.trend)">
                        <span class="trend-icon">{{ getTrendIcon(product.trend) }}</span>
                        <span class="trend-value">{{ formatPercentage(product.trend) }}</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Traffic Sources -->
        <div class="analytics-table traffic-sources">
          <div class="table-header">
            <h3>🌐 Nguồn Traffic</h3>
          </div>
          <div class="table-content">
            <div class="traffic-list">
              <div 
                v-for="source in analytics.trafficSources" 
                :key="source.name"
                class="traffic-item"
              >
                <div class="traffic-info">
                  <div class="traffic-icon">{{ source.icon }}</div>
                  <div class="traffic-details">
                    <div class="traffic-name">{{ source.name }}</div>
                    <div class="traffic-visitors">{{ source.visitors.toLocaleString() }} lượt truy cập</div>
                  </div>
                </div>
                <div class="traffic-metrics">
                  <div class="traffic-percentage">{{ formatPercentage(source.percentage) }}</div>
                  <div class="traffic-conversion">{{ formatPercentage(source.conversion) }} chuyển đổi</div>
                </div>
                <div class="traffic-chart">
                  <div 
                    class="traffic-bar" 
                    :style="{ width: source.percentage + '%', backgroundColor: source.color }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Performance Alerts -->
        <div class="analytics-alerts">
          <div class="alerts-header">
            <h3>⚠️ Cảnh báo Hiệu suất</h3>
          </div>
          <div class="alerts-content">
            <div v-if="performanceAlerts.length === 0" class="no-alerts">
              <div class="no-alerts-icon">✅</div>
              <p>Tất cả chỉ số đều ở mức tốt!</p>
            </div>
            <div v-else class="alerts-list">
              <div 
                v-for="alert in performanceAlerts" 
                :key="alert.id"
                class="alert-item"
                :class="alert.severity"
              >
                <div class="alert-icon">{{ alert.icon }}</div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-description">{{ alert.description }}</div>
                  <div class="alert-actions">
                    <button @click="dismissAlert(alert.id)" class="alert-btn dismiss">
                      Đã hiểu
                    </button>
                    <button v-if="alert.action" @click="alert.action()" class="alert-btn action">
                      {{ alert.actionLabel }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Export Options -->
      <div class="export-section">
        <div class="export-header">
          <h3>📊 Xuất Báo cáo</h3>
        </div>
        <div class="export-options">
          <div class="export-option">
            <div class="export-info">
              <div class="export-title">Báo cáo Doanh thu</div>
              <div class="export-description">Chi tiết doanh thu theo thời gian và sản phẩm</div>
            </div>
            <div class="export-actions">
              <button @click="exportRevenue('pdf')" class="export-btn pdf">PDF</button>
              <button @click="exportRevenue('excel')" class="export-btn excel">Excel</button>
            </div>
          </div>

          <div class="export-option">
            <div class="export-info">
              <div class="export-title">Báo cáo Sản phẩm</div>
              <div class="export-description">Hiệu suất và xu hướng từng sản phẩm</div>
            </div>
            <div class="export-actions">
              <button @click="exportProducts('pdf')" class="export-btn pdf">PDF</button>
              <button @click="exportProducts('excel')" class="export-btn excel">Excel</button>
            </div>
          </div>

          <div class="export-option">
            <div class="export-info">
              <div class="export-title">Báo cáo Khách hàng</div>
              <div class="export-description">Phân tích hành vi và giá trị khách hàng</div>
            </div>
            <div class="export-actions">
              <button @click="exportCustomers('pdf')" class="export-btn pdf">PDF</button>
              <button @click="exportCustomers('excel')" class="export-btn excel">Excel</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Custom Date Range Modal -->
    <div v-if="showDateModal" class="date-modal-overlay" @click="showDateModal = false">
      <div class="date-modal" @click.stop>
        <h3>📅 Chọn khoảng thời gian</h3>
        <div class="date-inputs">
          <div class="date-group">
            <label>Từ ngày:</label>
            <input v-model="customDateRange.start" type="date" class="date-input" />
          </div>
          <div class="date-group">
            <label>Đến ngày:</label>
            <input v-model="customDateRange.end" type="date" class="date-input" />
          </div>
        </div>
        <div class="date-actions">
          <button @click="showDateModal = false" class="btn-cancel">Hủy</button>
          <button @click="applyCustomDate" class="btn-apply">Áp dụng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useSellerStore } from '@/stores/seller'
import RevenueChart from '@/components/charts/RevenueChart.vue'
import ProductChart from '@/components/charts/ProductChart.vue'
import OrderStatusChart from '@/components/charts/OrderStatusChart.vue'
import CustomerChart from '@/components/charts/CustomerChart.vue'

const sellerStore = useSellerStore()

// Reactive data
const loading = ref(false)
const exporting = ref(false)
const selectedPeriod = ref('30d')
const revenueChartType = ref('line')
const productMetric = ref('revenue')
const customerView = ref('acquisition')
const showAllProducts = ref(false)
const showDateModal = ref(false)
const customDateRange = ref({
  start: '',
  end: ''
})

// Analytics data
const analytics = ref({
  revenue: {
    current: 125000000,
    growth: 15.2
  },
  orders: {
    current: 1247,
    growth: 8.7
  },
  conversion: {
    current: 3.2,
    growth: -2.1
  },
  averageOrderValue: {
    current: 850000,
    growth: 12.5
  },
  revenueChart: generateRevenueData(),
  productPerformance: generateProductData(),
  orderStatus: generateOrderStatusData(),
  customerInsights: {
    acquisition: generateCustomerAcquisitionData(),
    retention: generateCustomerRetentionData(),
    value: generateCustomerValueData()
  },
  topProducts: generateTopProductsData(),
  trafficSources: generateTrafficSourcesData()
})

const performanceAlerts = ref([
  {
    id: 1,
    severity: 'warning',
    icon: '⚠️',
    title: 'Tỷ lệ chuyển đổi giảm',
    description: 'Tỷ lệ chuyển đổi giảm 2.1% so với kỳ trước. Cần kiểm tra và tối ưu hóa.',
    actionLabel: 'Xem chi tiết',
    action: () => alert('Chuyển đến trang tối ưu hóa')
  },
  {
    id: 2,
    severity: 'info',
    icon: '💡',
    title: 'Cơ hội tăng doanh thu',
    description: 'Có 5 sản phẩm có tiềm năng tăng giá bán dựa trên nhu cầu thị trường.',
    actionLabel: 'Xem gợi ý',
    action: () => alert('Hiển thị gợi ý tối ưu giá')
  }
])

// Computed
const currentPeriod = computed(() => {
  const end = new Date()
  const start = new Date()
  
  switch (selectedPeriod.value) {
    case '7d':
      start.setDate(end.getDate() - 7)
      break
    case '30d':
      start.setDate(end.getDate() - 30)
      break
    case '90d':
      start.setDate(end.getDate() - 90)
      break
    case '1y':
      start.setFullYear(end.getFullYear() - 1)
      break
    default:
      return { start: new Date(), end: new Date() }
  }
  
  return { start, end }
})

const previousPeriod = computed(() => {
  const current = currentPeriod.value
  const duration = current.end.getTime() - current.start.getTime()
  
  return {
    start: new Date(current.start.getTime() - duration),
    end: new Date(current.start.getTime())
  }
})

const displayedTopProducts = computed(() => {
  return showAllProducts.value 
    ? analytics.value.topProducts 
    : analytics.value.topProducts.slice(0, 5)
})

// Methods
const loadAnalytics = async () => {
  loading.value = true
  try {
    await sellerStore.fetchAnalytics(selectedPeriod.value)
    
    // Update analytics with real data when available
    if (sellerStore.analytics.revenueData) {
      analytics.value = {
        ...analytics.value,
        ...sellerStore.analytics
      }
    }
  } catch (error) {
    console.error('Error loading analytics:', error)
  } finally {
    loading.value = false
  }
}

const refreshAnalytics = async () => {
  await loadAnalytics()
}

const exportReport = async () => {
  exporting.value = true
  try {
    await sellerStore.exportAnalyticsReport(selectedPeriod.value)
  } finally {
    exporting.value = false
  }
}

const exportRevenue = (format) => {
  alert(`Xuất báo cáo doanh thu định dạng ${format.toUpperCase()}`)
}

const exportProducts = (format) => {
  alert(`Xuất báo cáo sản phẩm định dạng ${format.toUpperCase()}`)
}

const exportCustomers = (format) => {
  alert(`Xuất báo cáo khách hàng định dạng ${format.toUpperCase()}`)
}

const updateProductChart = () => {
  // Update product chart based on selected metric
  console.log('Updating product chart for metric:', productMetric.value)
}

const dismissAlert = (alertId) => {
  const index = performanceAlerts.value.findIndex(alert => alert.id === alertId)
  if (index !== -1) {
    performanceAlerts.value.splice(index, 1)
  }
}

const applyCustomDate = () => {
  if (customDateRange.value.start && customDateRange.value.end) {
    selectedPeriod.value = 'custom'
    showDateModal.value = false
    loadAnalytics()
  }
}

// Utility functions
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatPercentage = (value) => {
  const absValue = Math.abs(value)
  return `${absValue.toFixed(1)}%`
}

const formatDateRange = (period) => {
  const start = period.start.toLocaleDateString('vi-VN')
  const end = period.end.toLocaleDateString('vi-VN')
  return `${start} - ${end}`
}

const getChangeClass = (value) => {
  if (value > 0) return 'positive'
  if (value < 0) return 'negative'
  return 'neutral'
}

const getChangeIcon = (value) => {
  if (value > 0) return '↗️'
  if (value < 0) return '↘️'
  return '➡️'
}

const getTrendClass = (value) => {
  if (value > 0) return 'trend-up'
  if (value < 0) return 'trend-down'
  return 'trend-neutral'
}

const getTrendIcon = (value) => {
  if (value > 5) return '🚀'
  if (value > 0) return '📈'
  if (value < -5) return '📉'
  if (value < 0) return '⬇️'
  return '➡️'
}

const getChartTypeIcon = (type) => {
  switch (type) {
    case 'line': return '📈'
    case 'bar': return '📊'
    case 'area': return '🏔️'
    default: return '📈'
  }
}

const getCustomerViewLabel = (view) => {
  switch (view) {
    case 'acquisition': return 'Tiếp cận'
    case 'retention': return 'Giữ chân'
    case 'value': return 'Giá trị'
    default: return view
  }
}

// Data generators (replace with real API calls)
function generateRevenueData() {
  const data = []
  const now = new Date()
  
  for (let i = 29; i >= 0; i--) {
    const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
    data.push({
      date: date.toISOString().split('T')[0],
      revenue: Math.floor(Math.random() * 10000000) + 2000000,
      orders: Math.floor(Math.random() * 50) + 20,
      visitors: Math.floor(Math.random() * 1000) + 500
    })
  }
  
  return data
}

function generateProductData() {
  return [
    { name: 'iPhone 15 Pro', revenue: 45000000, orders: 89, views: 2340, conversion: 3.8 },
    { name: 'Samsung Galaxy S24', revenue: 38000000, orders: 76, views: 1890, conversion: 4.0 },
    { name: 'MacBook Pro M3', revenue: 52000000, orders: 34, views: 1560, conversion: 2.2 },
    { name: 'iPad Air', revenue: 28000000, orders: 67, views: 2100, conversion: 3.2 },
    { name: 'AirPods Pro', revenue: 15000000, orders: 125, views: 3200, conversion: 3.9 }
  ]
}

function generateOrderStatusData() {
  return [
    { status: 'Đã giao', count: 856, percentage: 68.7, color: '#10b981' },
    { status: 'Đang giao', count: 234, percentage: 18.8, color: '#8b5cf6' },
    { status: 'Đang xử lý', count: 98, percentage: 7.9, color: '#3b82f6' },
    { status: 'Chờ xử lý', count: 45, percentage: 3.6, color: '#f59e0b' },
    { status: 'Đã hủy', count: 14, percentage: 1.1, color: '#ef4444' }
  ]
}

function generateCustomerAcquisitionData() {
  return [
    { channel: 'Organic Search', customers: 456, cost: 0, color: '#10b981' },
    { channel: 'Social Media', customers: 234, cost: 12500000, color: '#3b82f6' },
    { channel: 'Paid Ads', customers: 178, cost: 8750000, color: '#8b5cf6' },
    { channel: 'Email', customers: 89, cost: 2100000, color: '#f59e0b' },
    { channel: 'Referral', customers: 67, cost: 0, color: '#ef4444' }
  ]
}

function generateCustomerRetentionData() {
  return [
    { period: 'Tháng 1', retained: 89, total: 100 },
    { period: 'Tháng 2', retained: 76, total: 89 },
    { period: 'Tháng 3', retained: 68, total: 76 },
    { period: 'Tháng 4', retained: 61, total: 68 },
    { period: 'Tháng 5', retained: 58, total: 61 },
    { period: 'Tháng 6', retained: 54, total: 58 }
  ]
}

function generateCustomerValueData() {
  return [
    { segment: 'VIP', customers: 45, averageValue: 15000000, totalValue: 675000000 },
    { segment: 'Thường xuyên', customers: 234, averageValue: 3500000, totalValue: 819000000 },
    { segment: 'Thỉnh thoảng', customers: 567, averageValue: 1200000, totalValue: 680400000 },
    { segment: 'Mới', customers: 789, averageValue: 650000, totalValue: 512850000 }
  ]
}

function generateTopProductsData() {
  return [
    {
      id: 1,
      name: 'iPhone 15 Pro Max 256GB',
      sku: 'IPH15PM256',
      image: '/placeholder-product.jpg',
      revenue: 45000000,
      orders: 89,
      conversion: 3.8,
      trend: 12.5
    },
    {
      id: 2,
      name: 'Samsung Galaxy S24 Ultra',
      sku: 'SGS24U128',
      image: '/placeholder-product.jpg',
      revenue: 38000000,
      orders: 76,
      conversion: 4.0,
      trend: 8.3
    },
    {
      id: 3,
      name: 'MacBook Pro M3 14 inch',
      sku: 'MBP14M3512',
      image: '/placeholder-product.jpg',
      revenue: 52000000,
      orders: 34,
      conversion: 2.2,
      trend: -2.1
    },
    {
      id: 4,
      name: 'iPad Air M2 Wi-Fi',
      sku: 'IPADAIR64',
      image: '/placeholder-product.jpg',
      revenue: 28000000,
      orders: 67,
      conversion: 3.2,
      trend: 5.7
    },
    {
      id: 5,
      name: 'AirPods Pro 2nd Gen',
      sku: 'APPRO2ND',
      image: '/placeholder-product.jpg',
      revenue: 15000000,
      orders: 125,
      conversion: 3.9,
      trend: 18.9
    }
  ]
}

function generateTrafficSourcesData() {
  return [
    {
      name: 'Google Search',
      icon: '🔍',
      visitors: 15420,
      percentage: 42.3,
      conversion: 3.8,
      color: '#10b981'
    },
    {
      name: 'Facebook',
      icon: '📘',
      visitors: 8760,
      percentage: 24.1,
      conversion: 2.9,
      color: '#3b82f6'
    },
    {
      name: 'Direct',
      icon: '🌐',
      visitors: 5640,
      percentage: 15.5,
      conversion: 5.2,
      color: '#8b5cf6'
    },
    {
      name: 'Instagram',
      icon: '📷',
      visitors: 4230,
      percentage: 11.6,
      conversion: 2.1,
      color: '#f59e0b'
    },
    {
      name: 'YouTube',
      icon: '📺',
      visitors: 2380,
      percentage: 6.5,
      conversion: 1.8,
      color: '#ef4444'
    }
  ]
}

// Watchers
watch(selectedPeriod, (newPeriod) => {
  if (newPeriod === 'custom') {
    showDateModal.value = true
  } else {
    loadAnalytics()
  }
})

// Lifecycle
onMounted(() => {
  loadAnalytics()
})
</script>

<style scoped>
.seller-analytics-page {
  padding: 2rem;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(16, 16, 24, 0.95), rgba(26, 26, 46, 0.9));
  color: #ffffff;
}

.page-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.title-section h1 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-section p {
  color: #a0aec0;
  font-size: 1rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.period-select {
  padding: 0.5rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  font-size: 0.875rem;
}

.btn-refresh, .btn-export {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #00d4ff;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
}

.btn-refresh:hover, .btn-export:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-2px);
}

.btn-refresh:disabled, .btn-export:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.period-comparison {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.6);
  border-radius: 8px;
  font-size: 0.875rem;
}

.comparison-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.period-label {
  color: #a0aec0;
}

.period-value {
  color: #e2e8f0;
  font-weight: 500;
}

.comparison-divider {
  color: #00d4ff;
  font-weight: bold;
  font-size: 1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #a0aec0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 212, 255, 0.2);
  border-top: 4px solid #00d4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.analytics-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.kpi-section h2 {
  margin-bottom: 1rem;
  color: #e2e8f0;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.kpi-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.kpi-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 212, 255, 0.4);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.1);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.kpi-title {
  color: #a0aec0;
  font-size: 0.875rem;
  font-weight: 500;
}

.kpi-icon {
  font-size: 1.5rem;
}

.kpi-value {
  font-size: 2rem;
  font-weight: bold;
  color: #00d4ff;
  margin-bottom: 0.5rem;
}

.kpi-change {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.kpi-change.positive {
  color: #10b981;
}

.kpi-change.negative {
  color: #ef4444;
}

.kpi-change.neutral {
  color: #a0aec0;
}

.change-label {
  color: #a0aec0;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
}

.chart-container {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.chart-header h3 {
  color: #e2e8f0;
  font-size: 1.1rem;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.chart-controls {
  display: flex;
  gap: 0.5rem;
}

.chart-type-selector {
  display: flex;
  gap: 0.25rem;
}

.chart-type-btn {
  padding: 0.5rem;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1rem;
}

.chart-type-btn.active,
.chart-type-btn:hover {
  background: rgba(0, 212, 255, 0.2);
  border-color: rgba(0, 212, 255, 0.5);
  color: #00d4ff;
}

.metric-select {
  padding: 0.5rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  font-size: 0.875rem;
}

.view-btn {
  padding: 0.5rem 1rem;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
}

.view-btn.active,
.view-btn:hover {
  background: rgba(0, 212, 255, 0.2);
  border-color: rgba(0, 212, 255, 0.5);
  color: #00d4ff;
}

.chart-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(16, 16, 24, 0.4);
  border-radius: 8px;
  padding: 1rem;
}

.detailed-analytics {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.analytics-table {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.table-header h3 {
  color: #e2e8f0;
  font-size: 1.1rem;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.toggle-view {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #00d4ff;
  cursor: pointer;
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.toggle-view:hover {
  background: rgba(0, 212, 255, 0.2);
}

.table-scroll {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 0.75rem;
  color: #a0aec0;
  font-weight: 500;
  font-size: 0.875rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.data-table td {
  padding: 1rem 0.75rem;
  border-bottom: 1px solid rgba(107, 114, 128, 0.2);
}

.product-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  background: rgba(107, 114, 128, 0.3);
}

.product-name {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.25rem;
}

.product-sku {
  font-size: 0.875rem;
  color: #a0aec0;
}

.metric-value {
  font-weight: 500;
  color: #e2e8f0;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.trend-indicator.trend-up {
  color: #10b981;
}

.trend-indicator.trend-down {
  color: #ef4444;
}

.trend-indicator.trend-neutral {
  color: #a0aec0;
}

.traffic-sources {
  grid-column: span 1;
}

.traffic-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.traffic-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(16, 16, 24, 0.6);
  border-radius: 8px;
}

.traffic-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.traffic-icon {
  font-size: 1.5rem;
}

.traffic-name {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.25rem;
}

.traffic-visitors {
  font-size: 0.875rem;
  color: #a0aec0;
}

.traffic-metrics {
  text-align: right;
  margin-right: 1rem;
}

.traffic-percentage {
  font-weight: 500;
  color: #00d4ff;
  margin-bottom: 0.25rem;
}

.traffic-conversion {
  font-size: 0.875rem;
  color: #a0aec0;
}

.traffic-chart {
  width: 60px;
  height: 8px;
  background: rgba(107, 114, 128, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.traffic-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.analytics-alerts {
  grid-column: span 2;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.alerts-header h3 {
  color: #e2e8f0;
  font-size: 1.1rem;
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.no-alerts {
  text-align: center;
  padding: 2rem;
  color: #a0aec0;
}

.no-alerts-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
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
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid;
}

.alert-item.warning {
  background: rgba(245, 158, 11, 0.1);
  border-color: rgba(245, 158, 11, 0.3);
}

.alert-item.info {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
}

.alert-item.error {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
}

.alert-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.5rem;
}

.alert-description {
  color: #a0aec0;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.alert-actions {
  display: flex;
  gap: 0.5rem;
}

.alert-btn {
  padding: 0.5rem 1rem;
  border: 1px solid;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.alert-btn.dismiss {
  background: rgba(107, 114, 128, 0.3);
  border-color: rgba(107, 114, 128, 0.5);
  color: #a0aec0;
}

.alert-btn.action {
  background: rgba(0, 212, 255, 0.2);
  border-color: rgba(0, 212, 255, 0.5);
  color: #00d4ff;
}

.alert-btn:hover {
  filter: brightness(1.2);
}

.export-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.export-header h3 {
  color: #e2e8f0;
  font-size: 1.1rem;
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.export-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.export-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(16, 16, 24, 0.6);
  border-radius: 8px;
}

.export-title {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.25rem;
}

.export-description {
  color: #a0aec0;
  font-size: 0.875rem;
}

.export-actions {
  display: flex;
  gap: 0.5rem;
}

.export-btn {
  padding: 0.5rem 1rem;
  border: 1px solid;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.export-btn.pdf {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.export-btn.excel {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.export-btn:hover {
  filter: brightness(1.2);
  transform: translateY(-1px);
}

.date-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.date-modal {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  width: 90%;
  color: #ffffff;
}

.date-modal h3 {
  margin: 0 0 1.5rem 0;
  color: #00d4ff;
}

.date-inputs {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.date-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #e2e8f0;
  font-weight: 500;
}

.date-input {
  width: 100%;
  padding: 0.75rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  font-family: inherit;
}

.date-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 0.75rem 1rem;
  background: rgba(107, 114, 128, 0.5);
  border: 1px solid rgba(107, 114, 128, 0.7);
  border-radius: 6px;
  color: #ffffff;
  cursor: pointer;
}

.btn-apply {
  padding: 0.75rem 1rem;
  background: #00d4ff;
  border: none;
  border-radius: 6px;
  color: #000000;
  cursor: pointer;
  font-weight: 500;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .detailed-analytics {
    grid-template-columns: 1fr;
  }
  
  .analytics-alerts {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .seller-analytics-page {
    padding: 1rem;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    grid-column: span 1;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .detailed-analytics {
    grid-template-columns: 1fr;
  }
  
  .traffic-item {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .export-option {
    flex-direction: column;
    gap: 1rem;
  }
  
  .export-actions {
    justify-content: center;
  }
  
  .period-comparison {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .data-table {
    font-size: 0.875rem;
  }
  
  .product-info {
    flex-direction: column;
    text-align: center;
  }
}
</style>