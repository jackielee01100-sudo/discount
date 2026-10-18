package com.example.discount.web;

import com.example.discount.domain.AppUser;
import com.example.discount.domain.Role;
import com.example.discount.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public record SignupRequest(String username, String password) {}

    public record MeResponse(boolean loggedIn, String username, boolean admin) {}

    /** 회원가입: 항상 일반사용자(USER)로 가입. 관리자는 DB에서 role을 ADMIN으로 변경 */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest req) {
        String username = req.username() == null ? "" : req.username().trim();
        String password = req.password() == null ? "" : req.password();

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {
            return error(HttpStatus.BAD_REQUEST, "아이디는 영문/숫자/밑줄 3~30자로 입력하세요.");
        }
        if (password.length() < 8) {
            return error(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상으로 입력하세요.");
        }
        if (appUserRepository.existsByUsername(username)) {
            return error(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        appUserRepository.save(AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", username));
    }

    /** 현재 로그인 상태 (상단 메뉴에서 사용) */
    @GetMapping("/auth/me")
    public MeResponse me(Authentication auth) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return new MeResponse(false, null, false);
        }
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new MeResponse(true, auth.getName(), admin);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
