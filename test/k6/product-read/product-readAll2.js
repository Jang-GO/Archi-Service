import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 5 },   // 5 VUs로 시작 (안정적 확인)
        { duration: '1m', target: 15 },  // 15 VUs로 증가
        { duration: '1m', target: 30 },  // 30 VUs로 증가
        { duration: '1m', target: 50 },  // 50 VUs로 증가
        { duration: '30s', target: 0 },  // 종료
    ]
};

export default function() {
    const baseUrl = 'http://localhost:8083';

    // 요금제 전체조회
    let plansResponse = http.get(`${baseUrl}/plans`, {
        tags: { name: 'plans' }
    });
    check(plansResponse, {
        'plans status is 200': (r) => r.status === 200,
        'plans response time < 500ms': (r) => r.timings.duration < 500,
        'plans response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    // 부가서비스 전체조회
    let vassResponse = http.get(`${baseUrl}/vass`, {
        tags: { name: 'vass' }
    });
    check(vassResponse, {
        'vass status is 200': (r) => r.status === 200,
        'vass response time < 500ms': (r) => r.timings.duration < 500,
        'vass response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    // 라이프쿠폰 전체조회
    let couponsResponse = http.get(`${baseUrl}/coupons`, {
        tags: { name: 'coupons' }
    });
    check(couponsResponse, {
        'coupons status is 200': (r) => r.status === 200,
        'coupons response time < 500ms': (r) => r.timings.duration < 500,
        'coupons response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
