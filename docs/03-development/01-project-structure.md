## Cấu trúc thư mục

Dự án được tổ chức theo hướng **Package by Feature**, kết hợp với một khu vực **Global** chứa các thành phần dùng chung cho toàn bộ hệ thống.

### `global/`

Chứa các thành phần dùng chung, không thuộc riêng một nghiệp vụ cụ thể.

```text
global/
├── common/         # Hằng số, enum, response dùng chung
├── config/         # Cấu hình ứng dụng (Redis, Security, Swagger...)
├── error/          # Exception và xử lý lỗi toàn cục
├── jwt/            # JWT Provider và Filter
├── security/       # Xác thực và phân quyền
└── util/           # Các lớp tiện ích
```

**Trách nhiệm**

* Quản lý cấu hình hệ thống.
* Cung cấp các tiện ích dùng chung.
* Xử lý ngoại lệ.
* Xác thực và phân quyền bằng JWT.
* Chứa các thành phần được tái sử dụng giữa nhiều module.

---

### `domain/`

Mỗi nghiệp vụ được tổ chức thành một module độc lập, bao gồm đầy đủ các thành phần như Controller, Service, Repository và DTO.

```text
domain/
├── auth/           # Xác thực và phân quyền
├── user/           # Quản lý người dùng
└── model/          # Các JPA Entity
```

Mỗi module nghiệp vụ có cấu trúc tương tự:

```text
booking/
├── controller/
├── service/
├── repository/
└── dto/
```

**Trách nhiệm**

* `controller`: Tiếp nhận HTTP Request và trả về Response.
* `service`: Xử lý nghiệp vụ.
* `repository`: Truy cập cơ sở dữ liệu.
* `dto`: Định nghĩa Request và Response.
* `model`: Định nghĩa các JPA Entity dùng chung cho toàn bộ hệ thống.
