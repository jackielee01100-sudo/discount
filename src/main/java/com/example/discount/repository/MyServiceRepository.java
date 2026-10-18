package com.example.discount.repository;

import com.example.discount.domain.MyService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MyServiceRepository extends JpaRepository<MyService, Long> {

    @Query("select m.provider.id from MyService m")
    List<Long> findProviderIds();
}
