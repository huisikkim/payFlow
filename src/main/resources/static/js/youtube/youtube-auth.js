/**
 * YouTube 페이지 인증 관련 함수
 */

// 로그인 상태 체크 및 UI 업데이트
function checkLoginStatus() {
    const token = localStorage.getItem('jwt_token');
    const username = localStorage.getItem('username');
    const nickname = localStorage.getItem('nickname');
    
    const userInfo = document.getElementById('userInfo');
    const loginLink = document.getElementById('loginLink');
    
    if (!userInfo || !loginLink) {
        console.warn('헤더 요소를 찾을 수 없습니다.');
        return;
    }
    
    if (token && username) {
        userInfo.style.display = 'flex';
        // 닉네임이 있으면 닉네임 표시, 없으면 username 표시
        const displayName = nickname || username;
        userInfo.querySelector('.username').textContent = displayName + '님';
        loginLink.style.display = 'none';
    } else {
        userInfo.style.display = 'none';
        loginLink.style.display = 'flex';
    }
}

// 로그아웃
function youtubeLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('username');
        localStorage.removeItem('nickname');
        
        // 로그인 페이지로 리다이렉트
        window.location.href = '/youtube/popular';
    }
}

// 페이지 로드 시 로그인 상태 체크
document.addEventListener('DOMContentLoaded', function() {
    checkLoginStatus();
});

// 다른 탭에서 로그인/로그아웃 시 동기화
window.addEventListener('storage', function(e) {
    if (e.key === 'jwt_token' || e.key === 'username' || e.key === 'nickname') {
        checkLoginStatus();
    }
});
