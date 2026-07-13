# System Analysis

# Phân tích hệ thống

---

# 1. Mục tiêu

Tài liệu này phân tích các bài toán cần giải quyết trong hệ thống đặt vé xem phim trực tuyến trước khi tiến hành thiết kế và phát triển.

Mục tiêu là xác định các vấn đề có thể xảy ra trong quá trình đặt vé và đề xuất hướng giải quyết ở mức phân tích, chưa đi vào chi tiết triển khai kỹ thuật.

---

# 2. Quy trình nghiệp vụ

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
Giữ ghế tạm thời
    │
    ▼
Thanh toán
    │
 ┌──┴────────┐
 │           │
Thành công  Thất bại
 │           │
 ▼           ▼
Xuất vé   Giải phóng ghế
```

---

# 3. Các bài toán cần giải quyết

## 3.1 Chọn ghế đồng thời

### Mô tả

Nhiều người dùng có thể cùng chọn một ghế trong cùng một thời điểm.

Nếu hệ thống không kiểm soát tốt sẽ xảy ra tình trạng:

* Hai người cùng thanh toán một ghế.
* Dữ liệu không nhất quán.
* Phải hoàn tiền cho khách hàng.

### Mục tiêu

Đảm bảo tại một thời điểm chỉ có một người được phép giữ một ghế.

---

## 3.2 Giữ ghế trước khi thanh toán

### Mô tả

Sau khi chọn ghế, người dùng cần thời gian để thanh toán.

Nếu không giữ ghế tạm thời:

* Người khác vẫn có thể chọn ghế đó.
* Dễ xảy ra tranh chấp.

Ngược lại, nếu giữ ghế quá lâu sẽ làm giảm số lượng ghế còn khả dụng.

### Mục tiêu

Cho phép giữ ghế trong một khoảng thời gian giới hạn và tự động giải phóng khi hết thời gian.

---

## 3.3 Thanh toán lặp

### Mô tả

Người dùng có thể:

* Nhấn nút thanh toán nhiều lần.
* Làm mới trang.
* Gửi lại yêu cầu.

Điều này có thể tạo nhiều đơn hàng cho cùng một giao dịch.

### Mục tiêu

Một giao dịch chỉ được tạo một đơn đặt vé.

---

## 3.4 Callback từ cổng thanh toán

### Mô tả

Cổng thanh toán có thể gửi callback nhiều lần để đảm bảo hệ thống nhận được kết quả.

Nếu xử lý không đúng:

* Có thể tạo nhiều vé.
* Có thể cập nhật trạng thái nhiều lần.

### Mục tiêu

Mỗi giao dịch chỉ được xử lý thành công một lần.

---

## 3.5 Tính nhất quán dữ liệu

### Mô tả

Quá trình đặt vé bao gồm nhiều bước:

* Giữ ghế
* Thanh toán
* Tạo vé
* Lưu lịch sử

Nếu một bước thất bại, dữ liệu có thể không đồng nhất.

### Mục tiêu

Đảm bảo dữ liệu luôn ở trạng thái hợp lệ sau mỗi giao dịch.

---

# 4. Yêu cầu hệ thống

Hệ thống cần đáp ứng các yêu cầu sau:

* Không bán trùng ghế.
* Không tạo đơn hàng trùng.
* Không tạo nhiều vé cho cùng một giao dịch.
* Cho phép nhiều người dùng truy cập đồng thời.
* Thời gian phản hồi nhanh.
* Có khả năng mở rộng khi lượng người dùng tăng.

---

# 5. Kết luận

Hệ thống đặt vé trực tuyến không chỉ là bài toán CRUD mà còn phải giải quyết các vấn đề về xử lý đồng thời, tính nhất quán dữ liệu và độ tin cậy trong quá trình thanh toán.

Các giải pháp kỹ thuật để giải quyết những bài toán này sẽ được trình bày trong các tài liệu thiết kế tiếp theo.
