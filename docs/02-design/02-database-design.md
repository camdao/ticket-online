# Database Design

# Thiết kế cơ sở dữ liệu

---

# 1. Mục đích

Tài liệu này mô tả thiết kế cơ sở dữ liệu của hệ thống đặt vé xem phim, bao gồm các thực thể chính, mối quan hệ giữa các bảng và các nguyên tắc thiết kế nhằm đảm bảo tính toàn vẹn dữ liệu.

---

# 2. Công nghệ

* Database: MySQL
* ORM: Spring Data JPA (Hibernate)

---

# 3. Nguyên tắc thiết kế

Cơ sở dữ liệu được thiết kế theo các nguyên tắc:

* Chuẩn hóa dữ liệu để hạn chế trùng lặp.
* Sử dụng khóa chính (Primary Key) cho mỗi bảng.
* Thiết lập khóa ngoại (Foreign Key) để đảm bảo tính toàn vẹn dữ liệu.
* Chỉ lưu dữ liệu lâu dài trong MySQL.
* Dữ liệu tạm thời (ví dụ giữ ghế) không lưu trong MySQL mà được xử lý bởi Redis.

---

# 4. Danh sách bảng

| Bảng            | Mô tả                              |
| --------------- | ---------------------------------- |
| users           | Thông tin người dùng               |
| movies          | Thông tin phim                     |
| showtimes       | Suất chiếu                         |
| seats           | Danh sách ghế                      |
| bookings        | Đơn đặt vé                         |
| booking_details | Danh sách ghế trong một đơn đặt vé |
| payments        | Thông tin thanh toán               |

---

# 5. Quan hệ giữa các bảng

```text
Users (1)
   │
   └──────────────< Bookings (N)

Movies (1)
   │
   └──────────────< Showtimes (N)

Showtimes (1)
   │
   └──────────────< Bookings (N)

Bookings (1)
   │
   └──────────────< Booking_Details (N)

Seats (1)
   │
   └──────────────< Booking_Details (N)

Bookings (1)
   │
   └────────────── Payments (1)
```

> ERD chi tiết được đính kèm trong thư mục `docs/diagrams`.

---

# 6. Mô tả các bảng

## Users

Lưu thông tin tài khoản người dùng.

Các thông tin chính:

* Họ tên
* Email
* Mật khẩu
* Vai trò

---

## Movies

Lưu thông tin phim.

Ví dụ:

* Tên phim
* Thời lượng
* Mô tả
* Hình ảnh

---

## Showtimes

Lưu thông tin các suất chiếu.

Mỗi suất chiếu thuộc một bộ phim.

---

## Seats

Lưu danh sách ghế trong rạp.

Mỗi ghế có:

* Mã ghế
* Loại ghế
* Trạng thái

---

## Bookings

Lưu thông tin đơn đặt vé.

Ví dụ:

* Mã đơn
* Người đặt
* Suất chiếu
* Tổng tiền
* Trạng thái

---

## Booking_Details

Lưu các ghế thuộc một đơn đặt vé.

Một đơn có thể chứa nhiều ghế.

---

## Payments

Lưu thông tin thanh toán.

Ví dụ:

* Mã giao dịch
* Phương thức thanh toán
* Trạng thái
* Thời gian thanh toán

---

# 7. Ràng buộc dữ liệu

* Email là duy nhất.
* Một giao dịch thanh toán chỉ thuộc một đơn đặt vé.
* Một đơn đặt vé phải thuộc một người dùng.
* Một đơn đặt vé có thể bao gồm nhiều ghế.

---

# 8. Chỉ mục (Index)

Các trường thường xuyên truy vấn sẽ được tạo Index:

*

---

# 9. Ghi chú

Việc giữ ghế tạm thời không được lưu trong MySQL.

Thông tin giữ ghế sẽ được quản lý tại Redis nhằm giảm tải cho cơ sở dữ liệu và hạn chế tranh chấp khi nhiều người dùng cùng chọn một ghế.

Chi tiết được trình bày trong `seat-reservation-design.md`.
