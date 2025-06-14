<template>
  <div class="cart-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🛒 Giỏ hàng vũ trụ</h1>
        <p class="page-subtitle">Quản lý các sản phẩm trong giỏ hàng của bạn</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading"></div>
        <p class="mt-2">Đang tải giỏ hàng...</p>
      </div>

      <!-- Empty Cart -->
      <div v-else-if="isEmpty" class="empty-cart">
        <div class="empty-cart-content space-card">
          <div class="empty-icon">🛒</div>
          <h3>Giỏ hàng trống</h3>
          <p>Chưa có sản phẩm nào trong giỏ hàng của bạn</p>
          <router-link to="/products" class="btn btn-primary">
            🌟 Khám phá sản phẩm
          </router-link>
        </div>
      </div>

      <!-- Cart Content -->
      <div v-else class="cart-content">
        <div class="cart-items-section">
          <div class="cart-header space-card">
            <h2>Sản phẩm trong giỏ ({{ totalItems }} sản phẩm)</h2>
            <button @click="clearCart" class="btn btn-danger btn-sm">
              🗑️ Xóa tất cả
            </button>
          </div>

          <div class="cart-items">
            <div v-for="item in items" :key="item.productId" class="cart-item space-card">
              <div class="item-image">
                <img 
                  :src="item.productImage || '/placeholder-product.jpg'" 
                  :alt="item.productName"
                />
              </div>

              <div class="item-info">
                <h3 class="item-name">{{ item.productName }}</h3>
                <p class="item-price">{{ formatPrice(item.productPrice) }}</p>
                
                <div class="item-controls">
                  <div class="quantity-controls">
                    <button 
                      @click="decreaseQuantity(item)" 
                      :disabled="item.quantity <= 1 || updating"
                      class="quantity-btn"
                    >
                      -
                    </button>
                    <span class="quantity-display">{{ item.quantity }}</span>
                    <button 
                      @click="increaseQuantity(item)" 
                      :disabled="updating"
                      class="quantity-btn"
                    >
                      +
                    </button>
                  </div>

                  <button 
                    @click="removeItem(item)" 
                    :disabled="updating"
                    class="remove-btn"
                  >
                    🗑️ Xóa
                  </button>
                </div>
              </div>

              <div class="item-total">
                <span class="subtotal-label">Tổng:</span>
                <span class="subtotal-amount">{{ formatPrice(item.subtotal) }}</span>
              </div>

              <!-- Loading overlay for item updates -->
              <div v-if="updating" class="item-loading">
                <div class="loading-small"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Cart Summary -->
        <div class="cart-summary-section">
          <div class="cart-summary space-card">
            <h3>📋 Tóm tắt đơn hàng</h3>
            
            <div class="summary-line">
              <span>Tạm tính:</span>
              <span>{{ formatPrice(totalAmount) }}</span>
            </div>
            
            <div class="summary-line">
              <span>Phí vận chuyển:</span>
              <span class="free-shipping">Miễn phí</span>
            </div>
            
            <div class="summary-line discount" v-if="discount > 0">
              <span>Giảm giá:</span>
              <span class="discount-amount">-{{ formatPrice(discount) }}</span>
            </div>
            
            <hr class="summary-divider">
            
            <div class="summary-total">
              <span>Tổng cộng:</span>
              <span class="total-amount">{{ formatPrice(finalTotal) }}</span>
            </div>

            <!-- Discount Code -->
            <div class="discount-section">
              <div class="discount-input-group">
                <input
                  v-model="discountCode"
                  type="text"
                  class="form-input discount-input"
                  placeholder="Mã giảm giá"
                />
                <button 
                  @click="applyDiscount" 
                  :disabled="!discountCode || applying"
                  class="btn btn-secondary btn-sm apply-btn"
                >
                  {{ applying ? 'Đang áp dụng...' : 'Áp dụng' }}
                </button>
              </div>
              <div v-if="discountError" class="discount-error">
                {{ discountError }}
              </div>
              <div v-if="discountSuccess" class="discount-success">
                {{ discountSuccess }}
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="cart-actions">
              <router-link to="/products" class="btn btn-secondary w-full">
                ⬅️ Tiếp tục mua sắm
              </router-link>
              
              <button 
                @click="proceedToCheckout" 
                :disabled="!isValidCart || processing"
                class="btn btn-primary w-full"
              >
                <span v-if="processing">🔄 Đang xử lý...</span>
                <span v-else>💳 Tiến hành thanh toán</span>
              </button>
            </div>

            <!-- Cart Validation -->
            <div v-if="validationError" class="cart-validation-error">
              ⚠️ {{ validationError }}
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="quick-actions space-card">
            <h4>⚡ Thao tác nhanh</h4>
            <div class="quick-action-buttons">
              <button @click="saveForLater" class="quick-action-btn">
                💾 Lưu để mua sau
              </button>
              <button @click="shareCart" class="quick-action-btn">
                📤 Chia sẻ giỏ hàng
              </button>
              <button @click="createWishlist" class="quick-action-btn">
                ❤️ Thêm vào yêu thích
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Recommended Products -->
      <div v-if="!isEmpty" class="recommended-section">
        <h2 class="section-title">🌟 Có thể bạn quan tâm</h2>
        <div class="recommended-grid">
          <div v-for="product in recommendedProducts" :key="product.id" class="recommended-card space-card">
            <router-link :to="`/products/${product.id}`">
              <img :src="product.images?.[0]" :alt="product.name" class="recommended-image" />
              <div class="recommended-info">
                <h4>{{ product.name }}</h4>
                <p class="recommended-price">{{ formatPrice(product.price) }}</p>
              </div>
            </router-link>
            <button @click="addRecommendedToCart(product)" class="btn btn-primary btn-sm">
              🛒 Thêm
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import { productAPI } from '@/services/api'

