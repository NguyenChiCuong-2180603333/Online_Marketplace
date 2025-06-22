<template>
  <div class="order-detail-page">
    <div class="container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-nav">
          <button @click="$router.go(-1)" class="back-btn">
            ← Quay lại
          </button>
          <div class="header-info">
            <h1 class="page-title">Chi tiết đơn hàng</h1>
            <p v-if="order" class="order-id">#{{ order.id }}</p>
          </div>
        </div>
        
        <div v-if="order" class="header-actions">
          <button
            v-if="order.status === 'PENDING'"
            @click="cancelOrder"
            class="btn btn-danger"
          >
            ❌ Hủy đơn hàng
          </button>
          <button
            v-if="order.status === 'DELIVERED'"
            @click="reorderItems"
            class="btn btn-secondary"
          >
            🔄 Đặt lại
          </button>
          <button @click="downloadInvoice" class="btn btn-primary">
            📄 Tải hóa đơn
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner">🔄</div>
        <p>Đang tải thông tin đơn hàng...</p>
      </div>

      <!-- Error State -->
      <div v-if="error" class="error-section">
        <div class="error-content">
          <div class="error-icon">❌</div>
          <h3>Không thể tải đơn hàng</h3>
          <p>{{ error }}</p>
          <button @click="loadOrder" class="btn btn-primary">Thử lại</button>
        </div>
      </div>

      <!-- Order Details -->
      <div v-if="!loading && !error && order" class="order-details">
        <!-- Order Status Card -->
        <div class="status-card space-card">
          <div class="status-header">
            <div class="status-info">
              <h2>Trạng thái đơn hàng</h2>
              <div class="status-badge" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </div>
            </div>
            <div class="status-date">
              <span class="date-label">Ngày đặt:</span>
              <span class="date-value">{{ formatDate(order.createdAt) }}</span>
            </div>
          </div>

          <!-- Progress Timeline -->
          <div class="progress-timeline">
            <div
              v-for="(step, index) in orderTimeline"
              :key="step.status"
              class="timeline-item"
              :class="getTimelineItemClass(step.status)"
            >
              <div class="timeline-icon">{{ step.icon }}</div>
              <div class="timeline-content">
                <div class="timeline-title">{{ step.title }}</div>
                <div class="timeline-description">{{ step.description }}</div>
                <div v-if="step.date" class="timeline-date">{{ formatDate(step.date) }}</div>
              </div>
              <div v-if="index < orderTimeline.length - 1" class="timeline-connector"></div>
            </div>
          </div>

          <!-- Estimated Delivery -->
          <div v-if="order.status === 'SHIPPED'" class="delivery-estimate">
            <div class="estimate-icon">🚚</div>
            <div class="estimate-info">
              <h4>Dự kiến giao hàng</h4>
              <p class="estimate-date">{{ formatDate(order.estimatedDelivery) }}</p>
              <p class="estimate-note">Thời gian có thể thay đổi tùy theo địa chỉ giao hàng</p>
            </div>
          </div>
        </div>

        <div class="detail-grid">
          <!-- Order Items -->
          <div class="items-section">
            <div class="section-card space-card">
              <h3 class="section-title">
                <span class="title-icon">📦</span>
                Sản phẩm đã đặt ({{ order.items.length }})
              </h3>
              
              <div class="items-list">
                <div
                  v-for="item in order.items"
                  :key="item.id"
                  class="order-item"
                >
                  <div class="item-image">
                    <img
                      :src="item.productImage || '/placeholder-product.jpg'"
                      :alt="item.productName"
                      @error="handleImageError"
                    />
                  </div>
                  
                  <div class="item-details">
                    <h4 class="item-name">{{ item.productName }}</h4>
                    <p v-if="item.variant" class="item-variant">{{ item.variant }}</p>
                    <div class="item-meta">
                      <span class="item-quantity">Số lượng: {{ item.quantity }}</span>
                      <span class="item-price">{{ formatCurrency(item.price) }}</span>
                    </div>
                    <div class="item-subtotal">
                      Thành tiền: {{ formatCurrency(item.quantity * item.price) }}
                    </div>
                  </div>
                  
                  <div class="item-actions">
                    <router-link :to="`/products/${item.productId}`" class="btn btn-secondary btn-sm">
                      Xem sản phẩm
                    </router-link>
                    <button
                      v-if="order.status === 'DELIVERED' && !item.hasReview"
                      @click="reviewProduct(item)"
                      class="btn btn-accent btn-sm"
                    >
                      ⭐ Đánh giá
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Summary & Info -->
          <div class="summary-section">
            <!-- Payment Summary -->
            <div class="section-card space-card">
              <h3 class="section-title">
                <span class="title-icon">💰</span>
                Tóm tắt thanh toán
              </h3>
              
              <div class="summary-details">
                <div class="summary-row">
                  <span class="summary-label">Tạm tính:</span>
                  <span class="summary-value">{{ formatCurrency(order.subtotal) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">Phí vận chuyển:</span>
                  <span class="summary-value">{{ formatCurrency(order.shippingFee) }}</span>
                </div>
                <div v-if="order.discount > 0" class="summary-row discount">
                  <span class="summary-label">Giảm giá:</span>
                  <span class="summary-value">-{{ formatCurrency(order.discount) }}</span>
                </div>
                <div v-if="order.tax > 0" class="summary-row">
                  <span class="summary-label">Thuế:</span>
                  <span class="summary-value">{{ formatCurrency(order.tax) }}</span>
                </div>
                <div class="summary-row total">
                  <span class="summary-label">Tổng cộng:</span>
                  <span class="summary-value">{{ formatCurrency(order.totalAmount) }}</span>
                </div>
              </div>
            </div>

            <!-- Payment Method -->
            <div class="section-card space-card">
              <h3 class="section-title">
                <span class="title-icon">💳</span>
                Phương thức thanh toán
              </h3>
              
              <div class="payment-info">
                <div class="payment-method">
                  <span class="method-icon">{{ getPaymentIcon(order.paymentMethod) }}</span>
                  <span class="method-name">{{ order.paymentMethod }}</span>
                </div>
                <div class="payment-status" :class="getPaymentStatusClass(order.paymentStatus)">
                  {{ getPaymentStatusText(order.paymentStatus) }}
                </div>
                <div v-if="order.paymentId" class="payment-id">
                  Mã giao dịch: {{ order.paymentId }}
                </div>
              </div>
            </div>

            <!-- Shipping Address -->
            <div class="section-card space-card">
              <h3 class="section-title">
                <span class="title-icon">📍</span>
                Địa chỉ giao hàng
              </h3>
              
              <div class="address-info">
                <div class="recipient-name">{{ order.shippingAddress.name }}</div>
                <div class="recipient-phone">{{ order.shippingAddress.phone }}</div>
                <div class="address-detail">
                  {{ order.shippingAddress.address }}, 
                  {{ order.shippingAddress.ward }}, 
                  {{ order.shippingAddress.district }}, 
                  {{ order.shippingAddress.city }}
                </div>
                <div v-if="order.shippingAddress.note" class="address-note">
                  Ghi chú: {{ order.shippingAddress.note }}
                </div>
              </div>
            </div>

            <!-- Order Actions -->
            <div class="section-card space-card">
              <h3 class="section-title">
                <span class="title-icon">⚡</span>
                Hành động
              </h3>
              
              <div class="action-buttons">
                <button
                  v-if="order.status === 'PENDING'"
                  @click="cancelOrder"
                  class="btn btn-danger btn-full"
                >
                  ❌ Hủy đơn hàng
                </button>
                
                <button
                  v-if="order.status === 'DELIVERED'"
                  @click="reorderItems"
                  class="btn btn-secondary btn-full"
                >
                  🔄 Đặt lại đơn hàng
                </button>
                
                <button @click="contactSupport" class="btn btn-secondary btn-full">
                  💬 Liên hệ hỗ trợ
                </button>
                
                <button @click="downloadInvoice" class="btn btn-primary btn-full">
                  📄 Tải hóa đơn
                </button>
                
                <button
                  v-if="order.status === 'DELIVERED'"
                  @click="requestReturn"
                  class="btn btn-warning btn-full"
                >
                  🔄 Yêu cầu trả hàng
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderAPI } from '@/services/api'

export default {
  name: 'OrderDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // Reactive data
    const order = ref(null)
    const loading = ref(true)
    const error = ref(null)
    
    // Computed
    const orderTimeline = computed(() => {
      if (!order.value) return []
      
      const timeline = [
        {
          status: 'PENDING',
          title: 'Đơn hàng đã được tạo',
          description: 'Đơn hàng của bạn đã được tiếp nhận và đang chờ xử lý',
          icon: '📋',
          date: order.value.createdAt
        },
        {
          status: 'PROCESSING',
          title: 'Đang chuẩn bị hàng',
          description: 'Chúng tôi đang chuẩn bị và đóng gói sản phẩm',
          icon: '📦',
          date: order.value.processingAt
        },
        {
          status: 'SHIPPED',
          title: 'Đã giao cho đơn vị vận chuyển',
          description: 'Đơn hàng đã được giao cho đơn vị vận chuyển',
          icon: '🚚',
          date: order.value.shippedAt
        },
        {
          status: 'DELIVERED',
          title: 'Giao hàng thành công',
          description: 'Đơn hàng đã được giao thành công đến địa chỉ của bạn',
          icon: '✅',
          date: order.value.deliveredAt
        }
      ]
      
      // Filter out steps that haven't occurred yet (except for current status)
      const currentStatusIndex = timeline.findIndex(step => step.status === order.value.status)
      return timeline.filter((step, index) => index <= currentStatusIndex)
    })
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const getStatusClass = (status) => {
      const statusClasses = {
        'PENDING': 'status-pending',
        'PROCESSING': 'status-processing',
        'SHIPPED': 'status-shipped',
        'DELIVERED': 'status-delivered',
        'CANCELLED': 'status-cancelled'
      }
      return statusClasses[status] || 'status-default'
    }
    
    const getStatusText = (status) => {
      const statusTexts = {
        'PENDING': 'Chờ xử lý',
        'PROCESSING': 'Đang xử lý',
        'SHIPPED': 'Đang giao hàng',
        'DELIVERED': 'Đã giao',
        'CANCELLED': 'Đã hủy'
      }
      return statusTexts[status] || status
    }
    
    const getTimelineItemClass = (status) => {
      const currentStatusIndex = orderTimeline.value.findIndex(step => step.status === order.value.status)
      const stepIndex = orderTimeline.value.findIndex(step => step.status === status)
      
      if (order.value.status === 'CANCELLED' && status === 'PENDING') {
        return 'timeline-completed'
      }
      
      if (stepIndex < currentStatusIndex) return 'timeline-completed'
      if (stepIndex === currentStatusIndex) return 'timeline-active'
      return 'timeline-pending'
    }
    
    const getPaymentIcon = (method) => {
      const icons = {
        'Thẻ tín dụng': '💳',
        'COD': '💵',
        'Ví MoMo': '📱',
        'Chuyển khoản': '🏦',
        'Ví ZaloPay': '💸'
      }
      return icons[method] || '💳'
    }
    
    const getPaymentStatusClass = (status) => {
      const classes = {
        'PENDING': 'payment-pending',
        'COMPLETED': 'payment-completed',
        'FAILED': 'payment-failed'
      }
      return classes[status] || 'payment-pending'
    }
    
    const getPaymentStatusText = (status) => {
      const texts = {
        'PENDING': 'Chờ thanh toán',
        'COMPLETED': 'Đã thanh toán',
        'FAILED': 'Thanh toán thất bại'
      }
      return texts[status] || status
    }
    
    const handleImageError = (event) => {
      event.target.src = '/placeholder-product.jpg'
    }
    
    const loadOrder = async () => {
      try {
        loading.value = true
        error.value = null
        
        const orderId = route.params.id
        
        // Mock data - replace with real API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        order.value = {
          id: orderId,
          status: 'SHIPPED',
          createdAt: '2024-12-20T09:15:00Z',
          processingAt: '2024-12-20T10:30:00Z',
          shippedAt: '2024-12-21T14:20:00Z',
          estimatedDelivery: '2024-12-23T18:00:00Z',
          paymentMethod: 'Thẻ tín dụng',
          paymentStatus: 'COMPLETED',
          paymentId: 'TXN-20241220-001',
          subtotal: 2590000,
          shippingFee: 30000,
          discount: 100000,
          tax: 0,
          totalAmount: 2520000,
          shippingAddress: {
            name: 'Nguyễn Văn A',
            phone: '0123456789',
            address: '123 Đường ABC',
            ward: 'Phường XYZ',
            district: 'Quận 1',
            city: 'TP.HCM',
            note: 'Giao hàng giờ hành chính'
          },
          items: [
            {
              id: 1,
              productId: 'PROD-001',
              productName: 'Smartphone Galaxy Cosmic Pro',
              productImage: '/placeholder-product.jpg',
              variant: 'Màu xanh, 128GB',
              quantity: 1,
              price: 1990000,
              hasReview: false
            },
            {
              id: 2,
              productId: 'PROD-002',
              productName: 'Tai nghe Wireless Premium',
              productImage: '/placeholder-product.jpg',
              variant: 'Màu đen',
              quantity: 1,
              price: 600000,
              hasReview: true
            }
          ]
        }
        
      } catch (err) {
        error.value = err.message || 'Có lỗi xảy ra khi tải đơn hàng'
      } finally {
        loading.value = false
      }
    }
    
    const cancelOrder = async () => {
      if (!confirm('Bạn có chắc muốn hủy đơn hàng này? Hành động này không thể hoàn tác.')) return
      
      try {
        await orderAPI.cancel(order.value.id)
        order.value.status = 'CANCELLED'
        alert('Đã hủy đơn hàng thành công')
      } catch (error) {
        console.error('Error canceling order:', error)
        alert('Có lỗi xảy ra khi hủy đơn hàng')
      }
    }
    
    const reorderItems = async () => {
      try {
        // Add all items to cart
        for (const item of order.value.items) {
          // Add to cart logic here - integrate with cart store
          // await cartStore.addItem(item.productId, item.quantity)
        }
        alert('Đã thêm tất cả sản phẩm vào giỏ hàng')
        router.push('/cart')
      } catch (error) {
        console.error('Error reordering:', error)
        alert('Có lỗi xảy ra khi đặt lại đơn hàng')
      }
    }
    
    const reviewProduct = (item) => {
      router.push(`/products/${item.productId}/review?orderId=${order.value.id}`)
    }
    
    const contactSupport = () => {
      // Navigate to support chat or open support modal
      alert('Tính năng liên hệ hỗ trợ sẽ được cập nhật sớm')
    }
    
    const downloadInvoice = () => {
      // Generate and download invoice PDF
      alert('Đang tạo hóa đơn... Tính năng sẽ được cập nhật sớm')
    }
    
    const requestReturn = () => {
      if (!confirm('Bạn có muốn yêu cầu trả hàng cho đơn hàng này?')) return
      alert('Yêu cầu trả hàng đã được gửi. Chúng tôi sẽ liên hệ với bạn sớm nhất.')
    }
    
    // Lifecycle
    onMounted(() => {
      loadOrder()
    })
    
    return {
      order,
      loading,
      error,
      orderTimeline,
      formatCurrency,
      formatDate,
      getStatusClass,
      getStatusText,
      getTimelineItemClass,
      getPaymentIcon,
      getPaymentStatusClass,
      getPaymentStatusText,
      handleImageError,
      loadOrder,
      cancelOrder,
      reorderItems,
      reviewProduct,
      contactSupport,
      downloadInvoice,
      requestReturn
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  color: var(--text-secondary);
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  border-color: var(--text-accent);
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.page-title {
  font-size: 2rem;
  color: var(--text-primary);
  margin: 0;
}

.order-id {
  font-size: 1rem;
  color: var(--text-accent);
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* Status Card */
.status-card {
  margin-bottom: 2rem;
  padding: 2rem;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.status-info h2 {
  color: var(--text-primary);
  margin: 0;
  font-size: 1.5rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  width: fit-content;
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

.status-date {
  text-align: right;
}

.date-label {
  display: block;
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.date-value {
  font-size: 0.9rem;
  color: var(--text-primary);
  font-weight: 500;
}

/* Progress Timeline */
.progress-timeline {
  position: relative;
  padding: 1rem 0;
  margin-bottom: 2rem;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  position: relative;
  padding-bottom: 2rem;
  transition: all 0.3s ease;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-icon {
  font-size: 2rem;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(26, 26, 46, 0.8);
  border: 2px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.timeline-completed .timeline-icon {
  background: rgba(0, 255, 136, 0.2);
  border-color: var(--text-success);
  color: var(--text-success);
}

.timeline-active .timeline-icon {
  background: rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent);
  color: var(--text-accent);
  animation: pulse 2s infinite;
}

.timeline-pending .timeline-icon {
  opacity: 0.4;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.timeline-content {
  flex: 1;
  padding-top: 0.5rem;
}

.timeline-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.timeline-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 0.5rem;
}

.timeline-date {
  font-size: 0.8rem;
  color: var(--text-accent);
  font-weight: 500;
}

.timeline-connector {
  position: absolute;
  left: 29px;
  top: 60px;
  width: 2px;
  height: calc(100% - 30px);
  background: rgba(255, 255, 255, 0.1);
  z-index: 1;
}

.timeline-completed .timeline-connector {
  background: rgba(0, 255, 136, 0.3);
}

.timeline-active .timeline-connector {
  background: rgba(0, 212, 255, 0.3);
}

/* Delivery Estimate */
.delivery-estimate {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 1rem;
}

.estimate-icon {
  font-size: 2rem;
}

.estimate-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.estimate-date {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-accent);
  margin-bottom: 0.25rem;
}

.estimate-note {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin: 0;
}

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  align-items: start;
}

/* Section Cards */
.section-card {
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.2rem;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.title-icon {
  font-size: 1.3rem;
}

/* Order Items */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.1);
  transition: all 0.3s ease;
}

.order-item:hover {
  border-color: rgba(0, 212, 255, 0.3);
  background: rgba(0, 0, 0, 0.3);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
}

.item-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  line-height: 1.3;
}

.item-variant {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.item-quantity {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.item-price {
  font-size: 0.9rem;
  color: var(--text-accent);
  font-weight: 500;
}

.item-subtotal {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-self: flex-start;
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

/* Summary Details */
.summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-row.discount .summary-value {
  color: var(--text-success);
}

.summary-row.total {
  padding-top: 0.75rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  font-weight: 600;
  font-size: 1.1rem;
}

.summary-label {
  color: var(--text-secondary);
}

.summary-value {
  color: var(--text-primary);
  font-weight: 500;
}

.summary-row.total .summary-value {
  color: var(--text-accent);
  font-size: 1.2rem;
}

/* Payment Info */
.payment-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.method-icon {
  font-size: 1.5rem;
}

.method-name {
  font-weight: 500;
  color: var(--text-primary);
}

.payment-status {
  padding: 0.375rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  width: fit-content;
}

.payment-pending {
  background: rgba(255, 205, 60, 0.2);
  color: var(--text-warning);
}

.payment-completed {
  background: rgba(0, 255, 136, 0.2);
  color: var(--text-success);
}

.payment-failed {
  background: rgba(233, 69, 96, 0.2);
  color: var(--text-danger);
}

.payment-id {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-family: 'Courier New', monospace;
}

/* Address Info */
.address-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.recipient-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.recipient-phone {
  color: var(--text-accent);
  font-weight: 500;
}

.address-detail {
  color: var(--text-secondary);
  line-height: 1.5;
}

.address-note {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-style: italic;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.btn-full {
  width: 100%;
  text-align: center;
  padding: 0.75rem;
  border-radius: 25px;
  font-weight: 500;
}

.btn-danger {
  background: rgba(233, 69, 96, 0.2);
  border: 1px solid rgba(233, 69, 96, 0.4);
  color: var(--text-danger);
}

.btn-danger:hover {
  background: rgba(233, 69, 96, 0.3);
  transform: translateY(-2px);
}

.btn-warning {
  background: rgba(255, 205, 60, 0.2);
  border: 1px solid rgba(255, 205, 60, 0.4);
  color: var(--text-warning);
}

.btn-warning:hover {
  background: rgba(255, 205, 60, 0.3);
  transform: translateY(-2px);
}

/* Loading & Error States */
.loading-section,
.error-section {
  text-align: center;
  padding: 4rem 0;
}

.loading-spinner {
  font-size: 3rem;
  margin-bottom: 1rem;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-content {
  max-width: 400px;
  margin: 0 auto;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.error-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.error-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

/* Responsive */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .order-detail-page {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .status-card,
  .section-card {
    padding: 1rem;
  }
  
  .status-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .order-item {
    flex-direction: column;
    text-align: center;
  }
  
  .item-details {
    text-align: left;
  }
  
  .item-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .item-actions {
    flex-direction: row;
    justify-content: center;
  }
  
  .timeline-item {
    gap: 0.75rem;
  }
  
  .timeline-icon {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }
  
  .timeline-connector {
    left: 24px;
    top: 50px;
  }
}

@media (max-width: 480px) {
  .header-nav {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .header-actions {
    flex-direction: column;
  }
  
  .status-header {
    text-align: center;
  }
  
  .status-date {
    text-align: center;
  }
  
  .delivery-estimate {
    flex-direction: column;
    text-align: center;
  }
  
  .order-item {
    padding: 0.75rem;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .timeline-content {
    padding-top: 0.25rem;
  }
}
</style>