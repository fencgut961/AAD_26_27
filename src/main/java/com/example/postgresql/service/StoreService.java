package com.example.postgresql.service;

import com.example.postgresql.model.Store;
import com.example.postgresql.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public Store createStore(Store store) {
        return storeRepository.create(store);
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id);
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public boolean updateStore(Store store) {
        return storeRepository.update(store);
    }
}
