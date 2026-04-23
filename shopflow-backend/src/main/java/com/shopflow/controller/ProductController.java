package com.shopflow.controller;

import com.shopflow.dto.ProductCreateDTO;
import com.shopflow.dto.ProductDTO;
import com.shopflow.service.FileStorageService;
import com.shopflow.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Double prixMin,
            @RequestParam(required = false) Double prixMax,
            @RequestParam(required = false) Boolean promo,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.search(q, categoryId, sellerId, prixMin, prixMax, promo, sort, page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.searchFullText(q, page, size));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductDTO>> getTopSelling() {
        return ResponseEntity.ok(productService.getTopSelling());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<List<ProductDTO>> getLowStock(
            @RequestParam(required = false, defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.getLowStock(threshold));
    }

    @GetMapping("/by-seller/{sellerId}")
    public ResponseEntity<List<ProductDTO>> getBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<List<ProductDTO>> getMyProducts(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(productService.getMyProducts(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        return new ResponseEntity<>(productService.saveProduct(productCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<ProductDTO> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        ProductDTO updatedProduct = productService.uploadImages(id, files);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/images/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // fallback
        }
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
