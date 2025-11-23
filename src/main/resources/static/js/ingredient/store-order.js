// 매장 발주 관리 JavaScript

const STORE_ID = 'STORE_001';
let allOrders = [];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
    updateStats();
});

// 발주 목록 로드
async function loadOrders() {
    try {
        const response = await fetch(`/api/ingredient-orders/store/${STORE_ID}`);
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
                <td colspan="6" class="empty-state">
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
            <td>${order.distributorId}</td>
            <td>${formatDateTime(order.orderedAt)}</td>
            <td><strong>${formatCurrency(order.totalAmount)}</strong></td>
            <td>${getStatusBadge(order.status)}</td>
            <td>
                <button class="btn btn-outline" onclick="viewOrderDetail('${order.orderId}')">
                    상세보기
                </button>
            </td>
        </tr>
    `).join('');
}

// 통계 업데이트
function updateStats() {
    const now = new Date();
    const thisMonth = allOrders.filter(order => {
        const orderDate = new Date(order.orderedAt);
        return orderDate.getMonth() === now.getMonth() && 
               orderDate.getFullYear() === now.getFullYear();
    });
    
    const pendingOrders = allOrders.filter(order => order.status === 'PENDING');
    const monthlyAmount = thisMonth.reduce((sum, order) => sum + order.totalAmount, 0);
    
    document.getElementById('monthlyOrderCount').textContent = thisMonth.length;
    document.getElementById('pendingOrderCount').textContent = pendingOrders.length;
    document.getElementById('monthlyOrderAmount').textContent = formatCurrency(monthlyAmount);
}

// 필터링
function filterOrders() {
    const status = document.getElementById('statusFilter').value;
    const filtered = status ? allOrders.filter(order => order.status === status) : allOrders;
    renderOrders(filtered);
}

// 새 발주 모달 열기
function openNewOrderModal() {
    document.getElementById('newOrderModal').classList.add('active');
}

// 새 발주 모달 닫기
function closeNewOrderModal() {
    document.getElementById('newOrderModal').classList.remove('active');
    document.getElementById('newOrderForm').reset();
    
    // 품목을 1개만 남기고 제거
    const itemsContainer = document.getElementById('orderItems');
    const items = itemsContainer.querySelectorAll('.order-item');
    for (let i = 1; i < items.length; i++) {
        items[i].remove();
    }
}

// 품목 추가
function addOrderItem() {
    const itemsContainer = document.getElementById('orderItems');
    const newItem = document.createElement('div');
    newItem.className = 'order-item';
    newItem.style.cssText = 'display: grid; grid-template-columns: 2fr 1fr 1fr 1fr 40px; gap: 0.5rem; margin-bottom: 0.5rem;';
    newItem.innerHTML = `
        <input type="text" class="form-input" placeholder="품목명" name="itemName[]" required>
        <input type="number" class="form-input" placeholder="수량" name="quantity[]" min="1" required>
        <input type="number" class="form-input" placeholder="단가" name="unitPrice[]" min="0" required>
        <input type="text" class="form-input" placeholder="단위" name="unit[]" value="개">
        <button type="button" class="btn btn-danger" onclick="removeOrderItem(this)" style="padding: 0.625rem;">✕</button>
    `;
    itemsContainer.appendChild(newItem);
}

// 품목 제거
function removeOrderItem(button) {
    const itemsContainer = document.getElementById('orderItems');
    if (itemsContainer.querySelectorAll('.order-item').length > 1) {
        button.closest('.order-item').remove();
    } else {
        showAlert('최소 1개의 품목이 필요합니다.', 'error');
    }
}

// 새 발주 제출
async function submitNewOrder(event) {
    event.preventDefault();
    
    const form = event.target;
    const distributorId = form.querySelector('#distributorId').value;
    
    const itemNames = form.querySelectorAll('input[name="itemName[]"]');
    const quantities = form.querySelectorAll('input[name="quantity[]"]');
    const unitPrices = form.querySelectorAll('input[name="unitPrice[]"]');
    const units = form.querySelectorAll('input[name="unit[]"]');
    
    const items = [];
    for (let i = 0; i < itemNames.length; i++) {
        items.push({
            itemName: itemNames[i].value,
            quantity: parseInt(quantities[i].value),
            unitPrice: parseInt(unitPrices[i].value),
            unit: units[i].value
        });
    }
    
    const orderData = {
        storeId: STORE_ID,
        distributorId: distributorId,
        items: items
    };
    
    try {
        const response = await fetch('/api/ingredient-orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });
        
        if (!response.ok) throw new Error('발주 생성 실패');
        
        const result = await response.json();
        showAlert('발주가 성공적으로 생성되었습니다!', 'success');
        closeNewOrderModal();
        loadOrders();
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 생성에 실패했습니다.', 'error');
    }
}

// 발주 상세 보기
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
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">유통사</div>
                        <div style="font-weight: 600;">${order.distributorId}</div>
                    </div>
                    <div>
                        <div style="color: var(--gray-600); font-size: 0.875rem; margin-bottom: 0.25rem;">발주일시</div>
                        <div>${formatDateTime(order.orderedAt)}</div>
                    </div>
                </div>
                
                ${order.rejectionReason ? `
                    <div class="alert alert-error">
                        <strong>거절 사유:</strong> ${order.rejectionReason}
                    </div>
                ` : ''}
                
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
        
        document.getElementById('orderDetailContent').innerHTML = content;
        document.getElementById('orderDetailModal').classList.add('active');
    } catch (error) {
        console.error('Error:', error);
        showAlert('발주 상세 정보를 불러오는데 실패했습니다.', 'error');
    }
}

// 발주 상세 모달 닫기
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('active');
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
