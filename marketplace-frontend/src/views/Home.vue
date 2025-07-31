<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <!-- Hero Text -->
          <div class="hero-text">
            <h1 class="hero-title">
              🌌 Khám phá
              <span class="gradient-text">Vũ trụ mua sắm</span>
            </h1>
            <p class="hero-subtitle">
              Trải nghiệm mua sắm thông minh. Khám phá hàng ngàn sản phẩm chất lượng từ khắp nơi
              trên thế giới.
            </p>

            <!-- Hero Search -->
            <div class="hero-search">
              <div class="search-container">
                <input
                  v-model="heroSearchQuery"
                  @keyup.enter="performHeroSearch"
                  type="text"
                  placeholder="Tìm kiếm sản phẩm mơ ước của bạn..."
                  class="hero-search-input"
                />
                <button @click="performHeroSearch" class="hero-search-btn">🔍 Tìm kiếm</button>
              </div>

              <!-- Quick search suggestions -->
              <div class="quick-suggestions" v-if="quickSuggestions.length > 0">
                <span class="suggestions-label">Gợi ý:</span>
                <div class="suggestions-list">
                  <button
                    v-for="suggestion in quickSuggestions"
                    :key="suggestion"
                    @click="searchSuggestion(suggestion)"
                    class="suggestion-tag"
                  >
                    {{ suggestion }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Hero Visual -->
          <div class="hero-visual">
            <div class="cosmic-animation">
              <div class="planet main-planet">🌍</div>
              <div class="orbit orbit-1">
                <div class="satellite">🛰️</div>
              </div>
              <div class="orbit orbit-2">
                <div class="satellite">🚀</div>
              </div>
              <div class="orbit orbit-3">
                <div class="satellite">⭐</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="recommendations-section" v-if="authStore.isAuthenticated">
      <div class="container">
        <RecommendedProducts
          :limit="12"
          :enable-load-more="true"
          :auto-refresh-interval="30"
          container-class="home-recommendations"
          @product-click="handleRecommendationClick"
          @add-to-cart="handleAddToCart"
          @add-to-wishlist="handleAddToWishlist"
        />
      </div>
    </section>

    <!-- Trending Section for Non-authenticated Users -->
    <!-- <section class="trending-section" v-else>
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="trending-icon">🔥</span>
            Sản phẩm Hot nhất
          </h2>
          <p class="section-subtitle">Những sản phẩm được yêu thích nhất hiện tại</p>
        </div>

        <TrendingProducts
          :limit="12"
          @product-click="handleProductClick"
          @add-to-cart="handleAddToCart"
        />
      </div>
    </section> -->

    <!-- Featured Products Section -->
    <section class="featured-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="featured-icon">⭐</span>
            Sản phẩm nổi bật
          </h2>
          <p class="section-subtitle">Được tuyển chọn đặc biệt bởi đội ngũ chuyên gia</p>
        </div>

        <!-- Loading State -->
        <div v-if="loadingFeatured" class="loading-container">
          <div class="loading-grid">
            <div v-for="n in 8" :key="n" class="loading-card">
              <div class="loading-image"></div>
              <div class="loading-content">
                <div class="loading-title"></div>
                <div class="loading-price"></div>
                <div class="loading-rating"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Featured Products Grid -->
        <div v-else class="products-grid">
          <div
            v-for="product in featuredProducts"
            :key="product.id"
            class="product-card"
            @click="handleProductClick(product)"
          >
            <!-- Product badges -->
            <div class="product-badges">
              <span v-if="product.isNew" class="badge badge-new">Mới</span>
              <span v-if="product.isHot" class="badge badge-hot">Hot</span>
              <span v-if="product.discount > 0" class="badge badge-sale">
                -{{ product.discount }}%
              </span>
            </div>

            <!-- Product image -->
            <div class="product-image-container">
              <img
                :src="
                  product.images && product.images.length > 0
                    ? product.images[0]
                    : '/api/placeholder/product'
                "
                :alt="product.name"
                class="product-image"
                @error="handleImageError"
                loading="lazy"
              />

              <!-- Quick actions overlay -->
              <div class="quick-actions">
                <button
                  @click.stop="toggleWishlist(product)"
                  class="quick-action-btn"
                  :class="{ active: isInWishlist(product.id) }"
                  title="Yêu thích"
                >
                  {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
                </button>

                <button
                  @click.stop="handleAddToCart(product)"
                  class="quick-action-btn"
                  title="Thêm vào giỏ hàng"
                >
                  🛒
                </button>
              </div>
            </div>

            <!-- Product info -->
            <div class="product-info">
              <div class="product-category">{{ product.category }}</div>
              <h3 class="product-name">{{ truncate(product.name, 50) }}</h3>

              <div class="product-price">
                <span class="current-price">{{ formatPrice(product.price) }}</span>
                <span
                  v-if="product.originalPrice && product.originalPrice > product.price"
                  class="original-price"
                >
                  {{ formatPrice(product.originalPrice) }}
                </span>
              </div>

              <div class="product-rating" v-if="product.rating">
                <div class="stars">
                  <span
                    v-for="star in 5"
                    :key="star"
                    class="star"
                    :class="{ filled: star <= product.rating }"
                  >
                    ⭐
                  </span>
                </div>
                <span class="rating-count">({{ product.reviewCount || 0 }})</span>
              </div>

              <button
                @click.stop="handleAddToCart(product)"
                class="btn btn-primary btn-sm"
                style="margin-top: auto"
              >
                🛒 Thêm vào giỏ
              </button>
            </div>
          </div>
        </div>

        <!-- View All Button -->
        <div class="section-footer">
          <router-link to="/products" class="btn btn-outline btn-lg">
            Xem tất cả sản phẩm →
          </router-link>
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="categories-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="categories-icon">🏪</span>
            Danh mục phổ biến
          </h2>
          <p class="section-subtitle">Khám phá các danh mục sản phẩm đa dạng</p>
        </div>
        <div v-if="categoriesLoading" class="loading-container">Đang tải danh mục...</div>
        <div v-else-if="categoriesError" class="error-message">{{ categoriesError }}</div>
        <div v-else class="categories-grid">
          <div
            v-for="category in popularCategories"
            :key="category.id"
            class="category-card"
            @click="navigateToCategory(category)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.productCount }} sản phẩm</p>
            <div class="category-overlay">
              <span class="view-category">Khám phá ngay</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useWishlistStore } from '@/stores/wishlist'
import { productAPI, orderAPI, categoryAPI } from '@/services/api'
import RecommendedProducts from '@/components/RecommendedProducts.vue'
import TrendingProducts from '@/components/TrendingProducts.vue'
import recommendationService from '@/services/recommendationService'

export default {
  name: 'Home',
  components: {
    RecommendedProducts,
    TrendingProducts,
  },

  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const userStore = useUserStore()
    const cartStore = useCartStore()
    const wishlistStore = useWishlistStore()

    // Reactive state
    const heroSearchQuery = ref('')
    const newsletterEmail = ref('')
    const subscribing = ref(false)
    const loadingFeatured = ref(false)
    const featuredProducts = ref([])

    // Real API state
    const categoriesLoading = ref(true)
    const categoriesError = ref('')
    const popularCategories = ref([])

    const quickSuggestions = ref(['iPhone 15', 'MacBook Air', 'AirPods Pro', 'iPad', 'Apple Watch'])

    // Load featured products
    const loadFeaturedProducts = async () => {
      loadingFeatured.value = true

      try {
        const response = await productAPI.getFeatured()
        featuredProducts.value = response.data || []

        // Track featured products loaded
        recommendationService.trackInteraction(null, 'FEATURED_PRODUCTS_LOADED', {
          count: featuredProducts.value.length,
          source: 'home_page',
        })
      } catch (error) {
        console.error('Error loading featured products:', error)
        featuredProducts.value = []
      } finally {
        loadingFeatured.value = false
      }
    }

    // Handle hero search
    const performHeroSearch = () => {
      if (heroSearchQuery.value.trim()) {
        // Track search
        recommendationService.trackSearch(heroSearchQuery.value.trim(), 0)

        // Navigate to search results
        router.push({
          name: 'Products',
          query: { search: heroSearchQuery.value.trim() },
        })
      }
    }

    // Handle search suggestion click
    const searchSuggestion = (suggestion) => {
      heroSearchQuery.value = suggestion
      performHeroSearch()
    }

    // Handle recommendation click
    const handleRecommendationClick = (data) => {
      const { product, index } = data

      // Additional tracking for recommendations from home page
      recommendationService.trackInteraction(product.id, 'RECOMMENDATION_CLICK', {
        source: 'home_page',
        position: index,
        section: 'personal_recommendations',
      })
    }

    const handleProductClick = (product) => {
      recommendationService.trackView(product.id, 'featured_products')

      router.push(`/products/${product.id}`)
    }

    const handleAddToCart = async (product) => {
      try {
        if (!authStore.isAuthenticated) {
          if (
            confirm('Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng. Chuyển đến trang đăng nhập?')
          ) {
            router.push('/login')
          }
          return
        }

        await recommendationService.trackAddToCart(product.id, 1)

        await cartStore.addItem(product.id, 1)

        console.log('✅ Added to cart:', product.name)
        alert(`Đã thêm "${product.name}" vào giỏ hàng!`)
      } catch (error) {
        console.error('❌ Error adding to cart:', error)
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng. Vui lòng thử lại!')
      }
    }

    const handleAddToWishlist = async (product) => {
      try {
        if (!authStore.isAuthenticated) {
          if (confirm('Bạn cần đăng nhập để thêm vào wishlist. Chuyển đến trang đăng nhập?')) {
            router.push('/login')
          }
          return
        }

        await recommendationService.trackInteraction(product.id, 'ADD_TO_WISHLIST', {
          source: 'home_page',
        })

        console.log('✅ Added to wishlist:', product.name)
        alert(`Đã thêm "${product.name}" vào danh sách yêu thích!`)
      } catch (error) {
        console.error('❌ Error adding to wishlist:', error)
        alert('Có lỗi xảy ra khi thêm vào wishlist.')
      }
    }

    // Toggle wishlist
    const toggleWishlist = (product) => {
      wishlistStore.toggleWishlist(product)
    }

    // Navigate to category
    const navigateToCategory = (category) => {
      // Track category click
      recommendationService.trackInteraction(null, 'CATEGORY_CLICK', {
        categoryId: category.id,
        categoryName: category.name,
        source: 'home_page',
      })
      // Chuyển sang dùng tên danh mục (name) thay vì id
      router.push(`/categories/${category.name}`)
    }

    // Newsletter subscription
    const subscribeNewsletter = async () => {
      subscribing.value = true

      try {
        // TODO: Add actual newsletter subscription logic
        await new Promise((resolve) => setTimeout(resolve, 1000)) // Mock delay

        // Track newsletter subscription
        recommendationService.trackInteraction(null, 'NEWSLETTER_SUBSCRIBE', {
          email: newsletterEmail.value,
          source: 'home_page',
        })

        // Reset form
        newsletterEmail.value = ''

        // Show success message
        alert('Đăng ký thành công! Cảm ơn bạn đã quan tâm.')
      } catch (error) {
        console.error('Error subscribing to newsletter:', error)
        alert('Có lỗi xảy ra. Vui lòng thử lại sau.')
      } finally {
        subscribing.value = false
      }
    }

    // Load categories from real API
    const loadCategories = async () => {
      categoriesLoading.value = true
      categoriesError.value = ''
      try {
        const res = await categoryAPI.getAll()
        // Map categories to match UI
        popularCategories.value = (res.data || []).map((cat) => ({
          id: cat.id || cat._id,
          name: cat.name,
          icon: cat.icon || '🏪',
          productCount: cat.productCount || cat.count || 0,
        }))
      } catch (err) {
        categoriesError.value = 'Không thể tải danh mục.'
      } finally {
        categoriesLoading.value = false
      }
    }

    // Helper functions
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(price)
    }

    const truncate = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.slice(0, length) + '...' : text
    }

    const handleImageError = (event) => {
      event.target.src = '/api/placeholder/product'
    }

    const isInWishlist = (productId) => {
      return wishlistStore.isInWishlist(productId)
    }

    // Lifecycle
    onMounted(() => {
      loadFeaturedProducts()
      loadCategories()

      // Track home page view
      recommendationService.trackInteraction(null, 'HOME_PAGE_VIEW', {
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
      })
    })

    return {
      // Stores
      authStore,
      userStore,

      // State
      heroSearchQuery,
      newsletterEmail,
      subscribing,
      loadingFeatured,
      featuredProducts,
      categoriesLoading,
      categoriesError,
      popularCategories,
      quickSuggestions,

      // Methods
      performHeroSearch,
      searchSuggestion,
      handleRecommendationClick,
      handleProductClick,
      handleAddToCart,
      handleAddToWishlist,
      toggleWishlist,
      navigateToCategory,
      subscribeNewsletter,

      // Helpers
      formatPrice,
      truncate,
      handleImageError,
      isInWishlist,
    }
  },
}
</script>

