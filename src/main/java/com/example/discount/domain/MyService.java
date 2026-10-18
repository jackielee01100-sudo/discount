package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

/** 나의 정보: 내가 사용하는 서비스 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true)
    private ServiceProvider provider;

    public MyService(ServiceProvider provider) {
        this.provider = provider;
    }
}
