package com.example.postgresql.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SaleDetail(Long id, Long saleId, Long productId, int quantity, BigDecimal unitPrice) {

}
