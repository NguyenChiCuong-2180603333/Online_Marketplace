<template>
  <div class="my-products">
    <!-- Header -->
    <div class="page-header">
      <h1>📦 Sản phẩm của tôi</h1>
      <p>Quản lý toàn bộ sản phẩm bạn đang bán</p>
      <button @click="openCreateModal" class="btn btn-primary">➕ Tạo sản phẩm mới</button>
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
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Đang tải sản phẩm...</p>
    </div>

    <!-- Products Grid -->
    <div v-else-if="filteredProducts.length > 0" class="products-grid">
      <div v-for="product in paginatedProducts" :key="product.id" class="product-card">
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
            <button @click="openEditModal(product)" class="btn btn-edit">✏️ Sửa</button>
            <button
              @click="toggleProductStatus(product)"
              class="btn btn-toggle"
              :class="product.isActive ? 'btn-warning' : 'btn-success'"
            >
              {{ product.isActive ? '⏸️ Tạm ngưng' : '▶️ Kích hoạt' }}
            </button>
            <button @click="duplicateProduct(product)" class="btn btn-secondary">
              📋 Nhân bản
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
      <button @click="openCreateModal" class="btn btn-primary">➕ Tạo sản phẩm đầu tiên</button>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button @click="currentPage--" :disabled="currentPage === 1" class="btn">← Trước</button>

      <div class="page-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="currentPage = page"
          class="btn"
          :class="{ 'btn-primary': page === currentPage }"
        >
          {{ page }}
        </button>
      </div>

      <button @click="currentPage++" :disabled="currentPage === totalPages" class="btn">
        Sau →
      </button>
    </div>

    <!-- Create/Edit Product Modal -->
    <div v-if="showProductModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditMode ? '✏️ Chỉnh sửa sản phẩm' : '➕ Thêm sản phẩm mới' }}</h3>
          <button @click="closeModal" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitProductForm">
            <div class="form-grid">
              <div class="form-group">
                <label>Tên sản phẩm *</label>
                <input
                  v-model="productForm.name"
                  type="text"
                  placeholder="Nhập tên sản phẩm..."
                  required
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label>Danh mục *</label>
                <select v-model="productForm.category" required class="form-select">
                  <option value="">Chọn danh mục</option>
                  <option v-for="category in categories" :key="category.id" :value="category.name">
                    {{ category.name }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>Giá (VNĐ) *</label>
                <input
                  v-model.number="productForm.price"
                  type="number"
                  min="0"
                  step="1000"
                  placeholder="0"
                  required
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label>Số lượng tồn kho *</label>
                <input
                  v-model.number="productForm.stockQuantity"
                  type="number"
                  min="0"
                  placeholder="0"
                  required
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label>Mô tả sản phẩm</label>
              <textarea
                v-model="productForm.description"
                placeholder="Nhập mô tả chi tiết về sản phẩm..."
                rows="4"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-group">
              <label>Hình ảnh sản phẩm</label>
              <div class="image-upload">
                <input
                  @change="handleImageUpload"
                  type="file"
                  multiple
                  accept="image/*"
                  class="file-input"
                  id="image-upload"
                />
                <label for="image-upload" class="upload-btn"> 📷 Chọn hình ảnh </label>
                <p class="upload-hint">Có thể chọn nhiều hình ảnh</p>
              </div>

              <div v-if="productForm.images.length" class="image-preview">
                <div v-for="(image, index) in productForm.images" :key="index" class="preview-item">
                  <img :src="image" :alt="`Preview ${index + 1}`" />
                  <button @click="removeImage(index)" type="button" class="remove-image">✕</button>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeModal" class="btn btn-cancel">Hủy</button>
              <button type="submit" :disabled="submitting" class="btn btn-primary">
                {{
                  submitting ? '⏳ Đang lưu...' : isEditMode ? '💾 Cập nhật' : '➕ Thêm sản phẩm'
                }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useSellerStore } from '@/stores/seller'
import { useNotificationStore } from '@/stores/notifications'
import { categoryAPI } from '@/services/api'

const router = useRouter()
const sellerStore = useSellerStore()
const notificationStore = useNotificationStore()

// Reactive data
const loading = ref(true)

// Modal form data
const showProductModal = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)
const isDragOver = ref(false)
const editingProductId = ref(null)

// Product form
const productForm = ref({
  name: '',
  description: '',
  category: '',
  brand: '',
  price: 0,
  stockQuantity: 0,
  images: [],
})

// File input ref - removed since we don't need it anymore
const searchQuery = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const sortBy = ref('newest')
const selectedProducts = ref([])
const currentPage = ref(1)
const itemsPerPage = ref(12)

// Categories for filter
const categories = ref([])
const loadingCategories = ref(false)

// Computed properties
const products = computed(() => sellerStore.products)
const stats = computed(() => sellerStore.productStats)

const filteredProducts = computed(() => {
  let filtered = [...products.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (product) =>
        product.name.toLowerCase().includes(query) || product.category.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (statusFilter.value) {
    filtered = filtered.filter((product) =>
      statusFilter.value === 'active' ? product.isActive : !product.isActive
    )
  }

  // Category filter
  if (categoryFilter.value) {
    filtered = filtered.filter((product) => product.category === categoryFilter.value)
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
      message: `Đã ${product.isActive ? 'tạm ngưng' : 'kích hoạt'} sản phẩm "${product.name}"`,
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi cập nhật trạng thái sản phẩm',
    })
  }
}

