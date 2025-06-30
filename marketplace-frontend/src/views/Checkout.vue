<template>
  <div class="checkout-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🚀 Thanh toán Galactic</h1>
        <div class="checkout-steps">
          <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
            <div class="step-icon">🛒</div>
            <span>Giỏ hàng</span>
          </div>
          <div class="step-line"></div>
          <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
            <div class="step-icon">📋</div>
            <span>Thông tin</span>
          </div>
          <div class="step-line"></div>
          <div class="step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
            <div class="step-icon">💳</div>
            <span>Thanh toán</span>
          </div>
          <div class="step-line"></div>
          <div class="step" :class="{ active: currentStep >= 4 }">
            <div class="step-icon">✨</div>
            <span>Hoàn thành</span>
          </div>
        </div>
      </div>

      <div class="checkout-content">
        <!-- Main Content -->
        <div class="checkout-main">
          <!-- Step 2: Shipping Information -->
          <div v-if="currentStep === 2" class="checkout-section">
            <div class="section-header">
              <h2>📍 Thông tin giao hàng</h2>
              <p>Vui lòng nhập địa chỉ để chúng tôi có thể giao hàng tận nơi</p>
            </div>

            <form @submit.prevent="validateShippingInfo" class="shipping-form space-card">
              <div class="form-row">
                <div class="form-group">
                  <label for="firstName" class="form-label">Họ *</label>
                  <input
                    id="firstName"
                    v-model="shippingInfo.firstName"
                    type="text"
                    class="form-input"
                    :class="{ error: errors.firstName }"
                    placeholder="Nhập họ của bạn"
                    required
                  />
                  <span v-if="errors.firstName" class="error-message">{{ errors.firstName }}</span>
                </div>

                <div class="form-group">
                  <label for="lastName" class="form-label">Tên *</label>
                  <input
                    id="lastName"
                    v-model="shippingInfo.lastName"
                    type="text"
                    class="form-input"
                    :class="{ error: errors.lastName }"
                    placeholder="Nhập tên của bạn"
                    required
                  />
                  <span v-if="errors.lastName" class="error-message">{{ errors.lastName }}</span>
                </div>
              </div>

              <div class="form-group">
                <label for="email" class="form-label">Email *</label>
                <input
                  id="email"
                  v-model="shippingInfo.email"
                  type="email"
                  class="form-input"
                  :class="{ error: errors.email }"
                  placeholder="example@galaxy.com"
                  required
                />
                <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
              </div>

              <div class="form-group">
                <label for="phone" class="form-label">Số điện thoại *</label>
                <input
                  id="phone"
                  v-model="shippingInfo.phone"
                  type="tel"
                  class="form-input"
                  :class="{ error: errors.phone }"
                  placeholder="0123456789"
                  required
                />
                <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
              </div>

              <div class="form-group">
                <label for="address" class="form-label">Địa chỉ *</label>
                <input
                  id="address"
                  v-model="shippingInfo.address"
                  type="text"
                  class="form-input"
                  :class="{ error: errors.address }"
                  placeholder="Số nhà, tên đường"
                  required
                />
                <span v-if="errors.address" class="error-message">{{ errors.address }}</span>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="city" class="form-label">Thành phố *</label>
                  <select
                    id="city"
                    v-model="shippingInfo.city"
                    class="form-input"
                    :class="{ error: errors.city }"
                    @change="loadDistricts"
                    required
                  >
                    <option value="">Chọn thành phố</option>
                    <option v-for="city in cities" :key="city.code" :value="city.code">
                      {{ city.name }}
                    </option>
                  </select>
                  <span v-if="errors.city" class="error-message">{{ errors.city }}</span>
                </div>

                <div class="form-group">
                  <label for="district" class="form-label">Quận/Huyện *</label>
                  <select
                    id="district"
                    v-model="shippingInfo.district"
                    class="form-input"
                    :class="{ error: errors.district }"
                    :disabled="!shippingInfo.city"
                    @change="loadWards"
                    required
                  >
                    <option value="">Chọn quận/huyện</option>
                    <option
                      v-for="district in districts"
                      :key="district.code"
                      :value="district.code"
                    >
                      {{ district.name }}
                    </option>
                  </select>
                  <span v-if="errors.district" class="error-message">{{ errors.district }}</span>
                </div>

                <div class="form-group">
                  <label for="ward" class="form-label">Phường/Xã *</label>
                  <select
                    id="ward"
                    v-model="shippingInfo.ward"
                    class="form-input"
                    :class="{ error: errors.ward }"
                    :disabled="!shippingInfo.district"
                    required
                  >
                    <option value="">Chọn phường/xã</option>
                    <option v-for="ward in wards" :key="ward.code" :value="ward.code">
                      {{ ward.name }}
                    </option>
                  </select>
                  <span v-if="errors.ward" class="error-message">{{ errors.ward }}</span>
                </div>
              </div>

              <div class="form-group">
                <label for="notes" class="form-label">Ghi chú (tùy chọn)</label>
                <textarea
                  id="notes"
                  v-model="shippingInfo.notes"
                  class="form-input"
                  rows="3"
                  placeholder="Ghi chú về địa chỉ giao hàng, thời gian nhận hàng..."
                ></textarea>
              </div>

              <!-- Delivery Options -->
              <div class="delivery-options">
                <h3>🚀 Phương thức giao hàng</h3>
                <div class="delivery-list">
                  <div
                    v-for="option in deliveryOptions"
                    :key="option.id"
                    class="delivery-option"
                    :class="{ selected: selectedDelivery === option.id }"
                    @click="selectedDelivery = option.id"
                  >
                    <div class="delivery-icon">{{ option.icon }}</div>
                    <div class="delivery-info">
                      <h4>{{ option.name }}</h4>
                      <p>{{ option.description }}</p>
                      <div class="delivery-time">{{ option.time }}</div>
                    </div>
                    <div class="delivery-price">
                      {{ option.price > 0 ? formatCurrency(option.price) : 'Miễn phí' }}
                    </div>
                    <div class="delivery-radio">
                      <input
                        type="radio"
                        :value="option.id"
                        v-model="selectedDelivery"
                        :id="`delivery-${option.id}`"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button type="button" @click="goToPreviousStep" class="btn btn-secondary">
                  ← Quay lại giỏ hàng
                </button>
                <button type="submit" class="btn btn-primary" :disabled="submitting">
                  {{ submitting ? '🔄 Đang xử lý...' : 'Tiếp tục thanh toán →' }}
                </button>
              </div>
            </form>
          </div>

          <!-- Step 3: Payment -->
          <div v-if="currentStep === 3" class="checkout-section">
            <div class="section-header">
              <h2>💳 Phương thức thanh toán</h2>
              <p>Chọn phương thức thanh toán phù hợp với bạn</p>
            </div>

            <div class="payment-methods space-card">
              <div class="payment-tabs">
                <button
                  v-for="method in paymentMethods"
                  :key="method.id"
                  class="payment-tab"
                  :class="{ active: selectedPayment === method.id }"
                  @click="selectedPayment = method.id"
                >
                  <span class="payment-icon">{{ method.icon }}</span>
                  <span>{{ method.name }}</span>
                </button>
              </div>

              <!-- Credit Card Payment -->
              <div v-if="selectedPayment === 'card'" class="payment-form">
                <form @submit.prevent="processPayment">
                  <div class="form-group">
                    <label for="cardNumber" class="form-label">Số thẻ *</label>
                    <input
                      id="cardNumber"
                      v-model="paymentInfo.cardNumber"
                      type="text"
                      class="form-input"
                      placeholder="1234 5678 9012 3456"
                      maxlength="19"
                      @input="formatCardNumber"
                      required
                    />
                  </div>

                  <div class="form-row">
                    <div class="form-group">
                      <label for="expiryDate" class="form-label">Ngày hết hạn *</label>
                      <input
                        id="expiryDate"
                        v-model="paymentInfo.expiryDate"
                        type="text"
                        class="form-input"
                        placeholder="MM/YY"
                        maxlength="5"
                        @input="formatExpiryDate"
                        required
                      />
                    </div>

                    <div class="form-group">
                      <label for="cvv" class="form-label">CVV *</label>
                      <input
                        id="cvv"
                        v-model="paymentInfo.cvv"
                        type="text"
                        class="form-input"
                        placeholder="123"
                        maxlength="4"
                        required
                      />
                    </div>
                  </div>

                  <div class="form-group">
                    <label for="cardName" class="form-label">Tên trên thẻ *</label>
                    <input
                      id="cardName"
                      v-model="paymentInfo.cardName"
                      type="text"
                      class="form-input"
                      placeholder="NGUYEN VAN A"
                      required
                    />
                  </div>

                  <!-- Security Info -->
                  <div class="security-notice">
                    <div class="security-icon">🔒</div>
                    <div class="security-text">
                      <strong>Thanh toán bảo mật</strong>
                      <p>
                        Thông tin thẻ của bạn được mã hóa 256-bit SSL và không được lưu trữ trên hệ
                        thống của chúng tôi
                      </p>
                    </div>
                  </div>
                </form>
              </div>

              <!-- E-wallet Payment -->
              <div v-if="selectedPayment === 'ewallet'" class="payment-form">
                <div class="ewallet-options">
                  <div
                    v-for="wallet in ewalletOptions"
                    :key="wallet.id"
                    class="ewallet-option"
                    :class="{ selected: selectedEwallet === wallet.id }"
                    @click="selectedEwallet = wallet.id"
                  >
                    <div class="ewallet-logo">{{ wallet.icon }}</div>
                    <div class="ewallet-info">
                      <h4>{{ wallet.name }}</h4>
                      <p>{{ wallet.description }}</p>
                    </div>
                    <div class="ewallet-radio">
                      <input
                        type="radio"
                        :value="wallet.id"
                        v-model="selectedEwallet"
                        :id="`ewallet-${wallet.id}`"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Bank Transfer -->
              <div v-if="selectedPayment === 'bank'" class="payment-form">
                <div class="bank-info">
                  <h3>🏦 Thông tin chuyển khoản</h3>
                  <div class="bank-details">
                    <div class="bank-item">
                      <span class="bank-label">Ngân hàng:</span>
                      <span class="bank-value">Ngân hàng Vũ trụ (COSMIC BANK)</span>
                    </div>
                    <div class="bank-item">
                      <span class="bank-label">Số tài khoản:</span>
                      <span class="bank-value">1234567890123456</span>
                      <button @click="copyBankAccount" class="copy-btn">📋</button>
                    </div>
                    <div class="bank-item">
                      <span class="bank-label">Chủ tài khoản:</span>
                      <span class="bank-value">COSMIC MARKETPLACE</span>
                    </div>
                    <div class="bank-item">
                      <span class="bank-label">Nội dung chuyển khoản:</span>
                      <span class="bank-value">{{ orderCode }}</span>
                      <button @click="copyOrderCode" class="copy-btn">📋</button>
                    </div>
                  </div>
                  <div class="bank-note">
                    <p>
                      <strong>Lưu ý:</strong> Vui lòng chuyển khoản đúng số tiền và nội dung để đơn
                      hàng được xử lý tự động.
                    </p>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <button type="button" @click="goToPreviousStep" class="btn btn-secondary">
                  ← Quay lại thông tin
                </button>
                <button @click="processPayment" class="btn btn-primary" :disabled="processing">
                  {{
                    processing ? '🔄 Đang xử lý...' : `💳 Thanh toán ${formatCurrency(totalAmount)}`
                  }}
                </button>
              </div>
            </div>
          </div>

          <!-- Step 4: Success -->
          <div v-if="currentStep === 4" class="checkout-section">
            <div class="success-content space-card">
              <div class="success-animation">
                <div class="success-icon">✅</div>
                <div class="success-particles">
                  <span class="particle">🎉</span>
                  <span class="particle">✨</span>
                  <span class="particle">🌟</span>
                  <span class="particle">💫</span>
                </div>
              </div>

              <h2>🎉 Đặt hàng thành công!</h2>
              <p>
                Cảm ơn bạn đã mua hàng tại Cosmic Marketplace. Đơn hàng của bạn đang được xử lý.
              </p>

              <div class="order-summary">
                <div class="order-info">
                  <h3>📋 Thông tin đơn hàng</h3>
                  <div class="order-item">
                    <span>Mã đơn hàng:</span>
                    <span class="order-code">{{ createdOrder?.orderCode || orderCode }}</span>
                  </div>
                  <div class="order-item">
                    <span>Trạng thái:</span>
                    <span class="order-status">{{ createdOrder?.status || 'Đang xử lý' }}</span>
                  </div>
                  <div class="order-item">
                    <span>Tổng tiền:</span>
                    <span class="order-total">{{
                      formatCurrency(createdOrder?.totalAmount || totalAmount)
                    }}</span>
                  </div>
                  <div class="order-item">
                    <span>Phương thức thanh toán:</span>
                    <span>{{ getPaymentMethodName() }}</span>
                  </div>
                  <div class="order-item">
                    <span>Thời gian giao hàng dự kiến:</span>
                    <span>{{ getEstimatedDelivery() }}</span>
                  </div>
                  <div v-if="createdOrder?.createdAt" class="order-item">
                    <span>Ngày đặt hàng:</span>
                    <span>{{ new Date(createdOrder.createdAt).toLocaleDateString('vi-VN') }}</span>
                  </div>
                </div>
              </div>

              <div class="success-actions">
                <router-link to="/orders" class="btn btn-primary"> 📋 Xem đơn hàng </router-link>
                <router-link to="/products" class="btn btn-secondary">
                  🛍️ Tiếp tục mua sắm
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Summary Sidebar -->
        <div class="order-summary-sidebar" v-if="orderItems && orderItems.length > 0">
          <div class="summary-card space-card">
            <h3>📦 Đơn hàng của bạn</h3>

            <div class="order-items">
              <div
                v-for="item in currentStep === 4 && createdOrder?.items
                  ? createdOrder.items
                  : orderItems"
                :key="item.id"
                class="order-item-summary"
              >
                <div class="item-image">
                  <img
                    :src="item.product?.images?.[0] || '/placeholder-product.jpg'"
                    :alt="item.product?.name || ''"
                  />
                  <span class="item-quantity">{{ item.quantity }}</span>
                </div>
                <div class="item-details">
                  <h4>{{ item.product?.name || 'Sản phẩm không xác định' }}</h4>
                  <div class="item-price">
                    {{ formatCurrency((item.product?.price || 0) * item.quantity) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="summary-calculations">
              <div class="calc-row">
                <span>Tạm tính:</span>
                <span>{{ formatCurrency(subtotal) }}</span>
              </div>
              <div class="calc-row">
                <span>Phí giao hàng:</span>
                <span>{{ getDeliveryPrice() }}</span>
              </div>
              <div v-if="discount > 0" class="calc-row discount">
                <span>Giảm giá:</span>
                <span>-{{ formatCurrency(discount) }}</span>
              </div>
              <div class="calc-divider"></div>
              <div class="calc-row total">
                <span>Tổng cộng:</span>
                <span>{{ formatCurrency(totalAmount) }}</span>
              </div>
            </div>

            <!-- Trust Badges -->
            <div class="trust-badges">
              <div class="trust-badge">
                <span class="badge-icon">🔒</span>
                <span>Thanh toán bảo mật</span>
              </div>
              <div class="trust-badge">
                <span class="badge-icon">🚚</span>
                <span>Giao hàng tận nơi</span>
              </div>
              <div class="trust-badge">
                <span class="badge-icon">↩️</span>
                <span>Đổi trả 30 ngày</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { orderAPI, paymentAPI, productAPI } from '@/services/api'

export default {
  name: 'Checkout',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()

    // Reactive data
    const currentStep = ref(2) // Start at shipping info step
    const submitting = ref(false)
    const processing = ref(false)
    const orderCode = ref(`CM${Date.now()}`)
    const createdOrder = ref(null) // Store the created order from API

    // Form data
    const shippingInfo = ref({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      district: '',
      ward: '',
      notes: '',
    })

    const paymentInfo = ref({
      cardNumber: '',
      expiryDate: '',
      cvv: '',
      cardName: '',
    })

    const errors = ref({})
    const selectedDelivery = ref('standard')
    const selectedPayment = ref('card')
    const selectedEwallet = ref('momo')

    // Mock data
    const cities = ref([
      { code: 'HCM', name: 'TP. Hồ Chí Minh' },
      { code: 'HN', name: 'Hà Nội' },
      { code: 'DN', name: 'Đà Nẵng' },
    ])

    const districts = ref([])
    const wards = ref([])

    const deliveryOptions = ref([
      {
        id: 'express',
        name: 'Giao hàng siêu tốc',
        description: 'Giao trong 2-4 giờ',
        time: '2-4 giờ',
        price: 100000,
        icon: '🚀',
      },
      {
        id: 'standard',
        name: 'Giao hàng tiêu chuẩn',
        description: 'Giao trong 1-2 ngày',
        time: '1-2 ngày',
        price: 50000,
        icon: '🚚',
      },
      {
        id: 'free',
        name: 'Giao hàng miễn phí',
        description: 'Giao trong 3-5 ngày',
        time: '3-5 ngày',
        price: 0,
        icon: '🛸',
      },
    ])

    const paymentMethods = ref([
      { id: 'card', name: 'Thẻ tín dụng/ghi nợ', icon: '💳' },
      { id: 'ewallet', name: 'Ví điện tử', icon: '📱' },
      { id: 'bank', name: 'Chuyển khoản ngân hàng', icon: '🏦' },
    ])

    const ewalletOptions = ref([
      {
        id: 'momo',
        name: 'MoMo',
        description: 'Thanh toán qua ví MoMo',
        icon: '🟣',
      },
      {
        id: 'zalopay',
        name: 'ZaloPay',
        description: 'Thanh toán qua ZaloPay',
        icon: '🔵',
      },
      {
        id: 'vnpay',
        name: 'VNPay',
        description: 'Thanh toán qua VNPay',
        icon: '🟢',
      },
    ])

    // Lấy orderItems từ cartStore (giỏ hàng thực tế)
    const orderItems = computed(() => cartStore.items)

    const discount = ref(0)

    // Computed properties
    const subtotal = computed(() => {
      return orderItems.value.reduce(
        (total, item) => total + (item.product?.price || 0) * item.quantity,
        0
      )
    })

    const deliveryPrice = computed(() => {
      const option = deliveryOptions.value.find((opt) => opt.id === selectedDelivery.value)
      return option ? option.price : 0
    })

    const totalAmount = computed(() => {
      return subtotal.value + deliveryPrice.value - discount.value
    })

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
    }

    const loadDistricts = () => {
      // Mock loading districts based on city
      if (shippingInfo.value.city === 'HCM') {
        districts.value = [
          { code: 'Q1', name: 'Quận 1' },
          { code: 'Q3', name: 'Quận 3' },
          { code: 'Q7', name: 'Quận 7' },
        ]
      } else {
        districts.value = [
          { code: 'BA', name: 'Ba Đình' },
          { code: 'HK', name: 'Hoàn Kiếm' },
        ]
      }
      shippingInfo.value.district = ''
      shippingInfo.value.ward = ''
      wards.value = []
    }

    const loadWards = () => {
      // Mock loading wards based on district
      wards.value = [
        { code: 'P1', name: 'Phường 1' },
        { code: 'P2', name: 'Phường 2' },
        { code: 'P3', name: 'Phường 3' },
      ]
      shippingInfo.value.ward = ''
    }

    const validateShippingInfo = () => {
      errors.value = {}

      if (!shippingInfo.value.firstName.trim()) {
        errors.value.firstName = 'Vui lòng nhập họ'
      }

      if (!shippingInfo.value.lastName.trim()) {
        errors.value.lastName = 'Vui lòng nhập tên'
      }

      if (!shippingInfo.value.email.trim()) {
        errors.value.email = 'Vui lòng nhập email'
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(shippingInfo.value.email)) {
        errors.value.email = 'Email không hợp lệ'
      }

      if (!shippingInfo.value.phone.trim()) {
        errors.value.phone = 'Vui lòng nhập số điện thoại'
      } else if (!/^[0-9]{10,11}$/.test(shippingInfo.value.phone.replace(/\s/g, ''))) {
        errors.value.phone = 'Số điện thoại không hợp lệ'
      }

      if (!shippingInfo.value.address.trim()) {
        errors.value.address = 'Vui lòng nhập địa chỉ'
      }

      if (!shippingInfo.value.city) {
        errors.value.city = 'Vui lòng chọn thành phố'
      }

      if (!shippingInfo.value.district) {
        errors.value.district = 'Vui lòng chọn quận/huyện'
      }

      if (!shippingInfo.value.ward) {
        errors.value.ward = 'Vui lòng chọn phường/xã'
      }

      if (Object.keys(errors.value).length === 0) {
        goToNextStep()
      }
    }

    const formatCardNumber = (event) => {
      let value = event.target.value.replace(/\D/g, '')
      value = value.replace(/(\d{4})(?=\d)/g, '$1 ')
      paymentInfo.value.cardNumber = value
    }

    const formatExpiryDate = (event) => {
      let value = event.target.value.replace(/\D/g, '')
      if (value.length >= 2) {
        value = value.substring(0, 2) + '/' + value.substring(2, 4)
      }
      paymentInfo.value.expiryDate = value
    }

    const copyBankAccount = () => {
      navigator.clipboard.writeText('1234567890123456')
      alert('Đã sao chép số tài khoản!')
    }

    const copyOrderCode = () => {
      navigator.clipboard.writeText(orderCode.value)
      alert('Đã sao chép mã đơn hàng!')
    }

    const processPayment = async () => {
      if (selectedPayment.value === 'card') {
        if (
          !paymentInfo.value.cardNumber ||
          !paymentInfo.value.expiryDate ||
          !paymentInfo.value.cvv ||
          !paymentInfo.value.cardName
        ) {
          alert('Vui lòng điền đầy đủ thông tin thẻ')
          return
        }
      }

      processing.value = true

      try {
        // Create order first
        const orderData = {
          shippingAddress: JSON.stringify({
            firstName: shippingInfo.value.firstName,
            lastName: shippingInfo.value.lastName,
            email: shippingInfo.value.email,
            phone: shippingInfo.value.phone,
            address: shippingInfo.value.address,
            city: shippingInfo.value.city,
            district: shippingInfo.value.district,
            ward: shippingInfo.value.ward,
            notes: shippingInfo.value.notes,
            deliveryMethod: selectedDelivery.value,
          }),
          billingAddress: JSON.stringify({
            firstName: shippingInfo.value.firstName,
            lastName: shippingInfo.value.lastName,
            email: shippingInfo.value.email,
            phone: shippingInfo.value.phone,
            address: shippingInfo.value.address,
            city: shippingInfo.value.city,
            district: shippingInfo.value.district,
            ward: shippingInfo.value.ward,
          }),
        }

        console.log('🔄 Creating order...')
        const orderResponse = await orderAPI.create(
          orderData.shippingAddress,
          orderData.billingAddress
        )
        const order = orderResponse.data

        console.log('✅ Order created:', order)

        // Store the created order
        createdOrder.value = order
        orderCode.value = order.orderCode || order.id // Use real order code from API

        // Process payment based on selected method
        if (selectedPayment.value === 'card') {
          // Create payment intent for card payment
          const paymentIntentResponse = await paymentAPI.createPaymentIntent(
            order.id,
            totalAmount.value
          )

          console.log('✅ Payment intent created:', paymentIntentResponse.data)

          // Confirm payment (in real implementation, this would integrate with payment gateway)
          const confirmResponse = await paymentAPI.confirmPayment(
            paymentIntentResponse.data.paymentIntentId
          )

          console.log('✅ Payment confirmed:', confirmResponse.data)
        } else if (selectedPayment.value === 'ewallet') {
          // Handle e-wallet payment
          const paymentIntentResponse = await paymentAPI.createPaymentIntent(
            order.id,
            totalAmount.value
          )

          console.log('✅ E-wallet payment intent created:', paymentIntentResponse.data)
        } else if (selectedPayment.value === 'bank') {
          // Handle bank transfer
          const paymentIntentResponse = await paymentAPI.createPaymentIntent(
            order.id,
            totalAmount.value
          )

          console.log('✅ Bank transfer payment intent created:', paymentIntentResponse.data)
        }

        // Clear cart after successful payment
        await cartStore.clearCart()

        // Go to success step
        currentStep.value = 4
      } catch (error) {
        console.error('❌ Payment error:', error)
        const errorMessage =
          error.response?.data?.message || 'Có lỗi xảy ra khi thanh toán. Vui lòng thử lại.'
        alert(errorMessage)
      } finally {
        processing.value = false
      }
    }

    const goToNextStep = () => {
      if (currentStep.value < 4) {
        currentStep.value++
      }
    }

    const goToPreviousStep = () => {
      if (currentStep.value === 2) {
        router.push('/cart')
      } else if (currentStep.value > 1) {
        currentStep.value--
      }
    }

    const getDeliveryPrice = () => {
      const option = deliveryOptions.value.find((opt) => opt.id === selectedDelivery.value)
      return option && option.price > 0 ? formatCurrency(option.price) : 'Miễn phí'
    }

    const getPaymentMethodName = () => {
      const method = paymentMethods.value.find((m) => m.id === selectedPayment.value)
      return method ? method.name : ''
    }

    const getEstimatedDelivery = () => {
      const option = deliveryOptions.value.find((opt) => opt.id === selectedDelivery.value)
      if (option) {
        const days = option.id === 'express' ? 1 : option.id === 'standard' ? 2 : 5
        const date = new Date()
        date.setDate(date.getDate() + days)
        return date.toLocaleDateString('vi-VN')
      }
      return ''
    }

    // Lifecycle
    onMounted(async () => {
      await cartStore.loadCart()
      // Join product info for each cart item if missing
      const items = cartStore.items
      for (const item of items) {
        if (!item.product && item.productId) {
          try {
            const res = await productAPI.getById(item.productId)
            item.product = res.data
          } catch (e) {
            item.product = { name: 'Không tìm thấy', price: 0, images: [] }
          }
        }
      }
    })

    return {
      currentStep,
      submitting,
      processing,
      orderCode,
      createdOrder,
      shippingInfo,
      paymentInfo,
      errors,
      selectedDelivery,
      selectedPayment,
      selectedEwallet,
      cities,
      districts,
      wards,
      deliveryOptions,
      paymentMethods,
      ewalletOptions,
      orderItems,
      discount,
      subtotal,
      deliveryPrice,
      totalAmount,
      formatCurrency,
      loadDistricts,
      loadWards,
      validateShippingInfo,
      formatCardNumber,
      formatExpiryDate,
      copyBankAccount,
      copyOrderCode,
      processPayment,
      goToNextStep,
      goToPreviousStep,
      getDeliveryPrice,
      getPaymentMethodName,
      getEstimatedDelivery,
    }
  },
}
</script>

