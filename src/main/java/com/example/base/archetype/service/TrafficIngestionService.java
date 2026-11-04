package com.example.base.archetype.service;

import com.example.base.archetype.model.Vehicle;
import com.example.base.archetype.util.FileExtension;
import com.example.base.archetype.util.TrafficDataPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrafficIngestionService {

    private static final DateTimeFormatter CORRUPT_FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private final TrafficDataPaths trafficDataPaths;
    private final VehicleService vehicleService;
    private final PayloadService payloadService;

    public void createOutputDirectories() throws IOException {
        Files.createDirectories(trafficDataPaths.processedDirectory());
        Files.createDirectories(trafficDataPaths.errorDirectory());
        Files.createDirectories(trafficDataPaths.payloadDirectory());
    }

    public void processTrafficData() throws IOException {
        Path inputDirectory = trafficDataPaths.inputDirectory();
        List<Vehicle> vehicles = new ArrayList<>();

        try (Stream<Path> inputFiles = Files.list(inputDirectory)) {
            for (Path inputFile : inputFiles.filter(Files::isRegularFile).sorted().toList()) {
                boolean corrupt = isolateIfCorrupt(inputFile);
                if (!corrupt && (FileExtension.CSV.matches(inputFile) || FileExtension.ENCRYPTED.matches(inputFile))) {
                    vehicles.addAll(vehicleService.readVehicles(inputFile));
                    moveToProcessed(inputFile);
                }
            }
        }

        log.info("Loaded {} vehicles from radar files", vehicles.size());
        payloadService.writeDispatchPayload(payloadService.generateDispatchPayload(vehicles));
    }

    private boolean isolateIfCorrupt(Path inputFile) throws IOException {
        if (Files.size(inputFile) == 0) {
            String originalFilename = inputFile.getFileName().toString();
            String timestamp = LocalDateTime.now().format(CORRUPT_FILE_TIMESTAMP);
            Path isolatedFile = trafficDataPaths.errorDirectory()
                    .resolve(appendTimestamp(originalFilename, timestamp));

            log.warn("[ALERTA DGT] Fichero corrupto detectado y aislado: {}", originalFilename);
            Files.move(inputFile, isolatedFile, StandardCopyOption.ATOMIC_MOVE);
            return true;
        }

        return false;
    }

    private void moveToProcessed(Path inputFile) throws IOException {
        String originalFilename = inputFile.getFileName().toString();
        String timestamp = LocalDateTime.now().format(CORRUPT_FILE_TIMESTAMP);
        Path processedFile = trafficDataPaths.processedDirectory()
                .resolve(appendTimestamp(originalFilename, timestamp));

        Files.move(inputFile, processedFile, StandardCopyOption.ATOMIC_MOVE);
        log.info("Processed file moved to {}", processedFile);
    }

    private String appendTimestamp(String filename, String timestamp) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0) {
            return filename + "-" + timestamp;
        }
        return filename.substring(0, extensionIndex) + "-" + timestamp + filename.substring(extensionIndex);
    }
}
