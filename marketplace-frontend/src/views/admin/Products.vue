<template>
  <div class="admin-products">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <h1>📦 Quản lý sản phẩm</h1>
          <p>Quản lý tất cả sản phẩm trong hệ thống</p>
        </div>
        <button @click="showCreateModal = true" class="btn-create">➕ Thêm sản phẩm mới</button>
      </div>

      <!-- Filter và Search -->
      <div class="filter-section">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm theo tên sản phẩm, danh mục..."
            class="search-input"
          />
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
            <option v-if="loadingCategories" disabled>Đang tải...</option>
            <option v-for="category in activeCategories" :key="category.id" :value="category.name">
              {{ category.name }}
            </option>
          </select>
          <select v-model="sortBy" class="filter-select">
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
          <p>
            {{
              searchQuery || statusFilter || categoryFilter
                ? 'Không tìm thấy sản phẩm nào với bộ lọc hiện tại. Hãy thử thay đổi điều kiện tìm kiếm.'
                : 'Chưa có sản phẩm nào trong hệ thống. Hãy thêm sản phẩm đầu tiên!'
            }}
          </p>
          <button
            v-if="!searchQuery && !statusFilter && !categoryFilter"
            @click="showCreateModal = true"
            class="btn-add-first"
          >
            ➕ Thêm sản phẩm đầu tiên
          </button>
          <button v-else @click="clearFilters" class="btn-add-first">🔄 Xóa bộ lọc</button>
        </div>

        <div v-else class="products-table">
          <table>
            <thead>
              <tr>
                <th style="width: 80px">
                  <div class="header-content">📷 Hình ảnh</div>
                </th>
                <th style="width: 300px">
                  <div class="header-content">📝 Thông tin sản phẩm</div>
                </th>
                <th style="width: 120px">
                  <div class="header-content">📂 Danh mục</div>
                </th>
                <th style="width: 120px">
                  <div class="header-content">💰 Giá</div>
                </th>
                <th style="width: 100px">
                  <div class="header-content">📦 Tồn kho</div>
                </th>
                <th style="width: 120px">
                  <div class="header-content">🔄 Trạng thái</div>
                </th>
                <th style="width: 120px">
                  <div class="header-content">📅 Ngày tạo</div>
                </th>
                <th style="width: 160px">
                  <div class="header-content">⚙️ Hành động</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(product, index) in paginatedProducts"
                :key="product.id"
                class="product-row"
                :style="{ 'animation-delay': index * 0.05 + 's' }"
              >
                <!-- Cột Hình ảnh -->
                <td>
                  <div class="product-image">
                    <img
                      :src="product.images?.[0] || '/placeholder-product.jpg'"
                      :alt="product.name"
                      @error="handleImageError"
                      loading="lazy"
                    />
                  </div>
                </td>

                <!-- Cột Thông tin sản phẩm -->
                <td>
                  <div class="product-info">
                    <h4 :title="product.name">{{ product.name }}</h4>
                    <p v-if="product.description" :title="product.description">
                      {{ truncateText(product.description, 80) }}
                    </p>
                    <p v-else class="no-description">Chưa có mô tả</p>
                    <span class="product-id" :title="'Mã sản phẩm: ' + product.id">
                      #{{ product.id }}
                    </span>
                  </div>
                </td>

                <!-- Cột Danh mục -->
                <td>
                  <span class="category-tag" :title="'Danh mục: ' + product.category">
                    {{ product.category }}
                  </span>
                </td>

                <!-- Cột Giá -->
                <td>
                  <div class="price-info">
                    <span class="price" :title="'Giá: ' + formatCurrency(product.price)">
                      {{ formatCurrency(product.price) }}
                    </span>
                  </div>
                </td>

                <!-- Cột Tồn kho -->
                <td>
                  <div class="stock-info">
                    <span
                      class="stock-number"
                      :class="{ 'low-stock': product.stockQuantity <= 10 }"
                      :title="'Số lượng tồn kho: ' + product.stockQuantity"
                    >
                      {{ product.stockQuantity }}
                    </span>
                    <span
                      v-if="product.stockQuantity <= 10"
                      class="low-stock-badge"
                      title="Cảnh báo: Sắp hết hàng"
                    >
                      ⚠️ Ít
                    </span>
                  </div>
                </td>

                <!-- Cột Trạng thái -->
                <td>
                  <span
                    class="status-badge"
                    :class="{ active: product.isActive, inactive: !product.isActive }"
                    :title="product.isActive ? 'Sản phẩm đang được bán' : 'Sản phẩm đã ngừng bán'"
                  >
                    <span class="status-icon">{{ product.isActive ? '✅' : '❌' }}</span>
                    <span class="status-text">{{
                      product.isActive ? 'Đang bán' : 'Ngừng bán'
                    }}</span>
                  </span>
                </td>

                <!-- Cột Ngày tạo -->
                <td>
                  <div class="date-info">
                    <span class="date" :title="'Ngày tạo: ' + formatDate(product.createdAt)">
                      {{ formatDate(product.createdAt) }}
                    </span>
                  </div>
                </td>

                <!-- Cột Hành động -->
                <td>
                  <div class="action-buttons">
                    <button
                      @click="viewProduct(product)"
                      class="btn btn-view"
                      title="Xem chi tiết sản phẩm"
                      :aria-label="'Xem chi tiết sản phẩm ' + product.name"
                    >
                      <span class="action-icon">👁️</span>
                    </button>

                    <button
                      @click="editProduct(product)"
                      class="btn btn-edit"
                      title="Chỉnh sửa sản phẩm"
                      :aria-label="'Chỉnh sửa sản phẩm ' + product.name"
                    >
                      <span class="action-icon">✏️</span>
                    </button>

                    <button
                      @click="toggleProductStatus(product)"
                      class="btn btn-toggle"
                      :title="product.isActive ? 'Ngừng bán sản phẩm' : 'Cho phép bán lại sản phẩm'"
                      :aria-label="
                        (product.isActive ? 'Ngừng bán' : 'Cho phép bán') +
                        ' sản phẩm ' +
                        product.name
                      "
                    >
                      <span class="action-icon">{{ product.isActive ? '⏸️' : '▶️' }}</span>
                    </button>

                    <button
                      @click="deleteProduct(product)"
                      class="btn btn-delete"
                      title="Xóa sản phẩm vĩnh viễn"
                      :aria-label="'Xóa sản phẩm ' + product.name"
                    >
                      <span class="action-icon">🗑️</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination cải tiến -->
      <div v-if="totalPages > 1" class="pagination">
        <div class="pagination-info">
          <span class="pagination-text">
            Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} -
            {{ Math.min(currentPage * itemsPerPage, filteredProducts.length) }}
            trong tổng số {{ filteredProducts.length }} sản phẩm
          </span>
        </div>

        <div class="pagination-controls">
          <button
            @click="currentPage = 1"
            :disabled="currentPage === 1"
            class="page-btn page-btn-first"
            title="Trang đầu"
          >
            ⏮️
          </button>

          <button
            @click="currentPage = Math.max(1, currentPage - 1)"
            :disabled="currentPage === 1"
            class="page-btn page-btn-prev"
            title="Trang trước"
          >
            ⬅️ Trước
          </button>

          <div class="page-numbers">
            <button
              v-for="page in getVisiblePages()"
              :key="page"
              @click="currentPage = page"
              :class="['page-number', { active: page === currentPage }]"
              :title="'Trang ' + page"
            >
              {{ page }}
            </button>
          </div>

          <button
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="page-btn page-btn-next"
            title="Trang sau"
          >
            Sau ➡️
          </button>

          <button
            @click="currentPage = totalPages"
            :disabled="currentPage === totalPages"
            class="page-btn page-btn-last"
            title="Trang cuối"
          >
            ⏭️
          </button>
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

        <span class="page-info"> Trang {{ currentPage }} / {{ totalPages }} </span>

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
    <div
      v-if="showCreateModal || showEditModal"
      class="modal-overlay"
      @click="closeCreateEditModal"
    >
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
                />
              </div>

              <div class="form-group">
                <label>Danh mục *</label>
                <select v-model="productForm.category" required class="form-select">
                  <option value="">Chọn danh mục</option>
                  <option v-if="loadingCategories" disabled>Đang tải danh mục...</option>
                  <option
                    v-for="category in activeCategories"
                    :key="category.id"
                    :value="category.name"
                  >
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
              <button type="button" @click="closeCreateEditModal" class="btn btn-cancel">
                Hủy
              </button>
              <button type="submit" :disabled="submitting" class="btn btn-primary">
                {{
                  submitting
                    ? '⏳ Đang lưu...'
                    : showCreateModal
                    ? '➕ Thêm sản phẩm'
                    : '💾 Cập nhật'
                }}
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
              />
            </div>
            <div class="detail-info">
              <h3>{{ selectedProduct.name }}</h3>
              <p class="detail-category">📂 {{ selectedProduct.category }}</p>
              <p class="detail-price">💰 {{ formatCurrency(selectedProduct.price) }}</p>
              <p class="detail-stock">📦 Tồn kho: {{ selectedProduct.stockQuantity }}</p>
              <p class="detail-status">
                <span
                  :class="{ active: selectedProduct.isActive, inactive: !selectedProduct.isActive }"
                >
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
import { useAdminStore } from '@/stores/admin'
import { adminAPI, categoryAPI, productAPI } from '@/services/api'