export default {
  name: 'Cart',
  setup() {
    const cartStore = useCartStore()
    const router = useRouter()
    
    // Reactive data
    const loading = ref(false)
    const updating = ref(false)
    const processing = ref(false)
    const applying = ref(false)
    const discountCode = ref('')
    const discount = ref(0)
    const discountError = ref('')
    const discountSuccess = ref('')
    const validationError = ref('')
    const recommendedProducts = ref([])

    // Computed properties
    const items = computed(() => cartStore.items)
    const totalItems = computed(() => cartStore.totalItems)
    const totalAmount = computed(() => cartStore.totalAmount)
    const isEmpty = computed(() => cartStore.isEmpty)
    const finalTotal = computed(() => Math.max(0, totalAmount.value - discount.value))
    const isValidCart = computed(() => {
      return items.value.length > 0 && !validationError.value
    })

    // Methods
    const loadCart = async () => {
      loading.value = true
      try {
        await cartStore.loadCart()
        await validateCart()
      } catch (error) {
        console.error('Error loading cart:', error)
      } finally {
        loading.value = false
      }
    }

    const validateCart = async () => {
      try {
        const isValid = await cartStore.validateCart()
        if (!isValid) {
          validationError.value = 'Một số sản phẩm trong giỏ hàng đã hết hàng hoặc không khả dụng'
        } else {
          validationError.value = ''
        }
      } catch (error) {
        validationError.value = 'Không thể kiểm tra tính hợp lệ của giỏ hàng'
      }
    }

    const increaseQuantity = async (item) => {
      updating.value = true
      try {
        await cartStore.updateItemQuantity(item.productId, item.quantity + 1)
        await validateCart()
      } catch (error) {
        console.error('Error updating quantity:', error)
      } finally {
        updating.value = false
      }
    }

    const decreaseQuantity = async (item) => {
      updating.value = true
      try {
        await cartStore.updateItemQuantity(item.productId, item.quantity - 1)
        await validateCart()
      } catch (error) {
        console.error('Error updating quantity:', error)
      } finally {
        updating.value = false
      }
    }

    const removeItem = async (item) => {
      updating.value = true
      try {
        await cartStore.removeItem(item.productId)
        await validateCart()
      } catch (error) {
        console.error('Error removing item:', error)
      } finally {
        updating.value = false
      }
    }

    const clearCart = async () => {
      if (confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?')) {
        updating.value = true
        try {
          await cartStore.clearCart()
        } catch (error) {
          console.error('Error clearing cart:', error)
        } finally {
          updating.value = false
        }
      }
    }

    const applyDiscount = async () => {
      applying.value = true
      discountError.value = ''
      discountSuccess.value = ''
      
      try {
        // Mock discount validation
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        if (discountCode.value.toUpperCase() === 'COSMIC10') {
          discount.value = totalAmount.value * 0.1
          discountSuccess.value = 'Áp dụng mã giảm giá thành công! Giảm 10%'
        } else if (discountCode.value.toUpperCase() === 'SPACE50') {
          discount.value = 50000
          discountSuccess.value = 'Áp dụng mã giảm giá thành công! Giảm 50.000₫'
        } else {
          discountError.value = 'Mã giảm giá không hợp lệ'
        }
      } catch (error) {
        discountError.value = 'Có lỗi xảy ra khi áp dụng mã giảm giá'
      } finally {
        applying.value = false
      }
    }

    const proceedToCheckout = async () => {
      processing.value = true
      try {
        await validateCart()
        if (isValidCart.value) {
          router.push('/checkout')
        }
      } catch (error) {
        console.error('Error proceeding to checkout:', error)
      } finally {
        processing.value = false
      }
    }

    const loadRecommendedProducts = async () => {
      try {
        const response = await productAPI.getFeatured()
        recommendedProducts.value = response.data.slice(0, 4)
      } catch (error) {
        console.error('Error loading recommended products:', error)
      }
    }

    const addRecommendedToCart = async (product) => {
      try {
        await cartStore.addItem(product.id, 1)
      } catch (error) {
        console.error('Error adding recommended product:', error)
      }
    }

    const saveForLater = () => {
      alert('Tính năng "Lưu để mua sau" sẽ được phát triển trong phiên bản tiếp theo!')
    }

    const shareCart = () => {
      alert('Tính năng "Chia sẻ giỏ hàng" sẽ được phát triển trong phiên bản tiếp theo!')
    }

    const createWishlist = () => {
      alert('Tính năng "Danh sách yêu thích" sẽ được phát triển trong phiên bản tiếp theo!')
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }

    onMounted(() => {
      loadCart()
      loadRecommendedProducts()
    })

    return {
      loading,
      updating,
      processing,
      applying,
      discountCode,
      discount,
      discountError,
      discountSuccess,
      validationError,
      recommendedProducts,
      items,
      totalItems,
      totalAmount,
      isEmpty,
      finalTotal,
      isValidCart,
      increaseQuantity,
      decreaseQuantity,
      removeItem,
      clearCart,
      applyDiscount,
      proceedToCheckout,
      addRecommendedToCart,
      saveForLater,
      shareCart,
      createWishlist,
      formatPrice
    }
  }
}
</script>

