<template>
  <div class="admin-categories">
    <div class="page-header">
      <!-- <h1>Quản lý danh mục</h1> -->
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
        <th>Icon</th>
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
        <td>
          <span class="category-id">{{ category.id.substring(0, 8) }}...</span>
        </td>
        
        <td>
          <div class="category-icon">
            <span class="icon-display">{{ category.icon || '🏷️' }}</span>
          </div>
        </td>
        
        <td>
          <div class="category-name">
            <strong>{{ category.name }}</strong>
          </div>
        </td>
        
        <td>
          <div class="category-description">
            {{ truncateText(category.description, 50) }}
          </div>
        </td>
        
        <td>
          <span class="product-count">{{ category.productCount || 0 }}</span>
        </td>
        
        <td>
          <span :class="['status-badge', category.active ? 'active' : 'inactive']">
            {{ category.active ? 'Hoạt động' : 'Không hoạt động' }}
          </span>
        </td>
        
        <td>
          <span class="created-date">{{ formatDate(category.createdAt) }}</span>
        </td>
        
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
      
      <!-- Empty state -->
      <tr v-if="filteredCategories.length === 0">
        <td colspan="8" class="empty-state">
          <div class="empty-content">
            <span class="empty-icon">📂</span>
            <p>Không có danh mục nào</p>
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
            <label>Mô tả</label>
            <textarea
              v-model="categoryForm.description"
              rows="3"
              class="form-textarea"
              placeholder="Mô tả danh mục..."
            ></textarea>
          </div>

          <div class="form-group">
          <label>Icon</label>
          <input 
            type="text" 
            v-model="categoryForm.icon" 
            placeholder="🏷️ (emoji hoặc icon class)"
            class="form-input"
          />
          <small class="form-hint">Nhập emoji hoặc icon class (VD: 🏷️, 📱, 👕)</small>
        </div>

          <!-- <div class="form-group">
            <label class="checkbox-label">
              <input v-model="categoryForm.active" type="checkbox" />
              <span>Hoạt động</span>
            </label>
          </div> -->

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
import { adminAPI } from '@/services/api'

export default {
  name: 'AdminCategories',
  setup() {
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
      description: '',
      icon: '',
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
        const response = await adminAPI.getCategories()
        categories.value = response.data || []
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
  categoryForm.value = { 
    id: category.id,
    name: category.name || '',
    description: category.description || '',
    icon: category.icon || '',
    active: category.active !== undefined ? category.active : true  
  }
  showEditModal.value = true
}

    const saveCategory = async () => {
  if (!categoryForm.value.name.trim()) {
    alert('Vui lòng nhập tên danh mục')
    return
  }

  saving.value = true
  try {
    const categoryData = {
      name: categoryForm.value.name.trim(),
      description: categoryForm.value.description || '',
      icon: categoryForm.value.icon || '',
      active: Boolean(categoryForm.value.active)  
    }

    console.log('Saving category data:', categoryData) 

    if (showEditModal.value) {
      await adminAPI.updateCategory(categoryForm.value.id, categoryData)
    } else {
      await adminAPI.createCategory(categoryData)
    }

    closeModal()
    loadCategories() 
    
  } catch (error) {
    console.error('Error saving category:', error)
    const errorMsg = error.response?.data?.message || error.message
    alert('Có lỗi xảy ra: ' + errorMsg)
  } finally {
    saving.value = false
  }
}

    const toggleCategoryStatus = async (category) => {
  try {
    console.log('Toggling status for category:', category.id) 
    
    await adminAPI.toggleCategoryStatus(category.id)
    
    const index = categories.value.findIndex(cat => cat.id === category.id)
    if (index !== -1) {
      categories.value[index].active = !categories.value[index].active
    }
    
  } catch (error) {
    console.error('Error toggling category status:', error)
    alert('Không thể thay đổi trạng thái: ' + (error.response?.data?.message || error.message))
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
        await adminAPI.deleteCategory(categoryToDelete.value.id)
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
        description: '',
        icon: '🏷️',
        active: true,
      }
    }

    const truncateText = (text, length) => {
  if (!text) return 'Không có mô tả'
  return text.length > length ? text.substring(0, length) + '...' : text
}

    const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit', 
      day: '2-digit'
    })
  } catch (error) {
    return 'Invalid date'
  }
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
  background: rgba(26, 26, 46, 0.8);
  border-radius: 12px;
  overflow: hidden;
}

.data-table th {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-primary);
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.data-table td {
  padding: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  vertical-align: middle;
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
  font-size: 1.5rem;
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
  font-weight: 500;
}

.checkbox-label input[type='checkbox'] {
  width: auto;
  margin: 0;
  transform: scale(1.1);
}
.checkbox-label span {
  user-select: none;
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
.form-hint {
  font-size: 0.8rem;
  color: #666;
  margin-top: 0.25rem;
  font-style: italic;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--text-accent);
  color: white;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.category-id {
  font-family: monospace;
  font-size: 0.8rem;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

/* 🆕 Icon display styling */
.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-display {
  font-size: 1.5rem;
  display: inline-block;
  align-items: center;
  justify-content: center;
  background: rgba(0, 212, 255, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  
}

/* Category name styling */
.category-name strong {
  color: var(--text-primary);
  font-size: 1rem;
}

/* Description styling */
.category-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

/* Product count styling */
.product-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.9rem;
  min-width: 40px;
}

/* Status badge styling */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status-badge.inactive {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

/* Date styling */
.created-date {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

/* Action buttons */
.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn {
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-sm {
  padding: 0.5rem;
  min-width: 35px;
  height: 35px;
}

.btn-outline {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent);
}

.btn-outline:hover {
  background: rgba(0, 212, 255, 0.2);
}

.btn-warning {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.btn-warning:hover {
  background: rgba(251, 191, 36, 0.3);
}

.btn-success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.btn-success:hover {
  background: rgba(16, 185, 129, 0.3);
}

.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.btn-danger:hover {
  background: rgba(239, 68, 68, 0.3);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 3rem;
}

.empty-content {
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

/* Responsive */
@media (max-width: 768px) {
  .data-table th,
  .data-table td {
    padding: 0.5rem;
  }
  
  .category-id {
    font-size: 0.7rem;
  }
  
  
  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }
}

</style>
