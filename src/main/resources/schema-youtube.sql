-- YouTube 검색 기록 테이블
CREATE TABLE IF NOT EXISTS youtube_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    search_query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    result_count INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_username ON youtube_search_history(username);
CREATE INDEX IF NOT EXISTS idx_searched_at ON youtube_search_history(searched_at);
