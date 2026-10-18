package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;

import java.time.LocalDate;
import java.util.List;

/** 할인 행사 + 하위 선택지(브랜드) */
public record EventResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        String description,
        List<String> brands
) {
    public static EventResponse of(DiscountEvent e, List<String> brands) {
        return new EventResponse(e.getId(), e.getName(), e.getStartDate(), e.getEndDate(),
                e.getColor(), e.getDescription(), brands);
    }
}
