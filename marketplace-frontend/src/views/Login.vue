<template>
  <div class="login-page">
    <div class="container">
      <div class="login-container">
        <div class="space-card login-card">
          <div class="login-header">
            <h1>🚀 Đăng nhập vào Cosmic Marketplace</h1>
            <p>Khám phá vũ trụ mua sắm cùng chúng tôi</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <!-- Main Error Display -->
            <div v-if="error" class="alert alert-error">
              {{ error }}
            </div>

            <!-- Success Message -->
            <div v-if="success" class="alert alert-success">
              {{ success }}
            </div>

            <div class="form-group">
              <label class="form-label">Email</label>
              <input
                v-model="credentials.email"
                type="email"
                class="form-input"
                :class="{ 'error-input': hasFieldError('email') || emailValidationError }"
                placeholder="Nhập email của bạn"
                required
                autocomplete="email"
                @blur="validateEmail"
                @input="clearEmailError"
              />
              <div
                v-if="hasFieldError('email') || emailValidationError"
                class="field-error-container"
              >
                <span class="error-icon">⚠️</span>
                <span class="field-error">
                  {{ getFieldError('email') || emailValidationError }}
                </span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Mật khẩu</label>
              <div class="password-input-wrapper">
                <input
                  v-model="credentials.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  :class="{ 'error-input': hasFieldError('password') || passwordValidationError }"
                  placeholder="Mật khẩu (chữ hoa, thường, số, ký tự đặc biệt)"
                  required
                  autocomplete="current-password"
                  @blur="validatePassword"
                  @input="clearPasswordError"
                />
                <button type="button" @click="togglePassword" class="password-toggle" tabindex="-1">
                  {{ showPassword ? '🙈' : '👁️' }}
                </button>
              </div>
              <div
                v-if="hasFieldError('password') || passwordValidationError"
                class="field-error-container"
              >
                <span class="error-icon">🔒</span>
                <span class="field-error">
                  {{ getFieldError('password') || passwordValidationError }}
                </span>
              </div>
            </div>

            <!-- Remember Me & Forgot Password -->
            <div class="form-options">
              <label class="remember-me-wrapper">
                <input v-model="rememberMe" type="checkbox" class="checkbox-input" />
                <span class="checkbox-custom"></span>
                <span class="checkbox-label">Ghi nhớ đăng nhập</span>
              </label>

              <button type="button" @click="showForgotPassword" class="forgot-password-link">
                Quên mật khẩu?
              </button>
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="loading">
              <span v-if="loading">🔄 Đang đăng nhập...</span>
              <span v-else>🚀 Đăng nhập</span>
            </button>
          </form>

          <div class="login-footer">
            <p>
              Chưa có tài khoản?
              <router-link to="/register" class="text-accent">Đăng ký ngay</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Forgot Password Modal -->
    <div v-if="showForgotModal" class="modal-overlay" @click="closeForgotModal">
      <div class="modal-content space-card" @click.stop>
        <div class="modal-header">
          <h3>🔑 Quên mật khẩu</h3>
          <button @click="closeForgotModal" class="modal-close">✕</button>
        </div>
        <form @submit.prevent="handleForgotPassword" class="forgot-form">
          <p class="modal-description">Nhập email của bạn để nhận liên kết đặt lại mật khẩu</p>
          <div class="form-group">
            <input
              v-model="forgotEmail"
              type="email"
              class="form-input"
              placeholder="Nhập email của bạn"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeForgotModal" class="btn btn-secondary">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="forgotLoading">
              {{ forgotLoading ? 'Đang gửi...' : 'Gửi email' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'Login',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const route = useRoute()

    const credentials = ref({
      email: '',
      password: '',
    })

    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const fieldErrors = ref({})
    const showPassword = ref(false)
    const rememberMe = ref(false)

    const showForgotModal = ref(false)
    const forgotEmail = ref('')
    const forgotLoading = ref(false)
    const emailValidationError = ref('')
    const passwordValidationError = ref('')

    const parseErrorResponse = (error) => {
      console.log('Login error object:', error)

      if (error.response?.data) {
        const errorData = error.response.data
        console.log('Login error data:', errorData)

        if (errorData.errors && typeof errorData.errors === 'object') {
          fieldErrors.value = { ...errorData.errors }

          const firstFieldError = Object.values(errorData.errors)[0]
          return firstFieldError || 'Dữ liệu không hợp lệ'
        }

        if (errorData.error) {
          const errorMsg = errorData.error

          if (errorMsg.includes('Tài khoản không tồn tại')) {
            return '❌ Tài khoản không tồn tại'
          }

          if (errorMsg.includes('Mật khẩu không đúng')) {
            return '❌ Mật khẩu không đúng'
          }

          if (errorMsg.includes('Tài khoản đã bị khóa')) {
            return '🔒 Tài khoản đã bị khóa. Vui lòng liên hệ hỗ trợ'
          }

          return errorMsg
        }

        if (errorData.message) {
          return errorData.message
        }

        if (typeof errorData === 'string') {
          return errorData
        }
      }

      return 'Đăng nhập thất bại. Vui lòng thử lại.'
    }

    const handleLogin = async () => {
      if (!validateEmail() || !validatePassword()) {
        return
      }

      loading.value = true
      error.value = ''
      success.value = ''
      fieldErrors.value = {}

      try {
        const loginData = {
          email: credentials.value.email.trim(),
          password: credentials.value.password,
        }

        await authStore.login(loginData)

        success.value = '✅ Đăng nhập thành công!'

        if (rememberMe.value) {
          localStorage.setItem('rememberMe', 'true')
          localStorage.setItem('rememberedEmail', credentials.value.email)
        } else {
          localStorage.removeItem('rememberMe')
          localStorage.removeItem('rememberedEmail')
        }

        setTimeout(() => {
          const redirectTo = route.query.redirect || '/'
          router.push(redirectTo)
        }, 1000)
      } catch (err) {
        error.value = parseErrorResponse(err)
        console.error('Login error:', err)
      } finally {
        loading.value = false
      }
    }

    const togglePassword = () => {
      showPassword.value = !showPassword.value
    }

    const showForgotPassword = () => {
      forgotEmail.value = credentials.value.email
      showForgotModal.value = true
    }

    const closeForgotModal = () => {
      showForgotModal.value = false
      forgotEmail.value = ''
    }

    const handleForgotPassword = async () => {
      forgotLoading.value = true

      try {
        alert('📧 Email đặt lại mật khẩu đã được gửi!')
        closeForgotModal()
      } catch (err) {
        alert('❌ Có lỗi xảy ra. Vui lòng thử lại.')
        console.error('Forgot password error:', err)
      } finally {
        forgotLoading.value = false
      }
    }

    const hasFieldError = (fieldName) => {
      return fieldErrors.value[fieldName]
    }

    const getFieldError = (fieldName) => {
      return fieldErrors.value[fieldName] || ''
    }

    const validateEmail = () => {
      const email = credentials.value.email.trim()

      if (!email) {
        emailValidationError.value = 'Vui lòng nhập email của bạn'
        return false
      }

      if (!email.includes('@')) {
        emailValidationError.value = `Vui lòng nhập '@' trong địa chỉ email. '${email}' thiếu '@'.`
        return false
      }

      const parts = email.split('@')
      if (parts.length !== 2 || !parts[1]) {
        emailValidationError.value = 'Email không hợp lệ. Vui lòng nhập đúng định dạng email.'
        return false
      }

      if (!parts[1].includes('.')) {
        emailValidationError.value = 'Email không hợp lệ. Thiếu phần domain (ví dụ: .com, .vn)'
        return false
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(email)) {
        emailValidationError.value = 'Email không đúng định dạng. Vui lòng kiểm tra lại.'
        return false
      }

      emailValidationError.value = ''
      return true
    }

    const clearEmailError = () => {
      emailValidationError.value = ''
    }

    const validatePassword = () => {
      const password = credentials.value.password

      if (!password) {
        passwordValidationError.value = 'Vui lòng nhập mật khẩu'
        return false
      }

      if (password.length < 6) {
        passwordValidationError.value = 'Mật khẩu phải có ít nhất 6 ký tự'
        return false
      }

      const hasUpperCase = /[A-Z]/.test(password)
      const hasLowerCase = /[a-z]/.test(password)
      const hasNumbers = /\d/.test(password)
      const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password)

      if (!hasUpperCase || !hasLowerCase || !hasNumbers || !hasSpecialChars) {
        passwordValidationError.value = 'Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt'
        return false
      }

      passwordValidationError.value = ''
      return true
    }

    const clearPasswordError = () => {
      passwordValidationError.value = ''
    }

    onMounted(() => {
      const remembered = localStorage.getItem('rememberMe')
      const rememberedEmail = localStorage.getItem('rememberedEmail')

      if (remembered === 'true' && rememberedEmail) {
        credentials.value.email = rememberedEmail
        rememberMe.value = true
      }
    })

    return {
      credentials,
      loading,
      error,
      success,
      fieldErrors,
      showPassword,
      rememberMe,
      showForgotModal,
      forgotEmail,
      forgotLoading,
      emailValidationError,
      passwordValidationError,
      handleLogin,
      togglePassword,
      showForgotPassword,
      closeForgotModal,
      handleForgotPassword,
      hasFieldError,
      getFieldError,
      validateEmail,
      clearEmailError,
      validatePassword,
      clearPasswordError,
    }
  },
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 40px 0;
}

