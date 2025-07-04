<template>
  <div class="categories-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <span class="categories-icon">🏷️</span>
          Danh mục sản phẩm
        </h1>
        <p class="page-subtitle">Khám phá các danh mục sản phẩm đa dạng của chúng tôi</p>
      </div>
    </div>

    <!-- Categories Content -->
    <div class="container">
      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <p>Đang tải danh mục...</p>
        </div>
      </div>

      <!-- Categories Grid -->
      <div v-else-if="categories.length > 0" class="categories-grid">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-card"
          @click="viewCategory(category.name)"
        >
          <div class="category-image category-icon-only">
            <div class="category-icon-large">{{ category.icon || '📦' }}</div>
            <div class="category-overlay"></div>
          </div>

          <div class="category-info">
            <h3 class="category-name">{{ category.name }}</h3>
            <p class="category-description">{{ category.description }}</p>

            <div class="category-stats">
              <div class="stat">
                <span class="stat-icon">📦</span>
                <span class="stat-value">{{ category.productCount || 0 }}</span>
                <span class="stat-label">sản phẩm</span>
              </div>
            </div>

            <div class="category-actions">
              <button class="btn btn-primary" @click.stop="viewCategory(category.name)">
                Xem sản phẩm
              </button>

              <div class="category-tags" v-if="category.tags && category.tags.length > 0">
                <span v-for="tag in category.tags.slice(0, 3)" :key="tag" class="tag">
                  {{ tag }}
                </span>
                <span v-if="category.tags.length > 3" class="tag more">
                  +{{ category.tags.length - 3 }}
                </span>
              </div>
            </div>
          </div>

          <!-- Popular Products Preview -->
          <div
            v-if="category.popularProducts && category.popularProducts.length > 0"
            class="popular-products"
          >
            <h4 class="popular-title">Sản phẩm nổi bật</h4>
            <div class="products-preview">
              <div
                v-for="product in category.popularProducts.slice(0, 3)"
                :key="product.id"
                class="product-preview"
                @click.stop="viewProduct(product.id)"
              >
                <img :src="product.imageUrl || '/placeholder-product.jpg'" :alt="product.name" />
                <div class="product-info">
                  <h5>{{ product.name }}</h5>
                  <p class="product-price">{{ formatCurrency(product.price) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No Categories -->
      <div v-else class="no-categories">
        <div class="no-categories-content">
          <div class="no-categories-icon">🏷️</div>
          <h3>Không có danh mục nào</h3>
          <p>Hiện tại chưa có danh mục sản phẩm nào được tạo.</p>
        </div>
      </div>

      <!-- Featured Categories Section -->
      <div v-if="featuredCategories.length > 0" class="featured-section">
        <h2 class="section-title">
          <span class="section-icon">⭐</span>
          Danh mục nổi bật
        </h2>

        <div class="featured-grid">
          <div
            v-for="category in featuredCategories"
            :key="category.id"
            class="featured-card"
            @click="viewCategory(category.name)"
          >
            <div class="featured-image">
              <img :src="category.imageUrl || '/placeholder-category.jpg'" :alt="category.name" />
              <div class="featured-badge">Nổi bật</div>
            </div>

            <div class="featured-info">
              <h3>{{ category.name }}</h3>
              <p>{{ category.description }}</p>
              <div class="featured-stats">
                <span>{{ category.productCount }} sản phẩm</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Category Statistics -->
      <div class="stats-section">
        <h2 class="section-title">
          <span class="section-icon">📊</span>
          Thống kê danh mục
        </h2>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">🏷️</div>
            <div class="stat-content">
              <h3>{{ totalCategories }}</h3>
              <p>Tổng số danh mục</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">📦</div>
            <div class="stat-content">
              <h3>{{ totalProducts }}</h3>
              <p>Tổng số sản phẩm</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">🔥</div>
            <div class="stat-content">
              <h3>{{ mostPopularCategory }}</h3>
              <p>Danh mục phổ biến nhất</p>
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
import { categoryAPI } from '@/services/api'

export default {
  name: 'Categories',
  setup() {
    const router = useRouter()

    // Reactive data
    const loading = ref(false)
    const error = ref(null)
    const categories = ref([])

    // Computed properties
    const featuredCategories = computed(() => {
      return categories.value.filter((cat) => cat.featured).slice(0, 4)
    })

    const totalCategories = computed(() => categories.value.length)

    const totalProducts = computed(() => {
      return categories.value.reduce((total, cat) => total + (cat.productCount || 0), 0)
    })

    const averageRating = computed(() => {
      const ratings = categories.value.map((cat) => cat.averageRating || 0)
      if (ratings.length === 0) return 0
      return (ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length).toFixed(1)
    })

    const mostPopularCategory = computed(() => {
      if (categories.value.length === 0) return 'N/A'
      const popular = categories.value.reduce((max, cat) =>
        (cat.productCount || 0) > (max.productCount || 0) ? cat : max
      )
      return popular.name
    })

    // Methods
    const loadCategories = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await categoryAPI.getAll()
        categories.value = response.data || []
      } catch (err) {
        error.value = 'Không thể tải danh mục sản phẩm'
        console.error('Load categories error:', err)
      } finally {
        loading.value = false
      }
    }

    const viewCategory = (slug) => {
      router.push(`/categories/${slug}`)
    }

    const viewProduct = (productId) => {
      router.push(`/products/${productId}`)
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-category.jpg'
    }

    // Lifecycle
    onMounted(() => {
      loadCategories()
    })

    return {
      loading,
      error,
      categories,
      featuredCategories,
      totalCategories,
      totalProducts,
      averageRating,
      mostPopularCategory,
      viewCategory,
      viewProduct,
      formatCurrency,
      handleImageError,
    }
  },
}
</script>

