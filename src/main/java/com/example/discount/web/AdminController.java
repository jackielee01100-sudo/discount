package com.example.discount.web;

import com.example.discount.domain.DiscountEvent;
import com.example.discount.domain.EventPlace;
import com.example.discount.repository.DiscountEventRepository;
import com.example.discount.repository.EventPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 관리자 전용 API (SecurityConfig에서 /api/admin/** 는 ADMIN만 허용) */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DiscountEventRepository discountEventRepository;
    private final EventPlaceRepository eventPlaceRepository;

    public record EventSummary(Long id, String name, String providerName,
                               LocalDate startDate, LocalDate endDate, String color, long placeCount) {}

    public record PlaceItem(String brand, String name, String address, String benefit) {}

    /** 이미지 카드에 들어갈 행사 상세 */
    public record EventCard(Long id, String name, String providerName, LocalDate startDate, LocalDate endDate,
                            String color, String description, List<PlaceItem> places) {}

    /** 전체 행사 목록 (나의 정보 필터 없이 전부) */
    @GetMapping("/events")
    public List<EventSummary> events() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : eventPlaceRepository.countByEvent()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return discountEventRepository.findAllWithProvider().stream()
                .map(e -> new EventSummary(e.getId(), e.getName(), providerName(e),
                        e.getStartDate(), e.getEndDate(), e.getColor(), counts.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    /** 행사 1개의 카드 데이터 (서비스 + 전체 매장) */
    @GetMapping("/events/{id}/card")
    public EventCard card(@PathVariable Long id) {
        DiscountEvent e = discountEventRepository.findAllWithProvider().stream()
                .filter(ev -> ev.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "행사를 찾을 수 없습니다: " + id));
        // 브랜드 가나다순 → 매장명 가나다순, 브랜드 없는 매장은 맨 뒤
        List<PlaceItem> places = eventPlaceRepository.findByEventId(id).stream()
                .map(AdminController::toItem)
                .sorted(Comparator.comparing(PlaceItem::brand, Comparator.nullsLast(Comparator.<String>naturalOrder()))
                        .thenComparing(PlaceItem::name))
                .toList();
        return new EventCard(e.getId(), e.getName(), providerName(e), e.getStartDate(), e.getEndDate(),
                e.getColor(), e.getDescription(), places);
    }

    private static PlaceItem toItem(EventPlace p) {
        return new PlaceItem(p.getBrand(), p.getName(), p.getAddress(), p.getBenefit());
    }

    private static String providerName(DiscountEvent e) {
        return e.getProvider() == null ? null : e.getProvider().getName();
    }
}