export default {
  name: 'AdminProducts',
  setup() {
    const adminStore = useAdminStore()

    // Reactive data
    const products = ref([])
    const categories = ref([])
    const loadingCategories = ref(false)
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
      images: [],
    })

    const stats = ref({
      total: 0,
      active: 0,
      inactive: 0,
      lowStock: 0,
    })

    const activeCategories = computed(() => categories.value.filter((c) => c.active))

    const clearFilters = () => {
      searchQuery.value = ''
      statusFilter.value = ''
      categoryFilter.value = ''
      sortBy.value = 'newest'
      filterProducts()
    }

    const getVisiblePages = () => {
      const total = totalPages.value
      const current = currentPage.value
      const visible = []

      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          visible.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            visible.push(i)
          }
          visible.push('...')
          visible.push(total)
        } else if (current >= total - 3) {
          visible.push(1)
          visible.push('...')
          for (let i = total - 4; i <= total; i++) {
            visible.push(i)
          }
        } else {
          visible.push(1)
          visible.push('...')
          for (let i = current - 1; i <= current + 1; i++) {
            visible.push(i)
          }
          visible.push('...')
          visible.push(total)
        }
      }

      return visible
    }

    const loadCategories = async () => {
      loadingCategories.value = true

      try {
        console.log('🔄 Loading categories from API...')

        // Load từ admin API để có full access
        const response = await adminAPI.getCategories()
        categories.value = response.data || []

        console.log('✅ Categories loaded:', categories.value.length, 'categories')
      } catch (error) {
        console.error('❌ Load categories error:', error)

        // Fallback to public categories API
        try {
          const fallbackResponse = await categoryAPI.getAll()
          categories.value = fallbackResponse.data || []
          console.log('✅ Categories loaded from fallback:', categories.value.length, 'categories')
        } catch (fallbackError) {
          console.error('❌ Fallback categories error:', fallbackError)
          categories.value = []
        }
      } finally {
        loadingCategories.value = false
      }
    }

    // Computed
    const sortedProducts = computed(() => {
      let products = [...filteredProducts.value]
      switch (sortBy.value) {
        case 'newest':
          products.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          break
        case 'oldest':
          products.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
          break
        case 'name':
          products.sort((a, b) => a.name.localeCompare(b.name))
          break
        case 'price-high':
          products.sort((a, b) => b.price - a.price)
          break
        case 'price-low':
          products.sort((a, b) => a.price - b.price)
          break
        case 'stock':
          products.sort((a, b) => b.stockQuantity - a.stockQuantity)
          break
      }
      return products
    })

    const paginatedProducts = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return sortedProducts.value.slice(start, end)
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredProducts.value.length / itemsPerPage)
    })

    const loadProducts = async () => {
      loading.value = true

      try {
        console.log('🔄 Loading products from API...')

        // Call real API thông qua admin store
        await adminStore.loadProducts()

        // Get data từ store
        products.value = adminStore.products
        filteredProducts.value = [...products.value]

        // Calculate stats
        calculateStats()

        console.log('✅ Products loaded:', products.value.length, 'products')
      } catch (error) {
        console.error('❌ Load products error:', error)
        alert('Không thể tải danh sách sản phẩm: ' + (error.message || 'Lỗi không xác định'))

        // Fallback to empty array
        products.value = []
        filteredProducts.value = []
        stats.value = { total: 0, active: 0, inactive: 0, lowStock: 0 }
      } finally {
        loading.value = false
      }
    }

    const calculateStats = () => {
      stats.value.total = products.value.length
      stats.value.active = products.value.filter((p) => p.isActive).length
      stats.value.inactive = products.value.filter((p) => !p.isActive).length
      stats.value.lowStock = products.value.filter((p) => p.stockQuantity <= 10).length
    }

    const filterProducts = () => {
      let filtered = [...products.value]

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(
          (product) =>
            product.name.toLowerCase().includes(query) ||
            product.description.toLowerCase().includes(query) ||
            product.category.toLowerCase().includes(query)
        )
      }

      if (statusFilter.value) {
        filtered = filtered.filter((product) =>
          statusFilter.value === 'active' ? product.isActive : !product.isActive
        )
      }

      if (categoryFilter.value) {
        filtered = filtered.filter((product) => product.category === categoryFilter.value)
      }

      filteredProducts.value = filtered
      currentPage.value = 1
    }

    const searchProducts = () => {
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
        images: product.images || [],
      }
      showEditModal.value = true
    }

    const toggleProductStatus = async (product) => {
      const action = product.isActive ? 'ngừng bán' : 'bán lại'
      if (!confirm(`Bạn có chắc chắn muốn ${action} sản phẩm "${product.name}"?`)) return

      try {
        console.log('🔄 Toggling product status...', product.id)

        // Call real API through store
        await adminStore.toggleProductStatus(product.id)

        // Refresh products from store to get updated data
        await adminStore.loadProducts()
        products.value = adminStore.products
        filteredProducts.value = [...products.value]
        calculateStats()

        alert(`Đã ${action} sản phẩm thành công`)
        console.log('✅ Product status toggled successfully')
      } catch (error) {
        console.error('❌ Toggle status error:', error)
        alert('Không thể cập nhật trạng thái sản phẩm: ' + (error.message || 'Lỗi không xác định'))
      }
    }

    const deleteProduct = async (product) => {
      if (
        !confirm(
          `Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?\nHành động này không thể hoàn tác!`
        )
      )
        return

      try {
        console.log('🔄 Deleting product...', product.id)

        await adminAPI.deleteProduct(product.id)

        const index = products.value.findIndex((p) => p.id === product.id)
        if (index !== -1) {
          products.value.splice(index, 1)
          filterProducts()
          calculateStats()
        }

        alert('Đã xóa sản phẩm thành công')
        console.log('✅ Product deleted successfully')
      } catch (error) {
        console.error('❌ Delete product error:', error)
        alert('Không thể xóa sản phẩm: ' + (error.message || 'Lỗi không xác định'))
      }
    }

    const submitProduct = async () => {
      if (!productForm.value.name || !productForm.value.category || !productForm.value.price) {
        alert('Vui lòng điền đầy đủ thông tin bắt buộc')
        return
      }

      submitting.value = true

      try {
        console.log('🔄 Submitting product...', productForm.value)

        if (showCreateModal.value) {
          const productData = {
            name: productForm.value.name,
            description: productForm.value.description,
            price: productForm.value.price,
            stockQuantity: productForm.value.stockQuantity,
            category: productForm.value.category,
            images: productForm.value.images || [],
          }

          console.log('📤 Creating product with productAPI:', productData)

          const response = await productAPI.create(productData)
          console.log('✅ Product created successfully:', response.data)

          alert('Đã thêm sản phẩm thành công')
        } else {
          await productAPI.update(productForm.value.id, productForm.value)
          alert('Đã cập nhật sản phẩm thành công')
        }

        // Reload products
        await loadProducts()
        closeCreateEditModal()
      } catch (error) {
        console.error('❌ Submit product error:', error)
        alert(
          'Lỗi: ' + (error.response?.data?.message || error.message || 'Không thể lưu sản phẩm')
        )
      } finally {
        submitting.value = false
      }
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
        images: [],
      }
    }

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

    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const formatDate = (date) => {
      return new Intl.DateTimeFormat('vi-VN').format(new Date(date))
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    onMounted(async () => {
      await Promise.all([loadProducts(), loadCategories()])
    })

    return {
      clearFilters,
      getVisiblePages,
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
      truncateText,
      loadingCategories,
      loadCategories,
      activeCategories,
    }
  },
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
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.loading {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(26, 26, 46, 0.6);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(0, 212, 255, 0.2);
  border-top: 4px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1.5rem;
}

