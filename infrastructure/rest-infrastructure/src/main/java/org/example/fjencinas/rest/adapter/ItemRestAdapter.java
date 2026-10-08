package org.example.fjencinas.rest.adapter;

import lombok.RequiredArgsConstructor;
import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.domain.usecase.ItemUseCase;
import org.example.fjencinas.rest.mapper.ItemMapper;
import org.example.fjencinas.rest.openapi.api.ItemsApi;
import org.example.fjencinas.rest.openapi.model.ItemRequest;
import org.example.fjencinas.rest.openapi.model.ItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ItemRestAdapter implements ItemsApi {

    private final ItemUseCase itemUseCase;

    private final ItemMapper itemMapper;

    @Override
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        List<Item> items = itemUseCase.findItem();
        if (CollectionUtils.isEmpty(items)) {
            return ResponseEntity.noContent().build();
        }
        List<ItemResponse> response = items.stream()
                .map(itemMapper::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }


//    curl -X POST "http://localhost:8080/items" \
//            -H "Content-Type: application/json" \
//            -d '{
//            "name": "Ejemplo",
//            "descripcion": "Un item de prueba",
//            "data": {
//                "clave1": "valor1",
//                "clave2": 123
//        }
//    }'

    @Override
    public ResponseEntity<ItemResponse> saveItem(ItemRequest itemRequest) {
        Item item = itemMapper.toDomain(itemRequest);
        Optional<Item> savedItem = itemUseCase.createItem(item);
        if (savedItem.isEmpty()) {
            return ResponseEntity.badRequest().build();
        } else {
            ItemResponse response = itemMapper.fromDomain(savedItem.get());
            return ResponseEntity.ok(response);
        }
    }
}
