views/seller/Orders.vue - Enhanced Order Management
<template>
  <div class="seller-orders-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1>📦 Quản lý Đơn hàng</h1>
          <p>Xử lý và theo dõi tất cả đơn hàng của bạn</p>
        </div>
        <div class="header-actions">
          <button @click="refreshOrders" :disabled="loading" class="btn-refresh">
            <span class="icon">🔄</span>
            {{ loading ? 'Đang tải...' : 'Làm mới' }}
          </button>
          <button @click="exportOrders" :disabled="loadingExport" class="btn-export">
            <span class="icon">📊</span>
            {{ loadingExport ? 'Đang xuất...' : 'Xuất báo cáo' }}
          </button>
        </div>
      </div>

      <div class="quick-stats">
        <div class="stat-card pending" @click="setStatusFilter('pending')">
          <div class="stat-number">{{ orderStats.pending }}</div>
          <div class="stat-label">Chờ xử lý</div>
        </div>
        <div class="stat-card processing" @click="setStatusFilter('processing')">
          <div class="stat-number">{{ orderStats.processing }}</div>
          <div class="stat-label">Đang xử lý</div>
        </div>
        <div class="stat-card shipped" @click="setStatusFilter('shipped')">
          <div class="stat-number">{{ orderStats.shipped }}</div>
          <div class="stat-label">Đã gửi</div>
        </div>
        <div class="stat-card delivered" @click="setStatusFilter('delivered')">
          <div class="stat-number">{{ orderStats.delivered }}</div>
          <div class="stat-label">Đã giao</div>
        </div>
      </div>
    </div>

    <div class="filters-section">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <span class="search-icon">🔍</span>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm theo mã đơn hàng, tên khách hàng, email..."
            class="search-input"
            @input="debounceSearch"
          />
          <button v-if="searchQuery" @click="clearSearch" class="clear-search">✕</button>
        </div>
      </div>

      <div class="filter-controls">
        <select v-model="filters.status" @change="applyFilters" class="filter-select">
          <option value="all">Tất cả trạng thái</option>
          <option value="pending">Chờ xử lý</option>
          <option value="processing">Đang xử lý</option>
          <option value="shipped">Đã gửi hàng</option>
          <option value="delivered">Đã giao hàng</option>
          <option value="cancelled">Đã hủy</option>
        </select>

        <select v-model="filters.dateRange" @change="applyFilters" class="filter-select">
          <option value="all">Tất cả thời gian</option>
          <option value="today">Hôm nay</option>
          <option value="week">7 ngày qua</option>
          <option value="month">30 ngày qua</option>
          <option value="quarter">90 ngày qua</option>
        </select>

        <select v-model="filters.sortBy" @change="applyFilters" class="filter-select">
          <option value="createdAt">Ngày tạo</option>
          <option value="totalAmount">Giá trị đơn hàng</option>
          <option value="customerName">Tên khách hàng</option>
          <option value="status">Trạng thái</option>
        </select>

        <button @click="toggleSortOrder" class="sort-toggle">
          {{ filters.sortOrder === 'desc' ? '↓' : '↑' }}
        </button>
      </div>
    </div>

    <div v-if="selectedOrders.length > 0" class="bulk-actions">
      <div class="bulk-info">
        <span class="selected-count">{{ selectedOrders.length }} đơn hàng được chọn</span>
        <button @click="clearSelection" class="btn-clear">Bỏ chọn</button>
      </div>
      <div class="bulk-buttons">
        <button @click="bulkUpdateStatus('PROCESSING')" class="btn-bulk processing">
          Chuyển sang Đang xử lý
        </button>
        <button @click="bulkUpdateStatus('SHIPPED')" class="btn-bulk shipped">
          Chuyển sang Đã gửi
        </button>
        <button @click="bulkUpdateStatus('DELIVERED')" class="btn-bulk delivered">
          Chuyển sang Đã giao
        </button>
        <button @click="bulkPrintLabels" class="btn-bulk print">In nhãn giao hàng</button>
      </div>
    </div>

    <div class="orders-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Đang tải đơn hàng...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>Không có đơn hàng nào</h3>
        <p>
          {{
            searchQuery
              ? 'Không tìm thấy đơn hàng phù hợp với tìm kiếm của bạn'
              : 'Chưa có đơn hàng nào được tạo'
          }}
        </p>
        <button v-if="searchQuery" @click="clearSearch" class="btn-clear-search">Xóa bộ lọc</button>
      </div>

      <div v-else class="orders-list">
        <div class="select-all-row">
          <label class="checkbox-wrapper">
            <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" />
            <span class="checkmark"></span>
            Chọn tất cả ({{ filteredOrders.length }})
          </label>
        </div>

        <div
          v-for="order in displayedOrders"
          :key="order.id"
          class="order-card"
          :class="{
            selected: selectedOrders.includes(order.id),
            urgent: isUrgentOrder(order),
          }"
        >
          <div class="order-selection">
            <label class="checkbox-wrapper">
              <input
                type="checkbox"
                :checked="selectedOrders.includes(order.id)"
                @change="toggleOrderSelection(order.id)"
              />
              <span class="checkmark"></span>
            </label>
          </div>

          <div class="order-header">
            <div class="order-info">
              <div class="order-id">
                <span class="id-label">Đơn hàng</span>
                <span class="id-value">#{{ order.id.slice(-8) }}</span>
                <span v-if="isUrgentOrder(order)" class="urgent-badge">🚨 Gấp</span>
              </div>
              <div class="order-meta">
                <span class="date">{{ formatDate(order.createdAt) }}</span>
                <span class="separator">•</span>
                <span class="customer">{{ order.customerName }}</span>
              </div>
            </div>

            <div class="order-status-section">
              <div class="status-badge" :style="{ backgroundColor: getStatusColor(order.status) }">
                {{ getStatusLabel(order.status) }}
              </div>
              <div class="order-value">{{ formatCurrency(order.totalAmount) }}</div>
            </div>
          </div>

          <div class="order-items-preview">
            <div class="items-info">
              <span class="items-count">{{ order.items.length }} sản phẩm</span>
              <span class="items-list">
                {{
                  order.items
                    .slice(0, 2)
                    .map((item) => item.productName)
                    .join(', ')
                }}
                <span v-if="order.items.length > 2"
                  >và {{ order.items.length - 2 }} sản phẩm khác</span
                >
              </span>
            </div>
          </div>

          <div class="order-timeline">
            <div class="timeline-item" :class="{ active: hasStatus(order, 'PENDING') }">
              <div class="timeline-dot"></div>
              <span class="timeline-label">Đặt hàng</span>
            </div>
            <div class="timeline-item" :class="{ active: hasStatus(order, 'PROCESSING') }">
              <div class="timeline-dot"></div>
              <span class="timeline-label">Xử lý</span>
            </div>
            <div class="timeline-item" :class="{ active: hasStatus(order, 'SHIPPED') }">
              <div class="timeline-dot"></div>
              <span class="timeline-label">Gửi hàng</span>
            </div>
            <div class="timeline-item" :class="{ active: hasStatus(order, 'DELIVERED') }">
              <div class="timeline-dot"></div>
              <span class="timeline-label">Giao hàng</span>
            </div>
          </div>

          <div class="order-actions">
            <button @click="viewOrderDetails(order)" class="action-btn primary">👁️ Chi tiết</button>

            <div class="status-actions">
              <button
                v-if="order.status === 'PENDING'"
                @click="quickStatusUpdate(order.id, 'PROCESSING')"
                :disabled="isUpdating(order.id)"
                class="action-btn success"
              >
                ✅ Xử lý
              </button>

              <button
                v-if="order.status === 'PROCESSING'"
                @click="quickStatusUpdate(order.id, 'SHIPPED')"
                :disabled="isUpdating(order.id)"
                class="action-btn info"
              >
                🚚 Gửi hàng
              </button>

              <button
                v-if="order.status === 'SHIPPED'"
                @click="quickStatusUpdate(order.id, 'DELIVERED')"
                :disabled="isUpdating(order.id)"
                class="action-btn delivered"
              >
                📦 Đã giao
              </button>
            </div>

            <div class="more-actions">
              <button @click="contactCustomer(order)" class="action-btn secondary">
                💬 Liên hệ
              </button>
              <button @click="printShippingLabel(order)" class="action-btn print">
                🖨️ In nhãn
              </button>
              <button @click="showOrderMenu(order)" class="action-btn menu">⋮</button>
            </div>
          </div>
        </div>

        <div v-if="filteredOrders.length > displayedOrders.length" class="load-more">
          <button @click="loadMoreOrders" class="btn-load-more">
            Xem thêm {{ filteredOrders.length - displayedOrders.length }} đơn hàng
          </button>
        </div>
      </div>
    </div>

    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content" @click.stop>
        <OrderDetailsModal
          :order="selectedOrder"
          @close="closeOrderModal"
          @status-updated="handleStatusUpdate"
          @message-sent="handleMessageSent"
        />
      </div>
    </div>

    <div v-if="showChatModal" class="modal-overlay" @click="closeChatModal">
      <div class="modal-content" @click.stop>
        <CustomerChatModal
          :order="chatOrder"
          @close="closeChatModal"
          @message-sent="handleChatMessage"
        />
      </div>
    </div>

    <div class="notifications">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification"
        :class="notification.type"
      >
        <span class="notification-message">{{ notification.message }}</span>
        <button @click="removeNotification(notification.id)" class="notification-close">✕</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useSellerStore } from '@/stores/seller'
