<template>
  <div class="profile-page">
    <div class="container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-cover">
          <div class="cover-overlay"></div>
          <div class="profile-main">
            <div class="profile-avatar-section">
              <div class="avatar-container">
                <img 
                  :src="userProfile.avatar || '/default-avatar.png'" 
                  :alt="userProfile.name"
                  class="profile-avatar"
                />
                <button @click="showAvatarUpload = true" class="avatar-edit-btn">
                  📷
                </button>
              </div>
              <div class="profile-info">
                <h1 class="profile-name">{{ userProfile.name }}</h1>
                <p class="profile-email">{{ userProfile.email }}</p>
                <div class="profile-badges">
                  <span v-if="userProfile.isVip" class="badge vip-badge">
                    👑 VIP Member
                  </span>
                  <span v-if="userProfile.isVerified" class="badge verified-badge">
                    ✅ Xác thực
                  </span>
                  <span class="badge member-badge">
                    🚀 {{ getMembershipLevel() }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-number">{{ userProfile.totalOrders }}</span>
                <span class="stat-label">Đơn hàng</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ formatCurrency(userProfile.totalSpent) }}</span>
                <span class="stat-label">Đã chi tiêu</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userProfile.wishlistCount }}</span>
                <span class="stat-label">Yêu thích</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userProfile.reviewCount }}</span>
                <span class="stat-label">Đánh giá</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Navigation -->
      <div class="profile-nav">
        <button 
          v-for="tab in profileTabs" 
          :key="tab.id"
          @click="activeTab = tab.id"
          class="nav-tab"
          :class="{ active: activeTab === tab.id }"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-text">{{ tab.name }}</span>
          <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
        </button>
      </div>

      <!-- Profile Content -->
      <div class="profile-content">
        <!-- Personal Info Tab -->
        <div v-if="activeTab === 'info'" class="tab-content">
          <div class="content-header">
            <h2>👤 Thông tin cá nhân</h2>
            <button @click="editMode = !editMode" class="btn btn-secondary">
              {{ editMode ? '❌ Hủy' : '✏️ Chỉnh sửa' }}
            </button>
          </div>

          <div class="info-cards">
            <div class="info-card space-card">
              <h3>📋 Thông tin chính</h3>
              <form @submit.prevent="updateProfile" class="info-form">
                <div class="form-row">
                  <div class="form-group">
                    <label for="firstName">Họ *</label>
                    <input 
                      id="firstName"
                      v-model="profileForm.firstName" 
                      type="text" 
                      class="form-input"
                      :disabled="!editMode"
                      required
                    />
                  </div>
                  <div class="form-group">
                    <label for="lastName">Tên *</label>
                    <input 
                      id="lastName"
                      v-model="profileForm.lastName" 
                      type="text" 
                      class="form-input"
                      :disabled="!editMode"
                      required
                    />
                  </div>
                </div>

                <div class="form-group">
                  <label for="email">Email *</label>
                  <input 
                    id="email"
                    v-model="profileForm.email" 
                    type="email" 
                    class="form-input"
                    disabled
                  />
                  <small class="field-note">Email không thể thay đổi</small>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="phone">Số điện thoại</label>
                    <input 
                      id="phone"
                      v-model="profileForm.phone" 
                      type="tel" 
                      class="form-input"
                      :disabled="!editMode"
                      placeholder="0123456789"
                    />
                  </div>
                  <div class="form-group">
                    <label for="birthday">Ngày sinh</label>
                    <input 
                      id="birthday"
                      v-model="profileForm.birthday" 
                      type="date" 
                      class="form-input"
                      :disabled="!editMode"
                    />
                  </div>
                </div>

                <div class="form-group">
                  <label for="address">Địa chỉ</label>
                  <textarea 
                    id="address"
                    v-model="profileForm.address" 
                    class="form-input"
                    :disabled="!editMode"
                    rows="3"
                    placeholder="Nhập địa chỉ của bạn..."
                  ></textarea>
                </div>

                <div class="form-actions" v-if="editMode">
                  <button type="submit" class="btn btn-primary" :disabled="updating">
                    {{ updating ? '🔄 Đang cập nhật...' : '💾 Lưu thay đổi' }}
                  </button>
                </div>
              </form>
            </div>

            <div class="info-card space-card">
              <h3>🔒 Bảo mật</h3>
              <div class="security-section">
                <div class="security-item">
                  <div class="security-info">
                    <h4>Mật khẩu</h4>
                    <p>Cập nhật lần cuối: {{ formatDate(userProfile.lastPasswordChange) }}</p>
                  </div>
                  <button @click="showChangePassword = true" class="btn btn-secondary">
                    🔑 Đổi mật khẩu
                  </button>
                </div>

                <div class="security-item">
                  <div class="security-info">
                    <h4>Xác thực 2 bước</h4>
                    <p>{{ userProfile.twoFactorEnabled ? 'Đã bật' : 'Chưa bật' }}</p>
                  </div>
                  <button 
                    @click="toggle2FA" 
                    class="btn"
                    :class="userProfile.twoFactorEnabled ? 'btn-danger' : 'btn-primary'"
                  >
                    {{ userProfile.twoFactorEnabled ? '❌ Tắt' : '🔐 Bật' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Orders Tab -->
        <div v-if="activeTab === 'orders'" class="tab-content">
          <div class="content-header">
            <h2>📦 Lịch sử đơn hàng</h2>
            <select v-model="orderFilter" @change="filterOrders" class="filter-select">
              <option value="all">Tất cả đơn hàng</option>
              <option value="pending">Chờ xử lý</option>
              <option value="processing">Đang xử lý</option>
              <option value="delivered">Đã giao</option>
              <option value="cancelled">Đã hủy</option>
            </select>
          </div>

          <div class="orders-list">
            <div 
              v-for="order in filteredOrders" 
              :key="order.id"
              class="order-card space-card"
            >
              <div class="order-header">
                <div class="order-info">
                  <h3 class="order-id">Đơn hàng #{{ order.id }}</h3>
                  <p class="order-date">{{ formatDate(order.date) }}</p>
                </div>
                <div class="order-status">
                  <span class="status-badge" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
                </div>
              </div>

              <div class="order-items">
                <div 
                  v-for="item in order.items.slice(0, 2)" 
                  :key="item.id"
                  class="order-item"
                >
                  <img 
                    :src="item.image || '/placeholder-product.jpg'" 
                    :alt="item.name"
                    class="item-image"
                  />
                  <div class="item-info">
                    <h4>{{ item.name }}</h4>
                    <p>Số lượng: {{ item.quantity }}</p>
                    <p class="item-price">{{ formatCurrency(item.price) }}</p>
                  </div>
                </div>
                <div v-if="order.items.length > 2" class="more-items">
                  +{{ order.items.length - 2 }} sản phẩm khác
                </div>
              </div>

              <div class="order-footer">
                <div class="order-total">
                  <span class="total-label">Tổng cộng:</span>
                  <span class="total-amount">{{ formatCurrency(order.total) }}</span>
                </div>
                <div class="order-actions">
                  <button @click="viewOrderDetail(order.id)" class="btn btn-secondary">
                    👁️ Chi tiết
                  </button>
                  <button 
                    v-if="order.status === 'delivered'" 
                    @click="reorder(order.id)" 
                    class="btn btn-primary"
                  >
                    🔄 Mua lại
                  </button>
                  <button 
                    v-if="order.status === 'pending'" 
                    @click="cancelOrder(order.id)" 
                    class="btn btn-danger"
                  >
                    ❌ Hủy
                  </button>
                </div>
              </div>
            </div>

            <div v-if="filteredOrders.length === 0" class="no-orders">
              <div class="no-orders-content">
                <div class="no-orders-icon">📦</div>
                <h3>Chưa có đơn hàng nào</h3>
                <p>Bắt đầu mua sắm để tích lũy lịch sử đơn hàng</p>
                <router-link to="/products" class="btn btn-primary">
                  🛍️ Bắt đầu mua sắm
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <!-- Wishlist Tab -->
        <div v-if="activeTab === 'wishlist'" class="tab-content">
          <div class="content-header">
            <h2>❤️ Danh sách yêu thích</h2>
            <p class="content-subtitle">{{ wishlistItems.length }} sản phẩm</p>
          </div>

          <div class="wishlist-grid">
            <div 
              v-for="item in wishlistItems" 
              :key="item.id"
              class="wishlist-item space-card"
            >
              <div class="wishlist-image">
                <img 
                  :src="item.image || '/placeholder-product.jpg'" 
                  :alt="item.name"
                />
                <button @click="removeFromWishlist(item.id)" class="remove-wishlist-btn">
                  ❌
                </button>
              </div>
              <div class="wishlist-info">
                <h3>{{ item.name }}</h3>
                <div class="wishlist-price">
                  <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">
                    {{ formatCurrency(item.originalPrice) }}
                  </span>
                  <span class="current-price">{{ formatCurrency(item.price) }}</span>
                </div>
                <div class="wishlist-rating">
                  <div class="rating-stars">
                    <span v-for="i in 5" :key="i" class="star" :class="[i <= item.rating ? 'filled' : '']">⭐</span>
                  </div>
                  <span class="rating-text">({{ item.reviewCount }})</span>
                </div>
                <div class="wishlist-actions">
                  <button @click="addToCart(item)" class="btn btn-primary">
                    🛒 Thêm vào giỏ
                  </button>
                  <router-link :to="`/products/${item.id}`" class="btn btn-secondary">
                    👁️ Xem
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <div v-if="wishlistItems.length === 0" class="no-wishlist">
            <div class="no-wishlist-content">
              <div class="no-wishlist-icon">💔</div>
              <h3>Danh sách yêu thích trống</h3>
              <p>Hãy thêm những sản phẩm bạn quan tâm vào danh sách yêu thích</p>
              <router-link to="/products" class="btn btn-primary">
                🌟 Khám phá sản phẩm
              </router-link>
            </div>
          </div>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="tab-content">
          <div class="content-header">
            <h2>⚙️ Cài đặt</h2>
          </div>

          <div class="settings-sections">
            <div class="settings-section space-card">
              <h3>🔔 Thông báo</h3>
              <div class="settings-list">
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Email thông báo đơn hàng</h4>
                    <p>Nhận email khi đơn hàng có cập nhật</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      v-model="settings.emailNotifications" 
                      type="checkbox"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>

                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Thông báo khuyến mãi</h4>
                    <p>Nhận thông tin về các chương trình ưu đãi</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      v-model="settings.promotionalNotifications" 
                      type="checkbox"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
              </div>
            </div>

            <div class="settings-section space-card">
              <h3>🎨 Giao diện</h3>
              <div class="settings-list">
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Chế độ tối</h4>
                    <p>Chuyển đổi giữa giao diện sáng và tối</p>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      v-model="settings.darkMode" 
                      type="checkbox"
                      @change="updateSettings"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>

                <div class="setting-item">
                  <div class="setting-info">
                    <h4>Ngôn ngữ</h4>
                    <p>Chọn ngôn ngữ hiển thị</p>
                  </div>
                  <select v-model="settings.language" @change="updateSettings" class="setting-select">
                    <option value="vi">Tiếng Việt</option>
                    <option value="en">English</option>
                    <option value="ko">한국어</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Modal -->
    <div v-if="showChangePassword" class="modal-overlay" @click="showChangePassword = false">
      <div class="modal-content space-card" @click.stop>
        <div class="modal-header">
          <h3>🔑 Đổi mật khẩu</h3>
          <button @click="showChangePassword = false" class="modal-close">✕</button>
        </div>
        <form @submit.prevent="changePassword" class="password-form">
          <div class="form-group">
            <label for="currentPassword">Mật khẩu hiện tại *</label>
            <input 
              id="currentPassword"
              v-model="passwordForm.currentPassword" 
              type="password" 
              class="form-input"
              required
            />
          </div>
          <div class="form-group">
            <label for="newPassword">Mật khẩu mới *</label>
            <input 
              id="newPassword"
              v-model="passwordForm.newPassword" 
              type="password" 
              class="form-input"
              required
            />
          </div>
          <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu mới *</label>
            <input 
              id="confirmPassword"
              v-model="passwordForm.confirmPassword" 
              type="password" 
              class="form-input"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" @click="showChangePassword = false" class="btn btn-secondary">
              Hủy
            </button>
            <button type="submit" class="btn btn-primary" :disabled="passwordChanging">
              {{ passwordChanging ? '🔄 Đang đổi...' : '💾 Đổi mật khẩu' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Avatar Upload Modal -->
    <div v-if="showAvatarUpload" class="modal-overlay" @click="showAvatarUpload = false">
      <div class="modal-content space-card" @click.stop>
        <div class="modal-header">
          <h3>📷 Cập nhật ảnh đại diện</h3>
          <button @click="showAvatarUpload = false" class="modal-close">✕</button>
        </div>
        <div class="avatar-upload">
          <div class="upload-area" @click="$refs.avatarInput.click()">
            <div v-if="previewAvatar" class="preview-container">
              <img :src="previewAvatar" alt="Preview" class="avatar-preview" />
            </div>
            <div v-else class="upload-placeholder">
              <div class="upload-icon">📷</div>
              <p>Nhấn để chọn ảnh</p>
            </div>
            <input 
              ref="avatarInput"
              type="file" 
              accept="image/*" 
              @change="handleAvatarSelect"
              style="display: none"
            />
          </div>
          <div class="modal-actions">
            <button @click="showAvatarUpload = false" class="btn btn-secondary">
              Hủy
            </button>
            <button @click="uploadAvatar" class="btn btn-primary" :disabled="!previewAvatar || uploading">
              {{ uploading ? '🔄 Đang tải...' : '💾 Cập nhật' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

export default {
  name: 'Profile',
  setup() {
    const authStore = useAuthStore()
    const cartStore = useCartStore()
    
    // Reactive data
    const activeTab = ref('info')
    const editMode = ref(false)
    const updating = ref(false)
    const passwordChanging = ref(false)
    const uploading = ref(false)
    const showChangePassword = ref(false)
    const showAvatarUpload = ref(false)
    const orderFilter = ref('all')
    const previewAvatar = ref('')
    
    // Profile data
    const userProfile = ref({
      id: 1,
      name: 'Nguyễn Văn A',
      email: 'user@cosmicmarket.com',
      avatar: '/user-avatar.jpg',
      phone: '0123456789',
      birthday: '1990-01-01',
      address: '123 Đường ABC, Quận 1, TP.HCM',
      isVip: true,
      isVerified: true,
      totalOrders: 25,
      totalSpent: 45000000,
      wishlistCount: 12,
      reviewCount: 18,
      lastPasswordChange: '2024-11-15',
      twoFactorEnabled: false
    })
    
    const profileForm = ref({
      firstName: 'Nguyễn Văn',
      lastName: 'A',
      email: 'user@cosmicmarket.com',
      phone: '0123456789',
      birthday: '1990-01-01',
      address: '123 Đường ABC, Quận 1, TP.HCM'
    })
    
    const passwordForm = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const settings = ref({
      emailNotifications: true,
      promotionalNotifications: false,
      darkMode: false,
      language: 'vi'
    })
    
    const profileTabs = ref([
      { id: 'info', name: 'Thông tin', icon: '👤' },
      { id: 'orders', name: 'Đơn hàng', icon: '📦', badge: userProfile.value.totalOrders },
      { id: 'wishlist', name: 'Yêu thích', icon: '❤️', badge: userProfile.value.wishlistCount },
      { id: 'settings', name: 'Cài đặt', icon: '⚙️' }
    ])
    
    // Mock orders data
    const orders = ref([
      {
        id: 'CM2024001',
        date: '2024-06-15',
        status: 'delivered',
        total: 25000000,
        items: [
          { id: 1, name: 'Laptop Gaming Galactic Pro', quantity: 1, price: 25000000, image: '/product1.jpg' }
        ]
      },
      {
        id: 'CM2024002',
        date: '2024-06-10',
        status: 'processing',
        total: 3500000,
        items: [
          { id: 2, name: 'Gaming Mouse Nebula', quantity: 1, price: 1500000, image: '/product2.jpg' },
          { id: 3, name: 'Mechanical Keyboard Cosmos', quantity: 1, price: 2000000, image: '/product3.jpg' }
        ]
      }
    ])
    
    // Mock wishlist data
    const wishlistItems = ref([
      {
        id: 1,
        name: 'Gaming Headset Galaxy',
        price: 1800000,
        originalPrice: 2200000,
        rating: 4.5,
        reviewCount: 89,
        image: '/product4.jpg'
      },
      {
        id: 2,
        name: 'Monitor 4K Stardust',
        price: 8500000,
        rating: 4.7,
        reviewCount: 156,
        image: '/product5.jpg'
      }
    ])
    
    // Computed properties
    const filteredOrders = computed(() => {
      if (orderFilter.value === 'all') return orders.value
      return orders.value.filter(order => order.status === orderFilter.value)
    })
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const formatDate = (dateString) => {
      return new Date(dateString).toLocaleDateString('vi-VN')
    }
    
    const getMembershipLevel = () => {
      const spent = userProfile.value.totalSpent
      if (spent >= 50000000) return 'Diamond'
      if (spent >= 20000000) return 'Gold'
      if (spent >= 5000000) return 'Silver'
      return 'Bronze'
    }
    
    const getStatusClass = (status) => {
      const classes = {
        pending: 'status-pending',
        processing: 'status-processing',
        delivered: 'status-delivered',
        cancelled: 'status-cancelled'
      }
      return classes[status] || 'status-pending'
    }
    
    const getStatusText = (status) => {
      const texts = {
        pending: 'Chờ xử lý',
        processing: 'Đang xử lý',
        delivered: 'Đã giao',
        cancelled: 'Đã hủy'
      }
      return texts[status] || 'Không xác định'
    }
    
    const updateProfile = async () => {
      updating.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        userProfile.value.name = `${profileForm.value.firstName} ${profileForm.value.lastName}`
        editMode.value = false
        alert('Cập nhật thông tin thành công!')
      } catch (error) {
        alert('Có lỗi xảy ra khi cập nhật thông tin')
      } finally {
        updating.value = false
      }
    }
    
    const changePassword = async () => {
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        alert('Mật khẩu xác nhận không khớp')
        return
      }
      
      passwordChanging.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
        showChangePassword.value = false
        alert('Đổi mật khẩu thành công!')
      } catch (error) {
        alert('Có lỗi xảy ra khi đổi mật khẩu')
      } finally {
        passwordChanging.value = false
      }
    }
    
    const handleAvatarSelect = (event) => {
      const file = event.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          previewAvatar.value = e.target.result
        }
        reader.readAsDataURL(file)
      }
    }
    
    const uploadAvatar = async () => {
      uploading.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1500))
        userProfile.value.avatar = previewAvatar.value
        showAvatarUpload.value = false
        previewAvatar.value = ''
        alert('Cập nhật ảnh đại diện thành công!')
      } catch (error) {
        alert('Có lỗi xảy ra khi tải ảnh')
      } finally {
        uploading.value = false
      }
    }
    
    const toggle2FA = () => {
      userProfile.value.twoFactorEnabled = !userProfile.value.twoFactorEnabled
      const status = userProfile.value.twoFactorEnabled ? 'bật' : 'tắt'
      alert(`Đã ${status} xác thực 2 bước`)
    }
    
    const filterOrders = () => {
      // Orders will be filtered automatically
    }
    
    const viewOrderDetail = (orderId) => {
      alert(`Xem chi tiết đơn hàng ${orderId}`)
    }
    
    const reorder = (orderId) => {
      alert('Đã thêm các sản phẩm vào giỏ hàng!')
    }
    
    const cancelOrder = (orderId) => {
      if (confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 'cancelled'
          alert('Đã hủy đơn hàng')
        }
      }
    }
    
    const removeFromWishlist = (itemId) => {
      const index = wishlistItems.value.findIndex(item => item.id === itemId)
      if (index > -1) {
        wishlistItems.value.splice(index, 1)
        userProfile.value.wishlistCount--
        alert('Đã xóa khỏi danh sách yêu thích')
      }
    }
    
    const addToCart = async (item) => {
      try {
        await cartStore.addToCart(item.id, 1)
        alert('Đã thêm sản phẩm vào giỏ hàng!')
      } catch (error) {
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng')
      }
    }
    
    const updateSettings = () => {
      alert('Cài đặt đã được lưu!')
    }
    
    // Lifecycle
    onMounted(() => {
      console.log('Profile page mounted')
    })
    
    return {
      activeTab,
      editMode,
      updating,
      passwordChanging,
      uploading,
      showChangePassword,
      showAvatarUpload,
      orderFilter,
      previewAvatar,
      userProfile,
      profileForm,
      passwordForm,
      settings,
      profileTabs,
      orders,
      wishlistItems,
      filteredOrders,
      formatCurrency,
      formatDate,
      getMembershipLevel,
      getStatusClass,
      getStatusText,
      updateProfile,
      changePassword,
      handleAvatarSelect,
      uploadAvatar,
      toggle2FA,
      filterOrders,
      viewOrderDetail,
      reorder,
      cancelOrder,
      removeFromWishlist,
      addToCart,
      updateSettings
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.profile-header {
  margin-bottom: 2rem;
}

.profile-cover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 2rem;
  position: relative;
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}

