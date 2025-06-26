import { useAuthStore } from '@/stores/auth'
import { useSellerStore } from '@/stores/seller'

export const canAccessSellerFeatures = (user) => {
  if (!user) return false
  

  return (
    user.isEnabled && 
    user.emailVerified && 
    (user.role === 'USER' || user.role === 'ADMIN') &&
    !user.isBanned
  )
}


export const hasCompletedSellerOnboarding = (user) => {
  if (!user) return false
  
  return (
    user.sellerProfile?.isVerified ||
    user.sellerProfile?.isActive ||
    user.hasSellerPermissions
  )
}


export const sellerGuard = (to, from, next) => {
  const authStore = useAuthStore()
  const user = authStore.user
  
  if (!authStore.isAuthenticated) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  if (!canAccessSellerFeatures(user)) {
    next({
      name: 'Forbidden',
      query: { 
        reason: 'seller_access_denied',
        message: 'Bạn không có quyền truy cập tính năng seller'
      }
    })
    return
  }
  
  const requiresOnboarding = [
    'SellerAnalytics',
    'SellerReports', 
    'SellerFinances',
    'SellerPayouts'
  ]
  
  if (requiresOnboarding.includes(to.name) && !hasCompletedSellerOnboarding(user)) {
    next({
      name: 'SellerSettings',
      query: { 
        tab: 'verification',
        message: 'Vui lòng hoàn tất xác thực tài khoản seller',
        redirect: to.fullPath
      }
    })
    return
  }
  
  next()
}


export const productOwnershipGuard = async (to, from, next) => {
  const authStore = useAuthStore()
  const sellerStore = useSellerStore()
  
  if (!to.params.id) {
    next()
    return
  }
  
  try {
    const product = await sellerStore.getProductById(to.params.id)
    
    if (!product) {
      next({ name: 'NotFound' })
      return
    }
    
    if (product.sellerId !== authStore.user.id && !authStore.isAdmin) {
      next({ 
        name: 'Forbidden',
        query: { 
          reason: 'product_access_denied',
          message: 'Bạn không có quyền truy cập sản phẩm này'
        }
      })
      return
    }
    
    next()
  } catch (error) {
    console.error('Product ownership check failed:', error)
    next({ name: 'ServerError' })
  }
}


export const orderOwnershipGuard = async (to, from, next) => {
  const authStore = useAuthStore()
  const sellerStore = useSellerStore()
  
  if (!to.params.id) {
    next()
    return
  }
  
  try {
    const order = await sellerStore.getOrderById(to.params.id)
    
    if (!order) {
      next({ name: 'NotFound' })
      return
    }
    
    const hasSellerProducts = order.items.some(item => 
      item.sellerId === authStore.user.id
    )
    
    if (!hasSellerProducts && !authStore.isAdmin) {
      next({ 
        name: 'Forbidden',
        query: { 
          reason: 'order_access_denied',
          message: 'Bạn không có quyền truy cập đơn hàng này'
        }
      })
      return
    }
    
    next()
  } catch (error) {
    console.error('Order ownership check failed:', error)
    next({ name: 'ServerError' })
  }
}


export const featureGuard = (requiredFeatures = []) => {
  return (to, from, next) => {
    const authStore = useAuthStore()
    const user = authStore.user
    
    const hasFeatures = requiredFeatures.every(feature => {
      switch (feature) {
        case 'analytics':
          return user.sellerFeatures?.analytics !== false
        case 'reports':
          return user.sellerFeatures?.reports !== false
        case 'bulk_operations':
          return user.sellerFeatures?.bulkOperations !== false
        case 'advanced_marketing':
          return user.sellerFeatures?.marketing !== false
        case 'financial_management':
          return user.sellerFeatures?.financials !== false
        default:
          return true
      }
    })
    
    if (!hasFeatures) {
      next({
        name: 'SellerDashboard',
        query: { 
          message: 'Tính năng này chưa được kích hoạt cho tài khoản của bạn',
          upgrade: 'true'
        }
      })
      return
    }
    
    next()
  }
}

