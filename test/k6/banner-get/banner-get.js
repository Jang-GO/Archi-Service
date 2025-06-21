import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
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

    const res = http.get(`${BASE_URL}/ad/ad-banner`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    check(res, {
        '응답 상태 200': (r) => r.status === 200,
        '응답 시간 < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
