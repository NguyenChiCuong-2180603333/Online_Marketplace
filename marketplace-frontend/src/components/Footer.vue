<template>
  <footer class="app-footer">
    <!-- Main Footer Content -->
    <div class="footer-main">
      <div class="container">
        <div class="footer-grid">
          <!-- Company Info -->
          <div class="footer-section company-info">
            <div class="footer-logo">
              <div class="logo-container">
                <div class="logo-icon">🚀</div>
                <div class="brand-text">
                  <h3>CosmicMarket</h3>
                  <span class="tagline">Future Commerce</span>
                </div>
              </div>
            </div>
            <p class="company-description">
              Nền tảng thương mại điện tử tiên tiến nhất Việt Nam, mang đến trải nghiệm mua sắm 
              tương lai với công nghệ AI và giao diện vũ trụ độc đáo.
            </p>
            
            <!-- Social Media -->
            <div class="social-media">
              <h4>Kết nối với chúng tôi</h4>
              <div class="social-links">
                <a href="#" class="social-link facebook" aria-label="Facebook">
                  <span class="social-icon">📘</span>
                </a>
                <a href="#" class="social-link instagram" aria-label="Instagram">
                  <span class="social-icon">📷</span>
                </a>
                <a href="#" class="social-link youtube" aria-label="YouTube">
                  <span class="social-icon">📺</span>
                </a>
                <a href="#" class="social-link tiktok" aria-label="TikTok">
                  <span class="social-icon">🎵</span>
                </a>
                <a href="#" class="social-link zalo" aria-label="Zalo">
                  <span class="social-icon">💬</span>
                </a>
              </div>
            </div>
          </div>
          
          <!-- Quick Links -->
          <div class="footer-section">
            <h4 class="footer-title">Liên kết nhanh</h4>
            <ul class="footer-links">
              <li><router-link to="/products">📦 Sản phẩm</router-link></li>
              <li><router-link to="/categories">🏪 Danh mục</router-link></li>
              <li><router-link to="/deals">🔥 Khuyến mãi</router-link></li>
              <li><router-link to="/flash-sale">⚡ Flash Sale</router-link></li>
              <li><router-link to="/bestsellers">⭐ Bán chạy</router-link></li>
              <li><router-link to="/new-arrivals">🆕 Hàng mới</router-link></li>
            </ul>
          </div>
          
          <!-- Customer Service -->
          <div class="footer-section">
            <h4 class="footer-title">Dịch vụ khách hàng</h4>
            <ul class="footer-links">
              <li><router-link to="/help">❓ Trung tâm trợ giúp</router-link></li>
              <li><router-link to="/shipping">🚚 Chính sách giao hàng</router-link></li>
              <li><router-link to="/returns">↩️ Đổi trả hàng</router-link></li>
              <li><router-link to="/warranty">🛡️ Bảo hành</router-link></li>
              <li><router-link to="/size-guide">📏 Hướng dẫn chọn size</router-link></li>
              <li><router-link to="/contact">📞 Liên hệ</router-link></li>
            </ul>
          </div>
          
          <!-- Account & Security -->
          <div class="footer-section">
            <h4 class="footer-title">Tài khoản & Bảo mật</h4>
            <ul class="footer-links">
              <li><router-link to="/login">🔑 Đăng nhập</router-link></li>
              <li><router-link to="/register">🚀 Đăng ký</router-link></li>
              <li><router-link to="/profile">👤 Hồ sơ cá nhân</router-link></li>
              <li><router-link to="/orders">📋 Đơn hàng của tôi</router-link></li>
              <li><router-link to="/wishlist">❤️ Yêu thích</router-link></li>
              <li><router-link to="/security">🔐 Bảo mật tài khoản</router-link></li>
            </ul>
          </div>
          
          <!-- Contact & Support -->
          <div class="footer-section contact-section">
            <h4 class="footer-title">Thông tin liên hệ</h4>
            <div class="contact-info">
              <div class="contact-item">
                <span class="contact-icon">📍</span>
                <div class="contact-details">
                  <strong>Địa chỉ:</strong>
                  <p>123 Đường Nguyễn Huệ, Quận 1<br>TP. Hồ Chí Minh, Việt Nam</p>
                </div>
              </div>
              
              <div class="contact-item">
                <span class="contact-icon">📞</span>
                <div class="contact-details">
                  <strong>Hotline:</strong>
                  <p>
                    <a href="tel:1900123456">1900 123 456</a><br>
                    <small>(8:00 - 22:00, tất cả các ngày)</small>
                  </p>
                </div>
              </div>
              
              <div class="contact-item">
                <span class="contact-icon">✉️</span>
                <div class="contact-details">
                  <strong>Email:</strong>
                  <p>
                    <a href="mailto:support@cosmicmarket.vn">support@cosmicmarket.vn</a><br>
                    <a href="mailto:business@cosmicmarket.vn">business@cosmicmarket.vn</a>
                  </p>
                </div>
              </div>
            </div>
            
            <!-- Newsletter Signup -->
            <div class="newsletter-signup">
              <h5>Đăng ký nhận tin</h5>
              <p>Nhận thông báo về khuyến mãi và sản phẩm mới</p>
              <form @submit.prevent="subscribeNewsletter" class="newsletter-form">
                <div class="input-group">
                  <input
                    v-model="newsletterEmail"
                    type="email"
                    placeholder="Email của bạn"
                    class="newsletter-input"
                    required
                  />
                  <button type="submit" class="newsletter-btn" :disabled="subscribing">
                    {{ subscribing ? '🔄' : '📧' }}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- App Download Section -->
    <div class="app-download-section">
      <div class="container">
        <div class="app-download-content">
          <div class="app-info">
            <h4>📱 Tải ứng dụng CosmicMarket</h4>
            <p>Trải nghiệm mua sắm tối ưu trên di động</p>
          </div>
          <div class="app-badges">
            <a href="#" class="app-badge ios">
              <img src="/app-store-badge.png" alt="Download on App Store" />
            </a>
            <a href="#" class="app-badge android">
              <img src="/google-play-badge.png" alt="Get it on Google Play" />
            </a>
          </div>
          <div class="qr-code">
            <div class="qr-image">📱</div>
            <p>Quét QR để tải app</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Security & Trust -->
    <div class="trust-section">
      <div class="container">
        <div class="trust-content">
          <div class="trust-item">
            <span class="trust-icon">🔒</span>
            <div class="trust-text">
              <strong>Thanh toán bảo mật</strong>
              <p>Mã hóa SSL 256-bit</p>
            </div>
          </div>
          
          <div class="trust-item">
            <span class="trust-icon">🚚</span>
            <div class="trust-text">
              <strong>Giao hàng nhanh</strong>
              <p>Toàn quốc 1-3 ngày</p>
            </div>
          </div>
          
          <div class="trust-item">
            <span class="trust-icon">↩️</span>
            <div class="trust-text">
              <strong>Đổi trả 30 ngày</strong>
              <p>Miễn phí đổi trả</p>
            </div>
          </div>
          
          <div class="trust-item">
            <span class="trust-icon">🎧</span>
            <div class="trust-text">
              <strong>Hỗ trợ 24/7</strong>
              <p>Tư vấn mọi lúc</p>
            </div>
          </div>
          
          <div class="trust-item">
            <span class="trust-icon">✅</span>
            <div class="trust-text">
              <strong>Hàng chính hãng</strong>
              <p>100% authentic</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Bottom Footer -->
    <div class="footer-bottom">
      <div class="container">
        <div class="bottom-content">
          <div class="copyright">
            <p>&copy; {{ currentYear }} CosmicMarket. Tất cả quyền được bảo lưu.</p>
            <p class="company-info-text">
              Công ty TNHH CosmicMarket Việt Nam | GPKD: 0123456789 do Sở KH&ĐT TP.HCM cấp
            </p>
          </div>
          
          <div class="legal-links">
            <router-link to="/terms">📋 Điều khoản dịch vụ</router-link>
            <router-link to="/privacy">🔒 Chính sách bảo mật</router-link>
            <router-link to="/cookies">🍪 Chính sách Cookie</router-link>
            <router-link to="/sitemap">🗺️ Sơ đồ trang</router-link>
          </div>
          
          <div class="payment-methods">
            <span class="payment-label">Phương thức thanh toán:</span>
            <div class="payment-icons">
              <span class="payment-icon" title="Visa">💳</span>
              <span class="payment-icon" title="Mastercard">💳</span>
              <span class="payment-icon" title="JCB">💳</span>
              <span class="payment-icon" title="MoMo">📱</span>
              <span class="payment-icon" title="ZaloPay">💸</span>
              <span class="payment-icon" title="VNPay">🏦</span>
              <span class="payment-icon" title="COD">💵</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Back to Top Button -->
    <button
      v-show="showBackToTop"
      @click="scrollToTop"
      class="back-to-top"
      aria-label="Về đầu trang"
    >
      🚀
    </button>
  </footer>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'

