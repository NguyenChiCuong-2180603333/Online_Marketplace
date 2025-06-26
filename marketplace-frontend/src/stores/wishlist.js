import { defineStore } from 'pinia'

export const useWishlistStore = defineStore('wishlist', {
  state: () => ({
    items: JSON.parse(localStorage.getItem('wishlist')) || [],
    loading: false,
    error: null
  }),

  getters: {
    totalItems: (state) => state.items.length,
    
    isInWishlist: (state) => (productId) => {
      return state.items.some(item => item.id === productId)
    }
  },

  actions: {
    addToWishlist(product) {
      if (!this.isInWishlist(product.id)) {
        this.items.push({
          id: product.id,
          name: product.name,
          price: product.price,
          imageUrl: product.imageUrl,
          category: product.category,
          addedAt: new Date().toISOString()
        })
        this.saveToStorage()
      }
    },

    removeFromWishlist(productId) {
      this.items = this.items.filter(item => item.id !== productId)
      this.saveToStorage()
    },

    toggleWishlist(product) {
      if (this.isInWishlist(product.id)) {
        this.removeFromWishlist(product.id)
      } else {
        this.addToWishlist(product)
      }
    },

    clearWishlist() {
      this.items = []
      this.saveToStorage()
    },

    saveToStorage() {
      localStorage.setItem('wishlist', JSON.stringify(this.items))
    },

    loadFromStorage() {
      const stored = localStorage.getItem('wishlist')
      if (stored) {
        this.items = JSON.parse(stored)
      }
    }
  }
}) 