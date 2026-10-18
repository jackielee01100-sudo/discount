package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.ServiceProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 순수 단위 테스트 (Spring/DB 없이 실행) */
class EventResponseTest {

    private DiscountEvent event(ServiceProvider provider) {
        return DiscountEvent.builder().id(10L).name("테스트 행사").provider(provider)
                .startDate(LocalDate.of(2026, 10, 20)).endDate(LocalDate.of(2026, 10, 26))
                .color("#e5004f").description("설명").build();
    }

    @Test
    @DisplayName("서비스가 연결된 행사는 서비스 id/이름이 함께 나간다")
    void withProvider() {
        ServiceProvider skt = ServiceProvider.builder().id(1L).category("통신사").name("SKT").build();

        EventResponse res = EventResponse.of(event(skt), List.of("던킨도너츠"));

        assertThat(res.providerId()).isEqualTo(1L);
        assertThat(res.providerName()).isEqualTo("SKT");
        assertThat(res.brands()).containsExactly("던킨도너츠");
        assertThat(res.startDate()).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    @DisplayName("서비스가 없는 행사는 provider 값이 null이다")
    void withoutProvider() {
        EventResponse res = EventResponse.of(event(null), List.of());

        assertThat(res.providerId()).isNull();
        assertThat(res.providerName()).isNull();
        assertThat(res.brands()).isEmpty();
    }
}
