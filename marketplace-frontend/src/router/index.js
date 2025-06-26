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
        title: 'Seller Dashboard',
        requiresSeller: true 
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
            breadcrumb: 'Dashboard',
            description: 'Tổng quan hoạt động kinh doanh'
          }
        },

        {
          path: 'products',
          name: 'SellerProducts',
          component: () => import('@/views/seller/MyProducts.vue'),
          meta: { 
            title: 'Sản phẩm của tôi - Seller',
            breadcrumb: 'Sản phẩm',
            description: 'Quản lý tất cả sản phẩm của bạn'
          }
        },
        {
          path: 'products/create',
          name: 'CreateProduct',
          component: () => import('@/views/seller/ProductForm.vue'),
          meta: { 
            title: 'Tạo sản phẩm mới - Seller',
            breadcrumb: 'Tạo sản phẩm',
            description: 'Thêm sản phẩm mới vào cửa hàng'
          }
        },
        {
          path: 'products/edit/:id',
          name: 'EditProduct',
          component: () => import('@/views/seller/ProductForm.vue'),
          meta: { 
            title: 'Chỉnh sửa sản phẩm - Seller',
            breadcrumb: 'Chỉnh sửa sản phẩm',
            description: 'Cập nhật thông tin sản phẩm'
          },
          props: true
        },
        {
          path: 'products/:id/details',
          name: 'SellerProductDetail',
          component: () => import('@/views/seller/ProductDetail.vue'),
          meta: { 
            title: 'Chi tiết sản phẩm - Seller',
            breadcrumb: 'Chi tiết sản phẩm',
            description: 'Xem chi tiết và thống kê sản phẩm'
          },
          props: true
        },
        {
          path: 'products/bulk',
          name: 'BulkProducts',
          component: () => import('@/views/seller/BulkProducts.vue'),
          meta: { 
            title: 'Xử lý hàng loạt - Seller',
            breadcrumb: 'Xử lý hàng loạt',
            description: 'Cập nhật nhiều sản phẩm cùng lúc'
          }
        },

        {
          path: 'orders',
          name: 'SellerOrders',
          component: () => import('@/views/seller/Orders.vue'),
          meta: { 
            title: 'Quản lý đơn hàng - Seller',
            breadcrumb: 'Đơn hàng',
            description: 'Xử lý và theo dõi đơn hàng'
          }
        },
        {
          path: 'orders/:id',
          name: 'SellerOrderDetail',
          component: () => import('@/views/seller/OrderDetail.vue'),
          meta: { 
            title: 'Chi tiết đơn hàng - Seller',
            breadcrumb: 'Chi tiết đơn hàng',
            description: 'Thông tin chi tiết đơn hàng'
          },
          props: true
        },
        {
          path: 'orders/:id/chat',
          name: 'OrderChat',
          component: () => import('@/views/seller/OrderChat.vue'),
          meta: { 
            title: 'Chat đơn hàng - Seller',
            breadcrumb: 'Chat khách hàng',
            description: 'Trò chuyện với khách hàng về đơn hàng'
          },
          props: true
        },
        {
          path: 'orders/bulk',
          name: 'BulkOrders',
          component: () => import('@/views/seller/BulkOrders.vue'),
          meta: { 
            title: 'Xử lý đơn hàng hàng loạt - Seller',
            breadcrumb: 'Xử lý hàng loạt',
            description: 'Cập nhật nhiều đơn hàng cùng lúc'
          }
        },

        {
          path: 'analytics',
          name: 'SellerAnalytics',
          component: () => import('@/views/seller/Analytics.vue'),
          meta: { 
            title: 'Thống kê & Phân tích - Seller',
            breadcrumb: 'Thống kê',
            description: 'Phân tích hiệu suất kinh doanh'
          }
        },
        {
          path: 'analytics/revenue',
          name: 'RevenueAnalytics',
          component: () => import('@/views/seller/analytics/Revenue.vue'),
          meta: { 
            title: 'Phân tích Doanh thu - Seller',
            breadcrumb: 'Doanh thu',
            description: 'Chi tiết về doanh thu và xu hướng'
          }
        },
        {
          path: 'analytics/products',
          name: 'ProductAnalytics',
          component: () => import('@/views/seller/analytics/Products.vue'),
          meta: { 
            title: 'Phân tích Sản phẩm - Seller',
            breadcrumb: 'Hiệu suất sản phẩm',
            description: 'Thống kê về sản phẩm bán chạy'
          }
        },
        {
          path: 'analytics/customers',
          name: 'CustomerAnalytics',
          component: () => import('@/views/seller/analytics/Customers.vue'),
          meta: { 
            title: 'Phân tích Khách hàng - Seller',
            breadcrumb: 'Khách hàng',
            description: 'Hành vi và giá trị khách hàng'
          }
        },

        {
          path: 'reports',
          name: 'SellerReports',
          component: () => import('@/views/seller/Reports.vue'),
          meta: { 
            title: 'Báo cáo - Seller',
            breadcrumb: 'Báo cáo',
            description: 'Tạo và xuất báo cáo kinh doanh'
          }
        },
        {
          path: 'reports/orders',
          name: 'OrderReports',
          component: () => import('@/views/seller/reports/Orders.vue'),
          meta: { 
            title: 'Báo cáo Đơn hàng - Seller',
            breadcrumb: 'Báo cáo đơn hàng',
            description: 'Thống kê chi tiết về đơn hàng'
          }
        },
        {
          path: 'reports/sales',
          name: 'SalesReports',
          component: () => import('@/views/seller/reports/Sales.vue'),
          meta: { 
            title: 'Báo cáo Bán hàng - Seller',
            breadcrumb: 'Báo cáo bán hàng',
            description: 'Phân tích doanh số và xu hướng'
          }
        },

        {
          path: 'inventory',
          name: 'SellerInventory',
          component: () => import('@/views/seller/Inventory.vue'),
          meta: { 
            title: 'Quản lý Kho hàng - Seller',
            breadcrumb: 'Kho hàng',
            description: 'Theo dõi và quản lý tồn kho'
          }
        },
        {
          path: 'inventory/low-stock',
          name: 'LowStockProducts',
          component: () => import('@/views/seller/inventory/LowStock.vue'),
          meta: { 
            title: 'Sản phẩm sắp hết - Seller',
            breadcrumb: 'Sắp hết hàng',
            description: 'Sản phẩm cần nhập thêm'
          }
        },
        {
          path: 'inventory/history',
          name: 'InventoryHistory',
          component: () => import('@/views/seller/inventory/History.vue'),
          meta: { 
            title: 'Lịch sử Kho hàng - Seller',
            breadcrumb: 'Lịch sử',
            description: 'Theo dõi thay đổi tồn kho'
          }
        },

        {
          path: 'marketing',
          name: 'SellerMarketing',
          component: () => import('@/views/seller/Marketing.vue'),
          meta: { 
            title: 'Marketing - Seller',
            breadcrumb: 'Marketing',
            description: 'Công cụ tiếp thị và khuyến mãi'
          }
        },
        {
          path: 'promotions',
          name: 'SellerPromotions',
          component: () => import('@/views/seller/Promotions.vue'),
          meta: { 
            title: 'Khuyến mãi - Seller',
            breadcrumb: 'Khuyến mãi',
            description: 'Tạo và quản lý chương trình khuyến mãi'
          }
        },
        {
          path: 'promotions/create',
          name: 'CreatePromotion',
          component: () => import('@/views/seller/PromotionForm.vue'),
          meta: { 
            title: 'Tạo khuyến mãi - Seller',
            breadcrumb: 'Tạo khuyến mãi',
            description: 'Thiết lập chương trình khuyến mãi mới'
          }
        },

        {
          path: 'reviews',
          name: 'SellerReviews',
          component: () => import('@/views/seller/Reviews.vue'),
          meta: { 
            title: 'Đánh giá - Seller',
            breadcrumb: 'Đánh giá',
            description: 'Quản lý đánh giá từ khách hàng'
          }
        },
        {
          path: 'reviews/:reviewId/respond',
          name: 'RespondReview',
          component: () => import('@/views/seller/ReviewResponse.vue'),
          meta: { 
            title: 'Phản hồi đánh giá - Seller',
            breadcrumb: 'Phản hồi',
            description: 'Trả lời đánh giá khách hàng'
          },
          props: true
        },

        {
          path: 'finances',
          name: 'SellerFinances',
          component: () => import('@/views/seller/Finances.vue'),
          meta: { 
            title: 'Tài chính - Seller',
            breadcrumb: 'Tài chính',
            description: 'Quản lý thu nhập và chi phí'
          }
        },
        {
          path: 'payouts',
          name: 'SellerPayouts',
          component: () => import('@/views/seller/Payouts.vue'),
          meta: { 
            title: 'Thanh toán - Seller',
            breadcrumb: 'Thanh toán',
            description: 'Lịch sử và yêu cầu thanh toán'
          }
        },

        {
          path: 'messages',
          name: 'SellerMessages',
          component: () => import('@/views/seller/Messages.vue'),
          meta: { 
            title: 'Tin nhắn - Seller',
            breadcrumb: 'Tin nhắn',
            description: 'Trung tâm tin nhắn với khách hàng'
          }
        },
        {
          path: 'messages/:conversationId',
          name: 'SellerConversation',
          component: () => import('@/views/seller/Conversation.vue'),
          meta: { 
            title: 'Cuộc trò chuyện - Seller',
            breadcrumb: 'Trò chuyện',
            description: 'Chat với khách hàng'
          },
          props: true
        },

        {
          path: 'tools',
          name: 'SellerTools',
          component: () => import('@/views/seller/Tools.vue'),
          meta: { 
            title: 'Công cụ - Seller',
            breadcrumb: 'Công cụ',
            description: 'Các công cụ hỗ trợ bán hàng'
          }
        },
        {
          path: 'tools/seo',
          name: 'SEOTools',
          component: () => import('@/views/seller/tools/SEO.vue'),
          meta: { 
            title: 'Công cụ SEO - Seller',
            breadcrumb: 'SEO',
            description: 'Tối ưu hóa sản phẩm cho tìm kiếm'
          }
        },
        {
          path: 'tools/pricing',
          name: 'PricingTools',
          component: () => import('@/views/seller/tools/Pricing.vue'),
          meta: { 
            title: 'Công cụ Định giá - Seller',
            breadcrumb: 'Định giá',
            description: 'Phân tích và đề xuất giá bán'
          }
        },

        {
          path: 'help',
          name: 'SellerHelp',
          component: () => import('@/views/seller/Help.vue'),
          meta: { 
            title: 'Trợ giúp - Seller',
            breadcrumb: 'Trợ giúp',
            description: 'Hướng dẫn và hỗ trợ'
          }
        },
        {
          path: 'support',
          name: 'SellerSupport',
          component: () => import('@/views/seller/Support.vue'),
          meta: { 
            title: 'Hỗ trợ - Seller',
            breadcrumb: 'Hỗ trợ',
            description: 'Liên hệ team hỗ trợ'
          }
        },

        {
          path: 'settings',
          name: 'SellerSettings',
          component: () => import('@/views/seller/Settings.vue'),
          meta: { 
            title: 'Cài đặt - Seller',
            breadcrumb: 'Cài đặt',
            description: 'Tùy chỉnh tài khoản và cửa hàng'
          }
        },
        {
          path: 'settings/store',
          name: 'StoreSettings',
          component: () => import('@/views/seller/settings/Store.vue'),
          meta: { 
            title: 'Cài đặt Cửa hàng - Seller',
            breadcrumb: 'Cửa hàng',
            description: 'Thông tin và giao diện cửa hàng'
          }
        },
        {
          path: 'settings/notifications',
          name: 'NotificationSettings',
          component: () => import('@/views/seller/settings/Notifications.vue'),
          meta: { 
            title: 'Cài đặt Thông báo - Seller',
            breadcrumb: 'Thông báo',
            description: 'Tùy chỉnh thông báo và email'
          }
        },
        {
          path: 'settings/shipping',
          name: 'ShippingSettings',
          component: () => import('@/views/seller/settings/Shipping.vue'),
          meta: { 
            title: 'Cài đặt Vận chuyển - Seller',
            breadcrumb: 'Vận chuyển',
            description: 'Cấu hình phương thức giao hàng'
          }
        },
        {
          path: 'settings/payments',
          name: 'PaymentSettings',
          component: () => import('@/views/seller/settings/Payments.vue'),
          meta: { 
            title: 'Cài đặt Thanh toán - Seller',
            breadcrumb: 'Thanh toán',
            description: 'Cấu hình tài khoản nhận tiền'
          }
        }
      ]
    },

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

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.title) {
    document.title = `${to.meta.title} | ${import.meta.env.VITE_APP_NAME || 'Cosmic Marketplace'}`
  } else {
    document.title = import.meta.env.VITE_APP_NAME || 'Cosmic Marketplace'
  }

  if (to.meta.description) {
    let metaDescription = document.querySelector('meta[name="description"]')
    if (!metaDescription) {
      metaDescription = document.createElement('meta')
      metaDescription.name = 'description'
      document.head.appendChild(metaDescription)
    }
    metaDescription.content = to.meta.description
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

  if (to.meta.requiresSeller && !authStore.canBeSeller) {
    next({ 
      name: 'Profile',
      query: { 
        message: 'Bạn cần hoàn tất đăng ký bán hàng để truy cập tính năng này',
        tab: 'seller'
      }
    })
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
      page_location: to.fullPath,
      page_path: to.path
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
    if (to.path.startsWith('/seller/')) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } else {
      window.scrollTo(0, 0)
    }
  }

  if (to.path.startsWith('/seller/') && typeof window.analytics !== 'undefined') {
    window.analytics.track('Seller Page View', {
      page: to.name,
      path: to.path,
      breadcrumb: to.meta.breadcrumb
    })
  }

  if (to.meta.breadcrumb && to.path.startsWith('/seller/')) {
    document.dispatchEvent(new CustomEvent('breadcrumb-update', {
      detail: { breadcrumb: to.meta.breadcrumb, path: to.path }
    }))
  }
})

export default router