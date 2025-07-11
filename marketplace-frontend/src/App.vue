<template>
  <div id="app" class="space-bg">
    <Header />
    <main class="main-content">
      <router-view />
    </main>

    <Footer />

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
import { computed, ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import NotificationContainer from '@/components/NotificationContainer.vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

export default {
  name: 'App',
  components: { Header, NotificationContainer, Footer },
  setup() {
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    const router = useRouter()
    const chatStore = useChatStore()

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
        console.log('App.vue: Gọi chatStore.initializeChat khi user login', authStore.user?.id)
        chatStore.initializeChat(authStore.user.id, authStore.token)
      }
    })

    watch(
      () => authStore.isAuthenticated,
      (val) => {
        if (val) {
          console.log(
            'App.vue: Gọi chatStore.initializeChat khi user login (watch)',
            authStore.user?.id
          )
          chatStore.initializeChat(authStore.user.id, authStore.token)
        } else {
          chatStore.disconnectChat()
        }
      }
    )

    return {
      authStore,
      cartStore,
      showUserMenu,
      loading,
      isAuthenticated,
      isAdmin,
      userName,
      cartItemsCount,
      handleLogout,
    }
  },
}
</script>

<style scoped>
.logo-icon {
  font-size: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
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
