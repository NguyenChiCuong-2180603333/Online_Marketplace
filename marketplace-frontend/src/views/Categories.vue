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
          <button v-if="searchQuery" @click="clearSearch" class="clear-search-btn">❌</button>
        </div>
        
        <!-- Filter Options -->
        <div class="filter-options">
          <button 
            @click="sortBy = 'name'"
            :class="{ active: sortBy === 'name' }"
            class="filter-btn"
          >
            🔤 Tên A-Z
          </button>
          <button 
            @click="sortBy = 'popularity'"
            :class="{ active: sortBy === 'popularity' }"
            class="filter-btn"
          >
            🔥 Phổ biến
          </button>
          <button 
            @click="sortBy = 'products'"
            :class="{ active: sortBy === 'products' }"
            class="filter-btn"
          >
            📦 Số lượng
          </button>
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
                    @click.stop="viewCategory(subCat)"
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
                      <span v-for="i in 5" :key="i" class="star" :class="[i <= Math.round(category.averageRating) ? 'filled' : '']">⭐</span>
                    </span>
                    <span class="rating-value">{{ category.averageRating?.toFixed(1) || '0.0' }}/5</span>
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
      <div v-if="!loading && featuredCategories.length" class="featured-categories-section">
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
                  <span class="stat-number">{{ featured.averageRating?.toFixed(1) || '0.0' }}</span>
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
      <div v-if="!loading && popularTags.length" class="tags-section">
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
        <div class="cosmic-loader">
          <div class="planet"></div>
          <div class="orbit"></div>
          <div class="orbit orbit-2"></div>
        </div>
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
    const sortBy = ref('popularity')
    const loading = ref(true)
    
    // Computed
    const filteredCategories = computed(() => {
      let filtered = [...categories.value]
      
      // Apply search filter
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(category =>
          category.name.toLowerCase().includes(query) ||
          category.description.toLowerCase().includes(query) ||
          category.subCategories?.some(sub => 
            sub.name.toLowerCase().includes(query)
          )
        )
      }
      
      // Apply sorting
      switch (sortBy.value) {
        case 'name':
          filtered.sort((a, b) => a.name.localeCompare(b.name, 'vi'))
          break
        case 'popularity':
          filtered.sort((a, b) => b.popularity - a.popularity)
          break
        case 'products':
          filtered.sort((a, b) => b.productCount - a.productCount)
          break
      }
      
      return filtered
    })
    
    // Methods
    const loadCategories = async () => {
      try {
        loading.value = true
        
        // Load all categories
        const categoriesResponse = await categoryAPI.getAll()
        categories.value = categoriesResponse.data || mockCategories()
        
        // Load featured categories
        const featuredResponse = await categoryAPI.getFeatured()
        featuredCategories.value = featuredResponse.data || mockFeaturedCategories()
        
        // Load popular tags
        const tagsResponse = await categoryAPI.getPopularTags()
        popularTags.value = tagsResponse.data || mockPopularTags()
        
      } catch (error) {
        console.error('Error loading categories:', error)
        // Use mock data as fallback
        categories.value = mockCategories()
        featuredCategories.value = mockFeaturedCategories()
        popularTags.value = mockPopularTags()
      } finally {
        loading.value = false
      }
    }
    
    const viewCategory = (category) => {
      router.push({
        path: '/products',
        query: { category: category.id }
      })
    }
    
    const handleSearch = () => {
      // Search is handled by computed property
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
    }
    
    const searchByTag = (tagName) => {
      router.push({
        path: '/products',
        query: { tag: tagName }
      })
    }
    
    // Mock data functions
    const mockCategories = () => [
      {
        id: 1,
        name: 'Điện thoại & Phụ kiện',
        icon: '📱',
        description: 'Smartphone, tablet, phụ kiện di động cao cấp',
        productCount: 1250,
        averageRating: 4.3,
        popularity: 95,
        subCategories: [
          { id: 11, name: 'iPhone' },
          { id: 12, name: 'Samsung Galaxy' },
          { id: 13, name: 'Xiaomi' },
          { id: 14, name: 'Oppo' }
        ]
      },
      {
        id: 2,
        name: 'Laptop & Máy tính',
        icon: '💻',
        description: 'Laptop gaming, văn phòng, linh kiện máy tính',
        productCount: 890,
        averageRating: 4.5,
        popularity: 88,
        subCategories: [
          { id: 21, name: 'Gaming Laptop' },
          { id: 22, name: 'MacBook' },
          { id: 23, name: 'Ultrabook' }
        ]
      },
      {
        id: 3,
        name: 'Thời trang Nam',
        icon: '👔',
        description: 'Quần áo, giày dép, phụ kiện thời trang nam',
        productCount: 2100,
        averageRating: 4.2,
        popularity: 82,
        subCategories: [
          { id: 31, name: 'Áo sơ mi' },
          { id: 32, name: 'Quần jeans' },
          { id: 33, name: 'Giày sneaker' }
        ]
      },
      {
        id: 4,
        name: 'Thời trang Nữ',
        icon: '👗',
        description: 'Váy áo, túi xách, giày cao gót thời trang',
        productCount: 3200,
        averageRating: 4.4,
        popularity: 90,
        subCategories: [
          { id: 41, name: 'Váy đầm' },
          { id: 42, name: 'Túi xách' },
          { id: 43, name: 'Giày cao gót' }
        ]
      },
      {
        id: 5,
        name: 'Điện gia dụng',
        icon: '🏠',
        description: 'Tủ lạnh, máy giặt, điều hòa, đồ gia dụng',
        productCount: 650,
        averageRating: 4.1,
        popularity: 75,
        subCategories: [
          { id: 51, name: 'Tủ lạnh' },
          { id: 52, name: 'Máy giặt' },
          { id: 53, name: 'Điều hòa' }
        ]
      },
      {
        id: 6,
        name: 'Sách & Văn phòng phẩm',
        icon: '📚',
        description: 'Sách, vở, bút, đồ dùng học tập văn phòng',
        productCount: 1800,
        averageRating: 4.0,
        popularity: 65,
        subCategories: [
          { id: 61, name: 'Sách tiếng Việt' },
          { id: 62, name: 'Sách tiếng Anh' },
          { id: 63, name: 'Văn phòng phẩm' }
        ]
      }
    ]
    
    const mockFeaturedCategories = () => [
      {
        id: 1,
        name: 'Điện thoại & Phụ kiện',
        icon: '📱',
        description: 'Công nghệ di động hàng đầu',
        productCount: 1250,
        averageRating: 4.3,
        popularity: 95
      },
      {
        id: 4,
        name: 'Thời trang Nữ',
        icon: '👗',
        description: 'Xu hướng thời trang mới nhất',
        productCount: 3200,
        averageRating: 4.4,
        popularity: 90
      },
      {
        id: 2,
        name: 'Laptop & Máy tính',
        icon: '💻',
        description: 'Hiệu năng mạnh mẽ cho mọi nhu cầu',
        productCount: 890,
        averageRating: 4.5,
        popularity: 88
      }
    ]
    
    const mockPopularTags = () => [
      { name: 'Sale', count: 250 },
      { name: 'New', count: 180 },
      { name: 'Hot', count: 95 },
      { name: 'Trending', count: 120 },
      { name: 'Gaming', count: 85 },
      { name: 'Fashion', count: 200 },
      { name: 'Tech', count: 160 },
      { name: 'Home', count: 75 },
      { name: 'Beauty', count: 110 },
      { name: 'Sport', count: 90 }
    ]
    
    // Lifecycle
    onMounted(() => {
      loadCategories()
    })
    
    return {
      categories,
      featuredCategories,
      popularTags,
      searchQuery,
      sortBy,
      loading,
      filteredCategories,
      viewCategory,
      handleSearch,
      clearSearch,
      searchByTag
    }
  }
}
</script>

