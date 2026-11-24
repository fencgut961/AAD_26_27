package com.example.postgresql.service;

import com.example.postgresql.model.Product;
import com.example.postgresql.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.create(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
