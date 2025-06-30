<template>
  <div class="product-form">
    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <h1>{{ isEdit ? '✏️ Chỉnh sửa sản phẩm' : '➕ Tạo sản phẩm mới' }}</h1>
        <p>{{ isEdit ? 'Cập nhật thông tin sản phẩm' : 'Điền thông tin để tạo sản phẩm mới' }}</p>
      </div>
      <div class="header-actions">
        <button @click="saveDraft" class="btn btn-secondary" :disabled="saving">
          💾 {{ saving ? 'Đang lưu...' : 'Lưu nháp' }}
        </button>
        <button @click="previewProduct" class="btn btn-outline">👁️ Xem trước</button>
      </div>
    </header>

    <!-- Form Container -->
    <form @submit.prevent="submitForm" class="form-container">
      <div class="form-layout">
        <!-- Left Column - Main Form -->
        <div class="main-form">
          <!-- Basic Information -->
          <div class="form-section">
            <h3>📝 Thông tin cơ bản</h3>

            <div class="form-field">
              <label for="productName"> Tên sản phẩm <span class="required">*</span> </label>
              <input
                id="productName"
                v-model="form.name"
                type="text"
                class="form-input"
                :class="{ error: errors.name }"
                placeholder="Nhập tên sản phẩm..."
                maxlength="100"
                @input="validateField('name')"
                @blur="generateSlug"
              />
              <div class="field-info">
                <span class="char-count">{{ form.name.length }}/100</span>
                <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
              </div>
            </div>

            <div class="form-field">
              <label for="productSlug">URL slug</label>
              <div class="slug-input">
                <span class="slug-prefix">cosmicmarketplace.com/products/</span>
                <input
                  id="productSlug"
                  v-model="form.slug"
                  type="text"
                  class="form-input"
                  placeholder="url-san-pham"
                  @input="validateSlug"
                />
              </div>
              <span v-if="errors.slug" class="error-message">{{ errors.slug }}</span>
            </div>

            <div class="form-field">
              <label for="productDescription">
                Mô tả sản phẩm <span class="required">*</span>
              </label>
              <div class="rich-editor">
                <div class="editor-toolbar">
                  <button type="button" @click="formatText('bold')" class="toolbar-btn">
                    <strong>B</strong>
                  </button>
                  <button type="button" @click="formatText('italic')" class="toolbar-btn">
                    <em>I</em>
                  </button>
                  <button type="button" @click="formatText('underline')" class="toolbar-btn">
                    <u>U</u>
                  </button>
                  <div class="toolbar-divider"></div>
                  <button
                    type="button"
                    @click="formatText('insertUnorderedList')"
                    class="toolbar-btn"
                  >
                    • List
                  </button>
                  <button
                    type="button"
                    @click="formatText('insertOrderedList')"
                    class="toolbar-btn"
                  >
                    1. List
                  </button>
                </div>
                <div
                  ref="descriptionEditor"
                  class="editor-content"
                  :class="{ error: errors.description }"
                  contenteditable="true"
                  @input="updateDescription"
                  @blur="validateField('description')"
                  data-placeholder="Mô tả chi tiết sản phẩm của bạn..."
                ></div>
              </div>
              <div class="field-info">
                <span class="word-count">{{ descriptionWordCount }} từ</span>
                <span v-if="errors.description" class="error-message">{{
                  errors.description
                }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-field">
                <label for="productCategory"> Danh mục <span class="required">*</span> </label>
                <select
                  id="productCategory"
                  v-model="form.category"
                  class="form-select"
                  :class="{ error: errors.category }"
                  @change="validateField('category')"
                >
                  <option value="">Chọn danh mục</option>
                  <option v-for="category in categories" :key="category.id" :value="category.name">
                    {{ category.name }}
                  </option>
                </select>
                <span v-if="errors.category" class="error-message">{{ errors.category }}</span>
              </div>

              <div class="form-field">
                <label for="productBrand">Thương hiệu</label>
                <input
                  id="productBrand"
                  v-model="form.brand"
                  type="text"
                  class="form-input"
                  placeholder="Tên thương hiệu"
                />
              </div>
            </div>

            <div class="form-field">
              <label>Tags sản phẩm</label>
              <div class="tags-input">
                <div class="tags-list">
                  <span v-for="(tag, index) in form.tags" :key="index" class="tag-item">
                    {{ tag }}
                    <button type="button" @click="removeTag(index)" class="tag-remove">×</button>
                  </span>
                </div>
                <input
                  v-model="newTag"
                  type="text"
                  class="tag-input"
                  placeholder="Nhập tag và nhấn Enter..."
                  @keydown.enter.prevent="addTag"
                  @keydown.comma.prevent="addTag"
                />
              </div>
              <div class="field-hint">Nhấn Enter hoặc dấu phẩy để thêm tag</div>
            </div>
          </div>

          <!-- Images Section -->
          <div class="form-section">
            <h3>📸 Hình ảnh sản phẩm</h3>

            <div class="image-upload-area">
              <div
                class="upload-zone"
                :class="{ dragover: isDragOver }"
                @drop.prevent="handleDrop"
                @dragover.prevent="isDragOver = true"
                @dragleave="isDragOver = false"
                @click="triggerFileInput"
              >
                <input
                  ref="fileInput"
                  type="file"
                  multiple
                  accept="image/*"
                  class="file-input"
                  @change="handleFileSelect"
                />

                <div v-if="form.images.length === 0" class="upload-placeholder">
                  <div class="upload-icon">📸</div>
                  <h4>Kéo thả hoặc click để upload hình ảnh</h4>
                  <p>Hỗ trợ: JPG, PNG, GIF (tối đa 5MB mỗi file)</p>
                </div>

                <div v-else class="images-grid">
                  <div
                    v-for="(image, index) in form.images"
                    :key="index"
                    class="image-item"
                    :class="{ primary: index === 0 }"
                  >
                    <img :src="image.url || image" :alt="`Product image ${index + 1}`" />
                    <div class="image-overlay">
                      <button type="button" @click.stop="setMainImage(index)" class="btn-overlay">
                        {{ index === 0 ? '⭐ Chính' : '🔄 Đặt chính' }}
                      </button>
                      <button
                        type="button"
                        @click.stop="removeImage(index)"
                        class="btn-overlay danger"
                      >
                        🗑️ Xóa
                      </button>
                    </div>
                    <div v-if="index === 0" class="primary-badge">Ảnh chính</div>
                  </div>

                  <div
                    v-if="form.images.length < 10"
                    class="add-more-images"
                    @click="triggerFileInput"
                  >
                    <div class="add-icon">➕</div>
                    <span>Thêm ảnh</span>
                  </div>
                </div>
              </div>

              <div class="image-guidelines">
                <h5>📋 Hướng dẫn ảnh sản phẩm:</h5>
                <ul>
                  <li>✅ Tối thiểu 3 ảnh, tối đa 10 ảnh</li>
                  <li>✅ Kích thước khuyến nghị: 800x800px</li>
                  <li>✅ Ảnh đầu tiên sẽ là ảnh chính</li>
                  <li>✅ Nền trắng, ánh sáng tốt</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Pricing & Inventory -->
          <div class="form-section">
            <h3>💰 Giá bán & Kho hàng</h3>

            <div class="form-row">
              <div class="form-field">
                <label for="productPrice"> Giá bán <span class="required">*</span> </label>
                <div class="price-input">
                  <input
                    id="productPrice"
                    v-model.number="form.price"
                    type="number"
                    min="0"
                    step="1000"
                    class="form-input"
                    :class="{ error: errors.price }"
                    placeholder="0"
                    @input="validateField('price')"
                  />
                  <span class="currency">VND</span>
                </div>
                <span v-if="errors.price" class="error-message">{{ errors.price }}</span>
                <div class="field-hint">Giá hiển thị: {{ formatCurrency(form.price) }}</div>
              </div>

              <div class="form-field">
                <label for="comparePrice">Giá gốc (để so sánh)</label>
                <div class="price-input">
                  <input
                    id="comparePrice"
                    v-model.number="form.comparePrice"
                    type="number"
                    min="0"
                    step="1000"
                    class="form-input"
                    placeholder="0"
                  />
                  <span class="currency">VND</span>
                </div>
                <div v-if="form.comparePrice > form.price" class="field-hint success">
                  Giảm giá: {{ Math.round((1 - form.price / form.comparePrice) * 100) }}%
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="form-field">
                <label for="productStock"> Số lượng tồn kho <span class="required">*</span> </label>
                <input
                  id="productStock"
                  v-model.number="form.stockQuantity"
                  type="number"
                  min="0"
                  class="form-input"
                  :class="{ error: errors.stockQuantity }"
                  placeholder="0"
                  @input="validateField('stockQuantity')"
                />
                <span v-if="errors.stockQuantity" class="error-message">{{
                  errors.stockQuantity
                }}</span>
              </div>

              <div class="form-field">
                <label for="productSku">Mã SKU</label>
                <input
                  id="productSku"
                  v-model="form.sku"
                  type="text"
                  class="form-input"
                  placeholder="AUTO-GENERATE"
                />
                <div class="field-hint">Để trống để tự động tạo</div>
              </div>
            </div>

            <div class="form-field">
              <label class="checkbox-label">
                <input v-model="form.trackQuantity" type="checkbox" class="form-checkbox" />
                <span class="checkmark"></span>
                Theo dõi số lượng tồn kho
              </label>
            </div>
          </div>

          <!-- Shipping Information -->
          <div class="form-section">
            <h3>🚚 Thông tin vận chuyển</h3>

            <div class="form-field">
              <label class="checkbox-label">
                <input
                  v-model="form.shippingInfo.freeShipping"
                  type="checkbox"
                  class="form-checkbox"
                />
                <span class="checkmark"></span>
                Miễn phí vận chuyển
              </label>
            </div>

            <div v-if="!form.shippingInfo.freeShipping" class="form-field">
              <label for="shippingCost">Phí vận chuyển</label>
              <div class="price-input">
                <input
                  id="shippingCost"
                  v-model.number="form.shippingInfo.shippingCost"
                  type="number"
                  min="0"
                  step="1000"
                  class="form-input"
                  placeholder="0"
                />
                <span class="currency">VND</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-field">
                <label for="productWeight">Cân nặng (gram)</label>
                <input
                  id="productWeight"
                  v-model.number="form.weight"
                  type="number"
                  min="0"
                  class="form-input"
                  placeholder="0"
                />
              </div>

              <div class="form-field">
                <label for="processingTime">Thời gian xử lý</label>
                <select
                  id="processingTime"
                  v-model="form.shippingInfo.processingTime"
                  class="form-select"
                >
                  <option value="1-2 days">1-2 ngày</option>
                  <option value="3-5 days">3-5 ngày</option>
                  <option value="1 week">1 tuần</option>
                  <option value="2 weeks">2 tuần</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-field">
                <label for="length">Chiều dài (cm)</label>
                <input
                  id="length"
                  v-model.number="form.dimensions.length"
                  type="number"
                  min="0"
                  class="form-input"
                  placeholder="0"
                />
              </div>
              <div class="form-field">
                <label for="width">Chiều rộng (cm)</label>
                <input
                  id="width"
                  v-model.number="form.dimensions.width"
                  type="number"
                  min="0"
                  class="form-input"
                  placeholder="0"
                />
              </div>
              <div class="form-field">
                <label for="height">Chiều cao (cm)</label>
                <input
                  id="height"
                  v-model.number="form.dimensions.height"
                  type="number"
                  min="0"
                  class="form-input"
                  placeholder="0"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column - Sidebar -->
        <div class="form-sidebar">
          <!-- SEO Settings -->
          <div class="sidebar-section">
            <h4>🔍 SEO & Tìm kiếm</h4>

            <div class="form-field">
              <label for="seoTitle">Tiêu đề SEO</label>
              <input
                id="seoTitle"
                v-model="form.seoTitle"
                type="text"
                class="form-input"
                :placeholder="form.name || 'Tiêu đề tối ưu SEO'"
                maxlength="60"
              />
              <div class="field-info">
                <span class="char-count">{{ (form.seoTitle || form.name).length }}/60</span>
              </div>
            </div>

            <div class="form-field">
              <label for="seoDescription">Mô tả SEO</label>
              <textarea
                id="seoDescription"
                v-model="form.seoDescription"
                class="form-textarea"
                rows="3"
                placeholder="Mô tả ngắn gọn về sản phẩm cho search engines"
                maxlength="160"
              ></textarea>
              <div class="field-info">
                <span class="char-count">{{ form.seoDescription.length }}/160</span>
              </div>
            </div>

            <!-- SEO Preview -->
            <div class="seo-preview">
              <h5>📋 Preview Google:</h5>
              <div class="search-result-preview">
                <div class="result-title">{{ form.seoTitle || form.name || 'Tên sản phẩm' }}</div>
                <div class="result-url">
                  cosmicmarketplace.com/products/{{ form.slug || 'san-pham' }}
                </div>
                <div class="result-description">
                  {{ form.seoDescription || 'Mô tả sản phẩm...' }}
                </div>
              </div>
            </div>
          </div>

          <!-- Product Status -->
          <div class="sidebar-section">
            <h4>📊 Trạng thái sản phẩm</h4>

            <div class="form-field">
              <label for="productStatus">Trạng thái</label>
              <select id="productStatus" v-model="form.status" class="form-select">
                <option value="draft">Nháp</option>
                <option value="active">Đang bán</option>
                <option value="inactive">Tạm ngưng</option>
              </select>
            </div>

            <div class="form-field">
              <label for="visibility">Hiển thị</label>
              <select id="visibility" v-model="form.visibility" class="form-select">
                <option value="public">Công khai</option>
                <option value="hidden">Ẩn</option>
              </select>
            </div>

            <div class="form-field">
              <label class="checkbox-label">
                <input v-model="form.featured" type="checkbox" class="form-checkbox" />
                <span class="checkmark"></span>
                Sản phẩm nổi bật
              </label>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="sidebar-section">
            <h4>⚡ Thao tác nhanh</h4>

            <div class="quick-actions">
              <button type="button" @click="fillSampleData" class="btn btn-outline btn-full">
                🎲 Điền dữ liệu mẫu
              </button>
              <button type="button" @click="clearForm" class="btn btn-outline btn-full">
                🗑️ Xóa form
              </button>
              <button type="button" @click="duplicateLastProduct" class="btn btn-outline btn-full">
                📋 Nhân bản sản phẩm cũ
              </button>
            </div>
          </div>

          <!-- Form Progress -->
          <div class="sidebar-section">
            <h4>📈 Tiến độ hoàn thành</h4>

            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: formProgress + '%' }"></div>
            </div>
            <div class="progress-text">{{ formProgress }}% hoàn thành</div>

            <div class="completion-checklist">
              <div class="checklist-item" :class="{ completed: form.name }">✅ Tên sản phẩm</div>
              <div class="checklist-item" :class="{ completed: form.description }">
                ✅ Mô tả sản phẩm
              </div>
              <div class="checklist-item" :class="{ completed: form.images.length > 0 }">
                ✅ Hình ảnh ({{ form.images.length }}/10)
              </div>
              <div class="checklist-item" :class="{ completed: form.price > 0 }">✅ Giá bán</div>
              <div class="checklist-item" :class="{ completed: form.category }">✅ Danh mục</div>
              <div class="checklist-item" :class="{ completed: form.stockQuantity > 0 }">
                ✅ Tồn kho
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Submit Actions -->
      <div class="form-actions">
        <div class="actions-left">
          <router-link to="/seller/products" class="btn btn-outline">
            ← Quay lại danh sách
          </router-link>
        </div>

        <div class="actions-right">
          <button type="button" @click="saveDraft" class="btn btn-secondary" :disabled="saving">
            💾 Lưu nháp
          </button>
          <button type="submit" class="btn btn-primary" :disabled="!isFormValid || saving">
            {{ isEdit ? '✅ Cập nhật sản phẩm' : '🚀 Tạo sản phẩm' }}
          </button>
        </div>
      </div>
    </form>

    <!-- Product Preview Modal -->
    <div v-if="showPreview" class="modal-overlay" @click="closePreview">
      <div class="modal-content product-preview-modal" @click.stop>
        <div class="modal-header">
          <h3>👁️ Xem trước sản phẩm</h3>
          <button @click="closePreview" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <!-- Product Preview Content -->
          <div class="preview-content">
            <div class="preview-images">
              <img
                :src="form.images[0]?.url || form.images[0] || '/placeholder-product.jpg'"
                alt="Preview"
              />
            </div>
            <div class="preview-info">
              <h1>{{ form.name || 'Tên sản phẩm' }}</h1>
              <div class="preview-price">{{ formatCurrency(form.price) }}</div>
              <div class="preview-description" v-html="form.description"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSellerStore } from '@/stores/seller'
