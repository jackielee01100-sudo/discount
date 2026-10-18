package com.example.discount.repository;

import com.example.discount.domain.DiscountEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountEventRepository extends JpaRepository<DiscountEvent, Long> {

    List<DiscountEvent> findAllByOrderByStartDateAsc();

    Optional<DiscountEvent> findByName(String name);
}
