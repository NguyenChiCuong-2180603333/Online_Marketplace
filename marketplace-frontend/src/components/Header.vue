<template>
  <header class="app-header">
    <nav class="navbar">
      <div class="container">
        <!-- Logo & Brand -->
        <div class="navbar-brand">
          <router-link to="/" class="brand-link">
            <div class="logo-container">
              <div class="logo-icon">🚀</div>
              <div class="brand-text">
                <h1>CosmicMarket</h1>
                <span class="tagline">Future Commerce</span>
              </div>
            </div>
          </router-link>
        </div>

        <!-- Search Bar -->
        <div class="search-section">
          <div class="search-container">
            <div class="search-input-wrapper">
              <input
                v-model="searchQuery"
                @keyup.enter="performSearch"
                type="text"
                placeholder="Tìm kiếm sản phẩm, thương hiệu..."
                class="search-input"
              />
              <button @click="performSearch" class="search-btn">
                🔍
              </button>
            </div>
            
            <!-- Search Suggestions -->
            <div v-if="showSuggestions && suggestions.length" class="search-suggestions">
              <div
                v-for="suggestion in suggestions"
                :key="suggestion.id"
                @click="selectSuggestion(suggestion)"
                class="suggestion-item"
              >
                <span class="suggestion-icon">{{ suggestion.type === 'product' ? '📦' : '🏷️' }}</span>
                <span class="suggestion-text">{{ suggestion.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Navigation Links -->
        <div class="navbar-nav">
          <router-link to="/categories" class="nav-link">
            <span class="nav-icon">🏪</span>
            <span class="nav-text">Danh mục</span>
          </router-link>
          
          <router-link to="/products" class="nav-link">
            <span class="nav-icon">📦</span>
            <span class="nav-text">Sản phẩm</span>
          </router-link>
          
          <div class="nav-divider"></div>
          
          <!-- User Authentication -->
          <div v-if="authStore.isAuthenticated" class="user-section">
            <!-- Cart -->
            <router-link to="/cart" class="nav-link cart-link">
              <span class="nav-icon cart-icon">🛒</span>
              <span v-if="cartStore.totalItems" class="cart-badge">{{ cartStore.totalItems }}</span>
            </router-link>
            
            <!-- Notifications -->
            <div class="notification-dropdown">
              <button @click="toggleNotifications" class="nav-link notification-btn">
                <span class="nav-icon">🔔</span>
                <span v-if="unreadNotifications" class="notification-badge">{{ unreadNotifications }}</span>
              </button>
              
              <!-- Notification Dropdown -->
              <div v-if="showNotifications" class="dropdown-menu notification-menu">
                <div class="dropdown-header">
                  <h4>Thông báo</h4>
                  <button @click="markAllAsRead" class="mark-read-btn">Đánh dấu đã đọc</button>
                </div>
                <div class="notification-list">
                  <div
                    v-for="notification in notifications"
                    :key="notification.id"
                    class="notification-item"
                    :class="{ unread: !notification.read }"
                  >
                    <div class="notification-icon">{{ notification.icon }}</div>
                    <div class="notification-content">
                      <p class="notification-message">{{ notification.message }}</p>
                      <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
                    </div>
                  </div>
                </div>
                <div class="dropdown-footer">
                  <router-link to="/notifications" class="view-all-btn">Xem tất cả</router-link>
                </div>
              </div>
            </div>
            
            <!-- User Profile Dropdown -->
            <div class="user-dropdown">
              <button @click="toggleUserMenu" class="user-btn">
                <img
                  :src="authStore.user?.avatar || '/default-avatar.jpg'"
                  :alt="authStore.user?.name"
                  class="user-avatar"
                  @error="handleAvatarError"
                />
                <span class="user-name">{{ authStore.user?.name }}</span>
                <span class="dropdown-arrow">▼</span>
              </button>
              
              <!-- User Dropdown Menu -->
              <div v-if="showUserMenu" class="dropdown-menu user-menu">
                <div class="dropdown-header">
                  <div class="user-info">
                    <img
                      :src="authStore.user?.avatar || '/default-avatar.jpg'"
                      :alt="authStore.user?.name"
                      class="user-avatar-large"
                      @error="handleAvatarError"
                    />
                    <div class="user-details">
                      <h4>{{ authStore.user?.name }}</h4>
                      <p>{{ authStore.user?.email }}</p>
                      <span v-if="authStore.user?.isVip" class="vip-badge">👑 VIP</span>
                    </div>
                  </div>
                </div>
                
                <div class="dropdown-links">
                  <router-link to="/profile" class="dropdown-link">
                    <span class="link-icon">👤</span>
                    <span>Hồ sơ cá nhân</span>
                  </router-link>
                  
                  <router-link to="/orders" class="dropdown-link">
                    <span class="link-icon">📋</span>
                    <span>Đơn hàng của tôi</span>
                  </router-link>
                  
                  <router-link to="/wishlist" class="dropdown-link">
                    <span class="link-icon">❤️</span>
                    <span>Yêu thích</span>
                  </router-link>
                  
                  <router-link to="/settings" class="dropdown-link">
                    <span class="link-icon">⚙️</span>
                    <span>Cài đặt</span>
                  </router-link>
                  
                  <!-- Admin Link (if user is admin) -->
                  <router-link
                    v-if="authStore.user?.role === 'ADMIN'"
                    to="/admin"
                    class="dropdown-link admin-link"
                  >
                    <span class="link-icon">👑</span>
                    <span>Quản trị</span>
                  </router-link>
                </div>
                
                <div class="dropdown-footer">
                  <button @click="logout" class="logout-btn">
                    <span class="link-icon">🚪</span>
                    <span>Đăng xuất</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Guest User (Not Authenticated) -->
          <div v-else class="auth-section">
            <router-link to="/login" class="nav-link auth-link">
              <span class="nav-icon">🔑</span>
              <span class="nav-text">Đăng nhập</span>
            </router-link>
            
            <router-link to="/register" class="btn btn-primary signup-btn">
              🚀 Đăng ký
            </router-link>
          </div>
        </div>

        <!-- Mobile Menu Toggle -->
        <button @click="toggleMobileMenu" class="mobile-menu-btn">
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
        </button>
      </div>
    </nav>

    <!-- Mobile Menu -->
    <div v-if="showMobileMenu" class="mobile-menu">
      <div class="mobile-search">
        <div class="search-input-wrapper">
          <input
            v-model="searchQuery"
            @keyup.enter="performSearchMobile"
            type="text"
            placeholder="Tìm kiếm..."
            class="search-input"
          />
          <button @click="performSearchMobile" class="search-btn">🔍</button>
        </div>
      </div>
      
      <div class="mobile-nav-links">
        <router-link to="/categories" class="mobile-nav-link" @click="closeMobileMenu">
          🏪 Danh mục
        </router-link>
        <router-link to="/products" class="mobile-nav-link" @click="closeMobileMenu">
          📦 Sản phẩm
        </router-link>
        
        <div v-if="authStore.isAuthenticated" class="mobile-user-section">
          <router-link to="/cart" class="mobile-nav-link" @click="closeMobileMenu">
            🛒 Giỏ hàng <span v-if="cartStore.totalItems" class="mobile-badge">{{ cartStore.totalItems }}</span>
          </router-link>
          <router-link to="/profile" class="mobile-nav-link" @click="closeMobileMenu">
            👤 Hồ sơ
          </router-link>
          <router-link to="/orders" class="mobile-nav-link" @click="closeMobileMenu">
            📋 Đơn hàng
          </router-link>
          <button @click="logout" class="mobile-nav-link logout-mobile">
            🚪 Đăng xuất
          </button>
        </div>
        
        <div v-else class="mobile-auth-section">
          <router-link to="/login" class="mobile-nav-link" @click="closeMobileMenu">
            🔑 Đăng nhập
          </router-link>
          <router-link to="/register" class="mobile-nav-link primary" @click="closeMobileMenu">
            🚀 Đăng ký
          </router-link>
        </div>
      </div>
    </div>

    <!-- Backdrop for mobile menu -->
    <div v-if="showMobileMenu" @click="closeMobileMenu" class="mobile-backdrop"></div>
  </header>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

export default {
  name: 'Header',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    
    // Reactive data
    const searchQuery = ref('')
    const showSuggestions = ref(false)
    const suggestions = ref([])
    const showNotifications = ref(false)
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const unreadNotifications = ref(3)
    
    // Sample notifications
    const notifications = ref([
      {
        id: 1,
        icon: '📦',
        message: 'Đơn hàng #12345 đã được giao thành công',
        createdAt: '2024-12-22T10:30:00',
        read: false
      },
      {
        id: 2,
        icon: '💰',
        message: 'Giảm giá 50% cho tất cả sản phẩm điện tử',
        createdAt: '2024-12-22T09:15:00',
        read: false
      },
      {
        id: 3,
        icon: '🎉',
        message: 'Chúc mừng! Bạn đã trở thành thành viên VIP',
        createdAt: '2024-12-21T14:20:00',
        read: true
      }
    ])
    
    // Methods
    const performSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({
          path: '/products',
          query: { search: searchQuery.value.trim() }
        })
        showSuggestions.value = false
      }
    }
    
    const performSearchMobile = () => {
      performSearch()
      closeMobileMenu()
    }
    
    const selectSuggestion = (suggestion) => {
      if (suggestion.type === 'product') {
        router.push(`/products/${suggestion.id}`)
      } else {
        router.push(`/categories/${suggestion.id}`)
      }
      showSuggestions.value = false
      searchQuery.value = ''
    }
    
    const toggleNotifications = () => {
      showNotifications.value = !showNotifications.value
      showUserMenu.value = false
    }
    
    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
      showNotifications.value = false
    }
    
    const toggleMobileMenu = () => {
      showMobileMenu.value = !showMobileMenu.value
    }
    
    const closeMobileMenu = () => {
      showMobileMenu.value = false
    }
    
    const markAllAsRead = () => {
      notifications.value.forEach(notification => {
        notification.read = true
      })
      unreadNotifications.value = 0
    }
    
    const formatTime = (dateString) => {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 60) return `${minutes} phút trước`
      if (hours < 24) return `${hours} giờ trước`
      return `${days} ngày trước`
    }
    
    const handleAvatarError = (event) => {
      event.target.src = '/default-avatar.jpg'
    }
    
    const logout = async () => {
      try {
        await authStore.logout()
        router.push('/')
        showUserMenu.value = false
        showMobileMenu.value = false
      } catch (error) {
        console.error('Logout error:', error)
      }
    }
    
    // Close dropdowns when clicking outside
    const handleClickOutside = (event) => {
      if (!event.target.closest('.notification-dropdown')) {
        showNotifications.value = false
      }
      if (!event.target.closest('.user-dropdown')) {
        showUserMenu.value = false
      }
      if (!event.target.closest('.search-container')) {
        showSuggestions.value = false
      }
    }
    
    // Search suggestions (mock)
    const loadSuggestions = async () => {
      if (searchQuery.value.length >= 2) {
        // Mock suggestions
        suggestions.value = [
          { id: 1, name: 'iPhone 15 Pro Max', type: 'product' },
          { id: 2, name: 'Samsung Galaxy S24', type: 'product' },
          { id: 3, name: 'Điện thoại', type: 'category' }
        ].filter(item => 
          item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
        showSuggestions.value = suggestions.value.length > 0
      } else {
        showSuggestions.value = false
      }
    }
    
    // Watchers
    watch(searchQuery, loadSuggestions)
    
    // Lifecycle
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
      // Load cart data if authenticated
      if (authStore.isAuthenticated) {
        cartStore.loadCart()
      }
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
    
    return {
      authStore,
      cartStore,
      searchQuery,
      showSuggestions,
      suggestions,
      showNotifications,
      showUserMenu,
      showMobileMenu,
      unreadNotifications,
      notifications,
      performSearch,
      performSearchMobile,
      selectSuggestion,
      toggleNotifications,
      toggleUserMenu,
      toggleMobileMenu,
      closeMobileMenu,
      markAllAsRead,
      formatTime,
      handleAvatarError,
      logout
    }
  }
}
</script>

