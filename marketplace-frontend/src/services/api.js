import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
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
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout'),
  refreshToken: () => api.post('/auth/refresh'),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, password) => api.post('/auth/reset-password', { token, password }),
}

export const productAPI = {
  getAll: (params) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  getFeatured: () => api.get('/products/featured'),
  getLatest: () => api.get('/products/latest'),
  getByCategory: (category) => api.get('/products', { params: { category } }),
  searchProducts: (query) => api.get('/products', { params: { search: query } }),
  getByPriceRange: (minPrice, maxPrice) =>
    api.get('/products', {
      params: { minPrice, maxPrice },
    }),

  create: (productData) => api.post('/products', productData),
  update: (id, productData) => api.put(`/products/${id}`, productData),
  delete: (id) => api.delete(`/products/${id}`),

  getBySeller: (sellerId) => api.get(`/products/seller/${sellerId}`),

  uploadImage: (formData) =>
    api.post('/products/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  toggleStatus: (id) => api.put(`/products/${id}/toggle-status`),
}

export const categoryAPI = {
  getAll: () => api.get('/categories'),
  getById: (id) => api.get(`/categories/${id}`),
  create: (categoryData) => api.post('/categories', categoryData),
  update: (id, categoryData) => api.put(`/categories/${id}`, categoryData),
  delete: (id) => api.delete(`/categories/${id}`),
}

export const cartAPI = {
  getCart: () => api.get('/cart'),
  addItem: (productId, quantity) => api.post('/cart/add', { productId, quantity }),
  updateItem: (productId, quantity) => api.put('/cart/update', { productId, quantity }),
  removeItem: (productId) => api.delete(`/cart/remove/${productId}`),
  clear: () => api.delete('/cart/clear'),
  validate: () => api.get('/cart/validate'),
}

export const orderAPI = {
  create: (shippingAddress, billingAddress) =>
    api.post('/orders/create', { shippingAddress, billingAddress }),
  getMyOrders: () => api.get('/orders/my-orders'),
  getById: (id) => api.get(`/orders/${id}`),
  cancel: (id) => api.put(`/orders/${id}/cancel`),
  getSellerOrders: () => api.get('/orders/seller/my-orders'),
  updateStatus: (orderId, status) => api.put(`/orders/${orderId}/status`, { status }),
  getOrdersBySeller: (sellerId) => api.get(`/orders/seller/${sellerId}`),
  validatePromoCode: (promoCode) => api.post('/orders/validate-promo', { promoCode }),
}

export const profileAPI = {
  getProfile: () => api.get('/profile'),
  updateProfile: (profileData) => api.put('/profile', profileData),
  uploadAvatar: (formData) =>
    api.post('/profile/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  changePassword: (passwordData) => api.put('/profile/change-password', passwordData),

  // User stats & history
  getUserStats: () => api.get('/profile/stats'),
  getPurchaseHistory: () => api.get('/profile/purchase-history'),

  //  SELLER
  getMyProducts: () => api.get('/profile/my-products'),
  getSellerStats: () => api.get('/profile/seller-stats'),

  // Wishlist
  getWishlist: () => api.get('/profile/wishlist'),
  addToWishlist: (productId) => api.post('/profile/wishlist', { productId }),
  removeFromWishlist: (productId) => api.delete(`/profile/wishlist/${productId}`),

  // Saved for Later
  getSavedItems: () => api.get('/profile/saved-items'),
  saveForLater: (productId) => api.post('/profile/saved-items', { productId }),
  removeFromSavedItems: (itemId) => api.delete(`/profile/saved-items/${itemId}`),

  // Addresses
  getAddresses: () => api.get('/profile/addresses'),
  addAddress: (addressData) => api.post('/profile/addresses', addressData),
  updateAddress: (addressId, addressData) =>
    api.put(`/profile/addresses/${addressId}`, addressData),
  deleteAddress: (addressId) => api.delete(`/profile/addresses/${addressId}`),
  setDefaultAddress: (addressId) => api.put(`/profile/addresses/${addressId}/default`),

  // Payment methods
  getPaymentMethods: () => api.get('/profile/payment-methods'),
  addPaymentMethod: (paymentData) => api.post('/profile/payment-methods', paymentData),
  updatePaymentMethod: (methodId, paymentData) =>
    api.put(`/profile/payment-methods/${methodId}`, paymentData),
  deletePaymentMethod: (methodId) => api.delete(`/profile/payment-methods/${methodId}`),
  setDefaultPaymentMethod: (methodId) => api.put(`/profile/payment-methods/${methodId}/default`),
}

export const dashboardAPI = {
  getAdminOverview: () => api.get('/admin/dashboard'),

  getSellerOverview: () => api.get('/dashboard/seller/overview'),

  getUserOverview: () => api.get('/dashboard/user/overview'),
}

export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),

  getUsers: () => api.get('/admin/users'),
  getUserById: (userId) => api.get(`/admin/users/${userId}`),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),

  getProducts: () => api.get('/admin/products'),
  toggleProductStatus: (productId) => api.put(`/admin/products/${productId}/toggle-status`),
  deleteProduct: (productId) => api.delete(`/admin/products/${productId}`),

  getOrders: () => api.get('/admin/orders'),
  updateOrderStatus: (orderId, status) => api.put(`/admin/orders/${orderId}/status`, { status }),

  getCategories: () => api.get('/admin/categories'),
  createCategory: (categoryData) => api.post('/categories', categoryData),
  updateCategory: (id, categoryData) => api.put(`/categories/${id}`, categoryData),
  toggleCategoryStatus: (id) => api.patch(`/admin/categories/${id}/toggle-status`),
  deleteCategory: (id) => api.delete(`/categories/${id}`),

  getAnalytics: (type) => api.get(`/admin/analytics/${type}`),
}

