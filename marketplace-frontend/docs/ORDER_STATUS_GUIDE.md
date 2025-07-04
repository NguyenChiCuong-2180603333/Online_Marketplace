# 📋 Order Status System Guide

## 🎯 Overview

Hệ thống trạng thái đơn hàng được thiết kế để phù hợp với góc nhìn của từng role khác nhau trong hệ thống marketplace.

## 🔄 Status Workflow

```
PENDING → PROCESSING → SHIPPED → DELIVERED
    ↓
CANCELLED (có thể hủy ở bất kỳ bước nào trước DELIVERED)
```

## 👥 Role-Based Status Labels

### 1. **ADMIN** (Quản trị viên)

- **Góc nhìn**: Hệ thống tổng thể, quản lý toàn bộ marketplace
- **Mục đích**: Giám sát và quản lý tất cả đơn hàng

```javascript
{
  PENDING: 'Chờ xử lý',
  PROCESSING: 'Đang xử lý',
  SHIPPED: 'Đang giao',        // Từ góc nhìn khách hàng
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy'
}
```

### 2. **SELLER** (Người bán)

- **Góc nhìn**: Quản lý đơn hàng của mình
- **Mục đích**: Xử lý và theo dõi đơn hàng

```javascript
{
  PENDING: 'Chờ xử lý',
  PROCESSING: 'Đang xử lý',
  SHIPPED: 'Đã gửi hàng',      // Từ góc nhìn người bán
  DELIVERED: 'Đã giao hàng',   // Mô tả chi tiết hơn
  CANCELLED: 'Đã hủy'
}
```

### 3. **CUSTOMER** (Khách hàng)

- **Góc nhìn**: Theo dõi đơn hàng của mình
- **Mục đích**: Biết được tình trạng đơn hàng

```javascript
{
  PENDING: 'Chờ xử lý',
  PROCESSING: 'Đang xử lý',
  SHIPPED: 'Đang giao',        // Từ góc nhìn khách hàng
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy'
}
```

## 🎨 Status Colors & Icons

```javascript
const STATUS_COLORS = {
  PENDING: '#f59e0b', // Orange
  PROCESSING: '#3b82f6', // Blue
  SHIPPED: '#8b5cf6', // Purple
  DELIVERED: '#10b981', // Green
  CANCELLED: '#ef4444', // Red
}

const STATUS_ICONS = {
  PENDING: '⏳',
  PROCESSING: '🔄',
  SHIPPED: '🚚',
  DELIVERED: '✅',
  CANCELLED: '❌',
}
```

## 🔧 Implementation

### Sử dụng trong Components

```javascript
import { getStatusLabel, getStatusColor, getStatusIcon } from '@/utils/constants'

// Trong component
const statusLabel = getStatusLabel(order.status, 'SELLER') // Role-specific
const statusColor = getStatusColor(order.status)
const statusIcon = getStatusIcon(order.status)
```

### Sử dụng trong Store

```javascript
// seller.js
getStatusLabel(status) {
  return getStatusLabel(status, 'SELLER')
}

// admin.js
getStatusLabel(status) {
  return getStatusLabel(status, 'ADMIN')
}
```

## 🚨 Key Differences Explained

### 1. **SHIPPED Status**

- **ADMIN/CUSTOMER**: "Đang giao"
  - Từ góc nhìn khách hàng: đơn hàng đang được vận chuyển
- **SELLER**: "Đã gửi hàng"
  - Từ góc nhìn người bán: đã hoàn thành việc gửi hàng

### 2. **DELIVERED Status**

- **ADMIN/CUSTOMER**: "Đã giao"
  - Đơn giản, dễ hiểu
- **SELLER**: "Đã giao hàng"
  - Mô tả chi tiết hơn, phù hợp với context quản lý

## ✅ Benefits

1. **User Experience**: Mỗi role thấy thông tin phù hợp với nhu cầu
2. **Clarity**: Tránh nhầm lẫn về ý nghĩa của trạng thái
3. **Consistency**: Tất cả status labels được centralize trong constants
4. **Maintainability**: Dễ dàng thay đổi và cập nhật

## 🔄 Migration Guide

### Trước khi sửa:

```javascript
// ❌ Inconsistent across files
const statusLabels = {
  pending: 'Chờ xử lý', // lowercase
  SHIPPED: 'Đang giao', // different meaning
  // ...
}
```

### Sau khi sửa:

```javascript
// ✅ Centralized and consistent
import { getStatusLabel } from '@/utils/constants'

const statusLabel = getStatusLabel(status, 'SELLER')
```

## 🧪 Testing

Chạy test để kiểm tra consistency:

```javascript
import { testOrderStatusConsistency } from '@/utils/orderStatusTest'

testOrderStatusConsistency()
```

## 📝 Notes

- Tất cả status values phải là UPPERCASE
- Sử dụng constants thay vì hardcode strings
- Luôn truyền role parameter khi gọi getStatusLabel()
- Cập nhật documentation khi thêm status mới
