# Đánh Giá Rủi Ro

**Cập nhật lần cuối:** 2026-08-04  
**Xem xét tiếp theo:** 2026-09-04

## Tổng quan

Tài liệu này xác định và đánh giá các rủi ro trong hệ thống đặt vé xem phim. Mỗi rủi ro được chấm điểm theo:

```
Điểm Rủi ro = Mức độ nghiêm trọng × Khả năng xảy ra
```

**Mức độ rủi ro:**
- 🔴 **NGHIÊM TRỌNG** (17-25): Cần hành động ngay lập tức
- ⚠️ **CAO** (10-16): Xử lý trong sprint
- 🟡 **TRUNG BÌNH** (5-9): Lập kế hoạch cho quý tiếp theo
- 🟢 **THẤP** (1-4): Giám sát và cải thiện

---

## Ma trận Tóm tắt Rủi ro

| ID | Rủi ro | Mức độ | Khả năng | Điểm | Ưu tiên |
|----|------|----------|------------|-------|----------|
| **T-1** | **Lỗi Payment Callback** | **5** | **4** | **20** | 🔴 **NGHIÊM TRỌNG** |
| **T-2** | **Redis Single Point of Failure** | **5** | **3** | **15** | ⚠️ **CAO** |
| **O-1** | **Downtime khi Deploy** | **4** | **4** | **16** | ⚠️ **CAO** |
| O-2 | Thiếu Giám sát | 3 | 5 | 15 | ⚠️ CAO |
| S-1 | Lạm dụng Giữ ghế | 4 | 3 | 12 | ⚠️ CAO |
| B-1 | Vấn đề Thời gian Thanh toán | 4 | 3 | 12 | ⚠️ CAO |
| B-3 | Kịch bản Bán vượt số lượng | 5 | 2 | 10 | ⚠️ CAO |
| O-3 | Downtime Tích hợp VNPay | 5 | 2 | 10 | ⚠️ CAO |
| T-3 | Trường hợp ngoại lệ Hết hạn TTL | 3 | 3 | 9 | 🟡 TRUNG BÌNH |
| T-6 | Race Condition trong Booking Transaction | 3 | 3 | 9 | 🟡 TRUNG BÌNH |
| T-5 | Cạn kiệt Connection Pool Database | 4 | 2 | 8 | 🟡 TRUNG BÌNH |
| T-7 | IP Spoofing trong X-Forwarded-For | 4 | 2 | 8 | 🟡 TRUNG BÌNH |
| B-5 | Không có Circuit Breaker cho VNPay | 4 | 2 | 8 | 🟡 TRUNG BÌNH |
| S-5 | Thiếu Rate Limiting API | 4 | 2 | 8 | 🟡 TRUNG BÌNH |
| B-4 | Vấn đề Lọc Vị trí Rạp | 2 | 3 | 6 | 🟡 TRUNG BÌNH |
| B-6 | Scheduler Không có Distributed Lock | 3 | 2 | 6 | 🟡 TRUNG BÌNH |
| T-8 | Thiếu Connection Pool Monitoring | 3 | 2 | 6 | 🟡 TRUNG BÌNH |
| S-4 | SQL Injection & XSS | 5 | 1 | 5 | 🟢 THẤP |
| O-4 | Sao lưu & Khôi phục Dữ liệu | 5 | 1 | 5 | 🟢 THẤP |
| S-6 | JWT Token Security | 3 | 1 | 3 | 🟢 THẤP |
| B-7 | Email/Phone Validation Bypass | 2 | 1 | 2 | 🟢 THẤP |
| **R-1** | **Redis Failure không có Fallback** | **5** | **3** | **15** | ⚠️ **CAO** |
| R-2 | Data Inconsistency sau Payment Success | 4 | 3 | 12 | ⚠️ CAO |
| R-3 | Mất dữ liệu khi Redis Restart | 4 | 3 | 12 | ⚠️ CAO |
| R-4 | Thiếu Pre-warm Strategy cho Hot Shows | 3 | 4 | 12 | ⚠️ CAO |

---

## Rủi ro Nghiêm trọng

### T-1. Lỗi xử lý Payment Return 🔴

**Điểm Rủi ro:** 20 (Mức độ: 5 | Khả năng: 4)

**Vấn đề:**

Hệ thống sử dụng **VNPay Return URL** để cập nhật trạng thái thanh toán. Nếu người dùng đóng trình duyệt, mất kết nối mạng hoặc quá trình xử lý Return thất bại do lỗi ứng dụng, trạng thái thanh toán có thể không được cập nhật mặc dù giao dịch đã thành công tại VNPay.

