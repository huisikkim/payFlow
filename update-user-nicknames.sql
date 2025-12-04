-- 기존 사용자에게 닉네임 추가
UPDATE users SET nickname = '호랑이' WHERE username = 'user';
UPDATE users SET nickname = '사자' WHERE username = 'admin';

-- 확인
SELECT username, nickname, email FROM users WHERE username IN ('user', 'admin');
