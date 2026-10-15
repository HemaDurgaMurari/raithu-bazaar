package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.model.Product;
import com.klu.raithubazaar.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping
    public List<Product> findAll() { return service.findAll(); }
    @GetMapping("/{id}")
    public Product findOne(@PathVariable Long id) { return service.findById(id); }
    @PostMapping
    public Product create(@RequestBody Product product) { return service.create(product); }
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) { return service.update(id, product); }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
