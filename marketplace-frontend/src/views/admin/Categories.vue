<template>
  <div class="admin-categories">
    <div class="page-header">
      <h1>Quản lý danh mục</h1>
      <button @click="showCreateModal = true" class="btn btn-primary">➕ Tạo danh mục mới</button>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📂</div>
        <div class="stat-content">
          <h3>{{ totalCategories }}</h3>
          <p>Tổng danh mục</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <h3>{{ totalProducts }}</h3>
          <p>Tổng sản phẩm</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🔥</div>
        <div class="stat-content">
          <h3>{{ activeCategories }}</h3>
          <p>Danh mục hoạt động</p>
        </div>
      </div>
    </div>

    <!-- Categories Table -->
    <div class="table-container">
      <div class="table-header">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm danh mục..."
            class="form-input"
          />
        </div>
        <div class="table-actions">
          <select v-model="statusFilter" class="form-select">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Không hoạt động</option>
          </select>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Hình ảnh</th>
              <th>Tên danh mục</th>
              <th>Mô tả</th>
              <th>Số sản phẩm</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in filteredCategories" :key="category.id">
              <td>{{ category.id }}</td>
              <td>
                <img
                  :src="category.image || '/placeholder-category.jpg'"
                  :alt="category.name"
                  class="category-image"
                  @error="handleImageError"
                />
              </td>
              <td>
                <div class="category-name">
                  <strong>{{ category.name }}</strong>
                  <span class="category-slug">/{{ category.slug }}</span>
                </div>
              </td>
              <td>
                <div class="category-description">
                  {{ truncateText(category.description, 50) }}
                </div>
              </td>
              <td>
                <span class="product-count">{{ category.productCount }}</span>
              </td>
              <td>
                <span :class="['status-badge', category.active ? 'active' : 'inactive']">
                  {{ category.active ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
              <td>{{ formatDate(category.createdAt) }}</td>
              <td>
                <div class="action-buttons">
                  <button
                    @click="editCategory(category)"
                    class="btn btn-sm btn-outline"
                    title="Chỉnh sửa"
                  >
                    ✏️
                  </button>
                  <button
                    @click="toggleCategoryStatus(category)"
                    :class="['btn', 'btn-sm', category.active ? 'btn-warning' : 'btn-success']"
                    :title="category.active ? 'Vô hiệu hóa' : 'Kích hoạt'"
                  >
                    {{ category.active ? '🚫' : '✅' }}
                  </button>
                  <button
                    @click="deleteCategory(category)"
                    class="btn btn-sm btn-danger"
                    title="Xóa"
                    :disabled="category.productCount > 0"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
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

    <!-- Create/Edit Category Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ showEditModal ? 'Chỉnh sửa danh mục' : 'Tạo danh mục mới' }}</h2>
          <button @click="closeModal" class="btn btn-icon">✕</button>
        </div>

        <form @submit.prevent="saveCategory" class="modal-body">
          <div class="form-group">
            <label>Tên danh mục *</label>
            <input
              v-model="categoryForm.name"
              type="text"
              required
              class="form-input"
              placeholder="Nhập tên danh mục"
            />
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input
              v-model="categoryForm.slug"
              type="text"
              class="form-input"
              placeholder="slug-tu-dong"
            />
            <small>Tự động tạo từ tên danh mục nếu để trống</small>
          </div>

          <div class="form-group">
            <label>Mô tả</label>
            <textarea
              v-model="categoryForm.description"
              rows="3"
              class="form-textarea"
              placeholder="Mô tả danh mục..."
            ></textarea>
          </div>

          <div class="form-group">
            <label>Hình ảnh</label>
            <input
              v-model="categoryForm.image"
              type="url"
              class="form-input"
              placeholder="URL hình ảnh"
            />
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="categoryForm.active" type="checkbox" />
              <span>Hoạt động</span>
            </label>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-outline">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu...' : showEditModal ? 'Cập nhật' : 'Tạo' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Xác nhận xóa</h2>
          <button @click="showDeleteModal = false" class="btn btn-icon">✕</button>
        </div>

        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa danh mục "{{ categoryToDelete?.name }}" không?</p>
          <p v-if="categoryToDelete?.productCount > 0" class="warning">
            ⚠️ Danh mục này có {{ categoryToDelete.productCount }} sản phẩm. Không thể xóa danh mục
            có sản phẩm.
          </p>

          <div class="form-actions">
            <button @click="showDeleteModal = false" class="btn btn-outline">Hủy</button>
            <button
              @click="confirmDelete"
              class="btn btn-danger"
              :disabled="categoryToDelete?.productCount > 0"
            >
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { api } from '@/services/api'

