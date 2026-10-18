package com.example.discount.config;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.EventPlace;
import com.example.discount.repository.DiscountEventRepository;
import com.example.discount.repository.EventPlaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * 샘플 데이터. 실제 행사 일정/매장이 아님.
 * 이미 있는 데이터는 건드리지 않음 (행사는 이름으로, 매장은 테이블이 비었을 때만 등록)
 */
@Configuration
public class SampleDataConfig {

    @Bean
    public CommandLineRunner seedSampleData(DiscountEventRepository events, EventPlaceRepository places) {
        return args -> {
            DiscountEvent tday = findOrCreate(events, "T데이",
                    LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 26), "#e5004f", "T데이 할인 기간");
            DiscountEvent membership = findOrCreate(events, "멤버십 위크",
                    LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 31), "#2563eb", "멤버십 회원 할인 기간");

            if (places.count() > 0) return;
            places.saveAll(List.of(
                    // T데이: 브랜드별 매장 (위치/혜택 모두 샘플)
                    place(tday, "던킨도너츠", "던킨도너츠 강남역점 (샘플)", "서울 강남구 (샘플 위치)", 37.4979, 127.0276, "샘플 혜택: 도넛 할인"),
                    place(tday, "던킨도너츠", "던킨도너츠 홍대점 (샘플)", "서울 마포구 (샘플 위치)", 37.5563, 126.9220, "샘플 혜택: 도넛 할인"),
                    place(tday, "파리바게뜨", "파리바게뜨 광화문점 (샘플)", "서울 종로구 (샘플 위치)", 37.5714, 126.9768, "샘플 혜택: 빵 할인"),
                    place(tday, "파리바게뜨", "파리바게뜨 잠실점 (샘플)", "서울 송파구 (샘플 위치)", 37.5110, 127.0980, "샘플 혜택: 빵 할인"),
                    place(tday, "쉑쉑버거", "쉑쉑버거 신논현점 (샘플)", "서울 강남구 (샘플 위치)", 37.5046, 127.0250, "샘플 혜택: 버거 할인"),
                    place(tday, "쉑쉑버거", "쉑쉑버거 여의도점 (샘플)", "서울 영등포구 (샘플 위치)", 37.5250, 126.9250, "샘플 혜택: 버거 할인"),
                    // 멤버십 위크: 브랜드 구분 없음
                    place(membership, null, "제휴 편의점 잠실점", "서울 송파구 올림픽로 (샘플)", 37.5133, 127.1001, "도시락 30% 할인"),
                    place(membership, null, "제휴 서점 종로점", "서울 종로구 종로 (샘플)", 37.5700, 126.9830, "도서 10% 할인"),
                    place(membership, null, "제휴 피자 신촌점", "서울 서대문구 연세로 (샘플)", 37.5551, 126.9368, "라지 피자 5,000원 할인")
            ));
        };
    }

    private DiscountEvent findOrCreate(DiscountEventRepository events, String name,
                                       LocalDate start, LocalDate end, String color, String description) {
        return events.findByName(name).orElseGet(() -> events.save(DiscountEvent.builder()
                .name(name).startDate(start).endDate(end).color(color).description(description).build()));
    }

    private EventPlace place(DiscountEvent event, String brand, String name, String address,
                             double lat, double lng, String benefit) {
        return EventPlace.builder().event(event).brand(brand).name(name).address(address)
                .latitude(lat).longitude(lng).benefit(benefit).build();
    }
}
