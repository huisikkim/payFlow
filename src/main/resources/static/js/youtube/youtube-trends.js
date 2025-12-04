// 구글 트렌드 관리
let currentTrends = [];

/**
 * 구글 트렌드 로드 (탭용)
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
            renderTrends(data.trends, container);
        } else {
            showTrendsEmpty(container);
        }
    } catch (error) {
        console.error('트렌드 로드 실패:', error);
        showTrendsError(error.message, container);
    }
}

/**
 * 구글 트렌드 로드 (전용 페이지용)
 */
async function loadGoogleTrendsPage() {
    const container = document.getElementById('trends-container');
    
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
            renderTrends(data.trends, container);
        } else {
            showTrendsEmpty(container);
        }
    } catch (error) {
        console.error('트렌드 로드 실패:', error);
        showTrendsError(error.message, container);
    }
}

/**
 * 트렌드 렌더링
 */
function renderTrends(trends, container) {
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
    
    // TOP 3 순위에 특별 클래스 추가
    let rankClass = 'trend-rank';
    if (trend.rank === 1) rankClass += ' top-1';
    else if (trend.rank === 2) rankClass += ' top-2';
    else if (trend.rank === 3) rankClass += ' top-3';
    
    // 트렌드 배지 생성
    const badges = [];
    if (trend.rank <= 3) {
        badges.push(`<span class="trend-badge"><span class="material-symbols-outlined">emoji_events</span>TOP ${trend.rank}</span>`);
    }
    if (trend.isNew) {
        badges.push(`<span class="trend-badge"><span class="material-symbols-outlined">new_releases</span>NEW</span>`);
    }
    if (trend.isRising) {
        badges.push(`<span class="trend-badge"><span class="material-symbols-outlined">trending_up</span>급상승</span>`);
    }
    
    // 통계 정보 생성
    const stats = [];
    if (trend.searchVolume) {
        stats.push(`
            <div class="trend-stat-item">
                <span class="material-symbols-outlined">search</span>
                검색량: <strong>${formatNumber(trend.searchVolume)}</strong>
            </div>
        `);
    }
    if (trend.growthRate) {
        stats.push(`
            <div class="trend-stat-item">
                <span class="material-symbols-outlined">show_chart</span>
                증가율: <strong>+${trend.growthRate}%</strong>
            </div>
        `);
    }
    if (trend.relatedArticles) {
        stats.push(`
            <div class="trend-stat-item">
                <span class="material-symbols-outlined">article</span>
                관련 기사: <strong>${trend.relatedArticles}개</strong>
            </div>
        `);
    }
    
    return `
        <div class="trend-card" onclick="searchTrendOnYouTube('${escapeHtml(trend.title)}')">
            <div class="${rankClass}">${trend.rank}</div>
            
            ${trend.imageUrl ? `
                <div class="trend-image-container">
                    <img src="${imageUrl}" alt="${trend.title}" class="trend-image" onerror="this.style.display='none'">
                </div>
            ` : ''}
            
            <div class="trend-content">
                <div class="trend-header">
                    <h3 class="trend-title">${escapeHtml(trend.title)}</h3>
                </div>
                
                ${badges.length > 0 ? `
                    <div class="trend-badges">
                        ${badges.join('')}
                    </div>
                ` : ''}
                
                ${trend.description ? `
                    <p class="trend-description">${escapeHtml(trend.description)}</p>
                ` : ''}
                
                ${stats.length > 0 ? `
                    <div class="trend-stats">
                        ${stats.join('')}
                    </div>
                ` : ''}
                
                <div class="trend-footer">
                    <div class="trend-traffic">
                        <span class="material-symbols-outlined">local_fire_department</span>
                        ${traffic}
                    </div>
                    <div class="trend-time">${timeAgo}</div>
                </div>
                
                <div class="trend-actions" onclick="event.stopPropagation()">
                    <button class="btn-search-youtube" onclick="searchTrendOnYouTube('${escapeHtml(trend.title)}')">
                        <span class="material-symbols-outlined">play_circle</span>
                        YouTube 검색
                    </button>
                    ${trend.newsUrl ? `
                        <button class="btn-view-news" onclick="window.open('${trend.newsUrl}', '_blank')">
                            <span class="material-symbols-outlined">article</span>
                            뉴스 보기
                        </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

/**
 * 숫자 포맷팅
 */
function formatNumber(num) {
    if (!num) return '0';
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toLocaleString();
}

/**
 * 트렌드 키워드로 YouTube 검색
 */
function searchTrendOnYouTube(keyword) {
    // popular 페이지로 이동하면서 검색
    window.location.href = `/youtube/popular?search=${encodeURIComponent(keyword)}`;
}

/**
 * 빈 상태 표시
 */
function showTrendsEmpty(container) {
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
function showTrendsError(message, container) {
    const reloadFunc = container.id === 'trends-container' ? 'loadGoogleTrendsPage()' : 'loadGoogleTrends()';
    container.innerHTML = `
        <div class="trends-empty">
            <span class="material-symbols-outlined">error</span>
            <h3>오류가 발생했습니다</h3>
            <p>${escapeHtml(message)}</p>
            <button class="btn-primary" onclick="${reloadFunc}" style="margin-top: 16px;">
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