.profile-main {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
}

.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.avatar-container {
  position: relative;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background: var(--aurora-gradient);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-edit-btn:hover {
  transform: scale(1.1);
}

.profile-info {
  color: white;
}

.profile-name {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.profile-email {
  opacity: 0.9;
  margin-bottom: 1rem;
}

.profile-badges {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  backdrop-filter: blur(10px);
  animation: slideIn 0.8s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.vip-badge {
  background: rgba(255, 215, 0, 0.9);
  color: #000;
}

.verified-badge {
  background: rgba(16, 185, 129, 0.9);
  color: white;
}

.member-badge {
  background: rgba(138, 43, 226, 0.9);
  color: white;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem;
  text-align: center;
  color: white;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.profile-nav {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  padding: 0.5rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  overflow-x: auto;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
}

.nav-tab:hover,
.nav-tab.active {
  background: var(--aurora-gradient);
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--neon-glow);
}

.tab-icon {
  font-size: 1.1rem;
}

.tab-text {
  font-weight: 500;
}

.tab-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.1rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.profile-content {
  min-height: 500px;
}

.tab-content {
  animation: fadeInUp 0.5s ease-out;
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

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.content-header h2 {
  color: var(--text-accent);
  font-size: 1.8rem;
}

.content-subtitle {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.info-cards {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
}

.info-card {
  padding: 2rem;
}

.info-card h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
}

.info-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.form-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field-note {
  color: var(--text-secondary);
  font-size: 0.8rem;
  opacity: 0.7;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.security-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.security-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.security-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.filter-select {
  padding: 0.5rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-card {
  padding: 1.5rem;
  border: 1px solid rgba(0, 212, 255, 0.2);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
}

.order-card:hover {
  border-color: var(--text-accent);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-id {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.order-date {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-pending {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.status-processing {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.status-delivered {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.order-item {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  flex: none;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.item-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.item-price {
  color: var(--text-accent);
  font-weight: 600;
}

.more-items {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-style: italic;
  text-align: center;
  padding: 0.5rem;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.order-total {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.total-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.total-amount {
  color: var(--text-accent);
  font-size: 1.3rem;
  font-weight: 600;
}

.order-actions {
  display: flex;
  gap: 0.75rem;
}

.wishlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.wishlist-item {
  padding: 1.5rem;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
}

.wishlist-item:hover {
  transform: translateY(-5px);
  box-shadow: var(--neon-glow);
}

.wishlist-image {
  position: relative;
  margin-bottom: 1rem;
}

.wishlist-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.remove-wishlist-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.remove-wishlist-btn:hover {
  background: rgba(239, 68, 68, 0.8);
  transform: scale(1.1);
}

.wishlist-info h3 {
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.wishlist-price {
  margin-bottom: 0.75rem;
}

.original-price {
  color: var(--text-secondary);
  text-decoration: line-through;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.current-price {
  color: var(--text-accent);
  font-size: 1.2rem;
  font-weight: 600;
}

.wishlist-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.rating-stars {
  display: flex;
  gap: 0.1rem;
}

.star {
  font-size: 1rem;
  opacity: 0.3;
}

.star.filled {
  opacity: 1;
}

.rating-text {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.wishlist-actions {
  display: flex;
  gap: 0.75rem;
}

.wishlist-actions .btn {
  flex: 1;
  padding: 0.6rem;
  font-size: 0.9rem;
}

.settings-sections {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.settings-section {
  padding: 2rem;
}

.settings-section h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.setting-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.setting-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.2);
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background: var(--aurora-gradient);
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.setting-select {
  padding: 0.5rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
}

.no-orders,
.no-wishlist {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.no-orders-content,
.no-wishlist-content {
  text-align: center;
  max-width: 400px;
}

.no-orders-icon,
.no-wishlist-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-orders-content h3,
.no-wishlist-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.no-orders-content p,
.no-wishlist-content p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  line-height: 1.6;
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
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
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
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.avatar-upload {
  text-align: center;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
}

.upload-area:hover {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.05);
}

.preview-container {
  display: flex;
  justify-content: center;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--text-accent);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-icon {
  font-size: 3rem;
  color: var(--text-secondary);
}

.upload-placeholder p {
  color: var(--text-secondary);
  margin: 0;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .profile-main {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .profile-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .info-cards {
    grid-template-columns: 1fr;
  }

  .wishlist-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 1rem 0;
  }

  .profile-cover {
    padding: 1.5rem;
  }

  .profile-avatar {
    width: 100px;
    height: 100px;
  }

  .profile-name {
    font-size: 1.5rem;
  }

  .profile-stats {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .profile-nav {
    flex-direction: column;
    gap: 0.25rem;
  }

  .nav-tab {
    justify-content: center;
    padding: 0.75rem;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .order-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .order-actions {
    width: 100%;
    justify-content: space-between;
  }

  .wishlist-grid {
    grid-template-columns: 1fr;
  }

  .wishlist-actions {
    flex-direction: column;
  }

  .setting-item {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .profile-cover {
    padding: 1rem;
  }

  .profile-avatar {
    width: 80px;
    height: 80px;
  }

  .avatar-edit-btn {
    width: 30px;
    height: 30px;
  }

  .profile-name {
    font-size: 1.3rem;
  }

  .profile-badges {
    justify-content: center;
  }

  .info-card {
    padding: 1.5rem;
  }

  .order-card,
  .wishlist-item {
    padding: 1rem;
  }

  .modal-content {
    margin: 1rem;
  }

  .upload-area {
    padding: 1.5rem;
  }

  .avatar-preview {
    width: 100px;
    height: 100px;
  }
}
</style>