<template>
  <div class="product-form-placeholder">
    <header class="page-header">
      <h1>{{ isEdit ? '✏️ Chỉnh sửa sản phẩm' : '➕ Tạo sản phẩm mới' }}</h1>
      <p>{{ isEdit ? 'Cập nhật thông tin sản phẩm' : 'Điền thông tin để tạo sản phẩm mới' }}</p>
    </header>

    <!-- Form Placeholder -->
    <div class="form-placeholder">
      <div class="form-section">
        <h3>📝 Thông tin cơ bản</h3>
        <div class="form-fields">
          <div class="field-placeholder">
            <label>Tên sản phẩm *</label>
            <div class="input-placeholder"></div>
          </div>
          <div class="field-placeholder">
            <label>Mô tả sản phẩm *</label>
            <div class="textarea-placeholder"></div>
          </div>
          <div class="fields-row">
            <div class="field-placeholder">
              <label>Danh mục *</label>
              <div class="select-placeholder"></div>
            </div>
            <div class="field-placeholder">
              <label>Giá bán *</label>
              <div class="input-placeholder"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3>📸 Hình ảnh sản phẩm</h3>
        <div class="upload-placeholder">
          <div class="upload-area">
            <div class="upload-icon">📸</div>
            <p>Kéo thả hoặc click để upload hình ảnh</p>
          </div>
        </div>
      </div>

      <!-- Coming Soon Notice -->
      <div class="coming-soon-notice">
        <div class="notice-content">
          <h3>🚀 Form tạo sản phẩm đang phát triển</h3>
          <p>Chức năng này sẽ có trong Phase 2 với đầy đủ tính năng:</p>
          <div class="features-list">
            <div class="feature-item">✅ Form validation chi tiết</div>
            <div class="feature-item">✅ Upload multiple images</div>
            <div class="feature-item">✅ Rich text editor</div>
            <div class="feature-item">✅ SEO optimization</div>
            <div class="feature-item">✅ Product variants</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isEdit = computed(() => !!route.params.id)
</script>
<style scoped>
/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.header-info h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.header-info p {
  color: var(--text-secondary, #a0aec0);
  font-size: 1rem;
}

.btn-create, .btn-create-first {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-create:hover, .btn-create-first:hover {
  background: #00c4ef;
  transform: translateY(-2px);
}

/* Loading */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--text-secondary, #a0aec0);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 212, 255, 0.3);
  border-top: 3px solid var(--text-accent, #00d4ff);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--text-secondary, #a0aec0);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.product-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent, #00d4ff);
}

.product-image {
  position: relative;
  margin-bottom: 1rem;
}

.product-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.product-status {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.product-info h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.product-price {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.product-stock {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit, .btn-toggle {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-edit {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  text-decoration: none;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.btn-toggle {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

/* Form Placeholders */
.form-placeholder {
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.form-section h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.fields-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.field-placeholder {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-placeholder label {
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
}

.input-placeholder, .select-placeholder {
  height: 40px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
}

.textarea-placeholder {
  height: 100px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
}

.upload-placeholder {
  text-align: center;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 3rem 2rem;
  background: rgba(0, 0, 0, 0.1);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

/* Orders */
.filter-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.filter-tab {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: var(--text-secondary, #a0aec0);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-tab:hover {
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
}

.count {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  margin-left: 0.5rem;
}

.orders-preview {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.order-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-header h4 {
  color: var(--text-primary, #ffffff);
  margin: 0;
}

.status-badge {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

/* Analytics */
.stats-preview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
}

.stat-card h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.chart-placeholder {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  font-size: 2rem;
}

/* Settings */
.settings-sections {
  max-width: 800px;
  margin: 0 auto 2rem;
}

.settings-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.settings-section h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
}

.settings-placeholder {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-item label {
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
}

/* Coming Soon Notice */
.coming-soon-notice {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  margin-top: 2rem;
}

.coming-soon-notice.main {
  margin-top: 0;
}

.notice-content h3 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 1rem;
}

.features-list, .features-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1.5rem;
  text-align: left;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.feature-item:last-child {
  border-bottom: none;
}

/* Animation */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .fields-row {
    grid-template-columns: 1fr;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }

  .stats-preview {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>