import { useNotificationStore } from '@/stores/notifications'

const route = useRoute()
const router = useRouter()
const sellerStore = useSellerStore()
const notificationStore = useNotificationStore()

// Reactive data
const saving = ref(false)
const isDragOver = ref(false)
const showPreview = ref(false)
const newTag = ref('')
const descriptionEditor = ref(null)
const fileInput = ref(null)

// Form data
const form = ref({
  name: '',
  slug: '',
  description: '',
  category: '',
  brand: '',
  price: 0,
  comparePrice: 0,
  stockQuantity: 0,
  sku: '',
  trackQuantity: true,
  weight: 0,
  dimensions: {
    length: 0,
    width: 0,
    height: 0,
  },
  shippingInfo: {
    freeShipping: false,
    shippingCost: 0,
    processingTime: '1-2 days',
  },
  images: [],
  tags: [],
  seoTitle: '',
  seoDescription: '',
  status: 'active',
  visibility: 'public',
  featured: false,
})

// Validation errors
const errors = ref({})

// Categories
const categories = ref([
  { id: 1, name: 'Điện tử' },
  { id: 2, name: 'Thời trang' },
  { id: 3, name: 'Nhà cửa' },
  { id: 4, name: 'Sách' },
  { id: 5, name: 'Thể thao' },
  { id: 6, name: 'Làm đẹp' },
  { id: 7, name: 'Ô tô & Xe máy' },
  { id: 8, name: 'Mẹ & Bé' },
])

