# API Design

# Thiết kế API

---

# 1. Mục đích

Tài liệu này mô tả các API cho người dùng cuối của hệ thống Đặt Vé Xem Phim Trực Tuyến, bao gồm quy ước thiết kế, phương thức HTTP, cấu trúc Request/Response và mã lỗi.

---

# 2. Quy ước API

* Kiến trúc: RESTful API
* Định dạng dữ liệu: JSON
* Encoding: UTF-8
* Base URL: `/api/v1`

---

# 3. Xác thực

![Authentication Flow](../diagrams/api-authentication-flow.svg)

Các API yêu cầu đăng nhập sử dụng JWT Bearer Token.

Ví dụ: ``` http Authorization: Bearer <access_token> ```

---

# 4. Danh sách API

## 4.1. Authentication

### POST /auth/register
Đăng ký tài khoản mới.

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phoneNumber": "0912345678"
}
```

**Response (201):**
```json
{
  "success": true,
  "status": 201,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### POST /auth/login
Đăng nhập vào hệ thống.

**Request:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### POST /auth/logout
Đăng xuất khỏi hệ thống.

**Headers:**
- `Authorization: Bearer <access_token>`

**Response (200):**
```json
{
  "success": true,
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### POST /auth/refresh
Làm mới access token.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 4.2. Movies

### GET /movies
Lấy danh sách phim với thông tin rạp chiếu.

**Query Parameters:**
- `status` (optional): Trạng thái phim (NOW_SHOWING, UPCOMING)
- `cinemaId` (optional): Lọc phim theo rạp chiếu
- `sortBy` (optional): Sắp xếp theo (releaseDate, title, rating)
- `sortDirection` (optional): Hướng sắp xếp (ASC, DESC)

**Examples:**
- `GET /movies` - Lấy tất cả phim với danh sách rạp đang chiếu
- `GET /movies?status=NOW_SHOWING` - Lấy phim đang chiếu
- `GET /movies?cinemaId=5` - Lấy phim đang chiếu tại rạp có ID = 5
- `GET /movies?status=NOW_SHOWING&cinemaId=5` - Lấy phim đang chiếu tại rạp có ID = 5

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Avatar: The Way of Water",
        "duration": 192,
        "description": "Jake Sully lives with his newfound family...",
        "genre": "Action, Adventure, Sci-Fi",
        "director": "James Cameron",
        "cast": "Sam Worthington, Zoe Saldana, Sigourney Weaver",
        "ageRating": "T13",
        "releaseDate": "2023-12-16",
        "posterUrl": "https://cdn.example.com/avatar2.jpg",
        "trailerUrl": "https://youtube.com/watch?v=...",
        "status": "NOW_SHOWING",
        "cinemas": [
          {
            "id": 5,
            "name": "CGV Vincom Center",
            "brand": "CGV",
            "logoUrl": "https://cdn.example.com/cgv-logo.png",
            "address": "72 Lê Thánh Tôn",
            "district": "Quận 1",
            "city": "TP. Hồ Chí Minh",
            "phone": "1900xxxx",
            "website": "https://www.cgv.vn",
            "description": "Premium cinema with IMAX",
            "totalScreens": null,
            "createdAt": "2024-01-15T10:00:00",
            "updatedAt": "2024-01-15T10:00:00"
          },
          {
            "id": 6,
            "name": "CGV Aeon Tân Phú",
            "brand": "CGV",
            "logoUrl": "https://cdn.example.com/cgv-logo.png",
            "address": "30 Bờ Bao Tân Thắng",
            "district": "Tân Phú",
            "city": "TP. Hồ Chí Minh",
            "phone": "1900xxxx",
            "website": "https://www.cgv.vn",
            "description": null,
            "totalScreens": null,
            "createdAt": "2024-01-15T10:00:00",
            "updatedAt": "2024-01-15T10:00:00"
          }
        ]
      }
    ],
    "totalElements": 15
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Lưu ý:**
- Mảng `cinemas` chứa danh sách các rạp đang có suất chiếu cho phim này
- Khi lọc theo `cinemaId`, chỉ trả về các phim đang chiếu tại rạp đó
- Có thể kết hợp filter `status` và `cinemaId` để lọc phim đang chiếu/sắp chiếu tại một rạp cụ thể

---

### GET /movies/{id}
Lấy chi tiết phim.

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "title": "Avatar: The Way of Water",
    "duration": 192,
    "description": "Jake Sully lives with his newfound family...",
    "genre": "Action, Adventure, Sci-Fi",
    "director": "James Cameron",
    "cast": "Sam Worthington, Zoe Saldana, Sigourney Weaver",
    "rating": 8.5,
    "ageRating": "T13",
    "releaseDate": "2023-12-16",
    "posterUrl": "https://cdn.example.com/avatar2.jpg",
    "trailerUrl": "https://youtube.com/watch?v=...",
    "status": "NOW_SHOWING"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```
---
### GET /movies/search
Tìm kiếm phim theo từ khóa.

**Query Parameters:**
- `keyword` (required): Từ khóa tìm kiếm

**Response:** Tương tự GET /movies

---

## 4.3. Cinemas

### GET /cinemas
Lấy danh sách rạp chiếu.

**Query Parameters:**
- `brand` (optional): Thương hiệu rạp (CGV, Lotte, Galaxy)
- `city` (optional): Thành phố
- `district` (optional): Quận/huyện

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 5,
      "name": "CGV Vincom Center",
      "brand": "CGV",
      "logoUrl": "https://cdn.example.com/cgv-logo.png",
      "address": "72 Lê Thánh Tôn",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh",
      "phoneNumber": "1900xxxx",
      "totalRooms": 8
    }
  ],
  "timestamp": "2024-01-15T14:30:00"
}
```
## 4.4.Showtimes


---

### GET /showtimes
Lấy danh sách suất chiếu của phim.

**Query Parameters:**
- `movieId` (optional): Lọc theo phim
- `cinemaId` (optional): Lọc theo rạp
- `city` (optional): Lọc theo thành phố
- `date` (optional): Lọc theo ngày (format: YYYY-MM-DD)
- `startDate` (optional): Từ ngày (format: YYYY-MM-DD)
- `endDate` (optional): Đến ngày (format: YYYY-MM-DD)

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 101,
      "movieId": 1,
      "cinemaId": 5,
      "cinemaName": "CGV Vincom Center",
      "cinemaAddress": "72 Lê Thánh Tôn, Quận 1, TP.HCM",
      "startTime": "2024-01-15T14:30:00",
      "endTime": "2024-01-15T17:42:00",
      "basePrice": 85000,
      "status": "ACTIVE",
      "availableSeats": 87,
      "totalSeats": 120
    }
  ],
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### GET /showtimes/dates
Lấy danh sách các ngày có suất chiếu của phim tại rạp.

**Query Parameters:**
- `movieId` (required): ID của phim
- `cinemaId` (required): ID của rạp chiếu

**Example:**
- `GET /showtimes/dates?movieId=1&cinemaId=5`

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": [
    "2026-08-11",
    "2026-08-12",
    "2026-08-14",
    "2026-08-15",
    "2026-08-17"
  ],
  "timestamp": "2026-08-11T11:15:00"
}
```

