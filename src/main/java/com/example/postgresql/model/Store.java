package com.example.postgresql.model;

import lombok.Builder;

@Builder
public record Store(Long id, String code, String name, String country, String region) {
}
