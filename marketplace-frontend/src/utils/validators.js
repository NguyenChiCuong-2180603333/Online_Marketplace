
import { VALIDATION_RULES, ERROR_MESSAGES } from './constants.js'

export const validateEmail = (email) => {
  if (!email) {
    return { isValid: false, message: 'Email là bắt buộc' }
  }
  
  if (!VALIDATION_RULES.EMAIL.test(email)) {
    return { isValid: false, message: ERROR_MESSAGES.EMAIL_INVALID }
  }
  
  return { isValid: true, message: '' }
}

export const validatePassword = (password) => {
  if (!password) {
    return { isValid: false, message: 'Mật khẩu là bắt buộc', strength: 'none' }
  }
  
  if (password.length < VALIDATION_RULES.PASSWORD_MIN_LENGTH) {
    return { 
      isValid: false, 
      message: ERROR_MESSAGES.PASSWORD_WEAK,
      strength: 'weak'
    }
  }
  
  let strength = 'weak'
  let score = 0
  
  if (password.length >= 8) score++
  if (password.length >= 12) score++
  
  if (/[a-z]/.test(password)) score++
  if (/[A-Z]/.test(password)) score++
  if (/[0-9]/.test(password)) score++
  if (/[^A-Za-z0-9]/.test(password)) score++
  
  if (score >= 3) strength = 'medium'
  if (score >= 5) strength = 'strong'
  
  return { 
    isValid: true, 
    message: '', 
    strength,
    score 
  }
}

export const validatePhone = (phone) => {
  if (!phone) {
    return { isValid: false, message: 'Số điện thoại là bắt buộc' }
  }
  
  const cleanPhone = phone.replace(/[\s-]/g, '')
  
  if (!VALIDATION_RULES.PHONE_VN.test(cleanPhone)) {
    return { isValid: false, message: ERROR_MESSAGES.PHONE_INVALID }
  }
  
  return { isValid: true, message: '' }
}

export const validateRequired = (value, fieldName) => {
  if (!value || (typeof value === 'string' && value.trim() === '')) {
    return { isValid: false, message: `${fieldName} là bắt buộc` }
  }
  
  return { isValid: true, message: '' }
}

export const validateLength = (value, minLength, maxLength, fieldName) => {
  if (!value) value = ''
  
  if (value.length < minLength) {
    return { 
      isValid: false, 
      message: `${fieldName} phải có ít nhất ${minLength} ký tự` 
    }
  }
  
  if (value.length > maxLength) {
    return { 
      isValid: false, 
      message: `${fieldName} không được vượt quá ${maxLength} ký tự` 
    }
  }
  
  return { isValid: true, message: '' }
}

export const validateRange = (value, min, max, fieldName) => {
  if (value === null || value === undefined || value === '') {
    return { isValid: false, message: `${fieldName} là bắt buộc` }
  }
  
  const num = Number(value)
  
  if (isNaN(num)) {
    return { isValid: false, message: `${fieldName} phải là số` }
  }
  
  if (num < min) {
    return { 
      isValid: false, 
      message: `${fieldName} phải lớn hơn hoặc bằng ${min}` 
    }
  }
  
  if (num > max) {
    return { 
      isValid: false, 
      message: `${fieldName} không được vượt quá ${max}` 
    }
  }
  
  return { isValid: true, message: '' }
}


export const validatePrice = (price) => {
  if (!price && price !== 0) {
    return { isValid: false, message: 'Giá là bắt buộc' }
  }
  
  const num = Number(price)
  
  if (isNaN(num)) {
    return { isValid: false, message: 'Giá phải là số' }
  }
  
  if (num < 0) {
    return { isValid: false, message: 'Giá không được âm' }
  }
  
  if (num > 999999999) {
    return { isValid: false, message: 'Giá quá lớn' }
  }
  
  return { isValid: true, message: '' }
}

export const validateQuantity = (quantity) => {
  if (!quantity && quantity !== 0) {
    return { isValid: false, message: 'Số lượng là bắt buộc' }
  }
  
  const num = Number(quantity)
  
  if (isNaN(num)) {
    return { isValid: false, message: 'Số lượng phải là số' }
  }
  
  if (!Number.isInteger(num)) {
    return { isValid: false, message: 'Số lượng phải là số nguyên' }
  }
  
  if (num < 1) {
    return { isValid: false, message: 'Số lượng phải lớn hơn 0' }
  }
  
  if (num > 10000) {
    return { isValid: false, message: 'Số lượng quá lớn' }
  }
  
  return { isValid: true, message: '' }
}


export const validateFile = (file, allowedTypes, maxSize) => {
  if (!file) {
    return { isValid: false, message: 'Vui lòng chọn file' }
  }
  
  if (!allowedTypes.includes(file.type)) {
    return { isValid: false, message: ERROR_MESSAGES.INVALID_FILE_TYPE }
  }
  
  if (file.size > maxSize) {
    return { isValid: false, message: ERROR_MESSAGES.FILE_TOO_LARGE }
  }
  
  return { isValid: true, message: '' }
}


export const validateImage = (file) => {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  const maxSize = 5 * 1024 * 1024 // 5MB
  
  return validateFile(file, allowedTypes, maxSize)
}


export const validateUrl = (url) => {
  if (!url) {
    return { isValid: false, message: 'URL là bắt buộc' }
  }
  
  try {
    new URL(url)
    return { isValid: true, message: '' }
  } catch {
    return { isValid: false, message: 'URL không hợp lệ' }
  }
}


