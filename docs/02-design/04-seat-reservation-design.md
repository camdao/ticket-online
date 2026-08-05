# Thiết Kế Hệ Thống Đặt Giữ Chỗ Ngồi

## 1. Overview

Hệ thống đặt giữ chỗ ngồi là thành phần quan trọng nhất của ứng dụng đặt vé xem phim, xử lý vấn đề đồng thời (concurrency) khi nhiều người dùng cùng đặt ghế cho một suất chiếu.

### 1.1. Business Requirements

1. Người dùng chọn ghế và hệ thống giữ ghế tạm thời (hold) trong 10 phút
2. Trong thời gian hold, chỉ người dùng đó có thể thanh toán cho ghế đã chọn
3. Sau 10 phút, nếu không thanh toán, ghế tự động được giải phóng
4. Người dùng có thể gia hạn thời gian hold (extend)
5. Khi thanh toán thành công, ghế chuyển từ trạng thái "held" sang "booked"

### 1.2. Technical Challenges

- **Race conditions**: Hai người dùng có thể cùng chọn một ghế cùng lúc
- **Overselling**: Nguy cơ bán nhiều hơn số ghế có sẵn
- **Temporary holds**: Giữ ghế tạm thời cho người dùng trong quá trình thanh toán
- **Timeout management**: Tự động giải phóng ghế khi người dùng không thanh toán
- **High throughput**: Xử lý hàng nghìn request đồng thời trong giờ cao điểm

### 1.3. Solution Approach

```
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  BookingService → RedisSeatScripts → Lua Execution      │
└───────────────┬─────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────┐
│                    Redis Layer                          │
│  • Distributed locks with TTL                           │
│  • Atomic Lua scripts                                   │
│  • Key: seat:hold:{showtimeId}:{seatId}                │
└───────────────┬─────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────┐
│                   Database Layer                        │
│  • Source of truth for confirmed bookings               │
│  • Persistent seat availability                         │
│  • ACID transactions                                    │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Architecture

### 2.1. Redis as Distributed Lock

**Why Redis over Database locks?**

| Aspect | Redis | Database |
|--------|-------|----------|
| Lock Duration | Minutes (TTL) | Milliseconds (transaction) |
| Automatic Expiry | Built-in (EXPIRE) | Manual cleanup needed |
| Performance | In-memory, < 1ms | Disk I/O, slower |
| Connection Overhead | No persistent connection | 1 connection per hold |
| Atomicity | Lua scripts | Transaction locks |
| Scalability | Horizontal (Cluster) | Vertical mostly |

**Redis advantages**:
```java
// ✅ Redis approach
redisSeatScripts.holdSeats(seatIds, showId, userId, 600);
// Lock persists for 10 minutes WITH automatic expiry
// No DB connection held
// Atomic operation với Lua
// TTL tự động cleanup
```

**Database approach issues**:
```java
// ❌ Database approach (KHÔNG dùng cho seat holding)
@Transactional
public void holdSeats(List<Long> seatIds) {
    // SELECT ... FOR UPDATE giữ row-level lock
    List<Seat> seats = seatRepository.findByIdsForUpdate(seatIds);
    // Lock released khi transaction commit (milliseconds)
    // Không thể hold 10 phút
}
```

### 2.2. Lua Scripts for Atomicity

**Problem with multiple commands**:
```java
// ❌ KHÔNG AN TOÀN - Race condition
String holder = redis.get("seat:hold:123:45");
if (holder == null) {
    redis.setex("seat:hold:123:45", 600, userId);  // Có thể bị override
}
```

Giữa GET và SETEX, một request khác có thể đã set giá trị → cả hai đều nghĩ mình hold thành công.

**Solution: Atomic Lua script**:
```lua
-- Check và set trong 1 transaction atomic
local holder = redis.call('GET', KEYS[1])
if holder == false then
    redis.call('SETEX', KEYS[1], ARGV[1], ARGV[2])
    return 1
else
    return 0
end
```

**Benefits**:
- Single network round-trip
- Atomic execution (blocks Redis)
- Conditional logic trong script
- Reduced latency

### 2.3. Redis Key Structure

```
Format: seat:hold:{showtimeId}:{seatId}
Value: {userId}
TTL: 600 seconds (10 minutes)

