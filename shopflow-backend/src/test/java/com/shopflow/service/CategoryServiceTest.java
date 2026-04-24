package com.shopflow.service;

import com.shopflow.dto.CategoryDTO;
import com.shopflow.entity.Category;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CategoryMapper;
import com.shopflow.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    private Category cat;
    private CategoryDTO dto;

    @BeforeEach
    void setUp() {
        cat = Category.builder().id(1L).nom("Tech").description("Tech items").build();
        dto = CategoryDTO.builder().id(1L).nom("Tech").description("Tech items").build();
    }

    @Test
    void getTree_returnsRoots() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(cat));
        when(categoryMapper.toDto(cat)).thenReturn(dto);

        List<CategoryDTO> tree = categoryService.getTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getNom()).isEqualTo("Tech");
    }

    @Test
    void getAll_returnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        when(categoryMapper.toDtoList(List.of(cat))).thenReturn(List.of(dto));

        assertThat(categoryService.getAll()).hasSize(1);
    }

    @Test
    void getById_found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryMapper.toDto(cat)).thenReturn(dto);

        assertThat(categoryService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_noParent_success() {
        CategoryDTO input = CategoryDTO.builder().nom("Books").description("Books").build();
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(dto);

        CategoryDTO result = categoryService.create(input);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_withParent_linksIt() {
        Category parent = Category.builder().id(2L).nom("Parent").build();
        CategoryDTO input = CategoryDTO.builder().nom("Child").parentId(2L).build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(dto);

        categoryService.create(input);

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_parentNotFound_throws() {
        CategoryDTO input = CategoryDTO.builder().nom("Child").parentId(99L).build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.create(input))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_patchesFields() {
        CategoryDTO input = CategoryDTO.builder().nom("NewName").description("NewDesc").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(cat)).thenReturn(cat);
        when(categoryMapper.toDto(cat)).thenReturn(dto);

        categoryService.update(1L, input);

        assertThat(cat.getNom()).isEqualTo("NewName");
        assertThat(cat.getDescription()).isEqualTo("NewDesc");
    }

    @Test
    void update_withParentId_updatesParent() {
        Category parent = Category.builder().id(5L).nom("Parent").build();
        CategoryDTO input = CategoryDTO.builder().parentId(5L).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(cat)).thenReturn(cat);
        when(categoryMapper.toDto(cat)).thenReturn(dto);

        categoryService.update(1L, input);

        assertThat(cat.getParent()).isEqualTo(parent);
    }

    @Test
    void update_notFound_throws() {
        when(categoryRepository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.update(77L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existing_removes() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        categoryService.delete(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(categoryRepository.existsById(88L)).thenReturn(false);
        assertThatThrownBy(() -> categoryService.delete(88L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(categoryRepository, never()).deleteById(any());
    }
}
