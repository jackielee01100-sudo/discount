package com.example.discount.web;

import com.example.discount.repository.DiscountEventRepository;
import com.example.discount.repository.EventPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class DiscountEventController {

    private final DiscountEventRepository discountEventRepository;
    private final EventPlaceRepository eventPlaceRepository;

    /** 할인 행사 목록 + 행사별 브랜드 (달력/지도 화면에서 사용) */
    @GetMapping
    public List<EventResponse> events() {
        Map<Long, List<String>> brandsByEvent = new HashMap<>();
        for (Object[] row : eventPlaceRepository.findEventBrands()) {
            brandsByEvent.computeIfAbsent((Long) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        return discountEventRepository.findAllByOrderByStartDateAsc().stream()
                .map(e -> EventResponse.of(e, brandsByEvent.getOrDefault(e.getId(), List.of())))
                .toList();
    }
}
