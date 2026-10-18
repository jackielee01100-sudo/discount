package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

/** 사용 중인 서비스 (예: 통신사 SKT/KT/LG U+). DB에 직접 등록 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 분류 (예: 통신사, 카드사) - 나의 정보 화면에서 그룹으로 묶어 보여줌 */
    @Column(nullable = false)
    private String category;

    /** 서비스명 (예: SKT) */
    @Column(nullable = false, unique = true)
    private String name;
}
