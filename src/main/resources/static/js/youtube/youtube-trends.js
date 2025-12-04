// 구글 트렌드 관리
let currentTrends = [];

/**
 * 구글 트렌드 로드
 */
async function loadGoogleTrends() {
    const container = document.getElementById('video-list');
    
    // 로딩 표시
    container.innerHTML = `
        <div class="trends-loading">
            <div class="loading-spinner"></div>
            <p>구글 트렌드를 불러오는 중...</p>
        </div>
    `;
    
    try {
        const response = await fetch('/api/youtube/trends');
        const data = await response.json();
        
        if (data.success && data.trends && data.trends.length > 0) {
            currentTrends = data.trends;
            renderTrends(data.trends);
        } else {
            showTrendsEmpty();
        }
    } catch (error) {
        console.error('트렌드 로드 실패:', error);
        showTrendsError(error.message);
    }
}

/**
 * 트렌드 렌더링
 */
function renderTrends(trends) {
    const container = document.getElementById('video-list');
    
    const html = `
        <div class="trends-container">
            <div class="trends-header">
                <h2>
                    <span class="material-symbols-outlined">trending_up</span>
                    실시간 구글 트렌드
                </h2>
                <p>지금 한국에서 가장 핫한 검색어와 뉴스</p>
            </div>
            <div class="trends-grid">
                ${trends.map(trend => createTrendCard(trend)).join('')}
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

/**
 * 트렌드 카드 생성
 */
function createTrendCard(trend) {
    const imageUrl = trend.imageUrl || '/images/default-trend.jpg';
    const traffic = trend.traffic || '검색 급상승';
    const timeAgo = formatTimeAgo(trend.publishedAt);
    
    return `
        <div class="trend-card">
            <div class="trend-rank">${trend.rank}</div>
            ${trend.imageUrl ? `<img src="${imageUrl}" alt="${trend.title}" class="trend-image" onerror="this.style.display='none'">` : ''}
            <div class="trend-content">
                <h3 class="trend-title">${escapeHtml(trend.title)}</h3>
                ${trend.description ? `<p class="trend-description">${escapeHtml(trend.description)}</p>` : ''}
                <div class="trend-meta">
                    <div class="trend-traffic">
                        <span class="material-symbols-outlined">local_fire_department</span>
                        ${traffic}
                    </div>
                    <div class="trend-time">${timeAgo}</div>
                </div>
                <div class="trend-actions">
                    <button class="btn-search-youtube" onclick="searchTrendOnYouTube('${escapeHtml(trend.title)}')">
                        <span class="material-symbols-outlined">play_circle</span>
                        YouTube 검색
                    </button>
                    ${trend.newsUrl ? `
                        <button class="btn-view-news" onclick="window.open('${trend.newsUrl}', '_blank')">
                            <span class="material-symbols-outlined">article</span>
                        </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

/**
 * 트렌드 키워드로 YouTube 검색
 */
function searchTrendOnYouTube(keyword) {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = keyword;
    }
    
    // 검색 실행
    if (typeof searchVideos === 'function') {
        searchVideos();
    }
}

/**
 * 빈 상태 표시
 */
function showTrendsEmpty() {
    const container = document.getElementById('video-list');
    container.innerHTML = `
        <div class="trends-empty">
            <span class="material-symbols-outlined">trending_down</span>
            <h3>트렌드를 불러올 수 없습니다</h3>
            <p>잠시 후 다시 시도해주세요</p>
        </div>
    `;
}

/**
 * 에러 표시
 */
function showTrendsError(message) {
    const container = document.getElementById('video-list');
    container.innerHTML = `
        <div class="trends-empty">
            <span class="material-symbols-outlined">error</span>
            <h3>오류가 발생했습니다</h3>
            <p>${escapeHtml(message)}</p>
            <button class="btn-primary" onclick="loadGoogleTrends()" style="margin-top: 16px;">
                다시 시도
            </button>
        </div>
    `;
}

/**
 * 시간 포맷팅
 */
function formatTimeAgo(dateStr) {
    if (!dateStr) return '방금 전';
    
    try {
        const date = new Date(dateStr);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);
        
        if (diffMins < 1) return '방금 전';
        if (diffMins < 60) return `${diffMins}분 전`;
        if (diffHours < 24) return `${diffHours}시간 전`;
        if (diffDays < 7) return `${diffDays}일 전`;
        return date.toLocaleDateString('ko-KR');
    } catch (e) {
        return '최근';
    }
}

/**
 * HTML 이스케이프
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
