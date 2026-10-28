package com.example.base.archetype.repository;

import org.springframework.stereotype.Repository;

@Repository
public class TxtReader implements CrudRepository<String> {


    @Override
    public String create() {
        return "";
    }

    @Override
    public String read(String content) {
        return "";
    }

    @Override
    public String update(String content) {
        // Implementación de la actualización del archivo CSV
        return null;
    }

    @Override
    public String delete(String content) {
        return "";
    }
}
