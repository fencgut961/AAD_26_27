package com.example.layared.archetype.repository;

import org.springframework.stereotype.Repository;

@Repository
public class TxtRepository implements CrudRepository<String> {


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
        return null;
    }

    @Override
    public String delete(String content) {
        return "";
    }
}
