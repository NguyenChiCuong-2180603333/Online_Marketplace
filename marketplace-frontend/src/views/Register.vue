<template>
  <div class="register-page">
    <div class="container">
      <div class="register-container">
        <div class="space-card register-card">
          <div class="register-header">
            <h1>⭐ Đăng ký Cosmic Marketplace</h1>
            <p>Tham gia cộng đồng vũ trụ mua sắm</p>
          </div>

          <form @submit.prevent="handleRegister" class="register-form">
            <!-- Main Error Display -->
            <div v-if="error" class="alert alert-error">
              {{ error }}
            </div>

            <div v-if="success" class="alert alert-success">
              {{ success }}
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Họ</label>
                <input
                  v-model="userData.lastName"
                  type="text"
                  class="form-input"
                  :class="{ 'error-input': hasFieldError('lastName') || lastNameValidationError }"
                  placeholder="Họ"
                  @blur="validateLastName"
                  @input="clearLastNameError"
                />
                <div
                  v-if="hasFieldError('lastName') || lastNameValidationError"
                  class="field-error-container"
                >
                  <span class="error-icon">👤</span>
                  <span class="field-error">
                    {{ getFieldError('lastName') || lastNameValidationError }}
                  </span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Tên</label>
                <input
                  v-model="userData.firstName"
                  type="text"
                  class="form-input"
                  :class="{ 'error-input': hasFieldError('firstName') || firstNameValidationError }"
                  placeholder="Tên"
                  @blur="validateFirstName"
                  @input="clearFirstNameError"
                />
                <div
                  v-if="hasFieldError('firstName') || firstNameValidationError"
                  class="field-error-container"
                >
                  <span class="error-icon">👤</span>
                  <span class="field-error">
                    {{ getFieldError('firstName') || firstNameValidationError }}
                  </span>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Email</label>
              <input
                v-model="userData.email"
                type="email"
                class="form-input"
                :class="{ 'error-input': hasFieldError('email') || emailValidationError }"
                placeholder="Nhập email của bạn"
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
              <label class="form-label">Số điện thoại</label>
              <input
                v-model="userData.phone"
                type="tel"
                class="form-input"
                :class="{ 'error-input': hasFieldError('phone') || phoneValidationError }"
                placeholder="Số điện thoại"
                @blur="validatePhone"
                @input="clearPhoneError"
              />
              <div
                v-if="hasFieldError('phone') || phoneValidationError"
                class="field-error-container"
              >
                <span class="error-icon">📱</span>
                <span class="field-error">
                  {{ getFieldError('phone') || phoneValidationError }}
                </span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Mật khẩu</label>
              <div class="password-input-wrapper">
                <input
                  v-model="userData.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  :class="{ 'error-input': hasFieldError('password') || passwordValidationError }"
                  placeholder="Mật khẩu (chữ hoa, thường, số, ký tự đặc biệt)"
                  minlength="6"
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

              <!-- Password Strength Indicator -->
              <div
                v-if="userData.password && !passwordValidationError && !hasFieldError('password')"
                class="password-strength"
              >
                <div class="strength-bar">
                  <div class="strength-fill" :class="passwordStrengthClass"></div>
                </div>
                <span class="strength-text">{{ passwordStrengthText }}</span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Xác nhận mật khẩu</label>
              <div class="password-input-wrapper">
                <input
                  v-model="confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="form-input"
                  :class="{ 'error-input': confirmPasswordValidationError }"
                  placeholder="Nhập lại mật khẩu"
                  @blur="validateConfirmPassword"
                  @input="clearConfirmPasswordError"
                />
                <button
                  type="button"
                  @click="toggleConfirmPassword"
                  class="password-toggle"
                  tabindex="-1"
                >
                  {{ showConfirmPassword ? '🙈' : '👁️' }}
                </button>
              </div>
              <div v-if="confirmPasswordValidationError" class="field-error-container">
                <span class="error-icon">🔐</span>
                <span class="field-error">
                  {{ confirmPasswordValidationError }}
                </span>
              </div>
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="loading">
              <span v-if="loading">🔄 Đang đăng ký...</span>
              <span v-else>⭐ Đăng ký ngay</span>
            </button>
          </form>

          <div class="register-footer">
            <p>
              Đã có tài khoản?
              <router-link to="/login" class="text-accent">Đăng nhập ngay</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'Register',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const userData = ref({
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phone: '',
    })

    const confirmPassword = ref('')
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const fieldErrors = ref({})
    const showPassword = ref(false)
    const showConfirmPassword = ref(false)
    const emailValidationError = ref('')
    const passwordValidationError = ref('')
    const confirmPasswordValidationError = ref('')
    const firstNameValidationError = ref('')
    const lastNameValidationError = ref('')
    const phoneValidationError = ref('')

    const parseErrorResponse = (error) => {
      console.log('Full error object:', error)

      if (error.response?.data) {
        const errorData = error.response.data
        console.log('Error data:', errorData)

        if (errorData.errors && typeof errorData.errors === 'object') {
          fieldErrors.value = { ...errorData.errors }

          const firstFieldError = Object.values(errorData.errors)[0]
          return firstFieldError || 'Dữ liệu không hợp lệ'
        }

        if (errorData.error) {
          return errorData.error
        }

        if (errorData.message) {
          return errorData.message
        }

        if (typeof errorData === 'string') {
          return errorData
        }
      }

      return 'Đăng ký thất bại. Vui lòng thử lại.'
    }

    const handleRegister = async () => {
      emailValidationError.value = ''
      passwordValidationError.value = ''
      confirmPasswordValidationError.value = ''
      firstNameValidationError.value = ''
      lastNameValidationError.value = ''
      phoneValidationError.value = ''

      let isValid = true
      isValid &= validateEmail()
      isValid &= validatePassword()
      isValid &= validateConfirmPassword()
      isValid &= validateFirstName()
      isValid &= validateLastName()
      isValid &= validatePhone()

      if (!isValid) {
        return
      }

      loading.value = true
      error.value = ''
      success.value = ''
      fieldErrors.value = {}

      if (userData.value.password !== confirmPassword.value) {
        error.value = 'Mật khẩu xác nhận không khớp'
        loading.value = false
        return
      }

      try {
        await authStore.register(userData.value)
        success.value = 'Đăng ký thành công! Chuyển hướng đến trang đăng nhập...'
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } catch (err) {
        error.value = parseErrorResponse(err)
        console.error('Registration error:', err)
      } finally {
        loading.value = false
      }
    }

    const hasFieldError = (fieldName) => {
      return fieldErrors.value[fieldName]
    }

    const getFieldError = (fieldName) => {
      return fieldErrors.value[fieldName] || ''
    }

    const togglePassword = () => {
      showPassword.value = !showPassword.value
    }

    const toggleConfirmPassword = () => {
      showConfirmPassword.value = !showConfirmPassword.value
    }

    const validateEmail = () => {
      const email = userData.value.email.trim()
      if (!email) {
        emailValidationError.value = 'Vui lòng nhập email'
        return false
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(email)) {
        emailValidationError.value = `Vui lòng nhập '@' trong địa chỉ email. '${email}' thiếu '@'.`
        return false
      }

      emailValidationError.value = ''
      return true
    }

    const clearEmailError = () => {
      emailValidationError.value = ''
    }

    const validatePassword = () => {
      const password = userData.value.password

      if (!password) {
        passwordValidationError.value = 'Vui lòng nhập mật khẩu'
        return false
      }

      if (password.length < 6) {
        passwordValidationError.value = 'Mật khẩu phải có ít nhất 6 ký tự'
        return false
      }

      // Kiểm tra độ mạnh mật khẩu
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

    const validateConfirmPassword = () => {
      const password = userData.value.password
      const confirm = confirmPassword.value

      if (!confirm) {
        confirmPasswordValidationError.value = 'Vui lòng xác nhận mật khẩu'
        return false
      }

      if (password !== confirm) {
        confirmPasswordValidationError.value = 'Mật khẩu xác nhận không khớp'
        return false
      }

      confirmPasswordValidationError.value = ''
      return true
    }

    const clearConfirmPasswordError = () => {
      confirmPasswordValidationError.value = ''
    }

    const validateFirstName = () => {
      const firstName = userData.value.firstName.trim()

      if (!firstName) {
        firstNameValidationError.value = 'Vui lòng nhập tên'
        return false
      }

      if (firstName.length < 2) {
        firstNameValidationError.value = 'Tên phải có ít nhất 2 ký tự'
        return false
      }

      firstNameValidationError.value = ''
      return true
    }

    const clearFirstNameError = () => {
      firstNameValidationError.value = ''
    }

    const validateLastName = () => {
      const lastName = userData.value.lastName.trim()

      if (!lastName) {
        lastNameValidationError.value = 'Vui lòng nhập họ'
        return false
      }

      if (lastName.length < 2) {
        lastNameValidationError.value = 'Họ phải có ít nhất 2 ký tự'
        return false
      }

      lastNameValidationError.value = ''
      return true
    }

    const clearLastNameError = () => {
      lastNameValidationError.value = ''
    }

    const validatePhone = () => {
      const phone = userData.value.phone.trim()

      if (!phone) {
        phoneValidationError.value = 'Vui lòng nhập số điện thoại'
        return false
      }

      const phoneRegex = /^[0-9]{10}$/
      if (!phoneRegex.test(phone)) {
        phoneValidationError.value = 'Số điện thoại không hợp lệ (phải là 10 số)'
        return false
      }

      phoneValidationError.value = ''
      return true
    }

    const clearPhoneError = () => {
      phoneValidationError.value = ''
    }

    // Password strength computed properties
    const passwordStrengthClass = computed(() => {
      const password = userData.value.password
      if (!password) return ''

      const hasUpperCase = /[A-Z]/.test(password)
      const hasLowerCase = /[a-z]/.test(password)
      const hasNumbers = /\d/.test(password)
      const hasSpecialChars = /[!@#$%^&*(),.?":{}|<>]/.test(password)
      const length = password.length

      let score = 0
      if (hasUpperCase) score++
      if (hasLowerCase) score++
      if (hasNumbers) score++
      if (hasSpecialChars) score++
      if (length >= 8) score++

      if (score <= 2) return 'very-weak'
      if (score <= 3) return 'weak'
      if (score <= 4) return 'medium'
      if (score <= 5) return 'strong'
      return 'very-strong'
    })

    const passwordStrengthText = computed(() => {
      const strength = passwordStrengthClass.value
      switch (strength) {
        case 'very-weak':
          return 'Rất yếu'
        case 'weak':
          return 'Yếu'
        case 'medium':
          return 'Trung bình'
        case 'strong':
          return 'Mạnh'
        case 'very-strong':
          return 'Rất mạnh'
        default:
          return ''
      }
    })

    return {
      userData,
      confirmPassword,
      loading,
      error,
      success,
      fieldErrors,
      showPassword,
      showConfirmPassword,
      emailValidationError,
      passwordValidationError,
      confirmPasswordValidationError,
      firstNameValidationError,
      lastNameValidationError,
      phoneValidationError,
      handleRegister,
      hasFieldError,
      getFieldError,
      togglePassword,
      toggleConfirmPassword,
      validateEmail,
      clearEmailError,
      validatePassword,
      clearPasswordError,
      validateConfirmPassword,
      clearConfirmPasswordError,
      validateFirstName,
      clearFirstNameError,
      validateLastName,
      clearLastNameError,
      validatePhone,
      clearPhoneError,
      passwordStrengthClass,
      passwordStrengthText,
    }
  },
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 40px 0;
}

.register-container {
  max-width: 500px;
  margin: 0 auto;
}

.register-card {
  padding: 40px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-accent);
}

.register-header p {
  color: var(--text-secondary);
}

.register-form {
  margin-bottom: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.register-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.register-footer a {
  text-decoration: none;
  font-weight: 500;
}

.register-footer a:hover {
  text-decoration: underline;
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

/* Error styling */
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

/* Password Strength Indicator */
.password-strength {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-fill.very-weak {
  width: 20%;
  background: #ff6b6b;
}

.strength-fill.weak {
  width: 40%;
  background: #ffa726;
}

.strength-fill.medium {
  width: 60%;
  background: #ffd54f;
}

.strength-fill.strong {
  width: 80%;
  background: #66bb6a;
}

.strength-fill.very-strong {
  width: 100%;
  background: #4caf50;
}

.strength-text {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-weight: 500;
  min-width: 60px;
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

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .register-card {
    padding: 2rem 1.5rem;
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
}
</style>
