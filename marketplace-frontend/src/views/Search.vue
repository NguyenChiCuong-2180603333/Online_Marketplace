<template>
  <div class="search-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <span class="search-icon">🔍</span>
          Kết quả tìm kiếm
        </h1>
        <p class="page-subtitle">
          Tìm thấy {{ totalResults }} kết quả cho "{{ searchQuery }}"
        </p>
      </div>
    </div>

    <!-- Search Content -->
    <div class="container">
      <!-- Search Filters -->
      <div class="search-filters">
        <div class="filter-controls">
          <!-- Sort Options -->
          <div class="sort-control">
            <label for="sortBy">Sắp xếp theo:</label>
            <select id="sortBy" v-model="sortBy" @change="applySort" class="form-select">
              <option value="relevance">Liên quan nhất</option>
              <option value="newest">Mới nhất</option>
              <option value="priceAsc">Giá tăng dần</option>
              <option value="priceDesc">Giá giảm dần</option>
              <option value="rating">Đánh giá cao nhất</option>
              <option value="popular">Phổ biến nhất</option>
            </select>
          </div>

          <!-- Price Range -->
          <div class="price-filter">
            <label>Khoảng giá:</label>
            <div class="price-inputs">
              <input 
                v-model="priceRange.min" 
                type="number" 
                placeholder="Từ"
                class="price-input"
                @input="applyPriceFilter"
              />
              <span class="price-separator">-</span>
              <input 
                v-model="priceRange.max" 
                type="number" 
                placeholder="Đến"
                class="price-input"
                @input="applyPriceFilter"
              />
            </div>
          </div>

          <!-- Category Filter -->
          <div class="category-filter">
            <label for="categoryFilter">Danh mục:</label>
            <select id="categoryFilter" v-model="selectedCategory" @change="applyCategoryFilter" class="form-select">
              <option value="">Tất cả danh mục</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>

          <!-- Clear Filters -->
          <button @click="clearAllFilters" class="clear-filters-btn">
            🗑️ Xóa bộ lọc
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <p>Đang tìm kiếm...</p>
        </div>
      </div>

      <!-- Search Results -->
      <div v-else-if="products.length > 0" class="search-results">
        <!-- Results Info -->
        <div class="results-info">
          <p class="results-count">
            Hiển thị {{ startIndex + 1 }}-{{ endIndex }} trong {{ totalResults }} kết quả
          </p>
          <div class="view-toggle">
            <button 
              @click="viewMode = 'grid'" 
              :class="['view-btn', { active: viewMode === 'grid' }]"
            >
              📱
            </button>
            <button 
              @click="viewMode = 'list'" 
              :class="['view-btn', { active: viewMode === 'list' }]"
            >
              📋
            </button>
          </div>
        </div>

        <!-- Products Grid -->
        <div :class="['products-grid', { 'list-view': viewMode === 'list' }]">
          <div 
            v-for="product in products" 
            :key="product.id"
            class="product-card"
            @click="viewProduct(product.id)"
          >
            <div class="product-image">
              <img 
                :src="product.imageUrl || '/placeholder-product.jpg'" 
                :alt="product.name"
                @error="handleImageError"
              />
              <div v-if="product.discount" class="discount-badge">
                -{{ product.discount }}%
              </div>
              <div v-if="isNewProduct(product.createdAt)" class="new-badge">
                Mới
              </div>
            </div>

            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-description">{{ truncateText(product.description, 100) }}</p>
              
              <div class="product-rating">
                <div class="stars">
                  <span v-for="i in 5" :key="i" class="star">
                    {{ i <= product.averageRating ? '⭐' : '☆' }}
                  </span>
                </div>
                <span class="rating-count">({{ product.reviewCount }})</span>
              </div>

              <div class="product-price">
                <span v-if="product.discountPrice" class="current-price">
                  {{ formatCurrency(product.discountPrice) }}
                </span>
                <span v-if="product.discountPrice" class="original-price">
                  {{ formatCurrency(product.price) }}
                </span>
                <span v-else class="current-price">
                  {{ formatCurrency(product.price) }}
                </span>
              </div>

              <div class="product-actions">
                <button 
                  @click.stop="addToCart(product.id)"
                  :disabled="cartLoading === product.id"
                  class="btn btn-primary"
                >
                  <span v-if="cartLoading === product.id" class="loading">⏳</span>
                  <span v-else>🛒 Thêm vào giỏ</span>
                </button>
                <button 
                  @click.stop="toggleWishlist(product.id)"
                  :class="['btn', 'btn-secondary', { 'in-wishlist': isInWishlist(product.id) }]"
                >
                  {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            ⬅️ Trước
          </button>
          
          <div class="pagination-numbers">
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="changePage(page)"
              class="pagination-btn"
              :class="{ active: page === currentPage }"
            >
              {{ page }}
            </button>
          </div>
          
          <button 
            @click="changePage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            Sau ➡️
          </button>
        </div>
      </div>

      <!-- No Results -->
      <div v-else class="no-results">
        <div class="no-results-content">
          <div class="no-results-icon">🔍</div>
          <h3>Không tìm thấy kết quả</h3>
          <p>Không có sản phẩm nào phù hợp với từ khóa "{{ searchQuery }}"</p>
          <div class="suggestions">
            <h4>Gợi ý:</h4>
            <ul>
              <li>Kiểm tra lại chính tả</li>
              <li>Thử từ khóa khác</li>
              <li>Sử dụng từ khóa ngắn hơn</li>
              <li>Xóa bộ lọc để tìm kiếm rộng hơn</li>
            </ul>
          </div>
          <router-link to="/products" class="btn btn-primary">
            🏪 Xem tất cả sản phẩm
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productAPI, categoryAPI } from '@/services/api'
import { useCartStore } from '@/stores/cart'
import { useWishlistStore } from '@/stores/wishlist'