<style scoped>
.categories-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem 0;
  background: linear-gradient(135deg, var(--accent-blue) 0%, var(--accent-purple) 100%);
  color: white;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.categories-icon {
  font-size: 3rem;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 0;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.spinner {
  width: 40px;
  height: 40px;
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

/* Categories Grid */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 4rem;
}

.category-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-8px);
  border-color: var(--text-accent);
  box-shadow: 0 12px 40px rgba(0, 212, 255, 0.2);
}

.category-image.category-icon-only {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: rgba(0, 212, 255, 0.05);
  position: relative;
}

.category-icon-large {
  font-size: 5rem;
  color: var(--text-accent);
  text-shadow: 0 2px 8px rgba(0, 212, 255, 0.15);
  z-index: 2;
}

.category-image.category-icon-only .category-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 0;
  z-index: 1;
  transition: background 0.3s;
}

.category-card:hover .category-image.category-icon-only .category-overlay {
  background: rgba(0, 212, 255, 0.12);
}

.category-info {
  padding: 1.5rem;
}

.category-name {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.category-description {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.category-stats {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-icon {
  font-size: 1.1rem;
}

.stat-value {
  font-weight: 700;
  color: var(--text-accent);
  font-size: 1.1rem;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.category-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  background: var(--text-accent);
  color: #1a1a2e;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:hover {
  background: #00c4ef;
  transform: translateY(-2px);
}

.category-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  padding: 0.25rem 0.5rem;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.tag.more {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
}

/* Popular Products */
.popular-products {
  padding: 1.5rem;
  background: rgba(0, 212, 255, 0.05);
  border-top: 1px solid rgba(0, 212, 255, 0.1);
}

.popular-title {
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.products-preview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.product-preview {
  background: rgba(26, 26, 46, 0.8);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-preview:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.product-preview img {
  width: 100%;
  height: 80px;
  object-fit: cover;
}

.product-info {
  padding: 0.75rem;
}

.product-info h5 {
  font-size: 0.8rem;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  line-height: 1.3;
}

.product-price {
  font-size: 0.75rem;
  color: var(--text-accent);
  font-weight: 600;
}

/* No Categories */
.no-categories {
  text-align: center;
  padding: 4rem 0;
}

.no-categories-content {
  max-width: 400px;
  margin: 0 auto;
}

.no-categories-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-categories h3 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.no-categories p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Featured Section */
.featured-section {
  margin: 4rem 0;
}

.section-title {
  font-size: 2rem;
  color: var(--text-primary);
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.section-icon {
  font-size: 2rem;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.featured-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.featured-card:hover {
  transform: translateY(-4px);
  border-color: #ffd700;
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.2);
}

.featured-image {
  position: relative;
  width: 100%;
  height: 150px;
}

.featured-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ffd700;
  color: #1a1a2e;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
}

.featured-info {
  padding: 1rem;
}

.featured-info h3 {
  font-size: 1.1rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.featured-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.featured-stats {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

/* Stats Section */
.stats-section {
  margin: 4rem 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  border-color: var(--text-accent);
  transform: translateY(-2px);
}

.stat-card .stat-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.25rem;
}

.stat-content p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .categories-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .category-stats {
    flex-direction: column;
    gap: 1rem;
  }

  .category-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .products-preview {
    grid-template-columns: 1fr;
  }

  .featured-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>
