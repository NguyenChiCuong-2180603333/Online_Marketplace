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
              @click="openImageModal"
            />
            <div class="image-controls">
              <button @click="openImageModal" class="control-btn">🔍</button>
              <button @click="favoriteProduct" class="control-btn" :class="{ active: isFavorited }">
                {{ isFavorited ? '❤️' : '🤍' }}
              </button>
              <button @click="shareProduct" class="control-btn">📤</button>
            </div>
            <!-- Product badges -->
            <div class="product-badges">
              <span v-if="product.isNew" class="badge badge-new">🆕 Mới</span>
              <span v-if="discountPercentage" class="badge badge-sale">🔥 Sale</span>
              <span v-if="product.isBestSeller" class="badge badge-bestseller">⭐ Bán chạy</span>
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
              <button @click="scrollToReviews" class="view-reviews-btn">Xem tất cả đánh giá</button>
            </div>

            <div class="price-section">
              <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                {{ formatCurrency(product.originalPrice) }}
              </span>
              <span class="current-price">{{ formatCurrency(product.price) }}</span>
              <span v-if="discountPercentage" class="discount-badge">-{{ discountPercentage }}%</span>
            </div>

            <!-- Shipping Info -->
            <div class="shipping-info">
              <div class="shipping-item">
                <span class="icon">🚚</span>
                <span>Giao hàng miễn phí cho đơn từ {{ formatCurrency(500000) }}</span>
              </div>
              <div class="shipping-item">
                <span class="icon">⚡</span>
                <span>Giao hàng nhanh trong 2-4 giờ</span>
              </div>
              <div class="shipping-item">
                <span class="icon">🔄</span>
                <span>Đổi trả miễn phí trong 7 ngày</span>
              </div>
            </div>
          </div>

          <!-- Product Variants -->
          <div v-if="product.variants && product.variants.length" class="product-variants">
            <h3>Lựa chọn:</h3>
            <div v-for="variant in product.variants" :key="variant.name" class="variant-group">
              <label>{{ variant.name }}:</label>
              <div class="variant-options">
                <button 
                  v-for="option in variant.options" 
                  :key="option.value"
                  @click="selectVariant(variant.name, option)"
                  class="variant-option"
                  :class="{ active: selectedVariants[variant.name] === option.value }"
                  :disabled="!option.available"
                >
                  {{ option.label }}
                  <span v-if="option.price !== product.price" class="variant-price">
                    (+{{ formatCurrency(option.price - product.price) }})
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div class="product-description">
            <h3>📝 Mô tả sản phẩm</h3>
            <div class="description-content" v-html="product.description"></div>
            <button v-if="product.fullDescription" @click="showFullDescription = !showFullDescription" class="toggle-description">
              {{ showFullDescription ? 'Thu gọn' : 'Xem thêm' }}
            </button>
            <div v-if="showFullDescription && product.fullDescription" class="full-description" v-html="product.fullDescription"></div>
          </div>

          <!-- Technical Specifications -->
          <div class="product-specs" v-if="product.specifications">
            <h3>🔧 Thông số kỹ thuật</h3>
            <div class="specs-table">
              <div v-for="spec in product.specifications" :key="spec.name" class="spec-row">
                <span class="spec-name">{{ spec.name }}</span>
                <span class="spec-value">{{ spec.value }}</span>
              </div>
            </div>
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
              <span class="total-price">Tổng: {{ formatCurrency(totalPrice) }}</span>
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

              <button @click="addToWishlist" class="btn btn-secondary btn-large">
                {{ isInWishlist ? '💖 Đã yêu thích' : '🤍 Yêu thích' }}
              </button>
            </div>

            <!-- Payment Methods -->
            <div class="payment-methods">
              <h4>Phương thức thanh toán:</h4>
              <div class="payment-icons">
                <span class="payment-icon">💳</span>
                <span class="payment-icon">🏧</span>
                <span class="payment-icon">📱</span>
                <span class="payment-icon">💰</span>
              </div>
            </div>
          </div>

          <!-- Seller Info -->
          <div class="seller-info space-card">
            <h3>🏪 Thông tin người bán</h3>
            <div class="seller-details">
              <div class="seller-avatar">
                <img :src="product.seller?.avatar || '/default-avatar.png'" :alt="product.seller?.name" />
                <div class="seller-status online">🟢</div>
              </div>
              <div class="seller-content">
                <h4>{{ product.seller?.name || 'Người bán' }}</h4>
                <div class="seller-stats">
                  <span>⭐ {{ product.seller?.rating || 4.5 }}/5</span>
                  <span>📦 {{ product.seller?.productCount || 0 }} sản phẩm</span>
                  <span>👥 {{ product.seller?.followerCount || 0 }} theo dõi</span>
                  <span>📍 {{ product.seller?.location || 'TP.HCM' }}</span>
                </div>
                <div class="seller-actions">
                  <button @click="chatWithSeller" class="btn btn-secondary btn-small">💬 Nhắn tin</button>
                  <button @click="viewSellerProfile" class="btn btn-outline btn-small">👤 Xem shop</button>
                  <button @click="followSeller" class="btn btn-outline btn-small">
                    {{ isFollowingSeller ? '✓ Đã theo dõi' : '+ Theo dõi' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Product FAQ -->
          <div class="product-faq" v-if="product.faq && product.faq.length">
            <h3>❓ Câu hỏi thường gặp</h3>
            <div class="faq-list">
              <div v-for="(faq, index) in product.faq" :key="index" class="faq-item">
                <button @click="toggleFaq(index)" class="faq-question">
                  <span>{{ faq.question }}</span>
                  <span class="faq-toggle">{{ expandedFaq.includes(index) ? '−' : '+' }}</span>
                </button>
                <div v-show="expandedFaq.includes(index)" class="faq-answer">
                  {{ faq.answer }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Reviews Section -->
      <div v-if="product" class="reviews-section" ref="reviewsSection">
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
            
            <!-- Rating breakdown -->
            <div class="rating-breakdown">
              <div v-for="rating in 5" :key="rating" class="rating-bar">
                <span>{{ 6 - rating }} ⭐</span>
                <div class="bar">
                  <div class="fill" :style="{ width: getRatingPercentage(6 - rating) + '%' }"></div>
                </div>
                <span>{{ getRatingCount(6 - rating) }}</span>
              </div>
            </div>

            <button @click="showReviewForm = !showReviewForm" class="btn btn-secondary">
              ✍️ Viết đánh giá
            </button>
          </div>
        </div>

        <!-- Review Filters -->
        <div class="review-filters">
          <button 
            v-for="filter in reviewFilters" 
            :key="filter.value"
            @click="selectedReviewFilter = filter.value"
            class="filter-btn"
            :class="{ active: selectedReviewFilter === filter.value }"
          >
            {{ filter.label }}
          </button>
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

            <!-- Image upload for review -->
            <div class="form-group">
              <label>Hình ảnh (tùy chọn):</label>
              <div class="image-upload">
                <input 
                  type="file" 
                  @change="handleReviewImageUpload" 
                  multiple 
                  accept="image/*"
                  class="file-input"
                />
                <div v-if="reviewImages.length" class="review-images-preview">
                  <div v-for="(image, index) in reviewImages" :key="index" class="image-preview">
                    <img :src="image" alt="Review image" />
                    <button @click="removeReviewImage(index)" class="remove-btn">×</button>
                  </div>
                </div>
              </div>
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
          <div v-for="review in filteredReviews" :key="review.id" class="review-item space-card">
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
                    <span v-if="review.verified" class="verified-badge">✅ Đã mua hàng</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="review-content">
              <h5 v-if="review.title">{{ review.title }}</h5>
              <p>{{ review.content }}</p>
              
              <!-- Review images -->
              <div v-if="review.images && review.images.length" class="review-images">
                <img 
                  v-for="(image, index) in review.images" 
                  :key="index"
                  :src="image" 
                  :alt="`Review image ${index + 1}`"
                  @click="openImageModal(image)"
                  class="review-image"
                />
              </div>
            </div>
            
            <div class="review-actions">
              <button @click="likeReview(review.id)" class="action-btn" :class="{ active: review.isLiked }">
                👍 {{ review.likeCount || 0 }}
              </button>
              <button @click="toggleReviewReply(review.id)" class="action-btn">
                💬 {{ review.replies?.length || 0 }} Trả lời
              </button>
              <button @click="reportReview(review.id)" class="action-btn">
                🚨 Báo cáo
              </button>
            </div>

            <!-- Review replies -->
            <div v-if="showReplies[review.id] && review.replies?.length" class="review-replies">
              <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                <div class="reply-author">
                  <img :src="reply.user?.avatar || '/default-avatar.png'" :alt="reply.user?.name" />
                  <span>{{ reply.user?.name }}</span>
                  <span class="reply-date">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <p>{{ reply.content }}</p>
              </div>
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

      <!-- Recently Viewed Products -->
      <div v-if="recentlyViewed.length" class="recently-viewed">
        <h2>👁️ Sản phẩm đã xem</h2>
        <div class="products-grid">
          <div v-for="product in recentlyViewed" :key="product.id" class="product-card space-card">
            <router-link :to="`/products/${product.id}`" class="product-link">
              <div class="product-image">
                <img :src="product.images?.[0] || '/placeholder-product.jpg'" :alt="product.name" />
              </div>
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <div class="product-price">{{ formatCurrency(product.price) }}</div>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Modal -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <div class="modal-content" @click.stop>
        <button @click="closeImageModal" class="close-btn">×</button>
        <img :src="modalImage" :alt="product?.name" />
        <div v-if="product.images?.length > 1" class="modal-navigation">
          <button @click="previousImage" class="nav-btn">‹</button>
          <button @click="nextImage" class="nav-btn">›</button>
        </div>
      </div>
    </div>

    <!-- Share Modal -->
    <div v-if="showShareModal" class="share-modal" @click="closeShareModal">
      <div class="modal-content" @click.stop>
        <h3>Chia sẻ sản phẩm</h3>
        <div class="share-options">
          <button @click="shareToFacebook" class="share-btn facebook">📘 Facebook</button>
          <button @click="shareToTwitter" class="share-btn twitter">🐦 Twitter</button>
          <button @click="copyProductLink" class="share-btn link">🔗 Sao chép link</button>
          <button @click="shareViaEmail" class="share-btn email">📧 Email</button>
        </div>
        <div class="share-link">
          <input :value="productUrl" readonly class="link-input" />
          <button @click="copyProductLink" class="copy-btn">📋</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
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
    const isInWishlist = ref(false)
    const isFollowingSeller = ref(false)
    const showReviewForm = ref(false)
    const showFullDescription = ref(false)
    const reviews = ref([])
    const relatedProducts = ref([])
    const recentlyViewed = ref([])
    const hasMoreReviews = ref(false)
    const selectedVariants = ref({})
    const expandedFaq = ref([])
    const showReplies = ref({})
    const selectedReviewFilter = ref('all')
    const reviewImages = ref([])
    
    // Modals
    const showImageModal = ref(false)
    const showShareModal = ref(false)
    const modalImage = ref('')
    const reviewsSection = ref(null)
    
    // Review form data
    const newReview = ref({
      rating: 5,
      title: '',
      content: ''
    })

    // Review filters
    const reviewFilters = [
      { label: 'Tất cả', value: 'all' },
      { label: '5 sao', value: '5' },
      { label: '4 sao', value: '4' },
      { label: '3 sao', value: '3' },
      { label: '2 sao', value: '2' },
      { label: '1 sao', value: '1' },
      { label: 'Có hình ảnh', value: 'with_images' },
      { label: 'Đã mua hàng', value: 'verified' }
    ]
    
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

    const totalPrice = computed(() => {
      let price = product.value?.price || 0
      // Add variant prices
      Object.keys(selectedVariants.value).forEach(variantName => {
        const variant = product.value?.variants?.find(v => v.name === variantName)
        const option = variant?.options?.find(o => o.value === selectedVariants.value[variantName])
        if (option && option.price !== price) {
          price = option.price
        }
      })
      return price * quantity.value
    })

    const productUrl = computed(() => {
      return window.location.href
    })

    const filteredReviews = computed(() => {
      let filtered = reviews.value
      
      if (selectedReviewFilter.value !== 'all') {
        if (selectedReviewFilter.value === 'with_images') {
          filtered = filtered.filter(review => review.images && review.images.length > 0)
        } else if (selectedReviewFilter.value === 'verified') {
          filtered = filtered.filter(review => review.verified)
        } else {
          filtered = filtered.filter(review => review.rating === parseInt(selectedReviewFilter.value))
        }
      }
      
      return filtered
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

    const getRatingPercentage = (rating) => {
      const total = reviews.value.length
      if (total === 0) return 0
      const count = reviews.value.filter(r => r.rating === rating).length
      return (count / total) * 100
    }

    const getRatingCount = (rating) => {
      return reviews.value.filter(r => r.rating === rating).length
    }

    const selectVariant = (variantName, option) => {
      selectedVariants.value[variantName] = option.value
    }

    const toggleFaq = (index) => {
      if (expandedFaq.value.includes(index)) {
        expandedFaq.value = expandedFaq.value.filter(i => i !== index)
      } else {
        expandedFaq.value.push(index)
      }
    }

    const toggleReviewReply = (reviewId) => {
      showReplies.value[reviewId] = !showReplies.value[reviewId]
    }

    const handleReviewImageUpload = (event) => {
      const files = Array.from(event.target.files)
      files.forEach(file => {
        if (file.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            reviewImages.value.push(e.target.result)
          }
          reader.readAsDataURL(file)
        }
      })
    }

    const removeReviewImage = (index) => {
      reviewImages.value.splice(index, 1)
    }

    const scrollToReviews = () => {
      nextTick(() => {
        reviewsSection.value?.scrollIntoView({ behavior: 'smooth' })
      })
    }

    const openImageModal = (image = null) => {
      modalImage.value = image || selectedImage.value || product.value.images?.[0]
      showImageModal.value = true
    }

    const closeImageModal = () => {
      showImageModal.value = false
    }

    const previousImage = () => {
      const currentIndex = product.value.images.indexOf(modalImage.value)
      const prevIndex = currentIndex > 0 ? currentIndex - 1 : product.value.images.length - 1
      modalImage.value = product.value.images[prevIndex]
    }

    const nextImage = () => {
      const currentIndex = product.value.images.indexOf(modalImage.value)
      const nextIndex = currentIndex < product.value.images.length - 1 ? currentIndex + 1 : 0
      modalImage.value = product.value.images[nextIndex]
    }

    const shareProduct = () => {
      showShareModal.value = true
    }

    const closeShareModal = () => {
      showShareModal.value = false
    }

    const shareToFacebook = () => {
      const url = encodeURIComponent(productUrl.value)
      window.open(`https://www.facebook.com/sharer/sharer.php?u=${url}`, '_blank')
    }

    const shareToTwitter = () => {
      const url = encodeURIComponent(productUrl.value)
      const text = encodeURIComponent(`Xem sản phẩm này: ${product.value.name}`)
      window.open(`https://twitter.com/intent/tweet?url=${url}&text=${text}`, '_blank')
    }

    const copyProductLink = async () => {
      try {
        await navigator.clipboard.writeText(productUrl.value)
        alert('Đã sao chép link sản phẩm!')
      } catch (err) {
        console.error('Failed to copy link:', err)
      }
    }

    const shareViaEmail = () => {
      const subject = encodeURIComponent(`Sản phẩm hay: ${product.value.name}`)
      const body = encodeURIComponent(`Xem sản phẩm này: ${productUrl.value}`)
      window.location.href = `mailto:?subject=${subject}&body=${body}`
    }

    const chatWithSeller = () => {
      // TODO: Implement chat functionality
      alert('Chức năng chat đang được phát triển!')
    }

    const viewSellerProfile = () => {
      router.push(`/sellers/${product.value.seller.id}`)
    }

    const followSeller = () => {
      isFollowingSeller.value = !isFollowingSeller.value
      // TODO: API call to follow/unfollow seller
    }

    const addToWishlist = () => {
      isInWishlist.value = !isInWishlist.value
      // TODO: API call to add/remove from wishlist
    }

    const reportReview = (reviewId) => {
      // TODO: Implement review reporting
      alert('Báo cáo đã được gửi!')
    }
    
    const loadProductDetail = async () => {
      try {
        loading.value = true
        const productId = route.params.id
        
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Mock product data with enhanced features
        product.value = {
          id: productId,
          name: 'Laptop Gaming Galactic Pro',
          description: 'Laptop gaming cao cấp với hiệu năng vượt trội, thiết kế futuristic và công nghệ tiên tiến từ tương lai.',
          fullDescription: 'Laptop Gaming Galactic Pro được trang bị những công nghệ tiên tiến nhất, mang đến trải nghiệm gaming đỉnh cao. Với thiết kế futuristic và hiệu năng vượt trội, đây là lựa chọn hoàn hảo cho game thủ chuyên nghiệp.',
          price: 25000000,
          originalPrice: 30000000,
          stock: 15,
          rating: 4.5,
          reviewCount: 128,
          isNew: true,
          isBestSeller: true,
          category: { name: 'Công nghệ' },
          images: [
            '/placeholder-product.jpg',
            '/placeholder-product-2.jpg',
            '/placeholder-product-3.jpg',
            '/placeholder-product-4.jpg'
          ],
          variants: [
            {
              name: 'Màu sắc',
              options: [
                { label: 'Đen', value: 'black', price: 25000000, available: true },
                { label: 'Bạc', value: 'silver', price: 25500000, available: true },
                { label: 'Xanh', value: 'blue', price: 26000000, available: false }
              ]
            },
            {
              name: 'RAM',
              options: [
                { label: '16GB', value: '16gb', price: 25000000, available: true },
                { label: '32GB', value: '32gb', price: 28000000, available: true },
                { label: '64GB', value: '64gb', price: 35000000, available: true }
              ]
            }
          ],
          specifications: [
            { name: 'CPU', value: 'Intel Core i9-13900H' },
            { name: 'GPU', value: 'NVIDIA RTX 4080 16GB' },
            { name: 'RAM', value: '32GB DDR5-4800' },
            { name: 'Storage', value: '1TB NVMe SSD' },
            { name: 'Display', value: '17.3" 4K 144Hz IPS' },
            { name: 'Weight', value: '2.8kg' },
            { name: 'Battery', value: '90Wh' },
            { name: 'OS', value: 'Windows 11 Pro' }
          ],
          features: [
            'CPU Intel Core i9 thế hệ 13',
            'GPU RTX 4080 16GB VRAM',
            'RAM 32GB DDR5',
            'SSD 1TB NVMe Gen4',
            'Màn hình 17.3" 4K 144Hz',
            'Hệ thống tản nhiệt quantum',
            'Bàn phím RGB với Anti-ghosting',
            'Audio by Harman Kardon'
          ],
          faq: [
            {
              question: 'Sản phẩm có bảo hành bao lâu?',
              answer: 'Sản phẩm được bảo hành chính hãng 24 tháng toàn cầu.'
            },
            {
              question: 'Có hỗ trợ upgrade RAM không?',
              answer: 'Có, máy hỗ trợ upgrade RAM lên tối đa 64GB.'
            },
            {
              question: 'Thời gian giao hàng?',
              answer: 'Giao hàng trong vòng 1-2 ngày làm việc tại TP.HCM và Hà Nội.'
            }
          ],
          seller: {
            id: 'seller123',
            name: 'Cosmic Tech Store',
            rating: 4.8,
            productCount: 456,
            followerCount: 12340,
            location: 'TP. Hồ Chí Minh',
            avatar: '/seller-avatar.jpg'
          }
        }
        
        // Initialize selected variants with defaults
        selectedVariants.value = {}
        product.value.variants?.forEach(variant => {
          const defaultOption = variant.options.find(o => o.available)
          if (defaultOption) {
            selectedVariants.value[variant.name] = defaultOption.value
          }
        })
        
        // Load reviews and related products
        await Promise.all([
          loadReviews(),
          loadRelatedProducts(),
          loadRecentlyViewed()
        ])
        
        // Add to recently viewed
        addToRecentlyViewed()
        
      } catch (error) {
        console.error('Error loading product:', error)
      } finally {
        loading.value = false
      }
    }
    
    const loadReviews = async () => {
      // Mock reviews data with enhanced features
      reviews.value = [
        {
          id: 1,
          rating: 5,
          title: 'Sản phẩm tuyệt vời!',
          content: 'Chất lượng vượt mong đợi, giao hàng nhanh, đóng gói cẩn thận. Máy chạy game rất mượt.',
          user: { name: 'Nguyễn Văn A', avatar: '/user-avatar-1.jpg' },
          createdAt: '2024-01-15',
          likeCount: 24,
          isLiked: false,
          verified: true,
          images: ['/review-image-1.jpg', '/review-image-2.jpg'],
          replies: [
            {
              id: 11,
              user: { name: 'Cosmic Tech Store', avatar: '/seller-avatar.jpg' },
              content: 'Cảm ơn anh đã tin tượng shop! Chúc anh gaming vui vẻ!',
              createdAt: '2024-01-16'
            }
          ]
        },
        {
          id: 2,
          rating: 4,
          title: 'Tốt nhưng có thể cải thiện',
          content: 'Sản phẩm ok, tuy nhiên có thể cải thiện thêm về bao bì và hướng dẫn sử dụng.',
          user: { name: 'Trần Thị B', avatar: '/user-avatar-2.jpg' },
          createdAt: '2024-01-10',
          likeCount: 12,
          isLiked: true,
          verified: true,
          images: [],
          replies: []
        },
        {
          id: 3,
          rating: 5,
          title: 'Máy gaming đỉnh cao!',
          content: 'Chạy mọi game ở setting cao, màn hình đẹp, âm thanh hay. Đáng đồng tiền bát gạo!',
          user: { name: 'Lê Văn C', avatar: '/user-avatar-3.jpg' },
          createdAt: '2024-01-08',
          likeCount: 8,
          isLiked: false,
          verified: false,
          images: ['/review-image-3.jpg'],
          replies: []
        }
      ]
      hasMoreReviews.value = true
    }
    
    const loadRelatedProducts = async () => {
      // Mock related products
      relatedProducts.value = [
        { 
          id: 2, 
          name: 'Gaming Mouse Nebula', 
          price: 1500000, 
          rating: 4.3, 
          reviewCount: 89,
          images: ['/placeholder-mouse.jpg']
        },
        { 
          id: 3, 
          name: 'Mechanical Keyboard Cosmos', 
          price: 2200000, 
          rating: 4.7, 
          reviewCount: 156,
          images: ['/placeholder-keyboard.jpg']
        },
        { 
          id: 4, 
          name: 'Gaming Headset Galaxy', 
          price: 1800000, 
          rating: 4.2, 
          reviewCount: 67,
          images: ['/placeholder-headset.jpg']
        },
        { 
          id: 5, 
          name: 'Gaming Monitor Aurora', 
          price: 8500000, 
          rating: 4.6, 
          reviewCount: 234,
          images: ['/placeholder-monitor.jpg']
        }
      ]
    }

    const loadRecentlyViewed = async () => {
      // Load from localStorage or API
      const recent = localStorage.getItem('recentlyViewed')
      if (recent) {
        recentlyViewed.value = JSON.parse(recent).slice(0, 4)
      }
    }

    const addToRecentlyViewed = () => {
      if (!product.value) return
      
      let recent = JSON.parse(localStorage.getItem('recentlyViewed') || '[]')
      recent = recent.filter(p => p.id !== product.value.id)
      recent.unshift({
        id: product.value.id,
        name: product.value.name,
        price: product.value.price,
        images: product.value.images
      })
      recent = recent.slice(0, 10) // Keep only last 10
      
      localStorage.setItem('recentlyViewed', JSON.stringify(recent))
      recentlyViewed.value = recent.slice(0, 4)
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
        
        const cartItem = {
          productId: product.value.id,
          quantity: quantity.value,
          variants: selectedVariants.value
        }
        
        await cartStore.addToCart(cartItem)
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
          isLiked: false,
          verified: true,
          images: [...reviewImages.value],
          replies: []
        }
        reviews.value.unshift(review)
        
        // Reset form
        newReview.value = { rating: 5, title: '', content: '' }
        reviewImages.value = []
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
      isInWishlist,
      isFollowingSeller,
      showReviewForm,
      showFullDescription,
      reviews,
      relatedProducts,
      recentlyViewed,
      hasMoreReviews,
      selectedVariants,
      expandedFaq,
      showReplies,
      selectedReviewFilter,
      reviewImages,
      showImageModal,
      showShareModal,
      modalImage,
      reviewsSection,
      newReview,
      reviewFilters,
      stockStatusClass,
      stockStatusText,
      discountPercentage,
      canAddToCart,
      totalPrice,
      productUrl,
      filteredReviews,
      formatCurrency,
      formatDate,
      getRatingPercentage,
      getRatingCount,
      selectVariant,
      toggleFaq,
      toggleReviewReply,
      handleReviewImageUpload,
      removeReviewImage,
      scrollToReviews,
      openImageModal,
      closeImageModal,
      previousImage,
      nextImage,
      shareProduct,
      closeShareModal,
      shareToFacebook,
      shareToTwitter,
      copyProductLink,
      shareViaEmail,
      chatWithSeller,
      viewSellerProfile,
      followSeller,
      addToWishlist,
      reportReview,
      increaseQuantity,
      decreaseQuantity,
      validateQuantity,
      addToCart,
      buyNow,
      favoriteProduct,
      submitReview,
      likeReview,
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
  cursor: pointer;
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

.product-badges {
  position: absolute;
  top: 1rem;
  left: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.badge-new {
  background: var(--aurora-gradient);
  color: white;
}

.badge-sale {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
}

.badge-bestseller {
  background: linear-gradient(135deg, #ffd93d, #ff6b6b);
  color: white;
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
  flex-wrap: wrap;
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

.view-reviews-btn {
  background: none;
  border: 1px solid var(--text-accent);
  color: var(--text-accent);
  padding: 0.25rem 0.75rem;
  border-radius: 16px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-reviews-btn:hover {
  background: var(--text-accent);
  color: white;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
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

.shipping-info {
  margin-top: 1rem;
}

.shipping-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.shipping-item .icon {
  font-size: 1rem;
}

.product-variants {
  margin-bottom: 2rem;
}

.product-variants h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.variant-group {
  margin-bottom: 1.5rem;
}

.variant-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.variant-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.variant-option {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.variant-option:hover:not(:disabled) {
  border-color: var(--text-accent);
  color: var(--text-accent);
}

.variant-option.active {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.variant-option:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.variant-price {
  font-size: 0.8rem;
  color: var(--text-accent);
}

.product-description h3,
.product-features h3,
.product-specs h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
  font-size: 1.2rem;
}

.description-content,
.full-description {
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 1rem;
}

.toggle-description {
  background: none;
  border: 1px solid var(--text-accent);
  color: var(--text-accent);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.toggle-description:hover {
  background: var(--text-accent);
  color: white;
}

.specs-table {
  display: grid;
  gap: 0.75rem;
}

.spec-row {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
  padding: 0.75rem;
  background: rgba(26, 26, 46, 0.3);
  border-radius: 8px;
  border-left: 3px solid var(--text-accent);
}

.spec-name {
  color: var(--text-secondary);
  font-weight: 500;
}

.spec-value {
  color: var(--text-primary);
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
  max-width: 200px;
  margin-bottom: 0.5rem;
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

.total-price {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 1.1rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.btn-large {
  flex: 1;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  font-weight: 600;
  min-height: 50px;
  min-width: 150px;
}

.payment-methods {
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding-top: 1rem;
}

.payment-methods h4 {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.payment-icons {
  display: flex;
  gap: 0.5rem;
}

.payment-icon {
  font-size: 1.5rem;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.payment-icon:hover {
  opacity: 1;
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
  position: relative;
  flex: none;
}

.seller-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(0, 212, 255, 0.3);
}

.seller-status {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
}

.seller-status.online {
  color: #10b981;
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
  flex-wrap: wrap;
}

.seller-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-small {
  padding: 0.4rem 1rem;
  font-size: 0.9rem;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--text-accent);
  color: var(--text-accent);
}

.btn-outline:hover {
  background: var(--text-accent);
  color: white;
}

.product-faq {
  margin-top: 2rem;
}

.product-faq h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.faq-item {
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  overflow: hidden;
}

.faq-question {
  width: 100%;
  background: rgba(26, 26, 46, 0.3);
  border: none;
  padding: 1rem;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  text-align: left;
}

.faq-question:hover {
  background: rgba(0, 212, 255, 0.1);
}

.faq-toggle {
  color: var(--text-accent);
  font-weight: bold;
  font-size: 1.2rem;
}

.faq-answer {
  padding: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  background: rgba(26, 26, 46, 0.1);
}

.reviews-section {
  margin-top: 4rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 2rem;
}

.reviews-header h2 {
  color: var(--text-accent);
}

.reviews-summary {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  flex-wrap: wrap;
}

.average-rating {
  display: flex;
  flex-direction: column;
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

.rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 200px;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.rating-bar span:first-child {
  width: 40px;
}

.rating-bar span:last-child {
  width: 30px;
  text-align: right;
}

.bar {
  flex: 1;
  height: 8px;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.fill {
  height: 100%;
  background: var(--text-accent);
  transition: width 0.3s ease;
}

.review-filters {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.filter-btn:hover,
.filter-btn.active {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
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

.image-upload {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
  transition: all 0.3s ease;
}

.image-upload:hover {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.05);
}

.file-input {
  width: 100%;
  padding: 0.5rem;
  background: transparent;
  color: var(--text-secondary);
  border: none;
  cursor: pointer;
}

.review-images-preview {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.image-preview {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ef4444;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  flex-wrap: wrap;
}

.review-meta .rating-stars .star {
  font-size: 1rem;
}

.review-date {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.verified-badge {
  background: var(--aurora-gradient);
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
}

.review-content h5 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.review-content p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1rem;
}

.review-images {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.review-image:hover {
  transform: scale(1.05);
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

.review-replies {
  margin-top: 1rem;
  padding-left: 2rem;
  border-left: 2px solid rgba(0, 212, 255, 0.2);
}

.reply-item {
  padding: 1rem;
  background: rgba(26, 26, 46, 0.3);
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.reply-author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.reply-author img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.reply-author span:first-of-type {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.9rem;
}

.reply-date {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.reply-item p {
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}

.load-more-container {
  text-align: center;
  margin-top: 2rem;
}

.related-products,
.recently-viewed {
  margin-top: 4rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.related-products h2,
.recently-viewed h2 {
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

/* Modals */
.image-modal,
.share-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
}

.modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  background: rgba(26, 26, 46, 0.95);
  border-radius: 16px;
  padding: 2rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.image-modal .modal-content {
  background: transparent;
  padding: 0;
  border: none;
}

.image-modal img {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 1.5rem;
  z-index: 1001;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(0, 212, 255, 0.8);
}

.modal-navigation {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 0 1rem;
  pointer-events: none;
}

.nav-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 2rem;
  pointer-events: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  background: rgba(0, 212, 255, 0.8);
}

.share-modal h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  text-align: center;
}

.share-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.share-btn {
  padding: 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: transparent;
  color: var(--text-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.share-btn:hover {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
}

.share-link {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.link-input {
  flex: 1;
  padding: 0.75rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary);
  border-radius: 8px;
  font-size: 0.9rem;
}

.copy-btn {
  padding: 0.75rem 1rem;
  border: 1px solid var(--text-accent);
  background: transparent;
  color: var(--text-accent);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.copy-btn:hover {
  background: var(--text-accent);
  color: white;
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

  .rating-breakdown {
    min-width: auto;
    width: 100%;
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

  .seller-actions {
    justify-content: flex-start;
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

  .seller-stats {
    flex-direction: column;
    gap: 0.25rem;
  }

  .share-options {
    grid-template-columns: 1fr;
  }

  .modal-content {
    padding: 1rem;
    margin: 1rem;
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

  .variant-options {
    justify-content: flex-start;
  }

  .spec-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }

  .rating-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>