package com.example.discount.domain;

import jakarta.persistence.*;
import lombok.*;

/** 할인 행사가 적용되는 매장(장소). 지도에 마커로 표시 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 행사의 매장인지 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DiscountEvent event;

    /** 브랜드 (예: 던킨도너츠). 행사 체크 시 하위 선택지로 사용, 없으면 null */
    private String brand;

    /** 매장명 */
    @Column(nullable = false)
    private String name;

    /** 주소 */
    private String address;

    /** 위도 */
    @Column(nullable = false)
    private double latitude;

    /** 경도 */
    @Column(nullable = false)
    private double longitude;

    /** 할인 내용 (예: 아메리카노 1+1) */
    private String benefit;
}
