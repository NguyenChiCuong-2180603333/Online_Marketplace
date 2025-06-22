<template>
  <div class="admin-products">
    <div class="container">
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

          <button @click="exportProducts" class="export-btn">📊 Xuất Excel</button>
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
          <p>Chưa có sản phẩm nào phù hợp với bộ lọc hiện tại</p>
          <button @click="clearFilters" class="btn-clear">🔄 Xóa bộ lọc</button>
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
                <th>Đánh giá</th>
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
                      class="product-thumbnail"
                      @error="handleImageError"
                    >
                  </div>
                </td>
                
                <td>
                  <div class="product-info">
                    <h4>{{ product.name }}</h4>
                    <p class="product-description">{{ truncateText(product.description, 100) }}</p>
                    <small class="product-id">ID: {{ product.id?.substring(0, 8) }}</small>
                  </div>
                </td>
                
                <td>
                  <span class="category-badge">{{ product.category }}</span>
                </td>
                
                <td>
                  <div class="product-price">
                    <strong>{{ formatCurrency(product.price) }}</strong>
                  </div>
                </td>
                
                <td>
                  <div class="stock-info">
                    <span 
                      class="stock-badge" 
                      :class="getStockClass(product.stockQuantity)"
                    >
                      {{ product.stockQuantity }}
                    </span>
                    <small v-if="product.stockQuantity <= 10" class="low-stock-warning">
                      ⚠️ Sắp hết
                    </small>
                  </div>
                </td>
                
                <td>
                  <button 
                    @click="toggleProductStatus(product)"
                    class="status-toggle"
                    :class="product.isActive ? 'status-active' : 'status-inactive'"
                  >
                    {{ product.isActive ? '✅ Đang bán' : '❌ Ngừng bán' }}
                  </button>
                </td>
                
                <td>
                  <div class="rating-info">
                    <div class="stars">
                      <span>⭐ {{ product.averageRating?.toFixed(1) || '0.0' }}</span>
                    </div>
                    <small>({{ product.reviewCount || 0 }} đánh giá)</small>
                  </div>
                </td>
                
                <td>
                  <div class="created-date">
                    <div>{{ formatDate(product.createdAt) }}</div>
                    <small>{{ formatTime(product.createdAt) }}</small>
                  </div>
                </td>
                
                <td>
                  <div class="action-buttons">
                    <button @click="viewProduct(product)" class="btn-view" title="Xem chi tiết">👁️</button>
                    <button @click="editProduct(product)" class="btn-edit" title="Chỉnh sửa">✏️</button>
                    <button @click="viewReviews(product.id)" class="btn-reviews" title="Xem đánh giá">💬</button>
                    <button @click="deleteProduct(product)" class="btn-delete" title="Xóa sản phẩm">🗑️</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            @click="currentPage = 1" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ⏪
          </button>
          <button 
            @click="currentPage--" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ◀️
          </button>
          
          <span class="page-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            @click="currentPage++" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ▶️
          </button>
          <button 
            @click="currentPage = totalPages" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ⏩
          </button>
        </div>
      </div>
    </div>

    <!-- Product Detail Modal -->
    <div v-if="selectedProduct" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết sản phẩm</h2>
          <button @click="closeModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="product-detail">
            <div class="product-images">
              <div class="main-image">
                <img :src="selectedProduct.images?.[0] || '/placeholder-product.jpg'" :alt="selectedProduct.name">
              </div>
              <div v-if="selectedProduct.images?.length > 1" class="image-gallery">
                <img 
                  v-for="(image, index) in selectedProduct.images" 
                  :key="index"
                  :src="image" 
                  :alt="`${selectedProduct.name} ${index + 1}`"
                  class="gallery-image"
                >
              </div>
            </div>
            
            <div class="product-details">
              <div class="detail-section">
                <h3>📋 Thông tin cơ bản</h3>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>Tên sản phẩm:</label>
                    <span>{{ selectedProduct.name }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Danh mục:</label>
                    <span>{{ selectedProduct.category }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Giá bán:</label>
                    <span>{{ formatCurrency(selectedProduct.price) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>Tồn kho:</label>
                    <span>{{ selectedProduct.stockQuantity }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h3>📝 Mô tả</h3>
                <p>{{ selectedProduct.description }}</p>
              </div>

              <div class="detail-section">
                <h3>⭐ Đánh giá</h3>
                <div class="rating-summary">
                  <div class="rating-score">
                    <span class="score">{{ selectedProduct.averageRating?.toFixed(1) || '0.0' }}</span>
                    <div class="stars">⭐⭐⭐⭐⭐</div>
                    <span>({{ selectedProduct.reviewCount || 0 }} đánh giá)</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h3>📊 Thống kê</h3>
                <div class="stats-row">
                  <div class="stat-item">
                    <span class="stat-number">{{ productStats.views || 0 }}</span>
                    <span class="stat-label">Lượt xem</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ productStats.orders || 0 }}</span>
                    <span class="stat-label">Đã bán</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ formatCurrency(productStats.revenue || 0) }}</span>
                    <span class="stat-label">Doanh thu</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Product Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeCreateEditModal">
      <div class="modal-content create-edit-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ showCreateModal ? '➕ Thêm sản phẩm mới' : '✏️ Chỉnh sửa sản phẩm' }}</h2>
          <button @click="closeCreateEditModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="submitProduct">
            <div class="form-grid">
              <div class="form-group">
                <label for="productName">Tên sản phẩm *</label>
                <input 
                  id="productName"
                  v-model="productForm.name" 
                  type="text" 
                  required
                  placeholder="Nhập tên sản phẩm"
                  class="form-input"
                >
              </div>

              <div class="form-group">
                <label for="productCategory">Danh mục *</label>
                <select id="productCategory" v-model="productForm.category" required class="form-select">
                  <option value="">Chọn danh mục</option>
                  <option v-for="category in categories" :key="category.id" :value="category.name">
                    {{ category.name }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label for="productPrice">Giá bán *</label>
                <input 
                  id="productPrice"
                  v-model.number="productForm.price" 
                  type="number" 
                  required
                  min="0"
                  placeholder="0"
                  class="form-input"
                >
              </div>

              <div class="form-group">
                <label for="productStock">Tồn kho *</label>
                <input 
                  id="productStock"
                  v-model.number="productForm.stockQuantity" 
                  type="number" 
                  required
                  min="0"
                  placeholder="0"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label for="productDescription">Mô tả sản phẩm *</label>
              <textarea 
                id="productDescription"
                v-model="productForm.description" 
                required
                placeholder="Nhập mô tả chi tiết về sản phẩm"
                class="form-textarea"
                rows="4"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="productImages">Hình ảnh sản phẩm</label>
              <div class="image-upload">
                <input 
                  id="productImages"
                  type="file" 
                  multiple 
                  accept="image/*"
                  @change="handleImageUpload"
                  class="file-input"
                >
                <div class="upload-text">
                  📷 Chọn hoặc kéo thả hình ảnh vào đây
                </div>
                <div class="image-preview" v-if="productForm.images?.length > 0">
                  <div v-for="(image, index) in productForm.images" :key="index" class="preview-item">
                    <img :src="image" :alt="`Preview ${index + 1}`">
                    <button type="button" @click="removeImage(index)" class="remove-image">✕</button>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeCreateEditModal" class="btn-cancel">
                Hủy
              </button>
              <button type="submit" class="btn-submit" :disabled="submitting">
                {{ submitting ? 'Đang lưu...' : (showCreateModal ? '➕ Thêm sản phẩm' : '💾 Cập nhật') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminProducts',
  data() {
    return {
      products: [],
      categories: [],
      filteredProducts: [],
      loading: true,
      searchQuery: '',
      statusFilter: '',
      categoryFilter: '',
      sortBy: 'newest',
      currentPage: 1,
      itemsPerPage: 12,
      selectedProduct: null,
      productStats: {},
      showCreateModal: false,
      showEditModal: false,
      submitting: false,
      productForm: {
        id: null,
        name: '',
        category: '',
        price: 0,
        stockQuantity: 0,
        description: '',
        images: []
      },
      stats: {
        total: 0,
        active: 0,
        inactive: 0,
        lowStock: 0
      }
    }
  },
  computed: {
    paginatedProducts() {
      const start = (this.currentPage - 1) * this.itemsPerPage
      const end = start + this.itemsPerPage
      return this.filteredProducts.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.filteredProducts.length / this.itemsPerPage)
    }
  },
  async mounted() {
    await this.loadProducts()
    await this.loadCategories()
    this.calculateStats()
  },
  methods: {
    async loadProducts() {
      try {
        this.loading = true
        const response = await axios.get('/api/admin/products', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.products = response.data
        this.filteredProducts = [...this.products]
      } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error)
        this.showToast('Không thể tải danh sách sản phẩm', 'error')
      } finally {
        this.loading = false
      }
    },

    async loadCategories() {
      try {
        const response = await axios.get('/api/categories/admin/all', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.categories = response.data
      } catch (error) {
        console.error('Lỗi khi tải danh mục:', error)
      }
    },

    async toggleProductStatus(product) {
      const action = product.isActive ? 'ngừng bán' : 'bán lại'
      if (!confirm(`Bạn có chắc chắn muốn ${action} sản phẩm "${product.name}"?`)) return

      try {
        await axios.put(`/api/admin/products/${product.id}/toggle-status`, {}, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        product.isActive = !product.isActive
        this.showToast(`Đã ${action} sản phẩm thành công`, 'success')
        this.calculateStats()
      } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái sản phẩm:', error)
        this.showToast(`Không thể ${action} sản phẩm`, 'error')
      }
    },

    async deleteProduct(product) {
      if (!confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?\nHành động này không thể hoàn tác!`)) return

      try {
        await axios.delete(`/api/admin/products/${product.id}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.products = this.products.filter(p => p.id !== product.id)
        this.filterProducts()
        this.calculateStats()
        this.showToast('Đã xóa sản phẩm thành công', 'success')
      } catch (error) {
        console.error('Lỗi khi xóa sản phẩm:', error)
        this.showToast('Không thể xóa sản phẩm', 'error')
      }
    },

    async viewProduct(product) {
      this.selectedProduct = product
      await this.loadProductStats(product.id)
    },

    async loadProductStats(productId) {
      try {
        const response = await axios.get(`/api/admin/products/${productId}/stats`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.productStats = response.data
      } catch (error) {
        console.error('Lỗi khi tải thống kê sản phẩm:', error)
        this.productStats = {}
      }
    },

    editProduct(product) {
      this.productForm = {
        id: product.id,
        name: product.name,
        category: product.category,
        price: product.price,
        stockQuantity: product.stockQuantity,
        description: product.description,
        images: product.images || []
      }
      this.showEditModal = true
    },

    viewReviews(productId) {
      this.$router.push(`/admin/reviews?productId=${productId}`)
    },

    async submitProduct() {
      try {
        this.submitting = true
        
        const url = this.showCreateModal 
          ? '/api/admin/products'
          : `/api/admin/products/${this.productForm.id}`
        
        const method = this.showCreateModal ? 'post' : 'put'
        
        const response = await axios[method](url, this.productForm, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (this.showCreateModal) {
          this.products.push(response.data)
        } else {
          const index = this.products.findIndex(p => p.id === this.productForm.id)
          if (index !== -1) {
            this.products[index] = response.data
          }
        }
        
        this.filterProducts()
        this.calculateStats()
        this.closeCreateEditModal()
        
        this.showToast(
          this.showCreateModal ? 'Đã thêm sản phẩm thành công' : 'Đã cập nhật sản phẩm thành công', 
          'success'
        )
      } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error)
        this.showToast('Không thể lưu sản phẩm', 'error')
      } finally {
        this.submitting = false
      }
    },

    handleImageUpload(event) {
      const files = Array.from(event.target.files)
      files.forEach(file => {
        if (file.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            this.productForm.images.push(e.target.result)
          }
          reader.readAsDataURL(file)
        }
      })
    },

    removeImage(index) {
      this.productForm.images.splice(index, 1)
    },

    filterProducts() {
      let filtered = [...this.products]

      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(product => 
          product.name.toLowerCase().includes(query) ||
          product.description.toLowerCase().includes(query) ||
          product.category.toLowerCase().includes(query)
        )
      }

      if (this.statusFilter) {
        filtered = filtered.filter(product => 
          this.statusFilter === 'active' ? product.isActive : !product.isActive
        )
      }

      if (this.categoryFilter) {
        filtered = filtered.filter(product => product.category === this.categoryFilter)
      }

      this.filteredProducts = filtered
      this.currentPage = 1
      this.sortProducts()
    },

    sortProducts() {
      switch (this.sortBy) {
        case 'oldest':
          this.filteredProducts.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
          break
        case 'name':
          this.filteredProducts.sort((a, b) => a.name.localeCompare(b.name))
          break
        case 'price-asc':
          this.filteredProducts.sort((a, b) => a.price - b.price)
          break
        case 'price-desc':
          this.filteredProducts.sort((a, b) => b.price - a.price)
          break
        case 'stock':
          this.filteredProducts.sort((a, b) => a.stockQuantity - b.stockQuantity)
          break
        case 'newest':
        default:
          this.filteredProducts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }
    },

    searchProducts() {
      this.filterProducts()
    },

    clearFilters() {
      this.searchQuery = ''
      this.statusFilter = ''
      this.categoryFilter = ''
      this.sortBy = 'newest'
      this.filterProducts()
    },

    calculateStats() {
      this.stats.total = this.products.length
      this.stats.active = this.products.filter(p => p.isActive).length
      this.stats.inactive = this.products.filter(p => !p.isActive).length
      this.stats.lowStock = this.products.filter(p => p.stockQuantity <= 10).length
    },

    closeModal() {
      this.selectedProduct = null
      this.productStats = {}
    },

    closeCreateEditModal() {
      this.showCreateModal = false
      this.showEditModal = false
      this.productForm = {
        id: null,
        name: '',
        category: '',
        price: 0,
        stockQuantity: 0,
        description: '',
        images: []
      }
    },

    exportProducts() {
      this.showToast('Tính năng xuất Excel đang được phát triển', 'info')
    },

    getStockClass(stock) {
      if (stock <= 5) return 'stock-critical'
      if (stock <= 10) return 'stock-low'
      if (stock <= 50) return 'stock-medium'
      return 'stock-high'
    },

    handleImageError(event) {
      event.target.src = '/placeholder-product.jpg'
    },

    showToast(message, type = 'info') {
      // Implement toast notification - có thể dùng thư viện như vue-toastification
      console.log(`${type.toUpperCase()}: ${message}`)
    },

    formatCurrency(amount) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('vi-VN')
    },

    formatTime(dateString) {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleTimeString('vi-VN')
    },

    truncateText(text, length) {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }
  }
}
</script>

