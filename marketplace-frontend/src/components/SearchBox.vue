<template>
  <div class="search-box-container" ref="searchContainer">
    <!-- Search Input -->
    <div class="search-input-wrapper">
      <div class="search-icon">🔍</div>
      <input
        ref="searchInput"
        v-model="searchQuery"
        type="text"
        class="search-input"
        :placeholder="placeholder"
        @input="handleInput"
        @focus="handleFocus"
        @keydown="handleKeydown"
        @blur="handleBlur"
      />
      
      <!-- Clear Button -->
      <button
        v-if="searchQuery"
        @click="clearSearch"
        class="clear-btn"
        type="button"
      >
        ✕
      </button>
      
      <!-- Voice Search Button -->
      <button
        v-if="showVoiceSearch"
        @click="toggleVoiceSearch"
        class="voice-btn"
        :class="{ active: isListening }"
        type="button"
        :title="isListening ? 'Đang nghe...' : 'Tìm kiếm bằng giọng nói'"
      >
        {{ isListening ? '🎤' : '🎙️' }}
      </button>
    </div>

    <!-- Search Suggestions Dropdown -->
    <transition name="dropdown">
      <div v-if="showDropdown" class="search-dropdown">
        <!-- Loading State -->
        <div v-if="loading" class="dropdown-section">
          <div class="loading-item">
            <span class="loading-icon">⏳</span>
            <span>Đang tìm kiếm...</span>
          </div>
        </div>

        <!-- No Results -->
        <div v-else-if="!hasResults" class="dropdown-section">
          <div class="no-results">
            <span class="no-results-icon">🔍</span>
            <span>Không tìm thấy kết quả cho "{{ searchQuery }}"</span>
          </div>
        </div>

        <!-- Search Results -->
        <template v-else>
          <!-- Quick Actions -->
          <div v-if="quickActions.length > 0" class="dropdown-section">
            <h4 class="section-title">⚡ Hành động nhanh</h4>
            <div
              v-for="action in quickActions"
              :key="action.id"
              @click="executeAction(action)"
              class="suggestion-item action-item"
              :class="{ highlighted: highlightedIndex === action.index }"
            >
              <div class="item-icon">{{ action.icon }}</div>
              <div class="item-content">
                <div class="item-title">{{ action.title }}</div>
                <div class="item-description">{{ action.description }}</div>
              </div>
            </div>
          </div>

          <!-- Product Suggestions -->
          <div v-if="productSuggestions.length > 0" class="dropdown-section">
            <h4 class="section-title">📦 Sản phẩm</h4>
            <div
              v-for="product in productSuggestions"
              :key="product.id"
              @click="selectProduct(product)"
              class="suggestion-item product-item"
              :class="{ highlighted: highlightedIndex === product.index }"
            >
              <img :src="product.image" :alt="product.name" class="product-image" />
              <div class="item-content">
                <div class="item-title" v-html="highlightText(product.name)"></div>
                <div class="item-price">{{ formatPrice(product.price) }}</div>
                <div class="item-rating">
                  <span class="stars">{{ getStarRating(product.rating) }}</span>
                  <span class="rating-count">({{ product.reviewCount }})</span>
                </div>
              </div>
              <div class="item-meta">
                <span class="stock-status" :class="{ 'in-stock': product.inStock }">
                  {{ product.inStock ? '✅ Còn hàng' : '❌ Hết hàng' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Category Suggestions -->
          <div v-if="categorySuggestions.length > 0" class="dropdown-section">
            <h4 class="section-title">🗂️ Danh mục</h4>
            <div
              v-for="category in categorySuggestions"
              :key="category.id"
              @click="selectCategory(category)"
              class="suggestion-item category-item"
              :class="{ highlighted: highlightedIndex === category.index }"
            >
              <div class="item-icon">{{ category.icon }}</div>
              <div class="item-content">
                <div class="item-title" v-html="highlightText(category.name)"></div>
                <div class="item-description">{{ category.productCount }} sản phẩm</div>
              </div>
            </div>
          </div>

          <!-- Search History -->
          <div v-if="searchHistory.length > 0 && !searchQuery" class="dropdown-section">
            <h4 class="section-title">
              🕒 Tìm kiếm gần đây
              <button @click="clearHistory" class="clear-history-btn">Xóa tất cả</button>
            </h4>
            <div
              v-for="(historyItem, index) in searchHistory"
              :key="index"
              @click="selectHistoryItem(historyItem)"
              class="suggestion-item history-item"
              :class="{ highlighted: highlightedIndex === historyItem.index }"
            >
              <div class="item-icon">🕒</div>
              <div class="item-content">
                <div class="item-title">{{ historyItem.query }}</div>
                <div class="item-description">{{ formatRelativeTime(historyItem.timestamp) }}</div>
              </div>
              <button 
                @click.stop="removeHistoryItem(index)"
                class="remove-history-btn"
                title="Xóa khỏi lịch sử"
              >
                ✕
              </button>
            </div>
          </div>

          <!-- Popular Searches -->
          <div v-if="popularSearches.length > 0 && !searchQuery" class="dropdown-section">
            <h4 class="section-title">🔥 Tìm kiếm phổ biến</h4>
            <div class="popular-tags">
              <span
                v-for="tag in popularSearches"
                :key="tag"
                @click="selectPopularSearch(tag)"
                class="popular-tag"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <!-- Search All Results -->
          <div v-if="searchQuery" class="dropdown-section">
            <div
              @click="searchAll"
              class="suggestion-item search-all-item"
              :class="{ highlighted: highlightedIndex === allResultsIndex }"
            >
              <div class="item-icon">🔍</div>
              <div class="item-content">
                <div class="item-title">Tìm kiếm tất cả "{{ searchQuery }}"</div>
                <div class="item-description">Xem tất cả kết quả tìm kiếm</div>
              </div>
              <div class="item-meta">
                <span class="enter-hint">↵ Enter</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </transition>

    <!-- Search Overlay (for mobile) -->
    <div v-if="showMobileOverlay" class="mobile-overlay" @click="closeMobileSearch">
      <div class="mobile-search-container" @click.stop>
        <div class="mobile-search-header">
          <h3>🔍 Tìm kiếm</h3>
          <button @click="closeMobileSearch" class="mobile-close-btn">✕</button>
        </div>
        
        <div class="mobile-search-input">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm sản phẩm, danh mục..."
            @input="handleInput"
            @keydown="handleKeydown"
            autofocus
          />
        </div>
        
        <!-- Mobile Results -->
        <div class="mobile-results">
          <!-- Same content as dropdown but mobile-optimized -->
          <div v-if="productSuggestions.length > 0" class="mobile-section">
            <h4>📦 Sản phẩm</h4>
            <div
              v-for="product in productSuggestions.slice(0, 5)"
              :key="product.id"
              @click="selectProduct(product)"
              class="mobile-result-item"
            >
              <img :src="product.image" :alt="product.name" class="mobile-product-image" />
              <div class="mobile-item-content">
                <div class="mobile-item-title">{{ product.name }}</div>
                <div class="mobile-item-price">{{ formatPrice(product.price) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { searchAPI } from '@/services/api'

export default {
  name: 'SearchBox',
  props: {
    placeholder: {
      type: String,
      default: 'Tìm kiếm sản phẩm, danh mục...'
    },
    showVoiceSearch: {
      type: Boolean,
      default: true
    },
    maxSuggestions: {
      type: Number,
      default: 5
    },
    mobile: {
      type: Boolean,
      default: false
    }
  },
  emits: ['search', 'select-product', 'select-category'],
  setup(props, { emit }) {
    const router = useRouter()
    
    // Refs
    const searchContainer = ref(null)
    const searchInput = ref(null)
    
    // Reactive data
    const searchQuery = ref('')
    const showDropdown = ref(false)
    const showMobileOverlay = ref(false)
    const loading = ref(false)
    const highlightedIndex = ref(-1)
    const isListening = ref(false)
    
    // Search results
    const productSuggestions = ref([])
    const categorySuggestions = ref([])
    const quickActions = ref([])
    
    // Search history (stored in localStorage)
    const searchHistory = ref([])
    const popularSearches = ref([
      'iPhone', 'Samsung', 'Laptop', 'Áo thun', 'Giày sneaker',
      'Tai nghe', 'Ốp lưng', 'Sách', 'Đồng hồ', 'Túi xách'
    ])
    
    // Debounce timer
    let searchTimeout = null
    let recognition = null
    
    // Computed properties
    const hasResults = computed(() => 
      productSuggestions.value.length > 0 || 
      categorySuggestions.value.length > 0 || 
      quickActions.value.length > 0 ||
      (searchHistory.value.length > 0 && !searchQuery.value)
    )
    
    const allSuggestions = computed(() => {
      let suggestions = []
      let index = 0
      
      // Add quick actions
      quickActions.value.forEach(action => {
        suggestions.push({ ...action, index: index++ })
      })
      
      // Add products
      productSuggestions.value.forEach(product => {
        suggestions.push({ ...product, index: index++ })
      })
      
      // Add categories
      categorySuggestions.value.forEach(category => {
        suggestions.push({ ...category, index: index++ })
      })
      
      // Add history items
      if (!searchQuery.value) {
        searchHistory.value.forEach(item => {
          suggestions.push({ ...item, index: index++ })
        })
      }
      
      return suggestions
    })
    
    const allResultsIndex = computed(() => allSuggestions.value.length)
    
    // Methods
    const handleInput = () => {
      clearTimeout(searchTimeout)
      searchTimeout = setTimeout(() => {
        if (searchQuery.value.trim()) {
          performSearch()
        } else {
          clearSuggestions()
        }
      }, 300)
    }
    
    const handleFocus = () => {
      showDropdown.value = true
      loadSearchHistory()
      if (!searchQuery.value) {
        showDefaultSuggestions()
      }
    }
    
    const handleBlur = () => {
      // Delay hiding dropdown to allow clicks
      setTimeout(() => {
        showDropdown.value = false
        highlightedIndex.value = -1
      }, 200)
    }
    
    const handleKeydown = (event) => {
      if (!showDropdown.value) return
      
      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault()
          highlightedIndex.value = Math.min(
            highlightedIndex.value + 1,
            allResultsIndex.value
          )
          break
          
        case 'ArrowUp':
          event.preventDefault()
          highlightedIndex.value = Math.max(highlightedIndex.value - 1, -1)
          break
          
        case 'Enter':
          event.preventDefault()
          if (highlightedIndex.value === allResultsIndex.value || highlightedIndex.value === -1) {
            searchAll()
          } else {
            const suggestion = allSuggestions.value[highlightedIndex.value]
            if (suggestion) {
              selectSuggestion(suggestion)
            }
          }
          break
          
        case 'Escape':
          showDropdown.value = false
          searchInput.value?.blur()
          break
      }
    }
    
    const performSearch = async () => {
      if (!searchQuery.value.trim()) return
      
      loading.value = true
      
      try {
        // Generate quick actions based on search query
        generateQuickActions()
        
        // Search products
        const productResponse = await searchAPI.products(searchQuery.value, { limit: props.maxSuggestions })
        productSuggestions.value = productResponse.data.products || []
        
        // Search categories
        const categoryResponse = await searchAPI.suggestions(searchQuery.value)
        categorySuggestions.value = categoryResponse.data.categories || []
        
      } catch (error) {
        console.error('Search error:', error)
        clearSuggestions()
      } finally {
        loading.value = false
      }
    }
    
    const generateQuickActions = () => {
      const query = searchQuery.value.toLowerCase()
      const actions = []
      
      // Add category search action
      actions.push({
        id: 'search-category',
        icon: '🗂️',
        title: `Tìm trong danh mục`,
        description: `Tìm "${searchQuery.value}" trong tất cả danh mục`,
        type: 'category-search'
      })
      
      // Add price filter actions
      if (query.includes('rẻ') || query.includes('giá thấp')) {
        actions.push({
          id: 'low-price',
          icon: '💰',
          title: 'Sản phẩm giá rẻ',
          description: 'Lọc sản phẩm có giá dưới 500.000đ',
          type: 'price-filter',
          data: { maxPrice: 500000 }
        })
      }
      
      // Add sale filter action
      if (query.includes('sale') || query.includes('giảm giá')) {
        actions.push({
          id: 'on-sale',
          icon: '🏷️',
          title: 'Sản phẩm đang sale',
          description: 'Chỉ hiển thị sản phẩm đang giảm giá',
          type: 'sale-filter'
        })
      }
      
      quickActions.value = actions
    }
    
    const clearSuggestions = () => {
      productSuggestions.value = []
      categorySuggestions.value = []
      quickActions.value = []
    }
    
    const showDefaultSuggestions = () => {
      // Show popular searches and recent history when no query
      clearSuggestions()
    }
    
    const selectSuggestion = (suggestion) => {
      if (suggestion.type === 'product') {
        selectProduct(suggestion)
      } else if (suggestion.type === 'category') {
        selectCategory(suggestion)
      } else if (suggestion.type === 'action') {
        executeAction(suggestion)
      } else if (suggestion.type === 'history') {
        selectHistoryItem(suggestion)
      }
    }
    
    const selectProduct = (product) => {
      addToHistory(product.name)
      showDropdown.value = false
      emit('select-product', product)
      router.push(`/products/${product.id}`)
    }
    
    const selectCategory = (category) => {
      addToHistory(category.name)
      showDropdown.value = false
      emit('select-category', category)
      router.push(`/categories?category=${category.id}`)
    }
    
    const executeAction = (action) => {
      showDropdown.value = false
      
      switch (action.type) {
        case 'category-search':
          router.push(`/products?q=${encodeURIComponent(searchQuery.value)}`)
          break
        case 'price-filter':
          router.push(`/products?q=${encodeURIComponent(searchQuery.value)}&maxPrice=${action.data.maxPrice}`)
          break
        case 'sale-filter':
          router.push(`/products?q=${encodeURIComponent(searchQuery.value)}&onSale=true`)
          break
      }
      
      addToHistory(searchQuery.value)
    }
    
    const selectHistoryItem = (historyItem) => {
      searchQuery.value = historyItem.query
      performSearch()
    }
    
    const selectPopularSearch = (tag) => {
      searchQuery.value = tag
      performSearch()
      addToHistory(tag)
    }
    
    const searchAll = () => {
      if (!searchQuery.value.trim()) return
      
      addToHistory(searchQuery.value)
      showDropdown.value = false
      emit('search', searchQuery.value)
      router.push(`/products?q=${encodeURIComponent(searchQuery.value)}`)
    }
    
    const clearSearch = () => {
      searchQuery.value = ''
      clearSuggestions()
      searchInput.value?.focus()
    }
    
    const addToHistory = (query) => {
      if (!query || query.trim().length < 2) return
      
      const trimmedQuery = query.trim()
      const existingIndex = searchHistory.value.findIndex(item => item.query === trimmedQuery)
      
      if (existingIndex > -1) {
        // Move to top
        searchHistory.value.splice(existingIndex, 1)
      }
      
      searchHistory.value.unshift({
        query: trimmedQuery,
        timestamp: Date.now()
      })
      
      // Keep only last 10 searches
      searchHistory.value = searchHistory.value.slice(0, 10)
      saveSearchHistory()
    }
    
    const loadSearchHistory = () => {
      try {
        const stored = localStorage.getItem('searchHistory')
        if (stored) {
          searchHistory.value = JSON.parse(stored)
        }
      } catch (error) {
        console.error('Error loading search history:', error)
        searchHistory.value = []
      }
    }
    
    const saveSearchHistory = () => {
      try {
        localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
      } catch (error) {
        console.error('Error saving search history:', error)
      }
    }
    
    const clearHistory = () => {
      searchHistory.value = []
      saveSearchHistory()
    }
    
    const removeHistoryItem = (index) => {
      searchHistory.value.splice(index, 1)
      saveSearchHistory()
    }
    
    // Voice search
    const toggleVoiceSearch = () => {
      if (isListening.value) {
        stopVoiceSearch()
      } else {
        startVoiceSearch()
      }
    }
    
    const startVoiceSearch = () => {
      if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
        alert('Trình duyệt không hỗ trợ nhận dạng giọng nói')
        return
      }
      
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      recognition = new SpeechRecognition()
      
      recognition.lang = 'vi-VN'
      recognition.continuous = false
      recognition.interimResults = false
      
      recognition.onstart = () => {
        isListening.value = true
      }
      
      recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript
        searchQuery.value = transcript
        performSearch()
      }
      
      recognition.onerror = (event) => {
        console.error('Speech recognition error:', event.error)
        isListening.value = false
      }
      
      recognition.onend = () => {
        isListening.value = false
      }
      
      recognition.start()
    }
    
    const stopVoiceSearch = () => {
      if (recognition) {
        recognition.stop()
        isListening.value = false
      }
    }
    
    // Mobile search
    const openMobileSearch = () => {
      showMobileOverlay.value = true
      nextTick(() => {
        const mobileInput = document.querySelector('.mobile-search-input input')
        mobileInput?.focus()
      })
    }
    
    const closeMobileSearch = () => {
      showMobileOverlay.value = false
      searchQuery.value = ''
      clearSuggestions()
    }
    
    // Utility methods
    const highlightText = (text) => {
      if (!searchQuery.value) return text
      
      const regex = new RegExp(`(${searchQuery.value})`, 'gi')
      return text.replace(regex, '<mark>$1</mark>')
    }
    
    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
    
    const getStarRating = (rating) => {
      const stars = Math.round(rating || 0)
      return '⭐'.repeat(stars) + '☆'.repeat(5 - stars)
    }
    
    const formatRelativeTime = (timestamp) => {
      const now = Date.now()
      const diff = now - timestamp
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 1) return 'Vừa xong'
      if (minutes < 60) return `${minutes} phút trước`
      if (hours < 24) return `${hours} giờ trước`
      if (days < 7) return `${days} ngày trước`
      return new Date(timestamp).toLocaleDateString('vi-VN')
    }
    
    // Click outside handler
    const handleClickOutside = (event) => {
      if (searchContainer.value && !searchContainer.value.contains(event.target)) {
        showDropdown.value = false
      }
    }
    
    // Lifecycle
    onMounted(() => {
      loadSearchHistory()
      document.addEventListener('click', handleClickOutside)
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      clearTimeout(searchTimeout)
      if (recognition) {
        recognition.abort()
      }
    })
    
    // Expose methods for parent components
    const focus = () => {
      searchInput.value?.focus()
    }
    
    const blur = () => {
      searchInput.value?.blur()
    }
    
    const setQuery = (query) => {
      searchQuery.value = query
      performSearch()
    }
    
    return {
      // Refs
      searchContainer,
      searchInput,
      
      // Data
      searchQuery,
      showDropdown,
      showMobileOverlay,
      loading,
      highlightedIndex,
      isListening,
      productSuggestions,
      categorySuggestions,
      quickActions,
      searchHistory,
      popularSearches,
      
      // Computed
      hasResults,
      allResultsIndex,
      
      // Methods
      handleInput,
      handleFocus,
      handleBlur,
      handleKeydown,
      selectProduct,
      selectCategory,
      executeAction,
      selectHistoryItem,
      selectPopularSearch,
      searchAll,
      clearSearch,
      clearHistory,
      removeHistoryItem,
      toggleVoiceSearch,
      openMobileSearch,
      closeMobileSearch,
      highlightText,
      formatPrice,
      getStarRating,
      formatRelativeTime,
      
      // Exposed methods
      focus,
      blur,
      setQuery
    }
  }
}
</script>

