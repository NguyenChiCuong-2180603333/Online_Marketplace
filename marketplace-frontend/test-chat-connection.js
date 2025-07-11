

console.log('🧪 Testing Chat Connection...')

async function testWebSocketConnection() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('❌ No token found. Please login first.')
      return
    }

    console.log('🔗 Testing WebSocket connection...')
    console.log('🔑 Token found:', token.substring(0, 20) + '...')

    // Test connection
    const socket = new WebSocket('ws://localhost:8080/ws')

    socket.onopen = () => {
      console.log('✅ WebSocket connected successfully!')

      const client = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: function (str) {
          console.log('STOMP Debug:', str)
        },
      })

      client.onConnect = () => {
        console.log('✅ STOMP connection successful!')

        client.subscribe('/user/queue/messages', (message) => {
          console.log('📨 Received message:', JSON.parse(message.body))
        })

        client.publish({
          destination: '/app/chat.send',
          body: JSON.stringify({
            conversationId: 'test-conversation',
            content: 'Test message',
            messageType: 'TEXT',
          }),
        })

        console.log('✅ Message sent successfully!')

        // Test typing indicator
        setTimeout(() => {
          client.publish({
            destination: '/app/chat.typing',
            body: JSON.stringify({
              conversationId: 'test-conversation',
              isTyping: true,
            }),
          })
          console.log('✅ Typing indicator sent!')
        }, 1000)

        setTimeout(() => {
          client.deactivate()
          console.log('🔌 Disconnected from WebSocket')
        }, 3000)
      }

      client.onStompError = (error) => {
        console.error('❌ STOMP error:', error)
      }

      client.activate()
    }

    socket.onerror = (error) => {
      console.error('❌ WebSocket error:', error)
    }

    socket.onclose = (event) => {
      console.log('🔌 WebSocket connection closed:', event.code, event.reason)
    }
  } catch (error) {
    console.error('❌ Test failed:', error)
  }
}

// Test REST API
async function testRestAPI() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('❌ No token found. Please login first.')
      return
    }

    console.log('🌐 Testing REST API...')

    // Test getting conversations
    const response = await fetch('http://localhost:8080/api/chat/conversations', {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    })

    if (response.ok) {
      const data = await response.json()
      console.log('✅ REST API test passed!', data)
    } else {
      console.error('❌ REST API test failed:', response.status)
    }
  } catch (error) {
    console.error('❌ REST API test failed:', error)
  }
}

// Run tests
testWebSocketConnection()
testRestAPI()

console.log('🧪 Tests completed. Check console for results.')
