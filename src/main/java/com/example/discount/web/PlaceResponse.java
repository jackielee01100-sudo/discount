package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.EventPlace;

import java.time.LocalDate;

/** 지도 화면에 내려주는 매장 정보 (행사 정보 포함) */
public record PlaceResponse(
        Long id,
        String brand,
        String name,
        String address,
        double latitude,
        double longitude,
        String benefit,
        Long eventId,
        String eventName,
        String color,
        LocalDate startDate,
        LocalDate endDate
) {
    public static PlaceResponse from(EventPlace p) {
        DiscountEvent e = p.getEvent();
        return new PlaceResponse(p.getId(), p.getBrand(), p.getName(), p.getAddress(), p.getLatitude(), p.getLongitude(),
                p.getBenefit(), e.getId(), e.getName(), e.getColor(), e.getStartDate(), e.getEndDate());
    }
}
