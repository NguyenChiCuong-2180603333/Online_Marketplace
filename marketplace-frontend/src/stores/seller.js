import { defineStore } from 'pinia'
import { profileAPI } from '@/services/api'

export const useSellerStore = defineStore('seller', {
  state: () => ({
    // Dashboard Stats
    dashboardStats: {
      totalProducts: 0,
      activeProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      totalRevenue: 0,
      monthlyRevenue: 0,
      revenueGrowth: 0,
      averageOrderValue: 0
    },
    
    // Products Management
    products: [],
    productStats: {
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0
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
    
    // Analytics
    analytics: {
      salesChart: [],
      topProducts: [],
      categoryBreakdown: [],
      monthlyStats: []
    },
    
    // Loading states
    loading: {
      dashboard: false,
      products: false,
      orders: false,
      analytics: false
    },
    
    // Error states
    errors: {
      dashboard: null,
      products: null,
      orders: null,
      analytics: null
    },
    
    // Product form data
    productForm: {
      id: null,
      name: '',
      description: '',
      category: '',
      price: 0,
      stockQuantity: 0,
      images: [],
      tags: [],
      seoTitle: '',
      seoDescription: '',
      weight: 0,
      dimensions: {
        length: 0,
        width: 0,
        height: 0
      },
      shippingInfo: {
        freeShipping: false,
        shippingCost: 0,
        processingTime: '1-2 days'
      },
      variants: [],
      isActive: true
    }
  }),

  getters: {
    // Dashboard getters
    totalRevenue: (state) => state.dashboardStats.totalRevenue,
    monthlyGrowth: (state) => state.dashboardStats.revenueGrowth,
    
    // Product getters
    activeProducts: (state) => state.products.filter(p => p.isActive),
    inactiveProducts: (state) => state.products.filter(p => !p.isActive),
    lowStockProducts: (state) => state.products.filter(p => p.stockQuantity < 10),
    
    // Order getters
    pendingOrders: (state) => state.orders.filter(o => o.status === 'pending'),
    processingOrders: (state) => state.orders.filter(o => o.status === 'processing'),
    
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
    }
  },

  actions: {
    // Dashboard Actions
    async fetchDashboardStats() {
      this.loading.dashboard = true
      this.errors.dashboard = null
      
      try {
        const response = await profileAPI.getSellerStats()
        this.dashboardStats = response.data
        
        // Calculate derived stats
        this.updateProductStats()
        this.updateOrderStats()
        
      } catch (error) {
        this.errors.dashboard = error.message
        console.error('Error fetching dashboard stats:', error)
      } finally {
        this.loading.dashboard = false
      }
    },

    // Product Actions
    async fetchProducts() {
      this.loading.products = true
      this.errors.products = null
      
      try {
        const response = await profileAPI.getMyProducts()
        this.products = response.data || []
        this.updateProductStats()
        
      } catch (error) {
        this.errors.products = error.message
        console.error('Error fetching products:', error)
        
        // Fallback with sample data for development
        this.products = this.generateSampleProducts()
        this.updateProductStats()
      } finally {
        this.loading.products = false
      }
    },

    async createProduct(productData) {
      try {
        // TODO: Implement with real API when backend is ready
        // const response = await productAPI.create(productData)
        
        // Mock implementation for now
        const newProduct = {
          id: Date.now().toString(),
          ...productData,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          soldCount: 0,
          reviewCount: 0,
          averageRating: 0
        }
        
        this.products.unshift(newProduct)
        this.updateProductStats()
        
        return newProduct
      } catch (error) {
        console.error('Error creating product:', error)
        throw error
      }
    },

    async updateProduct(productId, productData) {
      try {
        // TODO: Implement with real API
        // const response = await productAPI.update(productId, productData)
        
        // Mock implementation
        const index = this.products.findIndex(p => p.id === productId)
        if (index !== -1) {
          this.products[index] = {
            ...this.products[index],
            ...productData,
            updatedAt: new Date().toISOString()
          }
          this.updateProductStats()
        }
        
        return this.products[index]
      } catch (error) {
        console.error('Error updating product:', error)
        throw error
      }
    },

    async toggleProductStatus(productId) {
      try {
        // TODO: Implement with real API
        // await productAPI.toggleStatus(productId)
        
        // Mock implementation
        const product = this.products.find(p => p.id === productId)
        if (product) {
          product.isActive = !product.isActive
          product.updatedAt = new Date().toISOString()
          this.updateProductStats()
        }
        
      } catch (error) {
        console.error('Error toggling product status:', error)
        throw error
      }
    },

    async deleteProduct(productId) {
      try {
        // TODO: Implement with real API
        // await productAPI.delete(productId)
        
        // Mock implementation
        const index = this.products.findIndex(p => p.id === productId)
        if (index !== -1) {
          this.products.splice(index, 1)
          this.updateProductStats()
        }
        
      } catch (error) {
        console.error('Error deleting product:', error)
        throw error
      }
    },

    async duplicateProduct(productId) {
      try {
        const originalProduct = this.products.find(p => p.id === productId)
        if (!originalProduct) throw new Error('Product not found')
        
        const duplicatedProduct = {
          ...originalProduct,
          id: Date.now().toString(),
          name: `${originalProduct.name} (Copy)`,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          soldCount: 0,
          reviewCount: 0,
          averageRating: 0
        }
        
        this.products.unshift(duplicatedProduct)
        this.updateProductStats()
        
        return duplicatedProduct
      } catch (error) {
        console.error('Error duplicating product:', error)
        throw error
      }
    },

    // Bulk Actions
    async bulkUpdateStatus(productIds, isActive) {
      try {
        // TODO: Implement with real API
        // await productAPI.bulkUpdateStatus(productIds, isActive)
        
        // Mock implementation
        productIds.forEach(id => {
          const product = this.products.find(p => p.id === id)
          if (product) {
            product.isActive = isActive
            product.updatedAt = new Date().toISOString()
          }
        })
        
        this.updateProductStats()
        
      } catch (error) {
        console.error('Error bulk updating status:', error)
        throw error
      }
    },

    async bulkDelete(productIds) {
      try {
        // TODO: Implement with real API
        // await productAPI.bulkDelete(productIds)
        
        // Mock implementation
        this.products = this.products.filter(p => !productIds.includes(p.id))
        this.updateProductStats()
        
      } catch (error) {
        console.error('Error bulk deleting products:', error)
        throw error
      }
    },

    // Order Actions
    async fetchOrders() {
      this.loading.orders = true
      this.errors.orders = null
      
      try {
        // TODO: Implement with real API
        // const response = await orderAPI.getSellerOrders()
        // this.orders = response.data || []
        
        // Mock implementation
        this.orders = this.generateSampleOrders()
        this.updateOrderStats()
        
      } catch (error) {
        this.errors.orders = error.message
        console.error('Error fetching orders:', error)
      } finally {
        this.loading.orders = false
      }
    },

    async updateOrderStatus(orderId, status) {
      try {
        // TODO: Implement with real API
        // await orderAPI.updateStatus(orderId, status)
        
        // Mock implementation
        const order = this.orders.find(o => o.id === orderId)
        if (order) {
          order.status = status
          order.updatedAt = new Date().toISOString()
          this.updateOrderStats()
        }
        
      } catch (error) {
        console.error('Error updating order status:', error)
        throw error
      }
    },

    // Analytics Actions
    async fetchAnalytics(period = '30d') {
      this.loading.analytics = true
      this.errors.analytics = null
      
      try {
        // TODO: Implement with real API
        // const response = await analyticsAPI.getSellerAnalytics(period)
        // this.analytics = response.data
        
        // Mock implementation
        this.analytics = this.generateSampleAnalytics()
        
      } catch (error) {
        this.errors.analytics = error.message
        console.error('Error fetching analytics:', error)
      } finally {
        this.loading.analytics = false
      }
    },

    // Product Form Actions
    initializeProductForm(product = null) {
      if (product) {
        this.productForm = {
          id: product.id,
          name: product.name || '',
          description: product.description || '',
          category: product.category || '',
          price: product.price || 0,
          stockQuantity: product.stockQuantity || 0,
          images: product.images || [],
          tags: product.tags || [],
          seoTitle: product.seoTitle || '',
          seoDescription: product.seoDescription || '',
          weight: product.weight || 0,
          dimensions: product.dimensions || { length: 0, width: 0, height: 0 },
          shippingInfo: product.shippingInfo || {
            freeShipping: false,
            shippingCost: 0,
            processingTime: '1-2 days'
          },
          variants: product.variants || [],
          isActive: product.isActive !== undefined ? product.isActive : true
        }
      } else {
        this.productForm = {
          id: null,
          name: '',
          description: '',
          category: '',
          price: 0,
          stockQuantity: 0,
          images: [],
          tags: [],
          seoTitle: '',
          seoDescription: '',
          weight: 0,
          dimensions: { length: 0, width: 0, height: 0 },
          shippingInfo: {
            freeShipping: false,
            shippingCost: 0,
            processingTime: '1-2 days'
          },
          variants: [],
          isActive: true
        }
      }
    },

    clearProductForm() {
      this.initializeProductForm()
    },

    // Utility Actions
    updateProductStats() {
      this.productStats = {
        total: this.products.length,
        active: this.products.filter(p => p.isActive).length,
        inactive: this.products.filter(p => !p.isActive).length,
        lowStock: this.products.filter(p => p.stockQuantity < 10).length
      }
    },

    updateOrderStats() {
      this.orderStats = {
        total: this.orders.length,
        pending: this.orders.filter(o => o.status === 'pending').length,
        processing: this.orders.filter(o => o.status === 'processing').length,
        shipped: this.orders.filter(o => o.status === 'shipped').length,
        delivered: this.orders.filter(o => o.status === 'delivered').length,
        cancelled: this.orders.filter(o => o.status === 'cancelled').length
      }
    },

    // Sample data generators (for development)
    generateSampleProducts() {
      const categories = ['Điện tử', 'Thời trang', 'Nhà cửa', 'Sách', 'Thể thao']
      const statuses = [true, true, true, false, true] // mostly active
      
      return Array.from({ length: 12 }, (_, index) => ({
        id: `product_${index + 1}`,
        name: `Sản phẩm mẫu ${index + 1}`,
        description: `Mô tả chi tiết cho sản phẩm mẫu ${index + 1}. Đây là một sản phẩm chất lượng cao với nhiều tính năng ưu việt.`,
        category: categories[index % categories.length],
        price: Math.floor(Math.random() * 1000000) + 100000,
        stockQuantity: Math.floor(Math.random() * 100) + 1,
        images: [
          '/api/placeholder/300/300',
          '/api/placeholder/300/300'
        ],
        tags: ['hot', 'new', 'featured'].slice(0, Math.floor(Math.random() * 3) + 1),
        isActive: statuses[index % statuses.length],
        soldCount: Math.floor(Math.random() * 50),
        reviewCount: Math.floor(Math.random() * 20),
        averageRating: Math.round((Math.random() * 2 + 3) * 10) / 10,
        createdAt: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
        updatedAt: new Date().toISOString()
      }))
    },

    generateSampleOrders() {
      const statuses = ['pending', 'processing', 'shipped', 'delivered', 'cancelled']
      
      return Array.from({ length: 8 }, (_, index) => ({
        id: `order_${index + 1}`,
        orderNumber: `#ORD${String(index + 1).padStart(4, '0')}`,
        customerName: `Khách hàng ${index + 1}`,
        customerEmail: `customer${index + 1}@example.com`,
        status: statuses[index % statuses.length],
        total: Math.floor(Math.random() * 2000000) + 100000,
        items: Math.floor(Math.random() * 3) + 1,
        createdAt: new Date(Date.now() - Math.random() * 5000000000).toISOString(),
        updatedAt: new Date().toISOString()
      }))
    },

    generateSampleAnalytics() {
      const now = new Date()
      const salesChart = Array.from({ length: 30 }, (_, index) => ({
        date: new Date(now.getTime() - (29 - index) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        sales: Math.floor(Math.random() * 500000) + 100000,
        orders: Math.floor(Math.random() * 20) + 5
      }))

      const topProducts = this.products
        .sort((a, b) => (b.soldCount || 0) - (a.soldCount || 0))
        .slice(0, 5)
        .map(product => ({
          id: product.id,
          name: product.name,
          sales: product.soldCount || 0,
          revenue: (product.soldCount || 0) * product.price
        }))

      return {
        salesChart,
        topProducts,
        categoryBreakdown: [
          { category: 'Điện tử', sales: 45, revenue: 2500000 },
          { category: 'Thời trang', sales: 38, revenue: 1800000 },
          { category: 'Nhà cửa', sales: 22, revenue: 1200000 },
          { category: 'Sách', sales: 15, revenue: 450000 },
          { category: 'Thể thao', sales: 18, revenue: 900000 }
        ],
        monthlyStats: Array.from({ length: 12 }, (_, index) => ({
          month: new Date(2024, index, 1).toLocaleDateString('vi-VN', { month: 'short' }),
          sales: Math.floor(Math.random() * 100) + 50,
          revenue: Math.floor(Math.random() * 5000000) + 1000000
        }))
      }
    }
  }
})