<style scoped>
.admin-products {
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.header-info h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin: 0;
}

.header-info p {
  color: #7f8c8d;
  font-size: 1.1rem;
  margin: 5px 0 0 0;
}

.btn-create {
  padding: 12px 24px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-create:hover {
  background: #229954;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px 0 0 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #3498db;
}

.search-btn {
  padding: 12px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #2980b9;
}

.filters {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-select {
  padding: 10px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  background: white;
  cursor: pointer;
}

.export-btn {
  padding: 10px 20px;
  background: #f39c12;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.export-btn:hover {
  background: #e67e22;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 3rem;
  opacity: 0.8;
}

.stat-info h3 {
  font-size: 2rem;
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 1rem;
}

.products-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.5;
}

.btn-clear {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 15px;
}

.products-table {
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
}

.products-table th {
  background: #f8f9fa;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e1e5e9;
}

.products-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
}

.product-row:hover {
  background: #f8f9fa;
}

.product-image {
  display: flex;
  justify-content: center;
}

.product-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e1e5e9;
}

.product-info h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
}

.product-description {
  color: #7f8c8d;
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.4;
}

.product-id {
  color: #95a5a6;
  font-size: 12px;
  font-family: monospace;
}

.category-badge {
  padding: 4px 12px;
  background: #e8f4fd;
  color: #2980b9;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.product-price strong {
  color: #27ae60;
  font-size: 16px;
}

