package com.example.discount.repository;

import com.example.discount.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** 실제 PostgreSQL에 저장/조회. 테스트 후 롤백되어 데이터는 남지 않음 */
@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void saveAndFind() {
        Product saved = productRepository.save(
                Product.builder().name("무선 이어폰").category("전자기기").price(129_000).build());

        assertNotNull(saved.getId());

        Product found = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals("무선 이어폰", found.getName());
        assertEquals("전자기기", found.getCategory());
        assertEquals(129_000, found.getPrice());
    }
}