**Tham chiếu Code:**
```java
// PaymentService.java
@Transactional
public void handleVnpayReturn(Map<String, String> params) {
    // Exception trong quá trình xử lý có thể làm transaction rollback,
    // khiến booking và payment không được cập nhật.
}
```

**Tác động:**

- Người dùng đã thanh toán thành công nhưng booking vẫn ở trạng thái `PENDING`
- Khách hàng không nhận được vé
- Dữ liệu giữa VNPay và hệ thống không nhất quán
- Cần kiểm tra và xử lý thủ công

**Giảm thiểu:**

- [ ] Dài hạn: Triển khai VNPay IPN để cập nhật trạng thái thanh toán độc lập với trình duyệt người dùng
- [ ] Ngắn hạn: Ghi log đầy đủ các giao dịch và lỗi xử lý Return để hỗ trợ điều tra
- [ ] Ngắn hạn: Hiển thị hướng dẫn để người dùng liên hệ hỗ trợ khi đã thanh toán nhưng chưa nhận được vé
---

## Rủi ro Ưu tiên Cao

### T-2. Redis Single Point of Failure ⚠️

**Điểm Rủi ro:** 15 (Mức độ: 5 | Khả năng: 3)

**Vấn đề:**
Redis xử lý tất cả thao tác giữ ghế. Nếu Redis crash:
- Người dùng không thể giữ ghế hoặc tạo booking mới
- Ghế đang giữ bị mất sau khi restart
- Hệ thống hoàn toàn không khả dụng cho đặt vé

**Tham chiếu Code:** `global/util/RedisSeatScripts.java`

**Tác động:**
- Hệ thống đặt vé ngừng hoạt động hoàn toàn
- Mất doanh thu trong thời gian downtime
- Mất dữ liệu cho các ghế đang giữ

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Bật Redis persistence (AOF hoặc RDB)
- [ ] **Ngay lập tức:** Thiết lập health checks và cảnh báo Redis
- [ ] **Ngắn hạn:** Deploy Redis Sentinel cho automatic failover
- [ ] **Ngắn hạn:** Giám sát metrics Redis (CPU, memory, latency)
- [ ] **Dài hạn:** Chuyển sang Redis Cluster hoặc managed service (AWS ElastiCache Multi-AZ)
- [ ] **Dài hạn:** Thiết kế lại kiến trúc với Database làm source of truth, Redis chỉ là cache

---

### O-1. Downtime khi Deploy ⚠️

**Điểm Rủi ro:** 16 (Mức độ: 4 | Khả năng: 4)

**Vấn đề:**
Restart ứng dụng gây ra:
- Reset kết nối Redis
- Mất ghế đang giữ (nếu không persist)
- Payment callbacks đang xử lý có thể thất bại
- Phiên người dùng đang hoạt động bị gián đoạn

**Tác động:**
- Gián đoạn dịch vụ trong khi deploy
- Mất bookings và doanh thu
- Tăng đột biến support tickets

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Triển khai zero-downtime deployment (rolling updates)
- [ ] **Ngay lập tức:** Thêm health check endpoint cho load balancer
- [ ] **Ngắn hạn:** Graceful shutdown handler để hoàn thành các request đang xử lý
- [ ] **Dài hạn:** Chiến lược blue-green deployment

---

### O-2. Thiếu Giám sát ⚠️

**Điểm Rủi ro:** 15 (Mức độ: 3 | Khả năng: 5)

**Vấn đề:**
Thiếu giám sát quan trọng cho:
- Sức khỏe Redis và sử dụng bộ nhớ
- Tỷ lệ thành công/thất bại giữ ghế
- Độ trễ và lỗi payment callback
- Phễu chuyển đổi booking
- Tỷ lệ lỗi hệ thống

**Tác động:**
- Phát hiện sự cố quá muộn
- Phản ứng chậm với sự cố
- Không có khả năng nhìn thấy suy giảm hệ thống

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Thiết lập thu thập metrics Prometheus
- [ ] **Ngắn hạn:** Tạo Grafana dashboards cho:
  - Khả dụng và độ trễ Redis
  - Metrics phễu booking
  - Tỷ lệ thành công payment
  - Thời gian phản hồi API (p50, p95, p99)
- [ ] **Ngắn hạn:** Cấu hình cảnh báo quan trọng:
  - Redis down hoặc sử dụng bộ nhớ cao
  - Tỷ lệ lỗi payment callback > 5%
  - Tỷ lệ lỗi booking > 10%
- [ ] **Dài hạn:** Tích hợp giải pháp APM (Datadog, New Relic)

---

### S-1. Lạm dụng Giữ ghế ⚠️

**Điểm Rủi ro:** 12 (Mức độ: 4 | Khả năng: 3)

