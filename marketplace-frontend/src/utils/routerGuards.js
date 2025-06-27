// utils/routerGuards.js - Enhanced với seller access
import { useAuthStore } from '@/stores/auth'
import { useSellerStore } from '@/stores/seller'

// 🔧 SIMPLIFIED: Seller access check - chỉ cần USER/ADMIN role
export const canAccessSellerFeatures = (user) => {
  if (!user) {
    console.log('❌ No user found')
    return false
  }
  
  // Simplified check - bất kỳ USER nào cũng có thể trở thành seller
  const canAccess = (user.role === 'USER' || user.role === 'ADMIN')
  
  console.log('🔍 Seller access check:', {
    userId: user.id,
    role: user.role,
    canAccess: canAccess
  })
  
  return canAccess
}

// 🔧 OPTIONAL: Seller onboarding check (có thể bỏ qua trong development)
export const hasCompletedSellerOnboarding = (user) => {
  if (!user) return false
  
  // Trong development, auto-approve mọi user
  // Trong production, có thể check verification status
  const isCompleted = true // hoặc user.sellerProfile?.isVerified
  
  console.log('🎓 Seller onboarding check:', {
    userId: user.id,
    isCompleted: isCompleted
  })
  
  return isCompleted
}

// 🚀 MAIN: Seller Guard - kiểm tra quyền truy cập seller features
export const sellerGuard = (to, from, next) => {
  const authStore = useAuthStore()
  const user = authStore.user
  
  console.log('🛡️ Seller Guard Triggered:', {
    path: to.path,
    isAuthenticated: authStore.isAuthenticated,
    userRole: user?.role,
    userId: user?.id
  })
  
  // Step 1: Check authentication
  if (!authStore.isAuthenticated) {
    console.log('❌ Not authenticated, redirecting to login')
    next({
      name: 'Login',
      query: { redirect: to.fullPath, reason: 'authentication_required' }
    })
    return
  }
  
  // Step 2: Check seller access permission
  if (!canAccessSellerFeatures(user)) {
    console.log('❌ No seller access, redirecting to home')
    next({
      name: 'Home',
      query: { 
        message: 'Bạn cần tài khoản USER để truy cập tính năng seller',
        type: 'warning'
      }
    })
    return
  }
  
  // Step 3: Check onboarding (optional - có thể skip)
  const requiresOnboarding = [
    'SellerAnalytics',
    'SellerReports', 
    'SellerFinances',
    'SellerPayouts'
  ]
  
  if (requiresOnboarding.includes(to.name) && !hasCompletedSellerOnboarding(user)) {
    console.log('⚠️ Onboarding required, redirecting to settings')
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
  
  console.log('✅ Seller access granted!')
  next()
}

// 🔐 Product ownership guard - chỉ owner hoặc admin mới được truy cập
export const productOwnershipGuard = async (to, from, next) => {
  const authStore = useAuthStore()
  const sellerStore = useSellerStore()
  
  if (!to.params.id) {
    next()
    return
  }
  
  try {
    console.log('🔍 Checking product ownership for ID:', to.params.id)
    
    // Load product data
    const product = await sellerStore.getProductById(to.params.id)
    
    if (!product) {
      console.log('❌ Product not found')
      next({ name: 'NotFound' })
      return
    }
    
    // Check ownership
    const isOwner = product.sellerId === authStore.user.id
    const isAdmin = authStore.isAdmin
    
    if (!isOwner && !isAdmin) {
      console.log('❌ Not product owner or admin')
      next({ 
        name: 'SellerProducts',
        query: { 
          message: 'Bạn không có quyền truy cập sản phẩm này'
        }
      })
      return
    }
    
    console.log('✅ Product access granted')
    next()
    
  } catch (error) {
    console.error('💥 Product ownership check failed:', error)
    next({ 
      name: 'SellerProducts',
      query: { 
        message: 'Lỗi kiểm tra quyền truy cập sản phẩm'
      }
    })
  }
}

// 🔐 Order ownership guard - chỉ seller có sản phẩm trong order mới được truy cập
export const orderOwnershipGuard = async (to, from, next) => {
  const authStore = useAuthStore()
  const sellerStore = useSellerStore()
  
  if (!to.params.id) {
    next()
    return
  }
  
  try {
    console.log('🔍 Checking order ownership for ID:', to.params.id)
    
    const order = await sellerStore.getOrderById(to.params.id)
    
    if (!order) {
      console.log('❌ Order not found')
      next({ name: 'NotFound' })
      return
    }
    
    // Check if seller has products in this order
    const hasSellerProducts = order.items?.some(item => 
      item.sellerId === authStore.user.id
    )
    
    const isAdmin = authStore.isAdmin
    
    if (!hasSellerProducts && !isAdmin) {
      console.log('❌ No seller products in this order')
      next({ 
        name: 'SellerOrders',
        query: { 
          message: 'Bạn không có quyền truy cập đơn hàng này'
        }
      })
      return
    }
    
    console.log('✅ Order access granted')
    next()
    
  } catch (error) {
    console.error('💥 Order ownership check failed:', error)
    next({ 
      name: 'SellerOrders',
      query: { 
        message: 'Lỗi kiểm tra quyền truy cập đơn hàng'
      }
    })
  }
}

// 🎛️ Feature guard - kiểm tra tính năng được enable
export const featureGuard = (requiredFeatures = []) => {
  return (to, from, next) => {
    const authStore = useAuthStore()
    const user = authStore.user
    
    // Trong development, auto-enable tất cả features
    const allFeaturesEnabled = true
    
    if (allFeaturesEnabled) {
      console.log('✅ All seller features enabled (development mode)')
      next()
      return
    }
    
    // Production logic:
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

// 🚨 Rate limiting guard (optional)
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
      console.log('🚨 Rate limit exceeded for user:', userId)
      next({
        name: 'SellerDashboard',
        query: { 
          message: 'Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút.',
          type: 'error'
        }
      })
      return
    }
    
    requestCounts.set(key, currentCount + 1)
    
    // Clean up after 1 minute
    setTimeout(() => {
      requestCounts.delete(key)
    }, 60000)
    
    next()
  }
}

// 🔧 Combine multiple guards
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

// 📋 Guard configurations for different route types
export const guardConfigs = {
  // Basic seller access
  seller: sellerGuard,
  
  // Product management with ownership
  productManagement: combineGuards([
    sellerGuard
  ]),
  
  // Product editing with ownership check
  productEdit: combineGuards([
    sellerGuard,
    productOwnershipGuard
  ]),
  
  // Order management
  orderManagement: combineGuards([
    sellerGuard
  ]),
  
  // Order detail with ownership
  orderDetail: combineGuards([
    sellerGuard,
    orderOwnershipGuard
  ]),
  
  // Analytics with rate limiting
  analytics: combineGuards([
    sellerGuard,
    featureGuard(['analytics']),
    rateLimitGuard(30)
  ]),
  
  // Reports with restrictions
  reports: combineGuards([
    sellerGuard,
    featureGuard(['reports']),
    rateLimitGuard(10)
  ])
}

// 🎯 Main exports
export default {
  sellerGuard,
  productOwnershipGuard,
  orderOwnershipGuard,
  featureGuard,
  rateLimitGuard,
  combineGuards,
  guardConfigs,
  canAccessSellerFeatures,
  hasCompletedSellerOnboarding
}