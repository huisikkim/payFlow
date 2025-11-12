#!/bin/bash

echo "🎤 채용 공고 검색 + 모의 면접 챗봇 테스트"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080/api/chatbot"
USER_ID="test_user_$(date +%s)"

echo "📝 사용자 ID: $USER_ID"
echo ""

# 1. 인사
echo "1️⃣ 인사"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"안녕하세요\"}")
echo "응답: $RESPONSE"
CONVERSATION_ID=$(echo $RESPONSE | grep -o '"conversationId":[0-9]*' | grep -o '[0-9]*')
echo ""
sleep 1

# 2. 채용 검색 시작
echo "2️⃣ 채용 검색 시작"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"채용 찾고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 3. 지역 선택
echo "3️⃣ 지역 선택 - 서울"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"서울\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 4. 업종 선택
echo "4️⃣ 업종 선택 - IT"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"IT\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 5. 연봉 입력
echo "5️⃣ 연봉 입력 - 4000~6000만원"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"4000만원에서 6000만원\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 6. 공고 선택
echo "6️⃣ 공고 선택 - 1번"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"1번 면접보고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 7. 기술 스택 입력
echo "7️⃣ 기술 스택 입력"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"Java, Spring Boot, MySQL, AWS, Docker\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 8-12. 면접 질문 답변 (5개)
echo "8️⃣ 질문 1 답변"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"JVM 메모리는 Heap, Stack, Method Area로 구성되어 있습니다. Heap은 객체가 저장되는 공간이고, Stack은 메서드 호출과 지역 변수가 저장됩니다. Method Area는 클래스 메타데이터가 저장되는 영역입니다.\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

echo "9️⃣ 질문 2 답변"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"@Transactional은 AOP를 통해 동작하며, 프록시 패턴을 사용합니다. 메서드 실행 전에 트랜잭션을 시작하고, 정상 종료 시 커밋, 예외 발생 시 롤백을 수행합니다.\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

echo "🔟 질문 3 답변"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"RESTful API 설계 시에는 명확한 URI 구조, 적절한 HTTP 메서드 사용, 상태 코드 활용, 버전 관리, 에러 처리 등을 고려해야 합니다.\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

echo "1️⃣1️⃣ 질문 4 답변"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"대용량 트래픽 처리 시 DB 병목 현상이 발생했습니다. Redis 캐싱을 도입하고 쿼리 최적화를 통해 응답 시간을 70% 개선했습니다.\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

echo "1️⃣2️⃣ 질문 5 답변 (마지막)"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"주로 백엔드 개발과 아키텍처 설계를 담당했습니다. 팀원들과 코드 리뷰를 통해 코드 품질을 높이고, 기술 공유 세션을 진행했습니다.\",\"conversationId\":$CONVERSATION_ID}" | jq -r '.message'
echo ""
sleep 1

# 13. 대화 히스토리 조회
echo "1️⃣3️⃣ 대화 히스토리 조회"
curl -s -X GET "$BASE_URL/conversations/$CONVERSATION_ID/history?userId=$USER_ID" | jq '.[] | select(.role == "BOT") | .message' | tail -1
echo ""

echo "✅ 테스트 완료!"
echo ""
echo "💡 팁: 실제 웹 UI에서 테스트하려면 http://localhost:8080/chatbot 을 방문하세요!"
