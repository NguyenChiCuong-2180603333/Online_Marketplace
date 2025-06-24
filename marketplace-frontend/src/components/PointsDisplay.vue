<template>
  <div class="points-display">
    <!-- Points Balance & Tier -->
    <div 
      class="points-container cursor-pointer hover:bg-gray-50 rounded-lg p-2 transition-colors duration-200"
      @click="$router.push('/loyalty')"
      :title="`Bấm để xem chi tiết loyalty`"
    >
      <!-- Desktop View -->
      <div class="hidden md:flex items-center space-x-3">
        <!-- Tier Badge -->
        <div class="tier-badge">
          <div 
            class="tier-icon flex items-center justify-center w-8 h-8 rounded-full text-white text-sm font-bold"
            :style="{ backgroundColor: loyaltyStore.tierColor }"
          >
            {{ tierInitial }}
          </div>
        </div>
        
        <!-- Points Info -->
        <div class="points-info">
          <div class="flex items-center space-x-2">
            <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
            </svg>
            <span class="text-sm font-semibold text-gray-800">
              {{ loyaltyStore.formattedPoints }}
            </span>
          </div>
          <div class="text-xs text-gray-500">
            {{ loyaltyStore.userPoints.tier }}
          </div>
        </div>
      </div>

      <!-- Mobile View -->
      <div class="md:hidden flex items-center space-x-2">
        <div 
          class="tier-icon-mobile w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-bold"
          :style="{ backgroundColor: loyaltyStore.tierColor }"
        >
          {{ tierInitial }}
        </div>
        <div class="flex items-center space-x-1">
          <svg class="w-3 h-3 text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
          </svg>
          <span class="text-xs font-semibold text-gray-800">
            {{ loyaltyStore.formattedPoints }}
          </span>
        </div>
      </div>
    </div>

    <!-- Points Animation (khi earn points) -->
    <Transition name="points-animation">
      <div 
        v-if="loyaltyStore.showPointsAnimation"
        class="fixed top-20 right-4 z-50 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center space-x-2"
      >
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
        </svg>
        <span class="font-bold">+{{ loyaltyStore.lastPointsEarned }} điểm!</span>
      </div>
    </Transition>

    <!-- Loading Skeleton -->
    <div v-if="loyaltyStore.pointsLoading" class="hidden md:flex items-center space-x-3">
      <div class="w-8 h-8 bg-gray-200 rounded-full animate-pulse"></div>
      <div class="space-y-1">
        <div class="w-16 h-4 bg-gray-200 rounded animate-pulse"></div>
        <div class="w-12 h-3 bg-gray-200 rounded animate-pulse"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useLoyaltyStore } from '@/stores/loyalty';
import { useUserStore } from '@/stores/user';

const loyaltyStore = useLoyaltyStore();
const userStore = useUserStore();

// Computed
const tierInitial = computed(() => {
  return loyaltyStore.userPoints.tier?.charAt(0) || 'B';
});

// Methods
const initializePoints = async () => {
  if (userStore.isLoggedIn) {
    await loyaltyStore.fetchUserPoints();
  }
};

// Lifecycle
onMounted(() => {
  initializePoints();
});

// Watch for login changes
watchEffect(() => {
  if (userStore.isLoggedIn) {
    initializePoints();
  } else {
    loyaltyStore.resetLoyalty();
  }
});
</script>

<style scoped>
/* Points Animation */
.points-animation-enter-active {
  transition: all 0.5s ease-out;
}

.points-animation-leave-active {
  transition: all 0.5s ease-in;
}

.points-animation-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.8);
}

.points-animation-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.8);
}

/* Tier Badge Glow Effect */
.tier-badge:hover .tier-icon {
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
  transform: scale(1.1);
  transition: all 0.3s ease;
}

/* Points Container Hover Effect */
.points-container:hover {
  transform: translateY(-1px);
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .points-container {
    padding: 8px;
  }
}
</style>