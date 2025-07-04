<template>
  <div class="admin-orders">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <h1>📋 Quản lý đơn hàng</h1>
          <p>Quản lý tất cả đơn hàng trong hệ thống</p>
        </div>
      </div>

      <!-- Filter và Search (gọn) -->
      <div class="filter-section">
        <div class="search-box">
          <input
            v-model="filters.search"
            type="text"
            placeholder="Tìm theo mã đơn, tên khách, email..."
            class="search-input"
            @input="debounceSearch"
          />
        </div>
        <button class="btn-advanced-filter" @click="showFilterModal = true">
          <span class="filter-icon">🔍</span> Bộ lọc nâng cao
        </button>
      </div>

      <!-- Popup Modal Advanced Filter -->
      <div v-if="showFilterModal" class="modal-overlay" @click="closeFilterModal">
        <div class="modal-content filter-modal" @click.stop>
          <div class="modal-header">
            <h2>Bộ lọc nâng cao</h2>
            <button @click="closeFilterModal" class="close-btn">✕</button>
          </div>
          <div class="modal-body">
            <div class="filter-grid">
              <div class="form-group">
                <label>Trạng thái</label>
                <select v-model="tempFilters.status" class="filter-select">
                  <option value="">Tất cả trạng thái</option>
                  <option value="PENDING">Chờ xử lý</option>
                  <option value="PROCESSING">Đang xử lý</option>
                  <option value="SHIPPED">Đang giao</option>
                  <option value="DELIVERED">Đã giao</option>
                  <option value="CANCELLED">Đã hủy</option>
                </select>
              </div>
              <div class="form-group">
                <label>Từ ngày</label>
                <input type="date" v-model="tempFilters.startDate" class="filter-select" />
              </div>
              <div class="form-group">
                <label>Đến ngày</label>
                <input type="date" v-model="tempFilters.endDate" class="filter-select" />
              </div>
              <div class="form-group">
                <label>Sắp xếp</label>
                <select v-model="tempSortBy" class="filter-select">
                  <option value="newest">Mới nhất</option>
                  <option value="oldest">Cũ nhất</option>
                  <option value="totalAsc">Giá trị thấp → cao</option>
                  <option value="totalDesc">Giá trị cao → thấp</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="clearTempFilters" class="btn-clear">Xóa bộ lọc</button>
            <button @click="applyTempFilters" class="btn-primary">Áp dụng</button>
            <button @click="closeFilterModal" class="btn-secondary">Đóng</button>
          </div>
        </div>
      </div>

      <!-- Statistics -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">📋</div>
          <div class="stat-info">
            <h3>{{ orderStats.total }}</h3>
            <p>Tổng đơn hàng</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⏳</div>
          <div class="stat-info">
            <h3>{{ orderStats.pending }}</h3>
            <p>Chờ xử lý</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🔄</div>
          <div class="stat-info">
            <h3>{{ orderStats.processing }}</h3>
            <p>Đang xử lý</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🚚</div>
          <div class="stat-info">
            <h3>{{ orderStats.shipped }}</h3>
            <p>Đang giao</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <h3>{{ orderStats.delivered }}</h3>
            <p>Đã giao</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">❌</div>
          <div class="stat-info">
            <h3>{{ orderStats.cancelled }}</h3>
            <p>Đã hủy</p>
          </div>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="orders-table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải đơn hàng...</p>
        </div>
        <div v-else-if="paginatedOrders.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <h3>Không có đơn hàng nào</h3>
          <p>
            {{
              filters.search || filters.status || filters.startDate || filters.endDate
                ? 'Không tìm thấy đơn hàng nào với bộ lọc hiện tại. Hãy thử thay đổi điều kiện tìm kiếm.'
                : 'Chưa có đơn hàng nào trong hệ thống.'
            }}
          </p>
          <button
            v-if="filters.search || filters.status || filters.startDate || filters.endDate"
            @click="clearFilters"
            class="btn-add-first"
          >
            🔄 Xóa bộ lọc
          </button>
        </div>
        <div v-else class="orders-table">
          <table>
            <thead>
              <tr>
                <th style="width: 120px">🆔 Mã đơn</th>
                <th style="width: 220px">👤 Khách hàng</th>
                <th style="width: 220px">📦 Sản phẩm</th>
                <th style="width: 120px">💰 Tổng tiền</th>
                <th style="width: 120px">🎯 Trạng thái</th>
                <th style="width: 120px">📅 Ngày đặt</th>
                <th style="width: 140px">⚡ Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in paginatedOrders" :key="order.id" class="order-row">
                <td class="order-id">#{{ order.id.slice(-8) }}</td>
                <td class="customer-info">
                  <div class="customer-main">
                    {{ order.customerName || order.userEmail || 'Không rõ' }}
                  </div>
                  <div class="customer-sub" v-if="order.customerEmail">
                    📧 {{ order.customerEmail }}
                  </div>
                  <div class="customer-sub" v-if="order.customerPhone">
                    📞 {{ order.customerPhone }}
                  </div>
                </td>
                <td class="product-list">
                  <span v-for="(item, idx) in order.items.slice(0, 2)" :key="item.productId">
                    {{ item.productName
                    }}<span v-if="idx < order.items.length - 1 && idx < 1">, </span>
                  </span>
                  <span v-if="order.items.length > 2">... ({{ order.items.length }})</span>
                </td>
                <td>
                  <span class="order-total">{{ formatPrice(order.totalAmount) }}</span>
                </td>
                <td>
                  <span v-if="order.status === 'CANCELLED'" class="status-badge cancelled">
                    {{ getStatusLabel(order.status) }}
                  </span>
                  <select
                    v-else
                    v-model="order.status"
                    @change="updateOrderStatus(order)"
                    class="status-select"
                  >
                    <option value="PENDING">Chờ xử lý</option>
                    <option value="PROCESSING">Đang xử lý</option>
                    <option value="SHIPPED">Đang giao</option>
                    <option value="DELIVERED">Đã giao</option>
                    <option value="CANCELLED">Đã hủy</option>
                  </select>
                </td>
                <td>
                  <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button
                      @click="goToOrderDetail(order)"
                      class="btn btn-view"
                      title="Xem chi tiết"
                    >
                      <span class="action-icon">👁️</span>
                    </button>
                    <!-- <button @click="printOrder(order)" class="btn btn-print" title="In đơn hàng">
                      <span class="action-icon">🖨️</span>
                    </button> -->
                    <button
                      v-if="order.status === 'PENDING'"
                      @click="cancelOrder(order)"
                      class="btn btn-cancel"
                      title="Hủy đơn hàng"
                    >
                      <span class="action-icon">❌</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">
          « Trước
        </button>
        <span class="page-info"> Trang {{ currentPage }} / {{ totalPages }} </span>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          Sau »
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminAPI } from '@/services/api'
import { getStatusLabel as getStatusLabelConst, getStatusColor } from '@/utils/constants'

