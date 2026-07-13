# Deployment Design

# Thiết kế triển khai hệ thống

---

# 1. Mục đích

Tài liệu này mô tả kiến trúc triển khai của hệ thống Đặt Vé Xem Phim Trực Tuyến, bao gồm các thành phần hạ tầng, cách các dịch vụ kết nối với nhau và định hướng mở rộng trong tương lai.

---

# 2. Phạm vi

Kiến trúc triển khai hiện tại phù hợp với môi trường phát triển, kiểm thử và triển khai cho hệ thống quy mô nhỏ đến vừa.

---

# 3. Kiến trúc triển khai

```text
                    Internet
                        │
                        ▼
                 +----------------+
                 |     Client     |
                 +----------------+
                        │
                        ▼
                 +----------------+
                 | Spring Boot API|
                 +----------------+
                  │      │       │
                  │      │       │
                  ▼      ▼       ▼
              +------+ +------+ +----------------+
              |MySQL | |Redis | | Payment Gateway|
              +------+ +------+ +----------------+
```

---

# 4. Thành phần triển khai

## Spring Boot

* Cung cấp REST API.
* Xử lý nghiệp vụ.
* Xác thực người dùng.
* Điều phối quá trình đặt vé và thanh toán.

---

## MySQL

Lưu trữ dữ liệu lâu dài:

* Người dùng
* Phim
* Suất chiếu
* Đơn đặt vé
* Thanh toán

---

## Redis

Lưu dữ liệu tạm thời:

* Giữ ghế.
* Cache (nếu có).

---

## Payment Gateway

Xử lý giao dịch thanh toán và gửi kết quả về hệ thống thông qua Callback.

---

# 5. Mạng kết nối

```text
Client
    │
HTTP/HTTPS
    │
Spring Boot
 ├── MySQL (TCP 3306)
 ├── Redis (TCP 6379)
 └── Payment Gateway (HTTPS)
```

---

# 6. Môi trường triển khai

## Development

* Docker Compose
* Spring Boot
* MySQL
* Redis

---

## Production

Có thể mở rộng bằng:

* Reverse Proxy (Nginx)
* Load Balancer
* Nhiều Backend Instance
* Redis Sentinel hoặc Redis Cluster
* MySQL Replication

---

# 7. Khả năng mở rộng

Kiến trúc hiện tại được thiết kế để dễ dàng mở rộng khi nhu cầu tăng.

Các hướng mở rộng bao gồm:

* Triển khai nhiều Backend Instance.
* Phân tải bằng Load Balancer.
* Mở rộng Redis.
* Mở rộng cơ sở dữ liệu.
* Hỗ trợ nhiều rạp chiếu phim.

---

# 8. Yêu cầu triển khai

* Docker Engine
* Docker Compose
* JDK 21
* MySQL 8
* Redis 7

---

# 9. Tài liệu liên quan

* System Design
* Database Design
* API Design
* Seat Reservation Design
* Payment Design
