<template>
  <div class="product-form-page">
    <header class="page-header">
      <h1>{{ isEdit ? '✏️ Chỉnh sửa sản phẩm' : '➕ Tạo sản phẩm mới' }}</h1>
      <p>{{ isEdit ? 'Cập nhật thông tin sản phẩm' : 'Điền thông tin để tạo sản phẩm mới' }}</p>
    </header>

    <!-- Form Placeholder -->
    <div class="form-container">
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
        <h3>💰 Giá & Kho hàng</h3>
        <div class="form-fields">
          <div class="fields-row">
            <div class="field-placeholder">
              <label>Giá bán (VND) *</label>
              <div class="input-placeholder"></div>
            </div>
            <div class="field-placeholder">
              <label>Số lượng trong kho *</label>
              <div class="input-placeholder"></div>
            </div>
          </div>
          <div class="field-placeholder">
            <label>Tags (cách nhau bằng dấu phẩy)</label>
            <div class="input-placeholder"></div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3>📸 Hình ảnh sản phẩm</h3>
        <div class="upload-section">
          <div class="upload-area">
            <div class="upload-icon">📸</div>
            <p>Kéo thả hoặc click để upload hình ảnh</p>
            <small>PNG, JPG, GIF up to 10MB</small>
          </div>
          <div class="image-preview-area">
            <div class="preview-placeholder">
              <span>Xem trước hình ảnh sẽ hiển thị ở đây</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="button" class="btn-secondary">
          💾 Lưu nháp
        </button>
        <button type="button" class="btn-primary">
          {{ isEdit ? '✅ Cập nhật sản phẩm' : '🚀 Tạo sản phẩm' }}
        </button>
      </div>
    </div>

    <!-- Coming Soon Notice -->
    <div class="coming-soon-notice">
      <div class="notice-content">
        <h3>🚀 Form tạo sản phẩm đang phát triển</h3>
        <p>Chức năng này sẽ có trong Phase 2 với đầy đủ tính năng:</p>
        <div class="features-grid">
          <div class="feature-item">✅ Form validation chi tiết</div>
          <div class="feature-item">✅ Upload multiple images</div>
          <div class="feature-item">✅ Rich text editor cho mô tả</div>
          <div class="feature-item">✅ SEO optimization</div>
          <div class="feature-item">✅ Product variants (size, color)</div>
          <div class="feature-item">✅ Inventory tracking</div>
          <div class="feature-item">✅ Shipping options</div>
          <div class="feature-item">✅ Auto-save draft</div>
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
.page-header {
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
  font-size: 1rem;
}

.form-container {
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
  font-size: 1.2rem;
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
  font-size: 0.9rem;
}

.input-placeholder, .select-placeholder {
  height: 45px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  position: relative;
}

.input-placeholder::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 1rem;
  width: 60%;
  height: 2px;
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-50%);
}

.textarea-placeholder {
  height: 120px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  position: relative;
}

.textarea-placeholder::after {
  content: '';
  position: absolute;
  top: 1rem;
  left: 1rem;
  right: 1rem;
  height: 2px;
  background: rgba(0, 212, 255, 0.3);
}

.upload-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 3rem 2rem;
  background: rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-area:hover {
  border-color: var(--text-accent, #00d4ff);
  background: rgba(0, 212, 255, 0.05);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.upload-area p {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.upload-area small {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.85rem;
}

.image-preview-area {
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  width: 100%;
  height: 200px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #a0aec0);
  font-style: italic;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.btn-primary, .btn-secondary {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 1rem;
}

.btn-primary {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  border: none;
}

.btn-secondary {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.btn-primary:hover {
  background: #00c4ef;
  transform: translateY(-2px);
}

.btn-secondary:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.coming-soon-notice {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  margin-top: 2rem;
}

.notice-content h3 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 1rem;
}

.notice-content p {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 1.5rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1.5rem;
  text-align: left;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .form-container {
    padding: 0 1rem;
  }

  .fields-row {
    grid-template-columns: 1fr;
  }

  .upload-section {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>