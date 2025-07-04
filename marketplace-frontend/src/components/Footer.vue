<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-brand">
        <div class="footer-logo">🚀</div>
        <div class="footer-title">CosmicMarket</div>
        <div class="footer-slogan">Khám phá vũ trụ mua sắm trực tuyến</div>
      </div>
      <div class="footer-links">
        <h4>Liên kết</h4>
        <router-link to="/about">Giới thiệu</router-link>
        <router-link to="/contact">Liên hệ</router-link>
        <router-link to="/help">Trợ giúp</router-link>
      </div>
      <div class="footer-support">
        <h4>Hỗ trợ</h4>
        <a href="tel:+84123456789">📞 0123 456 789</a>
        <a href="mailto:support@cosmic.com">📧 support@cosmic.com</a>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2025 CosmicMarket. Mọi quyền được bảo lưu.</p>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer',
}
</script>

<style scoped>
.app-footer {
  background: rgba(26, 26, 46, 0.98);
  color: #fff;
  border-top: 1px solid rgba(0, 212, 255, 0.15);
  padding-top: 2rem;
  margin-top: 3rem;
}
.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem 2rem 1rem;
  gap: 2rem;
  flex-wrap: wrap;
}
.footer-brand {
  flex: 1 1 220px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
}
.footer-logo {
  font-size: 2.2rem;
  margin-bottom: 0.2rem;
  filter: drop-shadow(0 0 10px #00d4ff88);
}
.footer-title {
  font-size: 1.3rem;
  font-weight: bold;
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.footer-slogan {
  font-size: 0.95rem;
  color: var(--text-secondary);
  margin-top: 0.2rem;
}
.footer-links,
.footer-support {
  flex: 1 1 180px;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.footer-links h4,
.footer-support h4 {
  margin-bottom: 0.5rem;
  color: var(--text-accent);
  font-size: 1.05rem;
}
.footer-links a,
.footer-links router-link,
.footer-support a {
  color: #fff;
  text-decoration: none;
  font-size: 0.97rem;
  transition: color 0.2s;
}
.footer-links a:hover,
.footer-links router-link:hover,
.footer-support a:hover {
  color: var(--text-accent);
}
.footer-bottom {
  border-top: 1px solid rgba(0, 212, 255, 0.08);
  text-align: center;
  padding: 1rem 0 0.5rem 0;
  color: var(--text-secondary);
  font-size: 0.95rem;
}
@media (max-width: 800px) {
  .footer-container {
    flex-direction: column;
    align-items: stretch;
    gap: 1.5rem;
  }
  .footer-brand,
  .footer-links,
  .footer-support {
    align-items: flex-start;
  }
}
</style>
