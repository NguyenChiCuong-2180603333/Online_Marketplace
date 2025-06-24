import api from './api'

class RecommendationService {
  constructor() {
    this.baseURL = '/api/recommendations'
    
    this.cache = new Map()
    this.cacheTimeout = 5 * 60 * 1000 
  }

  async getPersonalRecommendations(limit = 20) {
    const cacheKey = `personal_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/for-you`, {
        params: { limit }
      })

      const data = response.data
      

      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting personal recommendations:', error)
      
      return await this.getTrendingRecommendations(limit)
    }
  }

  async getSimilarProducts(productId, limit = 10) {
    const cacheKey = `similar_${productId}_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/similar/${productId}`, {
        params: { limit }
      })

      const data = response.data
      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting similar products:', error)
      
      return {
        similarProducts: [],
        count: 0,
        baseProductId: productId,
        algorithm: 'fallback'
      }
    }
  }

  async getTrendingRecommendations(limit = 15) {
    const cacheKey = `trending_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/trending`, {
        params: { limit }
      })

      const data = response.data
      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting trending recommendations:', error)
      
      return {
        trending: [],
        count: 0,
        timeframe: 'last_7_days',
        algorithm: 'fallback'
      }
    }
  }

  async getCrossSellRecommendations(productId, limit = 8) {
    const cacheKey = `crosssell_${productId}_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/cross-sell/${productId}`, {
        params: { limit }
      })

      const data = response.data
      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting cross-sell recommendations:', error)
      
      const fallback = await this.getSimilarProducts(productId, limit)
      return {
        crossSellProducts: fallback.similarProducts || [],
        count: fallback.count || 0,
        algorithm: 'fallback_similar'
      }
    }
  }

  async getRecommendationsByPriceRange(minPrice, maxPrice, limit = 15) {
    const cacheKey = `price_${minPrice}_${maxPrice}_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/price-range`, {
        params: { minPrice, maxPrice, limit }
      })

      const data = response.data
      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting price-range recommendations:', error)
      
      const personal = await this.getPersonalRecommendations(limit * 2)
      const filtered = personal.recommendations?.filter(
        p => p.price >= minPrice && p.price <= maxPrice
      ).slice(0, limit) || []
      
      return {
        recommendations: filtered,
        priceRange: { min: minPrice, max: maxPrice },
        count: filtered.length,
        algorithm: 'fallback_filtered'
      }
    }
  }

  async getCategoryRecommendations(category, limit = 10) {
    const cacheKey = `category_${category}_${limit}`
    
    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/category/${category}`, {
        params: { limit }
      })

      const data = response.data
      this.setCache(cacheKey, data)
      
      return data
    } catch (error) {
      console.error('❌ Error getting category recommendations:', error)
      
      return {
        recommendations: [],
        category,
        count: 0,
        algorithm: 'fallback'
      }
    }
  }


  async trackInteraction(productId, interactionType, metadata = {}) {
    
    try {
      const payload = {
        productId,
        interactionType: interactionType.toUpperCase(),
        timestamp: new Date().toISOString(),
        metadata: {
          source: 'web_app',
          userAgent: navigator.userAgent,
          page: window.location.pathname,
          ...metadata
        }
      }

      const response = await api.post(`${this.baseURL}/track`, payload)
      
      this.clearUserRelatedCaches()
      
      return response.data
    } catch (error) {
      console.error('❌ Error tracking interaction:', error)
      
      return { success: false, error: error.message }
    }
  }


  async refreshUserPreferences() {
    try {
      const response = await api.post(`${this.baseURL}/refresh-preferences`)
      
      this.clearAllCaches()
      
      return response.data
    } catch (error) {
      console.error('❌ Error refreshing preferences:', error)
      throw error
    }
  }

  hasValidCache(key) {
    const cached = this.cache.get(key)
    if (!cached) return false
    
    const now = Date.now()
    return (now - cached.timestamp) < this.cacheTimeout
  }

  setCache(key, data) {
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    })
  }

  clearUserRelatedCaches() {
    const userCacheKeys = ['personal_', 'crosssell_', 'category_', 'price_']
    
    for (const [key] of this.cache) {
      if (userCacheKeys.some(prefix => key.startsWith(prefix))) {
        this.cache.delete(key)
      }
    }
  }


  clearAllCaches() {
    this.cache.clear()
  }

  
  trackView(productId, source = 'product_list') {
    return this.trackInteraction(productId, 'VIEW', { source })
  }

  trackClick(productId, source = 'product_card') {
    return this.trackInteraction(productId, 'CLICK', { source })
  }

  trackAddToCart(productId, quantity = 1) {
    return this.trackInteraction(productId, 'ADD_TO_CART', { quantity })
  }

  trackPurchase(productId, orderValue, orderId) {
    return this.trackInteraction(productId, 'PURCHASE', { 
      orderValue, 
      orderId,
      conversionSource: 'recommendation'
    })
  }

  trackSearch(searchQuery, resultCount) {
    return this.trackInteraction(null, 'SEARCH', { 
      searchQuery, 
      resultCount,
      timestamp: new Date().toISOString()
    })
  }
}

const recommendationService = new RecommendationService()

export default recommendationService

export const {
  getPersonalRecommendations,
  getSimilarProducts,
  getTrendingRecommendations,
  getCrossSellRecommendations,
  getRecommendationsByPriceRange,
  getCategoryRecommendations,
  trackInteraction,
  trackView,
  trackClick,
  trackAddToCart,
  trackPurchase,
  trackSearch
} = recommendationService