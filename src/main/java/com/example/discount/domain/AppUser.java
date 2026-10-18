package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 로그인 계정.
 * 관리자 지정: update app_user set role = 'ADMIN' where username = '아이디';
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 아이디 */
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    /** BCrypt로 암호화된 비밀번호 (원문은 저장하지 않음) */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
