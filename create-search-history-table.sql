-- H2 콘솔에서 직접 실행할 수 있는 SQL
-- http://localhost:8080/h2-console 접속 후 실행

CREATE TABLE IF NOT EXISTS youtube_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    search_query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    result_count INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_username ON youtube_search_history(username);
CREATE INDEX IF NOT EXISTS idx_searched_at ON youtube_search_history(searched_at);

-- 테이블 확인
SELECT * FROM youtube_search_history;
