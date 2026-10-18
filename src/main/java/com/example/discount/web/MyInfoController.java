package com.example.discount.web;

import com.example.discount.domain.AppUser;
import com.example.discount.domain.MyService;
import com.example.discount.domain.ServiceProvider;
import com.example.discount.repository.AppUserRepository;
import com.example.discount.repository.MyServiceRepository;
import com.example.discount.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyInfoController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final MyServiceRepository myServiceRepository;
    private final AppUserRepository appUserRepository;

    /** 선택 가능한 서비스 목록 (service_provider 테이블) */
    @GetMapping("/providers")
    public List<ServiceProvider> providers() {
        return serviceProviderRepository.findAllByOrderByCategoryAscIdAsc();
    }

    /** 로그인한 사용자가 고른 서비스 id 목록 */
    @GetMapping("/me/services")
    public List<Long> myServices(Authentication auth) {
        return myServiceRepository.findProviderIds(auth.getName());
    }

    /** 로그인한 사용자의 서비스 저장 (보낸 목록으로 통째로 교체). 예) PUT /api/me/services  body: [1, 3] */
    @PutMapping("/me/services")
    @Transactional
    public List<Long> saveMyServices(Authentication auth, @RequestBody List<Long> providerIds) {
        AppUser user = appUserRepository.findByUsername(auth.getName()).orElseThrow();
        myServiceRepository.deleteByUserId(user.getId());
        myServiceRepository.saveAll(serviceProviderRepository.findAllById(providerIds).stream()
                .map(p -> new MyService(user, p))
                .toList());
        return myServiceRepository.findProviderIds(user.getUsername());
    }
}
