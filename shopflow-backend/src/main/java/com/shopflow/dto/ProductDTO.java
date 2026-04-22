package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductDTO {
    private Long id;
    private Long sellerId;
    private String nom;
    private String description;
    private Double prix;
    private Double prixPromo;
    private Integer stock;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private List<Long> categoryIds;
    private List<String> images;
    private List<ProductVariantDTO> variantes;
    private Double noteMoyenne;
    private Integer nbAvis;
    private String sellerNom;
}
