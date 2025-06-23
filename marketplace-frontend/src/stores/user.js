import { defineStore } from 'pinia'
import { profileAPI, orderAPI } from '@/services/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    // Profile data
    profile: {
      id: null,
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      birthday: '',
      address: '',
      avatar: '',
      isVip: false,
      isVerified: false,
      totalOrders: 0,
      totalSpent: 0,
      wishlistCount: 0,
      reviewCount: 0,
      lastPasswordChange: null,
      twoFactorEnabled: false,
      createdAt: null
    },
    
    // User orders
    orders: [],
    orderHistory: [],
    
    // User preferences
    preferences: {
      theme: 'space', // space, light, dark
      language: 'vi',
      currency: 'VND',
      notifications: {
        email: true,
        push: true,
        sms: false,
        orderUpdates: true,
        promotions: true,
        newProducts: false
      },
      privacy: {
        showProfile: true,
        showOrders: false,
        showReviews: true
      }
    },
    
    // Wishlist
    wishlist: [],
    
    // Recently viewed products
    recentlyViewed: [],
    
    // User addresses
    addresses: [],
    defaultAddressId: null,
    
    // Payment methods
    paymentMethods: [],
    defaultPaymentMethodId: null,
    
    // User statistics
    stats: {
      monthlySpending: 0,
      averageOrderValue: 0,
      favoriteCategory: '',
      loyaltyPoints: 0,
      membershipLevel: 'Bronze', // Bronze, Silver, Gold, Platinum
      nextLevelPoints: 0
    },
    
    // Loading states
    loading: {
      profile: false,
      orders: false,
      wishlist: false,
      addresses: false,
      paymentMethods: false,
      preferences: false
    },
    
    // Error states
    errors: {
      profile: null,
      orders: null,
      wishlist: null,
      addresses: null,
      paymentMethods: null,
      preferences: null
    }
  }),

  getters: {
    fullName: (state) => `${state.profile.firstName} ${state.profile.lastName}`.trim(),
    
    isProfileComplete: (state) => {
      const required = ['firstName', 'lastName', 'email', 'phone']
      return required.every(field => state.profile[field])
    },
    
    membershipProgress: (state) => {
      const levels = {
        'Bronze': { min: 0, max: 1000 },
        'Silver': { min: 1000, max: 5000 },
        'Gold': { min: 5000, max: 15000 },
        'Platinum': { min: 15000, max: Infinity }
      }
      const current = levels[state.stats.membershipLevel]
      const progress = current.max === Infinity ? 100 : 
        ((state.stats.loyaltyPoints - current.min) / (current.max - current.min)) * 100
      return Math.min(progress, 100)
    },
    
    recentOrders: (state) => {
      return state.orders.slice(0, 5).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    },
    
    pendingOrders: (state) => {
      return state.orders.filter(order => ['PENDING', 'PROCESSING', 'SHIPPED'].includes(order.status))
    },
    
    completedOrders: (state) => {
      return state.orders.filter(order => order.status === 'DELIVERED')
    },
    
    defaultAddress: (state) => {
      return state.addresses.find(addr => addr.id === state.defaultAddressId) || state.addresses[0]
    },
    
    defaultPaymentMethod: (state) => {
      return state.paymentMethods.find(pm => pm.id === state.defaultPaymentMethodId) || state.paymentMethods[0]
    },
    
    wishlistItems: (state) => state.wishlist,
    
    recentProducts: (state) => state.recentlyViewed.slice(0, 10)
  },

  actions: {
    // Profile actions
    async loadProfile() {
      this.loading.profile = true
      this.errors.profile = null
      
      try {
        const response = await profileAPI.getProfile()
        this.profile = { ...this.profile, ...response.data }
        
        // Load related data
        await Promise.all([
          this.loadUserStats(),
          this.loadPreferences()
        ])
      } catch (error) {
        this.errors.profile = error.response?.data?.message || 'Không thể tải thông tin profile'
        console.error('Load profile error:', error)
      } finally {
        this.loading.profile = false
      }
    },

    async updateProfile(profileData) {
      this.loading.profile = true
      this.errors.profile = null
      
      try {
        const response = await profileAPI.updateProfile(profileData)
        this.profile = { ...this.profile, ...response.data }
        return response.data
      } catch (error) {
        this.errors.profile = error.response?.data?.message || 'Không thể cập nhật profile'
        throw error
      } finally {
        this.loading.profile = false
      }
    },

    async uploadAvatar(file) {
      this.loading.profile = true
      this.errors.profile = null
      
      try {
        const formData = new FormData()
        formData.append('avatar', file)
        
        const response = await profileAPI.uploadAvatar(formData)
        this.profile.avatar = response.data.avatar
        return response.data
      } catch (error) {
        this.errors.profile = error.response?.data?.message || 'Không thể tải ảnh đại diện'
        throw error
      } finally {
        this.loading.profile = false
      }
    },

    async changePassword(passwordData) {
      this.loading.profile = true
      this.errors.profile = null
      
      try {
        await profileAPI.changePassword(passwordData)
        this.profile.lastPasswordChange = new Date().toISOString()
        return true
      } catch (error) {
        this.errors.profile = error.response?.data?.message || 'Không thể đổi mật khẩu'
        throw error
      } finally {
        this.loading.profile = false
      }
    },

    // Orders actions
    async loadOrders() {
      this.loading.orders = true
      this.errors.orders = null
      
      try {
        const response = await orderAPI.getMyOrders()
        this.orders = response.data.orders || []
        this.orderHistory = [...this.orders]
      } catch (error) {
        this.errors.orders = error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
        console.error('Load orders error:', error)
      } finally {
        this.loading.orders = false
      }
    },

    async cancelOrder(orderId) {
      try {
        const response = await orderAPI.cancel(orderId)
        
        // Update order in local state
        const orderIndex = this.orders.findIndex(order => order.id === orderId)
        if (orderIndex !== -1) {
          this.orders[orderIndex] = { ...this.orders[orderIndex], ...response.data }
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể hủy đơn hàng'
        throw new Error(errorMsg)
      }
    },

    // Wishlist actions
    async loadWishlist() {
      this.loading.wishlist = true
      this.errors.wishlist = null
      
      try {
        const response = await profileAPI.getWishlist()
        this.wishlist = response.data.items || []
      } catch (error) {
        this.errors.wishlist = error.response?.data?.message || 'Không thể tải danh sách yêu thích'
        console.error('Load wishlist error:', error)
      } finally {
        this.loading.wishlist = false
      }
    },

    async addToWishlist(productId) {
      try {
        const response = await profileAPI.addToWishlist(productId)
        
        // Add to local state if not exists
        if (!this.wishlist.some(item => item.productId === productId)) {
          this.wishlist.push(response.data)
        }
        
        this.profile.wishlistCount = this.wishlist.length
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể thêm vào danh sách yêu thích'
        throw new Error(errorMsg)
      }
    },

    async removeFromWishlist(productId) {
      try {
        await profileAPI.removeFromWishlist(productId)
        
        // Remove from local state
        this.wishlist = this.wishlist.filter(item => item.productId !== productId)
        this.profile.wishlistCount = this.wishlist.length
        
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể xóa khỏi danh sách yêu thích'
        throw new Error(errorMsg)
      }
    },

    // Addresses actions
    async loadAddresses() {
      this.loading.addresses = true
      this.errors.addresses = null
      
      try {
        const response = await profileAPI.getAddresses()
        this.addresses = response.data.addresses || []
        this.defaultAddressId = response.data.defaultAddressId
      } catch (error) {
        this.errors.addresses = error.response?.data?.message || 'Không thể tải danh sách địa chỉ'
        console.error('Load addresses error:', error)
      } finally {
        this.loading.addresses = false
      }
    },

    async addAddress(addressData) {
      try {
        const response = await profileAPI.addAddress(addressData)
        this.addresses.push(response.data)
        
        // Set as default if it's the first address
        if (this.addresses.length === 1) {
          this.defaultAddressId = response.data.id
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể thêm địa chỉ'
        throw new Error(errorMsg)
      }
    },

    async updateAddress(addressId, addressData) {
      try {
        const response = await profileAPI.updateAddress(addressId, addressData)
        
        // Update in local state
        const addressIndex = this.addresses.findIndex(addr => addr.id === addressId)
        if (addressIndex !== -1) {
          this.addresses[addressIndex] = { ...this.addresses[addressIndex], ...response.data }
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể cập nhật địa chỉ'
        throw new Error(errorMsg)
      }
    },

    async deleteAddress(addressId) {
      try {
        await profileAPI.deleteAddress(addressId)
        
        // Remove from local state
        this.addresses = this.addresses.filter(addr => addr.id !== addressId)
        
        // Update default if deleted address was default
        if (this.defaultAddressId === addressId) {
          this.defaultAddressId = this.addresses[0]?.id || null
        }
        
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể xóa địa chỉ'
        throw new Error(errorMsg)
      }
    },

    async setDefaultAddress(addressId) {
      try {
        await profileAPI.setDefaultAddress(addressId)
        this.defaultAddressId = addressId
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể đặt địa chỉ mặc định'
        throw new Error(errorMsg)
      }
    },

    // Payment methods actions
    async loadPaymentMethods() {
      this.loading.paymentMethods = true
      this.errors.paymentMethods = null
      
      try {
        const response = await profileAPI.getPaymentMethods()
        this.paymentMethods = response.data.paymentMethods || []
        this.defaultPaymentMethodId = response.data.defaultPaymentMethodId
      } catch (error) {
        this.errors.paymentMethods = error.response?.data?.message || 'Không thể tải phương thức thanh toán'
        console.error('Load payment methods error:', error)
      } finally {
        this.loading.paymentMethods = false
      }
    },

    async addPaymentMethod(paymentData) {
      try {
        const response = await profileAPI.addPaymentMethod(paymentData)
        this.paymentMethods.push(response.data)
        
        // Set as default if it's the first payment method
        if (this.paymentMethods.length === 1) {
          this.defaultPaymentMethodId = response.data.id
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể thêm phương thức thanh toán'
        throw new Error(errorMsg)
      }
    },

    async deletePaymentMethod(paymentMethodId) {
      try {
        await profileAPI.deletePaymentMethod(paymentMethodId)
        
        // Remove from local state
        this.paymentMethods = this.paymentMethods.filter(pm => pm.id !== paymentMethodId)
        
        // Update default if deleted payment method was default
        if (this.defaultPaymentMethodId === paymentMethodId) {
          this.defaultPaymentMethodId = this.paymentMethods[0]?.id || null
        }
        
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể xóa phương thức thanh toán'
        throw new Error(errorMsg)
      }
    },

    async setDefaultPaymentMethod(paymentMethodId) {
      try {
        await profileAPI.setDefaultPaymentMethod(paymentMethodId)
        this.defaultPaymentMethodId = paymentMethodId
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể đặt phương thức thanh toán mặc định'
        throw new Error(errorMsg)
      }
    },

    // Preferences actions
    async loadPreferences() {
      this.loading.preferences = true
      this.errors.preferences = null
      
      try {
        const response = await profileAPI.getPreferences()
        this.preferences = { ...this.preferences, ...response.data }
      } catch (error) {
        this.errors.preferences = error.response?.data?.message || 'Không thể tải cài đặt'
        console.error('Load preferences error:', error)
      } finally {
        this.loading.preferences = false
      }
    },

    async updatePreferences(preferencesData) {
      this.loading.preferences = true
      this.errors.preferences = null
      
      try {
        const response = await profileAPI.updatePreferences(preferencesData)
        this.preferences = { ...this.preferences, ...response.data }
        return response.data
      } catch (error) {
        this.errors.preferences = error.response?.data?.message || 'Không thể cập nhật cài đặt'
        throw error
      } finally {
        this.loading.preferences = false
      }
    },

    // Recently viewed products
    addToRecentlyViewed(product) {
      // Remove if already exists
      this.recentlyViewed = this.recentlyViewed.filter(p => p.id !== product.id)
      
      // Add to beginning
      this.recentlyViewed.unshift(product)
      
      // Keep only last 20 items
      this.recentlyViewed = this.recentlyViewed.slice(0, 20)
      
      // Save to localStorage
      try {
        localStorage.setItem('recentlyViewed', JSON.stringify(this.recentlyViewed))
      } catch (error) {
        console.warn('Could not save recently viewed to localStorage:', error)
      }
    },

    loadRecentlyViewed() {
      try {
        const saved = localStorage.getItem('recentlyViewed')
        if (saved) {
          this.recentlyViewed = JSON.parse(saved)
        }
      } catch (error) {
        console.warn('Could not load recently viewed from localStorage:', error)
        this.recentlyViewed = []
      }
    },

    clearRecentlyViewed() {
      this.recentlyViewed = []
      try {
        localStorage.removeItem('recentlyViewed')
      } catch (error) {
        console.warn('Could not clear recently viewed from localStorage:', error)
      }
    },

    // User statistics
    async loadUserStats() {
      try {
        const response = await profileAPI.getUserStats()
        this.stats = { ...this.stats, ...response.data }
      } catch (error) {
        console.error('Load user stats error:', error)
      }
    },

    // Two-factor authentication
    async enableTwoFactor() {
      try {
        const response = await profileAPI.enableTwoFactor()
        this.profile.twoFactorEnabled = true
        return response.data // Contains QR code, backup codes, etc.
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể bật xác thực 2 bước'
        throw new Error(errorMsg)
      }
    },

    async disableTwoFactor(verificationCode) {
      try {
        await profileAPI.disableTwoFactor({ code: verificationCode })
        this.profile.twoFactorEnabled = false
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể tắt xác thực 2 bước'
        throw new Error(errorMsg)
      }
    },

    async verifyTwoFactor(code) {
      try {
        const response = await profileAPI.verifyTwoFactor({ code })
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Mã xác thực không đúng'
        throw new Error(errorMsg)
      }
    },

    // Account management
    async requestAccountDeletion() {
      try {
        const response = await profileAPI.requestAccountDeletion()
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể yêu cầu xóa tài khoản'
        throw new Error(errorMsg)
      }
    },

    async cancelAccountDeletion() {
      try {
        const response = await profileAPI.cancelAccountDeletion()
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể hủy yêu cầu xóa tài khoản'
        throw new Error(errorMsg)
      }
    },

    async exportUserData() {
      try {
        const response = await profileAPI.exportUserData()
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể xuất dữ liệu người dùng'
        throw new Error(errorMsg)
      }
    },

    // Notifications
    async getNotifications() {
      try {
        const response = await profileAPI.getNotifications()
        return response.data
      } catch (error) {
        console.error('Get notifications error:', error)
        return []
      }
    },

    async markNotificationAsRead(notificationId) {
      try {
        await profileAPI.markNotificationAsRead(notificationId)
        return true
      } catch (error) {
        console.error('Mark notification as read error:', error)
        return false
      }
    },

    async markAllNotificationsAsRead() {
      try {
        await profileAPI.markAllNotificationsAsRead()
        return true
      } catch (error) {
        console.error('Mark all notifications as read error:', error)
        return false
      }
    },

    // Loyalty program
    async getLoyaltyPoints() {
      try {
        const response = await profileAPI.getLoyaltyPoints()
        this.stats.loyaltyPoints = response.data.points
        this.stats.membershipLevel = response.data.level
        this.stats.nextLevelPoints = response.data.nextLevelPoints
        return response.data
      } catch (error) {
        console.error('Get loyalty points error:', error)
      }
    },

    async redeemLoyaltyPoints(pointsToRedeem, rewardType) {
      try {
        const response = await profileAPI.redeemLoyaltyPoints({
          points: pointsToRedeem,
          rewardType
        })
        
        // Update points
        this.stats.loyaltyPoints -= pointsToRedeem
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể đổi điểm thưởng'
        throw new Error(errorMsg)
      }
    },

    // Clear errors
    clearError(type) {
      if (this.errors[type]) {
        this.errors[type] = null
      }
    },

    clearAllErrors() {
      Object.keys(this.errors).forEach(key => {
        this.errors[key] = null
      })
    },

    // Reset store
    reset() {
      this.profile = {
        id: null,
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        birthday: '',
        address: '',
        avatar: '',
        isVip: false,
        isVerified: false,
        totalOrders: 0,
        totalSpent: 0,
        wishlistCount: 0,
        reviewCount: 0,
        lastPasswordChange: null,
        twoFactorEnabled: false,
        createdAt: null
      }
      
      this.orders = []
      this.orderHistory = []
      this.wishlist = []
      this.addresses = []
      this.paymentMethods = []
      this.defaultAddressId = null
      this.defaultPaymentMethodId = null
      
      this.clearAllErrors()
      this.clearRecentlyViewed()
    },

    // Utility methods
    isInWishlist(productId) {
      return this.wishlist.some(item => item.productId === productId)
    },

    getOrderById(orderId) {
      return this.orders.find(order => order.id === orderId)
    },

    getAddressById(addressId) {
      return this.addresses.find(address => address.id === addressId)
    },

    getPaymentMethodById(paymentMethodId) {
      return this.paymentMethods.find(pm => pm.id === paymentMethodId)
    },

    // Format methods
    formatMembershipLevel(level) {
      const levels = {
        'Bronze': '🥉 Đồng',
        'Silver': '🥈 Bạc', 
        'Gold': '🥇 Vàng',
        'Platinum': '💎 Bạch Kim'
      }
      return levels[level] || level
    },

    getNextMembershipLevel() {
      const levels = ['Bronze', 'Silver', 'Gold', 'Platinum']
      const currentIndex = levels.indexOf(this.stats.membershipLevel)
      return currentIndex < levels.length - 1 ? levels[currentIndex + 1] : null
    },

    // Initialize store
    async initialize() {
      try {
        // Load recently viewed from localStorage
        this.loadRecentlyViewed()
        
        // Load profile and related data
        await this.loadProfile()
        
        // Load additional data in parallel
        await Promise.all([
          this.loadOrders(),
          this.loadWishlist(),
          this.loadAddresses(),
          this.loadPaymentMethods(),
          this.getLoyaltyPoints()
        ])
      } catch (error) {
        console.error('User store initialization error:', error)
      }
    }
  }
})