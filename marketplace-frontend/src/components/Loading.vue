<template>
  <div 
    class="loading-container"
    :class="{ 
      'fullscreen': fullscreen,
      'overlay': overlay,
      'inline': inline,
      'compact': compact
    }"
  >
    <!-- Cosmic Loading Animation -->
    <div v-if="type === 'cosmic'" class="cosmic-loader">
      <div class="planet"></div>
      <div class="orbit"></div>
      <div class="orbit orbit-2"></div>
      <div class="orbit orbit-3"></div>
      <div class="stars">
        <div class="star" v-for="i in 12" :key="i"></div>
      </div>
    </div>
    
    <!-- Spinner Loading -->
    <div v-else-if="type === 'spinner'" class="spinner-loader">
      <div class="spinner"></div>
    </div>
    
    <!-- Dots Loading -->
    <div v-else-if="type === 'dots'" class="dots-loader">
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>
    
    <!-- Pulse Loading -->
    <div v-else-if="type === 'pulse'" class="pulse-loader">
      <div class="pulse-circle"></div>
    </div>
    
    <!-- Wave Loading -->
    <div v-else-if="type === 'wave'" class="wave-loader">
      <div class="wave-bar" v-for="i in 5" :key="i"></div>
    </div>
    
    <!-- Progress Bar -->
    <div v-else-if="type === 'progress'" class="progress-loader">
      <div class="progress-track">
        <div 
          class="progress-fill"
          :style="{ width: progressValue + '%' }"
        ></div>
      </div>
      <div v-if="showPercentage" class="progress-text">
        {{ Math.round(progressValue) }}%
      </div>
    </div>
    
    <!-- Skeleton Loading -->
    <div v-else-if="type === 'skeleton'" class="skeleton-loader">
      <div class="skeleton-item skeleton-title"></div>
      <div class="skeleton-item skeleton-text"></div>
      <div class="skeleton-item skeleton-text short"></div>
      <div class="skeleton-item skeleton-button"></div>
    </div>
    
    <!-- Card Skeleton -->
    <div v-else-if="type === 'card-skeleton'" class="card-skeleton-loader">
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-item skeleton-title"></div>
        <div class="skeleton-item skeleton-text"></div>
        <div class="skeleton-item skeleton-price"></div>
      </div>
    </div>
    
    <!-- Loading Text -->
    <div v-if="message && !compact" class="loading-message">
      {{ message }}
    </div>
    
    <!-- Loading Tips -->
    <div v-if="showTips && !compact" class="loading-tips">
      <div class="tip-icon">💡</div>
      <div class="tip-text">{{ currentTip }}</div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'

export default {
  name: 'Loading',
  props: {
    type: {
      type: String,
      default: 'cosmic',
      validator: (value) => [
        'cosmic', 'spinner', 'dots', 'pulse', 'wave', 
        'progress', 'skeleton', 'card-skeleton'
      ].includes(value)
    },
    message: {
      type: String,
      default: ''
    },
    fullscreen: {
      type: Boolean,
      default: false
    },
    overlay: {
      type: Boolean,
      default: false
    },
    inline: {
      type: Boolean,
      default: false
    },
    compact: {
      type: Boolean,
      default: false
    },
    progress: {
      type: Number,
      default: 0,
      validator: (value) => value >= 0 && value <= 100
    },
    showPercentage: {
      type: Boolean,
      default: true
    },
    showTips: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    }
  },
  setup(props) {
    // Reactive data
    const currentTipIndex = ref(0)
    const progressValue = ref(props.progress)
    let tipInterval = null
    
    // Loading tips
    const loadingTips = [
      'Đang tải dữ liệu sản phẩm mới nhất...',
      'Hệ thống đang tối ưu hóa kết quả cho bạn...',
      'Đang kiểm tra khuyến mãi có sẵn...',
      'Chuẩn bị trải nghiệm mua sắm tuyệt vời...',
      'Đang xử lý thông tin bảo mật...',
      'Gần hoàn thành rồi, vui lòng đợi thêm chút...'
    ]
    
    // Computed
    const currentTip = computed(() => {
      return loadingTips[currentTipIndex.value]
    })
    
    // Methods
    const rotateTips = () => {
      currentTipIndex.value = (currentTipIndex.value + 1) % loadingTips.length
    }
    
    const updateProgress = () => {
      if (props.type === 'progress' && props.progress !== progressValue.value) {
        progressValue.value = props.progress
      }
    }
    
    // Lifecycle
    onMounted(() => {
      if (props.showTips) {
        tipInterval = setInterval(rotateTips, 3000)
      }
      
      // Animate progress if it's a progress loader
      if (props.type === 'progress' && props.progress > 0) {
        const startProgress = progressValue.value
        const targetProgress = props.progress
        const duration = 1000 // 1 second
        const steps = 60 // 60fps
        const increment = (targetProgress - startProgress) / steps
        
        let currentStep = 0
        const progressAnimation = setInterval(() => {
          currentStep++
          progressValue.value = Math.min(startProgress + (increment * currentStep), targetProgress)
          
          if (currentStep >= steps || progressValue.value >= targetProgress) {
            clearInterval(progressAnimation)
          }
        }, duration / steps)
      }
    })
    
    onUnmounted(() => {
      if (tipInterval) {
        clearInterval(tipInterval)
      }
    })
    
    return {
      currentTip,
      progressValue,
      updateProgress
    }
  }
}
</script>

