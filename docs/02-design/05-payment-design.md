# Payment Design

# Thiết kế Hệ thống Thanh Toán

---

## 1. Tổng quan

Hệ thống thanh toán xử lý luồng thanh toán cho đơn đặt vé phim, tích hợp với cổng thanh toán VNPay. Sau khi người dùng hoàn tất thanh toán trên VNPay và được chuyển về hệ thống thông qua Return URL, hệ thống dựa trên kết quả trả về để cập nhật trạng thái thanh toán và đơn đặt vé.

---

## 2. Kiến trúc

### 2.1. Luồng thanh toán (3 bước)

```
1. INITIATE (Khởi tạo thanh toán)
   User xác nhận booking → Tạo Payment (PENDING) → Nhận VNPay payment URL
   ↓
2. REDIRECT (Chuyển hướng)
   User được chuyển đến VNPay → Thực hiện thanh toán
   ↓
3. RETURN (Nhận kết quả thanh toán)
   VNPay chuyển hướng người dùng về /vnpay-return
   → Hệ thống đọc các tham số trả về
   → Cập nhật Payment và Booking
   → Hiển thị kết quả thanh toán
```

### 2.2. Payment Status (Trạng thái thanh toán)

```java
public enum PaymentStatus {
    PENDING,    // Chờ thanh toán
    SUCCESS,    // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    REFUNDED    // Đã hoàn tiền
}
```

### 2.3. Payment Method

```java
public enum PaymentMethod {
    VNPAY,  // Đã triển khai
    MOMO,   // Chưa triển khai
    CASH    // Chưa triển khai
}
```

---

## 3. Tích hợp VNPay

### 3.1. VNPay Configuration

```yaml
# application.yml
vnpay:
  tmn-code: U2BYG43H
  hash-secret: ${VNPAY_HASH_SECRET:}
  pay-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
  return-url: http://localhost:8081/api/v1/payments/vnpay-return
```

### 3.2. VNPay Payment URL Generation

Class `VnpayService.java` xử lý tạo payment URL:

```java
public String createPaymentUrl(String transactionId, long amount, String orderInfo, 
                                String returnUrl, String ipAddress)
```

**Các bước:**
1. Build payment parameters (vnp_Version, vnp_Command, vnp_TmnCode, etc.)
2. **Chuyển đổi amount:** VNPay yêu cầu `amount * 100` (VND)
3. Set thời gian hết hạn: **15 phút** từ lúc tạo
4. Sắp xếp parameters theo thứ tự alphabet
5. Tạo hash data từ parameters
6. Ký với **HMAC SHA512** sử dụng `hash-secret`
7. Append `vnp_SecureHash` vào query string
8. Return complete VNPay payment URL

### 3.3. VNPay Callback Validation

```java
public boolean validateCallback(Map<String, String> params)
```

**Logic:**
1. Lấy `vnp_SecureHash` từ params
2. Remove `vnp_SecureHash` và `vnp_SecureHashType` khỏi params
3. Rebuild hash data từ remaining params (sorted alphabetically)
4. Tính hash với **HMAC SHA512**
5. So sánh với `vnp_SecureHash` từ VNPay
6. Return `true` nếu khớp (valid), `false` nếu không khớp (invalid)

---

## 4. Quy trình thanh toán chi tiết

### 4.1. Bước 1: Initiate Payment (Khởi tạo thanh toán)

**API:** `POST /api/v1/payments`

**Request:**
```json
{
  "bookingId": 5001,
  "paymentMethod": "VNPAY"
}
```

**Xử lý trong `PaymentService.initiatePayment()`:**

