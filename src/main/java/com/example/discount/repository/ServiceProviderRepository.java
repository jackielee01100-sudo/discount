package com.example.discount.repository;

import com.example.discount.domain.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {

    List<ServiceProvider> findAllByOrderByCategoryAscIdAsc();
}
