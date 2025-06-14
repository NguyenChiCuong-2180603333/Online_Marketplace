package com.marketplace.repository;

import com.marketplace.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByActiveTrue();
    Optional<Category> findByNameAndActiveTrue(String name);
    boolean existsByName(String name);
    List<Category> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}