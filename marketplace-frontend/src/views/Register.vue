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
                  :class="{ 'error-input': hasFieldError('lastName') }"
                  placeholder="Họ"
                  required
                />
                <span v-if="hasFieldError('lastName')" class="field-error">
                  {{ getFieldError('lastName') }}
                </span>
              </div>

              <div class="form-group">
                <label class="form-label">Tên</label>
                <input
                  v-model="userData.firstName"
                  type="text"
                  class="form-input"
                  :class="{ 'error-input': hasFieldError('firstName') }"
                  placeholder="Tên"
                  required
                />
                <span v-if="hasFieldError('firstName')" class="field-error">
                  {{ getFieldError('firstName') }}
                </span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Email</label>
              <input
                v-model="userData.email"
                type="email"
                class="form-input"
                :class="{ 'error-input': hasFieldError('email') }"
                placeholder="Nhập email của bạn"
                required
              />
              <span v-if="hasFieldError('email')" class="field-error">
                {{ getFieldError('email') }}
              </span>
            </div>

            <div class="form-group">
              <label class="form-label">Số điện thoại (tùy chọn)</label>
              <input
                v-model="userData.phone"
                type="tel"
                class="form-input"
                :class="{ 'error-input': hasFieldError('phone') }"
                placeholder="Số điện thoại"
              />
              <span v-if="hasFieldError('phone')" class="field-error">
                {{ getFieldError('phone') }}
              </span>
            </div>

            <div class="form-group">
              <label class="form-label">Mật khẩu</label>
              <input
                v-model="userData.password"
                type="password"
                class="form-input"
                :class="{ 'error-input': hasFieldError('password') }"
                placeholder="Mật khẩu (tối thiểu 6 ký tự)"
                required
                minlength="6"
              />
              <span v-if="hasFieldError('password')" class="field-error">
                {{ getFieldError('password') }}
              </span>
            </div>

            <div class="form-group">
              <label class="form-label">Xác nhận mật khẩu</label>
              <input
                v-model="confirmPassword"
                type="password"
                class="form-input"
                placeholder="Nhập lại mật khẩu"
                required
              />
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="loading">
              <span v-if="loading">🔄 Đang đăng ký...</span>
              <span v-else>⭐ Đăng ký ngay</span>
            </button>
          </form>

          <div class="register-footer">
            <p>Đã có tài khoản?
              <router-link to="/login" class="text-accent">Đăng nhập ngay</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
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
      phone: ''
    })
    
    const confirmPassword = ref('')
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const fieldErrors = ref({})

    // Enhanced error parsing function
    const parseErrorResponse = (error) => {
      console.log('Full error object:', error)
      
      if (error.response?.data) {
        const errorData = error.response.data
        console.log('Error data:', errorData)
        
        // Case 1: Validation errors with 'errors' object
        if (errorData.errors && typeof errorData.errors === 'object') {
          fieldErrors.value = { ...errorData.errors }
          
          // Get first error message to display as main error
          const firstFieldError = Object.values(errorData.errors)[0]
          return firstFieldError || 'Dữ liệu không hợp lệ'
        }
        
        // Case 2: Single error message
        if (errorData.error) {
          return errorData.error
        }
        
        // Case 3: Message field
        if (errorData.message) {
          return errorData.message
        }
        
        // Case 4: Direct string errors
        if (typeof errorData === 'string') {
          return errorData
        }
      }
      
      // Default fallback
      return 'Đăng ký thất bại. Vui lòng thử lại.'
    }

    const handleRegister = async () => {
      loading.value = true
      error.value = ''
      success.value = ''
      fieldErrors.value = {}

      // Validate passwords match
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

    // Helper function to check if a field has error
    const hasFieldError = (fieldName) => {
      return fieldErrors.value[fieldName]
    }

    // Helper function to get field error message
    const getFieldError = (fieldName) => {
      return fieldErrors.value[fieldName] || ''
    }

    return {
      userData,
      confirmPassword,
      loading,
      error,
      success,
      fieldErrors,
      handleRegister,
      hasFieldError,
      getFieldError
    }
  }
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

/* Error styling */
.error-input {
  border-color: #ff6b6b !important;
  background-color: rgba(255, 107, 107, 0.1) !important;
}

.field-error {
  display: block;
  color: #ff6b6b;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  font-weight: 500;
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
}
</style>