import OrderDetailsModal from '@/components/seller/OrderDetailsModal.vue'
import CustomerChatModal from '@/components/seller/CustomerChatModal.vue'

const sellerStore = useSellerStore()

// Reactive data
const loading = ref(false)
const loadingExport = ref(false)
const searchQuery = ref('')
const ordersToShow = ref(20)
const showOrderModal = ref(false)
const showChatModal = ref(false)
const selectedOrder = ref(null)
const chatOrder = ref(null)

// Filters
const filters = ref({
  status: 'all',
  dateRange: 'all',
  sortBy: 'createdAt',
  sortOrder: 'desc',
})

// Computed properties
const filteredOrders = computed(() => sellerStore.filteredOrders)
const displayedOrders = computed(() => filteredOrders.value.slice(0, ordersToShow.value))
const orderStats = computed(() => sellerStore.orderStats)
const selectedOrders = computed(() => sellerStore.selectedOrders)
const notifications = computed(() => sellerStore.notifications)

const allSelected = computed({
  get: () =>
    filteredOrders.value.length > 0 &&
    filteredOrders.value.every((order) => selectedOrders.value.includes(order.id)),
  set: (value) => {
    if (value) {
      sellerStore.selectAllOrders()
    } else {
      sellerStore.clearOrderSelection()
    }
  },
})

