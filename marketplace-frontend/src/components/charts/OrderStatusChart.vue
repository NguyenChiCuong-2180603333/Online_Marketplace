<template>
  <div class="order-status-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">📋</div>
        <p>Không có dữ liệu đơn hàng</p>
      </div>
      <div v-else class="chart-container">
        <div class="pie-chart-section">
          <ResponsiveContainer width="60%" height="100%">
            <PieChart>
              <Pie
                :data="chartData"
                cx="50%"
                cy="50%"
                innerRadius="40"
                outerRadius="80"
                dataKey="count"
                label="false"
              >
                <Cell v-for="(entry, index) in chartData" :key="`cell-${index}`" :fill="entry.color" />
              </Pie>
              <Tooltip 
                contentStyle="{
                  backgroundColor: 'rgba(26, 26, 46, 0.95)',
                  border: '1px solid rgba(0, 212, 255, 0.3)',
                  borderRadius: '6px',
                  color: '#e2e8f0'
                }"
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
        
        <div class="legend-section">
          <div class="legend-list">
            <div 
              v-for="item in chartData" 
              :key="item.status"
              class="legend-item"
            >
              <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
              <div class="legend-info">
                <div class="legend-label">{{ item.status }}</div>
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
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts'

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

const chartData = computed(() => {
  return props.data.map(item => ({
    status: item.status,
    count: item.count,
    percentage: item.percentage,
    color: item.color
  }))
})
</script>

<style scoped>
.order-status-chart {
  width: 100%;
  height: 100%;
}

.chart-wrapper {
  position: relative;
  width: 100%;
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
}

.chart-container {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 1rem;
}

.pie-chart-section {
  flex: 1;
}

.legend-section {
  flex: 1;
  display: flex;
  align-items: center;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: rgba(16, 16, 24, 0.6);
  border-radius: 6px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.legend-info {
  flex: 1;
}

.legend-label {
  font-weight: 500;
  color: #e2e8f0;
  margin-bottom: 0.25rem;
}

.legend-stats {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.legend-count {
  color: #00d4ff;
  font-weight: 500;
}

.legend-percentage {
  color: #a0aec0;
  font-size: 0.875rem;
}
</style>

