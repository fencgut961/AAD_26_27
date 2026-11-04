package com.example.base.archetype.service;

import com.example.base.archetype.model.Infraction;
import com.example.base.archetype.model.Payload;
import com.example.base.archetype.model.Vehicle;
import com.example.base.archetype.repository.JsonCrudFileRepository;
import com.example.base.archetype.util.TrafficDataPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayloadService {

    private static final String RADAR_ID = "RADAR-M30-KM12";
    private static final String PAYLOAD_STATUS = "READY_FOR_DISPATCH";
    private static final String PAYLOAD_FILENAME = "dgt_dispatch_payload";
    private static final DateTimeFormatter PAYLOAD_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final JsonCrudFileRepository jsonFileRepository;
    private final TrafficDataPaths trafficDataPaths;

    public Payload generateDispatchPayload(List<Vehicle> vehicles) {
        List<Infraction> infractions = new ArrayList<>();
        BigDecimal totalFines = BigDecimal.ZERO;
        double maxSpeed = 0;

        for (Vehicle vehicle : vehicles) {
            if (!Double.isFinite(vehicle.getVelocidadMedida())) {
                throw new IllegalArgumentException("Vehicle speed must be finite: " + vehicle.getMatricula());
            }
            maxSpeed = Math.max(maxSpeed, vehicle.getVelocidadMedida());
            BigDecimal fine = vehicle.getSancionEuros();
            if (fine != null && fine.signum() > 0) {
                infractions.add(new Infraction(
                        vehicle.getMatricula(),
                        vehicle.getMarcaModelo(),
                        vehicle.getVelocidadMedida(),
                        fine
                ));
                totalFines = totalFines.add(fine);
            }
        }

        Payload payload = new Payload(
                RADAR_ID,
                PAYLOAD_STATUS,
                vehicles.size(),
                infractions.size(),
                maxSpeed,
                totalFines,
                List.copyOf(infractions)
        );
        log.info("Dispatch payload prepared for {} vehicles and {} infractions",
                payload.getTotalVehicles(), payload.getTotalInfractions());
        return payload;
    }

    public void writeDispatchPayload(Payload payload) throws IOException {
        String timestamp = LocalDateTime.now().format(PAYLOAD_TIMESTAMP_FORMAT);
        Path payloadFile = trafficDataPaths.payloadDirectory().resolve(PAYLOAD_FILENAME + "_" + timestamp + ".json");
        jsonFileRepository.create(payloadFile, payload);
        log.info("Dispatch payload written to {}", payloadFile);
    }
}
