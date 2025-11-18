const applicantId = window.location.pathname.split('/').pop();

async function loadApplicantDetail() {
    try {
        const response = await fetch(`/api/ainjob/applicants/${applicantId}`);
        
        if (!response.ok) {
            throw new Error('지원자 정보 로드 실패');
        }
        
        const applicant = await response.json();
        
        let html = `
            <div class="detail-header">
                <div>
                    <h1 class="detail-title">${applicant.name}</h1>
                    <p style="color: #6c757d; margin-top: 8px;">${applicant.email} · ${applicant.phone || '-'}</p>
                </div>
            </div>
            
            <div class="detail-section">
                <h3 class="detail-section-title">기본 정보</h3>
                <div class="detail-grid">
                    <div class="detail-item">
                        <div class="detail-item-label">이름</div>
                        <div class="detail-item-value">${applicant.name}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-item-label">이메일</div>
                        <div class="detail-item-value">${applicant.email}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-item-label">전화번호</div>
                        <div class="detail-item-value">${applicant.phone || '-'}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-item-label">총 경력</div>
                        <div class="detail-item-value">${applicant.totalYearsOfExperience}년</div>
                    </div>
                </div>
            </div>
        `;
        
        if (applicant.educations && applicant.educations.length > 0) {
            html += `
                <div class="detail-section">
                    <h3 class="detail-section-title">학력</h3>
                    ${applicant.educations.map(edu => `
                        <div class="detail-item" style="margin-bottom: 16px;">
                            <div style="font-weight: 600; margin-bottom: 8px;">${edu.schoolName}</div>
                            <div style="color: #6c757d; font-size: 14px;">
                                ${edu.level} · ${edu.majorName || '-'} · ${edu.status}
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        }
        
        if (applicant.careers && applicant.careers.length > 0) {
            html += `
                <div class="detail-section">
                    <h3 class="detail-section-title">경력</h3>
                    ${applicant.careers.map(career => `
                        <div style="margin-bottom: 24px; padding: 16px; background: #f8f9fa; border-radius: 8px;">
                            <div style="font-weight: 600; font-size: 16px; margin-bottom: 8px;">${career.companyName}</div>
                            <div style="color: #6c757d; margin-bottom: 8px;">${career.position} · ${career.yearsOfExperience}년</div>
                            ${career.description ? `<p style="margin-bottom: 12px;">${career.description}</p>` : ''}
                            ${career.skills && career.skills.length > 0 ? `
                                <div class="skill-tags">
                                    ${career.skills.map(skill => 
                                        `<span class="skill-tag">${skill.skillName} (${skill.proficiencyLevel})</span>`
                                    ).join('')}
                                </div>
                            ` : ''}
                        </div>
                    `).join('')}
                </div>
            `;
        }
        
        html += `
            <div class="btn-group">
                <button class="btn-secondary" onclick="location.href='/ainjob/applicants'">목록으로</button>
            </div>
        `;
        
        document.getElementById('applicant-detail').innerHTML = html;
        
    } catch (error) {
        console.error('지원자 정보 로드 실패:', error);
        document.getElementById('applicant-detail').innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <div class="empty-state-title">데이터를 불러올 수 없습니다</div>
                <div class="empty-state-desc">${error.message}</div>
            </div>
        `;
    }
}

window.addEventListener('load', loadApplicantDetail);