<style scoped>
.checkout-page {
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
  margin-bottom: 2rem;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.checkout-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  max-width: 600px;
  margin: 0 auto;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  position: relative;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.step.active {
  opacity: 1;
  color: var(--text-accent);
}

.step.completed {
  opacity: 1;
  color: #10b981;
}

.step-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(26, 26, 46, 0.8);
  border: 2px solid rgba(0, 212, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  transition: all 0.3s ease;
}

.step.active .step-icon {
  background: var(--aurora-gradient);
  border-color: var(--text-accent);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.step.completed .step-icon {
  background: #10b981;
  border-color: #10b981;
}

.step span {
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.step-line {
  width: 60px;
  height: 2px;
  background: rgba(0, 212, 255, 0.3);
  margin: 0 1rem;
}

.checkout-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 3rem;
  align-items: start;
}

.checkout-main {
  display: flex;
  flex-direction: column;
}

.checkout-section {
  margin-bottom: 2rem;
}

.section-header {
  margin-bottom: 2rem;
}

.section-header h2 {
  color: var(--text-accent);
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.section-header p {
  color: var(--text-secondary);
  opacity: 0.9;
}

.shipping-form {
  padding: 2rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-row.three-cols {
  grid-template-columns: 1fr 1fr 1fr;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
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
  background: rgba(26, 26, 46, 0.9);
}

.form-input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
}

.form-input::placeholder {
  color: rgba(184, 198, 219, 0.6);
}

.error-message {
  color: #ef4444;
  font-size: 0.8rem;
  margin-top: 0.25rem;
  display: block;
}

.delivery-options {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.delivery-options h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.delivery-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.delivery-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delivery-option:hover,
.delivery-option.selected {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.delivery-icon {
  font-size: 1.5rem;
  flex: none;
}

.delivery-info {
  flex: 1;
}

.delivery-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.delivery-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.delivery-time {
  color: var(--text-accent);
  font-size: 0.8rem;
  font-weight: 500;
}

.delivery-price {
  font-weight: 600;
  color: var(--text-accent);
  min-width: 100px;
  text-align: right;
}

.delivery-radio {
  flex: none;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.payment-methods {
  padding: 2rem;
}

.payment-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.payment-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.payment-tab:hover,
.payment-tab.active {
  color: var(--text-accent);
  border-bottom-color: var(--text-accent);
}

.payment-icon {
  font-size: 1.2rem;
}

.payment-form {
  margin-top: 2rem;
}

.security-notice {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1.5rem;
}

.security-icon {
  font-size: 1.5rem;
  color: #10b981;
  flex: none;
}

.security-text strong {
  color: #10b981;
  display: block;
  margin-bottom: 0.25rem;
}

.security-text p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.ewallet-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.ewallet-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ewallet-option:hover,
.ewallet-option.selected {
  border-color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

.ewallet-logo {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
}

.ewallet-info {
  flex: 1;
}

.ewallet-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.ewallet-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.ewallet-radio {
  flex: none;
}

.bank-info {
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  padding: 1.5rem;
}

.bank-info h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
}

.bank-details {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.bank-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.bank-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.bank-value {
  color: var(--text-primary);
  font-weight: 600;
  flex: 1;
  text-align: right;
  margin-right: 0.5rem;
}

.copy-btn {
  background: var(--aurora-gradient);
  border: none;
  color: white;
  padding: 0.4rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.copy-btn:hover {
  transform: scale(1.05);
}

.bank-note {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  padding: 1rem;
}

.bank-note p {
  color: #f59e0b;
  margin: 0;
  font-size: 0.9rem;
}

.success-content {
  text-align: center;
  padding: 3rem;
}

.success-animation {
  position: relative;
  margin-bottom: 2rem;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon {
  font-size: 4rem;
  animation: bounce 2s ease-in-out infinite;
}

.success-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.particle {
  position: absolute;
  font-size: 1.5rem;
  animation: float 3s ease-in-out infinite;
}

.particle:nth-child(1) {
  top: 20%;
  left: 20%;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 30%;
  right: 25%;
  animation-delay: 0.5s;
}

.particle:nth-child(3) {
  bottom: 25%;
  left: 30%;
  animation-delay: 1s;
}

.particle:nth-child(4) {
  bottom: 20%;
  right: 20%;
  animation-delay: 1.5s;
}

@keyframes bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-15px) rotate(5deg);
    opacity: 1;
  }
}

.success-content h2 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 2rem;
}

.success-content > p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.order-summary {
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
}

.order-info h3 {
  color: var(--text-accent);
  margin-bottom: 1rem;
}

.order-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
}

.order-code {
  color: var(--text-accent);
  font-weight: 600;
  font-family: monospace;
}

.order-status {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 1.2rem;
}

.order-total {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 1.2rem;
}

.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.order-summary-sidebar {
  position: sticky;
  top: 2rem;
}

.summary-card {
  padding: 1.5rem;
}

.summary-card h3 {
  color: var(--text-accent);
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
}

.order-items {
  margin-bottom: 1.5rem;
}

.order-item-summary {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.order-item-summary:last-child {
  border-bottom: none;
}

.item-image {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex: none;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-quantity {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--accent-gradient);
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 0.7rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.item-details {
  flex: 1;
}

.item-details h4 {
  color: var(--text-primary);
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.item-price {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 0.9rem;
}

.summary-calculations {
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding-top: 1rem;
}

.calc-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
}

.calc-row.discount {
  color: #10b981;
}

.calc-row.total {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0;
}

.calc-row.total span:last-child {
  color: var(--text-accent);
  font-size: 1.3rem;
}

.calc-divider {
  height: 1px;
  background: rgba(0, 212, 255, 0.2);
  margin: 1rem 0;
}

.trust-badges {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.trust-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.trust-badge:last-child {
  margin-bottom: 0;
}

.badge-icon {
  flex: none;
  color: #10b981;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .checkout-content {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .order-summary-sidebar {
    position: static;
    order: -1;
  }
}

@media (max-width: 768px) {
  .checkout-page {
    padding: 1rem 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .checkout-steps {
    gap: 0.5rem;
  }

  .step span {
    font-size: 0.7rem;
  }

  .step-line {
    width: 30px;
    margin: 0 0.5rem;
  }

  .shipping-form,
  .payment-methods,
  .summary-card {
    padding: 1.5rem;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .form-row.three-cols {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .delivery-option {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .delivery-price {
    text-align: left;
    min-width: auto;
  }

  .payment-tabs {
    flex-direction: column;
    gap: 0;
  }

  .success-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .checkout-steps {
    flex-wrap: wrap;
    gap: 1rem;
    justify-content: space-around;
  }

  .step-line {
    display: none;
  }

  .step-icon {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .bank-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .bank-value {
    text-align: left;
    margin-right: 0;
    word-break: break-all;
  }

  .success-content {
    padding: 2rem 1rem;
  }

  .success-icon {
    font-size: 3rem;
  }
}
</style>
