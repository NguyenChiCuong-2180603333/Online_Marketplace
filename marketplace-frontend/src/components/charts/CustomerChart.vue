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