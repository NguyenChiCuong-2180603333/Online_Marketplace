<template>
  <div class="notification-container">
    <transition-group name="notification" tag="div">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification"
        :class="[`notification--${notification.type}`]"
        @click="removeNotification(notification.id)"
      >
        <div class="notification__icon">
          {{ getIcon(notification.type) }}
        </div>
        <div class="notification__content">
          <div v-if="notification.title" class="notification__title">
            {{ notification.title }}
          </div>
          <div class="notification__message">
            {{ notification.message }}
          </div>
        </div>
        <button 
          class="notification__close"
          @click.stop="removeNotification(notification.id)"
        >
          ×
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useNotificationStore } from '@/stores/notifications'

const notificationStore = useNotificationStore()

const notifications = computed(() => notificationStore.activeNotifications)

const getIcon = (type) => {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[type] || 'ℹ️'
}

const removeNotification = (id) => {
  notificationStore.removeNotification(id)
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 400px;
}

.notification {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  backdrop-filter: blur(10px);
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

.notification:hover {
  transform: translateX(-2px);
}

.notification--success {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.notification--error {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.notification--warning {
  background: rgba(245, 158, 11, 0.15);
  border-color: rgba(245, 158, 11, 0.3);
  color: #f59e0b;
}

.notification--info {
  background: rgba(0, 212, 255, 0.15);
  border-color: rgba(0, 212, 255, 0.3);
  color: #00d4ff;
}

.notification__icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.notification__content {
  flex: 1;
}

.notification__title {
  font-weight: 600;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.notification__message {
  font-size: 0.85rem;
  line-height: 1.4;
  color: inherit;
  opacity: 0.9;
}

.notification__close {
  background: none;
  border: none;
  color: inherit;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s ease;
  flex-shrink: 0;
}

.notification__close:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* Animations */
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.notification-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.notification-move {
  transition: transform 0.3s ease;
}

/* Responsive */
@media (max-width: 768px) {
  .notification-container {
    left: 1rem;
    right: 1rem;
    max-width: none;
  }
  
  .notification {
    padding: 0.75rem;
  }
}
</style>