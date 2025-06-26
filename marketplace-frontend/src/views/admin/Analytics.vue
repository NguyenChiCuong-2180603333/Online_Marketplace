<template>
  <div class="analytics-dashboard">
    <div class="container mx-auto px-4 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">📊 Analytics Dashboard</h1>
        <p class="text-gray-600">Real-time insights and performance metrics</p>
      </div>

      <!-- Time Range Selector -->
      <div class="mb-6">
        <div class="flex items-center space-x-4">
          <label class="text-sm font-medium text-gray-700">Time Range:</label>
          <select v-model="selectedTimeRange" @change="updateTimeRange" class="border border-gray-300 rounded-md px-3 py-2">
            <option value="7d">Last 7 days</option>
            <option value="30d">Last 30 days</option>
            <option value="90d">Last 90 days</option>
            <option value="1y">Last year</option>
          </select>
          <button @click="refreshData" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
            🔄 Refresh
          </button>
        </div>
      </div>

      <!-- Key Metrics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-green-100 text-green-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"/>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Total Revenue</p>
              <p class="text-2xl font-bold text-gray-900">{{ formatCurrency(metrics.totalRevenue) }}</p>
              <p class="text-sm text-green-600">+{{ metrics.revenueGrowth }}% vs last period</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-blue-100 text-blue-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Total Orders</p>
              <p class="text-2xl font-bold text-gray-900">{{ metrics.totalOrders }}</p>
              <p class="text-sm text-blue-600">+{{ metrics.orderGrowth }}% vs last period</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-purple-100 text-purple-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Active Users</p>
              <p class="text-2xl font-bold text-gray-900">{{ metrics.activeUsers }}</p>
              <p class="text-sm text-purple-600">+{{ metrics.userGrowth }}% vs last period</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center">
            <div class="p-3 rounded-full bg-orange-100 text-orange-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-600">Conversion Rate</p>
              <p class="text-2xl font-bold text-gray-900">{{ metrics.conversionRate }}%</p>
              <p class="text-sm text-orange-600">+{{ metrics.conversionGrowth }}% vs last period</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <!-- Revenue Chart -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Revenue Trend</h3>
          <div class="h-64">
            <canvas ref="revenueChart"></canvas>
          </div>
        </div>

        <!-- Orders Chart -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Orders Trend</h3>
          <div class="h-64">
            <canvas ref="ordersChart"></canvas>
          </div>
        </div>
      </div>

      <!-- Top Products & Categories -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <!-- Top Products -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Top Selling Products</h3>
          <div class="space-y-4">
            <div v-for="product in topProducts" :key="product.id" class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <img :src="product.imageUrl" :alt="product.name" class="w-10 h-10 rounded object-cover">
                <div>
                  <p class="font-medium text-gray-900">{{ product.name }}</p>
                  <p class="text-sm text-gray-600">{{ product.category }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-semibold text-gray-900">{{ product.sales }}</p>
                <p class="text-sm text-gray-600">{{ formatCurrency(product.revenue) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Top Categories -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Top Categories</h3>
          <div class="space-y-4">
            <div v-for="category in topCategories" :key="category.name" class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 rounded bg-gray-100 flex items-center justify-center">
                  <span class="text-lg">{{ category.icon }}</span>
                </div>
                <div>
                  <p class="font-medium text-gray-900">{{ category.name }}</p>
                  <p class="text-sm text-gray-600">{{ category.productCount }} products</p>
                </div>
              </div>
              <div class="text-right">
                <p class="font-semibold text-gray-900">{{ category.sales }}</p>
                <p class="text-sm text-gray-600">{{ formatCurrency(category.revenue) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- User Activity & Loyalty -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- User Activity -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">User Activity</h3>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-gray-600">New Registrations</span>
              <span class="font-semibold">{{ userActivity.newRegistrations }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Active Sessions</span>
              <span class="font-semibold">{{ userActivity.activeSessions }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Avg. Session Duration</span>
              <span class="font-semibold">{{ userActivity.avgSessionDuration }}m</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Bounce Rate</span>
              <span class="font-semibold">{{ userActivity.bounceRate }}%</span>
            </div>
          </div>
        </div>

        <!-- Loyalty Program -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Loyalty Program</h3>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Total Points Awarded</span>
              <span class="font-semibold">{{ loyaltyStats.totalPointsAwarded.toLocaleString() }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Points Redeemed</span>
              <span class="font-semibold">{{ loyaltyStats.pointsRedeemed.toLocaleString() }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Active Members</span>
              <span class="font-semibold">{{ loyaltyStats.activeMembers }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">Avg. Points per User</span>
              <span class="font-semibold">{{ loyaltyStats.avgPointsPerUser }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'

// Register Chart.js components
Chart.register(...registerables)

// Reactive data
const selectedTimeRange = ref('30d')
const metrics = ref({
  totalRevenue: 0,
  revenueGrowth: 0,
  totalOrders: 0,
  orderGrowth: 0,
  activeUsers: 0,
  userGrowth: 0,
  conversionRate: 0,
  conversionGrowth: 0
})

const topProducts = ref([])
const topCategories = ref([])
const userActivity = ref({
  newRegistrations: 0,
  activeSessions: 0,
  avgSessionDuration: 0,
  bounceRate: 0
})
const loyaltyStats = ref({
  totalPointsAwarded: 0,
  pointsRedeemed: 0,
  activeMembers: 0,
  avgPointsPerUser: 0
})

// Chart references
const revenueChart = ref(null)
const ordersChart = ref(null)

// Methods
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const updateTimeRange = async () => {
  await loadAnalyticsData()
}

const refreshData = async () => {
  await loadAnalyticsData()
}

const loadAnalyticsData = async () => {
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Mock data
    metrics.value = {
      totalRevenue: 125000000,
      revenueGrowth: 12.5,
      totalOrders: 1250,
      orderGrowth: 8.3,
      activeUsers: 850,
      userGrowth: 15.2,
      conversionRate: 3.2,
      conversionGrowth: 2.1
    }

    topProducts.value = [
      { id: 1, name: 'Laptop Gaming Pro', category: 'Electronics', sales: 45, revenue: 22500000, imageUrl: '/placeholder.jpg' },
      { id: 2, name: 'Smartphone Galaxy', category: 'Electronics', sales: 38, revenue: 19000000, imageUrl: '/placeholder.jpg' },
      { id: 3, name: 'Wireless Headphones', category: 'Audio', sales: 32, revenue: 9600000, imageUrl: '/placeholder.jpg' }
    ]

    topCategories.value = [
      { name: 'Electronics', icon: '📱', sales: 120, revenue: 60000000, productCount: 45 },
      { name: 'Fashion', icon: '👕', sales: 85, revenue: 42500000, productCount: 120 },
      { name: 'Home & Garden', icon: '🏠', sales: 65, revenue: 32500000, productCount: 80 }
    ]

    userActivity.value = {
      newRegistrations: 125,
      activeSessions: 450,
      avgSessionDuration: 8.5,
      bounceRate: 35.2
    }

    loyaltyStats.value = {
      totalPointsAwarded: 125000,
      pointsRedeemed: 45000,
      activeMembers: 850,
      avgPointsPerUser: 147
    }

    await nextTick()
    initializeCharts()
    
  } catch (error) {
    console.error('Error loading analytics data:', error)
  }
}

const initializeCharts = () => {
  // Revenue Chart
  if (revenueChart.value) {
    const ctx = revenueChart.value.getContext('2d')
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
          label: 'Revenue',
          data: [65, 78, 90, 85, 95, 110],
          borderColor: 'rgb(59, 130, 246)',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    })
  }

  // Orders Chart
  if (ordersChart.value) {
    const ctx = ordersChart.value.getContext('2d')
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
          label: 'Orders',
          data: [120, 150, 180, 160, 200, 220],
          backgroundColor: 'rgba(147, 51, 234, 0.8)'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true
          }
        }
      }
    })
  }
}

// Lifecycle
onMounted(() => {
  loadAnalyticsData()
})
</script>

<style scoped>
.analytics-dashboard {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style> 