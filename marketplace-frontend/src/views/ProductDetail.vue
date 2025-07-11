<template>
  <div class="product-detail">
    <div class="container">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <router-link to="/" class="breadcrumb-item">🏠 Trang chủ</router-link>
        <span class="breadcrumb-separator">→</span>
        <router-link to="/products" class="breadcrumb-item">📦 Sản phẩm</router-link>
        <span class="breadcrumb-separator">→</span>
        <router-link
          v-if="product?.category"
          :to="`/categories/${product.categoryId}`"
          class="breadcrumb-item"
        >
          {{ product.category }}
        </router-link>
        <span v-if="product?.category" class="breadcrumb-separator">→</span>
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
        <!-- Product Gallery & Info -->
        <div class="product-main">
          <div class="product-gallery">
            <!-- Main Image -->
            <div class="main-image">
              <img
                :src="selectedImage || product.images?.[0] || '/placeholder-product.jpg'"
                :alt="product.name"
                class="main-product-image"
              />
              <div class="image-controls">
                <button
                  @click="favoriteProduct"
                  class="control-btn"
                  :class="{ active: isFavorited }"
                >
                  {{ isFavorited ? '❤️' : '🤍' }}
                </button>
                <button @click="shareProduct" class="control-btn">🔗</button>
              </div>
            </div>

            <!-- Thumbnail Gallery -->
            <div class="thumbnail-gallery" v-if="product.images?.length > 1">
              <div
                v-for="(image, index) in product.images"
                :key="index"
                class="thumbnail"
                :class="{ active: selectedImage === image }"
                @click="selectedImage = image"
              >
                <img :src="image" :alt="`${product.name} ${index + 1}`" />
              </div>
            </div>
          </div>

          <div class="product-info">
            <!-- Product Header -->
            <div class="product-header">
              <div class="product-category">
                <span class="category-tag">{{ product.category }}</span>
                <span class="stock-status" :class="stockStatusClass">
                  {{ stockStatusText }}
                </span>
              </div>

              <h1 class="product-title">{{ product.name }}</h1>

              <!-- Rating Section -->
              <div class="rating-section">
                <div class="rating-stars">
                  <span
                    v-for="star in 5"
                    :key="star"
                    class="star"
                    :class="{ filled: star <= (product.averageRating || 0) }"
                  >
                    ⭐
                  </span>
                </div>
                <span class="rating-text">
                  {{ (product.averageRating || 0).toFixed(1) }}/5 ({{ product.reviewCount || 0 }}
                  đánh giá)
                </span>
                <div class="rating-actions" style="margin-top: 8px; display: flex; gap: 10px">
                  <button @click="openReviewListModal" class="btn btn-outline view-reviews-btn">
                    Xem đánh giá
                  </button>
                  <button @click="openReviewModal" class="btn btn-primary">✍️ Viết đánh giá</button>
                </div>
              </div>

              <!-- Price Section -->
              <div class="price-section">
                <span
                  v-if="product.originalPrice && product.originalPrice > product.price"
                  class="original-price"
                >
                  {{ formatPrice(product.originalPrice) }}
                </span>
                <span class="current-price">{{ formatPrice(product.price) }}</span>
                <span v-if="product.discount > 0" class="discount-badge">
                  Tiết kiệm {{ product.discount }}%
                </span>
              </div>
            </div>

            <!-- Product Description -->
            <div class="product-description">
              <h3>📝 Mô tả sản phẩm</h3>
              <div class="description-content">
                <p>{{ product.description || 'Chưa có mô tả chi tiết.' }}</p>
              </div>
            </div>

            <!-- Product Variants -->
            <div class="product-variants" v-if="product.variants?.length">
              <h4>🎨 Tùy chọn sản phẩm</h4>
              <div class="variant-options">
                <button
                  v-for="variant in product.variants"
                  :key="variant.id"
                  class="variant-btn"
                  :class="{ active: selectedVariant?.id === variant.id }"
                  @click="selectVariant(variant)"
                >
                  {{ variant.name }}
                </button>
              </div>
            </div>

            <!-- Quantity & Actions -->
            <div class="product-actions">
              <div class="quantity-selector">
                <label>Số lượng:</label>
                <div class="quantity-controls">
                  <button @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
                  <input
                    v-model.number="quantity"
                    type="number"
                    min="1"
                    :max="product.stockQuantity"
                  />
                  <button @click="increaseQuantity" :disabled="quantity >= product.stockQuantity">
                    +
                  </button>
                </div>
              </div>

              <div class="action-buttons">
                <button
                  @click="addToCart"
                  :disabled="product.stockQuantity <= 0 || addingToCart"
                  class="btn btn-primary btn-lg add-to-cart-btn"
                >
                  <span v-if="addingToCart">⏳ Đang thêm...</span>
                  <span v-else>🛒 Thêm vào giỏ hàng</span>
                </button>

                <button @click="buyNow" class="btn btn-accent btn-lg">⚡ Mua ngay</button>
              </div>
            </div>

            <!-- Seller Info -->
            <div class="seller-info">
              <h4>👨‍💼 Thông tin người bán</h4>
              <div class="seller-details">
                <div class="seller-avatar">
                  <img :src="seller?.avatar || '/placeholder-avatar.jpg'" :alt="seller?.name" />
                </div>
                <div class="seller-data">
                  <h5>{{ seller?.name || 'Người bán' }}</h5>
                </div>
                <div class="seller-actions">
                  <button @click="contactSeller" class="btn btn-secondary btn-sm">
                    💬 Liên hệ
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Product Specifications -->
        <div class="product-specifications" v-if="product.specifications?.length">
          <h3>📋 Thông số kỹ thuật</h3>
          <div class="specs-grid">
            <div v-for="spec in product.specifications" :key="spec.name" class="spec-row">
              <span class="spec-name">{{ spec.name }}</span>
              <span class="spec-value">{{ spec.value }}</span>
            </div>
          </div>
        </div>

        <!-- Similar Products Section -->
        <SimilarProducts
          :product-id="product.id"
          :product-price="product.price"
          :product-category="product.category"
          :limit="3"
          :display-limit="3"
          :show-similarity-scores="false"
          container-class="product-detail-similar"
          @product-click="handleSimilarProductClick"
          @add-to-cart="handleSimilarAddToCart"
          @add-to-wishlist="handleSimilarAddToWishlist"
          @compare-products="handleCompareProducts"
        />

        <!-- Cross-sell Recommendations -->
        <div class="cross-sell-section" v-if="crossSellProducts.length > 0">
          <div class="section-header">
            <h3 class="section-title">
              <span class="cross-sell-icon">🛍️</span>
              Thường được mua cùng
            </h3>
            <p class="section-subtitle">
              Các sản phẩm khách hàng thường mua kèm với {{ product.name }}
            </p>
          </div>

          <div class="cross-sell-grid">
            <div
              v-for="crossProduct in crossSellProducts"
              :key="crossProduct.id"
              class="cross-sell-card"
              @click="navigateToProduct(crossProduct.id)"
            >
              <img
                :src="crossProduct.images?.[0] || '/api/placeholder/product'"
                :alt="crossProduct.name"
                class="cross-sell-image"
              />
              <div class="cross-sell-info">
                <h4>{{ truncate(crossProduct.name, 30) }}</h4>
                <span class="cross-sell-price">{{ formatPrice(crossProduct.price) }}</span>
                <button @click.stop="addCrossSellToCart(crossProduct)" class="cross-sell-btn">
                  Thêm vào giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Not Found -->
      <div v-else-if="error" class="error-container">
        <div class="error-content">
          <span class="error-icon">😵</span>
          <h2>Không tìm thấy sản phẩm</h2>
          <p>{{ error }}</p>
          <router-link to="/products" class="btn btn-primary">
            🔙 Quay lại danh sách sản phẩm
          </router-link>
        </div>
      </div>
    </div>

    <!-- Modals -->

    <!-- Review Modal -->
    <div v-if="showReviewModal" class="modal-overlay" @click="closeReviewModal">
      <div class="modal-content review-modal" @click.stop>
        <div class="modal-header">
          <h3>✍️ Viết đánh giá cho {{ product?.name }}</h3>
          <button @click="closeReviewModal" class="modal-close">❌</button>
        </div>
        <div v-if="reviewError" class="review-error">{{ reviewError }}</div>
        <form @submit.prevent="submitReview" class="review-form">
          <div class="form-group">
            <label>Đánh giá của bạn:</label>
            <div class="rating-input">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                class="rating-star"
                :class="{ selected: star <= newReview.rating }"
                @click="newReview.rating = star"
              >
                ⭐
              </button>
            </div>
          </div>

          <div class="form-group">
            <label>Nhận xét:</label>
            <textarea
              v-model="newReview.comment"
              placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
              rows="4"
              required
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeReviewModal" class="btn btn-secondary">Hủy</button>
            <button type="submit" :disabled="submitingReview" class="btn btn-primary">
              {{ submitingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Share Modal -->
    <div v-if="showShareModal" class="modal-overlay" @click="closeShareModal">
      <div class="modal-content share-modal" @click.stop>
        <div class="modal-header">
          <h3>🔗 Chia sẻ sản phẩm</h3>
          <button @click="closeShareModal" class="modal-close">❌</button>
        </div>

        <div class="share-content">
          <div class="share-options">
            <button @click="shareToFacebook" class="share-btn facebook">📘 Facebook</button>
            <button @click="shareToTwitter" class="share-btn twitter">🐦 Twitter</button>
            <button @click="shareToWhatsApp" class="share-btn whatsapp">💬 WhatsApp</button>
            <button @click="shareToEmail" class="share-btn email">📧 Email</button>
          </div>

          <div class="share-link">
            <input :value="productUrl" readonly class="link-input" />
            <button @click="copyProductLink" class="copy-btn">
              {{ linkCopied ? 'Đã sao chép!' : 'Sao chép' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Button for Contacting Seller -->
    <!-- <ChatButton
      v-if="product && authStore.isAuthenticated"
      :seller-id="product.sellerId"
      :product-id="product.id"
      :product-name="product.name"
      @chat-opened="handleChatOpened"
    /> -->

    <!-- Modal danh sách đánh giá -->
    <div
      v-if="showReviewListModal"
      class="modal-overlay review-list-modal"
      @click.self="showReviewListModal = false"
    >
      <div class="modal-content review-list-content">
        <div class="modal-header">
          <h3>📝 Tất cả đánh giá sản phẩm</h3>
          <button @click="showReviewListModal = false" class="modal-close">❌</button>
        </div>
        <div class="review-stats-bar">
          <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="review-stats-row">
            <span class="review-star-label">{{ star }}★</span>
            <span class="review-star-count">({{ getRatingCount(star) }})</span>
          </div>
        </div>
        <div class="reviews-list-modal">
          <div v-if="reviews.length > 0">
            <div v-for="review in reviews" :key="review.id" class="review-item-modal">
              <img
                :src="review.userAvatar || '/placeholder-avatar.jpg'"
                :alt="review.userName || 'Người dùng ẩn danh'"
                class="review-avatar"
              />
              <div class="review-main">
                <div class="review-header">
                  <span class="reviewer-name">{{ review.userName || 'Người dùng ẩn danh' }}</span>
                  <span class="review-stars">
                    <span
                      v-for="star in 5"
                      :key="star"
                      class="star-sm"
                      :class="{ filled: star <= review.rating }"
                      >⭐</span
                    >
                  </span>
                  <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                </div>
                <div class="review-comment-shopee">{{ review.comment }}</div>
                <div v-if="review.images?.length" class="review-images-shopee">
                  <img
                    v-for="(image, index) in review.images"
                    :key="index"
                    :src="image"
                    :alt="`Review image ${index + 1}`"
                    @click="openImageModal(image)"
                    class="review-image-thumb-shopee"
                  />
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-reviews-modal">
            <span class="no-reviews-icon">📝</span>
            <h4>Chưa có đánh giá nào</h4>
            <p>Hãy là người đầu tiên đánh giá sản phẩm này!</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { productAPI, reviewAPI, chatAPI } from '@/services/api'
import SimilarProducts from '@/components/SimilarProducts.vue'
import ChatButton from '@/components/ChatButton.vue'
import recommendationService from '@/services/recommendationService'

export default {
  name: 'ProductDetail',
  components: {
    SimilarProducts,
    ChatButton,
  },

  setup() {
    const route = useRoute()
    const router = useRouter()
    const authStore = useAuthStore()
    const userStore = useUserStore()
    const cartStore = useCartStore()

    // Reactive state
    const product = ref(null)
    const seller = ref(null)
    const loading = ref(false)
    const error = ref(null)
    const selectedImage = ref('')
    const selectedVariant = ref(null)
    const quantity = ref(1)
    const addingToCart = ref(false)
    const isFavorited = ref(false)

    // Cross-sell products
    const crossSellProducts = ref([])
    const loadingCrossSell = ref(false)

    // Reviews
    const reviews = ref([])
    const displayedReviews = ref([])
    const reviewsPerPage = 5
    const loadingReviews = ref(false)

    // Modals
    const showImageModal = ref(false)
    const showReviewModal = ref(false)
    const showShareModal = ref(false)
    const linkCopied = ref(false)
    const showReviewListModal = ref(false)

    // Review form
    const newReview = ref({
      rating: 5,
      comment: '',
    })
    const submitingReview = ref(false)

    // Refs
    const reviewsRef = ref(null)

    // Computed properties
    const stockStatusClass = computed(() => {
      if (!product.value) return ''
      if (product.value.stockQuantity > 10) return 'in-stock'
      if (product.value.stockQuantity > 0) return 'low-stock'
      return 'out-of-stock'
    })

    const stockStatusText = computed(() => {
      if (!product.value) return ''
      if (product.value.stockQuantity > 10) return `Còn hàng (${product.value.stockQuantity})`
      if (product.value.stockQuantity > 0) return `Sắp hết hàng (${product.value.stockQuantity})`
      return 'Hết hàng'
    })

    const productUrl = computed(() => {
      return window.location.href
    })

    // Load product data
    const loadProduct = async (productId) => {
      loading.value = true
      error.value = null

      try {
        const response = await productAPI.getById(productId)
        product.value = response.data.product
        seller.value = response.data.seller

        // Set initial state
        selectedImage.value = product.value.images?.[0] || '/placeholder-product.jpg'
        selectedVariant.value = product.value.variants?.[0] || null
        isFavorited.value = userStore.wishlist?.some((item) => item.id === productId) || false

        // Track product view
        await recommendationService.trackView(productId, 'product_detail')

        // Load related data
        await Promise.all([loadReviews(productId), loadCrossSellProducts(productId)])
      } catch (err) {
        console.error('Error loading product:', err)
        error.value = err.response?.data?.message || 'Không thể tải thông tin sản phẩm'
      } finally {
        loading.value = false
      }
    }

    // Load cross-sell products
    const loadCrossSellProducts = async (productId) => {
      loadingCrossSell.value = true

      try {
        const response = await recommendationService.getCrossSellRecommendations(productId, 6)
        crossSellProducts.value = response.crossSellProducts || []
      } catch (error) {
        console.error('Error loading cross-sell products:', error)
        crossSellProducts.value = []
      } finally {
        loadingCrossSell.value = false
      }
    }

    // Load reviews
    const loadReviews = async (productId) => {
      loadingReviews.value = true

      try {
        const response = await reviewAPI.getByProduct(productId)
        reviews.value = response.data || []
        displayedReviews.value = reviews.value.slice(0, reviewsPerPage)
      } catch (error) {
        console.error('Error loading reviews:', error)
        reviews.value = []
        displayedReviews.value = []
      } finally {
        loadingReviews.value = false
      }
    }

    // Product actions
    const selectVariant = (variant) => {
      selectedVariant.value = variant

      // Track variant selection
      recommendationService.trackInteraction(product.value.id, 'VARIANT_SELECTED', {
        variantId: variant.id,
        variantName: variant.name,
      })
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
      if (addingToCart.value) return
      addingToCart.value = true
      try {
        // Track add to cart action
        await recommendationService.trackAddToCart(product.value.id, quantity.value)
        // Thêm thực sự vào giỏ hàng
        await cartStore.addItem(product.value.id, quantity.value)
        // Show success notification
        alert(`Đã thêm ${quantity.value} ${product.value.name} vào giỏ hàng!`)
      } catch (error) {
        console.error('Error adding to cart:', error)
        alert('Có lỗi khi thêm vào giỏ hàng')
      } finally {
        addingToCart.value = false
      }
    }

    const buyNow = async () => {
      await addToCart()
      if (!addingToCart.value) {
        router.push('/checkout')
      }
    }

    const favoriteProduct = async () => {
      try {
        isFavorited.value = !isFavorited.value

        const action = isFavorited.value ? 'ADD_TO_WISHLIST' : 'REMOVE_FROM_WISHLIST'
        await recommendationService.trackInteraction(product.value.id, action)

        // TODO: Add actual wishlist logic here
        console.log(isFavorited.value ? 'Added to wishlist' : 'Removed from wishlist')
      } catch (error) {
        console.error('Error toggling wishlist:', error)
        isFavorited.value = !isFavorited.value // Revert on error
      }
    }

    // Similar products handlers
    const handleSimilarProductClick = (data) => {
      const { product: similarProduct, index } = data

      // Track similar product click
      recommendationService.trackInteraction(similarProduct.id, 'SIMILAR_PRODUCT_CLICK', {
        originalProductId: product.value.id,
        position: index,
        source: 'product_detail',
      })
    }

    const handleSimilarAddToCart = (similarProduct) => {
      // Track cross-category purchase intent
      recommendationService.trackInteraction(similarProduct.id, 'CROSS_PRODUCT_ADD_TO_CART', {
        originalProductId: product.value.id,
        source: 'similar_products',
      })
    }

    const handleSimilarAddToWishlist = (similarProduct) => {
      // Track wishlist action from similar products
      recommendationService.trackInteraction(similarProduct.id, 'CROSS_PRODUCT_WISHLIST', {
        originalProductId: product.value.id,
        source: 'similar_products',
      })
    }

    const handleCompareProducts = (products) => {
      // Track product comparison
      recommendationService.trackInteraction(product.value.id, 'PRODUCT_COMPARISON', {
        comparedProducts: products.map((p) => p.id),
        count: products.length,
      })
    }

    // Cross-sell actions
    const addCrossSellToCart = async (crossProduct) => {
      try {
        await recommendationService.trackAddToCart(crossProduct.id, 1)
        // Thêm thực sự vào giỏ hàng
        await cartStore.addItem(crossProduct.id, 1)
        // Track cross-sell conversion
        await recommendationService.trackInteraction(crossProduct.id, 'CROSS_SELL_CONVERSION', {
          originalProductId: product.value.id,
          source: 'cross_sell_section',
        })
        console.log('Added cross-sell to cart:', crossProduct.name)
        alert(`Đã thêm ${crossProduct.name} vào giỏ hàng!`)
      } catch (error) {
        console.error('Error adding cross-sell to cart:', error)
        alert('Có lỗi khi thêm vào giỏ hàng')
      }
    }

    const navigateToProduct = (productId) => {
      router.push(`/products/${productId}`)
    }

    // Modal handlers
    const openImageModal = (image = null) => {
      showImageModal.value = true
    }

    const closeImageModal = () => {
      showImageModal.value = false
    }

    const openReviewModal = () => {
      if (!authStore.isAuthenticated) {
        router.push('/login')
        return
      }
      showReviewModal.value = true
      showReviewListModal.value = false
      reviewError.value = ''
    }

    const closeReviewModal = () => {
      showReviewModal.value = false
      newReview.value = { rating: 5, comment: '' }
    }

    const submitReview = async () => {
      if (!newReview.value.comment.trim()) return

      submitingReview.value = true

      try {
        const reviewData = {
          productId: product.value.id,
          rating: newReview.value.rating,
          comment: newReview.value.comment.trim(),
        }

        await reviewAPI.create(reviewData)

        await recommendationService.trackInteraction(product.value.id, 'PRODUCT_REVIEW', {
          rating: newReview.value.rating,
        })

        await loadReviews(product.value.id)
        await loadProduct(product.value.id)

        closeReviewModal()
        alert('Cảm ơn bạn đã đánh giá sản phẩm!')
      } catch (error) {
        console.error('Error submitting review:', error)
        alert('Có lỗi khi gửi đánh giá')
      } finally {
        submitingReview.value = false
      }
    }

    const loadMoreReviews = () => {
      const nextBatch = reviews.value.slice(
        displayedReviews.value.length,
        displayedReviews.value.length + reviewsPerPage
      )
      displayedReviews.value.push(...nextBatch)
    }

    // Share functionality
    const shareProduct = () => {
      showShareModal.value = true
    }

    const closeShareModal = () => {
      showShareModal.value = false
      linkCopied.value = false
    }

    const shareToFacebook = () => {
      const url = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(
        productUrl.value
      )}`
      window.open(url, '_blank')
    }

    const shareToTwitter = () => {
      const text = `Check out this amazing product: ${product.value.name}`
      const url = `https://twitter.com/intent/tweet?text=${encodeURIComponent(
        text
      )}&url=${encodeURIComponent(productUrl.value)}`
      window.open(url, '_blank')
    }

    const shareToWhatsApp = () => {
      const text = `Xem sản phẩm này: ${product.value.name} - ${productUrl.value}`
      const url = `https://wa.me/?text=${encodeURIComponent(text)}`
      window.open(url, '_blank')
    }

    const shareToEmail = () => {
      const subject = `Sản phẩm hay: ${product.value.name}`
      const body = `Tôi muốn chia sẻ với bạn sản phẩm này:\n\n${product.value.name}\n${productUrl.value}`
      const url = `mailto:?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`
      window.open(url)
    }

    const copyProductLink = async () => {
      try {
        await navigator.clipboard.writeText(productUrl.value)
        linkCopied.value = true
        setTimeout(() => (linkCopied.value = false), 2000)
      } catch (error) {
        console.error('Error copying link:', error)
      }
    }

    // Seller actions
    const contactSeller = async () => {
      try {
        const res = await chatAPI.createConversation(product.value.sellerId, product.value.id)
        const conversation = res.data
        router.push({
          path: '/chat',
          query: { conversationId: conversation.id },
        })
      } catch (e) {
        alert('Không thể tạo cuộc trò chuyện!')
        console.error(e)
      }
    }

    const viewSellerStore = () => {
      router.push(`/sellers/${product.value.sellerId}`)
    }

    const handleChatOpened = (data) => {
      recommendationService.trackInteraction(product.value.id, 'SELLER_CONTACT', {
        sellerId: data.sellerId,
        method: 'chat',
      })
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(price)
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const truncate = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.slice(0, length) + '...' : text
    }

    const scrollToReviews = () => {
      if (reviewsRef.value) {
        reviewsRef.value.scrollIntoView({ behavior: 'smooth' })
      }
    }

    const getRatingPercentage = (rating) => {
      if (!reviews.value.length) return 0
      const count = reviews.value.filter((r) => r.rating === rating).length
      return (count / reviews.value.length) * 100
    }

    const getRatingCount = (rating) => {
      return reviews.value.filter((r) => r.rating === rating).length
    }

    watch(
      () => route.params.id,
      (newId) => {
        if (newId) {
          loadProduct(newId)
        }
      }
    )

    onMounted(() => {
      if (route.params.id) {
        loadProduct(route.params.id)
      }
    })

    const openReviewListModal = () => {
      showReviewListModal.value = true
      showReviewModal.value = false
    }

    return {
      // Stores
      authStore,
      userStore,
      cartStore,

      // State
      product,
      seller,
      loading,
      error,
      selectedImage,
      selectedVariant,
      quantity,
      addingToCart,
      isFavorited,
      crossSellProducts,
      loadingCrossSell,
      reviews,
      displayedReviews,
      loadingReviews,

      // Modals
      showImageModal,
      showReviewModal,
      showShareModal,
      linkCopied,
      showReviewListModal,

      // Forms
      newReview,
      submitingReview,

      // Refs
      reviewsRef,

      // Computed
      stockStatusClass,
      stockStatusText,
      productUrl,

      // Methods
      selectVariant,
      increaseQuantity,
      decreaseQuantity,
      addToCart,
      buyNow,
      favoriteProduct,
      handleSimilarProductClick,
      handleSimilarAddToCart,
      handleSimilarAddToWishlist,
      handleCompareProducts,
      addCrossSellToCart,
      navigateToProduct,
      openImageModal,
      closeImageModal,
      openReviewModal,
      closeReviewModal,
      submitReview,
      loadMoreReviews,
      shareProduct,
      closeShareModal,
      shareToFacebook,
      shareToTwitter,
      shareToWhatsApp,
      shareToEmail,
      copyProductLink,
      contactSeller,
      viewSellerStore,
      handleChatOpened,

      // Utilities
      formatPrice,
      formatDate,
      truncate,
      scrollToReviews,
      getRatingPercentage,
      getRatingCount,
      openReviewListModal,
    }
  },
}
</script>

<style scoped>


.cross-sell-section {
  margin: 3rem 0;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.cross-sell-section .section-header {
  text-align: center;
  margin-bottom: 2rem;
}

.cross-sell-section .section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.cross-sell-section .section-subtitle {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.cross-sell-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.cross-sell-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cross-sell-card:hover {
  transform: translateY(-3px);
  border-color: var(--text-accent);
  box-shadow: 0 8px 20px rgba(0, 212, 255, 0.2);
}

.cross-sell-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.cross-sell-info {
  padding: 1rem;
  text-align: center;
}

.cross-sell-info h4 {
  font-size: 0.9rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.cross-sell-price {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-accent);
  display: block;
  margin-bottom: 0.75rem;
}

.cross-sell-btn {
  background: var(--accent-gradient);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
}

.cross-sell-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
}

/* Responsive adjustments for cross-sell section */
@media (max-width: 768px) {
  .cross-sell-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;
  }

  .cross-sell-section {
    margin: 2rem 0;
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .cross-sell-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .cross-sell-info {
    padding: 0.75rem;
  }

  .cross-sell-info h4 {
    font-size: 0.8rem;
  }
}

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
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
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
  background: linear-gradient(135deg, #f8fafc 60%, #e0f7fa 100%); /* nền sáng nhẹ */
  cursor: pointer;
}

.main-product-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
  transition: transform 0.3s ease;
  border-radius: 12px;
  box-shadow: 0 2px 8px #00d4ff22;
  background: transparent;
  display: block;
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

/* Modal share (chia sẻ sản phẩm) căn giữa */
.share-modal {
  position: fixed !important;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10000;
  background: #23243a;
  border-radius: 16px;
  padding: 1.5rem 1.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 260px;
  max-width: 420px;
  max-height: 90vh;
}

.share-modal .modal-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.share-modal .modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: #fff;
  cursor: pointer;
}

.share-modal .share-link {
  margin-top: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.share-modal .share-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 1rem;
}

.share-modal input[type='text'] {
  background: #18192b;
  color: #fff;
  border: 1px solid #444;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  width: 220px;
}

.share-modal .copy-btn {
  background: var(--accent-gradient);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-weight: 600;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content.review-modal {
  background: #181c2a;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  padding: 32px 24px 24px 24px;
  min-width: 340px;
  max-width: 95vw;
  width: 400px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  position: relative;
  gap: 18px;
}

.review-modal .modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.review-modal .modal-header h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #ffd700;
  margin: 0;
  letter-spacing: 0.5px;
}

.review-modal .modal-close {
  background: none;
  border: none;
  font-size: 1.3rem;
  color: #fff;
  cursor: pointer;
  transition: color 0.2s;
}
.review-modal .modal-close:hover {
  color: #ff4d4f;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.review-form label {
  font-weight: 500;
  color: #fff;
  margin-bottom: 2px;
}

.rating-input {
  display: flex;
  gap: 4px;
}
.rating-star {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #ffd700;
  cursor: pointer;
  transition: transform 0.15s;
}
.rating-star.selected,
.rating-star:hover {
  transform: scale(1.2);
  color: #ffb300;
}

.review-form textarea {
  border-radius: 8px;
  border: 1px solid #2d334d;
  background: #23263a;
  color: #fff;
  padding: 10px;
  font-size: 1rem;
  resize: vertical;
  min-height: 70px;
  transition: border 0.2s;
}
.review-form textarea:focus {
  border: 1.5px solid #ffd700;
  outline: none;
}

.review-form input[type='file'] {
  background: #23263a;
  color: #fff;
  border-radius: 8px;
  padding: 6px 0;
  font-size: 0.95rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.btn {
  border: none;
  border-radius: 24px;
  padding: 10px 24px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}
.btn.btn-primary {
  background: linear-gradient(90deg, #ff6a00 0%, #ffd700 100%);
  color: #181c2a;
  box-shadow: 0 2px 8px rgba(255, 186, 0, 0.15);
}
.btn.btn-primary:hover {
  background: linear-gradient(90deg, #ffd700 0%, #ff6a00 100%);
  color: #181c2a;
}
.btn.btn-secondary {
  background: #23263a;
  color: #ffd700;
  border: 1.5px solid #ffd700;
}
.btn.btn-secondary:hover {
  background: #ffd700;
  color: #23263a;
}

@media (max-width: 600px) {
  .modal-content.review-modal {
    width: 95vw;
    min-width: unset;
    padding: 16px 8px;
  }
  .btn {
    padding: 10px 10px;
    font-size: 0.95rem;
  }
}

.review-error {
  color: #ff4d4f;
  background: #fff0f0;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 10px;
  font-weight: 500;
  text-align: center;
  font-size: 1rem;
}

.review-list-modal {
  z-index: 1100;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
}
.review-list-content {
  background: #181c2a;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  padding: 32px 32px 24px 32px;
  min-width: 340px;
  max-width: 500px;
  width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  position: relative;
}
.review-list-content .modal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}
.review-list-content .modal-header h3 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #ffd700;
  margin: 0 auto;
  text-align: center;
  flex: 1;
}
.review-list-content .modal-close {
  position: absolute;
  top: 18px;
  right: 24px;
  background: none;
  border: none;
  font-size: 1.3rem;
  color: #fff;
  cursor: pointer;
  transition: color 0.2s;
}
.review-list-content .modal-close:hover {
  color: #ff4d4f;
}
.review-list-content .btn.btn-primary {
  display: block;
  margin: 0 auto 18px auto;
  min-width: 180px;
  font-size: 1.05rem;
}
.reviews-list-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 8px;
}
.review-item-modal {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  background: #23263a;
  border: 1.5px solid #23263a;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 18px;
  gap: 0;
}
.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
  border: 1.5px solid #2d334d;
}
.review-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.reviewer-name {
  font-weight: 600;
  font-size: 1.08rem;
  color: #ffd700;
  margin-right: 8px;
}
.review-stars {
  display: flex;
  align-items: center;
  margin-right: 8px;
}
.review-stars .star-sm {
  font-size: 1.1rem;
  color: #ffd700;
  margin-right: 1px;
}
.review-date {
  font-size: 0.93rem;
  color: #888;
  margin-left: 8px;
}
.review-comment-shopee {
  font-size: 1.05rem;
  color: #f3f3f3;
  line-height: 1.5;
  margin-top: 6px;
  margin-bottom: 4px;
  word-break: break-word;
}
.review-images-shopee {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
}
.review-image-thumb-shopee {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1.5px solid #2d334d;
  margin: 4px 4px 0 0;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}
