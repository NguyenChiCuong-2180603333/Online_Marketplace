<template>
  <div class="seller-layout">
    <!-- Seller Sidebar -->
    <aside class="seller-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Seller Profile Section -->
      <div class="seller-profile">
        <div class="profile-avatar">
          <img
            :src="user?.avatar || '/placeholder-avatar.jpg'"
            :alt="user?.name || 'Seller Avatar'"
            class="avatar-image"
          />
          <div class="status-indicator online"></div>
        </div>

        <div class="profile-info" v-if="!sidebarCollapsed">
          <h3 class="seller-name">{{ getSellerName() }}</h3>
          <p class="seller-stats">
            <span class="stat-item"> 📦 {{ sellerStats?.totalProducts || 0 }} sản phẩm </span>
          </p>
          <div class="seller-badge">
            <span class="badge-icon">⭐</span>
            <span class="badge-text">Seller</span>
          </div>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="seller-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/seller/dashboard" class="nav-link">
              <span class="nav-icon">📊</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Dashboard</span>
            </router-link>
          </li>

          <li class="nav-item">
            <router-link to="/seller/products" class="nav-link">
              <span class="nav-icon">📦</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Sản phẩm của tôi</span>
              <span class="nav-badge" v-if="sellerStats?.totalProducts > 0">
                {{ sellerStats.totalProducts }}
              </span>
            </router-link>
          </li>

          <!--
          <li class="nav-item">
            <router-link to="/seller/products/create" class="nav-link">
              <span class="nav-icon">➕</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Tạo sản phẩm</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/seller/orders" class="nav-link">
              <span class="nav-icon">📋</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Đơn hàng</span>
              <span class="nav-badge urgent" v-if="sellerStats?.pendingOrders > 0">
                {{ sellerStats.pendingOrders }}
              </span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/seller/analytics" class="nav-link">
              <span class="nav-icon">📈</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Thống kê</span>
            </router-link>
          </li>
          <li class="nav-divider" v-if="!sidebarCollapsed"></li>
          <li class="nav-item">
            <router-link to="/seller/settings" class="nav-link">
              <span class="nav-icon">⚙️</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Cài đặt</span>
            </router-link>
          </li>
          -->
        </ul>
      </nav>

      <!-- Quick Stats (when collapsed) -->
      <div class="quick-stats" v-if="sidebarCollapsed">
        <div class="quick-stat">
          <span class="stat-number">{{ sellerStats?.totalProducts || 0 }}</span>
          <span class="stat-label">📦</span>
        </div>
        <div class="quick-stat">
          <span class="stat-number">{{ sellerStats?.pendingOrders || 0 }}</span>
          <span class="stat-label">📋</span>
        </div>
      </div>

      <!-- Collapse Toggle -->
      <button
        @click="toggleSidebar"
        class="sidebar-toggle"
        :title="sidebarCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'"
      >
        <span v-if="sidebarCollapsed">👉</span>
        <span v-else>👈</span>
      </button>
    </aside>

    <!-- Main Content Area -->
    <main class="seller-main">
      <!-- Page Content -->
      <div class="seller-content">
        <router-view />
      </div>
    </main>

    <!-- Loading Overlay -->
    <div v-if="loading.dashboard" class="loading-overlay">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSellerStore } from '@/stores/seller'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const sellerStore = useSellerStore()

// Local state
const sidebarCollapsed = ref(false)
const showNotifications = ref(false)
const showUserMenu = ref(false)
const refreshing = ref(false)

// Mock notifications - replace with real data
const notifications = ref([
  {
    id: 1,
    icon: '📦',
    title: 'Đơn hàng mới',
    message: 'Bạn có 1 đơn hàng mới cần xử lý',
    createdAt: new Date(),
    read: false,
  },
  {
    id: 2,
    icon: '⚠️',
    title: 'Sản phẩm sắp hết hàng',
    message: 'iPhone 15 Pro chỉ còn 2 sản phẩm trong kho',
    createdAt: new Date(Date.now() - 1000 * 60 * 30),
    read: false,
  },
])

// Computed
const user = computed(() => authStore.user)
const sellerStats = computed(
  () =>
    sellerStore.stats || {
      totalProducts: 0,
      pendingOrders: 0,
      activeProducts: 0,
      totalRevenue: 0,
    }
)
const cartItemsCount = computed(() => sellerStore.cartItemsCount)
const loading = computed(() => sellerStore.loading)

const notificationCount = computed(() => notifications.value.filter((n) => !n.read).length)

// Methods
const getSellerName = () => {
  const user = authStore.user
  if (user?.firstName && user?.lastName) {
    return `${user.firstName} ${user.lastName}`
  }
  return user?.firstName || user?.email || 'Seller'
}