Examples:
seat:hold:123:45 → "user-uuid-abc" (expires in 590s)
seat:hold:123:46 → "user-uuid-def" (expires in 200s)
seat:hold:123:47 → null (available)
```

**Why this structure?**
- `showtimeId`: Phân biệt suất chiếu (cùng ghế có thể được đặt cho nhiều suất)
- `seatId`: Định danh ghế cụ thể
- `userId`: Verify ownership khi extend/release/confirm
- `TTL`: Tự động giải phóng ghế khi hết thời gian

### 2.4. Hybrid Redis + Database Architecture

**Design Decision**: Database is source of truth, Redis is temporary state

```
┌─────────────────┐           ┌──────────────────┐
│  Redis          │           │  Database        │
│  (Temporary)    │  confirm  │  (Permanent)     │
│                 │  ──────→  │                  │
│  • Seat holds   │           │  • Confirmed     │
│  • TTL 10min    │           │    bookings      │
│  • Fast reads   │  rollback │  • Seat          │
│                 │  ←──────  │    availability  │
└─────────────────┘           └──────────────────┘
```

**Why not Redis-only?**
- ❌ Data loss risk nếu Redis crash
- ❌ Không có complex queries (reports, analytics)
- ❌ Không có ACID transactions
- ❌ Scaling limit (vertical scaling mostly)

**Why not Database-only?**
- ❌ Connection pool exhaustion với long-running holds
- ❌ Không có automatic TTL expiry
- ❌ Row locks cause contention
- ❌ Slower performance (disk I/O)

---

## 3. Reservation Workflow

### 3.1. Complete Booking Flow

```
┌──────────┐
│  User    │ Select seats
│  Action  │─────────────────────┐
└──────────┘                     │
                                 ▼
                    ┌────────────────────────┐
                    │ 1. holdSeats()         │
                    │    Redis: SETEX        │
                    │    TTL: 600s           │
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │ 2. createBooking()     │
                    │    DB: Save PENDING    │
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │ 3. createPayment()     │
                    │    Redirect to VNPay   │
                    └───────────┬────────────┘
                                │
                    ┌───────────┴────────────┐
                    │                        │
                    ▼                        ▼
        ┌───────────────────┐    ┌──────────────────┐
        │ Payment Success   │    │ Payment Timeout  │
        └────────┬──────────┘    └────────┬─────────┘
                 │                        │
                 ▼                        ▼
    ┌────────────────────┐    ┌──────────────────────┐
    │ 4. confirmSeats()  │    │ TTL expires          │
    │    Redis: DEL      │    │ Redis: Auto-delete   │
    │    DB: CONFIRMED   │    │ Seats released       │
    └────────────────────┘    └──────────────────────┘
```

### 3.2. Seat Lifecycle States

```
┌─────────────┐
│  AVAILABLE  │  ← No Redis key exists
└──────┬──────┘
       │ holdSeats()
       │ (User selects seats)
       ▼
┌─────────────┐
│    HELD     │  ← Redis key exists with TTL
└──────┬──────┘    Value: userId
       │
       ├──→ checkAndExtendSeats() → HELD (TTL renewed)
       │    (User needs more time)
       │
       ├──→ releaseSeats() → AVAILABLE
       │    (User cancels, payment fails)
       │
       ├──→ TTL expires → AVAILABLE
       │    (10 minutes passed, no payment)
       │
       └──→ confirmSeats() + payment success
            ↓
    ┌─────────────┐
    │   BOOKED    │  ← Persisted in Database
    └─────────────┘    Redis key deleted
```

### 3.3. Implementation Details

#### 3.3.1. holdSeats()

**Function signature**:
```java
public List<Long> holdSeats(
    List<Long> seatIds,
    Long showId,
    String userId,
    long ttlSeconds
)
```

**Lua script logic**:
1. Loop qua từng seatId
2. Tạo key `seat:hold:{showId}:{seatId}`
3. Check `GET key`
   - Nếu null (ghế trống) → `SETEX key ttl userId`, thêm vào success list
   - Nếu có giá trị (đã bị hold) → skip, thêm vào failed list
4. Return danh sách seatIds đã hold thành công

**Usage example**:
```java
List<Long> successSeats = redisSeatScripts.holdSeats(
    seatIds,      // [1, 2, 3]
    showtimeId,   // 123
    userId,       // "user-uuid"
    600           // 10 minutes
);

