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

// Payment API
export const paymentAPI = {
  createIntent: (orderId) => api.post('/payments/create-intent', { orderId }),
  confirm: (paymentIntentId) => api.post('/payments/confirm', { paymentIntentId }),
  cancel: (paymentIntentId) => api.post('/payments/cancel', { paymentIntentId }),
  getStatus: (paymentIntentId) => api.get(`/payments/status/${paymentIntentId}`)
}

// Admin API
export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: () => api.get('/admin/users'),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
  getProducts: () => api.get('/admin/products'),
  toggleProductStatus: (productId) => api.put(`/admin/products/${productId}/toggle-status`),
  deleteProduct: (productId) => api.delete(`/admin/products/${productId}`),
  getOrders: () => api.get('/admin/orders'),
  updateOrderStatus: (orderId, status) => api.put(`/admin/orders/${orderId}/status`, { status }),
  getAnalytics: (type) => api.get(`/admin/analytics/${type}`)
}

export default api