<template>
  <div class="seller-analytics-page">
    <header class="page-header">
      <h1>📈 Thống kê & Báo cáo</h1>
      <p>Phân tích hiệu suất bán hàng và xu hướng kinh doanh</p>
    </header>

    <!-- Time Period Selector -->
    <div class="time-selector">
      <button 
        v-for="period in timePeriods" 
        :key="period.value"
        class="time-btn"
        :class="{ active: activePeriod === period.value }"
        @click="activePeriod = period.value"
      >
        {{ period.label }}
      </button>
    </div>

    <!-- Stats Overview -->
    <div class="stats-overview">
      <div class="stat-card revenue">
        <div class="stat-header">
          <h3>💰 Doanh thu</h3>
          <span class="trend-up">+12%</span>
        </div>
        <div class="stat-value">₫2,450,000</div>
        <div class="stat-subtitle">So với tháng trước</div>
      </div>

      <div class="stat-card orders">
        <div class="stat-header">
          <h3>📦 Đơn hàng</h3>
          <span class="trend-up">+8%</span>
        </div>
        <div class="stat-value">24</div>
        <div class="stat-subtitle">Đơn hàng hoàn thành</div>
      </div>

      <div class="stat-card conversion">
        <div class="stat-header">
          <h3>📊 Tỷ lệ chuyển đổi</h3>
          <span class="trend-down">-2%</span>
        </div>
        <div class="stat-value">3.2%</div>
        <div class="stat-subtitle">Từ lượt xem</div>
      </div>

      <div class="stat-card customers">
        <div class="stat-header">
          <h3>👥 Khách hàng</h3>
          <span class="trend-up">+15%</span>
        </div>
        <div class="stat-value">18</div>
        <div class="stat-subtitle">Khách hàng mới</div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <div class="chart-card main-chart">
        <h3>📈 Doanh thu theo thời gian</h3>
        <div class="chart-placeholder">
          <div class="chart-mock">
            <div class="chart-bars">
              <div class="bar" style="height: 60%"></div>
              <div class="bar" style="height: 80%"></div>
              <div class="bar" style="height: 45%"></div>
              <div class="bar" style="height: 90%"></div>
              <div class="bar" style="height: 70%"></div>
              <div class="bar" style="height: 85%"></div>
              <div class="bar" style="height: 95%"></div>
            </div>
            <div class="chart-labels">
              <span>T2</span>
              <span>T3</span>
              <span>T4</span>
              <span>T5</span>
              <span>T6</span>
              <span>T7</span>
              <span>CN</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <h3>🏆 Top sản phẩm bán chạy</h3>
        <div class="top-products">
          <div class="product-rank">
            <span class="rank">1</span>
            <span class="product-name">iPhone 15 Pro</span>
            <span class="sales">8 bán</span>
          </div>
          <div class="product-rank">
            <span class="rank">2</span>
            <span class="product-name">MacBook Air M2</span>
            <span class="sales">5 bán</span>
          </div>
          <div class="product-rank">
            <span class="rank">3</span>
            <span class="product-name">AirPods Pro</span>
            <span class="sales">3 bán</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Detailed Analytics -->
    <div class="detailed-analytics">
      <div class="analytics-card">
        <h3>🎯 Hiệu suất sản phẩm</h3>
        <div class="performance-metrics">
          <div class="metric">
            <span class="metric-label">Lượt xem trung bình</span>
            <span class="metric-value">156</span>
          </div>
          <div class="metric">
            <span class="metric-label">Thời gian xem trung bình</span>
            <span class="metric-value">2m 34s</span>
          </div>
          <div class="metric">
            <span class="metric-label">Tỷ lệ thêm vào giỏ</span>
            <span class="metric-value">12.4%</span>
          </div>
        </div>
      </div>

      <div class="analytics-card">
        <h3>📊 Phân tích khách hàng</h3>
        <div class="customer-insights">
          <div class="insight-item">
            <span class="insight-label">Khách hàng quay lại</span>
            <span class="insight-value">34%</span>
          </div>
          <div class="insight-item">
            <span class="insight-label">Đánh giá trung bình</span>
            <span class="insight-value">4.2/5</span>
          </div>
          <div class="insight-item">
            <span class="insight-label">Thời gian giao hàng TB</span>
            <span class="insight-value">2.3 ngày</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Coming Soon Notice -->
    <div class="coming-soon-notice">
      <div class="notice-content">
        <h3>🚀 Analytics Dashboard đang phát triển</h3>
        <p>Tính năng thống kê chi tiết sẽ có trong các phase tiếp theo với:</p>
        <div class="features-grid">
          <div class="feature-group">
            <h4>📊 Báo cáo Doanh thu</h4>
            <div class="feature-item">• Real-time revenue tracking</div>
            <div class="feature-item">• So sánh với kỳ trước</div>
            <div class="feature-item">• Dự đoán xu hướng AI</div>
            <div class="feature-item">• Export PDF/Excel</div>
          </div>
          <div class="feature-group">
            <h4>📦 Phân tích Sản phẩm</h4>
            <div class="feature-item">• Product performance metrics</div>
            <div class="feature-item">• Inventory optimization</div>
            <div class="feature-item">• Pricing recommendations</div>
            <div class="feature-item">• A/B testing tools</div>
          </div>
          <div class="feature-group">
            <h4>👥 Customer Analytics</h4>
            <div class="feature-item">• Customer lifetime value</div>
            <div class="feature-item">• Segmentation analysis</div>
            <div class="feature-item">• Churn prediction</div>
            <div class="feature-item">• Marketing attribution</div>
          </div>
          <div class="feature-group">
            <h4>🤖 AI Insights</h4>
            <div class="feature-item">• Automated insights</div>
            <div class="feature-item">• Anomaly detection</div>
            <div class="feature-item">• Forecasting models</div>
            <div class="feature-item">• Personalized recommendations</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activePeriod = ref('7d')

