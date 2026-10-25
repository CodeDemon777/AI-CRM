package com.aicrm.service;

import com.aicrm.exception.ResourceNotFoundException;
import com.aicrm.model.Product;
import com.aicrm.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final MongoHealthService mongoHealthService;

    // In-memory fallback map initialized with demo data until MongoDB Atlas URI is active
    private final Map<String, Product> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public ProductService(ProductRepository productRepository, MongoHealthService mongoHealthService) {
        this.productRepository = productRepository;
        this.mongoHealthService = mongoHealthService;
        initFallbackData();
    }

    private void initFallbackData() {
        Product p1 = new Product(
                "p1",
                "Cloud CRM Enterprise",
                "PRD-CRM-01",
                "Software",
                45000.0,
                50,
                "Active",
                "Full-featured cloud CRM suite with multi-channel support."
        );
        Product p2 = new Product(
                "p2",
                "AI Sales Copilot Add-on",
                "PRD-AI-02",
                "AI Services",
                18000.0,
                100,
                "Active",
                "Predictive analytics & deal closing recommendation engine."
        );
        Product p3 = new Product(
                "p3",
                "Dedicated Onboarding & Training",
                "PRD-SRV-03",
                "Services",
                12000.0,
                20,
                "Active",
                "1-on-1 enterprise setup and sales rep onboarding workshop."
        );
        fallbackStore.put(p1.getId(), p1);
        fallbackStore.put(p2.getId(), p2);
        fallbackStore.put(p3.getId(), p3);
    }

    public List<Product> getAllProducts(String search, String category) {
        if (mongoHealthService.isConnected()) {
            try {
                if (search != null && !search.trim().isEmpty()) {
                    String query = search.trim();
                    return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrSkuContainingIgnoreCase(
                            query, query, query
                    );
                } else if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
                    return productRepository.findByCategoryIgnoreCase(category.trim());
                }
                return productRepository.findAll();
            } catch (Exception e) {
                log.warn("Failed fetching products from MongoDB Atlas, using fallback: {}", e.getMessage());
            }
        }

        // Fallback store
        List<Product> list = new ArrayList<>(fallbackStore.values());
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.trim().toLowerCase();
            return list.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lower)) ||
                                 (p.getCategory() != null && p.getCategory().toLowerCase().contains(lower)) ||
                                 (p.getSku() != null && p.getSku().toLowerCase().contains(lower)))
                    .toList();
        }
        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            String catLower = category.trim().toLowerCase();
            return list.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().toLowerCase().equals(catLower))
                    .toList();
        }
        return list;
    }

    public Optional<Product> getProductById(String id) {
        if (mongoHealthService.isConnected()) {
            try {
                return productRepository.findById(id);
            } catch (Exception e) {
                log.warn("MongoDB lookup failed for product id {}: {}", id, e.getMessage());
            }
        }
        return Optional.ofNullable(fallbackStore.get(id));
    }

    public Product createProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }

        product.setName(product.getName().trim());
        if (product.getSku() != null) {
            product.setSku(product.getSku().trim().toUpperCase());
        } else {
            product.setSku("PRD-" + System.currentTimeMillis() % 10000);
        }
        if (product.getCategory() != null) {
            product.setCategory(product.getCategory().trim());
        } else {
            product.setCategory("General");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            product.setStock(0);
        }
        if (product.getStatus() == null || product.getStatus().isBlank()) {
            product.setStatus(product.getStock() > 0 ? "Active" : "Out of Stock");
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return productRepository.save(product);
            } catch (Exception e) {
                log.warn("MongoDB save failed for product, saving to fallback store: {}", e.getMessage());
            }
        }

        if (product.getId() == null || product.getId().trim().isEmpty()) {
            product.setId(String.valueOf(System.currentTimeMillis()));
        }

        fallbackStore.put(product.getId(), product);
        return product;
    }

    public Product updateProduct(String id, Product updated) {
        Product existing = getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existing.setName(updated.getName().trim());
        if (updated.getSku() != null) {
            existing.setSku(updated.getSku().trim().toUpperCase());
        }
        if (updated.getCategory() != null) {
            existing.setCategory(updated.getCategory().trim());
        }
        if (updated.getPrice() != null) {
            existing.setPrice(updated.getPrice());
        }
        if (updated.getStock() != null) {
            existing.setStock(updated.getStock());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus().trim());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription().trim());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        if (mongoHealthService.isConnected()) {
            try {
                return productRepository.save(existing);
            } catch (Exception e) {
                log.warn("MongoDB update product failed: {}", e.getMessage());
            }
        }

        fallbackStore.put(id, existing);
        return existing;
    }

    public boolean deleteProduct(String id) {
        boolean removed = false;
        if (mongoHealthService.isConnected()) {
            try {
                if (productRepository.existsById(id)) {
                    productRepository.deleteById(id);
                    removed = true;
                }
            } catch (Exception e) {
                log.warn("MongoDB delete product failed: {}", e.getMessage());
            }
        }

        if (fallbackStore.remove(id) != null) {
            removed = true;
        }

        return removed;
    }

    public long countProducts() {
        if (mongoHealthService.isConnected()) {
            try {
                return productRepository.count();
            } catch (Exception e) {
                log.warn("MongoDB count failed for products: {}", e.getMessage());
            }
        }
        return fallbackStore.size();
    }
}
