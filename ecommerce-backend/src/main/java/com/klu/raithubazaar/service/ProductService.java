package com.klu.raithubazaar.service;

import com.klu.raithubazaar.model.Product;
import com.klu.raithubazaar.repository.ProductRepository;
import com.klu.raithubazaar.repository.OrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private static final List<String> CATEGORIES = List.of("Vegetables", "Fruits", "Snacks", "Dairy");
    private final ProductRepository products;
    private final OrderItemRepository orderItems;
    public ProductService(ProductRepository products, OrderItemRepository orderItems) {
        this.products = products;
        this.orderItems = orderItems;
    }

    public List<Product> findAll() { return products.findAll(); }

    @Transactional
    public Product create(Product product) {
        validate(product);
        product.setStatus(product.getStock() > 0 ? "AVAILABLE" : "OUT_OF_STOCK");
        if (product.getRating() == null) product.setRating(4.8);
        return products.save(product);
    }

    @Transactional
    public Product update(Long id, Product input) {
        validate(input);
        Product current = findById(id);
        current.setProductName(input.getProductName().trim());
        current.setCategory(input.getCategory());
        current.setDescription(input.getDescription());
        current.setPrice(input.getPrice());
        current.setStock(input.getStock());
        current.setUnit(input.getUnit());
        current.setImageUrl(input.getImageUrl());
        current.setRating(input.getRating() == null ? current.getRating() : input.getRating());
        current.setStatus(input.getStock() > 0 ? "AVAILABLE" : "OUT_OF_STOCK");
        return products.save(current);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        if (orderItems.existsByProductProductId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This product is part of order history. Set its stock to zero instead of deleting it.");
        }
        products.delete(product);
    }

    public Product findById(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void validate(Product p) {
        if (p.getProductName() == null || p.getProductName().isBlank()
                || p.getProductName().length() > 255
                || p.getCategory() == null || !CATEGORIES.contains(p.getCategory())
                || p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0
                || p.getStock() == null || p.getStock() < 0
                || p.getUnit() == null || p.getUnit().isBlank() || p.getUnit().length() > 255
                || (p.getDescription() != null && p.getDescription().length() > 1000)
                || (p.getImageUrl() != null && p.getImageUrl().length() > 2048)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a name (up to 255 characters), supported category, positive price, unit (up to 255 characters), and non-negative stock. Description must be at most 1000 characters and image URL at most 2048 characters.");
        }
    }
}
