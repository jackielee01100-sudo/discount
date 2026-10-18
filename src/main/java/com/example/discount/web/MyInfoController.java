package com.example.discount.web;

import com.example.discount.domain.MyService;
import com.example.discount.domain.ServiceProvider;
import com.example.discount.repository.MyServiceRepository;
import com.example.discount.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyInfoController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final MyServiceRepository myServiceRepository;

    /** 선택 가능한 서비스 목록 (service_provider 테이블) */
    @GetMapping("/providers")
    public List<ServiceProvider> providers() {
        return serviceProviderRepository.findAllByOrderByCategoryAscIdAsc();
    }

    /** 내가 고른 서비스 id 목록 */
    @GetMapping("/me/services")
    public List<Long> myServices() {
        return myServiceRepository.findProviderIds();
    }

    /** 내가 고른 서비스 저장 (보낸 목록으로 통째로 교체). 예) PUT /api/me/services  body: [1, 3] */
    @PutMapping("/me/services")
    @Transactional
    public List<Long> saveMyServices(@RequestBody List<Long> providerIds) {
        myServiceRepository.deleteAllInBatch();
        myServiceRepository.flush();
        myServiceRepository.saveAll(serviceProviderRepository.findAllById(providerIds).stream()
                .map(MyService::new)
                .toList());
        return myServiceRepository.findProviderIds();
    }
}
