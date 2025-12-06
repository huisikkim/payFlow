// YouTube 영상 분석 JavaScript

let currentReport = null;

/**
 * 예시 URL 설정
 */
function setExampleUrl(url) {
    document.getElementById('videoUrl').value = url;
}

/**
 * 영상 분석 시작
 */
async function analyzeVideo() {
    const urlInput = document.getElementById('videoUrl');
    const url = urlInput.value.trim();
    
    if (!url) {
        showError('YouTube URL 또는 Video ID를 입력해주세요.');
        return;
    }
    
    // UI 초기화
    hideError();
    hideReport();
    showLoading();
    
    try {
        // API 호출
        const response = await fetch('/api/youtube/analysis/url', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ url: url })
        });
        
        const data = await response.json();
        
        if (!data.success) {
            throw new Error(data.message || '영상 분석에 실패했습니다.');
        }
        
        // 리포트 표시
        currentReport = data.report;
        displayReport(currentReport);
        
    } catch (error) {
        console.error('분석 오류:', error);
        showError(error.message || '영상 분석 중 오류가 발생했습니다.');
    } finally {
        hideLoading();
    }
}

/**
 * 리포트 표시
 */
function displayReport(report) {
    // 영상 정보
    document.getElementById('thumbnail').src = report.thumbnailUrl || '';
    document.getElementById('videoTitle').textContent = report.videoTitle || '제목 없음';
    document.getElementById('channelName').textContent = report.channel || '채널 없음';
    document.getElementById('subscriberCount').textContent = 
        report.channelSubscribers ? formatNumber(report.channelSubscribers) + ' 구독자' : '';
    document.getElementById('publishDate').textContent = 
        report.publishedAt ? '업로드: ' + formatDate(report.publishedAt) : '';
    
    // 종합 점수
    document.getElementById('overallScore').textContent = report.overallScore || 0;
    document.getElementById('gradeBadge').textContent = report.overallGrade || '-';
    document.getElementById('scoreDescription').textContent = getScoreDescription(report.overallScore);
    
    // 핵심 지표
    document.getElementById('currentViews').textContent = formatNumber(report.currentViews || 0);
    document.getElementById('dailyGrowth').textContent = formatNumber(Math.round(report.dailyGrowthRate || 0)) + '/일';
    document.getElementById('predictedViews').textContent = formatNumber(report.predictedViews || 0);
    document.getElementById('predictedGrowth').textContent = formatNumber(report.predictedGrowth || 0);
    document.getElementById('currentRevenue').textContent = '₩' + formatNumber(report.avgRevenue || 0);
    document.getElementById('revenueRange').textContent = 
        '₩' + formatNumber(report.minRevenue || 0) + ' ~ ₩' + formatNumber(report.maxRevenue || 0);
    document.getElementById('predictedRevenue').textContent = '₩' + formatNumber(report.predictedRevenue || 0);
    
    // 경쟁 분석
    const competitionScore = report.competitionScore || 0;
    document.getElementById('competitionBar').style.width = competitionScore + '%';
    document.getElementById('competitionScore').textContent = competitionScore;
    document.getElementById('competitionLevel').textContent = report.competitionLevel || '-';
    document.getElementById('recentCompetitors').textContent = formatNumber(report.recentCompetitors || 0);
    document.getElementById('avgCompetitorViews').textContent = formatNumber(report.avgCompetitorViews || 0);
    document.getElementById('competitionRecommendation').textContent = report.competitionRecommendation || '';
    
    // 참여율 분석
    document.getElementById('likeCount').textContent = formatNumber(report.currentLikes || 0);
    document.getElementById('commentCount').textContent = formatNumber(report.currentComments || 0);
    const engagementRate = report.engagementRate || 0;
    document.getElementById('engagementRate').textContent = engagementRate.toFixed(2) + '%';
    document.getElementById('engagementBar').style.width = Math.min(engagementRate * 10, 100) + '%';
    document.getElementById('engagementDescription').textContent = getEngagementDescription(engagementRate);
    
    // 추천 사항
    displayRecommendations(report.recommendations || []);
    displayTitleRecommendations(report.recommendedTitles || []);
    displayKeywords(report.extractedKeywords || []);
    
    // 연락처 정보
    displayContactInfo(report);
    
    // ===== 새로운 분석 결과 표시 =====
    
    // 경쟁 영상 상세 통계
    if (report.competitorAnalysis) {
        displayCompetitorAnalysis(report.competitorAnalysis);
    }
    
    // 제목 분석
    if (report.titleAnalysis) {
        displayTitleAnalysis(report.titleAnalysis);
    }
    
    // SEO 분석
    if (report.seoAnalysis) {
        displaySeoAnalysis(report.seoAnalysis);
    }
    
    // CTR 추정
    if (report.ctrEstimate) {
        displayCtrEstimate(report.ctrEstimate);
    }
    
    // 품질 점수
    if (report.qualityScore) {
        displayQualityScore(report.qualityScore);
    }
    
    // 리포트 섹션 표시
    showReport();
}

