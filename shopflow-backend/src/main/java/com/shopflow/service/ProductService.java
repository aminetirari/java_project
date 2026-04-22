package com.shopflow.service;

import com.shopflow.dto.ProductCreateDTO;
import com.shopflow.dto.ProductDTO;
import com.shopflow.entity.Category;
import com.shopflow.entity.Product;
import com.shopflow.entity.SellerProfile;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.ProductMapper;
import com.shopflow.repository.CategoryRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productMapper.toDtoList(productRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> search(String q, Long categoryId, Long sellerId, Double prixMin,
                                   Double prixMax, Boolean promo, String sort, int page, int size) {
        Sort springSort = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, springSort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isTrue(root.get("actif")));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("nom")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like)
                ));
            }
            if (categoryId != null) {
                Join<Object, Object> cat = root.join("categories");
                preds.add(cb.equal(cat.get("id"), categoryId));
                if (query != null) query.distinct(true);
            }
            if (sellerId != null) {
                preds.add(cb.equal(root.get("seller").get("id"), sellerId));
            }
            if (prixMin != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("prix"), prixMin));
            }
            if (prixMax != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("prix"), prixMax));
            }
            if (Boolean.TRUE.equals(promo)) {
                preds.add(cb.isNotNull(root.get("prixPromo")));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Page<Product> pageResult = productRepository.findAll(spec, pageable);
        List<ProductDTO> dtos = productMapper.toDtoList(pageResult.getContent());
        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchFullText(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productRepository.searchByText(q == null ? "" : q, pageable);
        return new PageImpl<>(productMapper.toDtoList(result.getContent()), pageable, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getTopSelling() {
        return productRepository.findTopSelling(PageRequest.of(0, 10)).stream()
                .map(row -> (Product) row[0])
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndActifTrue(sellerId).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getMyProducts(String email) {
        com.shopflow.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        SellerProfile seller = sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profil vendeur non trouvé"));
        return productRepository.findBySellerId(seller.getId()).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDTO saveProduct(ProductCreateDTO createDTO) {
        SellerProfile seller = sellerProfileRepository.findById(createDTO.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec id: " + createDTO.getSellerId()));

        Product product = productMapper.toEntity(createDTO);
        product.setSeller(seller);

        if (createDTO.getCategoryIds() != null && !createDTO.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(createDTO.getCategoryIds());
            product.setCategories(categories);
        }

        if (product.getVariantes() != null) {
            product.getVariantes().forEach(v -> v.setProduct(product));
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductCreateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));
        productMapper.updateEntityFromDto(dto, product);
        if (dto.getCategoryIds() != null) {
            product.setCategories(new ArrayList<>(categoryRepository.findAllById(dto.getCategoryIds())));
        }
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDTO uploadImages(Long id, MultipartFile[] files) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec id: " + id));

        List<String> fileDownloadUris = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = fileStorageService.storeFile(file);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/products/images/download/")
                    .path(fileName)
                    .toUriString();

            fileDownloadUris.add(fileDownloadUri);
        }

        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }
        product.getImages().addAll(fileDownloadUris);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec id: " + id));

        // Soft delete
        product.setActif(false);
        productRepository.save(product);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Order.desc("dateCreation"));
        switch (sort) {
            case "price_asc": return Sort.by(Sort.Order.asc("prix"));
            case "price_desc": return Sort.by(Sort.Order.desc("prix"));
            case "newest": return Sort.by(Sort.Order.desc("dateCreation"));
            case "name": return Sort.by(Sort.Order.asc("nom"));
            default: return Sort.by(Sort.Order.desc("dateCreation"));
        }
    }
}
