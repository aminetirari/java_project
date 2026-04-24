package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDTO {
    private Long id;
    private Long customerId;
    private String customerName; // Pratique pour l'affichage
    private Long productId;
    private String productNom;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateCreation;
    private Boolean approuve;
}
