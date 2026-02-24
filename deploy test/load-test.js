import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 200 }, // Tăng dần lên 20 người trong 30 giây đầu
    { duration: '1m', target: 500 },  // Tiếp tục tăng lên 50 người trong 1 phút
    { duration: '30s', target: 0 },  // Giảm dần về 0 để hệ thống hồi phục
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],   // Tỷ lệ lỗi phải thấp hơn 1%
    http_req_duration: ['p(95)<500'], // 95% request phải phản hồi dưới 500ms
  },
};

export default function () {
  // 1. Truy cập trang chủ
  let res = http.get('http://localhost:8080/categories');
  check(res, { 'status is 200': (r) => r.status === 200 });

  sleep(1); // Nghỉ 1 giây như người dùng thật đang đọc web

  // 2. Truy cập API Actuator để xem Prometheus lấy dữ liệu gì
  http.get('http://localhost:8080/delivery-actuator/prometheus');
  
  sleep(2);
}
