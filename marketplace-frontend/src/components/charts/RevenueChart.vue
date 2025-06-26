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

<!-- components/charts/ProductChart.vue -->
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

<!-- components/charts/OrderStatusChart.vue -->
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

<!-- components/charts/CustomerChart.vue -->
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
          <ResponsiveContainer width="100%" height="100%">
            <BarChart :data="chartData" margin="{ top: 20, right: 30, left: 20, bottom: 5 }">
              <XAxis dataKey="channel" tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <YAxis tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <Tooltip 
                contentStyle="{
                  backgroundColor: 'rgba(26, 26, 46, 0.95)',
                  border: '1px solid rgba(0, 212, 255, 0.3)',
                  borderRadius: '6px',
                  color: '#e2e8f0'
                }"
              />
              <Bar dataKey="customers" fill="#00d4ff" radius="[4, 4, 0, 0]" />
              <Bar dataKey="cost" fill="#8b5cf6" radius="[4, 4, 0, 0]" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <!-- Retention View -->
        <div v-else-if="view === 'retention'" class="retention-chart">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart :data="retentionData" margin="{ top: 20, right: 30, left: 20, bottom: 5 }">
              <XAxis dataKey="period" tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <YAxis tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <Tooltip 
                contentStyle="{
                  backgroundColor: 'rgba(26, 26, 46, 0.95)',
                  border: '1px solid rgba(0, 212, 255, 0.3)',
                  borderRadius: '6px',
                  color: '#e2e8f0'
                }"
              />
              <Line 
                type="monotone" 
                dataKey="retentionRate" 
                stroke="#00d4ff" 
                strokeWidth="3"
                dot="{ fill: '#00d4ff', strokeWidth: 2, r: 4 }"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <!-- Value View -->
        <div v-else class="value-chart">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart :data="valueData" layout="horizontal" margin="{ top: 20, right: 30, left: 20, bottom: 5 }">
              <XAxis type="number" tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <YAxis type="category" dataKey="segment" tick="{ fill: '#a0aec0', fontSize: 12 }" />
              <Tooltip 
                contentStyle="{
                  backgroundColor: 'rgba(26, 26, 46, 0.95)',
                  border: '1px solid rgba(0, 212, 255, 0.3)',
                  borderRadius: '6px',
                  color: '#e2e8f0'
                }"
              />
              <Bar dataKey="averageValue" fill="#10b981" radius="[0, 4, 4, 0]" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ResponsiveContainer, BarChart, LineChart, Bar, Line, XAxis, YAxis, Tooltip } from 'recharts'

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
</script>

<style scoped>
.customer-chart {
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

.acquisition-chart,
.retention-chart,
.value-chart {
  width: 100%;
  height: 100%;
}
</style>