import axiosInstance from './api';

export const loyaltyAPI = {
  async getUserPoints() {
    try {
      const response = await axiosInstance.get('/loyalty/points');
      return response.data;
    } catch (error) {
      console.error('Error fetching user points:', error);
      throw error;
    }
  },

  async getPointsHistory(page = 1, limit = 10) {
    try {
      const response = await axiosInstance.get('/loyalty/history', {
        params: { page, limit }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching points history:', error);
      throw error;
    }
  },

  async getAvailableRewards() {
    try {
      const response = await axiosInstance.get('/loyalty/rewards');
      return response.data;
    } catch (error) {
      console.error('Error fetching rewards:', error);
      throw error;
    }
  },

  async redeemReward(rewardId, pointsToSpend) {
    try {
      const response = await axiosInstance.post('/loyalty/redeem', {
        rewardId,
        pointsToSpend
      });
      return response.data;
    } catch (error) {
      console.error('Error redeeming reward:', error);
      throw error;
    }
  },

  async getTierInfo() {
    try {
      const response = await axiosInstance.get('/loyalty/tiers');
      return response.data;
    } catch (error) {
      console.error('Error fetching tier info:', error);
      throw error;
    }
  },

  async awardPoints(action, metadata = {}) {
    try {
      const response = await axiosInstance.post('/loyalty/award', {
        action, 
        metadata
      });
      return response.data;
    } catch (error) {
      console.error('Error awarding points:', error);
      throw error;
    }
  },

  async getLoyaltyStats() {
    try {
      const response = await axiosInstance.get('/loyalty/stats');
      return response.data;
    } catch (error) {
      console.error('Error fetching loyalty stats:', error);
      throw error;
    }
  }
};

export default loyaltyAPI;