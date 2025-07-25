<template>
  <div class="cart-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🛒 Giỏ Hành Tinh</h1>
        <p class="page-subtitle">Quản lý các sản phẩm trong hành trình khám phá vũ trụ của bạn</p>
      </div>

      <!-- Cart Content -->
      <div v-if="cartItems.length > 0" class="cart-content">
        <div class="cart-main">
          <!-- Cart Items -->
          <div class="cart-items">
            <div class="cart-header">
              <h2>Sản phẩm trong giỏ ({{ totalItems }})</h2>
              <button @click="clearCart" class="clear-cart-btn">🗑️ Xóa tất cả</button>
            </div>

            <div class="items-list">
              <div
                v-for="item in cartItems"
                :key="item.productId"
                class="cart-item space-card"
                :class="{ removing: item.isRemoving }"
              >
                <div class="item-image">
                  <img
                    :src="item.productImage || '/placeholder-product.jpg'"
                    :alt="item.productName"
                  />
                </div>

                <div class="item-details">
                  <div class="item-info">
                    <h3 class="item-name">
                      <router-link :to="`/products/${item.productId}`">
                        {{ item.productName }}
                      </router-link>
                    </h3>
                  </div>

                  <div class="item-actions">
                    <div class="quantity-controls">
                      <label>Số lượng:</label>
                      <div class="quantity-selector">
                        <button
                          @click="updateQuantity(item.productId, item.quantity - 1)"
                          :disabled="item.quantity <= 1 || loading"
                          class="quantity-btn"
                        >
                          -
                        </button>
                        <input
                          v-model.number="item.quantity"
                          @change="updateQuantity(item.productId, item.quantity)"
                          type="number"
                          min="1"
                          class="quantity-input"
                          :disabled="loading"
                        />
                        <button
                          @click="updateQuantity(item.productId, item.quantity + 1)"
                          :disabled="loading"
                          class="quantity-btn"
                        >
                          +
                        </button>
                      </div>
                    </div>

                    <div class="item-price-section">
                      <div class="price-details">
                        <span class="unit-price">{{ formatCurrency(item.productPrice) }}</span>
                      </div>
                      <div class="total-price">
                        {{ formatCurrency(item.subtotal) }}
                      </div>
                    </div>

                    <div class="item-controls">
                      <button @click="saveForLater(item.productId)" class="action-btn">
                        💾 Lưu lại sau
                      </button>
                      <button
                        @click="removeItem(item.productId)"
                        class="action-btn remove-btn"
                        :disabled="loading"
                      >
                        {{ loading ? '🔄' : '🗑️' }} Xóa
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Loading overlay -->
                <div v-if="item.updating" class="item-loading">
                  <div class="loading-spinner"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Cart Summary -->
        <div class="cart-summary">
          <div class="summary-card space-card">
            <h3>📊 Tóm tắt đơn hàng</h3>

            <div class="summary-details">
              <div class="summary-row">
                <span>Tạm tính ({{ totalItems }} sản phẩm):</span>
                <span>{{ formatCurrency(subtotal) }}</span>
              </div>

              <div v-if="discount > 0" class="summary-row discount-row">
                <span>Giảm giá:</span>
                <span class="discount-amount">-{{ formatCurrency(discount) }}</span>
              </div>

              <div class="summary-divider"></div>

              <div class="summary-row total-row">
                <span>Tổng cộng:</span>
                <span class="total-amount">{{ formatCurrency(totalAmount) }}</span>
              </div>
            </div>

            <!-- Checkout Button -->
            <button
              @click="proceedToCheckout"
              class="checkout-btn"
              :disabled="cartItems.length === 0 || hasOutOfStockItems"
            >
              🚀 Tiến hành thanh toán
            </button>

            <!-- Security Info -->
            <div class="security-info">
              <div class="security-item">
                <span class="security-icon">🔒</span>
                <span>Thanh toán bảo mật 256-bit</span>
              </div>
              <div class="security-item">
                <span class="security-icon">🛡️</span>
                <span>Bảo vệ người mua 100%</span>
              </div>
              <div class="security-item">
                <span class="security-icon">↩️</span>
                <span>Đổi trả trong 30 ngày</span>
              </div>
            </div>
          </div>

          <!-- Recommended Products -->
          <div class="recommended-products space-card">
            <h3>🌟 Có thể bạn sẽ thích</h3>
            <div class="recommended-list">
              <div
                v-for="product in recommendedProducts"
                :key="product.id"
                class="recommended-item"
              >
                <router-link :to="`/products/${product.id}`" class="recommended-link">
                  <div class="recommended-image">
                    <img
                      :src="product.images?.[0] || '/placeholder-product.jpg'"
                      :alt="product.name"
                    />
                  </div>
                  <div class="recommended-info">
                    <h4>{{ product.name }}</h4>
                    <div class="recommended-price">{{ formatCurrency(product.price) }}</div>
                    <div class="recommended-rating">
                      <span class="stars">⭐⭐⭐⭐⭐</span>
                      <span class="rating-count">({{ product.reviewCount }})</span>
                    </div>
                  </div>
                </router-link>
                <button @click="addRecommendedToCart(product)" class="add-recommended-btn">
                  🛒
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Saved for Later -->
      <div v-if="savedItems.length > 0" class="saved-items">
        <h3>💾 Đã lưu để mua sau ({{ savedItems.length }})</h3>
        <div class="saved-items-list">
          <div v-for="product in savedItems" :key="product.id" class="saved-item space-card">
            <div class="saved-item-image">
              <img
                :src="
                  product.images && product.images.length > 0
                    ? product.images[0]
                    : '/placeholder-product.jpg'
                "
                :alt="product.name || 'Sản phẩm đã lưu'"
              />
            </div>
            <div class="saved-item-info">
              <h4>{{ product.name || 'Sản phẩm không tồn tại' }}</h4>
              <div class="saved-item-price">{{ formatCurrency(product.price || 0) }}</div>
            </div>
            <div class="saved-item-actions">
              <button @click="moveToCart(product.id)" class="btn btn-secondary btn-small">
                🛒 Thêm vào giỏ
              </button>
              <button @click="deleteSavedItem(product.id)" class="btn btn-danger btn-small">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty Cart -->
      <div v-if="cartItems.length === 0 && savedItems.length === 0" class="empty-cart">
        <div class="empty-cart-content space-card">
          <div class="empty-cart-animation">
            <div class="floating-cart">🛒</div>
            <div class="cosmic-particles">
              <span class="particle">✨</span>
              <span class="particle">🌟</span>
              <span class="particle">💫</span>
              <span class="particle">⭐</span>
            </div>
          </div>

          <h2>Giỏ hành tinh của bạn đang trống</h2>
          <p>Hãy khám phá vũ trụ sản phẩm và thêm những món đồ tuyệt vời vào giỏ hàng!</p>

          <div class="empty-cart-actions">
            <router-link to="/products" class="btn btn-primary btn-large">
              🚀 Khám phá sản phẩm
            </router-link>
            <router-link to="/categories" class="btn btn-secondary btn-large">
              🌌 Duyệt danh mục
            </router-link>
          </div>

          <!-- Quick Categories -->
          <div class="quick-categories">
            <h3>Danh mục phổ biến</h3>
            <div class="category-chips">
              <router-link
                v-for="category in popularCategories"
                :key="category.id"
                :to="`/categories/${category.slug}`"
                class="category-chip"
              >
                {{ category.icon }} {{ category.name }}
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { recommendationAPI, categoryAPI, profileAPI, orderAPI } from '@/services/api'

