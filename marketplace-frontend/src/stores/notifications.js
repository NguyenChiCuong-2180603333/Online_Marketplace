import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: []
  }),

  getters: {
    activeNotifications: (state) => state.notifications
  },

  actions: {
    addNotification(notification) {
      const id = Date.now().toString()
      const newNotification = {
        id,
        type: notification.type || 'info', // success, error, warning, info
        message: notification.message,
        title: notification.title || '',
        duration: notification.duration || 5000,
        persistent: notification.persistent || false,
        createdAt: Date.now()
      }

      this.notifications.push(newNotification)

      // Auto remove notification after duration
      if (!newNotification.persistent) {
        setTimeout(() => {
          this.removeNotification(id)
        }, newNotification.duration)
      }

      return id
    },

    removeNotification(id) {
      const index = this.notifications.findIndex(n => n.id === id)
      if (index > -1) {
        this.notifications.splice(index, 1)
      }
    },

    clearAll() {
      this.notifications = []
    },

    // Convenience methods
    success(message, options = {}) {
      return this.addNotification({
        type: 'success',
        message,
        ...options
      })
    },

    error(message, options = {}) {
      return this.addNotification({
        type: 'error',
        message,
        duration: 8000, // Error messages stay longer
        ...options
      })
    },

    warning(message, options = {}) {
      return this.addNotification({
        type: 'warning',
        message,
        ...options
      })
    },

    info(message, options = {}) {
      return this.addNotification({
        type: 'info',
        message,
        ...options
      })
    }
  }
})