**Vấn đề:**
Người dùng độc hại có thể:
- Giữ quá nhiều ghế cho phim hot
- Không bao giờ hoàn tất thanh toán, để ghế hết hạn
- Lặp lại tấn công để từ chối dịch vụ cho người dùng hợp pháp

**Trạng thái hiện tại:**
- Quy tắc nghiệp vụ giới hạn 10 ghế mỗi lần giữ (được ghi trong thiết kế API)
- Validation có trong DTO (@Size(max = 10)) nhưng cần verify enforcement
- Không có rate limiting mỗi người dùng

**Tham chiếu Code:** `CreateBookingRequest.java:23`

**Tác động:**
- Người dùng hợp pháp không thể đặt vé
- Mất doanh thu trong giờ cao điểm
- Trải nghiệm người dùng kém

**Giảm thiểu:**
- [x] **Đã có:** Validation tối đa 10 ghế trong DTO
- [ ] **Ngay lập tức:** Rate limit: Tối đa 3 yêu cầu giữ ghế mỗi phút mỗi người dùng
- [ ] **Ngắn hạn:** Triển khai rate limiting theo IP
- [ ] **Trung hạn:** Thêm CAPTCHA sau nhiều lần giữ ghế từ cùng người dùng
- [ ] **Dài hạn:** Phát hiện bất thường cho các mẫu đặt vé đáng ngờ

---

### B-1. Vấn đề Thời gian Thanh toán ⚠️

**Điểm Rủi ro:** 12 (Mức độ: 4 | Khả năng: 3)

**Vấn đề:**
- Người dùng có 15 phút để hoàn tất thanh toán sau khi tạo booking
- Xử lý VNPay có thể mất thời gian (độ trễ mạng, chậm trễ người dùng)
- Nếu payment callback đến sau 15 phút, booking có thể đã hết hạn

**Tham chiếu Code:** `domain/payments/application/VnpayService.java:48-50`

**Tác động:**
- Người dùng thanh toán thành công nhưng bookings đã hết hạn
- Cần can thiệp thủ công để đối soát
- Khiếu nại khách hàng

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Gia hạn booking TTL lên 20 phút (đệm 5 phút)
- [ ] **Ngắn hạn:** Xử lý thanh toán muộn một cách khéo léo:
  - Nếu payment thành công sau khi hết hạn, kích hoạt lại booking nếu ghế vẫn còn
  - Nếu không, kích hoạt hoàn tiền tự động
- [ ] **Giám sát:** Theo dõi phân phối thời gian xử lý payment (p50, p95, p99)

---

### B-3. Kịch bản Bán vượt số lượng ⚠️

**Điểm Rủi ro:** 10 (Mức độ: 5 | Khả năng: 2)

**Vấn đề:**
Không nhất quán dữ liệu giữa Redis và Database có thể gây ra:
- Ghế được đánh dấu khả dụng trong Redis nhưng đã đặt trong DB (hoặc ngược lại)
- Nhiều người dùng đặt cùng một ghế
- Hỗn loạn tại rạp trong suất chiếu

**Thiết kế hiện tại:**
- Database là source of truth cho bookings đã xác nhận
- Redis giữ ghế tạm thời
- Khi payment thành công: giải phóng từ Redis, đánh dấu đã đặt trong DB

**Tác động:**
- Nhiều vé cho cùng một ghế
- Ác mộng dịch vụ khách hàng
- Chi phí hoàn tiền và bồi thường
- Vấn đề pháp lý tiềm ẩn

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Tạo job đối soát so sánh trạng thái Redis vs Database
- [ ] **Ngắn hạn:** Thêm unique constraint database trên (showtime_id, seat_id, booking_id)
- [ ] **Ngắn hạn:** Thêm transaction logging để debug
- [ ] **Dài hạn:** Triển khai kiểm tra tính nhất quán cuối cùng với cảnh báo

---

### O-3. Downtime Tích hợp VNPay ⚠️

**Điểm Rủi ro:** 10 (Mức độ: 5 | Khả năng: 2)

**Vấn đề:**
- VNPay là phương thức thanh toán duy nhất được triển khai
- Nếu dịch vụ VNPay down hoặc chậm, không có payment nào được xử lý
- Bookings bị kẹt ở trạng thái PENDING

**Tác động:**
- Không có doanh thu trong thời gian VNPay ngừng hoạt động
- Khách hàng thất vọng
- Mất cơ hội bán hàng

