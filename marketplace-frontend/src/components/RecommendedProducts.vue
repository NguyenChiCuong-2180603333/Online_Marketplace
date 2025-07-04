<template>
  <div class="recommended-products">
    <!-- Header với AI badge -->
    <div class="section-header">
      <div class="header-content">
        <h2 class="section-title">
          Đề xuất cho bạn
        </h2>
        <p class="section-subtitle" v-if="!loading && recommendations.length > 0">
          Được cá nhân hóa dựa trên sở thích của bạn
        </p>
      </div>

      <!-- Refresh button -->
      <button
        @click="refreshRecommendations"
        :disabled="loading"
        class="refresh-btn"
        title="Làm mới đề xuất"
      >
        <span class="refresh-icon" :class="{ spinning: loading }">🔄</span>
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="loading-grid">
        <div v-for="n in limit" :key="n" class="loading-card">
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-price"></div>
            <div class="skeleton-rating"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="error-container">
      <div class="error-message">
        <span class="error-icon">⚠️</span>
        <h3>Không thể tải đề xuất</h3>
        <p>{{ error }}</p>
        <button @click="loadRecommendations" class="retry-btn">Thử lại</button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="recommendations.length === 0" class="empty-container">
      <div class="empty-message">
        <span class="empty-icon">🎯</span>
        <h3>Chưa có đề xuất</h3>
        <p>Hãy khám phá thêm sản phẩm để nhận được đề xuất cá nhân hóa!</p>
        <router-link to="/products" class="explore-btn"> Khám phá ngay </router-link>
      </div>
    </div>

    <!-- Products grid -->
    <div v-else class="products-container">
      <!-- Algorithm info (for debugging) -->
      <div v-if="showDebugInfo && algorithmInfo" class="debug-info">
        <small>
          📊 Algorithm: {{ algorithmInfo.algorithm }} | Count: {{ algorithmInfo.count }} |
          <span v-if="algorithmInfo.timestamp">
            Updated: {{ formatTime(algorithmInfo.timestamp) }}
          </span>
        </small>
      </div>

      <!-- Products grid -->
      <div class="products-grid">
        <div
          v-for="(product, index) in recommendations"
          :key="product.id"
          class="product-card"
          @click="handleProductClick(product, index)"
        >
          <!-- Product image -->
          <div class="product-image-container">
            <img
              :src="product.images?.[0] || '/api/placeholder/product'"
              :alt="product.name"
              class="product-image"
              @error="handleImageError"
              loading="lazy"
            />

            <!-- Quick actions overlay -->
            <div class="quick-actions">
              <button
                @click.stop="addToWishlist(product)"
                class="quick-action-btn wishlist-btn"
                :class="{ active: isInWishlist(product.id) }"
                title="Thêm vào yêu thích"
              >
                ❤️
              </button>

              <button
                @click.stop="quickAddToCart(product)"
                class="quick-action-btn cart-btn"
                title="Thêm vào giỏ hàng"
              >
                🛒
              </button>
            </div>

            <!-- Discount badge -->
            <div v-if="product.discount > 0" class="discount-badge">-{{ product.discount }}%</div>

            <!-- AI recommendation reason -->
            <div class="ai-reason" v-if="getRecommendationReason(product, index)">
              <small>{{ getRecommendationReason(product, index) }}</small>
            </div>
          </div>

          <!-- Product info -->
          <div class="product-info">
            <!-- Category -->
            <div class="product-category">
              {{ product.category || 'Chưa phân loại' }}
            </div>

            <!-- Name -->
            <h3 class="product-name" :title="product.name">
              {{ truncate(product.name, 50) }}
            </h3>

            <!-- Price -->
            <div class="product-pricing">
              <span class="current-price">
                {{ formatPrice(product.price) }}
              </span>
              <span
                v-if="product.originalPrice && product.originalPrice > product.price"
                class="original-price"
              >
                {{ formatPrice(product.originalPrice) }}
              </span>
            </div>

            <!-- Rating & reviews -->
            <div class="product-rating" v-if="product.rating || product.reviewCount">
              <div class="stars">
                <span
                  v-for="star in 5"
                  :key="star"
                  class="star"
                  :class="{ filled: star <= (product.rating || 0) }"
                >
                  ⭐
                </span>
              </div>
              <span class="rating-text">
                {{ product.rating || 0 }}/5
                <span v-if="product.reviewCount" class="review-count">
                  ({{ product.reviewCount }})
                </span>
              </span>
            </div>

            <!-- Seller info -->
            <div class="seller-info" v-if="product.seller">
              <small>{{ product.seller.name || 'Người bán' }}</small>
            </div>
          </div>
        </div>
      </div>

      <!-- View more button -->
      <div v-if="canLoadMore" class="view-more-container">
        <button @click="loadMore" :disabled="loadingMore" class="view-more-btn">
          <span v-if="loadingMore" class="loading-spinner">⏳</span>
          {{ loadingMore ? 'Đang tải...' : 'Xem thêm đề xuất' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import recommendationService from '@/services/recommendationService'

export default {
  name: 'RecommendedProducts',
  props: {
    // Số lượng sản phẩm hiển thị ban đầu
    limit: {
      type: Number,
      default: 12,
    },

    // Hiển thị debug info
    showDebugInfo: {
      type: Boolean,
      default: false,
    },

    // Có cho phép load more không
    enableLoadMore: {
      type: Boolean,
      default: true,
    },

    // Custom CSS class
    containerClass: {
      type: String,
      default: '',
    },

    // Auto refresh interval (minutes)
    autoRefreshInterval: {
      type: Number,
      default: 30,
    },
  },

  emits: ['product-click', 'add-to-cart', 'add-to-wishlist'],

  setup(props, { emit }) {
    const router = useRouter()
    const authStore = useAuthStore()
    const userStore = useUserStore()

    // Reactive state
    const recommendations = ref([])
    const loading = ref(false)
    const loadingMore = ref(false)
    const error = ref(null)
    const algorithmInfo = ref(null)
    const currentLimit = ref(props.limit)

    // Computed properties
    const canLoadMore = computed(() => {
      return (
        props.enableLoadMore &&
        recommendations.value.length >= currentLimit.value &&
        recommendations.value.length < 100
      ) // Reasonable limit
    })

    // Load recommendations
    const loadRecommendations = async (showLoader = true) => {
      if (showLoader) loading.value = true
      error.value = null

      try {
        const response = await recommendationService.getPersonalRecommendations(currentLimit.value)

        recommendations.value = response.recommendations || []
        algorithmInfo.value = {
          algorithm: response.algorithm,
          count: response.count,
          timestamp: response.timestamp,
        }

        // Track đã load recommendations
        if (recommendations.value.length > 0) {
          recommendationService.trackInteraction(null, 'RECOMMENDATIONS_LOADED', {
            count: recommendations.value.length,
            algorithm: response.algorithm,
          })
        }
      } catch (err) {
        console.error('Error loading recommendations:', err)
        error.value = err.response?.data?.message || 'Có lỗi khi tải đề xuất'
        recommendations.value = []
      } finally {
        loading.value = false
      }
    }

    // Load more recommendations
    const loadMore = async () => {
      if (loadingMore.value) return

      loadingMore.value = true
      const newLimit = currentLimit.value + props.limit

      try {
        const response = await recommendationService.getPersonalRecommendations(newLimit)
        recommendations.value = response.recommendations || []
        currentLimit.value = newLimit

        algorithmInfo.value = {
          algorithm: response.algorithm,
          count: response.count,
          timestamp: response.timestamp,
        }
      } catch (err) {
        console.error('Error loading more recommendations:', err)
      } finally {
        loadingMore.value = false
      }
    }

    // Refresh recommendations
    const refreshRecommendations = async () => {
      currentLimit.value = props.limit
      await loadRecommendations(true)
    }

    // Handle product click
    const handleProductClick = async (product, index) => {
      // Track click
      await recommendationService.trackClick(product.id, 'recommendation_card')

      // Emit event
      emit('product-click', { product, index })

      // Navigate to product detail
      router.push(`/products/${product.id}`)
    }

    // Quick add to cart
    const quickAddToCart = async (product) => {
      try {
        // Add to cart logic here (call cart store)
        await recommendationService.trackAddToCart(product.id, 1)

        emit('add-to-cart', product)

        // Show success notification
        // TODO: Add notification system
      } catch (err) {
        console.error('Error adding to cart:', err)
      }
    }

    // Add to wishlist
    const addToWishlist = async (product) => {
      try {
        // Add to wishlist logic here
        await recommendationService.trackInteraction(product.id, 'ADD_TO_WISHLIST')

        emit('add-to-wishlist', product)
      } catch (err) {
        console.error('Error adding to wishlist:', err)
      }
    }

    // Helper functions
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(price)
    }

    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString('vi-VN')
    }

    const truncate = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.slice(0, length) + '...' : text
    }

    const handleImageError = (event) => {
      event.target.src = '/api/placeholder/product'
    }

    const isInWishlist = (productId) => {
      return userStore.wishlist.some((item) => item.id === productId)
    }

    const getRecommendationReason = (product, index) => {
      const reasons = [
        '🎯 Phù hợp với sở thích',
        '🔥 Đang trending',
        '⭐ Đánh giá cao',
        '💝 Yêu thích của người dùng khác',
      ]
      return reasons[index % reasons.length]
    }

    // Auto refresh
    let refreshInterval = null

    const setupAutoRefresh = () => {
      if (props.autoRefreshInterval > 0) {
        refreshInterval = setInterval(() => {
          loadRecommendations(false) // Silent refresh
        }, props.autoRefreshInterval * 60 * 1000)
      }
    }

    const clearAutoRefresh = () => {
      if (refreshInterval) {
        clearInterval(refreshInterval)
        refreshInterval = null
      }
    }

    // Lifecycle
    onMounted(() => {
      loadRecommendations()
      setupAutoRefresh()
    })

    // Cleanup
    const cleanup = () => {
      clearAutoRefresh()
    }

    // Return reactive data and methods
    return {
      // State
      recommendations,
      loading,
      loadingMore,
      error,
      algorithmInfo,
      canLoadMore,

      // Methods
      loadRecommendations,
      loadMore,
      refreshRecommendations,
      handleProductClick,
      quickAddToCart,
      addToWishlist,

      // Helpers
      formatPrice,
      formatTime,
      truncate,
      handleImageError,
      isInWishlist,
      getRecommendationReason,

      // Cleanup
      cleanup,
    }
  },

  beforeUnmount() {
    this.cleanup()
  },
}
</script>

