<template>
  <div class="loyalty-page bg-gray-50 min-h-screen">
    <!-- Header Section -->
    <div class="bg-gradient-to-r from-purple-600 to-blue-600 text-white">
      <div class="container mx-auto px-4 py-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold mb-2">Chương trình Khách hàng Thân thiết</h1>
          <p class="text-blue-100">Tích điểm - Nhận thưởng - Thăng hạng</p>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <!-- Points Overview -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <!-- Current Points -->
        <div class="bg-white rounded-lg shadow-lg p-6 text-center">
          <div class="text-4xl font-bold text-purple-600 mb-2">
            {{ loyaltyStore.formattedPoints }}
          </div>
          <div class="text-gray-600">Điểm hiện tại</div>
          <div class="mt-4">
            <svg class="w-12 h-12 mx-auto text-yellow-500" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
            </svg>
          </div>
        </div>

        <!-- Current Tier -->
        <div class="bg-white rounded-lg shadow-lg p-6 text-center">
          <div 
            class="w-16 h-16 mx-auto rounded-full flex items-center justify-center text-white text-2xl font-bold mb-3"
            :style="{ backgroundColor: loyaltyStore.tierColor }"
          >
            {{ tierInitial }}
          </div>
          <div class="text-xl font-bold text-gray-800 mb-1">
            {{ loyaltyStore.userPoints.tier }}
          </div>
          <div class="text-gray-600">Hạng thành viên</div>
        </div>

        <!-- Next Tier Progress -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <div class="text-center mb-4">
            <div class="text-lg font-semibold text-gray-800">Thăng hạng</div>
            <div class="text-sm text-gray-600">
              {{ loyaltyStore.userPoints.pointsToNextTier }} điểm nữa
            </div>
          </div>
          
          <!-- Progress Bar -->
          <div class="relative">
            <div class="bg-gray-200 rounded-full h-3 mb-2">
              <div 
                class="bg-gradient-to-r from-purple-500 to-blue-500 h-3 rounded-full transition-all duration-500"
                :style="{ width: loyaltyStore.tierProgress + '%' }"
              ></div>
            </div>
            <div class="text-center text-sm text-gray-600">
              {{ loyaltyStore.tierProgress }}% hoàn thành
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Tabs -->
      <div class="bg-white rounded-lg shadow-lg">
        <!-- Tab Headers -->
        <div class="border-b border-gray-200">
          <nav class="flex space-x-8 px-6" aria-label="Tabs">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                'py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200',
                activeTab === tab.id
                  ? 'border-purple-500 text-purple-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              ]"
            >
              {{ tab.name }}
            </button>
          </nav>
        </div>

        <!-- Tab Content -->
        <div class="p-6">
          <!-- Rewards Tab -->
          <div v-if="activeTab === 'rewards'">
            <div class="mb-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">Phần thưởng có thể đổi</h3>
              <p class="text-gray-600">Sử dụng điểm tích lũy để đổi voucher và ưu đãi hấp dẫn</p>
            </div>

            <!-- Loading -->
            <div v-if="loyaltyStore.rewardsLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div v-for="i in 6" :key="i" class="border rounded-lg p-4">
                <div class="animate-pulse">
                  <div class="h-4 bg-gray-200 rounded mb-3"></div>
                  <div class="h-6 bg-gray-200 rounded mb-2"></div>
                  <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                </div>
              </div>
            </div>

            <!-- Rewards Grid -->
            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div 
                v-for="reward in loyaltyStore.availableRewards" 
                :key="reward.id"
                class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow duration-200"
              >
                <!-- Reward Icon -->
                <div class="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-lg mb-3">
                  <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"/>
                  </svg>
                </div>

                <!-- Reward Info -->
                <h4 class="font-semibold text-gray-800 mb-2">{{ reward.title }}</h4>
                <p class="text-sm text-gray-600 mb-3">{{ reward.description }}</p>
                
                <!-- Points Required -->
                <div class="flex items-center justify-between mb-3">
                  <span class="text-lg font-bold text-purple-600">{{ reward.pointsRequired }} điểm</span>
                  <span 
                    v-if="reward.discount" 
                    class="bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full"
                  >
                    {{ reward.discount }}% OFF
                  </span>
                </div>

                <!-- Redeem Button -->
                <button
                  @click="openRedemptionModal(reward)"
                  :disabled="!loyaltyStore.canRedeem(reward.pointsRequired)"
                  :class="[
                    'w-full py-2 px-4 rounded-lg font-medium transition-colors duration-200',
                    loyaltyStore.canRedeem(reward.pointsRequired)
                      ? 'bg-purple-600 hover:bg-purple-700 text-white'
                      : 'bg-gray-200 text-gray-500 cursor-not-allowed'
                  ]"
                >
                  {{ loyaltyStore.canRedeem(reward.pointsRequired) ? 'Đổi ngay' : 'Không đủ điểm' }}
                </button>
              </div>
            </div>

            <!-- Empty State -->
            <div v-if="!loyaltyStore.rewardsLoading && loyaltyStore.availableRewards.length === 0" 
                 class="text-center py-12">
              <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"/>
              </svg>
              <h3 class="text-lg font-medium text-gray-800 mb-2">Chưa có phần thưởng</h3>
              <p class="text-gray-600">Các phần thưởng sẽ sớm được cập nhật</p>
            </div>
          </div>

          <!-- History Tab -->
          <div v-if="activeTab === 'history'">
            <div class="mb-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">Lịch sử tích điểm</h3>
              <p class="text-gray-600">Theo dõi tất cả hoạt động tích điểm và đổi thưởng</p>
            </div>

            <!-- Loading -->
            <div v-if="loyaltyStore.historyLoading" class="space-y-4">
              <div v-for="i in 5" :key="i" class="animate-pulse flex items-center space-x-4 p-4 border rounded-lg">
                <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
                <div class="flex-1 space-y-2">
                  <div class="h-4 bg-gray-200 rounded w-1/4"></div>
                  <div class="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
                <div class="h-4 bg-gray-200 rounded w-16"></div>
              </div>
            </div>

            <!-- History List -->
            <div v-else class="space-y-4">
              <div 
                v-for="item in loyaltyStore.pointsHistory" 
                :key="item.id"
                class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors duration-200"
              >
                <div class="flex items-center space-x-4">
                  <!-- Action Icon -->
                  <div 
                    :class="[
                      'w-10 h-10 rounded-full flex items-center justify-center',
                      item.points > 0 ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
                    ]"
                  >
                    <svg v-if="item.points > 0" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 12L15 7H5L10 12Z"/>
                    </svg>
                    <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 8L5 13H15L10 8Z"/>
                    </svg>
                  </div>

                  <!-- Action Details -->
                  <div>
                    <div class="font-medium text-gray-800">{{ item.description }}</div>
                    <div class="text-sm text-gray-500">{{ formatDate(item.createdAt) }}</div>
                  </div>
                </div>

                <!-- Points -->
                <div 
                  :class="[
                    'text-lg font-bold',
                    item.points > 0 ? 'text-green-600' : 'text-red-600'
                  ]"
                >
                  {{ item.points > 0 ? '+' : '' }}{{ item.points }}
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="loyaltyStore.historyPagination.totalPages > 1" 
                 class="flex justify-center mt-6">
              <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                <button
                  v-for="page in loyaltyStore.historyPagination.totalPages"
                  :key="page"
                  @click="loadHistoryPage(page)"
                  :class="[
                    'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                    page === loyaltyStore.historyPagination.currentPage
                      ? 'z-10 bg-purple-50 border-purple-500 text-purple-600'
                      : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                  ]"
                >
                  {{ page }}
                </button>
              </nav>
            </div>

            <!-- Empty State -->
            <div v-if="!loyaltyStore.historyLoading && loyaltyStore.pointsHistory.length === 0" 
                 class="text-center py-12">
              <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
              </svg>
              <h3 class="text-lg font-medium text-gray-800 mb-2">Chưa có lịch sử</h3>
              <p class="text-gray-600">Bạn chưa có hoạt động tích điểm nào</p>
            </div>
          </div>

          <!-- Tiers Tab -->
          <div v-if="activeTab === 'tiers'">
            <div class="mb-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-2">Hệ thống hạng thành viên</h3>
              <p class="text-gray-600">Thăng hạng để nhận được nhiều ưu đãi hơn</p>
            </div>

            <div class="space-y-4">
              <div 
                v-for="tier in loyaltyStore.tierInfo" 
                :key="tier.level"
                :class="[
                  'border-2 rounded-lg p-6 transition-all duration-200',
                  tier.name === loyaltyStore.userPoints.tier 
                    ? 'border-purple-500 bg-purple-50' 
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center space-x-4">
                    <div 
                      class="w-12 h-12 rounded-full flex items-center justify-center text-white text-lg font-bold"
                      :style="{ backgroundColor: tier.color }"
                    >
                      {{ tier.name.charAt(0) }}
                    </div>
                    <div>
                      <h4 class="text-xl font-bold text-gray-800">{{ tier.name }}</h4>
                      <p class="text-gray-600">{{ tier.pointsRequired }} điểm trở lên</p>
                    </div>
                  </div>
                  
                  <div v-if="tier.name === loyaltyStore.userPoints.tier" 
                       class="bg-purple-500 text-white px-3 py-1 rounded-full text-sm font-medium">
                    Hạng hiện tại
                  </div>
                </div>

                <!-- Benefits -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-for="benefit in tier.benefits" :key="benefit" 
                       class="flex items-center space-x-2">
                    <svg class="w-4 h-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="text-sm text-gray-700">{{ benefit }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Redemption Modal -->
    <RedemptionModal 
      v-if="showRedemptionModal"
      :reward="selectedReward"
      @close="closeRedemptionModal"
      @redeem="handleRedeem"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useLoyaltyStore } from '@/stores/loyalty';