const duplicateProduct = async (product) => {
  try {
    await sellerStore.duplicateProduct(product.id)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã nhân bản sản phẩm "${product.name}"`,
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi nhân bản sản phẩm',
    })
  }
}

const deleteProduct = async (product) => {
  if (
    !confirm(`Bạn có chắc muốn xóa sản phẩm "${product.name}"? Hành động này không thể hoàn tác.`)
  ) {
    return
  }

  try {
    await sellerStore.deleteProduct(product.id)
    notificationStore.addNotification({
      type: 'success',
      message: `Đã xóa sản phẩm "${product.name}"`,
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi xóa sản phẩm',
    })
  }
}

// Utility methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
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

onMounted(async () => {
  console.log('MyProducts mounted, fetching products...')
  try {
    await sellerStore.fetchProducts()
    console.log('fetchProducts DONE')
  } catch (error) {
    console.error('fetchProducts ERROR', error)
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi tải danh sách sản phẩm',
    })
  } finally {
    loading.value = false
  }
})

// Watchers
watch(
  () => filteredProducts.value.length,
  () => {
    if (currentPage.value > totalPages.value) {
      currentPage.value = 1
    }
  }
)

// Modal form methods
const loadCategories = async () => {
  loadingCategories.value = true
  try {
    const response = await categoryAPI.getAll()
    categories.value = response.data || []
  } catch (error) {
    categories.value = []
  } finally {
    loadingCategories.value = false
  }
}

const openCreateModal = async () => {
  isEditMode.value = false
  editingProductId.value = null
  resetForm()
  await loadCategories()
  showProductModal.value = true
}

const openEditModal = async (product) => {
  isEditMode.value = true
  editingProductId.value = product.id
  populateForm(product)
  await loadCategories()
  showProductModal.value = true
}

const closeModal = () => {
  showProductModal.value = false
  resetForm()
}

const resetForm = () => {
  productForm.value = {
    name: '',
    description: '',
    category: '',
    brand: '',
    price: 0,
    stockQuantity: 0,
    images: [],
  }
}

const populateForm = (product) => {
  productForm.value = {
    name: product.name || '',
    description: product.description || '',
    category: product.category || '',
    brand: product.brand || '',
    price: product.price || 0,
    stockQuantity: product.stockQuantity || 0,
    images: product.images || [],
  }
}

const submitProductForm = async () => {
  submitting.value = true

  try {
    if (isEditMode.value) {
      await sellerStore.updateProduct(editingProductId.value, productForm.value)
      notificationStore.addNotification({
        type: 'success',
        message: 'Đã cập nhật sản phẩm thành công',
      })
    } else {
      await sellerStore.createProduct(productForm.value)
      notificationStore.addNotification({
        type: 'success',
        message: 'Đã tạo sản phẩm mới thành công',
      })
    }

    closeModal()
    await sellerStore.fetchProducts() // Refresh the list
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi lưu sản phẩm',
    })
  } finally {
    submitting.value = false
  }
}

// Image upload methods
const handleImageUpload = async (event) => {
  const files = Array.from(event.target.files)
  const token = localStorage.getItem('token')
  for (const file of files) {
    if (file.type.startsWith('image/')) {
      if (file.size > 5 * 1024 * 1024) {
        alert(`File ${file.name} quá lớn. Kích thước tối đa 5MB.`)
        continue
      }
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await fetch('http://localhost:8080/api/upload/image', {
          method: 'POST',
          body: formData,
          headers: token ? { Authorization: `Bearer ${token}` } : {},
          credentials: 'include',
        })
        const data = await res.json()
        if (data.imageUrl) {
          productForm.value.images.push(data.imageUrl)
        } else {
          alert(`Lỗi upload ảnh: ${file.name}`)
        }
      } catch (e) {
        alert(`Lỗi upload ảnh: ${file.name}`)
      }
    }
  }
}

const removeImage = (index) => {
  productForm.value.images.splice(index, 1)
}
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.modal-header h3 {
  color: var(--text-accent);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: color 0.3s ease;
}

.modal-close:hover {
  color: var(--text-accent);
}

.modal-body {
  padding: 1.5rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.3);
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

.image-upload {
  margin-bottom: 1rem;
}

.file-input {
  display: none;
}

.upload-btn {
  display: inline-block;
  padding: 0.75rem 1rem;
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid var(--text-accent);
  border-radius: 6px;
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background: var(--text-accent);
  color: white;
}

.upload-hint {
  color: var(--text-secondary);
  font-size: 0.8rem;
  margin-top: 0.5rem;
}

.image-preview {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(239, 68, 68, 0.8);
  border: none;
  border-radius: 50%;
  color: white;
  width: 20px;
  height: 20px;
  font-size: 0.7rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-cancel {
  padding: 0.75rem 1.5rem;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid #ef4444;
  border-radius: 6px;
  color: #ef4444;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #ef4444;
  color: white;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: var(--text-accent);
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.8);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive Modal */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
