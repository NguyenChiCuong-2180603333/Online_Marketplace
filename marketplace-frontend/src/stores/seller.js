import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { productAPI, orderAPI, profileAPI } from '@/services/api'

export const useSellerStore = defineStore('seller', () => {
  const products = ref([])
  const orders = ref([])
  const stats = ref({
    totalProducts: 0,
    activeProducts: 0,
    totalOrders: 0,
    pendingOrders: 0,
    completedOrders: 0,
    totalRevenue: 0,
    thisMonthRevenue: 0,
    lowStockProducts: 0
  })
  
  const loading = ref({
    products: false,
    orders: false,
    stats: false,
    dashboard: false
  })
  
  const errors = ref({
    products: null,
    orders: null,
    stats: null,
    dashboard: null
  })

  const activeProducts = computed(() => 
    products.value.filter(p => p.active === true)
  )
  
  const inactiveProducts = computed(() =>
    products.value.filter(p => p.active === false)
  )
  
  const lowStockProducts = computed(() =>
    products.value.filter(p => p.stockQuantity <= 5)
  )
  
  const totalRevenue = computed(() =>
    orders.value
      .filter(o => o.status === 'DELIVERED')
      .reduce((sum, o) => sum + (o.totalAmount || 0), 0)
  )
  
  const pendingOrders = computed(() =>
    orders.value.filter(o => o.status === 'PENDING')
  )
  
  const processingOrders = computed(() =>
    orders.value.filter(o => o.status === 'PROCESSING')
  )
  
  const recentOrders = computed(() =>
    orders.value
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 5)
  )

  const loadMyProducts = async () => {
    loading.value.products = true
    errors.value.products = null
    
    try {
      const response = await profileAPI.getMyProducts()
      products.value = response.data || []
      
      stats.value.totalProducts = products.value.length
      stats.value.activeProducts = activeProducts.value.length
      stats.value.lowStockProducts = lowStockProducts.value.length
      
    } catch (error) {
      console.error('Load my products error:', error)
      errors.value.products = error.response?.data?.message || 'Không thể tải sản phẩm'
    } finally {
      loading.value.products = false
    }
  }

  const loadSellerOrders = async () => {
    loading.value.orders = true
    errors.value.orders = null
    
    try {
      const response = await orderAPI.getSellerOrders()
      orders.value = response.data || []
      
      stats.value.totalOrders = orders.value.length
      stats.value.pendingOrders = pendingOrders.value.length
      stats.value.completedOrders = orders.value.filter(o => o.status === 'DELIVERED').length
      stats.value.totalRevenue = totalRevenue.value
      
    } catch (error) {
      console.error('Load seller orders error:', error)
      errors.value.orders = error.response?.data?.message || 'Không thể tải đơn hàng'
    } finally {
      loading.value.orders = false
    }
  }

  const loadDashboardStats = async () => {
    loading.value.dashboard = true
    errors.value.dashboard = null
    
    try {
      await Promise.all([
        loadMyProducts(),
        loadSellerOrders()
      ])
      
      const currentMonth = new Date().getMonth()
      const currentYear = new Date().getFullYear()
      
      stats.value.thisMonthRevenue = orders.value
        .filter(o => {
          const orderDate = new Date(o.createdAt)
          return o.status === 'DELIVERED' && 
                 orderDate.getMonth() === currentMonth && 
                 orderDate.getFullYear() === currentYear
        })
        .reduce((sum, o) => sum + (o.totalAmount || 0), 0)
        
    } catch (error) {
      console.error('Load dashboard stats error:', error)
      errors.value.dashboard = error.response?.data?.message || 'Không thể tải thống kê'
    } finally {
      loading.value.dashboard = false
    }
  }

  const createProduct = async (productData) => {
    try {
      const response = await productAPI.create(productData)
      products.value.push(response.data)
      
      stats.value.totalProducts++
      if (response.data.active) {
        stats.value.activeProducts++
      }
      
      return response.data
    } catch (error) {
      console.error('Create product error:', error)
      throw error
    }
  }

  const updateProduct = async (productId, productData) => {
    try {
      const response = await productAPI.update(productId, productData)
      
      const index = products.value.findIndex(p => p.id === productId)
      if (index !== -1) {
        products.value[index] = response.data
      }
      
      stats.value.activeProducts = activeProducts.value.length
      stats.value.lowStockProducts = lowStockProducts.value.length
      
      return response.data
    } catch (error) {
      console.error('Update product error:', error)
      throw error
    }
  }

  const deleteProduct = async (productId) => {
    try {
      await productAPI.delete(productId)
      
      const deletedProduct = products.value.find(p => p.id === productId)
      products.value = products.value.filter(p => p.id !== productId)
      
      stats.value.totalProducts--
      if (deletedProduct && deletedProduct.active) {
        stats.value.activeProducts--
      }
      
    } catch (error) {
      console.error('Delete product error:', error)
      throw error
    }
  }

  const toggleProductStatus = async (productId) => {
    try {
      const product = products.value.find(p => p.id === productId)
      if (!product) return
      
      const updatedProduct = await updateProduct(productId, { 
        ...product, 
        active: !product.active 
      })
      
      return updatedProduct
    } catch (error) {
      console.error('Toggle product status error:', error)
      throw error
    }
  }

  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      const response = await orderAPI.updateStatus(orderId, newStatus)
      
      const index = orders.value.findIndex(o => o.id === orderId)
      if (index !== -1) {
        orders.value[index].status = newStatus
      }
      
      stats.value.pendingOrders = pendingOrders.value.length
      stats.value.completedOrders = orders.value.filter(o => o.status === 'DELIVERED').length
      stats.value.totalRevenue = totalRevenue.value
      
      return response.data
    } catch (error) {
      console.error('Update order status error:', error)
      throw error
    }
  }

  const $reset = () => {
    products.value = []
    orders.value = []
    stats.value = {
      totalProducts: 0,
      activeProducts: 0,
      totalOrders: 0,
      pendingOrders: 0,
      completedOrders: 0,
      totalRevenue: 0,
      thisMonthRevenue: 0,
      lowStockProducts: 0
    }
    loading.value = {
      products: false,
      orders: false,
      stats: false,
      dashboard: false
    }
    errors.value = {
      products: null,
      orders: null,
      stats: null,
      dashboard: null
    }
  }

  return {
    products,
    orders,
    stats,
    loading,
    errors,

    activeProducts,
    inactiveProducts,
    lowStockProducts,
    totalRevenue,
    pendingOrders,
    processingOrders,
    recentOrders,

    loadMyProducts,
    loadSellerOrders,
    loadDashboardStats,
    createProduct,
    updateProduct,
    deleteProduct,
    toggleProductStatus,
    updateOrderStatus,
    $reset
  }
})