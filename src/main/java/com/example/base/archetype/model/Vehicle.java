package com.example.base.archetype.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    private String matricula;
    private String marcaModelo;
    private double velocidadMedida;
    private int limiteVia;
    private BigDecimal sancionEuros;
}
