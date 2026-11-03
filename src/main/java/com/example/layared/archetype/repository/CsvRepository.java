package com.example.layared.archetype.repository;

import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public class CsvRepository implements CrudRepository<File> {


    @Override
    public File create() {
        return null;
    }

    @Override
    public File read(String content) {
        return null;
    }

    @Override
    public File update(String content) {
        // Implementación de la actualización del archivo CSV
        return null;
    }

    @Override
    public File delete(String content) {
        return null;
    }
}
