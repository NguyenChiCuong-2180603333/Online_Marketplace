<template>
  <div class="products-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🌌 Khám phá Vũ trụ Sản phẩm</h1>
        <p class="page-subtitle">Tìm kiếm những báu vật tuyệt vời từ khắp thiên hà</p>
      </div>

      <!-- Search & Filters -->
      <div class="search-filters">
        <div class="search-container">
          <div class="search-box">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm kiếm sản phẩm trong vũ trụ..."
              class="search-input"
              @keyup.enter="performSearch"
            />
            <button @click="performSearch" class="search-btn" :disabled="loading">
              🔍 Tìm kiếm
            </button>
          </div>
        </div>

        <!-- Filters -->
        <div class="filter-controls">
          <div class="filter-group">
            <label for="category">Danh mục:</label>
            <select
              id="category"
              v-model="filters.category"
              @change="applyFilters"
              class="filter-select"
            >
              <option value="">Tất cả danh mục</option>
              <option v-for="category in categories" :key="category.id" :value="category.name">
                {{ category.name }}
              </option>
            </select>
          </div>

          <div class="filter-group">
            <label for="priceRange">Giá:</label>
            <select
              id="priceRange"
              v-model="filters.priceRange"
              @change="applyFilters"
              class="filter-select"
            >
              <option value="">Tất cả mức giá</option>
              <option value="0-500000">Dưới 500k</option>
              <option value="500000-2000000">500k - 2M</option>
              <option value="2000000-10000000">2M - 10M</option>
              <option value="10000000+">Trên 10M</option>
            </select>
          </div>

          <div class="filter-group">
            <label for="sortBy">Sắp xếp:</label>
            <select
              id="sortBy"
              v-model="filters.sortBy"
              @change="applyFilters"
              class="filter-select"
            >
              <option value="newest">Mới nhất</option>
              <option value="price-asc">Giá tăng dần</option>
              <option value="price-desc">Giá giảm dần</option>
              <option value="rating">Đánh giá cao</option>
              <option value="popular">Phổ biến</option>
            </select>
          </div>

          <button @click="clearAllFilters" class="clear-filters-btn">🔄 Xóa bộ lọc</button>
        </div>
      </div>

      <!-- Results Info -->
      <div class="results-info">
        <div class="results-count">
          <span v-if="!loading">
            Tìm thấy {{ totalResults }} sản phẩm
            <span v-if="searchQuery"> cho "{{ searchQuery }}"</span>
          </span>
          <span v-else>Đang tìm kiếm...</span>
        </div>

        <div class="view-toggle">
          <button
            @click="viewMode = 'grid'"
            class="view-btn"
            :class="{ active: viewMode === 'grid' }"
          >
            ⚏ Lưới
          </button>
          <button
            @click="viewMode = 'list'"
            class="view-btn"
            :class="{ active: viewMode === 'list' }"
          >
            ☰ Danh sách
          </button>
        </div>
      </div>

      <!-- Products Grid/List -->
      <div v-if="!loading && products.length > 0" class="products-container">
        <div class="products-grid" :class="{ 'list-view': viewMode === 'list' }">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card space-card"
            @click="viewProduct(product.id)"
          >
            <div class="product-image">
              <img
                :src="product.imageUrl || product.images?.[0] || '/placeholder-product.jpg'"
                :alt="product.name"
                loading="lazy"
                @error="handleImageError"
              />
              <div class="product-badges">
                <span v-if="isNewProduct(product)" class="badge new-badge">🆕 Mới</span>
                <span v-if="product.discount && product.discount > 0" class="badge discount-badge">
                  -{{ product.discount }}%
                </span>
                <span v-if="product.featured" class="badge bestseller-badge"> 🔥 Nổi bật </span>
              </div>
              <div class="product-actions-overlay">
                <button @click.stop="quickView(product)" class="action-btn" title="Xem nhanh">
                  👁️
                </button>
                <button
                  @click.stop="toggleWishlist(product.id)"
                  class="action-btn"
                  :class="{ active: isInWishlist(product.id) }"
                  title="Yêu thích"
                >
                  {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
                </button>
              </div>
            </div>

            <div class="product-info">
              <div class="product-category">{{ product.category }}</div>
              <h3 class="product-title">{{ product.name }}</h3>

              <div class="product-rating">
                <div class="rating-stars">
                  <span
                    v-for="i in 5"
                    :key="i"
                    class="star"
                    :class="[i <= Number(product.averageRating || 0) ? 'filled' : '']"
                    >⭐</span
                  >
                </div>
                <span class="rating-text">({{ product.reviewCount || 0 }})</span>
              </div>

              <p class="product-description">{{ truncateText(product.description, 80) }}</p>

              <div class="product-price">
                <span
                  v-if="product.originalPrice && product.originalPrice > product.price"
                  class="original-price"
                >
                  {{ formatCurrency(product.originalPrice) }}
                </span>
                <span class="current-price">{{ formatCurrency(product.price) }}</span>
              </div>

              <div class="product-actions">
                <button
                  @click.stop="addToCart(product)"
                  class="btn btn-primary"
                  :disabled="cartLoading || product.stock <= 0"
                  :class="{ loading: cartLoading }"
                >
                  {{ product.stock <= 0 ? '🚫 Hết hàng' : '🛒 Thêm vào giỏ' }}
                </button>
                <button
                  @click.stop="buyNow(product)"
                  class="btn btn-secondary"
                  :disabled="product.stock <= 0"
                >
                  🚀 Mua ngay
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-else-if="loading" class="loading-container">
        <div class="cosmic-loader">
          <div class="planet"></div>
          <div class="orbit"></div>
          <div class="orbit orbit-2"></div>
        </div>
        <p>Đang khám phá vũ trụ sản phẩm...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-container">
        <div class="error-content space-card">
          <div class="error-icon">⚠️</div>
          <h3>Có lỗi xảy ra</h3>
          <p>{{ error }}</p>
          <button @click="loadProducts" class="btn btn-primary">🔄 Thử lại</button>
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
      <div v-if="totalPages > 1 && !loading" class="pagination-container">
        <div class="pagination">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            ← Trước
          </button>

          <div class="pagination-numbers">
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="goToPage(page)"
              class="pagination-btn"
              :class="{ active: page === currentPage }"
              v-show="page !== '...'"
            >
              {{ page }}
            </button>
          </div>

          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            Sau →
          </button>
        </div>

        <div class="pagination-info">
          Trang {{ currentPage }} / {{ totalPages }} ({{ totalResults }} sản phẩm)
        </div>
      </div>
    </div>

    <!-- Quick View Modal -->
    <div v-if="showQuickView" class="modal-overlay" @click="closeQuickView">
      <div class="modal-content space-card" @click.stop>
        <button @click="closeQuickView" class="modal-close">✕</button>
        <div v-if="selectedProduct" class="quick-view-content">
          <div class="quick-view-image">
            <img
              :src="
                selectedProduct.imageUrl ||
                selectedProduct.images?.[0] ||
                '/placeholder-product.jpg'
              "
              :alt="selectedProduct.name"
            />
          </div>
          <div class="quick-view-info">
            <h3>{{ selectedProduct.name }}</h3>
            <div class="quick-view-rating">
              <div class="rating-stars">
                <span
                  v-for="i in 5"
                  :key="i"
                  class="star"
                  :class="[i <= Math.round(selectedProduct.rating || 0) ? 'filled' : '']"
                  >⭐</span
                >
              </div>
              <span>({{ selectedProduct.reviewCount || 0 }} đánh giá)</span>
            </div>
            <div class="quick-view-price">
              <span
                v-if="
                  selectedProduct.originalPrice &&
                  selectedProduct.originalPrice > selectedProduct.price
                "
                class="original-price"
              >
                {{ formatCurrency(selectedProduct.originalPrice) }}
              </span>
              <span class="current-price">{{ formatCurrency(selectedProduct.price) }}</span>
            </div>
            <p class="quick-view-description">{{ selectedProduct.description }}</p>
            <div class="quick-view-actions">
              <button
                @click="addToCart(selectedProduct)"
                class="btn btn-primary"
                :disabled="selectedProduct.stock <= 0"
              >
                {{ selectedProduct.stock <= 0 ? '🚫 Hết hàng' : '🛒 Thêm vào giỏ' }}
              </button>
              <router-link :to="`/products/${selectedProduct.id}`" class="btn btn-secondary">
                📋 Xem chi tiết
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { productAPI, categoryAPI } from '@/services/api'
import { searchAPI } from '@/services/api'

export default {
  name: 'Products',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const cartStore = useCartStore()

    // Reactive data
    const loading = ref(false)
    const cartLoading = ref(false)
    const error = ref(null)
    const searchQuery = ref('')
    const viewMode = ref('grid')
    const showQuickView = ref(false)
    const selectedProduct = ref(null)
    const wishlist = ref([])

    const filters = ref({
      category: '',
      priceRange: '',
      sortBy: 'newest',
    })

    const currentPage = ref(1)
    const itemsPerPage = ref(12)
    const totalResults = ref(0)

    // Data from API
    const products = ref([])
    const categories = ref([])

    // Computed properties
    const totalPages = computed(() => {
      return Math.ceil(totalResults.value / itemsPerPage.value)
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

    const loadProducts = async () => {
      try {
        loading.value = true
        error.value = null

        const params = {
          page: currentPage.value - 1,
          size: itemsPerPage.value,
        }

        if (filters.value.category) {
          params.category = filters.value.category
        }

        if (filters.value.priceRange) {
          const [min, max] = filters.value.priceRange.split('-').map(Number)
          params.minPrice = min
          if (max) {
            params.maxPrice = max
          }
        }

        const sortMap = {
          newest: { sortBy: 'createdAt', sortOrder: 'desc' },
          'price-asc': { sortBy: 'price', sortOrder: 'asc' },
          'price-desc': { sortBy: 'price', sortOrder: 'desc' },
          rating: { sortBy: 'rating', sortOrder: 'desc' },
          popular: { sortBy: 'reviewCount', sortOrder: 'desc' },
        }
        const sort = sortMap[filters.value.sortBy] || { sortBy: 'createdAt', sortOrder: 'desc' }
        params.sortBy = sort.sortBy
        params.sortOrder = sort.sortOrder

        const query = searchQuery.value.trim() || undefined

        const response = await searchAPI.products(query, params)

        if (response.data.products) {
          products.value = response.data.products
          totalResults.value = response.data.totalElements || response.data.products.length
        } else if (Array.isArray(response.data)) {
          products.value = response.data
          totalResults.value = response.data.length
        } else {
          products.value = []
          totalResults.value = 0
        }
      } catch (err) {
        console.error('Error loading products:', err)
        error.value = err.response?.data?.message || 'Không thể tải danh sách sản phẩm'
        products.value = []
        totalResults.value = 0
      } finally {
        loading.value = false
      }
    }

    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getAll()
        categories.value = response.data || []
      } catch (err) {
        console.error('Error loading categories:', err)
        categories.value = []
      }
    }

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

    const isNewProduct = (product) => {
      if (!product.createdAt) return false
      const createdDate = new Date(product.createdAt)
      const now = new Date()
      const daysDiff = (now - createdDate) / (1000 * 60 * 60 * 24)
      return daysDiff <= 7 
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    
    const performSearch = () => {
      currentPage.value = 1
      loadProducts()
    }

    const applyFilters = () => {
      currentPage.value = 1
      loadProducts()
    }

    const clearAllFilters = () => {
      searchQuery.value = ''
      filters.value = {
        category: '',
        priceRange: '',
        sortBy: 'newest',
      }
      currentPage.value = 1
      loadProducts()
    }

    
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        loadProducts()
        window.scrollTo({ top: 0, behavior: 'smooth' })
      }
    }

    const viewProduct = (productId) => {
      router.push(`/products/${productId}`)
    }

  
    const quickView = (product) => {
      selectedProduct.value = product
      showQuickView.value = true
    }

    const closeQuickView = () => {
      showQuickView.value = false
      selectedProduct.value = null
    }

    
    const addToCart = async (product) => {
      if (cartLoading.value || product.stock <= 0) return

      try {
        cartLoading.value = true
        await cartStore.addItem(product.id, 1)

        const message = `Đã thêm "${product.name}" vào giỏ hàng!`
        alert(message) 
      } catch (error) {
        console.error('Error adding to cart:', error)
        const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng'
        alert(errorMessage)
      } finally {
        cartLoading.value = false
      }
    }

    const buyNow = async (product) => {
      if (product.stock <= 0) return

      await addToCart(product)
      if (!cartLoading.value) {
        router.push('/cart')
      }
    }

    
    const toggleWishlist = (productId) => {
      const index = wishlist.value.indexOf(productId)
      if (index > -1) {
        wishlist.value.splice(index, 1)
      } else {
        wishlist.value.push(productId)
      }
    }

    const isInWishlist = (productId) => {
      return wishlist.value.includes(productId)
    }

    onMounted(async () => {
      await Promise.all([loadCategories(), loadProducts()])

      if (route.query.search) {
        searchQuery.value = route.query.search
      }
      if (route.query.category) {
        filters.value.category = route.query.category
      }

      if (route.query.search || route.query.category) {
        await loadProducts()
      }
    })

    watch(
      () => route.query,
      async (newQuery) => {
        if (newQuery.search !== searchQuery.value) {
          searchQuery.value = newQuery.search || ''
        }
        if (newQuery.category !== filters.value.category) {
          filters.value.category = newQuery.category || ''
        }

        currentPage.value = 1
        await loadProducts()
      }
    )

    return {
      loading,
      cartLoading,
      error,
      searchQuery,
      viewMode,
      showQuickView,
      selectedProduct,
      wishlist,
      filters,
      currentPage,
      totalResults,
      products,
      categories,

      totalPages,
      visiblePages,

      formatCurrency,
      truncateText,
      isNewProduct,
      handleImageError,
      performSearch,
      applyFilters,
      clearAllFilters,
      goToPage,
      viewProduct,
      quickView,
      closeQuickView,
      addToCart,
      buyNow,
      toggleWishlist,
      isInWishlist,
      loadProducts,
    }
  },
}
</script>

