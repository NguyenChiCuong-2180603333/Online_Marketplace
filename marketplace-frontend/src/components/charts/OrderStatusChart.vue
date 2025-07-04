<template>
  <div class="order-status-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">📊</div>
        <p>Không có dữ liệu trạng thái đơn hàng</p>
      </div>
      <div v-else class="chart-container">
        <div class="pie-chart-container">
          <!-- Simple pie chart using CSS -->
          <div class="pie-chart" :style="pieChartStyle">
            <div class="pie-center">
              <div class="total-orders">{{ totalOrders }}</div>
              <div class="total-label">Tổng đơn hàng</div>
            </div>
          </div>
        </div>
        
        <div class="legend-section">
          <div class="legend-list">
            <div 
              v-for="(item, index) in chartData" 
              :key="item.status"
              class="legend-item"
            >
              <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
              <div class="legend-info">
                <div class="legend-label">{{ getStatusLabel(item.status) }}</div>
                <div class="legend-stats">
                  <span class="legend-count">{{ item.count.toLocaleString() }}</span>
                  <span class="legend-percentage">({{ item.percentage.toFixed(1) }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  height: {
    type: Number,
    default: 300
  }
})

const statusColors = {
  PENDING: '#f59e0b',
  PROCESSING: '#3b82f6', 
  SHIPPED: '#8b5cf6',
  DELIVERED: '#10b981',
  CANCELLED: '#ef4444'
}

const chartData = computed(() => {
  const total = props.data.reduce((sum, item) => sum + item.count, 0)
  
  return props.data.map(item => ({
    status: item.status,
    count: item.count,
    percentage: total > 0 ? (item.count / total) * 100 : 0,
    color: statusColors[item.status] || '#6b7280'
  }))
})

const totalOrders = computed(() => {
  return props.data.reduce((sum, item) => sum + item.count, 0)
})

const pieChartStyle = computed(() => {
  if (chartData.value.length === 0) return {}
  
  let cumulativePercentage = 0
  const gradientStops = []
  
  chartData.value.forEach(item => {
    const startPercentage = cumulativePercentage
    const endPercentage = cumulativePercentage + item.percentage
    
    gradientStops.push(`${item.color} ${startPercentage}% ${endPercentage}%`)
    cumulativePercentage = endPercentage
  })
  
  return {
    background: `conic-gradient(${gradientStops.join(', ')})`
  }
})

const getStatusLabel = (status) => {
  const labels = {
    PENDING: 'Chờ xử lý',
    PROCESSING: 'Đang xử lý', 
    SHIPPED: 'Đã gửi hàng',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy'
  }
  return labels[status] || status
}
</script>

<style scoped>
.order-status-chart {
  width: 100%;
  height: 100%;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(107, 114, 128, 0.2);
}

.chart-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a0aec0;
}

.no-data-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.chart-container {
  display: flex;
  align-items: center;
  gap: 30px;
  height: 100%;
}

.pie-chart-container {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.pie-center {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(26, 26, 46, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(107, 114, 128, 0.3);
}

.total-orders {
  font-size: 1.5rem;
  font-weight: bold;
  color: #e2e8f0;
}

.total-label {
  font-size: 0.75rem;
  color: #a0aec0;
  text-align: center;
}

.legend-section {
  flex: 1;
  display: flex;
  align-items: center;
}

.legend-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: rgba(107, 114, 128, 0.1);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.legend-item:hover {
  background: rgba(107, 114, 128, 0.2);
  transform: translateX(4px);
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.legend-info {
  flex: 1;
}

.legend-label {
  color: #e2e8f0;
  font-weight: 500;
  font-size: 0.875rem;
}

.legend-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.legend-count {
  color: #a0aec0;
  font-size: 0.875rem;
  font-weight: 600;
}

.legend-percentage {
  color: #6b7280;
  font-size: 0.75rem;
}

@media (max-width: 768px) {
  .order-status-chart {
    padding: 15px;
  }
  
  .chart-container {
    flex-direction: column;
    gap: 20px;
  }
  
  .pie-chart {
    width: 150px;
    height: 150px;
  }
  
  .pie-center {
    width: 75px;
    height: 75px;
  }
  
  .total-orders {
    font-size: 1.25rem;
  }
  
  .total-label {
    font-size: 0.625rem;
  }
}
</style>