// Computed properties
const isEdit = computed(() => !!route.params.id)

const descriptionWordCount = computed(() => {
  const text = form.value.description.replace(/<[^>]*>/g, '')
  return text.trim() ? text.trim().split(/\s+/).length : 0
})

const formProgress = computed(() => {
  const requiredFields = [
    form.value.name,
    form.value.description,
    form.value.category,
    form.value.price > 0,
    form.value.stockQuantity > 0,
    form.value.images.length > 0,
  ]

  const completed = requiredFields.filter(Boolean).length
  return Math.round((completed / requiredFields.length) * 100)
})

const isFormValid = computed(() => {
  return (
    form.value.name &&
    form.value.description &&
    form.value.category &&
    form.value.price > 0 &&
    form.value.stockQuantity >= 0 &&
    form.value.images.length > 0 &&
    Object.keys(errors.value).length === 0
  )
})

// Methods
const validateField = (field) => {
  switch (field) {
    case 'name':
      if (!form.value.name.trim()) {
        errors.value.name = 'Tên sản phẩm là bắt buộc'
      } else if (form.value.name.length < 5) {
        errors.value.name = 'Tên sản phẩm phải có ít nhất 5 ký tự'
      } else {
        delete errors.value.name
      }
      break

    case 'description':
      const textContent = form.value.description.replace(/<[^>]*>/g, '').trim()
      if (!textContent) {
        errors.value.description = 'Mô tả sản phẩm là bắt buộc'
      } else if (textContent.length < 20) {
        errors.value.description = 'Mô tả phải có ít nhất 20 ký tự'
      } else {
        delete errors.value.description
      }
      break

    case 'category':
      if (!form.value.category) {
        errors.value.category = 'Vui lòng chọn danh mục'
      } else {
        delete errors.value.category
      }
      break

    case 'price':
      if (form.value.price <= 0) {
        errors.value.price = 'Giá sản phẩm phải lớn hơn 0'
      } else {
        delete errors.value.price
      }
      break

    case 'stockQuantity':
      if (form.value.trackQuantity && form.value.stockQuantity < 0) {
        errors.value.stockQuantity = 'Số lượng tồn kho không được âm'
      } else {
        delete errors.value.stockQuantity
      }
      break
  }
}

