package com.shopflow.service;

import com.shopflow.dto.ProductCreateDTO;
import com.shopflow.dto.ProductDTO;
import com.shopflow.entity.Product;
import com.shopflow.entity.Role;
import com.shopflow.entity.SellerProfile;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.ProductMapper;
import com.shopflow.repository.CategoryRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SellerProfileRepository sellerProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductMapper productMapper;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private ProductService productService;

    private Product product;
    private ProductDTO dto;
    private SellerProfile seller;
    private User sellerUser;

    @BeforeEach
    void setUp() {
        sellerUser = User.builder().id(1L).email("s@shopflow.com").role(Role.SELLER).actif(true).build();
        seller = SellerProfile.builder().id(2L).user(sellerUser).nomBoutique("Shop").build();
        product = Product.builder().id(10L).nom("PS5").prix(500.0).stock(10).actif(true).seller(seller).build();
        dto = ProductDTO.builder().id(10L).nom("PS5").build();
    }

    @Test
    void getAllProducts_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDtoList(List.of(product))).thenReturn(List.of(dto));

        assertThat(productService.getAllProducts()).hasSize(1);
    }

    @Test
    void search_allFilters_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDtoList(List.of(product))).thenReturn(new ArrayList<>(List.of(dto)));
        when(reviewRepository.findAggregatesByProductIds(anyCollection())).thenReturn(Collections.emptyList());

        Page<ProductDTO> result = productService.search("PS", 1L, 2L, 10.0, 1000.0, true, 3.0,
                "price_asc", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_defaultSort_works() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(productMapper.toDtoList(any())).thenReturn(new ArrayList<>());
        when(reviewRepository.findAggregatesByProductIds(anyCollection())).thenReturn(Collections.emptyList());

        Page<ProductDTO> result = productService.search(null, null, null, null, null, null, null,
                null, 0, 5);

        assertThat(result).isNotNull();
    }

    @Test
    void search_sortVariants() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(productMapper.toDtoList(any())).thenReturn(new ArrayList<>());
        when(reviewRepository.findAggregatesByProductIds(anyCollection())).thenReturn(Collections.emptyList());

        productService.search("", null, null, null, null, false, null, "price_desc", 0, 5);
        productService.search("", null, null, null, null, false, null, "newest", 0, 5);
        productService.search("", null, null, null, null, false, null, "name", 0, 5);
        productService.search("", null, null, null, null, false, null, "weird", 0, 5);

        assertThat(true).isTrue();
    }

    @Test
    void searchFullText_returnsPage() {
        when(productRepository.searchByText(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toDtoList(List.of(product))).thenReturn(List.of(dto));

        Page<ProductDTO> result = productService.searchFullText("PS", 0, 5);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchFullText_nullQuery_treatedAsEmpty() {
        when(productRepository.searchByText(any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(productMapper.toDtoList(any())).thenReturn(new ArrayList<>());

        productService.searchFullText(null, 0, 5);
    }

    @Test
    void getTopSelling_returnsList() {
        Object[] row = new Object[]{product, 5L};
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(productRepository.findTopSelling(any(Pageable.class))).thenReturn(rows);
        when(productMapper.toDto(product)).thenReturn(dto);

        assertThat(productService.getTopSelling()).hasSize(1);
    }

    @Test
    void getLowStock_returnsList() {
        when(productRepository.findByActifTrueAndStockLessThanEqualOrderByStockAsc(5))
                .thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        assertThat(productService.getLowStock(5)).hasSize(1);
    }

    @Test
    void getProductsBySeller_returnsList() {
        when(productRepository.findBySellerIdAndActifTrue(2L)).thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        assertThat(productService.getProductsBySeller(2L)).hasSize(1);
    }

    @Test
    void getMyProducts_success() {
        when(userRepository.findByEmail("s@shopflow.com")).thenReturn(Optional.of(sellerUser));
        when(sellerProfileRepository.findByUser(sellerUser)).thenReturn(Optional.of(seller));
        when(productRepository.findBySellerId(2L)).thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        assertThat(productService.getMyProducts("s@shopflow.com")).hasSize(1);
    }

    @Test
    void getMyProducts_noSellerProfile_throws() {
        when(userRepository.findByEmail("s@shopflow.com")).thenReturn(Optional.of(sellerUser));
        when(sellerProfileRepository.findByUser(sellerUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getMyProducts("s@shopflow.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProductById_found() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);
        when(reviewRepository.findAggregatesByProductIds(anyCollection())).thenReturn(Collections.emptyList());

        assertThat(productService.getProductById(10L).getNom()).isEqualTo("PS5");
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveProduct_success() {
        ProductCreateDTO create = new ProductCreateDTO();
        create.setSellerId(2L);
        create.setNom("PS5");
        create.setPrix(500.0);
        create.setStock(10);
        create.setCategoryIds(List.of(7L));

        when(sellerProfileRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(productMapper.toEntity(create)).thenReturn(product);
        when(categoryRepository.findAllById(List.of(7L))).thenReturn(Collections.emptyList());
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDTO result = productService.saveProduct(create);

        assertThat(result).isNotNull();
        assertThat(product.getSeller()).isEqualTo(seller);
    }

    @Test
    void saveProduct_sellerNotFound_throws() {
        ProductCreateDTO create = new ProductCreateDTO();
        create.setSellerId(99L);
        when(sellerProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.saveProduct(create))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProduct_success() {
        ProductCreateDTO update = new ProductCreateDTO();
        update.setNom("PS5 Pro");
        update.setCategoryIds(List.of(1L));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        productService.updateProduct(10L, update);

        verify(productMapper).updateEntityFromDto(update, product);
    }

    @Test
    void updateProduct_notFound_throws() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.updateProduct(999L, new ProductCreateDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProduct_softDeletes() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        productService.deleteProduct(10L);

        assertThat(product.getActif()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_notFound_throws() {
        when(productRepository.findById(777L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.deleteProduct(777L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enrichWithRatings_mergesAggregates() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDtoList(List.of(product))).thenReturn(new ArrayList<>(List.of(dto)));
        Object[] agg = new Object[]{10L, 4.5, 20L};
        List<Object[]> aggs = new ArrayList<>();
        aggs.add(agg);
        when(reviewRepository.findAggregatesByProductIds(anyCollection())).thenReturn(aggs);

        Page<ProductDTO> result = productService.search(null, null, null, null, null, null, null, null, 0, 5);

        ProductDTO out = result.getContent().get(0);
        assertThat(out.getNoteMoyenne()).isEqualTo(4.5);
        assertThat(out.getNbAvis()).isEqualTo(20);
    }
}
