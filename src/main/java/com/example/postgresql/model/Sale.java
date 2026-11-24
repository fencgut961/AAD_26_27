package com.example.postgresql.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record Sale(Long id, Long storeId, LocalDateTime saleDate, BigDecimal total) {
}