<style scoped>
/* Categories Page */
.categories-page {
  min-height: 100vh;
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  background: var(--aurora-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
}

/* Search Section */
.search-section {
  margin-bottom: 3rem;
}

.search-box {
  position: relative;
  max-width: 600px;
  margin: 0 auto 2rem;
}

.search-input {
  width: 100%;
  padding: 1rem 1.5rem;
  padding-right: 3rem;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 25px;
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--text-accent);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.2);
}

.search-input::placeholder {
  color: var(--text-secondary);
}

.clear-search-btn {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.9rem;
  padding: 0.25rem;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.clear-search-btn:hover {
  color: var(--text-accent);
  background: rgba(0, 212, 255, 0.1);
}

/* Filter Options */
.filter-options {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 20px;
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.filter-btn:hover,
.filter-btn.active {
  background: var(--aurora-gradient);
  color: white;
  border-color: transparent;
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
  filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.3));
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
  cursor: pointer;
  transition: all 0.3s ease;
}

.sub-category-tag:hover {
  background: rgba(0, 212, 255, 0.2);
  transform: translateY(-1px);
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
  filter: drop-shadow(0 0 15px rgba(0, 212, 255, 0.5));
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
  font-weight: 600;
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
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.tag-item:hover {
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  transform: translateY(-2px);
  border-color: rgba(0, 212, 255, 0.4);
}

.tag-item.tag-large {
  font-size: 1.1rem;
  padding: 0.75rem 1.25rem;
  background: rgba(0, 212, 255, 0.1);
  color: var(--text-accent);
  border-color: rgba(0, 212, 255, 0.4);
}

.tag-item.tag-medium {
  font-size: 1rem;
  padding: 0.6rem 1.1rem;
  background: rgba(255, 255, 255, 0.15);
}

/* Loading Section */
.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 4rem 0;
}

