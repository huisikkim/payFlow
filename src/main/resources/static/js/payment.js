// 토큰 관리
function getToken() {
    return localStorage.getItem('accessToken');
}

function setToken(token) {
    localStorage.setItem('accessToken', token);
}

function removeToken() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
}

function getUsername() {
    return localStorage.getItem('username');
}

// 상품별 상세 내역 데이터
const productDetails = {
    'MSA 아키텍처 설계 패키지': {
        description: '시니어 개발자의 실전 마이크로서비스 아키텍처 설계 노하우를 담은 프리미엄 패키지입니다.',
        features: [
            '모놀리식 → 모듈러 모놀리스 → MSA 단계별 전환 전략',
            '서비스 경계 식별 및 도메인 분리 기법',
            'API Gateway, Service Mesh 패턴 구현',
            '분산 트랜잭션 처리 (Saga Pattern)',
            '서비스 간 통신 최적화 (동기/비동기)',
            '모니터링 및 로깅 전략 (ELK Stack)',
            '장애 격리 및 Circuit Breaker 패턴',
            '실제 프로젝트 마이그레이션 사례 연구'
        ],
        includes: '1:1 컨설팅 1회'
    },
    'DDD 도메인 설계': {
        description: '도메인 주도 설계의 핵심 개념을 실무에 바로 적용할 수 있는 실전 가이드입니다.',
        features: [
            '전략적 설계: Bounded Context, Context Mapping',
            '전술적 설계: Entity, Value Object, Aggregate',
            'Repository 패턴과 영속성 관리',
            'Domain Event를 활용한 느슨한 결합',
            'Layered Architecture vs Hexagonal Architecture',
            '도메인 모델 리팩토링 기법',
            '유비쿼터스 언어 정립 방법론',
            '실무 도메인 모델링 워크샵 자료'
        ],
        includes: '코드 리뷰 1회'
    },
    'EDA 이벤트 기반 처리 (Kafka 통합)': {
        description: 'Apache Kafka를 활용한 이벤트 기반 아키텍처 구축의 모든 것을 담았습니다.',
        features: [
            'Kafka 클러스터 구성 및 운영 노하우',
            'Producer/Consumer 최적화 전략',
            'Event Sourcing 패턴 구현',
            'Topic 설계 및 파티셔닝 전략',
            '메시지 순서 보장 및 중복 처리',
            'Kafka Streams를 활용한 실시간 처리',
            '모니터링 및 장애 대응 가이드'
        ],
        includes: 'Kafka 설정 파일, 트러블슈팅 가이드'
    },
    '토스페이먼츠 통합 솔루션': {
        description: '토스페이먼츠 API를 활용한 완벽한 결제 시스템 구축 가이드입니다.',
        features: [
            '카드/계좌이체/간편결제 통합 구현',
            '결제 보안 및 PCI-DSS 준수 가이드',
            '결제 승인/취소/환불 프로세스',
            '정산 데이터 관리 및 리포팅',
            '결제 실패 처리 및 재시도 로직',
            'PG사 연동 테스트 자동화',
            '모바일 결제 최적화 (앱/웹뷰)'
        ],
        includes: '결제 모듈 소스코드, 테스트 시나리오'
    },
    '실무 경험 기반 서비스 상품': {
        description: '레거시 시스템을 현대적인 아키텍처로 전환한 실전 경험을 공유합니다.',
        features: [
            '레거시 코드 분석 및 리팩토링 전략',
            '기술 부채 측정 및 우선순위 설정',
            '무중단 마이그레이션 기법 (Strangler Pattern)',
            '데이터 마이그레이션 및 동기화 전략',
            '회귀 테스트 자동화 구축',
            '성능 개선 및 병목 지점 해소',
            '팀 협업 및 지식 전파 방법론',
            '마이그레이션 체크리스트 및 롤백 계획'
        ],
        includes: '사례 연구'
    },
    'Spring Security + JWT 인증': {
        description: '토큰 기반 인증/인가 시스템을 Spring Security로 완벽하게 구현하는 방법입니다.',
        features: [
            'JWT 토큰 생성 및 검증 로직',
            'Spring Security Filter Chain 커스터마이징',
            '사용자 인증 및 권한 관리 (RBAC)',
            'Access Token / Refresh Token 전략',
            '토큰 블랙리스트 및 로그아웃 처리',
            'OAuth2 소셜 로그인 통합 (Google, Kakao)',
            '다중 디바이스 세션 관리',
            'API 엔드포인트별 권한 설정'
        ],
        includes: 'Security 설정 코드'
    },
    '블록체인 연동 모듈': {
        description: '블록체인 기술을 실제 서비스에 통합한 경험을 바탕으로 한 실전 가이드입니다.',
        features: [
            'Ethereum 스마트 컨트랙트 개발 (Solidity)',
            'Web3.js를 활용한 블록체인 연동',
            '암호화폐 지갑 생성 및 관리',
            '국내/해외 거래소 API 통합 (Upbit, Binance)',
            '개인키 보안 및 암호화 전략',
            '가스비 최적화 기법',
            'NFT 발행 및 거래 시스템 구축',
            '실시간 시세 조회 및 자동 매매 로직'
        ],
        includes: '스마트 컨트랙트 소스, Web3 연동 모듈'
    },
    '키오스크 셀프 결제 시스템': {
        description: '무인 주문/결제 단말기 개발 경험을 담은 하드웨어 연동 실전 가이드입니다.',
        features: [
            '터치스크린 최적화',
            '카드 리더기 연동 (VAN사 프로토콜)',
            '영수증 프린터 제어 (ESC/POS 명령)',
            '음성 안내 및 접근성 기능',
            'QR코드 결제 통합 (제로페이, 카카오페이)',
            '주문 데이터 실시간 동기화',
            '오프라인 모드 및 장애 대응'
        ],
        includes: '키오스크 앱 소스코드, 하드웨어 연동 가이드, 설치 매뉴얼'
    },
    'GR 카트 영상 관리 시스템': {
        description: 'TCP 소켓 통신을 활용한 실시간 영상 스트리밍 및 관리 시스템 구축 경험입니다.',
        features: [
            'GR 카트 센서 데이터 실시간 수집',
            '다중 카메라 영상 스트리밍',
            'TCP 소켓 서버/클라이언트 구현',
            '영상 녹화 및 클라우드 저장 (S3)',
            '영상 인코딩/디코딩 최적화 (FFmpeg)',
            '실시간 텔레메트리 데이터 시각화',
            '영상 동기화 및 타임스탬프 관리',
            '이벤트 기반 영상 하이라이트 추출'
        ],
        includes: '영상 처리 모듈, 관리자 대시보드'
    }
};

