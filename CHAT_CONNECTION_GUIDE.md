# Hướng dẫn Test Kết nối Chat

## 🎯 Tổng quan

Backend và Frontend đã được kết nối thành công! Dưới đây là hướng dẫn test để đảm bảo mọi thứ hoạt động đúng.

## 🚀 Các bước test

### 1. Khởi động Backend

```bash
cd marketplace-backend
./mvnw spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### 2. Khởi động Frontend

```bash
cd marketplace-frontend
npm install
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:5173`

### 3. Test WebSocket Connection

#### Cách 1: Sử dụng Browser Console

1. Mở browser và truy cập `http://localhost:5173`
2. Đăng nhập để có token
3. Mở Developer Tools (F12)
4. Copy và paste script từ file `test-chat-connection.js`
5. Chạy script trong console

#### Cách 2: Test thủ công

1. Mở WebSocket tester (ví dụ: wscat)
2. Kết nối đến: `ws://localhost:8080/ws`
3. Gửi STOMP CONNECT frame với JWT token
4. Subscribe đến `/user/queue/messages`
5. Gửi message đến `/app/chat.send`

### 4. Test REST API

#### Test tạo conversation:

```bash
curl -X POST http://localhost:8080/api/chat/conversations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"otherUserId": "user2", "productId": "product1"}'
```

#### Test lấy conversations:

```bash
curl -X GET http://localhost:8080/api/chat/conversations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Test lấy messages:

```bash
curl -X GET http://localhost:8080/api/chat/conversations/CONVERSATION_ID/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## ✅ Kết quả mong đợi

### WebSocket Test:

- ✅ WebSocket connected successfully!
- ✅ STOMP connection successful!
- ✅ Message sent successfully!

### REST API Test:

- ✅ Status: 200 OK
- ✅ Response: JSON data

## 🔧 Troubleshooting

### Lỗi WebSocket:

1. Kiểm tra backend đã chạy chưa
2. Kiểm tra CORS configuration
3. Kiểm tra JWT token có hợp lệ không

### Lỗi REST API:

1. Kiểm tra authentication
2. Kiểm tra MongoDB connection
3. Kiểm tra request format

### Lỗi Database:

1. Kiểm tra MongoDB đã chạy chưa
2. Kiểm tra connection string
3. Kiểm tra database permissions

## 📝 Logs cần kiểm tra

### Backend Logs:

```
INFO  - WebSocket endpoint registered: /ws
INFO  - STOMP broker configured
INFO  - JWT authentication configured
DEBUG - Message received: /app/chat.send
DEBUG - Message sent to: /user/queue/messages
```

### Frontend Logs:

```
💬 WebSocket connected successfully!
💬 STOMP connection successful!
💬 Message sent: {conversationId, messageType}
💬 Received message: {messageData}
```

## 🎉 Hoàn thành!

Nếu tất cả test đều pass, chức năng chat đã hoạt động hoàn hảo!

**Thời gian thực hiện:** ~2 giờ
**Trạng thái:** ✅ Hoàn thành
