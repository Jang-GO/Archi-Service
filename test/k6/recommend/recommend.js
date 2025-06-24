import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 5 },   // 5 VUs로 시작
        { duration: '1m', target: 10 },   // 10 VUs로 증가
        { duration: '1m', target: 15 },   // 15 VUs로 증가
        { duration: '30s', target: 0 },   // 종료
    ]
};

const BASE_URL = 'http://localhost:8083';

export function setup() {
    // 로그인: 사용자 계정 정보는 실제 DB에 존재하는 계정으로
    const loginPayload = JSON.stringify({
        email: 'user1@test.com',
        password: 'pw1',
    });

    const loginHeaders = {
        'Content-Type': 'application/json',
    };

    const res = http.post(`${BASE_URL}/auth/login`, loginPayload, { headers: loginHeaders });
    const result = res.json();
    const token = result.data?.accessToken;

    check(token, { '로그인 성공, 토큰 존재': () => token !== undefined });

    return { token };
}

export default function (data) {
    const token = data.token;

    const res = http.get(`${BASE_URL}/recommend`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    check(res, {
        '응답 상태 200': (r) => r.status === 200,
        '응답 시간 < 3000ms': (r) => r.timings.duration < 3000,  // 복잡한 로직이므로 3초로 설정
        '응답 시간 < 5000ms': (r) => r.timings.duration < 5000,  // 최대 허용 5초
        '추천 데이터 존재': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data &&
                    body.data.plans &&
                    body.data.vass &&
                    body.data.coupons;
            } catch {
                return false;
            }
        }
    });

    sleep(2); // 복잡한 로직이므로 대기시간 늘림
}