```java
1. Validate booking:
   - Booking tồn tại và thuộc về user
   - Booking ở trạng thái PENDING
   - Booking chưa CONFIRMED hoặc EXPIRED

2. Idempotency check:
   - Kiểm tra xem payment đã tồn tại cho booking này chưa
   - Nếu đã tồn tại và SUCCESS → throw PAYMENT_ALREADY_COMPLETED
   - Ngăn user tạo nhiều payment cho cùng 1 booking

3. Generate transaction ID:
   - Format: PAY + last 10 digits of timestamp + 8-char UUID
   - Example: PAY1743235678a1b2c3d4

4. Extract client IP:
   - Ưu tiên: X-Forwarded-For header
   - Fallback 1: X-Real-IP header
   - Fallback 2: RemoteAddr

5. Create VNPay payment URL:
   - Order info: "Thanh toan ve phim - Booking: BK20240115001"
   - Amount: booking.getTotalAmount() (VNPay sẽ nhân 100)
   - Return URL: từ config (http://localhost:8081/api/v1/payments/vnpay-return)
   - Expiration: 15 phút

6. Save Payment entity:
   - Status: PENDING
   - TransactionId: generated ID
   - PaymentMethod: VNPAY
   - Amount: booking amount
   - PaymentUrl: VNPay URL

7. Return PaymentResponse với paymentUrl
```

**Response:**
```json
{
  "paymentId": 7001,
  "bookingId": 5001,
  "paymentMethod": "VNPAY",
  "amount": 255000,
  "status": "PENDING",
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
  "transactionId": "PAY1743235678a1b2c3d4",
  "expiresAt": "2024-01-15T14:45:00"
}
```

### 4.2. Bước 2: User thanh toán trên VNPay

1. Frontend redirect user đến `paymentUrl`
2. User nhập thông tin thanh toán trên VNPay
3. VNPay xử lý thanh toán
4. VNPay redirect user về `return-url` với các query parameters

### 4.3. Bước 3: Xử lý VNPay callback

**API:** `GET /api/v1/payments/vnpay-return`

**Query Parameters từ VNPay:**
- `vnp_TxnRef`: Transaction ID (PAY1743235678a1b2c3d4)
- `vnp_ResponseCode`: Mã kết quả ("00" = success, khác = failed)
- `vnp_SecureHash`: Chữ ký để verify
- Và nhiều params khác...

**Xử lý trong `PaymentService.handleVnpayCallback()`:**

```java
1. Validate VNPay signature:
   - Gọi vnpayService.validateCallback(params)
   - Nếu invalid → throw INVALID_PAYMENT_CALLBACK
   - Đảm bảo request đến từ VNPay (không phải attacker)

2. Extract transaction info:
   - transactionId = params.get("vnp_TxnRef")
   - responseCode = params.get("vnp_ResponseCode")

3. Find payment by transactionId:
   - Nếu không tìm thấy → throw PAYMENT_NOT_FOUND

4. *** CRITICAL IDEMPOTENCY CHECK ***:
   - if (!payment.isPending()) {
       log.info("Payment already processed");
       return; // Skip processing
   }
   - Điều này ngăn xử lý trùng lặp nếu VNPay gọi callback nhiều lần
   - Hoặc nếu user refresh trang callback

5a. Nếu responseCode == "00" (Success):
   - payment.markAsSuccess(gatewayResponse)
   - booking.confirm() → Booking status = CONFIRMED
   - releaseSeats() → Xóa seats khỏi Redis (đã book vĩnh viễn trong DB)
   - Log success

5b. Nếu responseCode != "00" (Failed):
   - payment.markAsFailed(gatewayResponse)
   - Booking vẫn PENDING → User có thể retry thanh toán
   - Log failure với responseCode

6. Handle errors:
   - Bọc trong try-catch
   - Nếu có exception → payment.markAsFailed(error message)
   - Đảm bảo payment luôn có final status
```

**Response:**
- Redirect về `/booking-success` nếu thành công
- Redirect về `/booking-error` nếu thất bại

---

## 5. Idempotency (Tính bất biến)

### 5.1. Vấn đề

- VNPay có thể gọi callback nhiều lần (retry mechanism)
- User có thể refresh trang callback
- Network issues có thể gây duplicate requests
- Nếu không xử lý đúng → payment được xử lý nhiều lần → booking confirmed nhiều lần

