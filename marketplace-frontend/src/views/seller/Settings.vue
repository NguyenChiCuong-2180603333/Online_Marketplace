<template>
  <div class="seller-settings-page">
    <header class="page-header">
      <h1>⚙️ Cài đặt Seller</h1>
      <p>Quản lý thông tin cửa hàng và cài đặt bán hàng</p>
    </header>

    <!-- Settings Navigation -->
    <div class="settings-nav">
      <button 
        v-for="tab in settingsTabs" 
        :key="tab.value"
        class="nav-tab"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
      </button>
    </div>

    <!-- Settings Content -->
    <div class="settings-content">
      <!-- Store Information -->
      <div v-show="activeTab === 'store'" class="settings-section">
        <h3>🏪 Thông tin cửa hàng</h3>
        <div class="settings-form">
          <div class="form-group">
            <label>Tên cửa hàng</label>
            <div class="input-wrapper">
              <input type="text" placeholder="Nhập tên cửa hàng..." class="settings-input" disabled />
              <span class="input-status">✏️</span>
            </div>
          </div>
          
          <div class="form-group">
            <label>Mô tả cửa hàng</label>
            <div class="input-wrapper">
              <textarea placeholder="Mô tả về cửa hàng của bạn..." class="settings-textarea" disabled></textarea>
              <span class="input-status">✏️</span>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Số điện thoại liên hệ</label>
              <div class="input-wrapper">
                <input type="tel" placeholder="0XXX XXX XXX" class="settings-input" disabled />
                <span class="input-status">✏️</span>
              </div>
            </div>
            
            <div class="form-group">
              <label>Email hỗ trợ</label>
              <div class="input-wrapper">
                <input type="email" placeholder="support@example.com" class="settings-input" disabled />
                <span class="input-status">✏️</span>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>Logo cửa hàng</label>
            <div class="logo-upload">
              <div class="upload-area">
                <div class="upload-placeholder">
                  <span class="upload-icon">🖼️</span>
                  <p>Click để upload logo</p>
                  <small>PNG, JPG tối đa 2MB</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Settings -->
      <div v-show="activeTab === 'payment'" class="settings-section">
        <h3>💳 Cài đặt thanh toán</h3>
        <div class="settings-form">
          <div class="form-group">
            <label>Phương thức nhận tiền</label>
            <div class="payment-methods">
              <div class="payment-option">
                <input type="radio" id="bank" name="payment" disabled />
                <label for="bank" class="payment-label">
                  <span class="payment-icon">🏦</span>
                  <span class="payment-text">Chuyển khoản ngân hàng</span>
                </label>
              </div>
              <div class="payment-option">
                <input type="radio" id="wallet" name="payment" disabled />
                <label for="wallet" class="payment-label">
                  <span class="payment-icon">💳</span>
                  <span class="payment-text">Ví điện tử</span>
                </label>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>Thông tin ngân hàng</label>
            <div class="bank-info">
              <div class="form-row">
                <div class="form-group">
                  <label>Tên ngân hàng</label>
                  <select class="settings-select" disabled>
                    <option>Chọn ngân hàng</option>
                    <option>Vietcombank</option>
                    <option>Techcombank</option>
                    <option>BIDV</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Số tài khoản</label>
                  <input type="text" placeholder="XXXX XXXX XXXX" class="settings-input" disabled />
                </div>
              </div>
              <div class="form-group">
                <label>Tên chủ tài khoản</label>
                <input type="text" placeholder="NGUYEN VAN A" class="settings-input" disabled />
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>Chu kỳ thanh toán</label>
            <select class="settings-select" disabled>
              <option>Hàng tuần</option>
              <option>Hàng tháng</option>
              <option>Theo yêu cầu</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Shipping Settings -->
      <div v-show="activeTab === 'shipping'" class="settings-section">
        <h3>🚚 Cài đặt giao hàng</h3>
        <div class="settings-form">
          <div class="form-group">
            <label>Phí giao hàng mặc định</label>
            <div class="shipping-fees">
              <div class="fee-item">
                <span class="fee-label">Trong thành phố</span>
                <input type="number" placeholder="30000" class="fee-input" disabled />
                <span class="fee-unit">VND</span>
              </div>
              <div class="fee-item">
                <span class="fee-label">Ngoại thành</span>
                <input type="number" placeholder="50000" class="fee-input" disabled />
                <span class="fee-unit">VND</span>
              </div>
              <div class="fee-item">
                <span class="fee-label">Miễn phí từ</span>
                <input type="number" placeholder="500000" class="fee-input" disabled />
                <span class="fee-unit">VND</span>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>Thời gian xử lý đơn hàng</label>
            <select class="settings-select" disabled>
              <option>1-2 ngày làm việc</option>
              <option>2-3 ngày làm việc</option>
              <option>3-5 ngày làm việc</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Đối tác giao hàng</label>
            <div class="shipping-partners">
              <div class="partner-option">
                <input type="checkbox" id="ghn" disabled />
                <label for="ghn" class="partner-label">
                  <span class="partner-logo">📦</span>
                  <span class="partner-name">Giao Hàng Nhanh</span>
                </label>
              </div>
              <div class="partner-option">
                <input type="checkbox" id="ghtk" disabled />
                <label for="ghtk" class="partner-label">
                  <span class="partner-logo">🚚</span>
                  <span class="partner-name">Giao Hàng Tiết Kiệm</span>
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Notifications -->
      <div v-show="activeTab === 'notifications'" class="settings-section">
        <h3>🔔 Cài đặt thông báo</h3>
        <div class="settings-form">
          <div class="notification-groups">
            <div class="notification-group">
              <h4>📧 Email</h4>
              <div class="notification-options">
                <div class="notification-item">
                  <label class="notification-label">
                    <input type="checkbox" disabled checked />
                    <span class="checkmark"></span>
                    <span class="notification-text">Đơn hàng mới</span>
                  </label>
                </div>
                <div class="notification-item">
                  <label class="notification-label">
                    <input type="checkbox" disabled />
                    <span class="checkmark"></span>
                    <span class="notification-text">Đánh giá mới</span>
                  </label>
                </div>
                <div class="notification-item">
                  <label class="notification-label">
                    <input type="checkbox" disabled />
                    <span class="checkmark"></span>
                    <span class="notification-text">Tin nhắn từ khách hàng</span>
                  </label>
                </div>
              </div>
            </div>
            
            <div class="notification-group">
              <h4>📱 Push Notifications</h4>
              <div class="notification-options">
                <div class="notification-item">
                  <label class="notification-label">
                    <input type="checkbox" disabled checked />
                    <span class="checkmark"></span>
                    <span class="notification-text">Đơn hàng khẩn cấp</span>
                  </label>
                </div>
                <div class="notification-item">
                  <label class="notification-label">
                    <input type="checkbox" disabled />
                    <span class="checkmark"></span>
                    <span class="notification-text">Sản phẩm hết hàng</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Security -->
      <div v-show="activeTab === 'security'" class="settings-section">
        <h3>🔒 Bảo mật</h3>
        <div class="settings-form">
          <div class="security-options">
            <div class="security-item">
              <div class="security-info">
                <h4>Xác thực hai yếu tố (2FA)</h4>
                <p>Tăng cường bảo mật cho tài khoản seller</p>
              </div>
              <button class="btn-toggle" disabled>
                <span class="toggle-slider"></span>
              </button>
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>Đăng nhập từ thiết bị mới</h4>
                <p>Gửi email thông báo khi có đăng nhập từ thiết bị lạ</p>
              </div>
              <button class="btn-toggle active" disabled>
                <span class="toggle-slider"></span>
              </button>
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>Phiên đăng nhập</h4>
                <p>Quản lý các thiết bị đang đăng nhập</p>
              </div>
              <button class="btn-view-sessions" disabled>Xem phiên</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Save Button -->
    <div class="settings-actions">
      <button class="btn-save" disabled>
        💾 Lưu thay đổi
      </button>
      <button class="btn-reset" disabled>
        🔄 Khôi phục mặc định
      </button>
    </div>

    <!-- Coming Soon Notice -->
    <div class="coming-soon-notice">
      <div class="notice-content">
        <h3>🚀 Cài đặt Seller đang phát triển</h3>
        <p>Các tính năng cài đặt sẽ có trong các phase tiếp theo:</p>
        <div class="features-grid">
          <div class="feature-group">
            <h4>🏪 Quản lý cửa hàng</h4>
            <div class="feature-item">• Tùy chỉnh giao diện cửa hàng</div>
            <div class="feature-item">• Upload banner và logo</div>
            <div class="feature-item">• SEO settings</div>
            <div class="feature-item">• Social media integration</div>
          </div>
          <div class="feature-group">
            <h4>💳 Thanh toán</h4>
            <div class="feature-item">• Multiple payment methods</div>
            <div class="feature-item">• Auto payout scheduling</div>
            <div class="feature-item">• Tax calculations</div>
            <div class="feature-item">• Invoice generation</div>
          </div>
          <div class="feature-group">
            <h4>🚚 Logistics</h4>
            <div class="feature-item">• Multi-carrier shipping</div>
            <div class="feature-item">• Real-time tracking</div>
            <div class="feature-item">• Bulk printing labels</div>
            <div class="feature-item">• Return management</div>
          </div>
          <div class="feature-group">
            <h4>🔧 Advanced</h4>
            <div class="feature-item">• API access & webhooks</div>
            <div class="feature-item">• Third-party integrations</div>
            <div class="feature-item">• Advanced analytics</div>
            <div class="feature-item">• White-label options</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('store')

