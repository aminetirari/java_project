package com.shopflow.mapper;

import com.shopflow.dto.OrderDTO;
import com.shopflow.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "statut", target = "status")
    @Mapping(source = "totalTTC", target = "total")
    @Mapping(source = "coupon.code", target = "codePromo")
    OrderDTO toDto(Order entity);

    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "status", target = "statut")
    @Mapping(source = "total", target = "totalTTC")
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "coupon", ignore = true)
    Order toEntity(OrderDTO dto);
}
