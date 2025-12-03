// 즐겨찾기 관련 변수
let currentFolderId = null;
let pendingVideo = null;

// 폴더 목록 로드
async function loadFolders() {
    const foldersList = document.getElementById('folders-list');
    foldersList.innerHTML = '<div class="loading"><div class="loading-spinner"></div></div>';
    
    try {
        const response = await fetch('/api/youtube/folders');
        const data = await response.json();
        
        if (data.success && data.folders) {
            renderFolders(data.folders);
        } else {
            foldersList.innerHTML = '<p class="empty-message">폴더를 불러올 수 없습니다.</p>';
        }
    } catch (err) {
        foldersList.innerHTML = '<p class="empty-message">오류가 발생했습니다.</p>';
    }
}

// 폴더 목록 렌더링
function renderFolders(folders) {
    const foldersList = document.getElementById('folders-list');
    
    if (folders.length === 0) {
        foldersList.innerHTML = `
            <div class="empty-folders">
                <span class="material-symbols-outlined">folder_off</span>
                <p>아직 폴더가 없습니다</p>
                <button class="btn-create-first" onclick="showCreateFolderModal()">
                    첫 폴더 만들기
                </button>
            </div>
        `;
        return;
    }
    
    foldersList.innerHTML = folders.map(folder => `
        <div class="folder-card" onclick="openFolder(${folder.id}, '${escapeHtml(folder.name)}')">
            <div class="folder-icon">
                <span class="material-symbols-outlined">folder</span>
            </div>
            <div class="folder-info">
                <div class="folder-name">${escapeHtml(folder.name)}</div>
                <div class="folder-meta">
                    <span class="video-count">${folder.videoCount || 0}개 영상</span>
                </div>
            </div>
            <span class="material-symbols-outlined folder-arrow">chevron_right</span>
        </div>
    `).join('');
}

// 폴더 열기
async function openFolder(folderId, folderName) {
    currentFolderId = folderId;
    document.getElementById('folders-list').style.display = 'none';
    document.getElementById('folder-videos').style.display = 'block';
    document.getElementById('current-folder-name').textContent = folderName;
    
    await loadFolderVideos(folderId);
}

// 폴더 목록으로 돌아가기
function showFoldersList() {
    currentFolderId = null;
    document.getElementById('folder-videos').style.display = 'none';
    document.getElementById('folders-list').style.display = 'block';
    loadFolders();
}

// 폴더 내 영상 로드
async function loadFolderVideos(folderId) {
    const videoList = document.getElementById('folder-video-list');
    videoList.innerHTML = '<div class="loading"><div class="loading-spinner"></div></div>';
    
    try {
        const response = await fetch(`/api/youtube/folders/${folderId}/videos`);
        const data = await response.json();
        
        if (data.success && data.videos) {
            renderFolderVideos(data.videos);
        }
    } catch (err) {
        videoList.innerHTML = '<p class="empty-message">영상을 불러올 수 없습니다.</p>';
    }
}

// 폴더 내 영상 렌더링
function renderFolderVideos(videos) {
    const videoList = document.getElementById('folder-video-list');
    
    if (videos.length === 0) {
        videoList.innerHTML = `
            <div class="empty-videos">
                <span class="material-symbols-outlined">video_library</span>
                <p>저장된 영상이 없습니다</p>
                <p class="sub">인기 영상에서 ⭐ 버튼을 눌러 추가하세요</p>
            </div>
        `;
        return;
    }
    
    videoList.innerHTML = videos.map(video => `
        <div class="video-item favorite-video-item">
            <div class="video-item-left">
                <div class="thumbnail-wrapper" onclick="window.open('https://www.youtube.com/watch?v=${video.videoId}', '_blank')">
                    <img class="thumbnail" src="${video.thumbnailUrl || ''}" alt="${escapeHtml(video.title)}" loading="lazy">
                    ${video.duration ? `<div class="duration-badge">${formatDuration(video.duration)}</div>` : ''}
                </div>
            </div>
            <div class="video-content" onclick="window.open('https://www.youtube.com/watch?v=${video.videoId}', '_blank')">
                <div class="video-title">${escapeHtml(video.title)}</div>
                <div class="channel-name">${escapeHtml(video.channelTitle || '')}</div>
                <div class="video-stats-inline">
                    <span><span class="material-symbols-outlined icon">visibility</span> ${formatNumber(video.viewCount)}</span>
                    <span><span class="material-symbols-outlined icon">thumb_up</span> ${formatNumber(video.likeCount)}</span>
                </div>
            </div>
            <button class="btn-remove-video" onclick="event.stopPropagation(); removeVideoFromFolder('${video.videoId}')" title="삭제">
                <span class="material-symbols-outlined">delete</span>
            </button>
        </div>
    `).join('');
}

// 폴더 생성 모달 표시
function showCreateFolderModal() {
    document.getElementById('create-folder-modal').style.display = 'flex';
    document.getElementById('folder-name').value = '';
    document.getElementById('folder-desc').value = '';
    document.getElementById('folder-name').focus();
}