<style scoped>
.cart-page {
  padding: 40px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-accent);
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

.loading-section {
  text-align: center;
  padding: 60px 0;
}

.empty-cart {
  padding: 60px 0;
}

.empty-cart-content {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
  padding: 40px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  opacity: 0.5;
}

.cart-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
  align-items: start;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.cart-header h2 {
  color: var(--text-accent);
  margin: 0;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  gap: 20px;
  padding: 20px;
  position: relative;
}

.item-image img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.item-price {
  font-size: 1rem;
  color: var(--text-accent);
  font-weight: 500;
  margin: 0;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--text-accent);
  background: transparent;
  color: var(--text-accent);
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-btn:hover:not(:disabled) {
  background: var(--text-accent);
  color: var(--space-dark);
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-display {
  min-width: 40px;
  text-align: center;
  font-weight: 500;
}

.remove-btn {
  background: none;
  border: none;
  color: var(--text-danger);
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: rgba(233, 69, 96, 0.1);
}

.item-total {
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.subtotal-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.subtotal-amount {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-accent);
}

.item-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.loading-small {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  border-top-color: var(--text-accent);
  animation: spin 1s ease-in-out infinite;
}

.cart-summary {
  padding: 24px;
  position: sticky;
  top: 100px;
}

.cart-summary h3 {
  margin-bottom: 20px;
  color: var(--text-accent);
}

.summary-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--text-secondary);
}

.free-shipping {
  color: var(--text-success);
  font-weight: 500;
}

.discount-amount {
  color: var(--text-success);
}

.summary-divider {
  border: none;
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 20px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 24px;
}

.total-amount {
  color: var(--text-accent);
}

.discount-section {
  margin-bottom: 24px;
}

.discount-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.discount-input {
  flex: 1;
  padding: 10px 12px;
}

.apply-btn {
  padding: 10px 16px;
  white-space: nowrap;
}

.discount-error {
  color: var(--text-danger);
  font-size: 14px;
}

.discount-success {
  color: var(--text-success);
  font-size: 14px;
}

.cart-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-validation-error {
  color: var(--text-warning);
  font-size: 14px;
  margin-top: 12px;
  padding: 8px 12px;
  background: rgba(255, 205, 60, 0.1);
  border-radius: 6px;
}

.quick-actions {
  padding: 20px;
  margin-top: 20px;
}

.quick-actions h4 {
  margin-bottom: 16px;
  color: var(--text-accent);
}

.quick-action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-action-btn {
  background: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: all 0.3s ease;
  font-size: 14px;
}

.quick-action-btn:hover {
  border-color: var(--text-accent);
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 600;
  margin: 60px 0 32px;
  color: var(--text-accent);
}

.recommended-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.recommended-card {
  padding: 16px;
  text-align: center;
}

.recommended-card a {
  text-decoration: none;
  color: inherit;
}

.recommended-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

.recommended-info h4 {
  margin-bottom: 8px;
  color: var(--text-primary);
}

.recommended-price {
  color: var(--text-accent);
  font-weight: 600;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
  
  .cart-item {
    grid-template-columns: 80px 1fr;
    gap: 12px;
  }
  
  .item-total {
    grid-column: 1 / -1;
    text-align: left;
    margin-top: 12px;
  }
  
  .item-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .discount-input-group {
    flex-direction: column;
  }
  
  .recommended-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>