export default {
  name: 'Cart',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()
    const authStore = useAuthStore()

    const loading = ref(false)
    const cartItems = computed(() => cartStore.items || [])
    const isEmpty = computed(() => cartStore.isEmpty)

    const savedItems = ref([])
    const recommendedProducts = ref([])
    const popularCategories = ref([])

    const totalItems = computed(() => cartStore.totalItems || 0)
    const subtotal = computed(() => cartStore.totalAmount || 0)

    const discount = computed(() => {
      return 0
    })

    const totalAmount = computed(() => {
      return subtotal.value - discount.value
    })

    const hasOutOfStockItems = computed(() => {
      return cartItems.value.some((item) => item.quantity > (item.stock || 100))
    })

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const loadSavedItems = async () => {
      try {
        const response = await profileAPI.getSavedItems()
        savedItems.value = response.data?.savedItems || []
        console.log('savedItems:', savedItems.value)
      } catch (error) {
        savedItems.value = []
      }
    }

    const loadRecommendedProducts = async () => {
      try {
        const response = await recommendationAPI.forUser(3)
        recommendedProducts.value = response.data?.recommendations || []
      } catch (error) {
        console.error('Error loading recommended products:', error)
        recommendedProducts.value = []
      }
    }

    const loadPopularCategories = async () => {
      try {
        const response = await categoryAPI.getAll()
        popularCategories.value = response.data?.slice(0, 4) || []
      } catch (error) {
        console.error('Error loading popular categories:', error)
        popularCategories.value = []
      }
    }

    const updateQuantity = async (productId, newQuantity) => {
      console.log('🔄 Update quantity called:', { productId, newQuantity })

      if (newQuantity < 1) {
        console.log('❌ Invalid quantity:', newQuantity)
        return
      }

      try {
        loading.value = true
        console.log('📡 Calling cartStore.updateItemQuantity...')

        await cartStore.updateItemQuantity(productId, newQuantity)

        console.log('✅ Quantity updated successfully')

        await cartStore.loadCart()
      } catch (error) {
        console.error('❌ Error updating quantity:', error)
        console.error('Error details:', error.response?.data)
        alert(
          'Có lỗi xảy ra khi cập nhật số lượng: ' + (error.response?.data?.message || error.message)
        )
      } finally {
        loading.value = false
      }
    }

    const removeItem = async (productId) => {
      console.log('🗑️ Remove item called:', { productId })

      if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) return

      try {
        loading.value = true
        console.log('📡 Calling cartStore.removeItem...')

        await cartStore.removeItem(productId)

        console.log('✅ Item removed successfully')

        await cartStore.loadCart()
      } catch (error) {
        console.error('❌ Error removing item:', error)
        console.error('Error details:', error.response?.data)
        alert('Có lỗi xảy ra khi xóa sản phẩm: ' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }

    const clearCart = async () => {
      if (!confirm('Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?')) return

      try {
        loading.value = true
        await cartStore.clearCart()
        console.log('✅ Cart cleared')
        alert('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
      } catch (error) {
        console.error('❌ Error clearing cart:', error)
        alert('Có lỗi xảy ra khi xóa giỏ hàng')
      } finally {
        loading.value = false
      }
    }

    const saveForLater = async (productId) => {
      const item = cartItems.value.find((item) => item.productId === productId)
      if (!item) return

      try {
        await profileAPI.saveForLater(productId)
        await removeItem(productId)
        await loadSavedItems()
        alert('Đã lưu sản phẩm để mua sau')
      } catch (error) {
        console.error('Error saving item:', error)
        alert('Có lỗi xảy ra khi lưu sản phẩm')
      }
    }

    const moveToCart = async (productId) => {
      const product = savedItems.value.find((p) => p.id === productId)
      if (!product) return

      try {
        await cartStore.addItem(product.id, 1)

        await profileAPI.removeFromSavedItems(productId)

        const index = savedItems.value.findIndex((p) => p.id === productId)
        if (index > -1) {
          savedItems.value.splice(index, 1)
        }

        alert('Đã thêm sản phẩm vào giỏ hàng')
      } catch (error) {
        console.error('Error moving to cart:', error)
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng')
      }
    }

    const deleteSavedItem = async (itemId) => {
      if (!confirm('Bạn có chắc muốn xóa sản phẩm đã lưu này?')) return

      try {
        await profileAPI.removeFromSavedItems(itemId)

        const index = savedItems.value.findIndex((item) => item.id === itemId)
        if (index > -1) {
          savedItems.value.splice(index, 1)
        }

        alert('Đã xóa sản phẩm khỏi danh sách lưu')
      } catch (error) {
        console.error('Error deleting saved item:', error)
        alert('Có lỗi xảy ra khi xóa sản phẩm')
      }
    }

    const proceedToCheckout = () => {
      if (!authStore.isAuthenticated) {
        if (confirm('Bạn cần đăng nhập để tiếp tục. Chuyển đến trang đăng nhập?')) {
          router.push('/login')
        }
        return
      }

      if (hasOutOfStockItems.value) {
        alert('Có sản phẩm trong giỏ hàng vượt quá số lượng tồn kho. Vui lòng điều chỉnh lại.')
        return
      }

      router.push('/checkout')
    }

    const addRecommendedToCart = async (product) => {
      try {
        await cartStore.addItem(product.id, 1)
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        console.error('Error adding recommended product:', error)
        alert('Có lỗi xảy ra khi thêm sản phẩm')
      }
    }

    onMounted(async () => {
      console.log('Cart page mounted - loading real cart data...')

      if (authStore.isAuthenticated) {
        try {
          loading.value = true

          // Load cart data
          await cartStore.loadCart()
          console.log('✅ Cart data loaded:', cartStore.items)

          // Load additional data
          await Promise.all([loadSavedItems(), loadRecommendedProducts(), loadPopularCategories()])
        } catch (error) {
          console.error('❌ Error loading cart:', error)
        } finally {
          loading.value = false
        }
      } else {
        console.log('User not authenticated, redirecting to login')
        router.push('/login')
      }
    })

    return {
      cartItems,
      isEmpty,
      totalItems,
      subtotal,
      totalAmount,
      discount,
      hasOutOfStockItems,

      loading,

      savedItems,
      recommendedProducts,
      popularCategories,

      formatCurrency,
      updateQuantity,
      removeItem,
      clearCart,
      saveForLater,
      moveToCart,
      deleteSavedItem,
      proceedToCheckout,
      addRecommendedToCart,
    }
  },
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  opacity: 0.9;
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 3rem;
  align-items: start;
}

.cart-main {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.cart-header h2 {
  color: var(--text-accent);
  font-size: 1.5rem;
}

.clear-cart-btn {
  background: none;
  border: 1px solid rgba(239, 68, 68, 0.5);
  color: #ef4444;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.clear-cart-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.cart-item {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  position: relative;
  transition: all 0.3s ease;
}

.cart-item.removing {
  opacity: 0.5;
  transform: scale(0.98);
}

.item-image {
  flex: none;
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.item-info {
  flex: 1;
}

.item-name {
  margin-bottom: 0.5rem;
}

.item-name a {
  color: var(--text-primary);
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.item-name a:hover {
  color: var(--text-accent);
}

.item-actions {
  flex: none;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: flex-end;
  min-width: 200px;
}

.quantity-controls label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: bold;
}

.quantity-btn:hover:not(:disabled) {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  text-align: center;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary);
  border-radius: 6px;
  padding: 0.4rem;
  font-size: 0.9rem;
}

.item-price-section {
  text-align: right;
}

.price-details {
  margin-bottom: 0.5rem;
}

.unit-price {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.total-price {
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-accent);
}

.item-controls {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  white-space: nowrap;
}

.action-btn:hover {
  border-color: var(--text-accent);
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.remove-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.item-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  backdrop-filter: blur(2px);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top: 3px solid var(--text-accent);
  border-radius: 50%;
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

.saved-items {
  margin-top: 2rem;
}

.saved-items h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.saved-items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.saved-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.saved-item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex: none;
}

.saved-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.saved-item-info {
  flex: 1;
}

.saved-item-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.saved-item-price {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 0.9rem;
}

.saved-item-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
}

.cart-summary {
  position: sticky;
  top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.summary-card {
  padding: 1.5rem;
}

.summary-card h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
}

.summary-details {
  margin-bottom: 1.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
}

.summary-row:last-child {
  margin-bottom: 0;
}

.discount-row {
  color: #10b981;
}

.discount-amount {
  color: #10b981;
}

.summary-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 1rem 0;
}

