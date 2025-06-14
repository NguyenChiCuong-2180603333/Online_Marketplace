<template>
  <div class="product-detail-page">
    <div class="container">
      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading"></div>
        <p class="mt-2">Đang tải thông tin sản phẩm...</p>
      </div>

      <!-- Product Detail -->
      <div v-else-if="product" class="product-detail">
        <!-- Breadcrumb -->
        <nav class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">🏠 Trang chủ</router-link>
          <span class="breadcrumb-separator">→</span>
          <router-link to="/products" class="breadcrumb-item">🌟 Sản phẩm</router-link>
          <span class="breadcrumb-separator">→</span>
          <span class="breadcrumb-current">{{ product.name }}</span>
        </nav>

        <!-- Main Product Info -->
        <div class="product-main space-card">
          <div class="product-images">
            <div class="main-image">
              <img 
                :src="selectedImage || product.images?.[0] || '/placeholder-product.jpg'" 
                :alt="product.name"
                class="main-product-image"
              />
              <div class="image-badges">
                <span v-if="isNewProduct" class="image-badge new">🆕 Mới</span>
                <span v-if="product.averageRating >= 4.5" class="image-badge hot">🔥 Hot</span>
              </div>
            </div>
            
            <div v-if="product.images && product.images.length > 1" class="thumbnail-images">
              <div 
                v-for="(image, index) in product.images" 
                :key="index"
                @click="selectedImage = image"
                :class="['thumbnail', { active: selectedImage === image }]"
              >
                <img :src="image" :alt="`${product.name} ${index + 1}`" />
              </div>
            </div>
          </div>

          <div class="product-info">
            <div class="product-category">{{ product.category }}</div>
            <h1 class="product-title">{{ product.name }}</h1>
            
            <div class="product-rating" v-if="product.averageRating > 0">
              <div class="stars">
                <span v-for="i in 5" :key="i" :class="['star', i <= product.averageRating ? 'filled' : '']">⭐</span>
              </div>
              <span class="rating-score">{{ product.averageRating.toFixed(1) }}</span>
              <span class="rating-count">({{ product.reviewCount }} đánh giá)</span>
            </div>

            <div class="product-price">
              <span class="current-price">{{ formatPrice(product.price) }}</span>
            </div>

            <div class="product-stock">
              <span v-if="product.stockQuantity > 0" class="in-stock">
                ✅ Còn {{ product.stockQuantity }} sản phẩm
              </span>
              <span v-else class="out-of-stock">
                ❌ Hết hàng
              </span>
            </div>

            <div class="product-description">
              <h3>📋 Mô tả sản phẩm</h3>
              <p>{{ product.description }}</p>
            </div>

            <div class="product-tags" v-if="product.tags && product.tags.length">
              <h4>🏷️ Tags:</h4>
              <div class="tags-list">
                <span v-for="tag in product.tags" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>

            <div class="product-actions">
              <div class="quantity-selector">
                <label class="quantity-label">Số lượng:</label>
                <div class="quantity-controls">
                  <button @click="decreaseQuantity" :disabled="quantity <= 1" class="quantity-btn">-</button>
                  <input v-model.number="quantity" type="number" min="1" :max="product.stockQuantity" class="quantity-input" />
                  <button @click="increaseQuantity" :disabled="quantity >= product.stockQuantity" class="quantity-btn">+</button>
                </div>
              </div>

              <div class="action-buttons">
                <button 
                  @click="addToCart" 
                  :disabled="product.stockQuantity === 0 || cartLoading"
                  class="btn btn-primary btn-lg"
                >
                  <span v-if="cartLoading">🔄 Đang thêm...</span>
                  <span v-else>🛒 Thêm vào giỏ hàng</span>
                </button>
                
                <button @click="buyNow" class="btn btn-warning btn-lg">
                  ⚡ Mua ngay
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Product Details Tabs -->
        <div class="product-tabs space-card">
          <div class="tab-headers">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="['tab-header', { active: activeTab === tab.id }]"
            >
              {{ tab.icon }} {{ tab.name }}
            </button>
          </div>

          <div class="tab-content">
            <!-- Description Tab -->
            <div v-if="activeTab === 'description'" class="tab-pane">
              <h3>Chi tiết sản phẩm</h3>
              <div class="description-content">
                <p>{{ product.description }}</p>
                <div v-if="product.tags && product.tags.length" class="product-specs">
                  <h4>Thông số:</h4>
                  <ul>
                    <li v-for="tag in product.tags" :key="tag">{{ tag }}</li>
                  </ul>
                </div>
              </div>
            </div>

            <!-- Reviews Tab -->
            <div v-if="activeTab === 'reviews'" class="tab-pane">
              <h3>Đánh giá sản phẩm</h3>
              <div class="reviews-summary">
                <div class="overall-rating">
                  <div class="rating-count-large">{{ product.reviewCount }} đánh giá</div>
                </div>
              </div>
              
              <div class="reviews-list">
                <div v-if="reviews.length === 0" class="no-reviews">
                  <p>Chưa có đánh giá nào cho sản phẩm này.</p>
                  <p>Hãy là người đầu tiên đánh giá!</p>
                </div>
                <div v-else>
                  <div v-for="review in reviews" :key="review.id" class="review-item">
                    <div class="review-header">
                      <div class="reviewer-info">
                        <img :src="review.userAvatar || '/default-avatar.png'" class="reviewer-avatar" />
                        <span class="reviewer-name">{{ review.userName }}</span>
                      </div>
                      <div class="review-rating">
                        <span v-for="i in 5" :key="i" :class="['star', i <= review.rating ? 'filled' : '']">⭐</span>
                      </div>
                    </div>
                    <div class="review-content">
                      <p>{{ review.comment }}</p>
                    </div>
                    <div class="review-date">
                      {{ formatDate(review.createdAt) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Seller Tab -->
            <div v-if="activeTab === 'seller'" class="tab-pane">
              <h3>Thông tin người bán</h3>
              <div class="seller-info">
                <div class="seller-avatar">👨‍🚀</div>
                <div class="seller-details">
                  <h4>{{ sellerInfo.name || 'Cosmic Seller' }}</h4>
                  <p class="seller-stats">
                    ⭐ Đánh giá: {{ sellerInfo.rating || '4.8' }}/5 <br>
                    📦 Sản phẩm: {{ sellerInfo.productCount || '25+' }} <br>
                    🕒 Tham gia: {{ sellerInfo.joinDate || '2024' }}
                  </p>
                  <button class="btn btn-secondary">📧 Liên hệ người bán</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Related Products -->
        <div class="related-products">
          <h2 class="section-title">🌟 Sản phẩm liên quan</h2>
          
          <div v-if="relatedLoading" class="loading-section">
            <div class="loading"></div>
          </div>
          
          <div v-else class="related-grid">
            <div v-for="relatedProduct in relatedProducts" :key="relatedProduct.id" class="related-card space-card">
              <router-link :to="`/products/${relatedProduct.id}`" class="related-link">
                <img 
                  :src="relatedProduct.images?.[0] || '/placeholder-product.jpg'" 
                  :alt="relatedProduct.name"
                  class="related-image"
                />
                <div class="related-info">
                  <h4>{{ relatedProduct.name }}</h4>
                  <p class="related-price">{{ formatPrice(relatedProduct.price) }}</p>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Not Found -->
      <div v-else class="not-found">
        <div class="not-found-content space-card">
          <div class="not-found-icon">🔍</div>
          <h2>Không tìm thấy sản phẩm</h2>
          <p>Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
          <router-link to="/products" class="btn btn-primary">🌟 Xem sản phẩm khác</router-link>
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
import { productAPI } from '@/services/api'

export default {
  name: 'ProductDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const cartStore = useCartStore()
    const authStore = useAuthStore()
    
    // Reactive data
    const product = ref(null)
    const relatedProducts = ref([])
    const reviews = ref([])
    const loading = ref(false)
    const relatedLoading = ref(false)
    const cartLoading = ref(false)
    const selectedImage = ref('')
    const quantity = ref(1)
    const activeTab = ref('description')
    
    // Computed
    const isNewProduct = computed(() => {
      if (!product.value) return false
      const createdDate = new Date(product.value.createdAt)
      const now = new Date()
      const diffDays = Math.floor((now - createdDate) / (1000 * 60 * 60 * 24))
      return diffDays <= 7
    })

    const sellerInfo = computed(() => ({
      name: 'Cosmic Seller',
      rating: '4.8',
      productCount: '25+',
      joinDate: '2024'
    }))

    const tabs = [
      { id: 'description', name: 'Mô tả', icon: '📋' },
      { id: 'reviews', name: 'Đánh giá', icon: '⭐' },
      { id: 'seller', name: 'Người bán', icon: '👨‍🚀' }
    ]

    // Methods
    const loadProduct = async () => {
      loading.value = true
      try {
        const response = await productAPI.getById(route.params.id)
        product.value = response.data
        selectedImage.value = product.value.images?.[0] || ''
        
        // Load related products
        loadRelatedProducts()
      } catch (error) {
        console.error('Error loading product:', error)
        product.value = null
      } finally {
        loading.value = false
      }
    }

    const loadRelatedProducts = async () => {
      if (!product.value) return
      
      relatedLoading.value = true
      try {
        const response = await productAPI.getAll({
          category: product.value.category,
          limit: 4
        })
        relatedProducts.value = response.data
          .filter(p => p.id !== product.value.id)
          .slice(0, 4)
      } catch (error) {
        console.error('Error loading related products:', error)
      } finally {
        relatedLoading.value = false
      }
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const increaseQuantity = () => {
      if (quantity.value < product.value.stockQuantity) {
        quantity.value++
      }
    }

    const decreaseQuantity = () => {
      if (quantity.value > 1) {
        quantity.value--
      }
    }

    const addToCart = async () => {
      if (!authStore.isAuthenticated) {
        router.push('/login')
        return
      }

      cartLoading.value = true
      try {
        await cartStore.addItem(product.value.id, quantity.value)
        // Show success message
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        console.error('Add to cart error:', error)
        alert('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng')
      } finally {
        cartLoading.value = false
      }
    }

    const buyNow = async () => {
      if (!authStore.isAuthenticated) {
        router.push('/login')
        return
      }

      try {
        await addToCart()
        router.push('/cart')
      } catch (error) {
        console.error('Buy now error:', error)
      }
    }

    // Watch route changes
    watch(() => route.params.id, () => {
      if (route.params.id) {
        loadProduct()
      }
    })

    onMounted(() => {
      loadProduct()
    })

    return {
      product,
      relatedProducts,
      reviews,
      loading,
      relatedLoading,
      cartLoading,
      selectedImage,
      quantity,
      activeTab,
      isNewProduct,
      sellerInfo,
      tabs,
      formatPrice,
      formatDate,
      increaseQuantity,
      decreaseQuantity,
      addToCart,
      buyNow
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  padding: 20px 0;
}

.loading-section {
  text-align: center;
  padding: 60px 0;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.breadcrumb-item {
  color: var(--text-secondary);
  text-decoration: none;
}

.breadcrumb-item:hover {
  color: var(--text-accent);
}

.breadcrumb-separator {
  color: var(--text-secondary);
}

.breadcrumb-current {
  color: var(--text-primary);
  font-weight: 500;
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 40px;
  margin-bottom: 32px;
}

.main-image {
  position: relative;
  margin-bottom: 16px;
}

.main-product-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 12px;
}

.image-badges {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.image-badge.new {
  background: var(--aurora-gradient);
  color: var(--space-dark);
}

.image-badge.hot {
  background: var(--nebula-gradient);
  color: white;
}

.thumbnail-images {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
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

.product-category {
  font-size: 14px;
  color: var(--text-accent);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.product-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rating-score {
  font-weight: 600;
  color: var(--text-accent);
}

.rating-count {
  color: var(--text-secondary);
}

.current-price {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-accent);
}

.product-stock {
  margin: 16px 0;
}

.in-stock {
  color: var(--text-success);
  font-weight: 500;
}

.out-of-stock {
  color: var(--text-danger);
  font-weight: 500;
}

.product-description {
  margin: 24px 0;
}

.product-description h3 {
  margin-bottom: 12px;
  color: var(--text-accent);
}

.product-tags {
  margin: 20px 0;
}

.product-tags h4 {
  margin-bottom: 8px;
  color: var(--text-secondary);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
}

.product-actions {
  margin-top: 32px;
}

.quantity-selector {
  margin-bottom: 20px;
}

.quantity-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 150px;
}

.quantity-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--text-accent);
  background: transparent;
  color: var(--text-accent);
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.quantity-btn:hover:not(:disabled) {
  background: var(--text-accent);
  color: var(--space-dark);
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  text-align: center;
  border: 1px solid var(--text-accent);
  background: transparent;
  color: var(--text-primary);
  padding: 8px;
  border-radius: 8px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.btn-lg {
  padding: 16px 32px;
  font-size: 16px;
  flex: 1;
}

.product-tabs {
  padding: 0;
  margin-bottom: 32px;
}

.tab-headers {
  display: flex;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.tab-header {
  padding: 16px 24px;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.tab-header:hover {
  color: var(--text-accent);
}

.tab-header.active {
  color: var(--text-accent);
  border-bottom-color: var(--text-accent);
}

.tab-content {
  padding: 32px;
}

.reviews-summary {
  margin-bottom: 32px;
}

.overall-rating {
  text-align: center;
  padding: 24px;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 12px;
}

.rating-score-large {
  font-size: 3rem;
  font-weight: 700;
  color: var(--text-accent);
}

.rating-stars-large {
  margin: 8px 0;
}

.star-large {
  font-size: 24px;
  opacity: 0.3;
}

.star-large.filled {
  opacity: 1;
}

.rating-count-large {
  color: var(--text-secondary);
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.review-item {
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  padding: 20px 0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.reviewer-name {
  font-weight: 500;
}

.review-content {
  margin-bottom: 8px;
  line-height: 1.6;
}

.review-date {
  color: var(--text-secondary);
  font-size: 14px;
}

.seller-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.seller-avatar {
  font-size: 4rem;
}

.seller-details h4 {
  color: var(--text-accent);
  margin-bottom: 8px;
}

.seller-stats {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 32px;
  color: var(--text-accent);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.related-card {
  padding: 16px;
  transition: all 0.3s ease;
}

.related-card:hover {
  transform: translateY(-5px);
}

.related-link {
  text-decoration: none;
  color: inherit;
}

.related-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

.related-info h4 {
  margin-bottom: 8px;
  color: var(--text-primary);
}

.related-price {
  color: var(--text-accent);
  font-weight: 600;
}

.not-found {
  padding: 60px 0;
}

.not-found-content {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
  padding: 40px;
}

.not-found-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  opacity: 0.5;
}

@media (max-width: 768px) {
  .product-main {
    grid-template-columns: 1fr;
    padding: 20px;
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
  
  .tab-headers {
    overflow-x: auto;
  }
  
  .related-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>score-large">{{ product.averageRating.toFixed(1) }}</div>
                  <div class="rating-stars-large">
                    <span v-for="i in 5" :key="i" :class="['star-large', i <= product.averageRating ? 'filled' : '']">⭐</span>
                  </div>
                  <div class="rating-count-large">{{ product.reviewCount }} đánh giá</div>