<template>
  <div class="similar-products" v-if="shouldShow">
    <!-- Section Header -->
    <div class="section-header">
      <h3 class="section-title">
        <span class="similarity-icon">🔗</span>
        Sản phẩm tương tự
      </h3>
      
      <div class="header-actions">
        <!-- Algorithm indicator -->
        <div v-if="algorithmInfo" class="algorithm-badge" :title="getAlgorithmDescription()">
          {{ getAlgorithmLabel() }}
        </div>
        
        <!-- View all button -->
        <button 
          v-if="hasMoreProducts"
          @click="viewAllSimilar"
          class="view-all-btn"
        >
          Xem tất cả
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-products">
        <div v-for="n in displayLimit" :key="n" class="loading-product-card">
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line medium"></div>
            <div class="skeleton-line long"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-content">
        <span class="error-icon">⚠️</span>
        <p class="error-message">{{ error }}</p>
        <button @click="loadSimilarProducts" class="retry-btn">
          Thử lại
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="similarProducts.length === 0" class="empty-state">
      <div class="empty-content">
        <span class="empty-icon">🔍</span>
        <p class="empty-message">Không tìm thấy sản phẩm tương tự</p>
      </div>
    </div>

    <!-- Products Horizontal Scroll -->
    <div v-else class="products-container">
      <!-- Similarity score info (optional debug) -->
      <div v-if="showSimilarityScores && algorithmInfo" class="similarity-info">
        <small>
          📊 Thuật toán: {{ algorithmInfo.algorithm }} | 
          Tìm thấy {{ algorithmInfo.count }} sản phẩm tương tự
        </small>
      </div>

      <!-- Horizontal scrolling container -->
      <div class="products-scroll-container">
        <div class="products-scroll" ref="productsScrollRef">
          <div 
            v-for="(product, index) in displayProducts" 
            :key="product.id"
            class="similar-product-card"
            @click="handleProductClick(product, index)"
          >
            <!-- Product Image -->
            <div class="product-image-wrapper">
              <img 
                :src="product.imageUrl || '/api/placeholder/product'" 
                :alt="product.name"
                class="product-image"
                @error="handleImageError"
                loading="lazy"
              />
              
              <!-- Similarity badge -->
              <div class="similarity-badge" v-if="getSimilarityScore(product, index)">
                {{ getSimilarityScore(product, index) }}% phù hợp
              </div>

              <!-- Quick add to compare -->
              <button 
                @click.stop="addToCompare(product)"
                class="compare-btn"
                :class="{ 'active': isInCompare(product.id) }"
                title="So sánh sản phẩm"
              >
                ⚖️
              </button>

              <!-- Discount badge -->
              <div v-if="product.discount > 0" class="discount-badge">
                -{{ product.discount }}%
              </div>
            </div>

            <!-- Product Info -->
            <div class="product-info">
              <!-- Category tag -->
              <div class="category-tag" v-if="product.category">
                {{ product.category }}
              </div>

              <!-- Product name -->
              <h4 class="product-name" :title="product.name">
                {{ truncate(product.name, 40) }}
              </h4>

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

              <!-- Rating -->
              <div class="product-rating" v-if="product.rating || product.reviewCount">
                <div class="stars-mini">
                  <span v-for="star in 5" :key="star" 
                        class="star-mini"
                        :class="{ 'filled': star <= (product.rating || 0) }">
                    ⭐
                  </span>
                </div>
                <span class="rating-text-mini">
                  {{ (product.rating || 0).toFixed(1) }}
                  <span v-if="product.reviewCount" class="review-count-mini">
                    ({{ formatReviewCount(product.reviewCount) }})
                  </span>
                </span>
              </div>

              <!-- Similarity reasons -->
              <div class="similarity-reasons" v-if="getSimilarityReasons(product, index)">
                <div class="reasons-list">
                  <span 
                    v-for="reason in getSimilarityReasons(product, index)" 
                    :key="reason"
                    class="reason-tag"
                  >
                    {{ reason }}
                  </span>
                </div>
              </div>

              <!-- Action buttons -->
              <div class="product-actions">
                <button 
                  @click.stop="quickAddToCart(product)"
                  class="add-to-cart-btn"
                  :disabled="isAddingToCart"
                >
                  <span v-if="isAddingToCart">⏳</span>
                  <span v-else>🛒</span>
                  {{ isAddingToCart ? 'Đang thêm...' : 'Thêm' }}
                </button>
                
                <button 
                  @click.stop="toggleWishlist(product)"
                  class="wishlist-btn"
                  :class="{ 'active': isInWishlist(product.id) }"
                >
                  {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Scroll navigation buttons -->
        <button 
          v-if="canScrollLeft"
          @click="scrollLeft"
          class="scroll-btn scroll-left"
          title="Cuộn trái"
        >
          ◀
        </button>
        
        <button 
          v-if="canScrollRight"
          @click="scrollRight"
          class="scroll-btn scroll-right"
          title="Cuộn phải"
        >
          ▶
        </button>
      </div>

      <!-- Compare panel (if products selected) -->
      <div v-if="compareProducts.length > 0" class="compare-panel">
        <div class="compare-header">
          <span class="compare-title">
            So sánh ({{ compareProducts.length }}/{{ maxCompareProducts }})
          </span>
          <button @click="clearCompare" class="clear-compare-btn">
            Xóa tất cả
          </button>
        </div>
        
        <div class="compare-products">
          <div 
            v-for="product in compareProducts" 
            :key="product.id"
            class="compare-product-item"
          >
            <img :src="product.imageUrl" :alt="product.name" class="compare-product-image" />
            <span class="compare-product-name">{{ truncate(product.name, 20) }}</span>
            <button @click="removeFromCompare(product.id)" class="remove-compare-btn">
              ❌
            </button>
          </div>
        </div>
        
        <button 
          v-if="compareProducts.length >= 2"
          @click="goToComparePage"
          class="compare-action-btn"
        >
          So sánh ngay
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import recommendationService from '@/services/recommendationService'

export default {
  name: 'SimilarProducts',
  props: {
    // ID sản phẩm gốc để tìm similar products
    productId: {
      type: String,
      required: true
    },
    
    // Số lượng sản phẩm hiển thị
    limit: {
      type: Number,
      default: 12
    },
    
    // Số lượng sản phẩm hiển thị ban đầu (responsive)
    displayLimit: {
      type: Number,
      default: 6
    },
    
    // Hiển thị similarity scores (debug mode)
    showSimilarityScores: {
      type: Boolean,
      default: false
    },
    
    // Tự động load khi component mount
    autoLoad: {
      type: Boolean,
      default: true
    },
    
    // Custom CSS classes
    containerClass: {
      type: String,
      default: ''
    }
  },
  
  emits: ['product-click', 'add-to-cart', 'add-to-wishlist', 'compare-products'],
  
  setup(props, { emit }) {
    const router = useRouter()
    const userStore = useUserStore()
    
    // Reactive state
    const similarProducts = ref([])
    const loading = ref(false)
    const error = ref(null)
    const algorithmInfo = ref(null)
    const isAddingToCart = ref(false)
    const compareProducts = ref([])
    const maxCompareProducts = 4
    
    // Scroll refs
    const productsScrollRef = ref(null)
    const canScrollLeft = ref(false)
    const canScrollRight = ref(false)
    
    // Computed properties
    const shouldShow = computed(() => {
      return props.productId && (loading.value || similarProducts.value.length > 0 || error.value)
    })
    
    const displayProducts = computed(() => {
      return similarProducts.value.slice(0, props.displayLimit)
    })
    
    const hasMoreProducts = computed(() => {
      return similarProducts.value.length > props.displayLimit
    })
    
    // Load similar products
    const loadSimilarProducts = async () => {
      if (!props.productId) {
        console.warn('No productId provided for similar products')
        return
      }
      
      loading.value = true
      error.value = null
      
      try {
        const response = await recommendationService.getSimilarProducts(props.productId, props.limit)
        
        similarProducts.value = response.similarProducts || []
        algorithmInfo.value = {
          algorithm: response.algorithm,
          count: response.count,
          baseProductId: response.baseProductId
        }
        
        // Track similar products loaded
        if (similarProducts.value.length > 0) {
          recommendationService.trackInteraction(props.productId, 'SIMILAR_PRODUCTS_LOADED', {
            count: similarProducts.value.length,
            algorithm: response.algorithm
          })
        }
        
        // Check scroll after loading
        await nextTick()
        checkScrollButtons()
        
      } catch (err) {
        console.error('Error loading similar products:', err)
        error.value = err.response?.data?.message || 'Không thể tải sản phẩm tương tự'
        similarProducts.value = []
      } finally {
        loading.value = false
      }
    }
    
    // Handle product click
    const handleProductClick = async (product, index) => {
      // Track click
      await recommendationService.trackClick(product.id, 'similar_products')
      
      // Emit event
      emit('product-click', { product, index, source: 'similar' })
      
      // Navigate to product detail
      router.push(`/products/${product.id}`)
    }
    
    // Quick add to cart
    const quickAddToCart = async (product) => {
      if (isAddingToCart.value) return
      
      isAddingToCart.value = true
      
      try {
        // Track add to cart from similar products
        await recommendationService.trackAddToCart(product.id, 1)
        
        // Emit to parent
        emit('add-to-cart', product)
        
        // TODO: Add cart logic here
        
      } catch (err) {
        console.error('Error adding to cart:', err)
      } finally {
        isAddingToCart.value = false
      }
    }
    
    // Toggle wishlist
    const toggleWishlist = async (product) => {
      try {
        if (isInWishlist(product.id)) {
          // Remove from wishlist
          await recommendationService.trackInteraction(product.id, 'REMOVE_FROM_WISHLIST')
        } else {
          // Add to wishlist
          await recommendationService.trackInteraction(product.id, 'ADD_TO_WISHLIST')
        }
        
        emit('add-to-wishlist', product)
        
      } catch (err) {
        console.error('Error toggling wishlist:', err)
      }
    }
    
    // Compare functionality
    const addToCompare = (product) => {
      if (compareProducts.value.length >= maxCompareProducts) {
        alert(`Chỉ có thể so sánh tối đa ${maxCompareProducts} sản phẩm`)
        return
      }
      
      if (!isInCompare(product.id)) {
        compareProducts.value.push(product)
        
        // Track compare action
        recommendationService.trackInteraction(product.id, 'ADD_TO_COMPARE', {
          compareCount: compareProducts.value.length
        })
      }
    }
    
    const removeFromCompare = (productId) => {
      compareProducts.value = compareProducts.value.filter(p => p.id !== productId)
    }
    
    const isInCompare = (productId) => {
      return compareProducts.value.some(p => p.id === productId)
    }
    
    const clearCompare = () => {
      compareProducts.value = []
    }
    
    const goToComparePage = () => {
      const productIds = compareProducts.value.map(p => p.id).join(',')
      router.push(`/compare?products=${productIds}`)
    }
    
    // Scroll functionality
    const checkScrollButtons = () => {
      if (!productsScrollRef.value) return
      
      const container = productsScrollRef.value
      canScrollLeft.value = container.scrollLeft > 0
      canScrollRight.value = container.scrollLeft < (container.scrollWidth - container.clientWidth)
    }
    
    const scrollLeft = () => {
      if (productsScrollRef.value) {
        productsScrollRef.value.scrollBy({ left: -300, behavior: 'smooth' })
      }
    }
    
    const scrollRight = () => {
      if (productsScrollRef.value) {
        productsScrollRef.value.scrollBy({ left: 300, behavior: 'smooth' })
      }
    }
    
    // View all similar products
    const viewAllSimilar = () => {
      router.push(`/products/${props.productId}/similar`)
    }
    
    // Helper functions
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
    
    const formatReviewCount = (count) => {
      if (count >= 1000) {
        return (count / 1000).toFixed(1) + 'k'
      }
      return count.toString()
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
    
    const getSimilarityScore = (product, index) => {
      // Mock similarity scores for display
      const scores = [95, 88, 82, 75, 70, 65]
      return scores[index] || Math.floor(Math.random() * 30 + 60)
    }
    
    const getSimilarityReasons = (product, index) => {
      const allReasons = [
        ['Cùng danh mục', 'Giá tương đương'],
        ['Thương hiệu', 'Đánh giá cao'],
        ['Màu sắc', 'Kích thước'],
        ['Phong cách', 'Chất liệu'],
        ['Tính năng', 'Mức giá']
      ]
      return allReasons[index % allReasons.length]
    }
    
    const getAlgorithmLabel = () => {
      if (!algorithmInfo.value) return ''
      
      const labels = {
        'content_based_similarity': 'Nội dung tương tự',
        'hybrid': 'AI Hybrid',
        'category_based': 'Cùng danh mục',
        'collaborative': 'Cộng tác',
        'fallback': 'Mặc định'
      }
      
      return labels[algorithmInfo.value.algorithm] || 'AI'
    }
    
    const getAlgorithmDescription = () => {
      if (!algorithmInfo.value) return ''
      
      const descriptions = {
        'content_based_similarity': 'Dựa trên đặc điểm sản phẩm',
        'hybrid': 'Kết hợp nhiều thuật toán AI',
        'category_based': 'Dựa trên danh mục sản phẩm',
        'collaborative': 'Dựa trên hành vi người dùng',
        'fallback': 'Thuật toán dự phòng'
      }
      
      return descriptions[algorithmInfo.value.algorithm] || 'Được tạo bởi AI'
    }
    
    // Watch for productId changes
    watch(() => props.productId, (newId, oldId) => {
      if (newId && newId !== oldId) {
        compareProducts.value = [] // Clear compare when changing product
        if (props.autoLoad) {
          loadSimilarProducts()
        }
      }
    })
    
    // Lifecycle
    onMounted(() => {
      if (props.autoLoad && props.productId) {
        loadSimilarProducts()
      }
    })
    
    // Return reactive data and methods
    return {
      // State
      similarProducts,
      loading,
      error,
      algorithmInfo,
      isAddingToCart,
      compareProducts,
      maxCompareProducts,
      
      // Computed
      shouldShow,
      displayProducts,
      hasMoreProducts,
      canScrollLeft,
      canScrollRight,
      
      // Refs
      productsScrollRef,
      
      // Methods
      loadSimilarProducts,
      handleProductClick,
      quickAddToCart,
      toggleWishlist,
      addToCompare,
      removeFromCompare,
      isInCompare,
      clearCompare,
      goToComparePage,
      checkScrollButtons,
      scrollLeft,
      scrollRight,
      viewAllSimilar,
      
      // Helpers
      formatPrice,
      formatReviewCount,
      truncate,
      handleImageError,
      isInWishlist,
      getSimilarityScore,
      getSimilarityReasons,
      getAlgorithmLabel,
      getAlgorithmDescription
    }
  }
}
</script>

<style scoped>
.similar-products {
  margin: 2rem 0;
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f1f5f9;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.similarity-icon {
  font-size: 1.2rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.algorithm-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: help;
}

.view-all-btn {
  background: transparent;
  border: 2px solid #667eea;
  color: #667eea;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 0.9rem;
}

.view-all-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
}

/* Loading State */
.loading-state {
  padding: 1rem 0;
}

.loading-products {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  padding-bottom: 1rem;
}

.loading-product-card {
  flex: 0 0 200px;
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  height: 120px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 0.75rem;
}

.skeleton-line {
  height: 0.75rem;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-line.short { width: 60%; }
.skeleton-line.medium { width: 80%; }
.skeleton-line.long { width: 100%; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* Error & Empty States */
.error-state,
.empty-state {
  text-align: center;
  padding: 2rem;
}

.error-content,
.empty-content {
  max-width: 300px;
  margin: 0 auto;
}

.error-icon,
.empty-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: 1rem;
}

.error-message,
.empty-message {
  color: #718096;
  margin-bottom: 1rem;
}

.retry-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

/* Similarity Info */
.similarity-info {
  background: #f7fafc;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-family: monospace;
  color: #718096;
  font-size: 0.85rem;
}

/* Products Container */
.products-container {
  position: relative;
}

.products-scroll-container {
  position: relative;
  overflow: hidden;
}

.products-scroll {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding-bottom: 1rem;
  scrollbar-width: thin;
  scrollbar-color: #e2e8f0 transparent;
}

.products-scroll::-webkit-scrollbar {
  height: 6px;
}

.products-scroll::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.products-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

.products-scroll::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

/* Product Card */
.similar-product-card {
  flex: 0 0 200px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.similar-product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
}

.product-image-wrapper {
  position: relative;
  height: 120px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.similar-product-card:hover .product-image {
  transform: scale(1.05);
}

.similarity-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.compare-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  opacity: 0;
}

.similar-product-card:hover .compare-btn {
  opacity: 1;
}

.compare-btn:hover,
.compare-btn.active {
  background: #667eea;
  color: white;
  transform: scale(1.1);
}

.discount-badge {
  position: absolute;
  bottom: 0.5rem;
  left: 0.5rem;
  background: #ff6b6b;
  color: white;
  padding: 0.2rem 0.4rem;
  border-radius: 8px;
  font-size: 0.7rem;
  font-weight: 600;
}

/* Product Info */
.product-info {
  padding: 0.75rem;
}

.category-tag {
  background: #edf2f7;
  color: #667eea;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  margin-bottom: 0.5rem;
  display: inline-block;
}

.product-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 0.5rem 0;
  line-height: 1.3;
}

.product-pricing {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.5rem;
}

.current-price {
  font-size: 1rem;
  font-weight: 700;
  color: #667eea;
}

.original-price {
  font-size: 0.8rem;
  color: #a0aec0;
  text-decoration: line-through;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.5rem;
}

.stars-mini {
  display: flex;
  gap: 1px;
}

.star-mini {
  font-size: 0.7rem;
  color: #e2e8f0;
}

.star-mini.filled {
  color: #ffd700;
}

.rating-text-mini {
  font-size: 0.75rem;
  color: #718096;
}

.review-count-mini {
  font-size: 0.7rem;
}

.similarity-reasons {
  margin-bottom: 0.75rem;
}

.reasons-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.reason-tag {
  background: #e6fffa;
  color: #234e52;
  font-size: 0.65rem;
  padding: 0.15rem 0.4rem;
  border-radius: 8px;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.add-to-cart-btn {
  flex: 1;
  background: #667eea;
  color: white;
  border: none;
  padding: 0.4rem 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  font-weight: 600;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: #5a6fd8;
  transform: translateY(-1px);
}

.add-to-cart-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.wishlist-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.wishlist-btn:hover,
.wishlist-btn.active {
  background: #ff6b6b;
  border-color: #ff6b6b;
  color: white;
}

/* Scroll Navigation */
.scroll-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.9);
  color: #667eea;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  font-weight: bold;
  z-index: 10;
}

.scroll-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-50%) scale(1.1);
}

