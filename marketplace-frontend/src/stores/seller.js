// stores/seller.js - Enhanced with Real API Integration
import { defineStore } from 'pinia'
import { profileAPI } from '@/services/api'
import axios from 'axios'

// Create seller-specific API instance
const sellerAPI = {
  // Orders APIs
  getSellerOrders: () => axios.get('/api/dashboard/seller/orders'),
  updateOrderStatus: (orderId, status) => axios.put(`/api/orders/${orderId}/status`, { status }),
  getOrderDetails: (orderId) => axios.get(`/api/orders/${orderId}`),
  getOrderMessages: (orderId) => axios.get(`/api/orders/${orderId}/messages`),
  sendOrderMessage: (orderId, message) => axios.post(`/api/orders/${orderId}/messages`, { message }),
  
  // Analytics APIs  
  getSellerAnalytics: (period = '30d') => axios.get(`/api/dashboard/seller/analytics?period=${period}`),
  getRevenueData: (period = '30d') => axios.get(`/api/dashboard/seller/revenue?period=${period}`),
  getProductStats: () => axios.get(`/api/dashboard/seller/products`),
  
  // Dashboard APIs
  getSellerOverview: () => axios.get('/api/dashboard/seller/overview'),
  
  // Export APIs
  exportOrdersReport: (filters) => axios.post('/api/reports/orders/export', filters, {
    responseType: 'blob'
  }),
  exportAnalyticsReport: (period) => axios.get(`/api/reports/analytics/export?period=${period}`, {
    responseType: 'blob'
  })
}

