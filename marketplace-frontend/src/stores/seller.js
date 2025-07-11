import { defineStore } from 'pinia'
import { profileAPI, productAPI } from '@/services/api'
import { sellerAPI } from '@/services/sellerAPI'
import axios from 'axios'

export const useSellerStore = defineStore('seller', {
  state: () => ({
    stats: {
      totalProducts: 0,
      activeProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      processingOrders: 0,
      shippedOrders: 0,
      deliveredOrders: 0,
      totalRevenue: 0,
      monthlyRevenue: 0,
      revenueGrowth: 0,
      averageOrderValue: 0,
      lowStockProducts: 0,
      newProductsThisMonth: 0, 
    },

    // Products Management
    products: [],
    productStats: {
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0,
      outOfStock: 0,
    },

    // Orders Management
    orders: [],
    orderStats: {
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0,
    },
    selectedOrders: [],
    orderMessages: {},

    // Analytics
    analytics: {
      salesChart: [],
      topProducts: [],
      categoryBreakdown: [],
      monthlyStats: [],
      customerInsights: {},
      revenueData: {},
      periodComparison: {},
    },

    // Loading states
    loading: {
      dashboard: false,
      products: false,
      orders: false,
      analytics: false,
      orderUpdate: {},
      export: false,
    },

    // Error states
    errors: {
      dashboard: null,
      products: null,
      orders: null,
      analytics: null,
    },

    // Filters & Search
    orderFilters: {
      status: 'all',
      search: '',
      dateRange: 'all',
      sortBy: 'createdAt',
      sortOrder: 'desc',
    },

    // Notifications
    notifications: [],

    // Real-time updates
    lastSyncTime: null,
    autoRefresh: true,
  }),

  getters: {

    totalRevenue: (state) => state.stats.totalRevenue || 0,
    monthlyGrowth: (state) => state.stats.revenueGrowth || 0,

    dashboardStats: (state) => state.stats, // Alias for backward compatibility

    // Product getters
    activeProducts: (state) => state.products.filter((p) => p.isActive),
    inactiveProducts: (state) => state.products.filter((p) => !p.isActive),
    lowStockProducts: (state) => state.products.filter((p) => p.stockQuantity < 10),
    outOfStockProducts: (state) => state.products.filter((p) => p.stockQuantity === 0),

    // Order getters
    filteredOrders: (state) => {
      let filtered = [...state.orders]

      // Filter by status
      if (state.orderFilters.status !== 'all') {
        filtered = filtered.filter(
          (order) => order.status.toLowerCase() === state.orderFilters.status.toLowerCase()
        )
      }

      // Search filter
      if (state.orderFilters.search) {
        const search = state.orderFilters.search.toLowerCase()
        filtered = filtered.filter(
          (order) =>
            order.id.toLowerCase().includes(search) ||
            order.customerName.toLowerCase().includes(search) ||
            order.customerEmail.toLowerCase().includes(search)
        )
      }

      // Date range filter
      if (state.orderFilters.dateRange !== 'all') {
        const now = new Date()
        const days = {
          today: 1,
          week: 7,
          month: 30,
          quarter: 90,
        }

        if (days[state.orderFilters.dateRange]) {
          const cutoff = new Date(
            now.getTime() - days[state.orderFilters.dateRange] * 24 * 60 * 60 * 1000
          )
          filtered = filtered.filter((order) => new Date(order.createdAt) >= cutoff)
        }
      }

      // Sort
      filtered.sort((a, b) => {
        const aVal = a[state.orderFilters.sortBy]
        const bVal = b[state.orderFilters.sortBy]

        if (state.orderFilters.sortOrder === 'asc') {
          return aVal > bVal ? 1 : -1
        } else {
          return aVal < bVal ? 1 : -1
        }
      })

      return filtered
    },

    pendingOrders: (state) => state.orders.filter((o) => o.status === 'PENDING'),
    processingOrders: (state) => state.orders.filter((o) => o.status === 'PROCESSING'),
    urgentOrders: (state) => {
      const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
      return state.orders.filter(
        (o) =>
          (o.status === 'PENDING' || o.status === 'PROCESSING') &&
          new Date(o.createdAt) < threeDaysAgo
      )
    },

    recentOrders: (state) => {
      return [...state.orders]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
    },

    // Analytics getters
    topSellingProducts: (state) => {
      return [...state.products].sort((a, b) => (b.soldCount || 0) - (a.soldCount || 0)).slice(0, 5)
    },

    recentProducts: (state) => {
      return [...state.products]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
    },

    // Performance metrics
    conversionRate: (state) => {
      const views = state.analytics.customerInsights.totalViews || 1
      const orders = state.stats.totalOrders || 0
      return ((orders / views) * 100).toFixed(2)
    },

    averageOrderValue: (state) => {
      if (state.stats.totalOrders === 0) return 0
      return (state.stats.totalRevenue / state.stats.totalOrders).toFixed(2)
    },
  },

  actions: {
    async fetchDashboardStats() {
      this.loading.dashboard = true
      this.errors.dashboard = null

      try {
        const response = await sellerAPI.getDashboardOverview()
        this.stats = {
          ...this.stats,
          ...response.data,
        }

        this.updateProductStats()
        this.updateOrderStats()
        this.lastSyncTime = new Date()
      } catch (error) {
        this.errors.dashboard = error.response?.data?.message || 'Không thể tải thống kê dashboard'
        console.error('Error fetching dashboard stats:', error)
      } finally {
        this.loading.dashboard = false
      }
    },

    async loadDashboardStats() {
      return this.fetchDashboardStats()
    },

    resetSeller() {
      this.stats = {
        totalProducts: 0,
        activeProducts: 0,
        totalOrders: 0,
        pendingOrders: 0,
        processingOrders: 0,
        shippedOrders: 0,
        deliveredOrders: 0,
        totalRevenue: 0,
        monthlyRevenue: 0,
        revenueGrowth: 0,
        averageOrderValue: 0,
        lowStockProducts: 0,
        newProductsThisMonth: 0,
      }
      this.products = []
      this.orders = []
      this.notifications = []
    },


    // Order Actions (keep existing)
    async loadOrders(refresh = false) {
      if (!refresh && this.loading.orders) return

      this.loading.orders = true
      this.errors.orders = null

      try {
        const response = await sellerAPI.getSellerOrders()
        this.orders = response.data || []
        this.updateOrderStats()
        this.checkUrgentOrders()
      } catch (error) {
        this.errors.orders = error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
        console.error('Error loading orders:', error)
      } finally {
        this.loading.orders = false
      }
    },


    updateOrderStats() {
      this.orderStats = {
        total: this.orders.length,
        pending: this.orders.filter((o) => o.status === 'PENDING').length,
        processing: this.orders.filter((o) => o.status === 'PROCESSING').length,
        shipped: this.orders.filter((o) => o.status === 'SHIPPED').length,
        delivered: this.orders.filter((o) => o.status === 'DELIVERED').length,
        cancelled: this.orders.filter((o) => o.status === 'CANCELLED').length,
      }
    },

    updateProductStats() {
      this.productStats = {
        total: this.products.length,
        active: this.products.filter((p) => p.isActive).length,
        inactive: this.products.filter((p) => !p.isActive).length,
        lowStock: this.products.filter((p) => p.stockQuantity < 10).length,
        outOfStock: this.products.filter((p) => p.stockQuantity === 0).length,
      }
    },

    checkUrgentOrders() {
      // Check for orders that need attention
      const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
      const urgentOrders = this.orders.filter(
        (order) =>
          (order.status === 'PENDING' || order.status === 'PROCESSING') &&
          new Date(order.createdAt) < threeDaysAgo
      )

      if (urgentOrders.length > 0) {
        console.log(`⚠️ Found ${urgentOrders.length} urgent orders that need attention`)
      }
    },

    async fetchProducts() {
      this.loading.products = true
      this.errors.products = null
      console.log('==> [sellerStore] fetchProducts CALLED')

      try {
        const response = await sellerAPI.getMyProducts()
        console.log('==> [sellerStore] API response:', response)
        this.products = Array.isArray(response.data) ? response.data : response.data.products || []
        this.updateProductStats()
      } catch (error) {
        this.errors.products = error.response?.data?.message || 'Không thể tải danh sách sản phẩm'
        console.error('==> [sellerStore] fetchProducts ERROR', error)
      } finally {
        this.loading.products = false
      }
    },

    async createProduct(productData) {
      try {
        await sellerAPI.createProduct(productData)
        await this.fetchProducts() 
        this.updateProductStats()
      } catch (error) {
        throw error
      }
    },

    async updateProduct(productId, productData) {
      try {
        const response = await sellerAPI.updateProduct(productId, productData)
        const index = this.products.findIndex((p) => p.id === productId)
        if (index !== -1) {
          this.products[index] = response.data
        }
        this.updateProductStats()
        return response.data
      } catch (error) {
        throw error
      }
    },

    async deleteProduct(productId) {
      try {
        await sellerAPI.deleteProduct(productId)
        this.products = this.products.filter((p) => p.id !== productId)
        this.updateProductStats()
      } catch (error) {
        throw error
      }
    },

    async toggleProductStatus(productId) {
      try {
        const product = this.products.find((p) => p.id === productId)
        if (product) {
          const response = await sellerAPI.toggleProductStatus(productId, !product.isActive)
          product.isActive = !product.isActive
          this.updateProductStats()
          return response.data
        }
      } catch (error) {
        throw error
      }
    },

    async duplicateProduct(productId) {
      try {
        const product = this.products.find((p) => p.id === productId)
        if (product) {
          const duplicateData = {
            ...product,
            name: `${product.name} (Bản sao)`,
            id: undefined,
          }
          const response = await sellerAPI.createProduct(duplicateData)
          this.products.push(response.data)
          this.updateProductStats()
          return response.data
        }
      } catch (error) {
        throw error
      }
    },

    async bulkUpdateStatus(productIds, isActive) {
      try {
        await Promise.all(productIds.map((id) => sellerAPI.updateProduct(id, { isActive })))
        this.products.forEach((product) => {
          if (productIds.includes(product.id)) {
            product.isActive = isActive
          }
        })
        this.updateProductStats()
      } catch (error) {
        throw error
      }
    },

    async bulkDelete(productIds) {
      try {
        await Promise.all(productIds.map((id) => sellerAPI.deleteProduct(id)))
        this.products = this.products.filter((p) => !productIds.includes(p.id))
        this.updateProductStats()
      } catch (error) {
        throw error
      }
    },

    // Order status update methods
    async updateOrderStatus(orderId, status) {
      this.loading.orderUpdate[orderId] = true
      try {
        const response = await sellerAPI.updateOrderStatus(orderId, status)
        const index = this.orders.findIndex((o) => o.id === orderId)
        if (index !== -1) {
          this.orders[index] = response.data
        }
        this.updateOrderStats()
        return response.data
      } catch (error) {
        throw error
      } finally {
        this.loading.orderUpdate[orderId] = false
      }
    },

    async bulkUpdateOrderStatus(orderIds, status) {
      try {
        await Promise.all(orderIds.map((id) => sellerAPI.updateOrderStatus(id, status)))
        await this.loadOrders(true)
      } catch (error) {
        throw error
      }
    },

    // Order selection methods
    toggleOrderSelection(orderId) {
      const index = this.selectedOrders.indexOf(orderId)
      if (index > -1) {
        this.selectedOrders.splice(index, 1)
      } else {
        this.selectedOrders.push(orderId)
      }
    },

    selectAllOrders() {
      this.selectedOrders = this.filteredOrders.map((order) => order.id)
    },

    clearOrderSelection() {
      this.selectedOrders = []
    },

    // Filter methods
    setOrderFilters(filters) {
      this.orderFilters = { ...this.orderFilters, ...filters }
    },

    // Status utility methods
    getStatusLabel(status) {
      const labels = {
        PENDING: 'Chờ xử lý',
        PROCESSING: 'Đang xử lý',
        SHIPPED: 'Đã gửi hàng',
        DELIVERED: 'Đã giao hàng',
        CANCELLED: 'Đã hủy',
      }
      return labels[status] || status
    },

    getStatusColor(status) {
      const colors = {
        PENDING: '#f59e0b',
        PROCESSING: '#3b82f6',
        SHIPPED: '#8b5cf6',
        DELIVERED: '#10b981',
        CANCELLED: '#ef4444',
      }
      return colors[status] || '#6b7280'
    },

    // Auto refresh methods
    startAutoRefresh() {
      if (this.autoRefresh) {
        this.refreshInterval = setInterval(() => {
          this.loadOrders(true)
        }, 30000) // Refresh every 30 seconds
      }
    },

    stopAutoRefresh() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    },

    // Export methods
    async exportOrdersReport() {
      this.loading.export = true
      try {
        const response = await sellerAPI.exportOrdersReport(this.orderFilters)
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute(
          'download',
          `orders-report-${new Date().toISOString().split('T')[0]}.xlsx`
        )
        document.body.appendChild(link)
        link.click()
        link.remove()
      } catch (error) {
        throw error
      } finally {
        this.loading.export = false
      }
    },

    // Notification methods
    addNotification(notification) {
      const id = Date.now()
      this.notifications.push({ ...notification, id })
      setTimeout(() => {
        this.removeNotification(id)
      }, 5000)
    },

    removeNotification(notificationId) {
      this.notifications = this.notifications.filter((n) => n.id !== notificationId)
    },

  },
})