.stock-info {
  text-align: center;
}

.stock-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  min-width: 40px;
}

.stock-critical { background: #ffebee; color: #c62828; }
.stock-low { background: #fff3e0; color: #ef6c00; }
.stock-medium { background: #f3e5f5; color: #7b1fa2; }
.stock-high { background: #e8f5e8; color: #2e7d32; }

.low-stock-warning {
  display: block;
  color: #ff5722;
  font-size: 10px;
  margin-top: 2px;
}

.status-toggle {
  padding: 6px 12px;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.status-toggle:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.rating-info {
  text-align: center;
}

.stars {
  color: #f39c12;
  margin-bottom: 2px;
}

.rating-info small {
  color: #7f8c8d;
  font-size: 11px;
}

.created-date div {
  font-weight: 500;
  color: #2c3e50;
}

.created-date small {
  color: #7f8c8d;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.action-buttons button {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-width: 32px;
}

.btn-view {
  background: #3498db;
  color: white;
}

.btn-view:hover {
  background: #2980b9;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-reviews {
  background: #9b59b6;
  color: white;
}

.btn-reviews:hover {
  background: #8e44ad;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e1e5e9;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 15px;
  font-weight: 500;
  color: #2c3e50;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 1000px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.create-edit-modal {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h2 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 5px;
  color: #7f8c8d;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 25px;
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.product-images .main-image img {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 12px;
  border: 2px solid #e1e5e9;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.gallery-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e1e5e9;
  cursor: pointer;
  transition: transform 0.3s;
}

.gallery-image:hover {
  transform: scale(1.05);
}

.product-details {
  display: grid;
  gap: 25px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.2rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item label {
  font-weight: 600;
  color: #7f8c8d;
  font-size: 14px;
}

.detail-item span {
  color: #2c3e50;
  font-size: 15px;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.rating-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.score {
  font-size: 2rem;
  font-weight: 700;
  color: #f39c12;
}

.stats-row {
  display: flex;
  gap: 30px;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  flex: 1;
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #3498db;
  margin-bottom: 5px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 14px;
}

/* Form Styles */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.form-input,
.form-textarea,
.form-select {
  padding: 12px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  border-color: #3498db;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.image-upload {
  border: 2px dashed #e1e5e9;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: border-color 0.3s;
}

.image-upload:hover {
  border-color: #3498db;
}

.upload-text {
  color: #7f8c8d;
  margin: 10px 0;
  font-size: 14px;
}

.file-input {
  margin-bottom: 15px;
}

.image-preview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 15px;
}

.preview-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.preview-item img {
  width: 100px;
  height: 100px;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 5px;
  right: 5px;
  background: rgba(231, 76, 60, 0.9);
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  cursor: pointer;
  font-size: 12px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.btn-cancel {
  padding: 12px 20px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.btn-submit {
  padding: 12px 25px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #229954;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .product-detail {
    grid-template-columns: 1fr;
  }

  .stats-row {
    flex-direction: column;
    gap: 15px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-products {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filters {
    flex-wrap: wrap;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .products-table {
    font-size: 14px;
  }
  
  .products-table th,
  .products-table td {
    padding: 10px 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .modal-body {
    padding: 15px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-info h3 {
    font-size: 1.5rem;
  }

  .product-thumbnail {
    width: 40px;
    height: 40px;
  }

  .image-preview {
    grid-template-columns: repeat(3, 1fr);
  }

  .preview-item img {
    width: 80px;
    height: 80px;
  }
}