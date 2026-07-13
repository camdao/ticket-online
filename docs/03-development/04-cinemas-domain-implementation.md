# Cinemas Domain Implementation

## Tổng quan

Domain **cinemas** đã được triển khai thành công để quản lý thông tin rạp chiếu phim và phòng chiếu (screens/rooms) trong hệ thống đặt vé xem phim.

---

## Cấu trúc domain

```
src/main/java/com/ticket_online/domain/cinemas/
├── api/
│   ├── CinemaController.java      # REST endpoints cho cinemas
│   └── ScreenController.java      # REST endpoints cho screens
├── application/
│   ├── CinemaService.java         # Business logic cho cinemas
│   └── ScreenService.java         # Business logic cho screens
├── dao/
│   ├── CinemaRepository.java      # JPA Repository cho Cinema
│   └── ScreenRepository.java      # JPA Repository cho Screen
├── domain/
│   ├── Cinema.java                # Entity đại diện cho rạp chiếu phim
│   └── Screen.java                # Entity đại diện cho phòng chiếu
└── dto/
    ├── request/
    │   ├── CinemaRequest.java     # DTO cho tạo/cập nhật cinema
    │   └── ScreenRequest.java     # DTO cho tạo/cập nhật screen
    └── response/
        ├── CinemaResponse.java         # DTO trả về thông tin cinema
        ├── CinemaListResponse.java     # DTO trả về danh sách cinemas
        ├── CinemaDetailResponse.java   # DTO chi tiết cinema kèm screens
        └── ScreenResponse.java         # DTO trả về thông tin screen
```

---

## Entities

### Cinema Entity

**Thuộc tính:**
- `id` - Primary key
- `name` - Tên rạp (VD: CGV Vincom Center)
- `brand` - Thương hiệu (VD: CGV, Lotte Cinema, Galaxy Cinema)
- `logoUrl` - URL logo thương hiệu
- `address` - Địa chỉ chi tiết
- `district` - Quận/Huyện
- `city` - Thành phố/Tỉnh
- `phone` - Số điện thoại
- `website` - Website
- `description` - Mô tả
- `createdAt`, `updatedAt` - Thời gian tạo/cập nhật

**Methods:**
- `createCinema()` - Factory method để tạo cinema mới
- `updateCinema()` - Cập nhật thông tin cinema
- `getFullAddress()` - Lấy địa chỉ đầy đủ

### Screen Entity

**Thuộc tính:**
- `id` - Primary key
- `cinemaId` - Foreign key tới Cinema
- `cinema` - ManyToOne relationship với Cinema
- `name` - Tên phòng (VD: Room 1, IMAX, VIP Room)
- `capacity` - Tổng số ghế
- `roomType` - Loại phòng (Standard, IMAX, VIP, 4DX)
- `createdAt`, `updatedAt` - Thời gian tạo/cập nhật

**Methods:**
- `createScreen()` - Factory method để tạo screen mới
- `updateScreen()` - Cập nhật thông tin screen
- `isStandardRoom()` - Kiểm tra có phải phòng standard
- `isPremiumRoom()` - Kiểm tra có phải phòng premium

---

## API Endpoints

### Cinema Endpoints

#### GET /api/cinemas
Lấy danh sách cinemas với filter options

**Query Parameters:**
- `brand` (optional) - Lọc theo thương hiệu
- `city` (optional) - Lọc theo thành phố
- `district` (optional) - Lọc theo quận/huyện
- `keyword` (optional) - Tìm kiếm theo tên hoặc thương hiệu

**Response:** `CinemaListResponse`

#### GET /api/cinemas/{id}
Lấy thông tin cinema theo ID

**Response:** `CinemaResponse`

#### GET /api/cinemas/{id}/detail
Lấy thông tin chi tiết cinema kèm danh sách screens

**Response:** `CinemaDetailResponse`

#### GET /api/cinemas/{id}/screens
Lấy danh sách screens của một cinema

**Response:** `List<ScreenResponse>`

#### GET /api/cinemas/brands
Lấy danh sách tất cả các thương hiệu

**Response:** `List<String>`

#### GET /api/cinemas/cities
Lấy danh sách tất cả các thành phố

**Response:** `List<String>`

#### POST /api/cinemas
Tạo cinema mới (Admin only)

**Request Body:** `CinemaRequest`
**Response:** `CinemaResponse` (201 Created)

#### PUT /api/cinemas/{id}
Cập nhật cinema (Admin only)

**Request Body:** `CinemaRequest`
**Response:** `CinemaResponse`

#### DELETE /api/cinemas/{id}
Xóa cinema (Admin only)

**Response:** 204 No Content

### Screen Endpoints

#### GET /api/screens/{id}
Lấy thông tin screen theo ID

**Response:** `ScreenResponse`

#### GET /api/screens
Lấy danh sách screens với filter

