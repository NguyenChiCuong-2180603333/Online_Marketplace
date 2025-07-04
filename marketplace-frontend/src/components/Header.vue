<template>
  <header class="app-header">
    <nav class="navbar">
      <div class="container header-flex2">
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

        <!-- Navigation + User -->
        <div class="header-right2">
          <div class="navbar-nav">
            <router-link to="/categories" class="nav-link">
              <span class="nav-icon">🏪</span>
              <span class="nav-text">Danh mục</span>
            </router-link>
            <router-link to="/products" class="nav-link">
              <span class="nav-icon">📦</span>
              <span class="nav-text">Sản phẩm</span>
            </router-link>
          </div>
          <div v-if="authStore.isAuthenticated" class="user-bar">
            <router-link to="/cart" class="nav-link cart-link">
              <span class="nav-icon cart-icon">🛒</span>
              <span v-if="cartStore.totalItems" class="cart-badge">{{ cartStore.totalItems }}</span>
            </router-link>
            <div class="notify-group">
              <router-link to="/notifications" class="nav-link notification-link" title="Thông báo">
                <span class="icon-badge-wrapper">
                  <span class="nav-icon">🔔</span>
                  <span v-if="unreadNotifications" class="notification-badge">{{
                    unreadNotifications
                  }}</span>
                </span>
              </router-link>
            </div>
            <div class="reward-group">
              <span class="star-icon">★</span>
              <span class="reward-points">{{ loyaltyStore.userPoints.current }}</span>
              <span class="tier-badge2" :style="{ background: loyaltyStore.tierColor }">
                {{ loyaltyStore.userPoints.tier }}
              </span>
            </div>
            <div class="user-dropdown user-info-group">
              <button @click="toggleUserMenu" class="user-btn">
                <img
                  :src="authStore.user?.avatar || '/default-avatar.jpg'"
                  :alt="authStore.user?.name"
                  class="user-avatar"
                  @error="handleAvatarError"
                />
                <span class="user-name"
                  >{{ currentUser?.firstName }} {{ currentUser?.lastName }}</span
                >
                <span class="dropdown-arrow">▼</span>
              </button>
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
                      <h4>{{ currentUser?.firstName }} {{ currentUser?.lastName }}</h4>
                      <p>{{ currentUser?.email }}</p>
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

                  <!-- Loyalty Link - NEW INTEGRATION -->
                  <router-link to="/loyalty" class="dropdown-link loyalty-link">
                    <span class="link-icon">⭐</span>
                    <span>Điểm thưởng</span>
                    <div class="loyalty-points-badge">{{ loyaltyStore.formattedPoints }}</div>
                  </router-link>

                  <router-link to="/wishlist" class="dropdown-link">
                    <span class="link-icon">❤️</span>
                    <span>Yêu thích</span>
                  </router-link>

                  <div v-if="!isAdmin" class="dropdown-divider"></div>
                  <router-link
                    v-if="!isAdmin"
                    to="/seller/dashboard"
                    class="dropdown-link seller-link"
                  >
                    <span class="link-icon">🏪</span>
                    <span>Seller Dashboard</span>
                    <span class="seller-badge">Bán hàng</span>
                  </router-link>

                  <router-link to="/settings" class="dropdown-link">
                    <span class="link-icon">⚙️</span>
                    <span>Cài đặt</span>
                  </router-link>

                  <!-- Admin Link (if user is admin) -->
                  <router-link v-if="isAdmin" to="/admin" class="dropdown-link admin-link">
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
          <div v-else class="auth-section">
            <router-link to="/login" class="nav-link auth-link">
              <span class="nav-icon">🔑</span>
              <span class="nav-text">Đăng nhập</span>
            </router-link>
            <router-link to="/register" class="btn btn-primary signup-btn">🚀 Đăng ký</router-link>
          </div>
        </div>
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
        <!-- Mobile SearchBox -->
        <SearchBox
          placeholder="Tìm kiếm sản phẩm..."
          :show-voice-search="false"
          :max-suggestions="4"
          @search="handleMobileSearch"
          @select-product="handleMobileSelectProduct"
          @select-category="handleMobileSelectCategory"
        />
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
            🛒 Giỏ hàng
            <span v-if="cartStore.totalItems" class="mobile-badge">{{ cartStore.totalItems }}</span>
          </router-link>

          <!-- Mobile Points Display - NEW INTEGRATION -->
          <router-link
            to="/loyalty"
            class="mobile-nav-link loyalty-mobile"
            @click="closeMobileMenu"
          >
            <span class="mobile-loyalty-content">
              ⭐ Điểm thưởng
              <span class="mobile-points-badge">{{ loyaltyStore.formattedPoints }}</span>
              <span class="mobile-tier-badge" :style="{ color: loyaltyStore.tierColor }">
                {{ loyaltyStore.userPoints.tier }}
              </span>
            </span>
          </router-link>

          <router-link to="/profile" class="mobile-nav-link" @click="closeMobileMenu">
            👤 Hồ sơ
          </router-link>
          <router-link to="/orders" class="mobile-nav-link" @click="closeMobileMenu">
            📋 Đơn hàng
          </router-link>

          <!-- 🆕 NEW: Mobile Seller Dashboard Link -->
          <router-link
            to="/seller/dashboard"
            class="mobile-nav-link seller-mobile"
            @click="closeMobileMenu"
          >
            🏪 Seller Dashboard
          </router-link>

          <button @click="logout" class="mobile-nav-link logout-mobile">🚪 Đăng xuất</button>
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
import { ref, computed, onMounted, onUnmounted, watch, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useLoyaltyStore } from '@/stores/loyalty' // NEW IMPORT
import { useSellerStore } from '@/stores/seller' // 🆕 NEW IMPORT
import SearchBox from '@/components/SearchBox.vue'
import PointsDisplay from '@/components/PointsDisplay.vue' // NEW IMPORT
import { notificationAPI } from '@/services/api'