**Giảm thiểu:**
- [ ] **Ngắn hạn:** Triển khai giám sát health check VNPay
- [ ] **Ngắn hạn:** Hiển thị trạng thái dịch vụ cho người dùng
- [ ] **Trung hạn:** Thêm phương thức thanh toán thay thế (MOMO)
- [ ] **Trung hạn:** Hỗ trợ thanh toán tiền mặt tại rạp
- [ ] **Dài hạn:** Triển khai logic fallback phương thức thanh toán

**Tham chiếu:** `domain/payments/domain/PaymentMethod.java`

---

## Rủi ro Ưu tiên Trung bình

### T-3. Trường hợp ngoại lệ Hết hạn TTL 🟡

**Điểm Rủi ro:** 9 (Mức độ: 3 | Khả năng: 3)

**Vấn đề:**
- TTL giữ ghế = 5 phút
- TTL booking = 15 phút
- Trường hợp ngoại lệ: Người dùng tạo booking ở phút 4:59 → chỉ còn 1 giây để thanh toán
- Clock skew giữa các application servers
- Redis eviction khi bộ nhớ đầy

**Tham chiếu Code:** `global/util/RedisSeatScripts.java:21`

**Tác động:**
- Bookings hết hạn trong khi người dùng đang thanh toán
- Trải nghiệm người dùng kém
- Giao dịch bị bỏ dở

**Giảm thiểu:**
- [ ] **Ngắn hạn:** Thêm đệm 30 giây khi gia hạn TTL trong createBooking
- [ ] **Trung hạn:** Hiển thị bộ đếm thời gian cho người dùng
- [ ] **Dài hạn:** TTL động dựa trên độ phức tạp phương thức thanh toán

---

### T-6. Race Condition trong Booking Transaction 🟡

**Điểm Rủi ro:** 9 (Mức độ: 3 | Khả năng: 3)

**Vấn đề:**
Mặc dù có Redis lock cho việc giữ ghế, vẫn có khoảng thời gian ngắn giữa các bước:
1. Redis holdSeats thành công
2. Tạo Booking entity trong database
3. Exception xảy ra trước khi save

Khi exception xảy ra sau khi lock Redis nhưng trước khi save database:
- Ghế bị lock trong Redis nhưng không có booking tương ứng
- Ghế sẽ bị khóa cho đến khi TTL hết hạn (5 phút)
- Không có cơ chế rollback Redis khi transaction database thất bại

**Tham chiếu Code:**
```java
// BookingService.java:69-99
redisSeatScripts.holdSeats(...);  // Lock Redis trước
// Exception có thể xảy ra ở đây
Booking booking = Booking.createBooking(...);
booking = bookingRepository.save(booking);  // DB transaction sau
```

**Tác động:**
- Ghế bị khóa vô lý khi booking thất bại
- Giảm số ghế khả dụng tạm thời
- Trải nghiệm người dùng kém

**Giảm thiểu:**
- [ ] **Ngắn hạn:** Thêm try-catch để release Redis locks khi booking creation thất bại
- [ ] **Ngắn hạn:** Thêm logging để track failed bookings với Redis locks
- [ ] **Trung hạn:** Triển khai compensating transaction pattern
- [ ] **Dài hạn:** Sử dụng distributed transaction pattern (Saga pattern)

---

### T-5. Cạn kiệt Connection Pool Database 🟡

**Điểm Rủi ro:** 8 (Mức độ: 4 | Khả năng: 2)

**Vấn đề:**
- Lưu lượng cao có thể cạn kiệt database connection pool
- Long-running transactions giữ connections
- Deadlocks trong bookings đồng thời
- HikariCP maximum-pool-size = 20 có thể không đủ cho high traffic

**Tham chiếu Code:** `application-datasource.yml:10`

**Tác động:**
- Ứng dụng không phản hồi
- Người dùng không thể đặt vé
- Lỗi dây chuyền

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Xem xét và điều chỉnh cài đặt HikariCP
- [ ] **Ngắn hạn:** Thêm metrics và cảnh báo connection pool
- [ ] **Ngắn hạn:** Tối ưu các queries chạy lâu
- [ ] **Dài hạn:** Triển khai read replicas cho các operations đọc nhiều

---

### T-7. IP Spoofing trong X-Forwarded-For 🟡

**Điểm Rủi ro:** 8 (Mức độ: 4 | Khả năng: 2)

**Vấn đề:**
Hệ thống lấy IP từ X-Forwarded-For header để gửi đến VNPay:
- Header này có thể bị forge bởi client
- Không có validation IP format
- Không kiểm tra trusted proxy

**Tham chiếu Code:**
```java
// BookingService.java:242-254 & PaymentService.java:207-219
private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();  // Không validate
    }
    return request.getRemoteAddr();
}
```

