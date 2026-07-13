# Project Overview

## Ticket Online - Hệ thống Đặt Vé Xem Phim Trực Tuyến

---

# 1. Giới thiệu

Ticket Online là hệ thống hỗ trợ người dùng đặt vé xem phim trực tuyến.

Người dùng có thể lựa chọn phim, suất chiếu, ghế ngồi và thanh toán ngay trên hệ thống mà không cần đến rạp để mua vé.

Dự án được xây dựng với mục tiêu mô phỏng quy trình đặt vé thực tế, đồng thời tập trung giải quyết các bài toán Backend như xử lý đồng thời (Concurrency), giữ ghế tạm thời (Seat Reservation), chống thanh toán trùng lặp (Idempotency) và tối ưu hiệu năng bằng Redis.

---

# 2. Bài toán

Trong thời điểm nhiều người cùng đặt vé cho một suất chiếu, hệ thống có thể gặp các vấn đề như:

* Nhiều người cùng chọn một ghế.
* Bán trùng ghế (Overselling).
* Người dùng thanh toán nhiều lần.
* Cổng thanh toán gửi callback nhiều lần.
* Hệ thống phản hồi chậm khi lượng truy cập tăng.

Các vấn đề trên ảnh hưởng trực tiếp đến trải nghiệm người dùng và tính chính xác của dữ liệu.

---

# 3. Giải pháp

Hệ thống được thiết kế nhằm giải quyết các bài toán trên thông qua:

* Xác thực người dùng bằng JWT.
* Giữ ghế tạm thời trước khi thanh toán.
* Chỉ xác nhận đặt vé sau khi thanh toán thành công.
* Sử dụng Redis để xử lý giữ ghế và giảm tải cơ sở dữ liệu.
* Áp dụng Idempotency để tránh tạo đơn hàng trùng.
* Theo dõi hệ thống bằng Prometheus và Grafana.

---

# 4. Quy trình đặt vé

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
Hệ thống giữ ghế 5 phút
    │
    ▼
Thanh toán
    │
 ┌──┴────────┐
 │           │
Thành công  Thất bại
 │           │
 ▼           ▼
Xuất vé    Giải phóng ghế
```

---

# 5. Các chức năng chính

* Đăng ký và đăng nhập.
* Xem danh sách phim.
* Xem thông tin phim.
* Xem lịch chiếu.
* Chọn ghế.
* Đặt vé.
* Thanh toán trực tuyến.
* Xem lịch sử đặt vé.

---

# 6. Công nghệ sử dụng

| Thành phần      | Công nghệ           |
| --------------- | ------------------- |
| Backend         | Java, Spring Boot   |
| Database        | MySQL               |
| Cache           | Redis               |
| Authentication  | JWT                 |
| Monitoring      | Prometheus, Grafana |
| API Testing     | Postman             |
| Version Control | Git, GitHub         |

---

# 7. Những bài toán kỹ thuật nổi bật

Dự án tập trung giải quyết một số bài toán thường gặp trong các hệ thống đặt vé trực tuyến:

* Xử lý nhiều người cùng chọn một ghế.
* Giữ ghế trong thời gian giới hạn.
* Ngăn chặn bán trùng ghế.
* Đảm bảo một giao dịch chỉ tạo một đơn đặt vé.
* Tối ưu hiệu năng bằng cơ chế lưu trữ tạm (Redis).
* Theo dõi tình trạng hoạt động của hệ thống.

---

# 8. Tài liệu dự án

Chi tiết dự án được trình bày trong thư mục **docs**:

* Business Requirements
* System Analysis
* System Design
* Database Design
* API Design
* Seat Reservation Design
* Payment Design
* Testing
* Deployment
* Monitoring