.loading p {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin: 0;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(26, 26, 46, 0.6);
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  opacity: 0.6;
  filter: grayscale(0.5);
}

.empty-state h3 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
  line-height: 1.6;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 212, 255, 0.5) transparent;
}

.products-table::-webkit-scrollbar {
  height: 8px;
}

.products-table::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}

.products-table::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.5);
  border-radius: 4px;
}

.products-table::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 212, 255, 0.7);
}

.products-table table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 1000px;
}

.products-table th,
.products-table td {
  padding: 1.2rem 1rem;
  text-align: left;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15);
  vertical-align: middle;
}

.products-table th {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(0, 150, 200, 0.15));
  color: var(--text-accent);
  font-weight: 700;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.products-table th:first-child {
  border-top-left-radius: 0;
}

.products-table th:last-child {
  border-top-right-radius: 0;
}

.product-row {
  transition: all 0.3s ease;
  background: rgba(26, 26, 46, 0.4);
}

.product-row:nth-child(even) {
  animation-delay: 0.1s;
}

.product-row:nth-child(odd) {
  animation-delay: 0.05s;
}

.product-row:hover {
  background: rgba(0, 212, 255, 0.08);
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.1);
}
.product-row:hover .product-image {
  transform: scale(1.05);
}

.product-image {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(0, 212, 255, 0.2);
  transition: all 0.3s ease;
  position: relative;
}

