import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '5s', target: 5 },   // 점진적 증가
        { duration: '20s', target: 10 }, // 유지
        { duration: '5s', target: 0 },   // 점진적 감소
    ],
};

const BASE_URL = 'http://localhost:8083';

// 다양한 사용자 테스트를 위한 계정들
const users = [
    { email: 'user1@test.com', password: 'pw1' },
    { email: 'user2@test.com', password: 'pw2' },
    { email: 'user3@test.com', password: 'pw3' },
];

export function setup() {
    const tokens = [];

    users.forEach(user => {
        const loginPayload = JSON.stringify(user);
        const res = http.post(`${BASE_URL}/auth/login`, loginPayload, {
            headers: { 'Content-Type': 'application/json' }
        });

        const result = res.json();
        const token = result.data?.accessToken;
        if (token) {
            tokens.push(token);
        }
    });

    check(tokens, { '로그인 성공': () => tokens.length > 0 });
    return { tokens };
}

export default function (data) {
    // 랜덤 사용자 선택
    const token = data.tokens[Math.floor(Math.random() * data.tokens.length)];

    // 1. 배너 생성 요청 (즉시 응답 테스트)
    const bannerRes = http.get(`${BASE_URL}/ad/ad-banner`, {
        headers: { Authorization: `Bearer ${token}` },
    });

    check(bannerRes, {
        '배너 요청 상태 200': (r) => r.status === 200,
        '배너 요청 시간 < 100ms': (r) => r.timings.duration < 100, // 즉시 응답 확인
        'taskId 존재': (r) => r.json().data !== undefined,
    });

    if (bannerRes.status === 200) {
        const taskId = bannerRes.json().data;

        // 2. 폴링으로 배너 생성 완료까지 측정
        const bannerResult = pollBannerResult(taskId, token);

        check(bannerResult, {
            '배너 생성 성공': (result) => result.success,
            '총 처리 시간 < 5000ms': (result) => result.totalTime < 5000,
            '폴링 횟수 < 10회': (result) => result.attempts < 10,
        });
    }

    sleep(1);
}

function pollBannerResult(taskId, token, maxAttempts = 10) {
    const startTime = Date.now();

    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        const resultRes = http.get(`${BASE_URL}/ad/ad-banner/result/${taskId}`, {
            headers: { Authorization: `Bearer ${token}` },
        });

        if (resultRes.status === 200) {
            const result = resultRes.json();
            if (result.data !== null) {
                // 배너 생성 완료
                const totalTime = Date.now() - startTime;
                return {
                    success: true,
                    totalTime: totalTime,
                    attempts: attempt,
                    banner: result.data
                };
            }
        } else if (resultRes.status === 404) {
            // TaskId 없음
            return {
                success: false,
                totalTime: Date.now() - startTime,
                attempts: attempt,
                error: 'TaskId not found'
            };
        }

        // 1초 대기 후 재시도
        sleep(1);
    }

    // 타임아웃
    return {
        success: false,
        totalTime: Date.now() - startTime,
        attempts: maxAttempts,
        error: 'Timeout'
    };
}

// 상태 확인만 하는 별도 테스트 (선택사항)
export function statusCheck() {
    const token = data.tokens[0];

    const bannerRes = http.get(`${BASE_URL}/ad/ad-banner`, {
        headers: { Authorization: `Bearer ${token}` },
    });

    if (bannerRes.status === 200) {
        const taskId = bannerRes.json().data;

        // 상태 확인 API 테스트
        const statusRes = http.get(`${BASE_URL}/ad/ad-banner/status/${taskId}`, {
            headers: { Authorization: `Bearer ${token}` },
        });

        check(statusRes, {
            '상태 확인 성공': (r) => r.status === 200,
            '상태값 존재': (r) => ['PROCESSING', 'COMPLETED', 'FAILED'].includes(r.json().data),
        });
    }
}