.scroll-left {
  left: -20px;
}

.scroll-right {
  right: -20px;
}

/* Compare Panel */
.compare-panel {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 2px dashed #cbd5e0;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.compare-title {
  font-weight: 600;
  color: #2d3748;
}

.clear-compare-btn {
  background: transparent;
  border: 1px solid #e2e8f0;
  color: #718096;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.compare-products {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.compare-product-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  position: relative;
}

.compare-product-image {
  width: 30px;
  height: 30px;
  object-fit: cover;
  border-radius: 4px;
}

.compare-product-name {
  font-size: 0.8rem;
  color: #2d3748;
}

.remove-compare-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.7rem;
  opacity: 0.7;
}

.remove-compare-btn:hover {
  opacity: 1;
}

.compare-action-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  width: 100%;
  transition: all 0.3s ease;
}

.compare-action-btn:hover {
  background: #5a6fd8;
  transform: translateY(-1px);
}

/* Responsive */
@media (max-width: 768px) {
  .similar-product-card {
    flex: 0 0 160px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    align-self: stretch;
    justify-content: space-between;
  }
  
  .scroll-btn {
    display: none;
  }
  
  .product-info {
    padding: 0.5rem;
  }
  
  .product-name {
    font-size: 0.8rem;
  }
  
  .current-price {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .similar-products {
    margin: 1rem 0;
    padding: 1rem;
  }
  
  .similar-product-card {
    flex: 0 0 140px;
  }
  
  .product-image-wrapper {
    height: 100px;
  }
  
  .section-title {
    font-size: 1.3rem;
  }
  
  .compare-panel {
    padding: 0.75rem;
  }
}
</style>