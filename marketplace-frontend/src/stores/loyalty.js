import { defineStore } from 'pinia'
import { loyaltyAPI } from '@/services/loyaltyAPI'
import { useNotificationStore } from './notifications'

export const useLoyaltyStore = defineStore('loyalty', {
  state: () => ({
    userPoints: {
      current: 0,
      lifetime: 0,
      tier: 'Bronze',
      tierLevel: 1,
      nextTierPoints: 1000,
      pointsToNextTier: 1000,
    },

    pointsHistory: [],
    historyLoading: false,
    historyPagination: {
      currentPage: 1,
      totalPages: 1,
      totalItems: 0,
    },

    availableRewards: [],
    rewardsLoading: false,

    tierInfo: [],

    pointsLoading: false,
    redemptionLoading: false,

    showPointsAnimation: false,
    lastPointsEarned: 0,
  }),

  getters: {
    tierColor() {
      const colors = {
        Bronze: '#CD7F32',
        Silver: '#C0C0C0',
        Gold: '#FFD700',
        Platinum: '#E5E4E2',
        Diamond: '#B9F2FF',
      }
      return colors[this.userPoints.tier] || '#CD7F32'
    },

    tierProgress() {
      if (this.userPoints.pointsToNextTier === 0) return 100
      const earned = this.userPoints.nextTierPoints - this.userPoints.pointsToNextTier
      return Math.round((earned / this.userPoints.nextTierPoints) * 100)
    },

    formattedPoints() {
      return this.userPoints.current.toLocaleString('vi-VN')
    },

    canRedeem: (state) => (requiredPoints) => {
      return state.userPoints.current >= requiredPoints
    },
  },

  actions: {
    async fetchUserPoints() {
      this.pointsLoading = true
      try {
        const data = await loyaltyAPI.getUserPoints()
        this.userPoints = { ...this.userPoints, ...data }
      } catch (error) {
        console.error('Failed to fetch user points:', error)
        const notification = useNotificationStore()
        notification.error('Không thể tải thông tin điểm thưởng')
      } finally {
        this.pointsLoading = false
      }
    },

    async fetchPointsHistory(page = 1) {
      this.historyLoading = true
      try {
        const data = await loyaltyAPI.getPointsHistory(page, 10)
        this.pointsHistory = data.history || []
        this.historyPagination = {
          currentPage: data.currentPage || 1,
          totalPages: data.totalPages || 1,
          totalItems: data.totalItems || 0,
        }
      } catch (error) {
        console.error('Failed to fetch points history:', error)
      } finally {
        this.historyLoading = false
      }
    },

    async fetchAvailableRewards() {
      this.rewardsLoading = true
      try {
        const data = await loyaltyAPI.getAvailableRewards()
        this.availableRewards = data.rewards || []
      } catch (error) {
        console.error('Failed to fetch rewards:', error)
      } finally {
        this.rewardsLoading = false
      }
    },

    async fetchTierInfo() {
      try {
        const data = await loyaltyAPI.getTierInfo()
        this.tierInfo = data.tiers || []
      } catch (error) {
        console.error('Failed to fetch tier info:', error)
      }
    },

    async redeemReward(rewardId, pointsToSpend) {
      this.redemptionLoading = true
      const notification = useNotificationStore()

      try {
        const result = await loyaltyAPI.redeemReward(rewardId, pointsToSpend)

        this.userPoints.current -= pointsToSpend

        this.pointsHistory.unshift({
          id: Date.now(),
          action: 'redeem',
          points: -pointsToSpend,
          description: result.description || 'Đổi thưởng',
          createdAt: new Date().toISOString(),
        })

        notification.success(`Đổi thưởng thành công! Voucher: ${result.voucherCode}`)
        return result
      } catch (error) {
        console.error('Failed to redeem reward:', error)
        notification.error('Đổi thưởng thất bại. Vui lòng thử lại!')
        throw error
      } finally {
        this.redemptionLoading = false
      }
    },

    async awardPoints(action, metadata = {}) {
      try {
        const result = await loyaltyAPI.awardPoints(action, metadata)

        if (result.pointsAwarded > 0) {
          this.userPoints.current += result.pointsAwarded
          this.userPoints.lifetime += result.pointsAwarded

          if (result.tierUpgrade) {
            this.userPoints.tier = result.newTier
            this.userPoints.tierLevel = result.newTierLevel
          }

          this.showPointsAnimation = true
          this.lastPointsEarned = result.pointsAwarded

          setTimeout(() => {
            this.showPointsAnimation = false
          }, 3000)

          this.pointsHistory.unshift({
            id: Date.now(),
            action,
            points: result.pointsAwarded,
            description: result.description,
            createdAt: new Date().toISOString(),
          })

          const notification = useNotificationStore()
          notification.success(`+${result.pointsAwarded} điểm thưởng!`)
        }

        return result
      } catch (error) {
        console.error('Failed to award points:', error)
      }
    },

    async initializeLoyalty() {
      await Promise.all([this.fetchUserPoints(), this.fetchTierInfo()])
    },

    resetLoyalty() {
      this.$reset()
    },
  },
})
