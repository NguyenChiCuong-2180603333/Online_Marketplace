package com.marketplace.service;

import com.marketplace.model.Category;
import com.marketplace.repository.CategoryRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + category.getName());
        }

        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category categoryDetails) {
        Category category = getCategoryById(id);

        // Check if name already exists (exclude current category)
        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.existsByName(categoryDetails.getName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + categoryDetails.getName());
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIcon(categoryDetails.getIcon());

        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        Category category = getCategoryById(id);

        // Check if category has products
        long productCount = productRepository.countByCategoryAndActiveTrue(category.getName());
        if (productCount > 0) {
            throw new BadRequestException("Không thể xóa danh mục vì có " + productCount + " sản phẩm đang sử dụng");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    public void updateProductCount(String categoryName) {
        Category category = categoryRepository.findByNameAndActiveTrue(categoryName)
                .orElse(null);

        if (category != null) {
            long productCount = productRepository.countByCategoryAndActiveTrue(categoryName);
            category.setProductCount((int) productCount);
            categoryRepository.save(category);
        }
    }

    public List<Category> searchCategories(String searchTerm) {
        return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm);
    }
}