export default {
  name: 'Footer',
  setup() {
    // Reactive data
    const newsletterEmail = ref('')
    const subscribing = ref(false)
    const showBackToTop = ref(false)
    
    // Computed
    const currentYear = computed(() => new Date().getFullYear())
    
    // Methods
    const subscribeNewsletter = async () => {
      if (!newsletterEmail.value) return
      
      try {
        subscribing.value = true
        
        // Mock API call
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        alert('Đăng ký nhận tin thành công! Cảm ơn bạn đã theo dõi CosmicMarket.')
        newsletterEmail.value = ''
      } catch (error) {
        console.error('Newsletter subscription error:', error)
        alert('Có lỗi xảy ra. Vui lòng thử lại sau.')
      } finally {
        subscribing.value = false
      }
    }
    
    const scrollToTop = () => {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    }
    
    const handleScroll = () => {
      showBackToTop.value = window.pageYOffset > 300
    }
    
    // Lifecycle
    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
    })
    
    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })
    
    return {
      newsletterEmail,
      subscribing,
      showBackToTop,
      currentYear,
      subscribeNewsletter,
      scrollToTop
    }
  }
}
</script>

<style scoped>
/* Footer Main Styles */
.app-footer {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: var(--text-primary);
  position: relative;
  overflow: hidden;
}

