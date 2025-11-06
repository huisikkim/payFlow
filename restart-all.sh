#!/bin/bash

echo "🔄 PayFlow 전체 재시작"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 1. 기존 서버 프로세스 종료
echo "1️⃣  기존 서버 프로세스 종료 중..."
pkill -f "gradlew bootRun" 2>/dev/null
sleep 2
echo "   ✅ 서버 종료 완료"
echo ""

# 2. Kafka 중지
echo "2️⃣  Kafka 중지 중..."
docker-compose down
echo "   ✅ Kafka 중지 완료"
echo ""

# 3. Kafka 시작
echo "3️⃣  Kafka 시작 중..."
docker-compose up -d
echo "   ✅ Kafka 시작 완료"
echo ""

# 4. Kafka 준비 대기
echo "4️⃣  Kafka 준비 대기 중 (10초)..."
sleep 10
echo "   ✅ Kafka 준비 완료"
echo ""

# 5. Kafka 상태 확인
echo "5️⃣  Kafka 상태 확인..."
docker-compose ps
echo ""

# 6. 서버 시작 안내
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Kafka 재시작 완료!"
echo ""
echo "🚀 서버 시작:"
echo "   ./gradlew bootRun"
echo ""
echo "🌐 브라우저 접속:"
echo "   http://localhost:8080"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