.review-image-thumb-shopee:hover {
  transform: scale(1.12);
  box-shadow: 0 2px 8px #ffd70044;
}
@media (max-width: 600px) {
  .review-item-modal {
    padding: 10px 4px;
    margin-bottom: 12px;
  }
  .review-avatar {
    width: 32px;
    height: 32px;
    margin-right: 7px;
  }
  .review-header {
    gap: 4px;
  }
  .review-comment-shopee {
    font-size: 0.97rem;
    margin-top: 4px;
  }
  .review-image-thumb-shopee {
    width: 44px;
    height: 44px;
  }
}
.no-reviews-modal {
  text-align: center;
  color: #fff;
  margin-top: 32px;
}
@media (max-width: 600px) {
  .review-list-content {
    width: 100vw;
    min-width: unset;
    max-width: 100vw;
    max-height: 100vh;
    border-radius: 0;
    padding: 12px 2px 12px 2px;
  }
  .review-list-content .btn.btn-primary {
    min-width: 120px;
    font-size: 0.98rem;
  }
  .review-item-modal {
    padding: 10px 6px;
    gap: 8px;
  }
  .review-item-modal .reviewer-info img {
    width: 36px;
    height: 36px;
    margin-right: 6px;
  }
}

.review-comment-box {
  background: #23263a;
  border-radius: 10px;
  padding: 12px 16px;
  margin-top: 4px;
  font-size: 1.08rem;
  line-height: 1.5;
  color: #f3f3f3;
  position: relative;
  word-break: break-word;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.07);
}
.review-quote {
  font-size: 1.5rem;
  color: #ffd700;
  margin-right: 6px;
  vertical-align: top;
}
.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.review-image-thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  border: 1.5px solid #2d334d;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.review-image-thumb:hover {
  box-shadow: 0 2px 8px #ffd70044;
}
@media (max-width: 600px) {
  .review-comment-box {
    font-size: 0.98rem;
    padding: 8px 6px;
  }
  .review-image-thumb {
    width: 48px;
    height: 48px;
  }
}

.review-stats-bar {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 18px;
  margin-bottom: 18px;
}
.review-stats-row {
  display: flex;
  align-items: center;
  font-size: 1.08rem;
  color: #ffd700;
  background: #23263a;
  border-radius: 8px;
  padding: 4px 10px;
  min-width: 60px;
  justify-content: center;
}
.review-star-label {
  font-weight: 600;
  margin-right: 4px;
}
.review-star-count {
  color: #fff;
  font-weight: 500;
}
</style>
