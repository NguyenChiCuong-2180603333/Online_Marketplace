<template>
  <div class="product-detail">
    <div class="container">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <router-link to="/" class="breadcrumb-item">🏠 Trang chủ</router-link>
        <span class="breadcrumb-separator">→</span>
        <router-link to="/products" class="breadcrumb-item">📦 Sản phẩm</router-link>
        <span class="breadcrumb-separator">→</span>
        <span class="breadcrumb-current">{{ product?.name || 'Đang tải...' }}</span>
      </nav>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="cosmic-loader">
          <div class="planet"></div>
          <div class="orbit"></div>
          <div class="orbit orbit-2"></div>
        </div>
        <p>Đang tải thông tin sản phẩm từ vũ trụ...</p>
      </div>

      <!-- Product Detail Content -->
      <div v-else-if="product" class="product-content">
        <div class="product-gallery">
          <!-- Main Image -->
          <div class="main-image">
            <img 
              :src="selectedImage || product.images?.[0] || '/placeholder-product.jpg'" 
              :alt="product.name"
              class="main-product-image"
            />
            <div class="image-controls">
              <button @click="zoomImage" class="control-btn">🔍</button>
              <button @click="favoriteProduct" class="control-btn" :class="{ active: isFavorited }">
                {{ isFavorited ? '❤️' : '🤍' }}
              </button>
            </div>
          </div>

          <!-- Thumbnail Gallery -->
          <div v-if="product.images && product.images.length > 1" class="thumbnail-gallery">
            <button 
              v-for="(image, index) in product.images" 
              :key="index"
              @click="selectedImage = image"
              class="thumbnail"
              :class="{ active: selectedImage === image || (index === 0 && !selectedImage) }"
            >
              <img :src="image" :alt="`${product.name} ${index + 1}`" />
            </button>
          </div>
        </div>

        <div class="product-info">
          <div class="product-header">
            <div class="product-category">
              <span class="category-tag">{{ product.category?.name || 'Danh mục' }}</span>
              <span class="stock-status" :class="stockStatusClass">
                {{ stockStatusText }}
              </span>
            </div>
            
            <h1 class="product-title">{{ product.name }}</h1>
            
            <div class="rating-section">
              <div class="rating-stars">
                <span v-for="i in 5" :key="i" class="star" :class="[i <= product.rating ? 'filled' : '']">⭐</span>
              </div>
              <span class="rating-text">({{ product.reviewCount || 0 }} đánh giá)</span>
            </div>

            <div class="price-section">
              <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                {{ formatCurrency(product.originalPrice) }}
              </span>
              <span class="current-price">{{ formatCurrency(product.price) }}</span>
              <span v-if="discountPercentage" class="discount-badge">-{{ discountPercentage }}%</span>
            </div>
          </div>

          <div class="product-description">
            <h3>📝 Mô tả sản phẩm</h3>
            <div class="description-content" v-html="product.description"></div>
          </div>

          <div class="product-features" v-if="product.features">
            <h3>✨ Tính năng nổi bật</h3>
            <ul class="features-list">
              <li v-for="feature in product.features" :key="feature">{{ feature }}</li>
            </ul>
          </div>

          <div class="product-actions">
            <div class="quantity-selector">
              <label for="quantity">Số lượng:</label>
              <div class="quantity-controls">
                <button @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
                <input 
                  id="quantity" 
                  v-model="quantity" 
                  type="number" 
                  min="1" 
                  :max="product.stock"
                  @change="validateQuantity"
                />
                <button @click="increaseQuantity" :disabled="quantity >= product.stock">+</button>
              </div>
            </div>

            <div class="action-buttons">
              <button 
                @click="addToCart" 
                class="btn btn-primary btn-large"
                :disabled="!canAddToCart || cartLoading"
                :class="{ loading: cartLoading }"
              >
                <span v-if="cartLoading">🔄 Đang thêm...</span>
                <span v-else>🛒 Thêm vào giỏ hàng</span>
              </button>
              
              <button 
                @click="buyNow" 
                class="btn btn-accent btn-large"
                :disabled="!canAddToCart"
              >
                🚀 Mua ngay
              </button>
            </div>
          </div>

          <!-- Seller Info -->
          <div class="seller-info space-card">
            <h3>🏪 Thông tin người bán</h3>
            <div class="seller-details">
              <div class="seller-avatar">
                <img :src="product.seller?.avatar || '/default-avatar.png'" :alt="product.seller?.name" />
              </div>
              <div class="seller-content">
                <h4>{{ product.seller?.name || 'Người bán' }}</h4>
                <div class="seller-stats">
                  <span>⭐ {{ product.seller?.rating || 4.5 }}/5</span>
                  <span>📦 {{ product.seller?.productCount || 0 }} sản phẩm</span>
                  <span>👥 {{ product.seller?.followerCount || 0 }} theo dõi</span>
                </div>
                <button class="btn btn-secondary btn-small">💬 Nhắn tin</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Reviews Section -->
      <div v-if="product" class="reviews-section">
        <div class="reviews-header">
          <h2>💬 Đánh giá sản phẩm</h2>
          <div class="reviews-summary">
            <div class="average-rating">
              <span class="rating-number">{{ product.rating || 0 }}</span>
              <div class="rating-stars">
                <span v-for="i in 5" :key="i" class="star" :class="[i <= product.rating ? 'filled' : '']">⭐</span>
              </div>
              <span class="total-reviews">({{ reviews.length }} đánh giá)</span>
            </div>
            <button @click="showReviewForm = !showReviewForm" class="btn btn-secondary">
              ✍️ Viết đánh giá
            </button>
          </div>
        </div>

        <!-- Review Form -->
        <div v-if="showReviewForm" class="review-form space-card">
          <h3>✍️ Viết đánh giá của bạn</h3>
          <form @submit.prevent="submitReview">
            <div class="form-group">
              <label>Đánh giá:</label>
              <div class="rating-input">
                <button 
                  v-for="i in 5" 
                  :key="i"
                  type="button"
                  @click="newReview.rating = i"
                  class="star-btn"
                  :class="{ active: i <= newReview.rating }"
                >
                  ⭐
                </button>
              </div>
            </div>
            
            <div class="form-group">
              <label for="review-title">Tiêu đề:</label>
              <input 
                id="review-title"
                v-model="newReview.title" 
                type="text" 
                class="form-input"
                placeholder="Tóm tắt đánh giá của bạn..."
                required
              />
            </div>
            
            <div class="form-group">
              <label for="review-content">Nội dung:</label>
              <textarea 
                id="review-content"
                v-model="newReview.content" 
                class="form-input"
                rows="4"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
                required
              ></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="showReviewForm = false" class="btn btn-secondary">Hủy</button>
              <button type="submit" class="btn btn-primary" :disabled="reviewLoading">
                {{ reviewLoading ? '🔄 Đang gửi...' : '📤 Gửi đánh giá' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Reviews List -->
        <div class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-item space-card">
            <div class="review-header">
              <div class="reviewer-info">
                <img :src="review.user?.avatar || '/default-avatar.png'" :alt="review.user?.name" class="reviewer-avatar" />
                <div class="reviewer-details">
                  <h4>{{ review.user?.name || 'Người dùng' }}</h4>
                  <div class="review-meta">
                    <div class="rating-stars">
                      <span v-for="i in 5" :key="i" class="star" :class="[i <= review.rating ? 'filled' : '']">⭐</span>
                    </div>
                    <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="review-content">
              <h5 v-if="review.title">{{ review.title }}</h5>
              <p>{{ review.content }}</p>
            </div>
            
            <div class="review-actions">
              <button @click="likeReview(review.id)" class="action-btn" :class="{ active: review.isLiked }">
                👍 {{ review.likeCount || 0 }}
              </button>
              <button @click="replyToReview(review.id)" class="action-btn">💬 Trả lời</button>
            </div>
          </div>

          <!-- Load More Reviews -->
          <div v-if="hasMoreReviews" class="load-more-container">
            <button @click="loadMoreReviews" class="btn btn-secondary" :disabled="reviewsLoading">
              {{ reviewsLoading ? '🔄 Đang tải...' : '📖 Xem thêm đánh giá' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Related Products -->
      <div class="related-products">
        <h2>🌟 Sản phẩm liên quan</h2>
        <div class="products-grid">
          <div v-for="relatedProduct in relatedProducts" :key="relatedProduct.id" class="product-card space-card">
            <router-link :to="`/products/${relatedProduct.id}`" class="product-link">
              <div class="product-image">
                <img :src="relatedProduct.images?.[0] || '/placeholder-product.jpg'" :alt="relatedProduct.name" />
              </div>
              <div class="product-info">
                <h3>{{ relatedProduct.name }}</h3>
                <div class="product-price">{{ formatCurrency(relatedProduct.price) }}</div>
                <div class="product-rating">
                  <div class="rating-stars">
                    <span v-for="i in 5" :key="i" class="star" :class="[i <= relatedProduct.rating ? 'filled' : '']">⭐</span>
                  </div>
                  <span>({{ relatedProduct.reviewCount }})</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'ProductDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const cartStore = useCartStore()
    const authStore = useAuthStore()
    
    // Reactive data
    const loading = ref(true)
    const cartLoading = ref(false)
    const reviewLoading = ref(false)
    const reviewsLoading = ref(false)
    const product = ref(null)
    const selectedImage = ref('')
    const quantity = ref(1)
    const isFavorited = ref(false)
    const showReviewForm = ref(false)
    const reviews = ref([])
    const relatedProducts = ref([])
    const hasMoreReviews = ref(false)
    
    // Review form data
    const newReview = ref({
      rating: 5,
      title: '',
      content: ''
    })
    
    // Computed properties
    const stockStatusClass = computed(() => {
      if (!product.value) return 'out-of-stock'
      if (product.value.stock === 0) return 'out-of-stock'
      if (product.value.stock < 10) return 'low-stock'
      return 'in-stock'
    })
    
    const stockStatusText = computed(() => {
      if (!product.value) return 'Hết hàng'
      if (product.value.stock === 0) return 'Hết hàng'
      if (product.value.stock < 10) return `Còn ${product.value.stock} sản phẩm`
      return 'Còn hàng'
    })
    
    const discountPercentage = computed(() => {
      if (!product.value?.originalPrice || product.value.originalPrice <= product.value.price) return null
      return Math.round(((product.value.originalPrice - product.value.price) / product.value.originalPrice) * 100)
    })
    
    const canAddToCart = computed(() => {
      return product.value && product.value.stock > 0 && quantity.value <= product.value.stock
    })
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }
    
    const loadProductDetail = async () => {
      try {
        loading.value = true
        const productId = route.params.id
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Mock product data
        product.value = {
          id: productId,
          name: 'Laptop Gaming Galactic Pro',
          description: 'Laptop gaming cao cấp với hiệu năng vượt trội, thiết kế futuristic và công nghệ tiên tiến từ tương lai.',
          price: 25000000,
          originalPrice: 30000000,
          stock: 15,
          rating: 4.5,
          reviewCount: 128,
          category: { name: 'Công nghệ' },
          images: [
            '/placeholder-product.jpg',
            '/placeholder-product-2.jpg',
            '/placeholder-product-3.jpg'
          ],
          features: [
            'CPU Intel Core i9 thế hệ 13',
            'GPU RTX 4080 16GB VRAM',
            'RAM 32GB DDR5',
            'SSD 1TB NVMe Gen4',
            'Màn hình 17.3" 4K 144Hz',
            'Hệ thống tản nhiệt quantum'
          ],
          seller: {
            name: 'Cosmic Tech Store',
            rating: 4.8,
            productCount: 456,
            followerCount: 12340,
            avatar: '/seller-avatar.jpg'
          }
        }
        
        // Load reviews and related products
        await Promise.all([
          loadReviews(),
          loadRelatedProducts()
        ])
        
      } catch (error) {
        console.error('Error loading product:', error)
      } finally {
        loading.value = false
      }
    }
    
    const loadReviews = async () => {
      // Mock reviews data
      reviews.value = [
        {
          id: 1,
          rating: 5,
          title: 'Sản phẩm tuyệt vời!',
          content: 'Chất lượng vượt mong đợi, giao hàng nhanh, đóng gói cẩn thận.',
          user: { name: 'Nguyễn Văn A', avatar: '/user-avatar-1.jpg' },
          createdAt: '2024-01-15',
          likeCount: 24,
          isLiked: false
        },
        {
          id: 2,
          rating: 4,
          title: 'Tốt nhưng có thể cải thiện',
          content: 'Sản phẩm ok, tuy nhiên có thể cải thiện thêm về bao bì.',
          user: { name: 'Trần Thị B', avatar: '/user-avatar-2.jpg' },
          createdAt: '2024-01-10',
          likeCount: 12,
          isLiked: true
        }
      ]
      hasMoreReviews.value = true
    }
    
    const loadRelatedProducts = async () => {
      // Mock related products
      relatedProducts.value = [
        { id: 2, name: 'Gaming Mouse Nebula', price: 1500000, rating: 4.3, reviewCount: 89 },
        { id: 3, name: 'Mechanical Keyboard Cosmos', price: 2200000, rating: 4.7, reviewCount: 156 },
        { id: 4, name: 'Gaming Headset Galaxy', price: 1800000, rating: 4.2, reviewCount: 67 }
      ]
    }
    
    const increaseQuantity = () => {
      if (quantity.value < product.value.stock) {
        quantity.value++
      }
    }
    
    const decreaseQuantity = () => {
      if (quantity.value > 1) {
        quantity.value--
      }
    }
    
    const validateQuantity = () => {
      if (quantity.value < 1) quantity.value = 1
      if (quantity.value > product.value.stock) quantity.value = product.value.stock
    }
    
    const addToCart = async () => {
      try {
        cartLoading.value = true
        await cartStore.addToCart(product.value.id, quantity.value)
        // Show success message
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        console.error('Error adding to cart:', error)
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng')
      } finally {
        cartLoading.value = false
      }
    }
    
    const buyNow = () => {
      addToCart().then(() => {
        router.push('/cart')
      })
    }
    
    const favoriteProduct = () => {
      isFavorited.value = !isFavorited.value
      // TODO: API call to add/remove favorite
    }
    
    const zoomImage = () => {
      // TODO: Implement image zoom functionality
      alert('Chức năng zoom ảnh đang được phát triển!')
    }
    
    const submitReview = async () => {
      if (!authStore.isAuthenticated) {
        alert('Vui lòng đăng nhập để viết đánh giá')
        return
      }
      
      try {
        reviewLoading.value = true
        // TODO: API call to submit review
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Add new review to list
        const review = {
          id: Date.now(),
          ...newReview.value,
          user: { name: authStore.user.name, avatar: authStore.user.avatar },
          createdAt: new Date().toISOString(),
          likeCount: 0,
          isLiked: false
        }
        reviews.value.unshift(review)
        
        // Reset form
        newReview.value = { rating: 5, title: '', content: '' }
        showReviewForm.value = false
        
        alert('Đánh giá đã được gửi thành công!')
      } catch (error) {
        console.error('Error submitting review:', error)
        alert('Có lỗi xảy ra khi gửi đánh giá')
      } finally {
        reviewLoading.value = false
      }
    }
    
    const likeReview = (reviewId) => {
      const review = reviews.value.find(r => r.id === reviewId)
      if (review) {
        review.isLiked = !review.isLiked
        review.likeCount += review.isLiked ? 1 : -1
      }
    }
    
    const replyToReview = (reviewId) => {
      alert('Chức năng trả lời đánh giá đang được phát triển!')
    }
    
    const loadMoreReviews = async () => {
      try {
        reviewsLoading.value = true
        await new Promise(resolve => setTimeout(resolve, 1000))
        // TODO: Load more reviews from API
        hasMoreReviews.value = false
      } finally {
        reviewsLoading.value = false
      }
    }
    
    // Lifecycle
    onMounted(() => {
      loadProductDetail()
    })
    
    // Watch route changes
    watch(() => route.params.id, () => {
      if (route.name === 'ProductDetail') {
        loadProductDetail()
      }
    })
    
    return {
      loading,
      cartLoading,
      reviewLoading,
      reviewsLoading,
      product,
      selectedImage,
      quantity,
      isFavorited,
      showReviewForm,
      reviews,
      relatedProducts,
      hasMoreReviews,
      newReview,
      stockStatusClass,
      stockStatusText,
      discountPercentage,
      canAddToCart,
      formatCurrency,
      formatDate,
      increaseQuantity,
      decreaseQuantity,
      validateQuantity,
      addToCart,
      buyNow,
      favoriteProduct,
      zoomImage,
      submitReview,
      likeReview,
      replyToReview,
      loadMoreReviews
    }
  }
}
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  padding: 2rem 0;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
  font-size: 0.9rem;
}

