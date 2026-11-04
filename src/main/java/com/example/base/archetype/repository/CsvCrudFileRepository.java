package com.example.base.archetype.repository;

import com.example.base.archetype.model.Vehicle;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CsvCrudFileRepository implements CrudFileRepository<String, List<Vehicle>> {

    private static final int EXPECTED_FIELD_COUNT = 5;

    @Override
    public List<Vehicle> read(String content) throws IOException {
        List<Vehicle> vehicles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    List<String> fields = parseCsvLine(line, lineNumber);
                    if (fields.size() != EXPECTED_FIELD_COUNT) {
                        throw new IOException("Expected " + EXPECTED_FIELD_COUNT
                                + " fields at line " + lineNumber);
                    }
                    try {
                        vehicles.add(new Vehicle(
                                fields.get(0),
                                fields.get(1),
                                Double.parseDouble(fields.get(2)),
                                Integer.parseInt(fields.get(3)),
                                new BigDecimal(fields.get(4))
                        ));
                    } catch (NumberFormatException exception) {
                        throw new IOException("Invalid numeric value at line " + lineNumber, exception);
                    }
                }
            }
        }
        return vehicles;
    }

    private List<String> parseCsvLine(String line, int lineNumber) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }

        if (quoted) {
            throw new IOException("Unclosed quoted field at line " + lineNumber);
        }
        fields.add(field.toString().trim());
        return fields;
    }
}