const getCurrentPageTitle = () => {
  const titles = {
    SellerDashboard: '📊 Dashboard',
    SellerProducts: '📦 Sản phẩm của tôi',
    CreateProduct: '➕ Tạo sản phẩm mới',
    EditProduct: '✏️ Chỉnh sửa sản phẩm',
    SellerOrders: '📋 Quản lý đơn hàng',
    SellerAnalytics: '📈 Thống kê',
    SellerSettings: '⚙️ Cài đặt',
  }
  return titles[route.name] || 'Seller Dashboard'
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('seller_sidebar_collapsed', sidebarCollapsed.value.toString())
}

const refreshData = async () => {
  refreshing.value = true
  try {
    await sellerStore.loadDashboardStats()
  } catch (error) {
    console.error('Refresh data error:', error)
  } finally {
    refreshing.value = false
  }
}

const markAllAsRead = () => {
  notifications.value.forEach((n) => (n.read = true))
}

const logout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

const formatTime = (date) => {
  const now = new Date()
  const diff = now - new Date(date)
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  if (minutes < 1440) return `${Math.floor(minutes / 60)} giờ trước`
  return `${Math.floor(minutes / 1440)} ngày trước`
}

// Close dropdowns when clicking outside
const handleClickOutside = (event) => {
  if (!event.target.closest('.notifications')) {
    showNotifications.value = false
  }
  if (!event.target.closest('.user-menu')) {
    showUserMenu.value = false
  }
}

// Lifecycle
onMounted(async () => {
  // Restore sidebar state
  const savedState = localStorage.getItem('seller_sidebar_collapsed')
  if (savedState !== null) {
    sidebarCollapsed.value = savedState === 'true'
  }

  // Load initial data
  await sellerStore.loadDashboardStats()

  // Add event listeners
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.seller-layout {
  display: flex;
  min-height: 100vh;
  background: var(--background-primary, linear-gradient(135deg, #1a1a2e 0%, #16213e 100%));
  color: var(--text-primary, #ffffff);
}

/* Sidebar Styles */
.seller-sidebar {
  width: 280px;
  background: rgba(26, 26, 46, 0.9);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(0, 212, 255, 0.2);
  transition: all 0.3s ease;
  position: relative;
  flex-shrink: 0;
}

.seller-sidebar.collapsed {
  width: 80px;
}

.seller-profile {
  padding: 2rem 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  text-align: center;
}

.profile-avatar {
  position: relative;
  display: inline-block;
  margin-bottom: 1rem;
}

.avatar-image {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(0, 212, 255, 0.5);
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #1a1a2e;
}

.status-indicator.online {
  background: #10b981;
}

.seller-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.seller-stats {
  font-size: 0.9rem;
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 1rem;
}

.seller-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

/* Navigation Styles */
.seller-nav {
  padding: 1rem 0;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin: 0.25rem 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  color: var(--text-secondary, #a0aec0);
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent, #00d4ff);
}

.nav-link.router-link-active {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  border-right: 3px solid var(--text-accent, #00d4ff);
}

.nav-icon {
  font-size: 1.2rem;
  width: 20px;
  text-align: center;
}

.nav-text {
  flex: 1;
  font-weight: 500;
}

.nav-badge {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.nav-badge.urgent {
  background: #ef4444;
  color: white;
  animation: pulse 2s infinite;
}

.nav-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 1rem 1.5rem;
}

/* Quick Stats */
.quick-stats {
  padding: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.quick-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1rem;
}

.stat-number {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-accent, #00d4ff);
}

.stat-label {
  font-size: 0.8rem;
  color: var(--text-secondary, #a0aec0);
}

/* Sidebar Toggle */
.sidebar-toggle {
  position: absolute;
  top: 50%;
  right: -15px;
  transform: translateY(-50%);
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.5);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  cursor: pointer;
  font-size: 0.8rem;
  color: var(--text-accent, #00d4ff);
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

/* Main Content */
.seller-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Content Area */
.seller-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.02);
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  text-align: center;
  color: var(--text-primary);
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

/* Animations */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.spinning {
  animation: spin 1s linear infinite;
}

/* Responsive */
@media (max-width: 768px) {
  .seller-layout {
    flex-direction: column;
  }

  .seller-sidebar {
    width: 100%;
    height: auto;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    transform: translateY(-100%);
    transition: transform 0.3s ease;
  }

  .seller-sidebar.open {
    transform: translateY(0);
  }

  .seller-main {
    margin-top: 0;
  }

  .seller-content {
    padding: 1rem;
  }

  .user-name {
    display: none;
  }
}

/* No notifications state */
.no-notifications {
  padding: 2rem 1rem;
  text-align: center;
  color: var(--text-secondary, #a0aec0);
}

.no-notifications p {
  margin: 0;
  font-size: 0.9rem;
}
</style>
