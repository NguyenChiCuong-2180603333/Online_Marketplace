<template>
  <div class="admin-layout">
    <!-- Admin Sidebar -->
    <aside class="admin-sidebar" :class="{ 'collapsed': sidebarCollapsed }">
      <!-- Admin Profile Section -->
      <div class="admin-profile">
        <div class="profile-avatar">
          <img 
            :src="user?.avatar || '/placeholder-avatar.jpg'" 
            :alt="user?.name || 'Admin Avatar'" 
            class="avatar-image"
          />
          <div class="status-indicator admin"></div>
        </div>
        
        <div class="profile-info" v-if="!sidebarCollapsed">
          <h3 class="admin-name">{{ getAdminName() }}</h3>
          <p class="admin-role">Administrator</p>
          <div class="admin-badge">
            <span class="badge-icon">👑</span>
            <span class="badge-text">Admin</span>
          </div>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="admin-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/admin/dashboard" class="nav-link">
              <span class="nav-icon">📊</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Dashboard</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/users" class="nav-link">
              <span class="nav-icon">👥</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Người dùng</span>
              <span class="nav-badge" v-if="stats.totalUsers > 0">
                {{ stats.totalUsers }}
              </span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/products" class="nav-link">
              <span class="nav-icon">📦</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Sản phẩm</span>
              <span class="nav-badge" v-if="stats.totalProducts > 0">
                {{ stats.totalProducts }}
              </span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/orders" class="nav-link">
              <span class="nav-icon">📋</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Đơn hàng</span>
              <span class="nav-badge urgent" v-if="stats.pendingOrders > 0">
                {{ stats.pendingOrders }}
              </span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/categories" class="nav-link">
              <span class="nav-icon">🏷️</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Danh mục</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/analytics" class="nav-link">
              <span class="nav-icon">📈</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Analytics</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/admin/notifications" class="nav-link">
              <span class="nav-icon">🔔</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Thông báo</span>
            </router-link>
          </li>
          
          <li class="nav-divider" v-if="!sidebarCollapsed"></li>
          
          <li class="nav-item">
            <router-link to="/admin/settings" class="nav-link">
              <span class="nav-icon">⚙️</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Cài đặt</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- Sidebar Toggle -->
      <div class="sidebar-toggle" @click="toggleSidebar">
        <span class="toggle-icon">{{ sidebarCollapsed ? '→' : '←' }}</span>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="admin-main">
      <!-- Header -->
      <header class="admin-header">
        <div class="header-left">
          <h1 class="page-title">{{ getCurrentPageTitle() }}</h1>
        </div>
        
        <div class="header-right">
          <!-- Quick Actions -->
          <div class="quick-actions">
            <button @click="refreshData" class="action-btn" title="Làm mới dữ liệu">
              <span :class="{ 'spinning': refreshing }">🔄</span>
            </button>
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
                  :class="{ 'unread': !notification.read }"
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
              <span class="user-name">{{ user?.firstName || 'Admin' }}</span>
              <span class="dropdown-arrow">▼</span>
            </button>
            
            <!-- User Dropdown -->
            <div v-if="showUserMenu" class="user-dropdown">
              <router-link to="/profile" class="dropdown-item">
                👤 Hồ sơ cá nhân
              </router-link>
              <router-link to="/" class="dropdown-item">
                🏪 Về trang chủ
              </router-link>
              <div class="dropdown-divider"></div>
              <button @click="logout" class="dropdown-item logout">
                🚪 Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="admin-content">
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
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const adminStore = useAdminStore()

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
    title: 'Sản phẩm mới',
    message: 'Có 5 sản phẩm mới được thêm vào hệ thống',
    createdAt: new Date(),
    read: false
  },
  {
    id: 2,
    icon: '⚠️',
    title: 'Cảnh báo hệ thống',
    message: 'Một số sản phẩm sắp hết hàng',
    createdAt: new Date(Date.now() - 1000 * 60 * 30),
    read: false
  }
])

// Computed
const user = computed(() => authStore.user)
const stats = computed(() => adminStore.dashboardStats)
const loading = computed(() => adminStore.loading)

