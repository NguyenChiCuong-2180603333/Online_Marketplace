import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/Products.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue')
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('@/views/Categories.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  // NEW: Loyalty route - Phase 2 integration
  {
    path: '/loyalty',
    name: 'Loyalty',
    component: () => import('@/views/LoyaltyPage.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Chương trình Khách hàng Thân thiết'
    }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('@/views/admin/Products.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('@/views/admin/Orders.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/Users.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // Check if route requires authentication
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!authStore.isAuthenticated) {
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check if route requires admin
    if (to.matched.some(record => record.meta.requiresAdmin)) {
      if (!authStore.isAdmin) {
        next({ name: 'Home' })
        return
      }
    }
  }
  
  // Check if route requires guest (not authenticated)
  if (to.matched.some(record => record.meta.requiresGuest)) {
    if (authStore.isAuthenticated) {
      next({ name: 'Home' })
      return
    }
  }
  
  // NEW: Initialize loyalty data when navigating to loyalty page
  if (to.name === 'Loyalty' && authStore.isAuthenticated) {
    try {
      // Import loyalty store dynamically to avoid circular imports
      const { useLoyaltyStore } = await import('@/stores/loyalty')
      const loyaltyStore = useLoyaltyStore()
      
      // Initialize loyalty data if not already loaded
      if (!loyaltyStore.userPoints.current) {
        await loyaltyStore.initializeLoyalty()
      }
    } catch (error) {
      console.error('Failed to initialize loyalty data:', error)
      // Don't block navigation, just log the error
    }
  }
  
  // Set page title if provided in meta
  if (to.meta.title) {
    document.title = `${to.meta.title} - CosmicMarket`
  } else {
    document.title = 'CosmicMarket - Future Commerce'
  }
  
  next()
})

// NEW: After each navigation, track page views for analytics
router.afterEach((to, from) => {
  // Track page view for analytics (can be enhanced later)
  if (typeof window !== 'undefined' && window.gtag) {
    window.gtag('config', 'GA_TRACKING_ID', {
      page_title: to.meta.title || to.name,
      page_location: window.location.href,
      page_path: to.fullPath
    })
  }
  
  // Log navigation for debugging in development
  if (import.meta.env.DEV) {
    console.log(`🧭 Navigated from ${from.name || from.path} to ${to.name || to.path}`)
  }
})

export default router