**Tác động:**
- Gửi IP không hợp lệ đến VNPay
- Potential bypass rate limiting dựa trên IP
- VNPay có thể reject payment với invalid IP

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Validate IP format trước khi sử dụng
- [ ] **Ngắn hạn:** Chỉ trust X-Forwarded-For khi request đến từ known proxy
- [ ] **Ngắn hạn:** Thêm whitelist trusted proxy IPs
- [ ] **Trung hạn:** Sử dụng X-Real-IP thay vì X-Forwarded-For nếu có load balancer

---

### B-5. Không có Circuit Breaker cho VNPay 🟡

**Điểm Rủi ro:** 8 (Mức độ: 4 | Khả năng: 2)

**Vấn đề:**
Khi VNPay API chậm hoặc timeout:
- Mỗi booking request sẽ đợi đến timeout
- Thread pool có thể cạn kiệt
- Cascading failure khi nhiều users cùng đặt vé
- Không có fallback mechanism

**Tham chiếu Code:**
```java
// BookingService.java:121-127
String paymentUrl = vnpayService.createPaymentUrl(...);  // Blocking call, no timeout handling
```

**Tác động:**
- Application become unresponsive
- Timeout cho tất cả booking requests
- Trải nghiệm người dùng rất kém

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Set connection timeout và read timeout cho VNPay HTTP client
- [ ] **Ngắn hạn:** Triển khai Resilience4j Circuit Breaker
- [ ] **Ngắn hạn:** Thêm request timeout cho booking creation
- [ ] **Trung hạn:** Hiển thị status page khi VNPay có vấn đề
- [ ] **Dài hạn:** Queue-based payment processing thay vì synchronous

---

### S-5. Thiếu Rate Limiting API 🟡

**Điểm Rủi ro:** 8 (Mức độ: 4 | Khả năng: 2)

**Vấn đề:**
Không có rate limiting cho các API endpoints:
- Booking creation endpoint
- Payment initiation endpoint
- Login/Register endpoints
- Potential DDoS attacks

**Tác động:**
- Hệ thống có thể bị overwhelm bởi requests
- Database và Redis connection pool exhaustion
- Legitimate users không thể truy cập
- Tăng chi phí infrastructure

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Triển khai Spring Rate Limiter cho critical endpoints
- [ ] **Ngắn hạn:** Rate limit per user: 10 requests/minute cho booking
- [ ] **Ngắn hạn:** Rate limit per IP: 100 requests/minute cho tất cả endpoints
- [ ] **Trung hạn:** Sử dụng Redis để store rate limit counters
- [ ] **Dài hạn:** Triển khai API Gateway với built-in rate limiting (Kong, AWS API Gateway)

---

### B-4. Vấn đề Lọc Vị trí Rạp 🟡

**Điểm Rủi ro:** 6 (Mức độ: 2 | Khả năng: 3)

**Vấn đề:**
- API lọc rạp theo thành phố/quận
- Dữ liệu không nhất quán (lỗi chính tả, vấn đề mã hóa) gây lỗi tìm kiếm
- Người dùng không thể tìm rạp trong khu vực của họ

**Tác động:**
- Trải nghiệm người dùng kém
- Mất bookings

**Giảm thiểu:**
- [ ] **Ngắn hạn:** Chuẩn hóa tên thành phố/quận trong database
- [ ] **Ngắn hạn:** Triển khai fuzzy text matching
- [ ] **Dài hạn:** Sử dụng tọa độ địa lý thay vì vị trí dựa trên văn bản
- [ ] **Dài hạn:** Triển khai tìm kiếm dựa trên bán kính

---

### B-6. Scheduler Không có Distributed Lock 🟡

**Điểm Rủi ro:** 6 (Mức độ: 3 | Khả năng: 2)

**Vấn đề:**
Booking expiration scheduler chạy mỗi phút:
- Trong môi trường multi-instance, tất cả instances chạy scheduler
- Duplicate processing của expired bookings
- Race condition khi nhiều instances cùng expire một booking
- Lãng phí tài nguyên database

**Tham chiếu Code:**
```java
// BookingScheduler.java:15-16
@Scheduled(fixedRate = 60000)  // Chạy trên mọi instance
public void expireOldBookings() {
```

**Tác động:**
- Duplicate work và database load không cần thiết
- Potential race conditions
- Log spam
- Không scale horizontally hiệu quả

**Giảm thiểu:**
- [ ] **Ngắn hạn:** Triển khai ShedLock cho distributed locking
- [ ] **Ngắn hạn:** Chỉ một instance được phép chạy scheduler tại một thời điểm
- [ ] **Trung hạn:** Sử dụng Redis lock cho scheduler coordination
- [ ] **Dài hạn:** Migrate sang job scheduler riêng biệt (Quartz cluster mode)