export const reviewAPI = {
  getByProduct: (productId) => api.get(`/reviews/product/${productId}`),
  getMyReviews: () => api.get('/reviews/my-reviews'),
  create: (reviewData) => api.post('/reviews', reviewData),
  update: (reviewId, reviewData) => api.put(`/reviews/${reviewId}`, reviewData),
  delete: (reviewId) => api.delete(`/reviews/${reviewId}`),
  checkCanReview: (productId) => api.get(`/reviews/check/${productId}`),
  getStats: (productId) => api.get(`/reviews/stats/${productId}`),
}

export const searchAPI = {
  products: (query, filters) => api.get('/search/products', { params: { q: query, ...filters } }),
  suggestions: (query) => api.get('/search/suggestions', { params: { q: query } }),
  trending: () => api.get('/search/trending'),
}

export const recommendationAPI = {
  forUser: (limit = 20) => api.get('/recommendations/for-you', { params: { limit } }),

  similar: (productId, limit = 10) =>
    api.get(`/recommendations/similar/${productId}`, { params: { limit } }),

  trending: (limit = 15) => api.get('/recommendations/trending', { params: { limit } }),

  crossSell: (productId, limit = 8) =>
    api.get(`/recommendations/cross-sell/${productId}`, { params: { limit } }),

  category: (category, limit = 10) =>
    api.get(`/recommendations/category/${category}`, { params: { limit } }),

  priceRange: (minPrice, maxPrice, limit = 15) =>
    api.get('/recommendations/price-range', {
      params: { minPrice, maxPrice, limit },
    }),

  recentlyViewed: (limit = 10) =>
    api.get('/recommendations/recently-viewed', { params: { limit } }),

  trackInteraction: (data) => api.post('/recommendations/track', data),

  refreshPreferences: () => api.post('/recommendations/refresh-preferences'),

  calculateSimilarities: () => api.post('/recommendations/admin/calculate-similarities'),
}

