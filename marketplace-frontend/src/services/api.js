import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor để thêm JWT token
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

// Response interceptor để handle errors
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

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout')
}

// Products API
export const productAPI = {
  getAll: (params) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  create: (product) => api.post('/products', product),
  update: (id, product) => api.put(`/products/${id}`, product),
  delete: (id) => api.delete(`/products/${id}`),
  getFeatured: () => api.get('/products/featured'),
  getLatest: () => api.get('/products/latest'),
  getBySeller: (sellerId) => api.get(`/products/seller/${sellerId}`)
}

// Categories API
export const categoryAPI = {
  getAll: () => api.get('/categories'),
  getById: (id) => api.get(`/categories/${id}`),
  create: (category) => api.post('/categories', category),
  update: (id, category) => api.put(`/categories/${id}`, category),
  delete: (id) => api.delete(`/categories/${id}`),
  search: (query) => api.get(`/categories/search?q=${query}`)
}

// Cart API
export const cartAPI = {
  get: () => api.get('/cart'),
  addItem: (productId, quantity) => api.post('/cart/add', { productId, quantity }),
  updateItem: (productId, quantity) => api.put('/cart/update', { productId, quantity }),
  removeItem: (productId) => api.delete(`/cart/remove/${productId}`),
  clear: () => api.delete('/cart/clear'),
  validate: () => api.get('/cart/validate')
}

// Orders API
export const orderAPI = {
  create: (shippingAddress, billingAddress) => api.post('/orders/create', { shippingAddress, billingAddress }),
  getMyOrders: () => api.get('/orders/my-orders'),
  getById: (id) => api.get(`/orders/${id}`),
  cancel: (id) => api.put(`/orders/${id}/cancel`),
  getSellerOrders: () => api.get('/orders/seller/my-orders')
}

// Profile API - MISSING ENDPOINTS ADDED
export const profileAPI = {
  // Basic profile
  getProfile: () => api.get('/profile'),
  updateProfile: (profileData) => api.put('/profile', profileData),
  uploadAvatar: (formData) => api.post('/profile/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  changePassword: (passwordData) => api.put('/profile/change-password', passwordData),
  
  // User stats
  getUserStats: () => api.get('/profile/stats'),
  getPurchaseHistory: () => api.get('/profile/purchase-history'),
  getMyProducts: () => api.get('/profile/my-products'),
  
  // Wishlist
  getWishlist: () => api.get('/profile/wishlist'),
  addToWishlist: (productId) => api.post('/profile/wishlist', { productId }),
  removeFromWishlist: (productId) => api.delete(`/profile/wishlist/${productId}`),
  
  // Addresses
  getAddresses: () => api.get('/profile/addresses'),
  addAddress: (addressData) => api.post('/profile/addresses', addressData),
  updateAddress: (addressId, addressData) => api.put(`/profile/addresses/${addressId}`, addressData),
  deleteAddress: (addressId) => api.delete(`/profile/addresses/${addressId}`),
  setDefaultAddress: (addressId) => api.put(`/profile/addresses/${addressId}/default`),
  
  // Payment methods
  getPaymentMethods: () => api.get('/profile/payment-methods'),
  addPaymentMethod: (paymentData) => api.post('/profile/payment-methods', paymentData),
  deletePaymentMethod: (paymentMethodId) => api.delete(`/profile/payment-methods/${paymentMethodId}`),
  setDefaultPaymentMethod: (paymentMethodId) => api.put(`/profile/payment-methods/${paymentMethodId}/default`),
  
  // Preferences
  getPreferences: () => api.get('/profile/preferences'),
  updatePreferences: (preferencesData) => api.put('/profile/preferences', preferencesData),
  
  // Two-factor authentication
  enableTwoFactor: () => api.post('/profile/2fa/enable'),
  disableTwoFactor: (data) => api.post('/profile/2fa/disable', data),
  verifyTwoFactor: (data) => api.post('/profile/2fa/verify', data),
  
  // Account management
  requestAccountDeletion: () => api.post('/profile/delete-request'),
  cancelAccountDeletion: () => api.delete('/profile/delete-request'),
  exportUserData: () => api.get('/profile/export'),
  
  // Notifications
  getNotifications: () => api.get('/profile/notifications'),
  markNotificationAsRead: (notificationId) => api.put(`/profile/notifications/${notificationId}/read`),
  markAllNotificationsAsRead: () => api.put('/profile/notifications/read-all'),
  
  // Loyalty program
  getLoyaltyPoints: () => api.get('/profile/loyalty'),
  redeemLoyaltyPoints: (data) => api.post('/profile/loyalty/redeem', data)
}

// Payment API
export const paymentAPI = {
  createIntent: (orderId) => api.post('/payments/create-intent', { orderId }),
  confirm: (paymentIntentId) => api.post('/payments/confirm', { paymentIntentId }),
  cancel: (paymentIntentId) => api.post('/payments/cancel', { paymentIntentId }),
  getStatus: (paymentIntentId) => api.get(`/payments/status/${paymentIntentId}`)
}

// Admin API
export const adminAPI = {
  // Dashboard
  getDashboard: () => api.get('/admin/dashboard'),
  
  // Users management
  getUsers: () => api.get('/admin/users'),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
  
  // Products management  
  getProducts: () => api.get('/admin/products'),
  toggleProductStatus: (productId) => api.put(`/admin/products/${productId}/toggle-status`),
  deleteProduct: (productId) => api.delete(`/admin/products/${productId}`),
  
  // Orders management
  getOrders: () => api.get('/admin/orders'),
  updateOrderStatus: (orderId, status) => api.put(`/admin/orders/${orderId}/status`, { status }),
  
  // Analytics
  getAnalytics: (type) => api.get(`/admin/analytics/${type}`)
}

// Reviews API
export const reviewAPI = {
  getByProduct: (productId) => api.get(`/reviews/product/${productId}`),
  getMyReviews: () => api.get('/reviews/my-reviews'),
  create: (reviewData) => api.post('/reviews', reviewData),
  update: (reviewId, reviewData) => api.put(`/reviews/${reviewId}`, reviewData),
  delete: (reviewId) => api.delete(`/reviews/${reviewId}`),
  checkCanReview: (productId) => api.get(`/reviews/check/${productId}`),
  getStats: (productId) => api.get(`/reviews/stats/${productId}`)
}

// Search API
export const searchAPI = {
  products: (query, filters) => api.get('/search/products', { params: { q: query, ...filters } }),
  suggestions: (query) => api.get('/search/suggestions', { params: { q: query } })
}

// Recommendations API
export const recommendationAPI = {
  forUser: (limit = 20) => api.get('/recommendations/for-you', { params: { limit } }),
  similar: (productId, limit = 10) => api.get(`/recommendations/similar/${productId}`, { params: { limit } }),
  trending: () => api.get('/recommendations/trending'),
  crossSell: (productId) => api.get(`/recommendations/cross-sell/${productId}`),
  trackInteraction: (data) => api.post('/recommendations/track', data)
}

export default api