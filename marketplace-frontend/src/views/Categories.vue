<template>
  <div class="categories-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🌌 Khám phá các Hành tinh</h1>
        <p class="page-subtitle">Mỗi danh mục là một hành tinh đầy bí ẩn và kho báu</p>
      </div>

      <!-- Featured Categories -->
      <section class="featured-categories">
        <h2 class="section-title">🔥 Hành tinh nổi bật</h2>
        <div class="featured-grid">
          <div 
            v-for="category in featuredCategories" 
            :key="category.id"
            class="featured-card space-card"
            @click="navigateToCategory(category.slug)"
          >
            <div class="featured-bg" :style="{ background: category.gradient }"></div>
            <div class="featured-content">
              <div class="featured-icon">{{ category.icon }}</div>
              <h3>{{ category.name }}</h3>
              <p>{{ category.description }}</p>
              <div class="featured-stats">
                <span class="stat">
                  <span class="stat-icon">📦</span>
                  {{ category.productCount }} sản phẩm
                </span>
                <span class="stat">
                  <span class="stat-icon">👥</span>
                  {{ category.sellerCount }} người bán
                </span>
              </div>
              <div class="featured-trending" v-if="category.trending">
                <span class="trending-badge">🔥 Trending</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- All Categories -->
      <section class="all-categories">
        <div class="section-header">
          <h2 class="section-title">🗺️ Bản đồ vũ trụ</h2>
          <div class="view-options">
            <button 
              @click="viewMode = 'grid'" 
              class="view-btn"
              :class="{ active: viewMode === 'grid' }"
            >
              ⚏ Lưới
            </button>
            <button 
              @click="viewMode = 'list'" 
              class="view-btn"
              :class="{ active: viewMode === 'list' }"
            >
              ☰ Danh sách
            </button>
          </div>
        </div>

        <div class="categories-container" :class="{ 'list-view': viewMode === 'list' }">
          <div 
            v-for="category in allCategories" 
            :key="category.id"
            class="category-card space-card"
            @click="navigateToCategory(category.slug)"
          >
            <div class="category-header">
              <div class="category-icon" :style="{ background: category.color }">
                {{ category.icon }}
              </div>
              <div class="category-info">
                <h3>{{ category.name }}</h3>
                <p>{{ category.description }}</p>
              </div>
              <div class="category-arrow">→</div>
            </div>

            <div class="category-stats">
              <div class="stat-item">
                <span class="stat-number">{{ category.productCount }}</span>
                <span class="stat-label">Sản phẩm</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ category.sellerCount }}</span>
                <span class="stat-label">Người bán</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ category.avgRating }}</span>
                <span class="stat-label">⭐ Đánh giá</span>
              </div>
            </div>

            <!-- Popular Products Preview -->
            <div class="popular-products" v-if="category.popularProducts">
              <h4>Sản phẩm nổi bật:</h4>
              <div class="products-preview">
                <div 
                  v-for="product in category.popularProducts.slice(0, 3)" 
                  :key="product.id"
                  class="product-preview"
                  @click.stop="viewProduct(product.id)"
                >
                  <img 
                    :src="product.image || '/placeholder-product.jpg'" 
                    :alt="product.name"
                  />
                  <div class="product-preview-info">
                    <span class="product-name">{{ product.name }}</span>
                    <span class="product-price">{{ formatCurrency(product.price) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Subcategories -->
            <div class="subcategories" v-if="category.subcategories">
              <h4>Danh mục con:</h4>
              <div class="subcategories-list">
                <span 
                  v-for="sub in category.subcategories.slice(0, 4)" 
                  :key="sub.id"
                  class="subcategory-tag"
                  @click.stop="navigateToCategory(sub.slug)"
                >
                  {{ sub.name }}
                </span>
                <span v-if="category.subcategories.length > 4" class="more-subcategories">
                  +{{ category.subcategories.length - 4 }} khác
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Category Explorer -->
      <section class="category-explorer">
        <h2 class="section-title">🎯 Tìm theo nhu cầu</h2>
        <div class="explorer-tabs">
          <button 
            v-for="tab in explorerTabs" 
            :key="tab.id"
            @click="activeExplorerTab = tab.id"
            class="explorer-tab"
            :class="{ active: activeExplorerTab === tab.id }"
          >
            {{ tab.icon }} {{ tab.name }}
          </button>
        </div>

        <div class="explorer-content">
          <div v-if="activeExplorerTab === 'trending'" class="explorer-section">
            <div class="trending-categories">
              <div 
                v-for="category in trendingCategories" 
                :key="category.id"
                class="trending-item"
                @click="navigateToCategory(category.slug)"
              >
                <div class="trending-rank">#{{ category.rank }}</div>
                <div class="trending-icon">{{ category.icon }}</div>
                <div class="trending-info">
                  <h4>{{ category.name }}</h4>
                  <p>{{ category.trendDescription }}</p>
                  <div class="trending-growth">
                    <span class="growth-icon">📈</span>
                    <span class="growth-text">+{{ category.growth }}% tuần này</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeExplorerTab === 'seasonal'" class="explorer-section">
            <div class="seasonal-categories">
              <div 
                v-for="category in seasonalCategories" 
                :key="category.id"
                class="seasonal-item"
                @click="navigateToCategory(category.slug)"
              >
                <div class="seasonal-image">
                  <div class="seasonal-icon">{{ category.icon }}</div>
                  <div class="seasonal-overlay">{{ category.season }}</div>
                </div>
                <div class="seasonal-info">
                  <h4>{{ category.name }}</h4>
                  <p>{{ category.seasonDescription }}</p>
                  <div class="seasonal-discount" v-if="category.discount">
                    <span class="discount-text">Giảm đến {{ category.discount }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="activeExplorerTab === 'new'" class="explorer-section">
            <div class="new-categories">
              <div 
                v-for="category in newCategories" 
                :key="category.id"
                class="new-item"
                @click="navigateToCategory(category.slug)"
              >
                <div class="new-badge">🆕 MỚI</div>
                <div class="new-icon">{{ category.icon }}</div>
                <div class="new-info">
                  <h4>{{ category.name }}</h4>
                  <p>{{ category.description }}</p>
                  <div class="new-date">
                    <span class="date-icon">📅</span>
                    <span class="date-text">Ra mắt {{ category.launchDate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Browse by Price -->
      <section class="browse-by-price">
        <h2 class="section-title">💰 Duyệt theo ngân sách</h2>
        <div class="price-ranges">
          <div 
            v-for="range in priceRanges" 
            :key="range.id"
            class="price-range-card space-card"
            @click="navigateToProducts(range.query)"
          >
            <div class="price-icon">{{ range.icon }}</div>
            <h3>{{ range.title }}</h3>
            <p class="price-description">{{ range.description }}</p>
            <div class="price-value">{{ range.range }}</div>
            <div class="price-categories">
              <span 
                v-for="cat in range.categories" 
                :key="cat"
                class="price-category-tag"
              >
                {{ cat }}
              </span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Categories',
  setup() {
    const router = useRouter()
    
    // Reactive data
    const viewMode = ref('grid')
    const activeExplorerTab = ref('trending')
    
    const featuredCategories = ref([
      {
        id: 1,
        name: 'Hành tinh Công nghệ',
        slug: 'technology',
        description: 'Khám phá những công nghệ tiên tiến nhất vũ trụ',
        icon: '💻',
        gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        productCount: 15420,
        sellerCount: 1250,
        trending: true
      },
      {
        id: 2,
        name: 'Hành tinh Thời trang',
        slug: 'fashion',
        description: 'Phong cách từ khắp các thiên hà',
        icon: '👗',
        gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        productCount: 8930,
        sellerCount: 890,
        trending: false
      },
      {
        id: 3,
        name: 'Hành tinh Gia đình',
        slug: 'home-living',
        description: 'Tạo không gian sống hoàn hảo',
        icon: '🏠',
        gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        productCount: 12350,
        sellerCount: 670,
        trending: true
      }
    ])
    
    const allCategories = ref([
      {
        id: 1,
        name: 'Điện tử & Công nghệ',
        slug: 'electronics',
        description: 'Laptop, điện thoại, thiết bị thông minh',
        icon: '📱',
        color: 'linear-gradient(45deg, #667eea, #764ba2)',
        productCount: 15420,
        sellerCount: 1250,
        avgRating: 4.5,
        popularProducts: [
          { id: 1, name: 'iPhone 15 Pro Max', price: 35000000, image: '/product1.jpg' },
          { id: 2, name: 'MacBook Pro M3', price: 45000000, image: '/product2.jpg' },
          { id: 3, name: 'Samsung Galaxy S24', price: 28000000, image: '/product3.jpg' }
        ],
        subcategories: [
          { id: 1, name: 'Điện thoại', slug: 'phones' },
          { id: 2, name: 'Laptop', slug: 'laptops' },
          { id: 3, name: 'Tablet', slug: 'tablets' },
          { id: 4, name: 'Âm thanh', slug: 'audio' },
          { id: 5, name: 'Phụ kiện', slug: 'accessories' }
        ]
      },
      {
        id: 2,
        name: 'Thời trang Nam',
        slug: 'mens-fashion',
        description: 'Quần áo, giày dép, phụ kiện nam',
        icon: '👔',
        color: 'linear-gradient(45deg, #f093fb, #f5576c)',
        productCount: 8930,
        sellerCount: 890,
        avgRating: 4.3,
        popularProducts: [
          { id: 4, name: 'Áo sơ mi công sở', price: 850000, image: '/product4.jpg' },
          { id: 5, name: 'Quần jean slim fit', price: 1200000, image: '/product5.jpg' },
          { id: 6, name: 'Giày sneaker', price: 2500000, image: '/product6.jpg' }
        ],
        subcategories: [
          { id: 6, name: 'Áo nam', slug: 'mens-shirts' },
          { id: 7, name: 'Quần nam', slug: 'mens-pants' },
          { id: 8, name: 'Giày nam', slug: 'mens-shoes' },
          { id: 9, name: 'Phụ kiện nam', slug: 'mens-accessories' }
        ]
      },
      {
        id: 3,
        name: 'Thời trang Nữ',
        slug: 'womens-fashion',
        description: 'Váy, áo, giày, túi xách nữ',
        icon: '👗',
        color: 'linear-gradient(45deg, #fa709a, #fee140)',
        productCount: 12580,
        sellerCount: 1120,
        avgRating: 4.6,
        popularProducts: [
          { id: 7, name: 'Váy dự tiệc', price: 1800000, image: '/product7.jpg' },
          { id: 8, name: 'Túi xách cao cấp', price: 3200000, image: '/product8.jpg' },
          { id: 9, name: 'Giày cao gót', price: 1500000, image: '/product9.jpg' }
        ],
        subcategories: [
          { id: 10, name: 'Váy nữ', slug: 'womens-dresses' },
          { id: 11, name: 'Áo nữ', slug: 'womens-tops' },
          { id: 12, name: 'Quần nữ', slug: 'womens-pants' },
          { id: 13, name: 'Giày nữ', slug: 'womens-shoes' },
          { id: 14, name: 'Túi xách', slug: 'handbags' }
        ]
      },
      {
        id: 4,
        name: 'Nhà cửa & Đời sống',
        slug: 'home-living',
        description: 'Nội thất, trang trí, đồ gia dụng',
        icon: '🏠',
        color: 'linear-gradient(45deg, #4facfe, #00f2fe)',
        productCount: 12350,
        sellerCount: 670,
        avgRating: 4.4,
        popularProducts: [
          { id: 10, name: 'Sofa 3 chỗ ngồi', price: 15000000, image: '/product10.jpg' },
          { id: 11, name: 'Bàn làm việc', price: 4500000, image: '/product11.jpg' },
          { id: 12, name: 'Đèn trang trí', price: 800000, image: '/product12.jpg' }
        ],
        subcategories: [
          { id: 15, name: 'Nội thất', slug: 'furniture' },
          { id: 16, name: 'Trang trí', slug: 'decor' },
          { id: 17, name: 'Đồ gia dụng', slug: 'appliances' },
          { id: 18, name: 'Vườn & Sân', slug: 'garden' }
        ]
      },
      {
        id: 5,
        name: 'Sức khỏe & Làm đẹp',
        slug: 'health-beauty',
        description: 'Mỹ phẩm, chăm sóc sức khỏe',
        icon: '💄',
        color: 'linear-gradient(45deg, #a8edea, #fed6e3)',
        productCount: 9870,
        sellerCount: 580,
        avgRating: 4.5,
        popularProducts: [
          { id: 13, name: 'Serum vitamin C', price: 650000, image: '/product13.jpg' },
          { id: 14, name: 'Kem chống nắng', price: 320000, image: '/product14.jpg' },
          { id: 15, name: 'Máy massage', price: 2800000, image: '/product15.jpg' }
        ],
        subcategories: [
          { id: 19, name: 'Chăm sóc da', slug: 'skincare' },
          { id: 20, name: 'Trang điểm', slug: 'makeup' },
          { id: 21, name: 'Chăm sóc tóc', slug: 'haircare' },
          { id: 22, name: 'Thực phẩm chức năng', slug: 'supplements' }
        ]
      },
      {
        id: 6,
        name: 'Thể thao & Du lịch',
        slug: 'sports-travel',
        description: 'Dụng cụ thể thao, đồ du lịch',
        icon: '⚽',
        color: 'linear-gradient(45deg, #ff9a9e, #fecfef)',
        productCount: 7650,
        sellerCount: 420,
        avgRating: 4.3,
        popularProducts: [
          { id: 16, name: 'Giày chạy bộ', price: 2200000, image: '/product16.jpg' },
          { id: 17, name: 'Balo du lịch', price: 1800000, image: '/product17.jpg' },
          { id: 18, name: 'Máy tập gym', price: 12000000, image: '/product18.jpg' }
        ],
        subcategories: [
          { id: 23, name: 'Thể thao', slug: 'sports' },
          { id: 24, name: 'Du lịch', slug: 'travel' },
          { id: 25, name: 'Outdoor', slug: 'outdoor' }
        ]
      }
    ])
    
    const explorerTabs = ref([
      { id: 'trending', name: 'Xu hướng', icon: '🔥' },
      { id: 'seasonal', name: 'Theo mùa', icon: '🌸' },
      { id: 'new', name: 'Mới ra mắt', icon: '🆕' }
    ])
    
    const trendingCategories = ref([
      {
        id: 1,
        rank: 1,
        name: 'Gaming Setup',
        slug: 'gaming',
        icon: '🎮',
        trendDescription: 'Bộ setup gaming hoàn hảo đang hot nhất',
        growth: 45
      },
      {
        id: 2,
        rank: 2,
        name: 'Smart Home',
        slug: 'smart-home',
        icon: '🏠',
        trendDescription: 'Thiết bị nhà thông minh ngày càng phổ biến',
        growth: 38
      },
      {
        id: 3,
        rank: 3,
        name: 'Fitness Tech',
        slug: 'fitness-tech',
        icon: '💪',
        trendDescription: 'Công nghệ hỗ trợ tập luyện đang lên ngôi',
        growth: 32
      }
    ])
    
    const seasonalCategories = ref([
      {
        id: 1,
        name: 'Thời trang Hè',
        slug: 'summer-fashion',
        icon: '☀️',
        season: 'MÙA HÈ',
        seasonDescription: 'Trang phục mát mẻ cho mùa hè',
        discount: 30
      },
      {
        id: 2,
        name: 'Dụng cụ BBQ',
        slug: 'bbq-tools',
        icon: '🔥',
        season: 'NƯỚNG NGOÀI TRỜI',
        seasonDescription: 'Thiết bị nướng cho buổi tiệc ngoài trời',
        discount: 25
      }
    ])
    
    const newCategories = ref([
      {
        id: 1,
        name: 'NFT Collectibles',
        slug: 'nft',
        icon: '🖼️',
        description: 'Bộ sưu tập NFT độc đáo và giá trị',
        launchDate: 'Tháng 6, 2025'
      },
      {
        id: 2,
        name: 'Metaverse Gear',
        slug: 'metaverse',
        icon: '🥽',
        description: 'Thiết bị cho thế giới ảo metaverse',
        launchDate: 'Tháng 5, 2025'
      }
    ])
    
    const priceRanges = ref([
      {
        id: 1,
        title: 'Tiết kiệm',
        description: 'Sản phẩm chất lượng với giá cả hợp lý',
        range: 'Dưới 500K',
        icon: '💰',
        query: 'price=0-500000',
        categories: ['Phụ kiện', 'Mỹ phẩm', 'Đồ dùng']
      },
      {
        id: 2,
        title: 'Trung bình',
        description: 'Cân bằng giữa chất lượng và giá cả',
        range: '500K - 2M',
        icon: '💳',
        query: 'price=500000-2000000',
        categories: ['Thời trang', 'Điện tử', 'Gia dụng']
      },
      {
        id: 3,
        title: 'Cao cấp',
        description: 'Sản phẩm premium chất lượng cao',
        range: '2M - 10M',
        icon: '💎',
        query: 'price=2000000-10000000',
        categories: ['Laptop', 'Điện thoại', 'Nội thất']
      },
      {
        id: 4,
        title: 'Luxury',
        description: 'Hàng hiệu và sản phẩm đặc biệt',
        range: 'Trên 10M',
        icon: '👑',
        query: 'price=10000000+',
        categories: ['Đồng hồ', 'Trang sức', 'Xe cộ']
      }
    ])
    
    // Methods
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }
    
    const navigateToCategory = (slug) => {
      router.push(`/products?category=${slug}`)
    }
    
    const navigateToProducts = (query) => {
      router.push(`/products?${query}`)
    }
    
    const viewProduct = (productId) => {
      router.push(`/products/${productId}`)
    }
    
    // Lifecycle
    onMounted(() => {
      console.log('Categories page mounted')
    })
    
    return {
      viewMode,
      activeExplorerTab,
      featuredCategories,
      allCategories,
      explorerTabs,
      trendingCategories,
      seasonalCategories,
      newCategories,
      priceRanges,
      formatCurrency,
      navigateToCategory,
      navigateToProducts,
      viewProduct
    }
  }
}
</script>

<style scoped>
.categories-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-header {
  text-align: center;
  margin-bottom: 4rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-accent);
  margin-bottom: 0.5rem;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  opacity: 0.9;
}

.section-title {
  font-size: 1.8rem;
  color: var(--text-accent);
  margin-bottom: 2rem;
  text-align: center;
}

/* Featured Categories */
.featured-categories {
  margin-bottom: 4rem;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.featured-card {
  position: relative;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  min-height: 250px;
}

.featured-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--space-glow);
}

.featured-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  transition: opacity 0.3s ease;
}

