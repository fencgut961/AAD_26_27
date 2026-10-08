package org.example.fjencinas.rest.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.rest.openapi.model.ItemRequest;
import org.example.fjencinas.rest.openapi.model.ItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "data", source = "data", qualifiedByName = "mapToJsonNode")
    Item toDomain(ItemRequest itemRequest);

    @Mapping(target = "data", source = "data", qualifiedByName = "jsonNodeToMap")
    ItemResponse fromDomain(Item item);

    @Named("mapToJsonNode")
    default JsonNode mapToJsonNode(Map<String, Object> value) {
        try {
            return new ObjectMapper().valueToTree(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("jsonNodeToMap")
    default Map<String, Object> jsonNodeToMap(JsonNode node) {
        try {
            return new ObjectMapper().convertValue(node, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}