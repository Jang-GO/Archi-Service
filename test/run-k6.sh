#!/bin/bash

# run-k6.sh 작성 예시
# 기본값 설정
SCRIPT_NAME=${1:-register.sample.js}   # 첫 번째 인자 없으면 기본값 사용

echo "[archi-service] 테스트 실행 중: $SCRIPT_NAME"
echo "[archi-service] Summary will be saved to: $SUMMARY_FILE"

# docker-compose 실행
docker compose -f docker-compose.k6.yml run -e SCRIPT=$SCRIPT_NAME k6 run --summary-export=/$SUMMARY_FILE $SCRIPT_NAME