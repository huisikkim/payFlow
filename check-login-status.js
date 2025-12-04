// 브라우저 콘솔(F12)에서 실행하여 로그인 상태 확인
console.log('=== 로그인 상태 확인 ===');
console.log('JWT Token:', localStorage.getItem('jwt_token'));
console.log('Username:', localStorage.getItem('username'));
console.log('Nickname:', localStorage.getItem('nickname'));

if (localStorage.getItem('jwt_token')) {
    console.log('✅ 로그인 상태입니다');
} else {
    console.log('❌ 로그인되지 않았습니다');
    console.log('로그인 페이지: http://localhost:8080/youtube/login');
}
