<template>
  <div class="admin-orders-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">📋 Quản lý Đơn hàng</h1>
        <p class="page-subtitle">Theo dõi và xử lý tất cả đơn hàng trong hệ thống</p>
      </div>
      
      <div class="header-actions">
        <button @click="refreshOrders" class="refresh-btn" :disabled="loading">
          {{ loading ? '⏳ Đang tải...' : '🔄 Làm mới' }}
        </button>
        <button @click="exportOrders" class="export-btn">
          📊 Xuất Excel
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">📦</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.total }}</h3>
            <p class="stat-label">Tổng đơn hàng</p>
          </div>
        </div>
        
        <div class="stat-card pending">
          <div class="stat-icon">⏳</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.pending }}</h3>
            <p class="stat-label">Chờ xử lý</p>
          </div>
        </div>
        
        <div class="stat-card processing">
          <div class="stat-icon">🔄</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.processing }}</h3>
            <p class="stat-label">Đang xử lý</p>
          </div>
        </div>
        
        <div class="stat-card shipped">
          <div class="stat-icon">🚚</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.shipped }}</h3>
            <p class="stat-label">Đang giao</p>
          </div>
        </div>
        
        <div class="stat-card delivered">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.delivered }}</h3>
            <p class="stat-label">Đã giao</p>
          </div>
        </div>
        
        <div class="stat-card cancelled">
          <div class="stat-icon">❌</div>
          <div class="stat-content">
            <h3 class="stat-number">{{ orderStats.cancelled }}</h3>
            <p class="stat-label">Đã hủy</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="filters-section">
      <div class="filters-grid">
        <!-- Status Filter -->
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="filters.status" @change="applyFilters">
            <option value="">Tất cả</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="SHIPPED">Đang giao</option>
            <option value="DELIVERED">Đã giao</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
        
        <!-- Date Range Filter -->
        <div class="filter-group">
          <label>Từ ngày:</label>
          <input 
            v-model="filters.startDate" 
            type="date" 
            @change="applyFilters"
          />
        </div>
        
        <div class="filter-group">
          <label>Đến ngày:</label>
          <input 
            v-model="filters.endDate" 
            type="date" 
            @change="applyFilters"
          />
        </div>
        
        <!-- Search -->
        <div class="filter-group search-group">
          <label>Tìm kiếm:</label>
          <input 
            v-model="filters.search" 
            type="text" 
            placeholder="Mã đơn hàng, tên khách hàng..."
            @input="debounceSearch"
          />
        </div>
        
        <!-- Sort -->
        <div class="filter-group">
          <label>Sắp xếp:</label>
          <select v-model="sortBy" @change="applySort">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="totalAsc">Giá trị thấp → cao</option>
            <option value="totalDesc">Giá trị cao → thấp</option>
          </select>
        </div>
        
        <div class="filter-actions">
          <button @click="clearFilters" class="clear-btn">
            🗑️ Xóa bộ lọc
          </button>
        </div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="orders-section">
      <div class="table-container">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">⏳ Đang tải đơn hàng...</div>
        </div>
        
        <div v-else-if="error" class="error-state">
          <div class="error-content">
            <div class="error-icon">⚠️</div>
            <h3>Có lỗi xảy ra</h3>
            <p>{{ error }}</p>
            <button @click="refreshOrders" class="retry-btn">Thử lại</button>
          </div>
        </div>
        
        <table v-else-if="paginatedOrders.length > 0" class="orders-table">
          <thead>
            <tr>
              <th>Mã đơn hàng</th>
              <th>Khách hàng</th>
              <th>Sản phẩm</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Ngày đặt</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in paginatedOrders" :key="order.id" class="order-row">
              <td>
                <div class="order-id">
                  <span class="order-code">#{{ order.id }}</span>
                  <span v-if="order.isUrgent" class="urgent-badge">🔥 Khẩn cấp</span>
                </div>
              </td>
              
              <td>
                <div class="customer-info">
                  <div class="customer-name">{{ order.customerName }}</div>
                  <div class="customer-email">{{ order.customerEmail }}</div>
                  <div class="customer-phone">{{ order.customerPhone }}</div>
                </div>
              </td>
              
              <td>
                <div class="products-summary">
                  <div class="product-count">{{ order.itemCount }} sản phẩm</div>
                  <div class="product-preview">
                    {{ truncateText(order.productNames.join(', '), 40) }}
                  </div>
                </div>
              </td>
              
              <td>
                <div class="order-total">
                  <span class="total-amount">{{ formatPrice(order.total) }}</span>
                  <span v-if="order.discountAmount > 0" class="discount-info">
                    (Giảm {{ formatPrice(order.discountAmount) }})
                  </span>
                </div>
              </td>
              
              <td>
                <select 
                  v-model="order.status" 
                  @change="updateOrderStatus(order)"
                  class="status-select"
                  :class="order.status.toLowerCase()"
                >
                  <option value="PENDING">Chờ xử lý</option>
                  <option value="PROCESSING">Đang xử lý</option>
                  <option value="SHIPPED">Đang giao</option>
                  <option value="DELIVERED">Đã giao</option>
                  <option value="CANCELLED">Đã hủy</option>
                </select>
              </td>
              
              <td>
                <div class="order-date">
                  <div class="date">{{ formatDate(order.createdAt) }}</div>
                  <div class="time">{{ formatTime(order.createdAt) }}</div>
                </div>
              </td>
              
              <td>
                <div class="action-buttons">
                  <button 
                    @click="viewOrderDetails(order)" 
                    class="action-btn view-btn"
                    title="Xem chi tiết"
                  >
                    👁️
                  </button>
                  
                  <button 
                    @click="printOrder(order)" 
                    class="action-btn print-btn"
                    title="In đơn hàng"
                  >
                    🖨️
                  </button>
                  
                  <button 
                    v-if="order.status === 'PENDING'"
                    @click="cancelOrder(order)" 
                    class="action-btn cancel-btn"
                    title="Hủy đơn hàng"
                  >
                    ❌
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-else class="no-orders">
          <div class="no-orders-content">
            <div class="no-orders-icon">📭</div>
            <h3>Không có đơn hàng nào</h3>
            <p>Thử thay đổi bộ lọc hoặc kiểm tra lại từ khóa tìm kiếm</p>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="pagination-btn"
        >
          ⬅️ Trước
        </button>
        
        <div class="pagination-info">
          Trang {{ currentPage }} / {{ totalPages }} 
          ({{ filteredOrders.length }} đơn hàng)
        </div>
        
        <div class="pagination-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="changePage(page)"
            class="pagination-btn"
            :class="{ active: page === currentPage }"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="pagination-btn"
        >
          Sau ➡️
        </button>
      </div>
    </div>

    <!-- Order Details Modal -->
    <div v-if="showOrderModal" class="modal-overlay" @click="closeOrderModal">
      <div class="modal-content order-modal" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết đơn hàng #{{ selectedOrder?.id }}</h2>
          <button @click="closeOrderModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body" v-if="selectedOrder">
          <div class="order-details-grid">
            <!-- Customer Information -->
            <div class="detail-section">
              <h3>👤 Thông tin khách hàng</h3>
              <div class="detail-content">
                <p><strong>Tên:</strong> {{ selectedOrder.customerName }}</p>
                <p><strong>Email:</strong> {{ selectedOrder.customerEmail }}</p>
                <p><strong>Điện thoại:</strong> {{ selectedOrder.customerPhone }}</p>
              </div>
            </div>
            
            <!-- Shipping Information -->
            <div class="detail-section">
              <h3>🚚 Địa chỉ giao hàng</h3>
              <div class="detail-content">
                <p>{{ selectedOrder.shippingAddress }}</p>
              </div>
            </div>
            
            <!-- Order Items -->
            <div class="detail-section full-width">
              <h3>📦 Sản phẩm đã đặt</h3>
              <div class="order-items">
                <div v-for="item in selectedOrder.items" :key="item.id" class="order-item">
                  <img :src="item.productImage" :alt="item.productName" class="item-image" />
                  <div class="item-info">
                    <h4>{{ item.productName }}</h4>
                    <p>Số lượng: {{ item.quantity }}</p>
                    <p>Giá: {{ formatPrice(item.price) }}</p>
                  </div>
                  <div class="item-total">
                    {{ formatPrice(item.quantity * item.price) }}
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Order Summary -->
            <div class="detail-section">
              <h3>💰 Tổng kết đơn hàng</h3>
              <div class="order-summary">
                <div class="summary-line">
                  <span>Tạm tính:</span>
                  <span>{{ formatPrice(selectedOrder.subtotal) }}</span>
                </div>
                <div class="summary-line">
                  <span>Phí vận chuyển:</span>
                  <span>{{ formatPrice(selectedOrder.shippingFee) }}</span>
                </div>
                <div v-if="selectedOrder.discountAmount > 0" class="summary-line discount">
                  <span>Giảm giá:</span>
                  <span>-{{ formatPrice(selectedOrder.discountAmount) }}</span>
                </div>
                <div class="summary-line total">
                  <span>Tổng cộng:</span>
                  <span>{{ formatPrice(selectedOrder.total) }}</span>
                </div>
              </div>
            </div>
            
            <!-- Order Timeline -->
            <div class="detail-section">
              <h3>📅 Lịch sử đơn hàng</h3>
              <div class="order-timeline">
                <div v-for="event in selectedOrder.timeline" :key="event.id" class="timeline-item">
                  <div class="timeline-icon">{{ event.icon }}</div>
                  <div class="timeline-content">
                    <h4>{{ event.title }}</h4>
                    <p>{{ event.description }}</p>
                    <span class="timeline-date">{{ formatDateTime(event.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="printOrder(selectedOrder)" class="btn-secondary">
            🖨️ In đơn hàng
          </button>
          <button @click="closeOrderModal" class="btn-primary">
            Đóng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminAPI } from '@/services/api'

export default {
  name: 'AdminOrders',
  setup() {
    const router = useRouter()
    
    // Reactive data
    const orders = ref([])
    const loading = ref(false)
    const error = ref(null)
    const selectedOrder = ref(null)
    const showOrderModal = ref(false)
    
    // Stats
    const orderStats = ref({
      total: 0,
      pending: 0,
      processing: 0,
      shipped: 0,
      delivered: 0,
      cancelled: 0
    })
    
    // Filters
    const filters = ref({
      status: '',
      startDate: '',
      endDate: '',
      search: ''
    })
    
    const sortBy = ref('newest')
    
    // Pagination
    const currentPage = ref(1)
    const itemsPerPage = 10
    
    // Debounce timer for search
    let searchTimeout = null
    
    // Computed properties
    const filteredOrders = computed(() => {
      let filtered = [...orders.value]
      
      // Status filter
      if (filters.value.status) {
        filtered = filtered.filter(order => order.status === filters.value.status)
      }
      
      // Date filter
      if (filters.value.startDate) {
        filtered = filtered.filter(order => 
          new Date(order.createdAt) >= new Date(filters.value.startDate)
        )
      }
      
      if (filters.value.endDate) {
        filtered = filtered.filter(order => 
          new Date(order.createdAt) <= new Date(filters.value.endDate)
        )
      }
      
      // Search filter
      if (filters.value.search) {
        const searchTerm = filters.value.search.toLowerCase()
        filtered = filtered.filter(order => 
          order.id.toString().includes(searchTerm) ||
          order.customerName.toLowerCase().includes(searchTerm) ||
          order.customerEmail.toLowerCase().includes(searchTerm) ||
          order.customerPhone.includes(searchTerm)
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
          return sorted.sort((a, b) => a.total - b.total)
        case 'totalDesc':
          return sorted.sort((a, b) => b.total - a.total)
        case 'newest':
        default:
          return sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      }
    })
    
    const paginatedOrders = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage
      return sortedOrders.value.slice(start, start + itemsPerPage)
    })
    
    const totalPages = computed(() => 
      Math.ceil(sortedOrders.value.length / itemsPerPage)
    )
    
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
        cancelled: 0
      }
      
      orders.value.forEach(order => {
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
    
    const viewOrderDetails = (order) => {
      selectedOrder.value = {
        ...order,
        timeline: [
          {
            id: 1,
            icon: '📝',
            title: 'Đơn hàng được tạo',
            description: 'Khách hàng đã đặt hàng thành công',
            createdAt: order.createdAt
          },
          {
            id: 2,
            icon: '💳',
            title: 'Thanh toán thành công',
            description: 'Đã nhận được thanh toán từ khách hàng',
            createdAt: order.createdAt
          }
        ]
      }
      showOrderModal.value = true
    }
    
    const closeOrderModal = () => {
      showOrderModal.value = false
      selectedOrder.value = null
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
        search: ''
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
        currency: 'VND'
      }).format(price)
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('vi-VN')
    }
    
    const formatTime = (date) => {
      return new Date(date).toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }
    
    const formatDateTime = (date) => {
      return new Date(date).toLocaleString('vi-VN')
    }
    
    const truncateText = (text, limit) => {
      if (!text) return ''
      return text.length > limit ? text.substring(0, limit) + '...' : text
    }
    
    // Mock data for development
    const loadMockData = () => {
      orders.value = [
        {
          id: 12345,
          customerName: 'Nguyễn Văn A',
          customerEmail: 'customer@example.com',
          customerPhone: '0123456789',
          itemCount: 3,
          productNames: ['iPhone 15 Pro', 'AirPods Pro', 'MacBook Air'],
          total: 75000000,
          discountAmount: 2000000,
          status: 'PENDING',
          createdAt: '2024-12-22T10:30:00',
          isUrgent: true,
          shippingAddress: '123 Đường ABC, Quận 1, TP.HCM',
          subtotal: 72000000,
          shippingFee: 50000,
          items: [
            {
              id: 1,
              productName: 'iPhone 15 Pro',
              productImage: '/placeholder-product.jpg',
              quantity: 1,
              price: 30000000
            }
          ]
        }
        // Add more mock orders...
      ]
      calculateStats()
    }
    
    // Lifecycle
    onMounted(() => {
      // Load real data or mock data for development
      loadMockData() // Remove this and uncomment loadOrders() for production
      // loadOrders()
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
      selectedOrder,
      showOrderModal,
      loadOrders,
      updateOrderStatus,
      viewOrderDetails,
      closeOrderModal,
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
      truncateText
    }
  }
}
</script>

<style scoped>
.admin-orders-page {
  padding: 24px;
  background: var(--bg-primary);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn,
.export-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: var(--accent-blue);
  color: white;
}

.export-btn {
  background: var(--accent-green);
  color: white;
}

.refresh-btn:hover,
.export-btn:hover {
  transform: translateY(-2px);
}

.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.1);
}