if (successSeats.size() < seatIds.size()) {
    // Một số ghế đã bị người khác hold
    // Rollback: Release ghế đã hold thành công
    redisSeatScripts.releaseSeats(showId, successSeats);
    throw new CustomException(ErrorCode.SOME_SEATS_UNAVAILABLE);
}
```

#### 3.3.2. checkAndExtendSeats()

**Function signature**:
```java
public List<Long> checkAndExtendSeats(
    Long showId,
    List<Long> seatIds,
    String userId,
    long ttlSeconds
)
```

**Lua script logic**:
1. Loop qua từng seatId
2. `GET seat:hold:{showId}:{seatId}`
3. So sánh value với userId
   - Nếu match → `EXPIRE key ttl`, thêm vào success list
   - Nếu không match hoặc key không tồn tại → skip, thêm vào failed list
4. Return danh sách seatIds đã extend thành công

**Use case**: Người dùng ở trang payment, cần thêm thời gian để nhập thông tin thẻ.

#### 3.3.3. releaseSeats()

**Function signature**:
```java
public Long releaseSeats(Long showId, List<Long> seatIds)
```

**Lua script logic**:
1. Loop qua từng seatId
2. `DEL seat:hold:{showId}:{seatId}`
3. Return số lượng keys đã xóa

**Use cases**:
- Người dùng hủy booking
- Thanh toán thất bại
- Người dùng đóng trang trước khi thanh toán

#### 3.3.4. confirmSeats()

**Function signature**:
```java
public Map<String, Object> confirmSeats(
    Long showId,
    List<Long> seatIds,
    String userId
)
```

**Lua script logic**:
1. Loop qua từng seatId
2. `GET seat:hold:{showId}:{seatId}`
3. Verify value == userId
   - Nếu match → `DEL key`, thêm vào success list (sẽ persist vào DB)
   - Nếu không match → thêm vào failed list
4. Return `{success: [...], failed: [...]}`

**Use case**: Sau khi payment thành công, chuyển ghế từ Redis hold sang DB booked state.

---

## 4. Seat Lock Mechanism

### 4.1. Distributed Lock Pattern

Redis implements distributed lock theo Redlock algorithm principles:

```java
// Simplified Redlock concept
boolean acquireLock(String resource, String token, long ttl) {
    // SET resource token NX PX ttl
    // NX = Only set if Not eXists
    // PX = expire in milliseconds
    
    return redis.set(resource, token, "NX", "PX", ttl);
}

boolean releaseLock(String resource, String token) {
    // Lua script: only delete if token matches
    String script = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    return redis.eval(script, Collections.singletonList(resource), 
                      Collections.singletonList(token));
}
```

**Application to seats**:
- `resource` = seat key (`seat:hold:123:45`)
- `token` = userId
- `ttl` = 600 seconds
- Release check token để prevent release by wrong user

### 4.2. TTL Management

**Current setting**: 10 minutes (600 seconds)

```
Short TTL (5 min)        vs        Long TTL (15 min)
├─ ✅ Less zombie locks          ├─ ✅ More time for payment
├─ ✅ Faster seat turnover       ├─ ✅ Better UX (less rush)
├─ ❌ User rushed                ├─ ❌ More seats locked
└─ ❌ Payment timeout risk       └─ ❌ Zombie lock longer
```

**Optimal duration factors**:
1. Average payment time (measured p95: ~3 minutes)
2. Payment gateway timeout (VNPay: 15 minutes)
3. User behavior (measured: 80% complete within 8 minutes)
4. Seat inventory pressure (high demand → shorter TTL)

**Future enhancement - Dynamic TTL**:
```java
long calculateTTL(Long showtimeId) {
    int seatsTaken = bookingRepository.countByShowtimeId(showtimeId);
    int totalSeats = showtimeRepository.getTotalSeats(showtimeId);
    
    double occupancy = (double) seatsTaken / totalSeats;
    
    if (occupancy > 0.8) {
        return 300;   // 5 min for hot shows (> 80% full)
    } else if (occupancy > 0.5) {
        return 600;   // 10 min for moderate demand
    } else {
        return 900;   // 15 min for low demand
    }
}
```

### 4.3. Race Condition Prevention

#### Scenario 1: Two users selecting same seat

```
Time  User A                          User B
-------------------------------------------------------------------
t0    Click "Chọn ghế 1"              -
t1    → holdSeats([1])                -
      Redis: GET seat:hold:123:1      
      → null (available)
t2    -                               Click "Chọn ghế 1"
                                      → holdSeats([1])
