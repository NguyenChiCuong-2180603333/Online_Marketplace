<template>
  <div class="category-products-page">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="breadcrumb">
          <router-link to="/">Trang chủ</router-link>
          <span class="separator">/</span>
          <router-link to="/categories">Danh mục</router-link>
          <span class="separator">/</span>
          <span class="current">{{ categoryName }}</span>
        </div>
        <h1 class="page-title">{{ categoryName }}</h1>
        <p class="page-subtitle">{{ totalProducts }} sản phẩm trong danh mục này</p>
      </div>

      <!-- Filters and Products -->
      <div class="content-wrapper">
        <!-- Filters Sidebar -->
        <div class="filters-sidebar">
          <div class="filter-section">
            <h3>Bộ lọc</h3>

            <!-- Price Range -->
            <div class="filter-group">
              <label>Khoảng giá:</label>
              <div class="price-inputs">
                <input type="number" v-model="priceRange.min" placeholder="Từ" class="form-input" />
                <span>-</span>
                <input
                  type="number"
                  v-model="priceRange.max"
                  placeholder="Đến"
                  class="form-input"
                />
              </div>
              <button @click="applyPriceFilter" class="btn btn-sm btn-primary">Áp dụng</button>
            </div>

            <!-- Sort Options -->
            <div class="filter-group">
              <label>Sắp xếp theo:</label>
              <select v-model="sortBy" @change="applySort" class="form-select">
                <option value="name">Tên A-Z</option>
                <option value="name_desc">Tên Z-A</option>
                <option value="price">Giá tăng dần</option>
                <option value="price_desc">Giá giảm dần</option>
                <option value="newest">Mới nhất</option>
                <option value="rating">Đánh giá cao nhất</option>
              </select>
            </div>

            <!-- Clear Filters -->
            <button @click="clearFilters" class="btn btn-outline">🗑️ Xóa bộ lọc</button>
          </div>
        </div>

        <!-- Products Grid -->
        <div class="products-section">
          <!-- Loading State -->
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>Đang tải sản phẩm...</p>
          </div>

          <!-- Products Grid -->
          <div v-else-if="products.length > 0" class="products-grid">
            <div v-for="product in products" :key="product.id" class="product-card">
              <div class="product-image">
                <img
                  :src="product.images[0] || '/placeholder-product.jpg'"
                  :alt="product.name"
                  @error="handleImageError"
                />
                <div v-if="product.discount" class="discount-badge">-{{ product.discount }}%</div>
                <div v-if="isNewProduct(product.createdAt)" class="new-badge">Mới</div>
              </div>

              <div class="product-info">
                <h3 class="product-name">
                  <router-link :to="`/products/${product.id}`">
                    {{ product.name }}
                  </router-link>
                </h3>

                <div class="product-rating">
                  <div class="stars">
                    <span
                      v-for="star in 5"
                      :key="star"
                      :class="['star', star <= product.rating ? 'filled' : 'empty']"
                    >
                      ★
                    </span>
                  </div>
                  <span class="rating-count">({{ product.reviewCount }})</span>
                </div>

                <div class="product-price">
                  <span v-if="product.discount" class="original-price">
                    {{ formatPrice(product.price) }}
                  </span>
                  <span class="current-price">
                    {{ formatPrice(getDiscountedPrice(product)) }}
                  </span>
                </div>

                <div class="product-actions">
                  <button
                    @click="addToCart(product)"
                    class="btn btn-primary btn-sm"
                    :disabled="!product.inStock"
                  >
                    {{ product.inStock ? '🛒 Thêm vào giỏ' : 'Hết hàng' }}
                  </button>
                  <button
                    @click="toggleWishlist(product)"
                    class="btn btn-outline btn-sm"
                    :class="{ 'in-wishlist': isInWishlist(product.id) }"
                  >
                    {{ isInWishlist(product.id) ? '❤️' : '🤍' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="empty-state">
            <div class="empty-icon">📦</div>
            <h3>Không có sản phẩm nào</h3>
            <p>Không tìm thấy sản phẩm nào trong danh mục này.</p>
            <router-link to="/products" class="btn btn-primary"> Xem tất cả sản phẩm </router-link>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="pagination">
            <button
              @click="changePage(currentPage - 1)"
              :disabled="currentPage === 1"
              class="btn btn-outline"
            >
              ← Trước
            </button>

            <div class="page-numbers">
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="changePage(page)"
                :class="['btn', page === currentPage ? 'btn-primary' : 'btn-outline']"
              >
                {{ page }}
              </button>
            </div>

            <button
              @click="changePage(currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="btn btn-outline"
            >
              Sau →
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useWishlistStore } from '@/stores/wishlist'
import { api } from '@/services/api'

export default {
  name: 'CategoryProducts',
  props: {
    category: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const route = useRoute()
    const cartStore = useCartStore()
    const wishlistStore = useWishlistStore()

    // Reactive data
    const products = ref([])
    const loading = ref(false)
    const totalProducts = ref(0)
    const currentPage = ref(1)
    const itemsPerPage = ref(12)
    const sortBy = ref('newest')
    const priceRange = ref({
      min: '',
      max: '',
    })

    // Computed
    const categoryName = computed(() => {
      return decodeURIComponent(props.category)
    })

    const totalPages = computed(() => {
      return Math.ceil(totalProducts.value / itemsPerPage.value)
    })

    const visiblePages = computed(() => {
      const pages = []
      const start = Math.max(1, currentPage.value - 2)
      const end = Math.min(totalPages.value, currentPage.value + 2)

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    })

    // Methods
    const loadProducts = async () => {
      loading.value = true
      try {
        const params = {
          category: props.category,
          page: currentPage.value,
          limit: itemsPerPage.value,
          sortBy: sortBy.value,
        }

        if (priceRange.value.min) {
          params.minPrice = priceRange.value.min
        }
        if (priceRange.value.max) {
          params.maxPrice = priceRange.value.max
        }

        const response = await api.get('/products', { params })
        products.value = response.data.products
        totalProducts.value = response.data.total
      } catch (error) {
        console.error('Error loading products:', error)
      } finally {
        loading.value = false
      }
    }

    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const applySort = () => {
      currentPage.value = 1
      loadProducts()
    }

    const applyPriceFilter = () => {
      currentPage.value = 1
      loadProducts()
    }

    const clearFilters = () => {
      priceRange.value = { min: '', max: '' }
      sortBy.value = 'newest'
      currentPage.value = 1
      loadProducts()
    }

    const addToCart = (product) => {
      cartStore.addToCart({
        id: product.id,
        name: product.name,
        price: getDiscountedPrice(product),
        image: product.images[0],
        quantity: 1,
      })
    }

    const toggleWishlist = (product) => {
      if (isInWishlist(product.id)) {
        wishlistStore.removeFromWishlist(product.id)
      } else {
        wishlistStore.addToWishlist(product)
      }
    }

    const isInWishlist = (productId) => {
      return wishlistStore.isInWishlist(productId)
    }

    const getDiscountedPrice = (product) => {
      if (product.discount) {
        return product.price * (1 - product.discount / 100)
      }
      return product.price
    }

    const formatPrice = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const isNewProduct = (createdAt) => {
      const oneWeekAgo = new Date()
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
      return new Date(createdAt) > oneWeekAgo
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    // Watchers
    watch(
      () => props.category,
      () => {
        currentPage.value = 1
        loadProducts()
      }
    )

    watch(currentPage, () => {
      loadProducts()
    })

    // Lifecycle
    onMounted(() => {
      loadProducts()
    })

    return {
      products,
      loading,
      totalProducts,
      currentPage,
      totalPages,
      visiblePages,
      categoryName,
      sortBy,
      priceRange,
      changePage,
      applySort,
      applyPriceFilter,
      clearFilters,
      addToCart,
      toggleWishlist,
      isInWishlist,
      getDiscountedPrice,
      formatPrice,
      isNewProduct,
      handleImageError,
    }
  },
}
</script>

<style scoped>
.category-products-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
  color: white;
}

.breadcrumb {
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.breadcrumb a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
}

.breadcrumb a:hover {
  color: white;
}

.separator {
  margin: 0 0.5rem;
  color: rgba(255, 255, 255, 0.6);
}

.current {
  color: white;
  font-weight: 600;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 2rem;
  align-items: start;
}

.filters-sidebar {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 2rem;
}

.filter-section h3 {
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.2rem;
}

.filter-group {
  margin-bottom: 1.5rem;
}

.filter-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
}

.price-inputs {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.price-inputs input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
}

.products-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.loading-container {
  text-align: center;
  padding: 3rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  background: white;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
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
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff4757;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.new-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #2ed573;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.product-info {
  padding: 1rem;
}

.product-name {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
}

.product-name a {
  color: #333;
  text-decoration: none;
}

.product-name a:hover {
  color: #667eea;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.stars {
  display: flex;
  gap: 1px;
}

.star {
  font-size: 0.9rem;
}

.star.filled {
  color: #ffa502;
}

.star.empty {
  color: #ddd;
}

.rating-count {
  font-size: 0.8rem;
  color: #666;
}

.product-price {
  margin-bottom: 1rem;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.current-price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #ff4757;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-outline {
  background: transparent;
  border: 1px solid #ddd;
  color: #666;
}

.btn-outline:hover {
  background: #f8f9fa;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.in-wishlist {
  background: #ff4757;
  color: white;
  border-color: #ff4757;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.empty-state p {
  margin-bottom: 1.5rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .filters-sidebar {
    position: static;
    margin-bottom: 1rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }

  .page-title {
    font-size: 2rem;
  }
}
</style>
