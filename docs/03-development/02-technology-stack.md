# Technology Stack

# Công nghệ sử dụng

---

# 1. Mục đích

Tài liệu này mô tả các công nghệ được sử dụng trong hệ thống Đặt Vé Xem Phim Trực Tuyến và vai trò của từng công nghệ trong kiến trúc tổng thể.

---

# 2. Backend

| Công nghệ       | Phiên bản | Mục đích                     |
| --------------- | --------- | ---------------------------- |
| Java            | 21        | Ngôn ngữ lập trình           |
| Spring Boot     | 3.x       | Framework phát triển Backend |
| Spring Security | 6.x       | Xác thực và phân quyền       |
| Spring Data JPA | 3.x       | Truy cập cơ sở dữ liệu       |
| Hibernate       | 6.x       | ORM                          |

---

# 3. Database

| Công nghệ | Mục đích                              |
| --------- | ------------------------------------- |
| MySQL 8   | Lưu trữ dữ liệu chính                 |
| Redis 7   | Giữ ghế tạm thời và lưu dữ liệu cache |

---

# 4. Authentication

| Công nghệ | Mục đích            |
| --------- | ------------------- |
| JWT       | Xác thực người dùng |
| BCrypt    | Mã hóa mật khẩu     |

---

# 5. API

| Công nghệ         | Mục đích                         |
| ----------------- | -------------------------------- |
| RESTful API       | Giao tiếp giữa Client và Backend |
| JSON              | Định dạng dữ liệu trao đổi       |
| OpenAPI / Swagger | Tài liệu API                     |

---

# 6. Build & Dependency

| Công nghệ | Mục đích                            |
| --------- | ----------------------------------- |
| Maven     | Quản lý dependency và build project |

---

# 7. Containerization

| Công nghệ      | Mục đích                          |
| -------------- | --------------------------------- |
| Docker         | Đóng gói ứng dụng                 |
| Docker Compose | Triển khai nhiều dịch vụ cùng lúc |

---

# 8. Testing

| Công nghệ | Mục đích             |
| --------- | -------------------- |
| JUnit 5   | Unit Test            |
| Mockito   | Mock trong Unit Test |

---

# 9. Development Tools

| Công cụ         | Mục đích                          |
| --------------- | --------------------------------- |
| IntelliJ IDEA   | IDE phát triển                    |
| MySQL Workbench | Thiết kế và quản lý cơ sở dữ liệu |
| Postman         | Kiểm thử API                      |
| Git             | Quản lý mã nguồn                  |
| GitHub          | Quản lý repository và cộng tác    |

---

# 10. Kiến trúc

* RESTful API
* Package by Feature
* Layered Architecture
* JWT Authentication

---

# 11. Tài liệu liên quan

* Project Structure
* System Design
* Deployment Design