// Methods
const refreshOrders = async () => {
  loading.value = true
  try {
    await sellerStore.loadOrders(true)
  } finally {
    loading.value = false
  }
}

const exportOrders = async () => {
  loadingExport.value = true
  try {
    await sellerStore.exportOrdersReport()
  } finally {
    loadingExport.value = false
  }
}

const setStatusFilter = (status) => {
  filters.value.status = status
  applyFilters()
}

const applyFilters = () => {
  sellerStore.setOrderFilters(filters.value)
}

const toggleSortOrder = () => {
  filters.value.sortOrder = filters.value.sortOrder === 'desc' ? 'asc' : 'desc'
  applyFilters()
}

const clearSearch = () => {
  searchQuery.value = ''
  sellerStore.setOrderFilters({ search: '' })
}

const debounceSearch = (() => {
  let timeout
  return () => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      sellerStore.setOrderFilters({ search: searchQuery.value })
    }, 300)
  }
})()

const toggleSelectAll = () => {
  if (allSelected.value) {
    sellerStore.clearOrderSelection()
  } else {
    sellerStore.selectAllOrders()
  }
}

const toggleOrderSelection = (orderId) => {
  sellerStore.toggleOrderSelection(orderId)
}

const clearSelection = () => {
  sellerStore.clearOrderSelection()
}

const bulkUpdateStatus = async (status) => {
  if (selectedOrders.value.length === 0) return

  const confirmed = confirm(
    `Bạn có chắc chắn muốn cập nhật ${
      selectedOrders.value.length
    } đơn hàng sang trạng thái "${sellerStore.getStatusLabel(status)}"?`
  )
  if (!confirmed) return

  try {
    await sellerStore.bulkUpdateOrderStatus(selectedOrders.value, status)
    sellerStore.clearOrderSelection()
  } catch (error) {
    console.error('Bulk update error:', error)
  }
}