// 폴더 생성 모달 숨기기
function hideCreateFolderModal() {
    document.getElementById('create-folder-modal').style.display = 'none';
}

// 폴더 생성
async function createFolder() {
    const name = document.getElementById('folder-name').value.trim();
    const description = document.getElementById('folder-desc').value.trim();
    
    if (!name) {
        alert('폴더명을 입력해주세요.');
        return;
    }
    
    try {
        const response = await fetch('/api/youtube/folders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description })
        });
        
        const data = await response.json();
        
        if (data.success) {
            hideCreateFolderModal();
            loadFolders();
            
            // 대기 중인 영상이 있으면 추가
            if (pendingVideo) {
                addVideoToFolder(data.folder.id, pendingVideo);
                pendingVideo = null;
            }
        } else {
            alert(data.message || '폴더 생성에 실패했습니다.');
        }
    } catch (err) {
        alert('오류가 발생했습니다.');
    }
}

// 현재 폴더 삭제
async function deleteCurrentFolder() {
    if (!currentFolderId) return;
    
    if (!confirm('이 폴더를 삭제하시겠습니까? 폴더 내 모든 영상도 함께 삭제됩니다.')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/youtube/folders/${currentFolderId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showFoldersList();
        } else {
            alert(data.message || '폴더 삭제에 실패했습니다.');
        }
    } catch (err) {
        alert('오류가 발생했습니다.');
    }
}

// 폴더에서 영상 삭제
async function removeVideoFromFolder(videoId) {
    if (!currentFolderId) return;
    
    try {
        const response = await fetch(`/api/youtube/folders/${currentFolderId}/videos/${videoId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.success) {
            loadFolderVideos(currentFolderId);
        }
    } catch (err) {
        alert('오류가 발생했습니다.');
    }
}

// 폴더 선택 모달 표시
async function showSelectFolderModal(video) {
    pendingVideo = video;
    document.getElementById('select-folder-modal').style.display = 'flex';
    
    const folderSelectList = document.getElementById('folder-select-list');
    folderSelectList.innerHTML = '<div class="loading"><div class="loading-spinner"></div></div>';
    
    try {
        const response = await fetch('/api/youtube/folders');
        const data = await response.json();
        
        if (data.success && data.folders) {
            if (data.folders.length === 0) {
                folderSelectList.innerHTML = '<p class="empty-message">폴더가 없습니다. 새 폴더를 만들어주세요.</p>';
            } else {
                folderSelectList.innerHTML = data.folders.map(folder => `
                    <div class="folder-select-item" onclick="addVideoToFolder(${folder.id})">
                        <span class="material-symbols-outlined">folder</span>
                        <span>${escapeHtml(folder.name)}</span>
                        <span class="video-count">${folder.videoCount || 0}개</span>
                    </div>
                `).join('');
            }
        }
    } catch (err) {
        folderSelectList.innerHTML = '<p class="empty-message">폴더를 불러올 수 없습니다.</p>';
    }
}

// 폴더 선택 모달 숨기기
function hideSelectFolderModal() {
    document.getElementById('select-folder-modal').style.display = 'none';
    pendingVideo = null;
}

// 폴더에 영상 추가
async function addVideoToFolder(folderId, video = null) {
    const videoToAdd = video || pendingVideo;
    if (!videoToAdd) return;
    
    try {
        const response = await fetch(`/api/youtube/folders/${folderId}/videos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(videoToAdd)
        });
        
        const data = await response.json();
        
        if (data.success) {
            hideSelectFolderModal();
            showToast('영상이 추가되었습니다');
        } else {
            alert(data.message || '영상 추가에 실패했습니다.');
        }
    } catch (err) {
        alert('오류가 발생했습니다.');
    }
    
    pendingVideo = null;
}

// 토스트 메시지 표시
function showToast(message) {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    
    toast.textContent = message;
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 2000);
}

// 기존 switchTab 함수 확장
const originalSwitchTab = window.switchTab;
window.switchTab = function(tab, skipLoad = false) {
    // 즐겨찾기 섹션 표시/숨김
    const favoritesSection = document.getElementById('favorites-section');
    const videoList = document.getElementById('video-list');
    const insightsSection = document.getElementById('insights-section');
    
    if (tab === 'favorites') {
        favoritesSection.style.display = 'block';
        videoList.style.display = 'none';
        if (insightsSection) insightsSection.style.display = 'none';
        document.getElementById('regionFilter').style.display = 'none';
        
        // 탭 버튼 활성화
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tab);
        });
        
        if (!skipLoad) {
            showFoldersList();
            loadFolders();
        }
    } else {
        favoritesSection.style.display = 'none';
        videoList.style.display = 'flex';
        
        if (originalSwitchTab) {
            originalSwitchTab(tab, skipLoad);
        }
    }
};
