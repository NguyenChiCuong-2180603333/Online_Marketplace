import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import { sellerGuard } from '@/utils/routerGuards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { title: 'Trang chủ - Cosmic Marketplace' },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: {
        guest: true,
        title: 'Đăng nhập - Cosmic Marketplace',
      },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: {
        guest: true,
        title: 'Đăng ký - Cosmic Marketplace',
      },
    },
    {
      path: '/products',
      name: 'Products',
      component: () => import('@/views/Products.vue'),
      meta: { title: 'Sản phẩm - Cosmic Marketplace' },
    },
    {
      path: '/products/:id',
      name: 'ProductDetail',
      component: () => import('@/views/ProductDetail.vue'),
      props: true,
      meta: { title: 'Chi tiết sản phẩm - Cosmic Marketplace' },
    },
    {
      path: '/categories',
      name: 'Categories',
      component: () => import('@/views/Categories.vue'),
      meta: { title: 'Danh mục sản phẩm - Cosmic Marketplace' },
    },
    {
      path: '/categories/:category',
      name: 'CategoryProducts',
      component: () => import('@/views/CategoryProducts.vue'),
      props: true,
      meta: { title: 'Danh mục sản phẩm - Cosmic Marketplace' },
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('@/views/Search.vue'),
      meta: { title: 'Tìm kiếm - Cosmic Marketplace' },
    },

    // User authenticated routes
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/Profile.vue'),
      meta: {
        requiresAuth: true,
        title: 'Hồ sơ cá nhân - Cosmic Marketplace',
      },
    },
    {
      path: '/cart',
      name: 'Cart',
      component: () => import('@/views/Cart.vue'),
      meta: {
        requiresAuth: true,
        title: 'Giỏ hàng - Cosmic Marketplace',
      },
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: () => import('@/views/Checkout.vue'),
      meta: {
        requiresAuth: true,
        title: 'Thanh toán - Cosmic Marketplace',
      },
    },
    {
      path: '/orders',
      name: 'Orders',
      component: () => import('@/views/Orders.vue'),
      meta: {
        requiresAuth: true,
        title: 'Đơn hàng của tôi - Cosmic Marketplace',
      },
    },
    {
      path: '/orders/:id',
      name: 'OrderDetail',
      component: () => import('@/views/OrderDetail.vue'),
      meta: {
        requiresAuth: true,
        title: 'Chi tiết đơn hàng - Cosmic Marketplace',
      },
      props: true,
    },
    {
      path: '/notifications',
      name: 'Notifications',
      component: () => import('@/views/Notifications.vue'),
      meta: {
        requiresAuth: true,
        title: 'Thông báo - Cosmic Marketplace',
      },
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/views/Chat.vue'),
      meta: {
        requiresAuth: true,
        title: 'Tin nhắn - Cosmic Marketplace',
      },
    },

    // Admin routes with layout
    {
      path: '/admin',
      name: 'AdminLayout',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        title: 'Admin Dashboard',
      },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard',
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: {
            title: 'Dashboard - Admin',
            breadcrumb: 'Dashboard',
          },
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/Products.vue'),
          meta: {
            title: 'Quản lý sản phẩm - Admin',
            breadcrumb: 'Sản phẩm',
          },
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/Orders.vue'),
          meta: {
            title: 'Quản lý đơn hàng - Admin',
            breadcrumb: 'Đơn hàng',
          },
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/Users.vue'),
          meta: {
            title: 'Quản lý người dùng - Admin',
            breadcrumb: 'Người dùng',
          },
        },
        // {
        //   path: 'analytics',
        //   name: 'AdminAnalytics',
        //   component: () => import('@/views/admin/Analytics.vue'),
        //   meta: {
        //     title: 'Analytics - Admin',
        //     breadcrumb: 'Analytics',
        //   },
        // },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('@/views/admin/Categories.vue'),
          meta: {
            title: 'Quản lý danh mục - Admin',
            breadcrumb: 'Danh mục',
          },
        },
        {
          path: 'notifications',
          name: 'AdminNotifications',
          component: () => import('@/views/admin/Notifications.vue'),
          meta: {
            title: 'Quản lý thông báo - Admin',
            breadcrumb: 'Thông báo',
          },
        },
        {
          path: 'settings',
          name: 'AdminSettings',
          component: () => import('@/views/admin/Settings.vue'),
          meta: {
            title: 'Cài đặt hệ thống - Admin',
            breadcrumb: 'Cài đặt',
          },
        },
      ],
    },

    {
      path: '/seller',
      name: 'SellerLayout',
      component: () => import('@/layouts/SellerLayout.vue'),
      meta: {
        requiresAuth: true,
        requiresSeller: true, 
        title: 'Seller Dashboard',
      },
      children: [
        {
          path: '',
          redirect: '/seller/dashboard',
        },
        {
          path: 'dashboard',
          name: 'SellerDashboard',
          component: () => import('@/views/seller/Dashboard.vue'),
          meta: {
            title: 'Dashboard - Seller',
            breadcrumb: 'Dashboard',
            requiresSeller: true, 
          },
        },
        {
          path: 'products',
          name: 'SellerProducts',
          component: () => import('@/views/seller/MyProducts.vue'),
          meta: {
            title: 'Sản phẩm của tôi - Seller',
            breadcrumb: 'Sản phẩm',
            requiresSeller: true,
          },
        },

        {
          path: 'orders',
          name: 'SellerOrders',
          component: () => import('@/views/seller/Orders.vue'),
          meta: {
            title: 'Quản lý đơn hàng - Seller',
            breadcrumb: 'Đơn hàng',
            requiresSeller: true,
          },
        },
        {
          path: 'analytics',
          name: 'SellerAnalytics',
          component: () => import('@/views/seller/Analytics.vue'),
          meta: {
            title: 'Analytics - Seller',
            breadcrumb: 'Analytics',
            requiresSeller: true,
          },
        },
        {
          path: 'chat',
          name: 'SellerChat',
          redirect: '/chat?context=seller',
        },
        {
          path: 'settings',
          name: 'SellerSettings',
          component: () => import('@/views/seller/Settings.vue'),
          meta: {
            title: 'Cài đặt - Seller',
            breadcrumb: 'Cài đặt',
            requiresSeller: true,
          },
        },
      ],
    },

    // 404 page
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { title: 'Không tìm thấy trang - Cosmic Marketplace' },
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // Update document title
  if (to.meta.title) {
    document.title = to.meta.title
  }

  if (to.path.startsWith('/seller') || to.meta.requiresSeller) {
    console.log('🎯 Applying seller guard for:', to.path)

    return sellerGuard(to, from, next)
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('❌ Not authenticated, redirecting to login')
    next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    console.log('❌ Not admin, redirecting to home')
    next('/')
    return
  }

  if (to.meta.guest && authStore.isAuthenticated) {
    console.log('✅ Already authenticated, redirecting from guest page')
    next('/')
    return
  }

  console.log('✅ Normal route access granted:', to.path)
  next()
})

router.onError((error) => {
  console.error('🚨 Router error:', error)

  if (error.message.includes('Loading chunk')) {
    console.log('🔄 Chunk loading failed, reloading page...')
    window.location.reload()
    return
  }

  router.push('/').catch(() => {
    window.location.href = '/'
  })
})

export default router