const bulkPrintLabels = () => {
  const orderIds = selectedOrders.value
  // Generate print labels for multiple orders
  const printContent = orderIds
    .map((id) => {
      const order = filteredOrders.value.find((o) => o.id === id)
      return generateShippingLabel(order)
    })
    .join('<div style="page-break-after: always;"></div>')

  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <html>
      <head><title>Shipping Labels</title></head>
      <body>${printContent}</body>
    </html>
  `)
  printWindow.print()
}

const quickStatusUpdate = async (orderId, status) => {
  try {
    await sellerStore.updateOrderStatus(orderId, status)
  } catch (error) {
    console.error('Quick status update error:', error)
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
  chatOrder.value = order
  showChatModal.value = true
}

const closeChatModal = () => {
  showChatModal.value = false
  chatOrder.value = null
}

const printShippingLabel = (order) => {
  const labelContent = generateShippingLabel(order)
  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <html>
      <head><title>Shipping Label - ${order.id}</title></head>
      <body>${labelContent}</body>
    </html>
  `)
  printWindow.print()
}

const generateShippingLabel = (order) => {
  return `
    <div style="width: 4in; height: 6in; border: 1px solid #000; padding: 10px; font-family: Arial;">
      <h3>COSMIC MARKETPLACE</h3>
      <p><strong>Đơn hàng:</strong> #${order.id.slice(-8)}</p>
      <p><strong>Người nhận:</strong> ${order.customerName}</p>
      <p><strong>Địa chỉ:</strong> ${order.shippingAddress}</p>
      <p><strong>SĐT:</strong> ${order.customerPhone}</p>
      <p><strong>Giá trị:</strong> ${formatCurrency(order.totalAmount)}</p>
      <p><strong>Ngày:</strong> ${formatDate(order.createdAt)}</p>
    </div>
  `
}

const showOrderMenu = (order) => {
  alert(`Menu cho đơn hàng #${order.id.slice(-8)}\n- Xem lịch sử\n- Tạo hoàn trả\n- Báo cáo vấn đề`)
}

const loadMoreOrders = () => {
  ordersToShow.value += 20
}

const handleStatusUpdate = (orderId, newStatus) => {
  console.log(`Order ${orderId} updated to ${newStatus}`)
}

const handleMessageSent = (orderId, message) => {
  console.log(`Message sent to order ${orderId}:`, message)
}

const handleChatMessage = (orderId, message) => {
  console.log(`Chat message sent to order ${orderId}:`, message)
}

const removeNotification = (notificationId) => {
  sellerStore.removeNotification(notificationId)
}

// Utility functions
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

const getStatusLabel = (status) => {
  return sellerStore.getStatusLabel(status)
}

const getStatusColor = (status) => {
  return sellerStore.getStatusColor(status)
}

const hasStatus = (order, status) => {
  const statusOrder = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED']
  const currentIndex = statusOrder.indexOf(order.status)
  const checkIndex = statusOrder.indexOf(status)
  return currentIndex >= checkIndex
}

const isUrgentOrder = (order) => {
  const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
  return (
    (order.status === 'PENDING' || order.status === 'PROCESSING') &&
    new Date(order.createdAt) < threeDaysAgo
  )
}

const isUpdating = (orderId) => {
  return sellerStore.loading.orderUpdate[orderId] || false
}

// Watchers
watch(
  filters,
  (newFilters) => {
    sellerStore.setOrderFilters(newFilters)
  },
  { deep: true }
)

// Lifecycle
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([sellerStore.loadOrders(), sellerStore.fetchDashboardStats()])

    // Start auto-refresh
    sellerStore.startAutoRefresh()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  sellerStore.stopAutoRefresh()
})
</script>

<style scoped>
.seller-orders-page {
  padding: 2rem;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(16, 16, 24, 0.95), rgba(26, 26, 46, 0.9));
  color: #ffffff;
}

.page-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.title-section h1 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-refresh,
.btn-export {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #00d4ff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-refresh:hover,
.btn-export:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-2px);
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 212, 255, 0.5);
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.stat-card.pending .stat-number {
  color: #f59e0b;
}
.stat-card.processing .stat-number {
  color: #3b82f6;
}
.stat-card.shipped .stat-number {
  color: #8b5cf6;
}
.stat-card.delivered .stat-number {
  color: #10b981;
}

