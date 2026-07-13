# System Design

# Thiết kế hệ thống

---

# 1. Mục đích

Tài liệu này mô tả kiến trúc tổng thể của hệ thống, các thành phần chính và cách chúng tương tác với nhau nhằm đáp ứng các yêu cầu nghiệp vụ đã được phân tích.

Các thiết kế chi tiết như cơ sở dữ liệu, API, Seat Reservation và Payment sẽ được trình bày trong các tài liệu riêng.

---

# 2. Kiến trúc tổng thể

```text
                    +------------------+
                    |      Client      |
                    +------------------+
                             │
                             ▼
                    +------------------+
                    | Spring Boot REST |
                    +------------------+
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
+---------------+    +---------------+    +----------------+
|     MySQL     |    |     Redis     |    | Payment Gateway|
+---------------+    +---------------+    +----------------+
```

---

# 3. Các thành phần của hệ thống

## Client

Cho phép người dùng:

* Đăng nhập
* Xem phim
* Chọn suất chiếu
* Chọn ghế
* Thanh toán
* Xem lịch sử đặt vé

---

## Backend

Backend được xây dựng bằng Spring Boot và chịu trách nhiệm:

* Xử lý nghiệp vụ.
* Xác thực người dùng.
* Quản lý đặt vé.
* Kết nối cơ sở dữ liệu.
* Tích hợp cổng thanh toán.

---

## MySQL

Lưu trữ dữ liệu chính của hệ thống:

* Người dùng
* Phim
* Suất chiếu
* Ghế
* Đơn đặt vé
* Thanh toán

---

## Redis

Redis được sử dụng để:

* Giữ ghế tạm thời trước khi thanh toán.
* Giảm tranh chấp khi nhiều người cùng chọn một ghế.

Chi tiết được trình bày trong **seat-reservation-design.md**.

---

## Payment Gateway

Có nhiệm vụ:

* Xử lý giao dịch thanh toán.
* Gửi kết quả thanh toán về hệ thống.

Chi tiết được trình bày trong **payment-design.md**.

---

# 4. Kiến trúc ứng dụng

```text
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Database
```

Mỗi tầng đảm nhiệm một vai trò riêng:

* Controller tiếp nhận HTTP Request.
* Service xử lý nghiệp vụ.
* Repository thao tác dữ liệu.
* Database lưu trữ dữ liệu.

---

# 5. Luồng xử lý tổng quát

```text
Đăng nhập
      │
      ▼
Chọn phim
      │
      ▼
Chọn suất chiếu
      │
      ▼
Chọn ghế
      │
      ▼
Thanh toán
      │
      ▼
Nhận kết quả thanh toán
      │
      ▼
Xuất vé điện tử
```

Các bước xử lý chi tiết được mô tả trong:

* Seat Reservation Design
* Payment Design

---

# 6. Các tài liệu thiết kế liên quan

| Tài liệu                   | Nội dung               |
| -------------------------- | ---------------------- |
| database-design.md         | Thiết kế cơ sở dữ liệu |
| api-design.md              | Thiết kế API           |
| seat-reservation-design.md | Thiết kế giữ ghế       |
| payment-design.md          | Thiết kế thanh toán    |
| deployment-design.md       | Kiến trúc triển khai   |
