// 정산 대시보드 JavaScript

let allSettlements = [];
let currentViewType = 'store';
let currentSettlementId = null;
let currentSettlement = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 기본적으로 STORE_001 선택
    document.getElementById('entitySelect').value = 'STORE_001';
    loadSettlements();
    
    // 지불 금액 입력 시 남은 미수금 계산
    document.getElementById('paidAmount').addEventListener('input', function() {
        if (currentSettlement) {
            const paidAmount = parseInt(this.value) || 0;
            const remaining = currentSettlement.settlementAmount - paidAmount;
            document.getElementById('remainingOutstanding').textContent = formatCurrency(remaining);
        }
    });
});

// 보기 방식 전환
function switchView() {
    currentViewType = document.getElementById('viewType').value;
    
    // 라벨 및 헤더 변경
    if (currentViewType === 'store') {
        document.getElementById('selectLabel').textContent = '매장 선택';
        document.getElementById('entityHeader').textContent = '매장';
        document.getElementById('entitySelect').innerHTML = `
            <option value="">전체</option>
            <option value="STORE_001">맛있는 식당 (STORE_001)</option>
            <option value="STORE_002">행복한 카페 (STORE_002)</option>
        `;
    } else {
        document.getElementById('selectLabel').textContent = '유통사 선택';
        document.getElementById('entityHeader').textContent = '유통사';
        document.getElementById('entitySelect').innerHTML = `
            <option value="">전체</option>
            <option value="DIST_001">신선식자재 (DIST_001)</option>
            <option value="DIST_002">프리미엄푸드 (DIST_002)</option>
        `;
    }
    
    loadSettlements();
}

// 정산 목록 로드
async function loadSettlements() {
    const entityId = document.getElementById('entitySelect').value;
    
    try {
        let settlements = [];
        
        if (entityId) {
            const endpoint = currentViewType === 'store' 
                ? `/api/settlements/store/${entityId}`
                : `/api/settlements/distributor/${entityId}`;
            
            const response = await fetch(endpoint);
            if (!response.ok) throw new Error('정산 목록 로드 실패');
            settlements = await response.json();
        } else {
            // 전체 조회 (실제로는 API가 필요하지만, 여기서는 샘플 데이터)
            settlements = [];
        }
        
        allSettlements = settlements;
        renderSettlements(settlements);
        updateStats();
    } catch (error) {
        console.error('Error:', error);
        showAlert('정산 목록을 불러오는데 실패했습니다.', 'error');
        
        // 빈 테이블 표시
        document.getElementById('settlementTableBody').innerHTML = `
            <tr>
                <td colspan="9" class="empty-state">
                    <div class="empty-state-icon">💰</div>
                    <div class="empty-state-text">정산 내역이 없습니다</div>
                </td>
            </tr>
        `;
    }
}