const generateSlug = () => {
  if (form.value.name && !form.value.slug) {
    form.value.slug = form.value.name
      .toLowerCase()
      .replace(/[^a-z0-9\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .trim('-')
  }
}

const validateSlug = () => {
  const slug = form.value.slug
  if (slug && !/^[a-z0-9-]+$/.test(slug)) {
    errors.value.slug = 'URL slug chỉ được chứa chữ thường, số và dấu gạch ngang'
  } else {
    delete errors.value.slug
  }
}

// Rich text editor
const formatText = (command) => {
  document.execCommand(command, false, null)
  updateDescription()
}

const updateDescription = () => {
  if (descriptionEditor.value) {
    form.value.description = descriptionEditor.value.innerHTML
    validateField('description')
  }
}

// Tags management
const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !form.value.tags.includes(tag) && form.value.tags.length < 10) {
    form.value.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (index) => {
  form.value.tags.splice(index, 1)
}

// Image upload
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files)
  for (const file of files) {
    if (form.value.images.length >= 10) break
    if (file.size > 5 * 1024 * 1024) {
      notificationStore.addNotification({
        type: 'error',
        message: `File ${file.name} quá lớn. Kích thước tối đa 5MB.`,
      })
      continue
    }
    // Upload lên backend
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await fetch('http://localhost:8080/api/upload/image', {
        method: 'POST',
        body: formData,
        credentials: 'include', // hoặc thêm header Authorization nếu cần
      })
      const data = await res.json()
      if (data.imageUrl) {
        form.value.images.push(data.imageUrl)
      } else {
        notificationStore.addNotification({
          type: 'error',
          message: `Lỗi upload ảnh: ${file.name}`,
        })
      }
    } catch (e) {
      notificationStore.addNotification({
        type: 'error',
        message: `Lỗi upload ảnh: ${file.name}`,
      })
    }
  }
}