export default {
  name: 'Search',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const cartStore = useCartStore()
    const wishlistStore = useWishlistStore()
    
    // Reactive data
    const loading = ref(false)
    const cartLoading = ref(null)
    const error = ref(null)
    const searchQuery = ref('')
    const viewMode = ref('grid')
    
    const filters = ref({
      category: '',
      priceRange: '',
      sortBy: 'relevance'
    })
    
    const priceRange = ref({
      min: '',
      max: ''
    })
    
    const selectedCategory = ref('')
    const currentPage = ref(1)
    const itemsPerPage = ref(12)
    const totalResults = ref(0)
    
    const products = ref([])
    const categories = ref([])
    
    const totalPages = computed(() => {
      return Math.ceil(totalResults.value / itemsPerPage.value)
    })
    
    const startIndex = computed(() => {
      return (currentPage.value - 1) * itemsPerPage.value
    })
    
    const endIndex = computed(() => {
      return Math.min(startIndex.value + itemsPerPage.value, totalResults.value)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const current = currentPage.value
      const total = totalPages.value
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) pages.push(i)
          pages.push('...')
          pages.push(total)
        } else if (current >= total - 3) {
          pages.push(1)
          pages.push('...')
          for (let i = total - 4; i <= total; i++) pages.push(i)
        } else {
          pages.push(1)
          pages.push('...')
          for (let i = current - 1; i <= current + 1; i++) pages.push(i)
          pages.push('...')
          pages.push(total)
        }
      }
      
      return pages
    })
    
    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getAll()
        categories.value = response.data
      } catch (err) {
        console.error('Error loading categories:', err)
      }
    }
    
    const performSearch = async () => {
      if (!searchQuery.value.trim()) return
      
      loading.value = true
      error.value = null
      
      try {
        const params = {
          q: searchQuery.value,
          page: currentPage.value,
          limit: itemsPerPage.value,
          sortBy: filters.value.sortBy
        }
        
        if (selectedCategory.value) {
          params.category = selectedCategory.value
        }
        
        if (priceRange.value.min) {
          params.minPrice = priceRange.value.min
        }
        
        if (priceRange.value.max) {
          params.maxPrice = priceRange.value.max
        }
        
        const response = await productAPI.search(params)
        products.value = response.data.products || []
        totalResults.value = response.data.total || 0
        
      } catch (err) {
        error.value = 'Không thể tìm kiếm sản phẩm'
        console.error('Search error:', err)
      } finally {
        loading.value = false
      }
    }
    
    const applySort = () => {
      currentPage.value = 1
      performSearch()
    }
    
    const applyPriceFilter = () => {
      currentPage.value = 1
      performSearch()
    }
    
    const applyCategoryFilter = () => {
      currentPage.value = 1
      performSearch()
    }
    
    const clearAllFilters = () => {
      selectedCategory.value = ''
      priceRange.value = { min: '', max: '' }
      filters.value.sortBy = 'relevance'
      currentPage.value = 1
      performSearch()
    }
    
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      performSearch()
    }
    
    const viewProduct = (productId) => {
      router.push(`/products/${productId}`)
    }
    
    const addToCart = async (productId) => {
      cartLoading.value = productId
      try {
        await cartStore.addItem(productId, 1)
      } catch (error) {
        console.error('Add to cart error:', error)
      } finally {
        cartLoading.value = null
      }
    }
    
    const toggleWishlist = (productId) => {
      wishlistStore.toggleWishlist(productId)
    }
    
    const isInWishlist = (productId) => {
      return wishlistStore.isInWishlist(productId)
    }
    
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const truncateText = (text, maxLength) => {
      if (!text) return ''
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
    }
    
    const isNewProduct = (createdAt) => {
      if (!createdAt) return false
      const created = new Date(createdAt)
      const now = new Date()
      const diffDays = (now - created) / (1000 * 60 * 60 * 24)
      return diffDays <= 7
    }
    
    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }
    
    watch(() => route.query.q, (newQuery) => {
      if (newQuery !== searchQuery.value) {
        searchQuery.value = newQuery || ''
        currentPage.value = 1
        if (searchQuery.value) {
          performSearch()
        }
      }
    })
    
    onMounted(async () => {
      await loadCategories()
      
      if (route.query.q) {
        searchQuery.value = route.query.q
        await performSearch()
      }
    })
    
    return {
      loading,
      cartLoading,
      error,
      searchQuery,
      viewMode,
      filters,
      priceRange,
      selectedCategory,
      currentPage,
      totalResults,
      products,
      categories,
      totalPages,
      startIndex,
      endIndex,
      visiblePages,
      performSearch,
      applySort,
      applyPriceFilter,
      applyCategoryFilter,
      clearAllFilters,
      changePage,
      viewProduct,
      addToCart,
      toggleWishlist,
      isInWishlist,
      formatCurrency,
      truncateText,
      isNewProduct,
      handleImageError
    }
  }
}
</script>

