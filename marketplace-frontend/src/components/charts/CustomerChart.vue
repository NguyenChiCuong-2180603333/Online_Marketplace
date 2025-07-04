<template>
  <div class="customer-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">👥</div>
        <p>Không có dữ liệu khách hàng</p>
      </div>
      <div v-else class="chart-container">
        <!-- Acquisition View -->
        <div v-if="view === 'acquisition'" class="acquisition-chart">
          <div class="chart-bars">
            <div 
              v-for="(item, index) in chartData" 
              :key="index"
              class="chart-bar"
              :style="{ 
                height: `${(item.customers / maxCustomers) * 80}%`,
                '--bar-color': `hsl(${200 + index * 40}, 65%, 55%)`
              }"
              :title="`${item.channel}: ${item.customers} khách hàng`"
            >
              <div class="bar-fill"></div>
              <div class="bar-label">{{ item.channel }}</div>
            </div>
          </div>
        </div>

        <!-- Retention View -->
        <div v-else-if="view === 'retention'" class="retention-chart">
          <div class="line-chart">
            <svg width="100%" height="200" viewBox="0 0 400 200">
              <defs>
                <linearGradient id="retentionGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#00d4ff;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#00d4ff;stop-opacity:0.05" />
                </linearGradient>
              </defs>
              
              <!-- Grid lines -->
              <g class="grid">
                <line v-for="i in 5" :key="`h${i}`" 
                      :x1="0" :y1="i * 40" :x2="400" :y2="i * 40" 
                      stroke="rgba(160, 174, 192, 0.2)" stroke-width="1"/>
                <line v-for="i in 6" :key="`v${i}`" 
                      :x1="i * 80" :y1="0" :x2="i * 80" :y2="200" 
                      stroke="rgba(160, 174, 192, 0.2)" stroke-width="1"/>
              </g>
              
              <!-- Area -->
              <path :d="retentionAreaPath" fill="url(#retentionGradient)" />
              
              <!-- Line -->
              <path :d="retentionLinePath" fill="none" stroke="#00d4ff" stroke-width="3" />
              
              <!-- Points -->
              <circle v-for="(point, index) in retentionPoints" :key="index"
                      :cx="point.x" :cy="point.y" r="4" 
                      fill="#00d4ff" stroke="#ffffff" stroke-width="2" />
            </svg>
          </div>
        </div>

        <!-- Value View -->
        <div v-else class="value-chart">
          <div class="horizontal-bars">
            <div 
              v-for="(item, index) in valueData" 
              :key="index"
              class="horizontal-bar"
            >
              <div class="bar-info">
                <span class="bar-label">{{ item.segment }}</span>
                <span class="bar-value">{{ formatCurrency(item.averageValue * 1000000) }}</span>
              </div>
              <div class="bar-track">
                <div 
                  class="bar-fill-horizontal"
                  :style="{ 
                    width: `${(item.averageValue / maxValue) * 100}%`,
                    background: `hsl(${120 + index * 30}, 60%, 50%)`
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Legend -->
        <div class="chart-legend">
          <div class="legend-item" v-if="view === 'acquisition'">
            <div class="legend-color" style="background: #00d4ff;"></div>
            <span>Khách hàng mới</span>
          </div>
          <div class="legend-item" v-else-if="view === 'retention'">
            <div class="legend-color" style="background: #00d4ff;"></div>
            <span>Tỷ lệ giữ chân (%)</span>
          </div>
          <div class="legend-item" v-else>
            <div class="legend-color" style="background: #10b981;"></div>
            <span>Giá trị trung bình</span>
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
  view: {
    type: String,
    default: 'acquisition'
  },
  height: {
    type: Number,
    default: 300
  }
})

const chartData = computed(() => {
  if (props.view === 'acquisition') {
    return props.data.map(item => ({
      channel: item.channel,
      customers: item.customers,
      cost: Math.round(item.cost / 1000000) // Convert to millions
    }))
  }
  return props.data
})

const retentionData = computed(() => {
  if (props.view === 'retention') {
    return props.data.map(item => ({
      period: item.period,
      retentionRate: ((item.retained / item.total) * 100).toFixed(1)
    }))
  }
  return []
})

const valueData = computed(() => {
  if (props.view === 'value') {
    return props.data.map(item => ({
      segment: item.segment,
      averageValue: Math.round(item.averageValue / 1000000) // Convert to millions
    }))
  }
  return []
})

const maxCustomers = computed(() => {
  if (chartData.value.length === 0) return 1
  return Math.max(...chartData.value.map(item => item.customers))
})

const maxValue = computed(() => {
  if (valueData.value.length === 0) return 1
  return Math.max(...valueData.value.map(item => item.averageValue))
})

const retentionPoints = computed(() => {
  if (retentionData.value.length === 0) return []
  
  return retentionData.value.map((item, index) => ({
    x: (index / (retentionData.value.length - 1)) * 400,
    y: 200 - (parseFloat(item.retentionRate) / 100) * 200
  }))
})

const retentionLinePath = computed(() => {
  if (retentionPoints.value.length === 0) return ''
  
  let path = `M ${retentionPoints.value[0].x} ${retentionPoints.value[0].y}`
  for (let i = 1; i < retentionPoints.value.length; i++) {
    path += ` L ${retentionPoints.value[i].x} ${retentionPoints.value[i].y}`
  }
  return path
})

const retentionAreaPath = computed(() => {
  if (retentionPoints.value.length === 0) return ''
  
  let path = `M ${retentionPoints.value[0].x} 200`
  path += ` L ${retentionPoints.value[0].x} ${retentionPoints.value[0].y}`
  
  for (let i = 1; i < retentionPoints.value.length; i++) {
    path += ` L ${retentionPoints.value[i].x} ${retentionPoints.value[i].y}`
  }
  
  path += ` L ${retentionPoints.value[retentionPoints.value.length - 1].x} 200 Z`
  return path
})

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
</script>

<style scoped>
.customer-chart {
  width: 100%;
  height: 100%;
  background: rgba(26, 26, 46, 0.5);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
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

.acquisition-chart,
.retention-chart,
.value-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
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
  background: linear-gradient(to top, var(--bar-color, #00d4ff), rgba(0, 212, 255, 0.3));
  border-radius: 4px 4px 0 0;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 212, 255, 0.3);
}

.bar-label {
  margin-top: 8px;
  font-size: 0.75rem;
  color: #a0aec0;
  text-align: center;
  white-space: nowrap;
}

.line-chart {
  flex: 1;
  padding: 20px 0;
}

.horizontal-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.horizontal-bar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bar-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bar-label {
  font-size: 0.875rem;
  color: #e2e8f0;
  font-weight: 500;
}

.bar-value {
  font-size: 0.875rem;
  color: #a0aec0;
}

.bar-track {
  height: 8px;
  background: rgba(107, 114, 128, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill-horizontal {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px 0;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
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
  .customer-chart {
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