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
    currentUser: (state) => state.user,
    userName: (state) => state.user ? `${state.user.firstName} ${state.user.lastName}` : ''
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = null
      
      try {
        const response = await authAPI.login(credentials)
        const { token, id, email, firstName, lastName, role } = response.data
        
        this.token = token
        this.user = { id, email, firstName, lastName, role }
        this.isAuthenticated = true
        
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(this.user))
        
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
        // Ignore logout errors
      } finally {
        this.token = null
        this.user = null
        this.isAuthenticated = false
        this.error = null
        
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    },

    clearError() {
      this.error = null
    }
  }
})