const handleDrop = async (event) => {
  isDragOver.value = false
  const files = Array.from(event.dataTransfer.files).filter((file) =>
    file.type.startsWith('image/')
  )
  for (const file of files) {
    if (form.value.images.length >= 10) break
    if (file.size > 5 * 1024 * 1024) {
      notificationStore.addNotification({
        type: 'error',
        message: `File ${file.name} quá lớn. Kích thước tối đa 5MB.`,
      })
      continue
    }
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await fetch('http://localhost:8080/api/upload/image', {
        method: 'POST',
        body: formData,
        credentials: 'include',
      })
      const data = await res.json()
      if (data.imageUrl) {
        form.value.images.push(data.imageUrl)
      } else {
        notificationStore.addNotification({
          type: 'error',
          message: `Lỗi upload ảnh: ${file.name}`,
        })
      }
    } catch (e) {
      notificationStore.addNotification({
        type: 'error',
        message: `Lỗi upload ảnh: ${file.name}`,
      })
    }
  }
}

const setMainImage = (index) => {
  if (index !== 0) {
    const image = form.value.images.splice(index, 1)[0]
    form.value.images.unshift(image)
  }
}

const removeImage = (index) => {
  form.value.images.splice(index, 1)
}

// Quick actions
const fillSampleData = () => {
  form.value = {
    ...form.value,
    name: 'iPhone 15 Pro Max 256GB',
    description:
      '<p><strong>iPhone 15 Pro Max</strong> - Điện thoại thông minh cao cấp nhất từ Apple</p><ul><li>Chip A17 Pro mạnh mẽ</li><li>Camera chính 48MP với zoom quang học</li><li>Màn hình Super Retina XDR 6.7 inch</li><li>Pin sử dụng cả ngày</li></ul>',
    category: 'Điện tử',
    brand: 'Apple',
    price: 32990000,
    comparePrice: 34990000,
    stockQuantity: 50,
    weight: 221,
    tags: ['iPhone', 'Apple', 'Premium', 'Hot'],
    seoTitle: 'iPhone 15 Pro Max 256GB - Mua ngay với giá tốt nhất',
    seoDescription:
      'iPhone 15 Pro Max 256GB chính hãng, giá tốt nhất thị trường. Chip A17 Pro, camera 48MP, màn hình 6.7 inch. Bảo hành 12 tháng.',
  }
  generateSlug()
}

