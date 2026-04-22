package com.shopflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ProductCreateDTO {
    @NotNull(message = "L'id du vendeur est obligatoire")
    private Long sellerId;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Min(value = 0, message = "Le prix doit être positif")
    private Double prix;

    @Min(value = 0, message = "Le prix promo doit être positif")
    private Double prixPromo;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    private List<Long> categoryIds;
    private List<String> images;
    private List<ProductVariantCreateDTO> variantes;
}