---

### GET /showtimes/{id}
Lấy chi tiết suất chiếu.

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 17,
    "startTime": "2024-01-15T14:30:00",
    "endTime": "2024-01-15T17:42:00",
    "basePrice": 85000,
    "status": "ACTIVE",
    "availableSeats": 87,
    "totalSeats": 120,
    "movie": {
      "id": 1,
      "title": "Avatar: The Way of Water",
      "duration": 192,
      "genre": "Action, Adventure, Sci-Fi",
      "ageRating": "T13",
      "posterUrl": "https://cdn.example.com/avatar2.jpg"
    },
    "cinema": {
      "id": 5,
      "name": "CGV Vincom Center",
      "brand": "CGV",
      "address": "72 Lê Thánh Tôn, Quận 1, TP.HCM",
      "phoneNumber": "1900xxxx"
    }
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---
### GET /showtimes/{id}/seats
Lấy sơ đồ ghế của suất chiếu.

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "showtimeId": 101,
    "seats": [
      {
        "id": 1201,
        "row": "A",
        "number": 1,
        "type": "REGULAR",
        "price": 85000,
        "status": "AVAILABLE"
      },
      {
        "id": 1202,
        "row": "A",
        "number": 2,
        "type": "REGULAR",
        "price": 85000,
        "status": "BOOKED"
      },
      {
        "id": 1203,
        "row": "A",
        "number": 3,
        "type": "REGULAR",
        "price": 85000,
        "status": "HELD"
      },
      {
        "id": 1215,
        "row": "A",
        "number": 15,
        "type": "VIP",
        "price": 120000,
        "status": "AVAILABLE"
      },
      {
        "id": 1230,
        "row": "B",
        "number": 15,
        "type": "COUPLE",
        "price": 200000,
        "status": "AVAILABLE"
      }
    ]
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Seat Status:**
- `AVAILABLE`: Ghế còn trống
- `BOOKED`: Đã được đặt (confirmed booking)
- `HELD`: Đang được giữ tạm thời (chưa thanh toán)

