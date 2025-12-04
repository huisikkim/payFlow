/**
 * YouTube 인기 급상승 - 메인 모듈
 * 전역 상태 관리 및 API 호출
 */

// 전역 상태
window.currentTab = window.currentTab || 'popular';  // window 객체에 저장하여 다른 스크립트와 공유
window.currentView = window.currentView || 'videos';  // 'videos' 또는 'channels'
let lastSearchQuery = '';
let isSearching = false;
let allVideos = [];
let currentShowRank = true;
let nextPageToken = null;
let isLoadingMore = false;
let hasMore = false;

// 초기화
document.addEventListener('DOMContentLoaded', () => {
    // URL에서 검색어 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    
    if (searchQuery) {
        // 검색어가 있으면 검색 탭으로 전환하고 검색 실행
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = searchQuery;
        }
        switchTab('search', true);
        doSearch(searchQuery);
    } else {
        loadVideos();
    }
    
    setupInfiniteScroll();
});

/**
 * 뷰 전환 (영상 / 채널영상)
 */
function switchView(view) {
    window.currentView = view;
    console.log('[View] Switched to:', view);
    
    // 네비게이션 버튼 활성화 상태 변경
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.view === view);
    });
    
    // 페이지 헤더 텍스트 변경
    const pageHeader = document.querySelector('.page-header h1');
    const pageDesc = document.querySelector('.page-header p');
    
    if (view === 'videos') {
        if (pageHeader) {
            pageHeader.innerHTML = `
                <span class="material-symbols-outlined fire-icon">local_fire_department</span>
                YouTube 인기 급상승
            `;
        }
        if (pageDesc) {
            pageDesc.textContent = '실시간 인기 영상을 확인하세요';
        }
    } else if (view === 'channels') {
        if (pageHeader) {
            pageHeader.innerHTML = `
                <span class="material-symbols-outlined fire-icon">subscriptions</span>
                인기 채널 영상
            `;
        }
        if (pageDesc) {
            pageDesc.textContent = '인기 채널의 최신 영상을 확인하세요';
        }
    }
    
    // 현재 탭 새로고침
    loadCurrentTab();
}

/**
 * 탭 전환
 */
