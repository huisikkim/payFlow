let currentTab = 'popular';
let lastSearchQuery = '';
let isSearching = false;

document.addEventListener('DOMContentLoaded', () => {
    loadVideos();
});

function switchTab(tab, skipLoad = false) {
    currentTab = tab;
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tab);
    });
    
    // 인기 탭에서만 국가 필터 표시
    document.getElementById('regionFilter').style.display = tab === 'popular' ? 'flex' : 'none';
    
    if (!skipLoad) {
        if (tab === 'popular') {
            loadVideos();
        } else if (tab === 'search' && lastSearchQuery) {
            doSearch(lastSearchQuery);
        }
    }
}

function loadCurrentTab() {
    if (currentTab === 'popular') {
        loadVideos();
    } else if (lastSearchQuery) {
        doSearch(lastSearchQuery);
    }
}

function handleSearchKeypress(event) {
    if (event.key === 'Enter') {
        searchVideos();
    }
}

function searchVideos() {
    const query = document.getElementById('searchQuery').value.trim();
    if (!query) {
        showError('검색어를 입력해주세요.');
        return;
    }
    
    lastSearchQuery = query;
    switchTab('search', true);
    doSearch(query);
}

async function doSearch(query) {
    if (isSearching) return;
    isSearching = true;
    
    const maxResults = document.getElementById('maxResults').value;
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const list = document.getElementById('video-list');
    
    loading.style.display = 'block';
    error.style.display = 'none';
    list.innerHTML = '';
    
    try {
        const response = await fetch(`/api/youtube/search?q=${encodeURIComponent(query)}&maxResults=${maxResults}`);
        const data = await response.json();
        
        loading.style.display = 'none';
        
        if (data.success && data.videos) {
            if (data.videos.length === 0) {
                showError(`"${query}"에 대한 검색 결과가 없습니다.`);
            } else {
                renderVideos(data.videos, false);
            }
        } else {
            showError('검색 결과를 불러올 수 없습니다.');
        }
    } catch (err) {
        loading.style.display = 'none';
        showError('검색 중 오류가 발생했습니다: ' + err.message);
    } finally {
        isSearching = false;
    }
}

async function loadVideos() {
    const regionCode = document.getElementById('regionCode').value;
    const maxResults = document.getElementById('maxResults').value;
    
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const list = document.getElementById('video-list');
    
    loading.style.display = 'block';
    error.style.display = 'none';
    list.innerHTML = '';
    
    try {
        const response = await fetch(`/api/youtube/popular/${regionCode}?maxResults=${maxResults}`);
        const data = await response.json();
        
        loading.style.display = 'none';
        
        if (data.success && data.videos) {
            renderVideos(data.videos, true);
        } else {
            showError('영상을 불러올 수 없습니다.');
        }
    } catch (err) {
        loading.style.display = 'none';
        showError('API 호출 중 오류가 발생했습니다: ' + err.message);
    }
}