const notificationCount = computed(() => 
  notifications.value.filter(n => !n.read).length
)

// Methods
const getAdminName = () => {
  const user = authStore.user
  if (user?.firstName && user?.lastName) {
    return `${user.firstName} ${user.lastName}`
  }
  return user?.firstName || user?.email || 'Administrator'
}

const getCurrentPageTitle = () => {
  const titles = {
    'AdminDashboard': '📊 Dashboard',
    'AdminUsers': '👥 Quản lý người dùng',
    'AdminProducts': '📦 Quản lý sản phẩm',
    'AdminOrders': '📋 Quản lý đơn hàng',
    'AdminCategories': '🏷️ Quản lý danh mục',
    'AdminAnalytics': '📈 Analytics',
    'AdminNotifications': '🔔 Quản lý thông báo',
    'AdminSettings': '⚙️ Cài đặt hệ thống'
  }
  return titles[route.name] || 'Admin Dashboard'
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('admin_sidebar_collapsed', sidebarCollapsed.value.toString())
}

const refreshData = async () => {
  refreshing.value = true
  try {
    await adminStore.loadDashboard()
  } catch (error) {
    console.error('Refresh data error:', error)
  } finally {
    refreshing.value = false
  }
}

const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
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
  const savedState = localStorage.getItem('admin_sidebar_collapsed')
  if (savedState !== null) {
    sidebarCollapsed.value = savedState === 'true'
  }

  // Load initial data
  await adminStore.loadDashboard()

  // Add event listeners
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--background-primary, linear-gradient(135deg, #1a1a2e 0%, #16213e 100%));
  color: var(--text-primary, #ffffff);
}

/* Sidebar Styles */
.admin-sidebar {
  width: 280px;
  background: rgba(26, 26, 46, 0.9);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(0, 212, 255, 0.2);
  transition: all 0.3s ease;
  position: relative;
  flex-shrink: 0;
}

.admin-sidebar.collapsed {
  width: 80px;
}

.admin-profile {
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
  border: 3px solid rgba(0, 212, 255, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #1a1a2e;
}

.status-indicator.admin {
  background: #ffd700;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.6);
}

.admin-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.admin-role {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.admin-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
}

/* Navigation */
.admin-nav {
  padding: 1rem 0;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 0.25rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.nav-link.router-link-active {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
  border-right: 3px solid var(--text-accent);
}

.nav-icon {
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
}

.nav-text {
  font-weight: 500;
}

.nav-badge {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
  font-size: 0.7rem;
  padding: 0.2rem 0.4rem;
  border-radius: 10px;
  margin-left: auto;
}

.nav-badge.urgent {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.nav-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 1rem 1.5rem;
}

/* Sidebar Toggle */
.sidebar-toggle {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateX(-50%) scale(1.1);
}

.toggle-icon {
  font-size: 0.8rem;
  font-weight: bold;
}

/* Main Content */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Header */
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent);
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.action-btn:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Notifications */
.notifications {
  position: relative;
}

.notification-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.notification-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
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
  color: var(--text-accent);
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
  color: var(--text-secondary);
}

.notification-content small {
  font-size: 0.7rem;
  color: var(--text-secondary);
}

.no-notifications {
  padding: 2rem;
  text-align: center;
  color: var(--text-secondary);
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
  color: var(--text-secondary);
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
  color: var(--text-primary);
  text-decoration: none;
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.3s ease;
  font-size: 0.9rem;
}

.dropdown-item:hover {
  background: rgba(0, 212, 255, 0.1);
}

.dropdown-item.logout {
  color: #ef4444;
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 0.5rem 0;
}

/* Content Area */
.admin-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
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
  align-items: center;
  justify-content: center;
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
  border-top: 3px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: -280px;
    z-index: 1000;
    height: 100vh;
  }
  
  .admin-sidebar.collapsed {
    left: -80px;
  }
  
  .admin-sidebar.show {
    left: 0;
  }
  
  .admin-header {
    padding: 1rem;
  }
  
  .admin-content {
    padding: 1rem;
  }
  
  .header-right {
    gap: 0.5rem;
  }
  
  .page-title {
    font-size: 1.2rem;
  }
}
</style>