# K6 부하 테스트 디렉토리

이 디렉토리는 K6 기반 부하 테스트 스크립트와 실행 도구들을 포함합니다.

- `register.sample.js`: 회원가입 API 테스트 예시
- `docker-compose.k6.yml`: Docker 기반 K6 실행 환경
- `run-k6.sh`: 테스트 실행 스크립트
- `summary/`: 결과 요약(summary.json) 저장 폴더

## 실행 예시

```bash
cd test/k6

chmod +x run-k6.sh

./run-k6.sh mentoring.js # 인자를 넣어야 원하는 스크립트 실행 가능
```

## 참고
- 기본 스크립트는 `register.sample.js`이며, 다른 스크립트를 실행하려면 인자로 파일명을 전달하세요.
- chmod +x run-k6.sh 를 수행해야 테스트 수행 가능
- 테스트 결과는 summary/ 디렉토리에 자동 저장됩니다. 
- 테스트 스크립트는 자유롭게 추가할 수 있습니다.