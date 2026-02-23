import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    ramping: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 100 },
        { duration: '3m', target: 100 },
        { duration: '30s', target: 0 },
      ],
      gracefulStop: '30s',
    },
  },
  thresholds: {
    'http_req_failed': ['rate<0.02'], // http errors should be <2%
    'http_req_duration': ['p(95)<1200'], // 95% of requests < 1200ms
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SHOW_ID = Number(__ENV.SHOW_ID || 1);
const MAX_SEAT_ID = Number(__ENV.MAX_SEAT_ID || 100); // seat id pool (show 1 has 100 seats)

function pickSeatIds(count) {
  const set = new Set();
  while (set.size < count) {
    const id = Math.floor(Math.random() * MAX_SEAT_ID) + 1;
    set.add(id);
  }
  return Array.from(set);
}

export default function () {
  // 1) Hold seats
  const seatCount = Math.floor(Math.random() * 4) + 1; // 1..4
  const seatIds = pickSeatIds(seatCount);
  const userId = Math.floor(Math.random() * 1000000) + 1;

  const holdPayload = JSON.stringify({
    showId: SHOW_ID,
    seatIds: seatIds,
    userId: userId,
  });

  const headers = { 'Content-Type': 'application/json' };

  const holdRes = http.post(`${BASE_URL}/seat-holds`, holdPayload, { headers });

  check(holdRes, {
    'hold: status is 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  // short think time to mimic user
  sleep(Math.random() * 2 + 0.5);

  // 2) Create order (only if hold succeeded)
  if (holdRes.status >= 200 && holdRes.status < 300) {
    const orderPayload = JSON.stringify({
      showId: SHOW_ID,
      seatIds: seatIds,
      userId: userId,
    });

    const orderRes = http.post(`${BASE_URL}/orders`, orderPayload, { headers });

    check(orderRes, {
      'order: status is 2xx': (r) => r.status >= 200 && r.status < 300,
      'order: has orderId in body': (r) => {
        try {
          const body = r.json();
          return body != null && (body.orderId || body.data?.orderId || body.order_id);
        } catch (e) {
          return false;
        }
      },
    });
  }

  // small pause between iterations
  sleep(Math.random() * 3 + 1);
}
