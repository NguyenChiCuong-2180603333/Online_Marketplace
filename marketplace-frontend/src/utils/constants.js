
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api',
  TIMEOUT: 30000, // 30 seconds
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000 // 1 second
}


export const USER_ROLES = {
  ADMIN: 'admin',
  MANAGER: 'manager', 
  STAFF: 'staff',
  CUSTOMER: 'customer',
  USER: 'user'
}


export const ORDER_STATUS = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  PROCESSING: 'processing',
  SHIPPING: 'shipping',
  DELIVERED: 'delivered',
  CANCELLED: 'cancelled',
  REFUNDED: 'refunded'
}


export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: '#ffcd3c',
  [ORDER_STATUS.CONFIRMED]: '#00d4ff',
  [ORDER_STATUS.PROCESSING]: '#533483',
  [ORDER_STATUS.SHIPPING]: '#ff6b35',
  [ORDER_STATUS.DELIVERED]: '#00ff88',
  [ORDER_STATUS.CANCELLED]: '#e94560',
  [ORDER_STATUS.REFUNDED]: '#b8c6db'
}


export const PAYMENT_METHODS = {
  CASH: 'cash',
  CREDIT_CARD: 'credit_card',
  DEBIT_CARD: 'debit_card',
  BANK_TRANSFER: 'bank_transfer',
  E_WALLET: 'e_wallet',
  MOMO: 'momo',
  ZALOPAY: 'zalopay',
  VNPAY: 'vnpay'
}

export const PRODUCT_CATEGORIES = {
  ELECTRONICS: 'electronics',
  CLOTHING: 'clothing',
  HOME: 'home',
  SPORTS: 'sports',
  BOOKS: 'books',
  BEAUTY: 'beauty',
  AUTOMOTIVE: 'automotive',
  FOOD: 'food',
  TOYS: 'toys',
  OTHER: 'other'
}


export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 12,
  MAX_PAGE_SIZE: 100,
  PAGE_SIZE_OPTIONS: [6, 12, 24, 48],
  ADMIN_PAGE_SIZE: 20
}


export const FILE_UPLOAD = {
  MAX_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'],
  ALLOWED_FILE_TYPES: ['application/pdf', 'application/doc', 'application/docx'],
  IMAGE_QUALITY: 0.8,
  MAX_IMAGE_WIDTH: 1920,
  MAX_IMAGE_HEIGHT: 1080,
  THUMBNAIL_SIZE: 300
}


export const VALIDATION_RULES = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE_VN: /^(0|\+84)[3-9]\d{8}$/,
  PASSWORD_MIN_LENGTH: 8,
  USERNAME_MIN_LENGTH: 3,
  USERNAME_MAX_LENGTH: 50,
  NAME_MAX_LENGTH: 100,
  DESCRIPTION_MAX_LENGTH: 1000,
  ADDRESS_MAX_LENGTH: 255
}


export const SEARCH_CONFIG = {
  MIN_SEARCH_LENGTH: 2,
  DEBOUNCE_DELAY: 300, // milliseconds
  MAX_SUGGESTIONS: 10,
  RECENT_SEARCHES_LIMIT: 5
}


export const SORT_OPTIONS = {
  PRODUCTS: [
    { value: 'created_at_desc', label: 'Mới nhất' },
    { value: 'created_at_asc', label: 'Cũ nhất' },
    { value: 'price_asc', label: 'Giá thấp đến cao' },
    { value: 'price_desc', label: 'Giá cao đến thấp' },
    { value: 'name_asc', label: 'Tên A-Z' },
    { value: 'name_desc', label: 'Tên Z-A' },
    { value: 'popularity', label: 'Phổ biến nhất' },
    { value: 'rating', label: 'Đánh giá cao nhất' }
  ],
  ORDERS: [
    { value: 'created_at_desc', label: 'Mới nhất' },
    { value: 'created_at_asc', label: 'Cũ nhất' },
    { value: 'total_amount_desc', label: 'Giá trị cao nhất' },
    { value: 'total_amount_asc', label: 'Giá trị thấp nhất' },
    { value: 'status', label: 'Trạng thái' }
  ],
  USERS: [
    { value: 'created_at_desc', label: 'Mới nhất' },
    { value: 'created_at_asc', label: 'Cũ nhất' },
    { value: 'name_asc', label: 'Tên A-Z' },
    { value: 'name_desc', label: 'Tên Z-A' },
    { value: 'email_asc', label: 'Email A-Z' }
  ]
}


export const STORAGE_KEYS = {
  TOKEN: 'marketplace_token',
  USER: 'marketplace_user',
  CART: 'marketplace_cart',
  THEME: 'marketplace_theme',
  LANGUAGE: 'marketplace_language',
  RECENT_SEARCHES: 'marketplace_recent_searches',
  USER_PREFERENCES: 'marketplace_preferences'
}


