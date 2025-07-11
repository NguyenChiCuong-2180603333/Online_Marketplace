# 🛒 Cosmic Marketplace - Dự án Chợ Trực Tuyến

## Tổng quan

Cosmic Marketplace là nền tảng thương mại điện tử hiện đại, hỗ trợ nhiều vai trò (Admin, Người bán, Khách hàng), chat thời gian thực, hệ thống tích điểm thành viên, quản lý đơn hàng, sản phẩm, danh mục, đánh giá, thông báo, và nhiều tính năng mở rộng khác.

---

## 1. Chức năng chính

### Backend (Spring Boot, MongoDB)

- **Quản lý người dùng**: Đăng ký, đăng nhập, phân quyền (Admin, User), cập nhật hồ sơ, đổi mật khẩu, thống kê người dùng.
- **Quản lý sản phẩm**: CRUD sản phẩm, duyệt sản phẩm, tìm kiếm, lọc, gợi ý AI, quản lý tồn kho, sản phẩm nổi bật/mới.
- **Quản lý danh mục**: CRUD danh mục, tìm kiếm, lọc, thống kê sản phẩm theo danh mục.
- **Quản lý đơn hàng**: Đặt hàng, cập nhật trạng thái (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED), thống kê doanh thu, lịch sử đơn hàng.
- **Quản lý đánh giá**: Đánh giá sản phẩm, hiển thị và quản lý review.
- **Quản lý thông báo**: Gửi/nhận thông báo hệ thống, thông báo đơn hàng, khuyến mãi.
- **Quản lý chat**: Chat thời gian thực giữa khách hàng và người bán qua WebSocket.
- ** loyalty**: Tích điểm, dùng để giảm giá sản phẩm.
- **Quản lý file**: Upload ảnh sản phẩm, avatar.
- **Thống kê**: Thống kê người dùng, sản phẩm, đơn hàng, doanh thu, sản phẩm bán chạy.

### Frontend (Vue 3, Vite, Pinia)

- **Trang chủ**: Giao diện hiện đại, gợi ý sản phẩm cá nhân hóa, sản phẩm hot, danh mục nổi bật.
- **Tìm kiếm & Lọc**: Tìm kiếm sản phẩm, lọc theo danh mục, giá, sắp xếp, gợi ý nhanh.
- **Trang sản phẩm**: Xem chi tiết, hình ảnh, đánh giá, thêm vào giỏ hàng.
- **Giỏ hàng & Thanh toán**: Quản lý giỏ hàng, quy trình thanh toán nhiều bước, chọn địa chỉ, phương thức thanh toán.
- **Quản lý đơn hàng**: Xem lịch sử, trạng thái, chi tiết đơn hàng, lọc/truy vết đơn hàng.
- **Quản lý tài khoản**: Hồ sơ cá nhân, đổi mật khẩu, quản lý địa chỉ.
- **Quản lý chat**: Giao diện chat trực quan, thông báo tin nhắn mới.
- **Quản lý đánh giá**: Viết, xem, quản lý đánh giá sản phẩm.
- **Quản lý thông báo**: Nhận thông báo đơn hàng, khuyến mãi, hệ thống.
- **Trang quản trị (Admin)**: Quản lý người dùng, sản phẩm, đơn hàng, danh mục, thống kê.
- **Trang người bán (Seller)**: Quản lý sản phẩm, đơn hàng.

---

## 2. Công nghệ sử dụng

- **Backend**: Java 17, Spring Boot, MongoDB, WebSocket, JWT, Cloudinary, Stripe, Docker
- **Frontend**: Vue 3, Vite, Pinia, Vue Router, Axios, Chart.js, CSS hiện đại

---

## 3. Khởi động nhanh

### Backend

```bash
cd marketplace-backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd marketplace-frontend
npm install
npm run dev
```

---


