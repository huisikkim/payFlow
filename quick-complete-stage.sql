-- 빠른 스테이지 완료 및 정산 테스트용 SQL
-- H2 Console에서 실행: http://localhost:8080/h2-console

-- 1. 스테이지 ID 확인
SELECT id, name, status, total_participants FROM stages;

-- 2. 특정 스테이지의 모든 결제를 완료 처리 (STAGE_ID를 실제 ID로 변경)
UPDATE stage_payments 
SET is_paid = true, 
    paid_at = CURRENT_TIMESTAMP, 
    payment_key = CONCAT('TEST-', id)
WHERE stage_id = 1;  -- 여기서 1을 실제 스테이지 ID로 변경

-- 3. 모든 약정금 생성 및 완료 처리
INSERT INTO stage_payouts (stage_id, username, turn_number, amount, is_completed, scheduled_at, completed_at, transaction_id)
SELECT 
    sp.stage_id,
    sp.username,
    sp.turn_number,
    s.monthly_payment * s.total_participants * (1 + s.interest_rate * (sp.turn_number - 1)) as amount,
    true as is_completed,
    CURRENT_TIMESTAMP as scheduled_at,
    CURRENT_TIMESTAMP as completed_at,
    CONCAT('TXN-TEST-', sp.id) as transaction_id
FROM stage_participants sp
JOIN stages s ON sp.stage_id = s.id
WHERE sp.stage_id = 1  -- 여기서 1을 실제 스테이지 ID로 변경
AND NOT EXISTS (
    SELECT 1 FROM stage_payouts 
    WHERE stage_id = sp.stage_id 
    AND turn_number = sp.turn_number
);

-- 4. 참여자의 약정금 수령 상태 업데이트
UPDATE stage_participants 
SET has_received_payout = true,
    payout_received_at = CURRENT_TIMESTAMP
WHERE stage_id = 1;  -- 여기서 1을 실제 스테이지 ID로 변경

-- 5. 스테이지 완료 처리
UPDATE stages 
SET status = 'COMPLETED',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 1;  -- 여기서 1을 실제 스테이지 ID로 변경

-- 6. 결과 확인
SELECT 
    s.id,
    s.name,
    s.status,
    COUNT(DISTINCT sp.id) as participants,
    COUNT(DISTINCT spm.id) as payments,
    COUNT(DISTINCT spo.id) as payouts
FROM stages s
LEFT JOIN stage_participants sp ON s.id = sp.stage_id
LEFT JOIN stage_payments spm ON s.id = spm.stage_id
LEFT JOIN stage_payouts spo ON s.id = spo.stage_id
WHERE s.id = 1  -- 여기서 1을 실제 스테이지 ID로 변경
GROUP BY s.id, s.name, s.status;

-- 7. 이제 정산 생성 API 호출 가능
-- POST http://localhost:8080/api/stages/1/settlement
-- 또는 브라우저에서: http://localhost:8080/stages/1/settlement
