<template>
  <div class="orders-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">📋 Đơn hàng của tôi</h1>
        <p class="page-subtitle">Quản lý và theo dõi tất cả đơn hàng</p>
      </div>

      <!-- Orders Filter -->
      <div class="filter-section">
        <div class="filter-tabs">
          <button
            v-for="status in orderStatuses"
            :key="status.key"
            @click="activeFilter = status.key"
            class="filter-tab"
            :class="{ active: activeFilter === status.key }"
          >
            <span class="tab-icon">{{ status.icon }}</span>
            <span class="tab-label">{{ status.label }}</span>
            <span v-if="getOrderCount(status.key) > 0" class="tab-count">{{
              getOrderCount(status.key)
            }}</span>
          </button>
        </div>

        <div class="filter-actions">
          <div class="search-box">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="🔍 Tìm kiếm đơn hàng..."
              class="search-input"
            />
          </div>
          <select v-model="sortBy" class="sort-select">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="amount-high">Giá trị cao</option>
            <option value="amount-low">Giá trị thấp</option>
          </select>
        </div>
      </div>

      <!-- Orders List -->
      <div v-if="!loading && filteredOrders.length" class="orders-section">
        <div class="orders-list">
          <div
            v-for="order in paginatedOrders"
            :key="order.id"
            class="order-card space-card"
            @click="viewOrderDetail(order.id)"
          >
            <div class="order-header">
              <div class="order-info">
                <div class="order-id">
                  <span class="id-label">Đơn hàng:</span>
                  <span class="id-value">#{{ order.id }}</span>
                </div>
                <div class="order-date">{{ formatDate(order.createdAt) }}</div>
              </div>
              <div class="order-status">
                <span class="status-badge" :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
            </div>

            <div class="order-content">
              <div class="order-items">
                <div class="items-preview">
                  <div
                    v-for="(item, index) in order.items.slice(0, 3)"
                    :key="item.id"
                    class="item-preview"
                  >
                    <img
                      :src="item.productImage || '/placeholder-product.jpg'"
                      :alt="item.productName"
                      class="item-image"
                    />
                  </div>
                  <div v-if="order.items.length > 3" class="more-items">
                    +{{ order.items.length - 3 }}
                  </div>
                </div>

                <div class="items-info">
                  <div class="item-count">{{ order.items.length }} sản phẩm</div>
                  <div class="item-names">
                    {{
                      order.items
                        .slice(0, 2)
                        .map((item) => item.productName)
                        .join(', ')
                    }}
                    <span v-if="order.items.length > 2">...</span>
                  </div>
                </div>
              </div>

              <div class="order-summary">
                <div class="summary-row">
                  <span class="summary-label">Tổng tiền:</span>
                  <span class="summary-value total-amount">{{
                    formatCurrency(order.totalAmount)
                  }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Phương thức:</span>
                  <span class="summary-value">{{ order.paymentMethod }}</span>
                </div>
              </div>
            </div>

            <div class="order-footer">
              <div class="order-progress">
                <div class="progress-steps">
                  <div
                    v-for="step in orderSteps"
                    :key="step.key"
                    class="progress-step"
                    :class="{
                      completed: getStepStatus(order.status, step.key) === 'completed',
                      active: getStepStatus(order.status, step.key) === 'active',
                    }"
                  >
                    <div class="step-icon">{{ step.icon }}</div>
                    <div class="step-label">{{ step.label }}</div>
                  </div>
                </div>
              </div>

              <div class="order-actions">
                <button
                  v-if="order.status === 'PENDING'"
                  @click.stop="cancelOrder(order.id)"
                  class="btn btn-secondary btn-sm"
                >
                  ❌ Hủy đơn
                </button>
                <button @click.stop="viewOrderDetail(order.id)" class="btn btn-primary btn-sm">
                  👁️ Chi tiết
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-section">
          <div class="pagination">
            <button
              @click="currentPage = Math.max(1, currentPage - 1)"
              :disabled="currentPage === 1"
              class="pagination-btn"
            >
              ← Trước
            </button>

            <div class="pagination-info">
              <span>Trang {{ currentPage }} / {{ totalPages }}</span>
              <span class="results-count">({{ filteredOrders.length }} đơn hàng)</span>
            </div>

            <button
              @click="currentPage = Math.min(totalPages, currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="pagination-btn"
            >
              Sau →
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && filteredOrders.length === 0" class="empty-section">
        <div class="empty-content">
          <div class="empty-animation">
            <div class="floating-box">📦</div>
            <div class="cosmic-particles">
              <span class="particle">✨</span>
              <span class="particle">🌟</span>
              <span class="particle">💫</span>
            </div>
          </div>

          <h3>{{ getEmptyMessage() }}</h3>
          <p>{{ getEmptyDescription() }}</p>

          <div class="empty-actions">
            <router-link to="/products" class="btn btn-primary"> 🛍️ Mua sắm ngay </router-link>
            <button
              v-if="activeFilter !== 'all'"
              @click="activeFilter = 'all'"
              class="btn btn-secondary"
            >
              🔍 Xem tất cả
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner">🔄</div>
        <p>Đang tải đơn hàng...</p>
      </div>

      <!-- Quick Stats -->
      <div v-if="!loading && orders.length > 0" class="stats-section">
        <div class="stats-grid">
          <div class="stat-card space-card">
            <div class="stat-icon">📊</div>
            <div class="stat-value">{{ orders.length }}</div>
            <div class="stat-label">Tổng đơn hàng</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">💰</div>
            <div class="stat-value">{{ formatCurrency(totalSpent) }}</div>
            <div class="stat-label">Tổng chi tiêu</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">⭐</div>
            <div class="stat-value">{{ completedOrders }}</div>
            <div class="stat-label">Đã hoàn thành</div>
          </div>
          <div class="stat-card space-card">
            <div class="stat-icon">🚀</div>
            <div class="stat-value">{{ averageOrderValue }}</div>
            <div class="stat-label">Giá trị TB</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getStatusLabel, getStatusColor } from '@/utils/constants'

