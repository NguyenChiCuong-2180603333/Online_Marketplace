<template>
  <div class="my-products">
    <!-- Header -->
    <div class="page-header">
      <h1>📦 Sản phẩm của tôi</h1>
      <p>Quản lý toàn bộ sản phẩm bạn đang bán</p>
      <router-link to="/seller/products/create" class="btn btn-primary">
        ➕ Tạo sản phẩm mới
      </router-link>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.total }}</span>
          <span class="stat-label">Tổng sản phẩm</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.active }}</span>
          <span class="stat-label">Đang bán</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⏸️</div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.inactive }}</span>
          <span class="stat-label">Tạm ngưng</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⚠️</div>
        <div class="stat-info">
          <span class="stat-number">{{ stats.lowStock }}</span>
          <span class="stat-label">Sắp hết hàng</span>
        </div>
      </div>
    </div>

    <!-- Filters & Search -->
    <div class="filters-section">
      <div class="search-bar">
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="🔍 Tìm kiếm sản phẩm..."
          @input="searchProducts"
        />
      </div>
      
      <div class="filters">
        <select v-model="statusFilter" @change="filterProducts">
          <option value="">Tất cả trạng thái</option>
          <option value="active">Đang bán</option>
          <option value="inactive">Tạm ngưng</option>
        </select>
        
        <select v-model="categoryFilter" @change="filterProducts">
          <option value="">Tất cả danh mục</option>
          <option v-for="category in categories" :key="category.id" :value="category.name">
            {{ category.name }}
          </option>
        </select>

        <select v-model="sortBy" @change="sortProducts">
          <option value="newest">Mới nhất</option>
          <option value="oldest">Cũ nhất</option>
          <option value="name">Tên A-Z</option>
          <option value="price-high">Giá cao → thấp</option>
          <option value="price-low">Giá thấp → cao</option>
          <option value="stock">Tồn kho</option>
        </select>
      </div>

      <!-- Bulk Actions -->
      <div v-if="selectedProducts.length > 0" class="bulk-actions">
        <span class="selected-count">Đã chọn {{ selectedProducts.length }} sản phẩm</span>
        <button @click="bulkActivate" class="btn btn-success">Kích hoạt</button>
        <button @click="bulkDeactivate" class="btn btn-warning">Tạm ngưng</button>
        <button @click="bulkDelete" class="btn btn-danger">Xóa</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Đang tải sản phẩm...</p>
    </div>

    <!-- Products Grid -->
    <div v-else-if="filteredProducts.length > 0" class="products-grid">
      <div 
        v-for="product in paginatedProducts" 
        :key="product.id"
        class="product-card"
        :class="{ 'selected': selectedProducts.includes(product.id) }"
      >
        <!-- Selection Checkbox -->
        <div class="product-checkbox">
          <input 
            type="checkbox" 
            :value="product.id"
            v-model="selectedProducts"
          />
        </div>

        <!-- Product Image -->
        <div class="product-image">
          <img 
            :src="product.images?.[0] || '/placeholder-product.jpg'" 
            :alt="product.name"
            @error="handleImageError"
          />
          <div class="image-overlay">
            <button @click="viewProduct(product)" class="btn-overlay">👁️ Xem</button>
          </div>
        </div>

        <!-- Product Info -->
        <div class="product-info">
          <div class="product-header">
            <h3>{{ product.name }}</h3>
            <div class="product-status" :class="getStatusClass(product.isActive)">
              {{ product.isActive ? '✅ Đang bán' : '⏸️ Tạm ngưng' }}
            </div>
          </div>

          <p class="product-category">📂 {{ product.category }}</p>
          
          <div class="product-metrics">
            <div class="metric">
              <span class="metric-label">Giá bán:</span>
              <span class="metric-value price">{{ formatCurrency(product.price) }}</span>
            </div>
            <div class="metric">
              <span class="metric-label">Tồn kho:</span>
              <span class="metric-value" :class="getStockClass(product.stockQuantity)">
                {{ product.stockQuantity }} sản phẩm
              </span>
            </div>
            <div class="metric">
              <span class="metric-label">Đã bán:</span>
              <span class="metric-value">{{ product.soldCount || 0 }} sản phẩm</span>
            </div>
            <div class="metric">
              <span class="metric-label">Đánh giá:</span>
              <span class="metric-value">
                ⭐ {{ product.averageRating?.toFixed(1) || 'Chưa có' }}
                <span v-if="product.reviewCount">({{ product.reviewCount }})</span>
              </span>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="product-actions">
            <router-link 
              :to="`/seller/products/edit/${product.id}`" 
              class="btn btn-edit"
            >
              ✏️ Sửa
            </router-link>
            <button 
              @click="toggleProductStatus(product)"
              class="btn btn-toggle"
              :class="product.isActive ? 'btn-warning' : 'btn-success'"
            >
              {{ product.isActive ? '⏸️ Tạm ngưng' : '▶️ Kích hoạt' }}
            </button>
            <button 
              @click="duplicateProduct(product)"
              class="btn btn-secondary"
            >
              📋 Nhân bản
            </button>
            <button 
              @click="deleteProduct(product)"
              class="btn btn-danger"
            >
              🗑️ Xóa
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">📦</div>
      <h3>Chưa có sản phẩm nào</h3>
      <p>Bắt đầu bán hàng bằng cách tạo sản phẩm đầu tiên của bạn!</p>
      <router-link to="/seller/products/create" class="btn btn-primary">
        ➕ Tạo sản phẩm đầu tiên
      </router-link>
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
          class="btn"
          :class="{ 'btn-primary': page === currentPage, 'btn-outline': page !== currentPage }"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        @click="changePage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="btn btn-outline"
      >
        Tiếp →
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useSellerStore } from '@/stores/seller'
import { useNotificationStore } from '@/stores/notifications'