export const validateDate = (date, minDate = null, maxDate = null) => {
  if (!date) {
    return { isValid: false, message: 'Ngày là bắt buộc' }
  }
  
  const dateObj = new Date(date)
  
  if (isNaN(dateObj.getTime())) {
    return { isValid: false, message: 'Ngày không hợp lệ' }
  }
  
  if (minDate) {
    const minDateObj = new Date(minDate)
    if (dateObj < minDateObj) {
      return { 
        isValid: false, 
        message: `Ngày phải sau ${minDateObj.toLocaleDateString('vi-VN')}` 
      }
    }
  }
  
  if (maxDate) {
    const maxDateObj = new Date(maxDate)
    if (dateObj > maxDateObj) {
      return { 
        isValid: false, 
        message: `Ngày phải trước ${maxDateObj.toLocaleDateString('vi-VN')}` 
      }
    }
  }
  
  return { isValid: true, message: '' }
}


export const validateBirthDate = (birthDate) => {
  if (!birthDate) {
    return { isValid: false, message: 'Ngày sinh là bắt buộc' }
  }
  
  const today = new Date()
  const birthDateObj = new Date(birthDate)
  
  if (isNaN(birthDateObj.getTime())) {
    return { isValid: false, message: 'Ngày sinh không hợp lệ' }
  }
  
  if (birthDateObj >= today) {
    return { isValid: false, message: 'Ngày sinh phải trong quá khứ' }
  }
  
  const age = today.getFullYear() - birthDateObj.getFullYear()
  
  if (age > 120) {
    return { isValid: false, message: 'Ngày sinh không hợp lệ' }
  }
  
  if (age < 13) {
    return { isValid: false, message: 'Bạn phải ít nhất 13 tuổi' }
  }
  
  return { isValid: true, message: '' }
}


export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) {
    return { isValid: false, message: 'Xác nhận mật khẩu là bắt buộc' }
  }
  
  if (password !== confirmPassword) {
    return { isValid: false, message: 'Mật khẩu xác nhận không khớp' }
  }
  
  return { isValid: true, message: '' }
}


export const validateUsername = (username) => {
  if (!username) {
    return { isValid: false, message: 'Tên đăng nhập là bắt buộc' }
  }
  
  const lengthCheck = validateLength(
    username, 
    VALIDATION_RULES.USERNAME_MIN_LENGTH, 
    VALIDATION_RULES.USERNAME_MAX_LENGTH, 
    'Tên đăng nhập'
  )
  
  if (!lengthCheck.isValid) {
    return lengthCheck
  }
  

  if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
    return { 
      isValid: false, 
      message: 'Tên đăng nhập chỉ được chứa chữ cái, số, dấu gạch dưới và dấu gạch ngang' 
    }
  }
  
  return { isValid: true, message: '' }
}


export const validateForm = (formData, rules) => {
  const errors = {}
  let isValid = true
  
  for (const [field, validators] of Object.entries(rules)) {
    const value = formData[field]
    
    for (const validator of validators) {
      const result = validator(value)
      
      if (!result.isValid) {
        errors[field] = result.message
        isValid = false
        break 
      }
    }
  }
  
  return { isValid, errors }
}


export const FORM_RULES = {
  register: {
    name: [
      (value) => validateRequired(value, 'Họ tên'),
      (value) => validateLength(value, 2, VALIDATION_RULES.NAME_MAX_LENGTH, 'Họ tên')
    ],
    email: [validateEmail],
    password: [validatePassword],
    confirmPassword: [
      (value, formData) => validateConfirmPassword(formData.password, value)
    ],
    phone: [validatePhone]
  },
  

  login: {
    email: [validateEmail],
    password: [(value) => validateRequired(value, 'Mật khẩu')]
  },
  

  product: {
    name: [
      (value) => validateRequired(value, 'Tên sản phẩm'),
      (value) => validateLength(value, 2, 255, 'Tên sản phẩm')
    ],
    price: [validatePrice],
    description: [
      (value) => validateRequired(value, 'Mô tả'),
      (value) => validateLength(value, 10, VALIDATION_RULES.DESCRIPTION_MAX_LENGTH, 'Mô tả')
    ],
    category_id: [(value) => validateRequired(value, 'Danh mục')],
    stock_quantity: [validateQuantity]
  },
  

  order: {
    shipping_address: [
      (value) => validateRequired(value, 'Địa chỉ giao hàng'),
      (value) => validateLength(value, 10, VALIDATION_RULES.ADDRESS_MAX_LENGTH, 'Địa chỉ')
    ],
    phone: [validatePhone],
    payment_method: [(value) => validateRequired(value, 'Phương thức thanh toán')]
  },
  

  profile: {
    name: [
      (value) => validateRequired(value, 'Họ tên'),
      (value) => validateLength(value, 2, VALIDATION_RULES.NAME_MAX_LENGTH, 'Họ tên')
    ],
    phone: [validatePhone],
    address: [
      (value) => validateLength(value, 0, VALIDATION_RULES.ADDRESS_MAX_LENGTH, 'Địa chỉ')
    ],
    birth_date: [validateBirthDate]
  }
}

export const validateField = (field, value, validators) => {
  for (const validator of validators) {
    const result = validator(value)
    if (!result.isValid) {
      return result
    }
  }
  return { isValid: true, message: '' }
}


export const sanitizeInput = (input) => {
  if (typeof input !== 'string') return input
  
  return input
    .replace(/[<>]/g, '') 
    .trim()
}


export const validateAndSanitizeForm = (formData, rules) => {
  const sanitizedData = {}
  for (const [key, value] of Object.entries(formData)) {
    sanitizedData[key] = typeof value === 'string' ? sanitizeInput(value) : value
  }
  
  const validation = validateForm(sanitizedData, rules)
  
  return {
    ...validation,
    data: sanitizedData
  }
}