package org.example.fjencinas.h2.adapter;

import lombok.RequiredArgsConstructor;
import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.domain.repository.Create;
import org.example.fjencinas.h2.mapper.ItemH2Mapper;
import org.example.fjencinas.h2.repository.ItemH2Repository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("itemH2CreateAdapter")
@RequiredArgsConstructor
public class ItemH2CreateAdapter implements Create {

    private final ItemH2Repository itemH2Repository;

    private final ItemH2Mapper itemH2Mapper;

    @Override
    public Optional<Item> save(Item item) {
        return Optional.ofNullable(itemH2Mapper.toDomain(itemH2Repository.save(itemH2Mapper.fromDomain(item))));
    }
}
