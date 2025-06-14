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
            <div v-if="error" class="alert alert-error">
              {{ error }}
            </div>

            <div class="form-group">
              <label class="form-label">Email</label>
              <input
                v-model="credentials.email"
                type="email"
                class="form-input"
                placeholder="Nhập email của bạn"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Mật khẩu</label>
              <input
                v-model="credentials.password"
                type="password"
                class="form-input"
                placeholder="Nhập mật khẩu"
                required
              />
            </div>

            <button type="submit" class="btn btn-primary w-full" :disabled="loading">
              <span v-if="loading">🔄 Đang đăng nhập...</span>
              <span v-else>🚀 Đăng nhập</span>
            </button>
          </form>

          <div class="login-footer">
            <p>Chưa có tài khoản? 
              <router-link to="/register" class="text-accent">Đăng ký ngay</router-link>
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
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'Login',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const route = useRoute()
    
    const credentials = ref({
      email: '',
      password: ''
    })
    
    const loading = ref(false)
    const error = ref('')

    const handleLogin = async () => {
      loading.value = true
      error.value = ''
      
      try {
        await authStore.login(credentials.value)
        
        // Redirect to intended page or home
        const redirectTo = route.query.redirect || '/'
        router.push(redirectTo)
      } catch (err) {
        error.value = authStore.error || 'Đăng nhập thất bại'
      } finally {
        loading.value = false
      }
    }

    return {
      credentials,
      loading,
      error,
      handleLogin
    }
  }
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
</style>