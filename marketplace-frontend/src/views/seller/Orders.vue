<template>
  <div class="seller-orders-page">
    <header class="page-header">
      <h1>📋 Quản lý đơn hàng</h1>
      <p>Xử lý và theo dõi tất cả đơn hàng của bạn</p>
    </header>

    <!-- Filter Tabs -->
    <div class="filter-tabs">
      <button 
        v-for="status in orderStatuses" 
        :key="status.value" 
        class="filter-tab"
        :class="{ active: activeFilter === status.value }"
        @click="activeFilter = status.value"
      >
        {{ status.label }}
        <span class="count">{{ getOrderCount(status.value) }}</span>
      </button>
    </div>

    <!-- Search and Actions -->
    <div class="controls-section">
      <div class="search-box">
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="Tìm theo mã đơn hàng, tên khách hàng..."
          class="search-input"
        />
        <span class="search-icon">🔍</span>
      </div>
      
      <div class="action-buttons">
        <button @click="exportReport" class="btn-export">📄 Xuất báo cáo</button>
        <button @click="refreshOrders" class="btn-refresh">🔄 Làm mới</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner">🔄</div>
      <p>Đang tải đơn hàng...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredOrders.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3>{{ activeFilter ? 'Không có đơn hàng nào' : 'Chưa có đơn hàng nào' }}</h3>
      <p>{{ activeFilter ? `Không có đơn hàng với trạng thái "${getStatusText(activeFilter)}"` : 'Đơn hàng sẽ hiển thị ở đây khi có khách hàng mua sản phẩm của bạn' }}</p>
    </div>

    <!-- Orders List -->
    <div v-else class="orders-list">
      <div v-for="order in displayedOrders" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <h4 class="order-id">#{{ order.id.slice(-8) }}</h4>
            <p class="order-date">{{ formatDate(order.createdAt) }}</p>
          </div>
          <div class="status-section">
            <select 
              v-model="order.status" 
              @change="updateOrderStatus(order)"
              class="status-select"
              :disabled="updating"
            >
              <option value="PENDING">🕐 Chờ xử lý</option>
              <option value="PROCESSING">⚙️ Đang xử lý</option>
              <option value="SHIPPED">🚚 Đã giao vận</option>
              <option value="DELIVERED">✅ Đã giao</option>
              <option value="CANCELLED">❌ Đã hủy</option>
            </select>
          </div>
        </div>

        <div class="order-content">
          <div class="customer-info">
            <h5>👤 Khách hàng</h5>
            <p>{{ order.customerName || 'Khách hàng' }}</p>
            <p class="customer-email">{{ order.customerEmail || 'email@example.com' }}</p>
          </div>

          <div class="order-summary">
            <h5>📦 Sản phẩm</h5>
            <p>{{ order.itemCount || 1 }} sản phẩm</p>
            <p class="order-total">{{ formatMoney(order.totalAmount) }}</p>
          </div>
        </div>

        <div class="order-actions">
          <button @click="viewOrderDetails(order)" class="action-btn view" title="Xem chi tiết">
            👁️ Chi tiết
          </button>
          <button @click="contactCustomer(order)" class="action-btn contact" title="Liên hệ khách hàng">
            💬 Liên hệ
          </button>
          <button @click="printShippingLabel(order)" class="action-btn print" title="In nhãn giao hàng">
            🖨️ In nhãn
          </button>
        </div>
      </div>

      <!-- Show more button if there are more orders -->
      <div v-if="filteredOrders.length > 5" class="show-more">
        <button @click="loadMoreOrders" class="btn-show-more">
          Xem thêm {{ filteredOrders.length - displayedOrders.length }} đơn hàng
        </button>
      </div>
    </div>

    <!-- Coming Soon Notice -->
    <div class="coming-soon-notice">
      <div class="notice-content">
        <h3>🚀 Quản lý đơn hàng đang phát triển</h3>
        <p>Chức năng này sẽ có trong Phase 3 với đầy đủ tính năng:</p>
        <div class="features-grid">
          <div class="feature-group">
            <h4>📋 Quản lý đơn hàng</h4>
            <div class="feature-item">• Real-time order tracking</div>
            <div class="feature-item">• Bulk status updates</div>
            <div class="feature-item">• Order analytics</div>
            <div class="feature-item">• Custom order statuses</div>
          </div>
          <div class="feature-group">
            <h4>🖨️ Logistics</h4>
            <div class="feature-item">• Auto print shipping labels</div>
            <div class="feature-item">• Multi-carrier integration</div>
            <div class="feature-item">• Return management</div>
            <div class="feature-item">• Delivery confirmation</div>
          </div>
          <div class="feature-group">
            <h4>💬 Communication</h4>
            <div class="feature-item">• In-app chat with customers</div>
            <div class="feature-item">• Automated email updates</div>
            <div class="feature-item">• SMS notifications</div>
            <div class="feature-item">• Order dispute resolution</div>
          </div>
          <div class="feature-group">
            <h4>📊 Advanced Features</h4>
            <div class="feature-item">• Predictive analytics</div>
            <div class="feature-item">• Export capabilities</div>
            <div class="feature-item">• API integrations</div>
            <div class="feature-item">• Custom reporting</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Details Modal -->
    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Chi tiết đơn hàng #{{ selectedOrder?.id?.slice(-8) }}</h3>
          <button @click="closeOrderModal" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedOrder" class="order-details">
            <div class="detail-section">
              <h4>Thông tin khách hàng</h4>
              <p><strong>Tên:</strong> {{ selectedOrder.customerName }}</p>
              <p><strong>Email:</strong> {{ selectedOrder.customerEmail }}</p>
              <p><strong>Số điện thoại:</strong> {{ selectedOrder.customerPhone || 'Chưa có' }}</p>
            </div>
            <div class="detail-section">
              <h4>Địa chỉ giao hàng</h4>
              <p>{{ selectedOrder.shippingAddress || 'Chưa có địa chỉ' }}</p>
            </div>
            <div class="detail-section">
              <h4>Sản phẩm</h4>
              <div class="products-list">
                <div v-for="item in selectedOrder.items" :key="item.id" class="product-item">
                  <span>{{ item.name }} × {{ item.quantity }}</span>
                  <span>{{ formatMoney(item.price) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useSellerStore } from '@/stores/seller'

const sellerStore = useSellerStore()

// Reactive data
const activeFilter = ref('')
const searchQuery = ref('')
const loading = ref(false)
const updating = ref(false)
const showOrderModal = ref(false)
const selectedOrder = ref(null)
const ordersToShow = ref(5)

const orderStatuses = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'PROCESSING', label: 'Đang xử lý' },
  { value: 'SHIPPED', label: 'Đã giao vận' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' }
]