export default {
  name: 'Header',
  components: {
    SearchBox,
    PointsDisplay, // NEW COMPONENT
  },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    const loyaltyStore = useLoyaltyStore()
    const sellerStore = useSellerStore() // 🆕 NEW STORE

    const isAuthenticated = computed(() => authStore.isAuthenticated)
    const isAdmin = computed(() => authStore.isAdmin)
    const currentUser = computed(() => authStore.currentUser)

    // 🆕 NEW: Check if user has any products (is a seller)
    const isSeller = computed(() => {
      return sellerStore.stats.totalProducts > 0
    })

    // Refs
    const searchBoxRef = ref(null)

    // Reactive data
    const showNotifications = ref(false)
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const unreadNotifications = computed(() => notifications.value.filter((n) => !n.read).length)
    // Sample notifications - Updated with loyalty notifications
    const notifications = ref([
      {
        id: 1,
        icon: '📦',
        message: 'Đơn hàng #12345 đã được giao thành công',
        createdAt: '2024-12-22T10:30:00',
        read: false,
      },
      {
        id: 2,
        icon: '⭐',
        message: 'Bạn vừa nhận được 50 điểm thưởng từ đánh giá sản phẩm!',
        createdAt: '2024-12-22T09:45:00',
        read: false,
      },
      {
        id: 3,
        icon: '💰',
        message: 'Giảm giá 50% cho tất cả sản phẩm điện tử',
        createdAt: '2024-12-22T09:15:00',
        read: false,
      },
      {
        id: 4,
        icon: '🎉',
        message: 'Chúc mừng! Bạn đã thăng hạng lên Silver',
        createdAt: '2024-12-21T14:20:00',
        read: true,
      },
    ])

    // SearchBox Event Handlers
    const handleSearch = (query) => {
      router.push({
        path: '/products',
        query: { q: query },
      })
    }

    const handleSelectProduct = (product) => {
      router.push(`/products/${product.id}`)
    }

    const handleSelectCategory = (category) => {
      router.push(`/categories?category=${category.id}`)
    }

    // Mobile Search Handlers
    const handleMobileSearch = (query) => {
      handleSearch(query)
      closeMobileMenu()
    }

    const handleMobileSelectProduct = (product) => {
      handleSelectProduct(product)
      closeMobileMenu()
    }

    const handleMobileSelectCategory = (category) => {
      handleSelectCategory(category)
      closeMobileMenu()
    }

    // Existing Methods
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
      notifications.value.forEach((notification) => {
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
        // Reset loyalty data on logout
        loyaltyStore.resetLoyalty()
        // 🆕 NEW: Reset seller data on logout
        sellerStore.resetSeller()
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
    }

    // Focus search shortcut (Ctrl/Cmd + K)
    const handleKeyboardShortcut = (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
        event.preventDefault()
        searchBoxRef.value?.focus()
      }

      // Escape to close mobile menu
      if (event.key === 'Escape' && showMobileMenu.value) {
        closeMobileMenu()
      }
    }

    // NEW: Initialize loyalty data when user logs in
    const initializeLoyaltyData = async () => {
      if (authStore.isAuthenticated) {
        await loyaltyStore.initializeLoyalty()
      }
    }

    // 🆕 NEW: Initialize seller data when user logs in
    const initializeSellerData = async () => {
      if (authStore.isAuthenticated) {
        try {
          await sellerStore.loadDashboardStats()
        } catch (error) {
          console.error('Load seller stats error:', error)
        }
      }
    }

    // NEW: Watch for authentication changes
    watchEffect(() => {
      if (authStore.isAuthenticated) {
        initializeLoyaltyData()
        initializeSellerData() // 🆕 NEW
        cartStore.loadCart()
      } else {
        loyaltyStore.resetLoyalty()
        sellerStore.resetSeller() // 🆕 NEW
      }
    })

    // Lifecycle
    onMounted(async () => {
      document.addEventListener('click', handleClickOutside)
      document.addEventListener('keydown', handleKeyboardShortcut)

      if (authStore.isAuthenticated) {
        initializeLoyaltyData()
        initializeSellerData()
        cartStore.loadCart()
        // Lấy thông báo thực tế từ backend
        try {
          const res = await notificationAPI.getUserNotifications()
          notifications.value = res.data || []
        } catch (e) {
          console.error('Không thể tải thông báo:', e)
        }
      }
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      document.removeEventListener('keydown', handleKeyboardShortcut)
    })

    return {
      // Stores
      authStore,
      cartStore,
      loyaltyStore,
      sellerStore,

      isAdmin,
      currentUser,
      isSeller,

      // Refs
      searchBoxRef,

      // Data
      showNotifications,
      showUserMenu,
      showMobileMenu,
      unreadNotifications,
      notifications,

      // SearchBox handlers
      handleSearch,
      handleSelectProduct,
      handleSelectCategory,
      handleMobileSearch,
      handleMobileSelectProduct,
      handleMobileSelectCategory,

      // Other methods
      toggleNotifications,
      toggleUserMenu,
      toggleMobileMenu,
      closeMobileMenu,
      markAllAsRead,
      formatTime,
      handleAvatarError,
      logout,
    }
  },
}
</script>

