-- H2 콘솔(http://localhost:8080/h2-console)에서 실행
-- JDBC URL: jdbc:h2:file:./data/payflowdb
-- Username: sa
-- Password: (비워두기)

-- 1. 기존 테이블 확인
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'YOUTUBE_SEARCH_HISTORY';

-- 2. 테이블 생성
CREATE TABLE IF NOT EXISTS youtube_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    search_query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    result_count INT DEFAULT 0
);

-- 3. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_username ON youtube_search_history(username);
CREATE INDEX IF NOT EXISTS idx_searched_at ON youtube_search_history(searched_at);

-- 4. 테이블 확인
SELECT * FROM youtube_search_history;

-- 5. 테스트 데이터 삽입 (선택사항)
INSERT INTO youtube_search_history (username, search_query, searched_at, result_count)
VALUES ('testuser', '테스트 검색', CURRENT_TIMESTAMP, 10);

-- 6. 데이터 확인
SELECT * FROM youtube_search_history ORDER BY searched_at DESC;
