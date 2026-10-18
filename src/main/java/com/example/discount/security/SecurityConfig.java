package com.example.discount.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 관리자 전용
                        .requestMatchers("/admin.html", "/api/admin/**").hasRole("ADMIN")
                        // 로그인 필요
                        .requestMatchers("/me.html", "/api/me/**").authenticated()
                        // 누구나 (달력·지도·로그인·회원가입)
                        .requestMatchers("/", "/index.html", "/map.html", "/login.html", "/signup.html",
                                "/js/**", "/favicon.ico", "/error",
                                "/api/events", "/api/places", "/api/providers",
                                "/api/auth/me", "/api/signup").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html")          // 로그인 화면
                        .loginProcessingUrl("/login")      // 로그인 폼 전송 주소 (POST)
                        .defaultSuccessUrl("/", false)     // 로그인 전 가려던 페이지가 있으면 그쪽으로
                        .failureUrl("/login.html?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/"))
                // CSRF 토큰을 쿠키(XSRF-TOKEN)로 내려주고, 요청 헤더(X-XSRF-TOKEN)나 폼 값(_csrf)으로 받음
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                // 로그인이 필요할 때: API는 401 응답, 화면은 로그인 페이지로 이동
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login.html"),
                                AnyRequestMatcher.INSTANCE));
        return http.build();
    }
}