.product-image::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent, rgba(0, 212, 255, 0.1), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-image:hover::before {
  opacity: 1;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.product-info {
  max-width: 300px;
}
.product-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-info p {
  color: var(--text-secondary);
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-id {
  color: rgba(0, 212, 255, 0.6);
  font-size: 0.75rem;
  font-family: 'Monaco', monospace;
  background: rgba(0, 212, 255, 0.1);
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.category-tag {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 150, 200, 0.3));
  color: var(--text-accent);
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  border: 1px solid rgba(0, 212, 255, 0.3);
  display: inline-block;
  text-transform: capitalize;
}

.price-info {
  text-align: right;
}

.price {
  color: #00ff88;
  font-weight: 700;
  font-size: 1.1rem;
  text-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
}
.stock-info {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
}
.stock-number {
  font-weight: 700;
  font-size: 1.1rem;
  padding: 0.4rem 0.8rem;
  border-radius: 12px;
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
  min-width: 50px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}
.stock-number:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}
.stock-number.low-stock {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
  border-color: rgba(255, 193, 7, 0.5);
  animation: pulse-warning 2s infinite;
}
.stock-number.out-of-stock {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.5);
  animation: pulse-danger 2s infinite;
}
@keyframes pulse-warning {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(255, 193, 7, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(255, 193, 7, 0);
  }
}

