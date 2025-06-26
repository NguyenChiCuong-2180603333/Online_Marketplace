<template>
  <div class="seller-dashboard">
    <!-- Welcome Header -->
    <header class="dashboard-header">
      <div class="welcome-section">
        <h1 class="welcome-title">
          <span class="icon">👋</span>
          Xin chào, {{ getSellerName() }}!
        </h1>
        <p class="welcome-subtitle">
          Chào mừng trở lại với Seller Dashboard. Hãy cùng xem tình hình kinh doanh hôm nay.
        </p>
      </div>
      
      <div class="header-actions">
        <button @click="refreshDashboard" :disabled="loading.dashboard" class="refresh-btn">
          <span :class="{ 'spinning': loading.dashboard }">🔄</span>
          {{ loading.dashboard ? 'Đang tải...' : 'Làm mới' }}
        </button>
        
        <router-link to="/seller/products/create" class="create-product-btn">
          ➕ Tạo sản phẩm mới
        </router-link>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="loading.dashboard && !hasInitialData" class="loading-state">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu dashboard...</p>
      </div>
    </div>

    <!-- Dashboard Content -->
    <div v-else class="dashboard-content">
      <!-- Stats Overview -->
      <section class="stats-section">
        <h2 class="section-title">📊 Tổng quan</h2>
        
        <div class="stats-grid">
          <!-- Total Products -->
          <div class="stat-card products">
            <div class="stat-icon">📦</div>
            <div class="stat-info">
              <h3 class="stat-number">{{ stats.totalProducts || 0 }}</h3>
              <p class="stat-label">Tổng sản phẩm</p>
            </div>
            <div class="stat-trend">
              <span class="trend-icon">📈</span>
              <span class="trend-text">+{{ stats.newProductsThisMonth || 0 }} tháng này</span>
            </div>
          </div>
          
          <!-- Active Products -->
          <div class="stat-card active">
            <div class="stat-icon">✅</div>
            <div class="stat-info">
              <h3 class="stat-number">{{ stats.activeProducts || 0 }}</h3>
              <p class="stat-label">Đang bán</p>
            </div>
            <div class="stat-progress">
              <div class="progress-bar">
                <div 
                  class="progress-fill" 
                  :style="{ width: getActiveProductPercentage() + '%' }"
                ></div>
              </div>
              <span class="progress-text">{{ getActiveProductPercentage() }}%</span>
            </div>
          </div>
          
          <!-- Total Orders -->
          <div class="stat-card orders">
            <div class="stat-icon">📋</div>
            <div class="stat-info">
              <h3 class="stat-number">{{ stats.totalOrders || 0 }}</h3>
              <p class="stat-label">Tổng đơn hàng</p>
            </div>
            <div class="stat-breakdown">
              <div class="breakdown-item">
                <span class="breakdown-label">Chờ xử lý:</span>
                <span class="breakdown-value pending">{{ stats.pendingOrders || 0 }}</span>
              </div>
            </div>
          </div>
          
          <!-- Revenue -->
          <div class="stat-card revenue">
            <div class="stat-icon">💰</div>
            <div class="stat-info">
              <h3 class="stat-number">{{ formatMoney(stats.totalRevenue || 0) }}</h3>
              <p class="stat-label">Tổng doanh thu</p>
            </div>
            <div class="stat-breakdown">
              <div class="breakdown-item">
                <span class="breakdown-label">Tháng này:</span>
                <span class="breakdown-value this-month">{{ formatMoney(stats.thisMonthRevenue || 0) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Quick Actions -->
      <section class="quick-actions-section">
        <h2 class="section-title">🚀 Hành động nhanh</h2>
        
        <div class="actions-grid">
          <router-link to="/seller/products/create" class="action-card primary">
            <div class="action-icon">➕</div>
            <h3 class="action-title">Tạo sản phẩm mới</h3>
            <p class="action-description">Thêm sản phẩm mới vào cửa hàng của bạn</p>
            <div class="action-cta">Bắt đầu tạo →</div>
          </router-link>
          
          <router-link to="/seller/products" class="action-card">
            <div class="action-icon">📦</div>
            <h3 class="action-title">Quản lý sản phẩm</h3>
            <p class="action-description">Chỉnh sửa, cập nhật và quản lý kho hàng</p>
            <div class="action-badge" v-if="lowStockProducts.length > 0">
              {{ lowStockProducts.length }} sản phẩm sắp hết
            </div>
          </router-link>
          
          <router-link to="/seller/orders" class="action-card">
            <div class="action-icon">📋</div>
            <h3 class="action-title">Xử lý đơn hàng</h3>
            <p class="action-description">Cập nhật trạng thái và giao hàng</p>
            <div class="action-badge urgent" v-if="stats.pendingOrders > 0">
              {{ stats.pendingOrders }} đơn chờ xử lý
            </div>
          </router-link>
          
          <router-link to="/seller/analytics" class="action-card">
            <div class="action-icon">📈</div>
            <h3 class="action-title">Xem thống kê</h3>
            <p class="action-description">Phân tích hiệu suất và xu hướng bán hàng</p>
            <div class="action-trend">
              <span class="trend-up">📊 Doanh thu tăng</span>
            </div>
          </router-link>
        </div>
      </section>

      <!-- Recent Orders -->
      <section class="recent-orders-section" v-if="recentOrders.length > 0">
        <div class="section-header">
          <h2 class="section-title">📋 Đơn hàng gần đây</h2>
          <router-link to="/seller/orders" class="view-all-link">
            Xem tất cả →
          </router-link>
        </div>
        
        <div class="orders-list">
          <div 
            v-for="order in recentOrders.slice(0, 5)" 
            :key="order.id" 
            class="order-item"
          >
            <div class="order-info">
              <h4 class="order-id">#{{ order.id.slice(-8) }}</h4>
              <p class="order-customer">{{ order.customerName || 'Khách hàng' }}</p>
              <p class="order-date">{{ formatDate(order.createdAt) }}</p>
            </div>
            
            <div class="order-products">
              <div class="product-summary">
                <span class="product-count">{{ order.items?.length || 1 }} sản phẩm</span>
              </div>
            </div>
            
            <div class="order-status">
              <span :class="['status-badge', getStatusClass(order.status)]">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            
            <div class="order-amount">
              <span class="amount">{{ formatMoney(order.totalAmount) }}</span>
            </div>
            
            <div class="order-actions">
              <router-link 
                :to="`/seller/orders/${order.id}`" 
                class="action-btn view"
                title="Xem chi tiết"
              >
                👁️
              </router-link>
              
              <button 
                v-if="order.status === 'PENDING'"
                @click="quickUpdateStatus(order.id, 'PROCESSING')"
                class="action-btn process"
                title="Xử lý đơn hàng"
              >
                ⚙️
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Performance Insights -->
      <section class="insights-section">
        <h2 class="section-title">💡 Thông tin hữu ích</h2>
        
        <div class="insights-grid">
          <!-- Best Selling Products -->
          <div class="insight-card">
            <h3 class="insight-title">🏆 Sản phẩm bán chạy</h3>
            <div class="insight-content">
              <div v-if="bestSellingProducts.length === 0" class="no-data">
                <p>Chưa có dữ liệu bán hàng</p>
              </div>
              <div v-else class="product-list">
                <div 
                  v-for="product in bestSellingProducts.slice(0, 3)" 
                  :key="product.id"
                  class="product-item"
                >
                  <img 
                    :src="product.images?.[0] || '/placeholder-product.jpg'" 
                    :alt="product.name"
                    class="product-image"
                  />
                  <div class="product-info">
                    <h4>{{ product.name }}</h4>
                    <p>{{ product.reviewCount || 0 }} đánh giá</p>
                  </div>
                  <div class="product-rating">
                    ⭐ {{ product.averageRating || 0 }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Low Stock Alert -->
          <div class="insight-card alert" v-if="lowStockProducts.length > 0">
            <h3 class="insight-title">⚠️ Cảnh báo tồn kho</h3>
            <div class="insight-content">
              <p class="alert-message">
                {{ lowStockProducts.length }} sản phẩm sắp hết hàng
              </p>
              <div class="low-stock-list">
                <div 
                  v-for="product in lowStockProducts.slice(0, 3)" 
                  :key="product.id"
                  class="stock-item"
                >
                  <span class="product-name">{{ product.name }}</span>
                  <span class="stock-count">{{ product.stockQuantity }} còn lại</span>
                </div>
              </div>
              <router-link to="/seller/products?filter=low-stock" class="alert-action">
                Xem tất cả →
              </router-link>
            </div>
          </div>
          
          <!-- Tips & Recommendations -->
          <div class="insight-card tips">
            <h3 class="insight-title">💭 Gợi ý cải thiện</h3>
            <div class="insight-content">
              <div class="tips-list">
                <div class="tip-item" v-if="stats.activeProducts === 0">
                  <span class="tip-icon">📦</span>
                  <span class="tip-text">Hãy tạo sản phẩm đầu tiên để bắt đầu bán hàng</span>
                </div>
                
                <div class="tip-item" v-if="lowStockProducts.length > 0">
                  <span class="tip-icon">📈</span>
                  <span class="tip-text">Nhập thêm hàng cho sản phẩm sắp hết để không bỏ lỡ đơn hàng</span>
                </div>
                
                <div class="tip-item" v-if="stats.activeProducts > 0 && stats.totalOrders === 0">
                  <span class="tip-icon">🎯</span>
                  <span class="tip-text">Tối ưu hóa tiêu đề và hình ảnh sản phẩm để thu hút khách hàng</span>
                </div>
                
                <div class="tip-item" v-if="stats.pendingOrders > 5">
                  <span class="tip-icon">⚡</span>
                  <span class="tip-text">Xử lý nhanh các đơn hàng chờ để tăng độ hài lòng khách hàng</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSellerStore } from '@/stores/seller'

const router = useRouter()
const authStore = useAuthStore()
const sellerStore = useSellerStore()

// Local state
const hasInitialData = ref(false)

// Computed properties
const user = computed(() => authStore.user)
const stats = computed(() => sellerStore.stats)
const loading = computed(() => sellerStore.loading)
const recentOrders = computed(() => sellerStore.recentOrders)
const lowStockProducts = computed(() => sellerStore.lowStockProducts)

const bestSellingProducts = computed(() => {
  return sellerStore.activeProducts
    .filter(p => p.reviewCount > 0)
    .sort((a, b) => (b.reviewCount || 0) - (a.reviewCount || 0))
    .slice(0, 3)
})

// Methods
const getSellerName = () => {
  const user = authStore.user
  if (user?.firstName && user?.lastName) {
    return `${user.firstName} ${user.lastName}`
  }
  return user?.firstName || user?.email?.split('@')[0] || 'Seller'
}

const getActiveProductPercentage = () => {
  if (stats.value.totalProducts === 0) return 0
  return Math.round((stats.value.activeProducts / stats.value.totalProducts) * 100)
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount || 0)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 1) return 'Hôm qua'
  if (diffDays <= 7) return `${diffDays} ngày trước`
  
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const getStatusClass = (status) => {
  const statusClasses = {
    'PENDING': 'pending',
    'PROCESSING': 'processing',
    'SHIPPED': 'shipped',
    'DELIVERED': 'delivered',
    'CANCELLED': 'cancelled'
  }
  return statusClasses[status] || 'pending'
}

const getStatusText = (status) => {
  const statusTexts = {
    'PENDING': '🕐 Chờ xử lý',
    'PROCESSING': '⚙️ Đang xử lý',
    'SHIPPED': '🚚 Đã giao vận',
    'DELIVERED': '✅ Đã giao',
    'CANCELLED': '❌ Đã hủy'
  }
  return statusTexts[status] || status
}

const refreshDashboard = async () => {
  try {
    await sellerStore.loadDashboardStats()
  } catch (error) {
    console.error('Refresh dashboard error:', error)
  }
}

const quickUpdateStatus = async (orderId, newStatus) => {
  try {
    await sellerStore.updateOrderStatus(orderId, newStatus)
  } catch (error) {
    console.error('Quick update status error:', error)
    alert('Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  }
}

// Lifecycle
onMounted(async () => {
  try {
    await sellerStore.loadDashboardStats()
    hasInitialData.value = true
  } catch (error) {
    console.error('Load dashboard error:', error)
    hasInitialData.value = true // Show UI even if error
  }
})
</script>

<style scoped>
.seller-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* Header Styles */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.welcome-section {
  flex: 1;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.welcome-title .icon {
  font-size: 2.5rem;
}

.welcome-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary, #a0aec0);
  line-height: 1.5;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.refresh-btn, .create-product-btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.9rem;
}

.refresh-btn {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.create-product-btn {
  background: var(--text-accent, #00d4ff);
  border: none;
  color: #1a1a2e;
  font-weight: 700;
}

.create-product-btn:hover {
  background: #00c4ef;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.3);
}

/* Loading State */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-spinner {
  text-align: center;
  color: var(--text-primary, #ffffff);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top: 3px solid var(--text-accent, #00d4ff);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

/* Dashboard Content */
.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* Section Styles */
.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Stats Section */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  padding: 2rem;
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
  background: var(--text-accent, #00d4ff);
  opacity: 0.5;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent, #00d4ff);
}

.stat-card.products::before { background: #667eea; }
.stat-card.active::before { background: #10b981; }
.stat-card.orders::before { background: #f59e0b; }
.stat-card.revenue::before { background: #ef4444; }

.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 1rem;
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
}

.stat-trend, .stat-breakdown {
  margin-top: 1rem;
  font-size: 0.85rem;
}

.trend-icon {
  margin-right: 0.5rem;
}

.trend-text {
  color: #10b981;
}

.stat-progress {
  margin-top: 1rem;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(0, 212, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: var(--text-accent, #00d4ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.breakdown-label {
  color: var(--text-secondary, #a0aec0);
}

.breakdown-value {
  font-weight: 600;
}

.breakdown-value.pending {
  color: #f59e0b;
}

.breakdown-value.this-month {
  color: #10b981;
}

/* Quick Actions Section */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  padding: 2rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  position: relative;
  display: block;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent, #00d4ff);
}

.action-card.primary {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border-color: var(--text-accent, #00d4ff);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.action-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.action-description {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 1rem;
  line-height: 1.5;
}

.action-cta {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
  font-size: 0.9rem;
}

.action-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.action-badge.urgent {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  animation: pulse 2s infinite;
}

.action-trend {
  margin-top: 1rem;
}

.trend-up {
  color: #10b981;
  font-size: 0.85rem;
  font-weight: 500;
}

/* Recent Orders Section */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.view-all-link {
  color: var(--text-accent, #00d4ff);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-all-link:hover {
  color: #00c4ef;
  transform: translateX(5px);
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr auto;
  gap: 1rem;
  align-items: center;
  transition: all 0.3s ease;
}

.order-item:hover {
  border-color: var(--text-accent, #00d4ff);
  transform: translateX(5px);
}

.order-id {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.25rem;
}

.order-customer, .order-date {
  font-size: 0.9rem;
  color: var(--text-secondary, #a0aec0);
  margin: 0;
}

.product-count {
  font-size: 0.9rem;
  color: var(--text-secondary, #a0aec0);
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
}

.status-badge.pending {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.status-badge.processing {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.status-badge.shipped {
  background: rgba(139, 92, 246, 0.2);
  color: #8b5cf6;
}

.status-badge.delivered {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.amount {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-accent, #00d4ff);
}

.order-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  font-size: 0.9rem;
}

.action-btn:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: scale(1.1);
}

/* Performance Insights Section */
.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
}

.insight-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  padding: 2rem;
  transition: all 0.3s ease;
}

.insight-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 212, 255, 0.1);
}

.insight-card.alert {
  border-color: rgba(245, 158, 11, 0.5);
  background: rgba(245, 158, 11, 0.05);
}

.insight-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.no-data {
  text-align: center;
  color: var(--text-secondary, #a0aec0);
  padding: 2rem;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 0.25rem 0;
  color: var(--text-primary, #ffffff);
  font-size: 0.9rem;
}

.product-info p {
  margin: 0;
  color: var(--text-secondary, #a0aec0);
  font-size: 0.8rem;
}

.product-rating {
  color: #fbbf24;
  font-size: 0.9rem;
  font-weight: 600;
}

.alert-message {
  color: #f59e0b;
  font-weight: 600;
  margin-bottom: 1rem;
}

.low-stock-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.stock-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
}

.product-name {
  color: var(--text-primary, #ffffff);
  font-size: 0.9rem;
}

.stock-count {
  color: #f59e0b;
  font-weight: 600;
  font-size: 0.9rem;
}

.alert-action {
  color: #f59e0b;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.alert-action:hover {
  color: #fbbf24;
  transform: translateX(5px);
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.tip-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.tip-text {
  color: var(--text-secondary, #a0aec0);
  line-height: 1.5;
  font-size: 0.9rem;
}

/* Animations */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.spinning {
  animation: spin 1s linear infinite;
}

/* Responsive Design */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .welcome-title {
    font-size: 1.5rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .insights-grid {
    grid-template-columns: 1fr;
  }

  .order-item {
    grid-template-columns: 1fr;
    gap: 1rem;
    text-align: left;
  }

  .order-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .seller-dashboard {
    padding: 0;
  }

  .section-title {
    font-size: 1.2rem;
  }

  .stat-card, .action-card, .insight-card {
    padding: 1.5rem;
  }

  .order-item {
    padding: 1rem;
  }
}
</style>

<style scoped>
/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.header-info h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.header-info p {
  color: var(--text-secondary, #a0aec0);
  font-size: 1rem;
}

.btn-create, .btn-create-first {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-create:hover, .btn-create-first:hover {
  background: #00c4ef;
  transform: translateY(-2px);
}

/* Loading */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--text-secondary, #a0aec0);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top: 3px solid var(--text-accent, #00d4ff);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--text-secondary, #a0aec0);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.product-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent, #00d4ff);
}

.product-image {
  position: relative;
  margin-bottom: 1rem;
}

.product-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.product-status {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.product-info h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.product-price {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.product-stock {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit, .btn-toggle {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-edit {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  text-decoration: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.btn-toggle {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

/* Form Placeholders */
.form-placeholder {
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.form-section h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.fields-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.field-placeholder {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-placeholder label {
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
}

.input-placeholder, .select-placeholder {
  height: 40px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
}

.textarea-placeholder {
  height: 100px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
}

.upload-placeholder {
  text-align: center;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 3rem 2rem;
  background: rgba(0, 0, 0, 0.1);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

/* Orders */
.filter-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.filter-tab {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: var(--text-secondary, #a0aec0);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-tab:hover {
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
}

.count {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  margin-left: 0.5rem;
}

.orders-preview {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.order-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-header h4 {
  color: var(--text-primary, #ffffff);
  margin: 0;
}

.status-badge {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

/* Analytics */
.stats-preview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
}

.stat-card h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.chart-placeholder {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  font-size: 2rem;
}

/* Settings */
.settings-sections {
  max-width: 800px;
  margin: 0 auto 2rem;
}

.settings-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.settings-section h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
}

.settings-placeholder {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-item label {
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
}

/* Coming Soon Notice */
.coming-soon-notice {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  margin-top: 2rem;
}

.coming-soon-notice.main {
  margin-top: 0;
}

.notice-content h3 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 1rem;
}

.features-list, .features-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1.5rem;
  text-align: left;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.feature-item:last-child {
  border-bottom: none;
}

/* Animation */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .fields-row {
    grid-template-columns: 1fr;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }

  .stats-preview {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>