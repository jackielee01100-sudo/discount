package com.example.discount.web;

import com.example.discount.repository.DiscountEventRepository;
import com.example.discount.repository.EventPlaceRepository;
import com.example.discount.repository.MyServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class DiscountEventController {

    private final DiscountEventRepository discountEventRepository;
    private final EventPlaceRepository eventPlaceRepository;
    private final MyServiceRepository myServiceRepository;

    /**
     * 할인 행사 목록 + 행사별 브랜드 (달력/지도 화면에서 사용)
     * mine=true이고 로그인한 경우: 나의 정보에서 고른 서비스의 행사 + 서비스 지정이 없는 행사만 반환.
     * 로그인하지 않았거나 나의 정보를 하나도 고르지 않았으면 전체 반환.
     */
    @GetMapping
    public List<EventResponse> events(@RequestParam(defaultValue = "false") boolean mine, Authentication auth) {
        boolean loggedIn = auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated();
        Set<Long> myProviders = mine && loggedIn
                ? new HashSet<>(myServiceRepository.findProviderIds(auth.getName()))
                : Set.of();

        Map<Long, List<String>> brandsByEvent = new HashMap<>();
        for (Object[] row : eventPlaceRepository.findEventBrands()) {
            brandsByEvent.computeIfAbsent((Long) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        return discountEventRepository.findAllWithProvider().stream()
                .filter(e -> myProviders.isEmpty() || e.getProvider() == null
                        || myProviders.contains(e.getProvider().getId()))
                .map(e -> EventResponse.of(e, brandsByEvent.getOrDefault(e.getId(), List.of())))
                .toList();
    }
}
