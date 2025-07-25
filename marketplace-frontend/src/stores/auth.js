import { defineStore } from 'pinia'
import { authAPI } from '@/services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token'),
    loading: false,
    error: null
  }),

  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isSeller: (state) => state.user?.role === 'SELLER' || state.user?.role === 'ADMIN',
    currentUser: (state) => state.user,
    userName: (state) => state.user ? `${state.user.firstName} ${state.user.lastName}` : ''
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = null
      
      try {
        const response = await authAPI.login(credentials)
        console.log('🔍 Full login response:', response.data)

        const { token, user } = response.data

        if (!user || !user.role) {
          throw new Error('Invalid user data from server')
        }

        console.log('👤 User data:', user) 
        console.log('🔑 User role:', user.role) 
        
        this.token = token
        this.user = {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          avatar: user.avatar || ''
        }
        this.isAuthenticated = true
        
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(this.user))
        
        console.log('💾 Saved to localStorage:', this.user) 
        console.log('🎯 Is Admin?', this.isAdmin) 

        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Đăng nhập thất bại'
        throw error
      } finally {
        this.loading = false
      }
    },

    async register(userData) {
      this.loading = true
      this.error = null
      
      try {
        const response = await authAPI.register(userData)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Đăng ký thất bại'
        throw error
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        await authAPI.logout()
      } catch (error) {
        console.error('Logout API error:', error)
      } finally {
        this.token = null
        this.user = null
        this.isAuthenticated = false
        this.error = null
        
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    },

    async refreshToken() {
      try {
        const response = await authAPI.refreshToken()
        const { token } = response.data
        
        this.token = token
        localStorage.setItem('token', token)
        
        return token
      } catch (error) {
        console.error('Token refresh failed:', error)
        this.logout()
        throw error
      }
    },

    async forgotPassword(email) {
      try {
        const response = await authAPI.forgotPassword(email)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Gửi email thất bại'
        throw error
      }
    },

    async resetPassword(token, password) {
      try {
        const response = await authAPI.resetPassword(token, password)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Đặt lại mật khẩu thất bại'
        throw error
      }
    },

    initializeFromStorage() {
      const token = localStorage.getItem('token')
      const userData = localStorage.getItem('user')
      
      if (token && userData) {
        try {
          this.token = token
          this.user = JSON.parse(userData)
          this.isAuthenticated = true
          
          console.log('Initialized from storage:', this.user)
          console.log('Is Admin?', this.isAdmin)
        } catch (error) {
          console.error('Error parsing user data from storage:', error)
          this.logout() 
        }
      }
    },

    clearError() {
      this.error = null
    }
  }
})