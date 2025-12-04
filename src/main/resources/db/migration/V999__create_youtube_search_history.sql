-- YouTube 검색 기록 테이블
CREATE TABLE IF NOT EXISTS youtube_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    search_query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    result_count INT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_searched_at (searched_at)
);