.total-row {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 0.5rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.total-amount {
  color: var(--text-accent);
  font-size: 1.3rem;
}

.checkout-btn {
  width: 100%;
  background: var(--accent-gradient);
  border: none;
  color: white;
  padding: 1rem 2rem;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--space-glow);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.security-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.security-icon {
  flex: none;
}

.recommended-products h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.recommended-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.recommended-item {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.recommended-item:last-child {
  border-bottom: none;
}

.recommended-link {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex: 1;
  text-decoration: none;
  color: inherit;
}

.recommended-image {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex: none;
}

.recommended-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommended-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 0.85rem;
}

.recommended-price {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.recommended-rating {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
}

.stars {
  color: #fbbf24;
}

.rating-count {
  color: var(--text-secondary);
}

.add-recommended-btn {
  background: var(--aurora-gradient);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  flex: none;
}

.add-recommended-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--neon-glow);
}

.empty-cart {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.empty-cart-content {
  text-align: center;
  max-width: 500px;
  padding: 3rem;
}

.empty-cart-animation {
  position: relative;
  margin-bottom: 2rem;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-cart {
  font-size: 4rem;
  animation: float 3s ease-in-out infinite;
}

.cosmic-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.particle {
  position: absolute;
  font-size: 1.5rem;
  animation: twinkle 2s ease-in-out infinite;
}

.particle:nth-child(1) {
  top: 20%;
  left: 20%;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 30%;
  right: 25%;
  animation-delay: 0.5s;
}

.particle:nth-child(3) {
  bottom: 25%;
  left: 30%;
  animation-delay: 1s;
}

.particle:nth-child(4) {
  bottom: 20%;
  right: 20%;
  animation-delay: 1.5s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes twinkle {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.empty-cart-content h2 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 1.8rem;
}

.empty-cart-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.empty-cart-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 2rem;
}

.btn-large {
  padding: 1rem 2rem;
  font-size: 1.1rem;
  border-radius: 12px;
}

.quick-categories {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.quick-categories h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
}

.category-chip {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  text-decoration: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.category-chip:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .cart-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .cart-summary {
    position: static;
    order: -1;
  }
}

@media (max-width: 768px) {
  .cart-page {
    padding: 1rem 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .cart-item {
    flex-direction: column;
    gap: 1rem;
  }

  .item-details {
    flex-direction: column;
    gap: 1rem;
  }

  .item-actions {
    align-items: flex-start;
    min-width: auto;
  }

  .quantity-controls {
    justify-content: flex-start;
  }

  .item-controls {
    flex-direction: column;
    width: 100%;
  }

  .empty-cart-actions {
    flex-direction: column;
  }

  .recommended-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .recommended-link {
    width: 100%;
  }

  .add-recommended-btn {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .item-image {
    width: 100px;
    height: 100px;
  }

  .summary-card {
    padding: 1rem;
  }

  .empty-cart-content {
    padding: 2rem 1rem;
  }

  .floating-cart {
    font-size: 3rem;
  }

  .category-chips {
    flex-direction: column;
  }

  .category-chip {
    text-align: center;
  }
}

/* Loading states */
.btn.loading {
  position: relative;
  color: transparent;
}

.btn.loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Animation for item removal */
.cart-item.removing {
  animation: slideOut 0.3s ease-out forwards;
}

@keyframes slideOut {
  0% {
    transform: translateX(0);
    opacity: 1;
  }
  100% {
    transform: translateX(-100%);
    opacity: 0;
  }
}

/* Custom scrollbar for recommended list */
.recommended-list::-webkit-scrollbar {
  width: 4px;
}

.recommended-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.recommended-list::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.5);
  border-radius: 2px;
}

.recommended-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 212, 255, 0.7);
}
</style>
