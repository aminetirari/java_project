package com.shopflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCreateDTO {
    public static ReviewCreateDTO from(Integer note, String commentaire) {
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.note = note;
        dto.commentaire = commentaire;
        return dto;
    }
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private Integer note;

    @NotBlank(message = "Le commentaire ne peut pas être vide")
    private String commentaire;
}
