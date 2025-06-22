<template>
  <div class="categories-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">🗂️ Danh mục sản phẩm</h1>
        <p class="page-subtitle">Khám phá vũ trụ sản phẩm theo từng danh mục</p>
      </div>

      <!-- Search Categories -->
      <div class="search-section">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="🔍 Tìm kiếm danh mục..."
            class="search-input"
            @input="handleSearch"
          />
        </div>
      </div>

      <!-- Categories Grid -->
      <div v-if="!loading && filteredCategories.length" class="categories-section">
        <div class="categories-grid">
          <div
            v-for="category in filteredCategories"
            :key="category.id"
            class="category-card space-card"
            @click="viewCategory(category)"
          >
            <div class="category-header">
              <div class="category-icon">{{ category.icon }}</div>
              <div class="category-stats">
                <span class="product-count">{{ category.productCount }}</span>
                <span class="stats-label">sản phẩm</span>
              </div>
            </div>
            
            <div class="category-content">
              <h3 class="category-name">{{ category.name }}</h3>
              <p class="category-description">{{ category.description }}</p>
              
              <!-- Sub-categories -->
              <div v-if="category.subCategories?.length" class="sub-categories">
                <div class="sub-category-label">Danh mục con:</div>
                <div class="sub-category-tags">
                  <span 
                    v-for="subCat in category.subCategories.slice(0, 3)" 
                    :key="subCat.id"
                    class="sub-category-tag"
                  >
                    {{ subCat.name }}
                  </span>
                  <span 
                    v-if="category.subCategories.length > 3"
                    class="sub-category-more"
                  >
                    +{{ category.subCategories.length - 3 }} khác
                  </span>
                </div>
              </div>
            </div>

            <div class="category-footer">
              <div class="category-meta">
                <div class="meta-item">
                  <span class="meta-label">Đánh giá TB:</span>
                  <div class="rating-display">
                    <span class="rating-stars">
                      <span v-for="i in 5" :key="i" class="star" :class="[i <= category.averageRating ? 'filled' : '']">⭐</span>
                    </span>
                    <span class="rating-value">{{ category.averageRating }}/5</span>
                  </div>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Phổ biến:</span>
                  <div class="popularity-bar">
                    <div class="popularity-fill" :style="{ width: category.popularity + '%' }"></div>
                  </div>
                </div>
              </div>
              
              <button class="btn btn-primary category-btn">
                Xem sản phẩm →
              </button>
            </div>

            <!-- Hover overlay -->
            <div class="category-overlay">
              <div class="overlay-content">
                <div class="overlay-icon">🚀</div>
                <div class="overlay-text">Khám phá {{ category.productCount }} sản phẩm</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Featured Categories Section -->
      <div v-if="!loading" class="featured-categories-section">
        <div class="section-header">
          <h2>⭐ Danh mục nổi bật</h2>
          <p>Những danh mục được quan tâm nhất</p>
        </div>
        
        <div class="featured-grid">
          <div
            v-for="featured in featuredCategories"
            :key="featured.id"
            class="featured-card space-card"
            @click="viewCategory(featured)"
          >
            <div class="featured-background">
              <div class="featured-pattern"></div>
            </div>
            
            <div class="featured-content">
              <div class="featured-icon">{{ featured.icon }}</div>
              <h3>{{ featured.name }}</h3>
              <p>{{ featured.description }}</p>
              
              <div class="featured-stats">
                <div class="stat">
                  <span class="stat-number">{{ featured.productCount }}</span>
                  <span class="stat-label">Sản phẩm</span>
                </div>
                <div class="stat">
                  <span class="stat-number">{{ featured.averageRating }}</span>
                  <span class="stat-label">Đánh giá</span>
                </div>
                <div class="stat">
                  <span class="stat-number">{{ Math.round(featured.popularity) }}%</span>
                  <span class="stat-label">Phổ biến</span>
                </div>
              </div>
              
              <div class="featured-action">
                <span class="action-text">Khám phá ngay</span>
                <span class="action-icon">→</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Popular Tags Section -->
      <div v-if="!loading" class="tags-section">
        <div class="section-header">
          <h2>🏷️ Thẻ phổ biến</h2>
          <p>Tìm kiếm nhanh theo thẻ</p>
        </div>
        
        <div class="tags-cloud">
          <span
            v-for="tag in popularTags"
            :key="tag.name"
            class="tag-item"
            :class="{ 'tag-large': tag.count > 100, 'tag-medium': tag.count > 50 }"
            @click="searchByTag(tag.name)"
          >
            {{ tag.name }} ({{ tag.count }})
          </span>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner">🔄</div>
        <p>Đang tải danh mục...</p>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && filteredCategories.length === 0" class="empty-section">
        <div class="empty-content">
          <div class="empty-icon">🔍</div>
          <h3>Không tìm thấy danh mục</h3>
          <p>Thử tìm kiếm với từ khóa khác</p>
          <button @click="clearSearch" class="btn btn-secondary">Xóa tìm kiếm</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryAPI } from '@/services/api'

