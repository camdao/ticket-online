# API Design

# Thiết kế API

---

# 1. Mục đích

Tài liệu này mô tả các API chính của hệ thống Đặt Vé Xem Phim Trực Tuyến, bao gồm quy ước thiết kế, phương thức HTTP, cấu trúc Request/Response và mã lỗi.

---

# 2. Quy ước API

* Kiến trúc: RESTful API
* Định dạng dữ liệu: JSON
* Encoding: UTF-8
* Base URL: `/api/v1`

---

# 3. Xác thực

Các API yêu cầu đăng nhập sử dụng JWT Bearer Token.

Ví dụ:

```http
Authorization: Bearer <access_token>
```

---

# 4. Danh sách API

## Authentication

| Method | Endpoint       | Mô tả             |
| ------ | -------------- | ----------------- |
| POST   | /auth/register | Đăng ký tài khoản |
| POST   | /auth/login    | Đăng nhập         |
| POST   | /auth/logout   | Đăng xuất         |

---

## Movie

| Method | Endpoint               | Mô tả                |
| ------ | ---------------------- | -------------------- |
| GET    | /movies                | Danh sách phim       |
| GET    | /movies/{id}           | Chi tiết phim        |
| GET    | /movies/{id}/showtimes | Danh sách suất chiếu |

---

## Booking

| Method | Endpoint              | Mô tả               |
| ------ | --------------------- | ------------------- |
| GET    | /showtimes/{id}/seats | Danh sách ghế       |
| POST   | /bookings             | Tạo yêu cầu đặt vé  |
| GET    | /bookings/{id}        | Chi tiết đơn đặt vé |
| GET    | /bookings/history     | Lịch sử đặt vé      |

---

## Payment

| Method | Endpoint           | Mô tả                       |
| ------ | ------------------ | --------------------------- |
| POST   | /payments          | Khởi tạo thanh toán         |
| POST   | /payments/callback | Callback từ cổng thanh toán |

---

# 5. Quy ước Response

## Thành công

```json
{
  "success": true,
  "data": {},
  "message": "Success"
}
```

---

## Thất bại

```json
{
  "success": false,
  "message": "Seat is already reserved."
}
```

---

# 6. HTTP Status Code

| Status | Ý nghĩa                                   |
| ------ | ----------------------------------------- |
| 200    | Thành công                                |
| 201    | Tạo mới thành công                        |
| 400    | Dữ liệu không hợp lệ                      |
| 401    | Chưa xác thực                             |
| 403    | Không có quyền truy cập                   |
| 404    | Không tìm thấy dữ liệu                    |
| 409    | Xung đột dữ liệu (ví dụ: ghế đã được giữ) |
| 500    | Lỗi hệ thống                              |

---

# 7. Nguyên tắc thiết kế

* Sử dụng danh từ trong URL.
* Mỗi endpoint chỉ thực hiện một chức năng.
* Sử dụng đúng HTTP Method.
* Trả về HTTP Status Code phù hợp.
* Không trả về thông tin nhạy cảm như mật khẩu.

---

# 8. Tài liệu liên quan

* System Design
* Database Design
* Seat Reservation Design
* Payment Design