/**
 * 추천 사항 표시
 */
function displayRecommendations(recommendations) {
    const container = document.getElementById('recommendationsList');
    container.innerHTML = '';
    
    if (recommendations.length === 0) {
        container.innerHTML = '<p class="no-contact">추천 사항이 없습니다.</p>';
        return;
    }
    
    recommendations.forEach(rec => {
        const div = document.createElement('div');
        div.className = 'recommendation-item';
        div.textContent = rec;
        container.appendChild(div);
    });
}

/**
 * 제목 추천 표시
 */
function displayTitleRecommendations(titles) {
    const container = document.getElementById('titleRecommendations');
    container.innerHTML = '';
    
    if (titles.length === 0) {
        container.innerHTML = '<p class="no-contact">제목 추천이 없습니다.</p>';
        return;
    }
    
    titles.forEach(title => {
        const div = document.createElement('div');
        div.className = 'title-item';
        div.textContent = title;
        div.onclick = () => {
            navigator.clipboard.writeText(title);
            alert('제목이 클립보드에 복사되었습니다!');
        };
        container.appendChild(div);
    });
}

/**
 * 키워드 표시
 */
function displayKeywords(keywords) {
    const container = document.getElementById('keywords');
    container.innerHTML = '';
    
    if (keywords.length === 0) {
        container.innerHTML = '<p class="no-contact">추출된 키워드가 없습니다.</p>';
        return;
    }
    
    keywords.forEach(keyword => {
        const span = document.createElement('span');
        span.className = 'keyword-tag';
        span.textContent = keyword;
        container.appendChild(span);
    });
}

/**
 * 연락처 정보 표시
 */
function displayContactInfo(report) {
    const container = document.getElementById('contactInfo');
    container.innerHTML = '';
    
    let hasContact = false;
    
    if (report.channelEmail) {
        hasContact = true;
        container.innerHTML += `
            <div class="contact-item">
                <span class="material-symbols-outlined">email</span>
                <span>이메일: ${report.channelEmail}</span>
            </div>
        `;
    }
    
    if (report.channelInstagram) {
        hasContact = true;
        container.innerHTML += `
            <div class="contact-item">
                <span class="material-symbols-outlined">photo_camera</span>
                <span>Instagram: ${report.channelInstagram}</span>
            </div>
        `;
    }
    
    if (report.channelTwitter) {
        hasContact = true;
        container.innerHTML += `
            <div class="contact-item">
                <span class="material-symbols-outlined">chat</span>
                <span>Twitter: ${report.channelTwitter}</span>
            </div>
        `;
    }
    
    if (report.channelWebsite) {
        hasContact = true;
        container.innerHTML += `
            <div class="contact-item">
                <span class="material-symbols-outlined">language</span>
                <span>웹사이트: <a href="${report.channelWebsite}" target="_blank">${report.channelWebsite}</a></span>
            </div>
        `;
    }
    
    if (!hasContact) {
        container.innerHTML = '<p class="no-contact">채널 설명에서 연락처를 찾을 수 없습니다.</p>';
    }
}

/**
 * 탭 전환
 */
function switchTab(tabName) {
    // 모든 탭 버튼 비활성화
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 모든 탭 콘텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    // 선택된 탭 활성화
    event.target.classList.add('active');
    document.getElementById(tabName + 'Tab').classList.add('active');
}

/**
 * 점수 설명
 */