const router = useRouter()
const sellerStore = useSellerStore()
const notificationStore = useNotificationStore()

// Reactive data
const loading = ref(true)
const searchQuery = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const sortBy = ref('newest')
const selectedProducts = ref([])
const currentPage = ref(1)
const itemsPerPage = ref(12)

// Categories for filter
const categories = ref([
  { id: 1, name: 'Điện tử' },
  { id: 2, name: 'Thời trang' },
  { id: 3, name: 'Nhà cửa' },
  { id: 4, name: 'Sách' },
  { id: 5, name: 'Thể thao' }
])

// Computed properties
const products = computed(() => sellerStore.products)
const stats = computed(() => sellerStore.productStats)

const filteredProducts = computed(() => {
  let filtered = [...products.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(product => 
      product.name.toLowerCase().includes(query) ||
      product.category.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (statusFilter.value) {
    filtered = filtered.filter(product => 
      statusFilter.value === 'active' ? product.isActive : !product.isActive
    )
  }

  // Category filter
  if (categoryFilter.value) {
    filtered = filtered.filter(product => 
      product.category === categoryFilter.value
    )
  }

  // Sort
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.createdAt) - new Date(a.createdAt)
      case 'oldest':
        return new Date(a.createdAt) - new Date(b.createdAt)
      case 'name':
        return a.name.localeCompare(b.name)
      case 'price-high':
        return b.price - a.price
      case 'price-low':
        return a.price - b.price
      case 'stock':
        return b.stockQuantity - a.stockQuantity
      default:
        return 0
    }
  })

  return filtered
})

const totalPages = computed(() => Math.ceil(filteredProducts.value.length / itemsPerPage.value))

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredProducts.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// Methods
const searchProducts = () => {
  currentPage.value = 1
}

const filterProducts = () => {
  currentPage.value = 1
}

const sortProducts = () => {
  currentPage.value = 1
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const viewProduct = (product) => {
  // Navigate to product detail or open modal
  router.push(`/products/${product.id}`)
}

const toggleProductStatus = async (product) => {
  try {
    await sellerStore.toggleProductStatus(product.id)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã ${product.isActive ? 'tạm ngưng' : 'kích hoạt'} sản phẩm "${product.name}"`
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi cập nhật trạng thái sản phẩm'
    })
  }
}

const duplicateProduct = async (product) => {
  try {
    await sellerStore.duplicateProduct(product.id)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã nhân bản sản phẩm "${product.name}"`
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi nhân bản sản phẩm'
    })
  }
}

