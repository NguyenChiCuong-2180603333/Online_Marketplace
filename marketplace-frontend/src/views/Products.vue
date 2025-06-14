<template>
  <div class="products-page">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <h1 class="page-title">🌟 Khám phá vũ trụ sản phẩm</h1>
        <p class="page-subtitle">Hàng nghìn sản phẩm tuyệt vời đang chờ bạn khám phá</p>
      </div>

      <!-- Filters & Search -->
      <div class="filters-section space-card">
        <div class="search-container">
          <div class="search-box">
            <input
              v-model="searchTerm"
              @input="handleSearch"
              type="text"
              class="form-input search-input"
              placeholder="🔍 Tìm kiếm sản phẩm..."
            />
            <button @click="clearSearch" v-if="searchTerm" class="clear-search">✕</button>
          </div>
        </div>

        <div class="filter-controls">
          <div class="filter-group">
            <label class="filter-label">Danh mục:</label>
            <select v-model="selectedCategory" @change="handleFilter" class="form-input filter-select">
              <option value="">Tất cả danh mục</option>
              <option v-for="category in categories" :key="category.id" :value="category.name">
                {{ category.name }}
              </option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label">Giá:</label>
            <select v-model="selectedPriceRange" @change="handleFilter" class="form-input filter-select">
              <option value="">Tất cả mức giá</option>
              <option value="0-500000">Dưới 500.000₫</option>
              <option value="500000-1000000">500.000₫ - 1.000.000₫</option>
              <option value="1000000-5000000">1.000.000₫ - 5.000.000₫</option>
              <option value="5000000-999999999">Trên 5.000.000₫</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label">Sắp xếp:</label>
            <select v-model="sortBy" @change="handleSort" class="form-input filter-select">
              <option value="newest">Mới nhất</option>
              <option value="price-asc">Giá thấp → cao</option>
              <option value="price-desc">Giá cao → thấp</option>
              <option value="rating">Đánh giá cao</option>
              <option value="popular">Phổ biến</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Results Summary -->
      <div class="results-summary" v-if="!loading">
        <p>
          <span class="text-accent">{{ filteredProducts.length }}</span> sản phẩm
          <span v-if="searchTerm"> cho "<strong>{{ searchTerm }}</strong>"</span>
          <span v-if="selectedCategory"> trong danh mục "<strong>{{ selectedCategory }}</strong>"</span>
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading"></div>
        <p class="mt-2">Đang tải sản phẩm...</p>
      </div>

      <!-- Products Grid -->
      <div v-else-if="filteredProducts.length > 0" class="product-grid">
        <div v-for="product in paginatedProducts" :key="product.id" class="product-card">
          <div class="product-image-container">
            <img 
              :src="product.images?.[0] || '/placeholder-product.jpg'" 
              :alt="product.name"
              class="product-image"
              @error="handleImageError"
            />
            <div class="product-badges">
              <span v-if="isNewProduct(product)" class="product-badge new">🆕 Mới</span>
              <span v-if="product.averageRating >= 4.5" class="product-badge hot">🔥 Hot</span>
            </div>
            <div class="product-overlay">
              <button @click="quickView(product)" class="quick-view-btn">👁️ Xem nhanh</button>
            </div>
          </div>
          
          <div class="product-content">
            <div class="product-category">{{ product.category }}</div>
            <h3 class="product-title">{{ product.name }}</h3>
            <p class="product-price">{{ formatPrice(product.price) }}</p>
            
            <div class="product-rating" v-if="product.averageRating > 0">
              <div class="stars">
                <span v-for="i in 5" :key="i" :class="['star', i <= product.averageRating ? 'filled' : '']">⭐</span>
              </div>
              <span class="rating-text">({{ product.reviewCount }})</span>
            </div>
            
            <p class="product-description">{{ truncateText(product.description, 80) }}</p>
            
            <div class="product-actions">
              <router-link :to="`/products/${product.id}`" class="btn btn-secondary">
                📋 Chi tiết
              </router-link>
              <button 
                @click="addToCart(product)" 
                class="btn btn-primary" 
                :disabled="cartLoading"
                :class="{ 'loading': cartLoading }"
              >
                🛒 Thêm vào giỏ
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div v-else class="no-results">
        <div class="no-results-content space-card">
          <div class="no-results-icon">🔍</div>
          <h3>Không tìm thấy sản phẩm</h3>
          <p>Thử tìm kiếm với từ khóa khác hoặc điều chỉnh bộ lọc</p>
          <button @click="clearAllFilters" class="btn btn-secondary">🔄 Xóa bộ lọc</button>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination-container">
        <div class="pagination">
          <button 
            @click="goToPage(currentPage - 1)" 
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            ← Trước
          </button>
          
          <span class="pagination-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            Sau →
          </button>
        </div>
      </div>
    </div>

    <!-- Quick View Modal -->
    <div v-if="showQuickView" class="modal-overlay" @click="closeQuickView">
      <div class="modal-content space-card" @click.stop>
        <button @click="closeQuickView" class="modal-close">✕</button>
        <div v-if="selectedProduct" class="quick-view-content">
          <div class="quick-view-image">
            <img :src="selectedProduct.images?.[0]" :alt="selectedProduct.name" />
          </div>
          <div class="quick-view-info">
            <h3>{{ selectedProduct.name }}</h3>
            <p class="price">{{ formatPrice(selectedProduct.price) }}</p>
            <p class="description">{{ selectedProduct.description }}</p>
            <div class="quick-view-actions">
              <router-link :to="`/products/${selectedProduct.id}`" class="btn btn-secondary">
                Xem chi tiết
              </router-link>
              <button @click="addToCart(selectedProduct)" class="btn btn-primary">
                Thêm vào giỏ
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { productAPI, categoryAPI } from '@/services/api'