.breadcrumb-item {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-item:hover {
  color: var(--text-accent);
}

.breadcrumb-current {
  color: var(--text-primary);
  font-weight: 500;
}

.breadcrumb-separator {
  color: var(--text-secondary);
  opacity: 0.6;
}

.loading-container {
  text-align: center;
  padding: 4rem 0;
}

.cosmic-loader {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
}

.planet {
  width: 40px;
  height: 40px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

.orbit:nth-child(2) {
  width: 60px;
  height: 60px;
  top: 10px;
  left: 10px;
}

.orbit-2 {
  width: 80px;
  height: 80px;
  top: 0;
  left: 0;
  animation-duration: 4s;
  animation-direction: reverse;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.1); }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.product-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-bottom: 4rem;
}

.product-gallery {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.main-image {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(26, 26, 46, 0.5);
}

.main-product-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.main-image:hover .main-product-image {
  transform: scale(1.05);
}

.image-controls {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  gap: 0.5rem;
}

.control-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.control-btn:hover {
  background: rgba(0, 212, 255, 0.8);
  transform: scale(1.1);
}

.control-btn.active {
  background: var(--accent-gradient);
}

.thumbnail-gallery {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding: 0.5rem;
}

.thumbnail {
  flex: none;
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.thumbnail.active {
  border-color: var(--text-accent);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.product-header {
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  padding-bottom: 2rem;
}

.product-category {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.category-tag {
  background: var(--aurora-gradient);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.stock-status {
  font-size: 0.9rem;
  font-weight: 500;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
}

.stock-status.in-stock {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.stock-status.low-stock {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
}

.stock-status.out-of-stock {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.product-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.2;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.rating-stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  font-size: 1.2rem;
  opacity: 0.3;
  transition: all 0.3s ease;
}

.star.filled {
  opacity: 1;
  filter: drop-shadow(0 0 5px currentColor);
}

.rating-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.original-price {
  font-size: 1.1rem;
  color: var(--text-secondary);
  text-decoration: line-through;
  opacity: 0.7;
}

.current-price {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-accent);
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}

.discount-badge {
  background: var(--accent-gradient);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.product-description h3,
.product-features h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
  font-size: 1.2rem;
}

.description-content {
  color: var(--text-secondary);
  line-height: 1.7;
}

.features-list {
  list-style: none;
  padding: 0;
}

.features-list li {
  color: var(--text-secondary);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  position: relative;
  padding-left: 2rem;
}

.features-list li:before {
  content: '🚀';
  position: absolute;
  left: 0;
  top: 0.5rem;
}

.product-actions {
  background: rgba(26, 26, 46, 0.5);
  padding: 2rem;
  border-radius: 16px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.quantity-selector {
  margin-bottom: 2rem;
}

.quantity-selector label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 150px;
}

.quantity-controls button {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.2rem;
  font-weight: bold;
}

.quantity-controls button:hover:not(:disabled) {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.quantity-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-controls input {
  flex: 1;
  text-align: center;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary);
  border-radius: 8px;
  padding: 0.5rem;
  font-size: 1rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.btn-large {
  flex: 1;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  font-weight: 600;
  min-height: 50px;
}

.seller-info {
  margin-top: 2rem;
}

.seller-info h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.seller-details {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.seller-avatar {
  flex: none;
}

.seller-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.seller-content h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.seller-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.btn-small {
  padding: 0.4rem 1rem;
  font-size: 0.9rem;
}

.reviews-section {
  margin-top: 4rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.reviews-header h2 {
  color: var(--text-accent);
}

.reviews-summary {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.rating-number {
  font-size: 2rem;
  font-weight: bold;
  color: var(--text-accent);
}

.total-reviews {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.review-form {
  margin-bottom: 2rem;
}

.review-form h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
}

.rating-input {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1rem;
}

.star-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  opacity: 0.3;
  transition: all 0.3s ease;
}

.star-btn.active,
.star-btn:hover {
  opacity: 1;
  transform: scale(1.1);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-item {
  padding: 1.5rem;
}

.review-header {
  margin-bottom: 1rem;
}

.reviewer-info {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.reviewer-details h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.review-meta .rating-stars .star {
  font-size: 1rem;
}

.review-date {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.review-content h5 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.review-content p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.review-actions {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
}

.action-btn {
  background: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.action-btn:hover,
.action-btn.active {
  border-color: var(--text-accent);
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.load-more-container {
  text-align: center;
  margin-top: 2rem;
}

.related-products {
  margin-top: 4rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.related-products h2 {
  color: var(--text-accent);
  margin-bottom: 2rem;
  text-align: center;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.product-card {
  padding: 1rem;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-link {
  text-decoration: none;
  color: inherit;
}

.product-card .product-image {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.product-card .product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-card .product-info h3 {
  font-size: 1rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.product-card .product-price {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.product-card .product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.product-card .rating-stars .star {
  font-size: 0.8rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .product-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .reviews-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .reviews-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .product-detail {
    padding: 1rem 0;
  }
  
  .breadcrumb {
    font-size: 0.8rem;
    flex-wrap: wrap;
  }
  
  .product-title {
    font-size: 1.5rem;
  }
  
  .current-price {
    font-size: 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .form-actions {
    justify-content: stretch;
  }
  
  .form-actions .btn {
    flex: 1;
  }
  
  .reviewer-info {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .review-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

@media (max-width: 480px) {
  .main-product-image {
    height: 250px;
  }
  
  .price-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .seller-details {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .seller-stats {
    flex-direction: column;
    gap: 0.25rem;
  }
}