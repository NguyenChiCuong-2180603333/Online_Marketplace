<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <h1 class="hero-title">
              🌌 Chào mừng đến với <br>
              <span class="text-gradient">Cosmic Marketplace</span>
            </h1>
            <p class="hero-subtitle">
              Khám phá vũ trụ mua sắm với hàng nghìn sản phẩm tuyệt vời từ khắp thiên hà.
              Trải nghiệm mua sắm như chưa từng có!
            </p>
            <div class="hero-actions">
              <router-link to="/products" class="btn btn-primary btn-large">
                🚀 Khám phá ngay
              </router-link>
              <router-link to="/categories" class="btn btn-secondary btn-large">
                📂 Danh mục
              </router-link>
            </div>
          </div>
          <div class="hero-visual">
            <div class="floating-elements">
              <div class="planet planet-1">🪐</div>
              <div class="planet planet-2">🌍</div>
              <div class="planet planet-3">🌙</div>
              <div class="stars">
                <span class="star star-1">⭐</span>
                <span class="star star-2">✨</span>
                <span class="star star-3">🌟</span>
                <span class="star star-4">💫</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card space-card">
            <div class="stat-icon">🏪</div>
            <div class="stat-number">{{ stats.totalProducts.toLocaleString() }}</div>
            <div class="stat-label">Sản phẩm</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">👥</div>
            <div class="stat-number">{{ stats.totalUsers.toLocaleString() }}</div>
            <div class="stat-label">Người dùng</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">📦</div>
            <div class="stat-number">{{ stats.totalOrders.toLocaleString() }}</div>
            <div class="stat-label">Đơn hàng</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">⭐</div>
            <div class="stat-number">{{ stats.averageRating }}</div>
            <div class="stat-label">Đánh giá TB</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Products -->
    <section class="featured-section">
      <div class="container">
        <div class="section-header">
          <h2>🌟 Sản phẩm nổi bật</h2>
          <p>Những sản phẩm được yêu thích nhất trong vũ trụ</p>
          <router-link to="/products?filter=featured" class="view-all">Xem tất cả →</router-link>
        </div>
        
        <div class="products-slider" v-if="featuredProducts.length">
          <div class="products-grid">
            <div 
              v-for="product in featuredProducts" 
              :key="product.id" 
              class="product-card space-card"
              @click="viewProduct(product.id)"
            >
              <div class="product-image">
                <img :src="product.images?.[0] || '/placeholder-product.jpg'" :alt="product.name" />
                <div class="product-badges">
                  <span v-if="product.isNew" class="badge badge-new">Mới</span>
                  <span v-if="product.isBestSeller" class="badge badge-hot">Hot</span>
                  <span v-if="product.discount" class="badge badge-sale">-{{ product.discount }}%</span>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <div class="product-price">
                  <span class="current-price">{{ formatCurrency(product.price) }}</span>
                  <span v-if="product.originalPrice" class="original-price">
                    {{ formatCurrency(product.originalPrice) }}
                  </span>
                </div>
                <div class="product-rating">
                  <div class="stars">
                    <span v-for="i in 5" :key="i" class="star" :class="[i <= product.rating ? 'filled' : '']">⭐</span>
                  </div>
                  <span class="rating-count">({{ product.reviewCount }})</span>
                </div>
                <button @click.stop="addToCart(product)" class="btn btn-primary btn-sm">
                  🛒 Thêm vào giỏ
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="loading-products">
          <div class="loading-spinner">🔄</div>
          <p>Đang tải sản phẩm...</p>
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="categories-section">
      <div class="container">
        <div class="section-header">
          <h2>🗂️ Danh mục phổ biến</h2>
          <p>Khám phá các danh mục sản phẩm đa dạng</p>
        </div>
        
        <div class="categories-grid">
          <div 
            v-for="category in popularCategories" 
            :key="category.id"
            class="category-card space-card"
            @click="viewCategory(category.slug)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.productCount }} sản phẩm</p>
            <div class="category-overlay">
              <span class="view-category">Xem danh mục</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2>✨ Tại sao chọn Cosmic Marketplace?</h2>
          <p>Những ưu điểm vượt trội mà chúng tôi mang lại</p>
        </div>
        
        <div class="features-grid">
          <div class="feature-card space-card">
            <div class="feature-icon">🚀</div>
            <h3>Giao hàng siêu tốc</h3>
            <p>Giao hàng nhanh như ánh sáng, đảm bảo sản phẩm đến tay bạn an toàn</p>
          </div>
          <div class="feature-card space-card">
            <div class="feature-icon">🛡️</div>
            <h3>Bảo mật tuyệt đối</h3>
            <p>Hệ thống bảo mật cấp ngân hàng, thông tin cá nhân được bảo vệ tối đa</p>
          </div>
          <div class="feature-card space-card">
            <div class="feature-icon">💫</div>
            <h3>Trải nghiệm độc đáo</h3>
            <p>Giao diện hiện đại, dễ sử dụng với công nghệ AI hỗ trợ mua sắm thông minh</p>
          </div>
          <div class="feature-card space-card">
            <div class="feature-icon">🎁</div>
            <h3>Ưu đãi hấp dẫn</h3>
            <p>Chương trình loyalty với điểm thưởng, khuyến mãi và quà tặng liên tục</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Newsletter Section -->
    <section class="newsletter-section">
      <div class="container">
        <div class="newsletter-content space-card">
          <div class="newsletter-text">
            <h2>📧 Đăng ký nhận tin</h2>
            <p>Nhận thông báo về sản phẩm mới, ưu đãi đặc biệt và tin tức từ Cosmic Marketplace</p>
          </div>
          <div class="newsletter-form">
            <form @submit.prevent="subscribeNewsletter">
              <div class="input-group">
                <input 
                  v-model="newsletterEmail" 
                  type="email" 
                  placeholder="Nhập email của bạn..."
                  class="form-input"
                  required
                />
                <button type="submit" class="btn btn-primary" :disabled="subscribing">
                  {{ subscribing ? '📤 Đang gửi...' : '📤 Đăng ký' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { productAPI, categoryAPI } from '@/services/api'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()
    
    // Reactive data
    const featuredProducts = ref([])
    const popularCategories = ref([])
    const newsletterEmail = ref('')
    const subscribing = ref(false)
    const loading = ref(true)
    
    // Stats data
    const stats = ref({
      totalProducts: 12847,
      totalUsers: 48291,
      totalOrders: 156739,
      averageRating: 4.8
    })
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const viewProduct = (productId) => {
      router.push(`/products/${productId}`)
    }
    
    const viewCategory = (categorySlug) => {
      router.push(`/products?category=${categorySlug}`)
    }
    
    const addToCart = async (product) => {
      try {
        await cartStore.addItem(product.id, 1)
        // Show success notification
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        console.error('Error adding to cart:', error)
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng')
      }
    }
    
    const subscribeNewsletter = async () => {
      if (!newsletterEmail.value) return
      
      subscribing.value = true
      try {
        // Mock API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        alert('Đăng ký nhận tin thành công!')
        newsletterEmail.value = ''
      } catch (error) {
        console.error('Newsletter subscription error:', error)
        alert('Có lỗi xảy ra khi đăng ký')
      } finally {
        subscribing.value = false
      }
    }
    
    const loadFeaturedProducts = async () => {
      try {
        // Mock data - replace with real API call
        featuredProducts.value = [
          {
            id: 1,
            name: 'Smartphone Galaxy Cosmic',
            price: 15990000,
            originalPrice: 18990000,
            discount: 15,
            rating: 4.8,
            reviewCount: 342,
            images: ['/placeholder-product.jpg'],
            isNew: true,
            isBestSeller: true
          },
          {
            id: 2,
            name: 'Laptop Gaming Nebula',
            price: 25990000,
            rating: 4.7,
            reviewCount: 189,
            images: ['/placeholder-product.jpg'],
            isNew: false,
            isBestSeller: true
          },
          {
            id: 3,
            name: 'Tai nghe Wireless Stellar',
            price: 2990000,
            originalPrice: 3990000,
            discount: 25,
            rating: 4.9,
            reviewCount: 567,
            images: ['/placeholder-product.jpg'],
            isNew: true,
            isBestSeller: false
          },
          {
            id: 4,
            name: 'Đồng hồ thông minh Aurora',
            price: 5990000,
            rating: 4.6,
            reviewCount: 234,
            images: ['/placeholder-product.jpg'],
            isNew: false,
            isBestSeller: false
          }
        ]
      } catch (error) {
        console.error('Error loading featured products:', error)
      }
    }
    
    const loadPopularCategories = async () => {
      try {
        // Mock data - replace with real API call
        popularCategories.value = [
          { id: 1, name: 'Điện tử', slug: 'electronics', icon: '📱', productCount: 1247 },
          { id: 2, name: 'Thời trang', slug: 'fashion', icon: '👗', productCount: 892 },
          { id: 3, name: 'Gia đình', slug: 'home', icon: '🏠', productCount: 654 },
          { id: 4, name: 'Sách', slug: 'books', icon: '📚', productCount: 423 },
          { id: 5, name: 'Thể thao', slug: 'sports', icon: '⚽', productCount: 567 },
          { id: 6, name: 'Làm đẹp', slug: 'beauty', icon: '💄', productCount: 389 }
        ]
      } catch (error) {
        console.error('Error loading categories:', error)
      }
    }
    
    // Lifecycle
    onMounted(async () => {
      try {
        await Promise.all([
          loadFeaturedProducts(),
          loadPopularCategories()
        ])
      } finally {
        loading.value = false
      }
    })
    
    return {
      featuredProducts,
      popularCategories,
      newsletterEmail,
      subscribing,
      loading,
      stats,
      formatCurrency,
      viewProduct,
      viewCategory,
      addToCart,
      subscribeNewsletter
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

/* Hero Section */
.hero {
  padding: 100px 0;
  position: relative;
  overflow: hidden;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.text-gradient {
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.btn-large {
  padding: 16px 32px;
  font-size: 1.1rem;
  border-radius: 50px;
}

/* Hero Visual */
.hero-visual {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-elements {
  position: relative;
  width: 100%;
  height: 100%;
}

.planet {
  position: absolute;
  font-size: 4rem;
  animation: float 6s ease-in-out infinite;
}

.planet-1 {
  top: 10%;
  left: 20%;
  animation-delay: 0s;
}

.planet-2 {
  top: 50%;
  right: 10%;
  animation-delay: 2s;
}

.planet-3 {
  bottom: 20%;
  left: 50%;
  animation-delay: 4s;
}

.star {
  position: absolute;
  font-size: 1.5rem;
  animation: twinkle 3s ease-in-out infinite;
}

.star-1 {
  top: 15%;
  right: 30%;
  animation-delay: 0.5s;
}

.star-2 {
  top: 70%;
  left: 10%;
  animation-delay: 1.5s;
}

.star-3 {
  top: 30%;
  left: 70%;
  animation-delay: 2.5s;
}

.star-4 {
  bottom: 10%;
  right: 50%;
  animation-delay: 3.5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

@keyframes twinkle {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* Stats Section */
.stats-section {
  padding: 80px 0;
  background: rgba(26, 26, 46, 0.5);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
}

.stat-card {
  text-align: center;
  padding: 40px 20px;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 10px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Section Headers */
.section-header {
  text-align: center;
  margin-bottom: 60px;
  position: relative;
}

.section-header h2 {
  font-size: 2.5rem;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.section-header p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.view-all {
  color: var(--text-accent);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.view-all:hover {
  color: var(--text-primary);
}

/* Featured Products */
.featured-section {
  padding: 80px 0;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.product-card {
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-new {
  background: var(--text-success);
  color: white;
}

.badge-hot {
  background: var(--text-danger);
  color: white;
}

.badge-sale {
  background: var(--text-warning);
  color: var(--space-black);
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  line-height: 1.3;
}

.product-price {
  margin-bottom: 12px;
}

.current-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-accent);
}

.original-price {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-decoration: line-through;
  margin-left: 8px;
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
  font-size: 0.8rem;
  opacity: 0.3;
}

.star.filled {
  opacity: 1;
}

.rating-count {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.btn-sm {
  padding: 8px 16px;
  font-size: 0.9rem;
  width: 100%;
}

/* Categories Section */
.categories-section {
  padding: 80px 0;
  background: rgba(26, 26, 46, 0.3);
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.category-card {
  text-align: center;
  padding: 40px 20px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.category-card:hover {
  transform: translateY(-5px);
}

.category-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.category-card h3 {
  font-size: 1.3rem;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.category-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.category-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 212, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-card:hover .category-overlay {
  opacity: 1;
}

.view-category {
  color: var(--text-accent);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Features Section */
.features-section {
  padding: 80px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 3.5rem;
  margin-bottom: 24px;
}

.feature-card h3 {
  font-size: 1.4rem;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Newsletter Section */
.newsletter-section {
  padding: 80px 0;
  background: rgba(26, 26, 46, 0.5);
}

.newsletter-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: center;
  padding: 60px;
  max-width: 1000px;
  margin: 0 auto;
}

.newsletter-text h2 {
  font-size: 2rem;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.newsletter-text p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.input-group {
  display: flex;
  gap: 12px;
}

.input-group .form-input {
  flex: 1;
  border-radius: 50px;
  padding: 16px 24px;
}

.input-group .btn {
  border-radius: 50px;
  padding: 16px 24px;
  white-space: nowrap;
}

/* Loading States */
.loading-products {
  text-align: center;
  padding: 60px 0;
}

.loading-spinner {
  font-size: 3rem;
  margin-bottom: 20px;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .newsletter-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 60px 0;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .section-header h2 {
    font-size: 2rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .input-group {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .newsletter-content {
    padding: 40px 20px;
  }
}
</style>