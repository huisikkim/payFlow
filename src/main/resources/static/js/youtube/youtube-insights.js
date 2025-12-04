/**
 * YouTube 인사이트 생성 모듈
 */

/**
 * 크리에이터 인사이트 생성
 */
function generateInsights(videos) {
    if (!videos || videos.length === 0) {
        document.getElementById('insights-section').style.display = 'none';
        return;
    }
    
    document.getElementById('insights-section').style.display = 'block';
    
    // 1. 인기 키워드 추출
    const keywords = extractKeywords(videos);
    document.getElementById('insight-keywords').textContent = keywords.slice(0, 3).join(', ') || '-';
    
    // 2. 평균 영상 길이
    const avgDuration = calculateAverageDuration(videos);
    document.getElementById('insight-duration').textContent = avgDuration;
    
    // 3. 평균 참여율
    const avgEngagement = calculateAverageEngagement(videos);
    document.getElementById('insight-engagement').textContent = avgEngagement + '%';
    
    // 4. 평균 조회수
    const avgViews = calculateAverageViews(videos);
    document.getElementById('insight-views').textContent = formatNumber(avgViews);
    
    // 5. 평균 제목 길이
    const avgTitleLength = calculateAverageTitleLength(videos);
    document.getElementById('insight-title-length').textContent = avgTitleLength + '자';
    
    // 6. 참여율 TOP 영상
    const topEngagementEl = document.getElementById('insight-top-engagement');
    const topVideo = findTopEngagementVideo(videos);
    if (topVideo.video) {
        topEngagementEl.innerHTML = `<a href="https://www.youtube.com/watch?v=${topVideo.video.videoId}" target="_blank" class="top-video-link" title="클릭하면 YouTube로 이동">${escapeHtml(topVideo.text)}</a>`;
    } else {
        topEngagementEl.textContent = '-';
    }
    
    // 7. 콘텐츠 제작 팁 생성
    const tips = generateTipsNew(videos, avgDuration, avgEngagement, avgTitleLength);
    document.getElementById('insight-tips').innerHTML = tips;
    
    // 8. TOP 영상 리스트 생성
    renderTopVideosList(videos);
}

/**
 * TOP 영상 리스트 렌더링
 */
function renderTopVideosList(videos) {
    const listEl = document.getElementById('insight-top-videos-list');
    if (!listEl) return;
    
    const topVideos = getTopEngagementVideos(videos, 3);
    const avgEngagement = parseFloat(calculateAverageEngagement(videos));
    
    listEl.innerHTML = topVideos.map(item => {
        const diff = (item.rate - avgEngagement).toFixed(1);
        const isPositive = diff >= 0;
        const title = item.video.title?.length > 15 ? item.video.title.substring(0, 15) + '...' : item.video.title;
        
        return `
            <li class="top-video-item" onclick="window.open('https://www.youtube.com/watch?v=${item.video.videoId}', '_blank')">
                <div class="top-video-item-left">
                    <img src="${item.video.thumbnailUrl || ''}" alt="" class="top-video-thumb">
                    <div class="top-video-info">
                        <div class="top-video-title">${escapeHtml(title)}</div>
                        <div class="top-video-engagement">${item.rate.toFixed(2)}% 참여율</div>
                    </div>
                </div>
                <div class="top-video-change ${isPositive ? 'positive' : 'negative'}">
                    ${isPositive ? '+' : ''}${diff}%
                </div>
            </li>
        `;
    }).join('');
}

/**
 * 참여율 상위 N개 영상 가져오기
 */
function getTopEngagementVideos(videos, n) {
    return videos
        .map(video => {
            const views = video.viewCount || 0;
            const likes = video.likeCount || 0;
            const comments = video.commentCount || 0;
            const rate = views > 0 ? ((likes + comments) / views) * 100 : 0;
            return { video, rate };
        })
        .sort((a, b) => b.rate - a.rate)
        .slice(0, n);
}

/**
 * 팁 생성
 */
function generateTipsNew(videos, avgDuration, avgEngagement, avgTitleLength) {
    const tips = [];
    
    // 영상 길이 팁
    const durationMatch = avgDuration.match(/(\d+)분/);
    if (durationMatch) {
        const mins = parseInt(durationMatch[1]);
        if (mins <= 5) {
            tips.push('현재 트렌드는 <strong>5분 이하 숏폼</strong> 콘텐츠가 인기예요');
        } else if (mins <= 15) {
            tips.push(`평균 <strong>${mins}분</strong> 길이의 영상이 인기 - 핵심만 담은 콘텐츠 추천`);
        } else {
            tips.push('긴 영상도 인기! <strong>깊이 있는 고퀄리티 콘텐츠</strong>로 승부해보세요');
        }
    }
    
    // 제목 팁
    if (avgTitleLength < 30) {
        tips.push(`제목은 <strong>${avgTitleLength}자</strong> 내외로 간결하게, 핵심 키워드를 앞에 배치하세요`);
    } else {
        tips.push('제목에 <strong>키워드</strong>를 충분히 포함하되 핵심을 앞에 배치하세요');
    }
    
    // 참여율 팁
    const engRate = parseFloat(avgEngagement);
    if (engRate >= 3) {
        tips.push('참여율이 높은 트렌드! <strong>댓글 유도 질문</strong>을 영상에 포함하세요');
    } else {
        tips.push('영상 끝에 <strong>좋아요/구독 요청</strong>과 <strong>질문</strong>을 넣어 참여를 유도하세요');
    }
    
    // 키워드 팁
    const keywords = extractKeywords(videos).slice(0, 3);
    if (keywords.length > 0) {
        tips.push(`인기 키워드: <strong>${keywords.join(', ')}</strong> - 제목/태그에 활용하세요`);
    }
    
    return tips.map(tip => `
        <div class="tip-item">
            <span class="material-symbols-outlined">check_circle</span>
            <p>${tip}</p>
        </div>
    `).join('');
}