.cosmic-loader {
  position: relative;
  width: 80px;
  height: 80px;
}

.planet {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  background: var(--aurora-gradient);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.6);
  animation: planetPulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  top: 50%;
  left: 50%;
  border: 2px solid rgba(0, 212, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: rotate 3s linear infinite;
}

.orbit:nth-child(2) {
  width: 40px;
  height: 40px;
}

.orbit.orbit-2 {
  width: 60px;
  height: 60px;
  border-color: rgba(142, 68, 173, 0.3);
  animation-duration: 4s;
  animation-direction: reverse;
}

@keyframes rotate {
  0% { transform: translate(-50%, -50%) rotate(0deg); }
  100% { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes planetPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}

/* Empty Section */
.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
}

.empty-content {
  text-align: center;
  max-width: 400px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-content h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.empty-content p {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .categories-grid {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
  
  .featured-grid {
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
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .search-section {
    margin-bottom: 2rem;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .category-card {
    padding: 1.5rem;
  }
  
  .featured-card {
    padding: 1.5rem;
    min-height: 180px;
  }
  
  .filter-options {
    gap: 0.5rem;
  }
  
  .filter-btn {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }
  
  .tags-cloud {
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 0.5rem;
  }
  
  .page-header {
    margin-bottom: 2rem;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .search-input {
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
  }
  
  .category-card {
    padding: 1rem;
  }
  
  .category-icon {
    font-size: 2.5rem;
  }
  
  .category-name {
    font-size: 1.2rem;
  }
  
  .featured-card {
    padding: 1rem;
    min-height: 150px;
  }
  
  .featured-icon {
    font-size: 2.5rem;
  }
  
  .featured-stats {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .stat {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .stat-number {
    font-size: 1rem;
  }
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
  .search-input {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .filter-btn {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .tag-item {
    background: rgba(255, 255, 255, 0.05);
  }
}

/* High Contrast Mode */
@media (prefers-contrast: high) {
  .category-card,
  .featured-card {
    border: 2px solid var(--text-primary);
  }
  
  .filter-btn {
    border-width: 2px;
  }
  
  .tag-item {
    border-width: 2px;
  }
}

/* Reduced Motion */
@media (prefers-reduced-motion: reduce) {
  .category-card,
  .featured-card,
  .tag-item,
  .sub-category-tag {
    transition: none;
  }
  
  .category-card:hover,
  .featured-card:hover {
    transform: none;
  }
  
  .planet,
  .orbit {
    animation: none;
  }
}

/* Print Styles */
@media print {
  .search-section,
  .filter-options,
  .category-overlay,
  .loading-section {
    display: none !important;
  }
  
  .categories-grid,
  .featured-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  
  .category-card,
  .featured-card {
    break-inside: avoid;
    border: 1px solid black !important;
    background: white !important;
    color: black !important;
  }
}
</style>
