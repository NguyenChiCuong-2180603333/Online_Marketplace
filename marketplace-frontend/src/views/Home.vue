<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-background">
        <div class="planet planet-1">🪐</div>
        <div class="planet planet-2">🌍</div>
        <div class="planet planet-3">🌕</div>
      </div>
      
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">
            Chào mừng đến với 
            <span class="text-accent">Cosmic Marketplace</span>
          </h1>
          <p class="hero-subtitle">
            Khám phá vũ trụ mua sắm trực tuyến với hàng ngàn sản phẩm tuyệt vời
          </p>
          <div class="hero-buttons">
            <router-link to="/products" class="btn btn-primary btn-lg">
              🚀 Khám phá ngay
            </router-link>
            <router-link v-if="!isAuthenticated" to="/register" class="btn btn-secondary btn-lg">
              ⭐ Đăng ký ngay
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="featured-products">
      <div class="container">
        <h2 class="section-title">
          ⭐ Sản phẩm nổi bật
        </h2>
        
        <div v-if="featuredLoading" class="text-center">
          <div class="loading"></div>
          <p class="mt-2">Đang tải sản phẩm...</p>
        </div>
        
        <div v-else class="product-grid">
          <div v-for="product in featuredProducts" :key="product.id" class="product-card">
            <div class="product-image-container">
              <img 
                :src="product.images?.[0] || '/placeholder-product.jpg'" 
                :alt="product.name"
                class="product-image"
              />
              <div class="product-badge">⭐ Nổi bật</div>
            </div>
            
            <div class="product-content">
              <h3 class="product-title">{{ product.name }}</h3>
              <p class="product-price">{{ formatPrice(product.price) }}</p>
              <p class="product-description">{{ product.description }}</p>
              
              <div class="product-rating" v-if="product.averageRating > 0">
                <span class="stars">
                  <span v-for="i in 5" :key="i" :class="['star', i <= product.averageRating ? 'filled' : '']">⭐</span>
                </span>
                <span class="rating-text">({{ product.reviewCount }} đánh giá)</span>
              </div>
              
              <div class="product-actions">
                <router-link :to="`/products/${product.id}`" class="btn btn-secondary">
                  Xem chi tiết
                </router-link>
                <button @click="addToCart(product)" class="btn btn-primary" :disabled="cartLoading">
                  🛒 Thêm vào giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest Products -->
    <section class="latest-products">
      <div class="container">
        <h2 class="section-title">
          🆕 Sản phẩm mới nhất
        </h2>
        
        <div v-if="latestLoading" class="text-center">
          <div class="loading"></div>
          <p class="mt-2">Đang tải sản phẩm...</p>
        </div>
        
        <div v-else class="product-grid">
          <div v-for="product in latestProducts" :key="product.id" class="product-card">
            <div class="product-image-container">
              <img 
                :src="product.images?.[0] || '/placeholder-product.jpg'" 
                :alt="product.name"
                class="product-image"
              />
              <div class="product-badge new">🆕 Mới</div>
            </div>
            
            <div class="product-content">
              <h3 class="product-title">{{ product.name }}</h3>
              <p class="product-price">{{ formatPrice(product.price) }}</p>
              <p class="product-description">{{ product.description }}</p>
              
              <div class="product-actions">
                <router-link :to="`/products/${product.id}`" class="btn btn-secondary">
                  Xem chi tiết
                </router-link>
                <button @click="addToCart(product)" class="btn btn-primary" :disabled="cartLoading">
                  🛒 Thêm vào giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features">
      <div class="container">
        <h2 class="section-title">Tại sao chọn Cosmic Marketplace?</h2>
        
        <div class="features-grid">
          <div class="feature-card space-card">
            <div class="feature-icon">🚀</div>
            <h3>Giao hàng nhanh</h3>
            <p>Giao hàng siêu tốc trong toàn vũ trụ với công nghệ warp drive</p>
          </div>
          
          <div class="feature-card space-card">
            <div class="feature-icon">🛡️</div>
            <h3>Bảo mật tuyệt đối</h3>
            <p>Thanh toán an toàn với công nghệ quantum encryption</p>
          </div>
          
          <div class="feature-card space-card">
            <div class="feature-icon">🌟</div>
            <h3>Chất lượng cao</h3>
            <p>Sản phẩm được kiểm định bởi Liên minh Thiên hà</p>
          </div>
          
          <div class="feature-card space-card">
            <div class="feature-icon">🎯</div>
            <h3>Hỗ trợ 24/7</h3>
            <p>Đội ngũ hỗ trợ hoạt động 24/7 trên toàn vũ trụ</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { productAPI } from '@/services/api'