/**
 * 키워드 추출
 */
function extractKeywords(videos) {
    const stopWords = ['the', 'a', 'an', 'is', 'are', 'was', 'were', 'be', 'been', 'being', 
        'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would', 'could', 'should',
        'of', 'at', 'by', 'for', 'with', 'about', 'against', 'between', 'into', 'through',
        'during', 'before', 'after', 'above', 'below', 'to', 'from', 'up', 'down', 'in', 'out',
        'on', 'off', 'over', 'under', 'again', 'further', 'then', 'once', 'and', 'but', 'or',
        'MV', 'Official', 'Video', 'M/V', 'ver', 'ver.', 'version', 'EP', 'Full', 'HD', '4K',
        '|', '-', '/', '(', ')', '[', ']', '#', '&', '!', '?', '~', '...'];
    
    const wordCount = {};
    
    videos.forEach(video => {
        if (!video.title) return;
        
        const words = video.title
            .replace(/[^\w\sㄱ-ㅎㅏ-ㅣ가-힣]/g, ' ')
            .split(/\s+/)
            .filter(word => word.length >= 2)
            .filter(word => !stopWords.includes(word.toLowerCase()));
        
        words.forEach(word => {
            const key = word.toLowerCase();
            wordCount[key] = (wordCount[key] || 0) + 1;
        });
    });
    
    return Object.entries(wordCount)
        .sort((a, b) => b[1] - a[1])
        .map(entry => entry[0]);
}

/**
 * 평균 영상 길이 계산
 */
function calculateAverageDuration(videos) {
    let totalSeconds = 0;
    let count = 0;
    
    videos.forEach(video => {
        if (!video.duration) return;
        const match = video.duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
        if (!match) return;
        
        const hours = parseInt(match[1]) || 0;
        const minutes = parseInt(match[2]) || 0;
        const seconds = parseInt(match[3]) || 0;
        
        totalSeconds += hours * 3600 + minutes * 60 + seconds;
        count++;
    });
    
    if (count === 0) return '-';
    
    const avgSeconds = Math.round(totalSeconds / count);
    const mins = Math.floor(avgSeconds / 60);
    const secs = avgSeconds % 60;
    
    return `${mins}분 ${secs}초`;
}

/**
 * 평균 참여율 계산
 */
function calculateAverageEngagement(videos) {
    let total = 0;
    let count = 0;
    
    videos.forEach(video => {
        const views = video.viewCount || 0;
        const likes = video.likeCount || 0;
        const comments = video.commentCount || 0;
        
        if (views > 0) {
            total += ((likes + comments) / views) * 100;
            count++;
        }
    });
    
    return count > 0 ? (total / count).toFixed(2) : '0';
}

/**
 * 평균 조회수 계산
 */
function calculateAverageViews(videos) {
    let total = 0;
    videos.forEach(video => {
        total += video.viewCount || 0;
    });
    return videos.length > 0 ? Math.round(total / videos.length) : 0;
}

/**
 * 평균 제목 길이 계산
 */
function calculateAverageTitleLength(videos) {
    let total = 0;
    videos.forEach(video => {
        total += (video.title || '').length;
    });
    return videos.length > 0 ? Math.round(total / videos.length) : 0;
}

/**
 * TOP 참여율 영상 찾기
 */
function findTopEngagementVideo(videos) {
    let topVideo = null;
    let topRate = 0;
    
    videos.forEach(video => {
        const views = video.viewCount || 0;
        const likes = video.likeCount || 0;
        const comments = video.commentCount || 0;
        
        if (views > 0) {
            const rate = ((likes + comments) / views) * 100;
            if (rate > topRate) {
                topRate = rate;
                topVideo = video;
            }
        }
    });
    
    if (!topVideo) return { video: null, text: '-' };
    
    const title = topVideo.title?.length > 25 
        ? topVideo.title.substring(0, 25) + '...' 
        : topVideo.title;
    
    return {
        video: topVideo,
        text: `${title} (${topRate.toFixed(2)}%)`
    };
}
