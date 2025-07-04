<template>
  <div class="admin-layout">
    <!-- Admin Sidebar -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
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
            <router-link to="/admin/notifications" class="nav-link">
              <span class="nav-icon">🔔</span>
              <span class="nav-text" v-if="!sidebarCollapsed">Thông báo</span>
            </router-link>
          </li>

          <li class="nav-divider" v-if="!sidebarCollapsed"></li>
        </ul>
      </nav>

      <!-- Sidebar Toggle -->
      <div class="sidebar-toggle" @click="toggleSidebar">
        <span class="toggle-icon">{{ sidebarCollapsed ? '→' : '←' }}</span>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="admin-main">
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
    read: false,
  },
  {
    id: 2,
    icon: '⚠️',
    title: 'Cảnh báo hệ thống',
    message: 'Một số sản phẩm sắp hết hàng',
    createdAt: new Date(Date.now() - 1000 * 60 * 30),
    read: false,
  },
])

// Computed
const user = computed(() => authStore.user)
const loading = computed(() => adminStore.loading)
const stats = computed(() => {
  const dashboardStats = adminStore.dashboardStats
  return (
    dashboardStats || {
      totalUsers: 0,
      totalProducts: 0,
      pendingOrders: 0,
      totalOrders: 0,
      totalRevenue: 0,
    }
  )
})

const notificationCount = computed(() => notifications.value.filter((n) => !n.read).length)

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
    AdminDashboard: '📊 Dashboard',
    AdminUsers: '👥 Quản lý người dùng',
    AdminProducts: '📦 Quản lý sản phẩm',
    AdminOrders: '📋 Quản lý đơn hàng',
    AdminCategories: '🏷️ Quản lý danh mục',
    AdminAnalytics: '📈 Analytics',
    AdminNotifications: '🔔 Quản lý thông báo',
    AdminSettings: '⚙️ Cài đặt hệ thống',
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

/* Page Content */
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

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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

  .admin-content {
    padding: 1rem;
  }
}
</style>