.app-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(0, 212, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(142, 68, 173, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(255, 205, 60, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  position: relative;
  z-index: 1;
}

/* Footer Main */
.footer-main {
  padding: 3rem 0 2rem;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1.5fr;
  gap: 2rem;
  align-items: start;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* Company Info */
.company-info {
  gap: 1.5rem;
}

.footer-logo .logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.footer-logo .logo-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 0 15px rgba(0, 212, 255, 0.5));
}

.footer-logo .brand-text h3 {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.footer-logo .tagline {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.company-description {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* Social Media */
.social-media h4 {
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
}

.social-links {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.social-link:hover {
  background: var(--aurora-gradient);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
}

.social-icon {
  font-size: 1.2rem;
}

/* Footer Sections */
.footer-title {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  position: relative;
  padding-bottom: 0.5rem;
}

.footer-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30px;
  height: 2px;
  background: var(--aurora-gradient);
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.footer-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-links a:hover {
  color: var(--text-accent);
  transform: translateX(5px);
}

/* Contact Section */
.contact-section {
  gap: 2rem;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.contact-item {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.contact-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.contact-details strong {
  color: var(--text-primary);
  display: block;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.contact-details p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.4;
}

.contact-details a {
  color: var(--text-accent);
  text-decoration: none;
  transition: all 0.3s ease;
}

.contact-details a:hover {
  color: var(--text-primary);
}

/* Newsletter */
.newsletter-signup {
  background: rgba(255, 255, 255, 0.05);
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.newsletter-signup h5 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
}

.newsletter-signup p {
  color: var(--text-secondary);
  margin: 0 0 1rem 0;
  font-size: 0.85rem;
}

.newsletter-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.input-group {
  display: flex;
  gap: 0.5rem;
}

.newsletter-input {
  flex: 1;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.newsletter-input:focus {
  outline: none;
  border-color: var(--text-accent);
  background: rgba(255, 255, 255, 0.15);
}

.newsletter-input::placeholder {
  color: var(--text-secondary);
}

.newsletter-btn {
  background: var(--aurora-gradient);
  border: none;
  border-radius: 8px;
  color: white;
  width: 3rem;
  height: 3rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.newsletter-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 212, 255, 0.3);
}

.newsletter-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* App Download Section */
.app-download-section {
  padding: 2rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  background: rgba(255, 255, 255, 0.02);
}

.app-download-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  flex-wrap: wrap;
}

.app-info h4 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
}

.app-info p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9rem;
}

.app-badges {
  display: flex;
  gap: 1rem;
}

.app-badge {
  display: block;
  transition: transform 0.3s ease;
}

.app-badge:hover {
  transform: scale(1.05);
}

.app-badge img {
  height: 40px;
  width: auto;
}

.qr-code {
  text-align: center;
}

.qr-image {
  font-size: 3rem;
  margin-bottom: 0.5rem;
  filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.3));
}

.qr-code p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.8rem;
}

/* Trust Section */
.trust-section {
  padding: 2rem 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  background: rgba(255, 255, 255, 0.01);
}

.trust-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  min-width: 200px;
}

.trust-icon {
  font-size: 2rem;
  filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.4));
}

.trust-text strong {
  color: var(--text-primary);
  display: block;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.trust-text p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.8rem;
}

