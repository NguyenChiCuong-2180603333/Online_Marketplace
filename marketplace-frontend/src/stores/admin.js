import { defineStore } from 'pinia'
import { adminAPI } from '@/services/api'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    dashboardStats: {
      totalUsers: 0,
      activeUsers: 0,
      totalProducts: 0,
      activeProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      processingOrders: 0,
      completedOrders: 0,
      totalRevenue: 0,
      users: { total: 0, active: 0, change: 0 },
      products: { total: 0, active: 0, change: 0 },
      orders: { total: 0, pending: 0, change: 0 },
      revenue: { total: 0, thisMonth: 0, change: 0 },
    },
    recentOrders: [],
    lowStockProducts: [],

    // Users management
    users: [],
    userStats: {
      total: 0,
      active: 0,
      blocked: 0,
      newThisMonth: 0,
      admins: 0,
      vip: 0,
    },

    // Products management
    products: [],
    productStats: {
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0,
    },

    // Orders management
    orders: [],
    orderStats: {
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0,
    },

    // Analytics
    analytics: {
      salesChart: [],
      topProducts: [],
      topCategories: [],
      userGrowth: [],
    },

    // Loading states
    loading: {
      dashboard: false,
      users: false,
      products: false,
      orders: false,
      analytics: false,
    },

    // Error states
    errors: {
      dashboard: null,
      users: null,
      products: null,
      orders: null,
      analytics: null,
    },

    // Filters and pagination
    filters: {
      users: {
        status: '',
        role: '',
        search: '',
        dateRange: '',
      },
      products: {
        category: '',
        status: '',
        search: '',
        sortBy: 'newest',
      },
      orders: {
        status: '',
        dateRange: '',
        search: '',
        sortBy: 'newest',
      },
    },

    pagination: {
      users: { page: 1, limit: 20, total: 0 },
      products: { page: 1, limit: 20, total: 0 },
      orders: { page: 1, limit: 20, total: 0 },
    },
  }),

  getters: {
    // Dashboard getters
    totalRevenue: (state) => state.dashboardStats.totalRevenue || 0,
    revenueGrowth: (state) => state.dashboardStats.revenue?.change || 0,

    // Users getters
    filteredUsers: (state) => {
      let filtered = [...state.users]
      const filters = state.filters.users

      if (filters.status) {
        filtered = filtered.filter((user) =>
          filters.status === 'active' ? user.enabled : !user.enabled
        )
      }

      if (filters.role) {
        filtered = filtered.filter((user) => user.role === filters.role)
      }

      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(
          (user) =>
            user.firstName?.toLowerCase().includes(search) ||
            user.lastName?.toLowerCase().includes(search) ||
            user.email?.toLowerCase().includes(search)
        )
      }

      return filtered
    },

    // Products getters
    filteredProducts: (state) => {
      let filtered = [...state.products]
      const filters = state.filters.products

      if (filters.category) {
        filtered = filtered.filter((product) => product.category === filters.category)
      }

      if (filters.status) {
        filtered = filtered.filter((product) =>
          filters.status === 'active' ? product.active : !product.active
        )
      }

      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(
          (product) =>
            product.name?.toLowerCase().includes(search) ||
            product.description?.toLowerCase().includes(search)
        )
      }

      return filtered
    },

    // Orders getters
    filteredOrders: (state) => {
      let filtered = [...state.orders]
      const filters = state.filters.orders

      if (filters.status) {
        filtered = filtered.filter((order) => order.status === filters.status)
      }

      if (filters.search) {
        const search = filters.search.toLowerCase()
        filtered = filtered.filter(
          (order) =>
            order.id?.toLowerCase().includes(search) ||
            order.userEmail?.toLowerCase().includes(search)
        )
      }

      return filtered
    },

    // Stats getters
    pendingOrdersCount: (state) => state.dashboardStats.pendingOrders || 0,
    lowStockProductsCount: (state) => state.productStats.lowStock || 0,
    blockedUsersCount: (state) => state.userStats.blocked || 0,
  },

  actions: {
    async loadDashboard() {
      this.loading.dashboard = true
      this.errors.dashboard = null

      try {
        console.log('🔄 Loading admin dashboard...')
        const response = await adminAPI.getDashboard()
        console.log('📊 Dashboard response:', response.data)

        const backendData = response.data

        this.dashboardStats = {
          totalUsers: backendData.totalUsers || 0,
          activeUsers: backendData.activeUsers || 0,
          totalProducts: backendData.totalProducts || 0,
          activeProducts: backendData.activeProducts || 0,
          totalOrders: backendData.totalOrders || 0,
          pendingOrders: backendData.pendingOrders || 0,
          processingOrders: backendData.processingOrders || 0,
          completedOrders: backendData.completedOrders || 0,
          totalRevenue: backendData.totalRevenue || 0,

          users: {
            total: backendData.totalUsers || 0,
            active: backendData.activeUsers || 0,
            change: 0, 
          },
          products: {
            total: backendData.totalProducts || 0,
            active: backendData.activeProducts || 0,
            change: 0,
          },
          orders: {
            total: backendData.totalOrders || 0,
            pending: backendData.pendingOrders || 0,
            change: 0,
          },
          revenue: {
            total: backendData.totalRevenue || 0,
            thisMonth: 0,
            change: 0,
          },
        }

        // Store additional data
        this.recentOrders = backendData.recentOrders || []
        this.lowStockProducts = backendData.lowStockProducts || []

        console.log('✅ Dashboard loaded successfully:', this.dashboardStats)
      } catch (error) {
        this.errors.dashboard = error.response?.data?.message || 'Không thể tải dữ liệu dashboard'
        console.error('❌ Load dashboard error:', error)

        this.dashboardStats = {
          totalUsers: 0,
          activeUsers: 0,
          totalProducts: 0,
          activeProducts: 0,
          totalOrders: 0,
          pendingOrders: 0,
          processingOrders: 0,
          completedOrders: 0,
          totalRevenue: 0,
          users: { total: 0, active: 0, change: 0 },
          products: { total: 0, active: 0, change: 0 },
          orders: { total: 0, pending: 0, change: 0 },
          revenue: { total: 0, thisMonth: 0, change: 0 },
        }
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
        this.users = response.data.users || response.data || []
        this.userStats = response.data.stats || this.userStats
        this.pagination.users.total = response.data.total || this.users.length

        console.log('✅ Users loaded:', this.users.length, 'users')
      } catch (error) {
        this.errors.users = error.response?.data?.message || 'Không thể tải danh sách người dùng'
        console.error('❌ Load users error:', error)
      } finally {
        this.loading.users = false
      }
    },

    async toggleUserStatus(userId) {
      try {
        const response = await adminAPI.toggleUserStatus(userId)

        // Update user in local state
        const userIndex = this.users.findIndex((user) => user.id === userId)
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
        this.products = response.data.products || response.data || []
        this.productStats = response.data.stats || this.productStats
        this.pagination.products.total = response.data.total || this.products.length

        console.log('✅ Products loaded:', this.products.length, 'products')
      } catch (error) {
        this.errors.products = error.response?.data?.message || 'Không thể tải danh sách sản phẩm'
        console.error('❌ Load products error:', error)
      } finally {
        this.loading.products = false
      }
    },

    async toggleProductStatus(productId) {
      try {
        const response = await adminAPI.toggleProductStatus(productId)

        // Update product in local state
        const productIndex = this.products.findIndex((product) => product.id === productId)
        if (productIndex !== -1) {
          // Backend now returns Product directly, not wrapped in data
          this.products[productIndex] = response.data || response
        }

        return response.data || response
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Không thể cập nhật trạng thái sản phẩm'
        throw new Error(errorMsg)
      }
    },

    // Orders management actions
    async loadOrders() {
      this.loading.orders = true
      this.errors.orders = null

      try {
        const response = await adminAPI.getOrders()
        this.orders = response.data.orders || response.data || []
        this.orderStats = response.data.stats || this.orderStats
        this.pagination.orders.total = response.data.total || this.orders.length

        console.log('✅ Orders loaded:', this.orders.length, 'orders')
      } catch (error) {
        this.errors.orders = error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
        console.error('❌ Load orders error:', error)
      } finally {
        this.loading.orders = false
      }
    },

    // Analytics actions
    async loadAnalytics(type = 'overview') {
      this.loading.analytics = true
      this.errors.analytics = null

      try {
        const response = await adminAPI.getAnalytics(type)
        this.analytics = { ...this.analytics, ...response.data }

        console.log('✅ Analytics loaded for type:', type)
      } catch (error) {
        this.errors.analytics = error.response?.data?.message || 'Không thể tải dữ liệu analytics'
        console.error('❌ Load analytics error:', error)
      } finally {
        this.loading.analytics = false
      }
    },

    // Utility actions
    clearErrors() {
      this.errors = {
        dashboard: null,
        users: null,
        products: null,
        orders: null,
        analytics: null,
      }
    },

    resetFilters() {
      this.filters = {
        users: { status: '', role: '', search: '', dateRange: '' },
        products: { category: '', status: '', search: '', sortBy: 'newest' },
        orders: { status: '', dateRange: '', search: '', sortBy: 'newest' },
      }
    },
  },
})
