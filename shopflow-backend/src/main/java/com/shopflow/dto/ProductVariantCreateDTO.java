package com.shopflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductVariantCreateDTO {
    @NotBlank(message = "L'attribut est obligatoire")
    private String attribut;

    @NotBlank(message = "La valeur est obligatoire")
    private String valeur;

    @Min(value = 0, message = "Le stock supplémentaire ne peut pas être négatif")
    private Integer stockSupplementaire;

    @Min(value = 0, message = "Le delta de prix doit être positif")
    private Double prixDelta;
}
