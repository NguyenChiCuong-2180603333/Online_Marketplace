<template>
  <div class="admin-products">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <h1>📦 Quản lý sản phẩm</h1>
          <p>Quản lý tất cả sản phẩm trong hệ thống</p>
        </div>
        <button @click="showCreateModal = true" class="btn-create">
          ➕ Thêm sản phẩm mới
        </button>
      </div>

      <!-- Filter và Search -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm theo tên sản phẩm, danh mục..."
            class="search-input"
          >
          <button @click="searchProducts" class="search-btn">🔍</button>
        </div>
        
        <div class="filters">
          <select v-model="statusFilter" @change="filterProducts" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Đang bán</option>
            <option value="inactive">Ngừng bán</option>
          </select>

          <select v-model="categoryFilter" @change="filterProducts" class="filter-select">
            <option value="">Tất cả danh mục</option>
            <option v-for="category in categories" :key="category.id" :value="category.name">
              {{ category.name }}
            </option>
          </select>

          <select v-model="sortBy" @change="sortProducts" class="filter-select">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="name">Tên A-Z</option>
            <option value="price-asc">Giá thấp → cao</option>
            <option value="price-desc">Giá cao → thấp</option>
            <option value="stock">Tồn kho</option>
          </select>
        </div>
      </div>

      <!-- Statistics -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <h3>{{ stats.total }}</h3>
            <p>Tổng sản phẩm</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <h3>{{ stats.active }}</h3>
            <p>Đang bán</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">❌</div>
          <div class="stat-info">
            <h3>{{ stats.inactive }}</h3>
            <p>Ngừng bán</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⚠️</div>
          <div class="stat-info">
            <h3>{{ stats.lowStock }}</h3>
            <p>Sắp hết hàng</p>
          </div>
        </div>
      </div>

      <!-- Products Table -->
      <div class="products-table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải sản phẩm...</p>
        </div>

        <div v-else-if="filteredProducts.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <h3>Không có sản phẩm nào</h3>
          <p>Chưa có sản phẩm nào trong hệ thống. Hãy thêm sản phẩm đầu tiên!</p>
          <button @click="showCreateModal = true" class="btn-add-first">➕ Thêm sản phẩm đầu tiên</button>
        </div>

        <div v-else class="products-table">
          <table>
            <thead>
              <tr>
                <th>Hình ảnh</th>
                <th>Thông tin sản phẩm</th>
                <th>Danh mục</th>
                <th>Giá</th>
                <th>Tồn kho</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in paginatedProducts" :key="product.id" class="product-row">
                <td>
                  <div class="product-image">
                    <img 
                      :src="product.images?.[0] || '/placeholder-product.jpg'" 
                      :alt="product.name"
                      @error="handleImageError"
                    />
                  </div>
                </td>
                <td>
                  <div class="product-info">
                    <h4>{{ product.name }}</h4>
                    <p>{{ truncateText(product.description, 100) }}</p>
                    <span class="product-id">ID: {{ product.id }}</span>
                  </div>
                </td>
                <td>
                  <span class="category-tag">{{ product.category }}</span>
                </td>
                <td>
                  <div class="price-info">
                    <span class="price">{{ formatCurrency(product.price) }}</span>
                  </div>
                </td>
                <td>
                  <div class="stock-info">
                    <span class="stock-number" :class="{ 'low-stock': product.stockQuantity <= 10 }">
                      {{ product.stockQuantity }}
                    </span>
                    <span v-if="product.stockQuantity <= 10" class="low-stock-badge">⚠️ Ít</span>
                  </div>
                </td>
                <td>
                  <span class="status-badge" :class="{ active: product.isActive, inactive: !product.isActive }">
                    {{ product.isActive ? '✅ Đang bán' : '❌ Ngừng bán' }}
                  </span>
                </td>
                <td>
                  <div class="date-info">
                    <span class="date">{{ formatDate(product.createdAt) }}</span>
                  </div>
                </td>
                <td>
                  <div class="action-buttons">
                    <button @click="viewProduct(product)" class="btn btn-view" title="Xem chi tiết">👁️</button>
                    <button @click="editProduct(product)" class="btn btn-edit" title="Chỉnh sửa">✏️</button>
                    <button @click="toggleProductStatus(product)" class="btn btn-toggle" title="Bật/Tắt">
                      {{ product.isActive ? '❌' : '✅' }}
                    </button>
                    <button @click="deleteProduct(product)" class="btn btn-delete" title="Xóa">🗑️</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="currentPage = Math.max(1, currentPage - 1)" 
          :disabled="currentPage === 1"
          class="page-btn"
        >
          « Trước
        </button>
        
        <span class="page-info">
          Trang {{ currentPage }} / {{ totalPages }}
        </span>
        
        <button 
          @click="currentPage = Math.min(totalPages, currentPage + 1)" 
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          Sau »
        </button>
      </div>
    </div>

    <!-- Create/Edit Product Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeCreateEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showCreateModal ? '➕ Thêm sản phẩm mới' : '✏️ Chỉnh sửa sản phẩm' }}</h3>
          <button @click="closeCreateEditModal" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitProduct">
            <div class="form-grid">
              <div class="form-group">
                <label>Tên sản phẩm *</label>
                <input 
                  v-model="productForm.name" 
                  type="text" 
                  placeholder="Nhập tên sản phẩm..."
                  required
                  class="form-input"
                >
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
                >
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
                >
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
                >
                <label for="image-upload" class="upload-btn">
                  📷 Chọn hình ảnh
                </label>
                <p class="upload-hint">Có thể chọn nhiều hình ảnh</p>
              </div>
              
              <div v-if="productForm.images.length" class="image-preview">
                <div v-for="(image, index) in productForm.images" :key="index" class="preview-item">
                  <img :src="image" :alt="`Preview ${index + 1}`">
                  <button @click="removeImage(index)" type="button" class="remove-image">✕</button>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeCreateEditModal" class="btn btn-cancel">
                Hủy
              </button>
              <button type="submit" :disabled="submitting" class="btn btn-primary">
                {{ submitting ? '⏳ Đang lưu...' : (showCreateModal ? '➕ Thêm sản phẩm' : '💾 Cập nhật') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Product Detail Modal -->
    <div v-if="selectedProduct" class="modal-overlay" @click="selectedProduct = null">
      <div class="modal-content detail-modal" @click.stop>
        <div class="modal-header">
          <h3>👁️ Chi tiết sản phẩm</h3>
          <button @click="selectedProduct = null" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <div class="product-detail">
            <div class="detail-images">
              <img 
                :src="selectedProduct.images?.[0] || '/placeholder-product.jpg'" 
                :alt="selectedProduct.name"
              >
            </div>
            <div class="detail-info">
              <h3>{{ selectedProduct.name }}</h3>
              <p class="detail-category">📂 {{ selectedProduct.category }}</p>
              <p class="detail-price">💰 {{ formatCurrency(selectedProduct.price) }}</p>
              <p class="detail-stock">📦 Tồn kho: {{ selectedProduct.stockQuantity }}</p>
              <p class="detail-status">
                <span :class="{ active: selectedProduct.isActive, inactive: !selectedProduct.isActive }">
                  {{ selectedProduct.isActive ? '✅ Đang bán' : '❌ Ngừng bán' }}
                </span>
              </p>
              <div class="detail-description">
                <h4>Mô tả:</h4>
                <p>{{ selectedProduct.description || 'Chưa có mô tả' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'AdminProducts',
  setup() {
    // Reactive data
    const products = ref([])
    const categories = ref([
      { id: 1, name: 'Electronics' },
      { id: 2, name: 'Fashion' },
      { id: 3, name: 'Books' },
      { id: 4, name: 'Home & Garden' },
      { id: 5, name: 'Sports' }
    ])
    const filteredProducts = ref([])
    const loading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    const sortBy = ref('newest')
    const currentPage = ref(1)
    const itemsPerPage = 12
    const selectedProduct = ref(null)
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const submitting = ref(false)
    
    const productForm = ref({
      id: null,
      name: '',
      category: '',
      price: 0,
      stockQuantity: 0,
      description: '',
      images: []
    })
    
    const stats = ref({
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0
    })

    // Computed
    const paginatedProducts = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return filteredProducts.value.slice(start, end)
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredProducts.value.length / itemsPerPage)
    })

    // Methods
    const loadProducts = () => {
      loading.value = true
      // Mock data for demo
      setTimeout(() => {
        products.value = [
          {
            id: 1,
            name: 'iPhone 15 Pro Max',
            category: 'Electronics',
            price: 35000000,
            stockQuantity: 25,
            description: 'Điện thoại thông minh cao cấp từ Apple với chip A17 Pro',
            images: ['/placeholder-product.jpg'],
            isActive: true,
            createdAt: new Date('2024-01-15')
          },
          {
            id: 2,
            name: 'Samsung Galaxy S24 Ultra',
            category: 'Electronics',
            price: 32000000,
            stockQuantity: 8,
            description: 'Flagship Android với bút S Pen và camera 200MP',
            images: ['/placeholder-product.jpg'],
            isActive: true,
            createdAt: new Date('2024-01-20')
          },
          {
            id: 3,
            name: 'MacBook Pro M3',
            category: 'Electronics',
            price: 45000000,
            stockQuantity: 3,
            description: 'Laptop professional với chip M3 mạnh mẽ',
            images: ['/placeholder-product.jpg'],
            isActive: false,
            createdAt: new Date('2024-01-10')
          }
        ]
        filteredProducts.value = [...products.value]
        calculateStats()
        loading.value = false
      }, 1000)
    }

    const calculateStats = () => {
      stats.value.total = products.value.length
      stats.value.active = products.value.filter(p => p.isActive).length
      stats.value.inactive = products.value.filter(p => !p.isActive).length
      stats.value.lowStock = products.value.filter(p => p.stockQuantity <= 10).length
    }

    const filterProducts = () => {
      let filtered = [...products.value]

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(product => 
          product.name.toLowerCase().includes(query) ||
          product.description.toLowerCase().includes(query) ||
          product.category.toLowerCase().includes(query)
        )
      }

      if (statusFilter.value) {
        filtered = filtered.filter(product => 
          statusFilter.value === 'active' ? product.isActive : !product.isActive
        )
      }

      if (categoryFilter.value) {
        filtered = filtered.filter(product => 
          product.category === categoryFilter.value
        )
      }

      filteredProducts.value = filtered
      currentPage.value = 1
    }

    const searchProducts = () => {
      filterProducts()
    }

    const sortProducts = () => {
      // Implementation for sorting
      filterProducts()
    }

    const viewProduct = (product) => {
      selectedProduct.value = product
    }

    const editProduct = (product) => {
      productForm.value = {
        id: product.id,
        name: product.name,
        category: product.category,
        price: product.price,
        stockQuantity: product.stockQuantity,
        description: product.description,
        images: product.images || []
      }
      showEditModal.value = true
    }

    const toggleProductStatus = (product) => {
      const action = product.isActive ? 'ngừng bán' : 'bán lại'
      if (!confirm(`Bạn có chắc chắn muốn ${action} sản phẩm "${product.name}"?`)) return

      product.isActive = !product.isActive
      calculateStats()
      alert(`Đã ${action} sản phẩm thành công`)
    }

    const deleteProduct = (product) => {
      if (!confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?\nHành động này không thể hoàn tác!`)) return

      const index = products.value.findIndex(p => p.id === product.id)
      if (index !== -1) {
        products.value.splice(index, 1)
        filterProducts()
        calculateStats()
        alert('Đã xóa sản phẩm thành công')
      }
    }

    const submitProduct = () => {
      if (!productForm.value.name || !productForm.value.category || !productForm.value.price) {
        alert('Vui lòng điền đầy đủ thông tin bắt buộc')
        return
      }

      submitting.value = true
      
      setTimeout(() => {
        if (showCreateModal.value) {
          // Add new product
          const newProduct = {
            ...productForm.value,
            id: Date.now(),
            isActive: true,
            createdAt: new Date()
          }
          products.value.push(newProduct)
          alert('Đã thêm sản phẩm thành công')
        } else {
          // Update existing product
          const index = products.value.findIndex(p => p.id === productForm.value.id)
          if (index !== -1) {
            products.value[index] = { ...products.value[index], ...productForm.value }
            alert('Đã cập nhật sản phẩm thành công')
          }
        }
        
        filterProducts()
        calculateStats()
        closeCreateEditModal()
        submitting.value = false
      }, 1000)
    }

    const closeCreateEditModal = () => {
      showCreateModal.value = false
      showEditModal.value = false
      productForm.value = {
        id: null,
        name: '',
        category: '',
        price: 0,
        stockQuantity: 0,
        description: '',
        images: []
      }
    }

    const handleImageUpload = (event) => {
      const files = Array.from(event.target.files)
      files.forEach(file => {
        if (file.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            productForm.value.images.push(e.target.result)
          }
          reader.readAsDataURL(file)
        }
      })
    }

    const removeImage = (index) => {
      productForm.value.images.splice(index, 1)
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    const formatDate = (date) => {
      return new Intl.DateTimeFormat('vi-VN').format(new Date(date))
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    // Lifecycle
    onMounted(() => {
      loadProducts()
    })

    return {
      products,
      categories,
      filteredProducts,
      loading,
      searchQuery,
      statusFilter,
      categoryFilter,
      sortBy,
      currentPage,
      itemsPerPage,
      selectedProduct,
      showCreateModal,
      showEditModal,
      submitting,
      productForm,
      stats,
      paginatedProducts,
      totalPages,
      loadProducts,
      calculateStats,
      filterProducts,
      searchProducts,
      sortProducts,
      viewProduct,
      editProduct,
      toggleProductStatus,
      deleteProduct,
      submitProduct,
      closeCreateEditModal,
      handleImageUpload,
      removeImage,
      handleImageError,
      formatCurrency,
      formatDate,
      truncateText
    }
  }
}
</script>

<style scoped>
.admin-products {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-info h1 {
  font-size: 2.5rem;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.header-info p {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.btn-create {
  padding: 0.75rem 1.5rem;
  background: var(--text-accent);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-create:hover {
  background: rgba(0, 212, 255, 0.8);
  transform: translateY(-2px);
}

.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px 0 0 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
}

.search-btn {
  padding: 0.75rem 1rem;
  background: var(--text-accent);
  border: none;
  border-radius: 0 8px 8px 0;
  color: white;
  cursor: pointer;
}

.filters {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
  opacity: 0.8;
}

.stat-info h3 {
  font-size: 1.8rem;
  color: var(--text-accent);
  margin-bottom: 0.25rem;
}

.stat-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.products-table-container {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 3rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 212, 255, 0.3);
  border-top: 4px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.btn-add-first {
  padding: 0.75rem 1.5rem;
  background: var(--text-accent);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-add-first:hover {
  background: rgba(0, 212, 255, 0.8);
  transform: translateY(-2px);
}

.products-table {
  width: 100%;
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
}

.products-table th,
.products-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.products-table th {
  background: rgba(0, 0, 0, 0.3);
  color: var(--text-accent);
  font-weight: 600;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

.product-info p {
  color: var(--text-secondary);
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.product-id {
  color: var(--text-secondary);
  font-size: 0.7rem;
  opacity: 0.7;
}

.category-tag {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.price {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 1rem;
}

.stock-number {
  font-weight: 600;
}

.stock-number.low-stock {
  color: #ffc107;
}

.low-stock-badge {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
  padding: 0.15rem 0.3rem;
  border-radius: 3px;
  font-size: 0.7rem;
  margin-left: 0.5rem;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.inactive {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-view {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
}

.btn-edit {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
}

.btn-toggle {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.btn-delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.btn:hover {
  transform: translateY(-1px);
  opacity: 0.8;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: var(--text-accent);
  color: white;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: var(--text-secondary);
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

.detail-modal {
  max-width: 800px;
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
}

.detail-images img {
  width: 100%;
  border-radius: 8px;
}

.detail-info h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.detail-category,
.detail-price,
.detail-stock,
.detail-status {
  margin-bottom: 0.75rem;
  font-size: 1.1rem;
}

.detail-description {
  margin-top: 1.5rem;
}

.detail-description h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.detail-description p {
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .product-detail {
    grid-template-columns: 1fr;
  }
}
</style>