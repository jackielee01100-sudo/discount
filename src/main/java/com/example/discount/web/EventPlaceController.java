package com.example.discount.web;

import com.example.discount.repository.EventPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class EventPlaceController {

    private final EventPlaceRepository eventPlaceRepository;

    /**
     * 지도에 표시할 매장 조회 (해당 날짜에 진행 중인 행사의 매장)
     * 예) /api/places?date=2026-10-20&eventIds=1,2&brands=던킨도너츠,파리바게뜨
     * brands를 주면 브랜드가 있는 매장은 그 브랜드만 남김 (브랜드 없는 매장은 그대로)
     */
    @GetMapping
    public List<PlaceResponse> places(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> eventIds,
            @RequestParam(required = false) List<String> brands) {
        if (eventIds == null || eventIds.isEmpty()) return List.of();
        return eventPlaceRepository.search(eventIds, date, date).stream()
                .filter(p -> brands == null || p.getBrand() == null || brands.contains(p.getBrand()))
                .map(PlaceResponse::from)
                .toList();
    }
}
