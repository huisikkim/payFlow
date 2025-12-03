/**
 * YouTube 수익 예측 기능
 */

// 카테고리별 CPM 정보 (원화)
const CATEGORY_CPM = {
    '1': { min: 3000, max: 8000, name: '영화/애니' },
    '2': { min: 2000, max: 5000, name: '자동차' },
    '10': { min: 4000, max: 10000, name: '음악' },
    '15': { min: 2000, max: 6000, name: '반려동물' },
    '17': { min: 3000, max: 8000, name: '스포츠' },
    '19': { min: 2000, max: 5000, name: '여행' },
    '20': { min: 3000, max: 7000, name: '게임' },
    '22': { min: 4000, max: 12000, name: '브이로그' },
    '23': { min: 3000, max: 8000, name: '코미디' },
    '24': { min: 4000, max: 10000, name: '엔터' },
    '25': { min: 3000, max: 8000, name: '뉴스' },
    '26': { min: 5000, max: 15000, name: '하우투' },
    '27': { min: 6000, max: 20000, name: '교육' },
    '28': { min: 8000, max: 25000, name: '과학/기술' },
    '29': { min: 3000, max: 8000, name: '비영리' },
    'default': { min: 3000, max: 8000, name: '일반' }
};

/**
 * 예상 광고 수익 계산
 */
function calculateEstimatedRevenue(video) {
    const viewCount = video.viewCount || 0;
    const categoryId = video.categoryId || 'default';
    
    const cpm = CATEGORY_CPM[categoryId] || CATEGORY_CPM['default'];
    
    // 수익 = (조회수 / 1000) × CPM
    const minRevenue = Math.floor((viewCount / 1000) * cpm.min);
    const maxRevenue = Math.floor((viewCount / 1000) * cpm.max);
    const avgRevenue = Math.floor((minRevenue + maxRevenue) / 2);
    
    return {
        min: minRevenue,
        max: maxRevenue,
        avg: avgRevenue,
        cpm: cpm,
        categoryName: cpm.name
    };
}

/**
 * 수익 잠재력 점수 계산 (0-100)
 */
function calculateRevenuePotential(video) {
    const views = video.viewCount || 0;
    const likes = video.likeCount || 0;
    const comments = video.commentCount || 0;
    const subscribers = video.channelSubscriberCount;
    const categoryId = video.categoryId || 'default';
    
    if (views === 0) return 0;
    
    // 1. 참여율 점수 (40%)
    const engagementRate = ((likes + comments) / views) * 100;
    const engagementScore = Math.min(100, (engagementRate / 10) * 100);
    
    // 2. 바이럴 점수 (30%)
    let viralScore = 0;
    if (subscribers && subscribers > 0) {
        const viralIndex = (views / subscribers) * 100;
        viralScore = Math.min(100, (viralIndex / 500) * 100);
    } else {
        // 구독자 정보 없으면 조회수 기반
        viralScore = Math.min(100, (Math.log10(views + 1) / 6) * 100);
    }
    
    // 3. CPM 점수 (30%)
    const cpm = CATEGORY_CPM[categoryId] || CATEGORY_CPM['default'];
    const cpmScore = Math.min(100, (cpm.max / 25000) * 100);
    
    // 가중 평균
    const totalScore = (engagementScore * 0.4) + (viralScore * 0.3) + (cpmScore * 0.3);
    
    return Math.round(totalScore);
}

/**
 * 수익 잠재력 레벨 정보
 */
function getRevenuePotentialLevel(score) {
    if (score >= 80) {
        return { class: 'revenue-excellent', label: '매우높음', icon: '🚀', color: '#a855f7' };
    } else if (score >= 60) {
        return { class: 'revenue-high', label: '높음', icon: '📈', color: '#ec4899' };
    } else if (score >= 40) {
        return { class: 'revenue-good', label: '보통', icon: '➡️', color: '#06b6d4' };
    } else if (score >= 20) {
        return { class: 'revenue-low', label: '낮음', icon: '📉', color: '#6b7280' };
    } else {
        return { class: 'revenue-very-low', label: '매우낮음', icon: '⚠️', color: '#4b5563' };
    }
}

