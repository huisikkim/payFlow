#!/bin/bash

echo "Kafka 시작 중..."
docker-compose up -d

echo "Kafka 준비 대기 중 (10초)..."
sleep 10

echo "Kafka 준비 완료!"
echo "Kafka 상태 확인:"
docker-compose ps

echo ""
echo "   ./gradlew bootRun"