// 상세 내역 렌더링 함수
function renderProductDetails(productName) {
    const details = productDetails[productName];
    if (!details) {
        document.getElementById('product-details').style.display = 'none';
        return;
    }
    
    document.getElementById('product-details').style.display = 'block';
    
    let html = `
        <p style="margin-bottom: 16px; font-weight: 500; text-align: left;">${details.description}</p>
        <div style="margin-bottom: 16px; text-align: left;">
            <strong style="display: block; margin-bottom: 8px; color: #212529; text-align: left;">✨ 포함 내용:</strong>
            <ul style="margin: 0; padding-left: 20px; text-align: left;">
                ${details.features.map(feature => `<li style="margin-bottom: 6px; text-align: left;">${feature}</li>`).join('')}
            </ul>
        </div>
        <div style="padding: 12px; background: white; border-radius: 6px; border-left: 3px solid #2d2d2d; text-align: left;">
            <strong style="color: #212529;">🎁 제공 항목:</strong> ${details.includes}
        </div>
    `;
    
    document.getElementById('product-details-content').innerHTML = html;
}

function showLoginForm() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('payment-section').style.display = 'none';
    document.getElementById('user-info').style.display = 'none';
}

function showPaymentSection() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('payment-section').style.display = 'block';
    document.getElementById('user-info').style.display = 'flex';
    document.getElementById('username-display').textContent = '👤 ' + getUsername();
}

// 로그아웃
function logout() {
    removeToken();
    showLoginForm();
}

// 초기화 함수
function initPaymentPage(clientKey) {
    const tossPayments = TossPayments(clientKey);
    
    // 페이지 로드 시 로그인 상태 확인 및 상품 정보 로드
    window.addEventListener('load', () => {
        // 선택된 상품 정보 로드
        const selectedProduct = sessionStorage.getItem('selectedProduct');
        if (selectedProduct) {
            const product = JSON.parse(selectedProduct);
            document.querySelector('.product-emoji').textContent = product.emoji;
            document.getElementById('product-name').textContent = product.name;
            document.getElementById('product-price').textContent = product.price.toLocaleString() + '원';
            // 전역 변수로 저장
            window.currentProduct = product;
            // 상세 내역 렌더링
            renderProductDetails(product.name);
        }
        
        const token = getToken();
        if (token) {
            showPaymentSection();
        } else {
            showLoginForm();
        }
    });
    
    // 로그인 처리
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const loading = document.getElementById('login-loading');
        
        try {
            loading.style.display = 'block';
            
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            
            if (!response.ok) {
                throw new Error('로그인 실패');
            }
            
            const data = await response.json();
            setToken(data.accessToken);
            localStorage.setItem('username', data.username);
            
            showPaymentSection();
        } catch (error) {
            console.error('로그인 실패:', error);
            alert('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
        } finally {
            loading.style.display = 'none';
        }
    });
    
    // 결제 처리
    document.getElementById('payment-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const customerName = document.getElementById('customerName').value;
        const customerEmail = document.getElementById('customerEmail').value;
        const loading = document.getElementById('loading');
        const token = getToken();
        
        if (!token) {
            alert('로그인이 필요합니다.');
            showLoginForm();
            return;
        }
        
        try {
            loading.style.display = 'block';
            
            // 현재 상품 정보 가져오기
            const product = window.currentProduct || { name: '프리미엄 개발자 도구', price: 1000 };
            
            // 1. 주문 생성 (Order Service) - JWT 토큰 포함
            const orderResponse = await fetch('/api/orders', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify({
                    orderName: product.name,
                    amount: product.price,
                    customerEmail: customerEmail,
                    customerName: customerName
                })
            });
            
            if (orderResponse.status === 401 || orderResponse.status === 403) {
                alert('인증이 만료되었습니다. 다시 로그인해주세요.');
                removeToken();
                showLoginForm();
                return;
            }
            
            const order = await orderResponse.json();
            console.log('주문 생성:', order);
            
            // 2. 토스페이먼츠 결제창 호출
            await tossPayments.requestPayment('카드', {
                amount: order.amount,
                orderId: order.orderId,
                orderName: order.orderName,
                customerName: customerName,
                customerEmail: customerEmail,
                successUrl: window.location.origin + '/success',
                failUrl: window.location.origin + '/fail',
            });
        } catch (error) {
            console.error('결제 요청 실패:', error);
            alert('결제 요청에 실패했습니다.');
            loading.style.display = 'none';
        }
    });
}
