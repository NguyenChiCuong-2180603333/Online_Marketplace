
export const formatCurrency = (amount) => {
  if (!amount && amount !== 0) return '0 ₫'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0
  }).format(num)
}


export const formatPrice = (price) => {
  if (!price && price !== 0) return '0 ₫'
  const num = typeof price === 'string' ? parseFloat(price) : price
  
  if (num >= 1000000000) {
    return `${(num / 1000000000).toFixed(1)}B ₫`
  } else if (num >= 1000000) {
    return `${(num / 1000000).toFixed(1)}M ₫`
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}K ₫`
  }
  return formatCurrency(num)
}

export const formatDate = (date, format = 'short') => {
  if (!date) return ''
  const dateObj = typeof date === 'string' ? new Date(date) : date
  
  const options = {
    short: { day: '2-digit', month: '2-digit', year: 'numeric' },
    long: { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    },
    datetime: { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }
  }
  
  return new Intl.DateTimeFormat('vi-VN', options[format]).format(dateObj)
}


export const formatRelativeTime = (date) => {
  if (!date) return ''
  const dateObj = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diffMs = now - dateObj
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffHours / 24)
  const diffWeeks = Math.floor(diffDays / 7)
  const diffMonths = Math.floor(diffDays / 30)
  
  if (diffMs < 60000) return 'Vừa xong'
  if (diffMs < 3600000) return `${Math.floor(diffMs / 60000)} phút trước`
  if (diffHours < 24) return `${diffHours} giờ trước`
  if (diffDays < 7) return `${diffDays} ngày trước`
  if (diffWeeks < 4) return `${diffWeeks} tuần trước`
  if (diffMonths < 12) return `${diffMonths} tháng trước`
  return formatDate(dateObj, 'short')
}


export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}


export const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return new Intl.NumberFormat('vi-VN').format(num)
}


export const formatPercentage = (value, decimals = 1) => {
  if (!value && value !== 0) return '0%'
  return `${value.toFixed(decimals)}%`
}


export const truncateText = (text, maxLength = 100) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}


export const formatPhoneNumber = (phone) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  
  // Vietnamese phone number format
  if (cleaned.length === 10) {
    return cleaned.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3')
  } else if (cleaned.length === 11 && cleaned.startsWith('84')) {
    return `+84 ${cleaned.substring(2).replace(/(\d{3})(\d{3})(\d{3})/, '$1 $2 $3')}`
  }
  return phone
}


export const formatAddress = (address, maxLength = 50) => {
  if (!address) return ''
  return truncateText(address, maxLength)
}

export const formatOrderStatus = (status) => {
  const statusMap = {
    'pending': 'Đang chờ',
    'confirmed': 'Đã xác nhận',
    'processing': 'Đang xử lý',
    'shipping': 'Đang giao',
    'delivered': 'Đã giao',
    'cancelled': 'Đã hủy',
    'refunded': 'Đã hoàn tiền'
  }
  return statusMap[status] || status
}


export const formatPaymentMethod = (method) => {
  const methodMap = {
    'cash': 'Tiền mặt',
    'credit_card': 'Thẻ tín dụng',
    'debit_card': 'Thẻ ghi nợ',
    'bank_transfer': 'Chuyển khoản',
    'e_wallet': 'Ví điện tử',
    'momo': 'MoMo',
    'zalopay': 'ZaloPay',
    'vnpay': 'VNPay'
  }
  return methodMap[method] || method
}


export const formatUserRole = (role) => {
  const roleMap = {
    'admin': 'Quản trị viên',
    'manager': 'Quản lý',
    'staff': 'Nhân viên',
    'customer': 'Khách hàng',
    'user': 'Người dùng'
  }
  return roleMap[role] || role
}


export const formatRating = (rating, maxRating = 5) => {
  if (!rating && rating !== 0) return '☆'.repeat(maxRating)
  const fullStars = Math.floor(rating)
  const hasHalfStar = rating % 1 >= 0.5
  const emptyStars = maxRating - fullStars - (hasHalfStar ? 1 : 0)
  
  return '★'.repeat(fullStars) + 
         (hasHalfStar ? '☆' : '') + 
         '☆'.repeat(emptyStars)
}


export const formatSlug = (text) => {
  if (!text) return ''
  return text
    .toLowerCase()
    .trim()
    .replace(/[àáạảãâầấậẩẫăằắặẳẵ]/g, 'a')
    .replace(/[èéẹẻẽêềếệểễ]/g, 'e')
    .replace(/[ìíịỉĩ]/g, 'i')
    .replace(/[òóọỏõôồốộổỗơờớợởỡ]/g, 'o')
    .replace(/[ùúụủũưừứựửữ]/g, 'u')
    .replace(/[ỳýỵỷỹ]/g, 'y')
    .replace(/đ/g, 'd')
    .replace(/[^a-z0-9 -]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}