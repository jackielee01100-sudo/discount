package com.example.discount.repository;

import com.example.discount.domain.DiscountEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiscountEventRepository extends JpaRepository<DiscountEvent, Long> {

    /** 전체 행사 (서비스 정보 포함, 시작일 순) */
    @Query("select e from DiscountEvent e left join fetch e.provider order by e.startDate, e.id")
    List<DiscountEvent> findAllWithProvider();
}
