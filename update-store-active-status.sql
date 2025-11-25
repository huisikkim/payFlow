-- 기존 stores 테이블의 isActive 컬럼을 true로 업데이트
UPDATE stores SET is_active = true WHERE is_active IS NULL;