function renderVideos(videos, showRank = true) {
    const list = document.getElementById('video-list');
    
    videos.forEach((video, index) => {
        const item = document.createElement('div');
        item.className = 'video-item';
        item.onclick = () => window.open(`https://www.youtube.com/watch?v=${video.videoId}`, '_blank');
        
        const engagementRate = calculateEngagementRate(video);
        const engagementInfo = getEngagementLevel(engagementRate);
        const performanceScore = calculatePerformanceScore(video);
        const performanceInfo = getPerformanceLevel(performanceScore);
        
        item.innerHTML = `
            <div class="video-item-left">
                ${showRank ? `<div class="rank-number">${index + 1}</div>` : ''}
                <div class="thumbnail-wrapper">
                    <img class="thumbnail" src="${video.thumbnailUrl || ''}" alt="${escapeHtml(video.title)}" loading="lazy">
                    <div class="performance-score ${performanceInfo.class}" title="${performanceInfo.tooltip}">
                        <span class="score-value">${performanceScore}</span>
                        <span class="score-label">점</span>
                    </div>
                    ${video.duration ? `<div class="duration-badge">${formatDuration(video.duration)}</div>` : ''}
                </div>
            </div>
            <div class="video-content">
                <div class="video-title">${escapeHtml(video.title)}</div>
                <div class="channel-name">${escapeHtml(video.channelTitle || '')}</div>
                <div class="metrics-row">
                    <div class="engagement-badge ${engagementInfo.class}" title="참여율: (좋아요 + 댓글) / 조회수">
                        <span class="material-symbols-outlined engagement-icon">local_fire_department</span>
                        <span class="engagement-label">참여율</span>
                        <span class="engagement-value">${engagementRate}%</span>
                        <span class="engagement-level">${engagementInfo.label}</span>
                    </div>
                    <div class="performance-badge ${performanceInfo.class}" title="${performanceInfo.tooltip}">
                        <span class="material-symbols-outlined performance-icon">star</span>
                        <span class="performance-label">성과도</span>
                        <span class="performance-value">${performanceScore}</span>
                        <span class="performance-level">${performanceInfo.label}</span>
                    </div>
                </div>
            </div>
            <div class="video-stats">
                <div class="stat-row views">
                    <span><span class="material-symbols-outlined icon">visibility</span> 조회수</span>
                    <span class="value">${formatNumber(video.viewCount)}</span>
                </div>
                <div class="stat-row likes">
                    <span><span class="material-symbols-outlined icon">thumb_up</span> 좋아요</span>
                    <span class="value">${formatNumber(video.likeCount)}</span>
                </div>
                <div class="stat-row comments">
                    <span><span class="material-symbols-outlined icon">chat_bubble</span> 댓글</span>
                    <span class="value">${formatNumber(video.commentCount)}</span>
                </div>
            </div>
        `;
        
        list.appendChild(item);
    });
}

function calculateEngagementRate(video) {
    const views = video.viewCount || 0;
    const likes = video.likeCount || 0;
    const comments = video.commentCount || 0;
    
    if (views === 0) return '0.00';
    
    const rate = ((likes + comments) / views) * 100;
    return rate.toFixed(2);
}

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
 * - 바이럴 속도 (30%): 일평균 조회수
 * - 좋아요 비율 (30%): 좋아요 / 조회수
 * - 참여율 (40%): (좋아요 + 댓글) / 조회수
 */
function calculatePerformanceScore(video) {
    const views = video.viewCount || 0;
    const likes = video.likeCount || 0;
    const comments = video.commentCount || 0;
    const publishedAt = video.publishedAt;
    
    if (views === 0) return 0;
    
    // 1. 바이럴 속도 점수 (일평균 조회수 기준, 최대 100점)
    let viralScore = 0;
    if (publishedAt) {
        const daysSinceUpload = Math.max(1, (Date.now() - new Date(publishedAt).getTime()) / (1000 * 60 * 60 * 24));
        const viewsPerDay = views / daysSinceUpload;
        // 일 100만 조회 = 100점, 로그 스케일 적용
        viralScore = Math.min(100, (Math.log10(viewsPerDay + 1) / Math.log10(1000000)) * 100);
    }
    
    // 2. 좋아요 비율 점수 (최대 100점)
    // 좋아요 비율 5% = 100점 기준
    const likeRatio = (likes / views) * 100;
    const likeScore = Math.min(100, (likeRatio / 5) * 100);
    
    // 3. 참여율 점수 (최대 100점)
    // 참여율 10% = 100점 기준
    const engagementRatio = ((likes + comments) / views) * 100;
    const engagementScore = Math.min(100, (engagementRatio / 10) * 100);
    
    // 가중 평균 계산
    const totalScore = (viralScore * 0.3) + (likeScore * 0.3) + (engagementScore * 0.4);
    
    return Math.round(totalScore);
}

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

function showError(message) {
    const error = document.getElementById('error');
    document.getElementById('error-text').textContent = message;
    error.style.display = 'block';
}

function formatNumber(num) {
    if (!num) return '0';
    if (num >= 100000000) return (num / 100000000).toFixed(1) + '억';
    if (num >= 10000) return (num / 10000).toFixed(1) + '만';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toLocaleString();
}

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

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
