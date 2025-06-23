import { defineStore } from 'pinia'
import { adminAPI } from '@/services/api'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    // Dashboard data
    dashboardStats: {
      users: { total: 0, active: 0, change: 0 },
      products: { total: 0, active: 0, change: 0 },
      orders: { total: 0, pending: 0, change: 0 },
      revenue: { total: 0, thisMonth: 0, change: 0 }
    },
    recentOrders: [],
    lowStockProducts: [],
    
    // Users management
    users: [],
    userStats: {
      total: 0,
      active: 0,
      blocked: 0,
      newThisMonth: 0
    },
    
    // Products management
    products: [],
    productStats: {
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0
    },
    
    // Orders management
    orders: [],
    orderStats: {
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0
    },
    
    // Analytics
    analytics: {
      salesChart: [],
      topProducts: [],
      topCategories: [],
      userGrowth: []
    },
    
    // Loading states
    loading: {
      dashboard: false,
      users: false,
      products: false,
      orders: false,
      analytics: false
    },
    
    // Error states
    errors: {
      dashboard: null,
      users: null,
      products: null,
      orders: null,
      analytics: null
    },
    
    // Filters and pagination
    filters: {
      users: {
        status: '',
        role: '',
        search: '',
        dateRange: ''
      },
      products: {
        category: '',
        status: '',
        search: '',
        sortBy: 'newest'
      },
      orders: {
        status: '',
        dateRange: '',
        search: '',
        sortBy: 'newest'
      }
    },
    
    pagination: {
      users: { page: 1, limit: 20, total: 0 },
      products: { page: 1, limit: 20, total: 0 },
      orders: { page: 1, limit: 20, total: 0 }
    }
  }),

  getters: {
    // Dashboard getters
    totalRevenue: (state) => state.dashboardStats.revenue.total,
    revenueGrowth: (state) => state.dashboardStats.revenue.change,
    
    // Users getters
    filteredUsers: (state) => {
      let filtered = [...state.users]
      const filters = state.filters.users
      
      if (filters.status) {
        filtered = filtered.filter(user => 
          filters.status === 'active' ? user.enabled : !user.enabled
        )
      }
      
      if (filters.role) {
        filtered = filtered.filter(user => user.role === filters.role)
      }
      
      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(user =>
          user.firstName.toLowerCase().includes(search) ||
          user.lastName.toLowerCase().includes(search) ||
          user.email.toLowerCase().includes(search)
        )
      }
      
      return filtered
    },
    
    // Products getters
    filteredProducts: (state) => {
      let filtered = [...state.products]
      const filters = state.filters.products
      
      if (filters.category) {
        filtered = filtered.filter(product => product.category === filters.category)
      }
      
      if (filters.status) {
        filtered = filtered.filter(product => 
          filters.status === 'active' ? product.active : !product.active
        )
      }
      
      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(product =>
          product.name.toLowerCase().includes(search) ||
          product.description.toLowerCase().includes(search)
        )
      }
      
      return filtered
    },
    
    // Orders getters
    filteredOrders: (state) => {
      let filtered = [...state.orders]
      const filters = state.filters.orders
      
      if (filters.status) {
        filtered = filtered.filter(order => order.status === filters.status)
      }
      
      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(order =>
          order.id.toLowerCase().includes(search) ||
          order.userEmail.toLowerCase().includes(search)
        )
      }
      
      return filtered
    },
    
    // Stats getters
    pendingOrdersCount: (state) => state.orderStats.pending,
    lowStockProductsCount: (state) => state.productStats.lowStock,
    blockedUsersCount: (state) => state.userStats.blocked
  },

  actions: {
    // Dashboard actions
    async loadDashboard() {
      this.loading.dashboard = true
      this.errors.dashboard = null
      
      try {
        const response = await adminAPI.getDashboard()
        this.dashboardStats = response.data.stats
        this.recentOrders = response.data.recentOrders || []
        this.lowStockProducts = response.data.lowStockProducts || []
      } catch (error) {
        this.errors.dashboard = error.response?.data?.message || 'Không thể tải dữ liệu dashboard'
        console.error('Load dashboard error:', error)
      } finally {
        this.loading.dashboard = false
      }
    },

    // Users management actions
    async loadUsers() {
      this.loading.users = true
      this.errors.users = null
      
      try {
        const response = await adminAPI.getUsers()
        this.users = response.data.users || []
        this.userStats = response.data.stats || this.userStats
        this.pagination.users.total = response.data.total || 0
      } catch (error) {
        this.errors.users = error.response?.data?.message || 'Không thể tải danh sách người dùng'
        console.error('Load users error:', error)
      } finally {
        this.loading.users = false
      }
    },

    async toggleUserStatus(userId) {
      try {
        const response = await adminAPI.toggleUserStatus(userId)
        
        // Update user in local state
        const userIndex = this.users.findIndex(user => user.id === userId)
        if (userIndex !== -1) {
          this.users[userIndex] = { ...this.users[userIndex], ...response.data }
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể cập nhật trạng thái người dùng'
        throw new Error(errorMsg)
      }
    },

    // Products management actions
    async loadProducts() {
      this.loading.products = true
      this.errors.products = null
      
      try {
        const response = await adminAPI.getProducts()
        this.products = response.data.products || []
        this.productStats = response.data.stats || this.productStats
        this.pagination.products.total = response.data.total || 0
      } catch (error) {
        this.errors.products = error.response?.data?.message || 'Không thể tải danh sách sản phẩm'
        console.error('Load products error:', error)
      } finally {
        this.loading.products = false
      }
    },

    async toggleProductStatus(productId) {
      try {
        const response = await adminAPI.toggleProductStatus(productId)
        
        // Update product in local state
        const productIndex = this.products.findIndex(product => product.id === productId)
        if (productIndex !== -1) {
          this.products[productIndex] = { ...this.products[productIndex], ...response.data }
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể cập nhật trạng thái sản phẩm'
        throw new Error(errorMsg)
      }
    },

    async deleteProduct(productId) {
      try {
        await adminAPI.deleteProduct(productId)
        
        // Remove product from local state
        this.products = this.products.filter(product => product.id !== productId)
        this.productStats.total -= 1
        
        return true
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể xóa sản phẩm'
        throw new Error(errorMsg)
      }
    },

    // Orders management actions
    async loadOrders() {
      this.loading.orders = true
      this.errors.orders = null
      
      try {
        const response = await adminAPI.getOrders()
        this.orders = response.data.orders || []
        this.orderStats = response.data.stats || this.orderStats
        this.pagination.orders.total = response.data.total || 0
      } catch (error) {
        this.errors.orders = error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
        console.error('Load orders error:', error)
      } finally {
        this.loading.orders = false
      }
    },

    async updateOrderStatus(orderId, status) {
      try {
        const response = await adminAPI.updateOrderStatus(orderId, status)
        
        // Update order in local state
        const orderIndex = this.orders.findIndex(order => order.id === orderId)
        if (orderIndex !== -1) {
          this.orders[orderIndex] = { ...this.orders[orderIndex], ...response.data }
        }
        
        return response.data
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng'
        throw new Error(errorMsg)
      }
    },

    // Analytics actions
    async loadAnalytics(type = 'sales') {
      this.loading.analytics = true
      this.errors.analytics = null
      
      try {
        const response = await adminAPI.getAnalytics(type)
        
        switch (type) {
          case 'sales':
            this.analytics.salesChart = response.data
            break
          case 'products':
            this.analytics.topProducts = response.data
            break
          case 'categories':
            this.analytics.topCategories = response.data
            break
          case 'users':
            this.analytics.userGrowth = response.data
            break
        }
      } catch (error) {
        this.errors.analytics = error.response?.data?.message || 'Không thể tải dữ liệu phân tích'
        console.error('Load analytics error:', error)
      } finally {
        this.loading.analytics = false
      }
    },

    // Filter actions
    setUserFilter(filterType, value) {
      this.filters.users[filterType] = value
      this.pagination.users.page = 1 // Reset to first page
    },

    setProductFilter(filterType, value) {
      this.filters.products[filterType] = value
      this.pagination.products.page = 1 // Reset to first page
    },

    setOrderFilter(filterType, value) {
      this.filters.orders[filterType] = value
      this.pagination.orders.page = 1 // Reset to first page
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

    // Reset filters
    resetUserFilters() {
      this.filters.users = {
        status: '',
        role: '',
        search: '',
        dateRange: ''
      }
      this.pagination.users.page = 1
    },

    resetProductFilters() {
      this.filters.products = {
        category: '',
        status: '',
        search: '',
        sortBy: 'newest'
      }
      this.pagination.products.page = 1
    },

    resetOrderFilters() {
      this.filters.orders = {
        status: '',
        dateRange: '',
        search: '',
        sortBy: 'newest'
      }
      this.pagination.orders.page = 1
    }
  }
})