export default {
  name: 'Orders',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    // Reactive data
    const activeFilter = ref('all')
    const searchQuery = ref('')
    const sortBy = ref('newest')
    const currentPage = ref(1)
    const itemsPerPage = 10

    // Order statuses for filtering
    const orderStatuses = ref([
      { key: 'all', label: 'Tất cả', icon: '📋' },
      { key: 'PENDING', label: 'Chờ xử lý', icon: '⏳' },
      { key: 'PROCESSING', label: 'Đang xử lý', icon: '⚙️' },
      { key: 'SHIPPED', label: 'Đang giao', icon: '🚚' },
      { key: 'DELIVERED', label: 'Đã giao', icon: '✅' },
      { key: 'CANCELLED', label: 'Đã hủy', icon: '❌' },
    ])

    // Order progress steps
    const orderSteps = ref([
      { key: 'PENDING', label: 'Chờ xử lý', icon: '⏳' },
      { key: 'PROCESSING', label: 'Xử lý', icon: '⚙️' },
      { key: 'SHIPPED', label: 'Giao hàng', icon: '🚚' },
      { key: 'DELIVERED', label: 'Hoàn thành', icon: '✅' },
    ])

    // Computed properties
    const orders = computed(() => userStore.orders)
    const loading = computed(() => userStore.loading.orders)
    const error = computed(() => userStore.errors.orders)

    const filteredOrders = computed(() => {
      let filtered = orders.value

      // Filter by status
      if (activeFilter.value !== 'all') {
        filtered = filtered.filter((order) => order.status === activeFilter.value)
      }

      // Filter by search query
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(
          (order) =>
            order.id.toLowerCase().includes(query) ||
            order.items.some((item) => item.productName.toLowerCase().includes(query))
        )
      }

      // Sort orders
      filtered.sort((a, b) => {
        switch (sortBy.value) {
          case 'oldest':
            return new Date(a.createdAt) - new Date(b.createdAt)
          case 'amount-high':
            return b.totalAmount - a.totalAmount
          case 'amount-low':
            return a.totalAmount - b.totalAmount
          case 'newest':
          default:
            return new Date(b.createdAt) - new Date(a.createdAt)
        }
      })

      return filtered
    })

    const paginatedOrders = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      const end = start + itemsPerPage
      return filteredOrders.value.slice(start, end)
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredOrders.value.length / itemsPerPage)
    })

    const totalSpent = computed(() => {
      return orders.value
        .filter((order) => order.status === 'DELIVERED')
        .reduce((sum, order) => sum + order.totalAmount, 0)
    })

    const completedOrders = computed(() => {
      return orders.value.filter((order) => order.status === 'DELIVERED').length
    })

    const averageOrderValue = computed(() => {
      const completedOrdersCount = completedOrders.value
      return completedOrdersCount > 0
        ? formatCurrency(totalSpent.value / completedOrdersCount)
        : formatCurrency(0)
    })

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    }

    const getOrderCount = (status) => {
      if (status === 'all') return orders.value.length
      return orders.value.filter((order) => order.status === status).length
    }

    const getStatusClass = (status) => {
      const statusClasses = {
        PENDING: 'status-pending',
        PROCESSING: 'status-processing',
        SHIPPED: 'status-shipped',
        DELIVERED: 'status-delivered',
        CANCELLED: 'status-cancelled',
      }
      return statusClasses[status] || 'status-default'
    }

    const getStatusText = (status) => {
      return getStatusLabel(status, 'CUSTOMER')
    }

    const truncateAddress = (address) => {
      return address.length > 30 ? address.substring(0, 30) + '...' : address
    }

    const getStepStatus = (orderStatus, stepKey) => {
      const stepOrder = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED']
      const orderIndex = stepOrder.indexOf(orderStatus)
      const stepIndex = stepOrder.indexOf(stepKey)

      if (orderStatus === 'CANCELLED') {
        return stepIndex === 0 ? 'completed' : 'inactive'
      }

      if (stepIndex < orderIndex) return 'completed'
      if (stepIndex === orderIndex) return 'active'
      return 'inactive'
    }

    const getEmptyMessage = () => {
      const messages = {
        all: 'Chưa có đơn hàng nào',
        PENDING: 'Không có đơn hàng chờ xử lý',
        PROCESSING: 'Không có đơn hàng đang xử lý',
        SHIPPED: 'Không có đơn hàng đang giao',
        DELIVERED: 'Không có đơn hàng đã giao',
        CANCELLED: 'Không có đơn hàng đã hủy',
      }
      return messages[activeFilter.value] || 'Không tìm thấy đơn hàng'
    }

    const getEmptyDescription = () => {
      if (searchQuery.value) {
        return `Không tìm thấy đơn hàng với từ khóa "${searchQuery.value}"`
      }

      const descriptions = {
        all: 'Hãy bắt đầu mua sắm để tạo đơn hàng đầu tiên',
        PENDING: 'Tất cả đơn hàng đều đã được xử lý',
        PROCESSING: 'Không có đơn hàng nào đang được xử lý',
        SHIPPED: 'Không có đơn hàng nào đang được giao',
        DELIVERED: 'Chưa có đơn hàng nào hoàn thành',
        CANCELLED: 'Chưa có đơn hàng nào bị hủy',
      }
      return descriptions[activeFilter.value] || ''
    }

    const viewOrderDetail = (orderId) => {
      router.push(`/orders/${orderId}`)
    }

    const cancelOrder = async (orderId) => {
      if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) return
      try {
        await userStore.cancelOrder(orderId)
        alert('Đã hủy đơn hàng thành công')
      } catch (error) {
        alert(error.message || 'Có lỗi xảy ra khi hủy đơn hàng')
      }
    }

    const reorderItems = async (order) => {
      try {
        for (const item of order.items) {
        }
        alert('Đã thêm các sản phẩm vào giỏ hàng')
        router.push('/cart')
      } catch (error) {
        console.error('Error reordering:', error)
        alert('Có lỗi xảy ra khi đặt lại đơn hàng')
      }
    }

    const reviewOrder = (orderId) => {
      router.push(`/orders/${orderId}/review`)
    }

    // Lifecycle
    onMounted(async () => {
      await userStore.loadOrders()
    })

    return {
      orders,
      activeFilter,
      searchQuery,
      sortBy,
      currentPage,
      loading,
      orderStatuses,
      orderSteps,
      filteredOrders,
      paginatedOrders,
      totalPages,
      totalSpent,
      completedOrders,
      averageOrderValue,
      formatCurrency,
      formatDate,
      getOrderCount,
      getStatusClass,
      getStatusText,
      truncateAddress,
      getStepStatus,
      getEmptyMessage,
      getEmptyDescription,
      viewOrderDetail,
      cancelOrder,
      reorderItems,
      reviewOrder,
    }
  },
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