<style scoped>
.products-page {
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

.search-filters {
  margin-bottom: 2rem;
}

.search-container {
  margin-bottom: 1.5rem;
}

.search-box {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
  gap: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 1rem 1.5rem;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: var(--text-accent);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.search-btn {
  background: var(--aurora-gradient);
  border: none;
  padding: 1rem 2rem;
  border-radius: 50px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--neon-glow);
}

.filter-controls {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.filter-select {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.clear-filters-btn {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.clear-filters-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: #ef4444;
}

.results-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
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
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.view-btn:hover,
.view-btn.active {
  background: var(--aurora-gradient);
  border-color: var(--text-accent);
  color: white;
}

.products-container {
  margin-bottom: 3rem;
}

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
  gap: 1.5rem;
  align-items: center;
}

.products-grid.list-view .product-image {
  width: 200px;
  height: 150px;
  flex: none;
}

.products-grid.list-view .product-info {
  flex: 1;
}

.product-card {
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--space-glow);
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 1rem;
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

.product-badges {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.new-badge {
  background: rgba(16, 185, 129, 0.9);
  color: white;
}

.discount-badge {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.bestseller-badge {
  background: rgba(245, 158, 11, 0.9);
  color: white;
}

.product-actions-overlay {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-actions-overlay {
  opacity: 1;
}

.action-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  font-size: 1rem;
}

.action-btn:hover {
  background: var(--aurora-gradient);
  transform: scale(1.1);
}

.action-btn.active {
  background: var(--accent-gradient);
}

.product-info {
  flex: 1;
}

.product-category {
  color: var(--text-accent);
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-title {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  line-height: 1.3;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.rating-stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  font-size: 1rem;
  opacity: 0.3;
  transition: all 0.3s ease;
}

.star.filled {
  opacity: 1;
  filter: drop-shadow(0 0 5px currentColor);
}

.rating-text {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.product-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.product-price {
  margin-bottom: 1rem;
}

.original-price {
  color: var(--text-secondary);
  text-decoration: line-through;
  font-size: 0.9rem;
  opacity: 0.7;
  margin-right: 0.5rem;
}

.current-price {
  color: var(--text-accent);
  font-size: 1.2rem;
  font-weight: 600;
}

.product-actions {
  display: flex;
  gap: 0.75rem;
}

.product-actions .btn {
  flex: 1;
  padding: 0.6rem 1rem;
  font-size: 0.9rem;
}

.loading-container {
  text-align: center;
  padding: 4rem 0;
}

.cosmic-loader {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
}

.planet {
  width: 40px;
  height: 40px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

.orbit:nth-child(2) {
  width: 60px;
  height: 60px;
  top: 10px;
  left: 10px;
}

.orbit-2 {
  width: 80px;
  height: 80px;
  top: 0;
  left: 0;
  animation-duration: 4s;
  animation-direction: reverse;
}

@keyframes pulse {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.no-results {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 40vh;
}

.no-results-content {
  text-align: center;
  padding: 3rem;
  max-width: 400px;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-results-content h3 {
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.no-results-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.pagination-container {
  margin-top: 3rem;
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.pagination-numbers {
  display: flex;
  gap: 0.25rem;
}

.pagination-btn {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.6rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  min-width: 40px;
}

.pagination-btn:hover:not(:disabled),
.pagination-btn.active {
  background: var(--aurora-gradient);
  border-color: var(--text-accent);
  color: white;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

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
  margin: 2rem;
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
  z-index: 1001;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.8);
  transform: scale(1.1);
}

.quick-view-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  padding: 2rem;
}

.quick-view-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 60%, #e0f7fa 100%); /* nền sáng nhẹ */
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 340px;
}
.quick-view-image img {
  width: 100%;
  height: 340px;
  object-fit: contain;
  background: transparent;
  border-radius: 10px;
  box-shadow: 0 2px 8px #00d4ff22;
  display: block;
}

.quick-view-info h3 {
  color: var(--text-primary);
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.quick-view-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.quick-view-price {
  margin-bottom: 1rem;
}

.quick-view-price .original-price {
  display: block;
  margin-bottom: 0.25rem;
}

.quick-view-price .current-price {
  font-size: 1.5rem;
}

.quick-view-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 2rem;
}

.quick-view-actions {
  display: flex;
  gap: 1rem;
}

.quick-view-actions .btn {
  flex: 1;
  padding: 0.8rem 1.5rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .filter-controls {
    grid-template-columns: repeat(2, 1fr);
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1.5rem;
  }
}

@media (max-width: 768px) {
  .products-page {
    padding: 1rem 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .search-box {
    flex-direction: column;
  }

  .search-btn {
    border-radius: 12px;
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
    gap: 0.25rem;
  }

  .pagination-numbers {
    flex-wrap: wrap;
  }

  .quick-view-content {
    grid-template-columns: 1fr;
    gap: 1rem;
    padding: 1.5rem;
  }

  .quick-view-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .product-card {
    padding: 1rem;
  }

  .product-image {
    height: 150px;
  }

  .product-actions {
    flex-direction: column;
  }

  .quick-view-image img {
    height: 220px;
  }
  .quick-view-image {
    min-height: 220px;
  }

  .modal-content {
    margin: 1rem;
  }

  .pagination-btn {
    padding: 0.5rem 0.75rem;
    font-size: 0.8rem;
    min-width: 35px;
  }
}

/* Loading Animation */
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

@keyframes spin {
  0% {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  100% {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

/* Custom scrollbar for modal */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.5);
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 212, 255, 0.7);
}
</style>