@keyframes pulse-danger {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(239, 68, 68, 0);
  }
}

.low-stock-badge {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.3), rgba(255, 140, 0, 0.3));
  color: #ffc107;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.65rem;
  font-weight: 600;
  border: 1px solid rgba(255, 193, 7, 0.5);
  animation: blink 1.5s infinite;
  white-space: nowrap;
}
.out-of-stock-badge {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(220, 38, 38, 0.3));
  color: #ef4444;
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.65rem;
  font-weight: 600;
  border: 1px solid rgba(239, 68, 68, 0.5);
  animation: blink 1.2s infinite;
  white-space: nowrap;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0.5;
  }
}

.status-badge {
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}

.status-badge.active {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(5, 150, 105, 0.3));
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.5);
  box-shadow: 0 0 15px rgba(16, 185, 129, 0.2);
}

.status-badge.inactive {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(220, 38, 38, 0.3));
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.5);
}

.date-info {
  text-align: center;
}

.date {
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 0.4rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 0.6rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transition: all 0.3s ease;
  transform: translate(-50%, -50%);
}

.btn:hover::before {
  width: 100%;
  height: 100%;
}

.btn-view {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 150, 200, 0.3));
  color: var(--text-accent);
  border: 1px solid rgba(0, 212, 255, 0.4);
}