const clearForm = () => {
  if (confirm('Bạn có chắc muốn xóa toàn bộ form? Dữ liệu sẽ không thể khôi phục.')) {
    form.value = {
      name: '',
      slug: '',
      description: '',
      category: '',
      brand: '',
      price: 0,
      comparePrice: 0,
      stockQuantity: 0,
      sku: '',
      trackQuantity: true,
      weight: 0,
      dimensions: { length: 0, width: 0, height: 0 },
      shippingInfo: {
        freeShipping: false,
        shippingCost: 0,
        processingTime: '1-2 days',
      },
      images: [],
      tags: [],
      seoTitle: '',
      seoDescription: '',
      status: 'active',
      visibility: 'public',
      featured: false,
    }
    errors.value = {}
    if (descriptionEditor.value) {
      descriptionEditor.value.innerHTML = ''
    }
  }
}

const duplicateLastProduct = () => {
  // Implementation would get last product and populate form
  notificationStore.addNotification({
    type: 'info',
    message: 'Tính năng sẽ được thêm sau khi có dữ liệu sản phẩm',
  })
}

// Form submission
const saveDraft = async () => {
  saving.value = true
  try {
    // Auto-save logic would go here
    await new Promise((resolve) => setTimeout(resolve, 1000)) // Simulate API call

    notificationStore.addNotification({
      type: 'success',
      message: 'Đã lưu nháp thành công',
    })
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Có lỗi xảy ra khi lưu nháp',
    })
  } finally {
    saving.value = false
  }
}

