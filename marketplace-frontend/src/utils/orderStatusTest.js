// Test file để kiểm tra consistency của order status labels
import { getStatusLabel, ORDER_STATUS } from './constants.js'

// Test function để kiểm tra tất cả status labels
export const testOrderStatusConsistency = () => {
  console.log('🧪 Testing Order Status Consistency...')

  const testCases = [
    { status: ORDER_STATUS.PENDING, expected: 'Chờ xử lý' },
    { status: ORDER_STATUS.PROCESSING, expected: 'Đang xử lý' },
    { status: ORDER_STATUS.SHIPPED, expected: 'Đang giao' }, // Admin/Customer
    { status: ORDER_STATUS.DELIVERED, expected: 'Đã giao' },
    { status: ORDER_STATUS.CANCELLED, expected: 'Đã hủy' },
  ]

  console.log('\n📊 Status Labels by Role:')
  console.log('='.repeat(50))

  const roles = ['ADMIN', 'SELLER', 'CUSTOMER']

  roles.forEach((role) => {
    console.log(`\n👤 ${role}:`)
    testCases.forEach((testCase) => {
      const actual = getStatusLabel(testCase.status, role)
      const isCorrect =
        actual === testCase.expected ||
        (role === 'SELLER' &&
          testCase.status === ORDER_STATUS.SHIPPED &&
          actual === 'Đã gửi hàng') ||
        (role === 'SELLER' &&
          testCase.status === ORDER_STATUS.DELIVERED &&
          actual === 'Đã giao hàng')

      console.log(`  ${testCase.status}: "${actual}" ${isCorrect ? '✅' : '❌'}`)
    })
  })

  console.log('\n🎯 Key Differences:')
  console.log('• ADMIN/CUSTOMER: SHIPPED = "Đang giao" (customer perspective)')
  console.log('• SELLER: SHIPPED = "Đã gửi hàng" (seller perspective)')
  console.log('• SELLER: DELIVERED = "Đã giao hàng" (more descriptive)')

  console.log('\n✅ Test completed!')
}

// Run test if this file is executed directly
if (import.meta.url === `file://${process.cwd()}/src/utils/orderStatusTest.js`) {
  testOrderStatusConsistency()
}
