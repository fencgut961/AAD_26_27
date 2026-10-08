package org.example.fjencinas.h2.mapper;

import org.example.fjencinas.domain.entity.Item;
import org.example.fjencinas.h2.entity.ItemH2Entity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemH2Mapper {

    @Mapping(target = "data", expression = "java(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.textNode(itemH2Entity.getDescription()))")
    Item toDomain(ItemH2Entity itemH2Entity);

    @Mapping(target = "description", expression = "java(item.getData() != null ? item.getData().toString() : null)")
    ItemH2Entity fromDomain(Item item);

}
