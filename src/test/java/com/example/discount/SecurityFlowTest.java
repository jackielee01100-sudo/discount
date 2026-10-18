package com.example.discount;

import com.example.discount.domain.AppUser;
import com.example.discount.domain.Role;
import com.example.discount.domain.ServiceProvider;
import com.example.discount.repository.AppUserRepository;
import com.example.discount.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 로그인/권한 흐름 테스트. 실제 PostgreSQL 사용, 테스트 후 롤백되어 데이터는 남지 않음 */
@SpringBootTest
@Transactional
class SecurityFlowTest {

    @Autowired WebApplicationContext context;
    @Autowired AppUserRepository appUserRepository;
    @Autowired ServiceProviderRepository serviceProviderRepository;
    @Autowired PasswordEncoder passwordEncoder;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private void signup(String username, String password) throws Exception {
        mvc.perform(post("/api/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void signupStoresEncryptedPasswordAsUser() throws Exception {
        signup("test_user1", "test-pass-123");

        AppUser saved = appUserRepository.findByUsername("test_user1").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getPassword()).isNotEqualTo("test-pass-123");
        assertThat(passwordEncoder.matches("test-pass-123", saved.getPassword())).isTrue();

        // 같은 아이디로 다시 가입하면 409
        mvc.perform(post("/api/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test_user1\",\"password\":\"another-pass\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void signupRejectsWithoutCsrfToken() throws Exception {
        mvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test_user2\",\"password\":\"test-pass-123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithCorrectAndWrongPassword() throws Exception {
        signup("test_user3", "test-pass-123");

        mvc.perform(formLogin("/login").user("test_user3").password("test-pass-123"))
                .andExpect(authenticated().withUsername("test_user3").withRoles("USER"));
        mvc.perform(formLogin("/login").user("test_user3").password("wrong-pass"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login.html?error"));
    }

    @Test
    void anonymousAccess() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isOk());          // 달력/지도 데이터는 공개
        mvc.perform(get("/map.html")).andExpect(status().isOk());
        mvc.perform(get("/api/me/services")).andExpect(status().isUnauthorized()); // API는 401
        mvc.perform(get("/me.html")).andExpect(status().is3xxRedirection())        // 화면은 로그인으로
                .andExpect(redirectedUrl("/login.html"));
        mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.loggedIn").value(false));
    }

    @Test
    void adminPageOnlyForAdmin() throws Exception {
        mvc.perform(get("/admin.html").with(user("someone").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").with(user("boss").roles("ADMIN")))
                .andExpect(jsonPath("$.admin").value(true));
        mvc.perform(get("/api/auth/me").with(user("someone").roles("USER")))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void myServicesAreSavedPerUser() throws Exception {
        signup("test_user4", "test-pass-123");
        signup("test_user5", "test-pass-123");
        ServiceProvider provider = serviceProviderRepository.save(
                ServiceProvider.builder().category("테스트").name("TEST-PROVIDER").build());

        mvc.perform(put("/api/me/services").with(user("test_user4")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + provider.getId() + "]"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/me/services").with(user("test_user4")))
                .andExpect(jsonPath("$[0]").value(provider.getId()));
        mvc.perform(get("/api/me/services").with(user("test_user5")))
                .andExpect(content().json("[]"));
    }
}