export const useSellerStore = defineStore('seller', {
  state: () => ({
    // Dashboard Stats
    dashboardStats: {
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
      lowStockProducts: 0
    },
    
    // Products Management
    products: [],
    productStats: {
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0,
      outOfStock: 0
    },
    
    // Orders Management
    orders: [],
    orderStats: {
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0
    },
    selectedOrders: [], // For bulk operations
    orderMessages: {}, // Store messages by orderId
    
    // Analytics
    analytics: {
      salesChart: [],
      topProducts: [],
      categoryBreakdown: [],
      monthlyStats: [],
      customerInsights: {},
      revenueData: {},
      periodComparison: {}
    },
    
    // Loading states
    loading: {
      dashboard: false,
      products: false,
      orders: false,
      analytics: false,
      orderUpdate: {},
      export: false
    },
    
    // Error states
    errors: {
      dashboard: null,
      products: null,
      orders: null,
      analytics: null
    },
    
    // Filters & Search
    orderFilters: {
      status: 'all',
      search: '',
      dateRange: 'all',
      sortBy: 'createdAt',
      sortOrder: 'desc'
    },
    
    // Notifications
    notifications: [],
    
    // Real-time updates
    lastSyncTime: null,
    autoRefresh: true
  }),

  getters: {
    // Dashboard getters
    totalRevenue: (state) => state.dashboardStats.totalRevenue || 0,
    monthlyGrowth: (state) => state.dashboardStats.revenueGrowth || 0,
    
    // Product getters
    activeProducts: (state) => state.products.filter(p => p.isActive),
    inactiveProducts: (state) => state.products.filter(p => !p.isActive),
    lowStockProducts: (state) => state.products.filter(p => p.stockQuantity < 10),
    outOfStockProducts: (state) => state.products.filter(p => p.stockQuantity === 0),
    
    // Order getters
    filteredOrders: (state) => {
      let filtered = [...state.orders]
      
      // Filter by status
      if (state.orderFilters.status !== 'all') {
        filtered = filtered.filter(order => 
          order.status.toLowerCase() === state.orderFilters.status.toLowerCase()
        )
      }
      
      // Search filter
      if (state.orderFilters.search) {
        const search = state.orderFilters.search.toLowerCase()
        filtered = filtered.filter(order => 
          order.id.toLowerCase().includes(search) ||
          order.customerName.toLowerCase().includes(search) ||
          order.customerEmail.toLowerCase().includes(search)
        )
      }
      
      // Date range filter
      if (state.orderFilters.dateRange !== 'all') {
        const now = new Date()
        const days = {
          'today': 1,
          'week': 7,
          'month': 30,
          'quarter': 90
        }
        
        if (days[state.orderFilters.dateRange]) {
          const cutoff = new Date(now.getTime() - days[state.orderFilters.dateRange] * 24 * 60 * 60 * 1000)
          filtered = filtered.filter(order => new Date(order.createdAt) >= cutoff)
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
    
    pendingOrders: (state) => state.orders.filter(o => o.status === 'PENDING'),
    processingOrders: (state) => state.orders.filter(o => o.status === 'PROCESSING'),
    urgentOrders: (state) => {
      const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
      return state.orders.filter(o => 
        (o.status === 'PENDING' || o.status === 'PROCESSING') && 
        new Date(o.createdAt) < threeDaysAgo
      )
    },
    
    // Analytics getters
    topSellingProducts: (state) => {
      return [...state.products]
        .sort((a, b) => (b.soldCount || 0) - (a.soldCount || 0))
        .slice(0, 5)
    },
    
    recentProducts: (state) => {
      return [...state.products]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
    },
    
    // Performance metrics
    conversionRate: (state) => {
      const views = state.analytics.customerInsights.totalViews || 1
      const orders = state.dashboardStats.totalOrders || 0
      return ((orders / views) * 100).toFixed(2)
    },
    
    averageOrderValue: (state) => {
      if (state.dashboardStats.totalOrders === 0) return 0
      return (state.dashboardStats.totalRevenue / state.dashboardStats.totalOrders).toFixed(2)
    }
  },

  actions: {
    // Dashboard Actions
    async fetchDashboardStats() {
      this.loading.dashboard = true
      this.errors.dashboard = null
      
      try {
        const response = await sellerAPI.getSellerOverview()
        this.dashboardStats = {
          ...this.dashboardStats,
          ...response.data
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

    // Order Actions
    async loadOrders(refresh = false) {
      if (!refresh && this.loading.orders) return
      
      this.loading.orders = true
      this.errors.orders = null
      
      try {
        const response = await sellerAPI.getSellerOrders()
        this.orders = response.data.orders || []
        this.updateOrderStats()
        
        // Load urgent notifications
        this.checkUrgentOrders()
        
      } catch (error) {
        this.errors.orders = error.response?.data?.message || 'Không thể tải danh sách đơn hàng'
        console.error('Error loading orders:', error)
      } finally {
        this.loading.orders = false
      }
    },

    async updateOrderStatus(orderId, status, note = '') {
      if (this.loading.orderUpdate[orderId]) return
      
      this.loading.orderUpdate[orderId] = true
      
      try {
        const response = await sellerAPI.updateOrderStatus(orderId, status)
        
        // Update local state
        const orderIndex = this.orders.findIndex(o => o.id === orderId)
        if (orderIndex !== -1) {
          this.orders[orderIndex] = {
            ...this.orders[orderIndex],
            ...response.data,
            updatedAt: new Date().toISOString()
          }
        }
        
        this.updateOrderStats()
        
        // Add notification
        this.addNotification({
          type: 'success',
          message: `Đã cập nhật trạng thái đơn hàng #${orderId.slice(-8)} thành ${this.getStatusLabel(status)}`,
          timestamp: new Date()
        })
        
        // Send note if provided
        if (note) {
          await this.sendOrderMessage(orderId, note, 'status_update')
        }
        
        return response.data
        
      } catch (error) {
        this.addNotification({
          type: 'error', 
          message: error.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng',
          timestamp: new Date()
        })
        throw error
      } finally {
        this.loading.orderUpdate[orderId] = false
      }
    },

    async bulkUpdateOrderStatus(orderIds, status) {
      const results = []
      
      for (const orderId of orderIds) {
        try {
          const result = await this.updateOrderStatus(orderId, status)
          results.push({ orderId, success: true, data: result })
        } catch (error) {
          results.push({ orderId, success: false, error: error.message })
        }
      }
      
      const successful = results.filter(r => r.success).length
      const failed = results.filter(r => !r.success).length
      
      this.addNotification({
        type: successful > failed ? 'success' : 'warning',
        message: `Cập nhật ${successful} đơn hàng thành công${failed > 0 ? `, ${failed} đơn thất bại` : ''}`,
        timestamp: new Date()
      })
      
      return results
    },

    async getOrderDetails(orderId) {
      try {
        const response = await sellerAPI.getOrderDetails(orderId)
        return response.data
      } catch (error) {
        console.error('Error getting order details:', error)
        throw error
      }
    },

    // Communication Actions
    async loadOrderMessages(orderId) {
      try {
        const response = await sellerAPI.getOrderMessages(orderId)
        this.orderMessages[orderId] = response.data || []
        return response.data
      } catch (error) {
        console.error('Error loading order messages:', error)
        return []
      }
    },

    async sendOrderMessage(orderId, message, type = 'general') {
      try {
        const response = await sellerAPI.sendOrderMessage(orderId, {
          message,
          type,
          timestamp: new Date().toISOString()
        })
        
        // Update local messages
        if (!this.orderMessages[orderId]) {
          this.orderMessages[orderId] = []
        }
        this.orderMessages[orderId].push(response.data)
        
        return response.data
      } catch (error) {
        console.error('Error sending order message:', error)
        throw error
      }
    },

    // Analytics Actions
    async fetchAnalytics(period = '30d') {
      this.loading.analytics = true
      this.errors.analytics = null
      
      try {
        const [analyticsRes, revenueRes, productRes] = await Promise.all([
          sellerAPI.getSellerAnalytics(period),
          sellerAPI.getRevenueData(period),
          sellerAPI.getProductStats()
        ])
        
        this.analytics = {
          ...this.analytics,
          ...analyticsRes.data,
          revenueData: revenueRes.data,
          productStats: productRes.data
        }
        
      } catch (error) {
        this.errors.analytics = error.response?.data?.message || 'Không thể tải dữ liệu phân tích'
        console.error('Error fetching analytics:', error)
      } finally {
        this.loading.analytics = false
      }
    },

    // Export Actions
    async exportOrdersReport(filters = {}) {
      this.loading.export = true
      
      try {
        const response = await sellerAPI.exportOrdersReport({
          ...this.orderFilters,
          ...filters
        })
        
        // Create download link
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `orders-report-${new Date().toISOString().slice(0, 10)}.xlsx`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        
        this.addNotification({
          type: 'success',
          message: 'Đã xuất báo cáo đơn hàng thành công',
          timestamp: new Date()
        })
        
      } catch (error) {
        this.addNotification({
          type: 'error',
          message: 'Không thể xuất báo cáo',
          timestamp: new Date()
        })
      } finally {
        this.loading.export = false
      }
    },

    async exportAnalyticsReport(period = '30d') {
      this.loading.export = true
      
      try {
        const response = await sellerAPI.exportAnalyticsReport(period)
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `analytics-report-${period}-${new Date().toISOString().slice(0, 10)}.pdf`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        
        this.addNotification({
          type: 'success',
          message: 'Đã xuất báo cáo phân tích thành công',
          timestamp: new Date()
        })
        
      } catch (error) {
        this.addNotification({
          type: 'error',
          message: 'Không thể xuất báo cáo phân tích',
          timestamp: new Date()
        })
      } finally {
        this.loading.export = false
      }
    },

    // Utility Actions
    updateOrderStats() {
      this.orderStats = {
        total: this.orders.length,
        pending: this.orders.filter(o => o.status === 'PENDING').length,
        processing: this.orders.filter(o => o.status === 'PROCESSING').length,
        shipped: this.orders.filter(o => o.status === 'SHIPPED').length,
        delivered: this.orders.filter(o => o.status === 'DELIVERED').length,
        cancelled: this.orders.filter(o => o.status === 'CANCELLED').length
      }
    },

    updateProductStats() {
      this.productStats = {
        total: this.products.length,
        active: this.products.filter(p => p.isActive).length,
        inactive: this.products.filter(p => !p.isActive).length,
        lowStock: this.products.filter(p => p.stockQuantity < 10).length,
        outOfStock: this.products.filter(p => p.stockQuantity === 0).length
      }
    },

    setOrderFilters(filters) {
      this.orderFilters = { ...this.orderFilters, ...filters }
    },

    toggleOrderSelection(orderId) {
      const index = this.selectedOrders.indexOf(orderId)
      if (index > -1) {
        this.selectedOrders.splice(index, 1)
      } else {
        this.selectedOrders.push(orderId)
      }
    },

    selectAllOrders() {
      this.selectedOrders = this.filteredOrders.map(o => o.id)
    },

    clearOrderSelection() {
      this.selectedOrders = []
    },

    addNotification(notification) {
      this.notifications.unshift({
        id: Date.now(),
        ...notification
      })
      
      // Auto remove after 5 seconds
      setTimeout(() => {
        this.removeNotification(notification.id || Date.now())
      }, 5000)
    },

    removeNotification(notificationId) {
      const index = this.notifications.findIndex(n => n.id === notificationId)
      if (index > -1) {
        this.notifications.splice(index, 1)
      }
    },

    checkUrgentOrders() {
      const urgent = this.urgentOrders
      if (urgent.length > 0) {
        this.addNotification({
          type: 'warning',
          message: `Có ${urgent.length} đơn hàng cần xử lý gấp (quá 3 ngày)`,
          timestamp: new Date(),
          action: 'view_urgent_orders'
        })
      }
    },

    getStatusLabel(status) {
      const labels = {
        'PENDING': 'Chờ xử lý',
        'PROCESSING': 'Đang xử lý', 
        'SHIPPED': 'Đã gửi hàng',
        'DELIVERED': 'Đã giao hàng',
        'CANCELLED': 'Đã hủy'
      }
      return labels[status] || status
    },

    getStatusColor(status) {
      const colors = {
        'PENDING': '#f59e0b',
        'PROCESSING': '#3b82f6',
        'SHIPPED': '#8b5cf6', 
        'DELIVERED': '#10b981',
        'CANCELLED': '#ef4444'
      }
      return colors[status] || '#6b7280'
    },

    // Auto-refresh functionality
    startAutoRefresh() {
      if (this.autoRefresh) return
      
      this.autoRefresh = true
      this.refreshInterval = setInterval(() => {
        if (document.visibilityState === 'visible') {
          this.loadOrders(true)
          this.fetchDashboardStats()
        }
      }, 30000) // Refresh every 30 seconds
    },

    stopAutoRefresh() {
      this.autoRefresh = false
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    }
  }
})