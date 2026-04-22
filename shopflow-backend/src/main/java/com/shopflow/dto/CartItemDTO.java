package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private Long productId;
    private Long variantId;
    private Integer quantite;
    private String productNom;
    private Double prixUnitaire;
    private Double sousTotal;
}