export default {
  name: 'AdminOrders',
  setup() {
    const router = useRouter()

    // Reactive data
    const orders = ref([])
    const loading = ref(false)
    const error = ref(null)

    // Stats
    const orderStats = ref({
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0,
    })

    // Filters
    const filters = ref({
      status: '',
      startDate: '',
      endDate: '',
      search: '',
    })

    const sortBy = ref('newest')

    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = 10

    // Debounce timer for search
    let searchTimeout = null

    // Popup filter modal state
    const showFilterModal = ref(false)
    const tempFilters = ref({ ...filters.value })
    const tempSortBy = ref(sortBy.value)

    // Computed properties
    const filteredOrders = computed(() => {
      let filtered = [...orders.value]

      // Status filter
      if (filters.value.status) {
        filtered = filtered.filter((order) => order.status === filters.value.status)
      }

      // Date filter
      if (filters.value.startDate) {
        filtered = filtered.filter(
          (order) => new Date(order.createdAt) >= new Date(filters.value.startDate)
        )
      }

      if (filters.value.endDate) {
        filtered = filtered.filter(
          (order) => new Date(order.createdAt) <= new Date(filters.value.endDate)
        )
      }

      // Search filter
      if (filters.value.search) {
        const searchTerm = filters.value.search.toLowerCase()
        filtered = filtered.filter(
          (order) =>
            order.id.toString().toLowerCase().includes(searchTerm) ||
            (order.customerName && order.customerName.toLowerCase().includes(searchTerm)) ||
            (order.userEmail && order.userEmail.toLowerCase().includes(searchTerm)) ||
            (order.customerPhone && order.customerPhone.toLowerCase().includes(searchTerm))
        )
      }

      return filtered
    })

    const sortedOrders = computed(() => {
      const sorted = [...filteredOrders.value]

      switch (sortBy.value) {
        case 'oldest':
          return sorted.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
        case 'totalAsc':
          return sorted.sort((a, b) => (a.totalAmount || 0) - (b.totalAmount || 0))
        case 'totalDesc':
          return sorted.sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0))
        case 'newest':
        default:
          return sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }
    })

    const paginatedOrders = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      return sortedOrders.value.slice(start, start + itemsPerPage)
    })

    const totalPages = computed(() => Math.ceil(sortedOrders.value.length / itemsPerPage))

    const visiblePages = computed(() => {
      const total = totalPages.value
      const current = currentPage.value
      const delta = 2

      let start = Math.max(1, current - delta)
      let end = Math.min(total, current + delta)

      if (end - start < delta * 2) {
        if (start === 1) {
          end = Math.min(total, start + delta * 2)
        } else {
          start = Math.max(1, end - delta * 2)
        }
      }

      return Array.from({ length: end - start + 1 }, (_, i) => start + i)
    })

    // Methods
    const loadOrders = async () => {
      loading.value = true
      error.value = null

      try {
        const response = await adminAPI.getOrders()
        orders.value = response.data.orders || response.data
        calculateStats()
      } catch (err) {
        error.value = 'Không thể tải danh sách đơn hàng'
        console.error('Error loading orders:', err)
      } finally {
        loading.value = false
      }
    }

    const calculateStats = () => {
      const stats = {
        total: orders.value.length,
        pending: 0,
        processing: 0,
        shipped: 0,
        delivered: 0,
        cancelled: 0,
      }

      orders.value.forEach((order) => {
        const status = order.status.toLowerCase()
        if (stats.hasOwnProperty(status)) {
          stats[status]++
        }
      })

      orderStats.value = stats
    }

    const updateOrderStatus = async (order) => {
      try {
        await adminAPI.updateOrderStatus(order.id, order.status)
        calculateStats()
        // Show success message
      } catch (err) {
        console.error('Error updating order status:', err)
        // Revert status change
        await loadOrders()
      }
    }

    const cancelOrder = async (order) => {
      if (confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${order.id}?`)) {
        order.status = 'CANCELLED'
        await updateOrderStatus(order)
      }
    }

    const printOrder = (order) => {
      // Print order logic
      window.print()
    }

    const exportOrders = () => {
      // Export orders to Excel logic
      console.log('Exporting orders...')
    }

    const refreshOrders = () => {
      loadOrders()
    }

    const applyFilters = () => {
      currentPage.value = 1
    }

    const applySort = () => {
      currentPage.value = 1
    }

    const clearFilters = () => {
      filters.value = {
        status: '',
        startDate: '',
        endDate: '',
        search: '',
      }
      sortBy.value = 'newest'
      currentPage.value = 1
    }

    const debounceSearch = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        applyFilters()
      }, 500)
    }

    const changePage = (page) => {
      currentPage.value = page
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(price)
    }

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }

    const formatTime = (date) => {
      return new Date(date).toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      })
    }

    const formatDateTime = (date) => {
      return new Date(date).toLocaleString('vi-VN')
    }

    const truncateText = (text, limit) => {
      if (!text) return ''
      return text.length > limit ? text.substring(0, limit) + '...' : text
    }

    const getStatusLabel = (status) => {
      return getStatusLabelConst(status, 'ADMIN')
    }

    const goToOrderDetail = (order) => {
      router.push(`/orders/${order.id}`)
    }

    // When opening modal, copy current filters
    const openFilterModal = () => {
      tempFilters.value = { ...filters.value }
      tempSortBy.value = sortBy.value
      showFilterModal.value = true
    }
    const closeFilterModal = () => {
      showFilterModal.value = false
    }
    // Clear temp filters in modal
    const clearTempFilters = () => {
      tempFilters.value = {
        status: '',
        startDate: '',
        endDate: '',
        search: '',
      }
      tempSortBy.value = 'newest'
    }
    // Apply temp filters to real filters
    const applyTempFilters = () => {
      filters.value = { ...tempFilters.value }
      sortBy.value = tempSortBy.value
      currentPage.value = 1
      showFilterModal.value = false
    }

    // Lifecycle
    onMounted(() => {
      // Load real data
      loadOrders()
    })

    return {
      orders,
      loading,
      error,
      orderStats,
      filters,
      sortBy,
      currentPage,
      filteredOrders,
      paginatedOrders,
      totalPages,
      visiblePages,
      loadOrders,
      updateOrderStatus,
      cancelOrder,
      printOrder,
      exportOrders,
      refreshOrders,
      applyFilters,
      applySort,
      clearFilters,
      debounceSearch,
      changePage,
      formatPrice,
      formatDate,
      formatTime,
      formatDateTime,
      truncateText,
      getStatusLabel,
      goToOrderDetail,
      showFilterModal,
      tempFilters,
      tempSortBy,
      openFilterModal,
      closeFilterModal,
      clearTempFilters,
      applyTempFilters,
    }
  },
}
</script>

<style scoped>
/* Tận dụng lại style từ admin product, chỉ đổi tên class cho phù hợp nếu cần */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 0;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}
.header-info h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #2563eb;
  margin-bottom: 8px;
}
.header-info p {
  color: #666;
  font-size: 1.1rem;
}
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 24px;
}
.search-box {
  flex: 1;
}
.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e7ef;
  border-radius: 8px;
  font-size: 1rem;
  background: #f6f8fa;
  color: #222;
}
.filters {
  display: flex;
  gap: 12px;
}
.filter-select {
  padding: 10px 16px;
  border: 1px solid #e0e7ef;
  border-radius: 8px;
  background: #fff;
  color: #222;
  font-size: 1rem;
}
.btn-clear {
  padding: 10px 18px;
  background: #f3f4f6;
  border: 1px solid #e0e7ef;
  border-radius: 8px;
  color: #2563eb;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-clear:hover {
  background: #e0e7ef;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px 18px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  font-size: 2rem;
  color: #2563eb;
}
.stat-info h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 2px;
}
.stat-info p {
  color: #666;
  font-size: 0.95rem;
}
.orders-table-container {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.07);
  padding: 24px;
  margin-bottom: 32px;
  overflow-x: auto;
}
.orders-table table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 1rem;
}
.orders-table th {
  background: #2563eb;
  color: #fff;
  font-weight: 700;
  padding: 14px 12px;
  text-align: left;
}
.orders-table td {
  padding: 12px 10px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}
.order-id {
  font-weight: 600;
  color: #2563eb;
}
.customer-info {
  min-width: 160px;
}
.customer-main {
  font-weight: 500;
  color: #222;
}
.customer-sub {
  font-size: 0.92em;
  color: #666;
}
.product-list {
  min-width: 120px;
  color: #333;
}
.order-total {
  font-weight: 700;
  color: #059669;
  font-size: 1.05em;
}
.status-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 16px;
  font-size: 0.98em;
  font-weight: 600;
  color: #fff;
  background: #64748b;
  min-width: 90px;
  text-align: center;
}
.status-badge.pending {
  background: #f59e42;
}
.status-badge.processing {
  background: #2563eb;
}
.status-badge.shipped {
  background: #0ea5e9;
}
.status-badge.delivered {
  background: #059669;
}
.status-badge.cancelled {
  background: #ef4444;
}
.order-date {
  color: #222;
  font-size: 0.98em;
}
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.btn {
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  padding: 7px 10px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 1.1em;
  position: relative;
}
.btn:hover {
  background: #dbeafe;
}
.btn-cancel:hover {
  background: #fee2e2;
}
.action-icon {
  vertical-align: middle;
}
.loading {
  text-align: center;
  padding: 40px 0;
}
.spinner {
  border: 4px solid #e0e7ef;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px auto;
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
  color: #888;
  padding: 32px 0 16px 0;
  font-size: 1.1em;
}
.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 8px;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
}
.page-btn {
  padding: 10px 16px;
  border: 1px solid #e0e7ef;
  background: #f6f8fa;
  color: #2563eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.page-info {
  color: #666;
  font-size: 1rem;
  margin: 0 16px;
}
.date-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.date-label {
  font-size: 0.85em;
  color: #2563eb;
  margin-bottom: 2px;
  font-weight: 500;
}
.status-select {
  padding: 6px 10px;
  border: 1px solid #e0e7ef;
  border-radius: 8px;
  background: #fff;
  color: #2563eb;
  font-size: 1em;
  font-weight: 600;
  min-width: 120px;
  outline: none;
  transition: border 0.2s;
}
.status-select:focus {
  border: 1.5px solid #2563eb;
}
.btn-advanced-filter {
  padding: 12px 20px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}
.btn-advanced-filter:hover {
  background: #1d4ed8;
}
.filter-icon {
  font-size: 1.2em;
}
/* Modal filter */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content.filter-modal {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 32px rgba(0, 0, 0, 0.18);
  padding: 32px 28px 20px 28px;
  min-width: 340px;
  max-width: 95vw;
  width: 420px;
  position: relative;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.modal-header h2 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #2563eb;
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
  color: #888;
}
.filter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 16px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.form-group label {
  font-size: 0.98em;
  color: #222;
  font-weight: 500;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 600;
  cursor: pointer;
  font-size: 1rem;
}
.btn-primary:hover {
  background: #1d4ed8;
}
.btn-secondary {
  background: #f3f4f6;
  color: #222;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 500;
  cursor: pointer;
  font-size: 1rem;
}
.btn-clear {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-weight: 500;
  cursor: pointer;
  font-size: 1rem;
}
.btn-clear:hover {
  background: #dc2626;
}
@media (max-width: 600px) {
  .modal-content.filter-modal {
    width: 98vw;
    min-width: 0;
    padding: 18px 6vw 12px 6vw;
  }
  .filter-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