.featured-card:hover .featured-bg {
  opacity: 0.2;
}

.featured-content {
  position: relative;
  z-index: 1;
}

.featured-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.featured-card h3 {
  color: var(--text-primary);
  font-size: 1.5rem;
  margin-bottom: 0.75rem;
}

.featured-card p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.featured-stats {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.stat-icon {
  color: var(--text-accent);
}

.featured-trending {
  position: absolute;
  top: 1rem;
  right: 1rem;
}

.trending-badge {
  background: var(--accent-gradient);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  animation: pulse 2s ease-in-out infinite;
}

/* All Categories */
.all-categories {
  margin-bottom: 4rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.view-options {
  display: flex;
  gap: 0.5rem;
}

.view-btn {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.view-btn:hover,
.view-btn.active {
  background: var(--aurora-gradient);
  border-color: var(--text-accent);
  color: white;
}

.categories-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
}

.categories-container.list-view {
  grid-template-columns: 1fr;
}

.category-card {
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.category-card:hover {
  transform: translateY(-5px);
  border-color: var(--text-accent);
  box-shadow: var(--neon-glow);
}

.category-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: white;
  flex: none;
}

.category-info {
  flex: 1;
}

.category-info h3 {
  color: var(--text-primary);
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
}

.category-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.category-arrow {
  color: var(--text-accent);
  font-size: 1.5rem;
  transition: transform 0.3s ease;
}

.category-card:hover .category-arrow {
  transform: translateX(5px);
}

.category-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  color: var(--text-accent);
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.popular-products {
  margin-bottom: 1.5rem;
}

.popular-products h4 {
  color: var(--text-primary);
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}

.products-preview {
  display: flex;
  gap: 1rem;
}

.product-preview {
  flex: 1;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.1);
}

.product-preview:hover {
  transform: scale(1.05);
}

.product-preview img {
  width: 100%;
  height: 60px;
  object-fit: cover;
}

.product-preview-info {
  padding: 0.5rem;
}

.product-name {
  display: block;
  color: var(--text-primary);
  font-size: 0.7rem;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  color: var(--text-accent);
  font-size: 0.7rem;
  font-weight: 600;
}

.subcategories h4 {
  color: var(--text-primary);
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}

.subcategories-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.subcategory-tag {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.subcategory-tag:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-1px);
}