export default {
  name: 'Home',
  setup() {
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    
    const featuredProducts = ref([])
    const latestProducts = ref([])
    const featuredLoading = ref(false)
    const latestLoading = ref(false)
    const cartLoading = ref(false)

    const isAuthenticated = computed(() => authStore.isAuthenticated)

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }

    const loadFeaturedProducts = async () => {
      featuredLoading.value = true
      try {
        const response = await productAPI.getFeatured()
        featuredProducts.value = response.data.slice(0, 6) // Hiển thị 6 sản phẩm
      } catch (error) {
        console.error('Error loading featured products:', error)
      } finally {
        featuredLoading.value = false
      }
    }

    const loadLatestProducts = async () => {
      latestLoading.value = true
      try {
        const response = await productAPI.getLatest()
        latestProducts.value = response.data.slice(0, 6) // Hiển thị 6 sản phẩm
      } catch (error) {
        console.error('Error loading latest products:', error)
      } finally {
        latestLoading.value = false
      }
    }

    const addToCart = async (product) => {
      if (!isAuthenticated.value) {
        // Redirect to login
        return
      }

      cartLoading.value = true
      try {
        await cartStore.addItem(product.id, 1)
        // Show success message
      } catch (error) {
        console.error('Add to cart error:', error)
        // Show error message
      } finally {
        cartLoading.value = false
      }
    }

    onMounted(() => {
      loadFeaturedProducts()
      loadLatestProducts()
    })

    return {
      isAuthenticated,
      featuredProducts,
      latestProducts,
      featuredLoading,
      latestLoading,
      cartLoading,
      formatPrice,
      addToCart
    }
  }
}
</script>

<style scoped>
.hero {
  position: relative;
  min-height: 80vh;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
}

.planet {
  position: absolute;
  font-size: 60px;
  opacity: 0.3;
  animation: float 6s ease-in-out infinite;
}

.planet-1 {
  top: 20%;
  right: 10%;
  animation-delay: -2s;
}

.planet-2 {
  bottom: 30%;
  left: 15%;
  animation-delay: -4s;
  font-size: 40px;
}

.planet-3 {
  top: 60%;
  right: 30%;
  animation-delay: -1s;
  font-size: 35px;
}

.hero-content {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 24px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: 40px;
  line-height: 1.6;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-lg {
  padding: 16px 32px;
  font-size: 16px;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 48px;
  color: var(--text-accent);
}

.featured-products,
.latest-products {
  padding: 80px 0;
}

.latest-products {
  background: rgba(26, 26, 46, 0.3);
}

.product-image-container {
  position: relative;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: var(--nebula-gradient);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.product-badge.new {
  background: var(--aurora-gradient);
  color: var(--space-dark);
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 14px;
  opacity: 0.3;
}

.star.filled {
  opacity: 1;
}

.rating-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.product-actions {
  display: flex;
  gap: 12px;
}

.product-actions .btn {
  flex: 1;
  padding: 10px 16px;
  font-size: 14px;
}

.features {
  padding: 80px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-accent);
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-lg {
    width: 100%;
    max-width: 300px;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .planet {
    font-size: 40px;
  }
}
</style>