---

### T-8. Thiếu Connection Pool Monitoring 🟡

**Điểm Rủi ro:** 6 (Mức độ: 3 | Khả năng: 2)

**Vấn đề:**
HikariCP configuration:
- maximum-pool-size: 20
- Không có metrics/monitoring cho pool usage
- Không có cảnh báo khi pool gần đầy
- maxLifetime có thể gây connection churn

**Tham chiếu Code:**
```yaml
# application-datasource.yml:8-10
hikari:
  maxLifetime: 580000
  maximum-pool-size: 20
```

**Tác động:**
- Connection pool exhaustion không được phát hiện sớm
- Sudden spike trong requests có thể gây pool starvation
- Không có visibility vào database connection health

**Giảm thiểu:**
- [ ] **Ngay lập tức:** Enable HikariCP metrics (Micrometer/Prometheus)
- [ ] **Ngắn hạn:** Set up alerts cho pool usage > 80%
- [ ] **Ngắn hạn:** Thêm dashboard hiển thị:
  - Active connections
  - Idle connections
  - Connection wait time
  - Connection creation time
- [ ] **Trung hạn:** Load test để xác định optimal pool size
- [ ] **Dài hạn:** Auto-scaling connection pool dựa trên load

---

## Rủi ro Fallback và Disaster Recovery

### R-1. Redis Failure không có Fallback ⚠️

**Điểm Rủi ro:** 15 (Mức độ: 5 | Khả năng: 3)

**Vấn đề:**

Khi Redis down, toàn bộ chức năng đặt vé ngừng hoạt động:
- `holdSeats()` throw exception
- Không có graceful degradation
- Không có fallback mechanism sang database
- Application hoàn toàn không khả dụng cho booking

**Tham chiếu Code:**
```java
// global/util/RedisSeatScripts.java
public HoldSeatsResponse holdSeats(...) {
    // Throw exception nếu Redis unavailable
    // Không có fallback logic
}
```

**Tác động:**
- Mất 100% doanh thu từ đặt vé online
- Tăng đột biến support tickets
- Khách hàng chuyển sang đối thủ cạnh tranh
- Thiệt hại danh tiếng thương hiệu

**Giảm thiểu:**

- [ ] **Ngay lập tức:** Implement try-catch với custom error message:
  ```java
  try {
      redisSeatScripts.holdSeats(...);
  } catch (RedisConnectionException e) {
      log.error("Redis unavailable, seat reservation disabled");
      throw new CustomException(ErrorCode.SERVICE_TEMPORARILY_UNAVAILABLE);
  }
  ```

- [ ] **Ngắn hạn:** Deploy Redis Sentinel cho auto-failover
- [ ] **Ngắn hạn:** Enable Redis persistence:
  - AOF (Append Only File) với appendfsync everysec
  - RDB snapshots mỗi 5 phút
- [ ] **Trung hạn:** Implement health checks:
  - Redis ping mỗi 10 giây
  - Alert khi Redis down > 30 giây
- [ ] **Dài hạn:** Redis Cluster cho horizontal scaling
- [ ] **Dài hạn:** Managed Redis service (AWS ElastiCache Multi-AZ, Redis Cloud)

**Backup Strategy:**
```yaml
# Redis Configuration
save 300 10        # Save if 10 keys changed in 5 minutes
save 60 10000      # Save if 10000 keys changed in 1 minute
appendonly yes
appendfsync everysec
```

---

### R-2. Data Inconsistency sau Payment Success ⚠️

**Điểm Rủi ro:** 12 (Mức độ: 4 | Khả năng: 3)

**Vấn đề:**

**Scenario:** Payment thành công nhưng `confirmSeats()` failed do network issue hoặc application error.

**Result:**
- Payment status = COMPLETED trong database
- Seats vẫn "held" trong Redis (sẽ expire sau 10 phút)
- Booking state = PENDING (chưa confirmed)
- User đã trả tiền nhưng không có vé

**Flow thất bại:**
```
1. User complete payment → VNPay callback
2. PaymentService.updateStatus(COMPLETED) → SUCCESS ✓
3. Network hiccup hoặc application exception
4. confirmSeats() FAILED ✗
5. booking.confirm() NEVER CALLED ✗
```

**Tác động:**
- User không nhận được vé dù đã thanh toán
- Cần manual intervention để resolve
- Tăng support workload
- Risk of duplicate bookings nếu user retry

**Giảm thiểu:**

- [ ] **Ngay lập tức:** Thêm extensive logging:
  ```java
  log.info("Payment {} completed, attempting to confirm seats", paymentId);
  try {
      confirmSeats();
      log.info("Seats confirmed for payment {}", paymentId);
  } catch (Exception e) {
      log.error("CRITICAL: Seats confirmation failed for payment {}", paymentId, e);
      // Alert ops team
  }
  ```

