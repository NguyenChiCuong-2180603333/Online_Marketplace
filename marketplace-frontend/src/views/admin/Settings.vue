<template>
  <div class="admin-settings">
    <div class="page-header">
      <h1>Cài đặt hệ thống</h1>
      <button @click="saveAllSettings" class="btn btn-primary" :disabled="saving">
        {{ saving ? 'Đang lưu...' : '💾 Lưu tất cả' }}
      </button>
    </div>

    <!-- Settings Tabs -->
    <div class="settings-container">
      <div class="settings-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          :class="['tab-button', activeTab === tab.id ? 'active' : '']"
        >
          {{ tab.icon }} {{ tab.name }}
        </button>
      </div>

      <div class="settings-content">
        <!-- General Settings -->
        <div v-if="activeTab === 'general'" class="settings-section">
          <h2>⚙️ Cài đặt chung</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>Tên website</label>
              <input
                v-model="settings.general.siteName"
                type="text"
                class="form-input"
                placeholder="Cosmic Marketplace"
              />
            </div>

            <div class="setting-group">
              <label>Mô tả website</label>
              <textarea
                v-model="settings.general.siteDescription"
                rows="3"
                class="form-textarea"
                placeholder="Mô tả website..."
              ></textarea>
            </div>

            <div class="setting-group">
              <label>Logo URL</label>
              <input
                v-model="settings.general.logoUrl"
                type="url"
                class="form-input"
                placeholder="https://example.com/logo.png"
              />
            </div>

            <div class="setting-group">
              <label>Favicon URL</label>
              <input
                v-model="settings.general.faviconUrl"
                type="url"
                class="form-input"
                placeholder="https://example.com/favicon.ico"
              />
            </div>

            <div class="setting-group">
              <label>Email liên hệ</label>
              <input
                v-model="settings.general.contactEmail"
                type="email"
                class="form-input"
                placeholder="contact@example.com"
              />
            </div>

            <div class="setting-group">
              <label>Số điện thoại liên hệ</label>
              <input
                v-model="settings.general.contactPhone"
                type="tel"
                class="form-input"
                placeholder="+84 123 456 789"
              />
            </div>
          </div>
        </div>

        <!-- Business Settings -->
        <div v-if="activeTab === 'business'" class="settings-section">
          <h2>💼 Cài đặt kinh doanh</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>Tiền tệ mặc định</label>
              <select v-model="settings.business.defaultCurrency" class="form-select">
                <option value="VND">VND (Việt Nam Đồng)</option>
                <option value="USD">USD (US Dollar)</option>
                <option value="EUR">EUR (Euro)</option>
              </select>
            </div>

            <div class="setting-group">
              <label>Phí hoa hồng (%)</label>
              <input
                v-model="settings.business.commissionRate"
                type="number"
                min="0"
                max="100"
                step="0.1"
                class="form-input"
                placeholder="5.0"
              />
            </div>

            <div class="setting-group">
              <label>Phí vận chuyển mặc định</label>
              <input
                v-model="settings.business.defaultShippingFee"
                type="number"
                min="0"
                class="form-input"
                placeholder="30000"
              />
            </div>

            <div class="setting-group">
              <label>Giá trị đơn hàng tối thiểu để miễn phí vận chuyển</label>
              <input
                v-model="settings.business.freeShippingThreshold"
                type="number"
                min="0"
                class="form-input"
                placeholder="500000"
              />
            </div>

            <div class="setting-group">
              <label>Thời gian xử lý đơn hàng (ngày)</label>
              <input
                v-model="settings.business.orderProcessingTime"
                type="number"
                min="1"
                class="form-input"
                placeholder="3"
              />
            </div>

            <div class="setting-group">
              <label>Thời gian hoàn tiền (ngày)</label>
              <input
                v-model="settings.business.refundPeriod"
                type="number"
                min="1"
                class="form-input"
                placeholder="7"
              />
            </div>
          </div>
        </div>

        <!-- Security Settings -->
        <div v-if="activeTab === 'security'" class="settings-section">
          <h2>🔒 Cài đặt bảo mật</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>Thời gian hết hạn JWT (phút)</label>
              <input
                v-model="settings.security.jwtExpiration"
                type="number"
                min="15"
                class="form-input"
                placeholder="60"
              />
            </div>

            <div class="setting-group">
              <label>Số lần đăng nhập sai tối đa</label>
              <input
                v-model="settings.security.maxLoginAttempts"
                type="number"
                min="3"
                class="form-input"
                placeholder="5"
              />
            </div>

            <div class="setting-group">
              <label>Thời gian khóa tài khoản (phút)</label>
              <input
                v-model="settings.security.accountLockoutDuration"
                type="number"
                min="5"
                class="form-input"
                placeholder="30"
              />
            </div>

            <div class="setting-group">
              <label>Yêu cầu xác thực email</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.security.requireEmailVerification" type="checkbox" />
                  <span>Bắt buộc xác thực email khi đăng ký</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Yêu cầu xác thực 2FA cho admin</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.security.require2FAForAdmin" type="checkbox" />
                  <span>Bắt buộc xác thực 2FA cho tài khoản admin</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Giới hạn kích thước file upload (MB)</label>
              <input
                v-model="settings.security.maxFileSize"
                type="number"
                min="1"
                class="form-input"
                placeholder="10"
              />
            </div>
          </div>
        </div>

        <!-- Email Settings -->
        <div v-if="activeTab === 'email'" class="settings-section">
          <h2>📧 Cài đặt email</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>SMTP Host</label>
              <input
                v-model="settings.email.smtpHost"
                type="text"
                class="form-input"
                placeholder="smtp.gmail.com"
              />
            </div>

            <div class="setting-group">
              <label>SMTP Port</label>
              <input
                v-model="settings.email.smtpPort"
                type="number"
                class="form-input"
                placeholder="587"
              />
            </div>

            <div class="setting-group">
              <label>Email gửi</label>
              <input
                v-model="settings.email.fromEmail"
                type="email"
                class="form-input"
                placeholder="noreply@example.com"
              />
            </div>

            <div class="setting-group">
              <label>Tên người gửi</label>
              <input
                v-model="settings.email.fromName"
                type="text"
                class="form-input"
                placeholder="Cosmic Marketplace"
              />
            </div>

            <div class="setting-group">
              <label>Username SMTP</label>
              <input
                v-model="settings.email.smtpUsername"
                type="text"
                class="form-input"
                placeholder="your-email@gmail.com"
              />
            </div>

            <div class="setting-group">
              <label>Password SMTP</label>
              <input
                v-model="settings.email.smtpPassword"
                type="password"
                class="form-input"
                placeholder="your-app-password"
              />
            </div>

            <div class="setting-group">
              <label>Bật SSL/TLS</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.email.enableSSL" type="checkbox" />
                  <span>Sử dụng SSL/TLS cho kết nối SMTP</span>
                </label>
              </div>
            </div>
          </div>

          <div class="test-email-section">
            <h3>Test Email</h3>
            <div class="test-email-form">
              <input
                v-model="testEmail"
                type="email"
                placeholder="Email để test"
                class="form-input"
              />
              <button @click="sendTestEmail" class="btn btn-outline" :disabled="sendingTest">
                {{ sendingTest ? 'Đang gửi...' : 'Gửi test email' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Payment Settings -->
        <div v-if="activeTab === 'payment'" class="settings-section">
          <h2>💳 Cài đặt thanh toán</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>Stripe Public Key</label>
              <input
                v-model="settings.payment.stripePublicKey"
                type="text"
                class="form-input"
                placeholder="pk_test_..."
              />
            </div>

            <div class="setting-group">
              <label>Stripe Secret Key</label>
              <input
                v-model="settings.payment.stripeSecretKey"
                type="password"
                class="form-input"
                placeholder="sk_test_..."
              />
            </div>

            <div class="setting-group">
              <label>Webhook Secret</label>
              <input
                v-model="settings.payment.webhookSecret"
                type="password"
                class="form-input"
                placeholder="whsec_..."
              />
            </div>

            <div class="setting-group">
              <label>Môi trường Stripe</label>
              <select v-model="settings.payment.stripeEnvironment" class="form-select">
                <option value="test">Test</option>
                <option value="live">Live</option>
              </select>
            </div>

            <div class="setting-group">
              <label>Bật thanh toán COD</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.payment.enableCOD" type="checkbox" />
                  <span>Cho phép thanh toán khi nhận hàng</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Bật thanh toán chuyển khoản</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.payment.enableBankTransfer" type="checkbox" />
                  <span>Cho phép chuyển khoản ngân hàng</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Notification Settings -->
        <div v-if="activeTab === 'notifications'" class="settings-section">
          <h2>🔔 Cài đặt thông báo</h2>

          <div class="settings-grid">
            <div class="setting-group">
              <label>Bật thông báo email</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.enableEmail" type="checkbox" />
                  <span>Gửi thông báo qua email</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Bật thông báo đẩy</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.enablePush" type="checkbox" />
                  <span>Gửi thông báo đẩy</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Thông báo đơn hàng mới</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.newOrderNotification" type="checkbox" />
                  <span>Thông báo khi có đơn hàng mới</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Thông báo đánh giá mới</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.newReviewNotification" type="checkbox" />
                  <span>Thông báo khi có đánh giá mới</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Thông báo khuyến mãi</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.promotionNotification" type="checkbox" />
                  <span>Gửi thông báo khuyến mãi</span>
                </label>
              </div>
            </div>

            <div class="setting-group">
              <label>Thông báo bảo trì</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input v-model="settings.notifications.maintenanceNotification" type="checkbox" />
                  <span>Thông báo khi bảo trì hệ thống</span>
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { api } from '@/services/api'

export default {
  name: 'AdminSettings',
  setup() {
    // Reactive data
    const activeTab = ref('general')
    const saving = ref(false)
    const sendingTest = ref(false)
    const testEmail = ref('')

    // Settings data
    const settings = ref({
      general: {
        siteName: '',
        siteDescription: '',
        logoUrl: '',
        faviconUrl: '',
        contactEmail: '',
        contactPhone: '',
      },
      business: {
        defaultCurrency: 'VND',
        commissionRate: 5.0,
        defaultShippingFee: 30000,
        freeShippingThreshold: 500000,
        orderProcessingTime: 3,
        refundPeriod: 7,
      },
      security: {
        jwtExpiration: 60,
        maxLoginAttempts: 5,
        accountLockoutDuration: 30,
        requireEmailVerification: true,
        require2FAForAdmin: false,
        maxFileSize: 10,
      },
      email: {
        smtpHost: '',
        smtpPort: 587,
        fromEmail: '',
        fromName: '',
        smtpUsername: '',
        smtpPassword: '',
        enableSSL: true,
      },
      payment: {
        stripePublicKey: '',
        stripeSecretKey: '',
        webhookSecret: '',
        stripeEnvironment: 'test',
        enableCOD: true,
        enableBankTransfer: false,
      },
      notifications: {
        enableEmail: true,
        enablePush: true,
        newOrderNotification: true,
        newReviewNotification: true,
        promotionNotification: true,
        maintenanceNotification: true,
      },
    })

    // Tab configuration
    const tabs = [
      { id: 'general', name: 'Chung', icon: '⚙️' },
      { id: 'business', name: 'Kinh doanh', icon: '💼' },
      { id: 'security', name: 'Bảo mật', icon: '🔒' },
      { id: 'email', name: 'Email', icon: '📧' },
      { id: 'payment', name: 'Thanh toán', icon: '💳' },
      { id: 'notifications', name: 'Thông báo', icon: '🔔' },
    ]

    // Methods
    const loadSettings = async () => {
      try {
        const response = await api.get('/admin/settings')
        settings.value = { ...settings.value, ...response.data }
      } catch (error) {
        console.error('Error loading settings:', error)
      }
    }

    const saveAllSettings = async () => {
      saving.value = true
      try {
        await api.put('/admin/settings', settings.value)
        // Show success message
      } catch (error) {
        console.error('Error saving settings:', error)
        // Show error message
      } finally {
        saving.value = false
      }
    }

    const sendTestEmail = async () => {
      if (!testEmail.value) return

      sendingTest.value = true
      try {
        await api.post('/admin/settings/test-email', {
          email: testEmail.value,
        })
        // Show success message
        testEmail.value = ''
      } catch (error) {
        console.error('Error sending test email:', error)
        // Show error message
      } finally {
        sendingTest.value = false
      }
    }

    // Lifecycle
    onMounted(() => {
      loadSettings()
    })

    return {
      activeTab,
      saving,
      sendingTest,
      testEmail,
      settings,
      tabs,
      saveAllSettings,
      sendTestEmail,
    }
  },
}
</script>

<style scoped>
.admin-settings {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
}

.settings-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.tab-button {
  padding: 1rem 1.5rem;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
}

.tab-button:hover {
  background: #e9ecef;
  color: #333;
}

.tab-button.active {
  background: white;
  color: #667eea;
  border-bottom-color: #667eea;
}

.settings-content {
  padding: 2rem;
}

.settings-section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.setting-group {
  display: flex;
  flex-direction: column;
}

.setting-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.form-textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  resize: vertical;
}

.form-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
}

.checkbox-group {
  margin-top: 0.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: normal;
}

.checkbox-label input[type='checkbox'] {
  width: auto;
}

.test-email-section {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.test-email-section h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.test-email-form {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.test-email-form .form-input {
  flex: 1;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-outline {
  background: transparent;
  border: 1px solid #ddd;
  color: #666;
}

.btn-outline:hover {
  background: #f8f9fa;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .admin-settings {
    padding: 1rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .settings-tabs {
    flex-wrap: wrap;
  }

  .tab-button {
    flex: 1;
    min-width: 120px;
  }

  .settings-content {
    padding: 1rem;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .test-email-form {
    flex-direction: column;
  }
}
</style>
