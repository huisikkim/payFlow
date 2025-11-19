// Pick Swap - Modern Marketplace JavaScript

const API_BASE_URL = '/api/products';
let currentPage = 0;
let currentCategory = null;
let currentKeyword = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    // 검색
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    
    if (searchBtn) {
        searchBtn.addEventListener('click', handleSearch);
    }
    
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }
    
    // 카테고리 필터
    const categoryChips = document.querySelectorAll('.category-chip');
    categoryChips.forEach(chip => {
        chip.addEventListener('click', () => {
            handleCategoryFilter(chip);
        });
    });
}

// 상품 목록 로드
async function loadProducts(page = 0, category = null, keyword = null) {
    showLoading();
    
    try {
        let url = `${API_BASE_URL}/feed?page=${page}&size=20`;
        
        if (category) {
            url = `${API_BASE_URL}/category/${category}?page=${page}&size=20`;
        } else if (keyword) {
            url = `${API_BASE_URL}/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=20`;
        }
        
        const response = await fetch(url);
        const data = await response.json();
        
        displayProducts(data.content);
        displayPagination(data);
        
        currentPage = page;
        currentCategory = category;
        currentKeyword = keyword;
        
    } catch (error) {
        console.error('상품 로드 실패:', error);
        showError('상품을 불러오는데 실패했습니다.');
    }
}

// 상품 표시
function displayProducts(products) {
    const grid = document.getElementById('product-grid');
    
    if (!products || products.length === 0) {
        grid.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-icon">📦</div>
                <div class="empty-title">등록된 상품이 없습니다</div>
                <div class="empty-description">첫 번째 상품을 등록해보세요!</div>
            </div>
        `;
        return;
    }
    
    grid.innerHTML = products.map(product => `
        <a href="/pickswap/products/${product.id}" class="product-card">
            <img src="${product.imageUrls[0] || 'https://via.placeholder.com/280x240?text=No+Image'}" 
                 alt="${product.title}" 
                 class="product-image"
                 onerror="this.src='https://via.placeholder.com/280x240?text=No+Image'">
            <div class="product-info">
                ${getStatusBadge(product.status)}
                <div class="product-title">${escapeHtml(product.title)}</div>
                <div class="product-price">${formatPrice(product.price)}원</div>
                <div class="product-location">📍 ${escapeHtml(product.location || '위치 미정')}</div>
                <div class="product-meta">
                    <span class="meta-item">❤️ ${product.likeCount}</span>
                    <span class="meta-item">👁️ ${product.viewCount}</span>
                    <span class="meta-item">💬 ${product.chatCount}</span>
                </div>
            </div>
        </a>
    `).join('');
}

// 페이지네이션 표시
function displayPagination(data) {
    const pagination = document.getElementById('pagination');
    
    if (data.totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let html = `
        <button class="page-btn" onclick="changePage(${data.number - 1})" 
                ${data.first ? 'disabled' : ''}>
            ← 이전
        </button>
    `;
    
    // 페이지 번호 (최대 5개)
    const startPage = Math.max(0, data.number - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + 4);
    
    for (let i = startPage; i <= endPage; i++) {
        html += `
            <button class="page-btn ${i === data.number ? 'active' : ''}" 
                    onclick="changePage(${i})">
                ${i + 1}
            </button>
        `;
    }
    
    html += `
        <button class="page-btn" onclick="changePage(${data.number + 1})" 
                ${data.last ? 'disabled' : ''}>
            다음 →
        </button>
    `;
    
    pagination.innerHTML = html;
}

// 페이지 변경
function changePage(page) {
    loadProducts(page, currentCategory, currentKeyword);
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 검색 처리
function handleSearch() {
    const searchInput = document.getElementById('search-input');
    const keyword = searchInput.value.trim();
    
    if (keyword) {
        // 카테고리 필터 초기화
        document.querySelectorAll('.category-chip').forEach(chip => {
            chip.classList.remove('active');
        });
        
        loadProducts(0, null, keyword);
    } else {
        loadProducts(0);
    }
}

// 카테고리 필터 처리
function handleCategoryFilter(chip) {
    const category = chip.dataset.category;
    
    // 검색어 초기화
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // 활성 상태 토글
    const wasActive = chip.classList.contains('active');
    document.querySelectorAll('.category-chip').forEach(c => {
        c.classList.remove('active');
    });
    
    if (!wasActive) {
        chip.classList.add('active');
        loadProducts(0, category);
    } else {
        loadProducts(0);
    }
}

// 로딩 표시
function showLoading() {
    const grid = document.getElementById('product-grid');
    grid.innerHTML = `
        <div class="loading-spinner" style="grid-column: 1 / -1;">
            <div class="spinner"></div>
        </div>
    `;
}

// 에러 표시
function showError(message) {
    const grid = document.getElementById('product-grid');
    grid.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
            <div class="empty-icon">⚠️</div>
            <div class="empty-title">오류가 발생했습니다</div>
            <div class="empty-description">${message}</div>
        </div>
    `;
}

// 상태 뱃지 생성
function getStatusBadge(status) {
    const badges = {
        'AVAILABLE': '<span class="status-badge status-available">판매중</span>',
        'RESERVED': '<span class="status-badge status-reserved">예약중</span>',
        'SOLD': '<span class="status-badge status-sold">판매완료</span>'
    };
    return badges[status] || '';
}

// 가격 포맷팅
function formatPrice(price) {
    return new Intl.NumberFormat('ko-KR').format(price);
}

// HTML 이스케이프
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
