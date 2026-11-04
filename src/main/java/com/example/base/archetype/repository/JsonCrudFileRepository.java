package com.example.base.archetype.repository;

import com.example.base.archetype.model.Payload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Repository
@RequiredArgsConstructor
public class JsonCrudFileRepository implements CrudFileRepository<Path, Payload> {

    private final ObjectMapper objectMapper;

    @Override
    public void create(Path file, Payload entity) throws IOException {
        try (OutputStream output = Files.newOutputStream(file,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(output, entity);
        }
    }

    @Override
    public Payload read(Path file) {
        return objectMapper.readValue(file.toFile(), Payload.class);
    }

    @Override
    public void update(Path file, Payload entity) throws IOException {
        try (OutputStream output = Files.newOutputStream(file,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(output, entity);
        }
    }

    @Override
    public void delete(Path file) throws IOException {
        Files.deleteIfExists(file);
    }
}
