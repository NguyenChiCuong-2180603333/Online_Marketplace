import api from './api.js'

export const adminAPI = {
  // Dashboard
  getDashboardStats: (timeRange = 30) => api.get(`/admin/dashboard?timeRange=${timeRange}`),

  // Users
  getUsers: () => api.get('/admin/users'),
  getUserById: (userId) => api.get(`/admin/users/${userId}`),
  getUserStats: () => api.get('/admin/users/stats'),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
  updateUserRole: (userId, role) => api.put(`/admin/users/${userId}/role`, { role }),
  toggleVipStatus: (userId) => api.put(`/admin/users/${userId}/vip`),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  createUser: (userData) => api.post('/admin/users', userData),
  updateUser: (userId, userData) => api.put(`/admin/users/${userId}`, userData),

  // Products
  getProducts: () => api.get('/admin/products'),
  toggleProductStatus: (productId) => api.put(`/admin/products/${productId}/toggle-status`),
  deleteProduct: (productId) => api.delete(`/admin/products/${productId}`),

  // Orders
  getOrders: () => api.get('/admin/orders'),
  getOrdersByStatus: (status) => api.get(`/admin/orders/status/${status}`),
  updateOrderStatus: (orderId, status) => api.put(`/admin/orders/${orderId}/status`, { status }),

  // Categories
  getCategories: () => api.get('/admin/categories'),
  createCategory: (categoryData) => api.post('/categories', categoryData),
  updateCategory: (id, categoryData) => api.put(`/categories/${id}`, categoryData),
  toggleCategoryStatus: (id) => api.patch(`/admin/categories/${id}/toggle-status`),
  deleteCategory: (id) => api.delete(`/categories/${id}`),

  // Analytics
  getRevenueAnalytics: () => api.get('/admin/analytics/revenue'),
  getOrderAnalytics: () => api.get('/admin/analytics/orders'),
  getProductAnalytics: () => api.get('/admin/analytics/products'),
  getUserAnalytics: () => api.get('/admin/analytics/users'),
}
