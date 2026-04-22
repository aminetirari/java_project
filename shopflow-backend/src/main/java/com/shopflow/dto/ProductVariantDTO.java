package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductVariantDTO {
    private Long id;
    private String attribut;
    private String valeur;
    private Integer stockSupplementaire;
    private Double prixDelta;
}
