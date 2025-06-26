import { sellerAPI } from '@/services/sellerAPI'
import { formatCurrency, showNotification } from '@/utils/sellerUtils'

export const formatCurrency = (amount, compact = false) => {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '0 ₫'
  }

  if (compact && amount >= 1000000) {
    const millions = amount / 1000000
    return `${millions.toFixed(1)}M ₫`
  }

  if (compact && amount >= 1000) {
    const thousands = amount / 1000
    return `${thousands.toFixed(1)}K ₫`
  }

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

export const formatPercentage = (value, decimals = 1) => {
  if (value === null || value === undefined || isNaN(value)) {
    return '0%'
  }
  
  const sign = value > 0 ? '+' : ''
  return `${sign}${value.toFixed(decimals)}%`
}


export const formatDate = (date, options = {}) => {
  if (!date) return '-'
  
  const defaultOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    ...options
  }
  
  return new Date(date).toLocaleDateString('vi-VN', defaultOptions)
}


export const formatDateTime = (date) => {
  if (!date) return '-'
  
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export const formatRelativeTime = (date) => {
  if (!date) return '-'
  
  const now = new Date()
  const past = new Date(date)
  const diffInSeconds = Math.floor((now - past) / 1000)
  
  if (diffInSeconds < 60) {
    return 'Vừa xong'
  } else if (diffInSeconds < 3600) {
    const minutes = Math.floor(diffInSeconds / 60)
    return `${minutes} phút trước`
  } else if (diffInSeconds < 86400) {
    const hours = Math.floor(diffInSeconds / 3600)
    return `${hours} giờ trước`
  } else if (diffInSeconds < 2592000) {
    const days = Math.floor(diffInSeconds / 86400)
    return `${days} ngày trước`
  } else {
    return formatDate(date)
  }
}

export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export const getOrderStatusConfig = (status) => {
  const configs = {
    'PENDING': {
      label: 'Chờ xử lý',
      color: '#f59e0b',
      bgColor: 'rgba(245, 158, 11, 0.1)',
      icon: '⏳',
      canUpdate: ['PROCESSING', 'CANCELLED']
    },
    'PROCESSING': {
      label: 'Đang xử lý',
      color: '#3b82f6',
      bgColor: 'rgba(59, 130, 246, 0.1)',
      icon: '⚙️',
      canUpdate: ['SHIPPED', 'CANCELLED']
    },
    'SHIPPED': {
      label: 'Đã gửi hàng',
      color: '#8b5cf6',
      bgColor: 'rgba(139, 92, 246, 0.1)',
      icon: '🚚',
      canUpdate: ['DELIVERED']
    },
    'DELIVERED': {
      label: 'Đã giao hàng',
      color: '#10b981',
      bgColor: 'rgba(16, 185, 129, 0.1)',
      icon: '✅',
      canUpdate: []
    },
    'CANCELLED': {
      label: 'Đã hủy',
      color: '#ef4444',
      bgColor: 'rgba(239, 68, 68, 0.1)',
      icon: '❌',
      canUpdate: []
    }
  }
  
  return configs[status] || configs['PENDING']
}


export const getProductStatusConfig = (isActive) => {
  return isActive ? {
    label: 'Đang bán',
    color: '#10b981',
    bgColor: 'rgba(16, 185, 129, 0.1)',
    icon: '✅'
  } : {
    label: 'Tạm ngưng',
    color: '#6b7280',
    bgColor: 'rgba(107, 114, 128, 0.1)',
    icon: '⏸️'
  }
}


export const getStockStatus = (stock, lowStockThreshold = 10) => {
  if (stock === 0) {
    return {
      label: 'Hết hàng',
      color: '#ef4444',
      level: 'out-of-stock'
    }
  } else if (stock <= lowStockThreshold) {
    return {
      label: 'Sắp hết',
      color: '#f59e0b',
      level: 'low-stock'
    }
  } else {
    return {
      label: 'Còn hàng',
      color: '#10b981',
      level: 'in-stock'
    }
  }
}

// ==================== VALIDATION UTILITIES ====================

export const validateProductData = (product) => {
  const errors = {}
  
  if (!product.name || product.name.trim().length < 3) {
    errors.name = 'Tên sản phẩm phải có ít nhất 3 ký tự'
  }
  
  if (!product.price || product.price <= 0) {
    errors.price = 'Giá sản phẩm phải lớn hơn 0'
  }
  
  if (!product.category) {
    errors.category = 'Vui lòng chọn danh mục sản phẩm'
  }
  
  if (!product.description || product.description.trim().length < 10) {
    errors.description = 'Mô tả sản phẩm phải có ít nhất 10 ký tự'
  }
  
  if (product.stockQuantity === undefined || product.stockQuantity < 0) {
    errors.stockQuantity = 'Số lượng tồn kho không được âm'
  }
  
  if (!product.images || product.images.length === 0) {
    errors.images = 'Sản phẩm phải có ít nhất 1 hình ảnh'
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  }
}

export const validateOrderData = (order) => {
  const errors = {}
  
  if (!order.customerName) {
    errors.customerName = 'Tên khách hàng là bắt buộc'
  }
  
  if (!order.customerEmail) {
    errors.customerEmail = 'Email khách hàng là bắt buộc'
  }
  
  if (!order.shippingAddress) {
    errors.shippingAddress = 'Địa chỉ giao hàng là bắt buộc'
  }
  
  if (!order.items || order.items.length === 0) {
    errors.items = 'Đơn hàng phải có ít nhất 1 sản phẩm'
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  }
}

// ==================== CALCULATION UTILITIES ====================

export const calculateOrderTotal = (items = [], shippingCost = 0, tax = 0) => {
  const subtotal = items.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
  
  const total = subtotal + shippingCost + tax
  
  return {
    subtotal,
    shippingCost,
    tax,
    total,
    itemCount: items.reduce((sum, item) => sum + item.quantity, 0)
  }
}


export const calculatePercentageChange = (current, previous) => {
  if (previous === 0) {
    return current > 0 ? 100 : 0
  }
  
  return ((current - previous) / previous) * 100
}


export const calculateConversionRate = (conversions, total) => {
  if (total === 0) return 0
  return (conversions / total) * 100
}

// ==================== SEARCH & FILTER UTILITIES ====================


export const filterProducts = (products, filters) => {
  return products.filter(product => {
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase()
      const searchableText = [
        product.name,
        product.description,
        product.sku,
        product.category
      ].join(' ').toLowerCase()
      
      if (!searchableText.includes(searchTerm)) {
        return false
      }
    }
    
    if (filters.status && filters.status !== 'all') {
      if (filters.status === 'active' && !product.isActive) return false
      if (filters.status === 'inactive' && product.isActive) return false
      if (filters.status === 'low-stock' && product.stockQuantity > 10) return false
      if (filters.status === 'out-of-stock' && product.stockQuantity > 0) return false
    }
    
    if (filters.category && filters.category !== 'all') {
      if (product.category !== filters.category) return false
    }
    
    if (filters.priceMin && product.price < filters.priceMin) return false
    if (filters.priceMax && product.price > filters.priceMax) return false
    
    if (filters.dateFrom) {
      const productDate = new Date(product.createdAt)
      const fromDate = new Date(filters.dateFrom)
      if (productDate < fromDate) return false
    }
    
    if (filters.dateTo) {
      const productDate = new Date(product.createdAt)
      const toDate = new Date(filters.dateTo)
      if (productDate > toDate) return false
    }
    
    return true
  })
}