export default {
  name: 'AdminCategories',
  setup() {
    // Reactive data
    const categories = ref([])
    const loading = ref(false)
    const saving = ref(false)
    const currentPage = ref(1)
    const totalPages = ref(1)
    const searchQuery = ref('')
    const statusFilter = ref('')

    // Modal states
    const showCreateModal = ref(false)
    const showEditModal = ref(false)
    const showDeleteModal = ref(false)
    const categoryToDelete = ref(null)

    // Form data
    const categoryForm = ref({
      name: '',
      slug: '',
      description: '',
      image: '',
      active: true,
    })

    // Computed
    const filteredCategories = computed(() => {
      let filtered = categories.value

      if (searchQuery.value) {
        filtered = filtered.filter(
          (category) =>
            category.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
            category.description.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      }

      if (statusFilter.value) {
        filtered = filtered.filter((category) => {
          if (statusFilter.value === 'active') return category.active
          if (statusFilter.value === 'inactive') return !category.active
          return true
        })
      }

      return filtered
    })

    const totalCategories = computed(() => categories.value.length)
    const totalProducts = computed(() =>
      categories.value.reduce((sum, cat) => sum + cat.productCount, 0)
    )
    const activeCategories = computed(() => categories.value.filter((cat) => cat.active).length)

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
    const loadCategories = async () => {
      loading.value = true
      try {
        const response = await api.get('/admin/categories', {
          params: {
            page: currentPage.value,
            limit: 20,
          },
        })
        categories.value = response.data.categories
        totalPages.value = response.data.totalPages
      } catch (error) {
        console.error('Error loading categories:', error)
      } finally {
        loading.value = false
      }
    }

    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const editCategory = (category) => {
      categoryForm.value = { ...category }
      showEditModal.value = true
    }

    const saveCategory = async () => {
      saving.value = true
      try {
        if (showEditModal.value) {
          await api.put(`/admin/categories/${categoryForm.value.id}`, categoryForm.value)
        } else {
          await api.post('/admin/categories', categoryForm.value)
        }

        closeModal()
        loadCategories()
      } catch (error) {
        console.error('Error saving category:', error)
      } finally {
        saving.value = false
      }
    }

    const toggleCategoryStatus = async (category) => {
      try {
        await api.patch(`/admin/categories/${category.id}/toggle-status`)
        loadCategories()
      } catch (error) {
        console.error('Error toggling category status:', error)
      }
    }

    const deleteCategory = (category) => {
      categoryToDelete.value = category
      showDeleteModal.value = true
    }

    const confirmDelete = async () => {
      if (!categoryToDelete.value || categoryToDelete.value.productCount > 0) {
        return
      }

      try {
        await api.delete(`/admin/categories/${categoryToDelete.value.id}`)
        showDeleteModal.value = false
        categoryToDelete.value = null
        loadCategories()
      } catch (error) {
        console.error('Error deleting category:', error)
      }
    }

    const closeModal = () => {
      showCreateModal.value = false
      showEditModal.value = false
      categoryForm.value = {
        name: '',
        slug: '',
        description: '',
        image: '',
        active: true,
      }
    }

    const truncateText = (text, length) => {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const handleImageError = (event) => {
      event.target.src = '/placeholder-category.jpg'
    }

    // Lifecycle
    onMounted(() => {
      loadCategories()
    })

    return {
      categories,
      loading,
      saving,
      currentPage,
      totalPages,
      searchQuery,
      statusFilter,
      showCreateModal,
      showEditModal,
      showDeleteModal,
      categoryToDelete,
      categoryForm,
      filteredCategories,
      totalCategories,
      totalProducts,
      activeCategories,
      visiblePages,
      changePage,
      editCategory,
      saveCategory,
      toggleCategoryStatus,
      deleteCategory,
      confirmDelete,
      closeModal,
      truncateText,
      formatDate,
      handleImageError,
    }
  },
}
</script>

<style scoped>
.admin-categories {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-content h3 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.25rem;
}

.stat-content p {
  color: #666;
  margin: 0;
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.search-box {
  flex: 1;
  max-width: 300px;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.form-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.category-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.category-name {
  display: flex;
  flex-direction: column;
}

.category-slug {
  font-size: 0.8rem;
  color: #666;
  font-family: monospace;
}

.category-description {
  max-width: 200px;
  color: #666;
}

.product-count {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.active {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.inactive {
  background: #ffebee;
  color: #c62828;
}

.action-buttons {
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

.btn-danger {
  background: #ff4757;
  color: white;
}

.btn-danger:hover {
  background: #e63946;
}

.btn-warning {
  background: #ffa502;
  color: white;
}

.btn-warning:hover {
  background: #ff9500;
}

.btn-success {
  background: #2ed573;
  color: white;
}

.btn-success:hover {
  background: #26d0ce;
}

.btn-sm {
  padding: 0.4rem 0.6rem;
  font-size: 0.8rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  border-top: 1px solid #eee;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #333;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  resize: vertical;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-label input[type='checkbox'] {
  width: auto;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.warning {
  color: #ff4757;
  font-weight: 600;
  margin: 1rem 0;
}

@media (max-width: 768px) {
  .admin-categories {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .table-header {
    flex-direction: column;
    gap: 1rem;
  }

  .search-box {
    max-width: none;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
