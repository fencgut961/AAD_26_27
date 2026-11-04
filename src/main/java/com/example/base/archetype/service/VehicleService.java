package com.example.base.archetype.service;

import com.example.base.archetype.model.Vehicle;
import com.example.base.archetype.repository.CsvCrudFileRepository;
import com.example.base.archetype.util.FileExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final CsvCrudFileRepository csvFileRepository;
    private final CryptoService cryptoService;

    private static String removeFirstLine(String content) {
        int newlineIndex = content.indexOf('\n');
        return newlineIndex < 0 ? "" : content.substring(newlineIndex + 1);
    }

    public List<Vehicle> readVehicles(Path inputFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(inputFile);
        String content;
        if (FileExtension.ENCRYPTED.matches(inputFile)) {
            content = cryptoService.decrypt(fileContent);
        } else {
            content = new String(fileContent, StandardCharsets.UTF_8);
        }
        return csvFileRepository.read(removeFirstLine(content));
    }
}
