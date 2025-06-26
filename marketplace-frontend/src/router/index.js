import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { guest: true }
    },
    {
      path: '/register', 
      name: 'Register',
      component: () => import('@/views/auth/Register.vue'),
      meta: { guest: true }
    },
    {
      path: '/products',
      name: 'Products',
      component: () => import('@/views/Products.vue')
    },
    {
      path: '/products/:id',
      name: 'ProductDetail',
      component: () => import('@/views/ProductDetail.vue'),
      props: true
    },
    {
      path: '/categories/:category',
      name: 'CategoryProducts', 
      component: () => import('@/views/CategoryProducts.vue'),
      props: true
    },

    // Protected User routes
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true }
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
      meta: { requiresAuth: true },
      props: true
    },

    {
      path: '/seller',
      name: 'SellerLayout',
      component: () => import('@/layouts/SellerLayout.vue'),
      meta: { 
        requiresAuth: true,
        title: 'Seller Dashboard'
      },
      children: [
        {
          path: '',
          redirect: '/seller/dashboard'
        },
        {
          path: 'dashboard',
          name: 'SellerDashboard',
          component: () => import('@/views/seller/Dashboard.vue'),
          meta: { 
            title: 'Dashboard - Seller',
            breadcrumb: 'Dashboard'
          }
        },
        {
          path: 'products',
          name: 'SellerProducts',
          component: () => import('@/views/seller/MyProducts.vue'),
          meta: { 
            title: 'Sản phẩm của tôi - Seller',
            breadcrumb: 'Sản phẩm'
          }
        },
        {
          path: 'products/create',
          name: 'CreateProduct',
          component: () => import('@/views/seller/ProductForm.vue'),
          meta: { 
            title: 'Tạo sản phẩm mới - Seller',
            breadcrumb: 'Tạo sản phẩm'
          }
        },
        {
          path: 'products/edit/:id',
          name: 'EditProduct',
          component: () => import('@/views/seller/ProductForm.vue'),
          meta: { 
            title: 'Chỉnh sửa sản phẩm - Seller',
            breadcrumb: 'Chỉnh sửa sản phẩm'
          },
          props: true
        },
        {
          path: 'orders',
          name: 'SellerOrders',
          component: () => import('@/views/seller/Orders.vue'),
          meta: { 
            title: 'Quản lý đơn hàng - Seller',
            breadcrumb: 'Đơn hàng'
          }
        },
        {
          path: 'orders/:id',
          name: 'SellerOrderDetail',
          component: () => import('@/views/seller/OrderDetail.vue'),
          meta: { 
            title: 'Chi tiết đơn hàng - Seller',
            breadcrumb: 'Chi tiết đơn hàng'
          },
          props: true
        },
        {
          path: 'analytics',
          name: 'SellerAnalytics',
          component: () => import('@/views/seller/Analytics.vue'),
          meta: { 
            title: 'Thống kê - Seller',
            breadcrumb: 'Thống kê'
          }
        },
        {
          path: 'settings',
          name: 'SellerSettings',
          component: () => import('@/views/seller/Settings.vue'),
          meta: { 
            title: 'Cài đặt - Seller',
            breadcrumb: 'Cài đặt'
          }
        }
      ]
    },

    // Admin routes
    {
      path: '/admin',
      name: 'AdminLayout',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { 
        requiresAuth: true, 
        requiresAdmin: true,
        title: 'Admin Dashboard'
      },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/Users.vue')
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/admin/Products.vue')
        },
        {
          path: 'orders',
          name: 'AdminOrders',
          component: () => import('@/views/admin/Orders.vue')
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('@/views/admin/Categories.vue')
        }
      ]
    },

    {
      path: '/chat',
      name: 'Chat',
      component: () => import('@/views/Chat.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/chat/:conversationId',
      name: 'ChatConversation',
      component: () => import('@/views/ChatConversation.vue'),
      meta: { requiresAuth: true },
      props: true
    },

    {
      path: '/404',
      name: 'NotFound',
      component: () => import('@/views/errors/404.vue')
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/errors/403.vue')
    },
    {
      path: '/500',
      name: 'ServerError',
      component: () => import('@/views/errors/500.vue')
    },

    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.title) {
    document.title = `${to.meta.title} | ${import.meta.env.VITE_APP_NAME || 'Cosmic Marketplace'}`
  } else {
    document.title = import.meta.env.VITE_APP_NAME || 'Cosmic Marketplace'
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'Forbidden' })
    return
  }

  if (to.meta.guest && authStore.isAuthenticated) {
    next({ name: 'Home' })
    return
  }

  next()
})

router.afterEach((to, from) => {
  if (typeof gtag !== 'undefined') {
    gtag('config', 'GA_MEASUREMENT_ID', {
      page_title: to.name,
      page_location: to.fullPath
    })
  }

  if (to.hash) {
    setTimeout(() => {
      const element = document.querySelector(to.hash)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
  } else {
    window.scrollTo(0, 0)
  }
})

export default router