// 정산 목록 렌더링
function renderSettlements(settlements) {
    const tbody = document.getElementById('settlementTableBody');
    
    if (settlements.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="empty-state">
                    <div class="empty-state-icon">💰</div>
                    <div class="empty-state-text">정산 내역이 없습니다</div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = settlements.map(settlement => `
        <tr>
            <td><strong>${settlement.settlementId}</strong></td>
            <td>${settlement.orderId}</td>
            <td>${currentViewType === 'store' ? settlement.storeId : settlement.distributorId}</td>
            <td><strong>${formatCurrency(settlement.settlementAmount)}</strong></td>
            <td>${formatCurrency(settlement.paidAmount)}</td>
            <td style="color: ${settlement.outstandingAmount > 0 ? 'var(--danger-color)' : 'var(--success-color)'};">
                <strong>${formatCurrency(settlement.outstandingAmount)}</strong>
            </td>
            <td>${getStatusBadge(settlement.status)}</td>
            <td>${formatDate(settlement.settlementDate)}</td>
            <td>
                <button class="btn btn-outline" onclick="viewSettlementDetail('${settlement.settlementId}')">
                    상세
                </button>
                ${settlement.status === 'PENDING' || settlement.status === 'PROCESSING' ? `
                    <button class="btn btn-success" onclick="openCompleteModal('${settlement.settlementId}')" style="margin-left: 0.25rem;">
                        완료
                    </button>
                ` : ''}
            </td>
        </tr>
    `).join('');
}

// 통계 업데이트
function updateStats() {
    const totalSettlement = allSettlements.reduce((sum, s) => sum + s.settlementAmount, 0);
    const totalOutstanding = allSettlements.reduce((sum, s) => sum + s.outstandingAmount, 0);
    const completed = allSettlements.filter(s => s.status === 'COMPLETED').length;
    const pending = allSettlements.filter(s => s.status === 'PENDING' || s.status === 'PROCESSING').length;
    
    document.getElementById('totalSettlement').textContent = formatCurrency(totalSettlement);
    document.getElementById('totalOutstanding').textContent = formatCurrency(totalOutstanding);
    document.getElementById('completedCount').textContent = completed;
    document.getElementById('pendingCount').textContent = pending;
}

// 필터링
function filterSettlements() {
    const status = document.getElementById('statusFilter').value;
    const filtered = status ? allSettlements.filter(s => s.status === status) : allSettlements;
    renderSettlements(filtered);
}

// 정산 상세 보기
async function viewSettlementDetail(settlementId) {
    try {
        const response = await fetch(`/api/settlements/${settlementId}`);
        if (!response.ok) throw new Error('정산 상세 로드 실패');
        
        const settlement = await response.json();
        
        // 발주 정보도 가져오기
        const orderResponse = await fetch(`/api/ingredient-orders/${settlement.orderId}`);
        const order = orderResponse.ok ? await orderResponse.json() : null;
        
        const content = `
            <div style="margin-bottom: 1.5rem;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">정산번호</div>
                        <div style="font-weight: 600;">${settlement.settlementId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">상태</div>
                        <div>${getStatusBadge(settlement.status)}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">매장</div>
                        <div style="font-weight: 600;">${settlement.storeId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">유통사</div>
                        <div style="font-weight: 600;">${settlement.distributorId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주번호</div>
                        <div>${settlement.orderId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">정산일</div>
                        <div>${formatDateTime(settlement.settlementDate)}</div>
                    </div>
                </div>
                
                <div style="background: var(--gray-50); padding: 1.5rem; border-radius: var(--border-radius); margin-bottom: 1.5rem;">
                    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 1rem;">
                        <div>
                            <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.5rem;">정산 금액</div>
                            <div style="font-size: 1.5rem; font-weight: 700;">${formatCurrency(settlement.settlementAmount)}</div>
                        </div>
                        <div>
                            <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.5rem;">지불 금액</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--success-color);">${formatCurrency(settlement.paidAmount)}</div>
                        </div>
                        <div>
                            <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.5rem;">미수금</div>
                            <div style="font-size: 1.5rem; font-weight: 700; color: var(--danger-color);">${formatCurrency(settlement.outstandingAmount)}</div>
                        </div>
                    </div>
                </div>
                
                ${order ? `
                    <h4 style="margin: 1.5rem 0 1rem; font-size: 1rem; font-weight: 600;">발주 품목</h4>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>품목명</th>
                                <th>수량</th>
                                <th>단가</th>
                                <th>소계</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${order.items.map(item => `
                                <tr>
                                    <td>${item.itemName}</td>
                                    <td>${item.quantity} ${item.unit || '개'}</td>
                                    <td>${formatCurrency(item.unitPrice)}</td>
                                    <td>${formatCurrency(item.subtotal)}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                ` : ''}
                
                ${settlement.completedAt ? `
                    <div class="alert alert-success" style="margin-top: 1rem;">
                        정산 완료: ${formatDateTime(settlement.completedAt)}
                    </div>
                ` : ''}
            </div>
        `;
        
        document.getElementById('settlementContent').innerHTML = content;
        document.getElementById('settlementModal').classList.add('active');
    } catch (error) {
        console.error('Error:', error);
        showAlert('정산 상세 정보를 불러오는데 실패했습니다.', 'error');
    }
}

// 정산 완료 모달 열기
async function openCompleteModal(settlementId) {
    try {
        const response = await fetch(`/api/settlements/${settlementId}`);
        if (!response.ok) throw new Error('정산 정보 로드 실패');
        
        currentSettlement = await response.json();
        currentSettlementId = settlementId;
        
        document.getElementById('settlementAmountDisplay').textContent = 
            formatCurrency(currentSettlement.settlementAmount);
        document.getElementById('currentOutstandingDisplay').textContent = 
            formatCurrency(currentSettlement.outstandingAmount);
        document.getElementById('paidAmount').value = currentSettlement.outstandingAmount;
        document.getElementById('remainingOutstanding').textContent = '0원';
        
        document.getElementById('completeModal').classList.add('active');
    } catch (error) {
        console.error('Error:', error);
        showAlert('정산 정보를 불러오는데 실패했습니다.', 'error');
    }
}

// 정산 완료 제출
async function submitComplete(event) {
    event.preventDefault();
    
    const paidAmount = parseInt(document.getElementById('paidAmount').value);
    
    if (!confirm(`${formatCurrency(paidAmount)}을 지불 처리하시겠습니까?`)) return;
    
    try {
        const response = await fetch(`/api/settlements/${currentSettlementId}/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ paidAmount })
        });
        
        if (!response.ok) throw new Error('정산 완료 실패');
        
        showAlert('정산이 완료되었습니다!', 'success');
        closeCompleteModal();
        loadSettlements();
    } catch (error) {
        console.error('Error:', error);
        showAlert('정산 완료에 실패했습니다.', 'error');
    }
}

// 모달 닫기
function closeSettlementModal() {
    document.getElementById('settlementModal').classList.remove('active');
}

function closeCompleteModal() {
    document.getElementById('completeModal').classList.remove('active');
    document.getElementById('completeForm').reset();
    currentSettlementId = null;
    currentSettlement = null;
}

// 상태 배지
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge badge-pending">대기중</span>',
        'PROCESSING': '<span class="badge" style="background: #dbeafe; color: #1e40af;">처리중</span>',
        'COMPLETED': '<span class="badge badge-completed">완료</span>',
        'FAILED': '<span class="badge badge-rejected">실패</span>'
    };
    return badges[status] || status;
}

// 날짜 포맷
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 금액 포맷
function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    }).format(amount);
}

// 알림 표시
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.position = 'fixed';
    alertDiv.style.top = '2rem';
    alertDiv.style.right = '2rem';
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}
