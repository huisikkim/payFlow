/**
 * YouTube 필터 & 정렬 모듈
 */

/**
 * 슬라이더 값 업데이트
 */
function updateSliderValue(type, value) {
    const numValue = parseFloat(value);
    let slider, max;
    
    if (type === 'minViews') {
        document.getElementById('minViewsValue').textContent = formatNumber(numValue);
        slider = document.getElementById('minViewsSlider');
        max = 1000000;
    } else if (type === 'minEngagement') {
        document.getElementById('minEngagementValue').textContent = numValue.toFixed(1);
        slider = document.getElementById('minEngagementSlider');
        max = 10;
    } else if (type === 'minPerformance') {
        document.getElementById('minPerformanceValue').textContent = Math.round(numValue);
        slider = document.getElementById('minPerformanceSlider');
        max = 100;
    }
    
    // 슬라이더 진행 바 색상 업데이트
    if (slider) {
        const percentage = (numValue / max) * 100;
        slider.style.background = `linear-gradient(to right, #3b82f6 0%, #3b82f6 ${percentage}%, #374151 ${percentage}%, #374151 100%)`;
    }
    
    // 실시간 필터 적용
    applySortAndFilter();
}

/**
 * 프리셋 적용
 */
function applyPreset(preset) {
    // 모든 프리셋 버튼 비활성화
    document.querySelectorAll('.quick-filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 현재 버튼 활성화
    event.target.closest('.quick-filter-btn').classList.add('active');
    
    // 슬라이더 초기화
    document.getElementById('minViewsSlider').value = 0;
    document.getElementById('minEngagementSlider').value = 0;
    document.getElementById('minPerformanceSlider').value = 0;
    document.getElementById('sortBy').value = 'default';
    
    switch(preset) {
        case 'all':
            // 전체보기 - 모든 필터 해제
            updateSliderValue('minViews', 0);
            updateSliderValue('minEngagement', 0);
            updateSliderValue('minPerformance', 0);
            break;
            
        case 'high-engagement':
            // 고참여율 (5% 이상)
            document.getElementById('minEngagementSlider').value = 5;
            updateSliderValue('minEngagement', 5);
            document.getElementById('sortBy').value = 'engagement-desc';
            break;
            
        case 'viral':
            // 바이럴 영상 (조회수 50만 이상, 참여율 2% 이상)
            document.getElementById('minViewsSlider').value = 500000;
            document.getElementById('minEngagementSlider').value = 2;
            updateSliderValue('minViews', 500000);
            updateSliderValue('minEngagement', 2);
            document.getElementById('sortBy').value = 'views-desc';
            break;
            
        case 'top-performance':
            // 고성과 (성과도 60점 이상)
            document.getElementById('minPerformanceSlider').value = 60;
            updateSliderValue('minPerformance', 60);
            document.getElementById('sortBy').value = 'performance-desc';
            break;
            
        case 'popular':
            // 인기영상 (조회수 10만 이상)
            document.getElementById('minViewsSlider').value = 100000;
            updateSliderValue('minViews', 100000);
            document.getElementById('sortBy').value = 'views-desc';
            break;
    }
    
    applySortAndFilter();
}

/**
 * 필터 및 정렬 적용
 */
function applySortAndFilter() {
    if (!allVideos || allVideos.length === 0) return;
    
    let filteredVideos = [...allVideos];
    const originalCount = filteredVideos.length;
    
    // 슬라이더 값 가져오기
    const minViews = parseInt(document.getElementById('minViewsSlider').value) || 0;
    const minEngagement = parseFloat(document.getElementById('minEngagementSlider').value) || 0;
    const minPerformance = parseInt(document.getElementById('minPerformanceSlider').value) || 0;
    
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
        // 정렬이 적용되었으면 순위 번호 숨기기, 기본 순서면 원래 순위 표시
        const showRank = sortBy === 'default' && currentShowRank;
        renderVideos(filteredVideos, showRank);
    }
    
    // 필터 결과 카운트 업데이트
    updateFilterResultCount(filteredVideos.length, originalCount);
    
    // 정렬 정보 표시
    updateSortInfo(sortBy);
}

/**
 * 필터 초기화
 */
function resetFilters() {
    // 정렬 초기화
    document.getElementById('sortBy').value = 'default';
    
    // 슬라이더 초기화
    document.getElementById('minViewsSlider').value = 0;
    document.getElementById('minEngagementSlider').value = 0;
    document.getElementById('minPerformanceSlider').value = 0;
    
    // 표시값 초기화
    updateSliderValue('minViews', 0);
    updateSliderValue('minEngagement', 0);
    updateSliderValue('minPerformance', 0);
    
    // 프리셋 버튼 초기화
    document.querySelectorAll('.quick-filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector('[data-preset="all"]')?.classList.add('active');
    
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

/**
 * 정렬 정보 표시
 */
function updateSortInfo(sortBy) {
    const sortInfoEl = document.getElementById('sortInfo');
    const sortInfoText = document.getElementById('sortInfoText');
    
    const sortLabels = {
        'views-desc': '조회수 높은순으로 정렬됨',
        'views-asc': '조회수 낮은순으로 정렬됨',
        'engagement-desc': '참여율 높은순으로 정렬됨',
        'engagement-asc': '참여율 낮은순으로 정렬됨',
        'performance-desc': '성과도 높은순으로 정렬됨',
        'performance-asc': '성과도 낮은순으로 정렬됨',
        'likes-desc': '좋아요 많은순으로 정렬됨',
        'comments-desc': '댓글 많은순으로 정렬됨'
    };
    
    if (sortBy !== 'default' && sortLabels[sortBy]) {
        sortInfoEl.style.display = 'flex';
        sortInfoText.textContent = sortLabels[sortBy];
    } else {
        sortInfoEl.style.display = 'none';
    }
}