<style scoped>
/* Hero Section */
.hero-section {
  background: var(--space-gradient);
  padding: 4rem 0 6rem;
  position: relative;
  overflow: hidden;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  min-height: 500px;
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.1;
  color: var(--text-primary);
}

.gradient-text {
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.hero-search {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-container {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 25px;
  padding: 0.5rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.hero-search-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 1rem;
  padding: 0.75rem 1.5rem;
}

.hero-search-input::placeholder {
  color: var(--text-secondary);
}

.hero-search-input:focus {
  outline: none;
}

.hero-search-btn {
  background: var(--accent-gradient);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.hero-search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.3);
}

.quick-suggestions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.suggestions-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.suggestions-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.suggestion-tag {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
  border: 1px solid rgba(0, 212, 255, 0.3);
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-tag:hover {
  background: var(--text-accent);
  color: white;
  transform: translateY(-1px);
}

/* Hero Visual */
.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.cosmic-animation {
  position: relative;
  width: 300px;
  height: 300px;
}

.main-planet {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 4rem;
  animation: rotate 20s linear infinite;
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.orbit-1 {
  width: 120px;
  height: 120px;
  animation: rotate 10s linear infinite;
}

.orbit-2 {
  width: 180px;
  height: 180px;
  animation: rotate 15s linear infinite reverse;
}

.orbit-3 {
  width: 240px;
  height: 240px;
  animation: rotate 25s linear infinite;
}

.satellite {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 1.5rem;
}

@keyframes rotate {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

/* Sections */
.recommendations-section,
.trending-section,
.featured-section,
.categories-section,
.features-section {
  padding: 4rem 0;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

/* Loading States */
.loading-container {
  padding: 2rem 0;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.loading-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(0, 212, 255, 0.1);
}

.loading-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(
    90deg,
    rgba(0, 212, 255, 0.1) 25%,
    rgba(0, 212, 255, 0.2) 50%,
    rgba(0, 212, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.loading-content {
  padding: 1rem;
}

.loading-title,
.loading-price,
.loading-rating {
  height: 1rem;
  background: linear-gradient(
    90deg,
    rgba(0, 212, 255, 0.1) 25%,
    rgba(0, 212, 255, 0.2) 50%,
    rgba(0, 212, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 0.75rem;
}

.loading-title {
  width: 80%;
}
.loading-price {
  width: 60%;
}
.loading-rating {
  width: 70%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  align-items: start;
}

.product-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  padding: 0.5rem 0.5rem 1rem 0.5rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 450px;
}

.product-card:hover {
  transform: translateY(-8px);
  border-color: var(--text-accent);
  box-shadow: 0 20px 40px rgba(0, 212, 255, 0.2);
}

.product-badges {
  position: absolute;
  top: 1rem;
  left: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  z-index: 10;
}

.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
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

.product-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #fff;
  border-radius: 10px;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.quick-actions {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .quick-actions {
  opacity: 1;
}

.quick-action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.quick-action-btn:hover,
.quick-action-btn.active {
  background: var(--text-accent);
  color: white;
  transform: scale(1.1);
}

.product-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-category {
  color: var(--text-accent);
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-wrap: break-word;
  min-height: 2.6rem;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  min-height: 1.5rem;
}

.current-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-accent);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.original-price {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  min-height: 1.2rem;
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
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  width: 100%;
}

/* Categories Grid */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.category-card {
  text-align: center;
  padding: 3rem 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.category-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent);
  box-shadow: 0 15px 30px rgba(0, 212, 255, 0.2);
}

.category-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
}

.category-card h3 {
  font-size: 1.3rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
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

/* Features Grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  text-align: center;
  padding: 2.5rem 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
}

.feature-card h3 {
  font-size: 1.2rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Newsletter Section */
.newsletter-section {
  background: rgba(0, 212, 255, 0.1);
  padding: 4rem 0;
}

.newsletter-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.newsletter-content h2 {
  font-size: 2rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.newsletter-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.newsletter-form {
  display: flex;
  gap: 1rem;
  max-width: 400px;
  margin: 0 auto;
}

.newsletter-input {
  flex: 1;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
}

.newsletter-input::placeholder {
  color: var(--text-secondary);
}

.newsletter-btn {
  background: var(--accent-gradient);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.newsletter-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.3);
}

.newsletter-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.section-footer {
  text-align: center;
  margin-top: 3rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 3rem;
    text-align: center;
  }

  .hero-title {
    font-size: 3rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 2rem 0 4rem;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
  }

  .product-card {
    min-height: 400px;
  }

  .newsletter-form {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }

  .section-title {
    font-size: 1.5rem;
    flex-direction: column;
    gap: 0.25rem;
  }

  .products-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .product-card {
    min-height: 350px;
  }

  .categories-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }

  .cosmic-animation {
    width: 200px;
    height: 200px;
  }

  .main-planet {
    font-size: 3rem;
  }
}
</style>
