<template>
  <div id="app" class="space-bg">
    <!-- Navigation -->
    <nav class="navbar">
      <div class="container">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <router-link to="/" class="flex items-center space-x-2">
            <div class="logo-icon">🌌</div>
            <h1 class="text-xl font-bold text-accent">Cosmic Marketplace</h1>
          </router-link>

          <!-- Navigation Links -->
          <div class="flex items-center space-x-4">
            <router-link to="/" class="nav-link">Trang chủ</router-link>
            <router-link to="/products" class="nav-link">Sản phẩm</router-link>
            <router-link to="/categories" class="nav-link">Danh mục</router-link>
            
            <template v-if="isAuthenticated">
              <router-link to="/cart" class="nav-link relative">
                🛒 Giỏ hàng
                <span v-if="cartItemsCount > 0" class="cart-badge">{{ cartItemsCount }}</span>
              </router-link>
              <router-link to="/orders" class="nav-link">Đơn hàng</router-link>
              
              <div class="relative user-menu">
                <button @click="showUserMenu = !showUserMenu" class="nav-link">
                  👨‍🚀 {{ userName }}
                </button>
                <div v-if="showUserMenu" class="user-dropdown space-card">
                  <router-link to="/profile" class="dropdown-item">Hồ sơ</router-link>
                  <router-link v-if="!isAdmin" to="/seller/dashboard" class="dropdown-item">Bán hàng</router-link>
                  <router-link v-if="isAdmin" to="/admin" class="dropdown-item">Quản trị</router-link>
                  <button @click="handleLogout" class="dropdown-item text-danger">Đăng xuất</button>
                </div>
              </div>
            </template>
            
            <template v-else>
              <router-link to="/login" class="btn btn-secondary">Đăng nhập</router-link>
              <router-link to="/register" class="btn btn-primary">Đăng ký</router-link>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h3>🌌 Cosmic Marketplace</h3>
            <p>Khám phá vũ trụ mua sắm trực tuyến</p>
          </div>
          <div class="footer-section">
            <h4>Liên kết</h4>
            <ul>
              <li><router-link to="/about">Giới thiệu</router-link></li>
              <li><router-link to="/contact">Liên hệ</router-link></li>
              <li><router-link to="/help">Trợ giúp</router-link></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>Hỗ trợ</h4>
            <ul>
              <li><a href="tel:+84123456789">📞 0123 456 789</a></li>
              <li><a href="mailto:support@cosmic.com">📧 support@cosmic.com</a></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 Cosmic Marketplace. Mọi quyền được bảo lưu.</p>
        </div>
      </div>
    </footer>

    <!-- Loading Overlay -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner">
        <div class="loading"></div>
        <p class="mt-2">Đang tải...</p>
      </div>
    </div>
    <NotificationContainer />
  </div>
</template>

<script>
import { computed, ref, onMounted  } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import NotificationContainer from '@/components/NotificationContainer.vue'

export default {
  name: 'App',
  setup() {
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    const router = useRouter()
    
    const showUserMenu = ref(false)
    const loading = ref(false)

    const isAuthenticated = computed(() => authStore.isAuthenticated)
    const isAdmin = computed(() => authStore.isAdmin)
    const userName = computed(() => authStore.userName)
    const cartItemsCount = computed(() => cartStore.totalItems)

    const handleLogout = async () => {
      try {
        await authStore.logout()
        showUserMenu.value = false
        router.push('/')
      } catch (error) {
        console.error('Logout error:', error)
      }
    }
    
    onMounted(() => {
      authStore.initializeFromStorage()

    if (isAuthenticated.value) {
      cartStore.loadCart()
    }
    })

    return {
      authStore,
      cartStore,
      showUserMenu,
      loading,
      isAuthenticated,
      isAdmin,
      userName,
      cartItemsCount,
      handleLogout
    }
  }
}
</script>

<style scoped>
.logo-icon {
  font-size: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.space-x-2 > * + * {
  margin-left: 8px;
}

.space-x-4 > * + * {
  margin-left: 16px;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--text-danger);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.user-menu {
  position: relative;
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 200px;
  margin-top: 8px;
  z-index: 1000;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 12px 16px;
  text-align: left;
  background: none;
  border: none;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.dropdown-item:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.main-content {
  min-height: calc(100vh - 200px);
  padding: 20px 0;
}

.footer {
  background: rgba(10, 10, 15, 0.9);
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding: 40px 0 20px;
  margin-top: 60px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-bottom: 30px;
}

.footer-section h3,
.footer-section h4 {
  color: var(--text-accent);
  margin-bottom: 16px;
}

.footer-section ul {
  list-style: none;
}

.footer-section ul li {
  margin-bottom: 8px;
}

.footer-section a {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-section a:hover {
  color: var(--text-accent);
}

.footer-bottom {
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding-top: 20px;
  text-align: center;
  color: var(--text-secondary);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 10, 15, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.loading-spinner {
  text-align: center;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .navbar .container {
    flex-direction: column;
    gap: 16px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
}
</style>