<style scoped>
/* Loading Container Base */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  padding: 2rem;
  color: var(--text-primary);
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(10px);
  z-index: 9999;
}

.loading-container.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(5px);
  z-index: 100;
}

.loading-container.inline {
  position: relative;
  padding: 1rem;
  background: transparent;
}

.loading-container.compact {
  padding: 0.5rem;
  gap: 0.75rem;
}

/* Cosmic Loader */
.cosmic-loader {
  position: relative;
  width: 120px;
  height: 120px;
}

.planet {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 24px;
  height: 24px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.6);
  animation: planetPulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: rotate 3s linear infinite;
}

.orbit:nth-child(2) {
  width: 60px;
  height: 60px;
}

.orbit.orbit-2 {
  width: 90px;
  height: 90px;
  border-color: rgba(142, 68, 173, 0.3);
  animation-duration: 4s;
  animation-direction: reverse;
}

.orbit.orbit-3 {
  width: 120px;
  height: 120px;
  border-color: rgba(255, 205, 60, 0.2);
  animation-duration: 5s;
}

.stars {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: white;
  border-radius: 50%;
  animation: twinkle 2s ease-in-out infinite;
}

.star:nth-child(1) { top: 10%; left: 20%; animation-delay: 0s; }
.star:nth-child(2) { top: 20%; right: 15%; animation-delay: 0.2s; }
.star:nth-child(3) { top: 40%; left: 10%; animation-delay: 0.4s; }
.star:nth-child(4) { top: 60%; right: 20%; animation-delay: 0.6s; }
.star:nth-child(5) { bottom: 20%; left: 25%; animation-delay: 0.8s; }
.star:nth-child(6) { bottom: 30%; right: 10%; animation-delay: 1s; }
.star:nth-child(7) { top: 15%; left: 50%; animation-delay: 1.2s; }
.star:nth-child(8) { top: 70%; right: 40%; animation-delay: 1.4s; }
.star:nth-child(9) { bottom: 10%; left: 60%; animation-delay: 1.6s; }
.star:nth-child(10) { top: 80%; left: 40%; animation-delay: 1.8s; }
.star:nth-child(11) { top: 30%; right: 30%; animation-delay: 2s; }
.star:nth-child(12) { bottom: 50%; right: 50%; animation-delay: 2.2s; }

