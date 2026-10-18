package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** 할인 행사 (예: T데이). 달력에 기간을 표시하는 데 사용 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 행사명 */
    @Column(nullable = false)
    private String name;

    /** 시작일 (포함) */
    @Column(nullable = false)
    private LocalDate startDate;

    /** 종료일 (포함) */
    @Column(nullable = false)
    private LocalDate endDate;

    /** 달력 표시 색상 (예: #e5004f) */
    @Column(nullable = false)
    private String color;

    /** 행사 설명 */
    private String description;
}