function getScoreDescription(score) {
    if (score >= 90) return '🏆 최상급 영상입니다! 모든 지표가 우수합니다.';
    if (score >= 80) return '⭐ 매우 좋은 영상입니다. 수익성이 높습니다.';
    if (score >= 70) return '👍 좋은 영상입니다. 개선의 여지가 있습니다.';
    if (score >= 60) return '✅ 평균 이상의 영상입니다.';
    if (score >= 50) return '📊 평균 수준의 영상입니다.';
    return '💡 개선이 필요한 영상입니다.';
}

/**
 * 참여율 설명
 */
function getEngagementDescription(rate) {
    if (rate > 10) return '🔥 참여율이 매우 높습니다! 알고리즘이 선호하는 콘텐츠입니다.';
    if (rate > 5) return '⭐ 참여율이 높습니다. 시청자들이 적극적으로 반응하고 있습니다.';
    if (rate > 2) return '👍 참여율이 좋은 편입니다.';
    if (rate > 1) return '✅ 참여율이 평균 수준입니다.';
    return '💡 참여율이 낮습니다. 시청자와의 소통을 늘려보세요.';
}

/**
 * 숫자 포맷팅
 */
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 1) return '오늘';
    if (diffDays < 7) return diffDays + '일 전';
    if (diffDays < 30) return Math.floor(diffDays / 7) + '주 전';
    if (diffDays < 365) return Math.floor(diffDays / 30) + '개월 전';
    return Math.floor(diffDays / 365) + '년 전';
}

/**
 * 경쟁 영상 상세 분석 표시
 */
function displayCompetitorAnalysis(analysis) {
    // 통계 표시
    document.getElementById('viewsMedian').textContent = formatNumber(analysis.viewsMedian || 0);
    document.getElementById('viewsAverage').textContent = formatNumber(analysis.viewsAverage || 0);
    document.getElementById('engagementAverage').textContent = (analysis.engagementAverage || 0).toFixed(2) + '%';
    
    // 성장 패턴 표시
    const patternsContainer = document.getElementById('growthPatterns');
    if (analysis.growthPatterns && analysis.growthPatterns.length > 0) {
        let html = '<h4>🚀 성장 패턴 (일일 조회수 기준)</h4><div class="patterns-list">';
        analysis.growthPatterns.slice(0, 5).forEach(pattern => {
            html += `
                <div class="pattern-item">
                    <p class="pattern-title">${pattern.title}</p>
                    <p class="pattern-stats">
                        조회수: ${formatNumber(pattern.views)} | 
                        일일: ${formatNumber(pattern.viewsPerDay)} | 
                        ${pattern.daysOld}일 전 | 
                        <span class="growth-badge">${pattern.growthRate}</span>
                    </p>
                </div>
            `;
        });
        html += '</div>';
        patternsContainer.innerHTML = html;
    }
}

/**
 * 제목 분석 표시
 */