**Seat Type:**
- `REGULAR`: Ghế thường
- `VIP`: Ghế VIP
- `COUPLE`: Ghế đôi

---

## 4.6. Bookings

### POST /bookings
Tạo đơn đặt vé với thanh toán (thao tác atomic: giữ ghế + tạo booking + tạo payment URL).

**Headers:**
- `Authorization: Bearer <access_token>`

**Request:**
```json
{
  "showtimeId": 101,
  "seatIds": [1201, 1202, 1203],
  "customerEmail": "john@example.com",
  "customerPhone": "0912345678",
  "paymentMethod": "VNPAY",
  "returnUrl": "https://example.com/booking-success"
}
```

**Response (201):**
```json
{
  "success": true,
  "status": 201,
  "data": {
    "id": 5001,
    "bookingCode": "BK20240115001",
    "userId": 123,
    "showtimeId": 101,
    "movieTitle": "Avatar: The Way of Water",
    "cinemaName": "CGV Vincom Center",
    "showtime": "2024-01-15T14:30:00",
    "seats": [
      {
        "id": 1201,
        "row": "A",
        "number": 1,
        "type": "REGULAR",
        "price": 85000
      },
      {
        "id": 1202,
        "row": "A",
        "number": 2,
        "type": "REGULAR",
        "price": 85000
      },
      {
        "id": 1203,
        "row": "A",
        "number": 3,
        "type": "REGULAR",
        "price": 85000
      }
    ],
    "totalAmount": 255000,
    "status": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
    "transactionId": "PAY20240115001",
    "createdAt": "2024-01-15T14:30:00",
    "expiresAt": "2024-01-15T14:45:00"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Error Response (400):**
```json
{
  "success": false,
  "status": 400,
  "data": {
    "errorClassName": "InvalidBookingException",
    "message": "Invalid booking data"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Error Response (409):**
```json
{
  "success": false,
  "status": 409,
  "data": {
    "errorClassName": "SeatConflictException",
    "message": "Some seats are already booked or held"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Lưu ý:**
- API này thực hiện 3 thao tác trong một transaction:
  1. Giữ ghế trong Redis (seat locking)
  2. Tạo booking với trạng thái PENDING
  3. Tạo payment URL để chuyển hướng người dùng
- Booking sẽ tự động hết hạn sau 15 phút nếu không thanh toán
- Ghế sẽ được giữ trong Redis cho đến khi booking được confirm hoặc hết hạn

---

### GET /bookings
Lấy lịch sử đặt vé của người dùng.

**Headers:**
- `Authorization: Bearer <access_token>`

**Query Parameters:**
- `status` (optional): Lọc theo trạng thái (PENDING, CONFIRMED, CANCELLED, EXPIRED)
- `page`, `size` (optional)

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 5001,
        "bookingCode": "BK20240115001",
        "movieTitle": "Avatar: The Way of Water",
        "moviePosterUrl": "https://cdn.example.com/avatar2.jpg",
        "cinemaName": "CGV Vincom Center",
        "showtime": "2024-01-15T14:30:00",
        "seatCount": 3,
        "totalAmount": 255000,
        "status": "CONFIRMED",
        "createdAt": "2024-01-15T14:30:00",
        "confirmedAt": "2024-01-15T14:32:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### GET /bookings/{id}
Lấy chi tiết đơn đặt vé.

**Headers:**
- `Authorization: Bearer <access_token>`

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 5001,
    "bookingCode": "BK20240115001",
    "userId": 123,
    "movie": {
      "id": 1,
      "title": "Avatar: The Way of Water",
      "posterUrl": "https://cdn.example.com/avatar2.jpg",
      "duration": 192,
      "ageRating": "T13"
    },
    "cinema": {
      "id": 5,
      "name": "CGV Vincom Center",
      "address": "72 Lê Thánh Tôn, Quận 1, TP.HCM"
    },
    "showtime": "2024-01-15T14:30:00",
    "seats": [
      {
        "id": 1201,
        "row": "A",
        "number": 1,
        "type": "REGULAR",
        "price": 85000
      },
      {
        "id": 1202,
        "row": "A",
        "number": 2,
        "type": "REGULAR",
        "price": 85000
      },
      {
        "id": 1203,
        "row": "A",
        "number": 3,
        "type": "REGULAR",
        "price": 85000
      }
    ],
    "totalAmount": 255000,
    "status": "CONFIRMED",
    "paymentStatus": "SUCCESS",
    "createdAt": "2024-01-15T14:30:00",
    "confirmedAt": "2024-01-15T14:32:00",
    "expiresAt": null
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### DELETE /bookings/{id}
Hủy đơn đặt vé (chỉ được phép hủy đơn chưa thanh toán hoặc chưa đến giờ chiếu).

**Headers:**
- `Authorization: Bearer <access_token>`

**Response (200):**
```json
{
  "success": true,
  "timestamp": "2024-01-15T14:30:00"
}
```

**Error Response (400):**
```json
{
  "success": false,
  "status": 400,
  "data": {
    "errorClassName": "CancellationNotAllowedException",
    "message": "Cannot cancel booking: either already confirmed and showtime is less than 2 hours away, or booking is already expired"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 4.7. Payments

### POST /payments
Khởi tạo thanh toán cho đơn đặt vé.

**Headers:**
- `Authorization: Bearer <access_token>`

**Request:**
```json
{
  "bookingId": 5001,
  "paymentMethod": "VNPAY",
  "returnUrl": "https://example.com/booking-success",
  "cancelUrl": "https://example.com/booking-cancel"
}
```

**Response (201):**
```json
{
  "success": true,
  "status": 201,
  "data": {
    "paymentId": 7001,
    "bookingId": 5001,
    "paymentMethod": "VNPAY",
    "amount": 255000,
    "status": "PENDING",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
    "transactionId": "PAY20240115001",
    "expiresAt": "2024-01-15T14:45:00"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

### GET /payments/callback
Xử lý callback từ cổng thanh toán (VNPay).

**Query Parameters:**
- Các tham số do VNPay trả về (vnp_ResponseCode, vnp_TransactionNo, vnp_SecureHash, etc.)

**Response:** Redirect về returnUrl hoặc cancelUrl với thông tin thanh toán.

---

### POST /payments/{id}/verify
Xác minh trạng thái thanh toán.

**Headers:**
- `Authorization: Bearer <access_token>`

**Response (200):**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "paymentId": 7001,
    "bookingId": 5001,
    "status": "SUCCESS",
    "amount": 255000,
    "transactionId": "PAY20240115001",
    "paidAt": "2024-01-15T14:32:00"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

---

# 5. Quy ước Response

## Thành công

```json
{
  "success": true,
  "status": 200,
  "data": {
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Mô tả các trường:**
- `success`: Boolean - Trạng thái thành công/thất bại
- `status`: Integer - HTTP status code
- `data`: Object - Dữ liệu trả về
- `timestamp`: String (ISO 8601) - Thời gian phản hồi

---

## Thất bại

```json
{
  "success": false,
  "status": 400,
  "data": {
    "errorClassName": "ValidationException",
    "message": "Validation failed for one or more fields"
  },
  "timestamp": "2024-01-15T14:30:00"
}
```

**Mô tả các trường:**
- `success`: false
- `status`: HTTP status code (4xx, 5xx)
- `data`: ErrorResponse object
  - `errorClassName`: Tên class exception
  - `message`: Thông báo lỗi
- `timestamp`: Thời gian phản hồi

---

# 6. HTTP Status Code

| Status | Ý nghĩa                                     |
| ------ | ------------------------------------------- |
| 200    | Thành công                                  |
| 201    | Tạo mới thành công                          |
| 400    | Dữ liệu không hợp lệ                        |
| 401    | Chưa xác thực                               |
| 403    | Không có quyền truy cập                     |
| 404    | Không tìm thấy dữ liệu                      |
| 409    | Xung đột dữ liệu (ví dụ: ghế đã được đặt)   |
| 422    | Validation error (dữ liệu không hợp lệ)     |
| 500    | Lỗi hệ thống                                |

---