### 5.2. Giải pháp

**Idempotency check trong `handleVnpayCallback()` (lines 137-143):**

```java
// Prevent duplicate processing (idempotency)
if (!payment.isPending()) {
    log.info("Payment {} already processed with status: {}", 
             transactionId, payment.getStatus());
    return;
}
```

**Cách hoạt động:**
1. Khi callback lần đầu: `payment.status = PENDING` → Xử lý bình thường
2. Sau khi xử lý: `payment.status = SUCCESS` hoặc `FAILED`
3. Khi callback lần 2+: `payment.status != PENDING` → Skip processing, return immediately
4. Đảm bảo payment chỉ được xử lý **một lần duy nhất**

### 5.3. Additional idempotency

**Trong `initiatePayment()`:**
```java
paymentRepository.findByBookingId(bookingId).ifPresent(existingPayment -> {
    if (existingPayment.isSuccess()) {
        throw new CustomException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
    }
});
```

Ngăn user tạo nhiều payment cho cùng một booking đã thanh toán thành công.

---

## 6. Bảo mật

### 6.1. HMAC SHA512 Signature Verification

**Tại sao cần verify signature:**
- Đảm bảo request đến từ VNPay (không phải attacker)
- Ngăn chặn man-in-the-middle attacks
- Ngăn chặn replay attacks

**Algorithm:**
```java
private String hmacSHA512(String key, String data) {
    Mac hmac512 = Mac.getInstance("HmacSHA512");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
    hmac512.init(secretKey);
    byte[] result = hmac512.doFinal(data.getBytes(UTF_8));
    
    // Convert to hex string
    return bytesToHex(result);
}
```

**Data để hash:**
- Tất cả query parameters (trừ vnp_SecureHash và vnp_SecureHashType)
- Sorted alphabetically
- URL encoded
- Format: `key1=value1&key2=value2&...`

**Secret key:**
- Lưu trong environment variable `VNPAY_HASH_SECRET`
- **KHÔNG commit vào git**
- Shared secret giữa merchant và VNPay

### 6.2. HTTPS

- VNPay sandbox: `https://sandbox.vnpayment.vn`
- Production: Phải dùng HTTPS cho callback URL
- Ngăn chặn eavesdropping

### 6.3. Transaction ID uniqueness

- Transaction ID là unique (indexed trong database)
- Ngăn collision giữa các transactions

---

## 7. Transaction ID Generation

### 7.1. Format

```
PAY + last 10 digits of timestamp + 8-char UUID
```

**Example:** `PAY1743235678a1b2c3d4`

### 7.2. Implementation

```java
private String generateTransactionId() {
    String prefix = "PAY";
    String timestamp = String.valueOf(System.currentTimeMillis());
    String random = UUID.randomUUID().toString().substring(0, 8);
    return prefix + timestamp.substring(timestamp.length() - 10) + random;
}
```

### 7.3. Đặc điểm

- **Prefix "PAY"**: Dễ identify payment transactions
- **Timestamp**: Sắp xếp chronologically, debug dễ dàng
- **Random UUID**: Tránh collision
- **Length**: 21 characters (3 + 10 + 8)
- **Uniqueness**: Được enforce bởi database unique constraint

---

## 8. Xử lý lỗi

### 8.1. Error Codes

```java
// ErrorCode.java
BOOKING_NOT_FOUND               // Booking không tồn tại
BOOKING_ALREADY_CONFIRMED       // Booking đã được confirm
BOOKING_EXPIRED                 // Booking đã hết hạn
PAYMENT_ALREADY_COMPLETED       // Payment đã hoàn thành
PAYMENT_NOT_FOUND               // Payment không tồn tại
INVALID_PAYMENT_CALLBACK        // Signature không hợp lệ
METHOD_NOT_ALLOWED              // Payment method chưa được triển khai
```

### 8.2. Error handling flow

