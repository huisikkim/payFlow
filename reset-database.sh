#!/bin/bash

echo "=========================================="
echo "데이터베이스 초기화"
echo "=========================================="
echo ""
echo "⚠️  경고: 이 작업은 모든 데이터를 삭제합니다!"
echo ""
read -p "계속하시겠습니까? (y/N): " confirm

if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "취소되었습니다."
    exit 0
fi

echo ""
echo "데이터베이스 파일 삭제 중..."

# H2 데이터베이스 파일 삭제
rm -f ./data/payflowdb.mv.db
rm -f ./data/payflowdb.trace.db

echo "✅ 데이터베이스 파일이 삭제되었습니다."
echo ""
echo "애플리케이션을 다시 시작하면 새로운 스키마가 생성됩니다."
echo ""