const submitForm = async () => {
  // Validate all fields
  validateField('name')
  validateField('description')
  validateField('category')
  validateField('price')
  validateField('stockQuantity')

  if (!isFormValid.value) {
    notificationStore.addNotification({
      type: 'error',
      message: 'Vui lòng kiểm tra và điền đầy đủ thông tin bắt buộc',
    })
    return
  }

  saving.value = true

  try {
    if (isEdit.value) {
      await sellerStore.updateProduct(route.params.id, form.value)
      notificationStore.addNotification({
        type: 'success',
        message: 'Cập nhật sản phẩm thành công!',
      })
    } else {
      await sellerStore.createProduct(form.value)
      notificationStore.addNotification({
        type: 'success',
        message: 'Tạo sản phẩm thành công!',
      })
    }

    router.push('/seller/products')
  } catch (error) {
    notificationStore.addNotification({
      type: 'error',
      message: `Có lỗi xảy ra: ${error.message}`,
    })
  } finally {
    saving.value = false
  }
}

// Preview
const previewProduct = () => {
  showPreview.value = true
}

const closePreview = () => {
  showPreview.value = false
}

// Utility
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount || 0)
}

// Lifecycle
onMounted(async () => {
  if (isEdit.value) {
    // Load product data for editing
    const productId = route.params.id
    // In real implementation, would fetch from API
    // const product = await sellerStore.getProduct(productId)
    // if (product) {
    //   form.value = { ...product }
    // }
  }

  // Set up description editor
  await nextTick()
  if (descriptionEditor.value) {
    descriptionEditor.value.innerHTML = form.value.description
  }
})

// Auto-save functionality
let autoSaveTimer = null
watch(
  form,
  () => {
    if (autoSaveTimer) clearTimeout(autoSaveTimer)
    autoSaveTimer = setTimeout(() => {
      // Auto-save draft
      console.log('Auto-saving draft...', form.value)
    }, 3000)
  },
  { deep: true }
)
</script>

