<template>
  <div class="order-details-modal">
    <!-- Modal Header -->
    <div class="modal-header">
      <div class="header-left">
        <h2>Chi tiết Đơn hàng #{{ order.id.slice(-8) }}</h2>
        <div class="order-status">
          <span class="status-badge" :style="{ backgroundColor: getStatusColor(order.status) }">
            {{ getStatusLabel(order.status) }}
          </span>
          <span class="order-date">{{ formatDate(order.createdAt) }}</span>
        </div>
      </div>
      <button @click="$emit('close')" class="close-btn">✕</button>
    </div>

    <!-- Modal Content -->
    <div class="modal-body">
      <!-- Order Summary -->
      <div class="section order-summary">
        <h3>📋 Tổng quan đơn hàng</h3>
        <div class="summary-grid">
          <div class="summary-item">
            <span class="label">Mã đơn hàng:</span>
            <span class="value">#{{ order.id.slice(-8) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Ngày đặt:</span>
            <span class="value">{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Tổng tiền:</span>
            <span class="value highlight">{{ formatCurrency(order.totalAmount) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Phương thức thanh toán:</span>
            <span class="value">{{ order.paymentMethod || 'COD' }}</span>
          </div>
        </div>
      </div>

      <!-- Customer Information -->
      <div class="section customer-info">
        <h3>👤 Thông tin khách hàng</h3>
        <div class="customer-grid">
          <div class="customer-card">
            <div class="customer-avatar">
              {{ order.customerName.charAt(0).toUpperCase() }}
            </div>
            <div class="customer-details">
              <h4>{{ order.customerName }}</h4>
              <p class="customer-email">{{ order.customerEmail }}</p>
              <p class="customer-phone">{{ order.customerPhone }}</p>
            </div>
            <button @click="openCustomerChat" class="btn-contact">
              💬 Liên hệ
            </button>
          </div>
        </div>
      </div>

      <!-- Shipping Information -->
      <div class="section shipping-info">
        <h3>🚚 Thông tin giao hàng</h3>
        <div class="address-grid">
          <div class="address-card">
            <h4>Địa chỉ giao hàng</h4>
            <p>{{ order.shippingAddress }}</p>
            <div class="address-actions">
              <button @click="copyAddress('shipping')" class="btn-copy">📋 Copy</button>
              <button @click="openMapsDirection" class="btn-map">🗺️ Chỉ đường</button>
            </div>
          </div>
          <div class="address-card" v-if="order.billingAddress !== order.shippingAddress">
            <h4>Địa chỉ thanh toán</h4>
            <p>{{ order.billingAddress }}</p>
            <button @click="copyAddress('billing')" class="btn-copy">📋 Copy</button>
          </div>
        </div>
      </div>

      <!-- Order Items -->
      <div class="section order-items">
        <h3>📦 Sản phẩm trong đơn hàng ({{ order.items.length }})</h3>
        <div class="items-list">
          <div v-for="item in order.items" :key="item.id" class="item-card">
            <div class="item-image">
              <img :src="item.productImage || '/placeholder-product.jpg'" :alt="item.productName" />
            </div>
            <div class="item-details">
              <h4>{{ item.productName }}</h4>
              <p class="item-sku">SKU: {{ item.productSku || 'N/A' }}</p>
              <div class="item-variants" v-if="item.variants">
                <span v-for="variant in item.variants" :key="variant.name" class="variant-tag">
                  {{ variant.name }}: {{ variant.value }}
                </span>
              </div>
            </div>
            <div class="item-pricing">
              <div class="quantity">
                <span class="quantity-label">SL:</span>
                <span class="quantity-value">{{ item.quantity }}</span>
              </div>
              <div class="price">
                <span class="unit-price">{{ formatCurrency(item.price) }}</span>
                <span class="total-price">{{ formatCurrency(item.price * item.quantity) }}</span>
              </div>
            </div>
            <div class="item-actions">
              <button @click="viewProduct(item.productId)" class="btn-view-product">
                👁️ Xem SP
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Status Management -->
      <div class="section status-management">
        <h3>⚙️ Quản lý trạng thái</h3>
        <div class="status-workflow">
          <div class="current-status">
            <span class="label">Trạng thái hiện tại:</span>
            <span class="status-badge" :style="{ backgroundColor: getStatusColor(order.status) }">
              {{ getStatusLabel(order.status) }}
            </span>
          </div>
          
          <div class="status-actions">
            <button 
              v-if="canUpdateTo('PROCESSING')"
              @click="updateStatus('PROCESSING')"
              :disabled="updating"
              class="status-btn processing"
            >
              ▶️ Bắt đầu xử lý
            </button>
            
            <button 
              v-if="canUpdateTo('SHIPPED')"
              @click="showShippingModal = true"
              :disabled="updating"
              class="status-btn shipped"
            >
              🚚 Đánh dấu đã gửi
            </button>
            
            <button 
              v-if="canUpdateTo('DELIVERED')"
              @click="updateStatus('DELIVERED')"
              :disabled="updating"
              class="status-btn delivered"
            >
              ✅ Đánh dấu đã giao
            </button>
            
            <button 
              v-if="canCancel()"
              @click="showCancelModal = true"
              :disabled="updating"
              class="status-btn cancel"
            >
              ❌ Hủy đơn hàng
            </button>
          </div>
        </div>

        <!-- Status Timeline -->
        <div class="status-timeline">
          <div class="timeline-header">
            <h4>📅 Lịch sử trạng thái</h4>
          </div>
          <div class="timeline">
            <div 
              v-for="statusItem in statusHistory" 
              :key="statusItem.timestamp"
              class="timeline-item"
              :class="{ active: statusItem.status === order.status }"
            >
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-status">{{ getStatusLabel(statusItem.status) }}</div>
                <div class="timeline-time">{{ formatDate(statusItem.timestamp) }}</div>
                <div v-if="statusItem.note" class="timeline-note">{{ statusItem.note }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Order Notes & Communication -->
      <div class="section order-communication">
        <h3>💬 Ghi chú & Tin nhắn</h3>
        
        <!-- Messages List -->
        <div class="messages-list">
          <div v-if="messages.length === 0" class="no-messages">
            <p>Chưa có tin nhắn nào cho đơn hàng này</p>
          </div>
          <div v-else>
            <div 
              v-for="message in messages" 
              :key="message.id"
              class="message-item"
              :class="{ 'seller-message': message.sender === 'seller' }"
            >
              <div class="message-header">
                <span class="sender">{{ message.sender === 'seller' ? 'Bạn' : order.customerName }}</span>
                <span class="message-time">{{ formatDate(message.timestamp) }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
          </div>
        </div>

        <!-- Send Message -->
        <div class="send-message">
          <div class="message-input-wrapper">
            <textarea 
              v-model="newMessage"
              placeholder="Nhập tin nhắn cho khách hàng..."
              rows="3"
              class="message-input"
            ></textarea>
            <div class="message-actions">
              <div class="message-templates">
                <button 
                  v-for="template in messageTemplates"
                  :key="template.id"
                  @click="useTemplate(template.content)"
                  class="template-btn"
                >
                  {{ template.name }}
                </button>
              </div>
              <button 
                @click="sendMessage"
                :disabled="!newMessage.trim() || sendingMessage"
                class="btn-send-message"
              >
                {{ sendingMessage ? 'Đang gửi...' : '📤 Gửi' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="section action-buttons">
        <div class="button-group">
          <button @click="printShippingLabel" class="action-btn print">
            🖨️ In nhãn giao hàng
          </button>
          <button @click="printInvoice" class="action-btn invoice">
            📄 In hóa đơn
          </button>
          <button @click="exportOrderDetails" class="action-btn export">
            💾 Xuất thông tin
          </button>
          <button @click="duplicateOrder" class="action-btn duplicate">
            📋 Tạo đơn tương tự
          </button>
        </div>
      </div>
    </div>

    <!-- Shipping Modal -->
    <div v-if="showShippingModal" class="shipping-modal-overlay" @click="showShippingModal = false">
      <div class="shipping-modal" @click.stop>
        <h3>🚚 Thông tin giao hàng</h3>
        <form @submit.prevent="confirmShipping">
          <div class="form-group">
            <label>Mã vận đơn:</label>
            <input v-model="shippingInfo.trackingNumber" type="text" placeholder="Nhập mã vận đơn" />
          </div>
          <div class="form-group">
            <label>Đơn vị vận chuyển:</label>
            <select v-model="shippingInfo.carrier">
              <option value="ghn">Giao Hàng Nhanh</option>
              <option value="ghtk">Giao Hàng Tiết Kiệm</option>
              <option value="viettel">Viettel Post</option>
              <option value="vnpost">Vietnam Post</option>
              <option value="other">Khác</option>
            </select>
          </div>
          <div class="form-group">
            <label>Ghi chú:</label>
            <textarea v-model="shippingInfo.note" rows="3" placeholder="Ghi chú về việc giao hàng..."></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showShippingModal = false" class="btn-cancel">Hủy</button>
            <button type="submit" :disabled="!shippingInfo.trackingNumber" class="btn-confirm">Xác nhận</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Cancel Modal -->
    <div v-if="showCancelModal" class="cancel-modal-overlay" @click="showCancelModal = false">
      <div class="cancel-modal" @click.stop>
        <h3>❌ Hủy đơn hàng</h3>
        <p>Bạn có chắc chắn muốn hủy đơn hàng này không?</p>
        <form @submit.prevent="confirmCancel">
          <div class="form-group">
            <label>Lý do hủy:</label>
            <select v-model="cancelInfo.reason" required>
              <option value="">Chọn lý do hủy</option>
              <option value="out_of_stock">Hết hàng</option>
              <option value="customer_request">Khách hàng yêu cầu</option>
              <option value="payment_issue">Vấn đề thanh toán</option>
              <option value="shipping_issue">Vấn đề giao hàng</option>
              <option value="other">Lý do khác</option>
            </select>
          </div>
          <div class="form-group">
            <label>Ghi chú:</label>
            <textarea v-model="cancelInfo.note" rows="3" placeholder="Mô tả chi tiết lý do hủy..."></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showCancelModal = false" class="btn-cancel">Đóng</button>
            <button type="submit" :disabled="!cancelInfo.reason" class="btn-confirm danger">Xác nhận hủy</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useSellerStore } from '@/stores/seller'

const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'status-updated', 'message-sent'])

const sellerStore = useSellerStore()

// Reactive data
const updating = ref(false)
const sendingMessage = ref(false)
const newMessage = ref('')
const messages = ref([])
const showShippingModal = ref(false)
const showCancelModal = ref(false)

const shippingInfo = ref({
  trackingNumber: '',
  carrier: 'ghn',
  note: ''
})

const cancelInfo = ref({
  reason: '',
  note: ''
})

// Message templates
const messageTemplates = ref([
  { id: 1, name: 'Đã xác nhận', content: 'Cảm ơn bạn đã đặt hàng! Chúng tôi đã xác nhận đơn hàng và sẽ xử lý trong thời gian sớm nhất.' },
  { id: 2, name: 'Đang chuẩn bị', content: 'Đơn hàng của bạn đang được chuẩn bị. Chúng tôi sẽ cập nhật thông tin giao hàng sớm nhất có thể.' },
  { id: 3, name: 'Đã gửi hàng', content: 'Đơn hàng của bạn đã được gửi đi. Mã vận đơn: [TRACKING_NUMBER]. Vui lòng theo dõi tình trạng giao hàng.' },
  { id: 4, name: 'Cảm ơn', content: 'Cảm ơn bạn đã mua hàng! Hy vọng bạn hài lòng với sản phẩm. Đừng quên để lại đánh giá nhé!' }
])

// Computed
const statusHistory = computed(() => {
  // Mock status history - replace with real data
  return [
    { status: 'PENDING', timestamp: props.order.createdAt, note: 'Đơn hàng được tạo' },
    ...(props.order.status !== 'PENDING' ? [{ 
      status: props.order.status, 
      timestamp: props.order.updatedAt,
      note: `Cập nhật trạng thái thành ${getStatusLabel(props.order.status)}`
    }] : [])
  ]
})

// Methods
const getStatusLabel = (status) => {
  return sellerStore.getStatusLabel(status)
}

const getStatusColor = (status) => {
  return sellerStore.getStatusColor(status)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const canUpdateTo = (status) => {
  const currentStatus = props.order.status
  const statusFlow = {
    'PENDING': ['PROCESSING'],
    'PROCESSING': ['SHIPPED'],
    'SHIPPED': ['DELIVERED']
  }
  return statusFlow[currentStatus]?.includes(status) || false
}

const canCancel = () => {
  return ['PENDING', 'PROCESSING'].includes(props.order.status)
}

const updateStatus = async (newStatus) => {
  if (updating.value) return
  
  try {
    updating.value = true
    await sellerStore.updateOrderStatus(props.order.id, newStatus)
    emit('status-updated', props.order.id, newStatus)
  } catch (error) {
    alert('Có lỗi xảy ra khi cập nhật trạng thái')
  } finally {
    updating.value = false
  }
}

const confirmShipping = async () => {
  try {
    updating.value = true
    const note = `Đã gửi hàng qua ${shippingInfo.value.carrier}. Mã vận đơn: ${shippingInfo.value.trackingNumber}. ${shippingInfo.value.note}`
    await sellerStore.updateOrderStatus(props.order.id, 'SHIPPED', note)
    showShippingModal.value = false
    emit('status-updated', props.order.id, 'SHIPPED')
  } catch (error) {
    alert('Có lỗi xảy ra khi cập nhật thông tin giao hàng')
  } finally {
    updating.value = false
  }
}

const confirmCancel = async () => {
  try {
    updating.value = true
    const note = `Hủy đơn hàng. Lý do: ${cancelInfo.value.reason}. ${cancelInfo.value.note}`
    await sellerStore.updateOrderStatus(props.order.id, 'CANCELLED', note)
    showCancelModal.value = false
    emit('status-updated', props.order.id, 'CANCELLED')
  } catch (error) {
    alert('Có lỗi xảy ra khi hủy đơn hàng')
  } finally {
    updating.value = false
  }
}

const loadMessages = async () => {
  try {
    const orderMessages = await sellerStore.loadOrderMessages(props.order.id)
    messages.value = orderMessages || []
  } catch (error) {
    console.error('Error loading messages:', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sendingMessage.value) return
  
  try {
    sendingMessage.value = true
    const message = await sellerStore.sendOrderMessage(props.order.id, newMessage.value.trim())
    messages.value.push(message)
    newMessage.value = ''
    emit('message-sent', props.order.id, message)
  } catch (error) {
    alert('Có lỗi xảy ra khi gửi tin nhắn')
  } finally {
    sendingMessage.value = false
  }
}

const useTemplate = (content) => {
  newMessage.value = content
}

const openCustomerChat = () => {
  // Open customer chat in new modal
  emit('open-chat', props.order)
}

const copyAddress = (type) => {
  const address = type === 'shipping' ? props.order.shippingAddress : props.order.billingAddress
  navigator.clipboard.writeText(address)
  alert('Đã copy địa chỉ vào clipboard')
}

const openMapsDirection = () => {
  const address = encodeURIComponent(props.order.shippingAddress)
  window.open(`https://www.google.com/maps/search/?api=1&query=${address}`, '_blank')
}

const viewProduct = (productId) => {
  // Navigate to product details
  window.open(`/products/${productId}`, '_blank')
}

const printShippingLabel = () => {
  const labelContent = `
    <div style="width: 4in; height: 6in; border: 1px solid #000; padding: 10px; font-family: Arial;">
      <h3>COSMIC MARKETPLACE</h3>
      <p><strong>Đơn hàng:</strong> #${props.order.id.slice(-8)}</p>
      <p><strong>Người nhận:</strong> ${props.order.customerName}</p>
      <p><strong>Địa chỉ:</strong> ${props.order.shippingAddress}</p>
      <p><strong>SĐT:</strong> ${props.order.customerPhone}</p>
      <p><strong>Giá trị:</strong> ${formatCurrency(props.order.totalAmount)}</p>
      <p><strong>Ngày:</strong> ${formatDate(props.order.createdAt)}</p>
    </div>
  `
  
  const printWindow = window.open('', '_blank')
  printWindow.document.write(`
    <html>
      <head><title>Shipping Label - ${props.order.id}</title></head>
      <body>${labelContent}</body>
    </html>
  `)
  printWindow.print()
}

const printInvoice = () => {
  // Generate and print invoice
  alert('Chức năng in hóa đơn sẽ được phát triển trong phiên bản tiếp theo')
}

const exportOrderDetails = () => {
  // Export order details to file
  const orderData = {
    ...props.order,
    exportedAt: new Date().toISOString()
  }
  
  const dataStr = JSON.stringify(orderData, null, 2)
  const dataBlob = new Blob([dataStr], {type: 'application/json'})
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `order-${props.order.id.slice(-8)}.json`
  link.click()
}

const duplicateOrder = () => {
  // Create a new order based on this one
  alert('Chức năng tạo đơn tương tự sẽ được phát triển trong phiên bản tiếp theo')
}

// Lifecycle
onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.order-details-modal {
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  color: #ffffff;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.header-left h2 {
  margin: 0 0 0.5rem 0;
  color: #00d4ff;
  font-size: 1.5rem;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  color: white;
  font-weight: 500;
  font-size: 0.875rem;
}

.order-date {
  color: #a0aec0;
  font-size: 0.875rem;
}

.close-btn {
  background: none;
  border: none;
  color: #a0aec0;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.modal-body {
  padding: 1.5rem;
}

.section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(16, 16, 24, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.1);
  border-radius: 12px;
}

.section h3 {
  margin: 0 0 1rem 0;
  color: #00d4ff;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 8px;
}

.summary-item .label {
  color: #a0aec0;
  font-size: 0.875rem;
}

.summary-item .value {
  font-weight: 500;
}

.summary-item .value.highlight {
  color: #00d4ff;
  font-weight: bold;
  font-size: 1.1rem;
}

.customer-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
}

.customer-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff, #0099cc);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
}

.customer-details {
  flex: 1;
}

.customer-details h4 {
  margin: 0 0 0.25rem 0;
  color: #ffffff;
}

.customer-email, .customer-phone {
  margin: 0.25rem 0;
  color: #a0aec0;
  font-size: 0.875rem;
}

.btn-contact {
  padding: 0.5rem 1rem;
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.5);
  border-radius: 8px;
  color: #00d4ff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-contact:hover {
  background: rgba(0, 212, 255, 0.3);
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.address-card {
  padding: 1rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 8px;
}

.address-card h4 {
  margin: 0 0 0.5rem 0;
  color: #00d4ff;
  font-size: 1rem;
}

.address-card p {
  margin: 0 0 1rem 0;
  color: #e2e8f0;
  line-height: 1.4;
}

.address-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-copy, .btn-map {
  padding: 0.5rem;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.btn-copy:hover, .btn-map:hover {
  background: rgba(107, 114, 128, 0.5);
  color: #ffffff;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 8px;
}

.item-image {
  width: 60px;
  height: 60px;
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

.item-details h4 {
  margin: 0 0 0.25rem 0;
  color: #ffffff;
  font-size: 1rem;
}

.item-sku {
  margin: 0 0 0.5rem 0;
  color: #a0aec0;
  font-size: 0.875rem;
}

.item-variants {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.variant-tag {
  padding: 0.25rem 0.5rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 4px;
  color: #00d4ff;
  font-size: 0.75rem;
}

.item-pricing {
  text-align: right;
  margin-right: 1rem;
}

.quantity {
  margin-bottom: 0.5rem;
}

.quantity-label {
  color: #a0aec0;
  font-size: 0.875rem;
}

.quantity-value {
  color: #ffffff;
  font-weight: 500;
  margin-left: 0.25rem;
}

.unit-price {
  display: block;
  color: #a0aec0;
  font-size: 0.875rem;
}

.total-price {
  display: block;
  color: #00d4ff;
  font-weight: bold;
  font-size: 1rem;
}

.btn-view-product {
  padding: 0.5rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #00d4ff;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.btn-view-product:hover {
  background: rgba(0, 212, 255, 0.2);
}

.status-workflow {
  margin-bottom: 1.5rem;
}

.current-status {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.current-status .label {
  color: #a0aec0;
}

.status-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.status-btn {
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-btn.processing { background: #3b82f6; color: white; }
.status-btn.shipped { background: #8b5cf6; color: white; }
.status-btn.delivered { background: #10b981; color: white; }
.status-btn.cancel { background: #ef4444; color: white; }

.status-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.status-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.status-timeline {
  margin-top: 1.5rem;
}

.timeline-header h4 {
  margin: 0 0 1rem 0;
  color: #e2e8f0;
  font-size: 1rem;
}

.timeline {
  position: relative;
  padding-left: 2rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(0, 212, 255, 0.3);
}

.timeline-item {
  position: relative;
  margin-bottom: 1rem;
}

.timeline-dot {
  position: absolute;
  left: -2rem;
  top: 0.25rem;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(107, 114, 128, 0.5);
  border: 3px solid rgba(26, 26, 46, 0.95);
}

.timeline-item.active .timeline-dot {
  background: #00d4ff;
}

.timeline-content {
  padding-left: 0.5rem;
}

.timeline-status {
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 0.25rem;
}

.timeline-time {
  color: #a0aec0;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.timeline-note {
  color: #e2e8f0;
  font-size: 0.875rem;
  font-style: italic;
}

.messages-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 1rem;
  padding: 1rem;
  background: rgba(26, 26, 46, 0.3);
  border-radius: 8px;
}

.no-messages {
  text-align: center;
  color: #a0aec0;
  padding: 2rem;
}

.message-item {
  margin-bottom: 1rem;
  padding: 1rem;
  background: rgba(107, 114, 128, 0.2);
  border-radius: 8px;
}

.message-item.seller-message {
  background: rgba(0, 212, 255, 0.1);
  margin-left: 2rem;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.sender {
  font-weight: 500;
  color: #00d4ff;
}

.message-time {
  color: #a0aec0;
  font-size: 0.875rem;
}

.message-content {
  color: #e2e8f0;
  line-height: 1.4;
}

.send-message {
  margin-top: 1rem;
}

.message-input-wrapper {
  background: rgba(26, 26, 46, 0.5);
  border-radius: 8px;
  padding: 1rem;
}

.message-input {
  width: 100%;
  padding: 0.75rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 1rem;
}

.message-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.message-templates {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.template-btn {
  padding: 0.5rem 0.75rem;
  background: rgba(107, 114, 128, 0.3);
  border: 1px solid rgba(107, 114, 128, 0.5);
  border-radius: 6px;
  color: #a0aec0;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.template-btn:hover {
  background: rgba(107, 114, 128, 0.5);
  color: #ffffff;
}

.btn-send-message {
  padding: 0.75rem 1rem;
  background: #00d4ff;
  border: none;
  border-radius: 6px;
  color: #000000;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-send-message:hover {
  background: #0099cc;
}

.btn-send-message:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.75rem 1rem;
  border: 1px solid;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-btn.print { 
  background: rgba(245, 158, 11, 0.1); 
  border-color: rgba(245, 158, 11, 0.5);
  color: #f59e0b; 
}

.action-btn.invoice { 
  background: rgba(139, 92, 246, 0.1); 
  border-color: rgba(139, 92, 246, 0.5);
  color: #8b5cf6; 
}

.action-btn.export { 
  background: rgba(16, 185, 129, 0.1); 
  border-color: rgba(16, 185, 129, 0.5);
  color: #10b981; 
}

.action-btn.duplicate { 
  background: rgba(107, 114, 128, 0.1); 
  border-color: rgba(107, 114, 128, 0.5);
  color: #6b7280; 
}

.action-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.2);
}

/* Modals */
.shipping-modal-overlay, .cancel-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.shipping-modal, .cancel-modal {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  color: #ffffff;
}

.shipping-modal h3, .cancel-modal h3 {
  margin: 0 0 1rem 0;
  color: #00d4ff;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #e2e8f0;
  font-weight: 500;
}

.form-group input, .form-group select, .form-group textarea {
  width: 100%;
  padding: 0.75rem;
  background: rgba(16, 16, 24, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: #ffffff;
  font-family: inherit;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-cancel {
  padding: 0.75rem 1rem;
  background: rgba(107, 114, 128, 0.5);
  border: 1px solid rgba(107, 114, 128, 0.7);
  border-radius: 6px;
  color: #ffffff;
  cursor: pointer;
}

.btn-confirm {
  padding: 0.75rem 1rem;
  background: #00d4ff;
  border: none;
  border-radius: 6px;
  color: #000000;
  cursor: pointer;
  font-weight: 500;
}

.btn-confirm.danger {
  background: #ef4444;
  color: #ffffff;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .order-details-modal {
    width: 95%;
    max-height: 95vh;
  }
  
  .modal-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .summary-grid {
    grid-template-columns: 1fr;
  }
  
  .address-grid {
    grid-template-columns: 1fr;
  }
  
  .customer-card {
    flex-direction: column;
    text-align: center;
  }
  
  .item-card {
    flex-direction: column;
    text-align: center;
  }
  
  .status-actions {
    flex-direction: column;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .message-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .message-templates {
    justify-content: center;
  }
}
</style>