**Query Parameters:**
- `cinemaId` (optional) - Lọc theo cinema
- `roomType` (optional) - Lọc theo loại phòng

**Response:** `List<ScreenResponse>`

#### POST /api/screens
Tạo screen mới (Admin only)

**Request Body:** `ScreenRequest`
**Response:** `ScreenResponse` (201 Created)

#### PUT /api/screens/{id}
Cập nhật screen (Admin only)

**Request Body:** `ScreenRequest`
**Response:** `ScreenResponse`

#### DELETE /api/screens/{id}
Xóa screen (Admin only)

**Response:** 204 No Content

---

## Repository Methods

### CinemaRepository

- `findByBrand(String brand)` - Tìm cinemas theo thương hiệu
- `findByCity(String city)` - Tìm cinemas theo thành phố
- `findByDistrict(String district)` - Tìm cinemas theo quận/huyện
- `findByCityAndDistrict(String city, String district)` - Tìm theo thành phố và quận
- `searchByKeyword(String keyword)` - Tìm kiếm theo keyword (name hoặc brand)
- `findAllBrands()` - Lấy danh sách tất cả thương hiệu (distinct)
- `findAllCities()` - Lấy danh sách tất cả thành phố (distinct)

### ScreenRepository

- `findByCinemaId(Long cinemaId)` - Tìm screens theo cinema
- `findByRoomType(String roomType)` - Tìm screens theo loại phòng
- `findByCinemaIdAndRoomType(Long cinemaId, String roomType)` - Tìm theo cinema và loại phòng
- `countByCinemaId(Long cinemaId)` - Đếm số lượng screens của cinema
- `getTotalCapacityByCinemaId(Long cinemaId)` - Tính tổng capacity của cinema

---

## Error Handling

Đã thêm 2 error codes mới vào `ErrorCode` enum:

- `CINEMA_NOT_FOUND` - Cinema không tồn tại (HTTP 404)
- `SCREEN_NOT_FOUND` - Screen không tồn tại (HTTP 404)

Services sử dụng `CustomException` với các error codes này để xử lý lỗi.

---

## Validation

### CinemaRequest
- `name` - Required, max 255 characters
- `brand` - Required, max 100 characters
- `logoUrl` - Optional, max 1000 characters
- `address` - Optional, max 500 characters
- `district` - Optional, max 100 characters
- `city` - Optional, max 100 characters
- `phone` - Optional, max 20 characters
- `website` - Optional, max 500 characters
- `description` - Optional, text

### ScreenRequest
- `cinemaId` - Required
- `name` - Required, max 100 characters
- `capacity` - Required, minimum 1
- `roomType` - Optional, max 50 characters

---

## Database Schema

Domain này ánh xạ tới 2 bảng trong database:

### Table: cinemas
- Đã tồn tại trong `init.sql`
- Lưu thông tin rạp chiếu phim
- Index trên `brand` để tối ưu query

### Table: rooms
- Đã tồn tại trong `init.sql` 
- Lưu thông tin phòng chiếu
- Foreign key tới `cinemas` với ON DELETE CASCADE
- Index trên `cinema_id`

---

## Tích hợp với các domain khác

### Quan hệ với Showtimes
- Mỗi showtime sẽ có reference tới `cinema_id` và `room_id` (screen)
- Cần cập nhật `Show` entity để thêm relationships với `Cinema` và `Screen`

### Tương lai
- Có thể extend để thêm các tính năng:
  - Quản lý giờ mở cửa/đóng cửa của cinema
  - Quản lý amenities (bãi đỗ xe, nhà hàng, v.v.)
  - Rating và reviews cho cinema
  - Khoảng cách từ vị trí người dùng
  - Ưu đãi và khuyến mãi theo cinema

---

## Testing

Các test cases cần được viết:
- Unit tests cho Services
- Integration tests cho Repositories
- API tests cho Controllers
- Validation tests cho DTOs

---

## Best Practices được áp dụng

1. **Domain-Driven Design**: Tuân theo kiến trúc domain-driven của project
2. **Separation of Concerns**: Tách biệt rõ ràng giữa các layers
3. **Factory Pattern**: Sử dụng factory methods trong entities
4. **DTO Pattern**: Sử dụng DTOs để tách biệt internal model và API response
5. **Repository Pattern**: Sử dụng Spring Data JPA repositories
6. **Exception Handling**: Sử dụng custom exceptions với error codes
7. **Validation**: Sử dụng Jakarta Validation annotations
8. **Immutability**: DTOs là immutable với Lombok builders
9. **Lazy Loading**: Sử dụng FetchType.LAZY cho relationships
10. **Code Formatting**: Tuân theo Spotless code style của project

---

## Build Status

✅ Build successful
✅ Code formatted with Spotless
✅ All files compiled without errors