package com.example.discount.repository;

import com.example.discount.domain.EventPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface EventPlaceRepository extends JpaRepository<EventPlace, Long> {

    /** 선택한 행사 중, 기간이 [from, to]와 하루라도 겹치는 행사의 매장 */
    @Query("""
            select p from EventPlace p join fetch p.event e
            where e.id in :eventIds
              and e.startDate <= :to
              and e.endDate >= :from
            order by e.startDate, p.name
            """)
    List<EventPlace> search(@Param("eventIds") Collection<Long> eventIds,
                            @Param("from") LocalDate from,
                            @Param("to") LocalDate to);

    /** 행사별 브랜드 목록 (중복 제거, 이름순) */
    @Query("""
            select distinct p.event.id, p.brand from EventPlace p
            where p.brand is not null
            order by p.brand
            """)
    List<Object[]> findEventBrands();
}
