import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '30s',
};

export default function() {
    const baseUrl = 'http://localhost:8083';

    // 요금제 전체조회
    let plansResponse = http.get(`${baseUrl}/plans`);
    check(plansResponse, {
        'plans status is 200': (r) => r.status === 200,
        'plans response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 부가서비스 전체조회
    let vassResponse = http.get(`${baseUrl}/vass`);
    check(vassResponse, {
        'vass status is 200': (r) => r.status === 200,
        'vass response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 라이프쿠폰 전체조회
    let couponsResponse = http.get(`${baseUrl}/coupons`);
    check(couponsResponse, {
        'coupons status is 200': (r) => r.status === 200,
        'coupons response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}