t3    Redis: SETEX ... userId_A       Redis: GET seat:hold:123:1
                                      → "userId_A" (already held)
t4    ✅ Success                      ❌ Failed (seat unavailable)
```

**Result**: User A hold thành công, User B nhận error ngay lập tức. Lua script's atomicity prevents race condition.

#### Scenario 2: Hold expiration during payment

```
Time  User A                          Redis TTL
-------------------------------------------------------------------
t0    holdSeats([1,2])                600s
      → Success
      
t590  Điền form payment...            10s (còn 10 giây)

t595  Click "Thanh toán"              5s
      → Payment processing
      
t605  confirmSeats([1,2])             -5s (EXPIRED!)
      → Check Redis
      ❌ Keys không tồn tại           
      → Throw SEAT_HOLD_EXPIRED
```

**Solutions**:
- Frontend countdown timer
- Auto-extend before expiry
- Payment callback re-validation

#### Scenario 3: Extend race with natural expiration

```
Time  User A                          Redis State
-------------------------------------------------------------------
t599  checkAndExtendSeats([1])        TTL = 1s
      → Lua script start
      → GET seat:hold:123:1
      → "userId_A" exists

t600  -                               TTL EXPIRED
                                      Redis auto-deletes key

t600  → EXPIRE seat:hold:123:1 600    Key không tồn tại
      → Failed (key not found)        
```

**Result**: Extend failed, user phải re-select seats. This is acceptable edge case.

---

## 5. Concurrency Control

### 5.1. Redis Single-threaded Model

Redis sử dụng single-threaded event loop, đảm bảo:
- Một command tại một thời điểm
- FIFO ordering cho commands từ cùng connection
- Lua scripts execute atomically (block toàn bộ Redis trong khi chạy)

**Implication**:
```
Request 1: holdSeats() → Lua script executing
Request 2: holdSeats() → QUEUED, waiting
Request 3: holdSeats() → QUEUED, waiting

→ Không có race condition giữa các Lua scripts
```

### 5.2. Multi-instance Application Servers

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Server 1  │    │   Server 2  │    │   Server 3  │
│             │    │             │    │             │
│ User A req  │    │ User B req  │    │ User C req  │
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │
       └──────────────────┼──────────────────┘
                          │
                    ┌─────▼─────┐
                    │   Redis   │
                    │ (Single)  │
                    └───────────┘
```

**Thread safety guarantees**:
- Redis acts as centralized lock coordinator
- Mỗi server instance có thể có nhiều threads
- Tất cả threads từ tất cả servers đều gửi commands đến cùng một Redis
- Redis serializes tất cả operations → thread-safe

### 5.3. Optimistic vs Pessimistic Locking

**Pessimistic (Database FOR UPDATE)**:
- Giả định conflict sẽ xảy ra → lock ngay từ đầu
- Block các transactions khác
- Phù hợp với high contention, low concurrency

**Optimistic (Version check)**:
```java
@Entity
public class Seat {
    @Version
    private Long version;  // JPA auto-increment on update
}

// Transaction 1
Seat seat = seatRepo.findById(1);  // version = 5
seat.setHeld(true);
seatRepo.save(seat);  // Update WHERE version = 5

// Transaction 2 (concurrent)
Seat seat = seatRepo.findById(1);  // version = 5
seat.setHeld(true);
seatRepo.save(seat);  // ❌ OptimisticLockException - version changed to 6
```

**Issues with Optimistic for seat holding**:
- Conflict detection sau khi đã xử lý business logic
- Cần retry mechanism → poor UX
- Không hỗ trợ TTL auto-expiry

**Redis approach (Hybrid)**:
- Pessimistic-like: Check-and-set atomic với Lua
- Optimistic-like: Lightweight, no heavy DB locks
- Best of both: Fast fail-fast detection + automatic expiry

### 5.4. Lua Scripts vs Multi-Command Transactions

**Option 1: Redis MULTI/EXEC** (Not chosen)
```java
// ❌ Không atomic cho conditional logic
redis.multi();
redis.get("seat:hold:123:45");
redis.setex("seat:hold:123:45", 600, userId);
List<Object> results = redis.exec();

// Không thể check GET result trước khi SETEX
```

**Problem**: MULTI/EXEC queues commands nhưng không thể có conditional logic.

