<template>
  <div class="categories-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">🗂️ Khám phá Danh mục</h1>
          <p class="hero-subtitle">Tìm kiếm sản phẩm theo từng danh mục cụ thể</p>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container">
      <div class="categories-content">
        
        <!-- Categories Grid -->
        <div class="categories-grid">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-card"
            :class="{ active: selectedCategory?.id === category.id }"
            @click="selectCategory(category)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3 class="category-name">{{ category.name }}</h3>
            <p class="category-description">{{ category.description }}</p>
            <div class="category-stats">
              <span class="product-count">{{ category.productCount }} sản phẩm</span>
            </div>
          </div>
        </div>

        <!-- Products Section -->
        <div v-if="selectedCategory" class="products-section">
          <div class="section-header">
            <div class="section-title">
              <h2>{{ selectedCategory.icon }} {{ selectedCategory.name }}</h2>
              <p class="section-subtitle">{{ filteredProducts.length }} sản phẩm được tìm thấy</p>
            </div>
            
            <!-- Filters & Sort -->
            <div class="filters-section">
              <div class="price-filter">
                <label>Giá từ:</label>
                <input 
                  v-model="filters.minPrice" 
                  type="number" 
                  placeholder="0"
                  @input="applyFilters"
                />
                <span>đến</span>
                <input 
                  v-model="filters.maxPrice" 
                  type="number" 
                  placeholder="∞"
                  @input="applyFilters"
                />
              </div>
              
              <div class="sort-filter">
                <label>Sắp xếp:</label>
                <select v-model="sortBy" @change="applySort">
                  <option value="newest">Mới nhất</option>
                  <option value="priceAsc">Giá thấp → cao</option>
                  <option value="priceDesc">Giá cao → thấp</option>
                  <option value="rating">Đánh giá cao</option>
                  <option value="popular">Phổ biến</option>
                </select>
              </div>
              
              <button @click="clearFilters" class="clear-filters-btn">
                🗑️ Xóa bộ lọc
              </button>
            </div>
          </div>

          <!-- Products Grid -->
          <div v-if="loading" class="loading-section">
            <div class="loading-spinner">⏳ Đang tải sản phẩm...</div>
          </div>
          
          <div v-else-if="filteredProducts.length > 0" class="products-grid">
            <div 
              v-for="product in paginatedProducts" 
              :key="product.id"
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <div class="product-image">
                <img :src="product.images?.[0] || '/placeholder-product.jpg'" :alt="product.name" />
                <div class="product-overlay">
                  <button class="quick-view-btn">👁️ Xem nhanh</button>
                </div>
              </div>
              
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-description">{{ truncateText(product.description, 60) }}</p>
                
                <div class="product-meta">
                  <div class="product-rating">
                    <span class="rating-stars">{{ getStarRating(product.averageRating) }}</span>
                    <span class="rating-count">({{ product.reviewCount }})</span>
                  </div>
                  
                  <div class="product-price">
                    <span v-if="product.discountPrice" class="original-price">
                      {{ formatPrice(product.price) }}
                    </span>
                    <span class="current-price">
                      {{ formatPrice(product.discountPrice || product.price) }}
                    </span>
                  </div>
                </div>
                
                <div class="product-actions">
                  <button 
                    @click.stop="addToCart(product)"
                    class="add-to-cart-btn"
                    :disabled="product.stock < 1"
                  >
                    {{ product.stock < 1 ? '🔒 Hết hàng' : '🛒 Thêm vào giỏ' }}
                  </button>
                  
                  <button @click.stop="toggleWishlist(product)" class="wishlist-btn">
                    {{ product.inWishlist ? '❤️' : '🤍' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="no-products">
            <div class="no-products-content">
              <div class="no-products-icon">📭</div>
              <h3>Không tìm thấy sản phẩm</h3>
              <p>Thử thay đổi bộ lọc hoặc chọn danh mục khác</p>
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

        <!-- No Category Selected -->
        <div v-else class="no-category-selected">
          <div class="no-category-content">
            <div class="no-category-icon">🗂️</div>
            <h3>Chọn một danh mục</h3>
            <p>Nhấp vào danh mục bên trên để xem sản phẩm</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { categoryAPI, productAPI } from '@/services/api'
import { useCartStore } from '@/stores/cart'

export default {
  name: 'Categories',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()
    
    // Reactive data
    const categories = ref([])
    const selectedCategory = ref(null)
    const products = ref([])
    const loading = ref(false)
    const error = ref(null)
    
    // Filters
    const filters = ref({
      minPrice: '',
      maxPrice: '',
      search: ''
    })
    
    const sortBy = ref('newest')
    
    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = 12
    
    // Computed properties
    const filteredProducts = computed(() => {
      let filtered = [...products.value]
      
      // Price filter
      if (filters.value.minPrice) {
        filtered = filtered.filter(product => 
          (product.discountPrice || product.price) >= parseFloat(filters.value.minPrice)
        )
      }
      
      if (filters.value.maxPrice) {
        filtered = filtered.filter(product => 
          (product.discountPrice || product.price) <= parseFloat(filters.value.maxPrice)
        )
      }
      
      return filtered
    })
    
    const sortedProducts = computed(() => {
      const sorted = [...filteredProducts.value]
      
      switch (sortBy.value) {
        case 'priceAsc':
          return sorted.sort((a, b) => 
            (a.discountPrice || a.price) - (b.discountPrice || b.price)
          )
        case 'priceDesc':
          return sorted.sort((a, b) => 
            (b.discountPrice || b.price) - (a.discountPrice || a.price)
          )
        case 'rating':
          return sorted.sort((a, b) => b.averageRating - a.averageRating)
        case 'popular':
          return sorted.sort((a, b) => b.sold - a.sold)
        case 'newest':
        default:
          return sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }
    })
    
    const paginatedProducts = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      return sortedProducts.value.slice(start, start + itemsPerPage)
    })
    
    const totalPages = computed(() => 
      Math.ceil(sortedProducts.value.length / itemsPerPage)
    )
    
    const visiblePages = computed(() => {
      const total = totalPages.value
      const current = currentPage.value
      const delta = 2
      
      let start = Math.max(1, current - delta)
      let end = Math.min(total, current + delta)
      
      if (end - start < delta * 2) {
        if (start === 1) {
          end = Math.min(total, start + delta * 2)
        } else {
          start = Math.max(1, end - delta * 2)
        }
      }
      
      return Array.from({ length: end - start + 1 }, (_, i) => start + i)
    })
    
    // Methods
    const loadCategories = async () => {
      try {
        const response = await categoryAPI.getAll()
        categories.value = response.data.map(category => ({
          ...category,
          icon: getCategoryIcon(category.name),
          productCount: category.productCount || 0
        }))
      } catch (err) {
        error.value = 'Không thể tải danh mục'
        console.error('Error loading categories:', err)
      }
    }
    
    const selectCategory = async (category) => {
      selectedCategory.value = category
      currentPage.value = 1
      await loadProducts(category.id)
    }
    
    const loadProducts = async (categoryId) => {
      loading.value = true
      try {
        const response = await productAPI.getAll({ category: categoryId })
        products.value = response.data.products || response.data
      } catch (err) {
        error.value = 'Không thể tải sản phẩm'
        console.error('Error loading products:', err)
      } finally {
        loading.value = false
      }
    }
    
    const getCategoryIcon = (categoryName) => {
      const icons = {
        'Điện tử': '📱',
        'Thời trang': '👕',
        'Nhà cửa': '🏠',
        'Sách': '📚',
        'Thể thao': '⚽',
        'Làm đẹp': '💄',
        'Ô tô': '🚗',
        'Mẹ và bé': '👶',
        'Thú cưng': '🐕',
        'Du lịch': '✈️'
      }
      return icons[categoryName] || '📦'
    }
    
    const applyFilters = () => {
      currentPage.value = 1
    }
    
    const applySort = () => {
      currentPage.value = 1
    }
    
    const clearFilters = () => {
      filters.value = {
        minPrice: '',
        maxPrice: '',
        search: ''
      }
      sortBy.value = 'newest'
      currentPage.value = 1
    }
    
    const changePage = (page) => {
      currentPage.value = page
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
    
    const goToProduct = (productId) => {
      router.push(`/products/${productId}`)
    }
    
    const addToCart = async (product) => {
      try {
        await cartStore.addItem(product.id, 1)
        // Show success message
      } catch (err) {
        console.error('Error adding to cart:', err)
      }
    }
    
    const toggleWishlist = (product) => {
      // Toggle wishlist logic
      product.inWishlist = !product.inWishlist
    }
    
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
    
    const getStarRating = (rating) => {
      const stars = Math.round(rating || 0)
      return '⭐'.repeat(stars) + '☆'.repeat(5 - stars)
    }
    
    const truncateText = (text, limit) => {
      if (!text) return ''
      return text.length > limit ? text.substring(0, limit) + '...' : text
    }
    
    // Lifecycle
    onMounted(() => {
      loadCategories()
    })
    
    return {
      categories,
      selectedCategory,
      products,
      filteredProducts,
      paginatedProducts,
      loading,
      error,
      filters,
      sortBy,
      currentPage,
      totalPages,
      visiblePages,
      selectCategory,
      applyFilters,
      applySort,
      clearFilters,
      changePage,
      goToProduct,
      addToCart,
      toggleWishlist,
      formatPrice,
      getStarRating,
      truncateText
    }
  }
}
</script>