<style scoped>
.search-box-container {
  position: relative;
  width: 100%;
  max-width: 500px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.search-input-wrapper:focus-within {
  border-color: var(--accent-blue);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.search-icon {
  padding: 0 12px;
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.search-input {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 1rem;
  outline: none;
}

.search-input::placeholder {
  color: var(--text-secondary);
}

.clear-btn,
.voice-btn {
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.clear-btn:hover,
.voice-btn:hover {
  background: var(--bg-primary);
  color: var(--text-primary);
}

.voice-btn.active {
  color: var(--accent-red);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
  margin-top: 4px;
}

.dropdown-section {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.dropdown-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 16px 8px;
  margin: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clear-history-btn {
  font-size: 0.75rem;
  color: var(--accent-red);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}

.clear-history-btn:hover {
  background: rgba(244, 67, 54, 0.1);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background: var(--bg-primary);
}

.item-icon {
  font-size: 1.2rem;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.product-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-title :deep(mark) {
  background: var(--accent-yellow);
  color: #8B4513;
  padding: 0 2px;
  border-radius: 2px;
}

.item-description {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.item-price {
  font-size: 0.85rem;
  color: var(--accent-blue);
  font-weight: 500;
}

.item-rating {
  font-size: 0.8rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.stars {
  color: var(--accent-yellow);
}

.item-meta {
  flex-shrink: 0;
  text-align: right;
}

.stock-status {
  font-size: 0.8rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(244, 67, 54, 0.1);
  color: var(--accent-red);
}

.stock-status.in-stock {
  background: rgba(76, 175, 80, 0.1);
  color: var(--accent-green);
}

.enter-hint {
  font-size: 0.8rem;
  color: var(--text-secondary);
  background: var(--bg-primary);
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.remove-history-btn {
  padding: 4px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 4px;
  font-size: 0.8rem;
  opacity: 0;
  transition: all 0.2s ease;
}

.history-item:hover .remove-history-btn {
  opacity: 1;
}

.remove-history-btn:hover {
  background: var(--accent-red);
  color: white;
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px;
}

.popular-tag {
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid var(--border-color);
}

.popular-tag:hover {
  background: var(--accent-blue);
  color: white;
  border-color: var(--accent-blue);
}

.loading-item,
.no-results {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: var(--text-secondary);
}

.loading-icon,
.no-results-icon {
  font-size: 1.2rem;
}

.search-all-item {
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  font-weight: 500;
}

.search-all-item:hover {
  background: var(--accent-blue);
  color: white;
}

/* Mobile Overlay */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 2000;
  padding: 20px;
}

.mobile-search-container {
  background: var(--bg-secondary);
  border-radius: 12px;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.mobile-search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.mobile-search-header h3 {
  margin: 0;
  color: var(--text-primary);
}

.mobile-close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
}

.mobile-search-input {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.mobile-search-input input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 1rem;
}

.mobile-results {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.mobile-section {
  margin-bottom: 24px;
}

.mobile-section h4 {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.mobile-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
}

.mobile-result-item:last-child {
  border-bottom: none;
}

.mobile-product-image {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
}

.mobile-item-content {
  flex: 1;
}

.mobile-item-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.mobile-item-price {
  font-size: 0.8rem;
  color: var(--accent-blue);
  font-weight: 500;
}

/* Dropdown animations */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive */
@media (max-width: 768px) {
  .search-box-container {
    max-width: none;
  }
  
  .search-input-wrapper {
    border-radius: 8px;
  }
  
  .search-dropdown {
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
  
  .suggestion-item {
    padding: 16px;
  }
  
  .item-title {
    font-size: 1rem;
  }
  
  .item-description {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .search-input {
    font-size: 16px; /* Prevent zoom on iOS */
  }
  
  .popular-tags {
    padding: 0 12px;
  }
  
  .popular-tag {
    padding: 8px 12px;
    font-size: 0.9rem;
  }
}
</style>