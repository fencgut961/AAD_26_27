package com.example.postgresql.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Product(Long id, String sku, String name, BigDecimal price) {
}