.filters-section {
  background: rgba(26, 26, 46, 0.6);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.search-bar {
  margin-bottom: 1rem;
}

.search-input-wrapper {
  position: relative;
  max-width: 500px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 2.5rem 0.75rem 2.5rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #ffffff;
  font-size: 1rem;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #00d4ff;
}

.clear-search {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #ff6b6b;
  cursor: pointer;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.5rem 1rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  min-width: 150px;
}

.sort-toggle {
  padding: 0.5rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #00d4ff;
  cursor: pointer;
  width: 40px;
}

.bulk-actions {
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bulk-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.bulk-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-bulk {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.btn-bulk.processing {
  background: #3b82f6;
  color: white;
}
.btn-bulk.shipped {
  background: #8b5cf6;
  color: white;
}
.btn-bulk.delivered {
  background: #10b981;
  color: white;
}
.btn-bulk.print {
  background: #6b7280;
  color: white;
}

.orders-container {
  background: rgba(26, 26, 46, 0.6);
  border-radius: 12px;
  padding: 1.5rem;
}

.loading-state {
  text-align: center;
  padding: 3rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 212, 255, 0.2);
  border-top: 4px solid #00d4ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
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
  padding: 3rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.select-all-row {
  padding: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  margin-bottom: 1rem;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-wrapper input[type='checkbox'] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(0, 212, 255, 0.5);
  border-radius: 4px;
  position: relative;
  transition: all 0.2s ease;
}

.checkbox-wrapper input[type='checkbox']:checked + .checkmark {
  background: #00d4ff;
  border-color: #00d4ff;
}

.checkbox-wrapper input[type='checkbox']:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  top: -2px;
  left: 2px;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.order-card {
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
}

.order-card:hover {
  border-color: rgba(0, 212, 255, 0.5);
  transform: translateY(-2px);
}

.order-card.selected {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.05);
}

.order-card.urgent {
  border-color: #f59e0b;
  background: rgba(245, 158, 11, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-id {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.id-value {
  font-weight: bold;
  color: #00d4ff;
}

.urgent-badge {
  background: #f59e0b;
  color: white;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
}

.order-meta {
  color: #a0aec0;
  font-size: 0.875rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  color: white;
  font-weight: 500;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.order-value {
  font-size: 1.125rem;
  font-weight: bold;
  color: #00d4ff;
}

.order-items-preview {
  margin-bottom: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 8px;
}

.order-timeline {
  display: flex;
  justify-content: space-between;
  margin: 1.5rem 0;
  position: relative;
}

.order-timeline::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  height: 2px;
  background: rgba(0, 212, 255, 0.2);
  z-index: 1;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.timeline-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(107, 114, 128, 0.5);
  border: 2px solid rgba(107, 114, 128, 0.5);
  transition: all 0.3s ease;
}

.timeline-item.active .timeline-dot {
  background: #00d4ff;
  border-color: #00d4ff;
}

.timeline-label {
  font-size: 0.75rem;
  margin-top: 0.5rem;
  color: #a0aec0;
}

.timeline-item.active .timeline-label {
  color: #00d4ff;
  font-weight: 500;
}

.order-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: space-between;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.action-btn.primary {
  background: #00d4ff;
  color: #000;
}
.action-btn.success {
  background: #10b981;
  color: white;
}
.action-btn.info {
  background: #3b82f6;
  color: white;
}
.action-btn.delivered {
  background: #8b5cf6;
  color: white;
}
.action-btn.secondary {
  background: rgba(107, 114, 128, 0.8);
  color: white;
}
.action-btn.print {
  background: rgba(245, 158, 11, 0.8);
  color: white;
}
.action-btn.menu {
  background: rgba(75, 85, 99, 0.8);
  color: white;
}

.action-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.1);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.status-actions,
.more-actions {
  display: flex;
  gap: 0.5rem;
}

.load-more {
  text-align: center;
  margin-top: 2rem;
}

.btn-load-more {
  padding: 0.75rem 2rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: #00d4ff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-load-more:hover {
  background: rgba(0, 212, 255, 0.2);
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
}

.modal-content {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  width: 90%;
}

.notifications {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 1001;
  max-width: 400px;
}

.notification {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 0.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: slideIn 0.3s ease;
}

.notification.success {
  border-color: #10b981;
}
.notification.error {
  border-color: #ef4444;
}
.notification.warning {
  border-color: #f59e0b;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.notification-close {
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  font-size: 1.2rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .seller-orders-page {
    padding: 1rem;
  }

  .header-content {
    flex-direction: column;
    gap: 1rem;
  }

  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-controls {
    flex-direction: column;
  }

  .bulk-actions {
    flex-direction: column;
    gap: 1rem;
  }

  .order-header {
    flex-direction: column;
    gap: 1rem;
  }

  .order-timeline {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .order-actions {
    flex-direction: column;
  }

  .status-actions,
  .more-actions {
    justify-content: center;
  }
}
</style>
