<template>
  <div class="admin-orders">
    <div class="container">
      <div class="page-header">
        <h1>📋 Quản lý đơn hàng</h1>
        <p>Quản lý tất cả đơn hàng trong hệ thống</p>
      </div>

      <!-- Filter và Search -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm theo ID đơn hàng, email khách hàng..."
            class="search-input"
          >
          <button @click="searchOrders" class="search-btn">🔍</button>
        </div>
        
        <div class="filters">
          <select v-model="statusFilter" @change="filterOrders" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="SHIPPED">Đã gửi</option>
            <option value="DELIVERED">Đã giao</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>

          <select v-model="dateFilter" @change="filterOrders" class="filter-select">
            <option value="">Tất cả thời gian</option>
            <option value="today">Hôm nay</option>
            <option value="week">Tuần này</option>
            <option value="month">Tháng này</option>
          </select>

          <button @click="exportOrders" class="export-btn">📊 Xuất Excel</button>
        </div>
      </div>

      <!-- Statistics -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <h3>{{ stats.total }}</h3>
            <p>Tổng đơn hàng</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">⏳</div>
          <div class="stat-info">
            <h3>{{ stats.pending }}</h3>
            <p>Chờ xử lý</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🚚</div>
          <div class="stat-info">
            <h3>{{ stats.processing }}</h3>
            <p>Đang xử lý</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-info">
            <h3>{{ formatCurrency(stats.revenue) }}</h3>
            <p>Doanh thu</p>
          </div>
        </div>
      </div>

      <!-- Orders Table -->
      <div class="orders-table-container">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải đơn hàng...</p>
        </div>

        <div v-else-if="orders.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <h3>Không có đơn hàng nào</h3>
          <p>Chưa có đơn hàng nào phù hợp với bộ lọc hiện tại</p>
        </div>

        <div v-else class="orders-table">
          <table>
            <thead>
              <tr>
                <th>ID Đơn hàng</th>
                <th>Khách hàng</th>
                <th>Sản phẩm</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in paginatedOrders" :key="order.id" class="order-row">
                <td>
                  <div class="order-id">
                    <strong>#{{ order.id.substring(0, 8) }}</strong>
                    <button @click="copyOrderId(order.id)" class="copy-btn">📋</button>
                  </div>
                </td>
                
                <td>
                  <div class="customer-info">
                    <strong>{{ order.customerName }}</strong>
                    <small>{{ order.customerEmail }}</small>
                  </div>
                </td>
                
                <td>
                  <div class="order-items">
                    <div v-for="item in order.items.slice(0, 2)" :key="item.id" class="item-preview">
                      <img :src="item.image || '/placeholder.jpg'" :alt="item.name" class="item-image">
                      <span>{{ item.name }} x{{ item.quantity }}</span>
                    </div>
                    <span v-if="order.items.length > 2" class="more-items">
                      +{{ order.items.length - 2 }} sản phẩm khác
                    </span>
                  </div>
                </td>
                
                <td>
                  <div class="order-total">
                    <strong>{{ formatCurrency(order.totalAmount) }}</strong>
                  </div>
                </td>
                
                <td>
                  <select 
                    v-model="order.status" 
                    @change="updateOrderStatus(order.id, order.status)"
                    class="status-select"
                    :class="getStatusClass(order.status)"
                  >
                    <option value="PENDING">Chờ xử lý</option>
                    <option value="PROCESSING">Đang xử lý</option>
                    <option value="SHIPPED">Đã gửi</option>
                    <option value="DELIVERED">Đã giao</option>
                    <option value="CANCELLED">Đã hủy</option>
                  </select>
                </td>
                
                <td>
                  <div class="order-date">
                    <div>{{ formatDate(order.createdAt) }}</div>
                    <small>{{ formatTime(order.createdAt) }}</small>
                  </div>
                </td>
                
                <td>
                  <div class="action-buttons">
                    <button @click="viewOrderDetail(order.id)" class="btn-view" title="Xem chi tiết">👁️</button>
                    <button @click="printOrder(order.id)" class="btn-print" title="In đơn hàng">🖨️</button>
                    <button @click="deleteOrder(order.id)" class="btn-delete" title="Xóa đơn hàng">🗑️</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination">
          <button 
            @click="currentPage = 1" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ⏪
          </button>
          <button 
            @click="currentPage--" 
            :disabled="currentPage === 1"
            class="page-btn"
          >
            ◀️
          </button>
          
          <span class="page-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            @click="currentPage++" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ▶️
          </button>
          <button 
            @click="currentPage = totalPages" 
            :disabled="currentPage === totalPages"
            class="page-btn"
          >
            ⏩
          </button>
        </div>
      </div>
    </div>

    <!-- Order Detail Modal -->
    <div v-if="selectedOrder" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Chi tiết đơn hàng #{{ selectedOrder.id.substring(0, 8) }}</h2>
          <button @click="closeModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="order-info-grid">
            <div class="info-section">
              <h3>Thông tin khách hàng</h3>
              <p><strong>Tên:</strong> {{ selectedOrder.customerName }}</p>
              <p><strong>Email:</strong> {{ selectedOrder.customerEmail }}</p>
              <p><strong>Địa chỉ giao hàng:</strong> {{ selectedOrder.shippingAddress }}</p>
              <p><strong>Địa chỉ thanh toán:</strong> {{ selectedOrder.billingAddress }}</p>
            </div>
            
            <div class="info-section">
              <h3>Thông tin đơn hàng</h3>
              <p><strong>Trạng thái:</strong> 
                <span :class="getStatusClass(selectedOrder.status)">
                  {{ getStatusText(selectedOrder.status) }}
                </span>
              </p>
              <p><strong>Ngày tạo:</strong> {{ formatDateTime(selectedOrder.createdAt) }}</p>
              <p><strong>Tổng tiền:</strong> {{ formatCurrency(selectedOrder.totalAmount) }}</p>
            </div>
          </div>
          
          <div class="order-items-detail">
            <h3>Sản phẩm</h3>
            <div class="items-list">
              <div v-for="item in selectedOrder.items" :key="item.id" class="item-detail">
                <img :src="item.image || '/placeholder.jpg'" :alt="item.name" class="item-image-large">
                <div class="item-info">
                  <h4>{{ item.name }}</h4>
                  <p>Số lượng: {{ item.quantity }}</p>
                  <p>Giá: {{ formatCurrency(item.price) }}</p>
                  <p>Thành tiền: {{ formatCurrency(item.price * item.quantity) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminOrders',
  data() {
    return {
      orders: [],
      filteredOrders: [],
      loading: true,
      searchQuery: '',
      statusFilter: '',
      dateFilter: '',
      currentPage: 1,
      itemsPerPage: 10,
      selectedOrder: null,
      stats: {
        total: 0,
        pending: 0,
        processing: 0,
        revenue: 0
      }
    }
  },
  computed: {
    paginatedOrders() {
      const start = (this.currentPage - 1) * this.itemsPerPage
      const end = start + this.itemsPerPage
      return this.filteredOrders.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.filteredOrders.length / this.itemsPerPage)
    }
  },
  async mounted() {
    await this.loadOrders()
    this.calculateStats()
  },
  methods: {
    async loadOrders() {
      try {
        this.loading = true
        const response = await axios.get('/api/admin/orders', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.orders = response.data.map(order => ({
          ...order,
          customerName: order.customerName || 'N/A',
          customerEmail: order.customerEmail || order.userEmail
        }))
        
        this.filteredOrders = [...this.orders]
      } catch (error) {
        console.error('Lỗi khi tải đơn hàng:', error)
        this.$toast.error('Không thể tải danh sách đơn hàng')
      } finally {
        this.loading = false
      }
    },

    async updateOrderStatus(orderId, newStatus) {
      try {
        await axios.put(`/api/orders/${orderId}/status`, 
          { status: newStatus },
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        )
        
        this.$toast.success('Cập nhật trạng thái đơn hàng thành công')
        this.calculateStats()
      } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái:', error)
        this.$toast.error('Không thể cập nhật trạng thái đơn hàng')
      }
    },

    filterOrders() {
      let filtered = [...this.orders]

      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(order => 
          order.id.toLowerCase().includes(query) ||
          order.customerEmail.toLowerCase().includes(query) ||
          order.customerName.toLowerCase().includes(query)
        )
      }

      if (this.statusFilter) {
        filtered = filtered.filter(order => order.status === this.statusFilter)
      }

      if (this.dateFilter) {
        const now = new Date()
        filtered = filtered.filter(order => {
          const orderDate = new Date(order.createdAt)
          switch (this.dateFilter) {
            case 'today':
              return orderDate.toDateString() === now.toDateString()
            case 'week':
              const weekAgo = new Date(now - 7 * 24 * 60 * 60 * 1000)
              return orderDate >= weekAgo
            case 'month':
              const monthAgo = new Date(now - 30 * 24 * 60 * 60 * 1000)
              return orderDate >= monthAgo
            default:
              return true
          }
        })
      }

      this.filteredOrders = filtered
      this.currentPage = 1
    },

    searchOrders() {
      this.filterOrders()
    },

    calculateStats() {
      this.stats.total = this.orders.length
      this.stats.pending = this.orders.filter(o => o.status === 'PENDING').length
      this.stats.processing = this.orders.filter(o => o.status === 'PROCESSING').length
      this.stats.revenue = this.orders.reduce((sum, order) => sum + order.totalAmount, 0)
    },

    viewOrderDetail(orderId) {
      this.selectedOrder = this.orders.find(order => order.id === orderId)
    },

    closeModal() {
      this.selectedOrder = null
    },

    async deleteOrder(orderId) {
      if (!confirm('Bạn có chắc chắn muốn xóa đơn hàng này?')) return

      try {
        await axios.delete(`/api/admin/orders/${orderId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        this.orders = this.orders.filter(order => order.id !== orderId)
        this.filterOrders()
        this.calculateStats()
        this.$toast.success('Đã xóa đơn hàng thành công')
      } catch (error) {
        console.error('Lỗi khi xóa đơn hàng:', error)
        this.$toast.error('Không thể xóa đơn hàng')
      }
    },

    copyOrderId(orderId) {
      navigator.clipboard.writeText(orderId)
      this.$toast.success('Đã copy ID đơn hàng')
    },

    printOrder(orderId) {
      window.open(`/admin/orders/${orderId}/print`, '_blank')
    },

    exportOrders() {
      // Implement Excel export
      this.$toast.info('Tính năng xuất Excel đang được phát triển')
    },

    getStatusClass(status) {
      const statusClasses = {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-processing', 
        'SHIPPED': 'status-shipped',
        'DELIVERED': 'status-delivered',
        'CANCELLED': 'status-cancelled'
      }
      return statusClasses[status] || 'status-default'
    },

    getStatusText(status) {
      const statusTexts = {
        'PENDING': 'Chờ xử lý',
        'PROCESSING': 'Đang xử lý',
        'SHIPPED': 'Đã gửi',
        'DELIVERED': 'Đã giao',
        'CANCELLED': 'Đã hủy'
      }
      return statusTexts[status] || status
    },

    formatCurrency(amount) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('vi-VN')
    },

    formatTime(dateString) {
      return new Date(dateString).toLocaleTimeString('vi-VN')
    },

    formatDateTime(dateString) {
      return new Date(dateString).toLocaleString('vi-VN')
    }
  }
}
</script>

<style scoped>
.admin-orders {
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 5px;
}

.page-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 300px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px 0 0 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #3498db;
}

.search-btn {
  padding: 12px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #2980b9;
}

.filters {
  display: flex;
  gap: 15px;
  align-items: center;
}

.filter-select {
  padding: 10px 15px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  background: white;
  cursor: pointer;
}

.export-btn {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.export-btn:hover {
  background: #219a52;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 3rem;
  opacity: 0.8;
}

.stat-info h3 {
  font-size: 2rem;
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.stat-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 1rem;
}

.orders-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.loading {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.5;
}

.orders-table {
  overflow-x: auto;
}

.orders-table table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th {
  background: #f8f9fa;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e1e5e9;
}

.orders-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: top;
}

.order-row:hover {
  background: #f8f9fa;
}

.order-id {
  display: flex;
  align-items: center;
  gap: 8px;
}

.copy-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  opacity: 0.6;
  transition: opacity 0.3s;
}

.copy-btn:hover {
  opacity: 1;
  background: #f1f3f4;
}

.customer-info strong {
  display: block;
  margin-bottom: 2px;
}

.customer-info small {
  color: #7f8c8d;
}

.order-items {
  max-width: 200px;
}

.item-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.item-image {
  width: 30px;
  height: 30px;
  object-fit: cover;
  border-radius: 4px;
}

.more-items {
  font-size: 12px;
  color: #7f8c8d;
  font-style: italic;
}

.status-select {
  padding: 6px 10px;
  border: 1px solid #e1e5e9;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  outline: none;
}

.status-pending { background: #fff3cd; color: #856404; border-color: #ffeaa7; }
.status-processing { background: #d4edda; color: #155724; border-color: #c3e6cb; }
.status-shipped { background: #cce5ff; color: #004085; border-color: #b8daff; }
.status-delivered { background: #d1ecf1; color: #0c5460; border-color: #bee5eb; }
.status-cancelled { background: #f8d7da; color: #721c24; border-color: #f5c6cb; }

.order-date div {
  font-weight: 500;
}

.order-date small {
  color: #7f8c8d;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons button {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-view {
  background: #3498db;
  color: white;
}

.btn-view:hover {
  background: #2980b9;
}

.btn-print {
  background: #f39c12;
  color: white;
}

.btn-print:hover {
  background: #e67e22;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e1e5e9;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 15px;
  font-weight: 500;
  color: #2c3e50;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 25px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h2 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 5px;
  color: #7f8c8d;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 25px;
}

.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.info-section h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.2rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.info-section p {
  margin: 8px 0;
  line-height: 1.5;
}

.order-items-detail h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 1.2rem;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.items-list {
  display: grid;
  gap: 15px;
}

.item-detail {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.item-image-large {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.item-info h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.item-info p {
  margin: 4px 0;
  color: #7f8c8d;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .order-info-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .admin-orders {
    padding: 10px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filters {
    flex-wrap: wrap;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .orders-table {
    font-size: 14px;
  }
  
  .orders-table th,
  .orders-table td {
    padding: 10px 8px;
  }
  
  .order-items {
    max-width: 150px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .modal-body {
    padding: 15px;
  }
  
  .item-detail {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-info h3 {
    font-size: 1.5rem;
  }
}