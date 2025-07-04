<template>
  <div class="product-chart">
    <div class="chart-wrapper" :style="{ height: height + 'px' }">
      <div v-if="!data || data.length === 0" class="no-data">
        <div class="no-data-icon">📦</div>
        <p>Không có dữ liệu sản phẩm</p>
      </div>
      <div v-else class="chart-container">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart :data="chartData" margin="{ top: 20, right: 30, left: 20, bottom: 5 }">
            <XAxis dataKey="name" tick="{ fill: '#a0aec0', fontSize: 12 }" />
            <YAxis tick="{ fill: '#a0aec0', fontSize: 12 }" />
            <Tooltip 
              contentStyle="{
                backgroundColor: 'rgba(26, 26, 46, 0.95)',
                border: '1px solid rgba(0, 212, 255, 0.3)',
                borderRadius: '6px',
                color: '#e2e8f0'
              }"
            />
            <Bar dataKey="value" fill="#00d4ff" radius="[4, 4, 0, 0]" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts'

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
    name: product.name.length > 15 ? product.name.substring(0, 15) + '...' : product.name,
    value: getMetricValue(product, props.metric),
    fullName: product.name
  }))
})

const getMetricValue = (product, metric) => {
  switch (metric) {
    case 'revenue':
      return Math.round(product.revenue / 1000000) // Convert to millions
    case 'orders':
      return product.orders
    case 'views':
      return product.views
    case 'conversion':
      return product.conversion
    default:
      return product.revenue
  }
}
</script>

<style scoped>
.product-chart {
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