<style scoped>
.header-flex2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.2rem;
}
.header-right2 {
  display: flex;
  align-items: center;
  gap: 1.2rem;
}
.user-bar {
  display: flex;
  align-items: center;
  gap: 1.2rem;
}
.notify-group {
  display: flex;
  align-items: center;
  position: relative;
  margin-right: 10px;
}
.notification-badge {
  position: absolute;
  top: -8px;
  right: -16px;
  background: #ff4757;
  color: #fff;
  border-radius: 50%;
  padding: 2px 10px;
  font-size: 0.95rem;
  font-weight: bold;
  min-width: 28px;
  text-align: center;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}
.reward-group {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  background: rgba(255, 255, 255, 0.07);
  border-radius: 16px;
  padding: 2px 10px 2px 8px;
  margin-left: 0.2rem;
}
.star-icon {
  color: #ffd700;
  font-size: 1.2rem;
  margin-right: 2px;
}
.reward-points {
  color: #fff;
  font-weight: 600;
  font-size: 1rem;
  margin-right: 4px;
}
.tier-badge2 {
  background: #cd7f32;
  color: #fff;
  font-size: 0.85rem;
  font-weight: bold;
  border-radius: 8px;
  padding: 2px 8px;
  margin-left: 2px;
  min-width: 40px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}
