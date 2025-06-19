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
              <button @click="clearCart" class="clear-cart-btn">
                🗑️ Xóa tất cả
              </button>
            </div>

            <div class="items-list">
              <div 
                v-for="item in cartItems" 
                :key="item.id" 
                class="cart-item space-card"
                :class="{ 'removing': item.isRemoving }"
              >
                <div class="item-image">
                  <img :src="item.product.images?.[0] || '/placeholder-product.jpg'" :alt="item.product.name" />
                  <div v-if="item.product.discount" class="discount-badge">
                    -{{ item.product.discount }}%
                  </div>
                </div>

                <div class="item-details">
                  <div class="item-info">
                    <h3 class="item-name">
                      <router-link :to="`/products/${item.product.id}`">
                        {{ item.product.name }}
                      </router-link>
                    </h3>
                    <p class="item-category">{{ item.product.category?.name }}</p>
                    
                    <div class="item-features">
                      <span v-for="feature in item.product.keyFeatures?.slice(0, 2)" :key="feature" class="feature-tag">
                        {{ feature }}
                      </span>
                    </div>

                    <div class="item-seller">
                      <span class="seller-info">
                        🏪 {{ item.product.seller?.name || 'Cosmic Store' }}
                      </span>
                      <span class="shipping-info">
                        🚀 Giao hàng từ {{ item.product.seller?.location || 'Thiên hà Milky Way' }}
                      </span>
                    </div>
                  </div>

                  <div class="item-actions">
                    <div class="quantity-controls">
                      <label>Số lượng:</label>
                      <div class="quantity-selector">
                        <button 
                          @click="updateQuantity(item.id, item.quantity - 1)"
                          :disabled="item.quantity <= 1 || item.updating"
                          class="quantity-btn"
                        >
                          -
                        </button>
                        <input 
                          v-model.number="item.quantity"
                          @change="updateQuantity(item.id, item.quantity)"
                          type="number" 
                          min="1" 
                          :max="item.product.stock"
                          class="quantity-input"
                          :disabled="item.updating"
                        />
                        <button 
                          @click="updateQuantity(item.id, item.quantity + 1)"
                          :disabled="item.quantity >= item.product.stock || item.updating"
                          class="quantity-btn"
                        >
                          +
                        </button>
                      </div>
                    </div>

                    <div class="item-price-section">
                      <div class="price-details">
                        <span v-if="item.product.originalPrice && item.product.originalPrice > item.product.price" 
                              class="original-price">
                          {{ formatCurrency(item.product.originalPrice) }}
                        </span>
                        <span class="unit-price">{{ formatCurrency(item.product.price) }}</span>
                      </div>
                      <div class="total-price">
                        {{ formatCurrency(item.product.price * item.quantity) }}
                      </div>
                    </div>

                    <div class="item-controls">
                      <button @click="saveForLater(item.id)" class="action-btn">
                        💾 Lưu lại sau
                      </button>
                      <button @click="removeItem(item.id)" class="action-btn remove-btn" :disabled="item.removing">
                        {{ item.removing ? '🔄' : '🗑️' }} Xóa
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

          <!-- Saved for Later -->
          <div v-if="savedItems.length > 0" class="saved-items">
            <h3>💾 Đã lưu để mua sau ({{ savedItems.length }})</h3>
            <div class="saved-items-list">
              <div v-for="item in savedItems" :key="item.id" class="saved-item space-card">
                <div class="saved-item-image">
                  <img :src="item.product.images?.[0] || '/placeholder-product.jpg'" :alt="item.product.name" />
                </div>
                <div class="saved-item-info">
                  <h4>{{ item.product.name }}</h4>
                  <div class="saved-item-price">{{ formatCurrency(item.product.price) }}</div>
                </div>
                <div class="saved-item-actions">
                  <button @click="moveToCart(item.id)" class="btn btn-secondary btn-small">
                    🛒 Thêm vào giỏ
                  </button>
                  <button @click="deleteSavedItem(item.id)" class="btn btn-danger btn-small">
                    🗑️
                  </button>
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
              
              <div class="summary-row">
                <span>Phí vận chuyển:</span>
                <span class="shipping-fee">
                  {{ shippingFee > 0 ? formatCurrency(shippingFee) : 'Miễn phí' }}
                </span>
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

            <!-- Promo Code -->
            <div class="promo-section">
              <div class="promo-input-group">
                <input 
                  v-model="promoCode" 
                  type="text" 
                  placeholder="Nhập mã giảm giá"
                  class="promo-input"
                  :disabled="promoLoading"
                />
                <button 
                  @click="applyPromoCode" 
                  class="promo-btn"
                  :disabled="!promoCode.trim() || promoLoading"
                >
                  {{ promoLoading ? '🔄' : '✨' }} Áp dụng
                </button>
              </div>
              <div v-if="promoMessage" class="promo-message" :class="{ error: promoError, success: !promoError }">
                {{ promoMessage }}
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

            <!-- Shipping Info -->
            <div class="shipping-info">
              <div class="shipping-option">
                <div class="shipping-icon">🚀</div>
                <div class="shipping-details">
                  <h4>Giao hàng siêu tốc</h4>
                  <p>Giao trong 24h với công nghệ teleport</p>
                </div>
              </div>
              
              <div class="shipping-option">
                <div class="shipping-icon">🛸</div>
                <div class="shipping-details">
                  <h4>Giao hàng không gian</h4>
                  <p>Miễn phí cho đơn hàng trên {{ formatCurrency(freeShippingThreshold) }}</p>
                </div>
              </div>
            </div>

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
              <div v-for="product in recommendedProducts" :key="product.id" class="recommended-item">
                <router-link :to="`/products/${product.id}`" class="recommended-link">
                  <div class="recommended-image">
                    <img :src="product.images?.[0] || '/placeholder-product.jpg'" :alt="product.name" />
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

      <!-- Empty Cart -->
      <div v-else class="empty-cart">
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
              <router-link v-for="category in popularCategories" :key="category.id" 
                           :to="`/categories/${category.slug}`" 
                           class="category-chip">
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