/**
 * 수익 정보 HTML 생성
 */
function renderRevenueInfo(video) {
    const revenue = calculateEstimatedRevenue(video);
    const potential = calculateRevenuePotential(video);
    const potentialInfo = getRevenuePotentialLevel(potential);
    
    return `
        <div class="metrics-row revenue-section">
            <div class="tooltip-wrapper">
                <div class="revenue-badge ${potentialInfo.class}">
                    <span class="revenue-icon">${potentialInfo.icon}</span>
                    <span class="revenue-label">예상수익</span>
                    <span class="revenue-value">${formatRevenue(revenue.avg)}</span>
                </div>
                <div class="tooltip-content" onclick="event.stopPropagation()">
                    <div class="tooltip-title">💰 예상 광고 수익</div>
                    <div class="tooltip-formula">조회수 ÷ 1,000 × CPM</div>
                    <div class="tooltip-desc">
                        <strong>${revenue.categoryName}</strong> 카테고리 기준<br>
                        최소: ${formatRevenue(revenue.min)}<br>
                        최대: ${formatRevenue(revenue.max)}<br>
                        평균: ${formatRevenue(revenue.avg)}
                    </div>
                    <div class="tooltip-levels">
                        <span>CPM: ${formatNumber(revenue.cpm.min)}~${formatNumber(revenue.cpm.max)}원</span>
                    </div>
                </div>
            </div>
            <div class="tooltip-wrapper">
                <div class="potential-badge ${potentialInfo.class}">
                    <span class="material-symbols-outlined potential-icon">trending_up</span>
                    <span class="potential-label">수익잠재력</span>
                    <span class="potential-value">${potential}점</span>
                    <span class="potential-level">${potentialInfo.label}</span>
                </div>
                <div class="tooltip-content" onclick="event.stopPropagation()">
                    <div class="tooltip-title">📊 수익 잠재력 점수</div>
                    <div class="tooltip-formula">참여율(40%) + 바이럴(30%) + CPM(30%)</div>
                    <div class="tooltip-desc">
                        이 스타일로 영상을 만들면 얼마나 수익성이 좋을지 예측해요.<br>
                        높을수록 같은 조회수로 더 많은 수익을 낼 수 있어요!
                    </div>
                    <div class="tooltip-levels">
                        <span>🚀 매우높음 80↑</span>
                        <span>📈 높음 60↑</span>
                        <span>➡️ 보통 40↑</span>
                        <span>📉 낮음 20↑</span>
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * 월 수익 시뮬레이션 HTML
 */
function renderMonthlySimulation(video, revenue, potential) {
    const scenarios = [
        { videos: 5, label: '주 1회', desc: '여유롭게' },
        { videos: 10, label: '주 2~3회', desc: '꾸준히' },
        { videos: 20, label: '거의 매일', desc: '열심히' }
    ];
    
    let html = '<div class="simulation-grid">';
    
    scenarios.forEach(scenario => {
        const monthlyRevenue = revenue.avg * scenario.videos;
        const potentialInfo = getRevenuePotentialLevel(potential);
        
        html += `
            <div class="simulation-card">
                <div class="simulation-header">
                    <div class="simulation-frequency">${scenario.label}</div>
                    <div class="simulation-desc">${scenario.desc}</div>
                </div>
                <div class="simulation-videos">${scenario.videos}개/월</div>
                <div class="simulation-revenue">${formatRevenue(monthlyRevenue)}</div>
                <div class="simulation-growth">
                    <span class="growth-icon">${potentialInfo.icon}</span>
                    <span class="growth-label">${potentialInfo.label}</span>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    return html;
}

/**
 * 수익 포맷팅 (만원 단위)
 */
function formatRevenue(amount) {
    if (amount >= 10000) {
        const man = Math.floor(amount / 10000);
        const remainder = amount % 10000;
        if (remainder === 0) {
            return `${man}만원`;
        } else {
            return `${man}.${Math.floor(remainder / 1000)}만원`;
        }
    } else if (amount >= 1000) {
        return `${Math.floor(amount / 1000)}천원`;
    } else {
        return `${amount}원`;
    }
}