**Option 2: Lua Scripts** (Chosen)
```lua
-- ✅ Atomic với conditional logic
local holder = redis.call('GET', KEYS[1])
if holder == false then
    redis.call('SETEX', KEYS[1], ARGV[1], ARGV[2])
    return 1
else
    return 0
end
```

**Trade-offs**:
- ✅ Single network round-trip
- ✅ Atomic execution (blocks Redis)
- ✅ Conditional logic trong script
- ✅ Reduced latency
- ❌ Lua script blocks Redis → must be fast
- ❌ Debugging harder than Java code
- ❌ Cannot call external services from Lua

**Guidelines**:
- Keep scripts < 100 lines
- No loops with unbounded iterations
- No external I/O
- Pre-load scripts với SCRIPT LOAD

---

## 6. Failure Handling

### 6.1. Redis Connection Failure

**Scenario**: Redis server down hoặc network partition

```java
try {
    redisSeatScripts.holdSeats(seatIds, showId, userId, ttl);
} catch (RedisConnectionException e) {
    log.error("Redis unavailable for showtime {}", showId, e);
    
    // Option 1: Fail fast (current implementation)
    throw new CustomException(ErrorCode.SERVICE_TEMPORARILY_UNAVAILABLE);
    
    // Option 2: Fallback to database (NOT RECOMMENDED - see Risk R-1)
    // return databaseFallbackHold(seatIds);
}
```

**Why not fallback to database?**
1. Inconsistency risk: Một số seats ở Redis, một số ở DB
2. Different expiry mechanisms: Redis TTL vs DB scheduled job
3. Race conditions: Redis users vs DB users
4. Complexity: Dual code paths tăng bugs

**Recommendation**: Fail fast + Redis HA (Sentinel/Cluster) thay vì fallback logic phức tạp.

### 6.2. Partial Hold Failure

**Scenario**: User chọn 5 ghế, chỉ 3 ghế available

```java
List<Long> requestedSeats = Arrays.asList(1L, 2L, 3L, 4L, 5L);
List<Long> heldSeats = redisSeatScripts.holdSeats(
    requestedSeats, showId, userId, 600
);

if (heldSeats.size() < requestedSeats.size()) {
    // Rollback: Release ghế đã hold thành công
    redisSeatScripts.releaseSeats(showId, heldSeats);
    
    // Tìm ghế nào bị fail
    List<Long> unavailableSeats = new ArrayList<>(requestedSeats);
    unavailableSeats.removeAll(heldSeats);
    
    throw new CustomException(
        ErrorCode.SEATS_UNAVAILABLE,
        "Seats not available: " + unavailableSeats
    );
}
```

**Design principle**: All-or-nothing semantics - Hoặc hold tất cả, hoặc không hold gì cả.

### 6.3. Payment Timeout

**Scenario**: Booking created, payment processing quá lâu

```
t0    : holdSeats() → Success (TTL = 600s)
t0+30 : createBooking() → Save to DB with status PENDING
t0+60 : redirectToPayment() → User đang ở VNPay
t600  : Redis TTL expires → Seats released
t700  : Payment callback arrives → confirmSeats() failed
```

**Solution 1: Extend TTL before payment**
```java
// Trước khi redirect to VNPay
boolean extended = redisSeatScripts.checkAndExtendSeats(
    showId, seatIds, userId, 900  // Extend to 15 minutes
);

if (!extended) {
    throw new CustomException(ErrorCode.SEAT_HOLD_EXPIRED);
}
```

**Solution 2: Payment callback re-validation**
```java
@Transactional
public void handlePaymentCallback(String bookingId) {
    Booking booking = bookingRepository.findById(bookingId);
    
    // Try confirm seats
    Map<String, Object> result = redisSeatScripts.confirmSeats(
        booking.getShowtimeId(),
        booking.getSeatIds(),
        booking.getUserId()
    );
    
    List<Long> failed = (List<Long>) result.get("failed");
    
    if (!failed.isEmpty()) {
        // Redis holds expired, but payment succeeded
        // Check if seats still available in DB
        boolean seatsStillAvailable = checkDatabaseAvailability(
            booking.getShowtimeId(),
            failed
        );
        
        if (seatsStillAvailable) {
            // Proceed with booking
            booking.confirm();
        } else {
            // Seats taken by someone else → Refund
            initiateRefund(booking);
            booking.setStatus(BookingStatus.REFUNDED);
        }
    } else {
        // All seats confirmed successfully
        booking.confirm();
    }
}
```

