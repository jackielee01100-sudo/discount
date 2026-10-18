package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.ServiceProvider;

import java.time.LocalDate;
import java.util.List;

/** 할인 행사 + 서비스 + 하위 선택지(브랜드) */
public record EventResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        String description,
        Long providerId,
        String providerName,
        List<String> brands
) {
    public static EventResponse of(DiscountEvent e, List<String> brands) {
        ServiceProvider p = e.getProvider();
        return new EventResponse(e.getId(), e.getName(), e.getStartDate(), e.getEndDate(),
                e.getColor(), e.getDescription(),
                p == null ? null : p.getId(), p == null ? null : p.getName(),
                brands);
    }
}
