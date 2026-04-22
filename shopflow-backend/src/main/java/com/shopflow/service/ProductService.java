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
import lombok.RequiredArgsConstructor;
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
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productMapper.toDtoList(productRepository.findAll());
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
        
        // Soft delete implementation:
        product.setActif(false);
        productRepository.save(product);
    }
}
