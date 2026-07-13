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
| cinemas         | Thông tin rạp chiếu phim           |
| rooms           | Phòng chiếu trong rạp              |
| showtimes       | Suất chiếu                         |
| seats           | Danh sách ghế trong phòng          |
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

Cinemas (1)
   ├──────────────< Showtimes (N)
   └──────────────< Rooms (N)

Rooms (1)
   ├──────────────< Showtimes (N)
   └──────────────< Seats (N)

Showtimes (1)
   │
   └──────────────< Bookings (N)

Bookings (1)
   ├──────────────< Booking_Details (N)
   └────────────── Payments (1)

Seats (1)
   │
   └──────────────< Booking_Details (N)
```

> ERD chi tiết được đính kèm trong thư mục `docs/diagrams`.

---

# 6. Mô tả các bảng

## Users

Lưu thông tin tài khoản người dùng.

**Các thông tin chính:**

* ID (PK)
* Họ tên
* Email (unique)
* Mật khẩu (hashed)
* Vai trò (ADMIN, USER)
* Ngày tạo, cập nhật

**Quan hệ:**
- **Users → Bookings (1:N)**: Một người dùng có thể có nhiều đơn đặt vé.

---

## Movies

Lưu thông tin phim.

**Các thông tin chính:**

* ID (PK)
* Tên phim
* Thời lượng (phút)
* Mô tả
* Thể loại
* Đạo diễn
* Diễn viên
* Ngày phát hành
* Hình ảnh/Poster URL
* Trailer URL
* Độ tuổi cho phép

**Quan hệ:**
- **Movies → Showtimes (1:N)**: Một phim có thể có nhiều suất chiếu.

---

## Cinemas

Lưu thông tin rạp chiếu phim.

**Các thông tin chính:**

* ID (PK)
* Tên rạp (CGV Vincom Center, Lotte Cinema Diamond Plaza...)
* Thương hiệu (CGV, Lotte Cinema, Galaxy Cinema...)
* Logo URL
* Địa chỉ
* Quận/Huyện
* Thành phố/Tỉnh
* Số điện thoại
* Website
* Mô tả

**Quan hệ:**
- **Cinemas → Rooms (1:N)**: Một rạp có thể có nhiều phòng chiếu.
- **Cinemas → Showtimes (1:N)**: Một rạp có thể có nhiều suất chiếu.

---

## Rooms

Lưu thông tin phòng chiếu trong cinema.

**Các thông tin chính:**

* ID (PK)
* Tên phòng (Room 1, Room 2, IMAX, 3D...)
* Tổng số ghế
* Loại phòng (Standard, VIP, IMAX)
* Cinema ID (FK)

**Quan hệ:**
- **Cinemas → Rooms (1:N)**: Mỗi phòng thuộc về một rạp cụ thể.
- **Rooms → Seats (1:N)**: Một phòng chứa nhiều ghế.
- **Rooms → Showtimes (1:N)**: Một phòng có thể có nhiều suất chiếu.

---

## Showtimes

Lưu thông tin các suất chiếu.

**Các thông tin chính:**

* ID (PK)
* Movie ID (FK)
* Cinema ID (FK)
* Room ID (FK)
* Thời gian bắt đầu
* Thời gian kết thúc
* Giá vé cơ bản
* Trạng thái (ACTIVE, CANCELLED)

**Quan hệ:**
- **Movies → Showtimes (1:N)**: Mỗi suất chiếu chiếu một bộ phim cụ thể.
- **Cinemas → Showtimes (1:N)**: Mỗi suất chiếu diễn ra tại một rạp cụ thể.
- **Rooms → Showtimes (1:N)**: Mỗi suất chiếu diễn ra tại một phòng cụ thể.
- **Showtimes → Bookings (1:N)**: Một suất chiếu có thể có nhiều đơn đặt vé.

**Lợi ích của việc thêm Cinema ID:**
- Dễ dàng query "phim X đang chiếu ở rạp nào": `SELECT DISTINCT cinema_id FROM showtimes WHERE movie_id = ?`
- Có thể đặt giá vé khác nhau cho cùng một phim ở các rạp khác nhau
- Hỗ trợ tốt cho tính năng "tìm phim gần tôi" (filter theo cinema)
- Logic rõ ràng hơn: Movie → Cinema → Showtime → Room

---

## Seats

Lưu danh sách ghế trong từng phòng chiếu.

**Các thông tin chính:**

* ID (PK)
* Room ID (FK)
* Mã hàng ghế (A, B, C...)
* Số ghế (1, 2, 3...)
* Loại ghế (REGULAR, VIP, COUPLE)
* Giá bổ sung (surcharge)

**Quan hệ:**
- **Rooms → Seats (1:N)**: Mỗi ghế thuộc về một phòng chiếu cụ thể.
- **Seats → Booking_Details (1:N)**: Mỗi ghế có thể được đặt trong nhiều đơn khác nhau (các suất chiếu khác nhau). Quan hệ này được thể hiện qua bảng trung gian `booking_details`.

**Lưu ý:**
- Ghế không trực tiếp nối với suất chiếu hay booking. 
- Việc ghế nào được đặt trong suất chiếu nào được quản lý thông qua bảng `booking_details`, kết hợp `seat_id` và `booking_id` (mà booking đã có `showtime_id`).
- Trạng thái ghế (đã đặt/còn trống) được tính toán runtime dựa trên dữ liệu trong `booking_details` và `bookings`.

---

## Bookings

Lưu thông tin đơn đặt vé.

**Các thông tin chính:**

* ID (PK)
* Mã đơn (unique code)
* User ID (FK)
* Showtime ID (FK)
* Tổng tiền
* Trạng thái (PENDING, CONFIRMED, CANCELLED, EXPIRED)
* Thời gian tạo
* Thời gian xác nhận
* Thời gian hết hạn

**Quan hệ:**
- **Users → Bookings (1:N)**: Mỗi đơn đặt vé thuộc về một người dùng.
- **Showtimes → Bookings (1:N)**: Mỗi đơn đặt vé dành cho một suất chiếu cụ thể.
- **Bookings → Booking_Details (1:N)**: Một đơn có thể chứa nhiều ghế.
- **Bookings → Payments (1:1)**: Một đơn có một giao dịch thanh toán.

---

## Booking_Details

Bảng trung gian lưu các ghế thuộc một đơn đặt vé.

**Các thông tin chính:**

* ID (PK)
* Booking ID (FK)
* Seat ID (FK)
* Giá vé (snapshot tại thời điểm đặt)

**Quan hệ:**
- **Bookings → Booking_Details (1:N)**: Một đơn có thể chứa nhiều ghế.
- **Seats → Booking_Details (1:N)**: Một ghế có thể xuất hiện trong nhiều booking_details (các suất chiếu khác nhau).

**Lưu ý:**
- Đây là bảng kết nối giữa Bookings và Seats.
- Kết hợp với `showtime_id` trong Bookings, ta biết được ghế nào đã được đặt trong suất chiếu nào.

---

## Payments

Lưu thông tin thanh toán.

**Các thông tin chính:**

* ID (PK)
* Booking ID (FK, unique)
* Mã giao dịch (transaction ID)
* Phương thức thanh toán (VNPAY, MOMO, CASH)
* Số tiền
* Trạng thái (PENDING, SUCCESS, FAILED)
* Thời gian thanh toán
* Thông tin phản hồi từ gateway (JSON)

**Quan hệ:**
- **Bookings → Payments (1:1)**: Một đơn đặt vé có một giao dịch thanh toán duy nhất.

---

# 7. Ràng buộc dữ liệu

* Email trong bảng Users là duy nhất.
* Mã đơn (booking_code) trong bảng Bookings là duy nhất.
* Một giao dịch thanh toán chỉ thuộc một đơn đặt vé (booking_id là unique trong Payments).
* Một đơn đặt vé phải thuộc một người dùng và một suất chiếu cụ thể.
* Một đơn đặt vé có thể bao gồm nhiều ghế (thông qua booking_details).
* Một ghế trong một phòng không được trùng lặp (unique constraint trên room_id, row, seat_number).
* Thời gian kết thúc suất chiếu phải sau thời gian bắt đầu.

---

# 8. Chỉ mục (Index)

Các trường thường xuyên truy vấn sẽ được tạo Index:

* `users.email` - Tìm kiếm người dùng theo email
* `bookings.user_id` - Lấy danh sách đơn của user
* `bookings.showtime_id` - Lấy danh sách booking của suất chiếu
* `bookings.booking_code` - Tìm kiếm đơn theo mã
* `showtimes.movie_id` - Lấy suất chiếu của phim
* `showtimes.cinema_id` - Lấy suất chiếu của rạp
* `showtimes.room_id` - Lấy suất chiếu của phòng
* `showtimes.start_time` - Tìm suất chiếu theo thời gian
* `showtimes.(movie_id, cinema_id)` - Composite index để tìm phim theo rạp
* `seats.room_id` - Lấy danh sách ghế của phòng
* `booking_details.booking_id` - Lấy chi tiết ghế của đơn
* `booking_details.seat_id` - Kiểm tra ghế đã được đặt chưa
* `payments.booking_id` - Lấy thông tin thanh toán của đơn
* `cinemas.brand` - Tìm kiếm rạp theo thương hiệu
* `rooms.cinema_id` - Lấy danh sách phòng của rạp

---

# 9. Ghi chú

## 9.1. Quản lý trạng thái ghế

Việc giữ ghế tạm thời không được lưu trong MySQL.

Thông tin giữ ghế sẽ được quản lý tại Redis nhằm giảm tải cho cơ sở dữ liệu và hạn chế tranh chấp khi nhiều người dùng cùng chọn một ghế.

Chi tiết được trình bày trong `seat-reservation-design.md`.

## 9.2. Giải thích về quan hệ Seats

Bảng Seats chỉ lưu thông tin cấu trúc của ghế trong phòng chiếu (vị trí, loại ghế). 

Quan hệ của ghế với booking và showtime được thể hiện gián tiếp qua:
- **Seats → Booking_Details**: Ghế nào được đặt
- **Booking_Details → Bookings**: Thuộc đơn đặt vé nào
- **Bookings → Showtimes**: Đơn đặt vé đó cho suất chiếu nào

Do đó, để biết ghế nào đã được đặt trong suất chiếu nào, cần JOIN:
```sql
SELECT s.*
FROM seats s
JOIN booking_details bd ON s.id = bd.seat_id
JOIN bookings b ON bd.booking_id = b.id
WHERE b.showtime_id = ? AND b.status = 'CONFIRMED'
```

## 9.3. Snapshot giá vé

Giá vé được lưu snapshot tại thời điểm đặt trong bảng `booking_details` để đảm bảo tính chính xác khi giá thay đổi theo thời gian.

---

# 10. Tham khảo

* ERD: `docs/diagrams/erd.svg`
* Thiết kế giữ ghế: `docs/02-design/seat-reservation-design.md`
* Database Schema: `init.sql`