export default {
  name: 'Cart',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()
    const authStore = useAuthStore()
    
    // Reactive data
    const loading = ref(false)
    const promoCode = ref('')
    const promoLoading = ref(false)
    const promoMessage = ref('')
    const promoError = ref(false)
    const freeShippingThreshold = ref(1000000) // 1M VND
    
    // Mock data
    const cartItems = ref([
      {
        id: 1,
        quantity: 2,
        updating: false,
        removing: false,
        product: {
          id: 1,
          name: 'Laptop Gaming Galactic Pro',
          price: 25000000,
          originalPrice: 30000000,
          stock: 15,
          discount: 17,
          category: { name: 'Công nghệ' },
          images: ['/placeholder-product.jpg'],
          keyFeatures: ['RTX 4080', 'Intel i9', '32GB RAM'],
          seller: { name: 'Cosmic Tech Store', location: 'Hệ mặt trời' }
        }
      },
      {
        id: 2,
        quantity: 1,
        updating: false,
        removing: false,
        product: {
          id: 2,
          name: 'Gaming Mouse Nebula',
          price: 1500000,
          stock: 50,
          category: { name: 'Phụ kiện' },
          images: ['/placeholder-product.jpg'],
          keyFeatures: ['RGB LED', 'Wireless'],
          seller: { name: 'Nebula Gaming', location: 'Sao Hỏa' }
        }
      }
    ])
    
    const savedItems = ref([
      {
        id: 3,
        product: {
          id: 3,
          name: 'Mechanical Keyboard Cosmos',
          price: 2200000,
          images: ['/placeholder-product.jpg']
        }
      }
    ])
    
    const recommendedProducts = ref([
      { id: 4, name: 'Gaming Headset Galaxy', price: 1800000, reviewCount: 89, images: ['/placeholder-product.jpg'] },
      { id: 5, name: 'Monitor 4K Stardust', price: 8500000, reviewCount: 156, images: ['/placeholder-product.jpg'] },
      { id: 6, name: 'Gaming Chair Cosmic', price: 6200000, reviewCount: 67, images: ['/placeholder-product.jpg'] }
    ])
    
    const popularCategories = ref([
      { id: 1, name: 'Công nghệ', slug: 'tech', icon: '💻' },
      { id: 2, name: 'Thời trang', slug: 'fashion', icon: '👗' },
      { id: 3, name: 'Gia đình', slug: 'home', icon: '🏠' },
      { id: 4, name: 'Sách', slug: 'books', icon: '📚' }
    ])
    
    // Computed properties
    const totalItems = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.quantity, 0)
    })
    
    const subtotal = computed(() => {
      return cartItems.value.reduce((total, item) => total + (item.product.price * item.quantity), 0)
    })
    
    const shippingFee = computed(() => {
      return subtotal.value >= freeShippingThreshold.value ? 0 : 50000
    })
    
    const discount = computed(() => {
      // Calculate discount based on promo code or other factors
      return 0
    })
    
    const totalAmount = computed(() => {
      return subtotal.value + shippingFee.value - discount.value
    })
    
    const hasOutOfStockItems = computed(() => {
      return cartItems.value.some(item => item.quantity > item.product.stock)
    })
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const updateQuantity = async (itemId, newQuantity) => {
      if (newQuantity < 1) return
      
      const item = cartItems.value.find(item => item.id === itemId)
      if (!item) return
      
      if (newQuantity > item.product.stock) {
        alert(`Chỉ còn ${item.product.stock} sản phẩm trong kho`)
        return
      }
      
      item.updating = true
      try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500))
        item.quantity = newQuantity
        
        // Update cart store
        await cartStore.updateQuantity(itemId, newQuantity)
      } catch (error) {
        console.error('Error updating quantity:', error)
        alert('Có lỗi xảy ra khi cập nhật số lượng')
      } finally {
        item.updating = false
      }
    }
    
    const removeItem = async (itemId) => {
      const item = cartItems.value.find(item => item.id === itemId)
      if (!item) return
      
      if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) return
      
      item.removing = true
      try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500))
        
        const index = cartItems.value.findIndex(item => item.id === itemId)
        if (index > -1) {
          cartItems.value.splice(index, 1)
        }
        
        // Update cart store
        await cartStore.removeItem(itemId)
      } catch (error) {
        console.error('Error removing item:', error)
        alert('Có lỗi xảy ra khi xóa sản phẩm')
        item.removing = false
      }
    }
    
    const clearCart = async () => {
      if (!confirm('Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?')) return
      
      try {
        loading.value = true
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        cartItems.value = []
        await cartStore.clearCart()
        
        alert('Đã xóa tất cả sản phẩm khỏi giỏ hàng')
      } catch (error) {
        console.error('Error clearing cart:', error)
        alert('Có lỗi xảy ra khi xóa giỏ hàng')
      } finally {
        loading.value = false
      }
    }
    
    const saveForLater = async (itemId) => {
      const item = cartItems.value.find(item => item.id === itemId)
      if (!item) return
      
      try {
        // Move item to saved items
        savedItems.value.push({
          id: itemId,
          product: item.product
        })
        
        // Remove from cart
        const index = cartItems.value.findIndex(item => item.id === itemId)
        if (index > -1) {
          cartItems.value.splice(index, 1)
        }
        
        alert('Đã lưu sản phẩm để mua sau')
      } catch (error) {
        console.error('Error saving item:', error)
        alert('Có lỗi xảy ra khi lưu sản phẩm')
      }
    }
    
    const moveToCart = async (itemId) => {
      const savedItem = savedItems.value.find(item => item.id === itemId)
      if (!savedItem) return
      
      try {
        // Add to cart
        cartItems.value.push({
          id: itemId,
          quantity: 1,
          updating: false,
          removing: false,
          product: savedItem.product
        })
        
        // Remove from saved items
        const index = savedItems.value.findIndex(item => item.id === itemId)
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
        const index = savedItems.value.findIndex(item => item.id === itemId)
        if (index > -1) {
          savedItems.value.splice(index, 1)
        }
        
        alert('Đã xóa sản phẩm khỏi danh sách lưu')
      } catch (error) {
        console.error('Error deleting saved item:', error)
        alert('Có lỗi xảy ra khi xóa sản phẩm')
      }
    }
    
    const applyPromoCode = async () => {
      if (!promoCode.value.trim()) return
      
      promoLoading.value = true
      promoMessage.value = ''
      promoError.value = false
      
      try {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Mock promo code validation
        const validCodes = ['COSMIC20', 'GALAXY15', 'SPACE10']
        if (validCodes.includes(promoCode.value.toUpperCase())) {
          promoMessage.value = 'Mã giảm giá đã được áp dụng thành công!'
          promoError.value = false
          // Apply discount logic here
        } else {
          promoMessage.value = 'Mã giảm giá không hợp lệ hoặc đã hết hạn'
          promoError.value = true
        }
      } catch (error) {
        console.error('Error applying promo code:', error)
        promoMessage.value = 'Có lỗi xảy ra khi áp dụng mã giảm giá'
        promoError.value = true
      } finally {
        promoLoading.value = false
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
        // Check if product already in cart
        const existingItem = cartItems.value.find(item => item.product.id === product.id)
        if (existingItem) {
          await updateQuantity(existingItem.id, existingItem.quantity + 1)
        } else {
          // Add new item to cart
          const newItem = {
            id: Date.now(),
            quantity: 1,
            updating: false,
            removing: false,
            product: product
          }
          cartItems.value.push(newItem)
          await cartStore.addToCart(product.id, 1)
        }
        
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        console.error('Error adding recommended product:', error)
        alert('Có lỗi xảy ra khi thêm sản phẩm')
      }
    }
    
    // Lifecycle
    onMounted(() => {
      // Load cart data from store or API
      console.log('Cart page mounted')
    })
    
    return {
      loading,
      cartItems,
      savedItems,
      recommendedProducts,
      popularCategories,
      promoCode,
      promoLoading,
      promoMessage,
      promoError,
      freeShippingThreshold,
      totalItems,
      subtotal,
      shippingFee,
      discount,
      totalAmount,
      hasOutOfStockItems,
      formatCurrency,
      updateQuantity,
      removeItem,
      clearCart,
      saveForLater,
      moveToCart,
      deleteSavedItem,
      applyPromoCode,
      proceedToCheckout,
      addRecommendedToCart
    }
  }
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

.discount-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--accent-gradient);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.7rem;
  font-weight: 600;
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

.item-category {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.item-features {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.feature-tag {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.item-seller {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
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

.original-price {
  display: block;
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-decoration: line-through;
  opacity: 0.7;
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

.shipping-fee {
  color: #10b981;
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

.promo-section {
  margin-bottom: 1.5rem;
}

.promo-input-group {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.promo-input {
  flex: 1;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary);
  border-radius: 8px;
  padding: 0.75rem;
  font-size: 0.9rem;
}

.promo-input:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.promo-btn {
  background: var(--aurora-gradient);
  border: none;
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  white-space: nowrap;
}

.promo-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--neon-glow);
}

.promo-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.promo-message {
  font-size: 0.8rem;
  padding: 0.5rem;
  border-radius: 6px;
}

.promo-message.success {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.promo-message.error {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
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

.shipping-info {
  margin-bottom: 1.5rem;
}

.shipping-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.shipping-option:last-child {
  border-bottom: none;
}

.shipping-icon {
  font-size: 1.5rem;
  flex: none;
}

.shipping-details h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.shipping-details p {
  color: var(--text-secondary);
  font-size: 0.8rem;
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
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
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
  
  .promo-input-group {
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