export const chatAPI = {
  createConversation: (otherUserId, productId) =>
    api.post('/chat/conversations', { otherUserId, productId }),
  getConversations: () => api.get('/chat/conversations'),
  getMessages: (conversationId) => api.get(`/chat/conversations/${conversationId}/messages`),
  markAsRead: (conversationId) => api.put(`/chat/conversations/${conversationId}/read`),
  getUnreadCount: () => api.get('/chat/unread-count'),
  deleteMessage: (messageId) => api.delete(`/chat/messages/${messageId}`),
}

export const notificationAPI = {
  getAll: () => api.get('/notifications'),
  markAsRead: (notificationId) => api.put(`/notifications/${notificationId}/read`),
  markAllAsRead: () => api.put('/notifications/mark-all-read'),
  delete: (notificationId) => api.delete(`/notifications/${notificationId}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
}

export const paymentAPI = {
  createPaymentIntent: (amount, currency = 'VND') =>
    api.post('/payments/create-intent', { amount, currency }),
  confirmPayment: (paymentIntentId) => api.post('/payments/confirm', { paymentIntentId }),
  getPaymentHistory: () => api.get('/payments/history'),
}

export const loyaltyAPI = {
  getPoints: () => api.get('/loyalty/points'),
  getHistory: () => api.get('/loyalty/history'),
  getRewards: () => api.get('/loyalty/rewards'),
  redeemReward: (rewardId) => api.post(`/loyalty/redeem/${rewardId}`),
  getTier: () => api.get('/loyalty/tier'),
  getLeaderboard: () => api.get('/loyalty/leaderboard'),
}

export const uploadFile = async (file, type = 'image') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const uploadAPI = {
  uploadFile: (file, type = 'image') => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    return api.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  uploadMultiple: (files, type = 'image') => {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append(`files[${index}]`, file)
    })
    formData.append('type', type)

    return api.post('/upload/multiple', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export const utils = {
  formatPrice: (price, currency = 'VND') => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency,
    }).format(price)
  },

  formatDate: (date, options = {}) => {
    return new Date(date).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      ...options,
    })
  },

  debounce: (func, wait) => {
    let timeout
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout)
        func(...args)
      }
      clearTimeout(timeout)
      timeout = setTimeout(later, wait)
    }
  },

  generateSlug: (name) => {
    return name
      .toLowerCase()
      .replace(/[áàảãạâấầẩẫậăắằẳẵặ]/g, 'a')
      .replace(/[éèẻẽẹêếềểễệ]/g, 'e')
      .replace(/[íìỉĩị]/g, 'i')
      .replace(/[óòỏõọôốồổỗộơớờởỡợ]/g, 'o')
      .replace(/[úùủũụưứừửữự]/g, 'u')
      .replace(/[ýỳỷỹỵ]/g, 'y')
      .replace(/đ/g, 'd')
      .replace(/[^a-z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '')
  },
}

export default api

export const apiUtils = {
  handleError: (error) => {
    console.error('API Error:', error)

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 400:
          return data.message || 'Dữ liệu không hợp lệ'
        case 401:
          return 'Bạn cần đăng nhập để thực hiện hành động này'
        case 403:
          return 'Bạn không có quyền thực hiện hành động này'
        case 404:
          return 'Không tìm thấy dữ liệu'
        case 409:
          return data.message || 'Dữ liệu đã tồn tại'
        case 422:
          return data.message || 'Dữ liệu không hợp lệ'
        case 500:
          return 'Lỗi máy chủ, vui lòng thử lại sau'
        default:
          return data.message || 'Có lỗi xảy ra'
      }
    } else if (error.request) {
      return 'Lỗi kết nối mạng, vui lòng kiểm tra internet'
    } else {
      return error.message || 'Có lỗi không xác định'
    }
  },

  formatResponse: (response) => {
    return {
      data: response.data,
      status: response.status,
      message: response.data.message || 'Thành công',
    }
  },

  createQueryString: (params) => {
    const searchParams = new URLSearchParams()

    Object.keys(params).forEach((key) => {
      if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
        searchParams.append(key, params[key])
      }
    })

    return searchParams.toString()
  },
}
