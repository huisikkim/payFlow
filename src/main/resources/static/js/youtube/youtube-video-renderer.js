/**
 * YouTube 영상 렌더링 모듈
 */

/**
 * 영상 리스트 렌더링
 */
function renderVideos(videos, showRank = true) {
    const list = document.getElementById('video-list');
    list.innerHTML = '';
    
    videos.forEach((video, index) => {
        const item = document.createElement('div');
        item.className = 'video-item';
        item.onclick = () => window.open(`https://www.youtube.com/watch?v=${video.videoId}`, '_blank');
        
        const engagementRate = calculateEngagementRate(video);
        const engagementInfo = getEngagementLevel(engagementRate);
        const performanceScore = calculatePerformanceScore(video);
        const performanceInfo = getPerformanceLevel(performanceScore);
        const viralIndex = calculateViralIndex(video);
        const viralInfo = getViralLevel(viralIndex);
        const explosiveness = calculateExplosiveness(video);
        const explosivenessInfo = getExplosivenessLevel(explosiveness);
        
        item.innerHTML = `
            <div class="video-item-left">
                ${showRank ? `<div class="rank-number">${index + 1}</div>` : ''}
                <div class="thumbnail-wrapper" data-video-id="${video.videoId}" onmouseenter="showVideoPreview(this)" onmouseleave="hideVideoPreview(this)">
                    <img class="thumbnail" src="${video.thumbnailUrl || ''}" alt="${escapeHtml(video.title)}" loading="lazy">
                    <div class="video-preview-container"></div>
                    <div class="performance-score ${performanceInfo.class}" title="${performanceInfo.tooltip}">
                        <span class="score-value">${performanceScore}</span>
                        <span class="score-label">점</span>
                    </div>
                    ${video.duration ? `<div class="duration-badge">${formatDuration(video.duration)}</div>` : ''}
                    <div class="preview-indicator">
                        <span class="material-symbols-outlined">play_circle</span>
                    </div>
                    <button class="btn-favorite" onclick="event.stopPropagation(); addToFavorites(${JSON.stringify(video).replace(/"/g, '&quot;')})" title="즐겨찾기에 추가">
                        <span class="material-symbols-outlined">bookmark_add</span>
                    </button>
                </div>
            </div>
            <div class="video-content">
                <div class="video-title">${escapeHtml(video.title)}</div>
                <div class="channel-name">${escapeHtml(video.channelTitle || '')}${video.channelSubscriberCount ? ` · 구독자 ${formatNumber(video.channelSubscriberCount)}명` : ''}</div>
                <div class="metrics-row">
                    <div class="tooltip-wrapper" onclick="event.stopPropagation()">
                        <div class="engagement-badge ${engagementInfo.class}">
                            <span class="material-symbols-outlined engagement-icon">local_fire_department</span>
                            <span class="engagement-label">참여율</span>
                            <span class="engagement-value">${engagementRate}%</span>
                            <span class="engagement-level">${engagementInfo.label}</span>
                        </div>
                        <div class="tooltip-content" onclick="event.stopPropagation()">
                            <div class="tooltip-title">🔥 참여율 (Engagement Rate)</div>
                            <div class="tooltip-formula">(좋아요 + 댓글) ÷ 조회수 × 100</div>
                            <div class="tooltip-desc">시청자들이 영상에 얼마나 적극적으로 반응하는지 나타내요. 높을수록 시청자 참여도가 좋은 영상이에요.</div>
                            <div class="tooltip-levels">
                                <span>🔴 최고 10%↑</span>
                                <span>🟠 높음 5%↑</span>
                                <span>🟢 좋음 2%↑</span>
                                <span>🔵 보통 1%↑</span>
                            </div>
                        </div>
                    </div>
                    <div class="tooltip-wrapper" onclick="event.stopPropagation()">
                        <div class="performance-badge ${performanceInfo.class}">
                            <span class="material-symbols-outlined performance-icon">star</span>
                            <span class="performance-label">성과도</span>
                            <span class="performance-value">${performanceScore}</span>
                            <span class="performance-level">${performanceInfo.label}</span>
                        </div>
                        <div class="tooltip-content" onclick="event.stopPropagation()">
                            <div class="tooltip-title">⭐ 종합 성과도</div>
                            <div class="tooltip-formula">바이럴속도(30%) + 좋아요비율(30%) + 참여율(40%)</div>
                            <div class="tooltip-desc">여러 지표를 종합해서 영상의 전체적인 성과를 0~100점으로 평가해요.</div>
                            <div class="tooltip-levels">
                                <span>🟣 S등급 80↑</span>
                                <span>🩷 A등급 60↑</span>
                                <span>🩵 B등급 40↑</span>
                                <span>🩶 C등급 20↑</span>
                            </div>
                        </div>
                    </div>
                    ${viralIndex !== null ? `
                    <div class="tooltip-wrapper" onclick="event.stopPropagation()">
                        <div class="viral-badge ${viralInfo.class}">
                            <span class="material-symbols-outlined viral-icon">rocket_launch</span>
                            <span class="viral-label">바이럴</span>
                            <span class="viral-value">${viralIndex}%</span>
                            <span class="viral-level">${viralInfo.label}</span>
                        </div>
                        <div class="tooltip-content" onclick="event.stopPropagation()">
                            <div class="tooltip-title">🚀 바이럴 지수</div>
                            <div class="tooltip-formula">조회수 ÷ 채널 구독자수 × 100</div>
                            <div class="tooltip-desc">구독자 대비 조회수 비율이에요. 100% 이상이면 비구독자 유입이 많다는 뜻으로, 유튜브 알고리즘 추천을 잘 받고 있어요!</div>
                            <div class="tooltip-levels">
                                <span>🔴 대박 500%↑</span>
                                <span>🟠 폭발 200%↑</span>
                                <span>🟡 확산 100%↑</span>
                                <span>🟢 양호 50%↑</span>
                            </div>
                        </div>
                    </div>
                    ` : ''}
                    <div class="tooltip-wrapper" onclick="event.stopPropagation()">
                        <div class="explosiveness-badge ${explosivenessInfo.class}">
                            <span class="material-symbols-outlined explosiveness-icon">bolt</span>
                            <span class="explosiveness-label">폭발력</span>
                            <span class="explosiveness-value">${explosiveness}</span>
                            <span class="explosiveness-level">${explosivenessInfo.label}</span>
                        </div>
                        <div class="tooltip-content" onclick="event.stopPropagation()">
                            <div class="tooltip-title">⚡ 폭발력 (시간당 조회수)</div>
                            <div class="tooltip-formula">조회수 ÷ 업로드 후 경과 시간</div>
                            <div class="tooltip-desc">영상이 얼마나 빠르게 퍼지고 있는지 나타내요. 높을수록 현재 핫한 영상이에요!</div>
                            <div class="tooltip-levels">
                                <span>🔴 초고속 10만/h↑</span>
                                <span>🟠 고속 5만/h↑</span>
                                <span>🟡 빠름 1만/h↑</span>
                                <span>🟢 보통 1천/h↑</span>
                            </div>
                        </div>
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
                ${renderContactInfo(video)}
                <button class="btn-analyze-video" onclick="event.stopPropagation(); analyzeVideoFromList('${video.videoId}')" title="이 영상 분석하기">
                    <span class="material-symbols-outlined">analytics</span>
                    <span>영상 분석</span>
                </button>
            </div>
        `;
        
        list.appendChild(item);
    });
}

