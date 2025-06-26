import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    
    console.error('API Error:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      data: error.response?.data
    })
    
    return Promise.reject(error)
  }
)

export const sellerAPI = {

  getDashboardOverview: () => {
    return api.get('/dashboard/seller/overview')
  },


  getSellerStats: () => {
    return api.get('/profile/seller/stats')
  },


  getMyProducts: (params = {}) => {
    return api.get('/profile/my-products', { params })
  },

  createProduct: (productData) => {
    return api.post('/products', productData)
  },

  updateProduct: (productId, productData) => {
    return api.put(`/products/${productId}`, productData)
  },

  deleteProduct: (productId) => {
    return api.delete(`/products/${productId}`)
  },

  toggleProductStatus: (productId, isActive) => {
    return api.patch(`/products/${productId}/status`, { isActive })
  },

  uploadProductImages: (productId, formData) => {
    return api.post(`/products/${productId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  deleteProductImage: (productId, imageId) => {
    return api.delete(`/products/${productId}/images/${imageId}`)
  },

  getSellerOrders: (params = {}) => {
    return api.get('/dashboard/seller/orders', { params })
  },

  getOrderDetails: (orderId) => {
    return api.get(`/orders/${orderId}`)
  },
  updateOrderStatus: (orderId, status, note = '') => {
    return api.put(`/orders/${orderId}/status`, { 
      status,
      note,
      updatedBy: 'seller'
    })
  },

  addOrderTracking: (orderId, trackingInfo) => {
    return api.post(`/orders/${orderId}/tracking`, trackingInfo)
  },

  getOrderHistory: (orderId) => {
    return api.get(`/orders/${orderId}/history`)
  },

  getOrderMessages: (orderId) => {
    return api.get(`/orders/${orderId}/messages`)
  },

  sendOrderMessage: (orderId, messageData) => {
    return api.post(`/orders/${orderId}/messages`, {
      ...messageData,
      sender: 'seller',
      timestamp: new Date().toISOString()
    })
  },

  markMessagesAsRead: (orderId, messageIds) => {
    return api.patch(`/orders/${orderId}/messages/read`, { messageIds })
  },


  uploadOrderFiles: (orderId, formData) => {
    return api.post(`/orders/${orderId}/files`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // ==================== ANALYTICS ====================
  
  getSellerAnalytics: (period = '30d', dateRange = null) => {
    const params = { period }
    if (period === 'custom' && dateRange) {
      params.startDate = dateRange.start
      params.endDate = dateRange.end
    }
    return api.get('/dashboard/seller/analytics', { params })
  },

  getRevenueAnalytics: (period = '30d') => {
    return api.get('/dashboard/seller/revenue', { params: { period } })
  },

  getProductAnalytics: (period = '30d', metric = 'revenue') => {
    return api.get('/dashboard/seller/products/analytics', { 
      params: { period, metric } 
    })
  },


  getCustomerAnalytics: (period = '30d', view = 'acquisition') => {
    return api.get('/dashboard/seller/customers/analytics', { 
      params: { period, view } 
    })
  },

  getTrafficAnalytics: (period = '30d') => {
    return api.get('/dashboard/seller/traffic', { params: { period } })
  },

  // ==================== REPORTS ====================
  
  exportOrdersReport: (filters = {}, format = 'excel') => {
    return api.post('/reports/orders/export', filters, {
      params: { format },
      responseType: 'blob'
    })
  },

  exportAnalyticsReport: (period = '30d', format = 'pdf') => {
    return api.get('/reports/analytics/export', {
      params: { period, format },
      responseType: 'blob'
    })
  },

  exportProductReport: (filters = {}, format = 'excel') => {
    return api.post('/reports/products/export', filters, {
      params: { format },
      responseType: 'blob'
    })
  },

  exportCustomerReport: (filters = {}, format = 'excel') => {
    return api.post('/reports/customers/export', filters, {
      params: { format },
      responseType: 'blob'
    })
  },

  // ==================== INVENTORY ====================
  
  getInventoryOverview: () => {
    return api.get('/inventory/overview')
  },

  updateProductStock: (productId, quantity, reason = '') => {
    return api.patch(`/products/${productId}/stock`, { 
      quantity, 
      reason,
      updatedBy: 'seller'
    })
  },

  getLowStockProducts: (threshold = 10) => {
    return api.get('/inventory/low-stock', { params: { threshold } })
  },

  getStockHistory: (productId) => {
    return api.get(`/products/${productId}/stock/history`)
  },

  // ==================== PERFORMANCE INSIGHTS ====================

  getPerformanceAlerts: () => {
    return api.get('/dashboard/seller/alerts')
  },


  dismissAlert: (alertId) => {
    return api.patch(`/dashboard/seller/alerts/${alertId}/dismiss`)
  },

 
  getOptimizationSuggestions: () => {
    return api.get('/dashboard/seller/optimizations')
  },

  getCompetitorAnalysis: (productIds = []) => {
    return api.post('/analytics/competitor-analysis', { productIds })
  },

  // ==================== FINANCIAL ====================

  getFinancialSummary: (period = '30d') => {
    return api.get('/dashboard/seller/financial', { params: { period } })
  },

 
  getPayoutHistory: (params = {}) => {
    return api.get('/seller/payouts', { params })
  },

  requestPayout: (payoutData) => {
    return api.post('/seller/payouts/request', payoutData)
  },

  getCommissionBreakdown: (period = '30d') => {
    return api.get('/seller/commissions', { params: { period } })
  },

  // ==================== MARKETING ====================
  
  getPromotionCampaigns: () => {
    return api.get('/seller/promotions')
  },

  createPromotionCampaign: (campaignData) => {
    return api.post('/seller/promotions', campaignData)
  },

  updatePromotionCampaign: (campaignId, campaignData) => {
    return api.put(`/seller/promotions/${campaignId}`, campaignData)
  },

  
  getSEOInsights: (productIds = []) => {
    return api.post('/analytics/seo-insights', { productIds })
  },

  // ==================== REVIEWS & RATINGS ====================
  
  getProductReviews: (productId, params = {}) => {
    return api.get(`/products/${productId}/reviews`, { params })
  },

  respondToReview: (reviewId, responseData) => {
    return api.post(`/reviews/${reviewId}/respond`, responseData)
  },

  getReviewAnalytics: (period = '30d') => {
    return api.get('/seller/reviews/analytics', { params: { period } })
  },

  // ==================== UTILITIES ====================
  
  getCategories: () => {
    return api.get('/categories')
  },

  getShippingMethods: () => {
    return api.get('/shipping/methods')
  },

  calculateShippingCost: (shippingData) => {
    return api.post('/shipping/calculate', shippingData)
  },

  validateProductData: (productData) => {
    return api.post('/products/validate', productData)
  },

  searchMyProducts: (searchParams) => {
    return api.get('/profile/my-products/search', { params: searchParams })
  },

  getNotificationPreferences: () => {
    return api.get('/seller/notifications/preferences')
  },

  updateNotificationPreferences: (preferences) => {
    return api.put('/seller/notifications/preferences', preferences)
  },

  // ==================== BULK OPERATIONS ====================
  
  bulkUpdateProductStatus: (productIds, isActive) => {
    return api.patch('/products/bulk/status', { productIds, isActive })
  },


  bulkUpdateOrderStatus: (orderIds, status) => {
    return api.patch('/orders/bulk/status', { orderIds, status })
  },


  bulkUpdateProductPrices: (updates) => {
    return api.patch('/products/bulk/prices', { updates })
  },

  bulkExportData: (exportConfig) => {
    return api.post('/export/bulk', exportConfig, {
      responseType: 'blob'
    })
  }
}

export const {
  getDashboardOverview,
  getSellerStats,
  getMyProducts,
  createProduct,
  updateProduct,
  deleteProduct,
  getSellerOrders,
  getOrderDetails,
  updateOrderStatus,
  getOrderMessages,
  sendOrderMessage,
  getSellerAnalytics,
  getRevenueAnalytics,
  exportOrdersReport,
  exportAnalyticsReport
} = sellerAPI

export default sellerAPI