.user-info-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
/* Giữ lại các style cũ phía dưới */
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

/* Search Section - Updated for SearchBox component */
.search-section {
  flex: 1;
  max-width: 500px;
  position: relative;
}

/* SearchBox component will handle its own styling, just ensure proper container */
.search-section :deep(.search-box-container) {
  width: 100%;
}

/* Customize SearchBox for header integration */
.search-section :deep(.search-input-wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
}

.search-section :deep(.search-input-wrapper:focus-within) {
  border-color: var(--text-accent);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.2);
}

.search-section :deep(.search-input) {
  background: transparent;
  color: var(--text-primary);
  border: none;
  padding: 0.75rem 1rem;
}

.search-section :deep(.search-input::placeholder) {
  color: var(--text-secondary);
}

.search-section :deep(.search-dropdown) {
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.3);
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
  font-size: 1.4rem;
  min-width: 28px;
  min-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
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

/* NEW: Loyalty-specific styles in Header */
.loyalty-link {
  position: relative;
}

.loyalty-points-badge {
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #1a1a2e;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.4rem;
  border-radius: 10px;
  margin-left: auto;
  box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
}

/* 🆕 NEW: Seller-specific styles in Header */
.seller-link {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  margin: 0.25rem 0;
  position: relative;
}

.seller-link:hover {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2) 0%, rgba(102, 126, 234, 0.2) 100%);
  border-color: var(--text-accent);
  transform: translateX(3px);
}

.seller-badge {
  position: absolute;
  top: 50%;
  right: 1rem;
  transform: translateY(-50%);
  background: var(--text-accent);
  color: #1a1a2e;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
}

/* NEW: Mobile Loyalty Styles */
.loyalty-mobile {
  border-left: 3px solid #ffd700 !important;
  background: linear-gradient(90deg, rgba(255, 215, 0, 0.1), transparent) !important;
}

.mobile-loyalty-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.mobile-points-badge {
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #1a1a2e;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.4rem;
  border-radius: 8px;
  margin-left: 0.5rem;
}

.mobile-tier-badge {
  font-size: 0.7rem;
  font-weight: 600;
  opacity: 0.8;
  margin-left: 0.25rem;
}

/* 🆕 NEW: Mobile Seller Styles */
.seller-mobile {
  border-left: 3px solid var(--text-accent) !important;
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.1), transparent) !important;
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

/* 🆕 NEW: Dropdown Divider */
.dropdown-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0.5rem 0;
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

/* Mobile SearchBox customization */
.mobile-search :deep(.search-box-container) {
  width: 100%;
}

.mobile-search :deep(.search-input-wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
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

/* NEW: PointsDisplay integration in header */
.user-section :deep(.points-display) {
  /* Ensure PointsDisplay fits well in header */
  margin: 0;
}

.user-section :deep(.points-container) {
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-section :deep(.points-container:hover) {
  background: rgba(0, 212, 255, 0.1);
  transform: translateY(-1px);
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

  /* Hide points text on smaller screens, keep only icon */
  .user-section :deep(.points-info .text-sm) {
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
.mobile-nav-link:focus {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}

/* Dark mode compatibility */
@media (prefers-color-scheme: dark) {
  .app-header {
    background: rgba(16, 16, 24, 0.95);
  }

  .dropdown-menu,
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

/* SearchBox keyboard shortcut hint */
.search-section::after {
  content: 'Ctrl+K';
  position: absolute;
  right: 3rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.7rem;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.1);
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  pointer-events: none;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .search-section::after {
    display: none;
  }
}

/* New styles for .header-notification-link */
.header-notification-link {
  margin: 0 1rem;
  font-size: 1.5rem;
  color: #222;
  text-decoration: none;
  position: relative;
}

.header-notification-link:hover {
  color: #667eea;
}

.icon-badge-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  transform: translate(50%, -50%);
  background: #ff4757;
  color: #fff;
  border-radius: 50%;
  padding: 2px 7px;
  font-size: 0.85rem;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
