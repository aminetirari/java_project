package com.shopflow.service;

import com.shopflow.dto.CategoryDTO;
import com.shopflow.entity.Category;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CategoryMapper;
import com.shopflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getTree() {
        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAll() {
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable")));
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente introuvable"));
        }
        Category category = Category.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .parent(parent)
                .build();
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
        if (dto.getNom() != null) category.setNom(dto.getNom());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getParentId() != null) {
            category.setParent(categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente introuvable")));
        }
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Catégorie introuvable");
        }
        categoryRepository.deleteById(id);
    }
}
