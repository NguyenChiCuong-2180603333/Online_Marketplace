<template>
  <div class="order-details-modal-overlay" @click="$emit('close')">
    <div class="order-details-modal" @click.stop>
      <!-- Modal Header -->
      <div class="modal-header">
        <div class="header-left">
          <h2>Chi tiết Đơn hàng #{{ order.id.slice(-8) }}</h2>
          <div class="order-status">
            <span class="status-badge" :style="{ backgroundColor: getStatusColor(order.status) }">
              {{ getSellerStatusLabel(order.status) }}
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
                <p>{{ order.customerEmail }}</p>
                <p>{{ order.customerPhone }}</p>
              </div>
            </div>
            <div class="shipping-address">
              <h4>📍 Địa chỉ giao hàng</h4>
              <p>{{ order.shippingAddress }}</p>
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="section order-items">
          <h3>📦 Sản phẩm đã đặt</h3>
          <div class="items-list">
            <div v-for="item in order.items" :key="item.id" class="item-card">
              <div class="item-image">
                <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
              </div>
              <div class="item-info">
                <h4>{{ item.name }}</h4>
                <p class="item-description">{{ item.description }}</p>
                <div class="item-variants" v-if="item.variants">
                  <span v-for="variant in item.variants" :key="variant.key" class="variant">
                    {{ variant.key }}: {{ variant.value }}
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
                {{ getSellerStatusLabel(order.status) }}
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
                ✅ Hoàn thành
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

          <!-- Status History -->
          <div class="status-history">
            <h4>📋 Lịch sử trạng thái</h4>
            <div class="history-timeline">
              <div v-for="(history, index) in statusHistory" :key="index" class="history-item">
                <div class="history-icon">
                  <span class="status-badge small" :style="{ backgroundColor: getStatusColor(history.status) }">
                    {{ getSellerStatusLabel(history.status) }}
                  </span>
                </div>
                <div class="history-content">
                  <p class="history-note">{{ history.note }}</p>
                  <p class="history-time">{{ formatDate(history.timestamp) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Customer Communication -->
        <div class="section communication">
          <h3>💬 Tin nhắn với khách hàng</h3>
          
          <div class="messages-list">
            <div v-for="message in messages" :key="message.id" 
                 :class="['message-item', message.sender === 'seller' ? 'seller-message' : 'customer-message']">
              <div class="message-header">
                <span class="sender">{{ message.sender === 'seller' ? 'Bạn' : order.customerName }}</span>
                <span class="message-time">{{ formatDate(message.timestamp) }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
          </div>

          <div class="send-message">
            <div class="message-input-wrapper">
              <textarea
                v-model="newMessage"
                placeholder="Nhập tin nhắn cho khách hàng..."
                class="message-input"
                rows="3"
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
                  {{ sendingMessage ? 'Đang gửi...' : 'Gửi' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="section actions">
          <h3>⚡ Hành động</h3>
          <div class="button-group">
            <button @click="printOrder" class="action-btn print">
              🖨️ In đơn hàng
            </button>
            <button @click="generateInvoice" class="action-btn invoice">
              📄 Tạo hóa đơn
            </button>
            <button @click="exportOrder" class="action-btn export">
              📊 Xuất Excel
            </button>
            <button @click="duplicateOrder" class="action-btn duplicate">
              📋 Tạo đơn tương tự
            </button>
          </div>
        </div>
      </div>

      <!-- Shipping Modal -->
      <div v-if="showShippingModal" class="modal-overlay" @click="showShippingModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h2>🚚 Thông tin giao hàng</h2>
            <button @click="showShippingModal = false" class="close-btn">✕</button>
          </div>
          <form @submit.prevent="confirmShipping" class="shipping-form">
            <div class="form-group">
              <label>Đơn vị vận chuyển:</label>
              <select v-model="shippingInfo.carrier" required>
                <option value="ghn">Giao Hàng Nhanh</option>
                <option value="ghtk">Giao Hàng Tiết Kiệm</option>
                <option value="viettel">Viettel Post</option>
                <option value="vnpost">VN Post</option>
                <option value="other">Khác</option>
              </select>
            </div>
            <div class="form-group">
              <label>Mã vận đơn:</label>
              <input
                v-model="shippingInfo.trackingNumber"
                type="text"
                placeholder="Nhập mã vận đơn..."
                required
              />
            </div>
            <div class="form-group">
              <label>Ghi chú:</label>
              <textarea
                v-model="shippingInfo.note"
                rows="3"
                placeholder="Thông tin bổ sung về giao hàng..."
              ></textarea>
            </div>
            <div class="modal-actions">
              <button type="button" @click="showShippingModal = false" class="btn-cancel">Hủy</button>
              <button type="submit" :disabled="updating" class="btn-confirm">
                {{ updating ? 'Đang cập nhật...' : 'Xác nhận gửi hàng' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Cancel Modal -->
      <div v-if="showCancelModal" class="modal-overlay" @click="showCancelModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h2>❌ Hủy đơn hàng</h2>
            <button @click="showCancelModal = false" class="close-btn">✕</button>
          </div>
          <form @submit.prevent="confirmCancel" class="cancel-form">
            <div class="form-group">
              <label>Lý do hủy:</label>
              <select v-model="cancelInfo.reason" required>
                <option value="">Chọn lý do...</option>
                <option value="out_of_stock">Hết hàng</option>
                <option value="customer_request">Khách hàng yêu cầu</option>
                <option value="payment_issue">Vấn đề thanh toán</option>
                <option value="shipping_issue">Vấn đề vận chuyển</option>
                <option value="other">Khác</option>
              </select>
            </div>
            <div class="form-group">
              <label>Ghi chú:</label>
              <textarea
                v-model="cancelInfo.note"
                rows="3"
                placeholder="Mô tả chi tiết lý do hủy..."
              ></textarea>
            </div>
            <div class="modal-actions">
              <button type="button" @click="showCancelModal = false" class="btn-cancel">Đóng</button>
              <button type="submit" :disabled="!cancelInfo.reason" class="btn-confirm danger">
                Xác nhận hủy
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useSellerStore } from '@/stores/seller'
import { getStatusLabel, getStatusColor } from '@/utils/constants'

const props = defineProps({
  order: {
    type: Object,
    required: true,
  },
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
  note: '',
})

const cancelInfo = ref({
  reason: '',
  note: '',
})

// Message templates
const messageTemplates = ref([
  {
    id: 1,
    name: 'Đã xác nhận',
    content:
      'Cảm ơn bạn đã đặt hàng! Chúng tôi đã xác nhận đơn hàng và sẽ xử lý trong thời gian sớm nhất.',
  },
  {
    id: 2,
    name: 'Đang chuẩn bị',
    content:
      'Đơn hàng của bạn đang được chuẩn bị. Chúng tôi sẽ cập nhật thông tin giao hàng sớm nhất có thể.',
  },
  {
    id: 3,
    name: 'Đã gửi hàng',
    content:
      'Đơn hàng của bạn đã được gửi đi. Mã vận đơn: [TRACKING_NUMBER]. Vui lòng theo dõi tình trạng giao hàng.',
  },
  {
    id: 4,
    name: 'Cảm ơn',
    content:
      'Cảm ơn bạn đã mua hàng! Hy vọng bạn hài lòng với sản phẩm. Đừng quên để lại đánh giá nhé!',
  },
])

// Computed
const statusHistory = computed(() => {
  // Mock status history - replace with real data
  return [
    { status: 'PENDING', timestamp: props.order.createdAt, note: 'Đơn hàng được tạo' },
    ...(props.order.status !== 'PENDING'
      ? [
          {
            status: props.order.status,
            timestamp: props.order.updatedAt,
            note: `Cập nhật trạng thái thành ${getSellerStatusLabel(props.order.status)}`,
          },
        ]
      : []),
  ]
})

// Methods
const getSellerStatusLabel = (status) => {
  return getStatusLabel(status, 'SELLER')
}

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

const canUpdateTo = (status) => {
  const currentStatus = props.order.status
  const statusFlow = {
    PENDING: ['PROCESSING'],
    PROCESSING: ['SHIPPED'],
    SHIPPED: ['DELIVERED'],
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

const useTemplate = (content) => {
  newMessage.value = content
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sendingMessage.value) return

  try {
    sendingMessage.value = true
    
    // Add message to local list
    const message = {
      id: Date.now(),
      sender: 'seller',
      content: newMessage.value,
      timestamp: new Date().toISOString(),
    }
    
    messages.value.push(message)
    
    // Send to backend
    await sellerStore.sendMessageToCustomer(props.order.id, newMessage.value)
    
    newMessage.value = ''
    emit('message-sent', message)
  } catch (error) {
    console.error('Error sending message:', error)
    alert('Có lỗi xảy ra khi gửi tin nhắn')
  } finally {
    sendingMessage.value = false
  }
}

const viewProduct = (productId) => {
  // Navigate to product page
  window.open(`/products/${productId}`, '_blank')
}

const printOrder = () => {
  // Generate and print order
  window.print()
}

const generateInvoice = () => {
  // Generate invoice
  alert('Tính năng tạo hóa đơn đang được phát triển')
}

const exportOrder = () => {
  // Export order to Excel
  alert('Tính năng xuất Excel đang được phát triển')
}

const duplicateOrder = () => {
  // Create similar order
  alert('Tính năng tạo đơn tương tự đang được phát triển')
}

// Load messages on mount
onMounted(async () => {
  try {
    messages.value = await sellerStore.getOrderMessages(props.order.id)
  } catch (error) {
    console.error('Error loading messages:', error)
  }
})
</script>

<style scoped>
.order-details-modal-overlay {
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
  padding: 20px;
}

.order-details-modal {
  background: white;
  border-radius: 12px;
  max-width: 1000px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
  background: white;
  border-radius: 12px 12px 0 0;
}

.header-left h2 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  color: white;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.small {
  padding: 4px 8px;
  font-size: 0.7rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #7f8c8d;
  padding: 5px;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background: white;
  border-radius: 6px;
}

.label {
  font-weight: 500;
  color: #7f8c8d;
}

.value {
  font-weight: 600;
  color: #2c3e50;
}

.value.highlight {
  color: #e74c3c;
  font-size: 1.1rem;
}

.customer-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.customer-card {
  display: flex;
  align-items: center;
  gap: 15px;
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.customer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.2rem;
}

.shipping-address {
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.item-card {
  display: flex;
  gap: 15px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  align-items: center;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.item-description {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin: 0 0 10px 0;
}

.item-variants {
  display: flex;
  gap: 10px;
}

.variant {
  background: #ecf0f1;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.item-pricing {
  text-align: right;
}

.quantity {
  margin-bottom: 5px;
}

.quantity-label {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.unit-price {
  color: #7f8c8d;
  font-size: 0.9rem;
  display: block;
}

.total-price {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1rem;
}

.btn-view-product {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
}

.status-workflow {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.current-status {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.status-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-btn {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.status-btn.processing {
  background: #3498db;
  color: white;
}

.status-btn.shipped {
  background: #9b59b6;
  color: white;
}

.status-btn.delivered {
  background: #27ae60;
  color: white;
}

.status-btn.cancel {
  background: #e74c3c;
  color: white;
}

.status-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.history-timeline {
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.history-item {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ecf0f1;
}

.history-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.messages-list {
  max-height: 300px;
  overflow-y: auto;
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 8px;
}

.message-item.customer-message {
  background: #f8f9fa;
  margin-right: 2rem;
}

.message-item.seller-message {
  background: #e3f2fd;
  margin-left: 2rem;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.sender {
  font-weight: 500;
  color: #2c3e50;
}

.message-time {
  color: #7f8c8d;
  font-size: 0.8rem;
}

.message-input-wrapper {
  background: white;
  padding: 15px;
  border-radius: 8px;
}

.message-input {
  width: 100%;
  padding: 10px;
  border: 2px solid #ecf0f1;
  border-radius: 6px;
  resize: vertical;
  font-family: inherit;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.message-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.message-templates {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.template-btn {
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.template-btn:hover {
  background: #e9ecef;
}

.btn-send-message {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-send-message:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 12px 20px;
  border: 1px solid;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
}

.action-btn.print {
  border-color: #f59e0b;
  color: #f59e0b;
}

.action-btn.print:hover {
  background: #fff3cd;
}

.action-btn.invoice {
  border-color: #8b5cf6;
  color: #8b5cf6;
}

.action-btn.invoice:hover {
  background: #f3e8ff;
}

.action-btn.export {
  border-color: #10b981;
  color: #10b981;
}

.action-btn.export:hover {
  background: #d1fae5;
}

.action-btn.duplicate {
  border-color: #64748b;
  color: #64748b;
}

.action-btn.duplicate:hover {
  background: #f1f5f9;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.shipping-form,
.cancel-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #2c3e50;
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #ecf0f1;
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #ecf0f1;
}

.btn-cancel {
  padding: 10px 20px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm.danger {
  background: #e74c3c;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .order-details-modal-overlay {
    padding: 10px;
  }
  
  .order-details-modal {
    max-height: 95vh;
  }
  
  .modal-body {
    padding: 15px;
  }
  
  .customer-grid {
    grid-template-columns: 1fr;
  }
  
  .item-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .status-actions {
    flex-direction: column;
  }
  
  .message-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .message-templates {
    justify-content: center;
    margin-bottom: 10px;
  }
  
  .button-group {
    flex-direction: column;
  }
}
</style>