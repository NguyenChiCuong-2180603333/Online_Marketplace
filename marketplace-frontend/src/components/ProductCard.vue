<template>
  <div
    class="product-card space-card"
    :class="{
      featured: product.featured,
      sale: product.onSale,
      new: product.isNew,
      'out-of-stock': product.stock === 0,
    }"
    @click="viewProduct"
  >
    <!-- Product Image -->
    <div class="product-image-container">
      <img
        :src="product.images?.[0] || '/placeholder-product.jpg'"
        :alt="product.name"
        class="product-image"
        :loading="lazy ? 'lazy' : 'eager'"
        @error="handleImageError"
      />

      <!-- Image Overlay -->
      <div class="image-overlay">
        <!-- Quick Actions -->
        <div class="quick-actions">
          <button
            @click.stop="toggleWishlist"
            class="action-btn wishlist-btn"
            :class="{ active: isInWishlist }"
            :title="isInWishlist ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'"
          >
            {{ isInWishlist ? '❤️' : '🤍' }}
          </button>

          <button @click.stop="quickView" class="action-btn quick-view-btn" title="Xem nhanh">
            👁️
          </button>

          <button
            @click.stop="compareProduct"
            class="action-btn compare-btn"
            :class="{ active: isInCompare }"
            title="So sánh sản phẩm"
          >
            ⚖️
          </button>
        </div>

        <!-- Add to Cart Button -->
        <button
          v-if="product.stock > 0"
          @click.stop="addToCart"
          class="add-to-cart-btn"
          :disabled="addingToCart"
        >
          {{ addingToCart ? '🔄 Đang thêm...' : '🛒 Thêm vào giỏ' }}
        </button>

        <div v-else class="out-of-stock-btn">❌ Hết hàng</div>
      </div>

      <!-- Product Badges -->
      <div class="product-badges">
        <span v-if="product.isNew" class="badge new-badge">🆕 Mới</span>
        <span v-if="product.featured" class="badge featured-badge">⭐ Nổi bật</span>
        <span v-if="product.onSale" class="badge sale-badge">🔥 Giảm giá</span>
        <span v-if="product.freeShipping" class="badge shipping-badge">🚚 Miễn phí vận chuyển</span>
        <span v-if="product.fastDelivery" class="badge fast-badge">⚡ Giao nhanh</span>
      </div>

      <!-- Discount Percentage -->
      <div v-if="discountPercentage > 0" class="discount-percentage">
        -{{ discountPercentage }}%
      </div>

      <!-- Stock Level Indicator -->
      <div v-if="product.stock <= 5 && product.stock > 0" class="stock-warning">
        ⚠️ Chỉ còn {{ product.stock }} sản phẩm
      </div>
    </div>

    <!-- Product Info -->
    <div class="product-info">
      <!-- Category -->
      <div class="product-category">
        <router-link :to="`/categories/${product.category?.id}`" @click.stop class="category-link">
          {{ product.category?.name }}
        </router-link>
      </div>

      <!-- Product Name -->
      <h3 class="product-name">
        {{ product.name }}
      </h3>

      <!-- Product Description (if compact mode is off) -->
      <p v-if="!compact && product.description" class="product-description">
        {{ truncateText(product.description, 80) }}
      </p>

      <!-- Rating & Reviews -->
      <div class="product-rating" v-if="product.averageRating || product.reviewCount">
        <div class="stars">
          <span
            v-for="star in 5"
            :key="star"
            :class="['star', star <= Number(product.averageRating || 0) ? 'filled' : 'empty']"
          >
            ★
          </span>
        </div>
        <span class="rating-value">{{ (product.averageRating || 0).toFixed(1) }}/5</span>
        <span class="rating-count">({{ product.reviewCount }})</span>
      </div>

      <!-- Price Section -->
      <div class="price-section">
        <div class="price-container">
          <span
            v-if="product.originalPrice && product.originalPrice > product.price"
            class="original-price"
          >
            {{ formatCurrency(product.originalPrice) }}
          </span>
          <span class="current-price">
            {{ formatCurrency(product.price) }}
          </span>
        </div>

        <!-- Installment Info -->
        <div v-if="installmentPrice > 0" class="installment-info">
          Hoặc {{ formatCurrency(installmentPrice) }}/tháng x 12 kỳ
        </div>
      </div>

      <!-- Seller Info (for marketplace) -->
      <div v-if="product.seller && showSeller" class="seller-info">
        <span class="seller-icon">🏪</span>
        <span class="seller-name">{{ product.seller.name }}</span>
        <span v-if="product.seller.verified" class="verified-badge" title="Người bán đã xác thực"
          >✅</span
        >
      </div>

      <!-- Shipping Info -->
      <div class="shipping-info">
        <div class="shipping-item">
          <span class="shipping-icon">🚚</span>
          <span class="shipping-text">
            {{
              product.freeShipping
                ? 'Miễn phí vận chuyển'
                : 'Phí vận chuyển: ' + formatCurrency(product.shippingFee || 25000)
            }}
          </span>
        </div>

        <div v-if="product.estimatedDelivery" class="shipping-item">
          <span class="shipping-icon">📅</span>
          <span class="shipping-text"> Giao hàng: {{ product.estimatedDelivery }} </span>
        </div>
      </div>

      <!-- Special Offers -->
      <div v-if="product.offers?.length" class="special-offers">
        <div v-for="offer in product.offers.slice(0, 2)" :key="offer.id" class="offer-item">
          <span class="offer-icon">🎁</span>
          <span class="offer-text">{{ offer.title }}</span>
        </div>
      </div>
    </div>

    <!-- Loading Overlay -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner">🔄</div>
    </div>
  </div>