<style scoped>
.recommended-products {
  width: 100%;
  margin: 2rem 0;
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid
    var(--primary-gradient, linear-gradient(135deg, #667eea 0%, #764ba2 100%));
}

.header-content {
  flex: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary, #2d3748);
  margin: 0 0 0.5rem 0;
}

.ai-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.section-subtitle {
  color: var(--text-secondary, #718096);
  margin: 0;
  font-size: 1rem;
}

.refresh-btn {
  background: transparent;
  border: 2px solid var(--primary-color, #667eea);
  padding: 0.5rem 1rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--primary-color, #667eea);
}

.refresh-btn:hover:not(:disabled) {
  background: var(--primary-color, #667eea);
  color: white;
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  display: inline-block;
  transition: transform 0.3s ease;
}

.refresh-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Loading State */
.loading-container {
  padding: 2rem 0;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.loading-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 1rem;
}

.skeleton-title,
.skeleton-price,
.skeleton-rating {
  height: 1rem;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-title {
  width: 80%;
}
.skeleton-price {
  width: 60%;
}
.skeleton-rating {
  width: 70%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Error State */
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
  color: var(--text-primary, #2d3748);
  margin-bottom: 0.5rem;
}

.error-message p,
.empty-message p {
  color: var(--text-secondary, #718096);
  margin-bottom: 1.5rem;
}

.retry-btn,
.explore-btn {
  background: var(--primary-color, #667eea);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  text-decoration: none;
  display: inline-block;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover,
.explore-btn:hover {
  background: var(--primary-dark, #5a6fd8);
  transform: translateY(-2px);
}

/* Debug Info */
.debug-info {
  background: #f7fafc;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-family: monospace;
  color: #718096;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* Product Card */
.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.product-image-container {
  position: relative;
  overflow: hidden;
  height: 200px;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.quick-actions {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  display: flex;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .quick-actions {
  opacity: 1;
}

.quick-action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.quick-action-btn:hover {
  background: white;
  transform: scale(1.1);
}

.wishlist-btn.active {
  background: #ff6b6b;
  color: white;
}

.discount-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  background: #ff6b6b;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
}

.ai-reason {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .ai-reason {
  opacity: 1;
}

/* Product Info */
.product-info {
  padding: 1rem;
}

.product-category {
  color: var(--primary-color, #667eea);
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 0.5rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary, #2d3748);
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
  color: var(--primary-color, #667eea);
}

.original-price {
  font-size: 1rem;
  color: #a0aec0;
  text-decoration: line-through;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
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
  color: var(--text-secondary, #718096);
}

.review-count {
  font-size: 0.8rem;
}

.seller-info {
  color: var(--text-secondary, #718096);
  font-size: 0.85rem;
}

/* View More */
.view-more-container {
  text-align: center;
  margin-top: 2rem;
}

.view-more-btn {
  background: transparent;
  border: 2px solid var(--primary-color, #667eea);
  color: var(--primary-color, #667eea);
  padding: 0.75rem 2rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
}

.view-more-btn:hover:not(:disabled) {
  background: var(--primary-color, #667eea);
  color: white;
  transform: translateY(-2px);
}

.view-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  margin-right: 0.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }

  .product-card {
    margin-bottom: 0.5rem;
  }
}

@media (max-width: 480px) {
  .products-grid {
    grid-template-columns: 1fr;
  }

  .recommended-products {
    margin: 1rem 0;
  }

  .section-title {
    font-size: 1.3rem;
  }
}
</style>
