/**
 * K6 테스트 스크립트 예시: register.sample.js
 *
 * 실행 명령어 예시:
 *   ./run-k6.sh register.sample.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '10s',
};

export default function () {
  const res = http.post('http://localhost:8083/user/register', JSON.stringify({
    email: `user${__VU}@test.com`,
    password: 'password123',
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'is status 200': (r) => r.status === 200,
  });

  sleep(1);
}