function displayTitleAnalysis(analysis) {
    const container = document.getElementById('titleAnalysisContent');
    
    let html = `
        <div class="analysis-card">
            <div class="score-header">
                <h4>제목 최적화 점수</h4>
                <div class="score-badge-large">${analysis.score}/100</div>
            </div>
            
            <div class="analysis-details">
                <p><strong>제목:</strong> ${analysis.title}</p>
                <p><strong>길이:</strong> ${analysis.length}자 
                    ${analysis.isOptimalLength ? '✅ 최적' : '⚠️ ' + (analysis.length < 40 ? '너무 짧음' : '너무 김')}
                </p>
                <p><strong>단어 수:</strong> ${analysis.wordCount}개</p>
                <p><strong>숫자 포함:</strong> ${analysis.hasNumbers ? '✅ 예' : '❌ 아니오'}</p>
                <p><strong>감정 단어:</strong> ${analysis.hasEmotionalWords ? '✅ 포함' : '❌ 없음'}</p>
                <p><strong>질문형:</strong> ${analysis.hasQuestionMark ? '✅ 예' : '❌ 아니오'}</p>
            </div>
            
            <div class="keywords-section">
                <h5>🔑 추출된 키워드</h5>
                <div class="keywords-list">
                    ${analysis.keywords.map(k => `<span class="keyword-tag">${k}</span>`).join('')}
                </div>
            </div>
            
            <div class="suggestions-section">
                <h5>💡 개선 제안</h5>
                ${analysis.suggestions.map(s => `<p class="suggestion-item">• ${s}</p>`).join('')}
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

/**
 * SEO 분석 표시
 */
function displaySeoAnalysis(analysis) {
    const container = document.getElementById('seoAnalysisContent');
    
    let html = `
        <div class="analysis-card">
            <div class="score-header">
                <h4>전체 SEO 점수</h4>
                <div class="score-badge-large">${analysis.overallSeoScore}/100</div>
            </div>
            
            <div class="seo-section">
                <h5>🏷️ 태그 분석 (점수: ${analysis.tagDiversityScore}/100)</h5>
                <p><strong>현재 태그 수:</strong> ${analysis.tagCount}개</p>
                ${analysis.currentTags.length > 0 ? `
                    <div class="tags-list">
                        ${analysis.currentTags.map(t => `<span class="tag-item">${t}</span>`).join('')}
                    </div>
                ` : '<p class="no-data">태그가 없습니다.</p>'}
                
                ${analysis.recommendedTags.length > 0 ? `
                    <p><strong>추천 태그:</strong></p>
                    <div class="tags-list">
                        ${analysis.recommendedTags.slice(0, 10).map(t => `<span class="tag-item recommended">${t}</span>`).join('')}
                    </div>
                ` : ''}
                
                ${analysis.missingTags.length > 0 ? `
                    <p class="warning-text">⚠️ 인기 태그 중 누락: ${analysis.missingTags.join(', ')}</p>
                ` : ''}
            </div>
            
            <div class="seo-section">
                <h5>📝 설명문 분석 (점수: ${analysis.descriptionScore}/100)</h5>
                <p><strong>길이:</strong> ${analysis.descriptionLength}자 
                    ${analysis.descriptionLength >= 250 ? '✅' : '⚠️ 최소 250자 권장'}
                </p>
                <p><strong>링크 포함:</strong> ${analysis.hasLinks ? '✅ 예' : '❌ 아니오'}</p>
                <p><strong>타임스탬프:</strong> ${analysis.hasTimestamps ? '✅ 예' : '❌ 아니오'}</p>
                <p><strong>해시태그:</strong> ${analysis.hasHashtags ? '✅ ' + analysis.hashtagCount + '개' : '❌ 없음'}</p>
                
                ${analysis.hashtags.length > 0 ? `
                    <div class="hashtags-list">
                        ${analysis.hashtags.map(h => `<span class="hashtag-item">${h}</span>`).join('')}
                    </div>
                ` : ''}
            </div>
            
            <div class="seo-section">
                <h5>✅ 최적화 체크리스트</h5>
                ${analysis.descriptionChecklist.map(item => `
                    <div class="checklist-item ${item.checked ? 'checked' : ''}">
                        <span class="check-icon">${item.checked ? '✅' : '❌'}</span>
                        <div>
                            <p class="check-title">${item.item}</p>
                            <p class="check-desc">${item.recommendation}</p>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

/**
 * CTR 추정 표시
 */
function displayCtrEstimate(estimate) {
    const container = document.getElementById('ctrEstimateContent');
    
    let html = `
        <div class="analysis-card">
            <div class="ctr-header">
                <h4>예상 클릭률 (CTR)</h4>
                <div class="ctr-value-large">${estimate.estimatedCtr.toFixed(2)}%</div>
                <div class="ctr-level">${estimate.ctrLevel}</div>
            </div>
            
            <div class="ctr-factors">
                <h5>📊 CTR 영향 요소</h5>
                ${estimate.factors.map(factor => `
                    <div class="factor-item">
                        <div class="factor-header">
                            <span class="factor-name">${factor.factor}</span>
                            <span class="factor-score ${factor.impact === '긍정적' ? 'positive' : factor.impact === '부정적' ? 'negative' : 'neutral'}">
                                ${factor.score}점
                            </span>
                        </div>
                        <p class="factor-desc">${factor.description}</p>
                        <div class="factor-bar">
                            <div class="factor-fill ${factor.impact === '긍정적' ? 'positive' : factor.impact === '부정적' ? 'negative' : 'neutral'}" 
                                 style="width: ${(factor.score / 20) * 100}%"></div>
                        </div>
                    </div>
                `).join('')}
            </div>
            
            ${estimate.improvements.length > 0 ? `
                <div class="improvements-section">
                    <h5>💡 개선 방안</h5>
                    ${estimate.improvements.map(imp => `<p class="improvement-item">• ${imp}</p>`).join('')}
                </div>
            ` : ''}
        </div>
    `;
    
    container.innerHTML = html;
}

/**
 * 품질 점수 표시
 */
function displayQualityScore(score) {
    const container = document.getElementById('qualityScoreContent');
    
    let html = `
        <div class="analysis-card">
            <div class="quality-header">
                <h4>종합 품질 점수</h4>
                <div class="quality-score-large">
                    <div class="score-circle-large">
                        <div class="score-value-large">${score.overallScore}</div>
                        <div class="score-label-large">/ 100</div>
                    </div>
                    <div class="grade-badge-large">${score.grade}</div>
                </div>
            </div>
            
            <div class="quality-breakdown">
                <h5>📊 세부 점수</h5>
                
                <div class="quality-item">
                    <div class="quality-item-header">
                        <span class="quality-name">제목 최적화</span>
                        <span class="quality-score">${score.titleOptimizationScore}/100</span>
                    </div>
                    <div class="quality-bar">
                        <div class="quality-fill" style="width: ${score.titleOptimizationScore}%"></div>
                    </div>
                    <div class="quality-detail">
                        <span class="status-badge ${score.titleDetail.status}">${score.titleDetail.status}</span>
                        <p>${score.titleDetail.feedback}</p>
                    </div>
                </div>
                
                <div class="quality-item">
                    <div class="quality-item-header">
                        <span class="quality-name">태그 다양성</span>
                        <span class="quality-score">${score.tagDiversityScore}/100</span>
                    </div>
                    <div class="quality-bar">
                        <div class="quality-fill" style="width: ${score.tagDiversityScore}%"></div>
                    </div>
                    <div class="quality-detail">
                        <span class="status-badge ${score.tagDetail.status}">${score.tagDetail.status}</span>
                        <p>${score.tagDetail.feedback}</p>
                    </div>
                </div>
                
                <div class="quality-item">
                    <div class="quality-item-header">
                        <span class="quality-name">설명문 품질</span>
                        <span class="quality-score">${score.descriptionLengthScore}/100</span>
                    </div>
                    <div class="quality-bar">
                        <div class="quality-fill" style="width: ${score.descriptionLengthScore}%"></div>
                    </div>
                    <div class="quality-detail">
                        <span class="status-badge ${score.descriptionDetail.status}">${score.descriptionDetail.status}</span>
                        <p>${score.descriptionDetail.feedback}</p>
                    </div>
                </div>
                
                <div class="quality-item">
                    <div class="quality-item-header">
                        <span class="quality-name">참여도</span>
                        <span class="quality-score">${score.engagementScore}/100</span>
                    </div>
                    <div class="quality-bar">
                        <div class="quality-fill" style="width: ${score.engagementScore}%"></div>
                    </div>
                    <div class="quality-detail">
                        <span class="status-badge ${score.engagementDetail.status}">${score.engagementDetail.status}</span>
                        <p>${score.engagementDetail.feedback}</p>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

/**
 * UI 헬퍼 함수들
 */
function showLoading() {
    document.getElementById('loading').style.display = 'block';
}

function hideLoading() {
    document.getElementById('loading').style.display = 'none';
}

function showError(message) {
    document.getElementById('errorText').textContent = message;
    document.getElementById('error').style.display = 'flex';
}

function hideError() {
    document.getElementById('error').style.display = 'none';
}

function showReport() {
    document.getElementById('reportSection').style.display = 'block';
}

function hideReport() {
    document.getElementById('reportSection').style.display = 'none';
}

// Enter 키로 분석 시작
document.addEventListener('DOMContentLoaded', () => {
    const urlInput = document.getElementById('videoUrl');
    if (urlInput) {
        urlInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                analyzeVideo();
            }
        });
    }
    
    // URL 파라미터에서 videoId 확인하고 자동 분석
    const urlParams = new URLSearchParams(window.location.search);
    const videoId = urlParams.get('videoId');
    if (videoId) {
        // 입력 필드에 videoId 설정
        urlInput.value = videoId;
        // 자동으로 분석 시작
        analyzeVideo();
    }
});