- [ ] **Ngắn hạn:** Implement recovery job chạy mỗi 1 phút:
  ```java
  @Scheduled(fixedRate = 60000)
  public void reconcilePayments() {
      // Find payments: status=COMPLETED AND booking.status=PENDING
      List<Payment> orphanedPayments = paymentRepository
          .findByStatusAndBookingStatus(
              PaymentStatus.COMPLETED,
              BookingStatus.PENDING
          );
      
      for (Payment payment : orphanedPayments) {
          try {
              // Retry confirmSeats() và booking.confirm()
              recoverBooking(payment);
              log.info("Recovered booking for payment {}", payment.getId());
          } catch (Exception e) {
              log.error("Recovery failed for payment {}", payment.getId(), e);
              // Alert for manual intervention
          }
      }
  }
  ```

- [ ] **Trung hạn:** Implement idempotency:
  - `confirmSeats()` should be idempotent
  - Check if seats already confirmed trước khi retry
  - Prevent duplicate confirmations

- [ ] **Dài hạn:** Distributed transaction pattern:
  - Saga pattern cho multi-step booking process
  - Compensating transactions cho rollback
  - Event sourcing để track tất cả state changes

---

### R-3. Mất dữ liệu khi Redis Restart ⚠️

**Điểm Rủi ro:** 12 (Mức độ: 4 | Khả năng: 3)

**Vấn đề:**

Khi Redis restart (planned maintenance, crash, hoặc reboot):
- Tất cả seat holds bị mất nếu không có persistence
- Seats đã booked trong DB có thể được hold lại bởi user khác
- Race condition: Nhiều người có thể book cùng ghế đã sold

**Scenario:**
```
Before restart:
- Redis: Seat A1 held by User X
- DB: Seat A1 booked and confirmed

After restart:
- Redis: Empty (nếu không có persistence)
- DB: Seat A1 still booked
- Problem: User Y có thể hold Seat A1 vì Redis không biết nó đã sold
```

**So sánh các giải pháp:**

| Giải pháp | Độ an toàn | Hiệu năng | Độ phức tạp | Production-ready |
|-----------|------------|-----------|-------------|------------------|
| Chỉ Redis Lock | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ | ❌ Không |
| Redis HA (Sentinel/Cluster) | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ Có |
| Redis + AOF | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ✅ Có |
| Hold trong Database | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ | ⚠️ Ít dùng cho hệ thống lớn |
| Booking PENDING trong Database | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ Rất phổ biến |
| Transaction + SELECT FOR UPDATE | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ✅ Bắt buộc |
| Unique Constraint | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ | ✅ Bắt buộc |
| Refund (Compensation) | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ Bắt buộc |

**Tác động:**
- Double booking cùng ghế
- Customer service nightmare
- Refund costs và compensation
- Legal liability

**Giảm thiểu:**

**Recommendation: Sử dụng kết hợp (Defense in Depth)**

1. **Redis cho temporary holds** (hiệu năng cao):
   ```yaml
   # Redis persistence
   appendonly yes
   appendfsync everysec
   save 300 10
   ```

2. **Database với unique constraint** (đảm bảo consistency):
   ```sql
   -- Prevent duplicate bookings
   ALTER TABLE booking_seats 
   ADD CONSTRAINT uk_showtime_seat_booking 
   UNIQUE (showtime_id, seat_id, booking_id);
   ```

3. **Redis HA/Sentinel** (high availability):
   - [ ] **Ngắn hạn:** Deploy Redis Sentinel (minimum 3 nodes)
   - [ ] **Trung hạn:** Automatic failover < 30 seconds
   - [ ] **Dài hạn:** Redis Cluster cho scaling

4. **Refund mechanism** (compensation cho edge cases):
   ```java
   @Transactional
   public void handleOverbooking(Booking booking) {
       // Detect duplicate booking
       if (isDuplicate(booking)) {
           // Automatic refund
           Payment payment = booking.getPayment();
           payment.refund();
           
           // Notify customer
           notificationService.sendOverbookingCompensation(booking);
           
           // Log for analysis
           log.error("OVERBOOKING detected for booking {}", booking.getId());
       }
   }
   ```

- [ ] **Ngay lập tức:** Enable Redis AOF persistence
- [ ] **Ngay lập tức:** Add unique constraint vào database
- [ ] **Ngắn hạn:** Implement validation trước khi confirm:
  ```java
  public void confirmSeats() {
      // Double check trong DB trước khi confirm
      if (isSeatAlreadyBooked()) {
          throw new SeatAlreadyBookedException();
      }
      // Proceed with confirmation
  }
  ```
