// 유통사 발주 확인 JavaScript

const DISTRIBUTOR_ID = 'DIST_001';
let allOrders = [];
let currentTab = 'pending';
let currentOrderId = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
});

// 발주 목록 로드
async function loadOrders() {
    try {
        const url = currentTab === 'pending' 
            ? `/api/distributor/orders/pending?distributorId=${DISTRIBUTOR_ID}`
            : `/api/distributor/orders?distributorId=${DISTRIBUTOR_ID}`;
            
        const response = await fetch(url);
        if (!response.ok) throw new Error('발주 목록 로드 실패');
        
        allOrders = await response.json();
        renderOrders(allOrders);
        updateStats();
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 목록을 불러오는데 실패했습니다.', 'error');
    }
}

// 발주 목록 렌더링
function renderOrders(orders) {
    const tbody = document.getElementById('orderTableBody');
    
    if (orders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-state">
                    <div class="empty-state-icon">📦</div>
                    <div class="empty-state-text">발주 내역이 없습니다</div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = orders.map(order => `
        <tr>
            <td><strong>${order.orderId}</strong></td>
            <td>${order.storeId}</td>
            <td>${formatDateTime(order.orderedAt)}</td>
            <td>${order.items.length}개</td>
            <td><strong>${formatCurrency(order.totalAmount)}</strong></td>
            <td>${getStatusBadge(order.status)}</td>
            <td>
                ${order.status === 'PENDING' ? `
                    <button class="btn btn-primary" onclick="viewOrderForConfirm('${order.orderId}')">
                        확인하기
                    </button>
                ` : `
                    <button class="btn btn-outline" onclick="viewOrderDetail('${order.orderId}')">
                        상세보기
                    </button>
                `}
            </td>
        </tr>
    `).join('');
}

// 통계 업데이트
function updateStats() {
    const pending = allOrders.filter(o => o.status === 'PENDING');
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayConfirmed = allOrders.filter(o => {
        if (o.status !== 'CONFIRMED' || !o.confirmedAt) return false;
        const confirmedDate = new Date(o.confirmedAt);
        confirmedDate.setHours(0, 0, 0, 0);
        return confirmedDate.getTime() === today.getTime();
    });
    
    const thisMonth = allOrders.filter(order => {
        const orderDate = new Date(order.orderedAt);
        return orderDate.getMonth() === today.getMonth() && 
               orderDate.getFullYear() === today.getFullYear();
    });
    const monthlyAmount = thisMonth.reduce((sum, order) => sum + order.totalAmount, 0);
    
    document.getElementById('pendingCount').textContent = pending.length;
    document.getElementById('pendingTabCount').textContent = pending.length;
    document.getElementById('todayConfirmedCount').textContent = todayConfirmed.length;
    document.getElementById('monthlyAmount').textContent = formatCurrency(monthlyAmount);
}

// 탭 전환
function switchTab(tab) {
    currentTab = tab;
    
    document.getElementById('pendingTab').classList.toggle('active', tab === 'pending');
    document.getElementById('allTab').classList.toggle('active', tab === 'all');
    
    document.getElementById('listTitle').textContent = 
        tab === 'pending' ? '대기 중인 발주' : '전체 발주';
    
    loadOrders();
}

// 발주 확인 화면 열기
async function viewOrderForConfirm(orderId) {
    try {
        const response = await fetch(`/api/ingredient-orders/${orderId}`);
        if (!response.ok) throw new Error('발주 상세 로드 실패');
        
        const order = await response.json();
        currentOrderId = orderId;
        
        const content = `
            <div style="margin-bottom: 1.5rem;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주번호</div>
                        <div style="font-weight: 600;">${order.orderId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">매장</div>
                        <div style="font-weight: 600;">${order.storeId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주일시</div>
                        <div>${formatDateTime(order.orderedAt)}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">상태</div>
                        <div>${getStatusBadge(order.status)}</div>
                    </div>
                </div>
                
                <h4 style="margin: 1.5rem 0 1rem; font-size: 1rem; font-weight: 600;">발주 품목 및 단가 조정</h4>
                <div id="itemsContainer">
                    ${order.items.map((item, index) => `
                        <div class="price-edit-row">
                            <div>
                                <strong>${item.itemName}</strong>
                            </div>
                            <div>${item.quantity} ${item.unit || '개'}</div>
                            <div>
                                <input type="number" 
                                       class="form-input" 
                                       id="price_${index}" 
                                       value="${item.unitPrice}" 
                                       min="0"
                                       onchange="updateItemPrice('${order.orderId}', ${index}, this.value)"
                                       style="width: 100%;">
                            </div>
                            <div id="subtotal_${index}">
                                <strong>${formatCurrency(item.subtotal)}</strong>
                            </div>
                            <div>
                                <button class="btn btn-secondary" onclick="resetPrice(${index}, ${item.unitPrice})" style="padding: 0.5rem;">
                                    초기화
                                </button>
                            </div>
                        </div>
                    `).join('')}
                </div>
                
                <div style="margin-top: 1.5rem; padding: 1rem; background: var(--gray-50); border-radius: var(--border-radius); display: flex; justify-content: space-between; align-items: center;">
                    <div style="font-size: 1.125rem; font-weight: 600;">총액</div>
                    <div id="totalAmount" style="font-size: 1.5rem; font-weight: 700; color: var(--primary-color);">
                        ${formatCurrency(order.totalAmount)}
                    </div>
                </div>
                
                <div style="display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 2rem;">
                    <button class="btn btn-danger" onclick="openRejectModal()">
                        거절하기
                    </button>
                    <button class="btn btn-success" onclick="confirmOrder()">
                        ✅ 발주 확인
                    </button>
                </div>
            </div>
        `;
        
        document.getElementById('orderContent').innerHTML = content;
        document.getElementById('orderModal').classList.add('active');
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 상세 정보를 불러오는데 실패했습니다.', 'error');
    }
}

// 발주 상세 보기 (읽기 전용)
async function viewOrderDetail(orderId) {
    try {
        const response = await fetch(`/api/ingredient-orders/${orderId}`);
        if (!response.ok) throw new Error('발주 상세 로드 실패');
        
        const order = await response.json();
        
        const content = `
            <div style="margin-bottom: 1.5rem;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주번호</div>
                        <div style="font-weight: 600;">${order.orderId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">상태</div>
                        <div>${getStatusBadge(order.status)}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">매장</div>
                        <div style="font-weight: 600;">${order.storeId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주일시</div>
                        <div>${formatDateTime(order.orderedAt)}</div>
                    </div>
                </div>
                
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
                                <td><strong>${formatCurrency(item.subtotal)}</strong></td>
                            </tr>
                        `).join('')}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" style="text-align: right; font-weight: 600;">총액</td>
                            <td><strong style="font-size: 1.125rem; color: var(--primary-color);">${formatCurrency(order.totalAmount)}</strong></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        `;
        
        document.getElementById('orderContent').innerHTML = content;
        document.getElementById('orderModal').classList.add('active');
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 상세 정보를 불러오는데 실패했습니다.', 'error');
    }
}

// 품목 단가 업데이트 (UI만)
function updateItemPrice(orderId, itemIndex, newPrice) {
    const priceInput = document.getElementById(`price_${itemIndex}`);
    const quantity = parseInt(priceInput.closest('.price-edit-row').children[1].textContent);
    const newSubtotal = quantity * parseInt(newPrice);
    
    document.getElementById(`subtotal_${itemIndex}`).innerHTML = 
        `<strong>${formatCurrency(newSubtotal)}</strong>`;
    
    // 총액 재계산
    let total = 0;
    document.querySelectorAll('.price-edit-row').forEach((row, idx) => {
        const price = parseInt(document.getElementById(`price_${idx}`).value);
        const qty = parseInt(row.children[1].textContent);
        total += price * qty;
    });
    
    document.getElementById('totalAmount').textContent = formatCurrency(total);
}

// 단가 초기화
function resetPrice(itemIndex, originalPrice) {
    document.getElementById(`price_${itemIndex}`).value = originalPrice;
    updateItemPrice(currentOrderId, itemIndex, originalPrice);
}

// 발주 확인
async function confirmOrder() {
    if (!confirm('이 발주를 확인하시겠습니까?')) return;
    
    try {
        // 단가 변경사항 먼저 저장
        const priceInputs = document.querySelectorAll('[id^="price_"]');
        for (let i = 0; i < priceInputs.length; i++) {
            const newPrice = parseInt(priceInputs[i].value);
            // 실제로는 itemId가 필요하지만, 여기서는 인덱스 사용
            // 실제 구현시에는 item.id를 사용해야 함
        }
        
        const response = await fetch(`/api/distributor/orders/${currentOrderId}/confirm`, {
            method: 'POST'
        });
        
        if (!response.ok) throw new Error('발주 확인 실패');
        
        showAlert('발주가 확인되었습니다!', 'success');
        closeOrderModal();
        loadOrders();
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 확인에 실패했습니다.', 'error');
    }
}

// 거절 모달 열기
function openRejectModal() {
    document.getElementById('rejectModal').classList.add('active');
}

// 거절 모달 닫기
function closeRejectModal() {
    document.getElementById('rejectModal').classList.remove('active');
    document.getElementById('rejectForm').reset();
}

// 발주 거절
async function submitReject(event) {
    event.preventDefault();
    
    const reason = document.getElementById('rejectReason').value;
    
    try {
        const response = await fetch(`/api/distributor/orders/${currentOrderId}/reject`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reason })
        });
        
        if (!response.ok) throw new Error('발주 거절 실패');
        
        showAlert('발주가 거절되었습니다.', 'success');
        closeRejectModal();
        closeOrderModal();
        loadOrders();
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 거절에 실패했습니다.', 'error');
    }
}

// 모달 닫기
function closeOrderModal() {
    document.getElementById('orderModal').classList.remove('active');
    currentOrderId = null;
}

// 상태 배지
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge badge-pending">대기중</span>',
        'CONFIRMED': '<span class="badge badge-confirmed">확인됨</span>',
        'COMPLETED': '<span class="badge badge-completed">완료</span>',
        'REJECTED': '<span class="badge badge-rejected">거절됨</span>',
        'CANCELLED': '<span class="badge badge-cancelled">취소됨</span>'
    };
    return badges[status] || status;
}

// 날짜 포맷
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
