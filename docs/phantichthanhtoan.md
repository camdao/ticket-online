1. tạo thanh toán
![img_5.png](img_5.png)

- khi user thanh toán tạo order và gia hạn thêm thời gian thanh toán
- khi user thanh toán thành công thì cập nhật trạng thái order là đã thanh toán
2. khi thanh toán thành công hệ thông retry nhiều lần

![img_6.png](img_6.png)

- xử lý Idempotency
3. Người dùng spam createOrder
![img_7.png](img_7.png)

4.Ghế hết hạn sau khi tạo order
redis bị tắt có thể gây lỗi

![img_9.png](img_9.png)

order kiểm tra ghế

