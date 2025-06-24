<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <!-- Backdrop -->
    <div 
      class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
      @click="$emit('close')"
    ></div>

    <!-- Modal Container -->
    <div class="flex items-center justify-center min-h-screen p-4">
      <div 
        class="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-auto transform transition-all"
        @click.stop
      >
        <!-- Header -->
        <div class="px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-800">
              Xác nhận đổi thưởng
            </h3>
            <button 
              @click="$emit('close')"
              class="text-gray-400 hover:text-gray-600 transition-colors duration-200"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="px-6 py-4">
          <!-- Reward Display -->
          <div class="text-center mb-6">
            <!-- Reward Icon -->
            <div class="flex items-center justify-center w-16 h-16 bg-purple-100 rounded-full mx-auto mb-4">
              <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"/>
              </svg>
            </div>

            <!-- Reward Info -->
            <h4 class="text-xl font-bold text-gray-800 mb-2">{{ reward?.title }}</h4>
            <p class="text-gray-600 mb-4">{{ reward?.description }}</p>
            
            <!-- Discount Badge -->
            <div v-if="reward?.discount" class="inline-block bg-red-100 text-red-800 px-3 py-1 rounded-full text-sm font-medium mb-4">
              Giảm {{ reward.discount }}%
            </div>
          </div>

          <!-- Points Transaction Info -->
          <div class="bg-gray-50 rounded-lg p-4 mb-6">
            <div class="flex items-center justify-between mb-3">
              <span class="text-gray-600">Điểm cần dùng:</span>
              <span class="text-lg font-bold text-red-600">{{ reward?.pointsRequired }} điểm</span>
            </div>
            
            <div class="flex items-center justify-between mb-3">
              <span class="text-gray-600">Điểm hiện tại:</span>
              <span class="text-lg font-semibold text-gray-800">{{ loyaltyStore.formattedPoints }} điểm</span>
            </div>
            
            <hr class="my-3 border-gray-200">
            
            <div class="flex items-center justify-between">
              <span class="text-gray-600">Điểm còn lại:</span>
              <span 
                class="text-lg font-bold"
                :class="remainingPoints >= 0 ? 'text-green-600' : 'text-red-600'"
              >
                {{ remainingPoints.toLocaleString('vi-VN') }} điểm
              </span>
            </div>
          </div>

          <!-- Terms and Conditions -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <h5 class="font-semibold text-blue-800 mb-2 flex items-center">
              <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
              </svg>
              Điều khoản sử dụng:
            </h5>
            <ul class="text-sm text-blue-700 space-y-1">
              <li>• Voucher có hiệu lực trong 30 ngày</li>
              <li>• Không thể hoàn lại điểm sau khi đổi</li>
              <li>• Voucher chỉ áp dụng cho đơn hàng từ {{ reward?.minOrderValue || '0' }}đ</li>
              <li>• Không áp dụng cùng các khuyến mãi khác</li>
            </ul>
          </div>

          <!-- Confirmation -->
          <div class="mb-6">
            <label class="flex items-start space-x-3">
              <input
                v-model="agreedToTerms"
                type="checkbox"
                class="mt-1 h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
              >
              <span class="text-sm text-gray-700">
                Tôi đã đọc và đồng ý với các điều khoản sử dụng voucher
              </span>
            </label>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-gray-200 bg-gray-50 rounded-b-lg">
          <div class="flex space-x-3">
            <!-- Cancel Button -->
            <button
              @click="$emit('close')"
              class="flex-1 px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium"
            >
              Hủy bỏ
            </button>

            <!-- Redeem Button -->
            <button
              @click="confirmRedeem"
              :disabled="!canRedeem"
              :class="[
                'flex-1 px-4 py-2 rounded-lg font-medium transition-all duration-200',
                canRedeem
                  ? 'bg-purple-600 hover:bg-purple-700 text-white transform hover:scale-105'
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              ]"
            >
              <span v-if="!loyaltyStore.redemptionLoading">
                Đổi ngay
              </span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Đang xử lý...
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useLoyaltyStore } from '@/stores/loyalty';

// Props
const props = defineProps({
  reward: {
    type: Object,
    required: true
  }
});

// Emits
const emit = defineEmits(['close', 'redeem']);

// Store
const loyaltyStore = useLoyaltyStore();

// Reactive data
const agreedToTerms = ref(false);

// Computed
const remainingPoints = computed(() => {
  return loyaltyStore.userPoints.current - (props.reward?.pointsRequired || 0);
});

const canRedeem = computed(() => {
  return (
    agreedToTerms.value &&
    !loyaltyStore.redemptionLoading &&
    loyaltyStore.canRedeem(props.reward?.pointsRequired || 0)
  );
});

// Methods
const confirmRedeem = async () => {
  if (!canRedeem.value) return;

  try {
    emit('redeem', props.reward.id, props.reward.pointsRequired);
  } catch (error) {
    console.error('Redemption failed:', error);
  }
};

// Handle ESC key
const handleEscape = (event) => {
  if (event.key === 'Escape') {
    emit('close');
  }
};

// Add escape listener
onMounted(() => {
  document.addEventListener('keydown', handleEscape);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape);
});
</script>

<style scoped>
/* Modal entrance animation */
@keyframes modalEnter {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-enter {
  animation: modalEnter 0.2s ease-out;
}

/* Responsive design */
@media (max-width: 640px) {
  .modal-container {
    margin: 16px;
  }
}
</style>