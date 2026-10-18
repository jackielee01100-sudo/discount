package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.EventPlace;
import com.example.discount.domain.ServiceProvider;
import com.example.discount.repository.DiscountEventRepository;
import com.example.discount.repository.EventPlaceRepository;
import com.example.discount.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 관리자 API 테스트. 테스트 후 롤백 */
@SpringBootTest
@Transactional
class AdminApiTest {

    @Autowired WebApplicationContext context;
    @Autowired ServiceProviderRepository providers;
    @Autowired DiscountEventRepository events;
    @Autowired EventPlaceRepository places;

    MockMvc mvc;
    DiscountEvent event;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        ServiceProvider telecom = providers.save(ServiceProvider.builder().category("통신사").name("TEST-TELECOM").build());
        event = events.save(DiscountEvent.builder().name("TEST-카드행사").provider(telecom)
                .startDate(LocalDate.of(2026, 10, 20)).endDate(LocalDate.of(2026, 10, 26))
                .color("#e5004f").description("테스트 설명").build());
        places.save(place(null, "TEST-제휴점", "혜택C"));
        places.save(place("TEST-빵", "TEST-빵 2호점", "혜택B"));
        places.save(place("TEST-도넛", "TEST-도넛 1호점", "혜택A"));
    }

    private EventPlace place(String brand, String name, String benefit) {
        return EventPlace.builder().event(event).brand(brand).name(name)
                .latitude(37.5).longitude(127.0).benefit(benefit).build();
    }

    @Test
    @DisplayName("일반사용자·비로그인은 관리자 API에 접근할 수 없다")
    void forbiddenForNonAdmin() throws Exception {
        mvc.perform(get("/api/admin/events").with(user("someone").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자는 행사 목록과 매장 수를 본다")
    void adminListsEvents() throws Exception {
        mvc.perform(get("/api/admin/events").with(user("boss").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'TEST-카드행사')].placeCount").value(3))
                .andExpect(jsonPath("$[?(@.name == 'TEST-카드행사')].providerName").value("TEST-TELECOM"));
    }

    @Test
    @DisplayName("카드 데이터: 매장은 브랜드순, 브랜드 없는 매장은 맨 뒤")
    void cardData() throws Exception {
        mvc.perform(get("/api/admin/events/{id}/card", event.getId()).with(user("boss").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST-카드행사"))
                .andExpect(jsonPath("$.providerName").value("TEST-TELECOM"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.places[*].name", contains("TEST-도넛 1호점", "TEST-빵 2호점", "TEST-제휴점")));
    }

    @Test
    @DisplayName("없는 행사는 404")
    void cardNotFound() throws Exception {
        mvc.perform(get("/api/admin/events/{id}/card", -1).with(user("boss").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }
}
