package org.example.fjencinas.application;

import lombok.RequiredArgsConstructor;
import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.domain.repository.Create;
import org.example.fjencinas.domain.repository.Read;
import org.example.fjencinas.domain.usecase.ItemUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemUseCaseImpl implements ItemUseCase {

    private final Create create;
    private final Read read;

    @Override
    public Optional<Item> createItem(Item item) {
        return create.save(item);
    }

    @Override
    public Optional<Item> findItem(String id) {
        return read.find(id);
    }

    @Override
    public List<Item> findItem() {
        return read.find();
    }

}

