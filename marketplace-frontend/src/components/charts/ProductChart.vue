<template>
  <div class="product-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">📦</div>
        <p>Không có dữ liệu sản phẩm</p>
      </div>
      <div v-else class="chart-container">
        <div class="simple-bar-chart">
          <div class="chart-bars">
            <div 
              v-for="(item, index) in chartData" 
              :key="index"
              class="chart-bar"
              :style="{ 
                height: `${(item.value / maxValue) * 80}%`,
                '--bar-color': `hsl(${280 + index * 30}, 60%, 50%)`
              }"
              :title="`${item.name}: ${formatValue(item.value)}`"
            >
              <div class="bar-fill"></div>
              <div class="bar-label">{{ item.name }}</div>
            </div>
          </div>
          <div class="chart-legend">
            <div class="legend-item">
              <div class="legend-color" style="background: #8b5cf6;"></div>
              <span>{{ getMetricLabel() }}</span>
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
  metric: {
    type: String,
    default: 'revenue'
  },
  height: {
    type: Number,
    default: 300
  }
})

const chartData = computed(() => {
  return props.data.map(product => ({
    name: product.name.length > 15 ? 
      product.name.substring(0, 15) + '...' : 
      product.name,
    value: product[props.metric] || 0
  }))
})

const maxValue = computed(() => {
  if (chartData.value.length === 0) return 1
  return Math.max(...chartData.value.map(item => item.value))
})

const formatValue = (value) => {
  if (props.metric === 'revenue') {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value)
  }
  return value.toLocaleString()
}

const getMetricLabel = () => {
  const labels = {
    revenue: 'Doanh thu',
    sales: 'Số lượng bán',
    views: 'Lượt xem',
    stock: 'Tồn kho'
  }
  return labels[props.metric] || props.metric
}
</script>

<style scoped>
.product-chart {
  width: 100%;
  height: 100%;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(139, 92, 246, 0.2);
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
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.simple-bar-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  padding: 20px 0;
  min-height: 200px;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-height: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chart-bar:hover {
  transform: scale(1.05);
}

.bar-fill {
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, var(--bar-color, #8b5cf6), rgba(139, 92, 246, 0.3));
  border-radius: 4px 4px 0 0;
  position: relative;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.bar-fill::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #8b5cf6;
  border-radius: 4px 4px 0 0;
}

.bar-label {
  margin-top: 8px;
  font-size: 0.75rem;
  color: #a0aec0;
  text-align: center;
  transform: rotate(-45deg);
  transform-origin: center;
  white-space: nowrap;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px 0;
  border-top: 1px solid rgba(139, 92, 246, 0.2);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e2e8f0;
  font-size: 0.875rem;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .product-chart {
    padding: 15px;
  }
  
  .chart-bars {
    gap: 4px;
    min-height: 150px;
  }
  
  .bar-label {
    font-size: 0.625rem;
  }
  
  .chart-legend {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
}
</style>