- [ ] **Trung hạn:** Deploy Redis Sentinel
- [ ] **Dài hạn:** Implement automatic refund flow cho overbooking cases

---

### R-4. Thiếu Pre-warm Strategy cho Hot Shows ⚠️

**Điểm Rủi ro:** 12 (Mức độ: 3 | Khả năng: 4)

**Vấn đề:**

**Khi nào cần pre-warm:**

| Kiến trúc | Pre-warm | Cache được cập nhật bằng |
|-----------|----------|--------------------------|
| Cache Aside | ✅ Cần | Cache Aside (DEL + rebuild) |
| Event-driven | ✅ Cần | Event (Kafka/RabbitMQ...) |

**Show ít người xem:**
```
User Request → Database (Sold Seats) + Redis (Lock Check) → Return
```
- Không cần cache vì DB chịu được
- Tránh overhead của cache synchronization

**Show rất hot (Marvel premiere, concert ticket):**
```
Before pre-warm: 
- 10,000 concurrent users
- 10,000 database queries
- DB overload, slow response

After pre-warm:
- 10,000 concurrent users  
- Redis cache hit
- Fast response, DB protected
```

**Vấn đề hiện tại:**
- Không có pre-warm strategy
- Cache Aside pattern: Cache miss → DB query → Rebuild cache
- Hot shows gây DB query storm khi cache empty
- Cold start problem sau Redis restart

**Tác động:**
- Database overload cho hot shows
- Slow response time (timeout)
- Poor user experience
- Mất bookings trong golden minutes đầu

**Giảm thiểu:**

- [ ] **Ngay lập tức:** Implement cache warming job:
  ```java
  @Scheduled(cron = "0 */30 * * * *")  // Every 30 minutes
  public void warmupPopularShowtimes() {
      // Get hot shows (high demand, upcoming release)
      List<Showtime> hotShowtimes = showtimeRepository
          .findUpcomingPopularShows(Instant.now());
      
      for (Showtime showtime : hotShowtimes) {
          try {
              // Pre-load vào Redis
              warmupShowtimeCache(showtime.getId());
              log.info("Warmed up cache for showtime {}", showtime.getId());
          } catch (Exception e) {
              log.error("Cache warmup failed for showtime {}", showtime.getId(), e);
          }
      }
  }
  
  private void warmupShowtimeCache(Long showtimeId) {
      // Load sold seats từ DB
      List<Seat> soldSeats = getSoldSeats(showtimeId);
      
      // Build Redis seat map
      String key = "showtime:" + showtimeId + ":seats";
      for (Seat seat : soldSeats) {
          redisTemplate.opsForHash()
              .put(key, seat.getId(), "BOOKED");
      }
      
      // Set TTL
      redisTemplate.expire(key, Duration.ofHours(2));
  }
  ```

- [ ] **Ngắn hạn:** Identify hot shows criteria:
  - New release trong 3 ngày đầu
  - Show có > 80% seats booked
  - Show với historical high demand

- [ ] **Ngắn hạn:** Manual pre-warm trigger:
  ```java
  @PostMapping("/admin/cache/warmup/{showtimeId}")
  public ResponseEntity<?> manualWarmup(@PathVariable Long showtimeId) {
      warmupShowtimeCache(showtimeId);
      return ResponseEntity.ok("Cache warmed up");
  }
  ```

- [ ] **Trung hạn:** Chiến lược theo traffic:
  - **Low traffic shows:** Cache Aside (lazy loading)
  - **High traffic shows:** Pre-warm + Event-driven updates

- [ ] **Trung hạn:** Event-driven cache updates:
  ```java
  @TransactionalEventListener
  public void onBookingConfirmed(BookingConfirmedEvent event) {
      // Update Redis cache immediately
      updateSeatCache(event.getShowtimeId(), event.getSeatIds());
  }
  ```

- [ ] **Dài hạn:** Predictive pre-warming:
  - Machine learning để predict hot shows
  - Auto pre-warm based on historical patterns
  - Dynamic TTL dựa trên demand

- [ ] **Dài hạn:** Implement cache invalidation strategy:
  ```java
  // When payment completes → Update cache
  Payment → Update DB → Invalidate/Update Redis Cache
  
  // Đánh đổi: Tăng complexity để giảm hàng chục nghìn query vào DB
  ```

**Monitoring:**
- [ ] Track cache hit/miss ratio per showtime
- [ ] Alert when cache miss > 20% cho hot shows
- [ ] Dashboard hiển thị cache warmup status

---

## Rủi ro Thấp