<style scoped>
.product-form {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.header-content h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.header-content p {
  color: var(--text-secondary, #a0aec0);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

/* Form Layout */
.form-layout {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 2rem;
  align-items: start;
}

/* Form Sections */
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
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sidebar-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.sidebar-section h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Form Fields */
.form-field {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-row.three-cols {
  grid-template-columns: 1fr 1fr 1fr;
}

label {
  display: block;
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #ef4444;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary, #ffffff);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--text-accent, #00d4ff);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input.error,
.form-select.error,
.form-textarea.error {
  border-color: #ef4444;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

/* Field Info */
.field-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  font-size: 0.8rem;
}

.char-count,
.word-count {
  color: var(--text-secondary, #a0aec0);
}

.error-message {
  color: #ef4444;
}

.field-hint {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.field-hint.success {
  color: #22c55e;
}

/* Slug Input */
.slug-input {
  display: flex;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.slug-prefix {
  background: rgba(0, 0, 0, 0.3);
  padding: 0.75rem;
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  border-right: 1px solid rgba(0, 212, 255, 0.2);
}

.slug-input .form-input {
  border: none;
  border-radius: 0;
}

/* Rich Text Editor */
.rich-editor {
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  background: rgba(0, 0, 0, 0.2);
  padding: 0.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.toolbar-btn {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 4px;
  color: var(--text-primary, #ffffff);
  padding: 0.25rem 0.5rem;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.toolbar-btn:hover {
  background: rgba(0, 212, 255, 0.2);
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(0, 212, 255, 0.3);
  margin: 0 0.5rem;
}

.editor-content {
  background: rgba(0, 0, 0, 0.2);
  padding: 1rem;
  min-height: 120px;
  color: var(--text-primary, #ffffff);
  line-height: 1.6;
}

.editor-content:empty::before {
  content: attr(data-placeholder);
  color: var(--text-secondary, #a0aec0);
}

.editor-content:focus {
  outline: none;
}

/* Tags Input */
.tags-input {
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.tag-item {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.tag-remove {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-weight: bold;
}

.tag-input {
  background: none;
  border: none;
  color: var(--text-primary, #ffffff);
  outline: none;
  width: 100%;
  padding: 0.25rem;
}

/* Price Input */
.price-input {
  position: relative;
  display: flex;
  align-items: center;
}

.currency {
  position: absolute;
  right: 0.75rem;
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  pointer-events: none;
}

.price-input .form-input {
  padding-right: 4rem;
}

/* Checkbox */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  color: var(--text-primary, #ffffff);
}

.form-checkbox {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.checkmark {
  position: relative;
  display: inline-block;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.form-checkbox:checked + .checkmark {
  background: var(--text-accent, #00d4ff);
  border-color: var(--text-accent, #00d4ff);
}

.form-checkbox:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #000;
  font-size: 12px;
  font-weight: bold;
}

/* Image Upload */
.image-upload-area {
  margin-bottom: 1rem;
}

.upload-zone {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.1);
}

.upload-zone:hover,
.upload-zone.dragover {
  border-color: var(--text-accent, #00d4ff);
  background: rgba(0, 212, 255, 0.05);
}

.file-input {
  display: none;
}

.upload-placeholder {
  color: var(--text-secondary, #a0aec0);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.upload-placeholder h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.image-item.primary {
  border-color: var(--text-accent, #00d4ff);
}

.image-item img {
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.btn-overlay {
  background: rgba(0, 212, 255, 0.8);
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  cursor: pointer;
}

.btn-overlay.danger {
  background: rgba(239, 68, 68, 0.8);
}

.primary-badge {
  position: absolute;
  top: 0.25rem;
  left: 0.25rem;
  background: var(--text-accent, #00d4ff);
  color: #000;
  padding: 0.125rem 0.25rem;
  border-radius: 4px;
  font-size: 0.6rem;
  font-weight: bold;
}

.add-more-images {
  aspect-ratio: 1;
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary, #a0aec0);
  transition: all 0.3s ease;
}

.add-more-images:hover {
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
}

.add-icon {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

/* Image Guidelines */
.image-guidelines {
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
}

.image-guidelines h5 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.image-guidelines ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.image-guidelines li {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

/* SEO Preview */
.seo-preview {
  margin-top: 1rem;
}

.seo-preview h5 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.search-result-preview {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 1rem;
}

.result-title {
  color: #1a73e8;
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.result-url {
  color: #006621;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.result-description {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  line-height: 1.4;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.btn-full {
  width: 100%;
  justify-content: center;
}

/* Progress */
.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--text-accent, #00d4ff), #00a8cc);
  transition: width 0.3s ease;
}

.progress-text {
  color: var(--text-primary, #ffffff);
  font-size: 0.9rem;
  font-weight: 500;
  text-align: center;
  margin-bottom: 1rem;
}

.completion-checklist {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.checklist-item {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.8rem;
  padding: 0.25rem 0;
  transition: color 0.3s ease;
}

.checklist-item.completed {
  color: #22c55e;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
}

.actions-left,
.actions-right {
  display: flex;
  gap: 1rem;
}

/* Buttons */
.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  text-decoration: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.btn-primary {
  background: var(--text-accent, #00d4ff);
  color: #000;
}

.btn-primary:hover:not(:disabled) {
  background: #00a8cc;
  transform: translateY(-1px);
}

.btn-secondary {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.btn-outline {
  background: transparent;
  color: var(--text-primary, #ffffff);
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.btn-outline:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.1);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Modal */
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
  z-index: 1000;
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

.product-preview-modal {
  width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.modal-header h3 {
  color: var(--text-primary, #ffffff);
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  color: var(--text-secondary, #a0aec0);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 1.5rem;
}

.preview-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
}

.preview-images img {
  width: 100%;
  border-radius: 8px;
}

.preview-info h1 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.preview-price {
  color: var(--text-accent, #00d4ff);
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.preview-description {
  color: var(--text-secondary, #a0aec0);
  line-height: 1.6;
}

/* Responsive */
@media (max-width: 1200px) {
  .form-layout {
    grid-template-columns: 1fr;
  }

  .form-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .product-form {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header-actions {
    width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
    gap: 1rem;
  }

  .actions-left,
  .actions-right {
    width: 100%;
    justify-content: center;
  }

  .preview-content {
    grid-template-columns: 1fr;
  }
}
</style>
