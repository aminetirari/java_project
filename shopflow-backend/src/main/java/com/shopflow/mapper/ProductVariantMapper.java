package com.shopflow.mapper;

import com.shopflow.dto.ProductVariantCreateDTO;
import com.shopflow.dto.ProductVariantDTO;
import com.shopflow.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    ProductVariantDTO toDto(ProductVariant variant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant toEntity(ProductVariantCreateDTO createDTO);
}