### 6.4. Application Crash During Hold

**Scenario**: Application server crash sau khi hold seats nhưng trước khi save booking

```
t0    : holdSeats([1,2,3]) → Success in Redis
t1    : new Booking(...) → Object created in memory
t2    : 💥 APPLICATION CRASH → Booking never saved to DB
t3-600: Seats vẫn held trong Redis (zombie holds)
t600  : TTL expires → Seats released
```

**Impact**: Seats bị lock vô ích trong 10 phút.

**Mitigations**:

1. **Short TTL cho initial hold** (hiện tại đã implement):
```java
// BookingService.java
redisSeatScripts.holdSeats(seatIds, showId, userId, 300);  // 5 minutes
```

2. **Idempotency token pattern**:
```java
String idempotencyKey = UUID.randomUUID().toString();

// Frontend gửi idempotency key
POST /api/bookings
Headers: X-Idempotency-Key: {uuid}

// Backend check duplicate
if (bookingRepository.existsByIdempotencyKey(idempotencyKey)) {
    return existingBooking;  // Retry-safe
}
```

3. **Health check cleanup job**:
```java
@Scheduled(fixedRate = 60000)  // Every minute
public void cleanupOrphanedHolds() {
    // Find Redis holds without corresponding PENDING bookings
    Set<String> redisHolds = getAllHoldKeys();
    
    for (String holdKey : redisHolds) {
        String userId = redis.get(holdKey);
        boolean hasBooking = bookingRepository
            .existsByUserIdAndStatusAndCreatedAtAfter(
                userId, 
                BookingStatus.PENDING,
                Instant.now().minus(15, ChronoUnit.MINUTES)
            );
        
        if (!hasBooking) {
            // Orphaned hold → Release
            redis.del(holdKey);
            log.warn("Released orphaned hold: {}", holdKey);
        }
    }
}
```

### 6.5. Redis Memory Eviction

**Scenario**: Redis memory đầy, evict keys theo eviction policy

```yaml
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru  # Evict least recently used
```

**Risk**: Seat hold keys có thể bị evict trước khi TTL expires.

**Solutions**:

1. **Set maxmemory-policy = noeviction** (Recommended):
```yaml
maxmemory-policy noeviction
# Redis will return errors when memory full
# → Application handle gracefully
```

2. **Monitor memory usage**:
```java
@Scheduled(fixedRate = 10000)
public void monitorRedisMemory() {
    RedisInfo info = redis.info("memory");
    long usedMemory = info.getUsedMemory();
    long maxMemory = info.getMaxMemory();
    
    double usagePercent = (usedMemory * 100.0) / maxMemory;
    
    if (usagePercent > 80) {
        alertService.sendAlert(
            "Redis memory usage high: " + usagePercent + "%"
        );
    }
}
```

3. **Reserved memory buffer**:
```yaml
maxmemory 1.8gb  # Reserve 200MB buffer from 2GB total
```

### 6.6. Clock Skew Between Servers

**Scenario**: Application server clocks không đồng bộ

```
Server 1 time: 14:00:00
Server 2 time: 14:00:10 (10 seconds ahead)

User A on Server 1: holdSeats() with TTL 600
  → Redis sets expiry at 14:10:00 (Redis time)

User A on Server 2: checkAndExtendSeats()
  → Server 2 thinks it's 14:00:10
  → 10 seconds already passed from TTL
```

**Mitigations**:

1. **NTP synchronization** (Recommended):
```bash
# All servers sync with same NTP source
sudo systemctl enable systemd-timesyncd
sudo timedatectl set-ntp true
```

2. **Server-side timestamps**:
```java
// Use Redis TIME command instead of System.currentTimeMillis()
List<String> time = redis.time();
long serverTimestamp = Long.parseLong(time.get(0));
```

3. **Generous TTL buffers**:
```java
// Add buffer to account for clock skew
long ttlWithBuffer = ttlSeconds + 30;  // 30 second buffer
```

---

## 7. Design Trade-offs

### 7.1. Consistency vs Availability (CAP Theorem)

**Trade-off chosen**: **Availability over Consistency**

```
┌──────────────┐
│  Consistency │  ← Redis đảm bảo tại thời điểm T
│              │    nhưng eventual consistency với DB
└──────────────┘
        ↕ TRADE-OFF
┌──────────────┐
│ Availability │  ← System vẫn hoạt động ngay cả khi
│              │    Redis và DB có độ trễ sync
└──────────────┘
```