.btn-edit {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.2), rgba(255, 140, 0, 0.3));
  color: #ffc107;
  border: 1px solid rgba(255, 193, 7, 0.4);
}

.btn-toggle {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(5, 150, 105, 0.3));
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

.btn-delete {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(220, 38, 38, 0.3));
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

.btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}
.btn-view:hover {
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
}

.btn-edit:hover {
  box-shadow: 0 8px 25px rgba(255, 193, 7, 0.4);
}

.btn-toggle:hover {
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
}

.btn-delete:hover {
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.4);
}

@media (max-width: 1200px) {
  .products-table th,
  .products-table td {
    padding: 1rem 0.8rem;
  }
  .product-info {
    max-width: 250px;
  }
}

@media (max-width: 768px) {
  .products-table {
    font-size: 0.9rem;
  }

  .products-table th,
  .products-table td {
    padding: 0.8rem 0.6rem;
  }

  .product-image {
    width: 50px;
    height: 50px;
  }

  .product-info {
    max-width: 200px;
  }

  .product-info h4 {
    font-size: 1rem;
  }

  .action-buttons {
    gap: 0.2rem;
  }

  .btn {
    min-width: 35px;
    height: 35px;
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .products-table table {
    min-width: 800px;
  }

  .products-table th,
  .products-table td {
    padding: 0.6rem 0.4rem;
    font-size: 0.8rem;
  }

  .product-image {
    width: 40px;
    height: 40px;
  }

  .product-info h4 {
    font-size: 0.9rem;
  }

  .category-tag,
  .status-badge {
    font-size: 0.7rem;
    padding: 0.3rem 0.6rem;
  }

  .btn {
    min-width: 30px;
    height: 30px;
    font-size: 0.8rem;
  }
}

.pagination {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}
.pagination-info {
  text-align: center;
}
.pagination-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.page-btn {
  padding: 0.6rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 150, 200, 0.3));
  border-color: var(--text-accent);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}
.page-btn-first,
.page-btn-last {
  padding: 0.6rem;
  min-width: 40px;
}
.page-numbers {
  display: flex;
  gap: 0.3rem;
  align-items: center;
}

.page-number {
  padding: 0.6rem 0.8rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  min-width: 40px;
  text-align: center;
}
.page-number:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.5);
  transform: translateY(-1px);
}
.page-number.active {
  background: linear-gradient(135deg, var(--text-accent), rgba(0, 150, 200, 0.8));
  border-color: var(--text-accent);
  color: white;
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
  transform: translateY(-1px);
}
.page-number.active:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 212, 255, 0.5);
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
.header-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
}

.no-description {
  font-style: italic;
  opacity: 0.6;
}

.status-icon,
.action-icon {
  display: inline-block;
  transition: transform 0.2s ease;
}

.btn:hover .action-icon {
  transform: scale(1.1);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

.btn-add-first {
  padding: 1rem 2rem;
  background: linear-gradient(135deg, var(--text-accent), rgba(0, 150, 200, 0.8));
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.3);
}

.btn-add-first:hover {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.9), rgba(0, 120, 180, 0.9));
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
}

/* Responsive cho pagination */
@media (max-width: 768px) {
  .pagination {
    padding: 1rem;
  }

  .pagination-controls {
    gap: 0.3rem;
  }

  .page-btn {
    padding: 0.5rem 0.7rem;
    font-size: 0.8rem;
  }

  .page-btn-first,
  .page-btn-last {
    padding: 0.5rem;
    min-width: 35px;
  }

  .page-number {
    padding: 0.5rem 0.6rem;
    min-width: 35px;
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .pagination-controls {
    flex-direction: column;
    gap: 0.8rem;
  }

  .pagination-text {
    font-size: 0.8rem;
  }

  .page-numbers {
    order: -1;
  }
}
</style>