<style scoped>
.categories-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.hero-section {
  background: linear-gradient(135deg, var(--accent-blue) 0%, var(--accent-purple) 100%);
  padding: 60px 0;
  color: white;
  text-align: center;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.categories-content {
  padding: 40px 0;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.category-card {
  background: var(--bg-secondary);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.2);
}

.category-card.active {
  border-color: var(--accent-blue);
  background: linear-gradient(135deg, var(--accent-blue) 0%, var(--accent-purple) 100%);
  color: white;
}

.category-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.category-name {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.category-description {
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.category-card.active .category-description {
  color: rgba(255, 255, 255, 0.8);
}

.product-count {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--accent-blue);
}

.category-card.active .product-count {
  color: rgba(255, 255, 255, 0.9);
}

.products-section {
  margin-top: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.section-title h2 {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.section-subtitle {
  color: var(--text-secondary);
}

.filters-section {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-filter input {
  width: 80px;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.sort-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-filter select {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.clear-filters-btn {
  padding: 8px 16px;
  background: var(--accent-red);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.clear-filters-btn:hover {
  background: #e53e3e;
}

.loading-section {
  text-align: center;
  padding: 60px 0;
}

.loading-spinner {
  font-size: 1.2rem;
  color: var(--text-secondary);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card {
  background: var(--bg-secondary);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.15);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.quick-view-btn {
  padding: 8px 16px;
  background: var(--accent-blue);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.product-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
}

.rating-count {
  color: var(--text-secondary);
}

.product-price {
  text-align: right;
}

.original-price {
  text-decoration: line-through;
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-right: 8px;
}

.current-price {
  color: var(--accent-red);
  font-weight: 600;
  font-size: 1.1rem;
}

.product-actions {
  display: flex;
  gap: 8px;
}

.add-to-cart-btn {
  flex: 1;
  padding: 10px 16px;
  background: var(--accent-blue);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: #0c7fba;
}

.add-to-cart-btn:disabled {
  background: var(--text-secondary);
  cursor: not-allowed;
}

.wishlist-btn {
  padding: 10px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.wishlist-btn:hover {
  border-color: var(--accent-red);
}

.no-products,
.no-category-selected {
  text-align: center;
  padding: 80px 0;
}

.no-products-content,
.no-category-content {
  max-width: 400px;
  margin: 0 auto;
}

.no-products-icon,
.no-category-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.no-products h3,
.no-category-content h3 {
  font-size: 1.3rem;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.no-products p,
.no-category-content p {
  color: var(--text-secondary);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 40px;
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: var(--accent-blue);
  color: white;
  border-color: var(--accent-blue);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: var(--accent-blue);
  color: white;
  border-color: var(--accent-blue);
}

.pagination-numbers {
  display: flex;
  gap: 4px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .price-filter,
  .sort-filter {
    justify-content: space-between;
  }
  
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }
  
  .categories-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
}
</style>