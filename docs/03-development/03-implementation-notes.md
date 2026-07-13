# Implementation Notes

# Ghi chú triển khai

---

# 1. Mục đích

Tài liệu này ghi lại các quyết định kỹ thuật trong quá trình phát triển hệ thống, bao gồm lý do lựa chọn công nghệ, các giải pháp đã áp dụng và những điểm cần cải thiện trong tương lai.

---

# 2. Kiến trúc dự án

* Sử dụng Package by Feature để tổ chức mã nguồn theo từng nghiệp vụ.
* Mỗi module chịu trách nhiệm cho một chức năng riêng biệt như Authentication, Booking, Payment và Movie Catalog.
* Các thành phần dùng chung được đặt trong package `global`.

---

# 3. Cơ sở dữ liệu

* Sử dụng MySQL làm cơ sở dữ liệu chính.
* Hibernate chịu trách nhiệm ánh xạ Entity với bảng dữ liệu.
* Thiết kế cơ sở dữ liệu được thực hiện trước khi phát triển chức năng.

---

# 4. Giữ ghế

* Redis được sử dụng để lưu trạng thái giữ ghế tạm thời.
* Thời gian giữ ghế mặc định là 5 phút.
* Sau khi hết thời gian giữ, ghế sẽ tự động được giải phóng.

---

# 5. Thanh toán

* Đơn đặt vé chỉ được xác nhận sau khi thanh toán thành công.
* Hệ thống xử lý callback từ cổng thanh toán để cập nhật trạng thái giao dịch.
* Mỗi giao dịch chỉ được xử lý một lần nhằm tránh tạo đơn đặt vé trùng lặp.

---

# 6. Bảo mật

* Sử dụng JWT để xác thực người dùng.
* Mật khẩu được mã hóa bằng BCrypt.
* Các API yêu cầu đăng nhập đều được bảo vệ bởi Spring Security.

---

# 7. Các quyết định kỹ thuật

| Quyết định         | Lý do                                      |
| ------------------ | ------------------------------------------ |
| Package by Feature | Dễ mở rộng theo nghiệp vụ                  |
| Spring Data JPA    | Giảm mã truy cập cơ sở dữ liệu             |
| Redis              | Quản lý dữ liệu tạm thời và hỗ trợ giữ ghế |
| JWT                | Phù hợp với RESTful API                    |
| Docker Compose     | Đơn giản hóa môi trường phát triển         |

---

# 8. Hạn chế hiện tại

* Hệ thống chỉ hỗ trợ một rạp chiếu phim.
* Chưa triển khai nhiều Backend Instance.
* Redis chưa được cấu hình theo mô hình High Availability.
* Chưa triển khai cơ chế giám sát và thu thập log tập trung.

---

# 9. Hướng phát triển

* Hỗ trợ nhiều rạp và nhiều cụm rạp.
* Triển khai Load Balancer và nhiều Backend Instance.
* Sử dụng Redis Sentinel hoặc Redis Cluster.
* Bổ sung kiểm thử tải và giám sát hệ thống.
