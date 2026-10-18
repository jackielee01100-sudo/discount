package com.example.discount.web;

import com.example.discount.domain.*;
import com.example.discount.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 할인 행사/매장 API 테스트. 실제 PostgreSQL 사용, 테스트마다 롤백되어 데이터는 남지 않음.
 * DB에 이미 있는 데이터(T데이 등)와 섞이지 않도록 테스트 전용 이름(TEST-...)으로 만들어 검증.
 */
@SpringBootTest
@Transactional
class DiscountApiTest {

    @Autowired WebApplicationContext context;
    @Autowired ServiceProviderRepository providers;
    @Autowired DiscountEventRepository events;
    @Autowired EventPlaceRepository places;
    @Autowired AppUserRepository users;
    @Autowired MyServiceRepository myServices;

    MockMvc mvc;
    ServiceProvider testTelecom;
    DiscountEvent telecomEvent;   // 10/20~10/26, 서비스 연결
    DiscountEvent commonEvent;    // 10/15~10/31, 서비스 없음

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        testTelecom = providers.save(ServiceProvider.builder().category("통신사").name("TEST-TELECOM").build());
        telecomEvent = events.save(DiscountEvent.builder().name("TEST-통신사데이").provider(testTelecom)
                .startDate(LocalDate.of(2026, 10, 20)).endDate(LocalDate.of(2026, 10, 26))
                .color("#e5004f").build());
        commonEvent = events.save(DiscountEvent.builder().name("TEST-공통행사")
                .startDate(LocalDate.of(2026, 10, 15)).endDate(LocalDate.of(2026, 10, 31))
                .color("#2563eb").build());

        places.save(place(telecomEvent, "TEST-도넛", "TEST-도넛 1호점"));
        places.save(place(telecomEvent, "TEST-빵", "TEST-빵 1호점"));
        places.save(place(commonEvent, null, "TEST-편의점"));
    }

    private EventPlace place(DiscountEvent event, String brand, String name) {
        return EventPlace.builder().event(event).brand(brand).name(name)
                .latitude(37.5).longitude(127.0).benefit("테스트 혜택").build();
    }

    private String ids(DiscountEvent... es) {
        StringBuilder sb = new StringBuilder();
        for (DiscountEvent e : es) sb.append(sb.isEmpty() ? "" : ",").append(e.getId());
        return sb.toString();
    }

    @Nested
    @DisplayName("GET /api/places - 날짜/행사/브랜드 필터")
    class Places {

        @Test
        @DisplayName("행사 기간 안의 날짜면 두 행사 매장이 모두 나온다")
        void dateInsideBothEvents() throws Exception {
            mvc.perform(get("/api/places")
                            .param("date", "2026-10-22")
                            .param("eventIds", ids(telecomEvent, commonEvent)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[*].name", hasItems("TEST-도넛 1호점", "TEST-빵 1호점", "TEST-편의점")));
        }

        @Test
        @DisplayName("통신사데이 시작 전 날짜면 공통행사 매장만 나온다")
        void dateBeforeTelecomEvent() throws Exception {
            mvc.perform(get("/api/places")
                            .param("date", "2026-10-18")
                            .param("eventIds", ids(telecomEvent, commonEvent)))
                    .andExpect(jsonPath("$[*].name", contains("TEST-편의점")));
        }

        @ParameterizedTest(name = "{0} → {1}곳")
        @CsvSource({
                "2026-10-20, 2",   // 시작일 당일 포함
                "2026-10-26, 2",   // 종료일 당일 포함
                "2026-10-19, 0",   // 시작 하루 전
                "2026-10-27, 0"    // 종료 다음 날
        })
        @DisplayName("행사 시작일/종료일 경계")
        void periodBoundary(String date, int expected) throws Exception {
            mvc.perform(get("/api/places").param("date", date).param("eventIds", ids(telecomEvent)))
                    .andExpect(jsonPath("$.length()").value(expected));
        }

        @Test
        @DisplayName("브랜드를 고르면 그 브랜드 매장만 남고, 브랜드 없는 매장은 그대로 나온다")
        void brandFilter() throws Exception {
            mvc.perform(get("/api/places")
                            .param("date", "2026-10-22")
                            .param("eventIds", ids(telecomEvent, commonEvent))
                            .param("brands", "TEST-도넛"))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("TEST-도넛 1호점", "TEST-편의점")));
        }

        @Test
        @DisplayName("행사를 하나도 고르지 않으면 빈 목록")
        void noEventSelected() throws Exception {
            mvc.perform(get("/api/places").param("date", "2026-10-22"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("날짜 형식이 틀리면 400")
        void badDate() throws Exception {
            mvc.perform(get("/api/places").param("date", "2026/10/22").param("eventIds", ids(telecomEvent)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/events?mine=true - 나의 정보 기준 필터")
    class MyEvents {

        private AppUser createUser(String username) {
            return users.save(AppUser.builder().username(username).password("{noop}not-used")
                    .role(Role.USER).createdAt(LocalDateTime.now()).build());
        }

        @Test
        @DisplayName("로그인하지 않으면 모든 행사가 나온다")
        void anonymousSeesAll() throws Exception {
            mvc.perform(get("/api/events").param("mine", "true"))
                    .andExpect(jsonPath("$[*].name", hasItems("TEST-통신사데이", "TEST-공통행사")));
        }

        @Test
        @DisplayName("다른 서비스를 고른 사용자에게는 통신사데이가 숨겨진다")
        void otherProviderHidesEvent() throws Exception {
            AppUser u = createUser("test_other");
            ServiceProvider other = providers.save(ServiceProvider.builder().category("통신사").name("TEST-OTHER").build());
            myServices.save(new MyService(u, other));

            mvc.perform(get("/api/events").param("mine", "true").with(user("test_other")))
                    .andExpect(jsonPath("$[*].name", hasItem("TEST-공통행사")))
                    .andExpect(jsonPath("$[*].name", not(hasItem("TEST-통신사데이"))));
        }

        @Test
        @DisplayName("같은 서비스를 고른 사용자에게는 통신사데이가 보이고 브랜드도 함께 나온다")
        void sameProviderShowsEvent() throws Exception {
            AppUser u = createUser("test_same");
            myServices.save(new MyService(u, testTelecom));

            mvc.perform(get("/api/events").param("mine", "true").with(user("test_same")))
                    .andExpect(jsonPath("$[?(@.name == 'TEST-통신사데이')].providerName").value("TEST-TELECOM"))
                    .andExpect(jsonPath("$[?(@.name == 'TEST-통신사데이')].brands[*]",
                            containsInAnyOrder("TEST-도넛", "TEST-빵")));
        }
    }

    @Nested
    @DisplayName("POST /api/signup - 입력값 검사")
    class SignupValidation {

        @ParameterizedTest(name = "아이디=''{0}'', 비밀번호 길이={1} → 400")
        @CsvSource({
                "ab, 12",              // 아이디 너무 짧음
                "has space, 12",       // 허용되지 않는 문자
                "한글아이디, 12",       // 한글 불가
                "valid_name, 7"        // 비밀번호 8자 미만
        })
        void rejectsInvalidInput(String username, int passwordLength) throws Exception {
            String password = "x".repeat(passwordLength);
            mvc.perform(post("/api/signup").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }
}
