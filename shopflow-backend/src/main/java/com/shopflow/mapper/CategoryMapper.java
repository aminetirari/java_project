package com.shopflow.mapper;

import com.shopflow.dto.CategoryDTO;
import com.shopflow.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDTO toDto(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(category.getId())
                .nom(category.getNom())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .sousCategories(category.getSousCategories() == null ? List.of()
                        : category.getSousCategories().stream().map(this::toDto).collect(Collectors.toList()))
                .build();
    }

    public List<CategoryDTO> toDtoList(List<Category> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}
