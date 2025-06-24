<template>
  <div class="trending-products">
    <!-- Section Header -->
    <div class="section-header">
      <div class="header-content">
        <h2 class="section-title">
          <span class="trending-icon">🔥</span>
          Đang Hot nhất
        </h2>
        <p class="section-subtitle">
          Sản phẩm được yêu thích và mua nhiều nhất tuần này
        </p>
      </div>
      
      <!-- Trending Stats -->
      <div class="trending-stats">
        <div class="stat-item">
          <span class="stat-icon">👁️</span>
          <span class="stat-text">{{ formatNumber(totalViews) }} lượt xem</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">🛒</span>
          <span class="stat-text">{{ formatNumber(totalPurchases) }} lượt mua</span>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-header">
        <div class="loading-title"></div>
        <div class="loading-subtitle"></div>
      </div>
      <div class="loading-grid">
        <div v-for="n in limit" :key="n" class="loading-card">
          <div class="loading-rank"></div>
          <div class="loading-image"></div>
          <div class="loading-content">
            <div class="loading-line short"></div>
            <div class="loading-line medium"></div>
            <div class="loading-line long"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-message">
        <span class="error-icon">⚠️</span>
        <h3>Không thể tải sản phẩm trending</h3>
        <p>{{ error }}</p>
        <button @click="loadTrendingProducts" class="retry-btn">
          Thử lại
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="trendingProducts.length === 0" class="empty-container">
      <div class="empty-message">
        <span class="empty-icon">📊</span>
        <h3>Chưa có dữ liệu trending</h3>
        <p>Dữ liệu xu hướng sẽ được cập nhật sớm!</p>
      </div>
    </div>

    <!-- Trending Products Grid -->
    <div v-else class="products-container">
      <!-- Time indicator -->
      <div class="time-indicator">
        <span class="time-icon">⏰</span>
        <span class="time-text">Cập nhật {{ formatRelativeTime(lastUpdated) }}</span>
        <span class="auto-refresh" v-if="autoRefreshEnabled">
          • Tự động làm mới sau {{ Math.ceil(nextRefreshIn / 1000) }}s
        </span>
      </div>

      <!-- Products Grid -->
      <div class="trending-grid">
        <div 
          v-for="(product, index) in trendingProducts" 
          :key="product.id"
          class="trending-product-card"
          @click="handleProductClick(product, index)"
        >
          <!-- Trending Rank -->
          <div class="trending-rank" :class="getRankClass(index)">
            <span class="rank-number">{{ index + 1 }}</span>
            <span class="rank-label">{{ getRankLabel(index) }}</span>
          </div>

          <!-- Product Image -->
          <div class="product-image-container">
            <img 
              :src="product.imageUrl || '/api/placeholder/product'" 
              :alt="product.name"
              class="product-image"
              @error="handleImageError"
              loading="lazy"
            />
            
            <!-- Trending badges -->
            <div class="trending-badges">
              <span v-if="product.isRising" class="badge badge-rising" title="Đang tăng">
                📈 +{{ product.growthRate }}%
              </span>
              <span v-if="product.isHot" class="badge badge-hot" title="Rất hot">
                🔥 HOT
              </span>
              <span v-if="product.discount > 0" class="badge badge-discount">
                -{{ product.discount }}%
              </span>
            </div>

            <!-- Quick view overlay -->
            <div class="quick-overlay">
              <button 
                @click.stop="quickView(product)"
                class="quick-view-btn"
                title="Xem nhanh"
              >
                👁️ Xem nhanh
              </button>
              
              <button 
                @click.stop="addToWishlist(product)"
                class="wishlist-btn"
                :class="{ 'active': isInWishlist(product.id) }"
                title="Yêu thích"
              >
                {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
              </button>
            </div>
          </div>

          <!-- Product Info -->
          <div class="product-info">
            <!-- Category & trending info -->
            <div class="product-meta">
              <span class="category-tag">{{ product.category }}</span>
              <span class="trending-score" :title="`Điểm trending: ${product.trendingScore}`">
                ⚡ {{ product.trendingScore || Math.floor(Math.random() * 100 + 50) }}
              </span>
            </div>

            <!-- Product name -->
            <h3 class="product-name" :title="product.name">
              {{ truncate(product.name, 45) }}
            </h3>

            <!-- Pricing -->
            <div class="product-pricing">
              <span class="current-price">
                {{ formatPrice(product.price) }}
              </span>
              <span v-if="product.originalPrice && product.originalPrice > product.price" 
                    class="original-price">
                {{ formatPrice(product.originalPrice) }}
              </span>
            </div>

            <!-- Rating & reviews -->
            <div class="product-rating" v-if="product.rating || product.reviewCount">
              <div class="stars">
                <span v-for="star in 5" :key="star" 
                      class="star"
                      :class="{ 'filled': star <= (product.rating || 0) }">
                  ⭐
                </span>
              </div>
              <span class="rating-text">
                {{ (product.rating || 0).toFixed(1) }}
                <span v-if="product.reviewCount" class="review-count">
                  ({{ formatNumber(product.reviewCount) }})
                </span>
              </span>
            </div>

            <!-- Trending stats -->
            <div class="trending-stats-mini">
              <div class="stat-mini" v-if="product.totalViews">
                <span class="stat-icon-mini">👁️</span>
                <span class="stat-value-mini">{{ formatNumber(product.totalViews) }}</span>
              </div>
              <div class="stat-mini" v-if="product.totalSales">
                <span class="stat-icon-mini">🛒</span>
                <span class="stat-value-mini">{{ formatNumber(product.totalSales) }}</span>
              </div>
              <div class="stat-mini" v-if="product.totalWishlisted">
                <span class="stat-icon-mini">❤️</span>
                <span class="stat-value-mini">{{ formatNumber(product.totalWishlisted) }}</span>
              </div>
            </div>

            <!-- Action button -->
            <button 
              @click.stop="handleAddToCart(product)"
              class="add-to-cart-btn"
              :disabled="!product.inStock"
            >
              <span v-if="!product.inStock">😞 Hết hàng</span>
              <span v-else>🛒 Thêm vào giỏ</span>
            </button>
          </div>
        </div>
      </div>

      <!-- View All Button -->
      <div class="view-all-container" v-if="hasMoreProducts">
        <button @click="viewAllTrending" class="view-all-btn">
          Xem tất cả sản phẩm trending →
        </button>
      </div>

      <!-- Sign up prompt for better recommendations -->
      <div class="signup-prompt">
        <div class="prompt-content">
          <h4>🎯 Nhận đề xuất cá nhân hóa?</h4>
          <p>Đăng ký để nhận được đề xuất sản phẩm phù hợp với sở thích riêng của bạn!</p>
          <div class="prompt-actions">
            <router-link to="/register" class="btn btn-primary">
              🚀 Đăng ký ngay
            </router-link>
            <router-link to="/login" class="btn btn-secondary">
              Đăng nhập
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import recommendationService from '@/services/recommendationService'

export default {
  name: 'TrendingProducts',
  props: {
    // Số lượng sản phẩm hiển thị
    limit: {
      type: Number,
      default: 12
    },
    
    // Tự động refresh
    autoRefresh: {
      type: Boolean,
      default: true
    },
    
    // Interval tự động refresh (minutes)
    refreshInterval: {
      type: Number,
      default: 10
    },
    
    // Custom CSS class
    containerClass: {
      type: String,
      default: ''
    }
  },
  
  emits: ['product-click', 'add-to-cart', 'quick-view'],
  
  setup(props, { emit }) {
    const router = useRouter()
    const userStore = useUserStore()
    
    // Reactive state
    const trendingProducts = ref([])
    const loading = ref(false)
    const error = ref(null)
    const lastUpdated = ref(new Date())
    const nextRefreshIn = ref(props.refreshInterval * 60 * 1000)
    
    // Mock trending stats
    const totalViews = ref(2547391)
    const totalPurchases = ref(148726)
    
    // Auto refresh
    let refreshTimer = null
    let countdownTimer = null
    
    // Computed properties
    const autoRefreshEnabled = computed(() => {
      return props.autoRefresh && refreshTimer
    })
    
    const hasMoreProducts = computed(() => {
      return trendingProducts.value.length >= props.limit
    })
    
    // Load trending products
    const loadTrendingProducts = async () => {
      loading.value = true
      error.value = null
      
      try {
        const response = await recommendationService.getTrendingRecommendations(props.limit)
        
        trendingProducts.value = response.trending || []
        lastUpdated.value = new Date()
        
        // Enhance with mock trending data
        enhanceWithTrendingData()
        
        // Track trending products loaded
        recommendationService.trackInteraction(null, 'TRENDING_PRODUCTS_LOADED', {
          count: trendingProducts.value.length,
          source: 'home_page_trending',
          userType: 'anonymous'
        })
        
      } catch (err) {
        console.error('Error loading trending products:', err)
        error.value = err.response?.data?.message || 'Không thể tải sản phẩm trending'
        trendingProducts.value = []
      } finally {
        loading.value = false
      }
    }
    
    // Enhance products with trending data
    const enhanceWithTrendingData = () => {
      trendingProducts.value = trendingProducts.value.map((product, index) => ({
        ...product,
        trendingScore: Math.floor(Math.random() * 50 + 50 + (10 - index) * 5),
        isRising: Math.random() > 0.7,
        isHot: index < 3,
        growthRate: Math.floor(Math.random() * 200 + 50),
        totalViews: Math.floor(Math.random() * 10000 + 1000),
        totalSales: Math.floor(Math.random() * 500 + 50),
        totalWishlisted: Math.floor(Math.random() * 200 + 20)
      }))
    }
    
    // Handle product click
    const handleProductClick = async (product, index) => {
      // Track trending product click
      await recommendationService.trackClick(product.id, 'trending_products')
      
      // Additional tracking for position
      await recommendationService.trackInteraction(product.id, 'TRENDING_PRODUCT_CLICK', {
        position: index + 1,
        trendingScore: product.trendingScore,
        source: 'trending_section'
      })
      
      // Emit event
      emit('product-click', { product, index })
      
      // Navigate to product detail
      router.push(`/products/${product.id}`)
    }
    
    // Handle add to cart
    const handleAddToCart = async (product) => {
      try {
        // Track add to cart from trending
        await recommendationService.trackAddToCart(product.id, 1)
        
        // Additional trending conversion tracking
        await recommendationService.trackInteraction(product.id, 'TRENDING_CONVERSION', {
          trendingScore: product.trendingScore,
          source: 'trending_section'
        })
        
        // Emit event
        emit('add-to-cart', product)
        
        // Show success feedback
        console.log('Added to cart from trending:', product.name)
        
      } catch (error) {
        console.error('Error adding trending product to cart:', error)
      }
    }
    
    // Quick view functionality
    const quickView = (product) => {
      // Track quick view action
      recommendationService.trackInteraction(product.id, 'QUICK_VIEW', {
        source: 'trending_products'
      })
      
      // Emit event
      emit('quick-view', product)
      
      // TODO: Implement quick view modal or navigate to product
      router.push(`/products/${product.id}`)
    }
    
    // Add to wishlist
    const addToWishlist = async (product) => {
      try {
        // Track wishlist action
        await recommendationService.trackInteraction(product.id, 'ADD_TO_WISHLIST', {
          source: 'trending_products'
        })
        
        console.log('Added to wishlist from trending:', product.name)
        
      } catch (error) {
        console.error('Error adding to wishlist:', error)
      }
    }
    
    // View all trending
    const viewAllTrending = () => {
      // Track view all action
      recommendationService.trackInteraction(null, 'VIEW_ALL_TRENDING', {
        currentCount: trendingProducts.value.length
      })
      
      router.push('/products?sort=trending')
    }
    
    // Auto refresh functionality
    const startAutoRefresh = () => {
      if (!props.autoRefresh) return
      
      // Main refresh timer
      refreshTimer = setInterval(() => {
        loadTrendingProducts()
        nextRefreshIn.value = props.refreshInterval * 60 * 1000
      }, props.refreshInterval * 60 * 1000)
      
      // Countdown timer
      countdownTimer = setInterval(() => {
        nextRefreshIn.value -= 1000
        if (nextRefreshIn.value <= 0) {
          nextRefreshIn.value = props.refreshInterval * 60 * 1000
        }
      }, 1000)
    }
    
    const stopAutoRefresh = () => {
      if (refreshTimer) {
        clearInterval(refreshTimer)
        refreshTimer = null
      }
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
    
    // Helper functions
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
    
    const formatNumber = (number) => {
      if (number >= 1000000) {
        return (number / 1000000).toFixed(1) + 'M'
      }
      if (number >= 1000) {
        return (number / 1000).toFixed(1) + 'K'
      }
      return number.toString()
    }
    
    const formatRelativeTime = (date) => {
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      
      if (minutes < 1) return 'vừa xong'
      if (minutes < 60) return `${minutes} phút trước`
      
      const hours = Math.floor(minutes / 60)
      if (hours < 24) return `${hours} giờ trước`
      
      const days = Math.floor(hours / 24)
      return `${days} ngày trước`
    }
    
    const truncate = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.slice(0, length) + '...' : text
    }
    
    const handleImageError = (event) => {
      event.target.src = '/api/placeholder/product'
    }
    
    const isInWishlist = (productId) => {
      return userStore.wishlist?.some(item => item.id === productId) || false
    }
    
    const getRankClass = (index) => {
      if (index === 0) return 'rank-gold'
      if (index === 1) return 'rank-silver'
      if (index === 2) return 'rank-bronze'
      return 'rank-normal'
    }
    
    const getRankLabel = (index) => {
      if (index === 0) return '👑'
      if (index === 1) return '🥈'
      if (index === 2) return '🥉'
      return '⭐'
    }
    
    // Lifecycle
    onMounted(() => {
      loadTrendingProducts()
      startAutoRefresh()
    })
    
    onUnmounted(() => {
      stopAutoRefresh()
    })
    
    return {
      // State
      trendingProducts,
      loading,
      error,
      lastUpdated,
      nextRefreshIn,
      totalViews,
      totalPurchases,
      
      // Computed
      autoRefreshEnabled,
      hasMoreProducts,
      
      // Methods
      loadTrendingProducts,
      handleProductClick,
      handleAddToCart,
      quickView,
      addToWishlist,
      viewAllTrending,
      
      // Helpers
      formatPrice,
      formatNumber,
      formatRelativeTime,
      truncate,
      handleImageError,
      isInWishlist,
      getRankClass,
      getRankLabel
    }
  }
}
</script>

<style scoped>
.trending-products {
  width: 100%;
  margin: 2rem 0;
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(255, 165, 0, 0.3);
}

.header-content {
  flex: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.trending-icon {
  font-size: 1.5rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.section-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1rem;
}

.trending-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  text-align: right;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.stat-icon {
  font-size: 1rem;
}

/* Loading State */
.loading-container {
  padding: 2rem 0;
}

.loading-header {
  margin-bottom: 2rem;
}

.loading-title,
.loading-subtitle {
  height: 1.5rem;
  background: linear-gradient(90deg, rgba(255,165,0,0.1) 25%, rgba(255,165,0,0.2) 50%, rgba(255,165,0,0.1) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.loading-title { width: 40%; }
.loading-subtitle { width: 60%; }

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.loading-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.loading-rank {
  position: absolute;
  top: 1rem;
  left: 1rem;
  width: 30px;
  height: 30px;
  background: linear-gradient(90deg, rgba(255,165,0,0.1) 25%, rgba(255,165,0,0.2) 50%, rgba(255,165,0,0.1) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 50%;
  z-index: 10;
}

.loading-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, rgba(255,165,0,0.1) 25%, rgba(255,165,0,0.2) 50%, rgba(255,165,0,0.1) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.loading-content {
  padding: 1rem;
}

.loading-line {
  height: 1rem;
  background: linear-gradient(90deg, rgba(255,165,0,0.1) 25%, rgba(255,165,0,0.2) 50%, rgba(255,165,0,0.1) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.loading-line.short { width: 60%; }
.loading-line.medium { width: 80%; }
.loading-line.long { width: 100%; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* Error & Empty States */
.error-container,
.empty-container {
  text-align: center;
  padding: 3rem 1rem;
}

.error-message,
.empty-message {
  max-width: 400px;
  margin: 0 auto;
}

.error-icon,
.empty-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
}

.error-message h3,
.empty-message h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.error-message p,
.empty-message p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.retry-btn {
  background: #ff6b35;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #e55a2b;
  transform: translateY(-2px);
}

/* Time Indicator */
.time-indicator {
  background: rgba(255, 165, 0, 0.1);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-align: center;
}

.time-icon {
  margin-right: 0.5rem;
}

.auto-refresh {
  font-weight: 600;
  color: #ff6b35;
}

/* Trending Grid */
.trending-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

/* Trending Product Card */
.trending-product-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 165, 0, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.trending-product-card:hover {
  transform: translateY(-8px);
  border-color: #ff6b35;
  box-shadow: 0 20px 40px rgba(255, 107, 53, 0.2);
}

/* Trending Rank */
.trending-rank {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 10;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 0.5rem;
  border-radius: 50%;
  min-width: 45px;
  min-height: 45px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  backdrop-filter: blur(5px);
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #000;
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #000;
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: #fff;
}

.rank-number {
  font-size: 1rem;
  font-weight: 700;
  line-height: 1;
}

.rank-label {
  font-size: 0.8rem;
  line-height: 1;
}

/* Product Image */
.product-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.trending-product-card:hover .product-image {
  transform: scale(1.05);
}

.trending-badges {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  z-index: 10;
}

.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-rising {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: white;
}

.badge-hot {
  background: linear-gradient(135deg, #ff6b35, #f56500);
  color: white;
}

.badge-discount {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: white;
}

.quick-overlay {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  right: 1rem;
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.trending-product-card:hover .quick-overlay {
  opacity: 1;
}

.quick-view-btn,
.wishlist-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  font-size: 0.8rem;
  font-weight: 600;
}

.quick-view-btn {
  flex: 1;
}

.wishlist-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-view-btn:hover,
.wishlist-btn:hover,
.wishlist-btn.active {
  background: #ff6b35;
  color: white;
  transform: translateY(-2px);
}

/* Product Info */
.product-info {
  padding: 1.5rem;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.category-tag {
  background: rgba(255, 165, 0, 0.2);
  color: #ff6b35;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
}

.trending-score {
  background: linear-gradient(135deg, #ff6b35, #ffa500);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
  line-height: 1.3;
}

.product-pricing {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.current-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #ff6b35;
}

.original-price {
  font-size: 1rem;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.stars {
  display: flex;
  gap: 1px;
}

.star {
  font-size: 0.9rem;
  color: #e2e8f0;
}

.star.filled {
  color: #ffd700;
}

.rating-text {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.review-count {
  font-size: 0.8rem;
}

.trending-stats-mini {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  justify-content: space-between;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.stat-icon-mini {
  font-size: 0.9rem;
}

.stat-value-mini {
  font-weight: 600;
}

.add-to-cart-btn {
  width: 100%;
  background: linear-gradient(135deg, #ff6b35, #ffa500);
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #e55a2b, #e8940f);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.3);
}

.add-to-cart-btn:disabled {
  background: #6b7280;
  cursor: not-allowed;
  transform: none;
}

/* View All & Signup Prompt */
.view-all-container {
  text-align: center;
  margin: 2rem 0;
}

.view-all-btn {
  background: transparent;
  border: 2px solid #ff6b35;
  color: #ff6b35;
  padding: 1rem 2rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 1rem;
}

.view-all-btn:hover {
  background: #ff6b35;
  color: white;
  transform: translateY(-2px);
}

.signup-prompt {
  margin: 3rem 0;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1), rgba(255, 165, 0, 0.1));
  border: 2px dashed rgba(255, 107, 53, 0.3);
  border-radius: 12px;
  text-align: center;
}

.prompt-content h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.prompt-content p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.prompt-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* Responsive Design */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .trending-stats {
    flex-direction: row;
    align-self: stretch;
    justify-content: space-between;
    text-align: left;
  }
  
  .trending-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
  
  .prompt-actions {
    flex-direction: column;
    align-items: center;
  }
}

@media (max-width: 480px) {
  .trending-grid {
    grid-template-columns: 1fr;
  }
  
  .trending-products {
    margin: 1rem 0;
  }
  
  .section-title {
    font-size: 1.3rem;
  }
  
  .product-info {
    padding: 1rem;
  }
  
  .trending-stats-mini {
    gap: 0.5rem;
  }
}
</style>