const deleteProduct = async (product) => {
  if (!confirm(`Bạn có chắc muốn xóa sản phẩm "${product.name}"? Hành động này không thể hoàn tác.`)) {
    return
  }

  try {
    await sellerStore.deleteProduct(product.id)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã xóa sản phẩm "${product.name}"`
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi xóa sản phẩm'
    })
  }
}

// Bulk actions
const bulkActivate = async () => {
  try {
    await sellerStore.bulkUpdateStatus(selectedProducts.value, true)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã kích hoạt ${selectedProducts.value.length} sản phẩm`
    })
    selectedProducts.value = []
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi cập nhật sản phẩm'
    })
  }
}

const bulkDeactivate = async () => {
  try {
    await sellerStore.bulkUpdateStatus(selectedProducts.value, false)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã tạm ngưng ${selectedProducts.value.length} sản phẩm`
    })
    selectedProducts.value = []
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi cập nhật sản phẩm'
    })
  }
}

const bulkDelete = async () => {
  if (!confirm(`Bạn có chắc muốn xóa ${selectedProducts.value.length} sản phẩm? Hành động này không thể hoàn tác.`)) {
    return
  }

  try {
    await sellerStore.bulkDelete(selectedProducts.value)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã xóa ${selectedProducts.value.length} sản phẩm`
    })
    selectedProducts.value = []
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi xóa sản phẩm'
    })
  }
}

// Utility methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const getStatusClass = (isActive) => {
  return isActive ? 'status-active' : 'status-inactive'
}

const getStockClass = (stock) => {
  if (stock === 0) return 'stock-out'
  if (stock < 10) return 'stock-low'
  return 'stock-good'
}

const handleImageError = (event) => {
  event.target.src = '/placeholder-product.jpg'
}

// Lifecycle
onMounted(async () => {
  try {
    await sellerStore.fetchProducts()
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi tải danh sách sản phẩm'
    })
  } finally {
    loading.value = false
  }
})

// Watchers
watch(() => filteredProducts.value.length, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = 1
  }
})
</script>

<style scoped>
.my-products {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.page-header h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: var(--text-secondary, #a0aec0);
  margin: 0;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-number {
  color: var(--text-primary, #ffffff);
  font-size: 1.5rem;
  font-weight: 600;
}

.stat-label {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
}

/* Filters Section */
.filters-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.search-bar {
  margin-bottom: 1rem;
}

.search-bar input {
  width: 100%;
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary, #ffffff);
  font-size: 1rem;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.filters select {
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary, #ffffff);
}

.bulk-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 8px;
}

.selected-count {
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.product-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
}

.product-card:hover {
  border-color: rgba(0, 212, 255, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 212, 255, 0.1);
}

.product-card.selected {
  border-color: var(--text-accent, #00d4ff);
  background: rgba(0, 212, 255, 0.05);
}

.product-checkbox {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  z-index: 2;
}

.product-checkbox input {
  width: 20px;
  height: 20px;
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

.image-overlay {
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

.product-card:hover .image-overlay {
  opacity: 1;
}

.btn-overlay {
  background: rgba(0, 212, 255, 0.8);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
}

.product-info {
  padding: 1.5rem;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.product-header h3 {
  color: var(--text-primary, #ffffff);
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.product-status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-left: 0.5rem;
}

.status-active {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.status-inactive {
  background: rgba(251, 146, 60, 0.2);
  color: #fb923c;
}

.product-category {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.product-metrics {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-label {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
}

.metric-value {
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

.metric-value.price {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
}

.stock-out {
  color: #ef4444;
}

.stock-low {
  color: #f59e0b;
}

.stock-good {
  color: #22c55e;
}

.product-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.product-actions .btn {
  flex: 1;
  min-width: 0;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  text-align: center;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
}

.btn-edit {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.btn-edit:hover {
  background: rgba(0, 212, 255, 0.3);
}

.btn-toggle {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.btn-secondary {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.btn-success {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.btn-warning {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 2rem;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.pagination .btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  background: rgba(26, 26, 46, 0.6);
  color: var(--text-primary, #ffffff);
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination .btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.2);
}

.pagination .btn.btn-primary {
  background: var(--text-accent, #00d4ff);
  color: #000;
}

.pagination .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 212, 255, 0.2);
  border-top: 3px solid var(--text-accent, #00d4ff);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .my-products {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-actions {
    flex-direction: column;
  }

  .bulk-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>