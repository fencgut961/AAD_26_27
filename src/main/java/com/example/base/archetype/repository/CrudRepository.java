package com.example.base.archetype.repository;

import java.io.File;

public interface CrudRepository<T> {

    public T create();

    public T read(String content);

    public T update(String content);

    public T delete(String content);
}