export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Lỗi kết nối mạng. Vui lòng thử lại.',
  UNAUTHORIZED: 'Bạn không có quyền truy cập.',
  FORBIDDEN: 'Truy cập bị từ chối.',
  NOT_FOUND: 'Không tìm thấy dữ liệu.',
  SERVER_ERROR: 'Lỗi máy chủ. Vui lòng thử lại sau.',
  VALIDATION_ERROR: 'Dữ liệu không hợp lệ.',
  FILE_TOO_LARGE: 'File quá lớn. Kích thước tối đa 5MB.',
  INVALID_FILE_TYPE: 'Định dạng file không được hỗ trợ.',
  LOGIN_REQUIRED: 'Vui lòng đăng nhập để tiếp tục.',
  SESSION_EXPIRED: 'Phiên đăng nhập đã hết hạn.',
  CART_EMPTY: 'Giỏ hàng trống.',
  STOCK_INSUFFICIENT: 'Số lượng trong kho không đủ.',
  PASSWORD_WEAK: 'Mật khẩu phải có ít nhất 8 ký tự.',
  EMAIL_INVALID: 'Email không hợp lệ.',
  PHONE_INVALID: 'Số điện thoại không hợp lệ.'
}


export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Đăng nhập thành công!',
  LOGOUT_SUCCESS: 'Đăng xuất thành công!',
  REGISTER_SUCCESS: 'Đăng ký thành công!',
  UPDATE_SUCCESS: 'Cập nhật thành công!',
  DELETE_SUCCESS: 'Xóa thành công!',
  CREATE_SUCCESS: 'Tạo mới thành công!',
  UPLOAD_SUCCESS: 'Tải lên thành công!',
  ORDER_SUCCESS: 'Đặt hàng thành công!',
  PAYMENT_SUCCESS: 'Thanh toán thành công!',
  EMAIL_SENT: 'Email đã được gửi thành công!',
  PASSWORD_CHANGED: 'Đổi mật khẩu thành công!',
  PROFILE_UPDATED: 'Cập nhật hồ sơ thành công!'
}

export const NOTIFICATION_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}

export const SPACE_COLORS = {
  BLACK: '#0a0a0f',
  DARK: '#1a1a2e',
  NAVY: '#16213e',
  BLUE: '#0f3460',
  PURPLE: '#533483',
  PINK: '#e94560',
  CYAN: '#00d4ff',
  GREEN: '#00ff88',
  ORANGE: '#ff6b35',
  YELLOW: '#ffcd3c',
  WHITE: '#ffffff',
  GRAY: '#b8c6db'
}

export const ANIMATION = {
  FAST: 200,
  NORMAL: 300,
  SLOW: 500,
  VERY_SLOW: 1000
}

export const BREAKPOINTS = {
  MOBILE: 768,
  TABLET: 1024,
  DESKTOP: 1200,
  WIDE: 1400
}

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503
}

export const DATE_FORMATS = {
  SHORT: 'DD/MM/YYYY',
  LONG: 'DD/MM/YYYY HH:mm',
  TIME: 'HH:mm',
  FULL: 'dddd, DD/MM/YYYY HH:mm:ss'
}

export const CURRENCY = {
  CODE: 'VND',
  SYMBOL: '₫',
  LOCALE: 'vi-VN'
}

export const FEATURES = {
  ENABLE_REVIEWS: true,
  ENABLE_WISHLIST: true,
  ENABLE_NOTIFICATIONS: true,
  ENABLE_CHAT: false,
  ENABLE_DARK_MODE: false, 
  ENABLE_PWA: false,
  ENABLE_ANALYTICS: false
}

export const ADMIN_CONFIG = {
  DASHBOARD_REFRESH_INTERVAL: 30000, // 30s
  MAX_RECENT_ACTIVITIES: 10,
  CHART_COLORS: [
    SPACE_COLORS.CYAN,
    SPACE_COLORS.GREEN,
    SPACE_COLORS.ORANGE,
    SPACE_COLORS.PINK,
    SPACE_COLORS.YELLOW,
    SPACE_COLORS.PURPLE
  ]
}

export const DEFAULTS = {
  PRODUCT_IMAGE: '/images/no-product.png',
  USER_AVATAR: '/images/default-avatar.png',
  PAGE_TITLE: 'Space Marketplace',
  META_DESCRIPTION: 'Discover amazing products in our space-themed marketplace',
  ITEMS_PER_PAGE: PAGINATION.DEFAULT_PAGE_SIZE,
  LANGUAGE: 'vi',
  TIMEZONE: 'Asia/Ho_Chi_Minh'
}