**Implications**:

**Scenario**: Redis says held, DB says available
```
t0  : holdSeats() → Redis success
t1  : DB query getAvailableSeats() → May still show as available
      (DB chưa biết về Redis hold)
t10 : Frontend poll lại → Seat đã unavailable
```

**Acceptance**: Eventual consistency là acceptable vì:
1. Hold duration (10 min) >> sync delay (< 1s)
2. User không mong đợi instant consistency
3. Alternative (strong consistency) sacrifices availability

### 7.2. Error Handling: Fail Fast vs Retry

**Trade-off**: **Fail Fast** (chosen) vs Silent Retry

**Fail Fast approach** (current):
```java
try {
    redisSeatScripts.holdSeats(seatIds, showId, userId, ttl);
} catch (RedisException e) {
    // Immediate exception to client
    throw new CustomException(ErrorCode.SERVICE_TEMPORARILY_UNAVAILABLE);
}
```

**Retry approach** (alternative):
```java
@Retryable(
    value = RedisException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public List<Long> holdSeatsWithRetry(...) {
    return redisSeatScripts.holdSeats(...);
}
```

**Why Fail Fast?**
1. **Seat holds are time-sensitive**: Retry adds 300-500ms latency
2. **User feedback**: Better to tell user "try again" than silent hang
3. **Avoid cascade**: Retry can amplify load during Redis issues
4. **Idempotency complexity**: Multiple retries risk duplicate holds

**When to use Retry**:
- Background jobs (không user-facing)
- Non-critical operations (metrics, logging)
- Network blips (transient failures)

**Current design principle**: Fast feedback > Silent resilience

### 7.3. Scalability: Vertical vs Horizontal

**Current architecture**: Single Redis instance (vertical scaling)

**Vertical scaling limits**:
```
Memory: 64GB max practical limit
CPU: Single-threaded, 1 core
Throughput: ~100K ops/sec max
Availability: Single point of failure
```

**Horizontal scaling options**:

**Option 1: Redis Sentinel** (Recommended for Phase 2)
```
┌─────────┐      ┌─────────┐      ┌─────────┐
│ Master  │◄────►│ Sentinel│◄────►│ Sentinel│
│ (R/W)   │      │ (Monitor)│     │ (Monitor)│
└────┬────┘      └─────────┘      └─────────┘
     │
     │ Replication
     ▼
┌─────────┐      ┌─────────┐
│ Replica │      │ Replica │
│ (R only)│      │ (R only)│
└─────────┘      └─────────┘
```

Benefits:
- Auto-failover (< 30s downtime)
- Read replicas scale reads
- No code changes needed

**Option 2: Redis Cluster** (For massive scale)
```
┌──────┐  ┌──────┐  ┌──────┐
│Shard1│  │Shard2│  │Shard3│
│ 0-5k │  │5k-10k│  │10k-16k│
└──────┘  └──────┘  └──────┘
   ↓          ↓         ↓
Hash slot based partitioning
```

Trade-offs:
- ✅ Horizontal scaling to 1000s nodes
- ✅ Multi-master writes
- ❌ Lua scripts limited to single hash slot
- ❌ MULTI/EXEC not supported across slots
- ❌ Client-side routing complexity

**Recommendation**: 
- Phase 1: Single instance + Persistence (current)
- Phase 2: Sentinel for HA (3-6 months)
- Phase 3: Cluster only if > 1M bookings/day (12+ months)

---

## Summary

Thiết kế seat reservation system cân bằng các yếu tố:

**Strengths**:
- ✅ **Correctness**: Lua scripts đảm bảo atomicity, no double-booking
- ✅ **Performance**: Redis in-memory, < 1ms latency
- ✅ **Scalability**: Horizontal scaling path với Sentinel/Cluster
- ✅ **Reliability**: TTL auto-expiry, graceful degradation

**Trade-offs**:
- ⚠️ **Complexity**: Distributed state (Redis + DB) requires careful sync
- ⚠️ **Availability**: Single Redis SPOF → mitigated by Sentinel

**Next Steps** (documented in Risk Assessment):
1. Enable Redis persistence (AOF/RDB)
2. Deploy Redis Sentinel (HA)
3. Implement reconciliation job
4. Add monitoring/alerting
5. Pre-warm cache cho hot shows