const timePeriods = [
  { value: '24h', label: '24 giờ' },
  { value: '7d', label: '7 ngày' },
  { value: '30d', label: '30 ngày' },
  { value: '3m', label: '3 tháng' },
  { value: '1y', label: '1 năm' }
]
</script>

<style scoped>
.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.page-header h1 {
  color: var(--text-primary, #ffffff);
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: var(--text-secondary, #a0aec0);
  font-size: 1rem;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.time-btn {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.time-btn:hover, .time-btn.active {
  background: rgba(0, 212, 255, 0.2);
  border-color: var(--text-accent, #00d4ff);
  color: var(--text-accent, #00d4ff);
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent, #00d4ff);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.stat-header h3 {
  color: var(--text-primary, #ffffff);
  font-size: 1rem;
  margin: 0;
}

.trend-up, .trend-down {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.trend-up {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.trend-down {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-accent, #00d4ff);
  margin-bottom: 0.5rem;
}

.stat-subtitle {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
}

.chart-card h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-mock {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: end;
  gap: 1rem;
  padding: 1rem 0;
}

.bar {
  flex: 1;
  background: linear-gradient(to top, var(--text-accent, #00d4ff), rgba(0, 212, 255, 0.5));
  border-radius: 4px 4px 0 0;
  min-height: 20px;
  transition: all 0.3s ease;
}

.bar:hover {
  background: linear-gradient(to top, #00c4ef, rgba(0, 196, 239, 0.7));
  transform: scaleY(1.1);
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  color: var(--text-secondary, #a0aec0);
  font-size: 0.8rem;
}

.top-products {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-rank {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.product-rank:hover {
  background: rgba(0, 212, 255, 0.1);
}

.rank {
  background: var(--text-accent, #00d4ff);
  color: #1a1a2e;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8rem;
}

.product-name {
  flex: 1;
  color: var(--text-primary, #ffffff);
  font-weight: 500;
}

.sales {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
}

.detailed-analytics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.analytics-card {
  background: rgba(26, 26, 46, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 2rem;
}

.analytics-card h3 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1.5rem;
  font-size: 1.1rem;
}

.performance-metrics, .customer-insights {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.metric, .insight-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.metric-label, .insight-label {
  color: var(--text-secondary, #a0aec0);
  font-size: 0.9rem;
}

.metric-value, .insight-value {
  color: var(--text-accent, #00d4ff);
  font-weight: 600;
  font-size: 1.1rem;
}

.coming-soon-notice {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
}

.notice-content h3 {
  color: var(--text-accent, #00d4ff);
  margin-bottom: 1rem;
}

.notice-content p {
  color: var(--text-secondary, #a0aec0);
  margin-bottom: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  text-align: left;
}

.feature-group h4 {
  color: var(--text-primary, #ffffff);
  margin-bottom: 1rem;
  font-size: 1rem;
}

.feature-item {
  color: var(--text-secondary, #a0aec0);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  font-size: 0.9rem;
}

.feature-item:last-child {
  border-bottom: none;
}

@media (max-width: 768px) {
  .charts-section {
    grid-template-columns: 1fr;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .detailed-analytics {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .time-selector {
    justify-content: center;
  }
}
</style>