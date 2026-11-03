package com.example.layared.archetype.repository;

public interface CrudRepository<T> {

    public T create();

    public T read(String content);

    public T update(String content);

    public T delete(String content);
}