import RedemptionModal from '@/components/RedemptionModal.vue';

const loyaltyStore = useLoyaltyStore();

// Reactive data
const activeTab = ref('rewards');
const showRedemptionModal = ref(false);
const selectedReward = ref(null);

// Tabs configuration
const tabs = [
  { id: 'rewards', name: 'Đổi thưởng' },
  { id: 'history', name: 'Lịch sử' },
  { id: 'tiers', name: 'Hạng thành viên' }
];

// Computed
const tierInitial = computed(() => {
  return loyaltyStore.userPoints.tier?.charAt(0) || 'B';
});

// Methods
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const openRedemptionModal = (reward) => {
  selectedReward.value = reward;
  showRedemptionModal.value = true;
};

const closeRedemptionModal = () => {
  showRedemptionModal.value = false;
  selectedReward.value = null;
};

const handleRedeem = async (rewardId, pointsToSpend) => {
  try {
    await loyaltyStore.redeemReward(rewardId, pointsToSpend);
    closeRedemptionModal();
  } catch (error) {
    // Error already handled in store
  }
};

const loadHistoryPage = async (page) => {
  await loyaltyStore.fetchPointsHistory(page);
};

// Lifecycle
onMounted(async () => {
  await Promise.all([
    loyaltyStore.fetchUserPoints(),
    loyaltyStore.fetchAvailableRewards(),
    loyaltyStore.fetchPointsHistory(),
    loyaltyStore.fetchTierInfo()
  ]);
});
</script>

<style scoped>
/* Custom animations and styles can be added here */
</style>