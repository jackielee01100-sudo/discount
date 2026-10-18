// 할인 행사 카드 이미지를 <canvas>에 그림. 관리자 화면에서 PNG로 다운로드할 때 사용.
// card: { name, providerName, startDate, endDate, color, description, places: [{brand, name, address, benefit}] }

const CARD_FONT = '"Malgun Gothic", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif';
const CARD_DOW = ['일', '월', '화', '수', '목', '금', '토'];

function cardDate(s) {
    const [y, m, d] = s.split('-').map(Number);
    return `${y}.${String(m).padStart(2, '0')}.${String(d).padStart(2, '0')}(${CARD_DOW[new Date(y, m - 1, d).getDay()]})`;
}

/** 배경색이 밝으면 어두운 글자, 어두우면 흰 글자 */
function cardTextColor(hex) {
    const h = hex.replace('#', '');
    const [r, g, b] = [0, 2, 4].map(i => parseInt(h.substr(i, 2), 16));
    return (0.299 * r + 0.587 * g + 0.114 * b) > 170 ? '#111827' : '#ffffff';
}

/** 최대 너비를 넘으면 줄바꿈 (한글은 글자 단위로 나눔) */
function cardWrap(ctx, text, maxWidth) {
    const lines = [];
    let line = '';
    for (const ch of String(text ?? '')) {
        if (ctx.measureText(line + ch).width > maxWidth && line) {
            lines.push(line);
            line = ch.trimStart();
        } else {
            line += ch;
        }
    }
    if (line) lines.push(line);
    return lines;
}

/** 매장을 브랜드별로 묶음 (브랜드 없는 매장은 '제휴 매장') */
function cardGroups(places) {
    const groups = new Map();
    places.forEach(p => {
        const key = p.brand || '제휴 매장';
        if (!groups.has(key)) groups.set(key, []);
        groups.get(key).push(p);
    });
    return groups;
}

function drawEventCard(canvas, card) {
    const W = 1080, PAD = 72, INNER = W - PAD * 2;
    const color = card.color || '#2563eb';
    const headText = cardTextColor(color);
    const ctx = canvas.getContext('2d');

    // 1차: 높이 계산, 2차: 실제 그리기 (같은 함수로 두 번 실행)
    let headHeight = 0; // 1차에서 계산한 머리 영역 높이
    function layout(draw) {
        let y = 0;
        if (draw) {
            ctx.fillStyle = color;
            ctx.fillRect(0, 0, W, headHeight);
        }
        const text = (str, x, size, weight, fill, align = 'left') => {
            ctx.font = `${weight} ${size}px ${CARD_FONT}`;
            if (draw) {
                ctx.fillStyle = fill;
                ctx.textAlign = align;
                ctx.textBaseline = 'top';
                ctx.fillText(str, x, y);
            }
        };
        const lines = (str, size, weight, fill, lineGap) => {
            ctx.font = `${weight} ${size}px ${CARD_FONT}`;
            cardWrap(ctx, str, INNER).forEach(l => { text(l, PAD, size, weight, fill); y += size + lineGap; });
        };

        // ---- 머리 영역 (행사 색) ----
        y += PAD;
        if (card.providerName) {
            // 서비스 배지 (예: SKT)
            ctx.font = `700 28px ${CARD_FONT}`;
            const bw = ctx.measureText(card.providerName).width + 36;
            const badgeTop = y;
            if (draw) {
                ctx.fillStyle = headText === '#ffffff' ? 'rgba(255,255,255,0.22)' : 'rgba(17,24,39,0.12)';
                ctx.beginPath();
                ctx.roundRect(PAD, badgeTop, bw, 48, 24);
                ctx.fill();
            }
            y = badgeTop + 10;
            text(card.providerName, PAD + 18, 28, 700, headText);
            y = badgeTop + 48 + 28;
        }
        lines(card.name, 68, 800, headText, 14);
        y += 10;
        lines(`${cardDate(card.startDate)} ~ ${cardDate(card.endDate)}`, 36, 600, headText, 10);
        if (card.description) {
            y += 6;
            lines(card.description, 28, 400, headText, 10);
        }
        y += PAD - 10;
        if (!draw) headHeight = y;

        // ---- 매장 목록 ----
        y += 56;
        const groups = cardGroups(card.places || []);
        if (groups.size === 0) {
            text('등록된 매장이 없습니다.', PAD, 30, 400, '#6b7280');
            y += 50;
        }
        groups.forEach((list, brand) => {
            // 브랜드 제목
            if (draw) {
                ctx.fillStyle = color;
                ctx.beginPath();
                ctx.arc(PAD + 10, y + 21, 10, 0, Math.PI * 2);
                ctx.fill();
            }
            text(brand, PAD + 34, 40, 800, '#111827');
            y += 40 + 22;

            list.forEach(p => {
                if (draw) {
                    ctx.fillStyle = '#e5e7eb';
                    ctx.fillRect(PAD, y, INNER, 2);
                }
                y += 22;
                lines(p.name, 32, 700, '#111827', 8);
                // 혜택은 행사 색으로 강조 (너무 밝은 색이면 읽기 어려우니 진한 글자)
                if (p.benefit) lines(p.benefit, 30, 600, headText === '#ffffff' ? color : '#111827', 8);
                if (p.address) lines(p.address, 24, 400, '#6b7280', 6);
                y += 14;
            });
            y += 30;
        });

        // ---- 바닥글 ----
        if (draw) {
            ctx.fillStyle = '#e5e7eb';
            ctx.fillRect(PAD, y, INNER, 2);
        }
        y += 26;
        const today = new Date();
        const made = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
        text(`할인 정보 · ${made} 생성`, PAD, 24, 400, '#9ca3af');
        y += 24 + PAD;
        return y;
    }

    const height = Math.ceil(layout(false));
    canvas.width = W;
    canvas.height = height;
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, W, height);
    layout(true);
}

/** canvas를 PNG 파일로 다운로드 */
function downloadCanvas(canvas, filename) {
    canvas.toBlob(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    }, 'image/png');
}