export const rateLimitGuard = (maxRequests = 60) => {
  const requestCounts = new Map()
  
  return (to, from, next) => {
    const userId = useAuthStore().user?.id
    if (!userId) {
      next()
      return
    }
    
    const now = Date.now()
    const windowStart = Math.floor(now / 60000) * 60000 // 1-minute window
    const key = `${userId}-${windowStart}`
    
    const currentCount = requestCounts.get(key) || 0
    
    if (currentCount >= maxRequests) {
      next({
        name: 'ServerError',
        query: { 
          reason: 'rate_limit_exceeded',
          message: 'Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút.'
        }
      })
      return
    }
    
    requestCounts.set(key, currentCount + 1)
    
    setTimeout(() => {
      requestCounts.delete(key)
    }, 60000)
    
    next()
  }
}


export const maintenanceGuard = (to, from, next) => {
  const isMaintenanceMode = import.meta.env.VITE_SELLER_MAINTENANCE === 'true'
  
  if (isMaintenanceMode && to.path.startsWith('/seller/')) {
    const authStore = useAuthStore()
    
    if (!authStore.isAdmin) {
      next({
        name: 'Home',
        query: { 
          maintenance: 'true',
          message: 'Seller Dashboard đang được bảo trì. Vui lòng thử lại sau.'
        }
      })
      return
    }
  }
  
  next()
}

export const browserSupportGuard = (to, from, next) => {
  const requiredFeatures = [
    'localStorage',
    'sessionStorage',
    'fetch',
    'Promise',
    'URLSearchParams'
  ]
  
  const unsupportedFeatures = requiredFeatures.filter(feature => {
    try {
      return !window[feature]
    } catch {
      return true
    }
  })
  
  if (unsupportedFeatures.length > 0 && to.path.startsWith('/seller/')) {
    next({
      name: 'Home',
      query: { 
        browser_unsupported: 'true',
        message: 'Trình duyệt của bạn không hỗ trợ Seller Dashboard. Vui lòng cập nhật trình duyệt.'
      }
    })
    return
  }
  
  next()
}


export const preloadGuard = (requiredData = []) => {
  return async (to, from, next) => {
    const sellerStore = useSellerStore()
    
    try {
      const promises = requiredData.map(dataType => {
        switch (dataType) {
          case 'dashboard':
            return sellerStore.fetchDashboardStats()
          case 'products':
            return sellerStore.fetchProducts()
          case 'orders':
            return sellerStore.loadOrders()
          case 'analytics':
            return sellerStore.fetchAnalytics()
          default:
            return Promise.resolve()
        }
      })
      
      await Promise.all(promises)
      next()
    } catch (error) {
      console.error('Data preloading failed:', error)
      next()
    }
  }
}


export const combineGuards = (guards = []) => {
  return (to, from, next) => {
    let currentIndex = 0
    
    const runNextGuard = () => {
      if (currentIndex >= guards.length) {
        next()
        return
      }
      
      const guard = guards[currentIndex++]
      guard(to, from, (result) => {
        if (result === undefined || result === true) {
          runNextGuard()
        } else {
          next(result)
        }
      })
    }
    
    runNextGuard()
  }
}

export const guardConfigs = {
  seller: sellerGuard,
  
  productManagement: combineGuards([
    sellerGuard,
    preloadGuard(['products'])
  ]),
  
  productEdit: combineGuards([
    sellerGuard,
    productOwnershipGuard
  ]),
  
  orderManagement: combineGuards([
    sellerGuard,
    preloadGuard(['orders'])
  ]),
  
  orderDetail: combineGuards([
    sellerGuard,
    orderOwnershipGuard
  ]),
  
  analytics: combineGuards([
    sellerGuard,
    featureGuard(['analytics']),
    rateLimitGuard(30), 
    preloadGuard(['analytics'])
  ]),
  
  reports: combineGuards([
    sellerGuard,
    featureGuard(['reports']),
    rateLimitGuard(10) 
  ]),
  
  financial: combineGuards([
    sellerGuard,
    featureGuard(['financial_management'])
  ]),
  
  bulkOperations: combineGuards([
    sellerGuard,
    featureGuard(['bulk_operations']),
    rateLimitGuard(20)
  ]),
  
  marketing: combineGuards([
    sellerGuard,
    featureGuard(['advanced_marketing'])
  ])
}

export const globalGuards = [
  maintenanceGuard,
  browserSupportGuard
]

export default {
  sellerGuard,
  productOwnershipGuard,
  orderOwnershipGuard,
  featureGuard,
  rateLimitGuard,
  maintenanceGuard,
  browserSupportGuard,
  preloadGuard,
  combineGuards,
  guardConfigs,
  globalGuards
}