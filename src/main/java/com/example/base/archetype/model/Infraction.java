package com.example.base.archetype.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Infraction {

    private String plate;
    private String model;
    private double speed;
    private BigDecimal fine;
}