/* Filter Section */
.filter-section {
  margin-bottom: 2rem;
}

.filter-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.7);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 25px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: fit-content;
}

.filter-tab:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: var(--text-accent);
}

.filter-tab.active {
  background: rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent);
  color: var(--text-accent);
}

.tab-icon {
  font-size: 1rem;
}

.tab-label {
  font-weight: 500;
}

.tab-count {
  background: var(--text-accent);
  color: var(--space-black);
  padding: 0.125rem 0.375rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
}

.filter-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  cursor: pointer;
}

/* Orders List */
.orders-section {
  margin-bottom: 3rem;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-card {
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.order-card:hover {
  transform: translateY(-2px);
  border-color: var(--text-accent);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-id {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.id-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.id-value {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-accent);
}

.order-date {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.status-badge {
  padding: 0.375rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-pending {
  background: rgba(255, 205, 60, 0.2);
  color: var(--text-warning);
  border: 1px solid rgba(255, 205, 60, 0.4);
}

.status-processing {
  background: rgba(0, 212, 255, 0.2);
  color: var(--text-accent);
  border: 1px solid rgba(0, 212, 255, 0.4);
}

.status-shipped {
  background: rgba(138, 43, 226, 0.2);
  color: #8a2be2;
  border: 1px solid rgba(138, 43, 226, 0.4);
}

.status-delivered {
  background: rgba(0, 255, 136, 0.2);
  color: var(--text-success);
  border: 1px solid rgba(0, 255, 136, 0.4);
}

.status-cancelled {
  background: rgba(233, 69, 96, 0.2);
  color: var(--text-danger);
  border: 1px solid rgba(233, 69, 96, 0.4);
}

.order-content {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.order-items {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.items-preview {
  display: flex;
  align-items: center;
  gap: -0.5rem;
}

.item-preview {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid rgba(0, 212, 255, 0.3);
  margin-left: -8px;
}

.item-preview:first-child {
  margin-left: 0;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.more-items {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background: rgba(26, 26, 46, 0.8);
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  font-size: 0.8rem;
  color: var(--text-accent);
  font-weight: 600;
  margin-left: -8px;
}

.items-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.item-count {
  font-weight: 600;
  color: var(--text-primary);
}

.item-names {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.3;
}

.order-summary {
  text-align: right;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  min-width: 200px;
}

.summary-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.summary-value {
  font-weight: 500;
  color: var(--text-primary);
}

.total-amount {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-accent);
}

.order-footer {
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding-top: 1rem;
}

.order-progress {
  margin-bottom: 1rem;
}

.progress-steps {
  display: flex;
  align-items: center;
  gap: 1rem;
  overflow-x: auto;
  padding: 0.5rem 0;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  min-width: 70px;
  text-align: center;
  opacity: 0.4;
  transition: opacity 0.3s ease;
}

.progress-step.completed,
.progress-step.active {
  opacity: 1;
}

.progress-step.active .step-icon {
  animation: pulse 2s infinite;
}

.step-icon {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.step-label {
  font-size: 0.7rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.progress-step.completed .step-label,
.progress-step.active .step-label {
  color: var(--text-accent);
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.order-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
  border-radius: 20px;
  white-space: nowrap;
}

.btn-accent {
  background: var(--nebula-gradient);
  border: none;
  color: white;
}

.btn-accent:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(233, 69, 96, 0.4);
}

/* Pagination */
.pagination-section {
  margin-top: 2rem;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.pagination-btn {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(0, 212, 255, 0.1);
  border-color: var(--text-accent);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-info {
  text-align: center;
  color: var(--text-secondary);
}

.results-count {
  display: block;
  font-size: 0.8rem;
  opacity: 0.7;
}

/* Empty State */
.empty-section {
  text-align: center;
  padding: 4rem 0;
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-animation {
  position: relative;
  height: 120px;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-box {
  font-size: 4rem;
  animation: float 3s ease-in-out infinite;
}

.cosmic-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.particle {
  position: absolute;
  font-size: 1.5rem;
  animation: twinkle 2s ease-in-out infinite;
}

.particle:nth-child(1) {
  top: 20%;
  left: 20%;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 30%;
  right: 25%;
  animation-delay: 0.7s;
}

.particle:nth-child(3) {
  bottom: 25%;
  left: 30%;
  animation-delay: 1.4s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes twinkle {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.empty-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 1.5rem;
}

.empty-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.empty-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* Loading State */
.loading-section {
  text-align: center;
  padding: 4rem 0;
}

.loading-spinner {
  font-size: 3rem;
  margin-bottom: 1rem;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Stats Section */
.stats-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  text-align: center;
  padding: 1.5rem;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
  display: block;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Responsive */
@media (max-width: 1024px) {
  .order-content {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .order-summary {
    text-align: left;
  }

  .summary-row {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .orders-page {
    padding: 1rem 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .filter-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

  .order-card {
    padding: 1rem;
  }

  .order-header {
    flex-direction: column;
    gap: 0.75rem;
  }

  .order-items {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .items-preview {
    align-self: flex-start;
  }

  .order-actions {
    justify-content: flex-start;
  }

  .progress-steps {
    justify-content: flex-start;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .filter-tabs {
    flex-direction: column;
    gap: 0.5rem;
  }

  .filter-tab {
    justify-content: center;
  }

  .order-actions {
    flex-direction: column;
  }

  .btn-sm {
    text-align: center;
  }

  .empty-actions {
    flex-direction: column;
    align-items: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
