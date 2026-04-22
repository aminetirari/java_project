package com.shopflow.mapper;

import com.shopflow.dto.ProductCreateDTO;
import com.shopflow.dto.ProductDTO;
import com.shopflow.entity.Category;
import com.shopflow.entity.Product;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProductVariantMapper.class})
public interface ProductMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.nomBoutique", target = "sellerNom")
    @Mapping(target = "categoryIds", expression = "java(mapCategoriesToIds(product.getCategories()))")
    @Mapping(target = "noteMoyenne", ignore = true)
    @Mapping(target = "nbAvis", ignore = true)
    ProductDTO toDto(Product product);

    List<ProductDTO> toDtoList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", constant = "true")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "seller", ignore = true) // To be set in Service
    @Mapping(target = "categories", ignore = true) // To be set in Service
    Product toEntity(ProductCreateDTO createDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateEntityFromDto(ProductCreateDTO createDTO, @MappingTarget Product product);

    default List<Long> mapCategoriesToIds(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream().map(Category::getId).collect(Collectors.toList());
    }
}