.login-container {
  max-width: 450px;
  margin: 0 auto;
}

.login-card {
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-accent);
}

.login-header p {
  color: var(--text-secondary);
}

.login-form {
  margin-bottom: 24px;
}

/* Password Input with Toggle */
.password-input-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: var(--text-accent);
}

/* Form Options Row */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 1.5rem 0;
}

/* Remember Me Checkbox */
.remember-me-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 0.9rem;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 4px;
  margin-right: 8px;
  position: relative;
  background: transparent;
  transition: all 0.3s ease;
}

.checkbox-input:checked + .checkbox-custom {
  background: var(--text-accent);
  border-color: var(--text-accent);
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 12px;
  font-weight: bold;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.checkbox-label {
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.remember-me-wrapper:hover .checkbox-label {
  color: var(--text-primary);
}

/* Forgot Password Link */
.forgot-password-link {
  background: none;
  border: none;
  color: var(--text-accent);
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: none;
  transition: opacity 0.3s ease;
}

.forgot-password-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}

/* Footer */
.login-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.login-footer a {
  text-decoration: none;
  font-weight: 500;
}

.login-footer a:hover {
  text-decoration: underline;
}

/* Error Styling */
.error-input {
  border-color: #ff6b6b !important;
  background-color: rgba(255, 107, 107, 0.1) !important;
}

.field-error-container {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: 8px;
  color: #ff6b6b;
  animation: slideInDown 0.3s ease;
}

/* Different error types */
.field-error-container.warning {
  background: rgba(255, 193, 7, 0.1);
  border-color: rgba(255, 193, 7, 0.3);
  color: #ffc107;
}

.field-error-container.info {
  background: rgba(13, 202, 240, 0.1);
  border-color: rgba(13, 202, 240, 0.3);
  color: #0dcaf0;
}

.field-error-container.success {
  background: rgba(25, 135, 84, 0.1);
  border-color: rgba(25, 135, 84, 0.3);
  color: #198754;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-icon {
  font-size: 1rem;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.field-error {
  display: block;
  color: #ff6b6b;
  font-size: 0.875rem;
  font-weight: 500;
  line-height: 1.4;
  flex: 1;
}

.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-weight: 500;
}

.alert-error {
  background-color: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
}

.alert-success {
  background-color: rgba(76, 217, 100, 0.1);
  border: 1px solid rgba(76, 217, 100, 0.3);
  color: #4cd964;
}

/* Modal Styling */
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
}

.modal-content {
  width: 90%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 2rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  color: var(--text-accent);
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.modal-close:hover {
  color: var(--text-primary);
}

.modal-description {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--text-secondary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
  color: var(--text-primary);
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .login-card {
    padding: 2rem 1.5rem;
  }

  .form-options {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .field-error-container {
    padding: 0.5rem;
    gap: 0.25rem;
  }

  .error-icon {
    font-size: 0.9rem;
  }

  .field-error {
    font-size: 0.8rem;
  }

  .modal-content {
    padding: 1.5rem;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style>
