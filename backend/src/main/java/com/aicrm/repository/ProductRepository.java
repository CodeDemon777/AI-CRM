package com.aicrm.repository;

import com.aicrm.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name, String category, String sku
    );

    List<Product> findByCategoryIgnoreCase(String category);
}