export default {
  name: 'Products',
  setup() {
    const cartStore = useCartStore()
    const authStore = useAuthStore()
    
    // Reactive data
    const products = ref([])
    const categories = ref([])
    const loading = ref(false)
    const cartLoading = ref(false)
    const searchTerm = ref('')
    const selectedCategory = ref('')
    const selectedPriceRange = ref('')
    const sortBy = ref('newest')
    const currentPage = ref(1)
    const itemsPerPage = 12
    
    // Quick view modal
    const showQuickView = ref(false)
    const selectedProduct = ref(null)

    // Computed properties
    const filteredProducts = computed(() => {
      let filtered = [...products.value]

      // Search filter
      if (searchTerm.value) {
        const term = searchTerm.value.toLowerCase()
        filtered = filtered.filter(product => 
          product.name.toLowerCase().includes(term) ||
          product.description.toLowerCase().includes(term)
        )
      }

      // Category filter
      if (selectedCategory.value) {
        filtered = filtered.filter(product => product.category === selectedCategory.value)
      }

      // Price range filter
      if (selectedPriceRange.value) {
        const [min, max] = selectedPriceRange.value.split('-').map(Number)
        filtered = filtered.filter(product => 
          product.price >= min && product.price <= max
        )
      }

      // Sort
      switch (sortBy.value) {
        case 'price-asc':
          filtered.sort((a, b) => a.price - b.price)
          break
        case 'price-desc':
          filtered.sort((a, b) => b.price - a.price)
          break
        case 'rating':
          filtered.sort((a, b) => b.averageRating - a.averageRating)
          break
        case 'popular':
          filtered.sort((a, b) => b.reviewCount - a.reviewCount)
          break
        case 'newest':
        default:
          filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }

      return filtered
    })

    const totalPages = computed(() => 
      Math.ceil(filteredProducts.value.length / itemsPerPage)
    )

    const paginatedProducts = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return filteredProducts.value.slice(start, end)
    })

    // Methods
    const loadProducts = async () => {
      loading.value = true
      try {
        const response = await productAPI.getAll()
        products.value = response.data
      } catch (error) {
        console.error('Error loading products:', error)
      } finally {
        loading.value = false
      }
    }

    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getAll()
        categories.value = response.data
      } catch (error) {
        console.error('Error loading categories:', error)
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
    }

    const handleFilter = () => {
      currentPage.value = 1
    }

    const handleSort = () => {
      currentPage.value = 1
    }

    const clearSearch = () => {
      searchTerm.value = ''
      currentPage.value = 1
    }

    const clearAllFilters = () => {
      searchTerm.value = ''
      selectedCategory.value = ''
      selectedPriceRange.value = ''
      currentPage.value = 1
    }

    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    const isNewProduct = (product) => {
      const createdDate = new Date(product.createdAt)
      const now = new Date()
      const diffDays = Math.floor((now - createdDate) / (1000 * 60 * 60 * 24))
      return diffDays <= 7 // Products created within 7 days
    }

    const addToCart = async (product) => {
      if (!authStore.isAuthenticated) {
        // Redirect to login
        return
      }

      cartLoading.value = true
      try {
        await cartStore.addItem(product.id, 1)
        // Show success message (you can add a toast notification here)
      } catch (error) {
        console.error('Add to cart error:', error)
        // Show error message
      } finally {
        cartLoading.value = false
      }
    }

    const quickView = (product) => {
      selectedProduct.value = product
      showQuickView.value = true
    }

    const closeQuickView = () => {
      showQuickView.value = false
      selectedProduct.value = null
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    // Watch for URL params
    watch(() => currentPage.value, () => {
      // You can add URL update logic here
    })

    onMounted(() => {
      loadProducts()
      loadCategories()
    })

    return {
      products,
      categories,
      loading,
      cartLoading,
      searchTerm,
      selectedCategory,
      selectedPriceRange,
      sortBy,
      currentPage,
      showQuickView,
      selectedProduct,
      filteredProducts,
      paginatedProducts,
      totalPages,
      handleSearch,
      handleFilter,
      handleSort,
      clearSearch,
      clearAllFilters,
      goToPage,
      formatPrice,
      truncateText,
      isNewProduct,
      addToCart,
      quickView,
      closeQuickView,
      handleImageError
    }
  }
}
</script>

