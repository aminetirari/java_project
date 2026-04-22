package com.shopflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemCreateDTO {
    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    private Long variantId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimale est de 1")
    private Integer quantite;
}
