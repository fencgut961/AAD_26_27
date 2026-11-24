package com.example.postgresql.repository;

import java.util.List;

public interface CrudRepository<T, ID> {

    T create(T entity);

    T findById(ID id);

    List<T> findAll();

    boolean update(T entity);

    boolean delete(ID id);
}
