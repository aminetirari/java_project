package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewableItemDTO {
    private Long productId;
    private String productNom;
    private String productImage;
    private Long orderId;
    private String numeroCommande;
    private LocalDateTime dateCommande;
    private String orderStatus;
}