<style scoped>
/* Header Styles */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.navbar {
  padding: 1rem 0;
}

.container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Brand Section */
.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  text-decoration: none;
  color: inherit;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  font-size: 2rem;
  filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.5));
}

.brand-text h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.tagline {
  font-size: 0.7rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Search Section */
.search-section {
  flex: 1;
  max-width: 500px;
  position: relative;
}

.search-container {
  position: relative;
}

.search-input-wrapper {
  display: flex;
  position: relative;
}

.search-input {
  flex: 1;
  padding: 0.75rem 1rem;
  padding-right: 3rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  color: var(--text-primary);
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.2);
}

.search-input::placeholder {
  color: var(--text-secondary);
}

.search-btn {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  background: var(--aurora-gradient);
  border: none;
  border-radius: 50%;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.4);
}

/* Search Suggestions */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  margin-top: 0.5rem;
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: rgba(0, 212, 255, 0.1);
}

.suggestion-icon {
  font-size: 1.1rem;
}

.suggestion-text {
  color: var(--text-primary);
  font-size: 0.9rem;
}

/* Navigation Links */
.navbar-nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-shrink: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.nav-icon {
  font-size: 1.1rem;
}

.nav-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
}

/* Cart & Notifications */
.cart-link {
  position: relative;
}