**Trong `initiatePayment()`:**
- Validate booking → throw nếu invalid
- Check existing payment → throw nếu already completed
- VNPay URL generation fail → throw exception

**Trong `handleVnpayCallback()`:**
- Signature invalid → throw INVALID_PAYMENT_CALLBACK
- Payment not found → throw PAYMENT_NOT_FOUND
- Processing error → catch và mark payment as FAILED với error message
- **Không throw exception** sau khi đã validate signature → luôn return response

**Trong `verifyPayment()`:**
- Payment not found → throw PAYMENT_NOT_FOUND
- Not owned by user → throw PAYMENT_NOT_FOUND (security)

---

## 9. Tích hợp với Booking và Seat

### 9.1. Payment → Booking relationship

```java
@Entity
public class Payment {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
    // ...
}
```

- **OneToOne relationship**: Mỗi booking có tối đa 1 payment
- **Unique constraint**: Enforce ở database level
- **Lazy loading**: Payment không tự động load booking

### 9.2. Payment success flow

```java
// Payment successful
payment.markAsSuccess(gatewayResponse);
paymentRepository.save(payment);

// Confirm booking
Booking booking = payment.getBooking();
booking.confirm();
bookingRepository.save(booking);

// Release seats from Redis
List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
List<Long> seatIds = details.stream().map(bd -> bd.getSeat().getId()).toList();
redisSeatScripts.releaseSeats(booking.getShowtime().getId(), seatIds);
```

**Tại sao release seats?**
- Seats đã được giữ trong Redis (HELD) từ lúc tạo booking
- Sau khi thanh toán thành công, seats được book vĩnh viễn trong database
- Không cần giữ trong Redis nữa → release để giải phóng memory

### 9.3. Payment failed flow

```java
// Payment failed
payment.markAsFailed(gatewayResponse);
paymentRepository.save(payment);

// Booking vẫn PENDING
// Seats vẫn HELD trong Redis
// User có thể retry thanh toán trong 15 phút
```

### 9.4. Booking expiration

- Sau 15 phút từ lúc tạo booking, nếu không thanh toán:
- Background job chuyển booking sang EXPIRED
- Release seats từ Redis
- User không thể thanh toán nữa

---

## 10. Database Schema

### 10.1. Payment Entity

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL UNIQUE,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP NULL,
    payment_url VARCHAR(2000),
    gateway_response TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_payment_booking (booking_id),
    INDEX idx_payment_transaction (transaction_id),
    INDEX idx_payment_status (status)
);
```

### 10.2. Key constraints

- `booking_id`: **UNIQUE** → Mỗi booking chỉ có 1 payment
- `transaction_id`: **UNIQUE** → Mỗi transaction ID là duy nhất
- `status`: **INDEXED** → Query theo status nhanh
- `gateway_response`: **TEXT** → Lưu toàn bộ response từ VNPay (debug)

---

## 11. Performance Considerations

### 11.1. Database Indexes

- `idx_payment_booking`: Tìm payment theo booking_id (frequent query)
- `idx_payment_transaction`: Tìm payment theo transaction_id (callback lookup)
- `idx_payment_status`: Query payments by status (admin dashboard)

### 11.2. Lazy Loading

```java
@OneToOne(fetch = FetchType.LAZY)
private Booking booking;
```

Payment không tự động load booking → giảm queries không cần thiết.

### 11.3. Transaction management

```java
@Transactional(readOnly = true)  // Default cho class
public class PaymentService {
    
    @Transactional  // Override cho write operations
    public PaymentResponse initiatePayment() { }
    
