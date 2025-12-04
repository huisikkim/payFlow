/**
 * YouTube 인기 급상승 - 메인 모듈
 * 전역 상태 관리 및 API 호출
 */

// 전역 상태
window.currentTab = window.currentTab || 'popular';  // window 객체에 저장하여 다른 스크립트와 공유
window.currentView = window.currentView || 'videos';  // 'videos' 또는 'channels'
let lastSearchQuery = '';
let isSearching = false;
let isLoadingChannels = false;  // 채널 영상 로딩 중 플래그
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
        } else if (tab === 'trends') {
            loadGoogleTrends();
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
    } else if (window.currentTab === 'trends') {
        loadGoogleTrends();
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
 * 채널 영상 로드 (인기 급상승 영상 분석으로 핫한 채널 찾기)
 */
async function loadChannelVideos(append = false) {
    // 이미 로딩 중이면 중복 호출 방지
    if (isLoadingChannels) {
        console.log('[loadChannelVideos] Already loading, skipping...');
        return;
    }
    
    isLoadingChannels = true;
    
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
        // 1단계: 핫한 채널 분석 데이터 한 번에 가져오기 (인기 영상 + 채널 상세 정보)
        const regionCode = document.getElementById('regionCode').value;
        const response = await fetch(`/api/youtube/hot-channels?regionCode=${regionCode}&maxResults=50`);
        const data = await response.json();
        
        if (!data.success || !data.videos || data.videos.length === 0) {
            throw new Error('인기 영상을 불러올 수 없습니다.');
        }
        
        // 채널 상세 정보 (이미 포함되어 있음)
        const channelDetails = data.channels || {};
        
        // 2단계: 채널별로 그룹화 및 분석
        const channelMap = new Map();
        
        data.videos.forEach(video => {
            const channelId = video.channelId;
            const channelTitle = video.channelTitle;
            
            if (!channelMap.has(channelId)) {
                // 채널 상세 정보 병합
                const channelInfo = channelDetails[channelId] || {};
                
                channelMap.set(channelId, {
                    channelId: channelId,
                    channelTitle: channelTitle,
                    channelThumbnail: video.channelThumbnail || channelInfo.thumbnailUrl,
                    subscriberCount: video.subscriberCount || video.channelSubscriberCount || channelInfo.subscriberCount || 0,
                    // 채널 상세 정보 추가
                    thumbnailUrl: channelInfo.thumbnailUrl,
                    publishedAt: channelInfo.publishedAt,
                    videoCount: channelInfo.videoCount,
                    totalViewCount: channelInfo.totalViewCount,
                    description: channelInfo.description,
                    country: channelInfo.country,
                    // 영상 통계
                    popularVideoCount: 0,
                    totalViews: 0,
                    totalLikes: 0,
                    videos: []
                });
            }
            
            const channel = channelMap.get(channelId);
            channel.popularVideoCount++;
            channel.totalViews += video.viewCount || 0;
            channel.totalLikes += video.likeCount || 0;
            channel.videos.push(video);
        });
        
        // 3단계: 채널 점수 계산 및 정렬
        const channels = Array.from(channelMap.values()).map(channel => {
            // 핫 스코어 = (인기 영상 등장 횟수 * 3) + (평균 조회수 / 10000) + (구독자 수 / 100000)
            const avgViews = channel.totalViews / channel.popularVideoCount;
            const hotScore = (channel.popularVideoCount * 3) + (avgViews / 10000) + (channel.subscriberCount / 100000);
            
            return {
                ...channel,
                avgViews: avgViews,
                hotScore: hotScore
            };
        }).sort((a, b) => b.hotScore - a.hotScore);
        
        // 4단계: 상위 10개 핫한 채널 선택
        const topChannels = channels.slice(0, 10);
        
        loading.style.display = 'none';
        list.innerHTML = '';
        
        // 5단계: 핫한 채널 정보 표시
        const channelListContainer = document.createElement('div');
        channelListContainer.className = 'hot-channels-container';
        
        const channelHeader = document.createElement('div');
        channelHeader.className = 'channel-info-banner';
        channelHeader.innerHTML = `
            <span class="material-symbols-outlined">trending_up</span>
            <p>지금 가장 <strong>핫한 채널 TOP ${topChannels.length}</strong> (인기 급상승 영상 분석 기반)</p>
            <button class="btn-refresh-channel" onclick="loadChannelVideos()">
                <span class="material-symbols-outlined">refresh</span>
                새로고침
            </button>
        `;
        list.appendChild(channelHeader);
        
        // 6단계: 각 채널 카드 렌더링
        topChannels.forEach((channel, index) => {
            const channelCard = document.createElement('div');
            channelCard.className = 'hot-channel-card';
            
            const rankBadge = index < 3 ? `<span class="rank-badge rank-${index + 1}">#${index + 1}</span>` : `<span class="rank-badge">#${index + 1}</span>`;
            
            // 채널 개설일 계산
            let channelAge = '';
            if (channel.publishedAt) {
                const publishDate = new Date(channel.publishedAt);
                const now = new Date();
                const diffTime = Math.abs(now - publishDate);
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                const diffYears = Math.floor(diffDays / 365);
                const diffMonths = Math.floor((diffDays % 365) / 30);
                
                if (diffYears > 0) {
                    channelAge = `${diffYears}년 ${diffMonths}개월 전`;
                } else if (diffMonths > 0) {
                    channelAge = `${diffMonths}개월 전`;
                } else {
                    channelAge = `${diffDays}일 전`;
                }
            }
            
            channelCard.innerHTML = `
                <div class="channel-card-header">
                    ${rankBadge}
                    <div class="channel-thumbnail">
                        <img src="${channel.thumbnailUrl || channel.channelThumbnail || '/images/default-channel.png'}" alt="${channel.channelTitle}">
                    </div>
                    <div class="channel-info">
                        <h3 class="channel-name">${channel.channelTitle}</h3>
                        ${channelAge ? `<p class="channel-age">📅 개설: ${channelAge}</p>` : ''}
                        <div class="channel-stats">
                            <span class="stat-item">
                                <span class="material-symbols-outlined">group</span>
                                ${formatNumber(channel.subscriberCount)} 구독자
                            </span>
                            <span class="stat-item">
                                <span class="material-symbols-outlined">local_fire_department</span>
                                인기 영상 ${channel.popularVideoCount}개
                            </span>
                            ${channel.videoCount ? `
                            <span class="stat-item">
                                <span class="material-symbols-outlined">video_library</span>
                                총 ${formatNumber(channel.videoCount)} 영상
                            </span>
                            ` : ''}
                            <span class="stat-item">
                                <span class="material-symbols-outlined">visibility</span>
                                평균 ${formatNumber(channel.avgViews)} 조회
                            </span>
                            ${channel.totalViewCount ? `
                            <span class="stat-item">
                                <span class="material-symbols-outlined">trending_up</span>
                                누적 ${formatNumber(channel.totalViewCount)} 조회
                            </span>
                            ` : ''}
                        </div>
                    </div>
                </div>
                <div class="channel-videos-preview" id="channel-videos-${channel.channelId}"></div>
            `;
            
            list.appendChild(channelCard);
            
            // 해당 채널의 영상 미리보기 (최대 3개)
            const previewVideos = channel.videos.slice(0, 3);
            const previewContainer = document.getElementById(`channel-videos-${channel.channelId}`);
            
            previewVideos.forEach(video => {
                const videoPreview = document.createElement('div');
                videoPreview.className = 'channel-video-preview';
                const thumbnailUrl = video.thumbnailUrl || video.thumbnail || 'https://i.ytimg.com/vi/' + video.videoId + '/hqdefault.jpg';
                videoPreview.innerHTML = `
                    <img src="${thumbnailUrl}" alt="${video.title}" onerror="this.src='https://i.ytimg.com/vi/${video.videoId}/hqdefault.jpg'">
                    <div class="preview-info">
                        <h4>${video.title}</h4>
                        <p>${formatNumber(video.viewCount)} 조회 • ${formatNumber(video.likeCount)} 좋아요</p>
                    </div>
                `;
                videoPreview.onclick = () => window.open(`https://www.youtube.com/watch?v=${video.videoId}`, '_blank');
                previewContainer.appendChild(videoPreview);
            });
        });
        
        // 전역 변수 업데이트
        allVideos = data.videos;
        currentShowRank = false;
        hasMore = false;
        
    } catch (err) {
        loading.style.display = 'none';
        showError('핫한 채널을 분석하는 중 오류가 발생했습니다: ' + err.message);
    } finally {
        isLoadingChannels = false;
    }
}

/**
 * 배열 섞기 (Fisher-Yates 알고리즘)
 */
function shuffleArray(array) {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
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