</template>

<script>
import { ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'

export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true,
    },
    compact: {
      type: Boolean,
      default: false,
    },
    lazy: {
      type: Boolean,
      default: true,
    },
    showSeller: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['wishlist-toggle', 'quick-view', 'compare-toggle'],
  setup(props, { emit }) {
    const router = useRouter()
    const cartStore = useCartStore()

    // Reactive data
    const addingToCart = ref(false)
    const loading = ref(false)
    const isInWishlist = ref(false) // This would come from wishlist store
    const isInCompare = ref(false) // This would come from compare store

    // Computed properties
    const discountPercentage = computed(() => {
      if (props.product.originalPrice && props.product.originalPrice > props.product.price) {
        return Math.round(
          ((props.product.originalPrice - props.product.price) / props.product.originalPrice) * 100
        )
      }
      return 0
    })

    const installmentPrice = computed(() => {
      if (props.product.price >= 3000000) {
        // Show installment for products >= 3M VND
        return Math.round(props.product.price / 12)
      }
      return 0
    })

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    const viewProduct = () => {
      router.push(`/products/${props.product.id}`)
    }

    const addToCart = async () => {
      if (props.product.stock === 0) return

      try {
        addingToCart.value = true
        await cartStore.addToCart(props.product.id, 1)

        // Show success feedback
        showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success')
      } catch (error) {
        console.error('Add to cart error:', error)
        showToast('Có lỗi xảy ra. Vui lòng thử lại!', 'error')
      } finally {
        addingToCart.value = false
      }
    }

    const toggleWishlist = () => {
      isInWishlist.value = !isInWishlist.value
      emit('wishlist-toggle', {
        product: props.product,
        isInWishlist: isInWishlist.value,
      })

      const message = isInWishlist.value
        ? 'Đã thêm vào danh sách yêu thích!'
        : 'Đã bỏ khỏi danh sách yêu thích!'
      showToast(message, 'info')
    }

    const quickView = () => {
      emit('quick-view', props.product)
    }

    const compareProduct = () => {
      isInCompare.value = !isInCompare.value
      emit('compare-toggle', {
        product: props.product,
        isInCompare: isInCompare.value,
      })

      const message = isInCompare.value ? 'Đã thêm vào so sánh!' : 'Đã bỏ khỏi so sánh!'
      showToast(message, 'info')
    }

    // Simple toast notification (you can replace with a toast library)
    const showToast = (message, type = 'info') => {
      // This is a simple implementation - in real app, use a toast library
      const toast = document.createElement('div')
      toast.className = `toast toast-${type}`
      toast.textContent = message
      toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        z-index: 10000;
        font-size: 0.9rem;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideInRight 0.3s ease;
      `

      document.body.appendChild(toast)

      setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease forwards'
        setTimeout(() => document.body.removeChild(toast), 300)
      }, 3000)
    }

    return {
      addingToCart,
      loading,
      isInWishlist,
      isInCompare,
      discountPercentage,
      installmentPrice,
      formatCurrency,
      truncateText,
      handleImageError,
      viewProduct,
      addToCart,
      toggleWishlist,
      quickView,
      compareProduct,
    }
  },
}
</script>

<style scoped>
/* Product Card Base */
.product-card {
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  height: fit-content;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 212, 255, 0.2);
}

.product-card.featured {
  border: 2px solid rgba(255, 205, 60, 0.5);
}

.product-card.sale {
  border: 2px solid rgba(239, 68, 68, 0.5);
}

.product-card.new {
  border: 2px solid rgba(34, 197, 94, 0.5);
}

.product-card.out-of-stock {
  opacity: 0.7;
  cursor: not-allowed;
}

.product-card.out-of-stock:hover {
  transform: none;
}

/* Product Image Section */
.product-image-container {
  position: relative;
  width: 100%;
  height: 250px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
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

/* Image Overlay */
.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  opacity: 0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1rem;
}

.product-card:hover .image-overlay {
  opacity: 1;
}

.quick-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.action-btn {
  width: 2.5rem;
  height: 2.5rem;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.action-btn:hover {
  background: white;
  transform: scale(1.1);
}

.action-btn.active {
  background: var(--aurora-gradient);
  color: white;
}

.add-to-cart-btn {
  background: var(--aurora-gradient);
  border: none;
  border-radius: 25px;
  color: white;
  padding: 0.75rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  text-align: center;
}

.add-to-cart-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
}

.add-to-cart-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.out-of-stock-btn {
  background: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  text-align: center;
  font-weight: 600;
  font-size: 0.9rem;
}

/* Product Badges */
.product-badges {
  position: absolute;
  top: 1rem;
  left: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 10;
}

.badge {
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
  white-space: nowrap;
}

.new-badge {
  background: rgba(34, 197, 94, 0.9);
}

.featured-badge {
  background: rgba(255, 205, 60, 0.9);
}

.sale-badge {
  background: rgba(239, 68, 68, 0.9);
}

.shipping-badge {
  background: rgba(59, 130, 246, 0.9);
}

.fast-badge {
  background: var(--aurora-gradient);
}

/* Discount Percentage */
.discount-percentage {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 50%;
  font-weight: 700;
  font-size: 0.85rem;
  min-width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

/* Stock Warning */
.stock-warning {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  right: 1rem;
  background: rgba(255, 205, 60, 0.9);
  color: white;
  padding: 0.5rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  text-align: center;
  backdrop-filter: blur(10px);
}

/* Product Info */
.product-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.product-category {
  margin-bottom: 0.5rem;
}

.category-link {
  color: var(--text-accent);
  text-decoration: none;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: color 0.3s ease;
}

.category-link:hover {
  color: var(--text-primary);
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  color: var(--text-secondary);
  font-size: 0.85rem;
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Rating Section */
.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  color: #ccc !important;
  font-size: 1.1rem;
  transition: color 0.2s;
}

.star.filled {
  color: #ffd700 !important;
}

.rating-value {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.rating-count {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

/* Price Section */
.price-section {
  margin-top: auto;
}

.price-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 0.5rem;
}

.original-price {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-decoration: line-through;
  opacity: 0.7;
}

.current-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-accent);
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
}

.installment-info {
  color: var(--text-secondary);
  font-size: 0.75rem;
  font-style: italic;
}

/* Seller Info */
.seller-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  font-size: 0.8rem;
}

.seller-icon {
  font-size: 1rem;
}

.seller-name {
  color: var(--text-secondary);
  flex: 1;
}

.verified-badge {
  font-size: 0.9rem;
}

/* Shipping Info */
.shipping-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.shipping-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
}

.shipping-icon {
  font-size: 0.9rem;
  color: var(--text-accent);
}

.shipping-text {
  color: var(--text-secondary);
}

/* Special Offers */
.special-offers {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.offer-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-warning);
}

.offer-icon {
  font-size: 0.8rem;
}

/* Loading Overlay */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
  z-index: 100;
}

.loading-spinner {
  font-size: 2rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Compact Mode */
.product-card.compact .product-image-container {
  height: 200px;
}

.product-card.compact .product-info {
  padding: 1rem;
  gap: 0.5rem;
}

.product-card.compact .product-name {
  font-size: 1rem;
  -webkit-line-clamp: 1;
}

.product-card.compact .current-price {
  font-size: 1.1rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .product-card {
    max-width: 100%;
  }

  .product-image-container {
    height: 200px;
  }

  .product-info {
    padding: 1rem;
    gap: 0.5rem;
  }

  .product-name {
    font-size: 1rem;
  }

  .current-price {
    font-size: 1.1rem;
  }

  .quick-actions {
    justify-content: center;
  }

  .action-btn {
    width: 2rem;
    height: 2rem;
    font-size: 0.9rem;
  }

  .add-to-cart-btn {
    padding: 0.5rem 1rem;
    font-size: 0.8rem;
  }

  .product-badges {
    top: 0.5rem;
    left: 0.5rem;
  }

  .discount-percentage {
    top: 0.5rem;
    right: 0.5rem;
    min-width: 2.5rem;
    height: 2.5rem;
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .product-card {
    border-radius: 8px;
  }

  .product-image-container {
    height: 180px;
    border-radius: 8px 8px 0 0;
  }

  .product-info {
    padding: 0.75rem;
  }

  .product-name {
    font-size: 0.95rem;
  }

  .current-price {
    font-size: 1rem;
  }

  .badge {
    font-size: 0.7rem;
    padding: 0.2rem 0.5rem;
  }
}

/* Animation Effects */
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOutRight {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

/* Hover Effects */
.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(142, 68, 173, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: inherit;
}

.product-card:hover::before {
  opacity: 1;
}

/* Action Button Specific Styles */
.wishlist-btn.active {
  background: rgba(239, 68, 68, 0.9);
}

.compare-btn.active {
  background: rgba(255, 205, 60, 0.9);
}

.quick-view-btn:hover {
  background: rgba(59, 130, 246, 0.9);
  color: white;
}

/* Badge Animation */
.badge {
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.sale-badge {
  animation: salePulse 1s ease-in-out infinite alternate;
}

@keyframes salePulse {
  0% {
    background: rgba(239, 68, 68, 0.9);
    box-shadow: 0 0 0 rgba(239, 68, 68, 0.7);
  }
  100% {
    background: rgba(239, 68, 68, 1);
    box-shadow: 0 0 10px rgba(239, 68, 68, 0.7);
  }
}

/* Accessibility */
.product-card:focus,
.action-btn:focus,
.add-to-cart-btn:focus,
.category-link:focus {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}

/* High Contrast Mode */
@media (prefers-contrast: high) {
  .product-card {
    border: 2px solid var(--text-primary);
  }

  .action-btn {
    border: 1px solid var(--text-primary);
  }

  .badge {
    border: 1px solid white;
  }
}

/* Reduced Motion */
@media (prefers-reduced-motion: reduce) {
  .product-card,
  .product-image,
  .action-btn,
  .add-to-cart-btn {
    transition: none;
  }

  .product-card:hover {
    transform: none;
  }

  .product-card:hover .product-image {
    transform: none;
  }

  .loading-spinner {
    animation: none;
  }

  .badge {
    animation: none;
  }
}

/* Print Styles */
@media print {
  .product-card {
    break-inside: avoid;
    border: 1px solid black !important;
    background: white !important;
    color: black !important;
  }

  .image-overlay,
  .quick-actions,
  .add-to-cart-btn {
    display: none !important;
  }

  .product-badges {
    position: static !important;
  }

  .badge {
    background: none !important;
    color: black !important;
    border: 1px solid black !important;
  }
}
</style>