function switchTab(tab, skipLoad = false) {
    window.currentTab = tab;
    console.log('[Tab] Switched to:', tab);
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
    if (window.currentTab === 'popular') {
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
        // JWT 토큰 가져오기
        const token = localStorage.getItem('jwt_token');
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(`/api/youtube/search?q=${encodeURIComponent(query)}&maxResults=${maxResults}`, {
            headers: headers
        });
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
async function loadVideos(append = false) {
    // 채널영상 뷰인 경우 채널 영상 로드
    if (window.currentView === 'channels') {
        loadChannelVideos(append);
        return;
    }
    
    const regionCode = document.getElementById('regionCode').value;
    const maxResults = document.getElementById('maxResults').value;
    
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const list = document.getElementById('video-list');
    
    if (!append) {
        loading.style.display = 'block';
        error.style.display = 'none';
        list.innerHTML = '';
        allVideos = [];
        nextPageToken = null;
    }
    
    try {
        let url = `/api/youtube/popular/${regionCode}?maxResults=${maxResults}`;
        if (append && nextPageToken) {
            url += `&pageToken=${nextPageToken}`;
        }
        
        const response = await fetch(url);
        const data = await response.json();
        
        loading.style.display = 'none';
        
        if (data.success && data.videos) {
            if (append) {
                allVideos = [...allVideos, ...data.videos];
            } else {
                allVideos = data.videos;
            }
            
            nextPageToken = data.nextPageToken || null;
            hasMore = data.hasMore || false;
            currentShowRank = true;
            
            renderVideos(allVideos, true);
            
            if (!append) {
                generateInsights(allVideos);
            }
            
            updateFilterResultCount(allVideos.length, allVideos.length);
            updateLoadMoreButton();
        } else {
            showError('영상을 불러올 수 없습니다.');
        }
    } catch (err) {
        loading.style.display = 'none';
        showError('API 호출 중 오류가 발생했습니다: ' + err.message);
    }
}

/**
 * 채널 영상 로드 (인기 채널의 최신 영상)
 */
async function loadChannelVideos(append = false) {
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const list = document.getElementById('video-list');
    
    if (!append) {
        loading.style.display = 'block';
        error.style.display = 'none';
        list.innerHTML = '';
        allVideos = [];
    }
    
    try {
        // 인기 채널 키워드로 검색 (예시)
        const popularChannels = [
            '침착맨', '워크맨', '백종원', '슈카월드', '문명특급',
            '피식대학', '빠더너스', '김계란', '쯔양', '히밥'
        ];
        
        const randomChannel = popularChannels[Math.floor(Math.random() * popularChannels.length)];
        const maxResults = document.getElementById('maxResults').value;
        
        const response = await fetch(`/api/youtube/search?q=${encodeURIComponent(randomChannel)}&maxResults=${maxResults}`);
        const data = await response.json();
        
        loading.style.display = 'none';
        
        if (data.success && data.videos) {
            allVideos = data.videos;
            currentShowRank = false;
            hasMore = false;
            
            renderVideos(allVideos, false);
            generateInsights(allVideos);
            updateFilterResultCount(allVideos.length, allVideos.length);
            
            // 채널 정보 표시
            const channelInfo = document.createElement('div');
            channelInfo.className = 'channel-info-banner';
            channelInfo.innerHTML = `
                <span class="material-symbols-outlined">info</span>
                <p>인기 채널 "<strong>${randomChannel}</strong>"의 영상을 보여드리고 있습니다</p>
                <button class="btn-refresh-channel" onclick="loadChannelVideos()">
                    <span class="material-symbols-outlined">refresh</span>
                    다른 채널 보기
                </button>
            `;
            list.insertBefore(channelInfo, list.firstChild);
        } else {
            showError('채널 영상을 불러올 수 없습니다.');
        }
    } catch (err) {
        loading.style.display = 'none';
        showError('API 호출 중 오류가 발생했습니다: ' + err.message);
    }
}

/**
 * 무한 스크롤 설정
 */
function setupInfiniteScroll() {
    window.addEventListener('scroll', () => {
        // 즐겨찾기 탭이거나 인기 탭이 아닌 경우 무한 스크롤 비활성화
        if (window.currentTab === 'favorites') {
            console.log('[Scroll] Blocked - favorites tab active, currentTab:', window.currentTab);
            return;
        }
        if (isLoadingMore || !hasMore || window.currentTab !== 'popular') return;
        
        const scrollPosition = window.innerHeight + window.scrollY;
        const threshold = document.documentElement.scrollHeight - 500;
        
        if (scrollPosition >= threshold) {
            console.log('[Scroll] Loading more videos... currentTab:', window.currentTab);
            loadMoreVideos();
        }
    });
}

/**
 * 더 많은 영상 로드
 */
async function loadMoreVideos() {
    if (isLoadingMore || !hasMore) return;
    
    isLoadingMore = true;
    showLoadingMore();
    
    await loadVideos(true);
    
    isLoadingMore = false;
    hideLoadingMore();
}

/**
 * 로딩 더보기 표시
 */
function showLoadingMore() {
    let loader = document.getElementById('loading-more');
    if (!loader) {
        loader = document.createElement('div');
        loader.id = 'loading-more';
        loader.className = 'loading-more';
        loader.innerHTML = `
            <div class="loading-spinner-small"></div>
            <p>더 많은 영상을 불러오는 중...</p>
        `;
        document.getElementById('video-list').after(loader);
    }
    loader.style.display = 'flex';
}

/**
 * 로딩 더보기 숨기기
 */
function hideLoadingMore() {
    const loader = document.getElementById('loading-more');
    if (loader) {
        loader.style.display = 'none';
    }
}

/**
 * 더보기 버튼 상태 업데이트
 */
function updateLoadMoreButton() {
    // 즐겨찾기 탭에서는 더보기 버튼을 표시하지 않음
    if (window.currentTab === 'favorites') {
        return;
    }
    
    const info = document.getElementById('load-more-info');
    if (!info) {
        const container = document.createElement('div');
        container.id = 'load-more-info';
        container.className = 'load-more-info';
        document.getElementById('video-list').after(container);
    }
    
    const infoEl = document.getElementById('load-more-info');
    if (hasMore) {
        infoEl.innerHTML = `
            <p>스크롤하면 자동으로 더 많은 영상을 불러옵니다</p>
            <button class="btn-load-more" onclick="loadMoreVideos()">
                <span class="material-symbols-outlined">expand_more</span>
                더 보기
            </button>
        `;
        infoEl.style.display = 'flex';
    } else {
        infoEl.innerHTML = `<p>모든 영상을 불러왔습니다</p>`;
        infoEl.style.display = 'flex';
    }
}
