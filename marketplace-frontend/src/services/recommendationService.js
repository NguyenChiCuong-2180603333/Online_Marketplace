import api from './api'

class RecommendationService {
  constructor() {
    this.baseURL = '/recommendations'

    this.cache = new Map()
    this.cacheTimeout = 5 * 60 * 1000 // 5 minutes
  }

  async getPersonalRecommendations(limit = 20) {
    const cacheKey = `personal_${limit}`

    if (this.hasValidCache(cacheKey)) {
      return this.cache.get(cacheKey).data
    }

    try {
      const response = await api.get(`${this.baseURL}/for-you`, {
        params: { limit },
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
        params: { limit },
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
        algorithm: 'fallback',
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
        params: { limit },
      })

      const data = response.data
      this.setCache(cacheKey, data)

      return data
    } catch (error) {
      console.error('❌ Error getting trending recommendations:', error)

      return {
        recommendations: [],
        count: 0,
        algorithm: 'fallback',
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
        params: { limit },
      })

      const data = response.data
      this.setCache(cacheKey, data)

      return data
    } catch (error) {
      console.error('❌ Error getting cross-sell recommendations:', error)

      return {
        crossSellProducts: [],
        count: 0,
        baseProductId: productId,
        algorithm: 'fallback',
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
        params: { limit },
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
        algorithm: 'fallback',
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
        params: { minPrice, maxPrice, limit },
      })

      const data = response.data
      this.setCache(cacheKey, data)

      return data
    } catch (error) {
      console.error('❌ Error getting price range recommendations:', error)

      const trending = await this.getTrendingRecommendations(limit * 2)
      const filtered = (trending.recommendations || [])
        .filter((product) => product.price >= minPrice && product.price <= maxPrice)
        .slice(0, limit)

      return {
        recommendations: filtered,
        priceRange: { min: minPrice, max: maxPrice },
        count: filtered.length,
        algorithm: 'fallback_filtered',
      }
    }
  }

  async getRecentlyViewed(limit = 10) {
    try {
      const response = await api.get(`${this.baseURL}/recently-viewed`, {
        params: { limit },
      })

      return response.data
    } catch (error) {
      console.error('❌ Error getting recently viewed:', error)

      return {
        recentlyViewed: [],
        count: 0,
      }
    }
  }

  async trackInteraction(productId, interactionType, context = {}) {
    try {
      const payload = {
        productId,
        interactionType: interactionType.toUpperCase(),
        timestamp: new Date().toISOString(),
        sessionId: localStorage.getItem('sessionId') || null,
        context: {
          source: 'web_app',
          userAgent: navigator.userAgent,
          page: window.location.pathname,
          ...context,
        },
      }

      const response = await api.post(`${this.baseURL}/track`, payload)
      this.clearUserRelatedCaches()
      return response.data
    } catch (error) {
      console.error('❌ Error tracking interaction:', error, error.response?.data)
      return { success: false, error: error.message, details: error.response?.data }
    }
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
      conversionSource: 'recommendation',
    })
  }

  trackSearch(searchQuery, resultCount) {
    return this.trackInteraction(null, 'SEARCH', {
      searchQuery,
      resultCount,
      timestamp: new Date().toISOString(),
    })
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
    return now - cached.timestamp < this.cacheTimeout
  }

  setCache(key, data) {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
    })
  }

  clearUserRelatedCaches() {
    const userCacheKeys = ['personal_', 'crosssell_', 'category_', 'price_']

    for (const [key] of this.cache) {
      if (userCacheKeys.some((prefix) => key.startsWith(prefix))) {
        this.cache.delete(key)
      }
    }
  }

  clearAllCaches() {
    this.cache.clear()
  }

  async calculateSimilarities() {
    try {
      const response = await api.post(`${this.baseURL}/admin/calculate-similarities`)
      return response.data
    } catch (error) {
      console.error('❌ Error calculating similarities:', error)
      throw error
    }
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
  getRecentlyViewed,
  trackInteraction,
  trackView,
  trackClick,
  trackAddToCart,
  trackPurchase,
  trackSearch,
  refreshUserPreferences,
} = recommendationService