// Mock data for demonstration
const mockOrders = [
  {
    id: 'ORD001234567890',
    customerName: 'Nguyễn Văn A',
    customerEmail: 'customer1@example.com',
    customerPhone: '0123456789',
    status: 'PENDING',
    totalAmount: 1250000,
    itemCount: 2,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2 hours ago
    shippingAddress: '123 Đường ABC, Quận 1, TP.HCM',
    items: [
      { id: 1, name: 'Laptop Gaming', quantity: 1, price: 1000000 },
      { id: 2, name: 'Chuột Gaming', quantity: 1, price: 250000 }
    ]
  },
  {
    id: 'ORD001234567891',
    customerName: 'Trần Thị B',
    customerEmail: 'customer2@example.com',
    customerPhone: '0987654321',
    status: 'PROCESSING',
    totalAmount: 890000,
    itemCount: 1,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24), // 1 day ago
    shippingAddress: '456 Đường XYZ, Quận 2, TP.HCM',
    items: [
      { id: 3, name: 'Bàn phím cơ', quantity: 1, price: 890000 }
    ]
  },
  {
    id: 'ORD001234567892',
    customerName: 'Lê Văn C',
    customerEmail: 'customer3@example.com',
    customerPhone: '0369852147',
    status: 'DELIVERED',
    totalAmount: 2100000,
    itemCount: 3,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3), // 3 days ago
    shippingAddress: '789 Đường KLM, Quận 3, TP.HCM',
    items: [
      { id: 4, name: 'Màn hình 24 inch', quantity: 1, price: 1500000 },
      { id: 5, name: 'Webcam HD', quantity: 2, price: 300000 }
    ]
  }
]

// Computed properties
const orders = computed(() => sellerStore.orders.length > 0 ? sellerStore.orders : mockOrders)

const filteredOrders = computed(() => {
  let filtered = orders.value

  // Filter by status
  if (activeFilter.value) {
    filtered = filtered.filter(order => order.status === activeFilter.value)
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(order => 
      order.id.toLowerCase().includes(query) ||
      order.customerName.toLowerCase().includes(query) ||
      order.customerEmail.toLowerCase().includes(query)
    )
  }

  return filtered
})

const displayedOrders = computed(() => {
  return filteredOrders.value.slice(0, ordersToShow.value)
})