.more-subcategories {
  color: var(--text-secondary);
  font-size: 0.7rem;
  font-style: italic;
}

/* Category Explorer */
.category-explorer {
  margin-bottom: 4rem;
}

.explorer-tabs {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.explorer-tab {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  color: var(--text-secondary);
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.explorer-tab:hover,
.explorer-tab.active {
  background: var(--aurora-gradient);
  border-color: var(--text-accent);
  color: white;
  transform: translateY(-2px);
}

.explorer-content {
  min-height: 300px;
}

.explorer-section {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.trending-categories,
.seasonal-categories,
.new-categories {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.trending-item,
.seasonal-item,
.new-item {
  background: rgba(26, 26, 46, 0.5);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.trending-item:hover,
.seasonal-item:hover,
.new-item:hover {
  border-color: var(--text-accent);
  transform: translateY(-5px);
  box-shadow: var(--neon-glow);
}

.trending-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.trending-rank {
  width: 40px;
  height: 40px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
}

.trending-icon {
  font-size: 2rem;
}

.trending-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.trending-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
}

.trending-growth {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.growth-icon {
  color: #10b981;
}

.growth-text {
  color: #10b981;
  font-size: 0.8rem;
  font-weight: 500;
}

.seasonal-item {
  text-align: center;
}

.seasonal-image {
  position: relative;
  margin-bottom: 1rem;
}

.seasonal-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

.seasonal-overlay {
  background: var(--accent-gradient);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.seasonal-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.seasonal-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.seasonal-discount {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  padding: 0.5rem;
}

.discount-text {
  color: #ef4444;
  font-size: 0.8rem;
  font-weight: 600;
}

.new-item {
  position: relative;
}

.new-badge {
  position: absolute;
  top: -0.5rem;
  right: -0.5rem;
  background: var(--accent-gradient);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.new-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.new-info h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.new-info p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.new-date {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.date-icon {
  color: var(--text-accent);
}

.date-text {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

/* Browse by Price */
.browse-by-price {
  margin-bottom: 4rem;
}

.price-ranges {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.price-range-card {
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.price-range-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--space-glow);
}

.price-range-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--aurora-gradient);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.price-range-card:hover::before {
  transform: scaleX(1);
}

.price-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.price-range-card h3 {
  color: var(--text-primary);
  font-size: 1.3rem;
  margin-bottom: 0.75rem;
}

.price-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.price-value {
  color: var(--text-accent);
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.price-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.price-category-tag {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .featured-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
  
  .categories-container {
    grid-template-columns: 1fr;
  }
  
  .trending-categories,
  .seasonal-categories,
  .new-categories {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .categories-page {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .featured-card {
    min-height: auto;
    padding: 1.5rem;
  }
  
  .category-header {
    flex-direction: column;
    text-align: center;
    gap: 0.75rem;
  }
  
  .category-stats {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .products-preview {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .product-preview {
    display: flex;
    gap: 0.75rem;
    align-items: center;
  }
  
  .product-preview img {
    width: 60px;
    height: 60px;
    flex: none;
  }
  
  .product-preview-info {
    flex: 1;
  }
  
  .explorer-tabs {
    flex-direction: column;
    align-items: center;
  }
  
  .trending-item {
    flex-direction: column;
    text-align: center;
    gap: 0.75rem;
  }
  
  .trending-info {
    text-align: center;
  }
  
  .price-ranges {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
  }
  
  .price-range-card {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .featured-card {
    padding: 1rem;
  }
  
  .category-card {
    padding: 1rem;
  }
  
  .category-icon {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }
  
  .explorer-tab {
    padding: 0.5rem 1rem;
    font-size: 0.8rem;
  }
  
  .trending-item,
  .seasonal-item,
  .new-item {
    padding: 1rem;
  }
  
  .price-ranges {
    grid-template-columns: 1fr;
  }
}

/* Animation enhancements */
.featured-card,
.category-card,
.price-range-card {
  animation: slideUp 0.6s ease-out;
}

.featured-card:nth-child(2) {
  animation-delay: 0.1s;
}

.featured-card:nth-child(3) {
  animation-delay: 0.2s;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Hover glow effects */
.trending-rank {
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.new-badge {
  box-shadow: 0 0 15px rgba(255, 107, 107, 0.4);
}

.seasonal-overlay {
  box-shadow: 0 0 10px rgba(138, 43, 226, 0.3);
}
</style>