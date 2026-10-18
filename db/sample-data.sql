-- 샘플 데이터 (앱이 자동 실행하지 않음. 새 DB에 데이터를 넣을 때 직접 실행)
-- 실행: docker exec -i discount-postgres psql -U discount -d discount < db/sample-data.sql
-- 테이블은 앱을 한 번 실행하면 자동 생성됨 (spring.jpa.hibernate.ddl-auto=update)
-- 행사 기간·매장 위치·혜택은 모두 샘플이며 실제 정보가 아님

-- 1) 서비스 (나의 정보 화면의 선택지)
insert into service_provider (category, name) values
    ('통신사', 'SKT'),
    ('통신사', 'KT'),
    ('통신사', 'LG U+');

-- 2) 할인 행사 (provider_id가 null이면 서비스와 무관하게 모두에게 보임)
insert into discount_event (name, provider_id, start_date, end_date, color, description) values
    ('T데이', (select id from service_provider where name = 'SKT'), '2026-10-20', '2026-10-26', '#e5004f', 'T데이 할인 기간'),
    ('멤버십 위크', null, '2026-10-15', '2026-10-31', '#2563eb', '멤버십 회원 할인 기간');

-- 3) 행사 매장 (brand가 있으면 지도에서 행사 아래 하위 선택지로 나옴)
insert into event_place (event_id, brand, name, address, latitude, longitude, benefit) values
    ((select id from discount_event where name = 'T데이'), '던킨도너츠', '던킨도너츠 강남역점 (샘플)', '서울 강남구 (샘플 위치)', 37.4979, 127.0276, '샘플 혜택: 도넛 할인'),
    ((select id from discount_event where name = 'T데이'), '던킨도너츠', '던킨도너츠 홍대점 (샘플)', '서울 마포구 (샘플 위치)', 37.5563, 126.9220, '샘플 혜택: 도넛 할인'),
    ((select id from discount_event where name = 'T데이'), '파리바게뜨', '파리바게뜨 광화문점 (샘플)', '서울 종로구 (샘플 위치)', 37.5714, 126.9768, '샘플 혜택: 빵 할인'),
    ((select id from discount_event where name = 'T데이'), '파리바게뜨', '파리바게뜨 잠실점 (샘플)', '서울 송파구 (샘플 위치)', 37.5110, 127.0980, '샘플 혜택: 빵 할인'),
    ((select id from discount_event where name = 'T데이'), '쉑쉑버거', '쉑쉑버거 신논현점 (샘플)', '서울 강남구 (샘플 위치)', 37.5046, 127.0250, '샘플 혜택: 버거 할인'),
    ((select id from discount_event where name = 'T데이'), '쉑쉑버거', '쉑쉑버거 여의도점 (샘플)', '서울 영등포구 (샘플 위치)', 37.5250, 126.9250, '샘플 혜택: 버거 할인'),
    ((select id from discount_event where name = '멤버십 위크'), null, '제휴 편의점 잠실점', '서울 송파구 올림픽로 (샘플)', 37.5133, 127.1001, '도시락 30% 할인'),
    ((select id from discount_event where name = '멤버십 위크'), null, '제휴 서점 종로점', '서울 종로구 종로 (샘플)', 37.5700, 126.9830, '도서 10% 할인'),
    ((select id from discount_event where name = '멤버십 위크'), null, '제휴 피자 신촌점', '서울 서대문구 연세로 (샘플)', 37.5551, 126.9368, '라지 피자 5,000원 할인');