/**
 * 연락처 정보 렌더링
 */
function renderContactInfo(video) {
    const hasContact = video.channelEmail || video.channelInstagram || video.channelTwitter || video.channelWebsite;
    if (!hasContact) return '';
    
    let contactHtml = '<div class="contact-info">';
    
    if (video.channelEmail) {
        contactHtml += `<a href="mailto:${escapeHtml(video.channelEmail)}" class="contact-item email" onclick="event.stopPropagation()" title="이메일">
            <span class="material-symbols-outlined">mail</span>
        </a>`;
    }
    if (video.channelInstagram) {
        contactHtml += `<a href="https://instagram.com/${video.channelInstagram.replace('@', '')}" target="_blank" class="contact-item instagram" onclick="event.stopPropagation()" title="Instagram ${escapeHtml(video.channelInstagram)}">
            <span class="instagram-icon">📸</span>
        </a>`;
    }
    if (video.channelTwitter) {
        contactHtml += `<a href="https://twitter.com/${video.channelTwitter.replace('@', '')}" target="_blank" class="contact-item twitter" onclick="event.stopPropagation()" title="Twitter ${escapeHtml(video.channelTwitter)}">
            <span class="twitter-icon">𝕏</span>
        </a>`;
    }
    if (video.channelWebsite) {
        contactHtml += `<a href="${escapeHtml(video.channelWebsite)}" target="_blank" class="contact-item website" onclick="event.stopPropagation()" title="웹사이트">
            <span class="material-symbols-outlined">language</span>
        </a>`;
    }
    
    contactHtml += '</div>';
    return contactHtml;
}

/**
 * 비디오 미리보기
 */
let previewTimer = null;
let currentPreviewElement = null;

function showVideoPreview(element) {
    const videoId = element.dataset.videoId;
    if (!videoId) return;
    
    if (previewTimer) {
        clearTimeout(previewTimer);
    }
    
    previewTimer = setTimeout(() => {
        const container = element.querySelector('.video-preview-container');
        if (!container) return;
        
        container.innerHTML = `
            <iframe 
                src="https://www.youtube.com/embed/${videoId}?autoplay=1&mute=1&controls=0&modestbranding=1&rel=0&showinfo=0&start=0&enablejsapi=1"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen
                class="video-preview-iframe"
            ></iframe>
        `;
        
        container.classList.add('active');
        element.querySelector('.thumbnail').style.opacity = '0';
        element.querySelector('.preview-indicator')?.classList.add('hidden');
        currentPreviewElement = element;
    }, 500);
}

function hideVideoPreview(element) {
    if (previewTimer) {
        clearTimeout(previewTimer);
        previewTimer = null;
    }
    
    const container = element.querySelector('.video-preview-container');
    if (!container) return;
    
    container.innerHTML = '';
    container.classList.remove('active');
    element.querySelector('.thumbnail').style.opacity = '1';
    element.querySelector('.preview-indicator')?.classList.remove('hidden');
    currentPreviewElement = null;
}

/**
 * 즐겨찾기에 추가
 */
function addToFavorites(video) {
    const videoData = {
        videoId: video.videoId,
        title: video.title,
        channelTitle: video.channelTitle,
        thumbnailUrl: video.thumbnailUrl,
        viewCount: video.viewCount,
        likeCount: video.likeCount,
        duration: video.duration
    };
    
    if (typeof showSelectFolderModal === 'function') {
        showSelectFolderModal(videoData);
    } else {
        alert('즐겨찾기 기능을 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
    }
}

/**
 * 영상 분석 페이지로 이동
 */
function analyzeVideoFromList(videoId) {
    // 영상 분석 페이지로 이동하면서 videoId를 URL 파라미터로 전달
    window.location.href = `/youtube/analysis?videoId=${videoId}`;
}
