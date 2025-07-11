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
                <label for="address" class="form-label">Địa chỉ đầy đủ *</label>
                <textarea
                  id="address"
                  v-model="shippingInfo.address"
                  class="form-input"
                  :class="{ error: errors.address }"
                  rows="3"
                  placeholder="Ví dụ: 123 Đường ABC, Phường 1, Quận 1, TP. Hồ Chí Minh"
                  required
                ></textarea>
                <div class="address-hint">
                  <span class="hint-icon">💡</span>
                  <span class="hint-text"
                    >Nhập đầy đủ: Số nhà, tên đường, phường/xã, quận/huyện, thành phố</span
                  >
                </div>
                <span v-if="errors.address" class="error-message">{{ errors.address }}</span>
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
                    v-for="option in availableDeliveryOptions"
                    :key="option.id"
                    class="delivery-option"
                    :class="{
                      selected: selectedDelivery === option.id,
                      disabled: !option.available,
                    }"
                    @click="option.available && (selectedDelivery = option.id)"
                  >
                    <div class="delivery-icon">{{ option.icon }}</div>
                    <div class="delivery-info">
                      <h4>{{ option.name }}</h4>
                      <p>{{ option.description }}</p>
                      <div class="delivery-time">{{ option.time }}</div>
                      <div class="delivery-partner">
                        <span class="partner-label">Đối tác:</span>
                        <span class="partner-name">{{ option.partners }}</span>
                      </div>
                      <div class="delivery-conditions">
                        <div
                          v-for="condition in option.conditions"
                          :key="condition"
                          class="condition-item"
                        >
                          <span class="condition-icon">ℹ️</span>
                          <span class="condition-text">{{ condition }}</span>
                        </div>
                      </div>
                      <div v-if="!option.available" class="unavailable-reason">
                        <span class="reason-icon">⚠️</span>
                        <span class="reason-text">
                          {{
                            option.id === 'express'
                              ? 'Chỉ áp dụng cho TP.HCM, Hà Nội và đơn hàng từ 200k'
                              : option.id === 'free'
                              ? 'Chỉ áp dụng cho đơn hàng từ 500k'
                              : 'Tạm thời không khả dụng'
                          }}
                        </span>
                      </div>
                    </div>
                    <div class="delivery-price">
                      <div v-if="option.finalPrice > option.price" class="price-breakdown">
                        <span class="original-price">{{ formatCurrency(option.price) }}</span>
                        <span class="surcharge"
                          >+{{ formatCurrency(option.finalPrice - option.price) }} phụ phí</span
                        >
                      </div>
                      <div class="final-price">
                        {{ option.finalPrice > 0 ? formatCurrency(option.finalPrice) : 'Miễn phí' }}
                      </div>
                    </div>
                    <div class="delivery-radio">
                      <input
                        type="radio"
                        :value="option.id"
                        v-model="selectedDelivery"
                        :id="`delivery-${option.id}`"
                        :disabled="!option.available"
                      />
                    </div>
                  </div>
                </div>

                <!-- Delivery Summary -->
                <div class="delivery-summary">
                  <div class="summary-item">
                    <span class="summary-label">Tạm tính:</span>
                    <span class="summary-value">{{ formatCurrency(subtotal) }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">Phí giao hàng:</span>
                    <span class="summary-value">{{ getDeliveryPrice() }}</span>
                  </div>
                  <div
                    v-if="
                      deliveryPrice > 0 &&
                      getDynamicDeliveryPrice(selectedDelivery) >
                        deliveryOptions.find((opt) => opt.id === selectedDelivery)?.price
                    "
                    class="summary-item surcharge-item"
                  >
                    <span class="summary-label">Phụ phí vùng xa:</span>
                    <span class="summary-value"
                      >+{{
                        formatCurrency(
                          deliveryPrice -
                            deliveryOptions.find((opt) => opt.id === selectedDelivery)?.price
                        )
                      }}</span
                    >
                  </div>
                  <div class="summary-item total">
                    <span class="summary-label">Tổng cộng:</span>
                    <span class="summary-value">{{ formatCurrency(totalAmount) }}</span>
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

              <!-- Stripe Card Payment -->
              <div v-if="selectedPayment === 'card'" class="payment-form">
                <form @submit.prevent="processPayment">
                  <div class="form-group">
                    <label class="form-label">Thông tin thẻ *</label>
                    <div id="stripe-card-element" class="stripe-card-element"></div>
                    <span v-if="cardError" class="error-message">{{ cardError }}</span>
                  </div>
                  <!-- Security Info -->
                  <div class="security-notice">
                    <div class="security-icon">🔒</div>
                    <div class="security-text">
                      <strong>Thanh toán bảo mật qua Stripe</strong>
                      <p>
                        Thông tin thẻ của bạn được mã hóa và xử lý an toàn qua Stripe. Chúng tôi
                        không lưu trữ thông tin thẻ.
                      </p>
                    </div>
                  </div>
                  <div class="form-actions">
                    <button type="button" @click="goToPreviousStep" class="btn btn-secondary">
                      ← Quay lại thông tin
                    </button>
                    <button
                      type="submit"
                      class="btn btn-primary"
                      :disabled="processing || !cardComplete"
                    >
                      {{
                        processing
                          ? '🔄 Đang xử lý...'
                          : `💳 Thanh toán ${formatCurrency(totalAmount)}`
                      }}
                    </button>
                  </div>
                </form>
              </div>

              <!-- COD Payment -->
              <div v-if="selectedPayment === 'cod'" class="payment-form">
                <div class="cod-info">
                  <div class="cod-icon">💵</div>
                  <div class="cod-text">
                    <strong>Thanh toán khi nhận hàng (COD)</strong>
                    <p>
                      Bạn sẽ thanh toán trực tiếp cho nhân viên giao hàng khi nhận được sản phẩm.
                    </p>
                  </div>
                </div>
                <div class="form-actions">
                  <button type="button" @click="goToPreviousStep" class="btn btn-secondary">
                    ← Quay lại thông tin
                  </button>
                  <button @click="processPayment" class="btn btn-primary" :disabled="processing">
                    {{ processing ? '🔄 Đang xử lý...' : 'Xác nhận đặt hàng' }}
                  </button>
                </div>
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
                    :src="item.productImage || '/placeholder-product.jpg'"
                    :alt="item.productName || ''"
                  />
                  <span class="item-quantity">{{ item.quantity }}</span>
                </div>
                <div class="item-details">
                  <h4>{{ item.productName || 'Sản phẩm không xác định' }}</h4>
                  <div class="item-price">
                    {{ formatCurrency((item.productPrice || 0) * item.quantity) }}
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

            <!-- Loyalty Points Section (Dùng điểm thưởng) -->
            <div class="loyalty-section">
              <label for="loyaltyPointsToUse" class="loyalty-label"> Dùng điểm thưởng: </label>
              <input
                id="loyaltyPointsToUse"
                type="number"
                min="0"
                :max="maxLoyaltyPoints"
                v-model.number="loyaltyPointsToUse"
                class="loyalty-input"
              />
              <span class="loyalty-max">
                (Tối đa: {{ (maxLoyaltyPoints || 0).toLocaleString('vi-VN') }})
              </span>
              <span v-if="loyaltyPointsToUse > 0" class="loyalty-discount">
                Giảm giá: <b>{{ loyaltyDiscount.toLocaleString('vi-VN') }}đ</b>
              </span>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import api, { orderAPI, paymentAPI, productAPI } from '@/services/api'
import { loadStripe } from '@stripe/stripe-js'
import { useNotificationStore } from '@/stores/notifications'

let stripe = null
let elements = null
let cardElement = null

export default {
  name: 'Checkout',
  setup() {
    const router = useRouter()
    const cartStore = useCartStore()
    const notification = useNotificationStore()

    const userAvailablePoints = ref(0)

    const currentStep = ref(2) 
    const submitting = ref(false)
    const processing = ref(false)
    const orderCode = ref(`CM${Date.now()}`)
    const createdOrder = ref(null) 

    const shippingInfo = ref({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
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

    // Mock data

    const deliveryOptions = ref([
      {
        id: 'express',
        name: 'Giao hàng siêu tốc',
        description: 'Giao trong 2-4 giờ',
        time: '2-4 giờ',
        price: 30000,
        icon: '🚀',
        conditions: ['Chỉ áp dụng cho TP.HCM, Hà Nội', 'Đơn hàng tối thiểu 200k'],
        partners: 'Grab Express, GoViet',
        available: true,
      },
      {
        id: 'standard',
        name: 'Giao hàng tiêu chuẩn',
        description: 'Giao trong 1-2 ngày',
        time: '1-2 ngày',
        price: 20000,
        icon: '🚚',
        conditions: ['Áp dụng toàn quốc'],
        partners: 'Giao Hang Nhanh, Viettel Post',
        available: true,
      },
      {
        id: 'free',
        name: 'Giao hàng miễn phí',
        description: 'Giao trong 3-5 ngày',
        time: '3-5 ngày',
        price: 0,
        icon: '🛸',
        conditions: ['Đơn hàng từ 500k trở lên'],
        partners: 'Giao Hang Tiet Kiem, GHTK',
        available: true,
      },
    ])

    const paymentMethods = ref([
      { id: 'card', name: 'Thẻ tín dụng/ghi nợ (Stripe)', icon: '💳' },
      { id: 'cod', name: 'Thanh toán khi nhận hàng (COD)', icon: '💵' },
    ])

    const orderItems = computed(() => cartStore.items)

    const discount = ref(0)

    const subtotal = computed(() => {
      return orderItems.value.reduce(
        (total, item) => total + (item.productPrice || 0) * item.quantity,
        0
      )
    })

    const deliveryPrice = computed(() => {
      return getDynamicDeliveryPrice(selectedDelivery.value)
    })

    const totalAmount = computed(() => {
      return subtotal.value + deliveryPrice.value - discount.value
    })

    // Delivery availability checks
    const canUseExpress = computed(() => {
      const address = shippingInfo.value.address.toLowerCase()
      const total = subtotal.value
      return (
        (address.includes('tp.hcm') ||
          address.includes('tp hcm') ||
          address.includes('tphcm') ||
          address.includes('tp. hồ chí minh') ||
          address.includes('tp ho chi minh') ||
          address.includes('hồ chí minh') ||
          address.includes('ho chi minh') ||
          address.includes('sài gòn') ||
          address.includes('hà nội')) &&
        total >= 200000
      )
    })

    const canUseFreeShipping = computed(() => {
      return subtotal.value >= 500000
    })

    const getDynamicDeliveryPrice = (optionId) => {
      const option = deliveryOptions.value.find((opt) => opt.id === optionId)
      if (!option) return 0

      let price = option.price
      const address = shippingInfo.value.address.toLowerCase()

      if (
        address.includes('miền núi') ||
        address.includes('tây nguyên') ||
        address.includes('đồng bằng sông cửu long') ||
        address.includes('miền trung')
      ) {
        price += 15000
      }

      if (
        address.includes('phú quốc') ||
        address.includes('côn đảo') ||
        address.includes('cù lao')
      ) {
        price += 30000
      }

      return price
    }

    const availableDeliveryOptions = computed(() => {
      return deliveryOptions.value.map((option) => {
        let available = option.available

        if (option.id === 'express') {
          available = canUseExpress.value
        } else if (option.id === 'free') {
          available = canUseFreeShipping.value
        }

        return {
          ...option,
          available,
          finalPrice: getDynamicDeliveryPrice(option.id),
        }
      })
    })

    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
      }).format(amount)
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
      } else if (shippingInfo.value.address.trim().length < 20) {
        errors.value.address = 'Địa chỉ phải có ít nhất 20 ký tự và bao gồm đầy đủ thông tin'
      }

      if (Object.keys(errors.value).length === 0) {
        goToNextStep()
      }
    }

    const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY)
    const cardComplete = ref(false)
    const cardError = ref('')
    const cardMounted = ref(false)

    const processPayment = async () => {
      processing.value = true
      try {
        if (selectedPayment.value === 'card') {
          const paymentIntentRes = await paymentAPI.createPaymentIntent(
            null, 
            finalTotalAmount.value
          )
          const clientSecret = paymentIntentRes.data.clientSecret
          stripe = await stripePromise
          const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
            payment_method: {
              card: cardElement,
              billing_details: {
                name: shippingInfo.value.firstName + ' ' + shippingInfo.value.lastName,
                email: shippingInfo.value.email,
              },
            },
          })
          if (error) {
            alert('Thanh toán thất bại: ' + error.message)
            processing.value = false
            return
          }
          // 2. Nếu thanh toán thành công, mới tạo đơn hàng
          const orderData = {
            shippingAddress: JSON.stringify({
              firstName: shippingInfo.value.firstName,
              lastName: shippingInfo.value.lastName,
              email: shippingInfo.value.email,
              phone: shippingInfo.value.phone,
              address: shippingInfo.value.address,
              notes: shippingInfo.value.notes,
              deliveryMethod: selectedDelivery.value,
            }),
            billingAddress: JSON.stringify({
              firstName: shippingInfo.value.firstName,
              lastName: shippingInfo.value.lastName,
              email: shippingInfo.value.email,
              phone: shippingInfo.value.phone,
              address: shippingInfo.value.address,
            }),
            paymentMethod: selectedPayment.value,
            shippingFee: deliveryPrice.value,
            loyaltyPointsToUse: loyaltyPointsToUse.value,
            paymentId: paymentIntent.id, 
          }
          const orderResponse = await orderAPI.create(orderData)
          const order = orderResponse.data
          createdOrder.value = order
          orderCode.value = order.orderCode || order.id
          alert('Thanh toán Stripe thành công!')
        } else if (selectedPayment.value === 'cod') {
          const orderData = {
            shippingAddress: JSON.stringify({
              firstName: shippingInfo.value.firstName,
              lastName: shippingInfo.value.lastName,
              email: shippingInfo.value.email,
              phone: shippingInfo.value.phone,
              address: shippingInfo.value.address,
              notes: shippingInfo.value.notes,
              deliveryMethod: selectedDelivery.value,
            }),
            billingAddress: JSON.stringify({
              firstName: shippingInfo.value.firstName,
              lastName: shippingInfo.value.lastName,
              email: shippingInfo.value.email,
              phone: shippingInfo.value.phone,
              address: shippingInfo.value.address,
            }),
            paymentMethod: selectedPayment.value,
            shippingFee: deliveryPrice.value,
            loyaltyPointsToUse: loyaltyPointsToUse.value,
          }
          const orderResponse = await orderAPI.create(orderData)
          const order = orderResponse.data
          createdOrder.value = order
          orderCode.value = order.orderCode || order.id
          alert('Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng.')
        }
        await cartStore.clearCart()
        currentStep.value = 4
        if (loyaltyPointsToUse.value > 0) {
          notification.success(
            `Đã sử dụng ${loyaltyPointsToUse.value.toLocaleString(
              'vi-VN'
            )} điểm thưởng để giảm giá đơn hàng!`
          )
        }
      } catch (error) {
        console.error('❌ Payment error:', error)
        const errorMessage =
          error.response?.data?.message || 'Có lỗi xảy ra khi thanh toán. Vui lòng thử lại.'
        alert(errorMessage)
        if (loyaltyPointsToUse.value > 0) {
          notification.error(
            'Sử dụng điểm thưởng thất bại. Vui lòng thử lại hoặc kiểm tra số điểm!'
          )
        }
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
      const price = getDynamicDeliveryPrice(selectedDelivery.value)
      return price > 0 ? formatCurrency(price) : 'Miễn phí'
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

    watch(selectedPayment, async (val, oldVal) => {
      if (currentStep.value === 3 && val === 'card') {
        await nextTick()
        if (document.getElementById('stripe-card-element')) {
          stripe = await stripePromise
          if (!elements) elements = stripe.elements()
          if (!cardElement) {
            cardElement = elements.create('card')
            cardElement.on('change', (event) => {
              cardComplete.value = event.complete
              cardError.value = event.error ? event.error.message : ''
            })
          }
          try {
            cardElement.unmount()
          } catch (e) {}
          cardElement.mount('#stripe-card-element')
          cardMounted.value = true
        }
      } else if (val !== 'card' && cardElement && cardMounted.value) {
        try {
          cardElement.unmount()
        } catch (e) {}
        cardMounted.value = false
      }
    })

    watch(currentStep, async (val, oldVal) => {
      if (val === 3 && selectedPayment.value === 'card') {
        await nextTick()
        if (document.getElementById('stripe-card-element')) {
          stripe = await stripePromise
          if (!elements) elements = stripe.elements()
          if (!cardElement) {
            cardElement = elements.create('card', {
              style: {
                base: {
                  color: '#ffffff',
                  fontSize: '16px',
                },
              },
            })
            cardElement.on('change', (event) => {
              cardComplete.value = event.complete
              cardError.value = event.error ? event.error.message : ''
            })
          }
          try {
            cardElement.unmount()
          } catch (e) {}
          cardElement.mount('#stripe-card-element')
          cardMounted.value = true
        }
      }
    })

    onMounted(async () => {
      await cartStore.loadCart()
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
      if (!cardMounted.value && document.getElementById('stripe-card-element')) {
        stripe = await stripePromise
        elements = stripe.elements()
        if (!cardElement) {
          cardElement = elements.create('card')
          cardElement.on('change', (event) => {
            cardComplete.value = event.complete
            cardError.value = event.error ? event.error.message : ''
          })
        }
        cardElement.mount('#stripe-card-element')
        cardMounted.value = true
      }

      try {
        const res = await api.getLoyaltyPoints()
        userAvailablePoints.value = res.data.points || 0
      } catch (e) {
        userAvailablePoints.value = 0
      }
    })

    const loyaltyPointsToUse = ref(0)
    const loyaltyPointsError = ref('')

    const maxLoyaltyPoints = computed(() => {
      return userAvailablePoints.value || 0
    })

    watch(loyaltyPointsToUse, (val) => {
      if (val < 0) loyaltyPointsToUse.value = 0
      if (val > maxLoyaltyPoints.value) loyaltyPointsToUse.value = maxLoyaltyPoints.value
    })

    const loyaltyDiscount = computed(() => {
      return Math.min(loyaltyPointsToUse.value, totalAmount.value)
    })

    const finalTotalAmount = computed(() => {
      return totalAmount.value - loyaltyDiscount.value
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

      deliveryOptions,
      availableDeliveryOptions,
      canUseExpress,
      canUseFreeShipping,
      paymentMethods,
      orderItems,
      discount,
      subtotal,
      deliveryPrice,
      totalAmount,
      formatCurrency,
      getDynamicDeliveryPrice,
      validateShippingInfo,
      processPayment,
      goToNextStep,
      goToPreviousStep,
      getDeliveryPrice,
      getPaymentMethodName,
      getEstimatedDelivery,
      cardComplete,
      cardError,
      cardMounted,
      loyaltyPointsToUse,
      loyaltyPointsError,
      maxLoyaltyPoints,
      loyaltyDiscount,
      finalTotalAmount,
      notification,
      userAvailablePoints,
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

.address-hint {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
}

.hint-icon {
  font-size: 1rem;
  color: var(--text-accent);
}

.hint-text {
  color: var(--text-secondary);
  font-size: 0.85rem;
  line-height: 1.4;
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

.delivery-option.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: rgba(0, 0, 0, 0.1);
}

.delivery-option.disabled:hover {
  border-color: rgba(0, 212, 255, 0.3);
  background: rgba(0, 0, 0, 0.1);
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
  min-width: 120px;
  text-align: right;
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
}

.original-price {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.surcharge {
  font-size: 0.75rem;
  color: #f59e0b;
  font-weight: 500;
}

.final-price {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-accent);
}

.delivery-partner {
  margin-top: 0.5rem;
  font-size: 0.8rem;
}

.partner-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.partner-name {
  color: var(--text-primary);
  margin-left: 0.25rem;
}

.delivery-conditions {
  margin-top: 0.5rem;
}

.condition-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-bottom: 0.25rem;
  font-size: 0.75rem;
}

.condition-icon {
  font-size: 0.7rem;
}

.condition-text {
  color: var(--text-secondary);
}

.unavailable-reason {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 6px;
}

.reason-icon {
  font-size: 0.8rem;
}

.reason-text {
  color: #ef4444;
  font-size: 0.8rem;
  font-weight: 500;
}

.delivery-radio {
  flex: none;
}

/* Delivery Summary */
.delivery-summary {
  margin-top: 2rem;
  padding: 1.5rem;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item.surcharge-item {
  color: #f59e0b;
  font-size: 0.9rem;
}

.summary-item.total {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 0.75rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  margin-top: 0.75rem;
}

.summary-item.total .summary-value {
  color: var(--text-accent);
  font-size: 1.2rem;
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

.cod-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}
.cod-icon {
  font-size: 2rem;
  color: #10b981;
}
.cod-text strong {
  color: #10b981;
  display: block;
  margin-bottom: 0.25rem;
}
.cod-text p {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin: 0;
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

/* Loyalty Points Section */
.loyalty-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.2rem 1rem;
  background: rgba(255, 215, 0, 0.13);
  border: 1.5px solid rgba(255, 215, 0, 0.35);
  border-radius: 12px;
  margin: 1.5rem 0;
  flex-wrap: wrap;
  justify-content: center;
}
.loyalty-label {
  color: var(--text-accent);
  font-weight: 700;
  font-size: 1.08rem;
  margin-right: 0.5rem;
  white-space: nowrap;
}
.loyalty-input {
  width: 90px;
  padding: 0.6rem 1rem;
  border: 1.5px solid var(--text-accent);
  border-radius: 8px;
  background: rgba(26, 26, 46, 0.92);
  color: var(--text-primary);
  font-size: 1.08rem;
  text-align: right;
  font-weight: 600;
  margin-right: 0.5rem;
  transition: border 0.2s, box-shadow 0.2s;
}
.loyalty-input:focus {
  outline: none;
  border-color: #ffd700;
  box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.18);
}
.loyalty-max {
  color: var(--text-secondary);
  font-size: 1.02rem;
  margin-right: 0.7rem;
  white-space: nowrap;
}
.loyalty-discount {
  color: #10b981;
  font-size: 1.08rem;
  font-weight: 600;
  margin-left: auto;
  white-space: nowrap;
}
@media (max-width: 600px) {
  .loyalty-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
    padding: 1rem 0.5rem;
  }
  .loyalty-label,
  .loyalty-input,
  .loyalty-max,
  .loyalty-discount {
    font-size: 1rem;
    margin: 0 0 0.2rem 0;
  }
  .loyalty-discount {
    margin-left: 0;
  }
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

  .cod-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .cod-value {
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

.stripe-card-element {
  padding: 0.75rem 1rem;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 1rem;
  margin-bottom: 0.5rem;
}
</style>