/* Spinner Loader */
.spinner-loader {
  width: 50px;
  height: 50px;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 4px solid rgba(0, 212, 255, 0.2);
  border-top: 4px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Dots Loader */
.dots-loader {
  display: flex;
  gap: 0.5rem;
}

.dot {
  width: 12px;
  height: 12px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  animation: dotBounce 1.4s ease-in-out infinite both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
.dot:nth-child(3) { animation-delay: 0s; }

/* Pulse Loader */
.pulse-loader {
  width: 60px;
  height: 60px;
}

.pulse-circle {
  width: 100%;
  height: 100%;
  background: var(--aurora-gradient);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

/* Wave Loader */
.wave-loader {
  display: flex;
  gap: 0.25rem;
  align-items: end;
  height: 40px;
}

.wave-bar {
  width: 4px;
  height: 10px;
  background: var(--aurora-gradient);
  border-radius: 2px;
  animation: wave 1.2s ease-in-out infinite;
}

.wave-bar:nth-child(1) { animation-delay: 0s; }
.wave-bar:nth-child(2) { animation-delay: 0.1s; }
.wave-bar:nth-child(3) { animation-delay: 0.2s; }
.wave-bar:nth-child(4) { animation-delay: 0.3s; }
.wave-bar:nth-child(5) { animation-delay: 0.4s; }

/* Progress Loader */
.progress-loader {
  width: 100%;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: center;
}

.progress-track {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: var(--aurora-gradient);
  border-radius: 4px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

.progress-text {
  color: var(--text-accent);
  font-weight: 600;
  font-size: 0.9rem;
}

/* Skeleton Loader */
.skeleton-loader {
  width: 100%;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.skeleton-item {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  animation: shimmer 2s infinite;
  position: relative;
  overflow: hidden;
}

.skeleton-title {
  height: 20px;
  width: 70%;
}

.skeleton-text {
  height: 16px;
  width: 100%;
}

.skeleton-text.short {
  width: 60%;
}

.skeleton-button {
  height: 36px;
  width: 120px;
  border-radius: 18px;
}

.skeleton-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: skeletonShimmer 2s infinite;
}

/* Card Skeleton */
.card-skeleton-loader {
  width: 250px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  animation: shimmer 2s infinite;
}

.skeleton-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.skeleton-price {
  height: 24px;
  width: 80px;
  background: rgba(0, 212, 255, 0.2);
}

/* Loading Message */
.loading-message {
  text-align: center;
  color: var(--text-secondary);
  font-size: 1rem;
  font-weight: 500;
  max-width: 300px;
  line-height: 1.4;
}

/* Loading Tips */
.loading-tips {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-align: center;
  max-width: 400px;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.tip-icon {
  font-size: 1.5rem;
  filter: drop-shadow(0 0 8px rgba(255, 205, 60, 0.5));
}

.tip-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

/* Animations */
@keyframes rotate {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes planetPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

@keyframes wave {
  0%, 40%, 100% { transform: scaleY(0.4); }
  20% { transform: scaleY(1); }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@keyframes skeletonShimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* Size Variations */
.loading-container.compact .cosmic-loader {
  width: 60px;
  height: 60px;
}

.loading-container.compact .planet {
  width: 12px;
  height: 12px;
}

.loading-container.compact .orbit:nth-child(2) {
  width: 30px;
  height: 30px;
}

.loading-container.compact .orbit.orbit-2 {
  width: 45px;
  height: 45px;
}

.loading-container.compact .orbit.orbit-3 {
  width: 60px;
  height: 60px;
}

.loading-container.compact .spinner-loader {
  width: 30px;
  height: 30px;
}

.loading-container.compact .pulse-loader {
  width: 30px;
  height: 30px;
}

.loading-container.compact .wave-loader {
  height: 20px;
}

.loading-container.compact .dots-loader .dot {
  width: 8px;
  height: 8px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .loading-container {
    padding: 1rem;
    gap: 1rem;
  }
  
  .cosmic-loader {
    width: 80px;
    height: 80px;
  }
  
  .planet {
    width: 16px;
    height: 16px;
  }
  
  .orbit:nth-child(2) {
    width: 40px;
    height: 40px;
  }
  
  .orbit.orbit-2 {
    width: 60px;
    height: 60px;
  }
  
  .orbit.orbit-3 {
    width: 80px;
    height: 80px;
  }
  
  .loading-message {
    font-size: 0.9rem;
  }
  
  .loading-tips {
    max-width: 280px;
    padding: 0.75rem;
  }
  
  .tip-text {
    font-size: 0.8rem;
  }
  
  .card-skeleton-loader {
    width: 200px;
  }
  
  .skeleton-image {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .loading-container {
    padding: 0.5rem;
    gap: 0.75rem;
  }
  
  .cosmic-loader {
    width: 60px;
    height: 60px;
  }
  
  .planet {
    width: 12px;
    height: 12px;
  }
  
  .orbit:nth-child(2) {
    width: 30px;
    height: 30px;
  }
  
  .orbit.orbit-2 {
    width: 45px;
    height: 45px;
  }
  
  .orbit.orbit-3 {
    width: 60px;
    height: 60px;
  }
  
  .spinner-loader {
    width: 40px;
    height: 40px;
  }
  
  .pulse-loader {
    width: 40px;
    height: 40px;
  }
  
  .loading-tips {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
  
  .progress-loader {
    max-width: 250px;
  }
  
  .card-skeleton-loader {
    width: 180px;
  }
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
  .loading-container.fullscreen {
    background: rgba(10, 10, 15, 0.95);
  }
  
  .loading-container.overlay {
    background: rgba(10, 10, 15, 0.8);
  }
  
  .loading-tips {
    background: rgba(255, 255, 255, 0.03);
  }
  
  .skeleton-item {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .card-skeleton-loader {
    background: rgba(255, 255, 255, 0.02);
  }
}

/* High Contrast Mode */
@media (prefers-contrast: high) {
  .orbit {
    border-width: 3px;
  }
  
  .spinner {
    border-width: 5px;
  }
  
  .progress-track {
    border: 1px solid var(--text-primary);
  }
  
  .loading-tips {
    border-width: 2px;
  }
}

/* Reduced Motion */
@media (prefers-reduced-motion: reduce) {
  .planet,
  .orbit,
  .spinner,
  .dot,
  .pulse-circle,
  .wave-bar,
  .star {
    animation-duration: 10s;
  }
  
  .progress-fill::after,
  .skeleton-item::after {
    animation: none;
  }
}

/* Print Styles */
@media print {
  .loading-container {
    display: none !important;
  }
}

/* Accessibility */
.loading-container[aria-label]::before {
  content: attr(aria-label);
  position: absolute;
  left: -10000px;
  width: 1px;
  height: 1px;
  overflow: hidden;
}

/* Focus Styles */
.loading-container:focus {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}

/* Custom Size Classes */
.loading-container.size-small .cosmic-loader {
  width: 60px;
  height: 60px;
}

.loading-container.size-small .planet {
  width: 12px;
  height: 12px;
}

.loading-container.size-small .orbit:nth-child(2) {
  width: 30px;
  height: 30px;
}

.loading-container.size-small .orbit.orbit-2 {
  width: 45px;
  height: 45px;
}

.loading-container.size-small .orbit.orbit-3 {
  width: 60px;
  height: 60px;
}

.loading-container.size-large .cosmic-loader {
  width: 160px;
  height: 160px;
}

.loading-container.size-large .planet {
  width: 32px;
  height: 32px;
}

.loading-container.size-large .orbit:nth-child(2) {
  width: 80px;
  height: 80px;
}

.loading-container.size-large .orbit.orbit-2 {
  width: 120px;
  height: 120px;
}

.loading-container.size-large .orbit.orbit-3 {
  width: 160px;
  height: 160px;
}

.loading-container.size-small .spinner-loader {
  width: 30px;
  height: 30px;
}

.loading-container.size-large .spinner-loader {
  width: 70px;
  height: 70px;
}

.loading-container.size-small .pulse-loader {
  width: 40px;
  height: 40px;
}

.loading-container.size-large .pulse-loader {
  width: 80px;
  height: 80px;
}

/* Loading State Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Error State */
.loading-container.error {
  color: #ef4444;
}

.loading-container.error .cosmic-loader .planet {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.6);
}

.loading-container.error .orbit {
  border-color: rgba(239, 68, 68, 0.3);
}

.loading-container.error .spinner {
  border-top-color: #ef4444;
}

.loading-container.error .dot,
.loading-container.error .pulse-circle,
.loading-container.error .wave-bar {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

/* Success State */
.loading-container.success {
  color: #10b981;
}

.loading-container.success .cosmic-loader .planet {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.6);
}

.loading-container.success .orbit {
  border-color: rgba(16, 185, 129, 0.3);
}

.loading-container.success .spinner {
  border-top-color: #10b981;
}

.loading-container.success .dot,
.loading-container.success .pulse-circle,
.loading-container.success .wave-bar {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}