.stat-icon {
  font-size: 2rem;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.stat-card.total .stat-number { color: var(--accent-blue); }
.stat-card.pending .stat-number { color: var(--accent-orange); }
.stat-card.processing .stat-number { color: var(--accent-blue); }
.stat-card.shipped .stat-number { color: var(--accent-purple); }
.stat-card.delivered .stat-number { color: var(--accent-green); }
.stat-card.cancelled .stat-number { color: var(--accent-red); }

.filters-section {
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.filter-group input,
.filter-group select {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.search-group {
  grid-column: span 2;
}

.clear-btn {
  padding: 10px 16px;
  background: var(--accent-red);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  height: fit-content;
}

.orders-section {
  background: var(--bg-secondary);
  border-radius: 12px;
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th {
  background: var(--bg-primary);
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.order-row {
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s ease;
}

.order-row:hover {
  background: var(--bg-primary);
}

.orders-table td {
  padding: 16px;
  vertical-align: top;
}

.order-id {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-code {
  font-weight: 600;
  color: var(--accent-blue);
}

.urgent-badge {
  font-size: 0.8rem;
  color: var(--accent-red);
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-name {
  font-weight: 500;
  color: var(--text-primary);
}

.customer-email,
.customer-phone {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.products-summary {
  max-width: 200px;
}

.product-count {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-preview {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.order-total {
  text-align: right;
}

.total-amount {
  font-weight: 600;
  color: var(--text-primary);
}

.discount-info {
  display: block;
  font-size: 0.8rem;
  color: var(--accent-green);
  margin-top: 4px;
}

.status-select {
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.8rem;
  cursor: pointer;
}

.status-select.pending { border-color: var(--accent-orange); }
.status-select.processing { border-color: var(--accent-blue); }
.status-select.shipped { border-color: var(--accent-purple); }
.status-select.delivered { border-color: var(--accent-green); }
.status-select.cancelled { border-color: var(--accent-red); }

.order-date {
  text-align: center;
}

.date {
  font-weight: 500;
  margin-bottom: 2px;
}

.time {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.view-btn {
  background: var(--accent-blue);
  color: white;
}

.print-btn {
  background: var(--accent-green);
  color: white;
}

.cancel-btn {
  background: var(--accent-red);
  color: white;
}

.action-btn:hover {
  transform: scale(1.05);
}

.loading-state,
.error-state,
.no-orders {
  padding: 60px 0;
  text-align: center;
}

.error-content h3,
.no-orders-content h3 {
  margin-bottom: 8px;
  color: var(--text-primary);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: var(--bg-primary);
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: var(--accent-blue);
  color: white;
}

.pagination-btn.active {
  background: var(--accent-blue);
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-secondary);
  border-radius: 12px;
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
}

.modal-body {
  padding: 24px;
}

.order-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.detail-section {
  background: var(--bg-primary);
  padding: 20px;
  border-radius: 8px;
}

.detail-section.full-width {
  grid-column: 1 / -1;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin-bottom: 4px;
  color: var(--text-primary);
}

.item-info p {
  margin: 2px 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.item-total {
  font-weight: 600;
  color: var(--accent-blue);
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-line.discount {
  color: var(--accent-green);
}

.summary-line.total {
  font-weight: 600;
  font-size: 1.1rem;
  padding-top: 8px;
  border-top: 1px solid var(--border-color);
  color: var(--accent-blue);
}

.order-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 12px;
}

.timeline-icon {
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
}

.timeline-content h4 {
  margin-bottom: 4px;
  color: var(--text-primary);
}

.timeline-content p {
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.timeline-date {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid var(--border-color);
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--accent-blue);
  color: white;
}

.btn-secondary {
  background: var(--bg-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .admin-orders-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .filters-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .search-group {
    grid-column: span 1;
  }
  
  .orders-table {
    font-size: 0.8rem;
  }
  
  .modal-content {
    margin: 10px;
  }
  
  .order-details-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-section.full-width {
    grid-column: 1;
  }
}
</style>