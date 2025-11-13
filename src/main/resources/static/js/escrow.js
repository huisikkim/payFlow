// 에스크로 거래 생성 폼 제출
document.getElementById('createEscrowForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    
    const request = {
        buyer: {
            userId: formData.get('buyerUserId'),
            name: formData.get('buyerName'),
            email: formData.get('buyerEmail'),
            phone: formData.get('buyerPhone')
        },
        seller: {
            userId: formData.get('sellerUserId'),
            name: formData.get('sellerName'),
            email: formData.get('sellerEmail'),
            phone: formData.get('sellerPhone')
        },
        vehicle: {
            vin: formData.get('vehicleVin'),
            manufacturer: formData.get('vehicleManufacturer'),
            model: formData.get('vehicleModel'),
            year: parseInt(formData.get('vehicleYear')),
            registrationNumber: formData.get('vehicleRegistrationNumber')
        },
        amount: parseFloat(formData.get('amount')),
        feeRate: parseFloat(formData.get('feeRate')) / 100
    };
    
    try {
        const response = await fetch('/api/escrow', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        });
        
        if (response.ok) {
            const result = await response.json();
            alert('에스크로 거래가 생성되었습니다.');
            window.location.href = `/escrow/${result.transactionId}`;
        } else {
            const error = await response.json();
            alert(`오류: ${error.message}`);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('거래 생성 중 오류가 발생했습니다.');
    }
});

// 이벤트 히스토리 보기
function viewEvents() {
    const transactionId = getTransactionIdFromUrl();
    window.location.href = `/escrow/${transactionId}/events`;
}

// 입금 내역 보기
function viewDeposits() {
    const transactionId = getTransactionIdFromUrl();
    window.location.href = `/escrow/${transactionId}/deposits`;
}

// 검증 내역 보기
function viewVerifications() {
    const transactionId = getTransactionIdFromUrl();
    window.location.href = `/escrow/${transactionId}/verifications`;
}

// 정산 내역 보기
function viewSettlement() {
    const transactionId = getTransactionIdFromUrl();
    window.location.href = `/escrow/${transactionId}/settlement`;
}

// URL에서 거래 ID 추출
function getTransactionIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    return pathParts[pathParts.length - 1];
}

// 금액 포맷팅
function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    }).format(amount);
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}
