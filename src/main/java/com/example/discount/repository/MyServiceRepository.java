package com.example.discount.repository;

import com.example.discount.domain.MyService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyServiceRepository extends JpaRepository<MyService, Long> {

    @Query("select m.provider.id from MyService m where m.user.username = :username")
    List<Long> findProviderIds(@Param("username") String username);

    @Modifying
    @Query("delete from MyService m where m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