<style scoped>
.search-page {
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

.search-icon {
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

/* Search Filters */
.search-filters {
  margin-bottom: 2rem;
}

.filter-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  align-items: end;
}

.sort-control,
.category-filter {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.sort-control label,
.category-filter label {
  font-weight: 500;
  color: var(--text-primary);
}

.form-select {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-select:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

.price-filter {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.price-filter label {
  font-weight: 500;
  color: var(--text-primary);
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.price-input:focus {
  outline: none;
  border-color: var(--text-accent);
}

.price-separator {
  color: var(--text-secondary);
  font-weight: 500;
}

.clear-filters-btn {
  padding: 0.75rem 1rem;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.clear-filters-btn:hover {
  background: rgba(239, 68, 68, 0.3);
  transform: translateY(-2px);
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Search Results */
.search-results {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.results-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.results-count {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.view-toggle {
  display: flex;
  gap: 0.5rem;
}

.view-btn {
  padding: 0.5rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.view-btn:hover {
  background: rgba(0, 212, 255, 0.1);
}

.view-btn.active {
  background: rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent);
  color: var(--text-accent);
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.products-grid.list-view {
  grid-template-columns: 1fr;
}

.products-grid.list-view .product-card {
  display: flex;
  gap: 2rem;
}

.products-grid.list-view .product-image {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.products-grid.list-view .product-info {
  flex: 1;
}

.product-card {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.2);
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ef4444;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.new-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #10b981;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.product-info {
  padding: 1.5rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.product-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  font-size: 0.9rem;
}

.rating-count {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.product-price {
  margin-bottom: 1.5rem;
}

.current-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-accent);
}

.original-price {
  font-size: 1rem;
  color: var(--text-secondary);
  text-decoration: line-through;
  margin-left: 0.5rem;
}

.product-actions {
  display: flex;
  gap: 0.75rem;
}

.btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-primary {
  background: var(--text-accent);
  color: #1a1a2e;
}

.btn-primary:hover:not(:disabled) {
  background: #00c4ef;
  transform: translateY(-2px);
}

.btn-secondary {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  flex: 0 0 auto;
  width: 50px;
}

.btn-secondary:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  border-color: var(--text-accent);
}

.btn-secondary.in-wishlist {
  color: #ef4444;
  border-color: #ef4444;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading {
  animation: spin 1s linear infinite;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 3rem;
}

.pagination-btn {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.1);
  border-color: var(--text-accent);
  color: var(--text-accent);
}

.pagination-btn.active {
  background: var(--text-accent);
  color: #1a1a2e;
  border-color: var(--text-accent);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-numbers {
  display: flex;
  gap: 0.5rem;
}

/* No Results */
.no-results {
  text-align: center;
  padding: 4rem 0;
}

.no-results-content {
  max-width: 500px;
  margin: 0 auto;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-results h3 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.no-results p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.suggestions {
  text-align: left;
  margin-bottom: 2rem;
}

.suggestions h4 {
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.suggestions ul {
  list-style: none;
  padding: 0;
}

.suggestions li {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  padding-left: 1rem;
  position: relative;
}

.suggestions li::before {
  content: '•';
  color: var(--text-accent);
  position: absolute;
  left: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .filter-controls {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .results-info {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .products-grid.list-view .product-card {
    flex-direction: column;
  }
  
  .products-grid.list-view .product-image {
    width: 100%;
    height: 200px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .pagination-numbers {
    flex-wrap: wrap;
  }
}
</style>