# Sử dụng JDK 17 (hoặc phiên bản bạn dùng)
FROM eclipse-temurin:21-jdk-alpine
# Copy file jar vào trong image
COPY app.jar app.jar
# Lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app.jar"]

