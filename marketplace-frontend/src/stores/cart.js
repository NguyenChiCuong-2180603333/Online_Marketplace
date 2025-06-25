import { defineStore } from 'pinia'
import { cartAPI } from '@/services/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cart: null,
    loading: false,
    error: null
  }),

  getters: {
    items: (state) => state.cart?.items || [],
    totalItems: (state) => state.cart?.totalItems || 0,
    totalAmount: (state) => state.cart?.totalAmount || 0,
    isEmpty: (state) => !state.cart?.items?.length
  },

  actions: {
    async loadCart() {
  this.loading = true
  this.error = null
  
  try {
    const response = await cartAPI.getCart() 
    this.cart = response.data
    console.log('✅ Cart loaded from API:', response.data)
  } catch (error) {
    this.error = error.response?.data?.message || 'Không thể tải giỏ hàng'
    console.error('❌ Load cart error:', error)
  } finally {
    this.loading = false
  }
},

    async addItem(productId, quantity = 1) {
      this.loading = true
      this.error = null
      
      try {
        const response = await cartAPI.addItem(productId, quantity)
        this.cart = response.data
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Không thể thêm sản phẩm vào giỏ hàng'
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateItemQuantity(productId, quantity) {
      this.loading = true
      this.error = null
      
      try {
        const response = await cartAPI.updateItem(productId, quantity)
        this.cart = response.data
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Không thể cập nhật số lượng'
        throw error
      } finally {
        this.loading = false
      }
    },

    async removeItem(productId) {
      this.loading = true
      this.error = null
      
      try {
        const response = await cartAPI.removeItem(productId)
        this.cart = response.data
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Không thể xóa sản phẩm'
        throw error
      } finally {
        this.loading = false
      }
    },

    async clearCart() {
      this.loading = true
      this.error = null
      
      try {
        await cartAPI.clear()
        this.cart = { items: [], totalItems: 0, totalAmount: 0 }
      } catch (error) {
        this.error = error.response?.data?.message || 'Không thể xóa giỏ hàng'
        throw error
      } finally {
        this.loading = false
      }
    },

    async validateCart() {
      try {
        const response = await cartAPI.validate()
        return response.data.valid
      } catch (error) {
        this.error = error.response?.data?.message || 'Không thể kiểm tra giỏ hàng'
        return false
      }
    },

    clearError() {
      this.error = null
    }
  }
})