const settingsTabs = [
  { value: 'store', label: 'Cửa hàng', icon: '🏪' },
  { value: 'payment', label: 'Thanh toán', icon: '💳' },
  { value: 'shipping', label: 'Giao hàng', icon: '🚚' },
  { value: 'notifications', label: 'Thông báo', icon: '🔔' },
  { value: 'security', label: 'Bảo mật', icon: '🔒' }
]
</script>

<style scoped>
.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.page-header h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: var(--text-secondary, #a0aec0);
  font-size: 1rem;
}

.settings-nav {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: var(--text-secondary, #a0aec0);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-size: 0.9rem;
}

.nav-tab:hover, .nav-tab.active {
  background: rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
}

.tab-icon {
  font-size: 1.1rem;
}

.settings-content {
  min-height: 400px;
}

.settings-section {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.settings-section h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 2rem;
  font-size: 1.2rem;
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-group label {
  color: var(--text-secondary, #a0aec0);
  font-weight: 500;
  font-size: 0.9rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.input-wrapper {
  position: relative;
}

.settings-input, .settings-textarea, .settings-select {
  width: 100%;
  padding: 0.75rem 1rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary, #ffffff);
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.settings-input:disabled, .settings-textarea:disabled, .settings-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.settings-textarea {
  resize: vertical;
  min-height: 100px;
}

.input-status {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-accent, #00d4ff);
  cursor: pointer;
}

.logo-upload {
  margin-top: 0.5rem;
}

.upload-area {
  border: 2px dashed rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.upload-icon {
  font-size: 2rem;
}

.upload-placeholder p {
  color: var(--text-primary, #ffffff);
  margin: 0;
}

.upload-placeholder small {
  color: var(--text-secondary, #a0aec0);
}

.payment-methods, .shipping-partners {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.payment-option, .partner-option {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.payment-label, .partner-label {
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  flex: 1;
  transition: all 0.3s ease;
}

.payment-label:hover, .partner-label:hover {
  background: rgba(0, 212, 255, 0.1);
}

.payment-icon, .partner-logo {
  font-size: 1.5rem;
}

.payment-text, .partner-name {
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

.bank-info {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 1.5rem;
  margin-top: 1rem;
}

.shipping-fees {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.fee-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.fee-label {
  flex: 1;
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

.fee-input {
  width: 120px;
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 6px;
  color: var(--text-primary, #ffffff);
  text-align: right;
}

.fee-unit {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  min-width: 40px;
}

.notification-groups {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.notification-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
}

.notification-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.notification-item {
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.notification-label {
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
}

.notification-text {
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

.security-options {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.security-info h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 0.5rem;
}

.security-info p {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
  margin: 0;
}

.btn-toggle {
  width: 50px;
  height: 24px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-toggle.active {
  background: var(--text-accent, #00d4ff);
}

.toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.btn-toggle.active .toggle-slider {
  transform: translateX(24px);
}

.btn-view-sessions {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-accent, #00d4ff);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.settings-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  margin-bottom: 2rem;
}

.btn-save, .btn-reset {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-save {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  border: none;
}

.btn-reset {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.btn-save:disabled, .btn-reset:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.coming-soon-notice {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
}

.notice-content h3 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 1rem;
}

.notice-content p {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  text-align: left;
}

.feature-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
  font-size: 1rem;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  font-size: 0.9rem;
}

.feature-item:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .settings-nav {
    flex-wrap: wrap;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .settings-actions {
    flex-direction: column;
  }

  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .fee-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .fee-input {
    width: 100%;
  }
}
</style>