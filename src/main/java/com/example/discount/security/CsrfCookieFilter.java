package com.example.discount.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 응답에 CSRF 토큰 쿠키(XSRF-TOKEN)가 내려가도록 토큰을 미리 꺼냄.
 * 정적 HTML 페이지의 JavaScript가 이 쿠키를 읽어 POST/PUT 요청에 담아 보냄.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) token.getToken();
        chain.doFilter(request, response);
    }
}