export default {
  name: 'Categories',
  setup() {
    const router = useRouter()
    
    // Reactive data
    const categories = ref([])
    const featuredCategories = ref([])
    const popularTags = ref([])
    const searchQuery = ref('')
    const loading = ref(true)
    
    // Computed
    const filteredCategories = computed(() => {
      if (!searchQuery.value) return categories.value
      
      const query = searchQuery.value.toLowerCase()
      return categories.value.filter(category =>
        category.name.toLowerCase().includes(query) ||
        category.description.toLowerCase().includes(query) ||
        category.subCategories?.some(sub => 
          sub.name.toLowerCase().includes(query)
        )
      )
    })
    
    // Methods
    const handleSearch = () => {
      // Search is reactive through computed property
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
    }
    
    const viewCategory = (category) => {
      router.push(`/products?category=${category.slug}`)
    }
    
    const searchByTag = (tagName) => {
      router.push(`/products?tag=${encodeURIComponent(tagName)}`)
    }
    
    const loadCategories = async () => {
      try {
        // Mock data - replace with real API call
        categories.value = [
          {
            id: 1,
            name: 'Điện tử & Công nghệ',
            slug: 'electronics',
            icon: '📱',
            description: 'Smartphone, laptop, phụ kiện công nghệ hiện đại',
            productCount: 1247,
            averageRating: 4.6,
            popularity: 85,
            subCategories: [
              { id: 11, name: 'Smartphone' },
              { id: 12, name: 'Laptop' },
              { id: 13, name: 'Phụ kiện' },
              { id: 14, name: 'Gaming' }
            ]
          },
          {
            id: 2,
            name: 'Thời trang',
            slug: 'fashion',
            icon: '👗',
            description: 'Quần áo, giày dép, phụ kiện thời trang',
            productCount: 892,
            averageRating: 4.4,
            popularity: 78,
            subCategories: [
              { id: 21, name: 'Nam' },
              { id: 22, name: 'Nữ' },
              { id: 23, name: 'Trẻ em' },
              { id: 24, name: 'Phụ kiện' }
            ]
          },
          {
            id: 3,
            name: 'Nhà cửa & Đời sống',
            slug: 'home-living',
            icon: '🏠',
            description: 'Nội thất, đồ gia dụng, trang trí nhà cửa',
            productCount: 654,
            averageRating: 4.5,
            popularity: 72,
            subCategories: [
              { id: 31, name: 'Nội thất' },
              { id: 32, name: 'Gia dụng' },
              { id: 33, name: 'Trang trí' }
            ]
          },
          {
            id: 4,
            name: 'Sách & Văn phòng phẩm',
            slug: 'books-stationery',
            icon: '📚',
            description: 'Sách, truyện, dụng cụ học tập và văn phòng',
            productCount: 423,
            averageRating: 4.7,
            popularity: 65,
            subCategories: [
              { id: 41, name: 'Sách' },
              { id: 42, name: 'Văn phòng phẩm' },
              { id: 43, name: 'Dụng cụ học tập' }
            ]
          },
          {
            id: 5,
            name: 'Thể thao & Du lịch',
            slug: 'sports-travel',
            icon: '⚽',
            description: 'Đồ thể thao, dụng cụ tập luyện, phụ kiện du lịch',
            productCount: 567,
            averageRating: 4.3,
            popularity: 68,
            subCategories: [
              { id: 51, name: 'Thể thao' },
              { id: 52, name: 'Du lịch' },
              { id: 53, name: 'Outdoor' }
            ]
          },
          {
            id: 6,
            name: 'Làm đẹp & Sức khỏe',
            slug: 'beauty-health',
            icon: '💄',
            description: 'Mỹ phẩm, chăm sóc sức khỏe, thực phẩm chức năng',
            productCount: 389,
            averageRating: 4.5,
            popularity: 75,
            subCategories: [
              { id: 61, name: 'Mỹ phẩm' },
              { id: 62, name: 'Chăm sóc da' },
              { id: 63, name: 'Sức khỏe' }
            ]
          },
          {
            id: 7,
            name: 'Ô tô & Xe máy',
            slug: 'automotive',
            icon: '🚗',
            description: 'Phụ kiện ô tô, xe máy, đồ bảo hộ',
            productCount: 234,
            averageRating: 4.2,
            popularity: 58,
            subCategories: [
              { id: 71, name: 'Ô tô' },
              { id: 72, name: 'Xe máy' },
              { id: 73, name: 'Phụ kiện' }
            ]
          },
          {
            id: 8,
            name: 'Mẹ & Bé',
            slug: 'mother-baby',
            icon: '👶',
            description: 'Đồ cho mẹ và bé, đồ chơi trẻ em',
            productCount: 445,
            averageRating: 4.8,
            popularity: 82,
            subCategories: [
              { id: 81, name: 'Đồ cho bé' },
              { id: 82, name: 'Đồ chơi' },
              { id: 83, name: 'Mẹ bầu' }
            ]
          }
        ]
        
        // Featured categories (top 4)
        featuredCategories.value = categories.value
          .sort((a, b) => b.popularity - a.popularity)
          .slice(0, 4)
          
      } catch (error) {
        console.error('Error loading categories:', error)
      }
    }
    
    const loadPopularTags = async () => {
      try {
        // Mock data - replace with real API call
        popularTags.value = [
          { name: 'iPhone', count: 156 },
          { name: 'Samsung', count: 142 },
          { name: 'Nike', count: 98 },
          { name: 'Adidas', count: 87 },
          { name: 'Gaming', count: 134 },
          { name: 'Laptop', count: 123 },
          { name: 'Wireless', count: 89 },
          { name: 'Premium', count: 76 },
          { name: 'Sale', count: 203 },
          { name: 'New', count: 167 },
          { name: 'Fashion', count: 112 },
          { name: 'Home', count: 94 },
          { name: 'Beauty', count: 78 },
          { name: 'Sport', count: 65 },
          { name: 'Books', count: 54 }
        ]
      } catch (error) {
        console.error('Error loading tags:', error)
      }
    }
    
    // Lifecycle
    onMounted(async () => {
      try {
        await Promise.all([
          loadCategories(),
          loadPopularTags()
        ])
      } finally {
        loading.value = false
      }
    })
    
    return {
      categories,
      featuredCategories,
      popularTags,
      searchQuery,
      loading,
      filteredCategories,
      handleSearch,
      clearSearch,
      viewCategory,
      searchByTag
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
  margin-bottom: 3rem;
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

/* Search Section */
.search-section {
  margin-bottom: 3rem;
}

.search-box {
  max-width: 500px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
  padding: 1rem 1.5rem;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50px;
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.2);
}

/* Categories Grid */
.categories-section {
  margin-bottom: 4rem;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.category-card {
  position: relative;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.category-icon {
  font-size: 3rem;
  line-height: 1;
}

.category-stats {
  text-align: right;
}

.product-count {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-accent);
}

.stats-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.category-content {
  margin-bottom: 1.5rem;
}

.category-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.category-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1rem;
}

.sub-categories {
  margin-top: 1rem;
}

.sub-category-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.5rem;
}

.sub-category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.sub-category-tag {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.sub-category-more {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
}

.category-footer {
  border-top: 1px solid rgba(0, 212, 255, 0.2);
  padding-top: 1rem;
}

.category-meta {
  margin-bottom: 1rem;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.meta-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.rating-stars {
  display: flex;
  gap: 1px;
}

.star {
  font-size: 0.7rem;
  opacity: 0.3;
}

.star.filled {
  opacity: 1;
}

.rating-value {
  font-size: 0.8rem;
  color: var(--text-accent);
}

.popularity-bar {
  width: 80px;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.popularity-fill {
  height: 100%;
  background: var(--aurora-gradient);
  transition: width 0.3s ease;
}

.category-btn {
  width: 100%;
  border-radius: 25px;
  font-weight: 500;
}

/* Category Overlay */
.category-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 212, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(5px);
}

.category-card:hover .category-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
  color: var(--text-primary);
}

.overlay-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

.overlay-text {
  font-size: 1.1rem;
  font-weight: 600;
}

/* Featured Categories */
.featured-categories-section {
  margin-bottom: 4rem;
}

.section-header {
  text-align: center;
  margin-bottom: 2rem;
}

.section-header h2 {
  font-size: 2rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.section-header p {
  color: var(--text-secondary);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.featured-card {
  position: relative;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  min-height: 200px;
}

.featured-card:hover {
  transform: translateY(-8px);
}

.featured-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
}

.featured-pattern {
  width: 100%;
  height: 100%;
  background: var(--aurora-gradient);
  transform: rotate(45deg) scale(1.5);
}

.featured-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.featured-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.featured-card h3 {
  font-size: 1.3rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.featured-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.featured-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 1rem;
}

.stat {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-accent);
}

.stat-label {
  font-size: 0.7rem;
  color: var(--text-secondary);
  text-transform: uppercase;
}

.featured-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: var(--text-accent);
  font-weight: 500;
  margin-top: 1rem;
}

.action-icon {
  transition: transform 0.3s ease;
}

.featured-card:hover .action-icon {
  transform: translateX(5px);
}

/* Tags Section */
.tags-section {
  margin-bottom: 4rem;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
}

.tag-item {
  background: rgba(26, 26, 46, 0.8);
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.tag-item:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  border-color: var(--text-accent);
  transform: translateY(-2px);
}

.tag-large {
  font-size: 1.1rem;
  font-weight: 600;
  padding: 0.75rem 1.25rem;
}

.tag-medium {
  font-size: 1rem;
  font-weight: 500;
  padding: 0.6rem 1.1rem;
}

/* Loading & Empty States */
.loading-section,
.empty-section {
  text-align: center;
  padding: 4rem 0;
}

.loading-spinner {
  font-size: 3rem;
  margin-bottom: 1rem;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-content {
  max-width: 400px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.empty-content p {
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

/* Responsive */
@media (max-width: 768px) {
  .categories-page {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
  }
  
  .featured-grid {
    grid-template-columns: 1fr;
  }
  
  .category-card {
    padding: 1.5rem;
  }
  
  .featured-stats {
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .category-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .category-stats {
    text-align: center;
  }
  
  .tags-cloud {
    justify-content: flex-start;
  }
  
  .tag-large,
  .tag-medium {
    font-size: 0.9rem;
    padding: 0.5rem 1rem;
  }
}
</style>