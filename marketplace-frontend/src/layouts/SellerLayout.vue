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
      <!-- Top Header -->
      <header class="seller-header">
        <div class="header-left">
          <!-- Breadcrumb -->
          <nav class="breadcrumb">
            <router-link to="/" class="breadcrumb-item">🏠 Trang chủ</router-link>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item current">
              {{ getCurrentPageTitle() }}
            </span>
          </nav>
        </div>

        <div class="header-right">
          <!-- Quick Actions -->
          <div class="quick-actions">
            <button @click="refreshData" class="action-btn" title="Làm mới dữ liệu">
              <span :class="{ spinning: refreshing }">🔄</span>
            </button>

            <router-link
              to="/seller/products/create"
              class="action-btn primary"
              title="Tạo sản phẩm mới"
            >
              ➕ Tạo sản phẩm
            </router-link>
          </div>

          <!-- Notifications -->
          <div class="notifications">
            <button @click="showNotifications = !showNotifications" class="notification-btn">
              🔔
              <span v-if="notificationCount > 0" class="notification-count">
                {{ notificationCount }}
              </span>
            </button>

            <!-- Notification Dropdown -->
            <div v-if="showNotifications" class="notification-dropdown">
              <div class="notification-header">
                <h3>Thông báo</h3>
                <button @click="markAllAsRead" class="mark-read-btn">Đánh dấu đã đọc</button>
              </div>

              <div class="notification-list">
                <div v-if="notifications.length === 0" class="no-notifications">
                  <p>Không có thông báo mới</p>
                </div>

                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.read }"
                >
                  <div class="notification-icon">{{ notification.icon }}</div>
                  <div class="notification-content">
                    <h4>{{ notification.title }}</h4>
                    <p>{{ notification.message }}</p>
                    <small>{{ formatTime(notification.createdAt) }}</small>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- User Menu -->
          <div class="user-menu">
            <button @click="showUserMenu = !showUserMenu" class="user-menu-btn">
              <img
                :src="user?.avatar || '/placeholder-avatar.jpg'"
                :alt="user?.name || 'User'"
                class="user-avatar"
              />
              <span class="user-name">{{ user?.firstName || 'User' }}</span>
              <span class="dropdown-arrow">▼</span>
            </button>

            <!-- User Dropdown -->
            <div v-if="showUserMenu" class="user-dropdown">
              <router-link to="/profile" class="dropdown-item"> 👤 Hồ sơ cá nhân </router-link>
              <router-link to="/" class="dropdown-item"> 🏪 Về trang chủ </router-link>
              <div class="dropdown-divider"></div>
              <button @click="logout" class="dropdown-item logout">🚪 Đăng xuất</button>
            </div>
          </div>
        </div>
      </header>

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
const sellerStats = computed(() => sellerStore.stats || {
  totalProducts: 0,
  pendingOrders: 0,
  activeProducts: 0,
  totalRevenue: 0
})
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

/* Header Styles */
.seller-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(26, 26, 46, 0.5);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.breadcrumb-item {
  color: var(--text-secondary, #a0aec0);
  text-decoration: none;
}

.breadcrumb-item:hover {
  color: var(--text-accent, #00d4ff);
}

.breadcrumb-item.current {
  color: var(--text-primary);
  font-weight: 500;
}

.breadcrumb-separator {
  color: var(--text-secondary, #a0aec0);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-btn {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
}

.action-btn:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.action-btn.primary {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
}

.action-btn.primary:hover {
  background: #00c4ef;
}

/* Notifications */
.notifications {
  position: relative;
}

.notification-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary, #a0aec0);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.notification-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent, #00d4ff);
}

.notification-count {
  position: absolute;
  top: 0;
  right: 0;
  background: #ef4444;
  color: white;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.4rem;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

.notification-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 300px;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  margin-top: 0.5rem;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.notification-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.mark-read-btn {
  background: transparent;
  border: none;
  color: var(--text-accent, #00d4ff);
  font-size: 0.8rem;
  cursor: pointer;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  transition: all 0.3s ease;
}

.notification-item:hover {
  background: rgba(0, 212, 255, 0.05);
}

.notification-item.unread {
  background: rgba(0, 212, 255, 0.1);
}

.notification-icon {
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
}

.notification-content {
  flex: 1;
}

.notification-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.notification-content p {
  margin: 0 0 0.5rem 0;
  font-size: 0.8rem;
  color: var(--text-secondary, #a0aec0);
}

.notification-content small {
  font-size: 0.7rem;
  color: var(--text-secondary, #a0aec0);
}

/* User Menu */
.user-menu {
  position: relative;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: transparent;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-menu-btn:hover {
  background: rgba(0, 212, 255, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.user-name {
  font-size: 0.9rem;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 0.7rem;
  color: var(--text-secondary, #a0aec0);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 200px;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  margin-top: 0.5rem;
  padding: 0.5rem 0;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  color: var(--text-secondary, #a0aec0);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  text-align: left;
}

.dropdown-item:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent, #00d4ff);
}

.dropdown-item.logout {
  color: #ef4444;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.dropdown-item.logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 0.5rem 0;
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

  .seller-header {
    padding: 1rem;
  }

  .seller-content {
    padding: 1rem;
  }

  .header-right {
    gap: 0.5rem;
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
