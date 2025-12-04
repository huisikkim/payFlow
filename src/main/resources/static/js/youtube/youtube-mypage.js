/**
 * YouTube 마이페이지 스크립트
 */

let searchHistory = [];

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    checkLoginAndLoadHistory();
    setupEventListeners();
});

// 로그인 체크 및 검색 기록 로드
async function checkLoginAndLoadHistory() {
    const token = localStorage.getItem('jwt_token');
    const username = localStorage.getItem('username');
    const nickname = localStorage.getItem('nickname');
    
    if (!token || !username) {
        // 로그인하지 않은 경우
        showError('로그인이 필요합니다.');
        setTimeout(() => {
            window.location.href = '/youtube/login?redirect=/youtube/mypage';
        }, 1500);
        return;
    }
    
    // 환영 메시지 업데이트
    const displayName = nickname || username;
    document.getElementById('welcome-message').textContent = `${displayName}님의 활동 내역`;
    
    // 검색 기록 로드
    await loadSearchHistory();
}

// 검색 기록 로드
async function loadSearchHistory() {
    const loading = document.getElementById('loading');
    const error = document.getElementById('error');
    const emptyState = document.getElementById('empty-state');
    const historyList = document.getElementById('search-history-list');
    
    loading.style.display = 'block';
    error.style.display = 'none';
    emptyState.style.display = 'none';
    historyList.innerHTML = '';
    
    try {
        const token = localStorage.getItem('jwt_token');
        const response = await fetch('/api/youtube/search-history', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || '검색 기록을 불러올 수 없습니다.');
        }
        
        searchHistory = data.history || [];
        
        if (searchHistory.length === 0) {
            emptyState.style.display = 'block';
        } else {
            renderSearchHistory();
        }
        
    } catch (err) {
        console.error('검색 기록 로드 실패:', err);
        showError(err.message);
    } finally {
        loading.style.display = 'none';
    }
}

// 검색 기록 렌더링
function renderSearchHistory() {
    const historyList = document.getElementById('search-history-list');
    historyList.innerHTML = '';
    
    searchHistory.forEach(item => {
        const historyItem = createHistoryItem(item);
        historyList.appendChild(historyItem);
    });
}

// 검색 기록 아이템 생성
function createHistoryItem(item) {
    const div = document.createElement('div');
    div.className = 'history-item';
    div.dataset.id = item.id;
    
    const searchedAt = new Date(item.searchedAt);
    const timeAgo = getTimeAgo(searchedAt);
    
    div.innerHTML = `
        <div class="history-info">
            <div class="history-query">
                <span class="material-symbols-outlined">search</span>
                ${escapeHtml(item.searchQuery)}
            </div>
            <div class="history-meta">
                <span>
                    <span class="material-symbols-outlined">schedule</span>
                    ${timeAgo}
                </span>
                <span>
                    <span class="material-symbols-outlined">video_library</span>
                    결과 ${item.resultCount}개
                </span>
            </div>
        </div>
        <div class="history-actions">
            <button class="btn-search-again" onclick="searchAgain('${escapeHtml(item.searchQuery)}')">
                <span class="material-symbols-outlined">refresh</span>
                다시 검색
            </button>
            <button class="btn-delete" onclick="deleteHistory(${item.id})">
                <span class="material-symbols-outlined">delete</span>
            </button>
        </div>
    `;
    
    return div;
}

// 시간 경과 표시
function getTimeAgo(date) {
    const now = new Date();
    const diff = now - date;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (days > 0) return `${days}일 전`;
    if (hours > 0) return `${hours}시간 전`;
    if (minutes > 0) return `${minutes}분 전`;
    return '방금 전';
}

// HTML 이스케이프
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 다시 검색
function searchAgain(query) {
    window.location.href = `/youtube/popular?search=${encodeURIComponent(query)}`;
}

// 검색 기록 삭제
async function deleteHistory(id) {
    if (!confirm('이 검색 기록을 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('jwt_token');
        const response = await fetch(`/api/youtube/search-history/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || '삭제에 실패했습니다.');
        }
        
        // UI에서 제거
        searchHistory = searchHistory.filter(item => item.id !== id);
        
        if (searchHistory.length === 0) {
            document.getElementById('empty-state').style.display = 'block';
            document.getElementById('search-history-list').innerHTML = '';
        } else {
            renderSearchHistory();
        }
        
    } catch (err) {
        console.error('검색 기록 삭제 실패:', err);
        alert(err.message);
    }
}

// 전체 검색 기록 삭제
async function clearAllHistory() {
    if (!confirm('모든 검색 기록을 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('jwt_token');
        const response = await fetch('/api/youtube/search-history', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || '삭제에 실패했습니다.');
        }
        
        searchHistory = [];
        document.getElementById('empty-state').style.display = 'block';
        document.getElementById('search-history-list').innerHTML = '';
        
    } catch (err) {
        console.error('전체 검색 기록 삭제 실패:', err);
        alert(err.message);
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    const clearAllBtn = document.getElementById('clearAllBtn');
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', clearAllHistory);
    }
}

// 에러 표시
function showError(message) {
    const error = document.getElementById('error');
    const errorText = document.getElementById('error-text');
    errorText.textContent = message;
    error.style.display = 'block';
}
