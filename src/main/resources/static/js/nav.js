// 공통 상단 메뉴. 페이지에 <nav id="nav" data-active="calendar|map|me|admin"></nav> 를 두고 이 파일을 불러옴.
// 로그인 상태에 따라 로그인/회원가입 또는 사용자명/로그아웃, 관리자면 관리자 메뉴를 보여줌.

/** CSRF 토큰 (서버가 내려준 XSRF-TOKEN 쿠키). POST/PUT/DELETE 요청 헤더 X-XSRF-TOKEN에 담아 보냄 */
function csrfToken() {
    const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return m ? decodeURIComponent(m[1]) : '';
}

(function () {
    const style = document.createElement('style');
    style.textContent = `
        nav#nav { background:#111827; padding:0 24px; display:flex; align-items:center; gap:4px; flex-wrap:wrap; }
        nav#nav .brand { color:#fff; font-weight:700; margin-right:20px; }
        nav#nav a { color:#d1d5db; text-decoration:none; padding:14px 12px; border-bottom:2px solid transparent; }
        nav#nav a.on, nav#nav a:hover { color:#fff; border-bottom-color:#60a5fa; }
        nav#nav .right { margin-left:auto; display:flex; align-items:center; gap:8px; color:#d1d5db; font-size:14px; }
        nav#nav .right a { padding:14px 6px; }
        nav#nav .badge { background:#f59e0b; color:#111827; font-size:11px; font-weight:700; padding:2px 6px; border-radius:999px; }
        nav#nav form { margin:0; }
        nav#nav button { background:none; border:1px solid #4b5563; color:#d1d5db; border-radius:6px; padding:4px 10px; cursor:pointer; font-size:13px; }
    `;
    document.head.appendChild(style);

    const nav = document.getElementById('nav');
    const active = nav.dataset.active;
    const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    const link = (key, href, text) => `<a href="${href}" class="${active === key ? 'on' : ''}">${text}</a>`;

    function render(me) {
        let html = '<span class="brand">할인 정보</span>'
            + link('calendar', '/', '달력')
            + link('map', '/map.html', '지도')
            + link('me', '/me.html', '나의 정보');
        if (me.admin) html += link('admin', '/admin.html', '관리자');

        html += '<span class="right">';
        if (me.loggedIn) {
            html += `<span>${esc(me.username)}</span>`
                + (me.admin ? '<span class="badge">관리자</span>' : '')
                + `<form method="post" action="/logout"><input type="hidden" name="_csrf" value="${esc(csrfToken())}"><button type="submit">로그아웃</button></form>`;
        } else {
            html += '<a href="/login.html">로그인</a><a href="/signup.html">회원가입</a>';
        }
        html += '</span>';
        nav.innerHTML = html;
    }

    render({ loggedIn: false, admin: false });
    fetch('/api/auth/me').then(r => r.json()).then(render).catch(() => {});
})();