// Methods
const getOrderCount = (status) => {
  if (!status) return orders.value.length
  return orders.value.filter(order => order.status === status).length
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': 'Chờ xử lý',
    'PROCESSING': 'Đang xử lý',
    'SHIPPED': 'Đã giao vận',
    'DELIVERED': 'Đã giao',
    'CANCELLED': 'Đã hủy'
  }
  return statusMap[status] || status
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount || 0)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffHours = Math.ceil(diffTime / (1000 * 60 * 60))
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffHours < 1) return 'Vừa xong'
  if (diffHours < 24) return `${diffHours} giờ trước`
  if (diffDays === 1) return 'Hôm qua'
  if (diffDays <= 7) return `${diffDays} ngày trước`
  
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const updateOrderStatus = async (order) => {
  try {
    updating.value = true
    await sellerStore.updateOrderStatus(order.id, order.status)
    alert(`Đã cập nhật trạng thái đơn hàng #${order.id.slice(-8)} thành "${getStatusText(order.status)}"`)
  } catch (error) {
    alert('Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
    console.error('Update order status error:', error)
  } finally {
    updating.value = false
  }
}

const viewOrderDetails = (order) => {
  selectedOrder.value = order
  showOrderModal.value = true
}

const closeOrderModal = () => {
  showOrderModal.value = false
  selectedOrder.value = null
}

const contactCustomer = (order) => {
  // Open chat with customer
  alert(`Mở chat với khách hàng: ${order.customerName}`)
}

const printShippingLabel = (order) => {
  // Print shipping label
  alert(`In nhãn giao hàng cho đơn #${order.id.slice(-8)}`)
}

const loadMoreOrders = () => {
  ordersToShow.value += 5
}

const refreshOrders = async () => {
  try {
    loading.value = true
    await sellerStore.loadOrders()
    alert('Đã làm mới danh sách đơn hàng')
  } catch (error) {
    alert('Có lỗi xảy ra khi tải đơn hàng')
  } finally {
    loading.value = false
  }
}

const exportReport = () => {
  // Export orders report
  alert('Xuất báo cáo đơn hàng (Chức năng sẽ có trong Phase 3)')
}

// Lifecycle
onMounted(async () => {
  try {
    loading.value = true
    await sellerStore.loadOrders()
  } catch (error) {
    console.error('Load orders error:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.seller-orders-page {
  padding: 2rem;
  min-height: 100vh;
  background: rgba(16, 16, 24, 0.95);
  color: var(--text-primary, #ffffff);
}

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

.filter-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-tab {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: var(--text-secondary, #a0aec0);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.filter-tab:hover, 
.filter-tab.active {
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
  background: rgba(0, 212, 255, 0.1);
}

.count {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent, #00d4ff);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  margin-left: 0.5rem;
  font-weight: 600;
}

.controls-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.search-box {
  flex: 1;
  position: relative;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 3rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  color: var(--text-primary, #ffffff);
  font-size: 0.9rem;
}

.search-input::placeholder {
  color: var(--text-secondary, #a0aec0);
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent, #00d4ff);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary, #a0aec0);
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-export, 
.btn-refresh {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-export:hover, 
.btn-refresh:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.loading-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--text-secondary, #a0aec0);
}

.loading-spinner {
  font-size: 2rem;
  margin-bottom: 1rem;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

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

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.order-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.order-card:hover {
  border-color: var(--text-accent, #00d4ff);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.order-info {
  flex: 1;
}

.order-id {
  color: var(--text-primary, #ffffff);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.order-date {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  margin: 0;
}

.status-section {
  flex: none;
}

.status-select {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-primary, #ffffff);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
}

.status-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.order-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.customer-info h5, 
.order-summary h5 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.customer-info p, 
.order-summary p {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.customer-email {
  color: var(--text-secondary, #a0aec0) !important;
  font-size: 0.85rem !important;
}

.order-total {
  color: var(--text-accent, #00d4ff) !important;
  font-weight: 600 !important;
  font-size: 1.1rem !important;
}

.order-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.action-btn {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.85rem;
  font-weight: 500;
}

.action-btn:hover {
  background: rgba(0, 212, 255, 0.3);
  transform: translateY(-2px);
}

.action-btn.contact {
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.action-btn.contact:hover {
  background: rgba(16, 185, 129, 0.3);
}

.action-btn.print {
  background: rgba(245, 158, 11, 0.2);
  border-color: rgba(245, 158, 11, 0.3);
  color: #f59e0b;
}

.action-btn.print:hover {
  background: rgba(245, 158, 11, 0.3);
}

.show-more {
  text-align: center;
  margin-top: 1rem;
}

.btn-show-more {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.75rem 2rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-show-more:hover {
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
  margin-bottom: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  text-align: left;
}

.feature-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
  font-size: 1rem;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  font-size: 0.9rem;
}

.feature-item:last-child {
  border-bottom: none;
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
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
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
  color: var(--text-accent, #00d4ff);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary, #a0aec0);
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

.modal-body {
  padding: 1.5rem;
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section h4 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 0.75rem;
}

.detail-section p {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .seller-orders-page {
    padding: 1rem;
  }

  .controls-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
    margin-bottom: 1rem;
  }

  .filter-tabs {
    justify-content: center;
    flex-wrap: wrap;
  }

  .order-content {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .order-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    margin: 1rem;
    width: calc(100% - 2rem);
  }
}

@media (max-width: 480px) {
  .order-actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .action-btn {
    text-align: center;
  }
}

/* CSS Variables */
:root {
  --text-primary: #ffffff;
  --text-secondary: #a0aec0;
  --text-accent: #00d4ff;
}
</style>