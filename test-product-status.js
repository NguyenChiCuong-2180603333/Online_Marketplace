// Test script để kiểm tra product status
const axios = require('axios')

const API_BASE = 'http://localhost:8080/api'

async function testProductStatus() {
  try {
    console.log('🧪 Testing Product Status API...')

    // 1. Get all products
    console.log('\n1. Getting all products...')
    const productsResponse = await axios.get(`${API_BASE}/admin/products`)
    const products = productsResponse.data
    console.log(`Found ${products.length} products`)

    if (products.length > 0) {
      const firstProduct = products[0]
      console.log(`First product: ${firstProduct.name} - Active: ${firstProduct.isActive}`)

      // 2. Toggle product status
      console.log('\n2. Toggling product status...')
      const toggleResponse = await axios.put(
        `${API_BASE}/admin/products/${firstProduct.id}/toggle-status`
      )
      const updatedProduct = toggleResponse.data
      console.log(`After toggle: ${updatedProduct.name} - Active: ${updatedProduct.isActive}`)

      // 3. Get products again to verify
      console.log('\n3. Getting products again...')
      const productsResponse2 = await axios.get(`${API_BASE}/admin/products`)
      const products2 = productsResponse2.data
      const sameProduct = products2.find((p) => p.id === firstProduct.id)
      console.log(`After reload: ${sameProduct.name} - Active: ${sameProduct.isActive}`)

      // 4. Toggle back
      console.log('\n4. Toggling back...')
      const toggleResponse2 = await axios.put(
        `${API_BASE}/admin/products/${firstProduct.id}/toggle-status`
      )
      const updatedProduct2 = toggleResponse2.data
      console.log(
        `After second toggle: ${updatedProduct2.name} - Active: ${updatedProduct2.isActive}`
      )
    } else {
      console.log('No products found to test')
    }
  } catch (error) {
    console.error('❌ Test failed:', error.response?.data || error.message)
  }
}

// Run test
testProductStatus()