/* Footer Bottom */
.footer-bottom {
  padding: 1.5rem 0;
  background: rgba(0, 0, 0, 0.2);
}

.bottom-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.copyright {
  flex: 1;
}

.copyright p {
  margin: 0 0 0.25rem 0;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.company-info-text {
  font-size: 0.75rem !important;
  opacity: 0.8;
}

.legal-links {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.legal-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.8rem;
  transition: color 0.3s ease;
}

.legal-links a:hover {
  color: var(--text-accent);
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.payment-label {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.payment-icons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.payment-icon {
  font-size: 1.5rem;
  opacity: 0.7;
  transition: all 0.3s ease;
  cursor: pointer;
}

.payment-icon:hover {
  opacity: 1;
  transform: scale(1.1);
}

/* Back to Top Button */
.back-to-top {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 3rem;
  height: 3rem;
  background: var(--aurora-gradient);
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.3);
}

.back-to-top:hover {
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr 1fr;
    gap: 2rem;
  }
  
  .company-info {
    grid-column: 1 / -1;
  }
  
  .contact-section {
    grid-column: 1 / -1;
  }
  
  .trust-content {
    justify-content: center;
  }
  
  .trust-item {
    min-width: 180px;
  }
}

@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }
  
  .company-info,
  .contact-section {
    grid-column: 1 / -1;
  }
  
  .app-download-content {
    flex-direction: column;
    text-align: center;
  }
  
  .trust-content {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .trust-item {
    justify-content: center;
    text-align: center;
  }
  
  .bottom-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .legal-links {
    justify-content: center;
  }
  
  .payment-methods {
    align-items: center;
  }
  
  .back-to-top {
    bottom: 1rem;
    right: 1rem;
    width: 2.5rem;
    height: 2.5rem;
  }
}

@media (max-width: 480px) {
  .footer-main {
    padding: 2rem 0 1.5rem;
  }
  
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .footer-logo .logo-container {
    justify-content: center;
    text-align: center;
  }
  
  .company-description {
    text-align: center;
  }
  
  .social-links {
    justify-content: center;
  }
  
  .newsletter-signup {
    padding: 1rem;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .newsletter-btn {
    width: 100%;
    height: 2.5rem;
  }
  
  .app-badges {
    flex-direction: column;
    align-items: center;
  }
  
  .trust-item {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .legal-links {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .payment-icons {
    justify-content: center;
  }
}

/* Animation Effects */
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

.footer-logo .logo-icon {
  animation: float 3s ease-in-out infinite;
}

.social-link {
  position: relative;
  overflow: hidden;
}

.social-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.social-link:hover::before {
  left: 100%;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
  .app-footer {
    background: linear-gradient(135deg, #0a0a0f 0%, #0f1419 50%, #0a1929 100%);
  }
  
  .newsletter-signup {
    background: rgba(255, 255, 255, 0.03);
  }
  
  .app-download-section,
  .trust-section {
    background: rgba(255, 255, 255, 0.01);
  }
  
  .footer-bottom {
    background: rgba(0, 0, 0, 0.4);
  }
}

/* High Contrast Mode */
@media (prefers-contrast: high) {
  .footer-links a,
  .legal-links a {
    border-bottom: 1px solid transparent;
  }
  
  .footer-links a:hover,
  .legal-links a:hover {
    border-bottom-color: var(--text-accent);
  }
  
  .social-link {
    border: 2px solid var(--text-accent);
  }
}

/* Print Styles */
@media print {
  .app-footer {
    background: none !important;
    color: black !important;
  }
  
  .social-links,
  .newsletter-signup,
  .app-download-section,
  .back-to-top {
    display: none !important;
  }
  
  .footer-grid {
    grid-template-columns: 1fr 1fr !important;
  }
  
  .contact-section {
    grid-column: 1 / -1 !important;
  }
}

/* Accessibility */
.footer-links a:focus,
.legal-links a:focus,
.social-link:focus,
.newsletter-input:focus,
.newsletter-btn:focus,
.back-to-top:focus {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}

/* Reduced Motion */
@media (prefers-reduced-motion: reduce) {
  .footer-logo .logo-icon {
    animation: none;
  }
  
  .social-link::before {
    transition: none;
  }
  
  .back-to-top {
    transition: none;
  }
}

/* Loading States */
.newsletter-btn[disabled] {
  position: relative;
}

.newsletter-btn[disabled]::after {
  content: '';
  position: absolute;
  width: 1rem;
  height: 1rem;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>