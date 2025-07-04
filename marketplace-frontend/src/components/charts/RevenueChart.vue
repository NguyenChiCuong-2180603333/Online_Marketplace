<template>
  <div class="revenue-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">📊</div>
        <p>Không có dữ liệu để hiển thị</p>
      </div>
      <div v-else class="chart-container">
        <LineChart
          v-if="type === 'line'"
          :data="chartData"
          :options="chartOptions"
          :width="400"
          :height="height"
        />
        <BarChart
          v-else-if="type === 'bar'"
          :data="chartData"
          :options="chartOptions"
          :width="400"
          :height="height"
        />
        <AreaChart
          v-else
          :data="chartData"
          :options="chartOptions"
          :width="400"
          :height="height"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { LineChart, BarChart, AreaChart } from 'recharts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'line'
  },
  height: {
    type: Number,
    default: 300
  }
})

const chartData = computed(() => {
  return props.data.map(item => ({
    date: new Date(item.date).toLocaleDateString('vi-VN', { 
      month: 'short', 
      day: 'numeric' 
    }),
    revenue: Math.round(item.revenue / 1000000), // Convert to millions
    orders: item.orders,
    visitors: item.visitors
  }))
})

const chartOptions = computed(() => ({
  responsive: true,
  plugins: {
    legend: {
      display: true,
      labels: {
        color: '#e2e8f0'
      }
    },
    tooltip: {
      backgroundColor: 'rgba(26, 26, 46, 0.95)',
      titleColor: '#00d4ff',
      bodyColor: '#e2e8f0',
      borderColor: 'rgba(0, 212, 255, 0.3)',
      borderWidth: 1
    }
  },
  scales: {
    x: {
      grid: {
        color: 'rgba(107, 114, 128, 0.2)'
      },
      ticks: {
        color: '#a0aec0'
      }
    },
    y: {
      grid: {
        color: 'rgba(107, 114, 128, 0.2)'
      },
      ticks: {
        color: '#a0aec0',
        callback: function(value) {
          return value + 'M'
        }
      }
    }
  }
}))
</script>

<style scoped>
.revenue-chart {
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
  width: 100%;
  height: 100%;
}
</style>

