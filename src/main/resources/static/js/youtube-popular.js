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
        
        item.innerHTML = `
            ${showRank ? `<div class="rank-number">${index + 1}</div>` : ''}
            <div class="thumbnail-wrapper">
                <img class="thumbnail" src="${video.thumbnailUrl || ''}" alt="${escapeHtml(video.title)}" loading="lazy">
                ${video.duration ? `<div class="duration-badge">${formatDuration(video.duration)}</div>` : ''}
            </div>
            <div class="video-content">
                <div class="video-title">${escapeHtml(video.title)}</div>
                <div class="channel-name">${escapeHtml(video.channelTitle || '')}</div>
            </div>
            <div class="video-stats">
                <div class="stat-row views">
                    <span class="icon">👁️</span>
                    <span>조회수</span>
                    <span class="value">${formatNumber(video.viewCount)}</span>
                </div>
                <div class="stat-row likes">
                    <span class="icon">👍</span>
                    <span>좋아요</span>
                    <span class="value">${formatNumber(video.likeCount)}</span>
                </div>
                <div class="stat-row comments">
                    <span class="icon">💬</span>
                    <span>댓글</span>
                    <span class="value">${formatNumber(video.commentCount)}</span>
                </div>
            </div>
        `;
        
        list.appendChild(item);
    });
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
