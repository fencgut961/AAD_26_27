package com.fencgut.base.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class CloudBucketService {

    private final ObjectMapper objectMapper;

    /**
     * Escribe el resumen JSON preliminar en /output/bucket/aadtex-daily-reports/2026/09/
     */
    public void saveSummaryToBucket(BatchSummary summary, String fileName) throws Exception {
        Path bucketPath = Paths.get("c:/workspace/store_data/output/bucket/aadtex-daily-reports/2026/09/");

        // Crear la estructura de directorios si no existe
        if (!Files.exists(bucketPath)) {
            Files.createDirectories(bucketPath);
        }

        Path targetFile = bucketPath.resolve(fileName);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile.toFile(), summary);
    }
}