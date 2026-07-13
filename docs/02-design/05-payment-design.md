# Payment Design

# Thiết kế chức năng thanh toán

---

# 1. Mục đích

Tài liệu này mô tả thiết kế chức năng thanh toán của hệ thống Đặt Vé Xem Phim Trực Tuyến, bao gồm quy trình thanh toán, xử lý kết quả giao dịch và các cơ chế đảm bảo tính nhất quán dữ liệu.

---

# 2. Phạm vi

Chức năng thanh toán bao gồm:

* Khởi tạo giao dịch thanh toán.
* Chuyển hướng đến cổng thanh toán.
* Tiếp nhận kết quả thanh toán.
* Xác nhận đơn đặt vé.
* Xử lý các trường hợp thanh toán thất bại hoặc gửi kết quả nhiều lần.

---

# 3. Kiến trúc

```text
Client
    │
    ▼
Payment API
    │
    ▼
Payment Gateway
    │
    ▼
Payment Callback
    │
    ▼
Booking Service
    │
    ▼
MySQL
```

---

# 4. Quy trình thanh toán

```text
Người dùng
      │
      ▼
Khởi tạo thanh toán
      │
      ▼
Chuyển đến cổng thanh toán
      │
      ▼
Người dùng thanh toán
      │
      ▼
Cổng thanh toán gửi Callback
      │
      ▼
Xác thực giao dịch
      │
 ┌────┴────┐
 │         │
Hợp lệ   Không hợp lệ
 │         │
 ▼         ▼
Xác nhận   Từ chối
 │
 ▼
Tạo vé
```

---

# 5. Luồng xử lý

## Bước 1

Người dùng chọn phương thức thanh toán.

---

## Bước 2

Hệ thống tạo yêu cầu thanh toán và chuyển hướng người dùng đến cổng thanh toán.

---

## Bước 3

Sau khi giao dịch hoàn tất, cổng thanh toán gửi kết quả về hệ thống thông qua Callback.

---

## Bước 4

Hệ thống xác thực thông tin giao dịch.

---

## Bước 5

Nếu giao dịch hợp lệ:

* Xác nhận thanh toán.
* Tạo đơn đặt vé.
* Cập nhật trạng thái thanh toán.
* Giải phóng dữ liệu giữ ghế tạm thời.

---

# 6. Các trường hợp cần xử lý

## Thanh toán thành công

* Lưu thông tin giao dịch.
* Tạo vé điện tử.
* Hoàn tất đơn đặt vé.

---

## Thanh toán thất bại

* Không tạo đơn đặt vé.
* Giải phóng ghế đang giữ.
* Thông báo cho người dùng.

---

## Người dùng hủy thanh toán

* Không tạo đơn.
* Giải phóng ghế.
* Cho phép đặt lại.

---

## Callback gửi nhiều lần

Hệ thống chỉ xử lý thành công một lần cho mỗi giao dịch nhằm tránh tạo nhiều đơn đặt vé.

---

# 7. Yêu cầu thiết kế

* Một giao dịch chỉ tương ứng với một đơn đặt vé.
* Không tạo đơn hàng trùng.
* Không tạo nhiều vé cho cùng một giao dịch.
* Đảm bảo tính nhất quán giữa thanh toán và đơn đặt vé.

---

# 8. Các tình huống đặc biệt

## Người dùng đóng trình duyệt

Nếu giao dịch đã hoàn tất, hệ thống vẫn xử lý kết quả dựa trên Callback từ cổng thanh toán.

---

## Callback đến chậm

Hệ thống vẫn tiếp nhận và xử lý nếu giao dịch còn hợp lệ.

---

## Callback không hợp lệ

Hệ thống từ chối xử lý và ghi nhận nhật ký để phục vụ việc kiểm tra.

---

# 9. Tài liệu liên quan

* System Design
* Database Design
* API Design
* Seat Reservation Design

10. Sequence Diagram
11. Idempotency Design
12. Transaction Design
13. Error Handling
14. Retry Strategy
15. Trade-offs