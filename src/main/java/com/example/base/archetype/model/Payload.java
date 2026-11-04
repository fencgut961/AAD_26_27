package com.example.base.archetype.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload {

    private String radarId;
    private String status;
    private int totalVehicles;
    private int totalInfractions;
    private double maxSpeed;
    private BigDecimal totalFines;
    private List<Infraction> infractions;
}
