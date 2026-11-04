package com.example.base.archetype.repository;

import java.io.IOException;

public interface CrudFileRepository<SOURCE, RESULT> {

    RESULT read(SOURCE source) throws IOException;

    default void create(SOURCE source, RESULT result) throws IOException {
        throw new UnsupportedOperationException("Create is not supported");
    }

    default void update(SOURCE source, RESULT result) throws IOException {
        throw new UnsupportedOperationException("Update is not supported");
    }

    default void delete(SOURCE source) throws IOException {
        throw new UnsupportedOperationException("Delete is not supported");
    }
}
