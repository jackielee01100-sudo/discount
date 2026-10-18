package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 상품명 */
    @Column(nullable = false)
    private String name;

    /** 카테고리 (할인 대상 지정에 사용) */
    @Column(nullable = false)
    private String category;

    /** 단가 (원) */
    @Column(nullable = false)
    private long price;
}
