/**
 * YouTube 인기 급상승 - 메인 모듈
 * 전역 상태 관리 및 API 호출
 */

// 전역 상태
let currentTab = 'popular';
let lastSearchQuery = '';
let isSearching = false;
let allVideos = [];
let currentShowRank = true;

// 초기화
document.addEventListener('DOMContentLoaded', () => {
    loadVideos();
});

/**
 * 탭 전환
 */
function switchTab(tab, skipLoad = false) {
    currentTab = tab;
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tab);
    });
    
    document.getElementById('regionFilter').style.display = tab === 'popular' ? 'flex' : 'none';
    
    if (!skipLoad) {
        if (tab === 'popular') {
            loadVideos();
        } else if (tab === 'search' && lastSearchQuery) {
            doSearch(lastSearchQuery);
        }
    }
}

/**
 * 현재 탭 새로고침
 */
function loadCurrentTab() {
    if (currentTab === 'popular') {
        loadVideos();
    } else if (lastSearchQuery) {
        doSearch(lastSearchQuery);
    }
}

/**
 * 검색 키 입력 처리
 */
function handleSearchKeypress(event) {
    if (event.key === 'Enter') {
        searchVideos();
    }
}

/**
 * 검색 실행
 */
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

/**
 * 검색 API 호출
 */
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
                allVideos = data.videos;
                currentShowRank = false;
                renderVideos(data.videos, false);
                updateFilterResultCount(data.videos.length, data.videos.length);
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

/**
 * 인기 영상 로드
 */
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
            allVideos = data.videos;
            currentShowRank = true;
            renderVideos(data.videos, true);
            generateInsights(data.videos);
            updateFilterResultCount(data.videos.length, data.videos.length);
        } else {
            showError('영상을 불러올 수 없습니다.');
        }
    } catch (err) {
        loading.style.display = 'none';
        showError('API 호출 중 오류가 발생했습니다: ' + err.message);
    }
}