    @Transactional
    public void handleVnpayCallback() { }
}
```

- Read-only transactions cho verify methods
- Write transactions cho initiate và callback
- Đảm bảo consistency

---

## 12. Testing Strategy

### 12.1. Unit Tests

```java
@Test
void initiatePayment_shouldSucceed_whenValidBooking() {
    // Given
    Booking booking = createPendingBooking();
    PaymentRequest request = new PaymentRequest(booking.getId(), VNPAY);
    
    // When
    PaymentResponse response = paymentService.initiatePayment(request, userId, httpRequest);
    
    // Then
    assertThat(response.getStatus()).isEqualTo(PENDING);
    assertThat(response.getPaymentUrl()).startsWith("https://sandbox.vnpayment.vn");
}

@Test
void handleVnpayCallback_shouldMarkAsSuccess_whenValidSignatureAndSuccessCode() {
    // Given
    Payment payment = createPendingPayment();
    Map<String, String> params = createValidVnpayCallback("00");
    
    // When
    paymentService.handleVnpayCallback(params);
    
    // Then
    Payment updated = paymentRepository.findById(payment.getId()).get();
    assertThat(updated.getStatus()).isEqualTo(SUCCESS);
    assertThat(updated.getBooking().getStatus()).isEqualTo(CONFIRMED);
}

@Test
void handleVnpayCallback_shouldBeIdempotent_whenCalledMultipleTimes() {
    // Given
    Payment payment = createPendingPayment();
    Map<String, String> params = createValidVnpayCallback("00");
    
    // When
    paymentService.handleVnpayCallback(params);
    paymentService.handleVnpayCallback(params); // Call twice
    
    // Then
    Payment updated = paymentRepository.findById(payment.getId()).get();
    assertThat(updated.getStatus()).isEqualTo(SUCCESS);
    // Verify booking was confirmed only once (no duplicate processing)
}
```

### 12.2. Integration Tests

- Test với VNPay sandbox environment
- Mock VNPay callback với valid signature
- Test signature validation
- Test concurrent callback requests (idempotency)

---

## 13. Monitoring và Alerting

### 13.1. Metrics

- **Payment success rate:** % payments chuyển sang SUCCESS
- **Payment failure rate:** % payments chuyển sang FAILED
- **Average payment time:** Thời gian từ initiate đến callback
- **Callback latency:** Thời gian xử lý callback
- **Duplicate callback rate:** Số lần callback bị skip do idempotency check

### 13.2. Alerts

- Payment success rate < 80%
- Payment failure rate > 20%
- VNPay signature validation failures
- Payment processing errors
- Callback timeout (VNPay không gọi callback sau 15 phút)

### 13.3. Logging

```java
log.info("Payment {} completed successfully", transactionId);
log.info("Payment {} failed with code: {}", transactionId, responseCode);
log.info("Payment {} already processed with status: {}", transactionId, payment.getStatus());
log.error("Invalid VNPay callback signature");
log.error("Error processing payment callback", e);
```

---

## 14. Future Improvements

1. **Multiple payment methods:**
   - Implement MOMO integration
   - Implement CASH payment (pay at counter)

2. **Partial refunds:**
   - Support partial refund amount
   - Track refund history

3. **Payment retry:**
   - Auto-retry failed payments với exponential backoff
   - Notify user khi retry thành công

4. **Webhook notifications:**
   - Send webhook đến external systems khi payment success
   - Real-time notifications

5. **Payment analytics:**
   - Dashboard showing payment trends
   - Success/failure analysis by time, amount, method

---

## Tài liệu tham khảo

- **Code chính:**
  - `domain/payments/domain/Payment.java` - Payment entity
  - `domain/payments/application/PaymentService.java` - Business logic (idempotency, callback handling)
  - `domain/payments/application/VnpayService.java` - VNPay integration (signature generation/verification)
  - `domain/payments/api/PaymentController.java` - REST endpoints
  
- **Configuration:**
  - `application.yml` - VNPay configuration (return-url: http://localhost:8081/api/v1/payments/vnpay-return)
  
- **API Documentation:**
  - `docs/02-design/03-api-design.md` - API endpoints và business rules
  
- **Related Documentation:**
  - `docs/02-design/02-seat-reservation-design.md` - Seat holding mechanism (liên quan đến payment success flow)
