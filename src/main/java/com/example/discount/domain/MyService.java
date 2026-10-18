package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

/** 나의 정보: 사용자별로 사용하는 서비스 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ServiceProvider provider;

    public MyService(AppUser user, ServiceProvider provider) {
        this.user = user;
        this.provider = provider;
    }
}
