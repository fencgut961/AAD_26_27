package org.example.fjencinas.h2.adapter;

import lombok.RequiredArgsConstructor;
import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.domain.repository.Read;
import org.example.fjencinas.h2.mapper.ItemH2Mapper;
import org.example.fjencinas.h2.repository.ItemH2Repository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemH2ReadAdapter implements Read {

    private final ItemH2Repository itemH2Repository;

    private final ItemH2Mapper itemH2Mapper;

    @Override
    public Optional<Item> find(String id) {
        return itemH2Repository.findById(Long.valueOf(id)).map(itemH2Mapper::toDomain);
    }

    @Override
    public List<Item> find() {
        return itemH2Repository.findAll().stream().map(itemH2Mapper::toDomain).toList();
    }

}
