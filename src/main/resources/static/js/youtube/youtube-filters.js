/**
 * YouTube 필터 & 정렬 모듈
 */

/**
 * 필터 및 정렬 적용
 */
function applySortAndFilter() {
    if (!allVideos || allVideos.length === 0) return;
    
    let filteredVideos = [...allVideos];
    const originalCount = filteredVideos.length;
    
    // 필터 적용
    const minViews = parseInt(document.getElementById('minViews').value) || 0;
    const minEngagement = parseFloat(document.getElementById('minEngagement').value) || 0;
    const minPerformance = parseInt(document.getElementById('minPerformance').value) || 0;
    
    filteredVideos = filteredVideos.filter(video => {
        const views = video.viewCount || 0;
        const engagement = parseFloat(calculateEngagementRate(video));
        const performance = calculatePerformanceScore(video);
        
        return views >= minViews && 
               engagement >= minEngagement && 
               performance >= minPerformance;
    });
    
    // 정렬 적용
    const sortBy = document.getElementById('sortBy').value;
    
    switch(sortBy) {
        case 'views-desc':
            filteredVideos.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0));
            break;
        case 'views-asc':
            filteredVideos.sort((a, b) => (a.viewCount || 0) - (b.viewCount || 0));
            break;
        case 'engagement-desc':
            filteredVideos.sort((a, b) => {
                const engA = parseFloat(calculateEngagementRate(a));
                const engB = parseFloat(calculateEngagementRate(b));
                return engB - engA;
            });
            break;
        case 'engagement-asc':
            filteredVideos.sort((a, b) => {
                const engA = parseFloat(calculateEngagementRate(a));
                const engB = parseFloat(calculateEngagementRate(b));
                return engA - engB;
            });
            break;
        case 'performance-desc':
            filteredVideos.sort((a, b) => {
                const perfA = calculatePerformanceScore(a);
                const perfB = calculatePerformanceScore(b);
                return perfB - perfA;
            });
            break;
        case 'performance-asc':
            filteredVideos.sort((a, b) => {
                const perfA = calculatePerformanceScore(a);
                const perfB = calculatePerformanceScore(b);
                return perfA - perfB;
            });
            break;
        case 'likes-desc':
            filteredVideos.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
            break;
        case 'comments-desc':
            filteredVideos.sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0));
            break;
        case 'default':
        default:
            // 기본 순서 유지
            break;
    }
    
    // 결과 렌더링
    const list = document.getElementById('video-list');
    list.innerHTML = '';
    
    if (filteredVideos.length === 0) {
        showError('필터 조건에 맞는 영상이 없습니다. 필터를 조정해보세요.');
    } else {
        document.getElementById('error').style.display = 'none';
        renderVideos(filteredVideos, currentShowRank);
    }
    
    // 필터 결과 카운트 업데이트
    updateFilterResultCount(filteredVideos.length, originalCount);
}

/**
 * 필터 초기화
 */
function resetFilters() {
    document.getElementById('sortBy').value = 'default';
    document.getElementById('minViews').value = '';
    document.getElementById('minEngagement').value = '';
    document.getElementById('minPerformance').value = '';
    
    if (allVideos && allVideos.length > 0) {
        document.getElementById('error').style.display = 'none';
        renderVideos(allVideos, currentShowRank);
        updateFilterResultCount(allVideos.length, allVideos.length);
    }
}

/**
 * 필터 결과 카운트 표시
 */
function updateFilterResultCount(filtered, total) {
    const countEl = document.getElementById('filterResultCount');
    const textEl = document.getElementById('filterResultText');
    
    if (filtered < total) {
        countEl.style.display = 'flex';
        textEl.textContent = `${filtered}개 영상 표시 중 (전체 ${total}개)`;
    } else {
        countEl.style.display = 'none';
    }
}
