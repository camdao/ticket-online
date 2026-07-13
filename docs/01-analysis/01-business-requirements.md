# Business Requirements Document (BRD)

# Hệ thống Đặt Vé Xem Phim Trực Tuyến

---

# 1. Giới thiệu

## 1.1 Mục đích

Tài liệu này mô tả các yêu cầu nghiệp vụ của hệ thống **Đặt Vé Xem Phim Trực Tuyến**, nhằm giúp người dùng có thể lựa chọn phim, suất chiếu, ghế ngồi và thanh toán trực tuyến một cách nhanh chóng, thuận tiện và an toàn.

Đồng thời, tài liệu là cơ sở để phân tích, thiết kế và phát triển hệ thống.

---

## 1.2 Phạm vi

Hệ thống hỗ trợ người dùng:

* Đăng ký tài khoản.
* Đăng nhập.
* Xem danh sách phim.
* Xem thông tin chi tiết phim.
* Xem lịch chiếu.
* Chọn ghế.
* Giữ ghế tạm thời.
* Thanh toán trực tuyến.
* Nhận vé điện tử.
* Xem lịch sử đặt vé.

Hệ thống **không bao gồm** chức năng quản trị, quản lý phim hoặc quản lý rạp.

---

# 2. Bài toán nghiệp vụ

Việc mua vé trực tiếp tại rạp còn tồn tại nhiều hạn chế:

* Người dùng phải đến trực tiếp để mua vé.
* Mất thời gian xếp hàng.
* Nhiều người có thể cùng chọn một ghế trong thời điểm cao điểm.
* Người dùng không biết tình trạng ghế theo thời gian thực.
* Có thể phát sinh thanh toán lặp khi người dùng nhấn nhiều lần hoặc cổng thanh toán gửi callback nhiều lần.

Hệ thống được xây dựng nhằm giải quyết các vấn đề trên và nâng cao trải nghiệm đặt vé trực tuyến.

---

# 3. Mục tiêu

* Cho phép người dùng đặt vé hoàn toàn trực tuyến.
* Hiển thị trạng thái ghế theo thời gian thực.
* Đảm bảo một ghế chỉ được bán cho một người.
* Hỗ trợ thanh toán trực tuyến.
* Ngăn chặn việc tạo đơn hàng trùng lặp.
* Cải thiện tốc độ phản hồi và trải nghiệm người dùng.

---

# 4. Đối tượng sử dụng

## Khách hàng

Khách hàng sử dụng hệ thống để:

* Đăng ký tài khoản.
* Đăng nhập.
* Tìm kiếm phim.
* Chọn suất chiếu.
* Chọn ghế.
* Thanh toán.
* Xem lịch sử đặt vé.

---

# 5. Chức năng nghiệp vụ

## 5.1 Quản lý tài khoản

* Đăng ký.
* Đăng nhập.
* Đăng xuất.

---

## 5.2 Tra cứu phim

* Xem danh sách phim.
* Xem phim đang chiếu.
* Tìm kiếm phim.
* Xem thông tin chi tiết.

---

## 5.3 Chọn suất chiếu

Người dùng có thể:

* Chọn rạp
* Chọn ngày chiếu.
* Chọn suất chiếu.

---

## 5.4 Đặt ghế

Người dùng có thể:

* Xem sơ đồ ghế.
* Chọn một hoặc nhiều ghế còn khả dụng.
* Xác nhận lựa chọn để chuyển sang bước thanh toán.
---

## 5.5 Thanh toán

Người dùng:

* Thanh toán trực tuyến.
* Nhận kết quả thanh toán.
* Nhận vé điện tử sau khi thanh toán thành công.

---

## 5.6 Lịch sử đặt vé

Người dùng có thể:

* Xem các vé đã mua.
* Xem trạng thái thanh toán.
* Xem thông tin vé.

---

# 6. Quy tắc nghiệp vụ

### BR-01

Một ghế chỉ được phép thuộc về một vé đã thanh toán.

---

### BR-02

Khi người dùng chọn ghế, hệ thống giữ ghế trong **05 phút**.

---

### BR-03

Nếu hết thời gian giữ ghế mà chưa thanh toán, ghế sẽ tự động được mở lại để người khác có thể đặt.

---

### BR-04

Một đơn đặt vé chỉ được xác nhận sau khi thanh toán thành công.

---

### BR-05

Nếu người dùng gửi nhiều yêu cầu thanh toán hoặc cổng thanh toán callback nhiều lần, hệ thống chỉ được tạo **một** đơn đặt vé.

---

### BR-06

Người dùng không thể chọn ghế đã được người khác giữ hoặc đã bán.

---

# 7. Yêu cầu phi chức năng

## Hiệu năng

* Phản hồi API nhanh.
* Hỗ trợ nhiều người dùng truy cập đồng thời.
* Hạn chế tình trạng nghẽn khi nhiều người cùng đặt một ghế.

---

## Tính nhất quán

* Không xảy ra tình trạng bán trùng ghế.
* Không tạo đơn hàng trùng.
* Dữ liệu thanh toán phải chính xác.

---

## Bảo mật

* Xác thực bằng JWT.
* Mật khẩu được mã hóa.
* Chỉ người đã đăng nhập mới được phép đặt vé.

---

## Khả năng mở rộng

Hệ thống có thể mở rộng để:

* Bổ sung thêm nhiều rạp.
* Bổ sung nhiều cụm rạp.
* Tăng số lượng người dùng đồng thời.

---

# 8. Tiêu chí thành công

Hệ thống được xem là đáp ứng yêu cầu khi:

* Người dùng có thể hoàn thành quy trình đặt vé trực tuyến.
* Không xảy ra bán trùng ghế.
* Không phát sinh đơn hàng trùng do thanh toán lặp.
* Trạng thái ghế được cập nhật chính xác.
* Người dùng nhận được vé sau khi thanh toán thành công.
