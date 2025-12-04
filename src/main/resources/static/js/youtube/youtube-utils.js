/**
 * YouTube 유틸리티 함수들
 */

/**
 * 숫자 포맷팅 (만, 억, K 단위)
 */
function formatNumber(num) {
    if (!num) return '0';
    if (num >= 100000000) return (num / 100000000).toFixed(1) + '억';
    if (num >= 10000) return (num / 10000).toFixed(1) + '만';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toLocaleString();
}

/**
 * 영상 길이 포맷팅 (ISO 8601 duration)
 */
function formatDuration(duration) {
    if (!duration) return '';
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
    if (!match) return duration;
    
    const hours = match[1] ? parseInt(match[1]) : 0;
    const minutes = match[2] ? parseInt(match[2]) : 0;
    const seconds = match[3] ? parseInt(match[3]) : 0;
    
    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
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

/**
 * 에러 메시지 표시
 */
function showError(message) {
    const error = document.getElementById('error');
    document.getElementById('error-text').textContent = message;
    error.style.display = 'block';
}

/**
 * 참여율 계산
 */
function calculateEngagementRate(video) {
    const views = video.viewCount || 0;
    const likes = video.likeCount || 0;
    const comments = video.commentCount || 0;
    
    if (views === 0) return '0.00';
    
    const rate = ((likes + comments) / views) * 100;
    return rate.toFixed(2);
}

/**
 * 참여율 레벨 정보
 */
function getEngagementLevel(rate) {
    const numRate = parseFloat(rate);
    
    if (numRate >= 10) {
        return { class: 'engagement-excellent', label: '최고' };
    } else if (numRate >= 5) {
        return { class: 'engagement-high', label: '높음' };
    } else if (numRate >= 2) {
        return { class: 'engagement-good', label: '좋음' };
    } else if (numRate >= 1) {
        return { class: 'engagement-normal', label: '보통' };
    } else {
        return { class: 'engagement-low', label: '낮음' };
    }
}

/**
 * 종합 성과도 점수 계산 (0~100점)
 */
function calculatePerformanceScore(video) {
    const views = video.viewCount || 0;
    const likes = video.likeCount || 0;
    const comments = video.commentCount || 0;
    const publishedAt = video.publishedAt;
    
    if (views === 0) return 0;
    
    // 1. 바이럴 속도 점수
    let viralScore = 0;
    if (publishedAt) {
        const daysSinceUpload = Math.max(1, (Date.now() - new Date(publishedAt).getTime()) / (1000 * 60 * 60 * 24));
        const viewsPerDay = views / daysSinceUpload;
        viralScore = Math.min(100, (Math.log10(viewsPerDay + 1) / Math.log10(1000000)) * 100);
    }
    
    // 2. 좋아요 비율 점수
    const likeRatio = (likes / views) * 100;
    const likeScore = Math.min(100, (likeRatio / 5) * 100);
    
    // 3. 참여율 점수
    const engagementRatio = ((likes + comments) / views) * 100;
    const engagementScore = Math.min(100, (engagementRatio / 10) * 100);
    
    // 가중 평균
    const totalScore = (viralScore * 0.3) + (likeScore * 0.3) + (engagementScore * 0.4);
    
    return Math.round(totalScore);
}

/**
 * 성과도 레벨 정보
 */
function getPerformanceLevel(score) {
    const tooltip = '성과도 = 바이럴속도(30%) + 좋아요비율(30%) + 참여율(40%)';
    
    if (score >= 80) {
        return { class: 'performance-excellent', label: 'S', tooltip };
    } else if (score >= 60) {
        return { class: 'performance-high', label: 'A', tooltip };
    } else if (score >= 40) {
        return { class: 'performance-good', label: 'B', tooltip };
    } else if (score >= 20) {
        return { class: 'performance-normal', label: 'C', tooltip };
    } else {
        return { class: 'performance-low', label: 'D', tooltip };
    }
}

/**
 * 바이럴 지수 계산
 */
function calculateViralIndex(video) {
    const views = video.viewCount || 0;
    const subscribers = video.channelSubscriberCount;
    
    if (!subscribers || subscribers === 0) return null;
    
    const index = (views / subscribers) * 100;
    return index.toFixed(0);
}

/**
 * 바이럴 레벨 정보
 */
function getViralLevel(index) {
    if (index === null) {
        return { class: 'viral-none', label: '-' };
    }
    
    const numIndex = parseFloat(index);
    
    if (numIndex >= 500) {
        return { class: 'viral-excellent', label: '대박' };
    } else if (numIndex >= 200) {
        return { class: 'viral-high', label: '폭발' };
    } else if (numIndex >= 100) {
        return { class: 'viral-good', label: '확산' };
    } else if (numIndex >= 50) {
        return { class: 'viral-normal', label: '양호' };
    } else {
        return { class: 'viral-low', label: '일반' };
    }
}

/**
 * 폭발력 계산 (시간당 조회수)
 */
function calculateExplosiveness(video) {
    const views = video.viewCount || 0;
    const publishedAt = video.publishedAt;
    
    if (!publishedAt || views === 0) return '0';
    
    const hoursSinceUpload = Math.max(1, (Date.now() - new Date(publishedAt).getTime()) / (1000 * 60 * 60));
    const viewsPerHour = views / hoursSinceUpload;
    
    return formatNumber(Math.round(viewsPerHour)) + '/h';
}

/**
 * 폭발력 레벨 정보
 */
function getExplosivenessLevel(explosiveness) {
    const numStr = explosiveness.replace(/[^0-9.]/g, '');
    let num = parseFloat(numStr) || 0;
    
    if (explosiveness.includes('억')) {
        num *= 100000000;
    } else if (explosiveness.includes('만')) {
        num *= 10000;
    } else if (explosiveness.includes('K')) {
        num *= 1000;
    }
    
    if (num >= 100000) {
        return { class: 'explosiveness-excellent', label: '초고속' };
    } else if (num >= 50000) {
        return { class: 'explosiveness-high', label: '고속' };
    } else if (num >= 10000) {
        return { class: 'explosiveness-good', label: '빠름' };
    } else if (num >= 1000) {
        return { class: 'explosiveness-normal', label: '보통' };
    } else {
        return { class: 'explosiveness-low', label: '느림' };
    }
}

/**
 * 로그인 체크 및 리다이렉트
 */
function requireLogin(message = '로그인이 필요한 기능입니다.') {
    const token = localStorage.getItem('jwt_token');
    
    if (!token) {
        if (confirm(message + '\n로그인 페이지로 이동하시겠습니까?')) {
            const currentUrl = encodeURIComponent(window.location.pathname + window.location.search);
            window.location.href = `/youtube/login?redirect=${currentUrl}`;
        }
        return false;
    }
    
    return true;
}

/**
 * JWT 토큰 가져오기
 */
function getAuthToken() {
    return localStorage.getItem('jwt_token');
}

/**
 * 인증 헤더 생성
 */
function getAuthHeaders() {
    const token = getAuthToken();
    return token ? {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    } : {
        'Content-Type': 'application/json'
    };
}

/**
 * 로그인 상태 확인
 */
function isLoggedIn() {
    return !!localStorage.getItem('jwt_token');
}