<style scoped>
.products-page {
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

.filters-section {
  margin-bottom: 32px;
  padding: 24px;
}

.search-container {
  margin-bottom: 24px;
}

.search-box {
  position: relative;
  max-width: 500px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
  padding-right: 50px;
}

.clear-search {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 18px;
}

.filter-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.filter-select {
  padding: 10px 12px;
}

.results-summary {
  margin-bottom: 24px;
  text-align: center;
  color: var(--text-secondary);
}

.loading-section {
  text-align: center;
  padding: 60px 0;
}

.product-image-container {
  position: relative;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
  height: 250px;
}

.product-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-badge.new {
  background: var(--aurora-gradient);
  color: var(--space-dark);
}

.product-badge.hot {
  background: var(--nebula-gradient);
  color: white;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.quick-view-btn {
  background: rgba(0, 212, 255, 0.9);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.quick-view-btn:hover {
  background: var(--text-accent);
  transform: translateY(-2px);
}

.product-category {
  font-size: 12px;
  color: var(--text-accent);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.no-results {
  padding: 60px 0;
}

.no-results-content {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
  padding: 40px;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  opacity: 0.5;
}

.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 30px;
  background: rgba(26, 26, 46, 0.7);
  border-radius: 25px;
  backdrop-filter: blur(10px);
}

.pagination-btn {
  background: var(--text-accent);
  color: var(--space-dark);
  border: none;
  padding: 8px 16px;
  border-radius: 15px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: var(--neon-glow);
}

.pagination-info {
  color: var(--text-secondary);
  font-weight: 500;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  z-index: 1001;
}

.quick-view-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  padding: 30px;
}

.quick-view-image img {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 12px;
}

.quick-view-info h3 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: var(--text-accent);
}

.quick-view-info .price {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-accent);
  margin-bottom: 16px;
}

.quick-view-info .description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 24px;
}

.quick-view-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .filter-controls {
    grid-template-columns: 1fr;
  }
  
  .quick-view-content {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    gap: 12px;
    padding: 16px 20px;
  }
}
</style>

</template