export const sortProducts = (products, sortBy, order = 'desc') => {
  return [...products].sort((a, b) => {
    let aValue, bValue
    
    switch (sortBy) {
      case 'name':
        aValue = a.name.toLowerCase()
        bValue = b.name.toLowerCase()
        break
      case 'price':
        aValue = a.price
        bValue = b.price
        break
      case 'stock':
        aValue = a.stockQuantity
        bValue = b.stockQuantity
        break
      case 'sales':
        aValue = a.soldCount || 0
        bValue = b.soldCount || 0
        break
      case 'created':
        aValue = new Date(a.createdAt)
        bValue = new Date(b.createdAt)
        break
      case 'updated':
        aValue = new Date(a.updatedAt)
        bValue = new Date(b.updatedAt)
        break
      default:
        aValue = a[sortBy]
        bValue = b[sortBy]
    }
    
    if (aValue < bValue) {
      return order === 'asc' ? -1 : 1
    }
    if (aValue > bValue) {
      return order === 'asc' ? 1 : -1
    }
    return 0
  })
}

// ==================== NOTIFICATION UTILITIES ====================


export const showNotification = (message, type = 'info', duration = 5000) => {
  const notification = document.createElement('div')
  notification.className = `notification notification-${type}`
  notification.innerHTML = `
    <div class="notification-content">
      <span class="notification-icon">${getNotificationIcon(type)}</span>
      <span class="notification-message">${message}</span>
      <button class="notification-close">✕</button>
    </div>
  `
  
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 10000;
    background: rgba(26, 26, 46, 0.95);
    border: 1px solid ${getNotificationColor(type)};
    border-radius: 8px;
    padding: 16px;
    color: white;
    font-size: 14px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    animation: slideIn 0.3s ease;
    max-width: 400px;
    min-width: 300px;
  `
  
  document.body.appendChild(notification)
  
  const timer = setTimeout(() => {
    removeNotification(notification)
  }, duration)
  
  notification.querySelector('.notification-close').addEventListener('click', () => {
    clearTimeout(timer)
    removeNotification(notification)
  })
}


const removeNotification = (notification) => {
  notification.style.animation = 'slideOut 0.3s ease'
  setTimeout(() => {
    if (notification.parentNode) {
      notification.parentNode.removeChild(notification)
    }
  }, 300)
}


const getNotificationIcon = (type) => {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[type] || icons.info
}


const getNotificationColor = (type) => {
  const colors = {
    success: '#10b981',
    error: '#ef4444',
    warning: '#f59e0b',
    info: '#3b82f6'
  }
  return colors[type] || colors.info
}

// ==================== FILE HANDLING UTILITIES ====================

export const handleFileUpload = async (files, options = {}) => {
  const {
    maxSize = 10 * 1024 * 1024, // 10MB
    allowedTypes = ['image/jpeg', 'image/png', 'image/webp'],
    maxFiles = 5
  } = options
  
  const validFiles = []
  const errors = []
  
  Array.from(files).forEach((file, index) => {
    if (index >= maxFiles) {
      errors.push(`Chỉ được upload tối đa ${maxFiles} file`)
      return
    }
    
    if (file.size > maxSize) {
      errors.push(`File "${file.name}" quá lớn (tối đa ${formatFileSize(maxSize)})`)
      return
    }
    
    if (!allowedTypes.includes(file.type)) {
      errors.push(`File "${file.name}" không đúng định dạng`)
      return
    }
    
    validFiles.push(file)
  })
  
  return {
    validFiles,
    errors,
    isValid: errors.length === 0
  }
}

export const generateImagePreview = (file) => {
  return new Promise((resolve, reject) => {
    if (!file.type.startsWith('image/')) {
      reject(new Error('File không phải là hình ảnh'))
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = () => reject(new Error('Không thể đọc file'))
    reader.readAsDataURL(file)
  })
}

// ==================== EXPORT UTILITIES ====================


export const downloadFile = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}


export const exportToCSV = (data, filename, headers = null) => {
  let csvContent = ''
  
  if (headers) {
    csvContent += headers.join(',') + '\n'
  }
  
  data.forEach(row => {
    const values = headers 
      ? headers.map(header => row[header] || '')
      : Object.values(row)
    
    csvContent += values.map(value => 
      typeof value === 'string' && value.includes(',') 
        ? `"${value}"` 
        : value
    ).join(',') + '\n'
  })
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  downloadFile(blob, `${filename}.csv`)
}

// ==================== DEBOUNCE UTILITY ====================


export const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// ==================== LOCAL STORAGE UTILITIES ====================


export const getStorageItem = (key, defaultValue = null) => {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (error) {
    console.warn(`Error reading from localStorage: ${error.message}`)
    return defaultValue
  }
}


export const setStorageItem = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.warn(`Error writing to localStorage: ${error.message}`)
  }
}


export const removeStorageItem = (key) => {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    console.warn(`Error removing from localStorage: ${error.message}`)
  }
}

const notificationStyles = `
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-close {
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
  margin-left: auto;
}

.notification-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}
`

if (typeof document !== 'undefined') {
  const styleSheet = document.createElement('style')
  styleSheet.textContent = notificationStyles
  document.head.appendChild(styleSheet)
}

export default {
  formatCurrency,
  formatPercentage,
  formatDate,
  formatDateTime,
  formatRelativeTime,
  formatFileSize,
  getOrderStatusConfig,
  getProductStatusConfig,
  getStockStatus,
  validateProductData,
  validateOrderData,
  calculateOrderTotal,
  calculatePercentageChange,
  calculateConversionRate,
  filterProducts,
  sortProducts,
  showNotification,
  handleFileUpload,
  generateImagePreview,
  downloadFile,
  exportToCSV,
  debounce,
  getStorageItem,
  setStorageItem,
  removeStorageItem
}