package com.shopflow.mapper;

import com.shopflow.dto.OrderItemDTO;
import com.shopflow.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "variant.id", target = "variantId")
    OrderItemDTO toDto(OrderItem entity);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "variantId", target = "variant.id")
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDTO dto);
}
