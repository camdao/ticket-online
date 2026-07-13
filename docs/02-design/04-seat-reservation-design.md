# Seat Reservation Design

# Thiết kế chức năng giữ ghế

---

# 1. Mục đích

Tài liệu này mô tả thiết kế chức năng giữ ghế trong hệ thống đặt vé xem phim nhằm đảm bảo một ghế không bị nhiều người đặt cùng lúc và hỗ trợ người dùng hoàn tất thanh toán trong khoảng thời gian giới hạn.

---

# 2. Bài toán

Trong giờ cao điểm, nhiều người dùng có thể cùng chọn một ghế gần như đồng thời.

Nếu hệ thống chỉ kiểm tra trạng thái ghế trong cơ sở dữ liệu mà không có cơ chế kiểm soát phù hợp, có thể xảy ra:

* Hai người cùng giữ một ghế.
* Bán trùng ghế.
* Dữ liệu không nhất quán.

---

# 3. Mục tiêu

* Một ghế chỉ được giữ bởi một người tại một thời điểm.
* Ghế được giữ trong thời gian giới hạn.
* Tự động giải phóng ghế khi hết thời gian giữ.
* Giảm tranh chấp khi nhiều người dùng cùng đặt vé.

---

# 4. Kiến trúc

```text
Client
    │
    ▼
Booking API
    │
    ▼
Redis (Seat Reservation)
    │
    ▼
MySQL
```

Redis được sử dụng để lưu trạng thái giữ ghế tạm thời, trong khi MySQL chỉ lưu dữ liệu sau khi thanh toán thành công.

---

# 5. Luồng xử lý

```text
Người dùng chọn ghế
        │
        ▼
Kiểm tra trạng thái ghế
        │
        ▼
Ghế còn khả dụng?
        │
   ┌────┴────┐
   │         │
 Có         Không
   │         │
   ▼         ▼
Giữ ghế    Thông báo
   │
   ▼
Thiết lập thời gian giữ
   │
   ▼
Thanh toán
```

---

# 6. Quản lý thời gian giữ ghế

* Thời gian giữ ghế: 5 phút.
* Trong thời gian này, người dùng khác không thể giữ cùng ghế.
* Khi hết thời gian giữ mà chưa thanh toán, hệ thống tự động giải phóng ghế.

---

# 7. Xử lý khi thanh toán

* Thanh toán thành công:

  * Xóa thông tin giữ ghế.
  * Lưu đơn đặt vé vào MySQL.
  * Cập nhật trạng thái vé.

* Thanh toán thất bại hoặc hủy:

  * Giải phóng ghế.
  * Cho phép người dùng khác đặt lại.

---

# 8. Các trường hợp đặc biệt

## Hai người cùng chọn một ghế

Hệ thống chỉ cho phép một yêu cầu giữ ghế thành công. Các yêu cầu còn lại sẽ nhận thông báo ghế không còn khả dụng.

---

## Hết thời gian giữ ghế

Nếu người dùng không hoàn tất thanh toán trong thời gian quy định, ghế sẽ được mở lại để người dùng khác có thể đặt.

---

## Redis không khả dụng

Trong trường hợp Redis gặp sự cố, hệ thống cần có cơ chế xử lý phù hợp để tránh bán trùng ghế và đảm bảo tính nhất quán dữ liệu.

---

# 9. Thiết kế liên quan

* System Design
* Database Design
* Payment Design

10. Redis Key Design

11. Lua Script (nếu dùng)

12. Transaction Flow

13. Failure Recovery

14. Sequence Diagram

15. Trade-offs