.cart-badge,
.notification-badge {
  position: absolute;
  top: -0.25rem;
  right: -0.25rem;
  background: var(--accent-gradient);
  color: white;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.4rem;
  border-radius: 10px;
  min-width: 1.2rem;
  text-align: center;
  line-height: 1;
}

.notification-dropdown,
.user-dropdown {
  position: relative;
}

.notification-btn,
.user-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.notification-btn:hover,
.user-btn:hover {
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.user-avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 0.7rem;
  transition: transform 0.3s ease;
}

/* Dropdown Menus */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 280px;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  margin-top: 0.5rem;
  z-index: 100;
  overflow: hidden;
}

.dropdown-header {
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.mark-read-btn {
  background: none;
  border: none;
  color: var(--text-accent);
  font-size: 0.8rem;
  cursor: pointer;
}

/* Notification Menu */
.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.3s ease;
}

.notification-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.notification-item.unread {
  background: rgba(0, 212, 255, 0.05);
}

.notification-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
}

.notification-message {
  margin: 0 0 0.25rem 0;
  color: var(--text-primary);
  font-size: 0.9rem;
  line-height: 1.4;
}

.notification-time {
  color: var(--text-secondary);
  font-size: 0.75rem;
}

/* User Menu */
.user-info {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.user-avatar-large {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.user-details h4 {
  margin: 0 0 0.25rem 0;
  color: var(--text-primary);
  font-size: 1rem;
}

.user-details p {
  margin: 0 0 0.25rem 0;
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.vip-badge {
  background: var(--accent-gradient);
  color: white;
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  border-radius: 8px;
  font-weight: 600;
}

.dropdown-links {
  padding: 0.5rem 0;
}

.dropdown-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.dropdown-link:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.admin-link {
  color: var(--text-warning);
}

.admin-link:hover {
  background: rgba(255, 205, 60, 0.1);
  color: var(--text-warning);
}

.dropdown-footer {
  padding: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn,
.view-all-btn {
  width: 100%;
  background: none;
  border: none;
  color: var(--text-secondary);
  text-align: left;
  padding: 0.75rem 1rem;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.view-all-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

/* Auth Section */
.auth-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.auth-link {
  color: var(--text-secondary);
}

.signup-btn {
  font-size: 0.9rem;
  padding: 0.5rem 1rem;
  white-space: nowrap;
}

/* Mobile Menu */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.hamburger-line {
  width: 1.5rem;
  height: 2px;
  background: var(--text-primary);
  transition: all 0.3s ease;
}

.mobile-menu {
  position: fixed;
  top: 100%;
  left: 0;
  right: 0;
  background: rgba(26, 26, 46, 0.98);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 212, 255, 0.3);
  z-index: 999;
  transform: translateY(-100%);
  animation: slideDown 0.3s ease forwards;
}

@keyframes slideDown {
  to {
    transform: translateY(0);
  }
}

.mobile-search {
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-nav-links {
  padding: 1rem 0;
}

.mobile-nav-link {
  display: block;
  padding: 1rem;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 1rem;
  font-weight: 500;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
  position: relative;
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.mobile-nav-link.primary {
  background: var(--aurora-gradient);
  color: white;
  font-weight: 600;
  margin: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
}

.mobile-nav-link.logout-mobile {
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  color: #ef4444;
}

.mobile-badge {
  background: var(--accent-gradient);
  color: white;
  font-size: 0.7rem;
  padding: 0.15rem 0.4rem;
  border-radius: 10px;
  margin-left: 0.5rem;
}

.mobile-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .container {
    gap: 1rem;
  }
  
  .search-section {
    max-width: 300px;
  }
  
  .nav-text {
    display: none;
  }
  
  .user-name {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-nav {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .search-section {
    max-width: 200px;
  }
  
  .brand-text h1 {
    font-size: 1.2rem;
  }
  
  .tagline {
    display: none;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 0.5rem;
    gap: 0.5rem;
  }
  
  .search-section {
    display: none;
  }
  
  .logo-container {
    gap: 0.5rem;
  }
  
  .logo-icon {
    font-size: 1.5rem;
  }
  
  .brand-text h1 {
    font-size: 1rem;
  }
}

/* Smooth Animations */
.nav-link,
.dropdown-link,
.mobile-nav-link {
  position: relative;
  overflow: hidden;
}

.nav-link::before,
.dropdown-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.nav-link:hover::before,
.dropdown-link:hover::before {
  left: 100%;
}

/* Accessibility */
.nav-link:focus,
.dropdown-link:focus,
.search-input:focus,
.mobile-nav-link:focus {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}

/* Dark mode compatibility */
@media (prefers-color-scheme: dark) {
  .app-header {
    background: rgba(16, 16, 24, 0.95);
  }
  
  .search-input {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .dropdown-menu,
  .search-suggestions,
  .mobile-menu {
    background: rgba(16, 16, 24, 0.95);
  }
}

/* High contrast mode */
@media (prefers-contrast: high) {
  .nav-link,
  .dropdown-link {
    border: 1px solid transparent;
  }
  
  .nav-link:hover,
  .dropdown-link:hover {
    border-color: var(--text-accent);
  }
}

/* Print styles */
@media print {
  .app-header {
    display: none;
  }
}